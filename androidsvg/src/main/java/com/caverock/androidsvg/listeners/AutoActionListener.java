package com.caverock.androidsvg.listeners;

import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGImageView;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;

public class AutoActionListener implements OnTouchListener, OnClickListener
{
   private PointF mPrevious1   = new PointF();
   private PointF mPrevious2   = new PointF();
   private PointF mCurrent     = new PointF();
   private RectF  selectdBound = new RectF();
   private SVGImageView mImageView = null;
   private SVG.SvgElementBase mSelectedElement = null;

   
   public AutoActionListener(SVGImageView imageView)
   {
      setImageView(imageView);
   }

   public AutoActionListener(SVGImageView imageView, Context context)
   {
      this(imageView);
   }

   public void setImageView(SVGImageView imageView)
   {
      mImageView = imageView;
   }
   
   @SuppressLint("ClickableViewAccessibility")
   @Override
   public boolean onTouch(View v, MotionEvent event)
   {
      mCurrent.x = event.getX();
      mCurrent.y = event.getY();
      
      float[] p = new float[]{event.getX(), event.getY()};
      mImageView.getImageMatrixRevert().mapPoints(p);
      
      if (mSelectedElement == null || mImageView == null)
      {
         return false;
      }
      
      if (event.getPointerCount() == 1 && selectdBound != null && selectdBound.contains(p[0], p[1]))
      {
         if (event.getActionMasked() == MotionEvent.ACTION_DOWN)
         {
            mPrevious1.x = mCurrent.x;
            mPrevious1.y = mCurrent.y;
            return true;
         }
         else if (event.getActionMasked() == MotionEvent.ACTION_MOVE)
         {
            MoveListener.TranslateElement(mSelectedElement, mCurrent.x-mPrevious1.x, mCurrent.y-mPrevious1.y);
            mPrevious1.x = mCurrent.x;
            mPrevious1.y = mCurrent.y;
            selectdBound = SelectListener.borderElement(mImageView, mSelectedElement, null);
            return true;
         }
      }
      else if (event.getPointerCount() == 2)
      {
         if (event.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN)
         {
            mPrevious1.x = event.getX(0);
            mPrevious1.y = event.getY(0);
            mPrevious2.x = event.getX(1);
            mPrevious2.y = event.getY(1);
            return true;
         }
         else if (event.getActionMasked() == MotionEvent.ACTION_MOVE)
         {
            float currD = (float)Math.sqrt(Math.pow(event.getX(0)-event.getX(1), 2) + Math.pow(event.getY(0)-event.getY(1), 2));
            float lastD = (float)Math.sqrt(Math.pow(mPrevious1.x-mPrevious2.x,   2) + Math.pow(mPrevious1.y-mPrevious2.y,   2));
            MoveListener.ZoomElement(mSelectedElement, currD/lastD, mImageView.getRenderer());
            mPrevious1.x = event.getX(0);
            mPrevious1.y = event.getY(0);
            mPrevious2.x = event.getX(1);
            mPrevious2.y = event.getY(1);
            selectdBound = SelectListener.borderElement(mImageView, mSelectedElement, null);
            return true;
         }
      }
      return false;
   }
   
   
   @Override
   public void onClick(View v)
   {
      mSelectedElement = SelectListener.getElementAt(mImageView, mCurrent.x, mCurrent.y);
      if (mSelectedElement == null)
      {
         mImageView.invalidate();
      }
      else
      {
         selectdBound = SelectListener.borderElement(mImageView, mSelectedElement, null);
      }
   }
   
}
