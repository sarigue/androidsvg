package com.caverock.androidsvg.extendedshapes;

import android.graphics.PointF;

import com.caverock.androidsvg.SVG.Length;
import com.caverock.androidsvg.SVG.Rect;
import com.caverock.androidsvg.SVG.Unit;
import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGAndroidRenderer;

public class Square extends Rect implements SVGExtendedShape
{

   protected PointF mStart = new PointF();
   
	public Square()
	{
		this.height = this.width;
	}
	
   public void setSide(float width)
   {
      setSide(new SVG.Length(width, SVG.Unit.px));
   }
   
	public void setSide(Length width)
	{
		this.width  = width;
		this.height = this.width;
	}
	

   @Override
   public boolean startShape(SVGAndroidRenderer renderer, float x, float y)
   {
      if (this.x == null)
      {
         this.x = new Length(x, Unit.px);
      }
      if (this.y == null)
      {
         this.y = new Length(y, Unit.px);
      }
      this.x.setValue(renderer, x);
      this.y.setValue(renderer, y);
      this.setSide(0);
      mStart.x = x;
      mStart.y = y;
      return true;
   }

   @Override
   public boolean changeShape(SVGAndroidRenderer renderer, float x, float y)
   {
      if (x - mStart.x < 0)
      {
         mStart.x = x;
         this.x.setValue(renderer, x);
      }
      if (y - mStart.y < 0)
      {
         mStart.y = y;
         this.y.setValue(renderer, y);
      }
      
      float w = (float)Math.max(x-mStart.x, y-mStart.y);
      this.width.setValue(renderer, w);
      return true;
   }

   @Override
   public boolean stopShape(SVGAndroidRenderer renderer, float x, float y)
   {
      return true;
   }
	
}
