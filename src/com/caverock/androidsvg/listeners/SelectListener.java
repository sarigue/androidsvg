package com.caverock.androidsvg.listeners;

import android.annotation.SuppressLint;
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
	
	@SuppressLint("NewApi")
	@Override
	public boolean onTouch(View v, MotionEvent event)
	{
		if (mImageView == null) return v.onTouchEvent(event);
		if (mImageView.getSVG() == null) return v.onTouchEvent(event);
		if (mImageView.getRenderer() == null) return v.onTouchEvent(event);

		if (event.getPointerCount() > 1) return v.onTouchEvent(event);
		
		if (event.getActionMasked() != MotionEvent.ACTION_DOWN) return v.onTouchEvent(event);
		
		float[] point = new float[]{event.getX(), event.getY()};
		mImageView.getImageMatrixRevert().mapPoints(point);
		SVG.SvgElementBase o = mImageView.getSVG().getRootElement().getTopElement(point[0], point[1], mImageView.getRenderer());
		mBounds.left = 0;
		mBounds.right = 0;
		mBounds.top = 0;
		mBounds.bottom = 0;
		if (o != null)
		{
			if (o instanceof SVG.Image)
			{
				((SVG.Image)o).getBounds(mBounds, mImageView.getRenderer());
			}
			else if (o instanceof SVG.Group)
			{
				((SVG.Group)o).getBounds(mBounds, mImageView.getRenderer());
			}
			else if (o instanceof SVG.TextPositionedContainer)
			{
				((SVG.TextPositionedContainer)o).getBounds(mBounds, mImageView.getRenderer());
			}
			else if (o instanceof SVG.GraphicsElement)
			{
				((SVG.GraphicsElement)o).getBounds(mBounds, mImageView.getRenderer());
			}
		}
		
		onSelected(o, mBounds, mImageView);
		
		Log.d("ONTOUCH", String.valueOf(o) + " : " + mBounds.toShortString());

		if (mPaint == null)
		{
			return true;
		}
		
		if (mBounds.width() > 0)
		{
			mImageView.getImageMatrix().mapRect(mBounds);
			mImageView.setBounds(mBounds);
			mImageView.setBoundPaint(mPaint);
		}
		else
		{
			mImageView.setBounds(null);
			mImageView.setBoundPaint(null);
		}
		
		mImageView.invalidate();
		
		return true;
	}
	
	public void onSelected(SVG.SvgElementBase element, RectF bounds, SVGImageView imageView)
	{
		if (mListener != null)
		{
			mListener.onSelected(element, bounds, imageView);
		}
	}
	
}
