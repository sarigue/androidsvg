package com.caverock.androidsvg.extendedshapes;

import com.caverock.androidsvg.SVG.Length;
import com.caverock.androidsvg.SVG.Rect;

public class Square extends Rect
{

	public Square()
	{
		this.height = this.width;
	}
	
	public void setSide(Length width)
	{
		this.width  = width;
		this.height = this.width;
	}
	
}
