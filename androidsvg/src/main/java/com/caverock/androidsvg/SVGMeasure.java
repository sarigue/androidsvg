/*
   Copyright 2013 Paul LeBeau, Cave Rock Software Ltd.
   Copyright 2015 François RAOULT, Personal work.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package com.caverock.androidsvg;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Set;

import android.graphics.Matrix;
import android.graphics.PathMeasure;
import android.graphics.RectF;

/**
 * The boundaries getter part of AndroidSVG.
 * 
 */
public class SVGMeasure
{

   /**
    * Get bounds
    * 
    * @param element
    * @param bound
    * @param renderer
    */
   public static void getBounds(SVG.SvgElement element, RectF bound, SVGAndroidRenderer renderer)
   {
      bound.left = 0;
      bound.right = 0;
      bound.top = 0;
      bound.bottom = 0;
      if (element instanceof SVG.Image) {
         getBounds(((SVG.Image) element), bound, renderer);
      }
      else if (element instanceof SVG.GraphicsElement) {
         getBounds(((SVG.GraphicsElement) element), bound, renderer);
      }
      else if (element instanceof SVG.TextPositionedContainer) {
         getBounds(((SVG.TextPositionedContainer) element), bound, renderer);
      }
      else if (element instanceof SVG.Switch) {
         getBounds(((SVG.Switch) element), bound, renderer);
      }
      else if (element instanceof SVG.Use) {
         getBounds(((SVG.Use) element), bound, renderer);
      }
      else if (element instanceof SVG.Group) {
         getBounds(((SVG.Group) element), bound, renderer);
      }
   }

   /**
    * Get bounds
    * 
    * @param element
    * @param bound
    * @param renderer
    */
   public static void getBounds(SVG.GraphicsElement element, RectF bound, SVGAndroidRenderer renderer)
   {
      if (element instanceof SVG.Line) {
         getBounds((SVG.Line) element, bound, renderer);
      }
      else if (element instanceof SVG.Path) {
         getBounds((SVG.Path) element, bound, renderer);
      }
      else if (element instanceof SVG.PolyLine) {
         getBounds((SVG.PolyLine) element, bound, renderer);
      }
      else if (element instanceof SVG.Rect) {
         getBounds((SVG.Rect) element, bound, renderer);
      }
      else if (element instanceof SVG.Circle) {
         getBounds((SVG.Circle) element, bound, renderer);
      }
      else if (element instanceof SVG.Ellipse) {
         getBounds((SVG.Ellipse) element, bound, renderer);
      }
   }   
   
   public static void getBounds(SVG.Switch sw, RectF bound, SVGAndroidRenderer renderer)
   {
      bound.set(0, 0, 0, 0);
      SVG.SvgElement child = getSwitchRenderedElement(sw, renderer);
      if (child != null)
      {
         getBounds(child, bound, renderer);
      }
   }
   
   public static void getBounds(SVG.Use use, RectF bound, SVGAndroidRenderer renderer)
   {
      SVG.SvgObject ref = use.document.resolveIRI(use.href);
      if (ref instanceof SVG.SvgElement)
      {
         getBounds((SVG.SvgElement)ref, bound, renderer);
      }
      Matrix m = new Matrix();
      if (use.x != null)
      {
         m.postTranslate(-bound.left + use.x.floatValue(renderer), 0);
      }
      if (use.y != null)
      {
         m.postTranslate(0, -bound.top + use.y.floatValue(renderer));
      }
      m.mapRect(bound);
      if (use.transform != null)
      {
         use.transform.mapRect(bound);
      }
      
   }

   /**
    * Get bounds
    * 
    * @param group
    * @param bound
    * @param renderer
    */
   public static void getBounds(SVG.Group group, RectF bound, SVGAndroidRenderer renderer)
   {
      bound.left = 0;
      bound.right = 0;
      bound.top = 0;
      bound.bottom = 0;
      RectF childBounds = new RectF();
      for (SVG.SvgObject child : group.children) {
         if (child instanceof SVG.SvgElement) {
            getBounds(((SVG.SvgElement) child), childBounds, renderer);
            bound.union(childBounds);
         }
      }
      if (group.transform != null) {
         group.transform.mapRect(bound);
      }
   }

   /**
    * Get bounds
    * 
    * @param path
    * @param bound
    * @param renderer
    */
   public static void getBounds(SVG.Path path, RectF bound, SVGAndroidRenderer renderer)
   {
      SVGAndroidRenderer.PathConverter converter = new SVGAndroidRenderer.PathConverter(
            path.d);
      android.graphics.Path p = converter.getPath();
      p.computeBounds(bound, true);
      if (path.transform != null) {
         path.transform.mapRect(bound);
      }
   }

   /**
    * Get bounds
    * 
    * @param rect
    * @param bound
    * @param renderer
    */
   public static void getBounds(SVG.Rect rect, RectF bound, SVGAndroidRenderer renderer)
   {
      bound.left = (rect.x == null) ? 0 : rect.x.floatValue(renderer);
      bound.top = (rect.y == null) ? 0 : rect.y.floatValue(renderer);
      bound.right = bound.left
            + (rect.width == null ? 0 : rect.width.floatValue(renderer));
      bound.bottom = bound.top
            + (rect.height == null ? 0 : rect.height.floatValue(renderer));
      if (rect.transform != null) {
         rect.transform.mapRect(bound);
      }
   }

   /**
    * Get bounds
    * 
    * @param circle
    * @param bound
    * @param renderer
    */
   public static void getBounds(SVG.Circle circle, RectF bound, SVGAndroidRenderer renderer)
   {
      float r = (circle.r == null) ? 0f : circle.r.floatValue(renderer);
      bound.left = circle.cx == null ? 0 : circle.cx.floatValue(renderer) - r;
      bound.top = circle.cy == null ? 0 : circle.cy.floatValue(renderer) - r;
      bound.right = bound.left + 2 * r;
      bound.bottom = bound.top + 2 * r;
      if (circle.transform != null) {
         circle.transform.mapRect(bound);
      }
   }

   /**
    * Get bounds
    * 
    * @param ellipse
    * @param bound
    * @param renderer
    */
   public static void getBounds(SVG.Ellipse ellipse, RectF bound, SVGAndroidRenderer renderer)
   {
      float rx = ellipse.rx == null ? 0 : ellipse.rx.floatValue(renderer);
      float ry = ellipse.ry == null ? 0 : ellipse.ry.floatValue(renderer);
      bound.left = ellipse.cx == null ? 0 : ellipse.cx.floatValue(renderer)
            - rx;
      bound.top = ellipse.cy == null ? 0 : ellipse.cy.floatValue(renderer) - ry;
      bound.right = bound.left + 2 * rx;
      bound.bottom = bound.top + 2 * ry;
      if (ellipse.transform != null) {
         ellipse.transform.mapRect(bound);
      }
   }

   /**
    * Get bounds
    * 
    * @param line
    * @param bound
    * @param renderer
    */
   public static void getBounds(SVG.Line line, RectF bound, SVGAndroidRenderer renderer)
   {
      bound.left = line.x1 == null ? 0 : line.x1.floatValue(renderer);
      bound.top = line.y1 == null ? 0 : line.y1.floatValue(renderer);
      bound.right = line.x2 == null ? 0 : line.x2.floatValue(renderer);
      bound.bottom = line.y2 == null ? 0 : line.y2.floatValue(renderer);
      bound.sort();
      if (line.transform != null) {
         line.transform.mapRect(bound);
      }
   }

   /**
    * Get bounds
    * 
    * @param polyline
    * @param bound
    * @param renderer
    */
   public static void getBounds(SVG.PolyLine polyline, RectF bound, SVGAndroidRenderer renderer)
   {
      bound.left = 0;
      bound.top = 0;
      bound.right = 0;
      bound.bottom = 0;
      if (polyline.points != null) {
         for (int i = 0; i < polyline.points.length - 1; i = i + 2) {
            float x = polyline.points[i];
            float y = polyline.points[i + 1];
            if (x < bound.left)
               bound.left = x;
            if (x > bound.right)
               bound.right = x;
            if (y < bound.top)
               bound.top = y;
            if (y > bound.bottom)
               bound.bottom = y;
         }
      }
      if (polyline.transform != null) {
         polyline.transform.mapRect(bound);
      }
   }

   /**
    * Get bounds
    * 
    * @param text
    * @param bound
    * @param renderer
    */
   public static void getBounds(SVG.TextPositionedContainer text, RectF bound, SVGAndroidRenderer renderer)
   {
      if (text.boundingBox == null) {
         SVGAndroidRenderer.TextBoundsCalculator proc = renderer.new TextBoundsCalculator(
               text.x.get(0).floatValueX(renderer),
               text.y.get(0).floatValueY(renderer)
         );
         renderer.enumerateTextSpans(text, proc);
         text.boundingBox = new SVG.Box(proc.bbox.left, proc.bbox.top, proc.bbox.width(), proc.bbox.height());
      }
      bound.top = text.boundingBox.minY;
      bound.left = text.boundingBox.minX;
      bound.right = bound.left + text.boundingBox.width;
      bound.bottom = bound.top + text.boundingBox.height;

      if (text instanceof SVG.Text) {
         if (((SVG.Text) text).transform != null) {
            ((SVG.Text) text).transform.mapRect(bound);
         }
      }

   }

   /**
    * Get bounds
    * 
    * @param image
    * @param bound
    * @param renderer
    */
   public static void getBounds(SVG.Image image, RectF bound, SVGAndroidRenderer renderer)
   {
      bound.left = image.x.floatValue(renderer);
      bound.top = image.y.floatValue(renderer);
      bound.right = bound.left + image.width.floatValue(renderer);
      bound.bottom = bound.top + image.height.floatValue(renderer);
      if (image.transform != null) {
         image.transform.mapRect(bound);
      }
   }

   /**
    * Get bounds
    * 
    * @param element
    * @param points
    * @param renderer
    */
   public static float[] getPoints(SVG.SvgElement element, SVGAndroidRenderer renderer)
   {
      if (element instanceof SVG.Image) {
         return getPoints(((SVG.Image) element), renderer);
      }
      else if (element instanceof SVG.GraphicsElement) {
         return getPoints(((SVG.GraphicsElement) element), renderer);
      }
      else if (element instanceof SVG.TextPositionedContainer) {
         return getPoints(((SVG.TextPositionedContainer) element), renderer);
      }
      else if (element instanceof SVG.Switch) {
         return getPoints(((SVG.Switch) element), renderer);
      }
      else if (element instanceof SVG.Use) {
         return getPoints(((SVG.Use) element), renderer);
      }
      else if (element instanceof SVG.Group) {
         return getPoints(((SVG.Group) element), renderer);
      }
      return null;
   }

   /**
    * Get bounds
    * 
    * @param element
    * @param points
    * @param renderer
    */
   public static float[] getPoints(SVG.GraphicsElement element, SVGAndroidRenderer renderer)
   {
      if (element instanceof SVG.Line) {
         return getPoints((SVG.Line) element, renderer);
      }
      else if (element instanceof SVG.Path) {
         return getPoints((SVG.Path) element, renderer);
      }
      else if (element instanceof SVG.PolyLine) {
         return getPoints((SVG.PolyLine) element, renderer);
      }
      else if (element instanceof SVG.Rect) {
         return getPoints((SVG.Rect) element, renderer);
      }
      else if (element instanceof SVG.Circle) {
         return getPoints((SVG.Circle) element, renderer);
      }
      else if (element instanceof SVG.Ellipse) {
         return getPoints((SVG.Ellipse) element, renderer);
      }
      return null;
   }

   public static float[] getPoints(SVG.Switch sw, SVGAndroidRenderer renderer)
   {
      SVG.SvgElement child = getSwitchRenderedElement(sw, renderer);
      if (child != null)
      {
         return getPoints(child, renderer);
      }
      return null;
   }
   
   public static float[] getPoints(SVG.Use use, SVGAndroidRenderer renderer)
   {
      float[] points = null;
      SVG.SvgObject ref = use.document.resolveIRI(use.href);
      if (ref instanceof SVG.SvgElement)
      {
         points = getPoints((SVG.SvgElement)ref, renderer);
      }
      if (points == null)
      {
         return null;
      }
      
      float x = Float.MAX_VALUE;
      float y = Float.MAX_VALUE;
      
      for(int i=0; i+1 < points.length; i++)
      {
         x = Math.min(x, points[i]);
         y = Math.min(y, points[i+1]);
      }
      
      Matrix m = new Matrix();
      if (use.x != null)
      {
         m.postTranslate(-x + use.x.floatValue(renderer), 0);
      }
      if (use.y != null)
      {
         m.postTranslate(0, -y + use.y.floatValue(renderer));
      }
      m.mapPoints(points);
      if (use.transform != null)
      {
         use.transform.mapPoints(points);
      }
      
      return points;
   }

   /**
    * Get bounds
    * 
    * @param group
    * @param bound
    * @param renderer
    */
   public static float[] getPoints(SVG.Group group, SVGAndroidRenderer renderer)
   {

      ArrayList<Float> list = new ArrayList<Float>();
      for (SVG.SvgObject child : group.children) {
         if (child instanceof SVG.SvgElement) {
            float[] childPoints = getPoints(((SVG.SvgElement) child), renderer);
            for (float f : childPoints) {
               list.add(f);
            }
         }
      }
      float[] points = new float[list.size()];
      for (int i = 0; i < list.size(); i++) {
         points[i] = (float) (list.get(i) == null ? 0 : list.get(0));
      }
      if (group.transform != null) {
         group.transform.mapPoints(points);
      }

      return points;
   }

   /**
    * Get bounds
    * 
    * @param path
    * @param bound
    * @param renderer
    */
   public static float[] getPoints(SVG.Path path, SVGAndroidRenderer renderer)
   {
      SVGAndroidRenderer.PathConverter converter = new SVGAndroidRenderer.PathConverter(path.d);
      android.graphics.Path p = converter.getPath();
      PathMeasure pm = new PathMeasure(p, false);
      float[] start = new float[2];
      float[] end = new float[2];
      pm.getPosTan(0, start, null);
      pm.getPosTan(pm.getLength(), end, null);
      float[] points = { start[0], start[1], end[0], end[1] };
      if (path.transform != null) {
         path.transform.mapPoints(points);
      }
      return points;
   }

   /**
    * Get bounds
    * 
    * @param rect
    * @param bound
    * @param renderer
    */
   public static float[] getPoints(SVG.Rect rect, SVGAndroidRenderer renderer)
   {
      float left = (rect.x == null) ? 0 : rect.x.floatValue(renderer);
      float top = (rect.y == null) ? 0 : rect.y.floatValue(renderer);
      float right = left + (rect.width == null ? 0 : rect.width.floatValue(renderer));
      float bottom = top + (rect.height == null ? 0 : rect.height.floatValue(renderer));

      float[] points = new float[] { left, top, right, top, left, bottom,
            right, bottom };

      if (rect.transform != null) {
         rect.transform.mapPoints(points);
      }

      return points;
   }

   /**
    * Get bounds
    * 
    * @param circle
    * @param bound
    * @param renderer
    */
   public static float[] getPoints(SVG.Circle circle, SVGAndroidRenderer renderer)
   {
      float r = (circle.r == null) ? 0f : circle.r.floatValue(renderer);
      float left = circle.cx == null ? 0 : circle.cx.floatValue(renderer) - r;
      float top = circle.cy == null ? 0 : circle.cy.floatValue(renderer) - r;
      float right = left + 2 * r;
      float bottom = top + 2 * r;

      float[] points = new float[] { left, (top + bottom) / 2, right,
            (top + bottom) / 2, (left + right) / 2, top, (left + right) / 2,
            bottom, };

      if (circle.transform != null) {
         circle.transform.mapPoints(points);
      }

      return points;
   }

   /**
    * Get bounds
    * 
    * @param ellipse
    * @param bound
    * @param renderer
    */
   public static float[] getPoints(SVG.Ellipse ellipse, SVGAndroidRenderer renderer)
   {
      float rx = ellipse.rx == null ? 0 : ellipse.rx.floatValue(renderer);
      float ry = ellipse.ry == null ? 0 : ellipse.ry.floatValue(renderer);
      float left = ellipse.cx == null ? 0 : ellipse.cx.floatValue(renderer)
            - rx;
      float top = ellipse.cy == null ? 0 : ellipse.cy.floatValue(renderer) - ry;
      float right = left + 2 * rx;
      float bottom = top + 2 * ry;

      float[] points = new float[] { left, (top + bottom) / 2, right,
            (top + bottom) / 2, (left + right) / 2, top, (left + right) / 2,
            bottom, };

      if (ellipse.transform != null) {
         ellipse.transform.mapPoints(points);
      }

      return points;
   }

   /**
    * Get bounds
    * 
    * @param line
    * @param bound
    * @param renderer
    */
   public static float[] getPoints(SVG.Line line, SVGAndroidRenderer renderer)
   {
      float[] points = new float[] { line.x1.floatValue(renderer),
            line.y1.floatValue(renderer), line.x2.floatValue(renderer),
            line.y2.floatValue(renderer), };
      if (line.transform != null) {
         line.transform.mapPoints(points);
      }

      return points;
   }

   /**
    * Get bounds
    * 
    * @param polyline
    * @param bound
    * @param renderer
    */
   public static float[] getPoints(SVG.PolyLine polyline,
         SVGAndroidRenderer renderer)
   {
      float[] points = new float[polyline.points.length];
      System.arraycopy(polyline.points, 0, points, 0, polyline.points.length);

      if (polyline.transform != null) {
         polyline.transform.mapPoints(points);
      }

      return points;
   }

   /**
    * Get bounds
    * 
    * @param text
    * @param bound
    * @param renderer
    */
   public static float[] getPoints(SVG.TextPositionedContainer text, SVGAndroidRenderer renderer)
   {
      if (text.boundingBox == null) {
         SVGAndroidRenderer.TextBoundsCalculator proc = renderer.new TextBoundsCalculator(
               text.x.get(0).floatValueX(renderer),
               text.y.get(0).floatValueY(
               renderer)
         );
         renderer.enumerateTextSpans(text, proc);
         text.boundingBox = new SVG.Box(proc.bbox.left, proc.bbox.top,
               proc.bbox.width(), proc.bbox.height());
      }
      float top = text.boundingBox.minY;
      float left = text.boundingBox.minX;
      float right = left + text.boundingBox.width;
      float bottom = top + text.boundingBox.height;

      float[] points = new float[] { left, top, right, top, left, bottom,
            right, bottom };

      if (text instanceof SVG.Text) {
         if (((SVG.Text) text).transform != null) {
            ((SVG.Text) text).transform.mapPoints(points);
         }
      }

      return points;
   }

   /**
    * Get bounds
    * 
    * @param image
    * @param bound
    * @param renderer
    */
   public static float[] getPoints(SVG.Image image, SVGAndroidRenderer renderer)
   {
      float left = image.x.floatValue(renderer);
      float top = image.y.floatValue(renderer);
      float right = left + image.width.floatValue(renderer);
      float bottom = top + image.height.floatValue(renderer);

      float[] points = new float[] { left, top, right, top, left, bottom,
            right, bottom };

      if (image.transform != null) {
         image.transform.mapPoints(points);
      }

      return points;
   }
   
   
   
   protected static boolean testConditionalObject(SVG.SvgConditional element, SVGAndroidRenderer renderer)
   {
      // Ignore any objects that isn't an Svg element
      if (!(element instanceof SVG.SvgElement))
         return false;
      
      // We don't support extensions
      if (element.getRequiredExtensions() != null)
         return false;

      String                   deviceLanguage = Locale.getDefault().getLanguage();
      SVGExternalFileResolver  fileResolver  = ((SVG.SvgElement)element).document.getFileResolver();

      // Check language
      Set<String>  syslang = element.getSystemLanguage();
      if (syslang != null && (syslang.isEmpty() || !syslang.contains(deviceLanguage)))
         return false;
      
      // Check features
      Set<String>  reqfeat = element.getRequiredFeatures();
      if (reqfeat != null) {
         if (SVGAndroidRenderer.supportedFeatures == null)
            SVGAndroidRenderer.initialiseSupportedFeaturesMap();
         if (reqfeat.isEmpty() || !SVGAndroidRenderer.supportedFeatures.containsAll(reqfeat))
            return false;
      }
      
      // Check formats (MIME types)
      Set<String>  reqfmts = element.getRequiredFormats();
      if (reqfmts != null) {
         if (reqfmts.isEmpty() || fileResolver==null)
            return false;
         for (String mimeType: reqfmts) {
            if (!fileResolver.isFormatSupported(mimeType))
               return false;
         }
      }
      
      // Check formats (MIME types)
      Set<String>  reqfonts = element.getRequiredFonts();
      if (reqfonts != null) {
         if (reqfonts.isEmpty() || fileResolver==null)
            return false;
         for (String fontName: reqfonts) {
            if (fileResolver.resolveFont(
                  fontName,
                  renderer.getState().style.fontWeight,
                  String.valueOf(renderer.getState().style.fontStyle)
            ) == null)
               return false;
         }
      }
      
      return true;
      
   } // testConditionalObject()
   
   
   public static SVG.SvgElement getSwitchRenderedElement(SVG.Switch sw, SVGAndroidRenderer renderer)
   {
      for (SVG.SvgObject child: sw.getChildren())
      {
         if (!(child instanceof SVG.SvgElement))     continue;
         if (!(child instanceof SVG.SvgConditional)) continue;
         
         if (testConditionalObject((SVG.SvgConditional)child, renderer))
         {
            return (SVG.SvgElement)child;
         }
      }
      return null;
   }
}
