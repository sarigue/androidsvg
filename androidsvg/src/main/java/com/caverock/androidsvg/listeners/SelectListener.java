package com.caverock.androidsvg.listeners;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Paint.Style;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGImageView;

public class SelectListener implements OnTouchListener
{
	
	public static interface OnSelectedListener
	{
		public void onSelected(SVG.SvgElementBase svgObject, RectF bounds, SVGImageView imageView);
	}
		
	private SVGImageView mImageView = null;
	private RectF mBounds = new RectF();
	private Paint mPaint  = new Paint();
	private OnSelectedListener mListener = null;

	public SelectListener(SVGImageView imageView)
	{
		mImageView = imageView;
		mImageView.getOnDrawListenerList().add(new SVGImageView.OnDrawListener() {
	      @Override public void beforeTransform(Canvas canvas){};
	      @Override public void beforeDraw(Canvas canvas){};
	      @Override public void afterRestore(Canvas canvas){};
	      @Override
	      public void afterDraw(Canvas canvas)
	      {
	         if (mPaint != null && ! mBounds.isEmpty())
	         {
	            canvas.drawRect(mBounds, mPaint);
	         }
	      }
		});
		mPaint.setColor(Color.BLACK);
		mPaint.setStyle(Style.STROKE);
		mPaint.setPathEffect(new DashPathEffect(new float[]{10, 10}, 0));
		mPaint.setStrokeWidth(2);
	}

	public SelectListener(SVGImageView imageView, Paint paintBounds)
	{
		this(imageView);
		mPaint = paintBounds;
	}

	public void setOnSelectedListener(OnSelectedListener listener)
	{
		mListener = listener;
	}
	
	public Paint getBoundPaint()
	{
	   return mPaint;
	}

	
	@SuppressLint({"ClickableViewAccessibility"})
	@Override
	public boolean onTouch(View v, MotionEvent event)
	{
	   
		if (mImageView               == null) return v.onTouchEvent(event);
		if (mImageView.getSVG()      == null) return v.onTouchEvent(event);
		if (mImageView.getRenderer() == null) return v.onTouchEvent(event);

		if (event.getPointerCount() > 1)      return v.onTouchEvent(event);
		if (event.getActionMasked() != MotionEvent.ACTION_DOWN) return v.onTouchEvent(event);
		
		float[] point = new float[]{event.getX(), event.getY()};
		mImageView.getImageMatrixRevert().mapPoints(point);
		
		SVG.SvgElementBase selected = mImageView.getSVG().getRootElement().getTopElement(point[0], point[1], mImageView.getRenderer());
		
		if (selected != null)
		{
		   selected.getBounds(mBounds, mImageView.getRenderer());
	      onSelected(selected, mBounds, mImageView);
		}
      mImageView.invalidate();

		return true;
	}
	
	
	public void onSelected(SVG.SvgElementBase element, RectF bounds, SVGImageView imageView)
	{
	   Log.i("Selected", element.getClass().getSimpleName());
		if (mListener != null)
		{
			mListener.onSelected(element, bounds, imageView);
		}
	}
	
}