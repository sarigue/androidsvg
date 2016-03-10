package com.caverock.androidsvg.extendedshapes;

import com.caverock.androidsvg.SVG.Polygon;

public class Triangle extends Polygon
{

	public Triangle()
	{
		this.points = new float[6];
	}
	
	public void setApex(int index, int x, int y)
	{
		this.points[(index%3)*2]   = x;
		this.points[(index%3)*2+1] = y;
	}
}
