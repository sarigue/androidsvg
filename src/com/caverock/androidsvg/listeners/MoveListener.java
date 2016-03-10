package com.caverock.androidsvg.listeners;

import android.annotation.SuppressLint;
import android.graphics.Matrix;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import com.caverock.androidsvg.SVGImageView;

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


}
