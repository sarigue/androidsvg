package com.caverock.androidsvg.extendedshapes;

import com.caverock.androidsvg.SVGAndroidRenderer;

public interface SVGExtendedShape
{
   public boolean startShape(SVGAndroidRenderer renderer, float x, float y);
   public boolean changeShape(SVGAndroidRenderer renderer, float x, float y);
   public boolean stopShape(SVGAndroidRenderer renderer, float x, float y);
}
