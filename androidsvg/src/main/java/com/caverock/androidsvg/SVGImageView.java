/*
   Copyright 2013 Paul LeBeau, Cave Rock Software Ltd.
   Copyright 2015 François RAOULT, Personal work.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package com.caverock.androidsvg;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;

import com.caverock.androidsvg.SVG.Box;
import com.caverock.androidsvg.SVG.Length;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Picture;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.PictureDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

/**
 * SVGImageView is a View widget that allows users to include SVG images in
 * their layouts.
 * 
 * It is implemented as a thin layer over {@code android.widget.ImageView}.
 * <p>
 * In its present form it has one significant limitation. It uses the
 * {@link SVG#renderToPicture()} method. That means that SVG documents that use
 * {@code <mask>} elements will not display correctly.
 * 
 * @attr ref R.styleable#SVGImageView_svg
 */
public class SVGImageView extends ImageView
{
   
   public static interface OnDrawListener{
      public static final int BEFORE_TRANSFORM = 1;
      public static final int BEFORE_DRAW      = 2;
      public static final int AFTER_DRAW       = 3;
      public static final int AFTER_RESTORE    = 4;
      public void onDraw(Canvas canvas, int moment);
   }
   
   private static Method      setLayerTypeMethod = null;

   private SVGAndroidRenderer   mRenderer          = null;
   private Matrix               mImageMatrixRevert = new Matrix();
   private SVG                  mSvg               = null;
   private List<OnDrawListener> mOnDrawListener    = null;

   {
      try {
         setLayerTypeMethod = View.class.getMethod("setLayerType", Integer.TYPE, Paint.class);
      }
      catch (NoSuchMethodException e) { /* do nothing */
      }
   }

   /**
    * Constructor
    * 
    * @param context
    */
   public SVGImageView(Context context)
   {
      super(context);
      init(null, 0);
   }

   /**
    * Constructor
    * 
    * @param context
    * @param attrs
    */
   public SVGImageView(Context context, AttributeSet attrs)
   {
      super(context, attrs, 0);
      init(attrs, 0);
   }

   /**
    * Constructor
    * 
    * @param context
    * @param attrs
    * @param defStyle
    */
   public SVGImageView(Context context, AttributeSet attrs, int defStyle)
   {
      super(context, attrs, defStyle);
      init(attrs, defStyle);
   }

   
   public List<OnDrawListener> getOnDrawListenerList()
   {
      if (mOnDrawListener == null)
      {
         mOnDrawListener = Collections.synchronizedList(new ArrayList<OnDrawListener>());
      }
      return mOnDrawListener;
   }

   /**
    * scale value
    * @return
    */
   public float getScaleValue()
   {
      float[] values = new float[9];
      this.getImageMatrix().getValues(values);
      float scaleX = values[Matrix.MSCALE_X];
      float skewY = values[Matrix.MSKEW_Y];
      float scale = (float) Math.sqrt(scaleX * scaleX + skewY * skewY);
      return scale;
   }

   /**
    * rotate value
    * @return
    */
   public float getRotateAngle()
   {
      float[] values = new float[9];
      this.getImageMatrix().getValues(values);
      return Math.round(Math.atan2(values[Matrix.MSKEW_X], values[Matrix.MSCALE_X]) * (180 / Math.PI));
   }

   /**
    * Initialize view
    * @param attrs
    * @param defStyle
    */
   @SuppressLint("NewApi")
   private void init(AttributeSet attrs, int defStyle)
   {
      if (attrs == null) return; // stop...

      // if (isInEditMode()) return;

      TypedArray a = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.SVGImageView, defStyle, 0);
      
      try 
      {
         String background = a.getString(R.styleable.SVGImageView_background);
         int backgrounddrawable = a.getResourceId(R.styleable.SVGImageView_background, -1);
         boolean usecheckerboard = a.getBoolean(R.styleable.SVGImageView_background, false);

         if (background != null) {
            if (background.startsWith("#")) {
               int backgroundcolor = a.getColor(R.styleable.SVGImageView_background, 0xFFFFFF);
               setBackgroundColor(backgroundcolor);
            }
            else if ("true".equals(background) && usecheckerboard) {
               Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.checkerboard);
               BitmapDrawable drawable = new BitmapDrawable(getResources(), bmp);
               drawable.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
               setBackground(drawable);
               // bmp.recycle();
               // bmp = null;
            }
            else if (backgrounddrawable != -1) {
               setBackgroundResource(backgrounddrawable);
            }
         }

         int resourceId = a.getResourceId(R.styleable.SVGImageView_svg, -1);
         if (resourceId != -1) {
            setImageResource(resourceId);
            return;
         }

         String url = a.getString(R.styleable.SVGImageView_svg);
         if (url != null) {
            Uri uri = Uri.parse(url);
            if (internalSetImageURI(uri, false))  return;
            // Last chance, try loading it as an asset filename
            setImageAsset(url);
         }

      }
      finally
      {
         a.recycle();
      }
      
   }

   
   @Override
   protected void onDraw(Canvas canvas)
   {
      getImageMatrix().invert(mImageMatrixRevert);
      callOnDrawListener(canvas, OnDrawListener.BEFORE_TRANSFORM);
      if (mSvg != null)
      {
         canvas.save();
         canvas.setMatrix(getImageMatrix());
         callOnDrawListener(canvas, OnDrawListener.BEFORE_DRAW);
         getOnDrawRenderer(canvas).renderDocument(mSvg, null, null, true);
         callOnDrawListener(canvas, OnDrawListener.AFTER_DRAW);
         canvas.restore();
      }
      else
      {
         super.onDraw(canvas);
      }
      callOnDrawListener(canvas, OnDrawListener.AFTER_RESTORE);
   }

   
   @SuppressLint("WrongCall")
   protected void callOnDrawListener(Canvas canvas, int moment)
   {
      if (mOnDrawListener == null || mOnDrawListener.isEmpty())
      {
         return;
      }
      synchronized (mOnDrawListener)
      {
         int size = mOnDrawListener.size();
         int i    = 0;
         for(i=0; i < size ; i++)
         {
            try
            {
               OnDrawListener listener = mOnDrawListener.get(i);
               listener.onDraw(canvas, moment);
            }
            catch(ConcurrentModificationException e)
            {
            }
            catch(IndexOutOfBoundsException ee)
            {
               OnDrawListener listener = mOnDrawListener.get(i-1);
               listener.onDraw(canvas, moment);
            }
            catch(Exception e)
            {
            }
         }
      }
   }
   
   private SVGAndroidRenderer getOnDrawRenderer(Canvas canvas)
   {
      if (mRenderer == null) {
         mRenderer = getNewRenderer(mSvg);
      }
      mRenderer.setCanvas(canvas);
      return mRenderer;
   }

   protected SVGAndroidRenderer getNewRenderer(SVG svg)
   {

      SVG.Svg rootElement = svg.getRootElement();
      Length width = rootElement.width;
      if (width != null) {
         float w = width.floatValue(svg.getRenderDPI());
         float h;
         Box rootViewBox = rootElement.viewBox;
         if (rootViewBox != null) {
            h = w * rootViewBox.height / rootViewBox.width;
         }
         else {
            Length height = rootElement.height;
            h = (height != null) ? height.floatValue(svg.getRenderDPI()) : w;
         }
         return new SVGAndroidRenderer(null, new Box(0f, 0f, w, h),
               svg.getRenderDPI());
      }

      return null;
   }

   public SVGAndroidRenderer getRenderer()
   {
      if (mRenderer == null)
         mRenderer = getNewRenderer(mSvg);
      return mRenderer;
   }

   public Matrix getImageMatrixRevert()
   {
      this.getImageMatrix().invert(mImageMatrixRevert);
      return mImageMatrixRevert;
   }

   public SVG getSVG()
   {
      return mSvg;
   }

   /**
    * Directly set the SVG.
    */
   public void setSVG(SVG mysvg)
   {
      if (mysvg == null)
         throw new IllegalArgumentException("Null value passed to setSVG()");

      setSoftwareLayerType();
      setImageDrawable(new PictureDrawable(mysvg.renderToPicture()));
      mSvg = mysvg;
      mRenderer = getNewRenderer(mSvg);
   }

   /**
    * Load an SVG image from the given resource id.
    */
   @Override
   public void setImageResource(int resourceId)
   {
      new LoadResourceTask().execute(resourceId);
   }

   /**
    * Load an SVG image from the given resource URI.
    */
   @Override
   public void setImageURI(Uri uri)
   {
      internalSetImageURI(uri, true);
   }

   /**
    * Load an SVG image from the given asset filename.
    */
   public void setImageAsset(String filename)
   {
      new LoadAssetTask().execute(filename);
   }

   /*
    * Attempt to set a picture from a Uri. Return true if it worked.
    */
   private boolean internalSetImageURI(Uri uri, boolean isDirectRequestFromUser)
   {
      InputStream is = null;
      try {
         is = getContext().getContentResolver().openInputStream(uri);
      }
      catch (FileNotFoundException e) {
         if (isDirectRequestFromUser)
            Log.e("SVGImageView", "File not found: " + uri);
         return false;
      }

      new LoadURITask().execute(is);
      return true;
   }

   // ===============================================================================================

   private class LoadResourceTask extends AsyncTask<Integer, Integer, Picture>
   {
      protected Picture doInBackground(Integer... resourceId)
      {
         try {
            SVG svg = SVG.getFromResource(getContext(), resourceId[0]);
            mSvg = svg;
            return svg.renderToPicture();
         }
         catch (SVGParseException e) {
            Log.e("SVGImageView",
                  String.format("Error loading resource 0x%x: %s", resourceId,
                        e.getMessage()));
         }
         return null;
      }

      protected void onPostExecute(Picture picture)
      {
         if (picture != null) {
            setSoftwareLayerType();
            setImageDrawable(new PictureDrawable(picture));
         }
      }
   }

   private class LoadAssetTask extends AsyncTask<String, Integer, Picture>
   {
      protected Picture doInBackground(String... filename)
      {
         try {
            SVG svg = SVG.getFromAsset(getContext().getAssets(), filename[0]);
            mSvg = svg;
            return svg.renderToPicture();
         }
         catch (SVGParseException e) {
            Log.e("SVGImageView",
                  "Error loading file " + filename + ": " + e.getMessage());
         }
         catch (FileNotFoundException e) {
            Log.e("SVGImageView", "File not found: " + filename);
         }
         catch (IOException e) {
            Log.e("SVGImageView", "Unable to load asset file: " + filename, e);
         }
         return null;
      }

      protected void onPostExecute(Picture picture)
      {
         if (picture != null) {
            setSoftwareLayerType();
            setImageDrawable(new PictureDrawable(picture));
         }
      }
   }

   private class LoadURITask extends AsyncTask<InputStream, Integer, Picture>
   {
      protected Picture doInBackground(InputStream... is)
      {
         try {
            SVG svg = SVG.getFromInputStream(is[0]);
            mSvg = svg;
            return svg.renderToPicture();
         }
         catch (SVGParseException e) {
            Log.e("SVGImageView", "Parse error loading URI: " + e.getMessage());
         }
         finally {
            try {
               is[0].close();
            }
            catch (IOException e) { /* do nothing */
            }
         }
         return null;
      }

      protected void onPostExecute(Picture picture)
      {
         if (picture != null) {
            setSoftwareLayerType();
            setImageDrawable(new PictureDrawable(picture));
         }
      }
   }

   // ===============================================================================================

   /*
    * Use reflection to call an API 11 method from this library (which is
    * configured with a minSdkVersion of 8)
    */
   private void setSoftwareLayerType()
   {
      if (setLayerTypeMethod == null)
         return;

      try {
         int LAYER_TYPE_SOFTWARE = View.class.getField("LAYER_TYPE_SOFTWARE")
               .getInt(new View(getContext()));
         setLayerTypeMethod.invoke(this, LAYER_TYPE_SOFTWARE, null);
      }
      catch (Exception e) {
         Log.w("SVGImageView", "Unexpected failure calling setLayerType", e);
      }
   }
}
