package com.caverock.androidsvg.listeners;

import android.annotation.SuppressLint;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGAndroidRenderer;
import com.caverock.androidsvg.SVGImageView;
import com.caverock.androidsvg.SVGMeasure;

public class MoveListener implements OnTouchListener
{

	private interface viewChanger
	{
		public boolean start(MotionEvent event);
		public boolean move(MotionEvent event);
		public boolean stop(MotionEvent event);
	}

	private SVGImageView mImageView   = null;
	private viewChanger  mViewChanger = null;
	
	
	public MoveListener(SVGImageView imageView)
	{
		mImageView = imageView;
	}

	@SuppressLint({"ClickableViewAccessibility"})
	@Override
	public boolean onTouch(View v, MotionEvent event)
	{
		if (mImageView == null) return v.onTouchEvent(event);
		if (mImageView.getSVG() == null) return v.onTouchEvent(event);
		if (mImageView.getRenderer() == null) return v.onTouchEvent(event);

		int pointerCount = event.getPointerCount();
		int actionMasked = event.getActionMasked();

		switch (actionMasked & MotionEvent.ACTION_MASK)
		{
			// Select action and start it
			case MotionEvent.ACTION_DOWN:
			case MotionEvent.ACTION_POINTER_DOWN:
				//if (mViewChanger == null)
				{
					if      (pointerCount == 1) mViewChanger = new Move();
					else if (pointerCount == 2) mViewChanger = new Zoom();
				}
				if (mViewChanger != null) return mViewChanger.start(event);
				else                      return v.onTouchEvent(event);
				
			// Do action
			case MotionEvent.ACTION_MOVE:
				if (mViewChanger != null) return mViewChanger.move(event);
				else                      return v.onTouchEvent(event);
				
			// End action
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_POINTER_UP:
				if (mViewChanger != null)
				{
					boolean result = mViewChanger.stop(event); 
					mViewChanger = null;
					return result;
				}
				else
				{
					return v.onTouchEvent(event);
				}
			
			default:
				return v.onTouchEvent(event);
		}
	}
	// END OF LISTENER
	
	// ------------------------------------------------------------------
	// ------------------------------------------------------------------

	/**
	 * Move picture
	 * 
	 * @author fraoult
	 *
	 */
	private class Move implements viewChanger
	{
		float mX = 0;
		float mY = 0;
		float mImgW = 0;
		float mImgH = 0;
		
		@Override
		public boolean start(MotionEvent event)
		{
			mX = event.getX();
			mY = event.getY();
			float scale = mImageView.getScaleValue();
			mImgW = mImageView.getSVG().getDocumentWidth() * scale;
			mImgH = mImageView.getSVG().getDocumentHeight() * scale;
			return true;
		}

		@Override
		public boolean move(MotionEvent event)
		{
         float[] values = new float[9];
         mImageView.getImageMatrix().getValues(values);
         
			float x = event.getX();
			float y = event.getY();
         float w  = mImageView.getWidth();
         float h  = mImageView.getHeight();
         float dx = x-mX;
         float dy = y-mY;
			float tx = values[Matrix.MTRANS_X];
			float ty = values[Matrix.MTRANS_Y];
			
			boolean smallerX = mImgW <= w;
			boolean smallerY = mImgH <= h;
			
			float right  = tx + mImgW;
			float bottom = ty + mImgH;
			
			if (smallerX)
			{
	              if (dx > 0 && right + dx > w)   dx = w - mImgW - tx;
	         else if (dx < 0 && tx + dx < 0)      dx = - tx;
			}
			else
			{
                 if (dx > 0 && tx + dx > 0)    dx = - tx;
            else if (dx < 0 && right + dx < w) dx = w - mImgW - tx;
			}
			
			if (smallerY)
			{
	              if (dy >  0 && bottom + dy > h) dy = h - mImgH - ty;
	         else if (dy < 0 && ty + dy < 0)      dy = - ty;
			}
			else
			{
                 if (dy > 0 && ty + dy > 0)      dy = - ty;
            else if (dy <  0 && bottom + dy < h) dy = h - mImgH - ty;
			}

         if (dx != 0 || dy != 0)
         {
            mImageView.getImageMatrix().postTranslate(dx, dy);
            mImageView.invalidate();
            mX += dx;
            mY += dy;
         }
         
			return true;
		}

		@Override
		public boolean stop(MotionEvent event)
		{
			return true;
		}
		
	}
	
	
	// ------------------------------------------------------------------
	// ------------------------------------------------------------------

	/**
	 * Zoom picture
	 * 
	 * @author fraoult
	 *
	 */
	private class Zoom implements viewChanger
	{
		float mD = 0;

		@Override
		public boolean start(MotionEvent event)
		{
			float x1 = event.getX(0);
			float y1 = event.getY(0);
			float x2 = event.getX(1);
			float y2 = event.getY(1);
			mD = (float)Math.sqrt((x2-x1)*(x2-x1) + (y2-y1)*(y2-y1));
			return true;
		}

		@Override
		public boolean move(MotionEvent event)
		{
			float x1 = event.getX(0);
			float y1 = event.getY(0);
			float x2 = event.getX(1);
			float y2 = event.getY(1);
         float d = (float)Math.sqrt((x2-x1)*(x2-x1) + (y2-y1)*(y2-y1));
         mImageView.getImageMatrix().postScale(d/mD, d/mD, (x1+x2)/2, (y1+y2)/2);
         mImageView.invalidate();
         mD = d;
			return true;
		}

		@Override
		public boolean stop(MotionEvent event)
		{
			return false;
		}

	}
	
	public static void TranslateElement(SVG.SvgElementBase element, float dx, float dy)
	{
	   if (element == null)
	   {
	      return;
	   }
	   
	   if (element instanceof SVG.Group)
	   {
	      if (((SVG.Group)element).transform == null)
	      {
	         ((SVG.Group)element).transform = new Matrix();
	      }
         ((SVG.Group)element).transform.postTranslate(dx, dy);
	   }
	   else if (element instanceof SVG.Image)
      {
         if (((SVG.Image)element).transform == null)
         {
            ((SVG.Image)element).transform = new Matrix();
         }
         ((SVG.Image)element).transform.postTranslate(dx, dy);
      }
      else if (element instanceof SVG.Text)
      {
         if (((SVG.Text)element).transform == null)
         {
            ((SVG.Text)element).transform = new Matrix();
         }
         ((SVG.Text)element).transform.postTranslate(dx, dy);
      }
      else if (element instanceof SVG.GraphicsElement)
      {
         if (((SVG.GraphicsElement)element).transform == null)
         {
            ((SVG.GraphicsElement)element).transform = new Matrix();
         }
         ((SVG.GraphicsElement)element).transform.postTranslate(dx, dy);
      }
	}

	public static void MoveElementTo(SVG.SvgElementBase element, float x, float y, SVGAndroidRenderer renderer)
	{
	   if (element == null)
	   {
	      return;
	   }

	   float[] position = SVGMeasure.getPosition(element, renderer);
      float dx = - position[0] + x;
      float dy = - position[1] + y;
      TranslateElement(element, dx, dy);
   }

	  
	public static void ZoomElement(SVG.SvgElementBase element, float scale, SVGAndroidRenderer renderer)
	{
	   if (element == null)
      {
         return;
      }

	   RectF bounds = new RectF();
	   SVGMeasure.getBounds(element, bounds, renderer);
	   
	   if (bounds.isEmpty())
	   {
	      return;
	   }
	     
	   if (element instanceof SVG.Group)
	   {
         if (((SVG.Group)element).transform == null)
         {
            ((SVG.Group)element).transform = new Matrix();
         }
	      ((SVG.Group)element).transform.postScale(scale, scale, bounds.centerX(), bounds.centerY());
	   }
	   else if (element instanceof SVG.Image)
	   {
         if (((SVG.Image)element).transform == null)
         {
            ((SVG.Image)element).transform = new Matrix();
         }
	      ((SVG.Image)element).transform.postScale(scale, scale, bounds.centerX(), bounds.centerY());
	   }  
	   else if (element instanceof SVG.Text)
	   {
         if (((SVG.Text)element).transform == null)
         {
            ((SVG.Text)element).transform = new Matrix();
         }
	      ((SVG.Text)element).transform.postScale(scale, scale, bounds.centerX(), bounds.centerY());
	   }
	   else if (element instanceof SVG.GraphicsElement)
	   {
         if (((SVG.GraphicsElement)element).transform == null)
         {
            ((SVG.GraphicsElement)element).transform = new Matrix();
         }
	      ((SVG.GraphicsElement)element).transform.postScale(scale, scale, bounds.centerX(), bounds.centerY());
	   }
	} 
	  

}
