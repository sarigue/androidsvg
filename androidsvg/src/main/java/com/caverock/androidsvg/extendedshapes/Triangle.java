package com.caverock.androidsvg.extendedshapes;

import com.caverock.androidsvg.SVG.Polygon;
import com.caverock.androidsvg.SVGAndroidRenderer;

public class Triangle extends Polygon implements SVGExtendedShape
{

	public Triangle()
	{
		this.points = new float[6];
	}
	
	public void setApex(int index, float x, float y)
	{
		this.points[(index%3)*2]   = x;
		this.points[(index%3)*2+1] = y;
	}

	public float[] getApex(int index)
	{
	   return new float[]{this.getApexX(index), this.getApexY(index)};
	}
	
	public float getApexX(int index)
	{
	   return this.points[(index%3)*2];
	}

	public float getApexY(int index)
	{
	   return this.points[(index%3)*2+1];
	}

   @Override
   public boolean startShape(SVGAndroidRenderer renderer, float x, float y)
   {
      this.setApex(0, x, y);
      this.setApex(1, x, y);
      this.setApex(2, x, y);
      return true;
   }

   @Override
   public boolean changeShape(SVGAndroidRenderer renderer, float x, float y)
   {

      float sommetX = this.getApexX(0);
      this.setApex(1, x, y);
      this.setApex(2, sommetX - (x-sommetX), y);
      return true;
   }

   @Override
   public boolean stopShape(SVGAndroidRenderer renderer, float x, float y)
   {
      return true;
   }
}
