/*
   Copyright 2015 François RAOULT, Personal work.
   Based on com.caverock.androidsvg.SVGAndroidRenderer

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


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.zip.GZIPOutputStream;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.util.Base64;
import android.util.Log;

import com.caverock.androidsvg.CSSParser.Rule;
import com.caverock.androidsvg.CSSParser.SimpleSelector;
import com.caverock.androidsvg.SVG.GraphicsElement;
import com.caverock.androidsvg.SVG.Length;
import com.caverock.androidsvg.SVG.PathDefinition;
import com.caverock.androidsvg.SVG.PathInterface;
import com.caverock.androidsvg.SVG.Style;
import com.caverock.androidsvg.SVG.Style.VectorEffect;
import com.caverock.androidsvg.SVG.PaintReference;
import com.caverock.androidsvg.SVG.SvgPreserveAspectRatioContainer;
import com.caverock.androidsvg.SVG.SvgConditional;
import com.caverock.androidsvg.SVG.SvgContainer;
import com.caverock.androidsvg.SVG.SvgElementBase;
import com.caverock.androidsvg.SVG.SvgObject;
import com.caverock.androidsvg.SVG.SvgViewBoxContainer;
import com.caverock.androidsvg.SVG.TextContainer;

/**
 * The writer part of AndroidSVG.
 * <p>
 * All interaction with AndroidSVG is via the SVG class.  You may ignore this class.
 * 
 * @hide
 */

public class SVGAndroidWriter
{
   private static final String  TAG = "SVGAndroidWriter";

   private OutputStreamWriter writer;

   /**
    *  Convert an internal PathDefinition to a String object for "d" attribute
    */
   private class PathConverter implements PathInterface
   {
	   
	   StringBuilder path = new StringBuilder();
	   
      public PathConverter(PathDefinition pathDef)
      {
         if (pathDef == null)
            return;
         pathDef.enumeratePath(this);
      }

      public String  getPathString()
      {
         return path.toString();
      }

      @Override
      public void moveTo(float x, float y)
      {
    	  path.append(" M "+x+","+y);
      }

      @Override
      public void lineTo(float x, float y)
      {
    	  path.append(" L "+x+","+y);
      }

      @Override
      public void cubicTo(float x1, float y1, float x2, float y2, float x3, float y3)
      {
    	  path.append(" C "+x1+","+y1+" "+x2+","+y2+" "+x3+","+y3);
      }

      @Override
      public void quadTo(float x1, float y1, float x2, float y2)
      {
    	  path.append(" Q "+x1+","+y1+" "+x2+","+y2);
      }

      @Override
      public void arcTo(float rx, float ry, float xAxisRotation, boolean largeArcFlag, boolean sweepFlag, float x, float y)
      {
    	  path.append(" A "+rx+","+ry+" "+xAxisRotation+" "+(largeArcFlag?"1":"0")+" "+(sweepFlag?"1":"0")+" "+x+","+y);
      }

      @Override
      public void close()
      {
    	  path.append(" Z");
      }
         
   }

   /**
    * Create a new writer instance.
    *
    * @param writer the stream to write to.
    */

   protected SVGAndroidWriter(OutputStreamWriter writer)
   {
      this.writer = writer;
   }
   
   /**
    * Write the given SVG to the given output stream writer
    * 
    * @param writer the stream to write to.
    * @param svg    the SVG object
    */
   public static void write(OutputStreamWriter writer, SVG svg) throws IOException
   {
	   SVGAndroidWriter instance = new SVGAndroidWriter(writer);
	   SVG.Svg document = svg.getRootElement();
	   writer.write("<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n");
 	   writer.append("\n<!-- Created with SVGAndroidWriter (package com.caverock.androidsvg) -->\n\n");
	   instance.write(document);
   }
   
   /**
    * Write the given SVG to the given output stream
    * 
    * @param writer the stream to write to.
    * @param svg    the SVG object
    */
   public static void write(OutputStream stream, SVG svg) throws IOException
   {
	   OutputStreamWriter writer = new OutputStreamWriter(stream);
	   write(writer, svg);
	   writer.close();
   }

   /**
    * Write the given SVG to the given file
    * 
    * @param writer the stream to write to.
    * @param svg    the SVG object
    */
   public static void write(File file, SVG svg) throws IOException
   {
	   if (! file.getParentFile().exists())
	   {
		   file.getParentFile().mkdirs();
	   }
	   if (! file.getParentFile().isDirectory())
	   {
		   error("File path is not a a directory");
		   throw new IOException("File path not a directory");
	   }
	   if (file.exists() && ! file.isFile())
	   {
		   error("File is not a regular file... (maybe a directory)");
		   throw new IOException("Not a regular file");
	   }
	   FileOutputStream stream = new FileOutputStream(file);
	   write(stream, svg);
	   stream.close();
   }


   /**
    * Write the given SVG to the given file with given format (JPG, PNG, WEBP, SVG, SVGZ)
    * The default background color for JPEG is white
    * 
    * @param writer the stream to write to.
    * @param svg    the SVG object
    * @param format the output format
    */
   public static void write(File file, SVG svg, SVG.OutputFormat format) throws IOException
   {
	   write(file, svg, format, Color.WHITE);
   }
   
   /**
    * Write the given SVG to the given file with given format (JPG, PNG, WEBP, SVG, SVGZ)
    * The background color for JPEG is backgroundColor parameter
    * 
    * @param writer the stream to write to.
    * @param svg    the SVG object
    * @param format the output format
    * @param backgroundColor the background color for JPEG format
    */
   @SuppressLint("NewApi")
   public static void write(File file, SVG svg, SVG.OutputFormat format, int backgroundColor) throws IOException
   {
	   OutputStream os = new FileOutputStream(file);
	   if (SVG.OutputFormat.jpg.equals(format))
	   {
		   Bitmap bmp = svg.getBitmap(backgroundColor);
		   bmp.compress(CompressFormat.JPEG, 90, os);
		   os.close();
		   bmp.recycle();
		   bmp = null;
		   ExifInterface exif = new ExifInterface(file.getAbsolutePath());
		   exif.setAttribute(ExifInterface.TAG_MAKE, "SVGAndroidWriter (package com.caverock.androidsvg)");
		   exif.setAttribute(ExifInterface.TAG_FLASH, "0");
		   exif.setAttribute(ExifInterface.TAG_IMAGE_LENGTH, svg.getDocumentHeight()+"px");
		   exif.setAttribute(ExifInterface.TAG_IMAGE_WIDTH, svg.getDocumentWidth()+"px");
	   }
	   else if (SVG.OutputFormat.png.equals(format))
	   {
		   Bitmap bmp = svg.getBitmap();
		   bmp.compress(CompressFormat.PNG, 90, os);
		   os.close();
		   bmp.recycle();
		   bmp = null;
	   }
	   else if (SVG.OutputFormat.webp.equals(format))
	   {
		   Bitmap bmp = svg.getBitmap();
		   bmp.compress(CompressFormat.WEBP, 90, os);
		   os.close();
		   bmp.recycle();
		   bmp = null;
	   }
	   else if (SVG.OutputFormat.svg.equals(format))
	   {
		   write(file, svg);
	   }
	   else if (SVG.OutputFormat.svgz.equals(format))
	   {
		   FileOutputStream fos = new FileOutputStream(file);
		   GZIPOutputStream gos = new GZIPOutputStream(fos);
		   write(gos, svg);
		   gos.close();
		   fos.close();
	   }
   }

   /**
    * Write the whole document.
    */
   protected void  writeDocument(SVG document) throws IOException
   {
      SVG.Svg  rootObj = document.getRootElement();

      if (rootObj == null) {
         warn("Nothing to render. Document is empty.");
         return;
      }

      writeXMLSpaceAttribute(rootObj);

      // Write the document
      write(rootObj);
   }


   //==============================================================================
   // Writer dispatcher


   private void  write(SVG.SvgObject obj) throws IOException
   {

      writeXMLSpaceAttribute(obj);

      if (obj instanceof SVG.Svg) {
         write((SVG.Svg) obj);
      } else if (obj instanceof SVG.Use) {
         write((SVG.Use) obj);
      } else if (obj instanceof SVG.Switch) {
         write((SVG.Switch) obj);
      } else if (obj instanceof SVG.Group) {
         write((SVG.Group) obj);
      } else if (obj instanceof SVG.Image) {
         write((SVG.Image) obj);
      } else if (obj instanceof SVG.Path) {
         write((SVG.Path) obj);
      } else if (obj instanceof SVG.Rect) {
         write((SVG.Rect) obj);
      } else if (obj instanceof SVG.Circle) {
         write((SVG.Circle) obj);
      } else if (obj instanceof SVG.Ellipse) {
         write((SVG.Ellipse) obj);
      } else if (obj instanceof SVG.Line) {
         write((SVG.Line) obj);
      } else if (obj instanceof SVG.Polygon) {
         write((SVG.Polygon) obj);
      } else if (obj instanceof SVG.PolyLine) {
         write((SVG.PolyLine) obj);
      } else if (obj instanceof SVG.Text) {
         write((SVG.Text) obj);
      } else if (obj instanceof SVG.Symbol) {
          write((SVG.Symbol) obj);
      } else if (obj instanceof SVG.SvgLinearGradient) {
          write((SVG.SvgLinearGradient) obj);
      } else if (obj instanceof SVG.SvgRadialGradient) {
          write((SVG.SvgRadialGradient) obj);
      } else if (obj instanceof SVG.SolidColor) {
          write((SVG.SolidColor) obj);
      } else if (obj instanceof SVG.Defs) {
          write((SVG.Defs) obj);
      } else if (obj instanceof SVG.Mask) {
          write((SVG.Mask) obj);
      } else if (obj instanceof SVG.Marker) {
          write((SVG.Marker) obj);
      } else if (obj instanceof SVG.Pattern) {
          write((SVG.Pattern) obj);
      } else if (obj instanceof SVG.Stop) {
          write((SVG.Stop) obj);
      } else if (obj instanceof SVG.View) {
          write((SVG.View) obj);
      }

   }


   //==============================================================================


   private void  writeChildren(SvgContainer obj) throws IOException
   {
      for (SVG.SvgObject child: obj.getChildren()) {
         write(child);
      }
   }


   //==============================================================================


   /**
    * Write attributes of the element
    */
   private void writeAttributesForElement(SvgElementBase obj) throws IOException
   {
      // ID
      
      if (obj.id != null && ! "".equals(obj.id))
      {
    	  writer.append(" id=\""+obj.id+"\"");
      }

      // Style and class names
      
      if (obj.baseStyle != null)
      {
          writeStyle(obj.baseStyle);
      }
      
      if (obj.style != null)
      {
          writeStyle(obj.style);
      }
      
      if (obj.classNames != null && ! obj.classNames.isEmpty())
      {
    	  writer.append(" class=\"");
    	  boolean first = true;
    	  for(String classname : obj.classNames)
    	  {
    		  writer.append((!first?" ":"")+classname);
    		  first = false;
    	  }
    	  writer.append("\"");
      }

      // Conditionnal attributes
      
      if (obj instanceof SvgConditional)
      {
    	  writeConditional((SvgConditional)obj);
      }
    	  
      // Transform
      
      if (obj instanceof GraphicsElement)
      {
    	  GraphicsElement graphic = (GraphicsElement)obj;
      	  writeTransformMatrix(graphic.transform);
      }
      else if(obj instanceof SVG.Group)
      {
    	  SVG.Group group = (SVG.Group)obj;
      	  writeTransformMatrix(group.transform);
      }
      else if(obj instanceof SVG.Text)
      {
    	  SVG.Text text = (SVG.Text)obj;
      	  writeTransformMatrix(text.transform);
      }
      else if(obj instanceof SVG.Image)
      {
    	  SVG.Image image = (SVG.Image)obj;
      	  writeTransformMatrix(image.transform);
      }
      
      // ViewBox
      
      if (obj instanceof SvgViewBoxContainer)
      {
    	  writeViewBox((SvgViewBoxContainer)obj);
      }
      
      // PreserveAspectRatio
      
      if (obj instanceof SvgPreserveAspectRatioContainer)
      {
    	  writePreserveAspectRatio((SvgPreserveAspectRatioContainer)obj);
      }
      
   }
   
   /**
    * Write transformation matrix with attributeName "transform"
    * 
    * @param transform the transformation matrix
    */
   private void writeTransformMatrix(Matrix transform) throws IOException
   {
	   writeTransformMatrix(transform, "transform");
   }
   
   /**
    * Write transformation matrix with given attribute name
    * 
    * @param transform the transformation matrix
    * @param attributeName the attribute name ("transform", "gradientTransform", ...)
    */
   private void writeTransformMatrix(Matrix transform, String attributeName) throws IOException
   {
	   if (transform == null)
	   {
		   return;
	   }
	   float[] values = new float[9];
	   transform.getValues(values);
	   float a = values[0];
	   float b = values[3];
	   float c = values[1];
	   float d = values[4];
	   float e = values[2];
	   float f = values[5];
	   writer.append(" "+attributeName+"=\"matrix("+a+", "+b+", "+c+", "+d+", "+e+", "+f+")\"");
   }

   /**
    * Write the preserveAspectRatio attribute
    * 
    * @param obj the SVG element with a preserveAspectRatio property
    */
   private void writePreserveAspectRatio(SvgPreserveAspectRatioContainer obj) throws IOException
   {
	      if (obj.preserveAspectRatio != null)
	      {
	    	  PreserveAspectRatio.Alignment alignment = obj.preserveAspectRatio.getAlignment();
	    	  PreserveAspectRatio.Scale     scale     = obj.preserveAspectRatio.getScale();
	    	  if (alignment != null || scale != null)
	    	  {
	        	  writer.append(" preserveAspectRatio=\""+(alignment==null ? "" : alignment)+(scale==null ? "" : " "+scale)+"\"");
	    	  }
	      }

   }
   
   /**
    * Write viewBox attribute
    * 
    * @param obj the SVG element with a viewBox property
    */
   private void writeViewBox(SvgViewBoxContainer obj) throws IOException
   {
	   if (obj.viewBox != null)
	   {
		   writer.append(" viewBox=\""+obj.viewBox.minX+" "+obj.viewBox.minY+" "+obj.viewBox.width+" "+obj.viewBox.height+"\"");
	   }
   }

   /**
    * Write conditionals attribute
    * 
    * @param obj the SVG element with conditionals properties (requiredXXX)
    */
   private void writeConditional(SvgConditional obj) throws IOException
   {
 	  String      ext     = obj.getRequiredExtensions();
 	  Set<String> feats   = obj.getRequiredFeatures();
 	  Set<String> fonts   = obj.getRequiredFonts();
 	  Set<String> formats = obj.getRequiredFormats();
 	  Set<String> lang    = obj.getSystemLanguage();
 	  if (ext != null && ! "".equals(ext))
 	  {
 		  writer.write(" requiredExtensions=\""+ext+"\"");
 	  }
 	  if (feats != null && ! feats.isEmpty())
 	  {
 		  writer.write(" requiredFeatures=\"");
 		  boolean start = true;
 		  for(String token : feats)
 		  {
 			  writer.write((!start?" ":"")+token);
 			  start = false;
 		  }
 		  writer.write("\"");
 	  }
 	  if (fonts != null && !fonts.isEmpty())
 	  {
 		  writer.write(" requiredFonts=\"");
 		  boolean start = true;
 		  for(String token : fonts)
 		  {
 			  writer.write((!start?" ":"")+token);
 			  start = false;
 		  }
 		  writer.write("\"");
 	  }
 	  if (formats != null && !formats.isEmpty())
 	  {
 		  writer.write(" requiredFormats=\"");
 		  boolean start = true;
 		  for(String token : formats)
 		  {
 			  writer.write((!start?" ":"")+token);
 			  start = false;
 		  }
 		  writer.write("\"");
 	  }
 	  if (lang != null && !lang.isEmpty())
 	  {
 		  writer.write(" systemLanguage=\"");
 		  boolean start = true;
 		  for(String token : lang)
 		  {
 			  writer.write((!start?" ":"")+token);
 			  start = false;
 		  }
 		  writer.write("\"");
 	  }
   }
   
   /**
    * Check and write xml:space handling.
    */
   private void writeXMLSpaceAttribute(SVG.SvgObject obj) throws IOException
   {
      if (!(obj instanceof SvgElementBase))
        return;

      SvgElementBase bobj = (SvgElementBase) obj;
      if (bobj.spacePreserve != null)
      {
    	  writer.write(" xml:space=\""+(bobj.spacePreserve ? "preserve" : "default")+"\"");
      }
   }


   //==============================================================================


   private static void  warn(String format, Object... args)
   {
      Log.w(TAG, String.format(format, args));
   }


   private static void  error(String format, Object... args)
   {
      Log.e(TAG, String.format(format, args));
   }

   private static void  debug(String format, Object... args)
   {
      if (LibConfig.DEBUG)
         Log.d(TAG, String.format(format, args));
   }


   @SuppressWarnings("unused")
   private static void  info(String format, Object... args)
   {
      Log.i(TAG, String.format(format, args));
   }


   //==============================================================================
   // Writers for each element type

   // When called from writeDocument, we pass in our own viewBox.
   // If write the whole document, it will be rootObj.viewBox.  When write a view
   // it will be the viewBox from the <view> element.
   
   private void write(SVG.Svg obj) throws IOException
   {
      debug("Svg writer");

      writer.append("<svg");

      if (obj.version != null && ! "".equals(obj.version))
      {
    	  writer.append(" version=\""+obj.version+"\"");
      }
      
      // xlink namespace
      writer.append(" xmlns=\"http://www.w3.org/2000/svg\" xmlns:link=\"http://www.w3.org/1999/xlink\"");

      if (obj.x != null)
      {
    	  writer.append(" x=\""+obj.x+"\"");
      }
      if (obj.y != null)
      {
    	  writer.append(" y=\""+obj.y+"\"");
      }
      if (obj.width != null)
      {
    	  writer.append(" width=\""+obj.width+"\"");
      }
      if (obj.height != null)
      {
    	  writer.append(" height=\""+obj.height+"\"");
      }

      writeAttributesForElement(obj);
   
      writer.append(">\n");
      
      // Title and desc
      
      String title = obj.document.getDocumentTitle();
      String desc  = obj.document.getDocumentDescription();
      
      if (title != null && ! title.equals(""))
      {
    	  writer.append("<title>"+title.trim()+"</title>\n");
      }
      if (desc != null && ! desc.equals(""))
      {
    	  writer.append("<desc>\n"+desc.trim()+"\n</desc>\n");
      }
      
      // StyleSheet
      List<Rule> cssRules = obj.document.getCSSRules();
      if (cssRules != null && cssRules.size() > 0)
      {
    	  writer.append("\n<!-- Style sheet -->\n");
    	  writer.append("<style type=\"text/css\" >\n");
    	  for(Rule rule : cssRules)
    	  {
    	      for (SimpleSelector sel: rule.selector.selector) writer.append(sel.toString()+" ");
    		  writer.append("{\n");
    		  writeStyle(rule.style, false, true);
    		  writer.append("}\n</style>\n");
        	  writer.append("<!-- End of style sheet -->\n\n");
    	  }
      }
      
      // children
      writeChildren(obj);
      
      // end of doc
      
      writer.append("</svg>");
      
   }


   //==============================================================================


   private void write(SVG.Group obj) throws IOException
   {
      debug("Group writer");
	  writer.append("\n<!-- Start GROUP -->\n");
      writer.append("<g");
      writeAttributesForElement(obj);
      writer.append(">\n");
      writeChildren(obj);
      writer.append("</g>\n");
	  writer.append("<!-- End GROUP -->\n\n");
   }


   //==============================================================================


   /**
    * Find the first child of the switch that passes the feature tests and write only that child.
    */
   private void write(SVG.Switch obj) throws IOException
   {
      debug("Switch writer");

      writer.write("<switch");
      
      writeAttributesForElement(obj);

      writer.write(">\n");
      writeSwitchChild(obj);
      writer.write("</switch>\n");
    
   }


   private void  writeSwitchChild(SVG.Switch obj) throws IOException
   {
      for (SVG.SvgObject child: obj.getChildren())
      {
         write(child);
      }
   }

   //==============================================================================

   private void write(SVG.Use obj) throws IOException
   {
      debug("Use writer");


      writer.append("<use");
      
      if (obj.x != null)
      {
    	  writer.append(" x=\""+obj.x+"\"");
      }
      if (obj.y != null)
      {
    	  writer.append(" y=\""+obj.y+"\"");
      }
      if (obj.width != null)
      {
    	  writer.append(" width=\""+obj.width+"\"");
      }
      if (obj.height != null)
      {
    	  writer.append(" height=\""+obj.height+"\"");
      }
      
      if (obj.href != null)
      {
    	  writer.append(" xlink:href=\""+obj.href+"\"");
      }

      writeAttributesForElement(obj);
      
      writer.append(" />\n");
   }


   //==============================================================================


   private void write(SVG.Path obj) throws IOException
   {
      debug("Path writer");

      if (obj.d == null)
         return;

      PathConverter converter = new PathConverter(obj.d);
      writer.write("<path");
      writeAttributesForElement(obj);
      writer.append(" d=\""+converter.getPathString().trim()+"\" />\n");

   }

   //==============================================================================


   private void write(SVG.Rect obj) throws IOException
   {
      debug("Rect write");

      writer.write("<rect");
      
      writeAttributesForElement(obj);

      
      if (obj.x != null)
      {
    	  writer.append(" x=\""+obj.x+"\"");
      }
      if (obj.y != null)
      {
    	  writer.append(" y=\""+obj.x+"\"");
      }
      if (obj.rx != null)
      {
    	  writer.append(" rx=\""+obj.rx+"\"");
      }
      if (obj.ry != null)
      {
    	  writer.append(" ry=\""+obj.rx+"\"");
      }
      if (obj.width != null)
      {
    	  writer.append(" width=\""+obj.width+"\"");
      }
      if (obj.height != null)
      {
    	  writer.append(" height=\""+obj.height+"\"");
      }
      
      writer.append(" />\n");

   }


   //==============================================================================


   private void write(SVG.Circle obj) throws IOException
   {
      debug("Circle writer");
      
      writer.append("<circle cx=\""+obj.cx+"\" cy=\""+obj.cy+"\" r=\""+obj.r+"\"");
      writeAttributesForElement(obj);
      writer.append("/>\n");
   }


   //==============================================================================


   private void write(SVG.Ellipse obj) throws IOException
   {
      debug("Ellipse writer");

      writer.append("<ellipse cx=\""+obj.cx+"\" cy=\""+obj.cy+"\" rx=\""+obj.rx+"\" ry=\""+obj.ry+"\"");
      writeAttributesForElement(obj);
      writer.append("/>\n");
      
   }


   //==============================================================================


   private void write(SVG.Line obj) throws IOException
   {
      debug("Line writer");

      writer.append("<line x1=\""+obj.x1+"\" y1=\""+obj.y1+"\" x2=\""+obj.x2+"\" y2=\""+obj.y2+"\"");
      writeAttributesForElement(obj);
      writer.append("/>\n");

   }

   //==============================================================================

   private void write(SVG.PolyLine obj) throws IOException
   {
      debug("PolyLine write");

      writer.append("<polyline points=\"");

      int numPoints = obj.points.length;
      if (numPoints > 2)
      {
    	  for(int i=0; i<numPoints-1; i=i+2)
    	  {
    		  if (i > 0)
    		  {
    			  writer.append(", ");
    		  }
    		  writer.append(obj.points[i]+","+obj.points[i+1]);
    	  }
      }
      
      writer.append("\"");

      writeAttributesForElement(obj);

      writer.append("/>\n");

   }

   //==============================================================================

   private void write(SVG.Polygon obj) throws IOException
   {
      debug("Polygon writer");

      writer.append("<polygon points=\"");

      int numPoints = obj.points.length;
      if (numPoints > 2)
      {
    	  for(int i=0; i<numPoints-1; i=i+2)
    	  {
    		  if (i > 0)
    		  {
    			  writer.append(", ");
    		  }
    		  writer.append(obj.points[i]+","+obj.points[i+1]);
    	  }
      }
      
      writer.append("\"");

      writeAttributesForElement(obj);

      writer.append("/>\n");
   }

   //==============================================================================

   private void write(SVG.Text obj) throws IOException
   {
      debug("Text writer");

      Length  x = (obj.x == null || obj.x.size() == 0) ? null  : obj.x.get(0);
      Length  y = (obj.y == null || obj.y.size() == 0) ? null : obj.y.get(0);
      Length  dx = (obj.dx == null || obj.dx.size() == 0) ? null : obj.dx.get(0);
      Length  dy = (obj.dy == null || obj.dy.size() == 0) ? null : obj.dy.get(0);

      writer.append("<text");
      if (x != null)
      {
          writer.append(" x=\""+x+"\"");
      }
      if (y != null)
      {
          writer.append(" y=\""+y+"\"");
      }
      if (dx != null)
      {
          writer.append(" dx=\""+dx+"\"");
      }
      if (dy != null)
      {
          writer.append(" dy=\""+dy+"\"");
      }
      
      writeAttributesForElement(obj);

      writer.append(">");

      enumerateTextSpans(obj, new PlainTextWriter());

     writer.append("</text>\n");
   }

   //==============================================================================

   private void write(SVG.Defs obj) throws IOException
   {
       debug("Defs writer");
 	   writer.append("\n<!-- Start DEFS -->\n");
	   writer.append("<defs");
	   writeAttributesForElement(obj);
	   writer.append(">\n");
	   writeChildren(obj);
	   writer.append("</defs>\n");
 	   writer.append("<!-- End DEFS -->\n\n");
   }
   

   //==============================================================================

   private void write(SVG.SvgLinearGradient obj) throws IOException
   {
       debug("LinearGradient writer");

	   writer.append("<linearGradient");
	   
	   writeAttributesForElement(obj);
	   
	   if (obj.gradientTransform != null)
	   {
		   writeTransformMatrix(obj.gradientTransform, "gradientTransform");
	   }
	   if (obj.gradientUnitsAreUser != null)
	   {
		   writer.append(" gradientUnits=\""+(obj.gradientUnitsAreUser?"userSpaceOnUse":"objectBoundingBox")+"\"");
	   }
	   if (obj.x1 != null)
	   {
		   writer.append(" x1=\""+obj.x1+"\"");
	   }
	   if (obj.y1 != null)
	   {
		   writer.append(" y1=\""+obj.y1+"\"");
	   }
	   if (obj.x2 != null)
	   {
		   writer.append(" x2=\""+obj.x2+"\"");
	   }
	   if (obj.y2 != null)
	   {
		   writer.append(" y2=\""+obj.y2+"\"");
	   }
	   if (obj.spreadMethod != null)
	   {
		   writer.append(" spreadMethod=\""+obj.spreadMethod+"\"");
	   }
	   if (obj.href != null)
	   {
		   writer.append(" xlink:href=\""+obj.href+"\"");
	   }
	   
	   writer.append(">\n");
	   
	   writeChildren(obj);
	   
	   writer.append("</linearGradient>\n");
   }

   //==============================================================================

   private void write(SVG.SvgRadialGradient obj) throws IOException
   {
       debug("RadialGradient writer");

	   writer.append("<radialGradient");
	   
	   writeAttributesForElement(obj);
	   
	   if (obj.gradientTransform != null)
	   {
		   writeTransformMatrix(obj.gradientTransform, "gradientTransform");
	   }
	   if (obj.gradientUnitsAreUser != null)
	   {
		   writer.append(" gradientUnits=\""+(obj.gradientUnitsAreUser?"userSpaceOnUse":"objectBoundingBox")+"\"");
	   }
	   if (obj.cy != null)
	   {
		   writer.append(" cx=\""+obj.cx+"\"");
	   }
	   if (obj.cy != null)
	   {
		   writer.append(" cy=\""+obj.cy+"\"");
	   }
	   if (obj.fx != null)
	   {
		   writer.append(" fx=\""+obj.fx+"\"");
	   }
	   if (obj.fy != null)
	   {
		   writer.append(" fy=\""+obj.fy+"\"");
	   }
	   if (obj.spreadMethod != null)
	   {
		   writer.append(" spreadMethod=\""+obj.spreadMethod+"\"");
	   }
	   if (obj.href != null)
	   {
		   writer.append(" xlink:href=\""+obj.href+"\"");
	   }
	   
	   writer.append(">\n");
	   
	   writeChildren(obj);
	   
	   writer.append("</radialGradient>\n");
   }
   
   //==============================================================================

   private void write(SVG.Stop obj) throws IOException
   {
	   
       debug("Stop writer");

	   writer.append("<stop");

	   writeAttributesForElement(obj);
	   
	   if (obj.offset != null)
	   {
		   writer.append(" offset=\""+obj.offset+"\"");
	   }
	   
	   writer.append("/>\n");
	   
   }
   
   //==============================================================================

   private void write(SVG.SolidColor obj) throws IOException
   {
       debug("SolidColor writer");

	   writer.append("<solidcolor");

	   writeAttributesForElement(obj);

      if (! isSpecified(obj.style, SVG.SPECIFIED_SOLID_COLOR) && ! isSpecified(obj.baseStyle, SVG.SPECIFIED_SOLID_COLOR))
      {
      	if (obj.solidColor != null)
      	{
          	writer.append(" solid-color=\""+obj.solidColor+"\"");
      	}
      }

      if (! isSpecified(obj.style, SVG.SPECIFIED_SOLID_OPACITY) && ! isSpecified(obj.baseStyle, SVG.SPECIFIED_SOLID_OPACITY))
      {
        	if (obj.solidOpacity != null)
          	{
        		writer.append(" solid-opacity=\""+obj.solidOpacity+"\"");
          	}
      }
	   
	   writer.append("/>\n");
	   
   }
   
   //==============================================================================

   private void write(SVG.Mask obj) throws IOException
   {
       debug("Mask writer");

	   writer.append("<mask");
	   
	   writeAttributesForElement(obj);

	   if (obj.maskUnitsAreUser != null)
	   {
		   writer.append(" masktUnits=\""+(obj.maskUnitsAreUser?"userSpaceOnUse":"objectBoundingBox")+"\"");
	   }
	   if (obj.maskContentUnitsAreUser != null)
	   {
		   writer.append(" masktContentUnits=\""+(obj.maskContentUnitsAreUser?"userSpaceOnUse":"objectBoundingBox")+"\"");
	   }

	   if (obj.x != null)
	   {
		   writer.append(" x=\""+obj.x+"\"");
	   }
	   if (obj.y != null)
	   {
		   writer.append(" y=\""+obj.y+"\"");
	   }
	   if (obj.width != null)
	   {
		   writer.append(" width=\""+obj.width+"\"");
	   }
	   if (obj.height != null)
	   {
		   writer.append(" height=\""+obj.height+"\"");
	   }

	   writer.append(">");
	   
	   writeChildren(obj);
	   
	   writer.append("</mask>\n");

   }
   
   //==============================================================================

   private void write(SVG.Marker obj) throws IOException
   {
       debug("Marker writer");

	   writer.append("<marker");
	   
	   writeAttributesForElement(obj);

	   writer.append(" markerUnits=\""+(obj.markerUnitsAreUser?"userSpaceOnUse":"objectBoundingBox")+"\"");

	   if (obj.refX != null)
	   {
		   writer.append(" refX=\""+obj.refX+"\"");
	   }
	   if (obj.refY != null)
	   {
		   writer.append(" refY=\""+obj.refY+"\"");
	   }
	   if (obj.markerWidth != null)
	   {
		   writer.append(" markerWidth=\""+obj.markerWidth+"\"");
	   }
	   if (obj.markerHeight != null)
	   {
		   writer.append(" markerHeight=\""+obj.markerHeight+"\"");
	   }
	   if (obj.orient != null)
	   {
		   writer.append(" orient=\""+obj.orient+"\"");
	   }

	   writer.append(">");
	   
	   writeChildren(obj);
	   
	   writer.append("</marker>\n");
	   
   }
   
   //==============================================================================

   private void write(SVG.Pattern obj) throws IOException
   {
       debug("Pattern writer");

 	   writer.append("\n<!-- Start PATTERN -->\n");

	   writer.append("<pattern");
	   
	   writeAttributesForElement(obj);

	   if (obj.patternUnitsAreUser != null)
	   {
		   writer.append(" patternUnits=\""+(obj.patternUnitsAreUser?"userSpaceOnUse":"objectBoundingBox")+"\"");
	   }
	   if (obj.patternContentUnitsAreUser != null)
	   {
		   writer.append(" patternContentUnits=\""+(obj.patternContentUnitsAreUser?"userSpaceOnUse":"objectBoundingBox")+"\"");
	   }
	   if (obj.patternTransform != null)
	   {
		   writeTransformMatrix(obj.patternTransform);
	   }

	   if (obj.x != null)
	   {
		   writer.append(" x=\""+obj.x+"\"");
	   }
	   if (obj.y != null)
	   {
		   writer.append(" y=\""+obj.y+"\"");
	   }
	   if (obj.width != null)
	   {
		   writer.append(" width=\""+obj.width+"\"");
	   }
	   if (obj.height != null)
	   {
		   writer.append(" height=\""+obj.height+"\"");
	   }
	   if (obj.href != null)
	   {
		   writer.append(" xlink:href=\""+obj.href+"\"");
	   }

	   writer.append(">");
	   
	   writeChildren(obj);
	   
	   writer.append("</pattern>\n");
	   
	   writer.append("<!-- End PATTERN -->\n\n");

   }
   
   
   //==============================================================================

   private void write(SVG.View obj) throws IOException
   {
       debug("View writer");

	   writer.append("<view");
	   
	   writeAttributesForElement(obj);

       if (obj.children != null && obj.children.size() > 0)
       {
           writer.append(">");
           writeChildren(obj);
           writer.append("</view>\n");
       }
       else
       {
           writer.append(" />\n");
       }
       
   }


   //==============================================================================


   private class  PlainTextWriter extends TextProcessor
   {
      @Override
      public void processText(String text)
      {
         debug("TextSequence writer");
         try
         {
             writer.append(text);
         }
         catch(IOException e)
         {
        	 error("TEXTPROCESSOR - IOException during write text");
        	 e.printStackTrace();
         }
      }
   }


   //==============================================================================
   // Text sequence enumeration

   private abstract class  TextProcessor
   {
      public boolean  doTextContainer(TextContainer obj)
      {
         return true;
      }

      public abstract void  processText(String text);
   }


   /**
    * Given a text container, recursively visit its children invoking the TextWriter
    * handler for each segment of text found.
    */
   private void enumerateTextSpans(TextContainer obj, TextProcessor textprocessor) throws IOException
   {

	  Iterator<SvgObject>  iter = obj.children.iterator();

      while (iter.hasNext())
      {
         SvgObject  child = iter.next();

         if (child instanceof SVG.TextSequence) {
            textprocessor.processText(((SVG.TextSequence)child).text);
         } else {
            processTextChild(child, textprocessor);
         }
      }
   }


   private void  processTextChild(SVG.SvgObject obj, TextProcessor textprocessor) throws IOException
   {
      // Ask the processor implementation if it wants to process this object
      if (!textprocessor.doTextContainer((SVG.TextContainer) obj))
         return;

      if (obj instanceof SVG.TextPath)
      {
          debug("TextPath render");
          writer.append("<textpath xlink:href=\""+((SVG.TextPath)obj).href+"\"");
          writeAttributesForElement((SVG.TextPath)obj);
          writer.append(">");
          enumerateTextSpans((SVG.TextPath)obj, textprocessor);
          writer.append("</textpath>");
      }
      else if (obj instanceof SVG.TSpan)
      {
         debug("TSpan writer");
         SVG.TSpan tspan = (SVG.TSpan) obj; 
         
         writer.append("<tspan");
         writeAttributesForElement(tspan);

        // Get the first coordinate pair from the lists in the x and y properties.
        
         Length x=null, y=null, dx=null, dy=null;
        
        if (textprocessor instanceof PlainTextWriter) {
           x = (tspan.x == null || tspan.x.size() == 0) ? null : tspan.x.get(0);
           y = (tspan.y == null || tspan.y.size() == 0) ? null : tspan.y.get(0);
           dx = (tspan.dx == null || tspan.dx.size() == 0) ? null : tspan.dx.get(0);
           dy = (tspan.dy == null || tspan.dy.size() == 0) ? null : tspan.dy.get(0);
        }
        
        if (x != null)
        {
            writer.append(" x=\""+x+"\"");
        }
        if (y != null)
        {
            writer.append(" y=\""+y+"\"");
        }
        if (dx != null)
        {
            writer.append(" dx=\""+dx+"\"");
        }
        if (dy != null)
        {
            writer.append(" dy=\""+dy+"\"");
        }

        writer.append(">");

        enumerateTextSpans(tspan, textprocessor);

        writer.append("</tspan>");

      }
      else if  (obj instanceof SVG.TRef)
      {
         debug("TRef writer");
         SVG.TRef tref = (SVG.TRef) obj; 
         writer.append("<tref xlink:href=\""+tref.href+"\"");
         writeAttributesForElement(tref);
         writer.append("/>");
      }
   }

   //==============================================================================

   private void write(SVG.Symbol obj) throws IOException
   {
      debug("Symbol writer");
      writer.append("<symbol");
      writeAttributesForElement(obj);
      writer.write(">");
      writeChildren(obj);
      writer.write("</symbol>");
   }

   //==============================================================================


   private void write(SVG.Image obj) throws IOException
   {
      debug("Image writer");

      writer.append("<image ");
      
      if (obj.x != null)
      {
    	  writer.append(" x=\""+obj.x+"\"");
      }
      if (obj.y != null)
      {
    	  writer.append(" y=\""+obj.y+"\"");
      }
      if (obj.width != null)
      {
    	  writer.append(" width=\""+obj.width+"\"");
      }
      if (obj.height != null)
      {
    	  writer.append(" height=\""+obj.height+"\"");
      }
      
      writeAttributesForElement(obj);
      

      writer.append(" xlink:href=\"");

      if (checkForImageDataURL(obj.href))
      {
    	  writer.append(obj.href);
      }
      else
      {
    	  File file = new File(obj.href);
    	  if (file.exists())
    	  {
    		  writeBase64Image(file);
    	  }
      }
      
      writer.append("\" />");

   }

   //==============================================================================

   /**
    * Check for an decode an image encoded in a data URL.
    * We don't handle all permutations of data URLs. Only base64 ones.
    */
   private boolean checkForImageDataURL(String url)
   {
      if (!url.startsWith("data:"))
      {
    	  error("Invalid data for image - string doesn't start with 'data:'");
          return false;
      }
      if (url.length() < 14)
      {
    	  error("Invalid data for image - string's length < 14");
          return false;
      }

      int  comma = url.indexOf(',');
      if (comma == -1 || comma < 12)
      {
    	  error("Invalid data for image - comma not found or incorrectly positioned");
          return false;
      }
      if (!";base64".equals(url.substring(comma-7, comma)))
      {
    	  error("Invalid data for image - string doesn't contains ';base64' substring or substring incorrectly positioned");
          return false;
      }
      return true;
   }

   /**
    * Encode an image and write it to xlink:href attribute
    */
   private void writeBase64Image(File file) throws IOException
   {
	   String type = null;
	   Options options = new Options();
	   options.inJustDecodeBounds = true;
	   BitmapFactory.decodeFile(file.getAbsolutePath(), options);
	   type = options.outMimeType;
	   SimpleAssetResolver resolv = new SimpleAssetResolver(null);
	   if (! resolv.isFormatSupported(type))
	   {
		   error("File is not a picture file");
		   return;
	   }
	   FileInputStream fis = new FileInputStream(file);
	   writeBase64Image(fis, type);
	   fis.close();
	   return;
   }
   
   /**
    * Encode an image and write it to xlink:href attribute
    */
   private void writeBase64Image(InputStream is, String mimeType) throws IOException
   {
	   InputStreamReader reader = new InputStreamReader(is);
	   writeBase64Image(reader, mimeType);
	   reader.close();
   }
   
   /**
    * Encode an image and write it to xlink:href attribute
    */
   private void writeBase64Image(InputStreamReader isr, String mimeType) throws IOException
   {
	   writer.append("data:"+mimeType+"base64,");
	   char[] buffer = new char[3];
	   byte[] input  = new byte[3];
	   int n = 0;
	   while((n=isr.read(buffer)) > 0)
	   {
		   input[0] = (n > 0) ? (byte)(buffer[0] & 0xFF) : 0b00000000;
		   input[1] = (n > 1) ? (byte)(buffer[1] & 0xFF) : 0b00000000;
		   input[2] = (n > 2) ? (byte)(buffer[2] & 0xFF) : 0b00000000;
		   writer.append(Base64.encodeToString(input, Base64.NO_WRAP));
	   }
	   return;

   }

   /**
    * @see {@link SVGAndroidRenderer#isSpecified(Style, long)}
    */
   private boolean  isSpecified(Style style, long flag)
   {
      return (style.specifiedFlags & flag) != 0;
   }


   /**
    * write the global style state with the style defined by the current object.
    */
   private void writeStyle(Style style) throws IOException
   {
	   writeStyle(style, true, false);
   }

   
   /**
    * write the global style state with the style defined by the current object.
    */
   private void writeStyle(Style style, boolean asAttribute, boolean breakLine) throws IOException
   {
      // Now update each style property we know about
      if (isSpecified(style, SVG.SPECIFIED_COLOR) && style.color != null)
      {
         appendStyle("color", style.color, asAttribute, breakLine);
      }

      if (isSpecified(style, SVG.SPECIFIED_OPACITY) && style.opacity != null)
      {
    	  appendStyle("opacity", style.opacity, asAttribute, breakLine);
      }

      if (isSpecified(style, SVG.SPECIFIED_FILL))
      {
      	if (style.fill == null)
      	{
      		appendStyle("fill", "none", asAttribute, breakLine);
      	}
      	else
      	{
      		String color = null;
      		if (style.fill instanceof SVG.Colour)
      		{
      			color = ((SVG.Colour)style.fill).toString();
      		}
      		else if (style.fill instanceof SVG.CurrentColor)
      		{
      			color = "currentColor";
      		}
      		else if (style.fill instanceof SVG.PaintReference)
      		{
      			PaintReference ref = (SVG.PaintReference)style.fill;
      			color = "url("+ref.href+")"+(ref.fallback==null?"":" "+ref.fallback);
      		}
      		if (color != null)
      		{
      			appendStyle("fill", color, asAttribute, breakLine);
      		}
      	}
      }

      if (isSpecified(style, SVG.SPECIFIED_FILL_OPACITY) && style.fillOpacity != null && style.fillOpacity < 1)
      {
			appendStyle("fill-opacity", style.fillOpacity, asAttribute, breakLine);
      }

      if (isSpecified(style, SVG.SPECIFIED_FILL_RULE) && style.fillRule != null)
      {
    	  appendStyle("fill-rule", style.fillRule.toString().toLowerCase(Locale.US), asAttribute, breakLine);
      }


      if (isSpecified(style, SVG.SPECIFIED_STROKE))
      {
        	if (style.stroke == null)
          	{
          	  appendStyle("stroke", "none", asAttribute, breakLine);
          	}
          	else
          	{
          		String color = null;
          		if (style.stroke instanceof SVG.Colour)
          		{
          			color = ((SVG.Colour)style.stroke).toString();
          		}
          		else if (style.stroke instanceof SVG.CurrentColor)
          		{
          			color = "currentColor";
          		}
          		else if (style.stroke instanceof SVG.PaintReference)
          		{
          			PaintReference ref = (SVG.PaintReference)style.stroke;
          			color = "url("+ref.href+")"+(ref.fallback==null?"":" "+ref.fallback);
          		}
          		if (color != null)
          		{
                	appendStyle("stroke", color, asAttribute, breakLine);
          		}
          	}
      }

      if (isSpecified(style, SVG.SPECIFIED_STROKE_OPACITY) && style.strokeOpacity != null && style.strokeOpacity < 1)
      {
			appendStyle("stroke-opacity", style.strokeOpacity, asAttribute, breakLine);
      }

      if (isSpecified(style, SVG.SPECIFIED_VECTOR_EFFECT) && style.vectorEffect != null)
      {
    	  String vectorEffect = style.vectorEffect.toString().toLowerCase(Locale.US);
    	  if (style.vectorEffect.equals(VectorEffect.NonScalingStroke))
    	  {
    		  vectorEffect="non-scaling-stroke";
    	  }
    	  appendStyle("vector-effect", vectorEffect, asAttribute, breakLine);
      }

      if (isSpecified(style, SVG.SPECIFIED_STROKE_WIDTH) && style.strokeWidth != null)
      {
    	  appendStyle("stroke-width", style.strokeWidth, asAttribute, breakLine);
      }

      if (isSpecified(style, SVG.SPECIFIED_STROKE_LINECAP) && style.strokeLineCap != null)
      {
    	  appendStyle("stroke-linecap", style.strokeLineCap.toString().toLowerCase(Locale.US), asAttribute, breakLine);
      }

      if (isSpecified(style, SVG.SPECIFIED_STROKE_LINEJOIN) && style.strokeLineJoin != null)
      {
    	  appendStyle("stroke-linejoin", style.strokeLineJoin.toString().toLowerCase(Locale.US), asAttribute, breakLine);
      }

      if (isSpecified(style, SVG.SPECIFIED_STROKE_MITERLIMIT) && style.strokeMiterLimit != null)
      {
    	  appendStyle("stroke-miterlimit", style.strokeMiterLimit, asAttribute, breakLine);
      }

      if (isSpecified(style, SVG.SPECIFIED_STROKE_DASHARRAY) && style.strokeDashArray != null && style.strokeDashArray.length > 0)
      {
    	  StringBuilder dasharray = new StringBuilder();
    	  for(Length size : style.strokeDashArray)
    	  {
    		  if (dasharray.length() > 0)
    		  {
    			  dasharray.append(", ");
    		  }
    		  dasharray.append(size.toString());
    	  }
    	  appendStyle("stroke-dasharray", dasharray.toString(), asAttribute, breakLine);
      }

      if (isSpecified(style, SVG.SPECIFIED_STROKE_DASHOFFSET) && style.strokeDashOffset != null)
      {
    	  appendStyle("stroke-dashoffset", style.strokeDashOffset, asAttribute, breakLine);
      }

      if (isSpecified(style, SVG.SPECIFIED_FONT_SIZE) && style.fontSize != null)
      {
    	  appendStyle("font-size", style.fontSize, asAttribute, breakLine);
      }

      if (isSpecified(style, SVG.SPECIFIED_FONT_FAMILY) && style.fontFamily != null)
      {
    	  StringBuilder families = new StringBuilder();
    	  for(String family : style.fontFamily)
    	  {
    		  if (families.length() > 0)
    		  {
    			  families.append(", ");
    		  }
    		  families.append(family);
    	  }
    	  appendStyle("font-family", families, asAttribute, breakLine);
      }

      if (isSpecified(style, SVG.SPECIFIED_FONT_WEIGHT) && style.fontWeight != null)
      {
         // Font weights are 100,200...900
         if (style.fontWeight == Style.FONT_WEIGHT_LIGHTER)
       	   appendStyle("font-weight", "lighter", asAttribute, breakLine);
         else if (style.fontWeight == Style.FONT_WEIGHT_BOLDER)
           appendStyle("font-weight", "bolder", asAttribute, breakLine);
         else
           appendStyle("font-weight", style.fontWeight, asAttribute, breakLine);
      }

      if (isSpecified(style, SVG.SPECIFIED_FONT_STYLE) && style.fontStyle != null)
      {
    	  appendStyle("font-style", style.fontStyle.toString().toLowerCase(Locale.US), asAttribute, breakLine);
      }

      if (isSpecified(style, SVG.SPECIFIED_TEXT_DECORATION) && style.textDecoration != null)
      {
    	  appendStyle("text-decoration", style.textDecoration.toString().toLowerCase(Locale.US), asAttribute, breakLine);
      }

      if (isSpecified(style, SVG.SPECIFIED_DIRECTION) && style.direction != null)
      {
    	  appendStyle("direction", style.direction.toString().toLowerCase(Locale.US), asAttribute, breakLine);
      }

      if (isSpecified(style, SVG.SPECIFIED_TEXT_ANCHOR) && style.textAnchor != null)
      {
    	  appendStyle("text-anchor", style.textAnchor.toString().toLowerCase(Locale.US), asAttribute, breakLine);
      }

      if (isSpecified(style, SVG.SPECIFIED_OVERFLOW) && style.overflow != null)
      {
    	  appendStyle("overflow", (style.overflow ? "visible" : "hidden"), asAttribute, breakLine); // TODO overflow:auto; overflow:scroll ?
      }

      if (isSpecified(style, SVG.SPECIFIED_MARKER_START) && style.markerStart != null)
      {
    	  appendStyle("marker-start", "url("+style.markerStart+")", asAttribute, breakLine);
      }

      if (isSpecified(style, SVG.SPECIFIED_MARKER_MID) && style.markerMid != null)
      {
    	  appendStyle("marker-mid", "url("+style.markerMid+")", asAttribute, breakLine);
      }

      if (isSpecified(style, SVG.SPECIFIED_MARKER_END) && style.markerEnd != null)
      {
    	  appendStyle("marker-end", "url("+style.markerEnd+")", asAttribute, breakLine);
      }

      if (isSpecified(style, SVG.SPECIFIED_DISPLAY) && style.display != null)
      {
    	  if (! style.display) // TODO specifics displays ?
    	  {
        	  appendStyle("display", "none", asAttribute, breakLine);
    	  }
      }

      if (isSpecified(style, SVG.SPECIFIED_VISIBILITY) && style.visibility != null)
      {
    	  appendStyle("visibility", (style.visibility ? "visible" : "hidden"), asAttribute, breakLine); // TODO visibility:collpase ?
      }

      if (isSpecified(style, SVG.SPECIFIED_CLIP) && style.clip != null)
      {
    	  appendStyle("clip", "rect("+style.clip.left+", "+style.clip.top+", "+style.clip.right+", "+style.clip.bottom+")", asAttribute, breakLine); // TODO visibility:collpase ?
      }

      if (isSpecified(style, SVG.SPECIFIED_CLIP_PATH) && style.clipPath != null)
      {
    	  appendStyle("clip-path", "url("+style.clipPath+")", asAttribute, breakLine);
      }

      if (isSpecified(style, SVG.SPECIFIED_CLIP_RULE) && style.clipRule != null)
      {
    	  appendStyle("clip-rule", style.clipRule.toString().toLowerCase(Locale.US), asAttribute, breakLine);
      }

      if (isSpecified(style, SVG.SPECIFIED_MASK) && style.mask != null)
      {
    	  appendStyle("mask", "url("+style.mask+")", asAttribute, breakLine);
      }

      if (isSpecified(style, SVG.SPECIFIED_STOP_COLOR) && style.stopColor != null)
      {
    	  appendStyle("stop-color", style.stopColor, asAttribute, breakLine);
      }

      if (isSpecified(style, SVG.SPECIFIED_STOP_OPACITY) && style.stopOpacity != null)
      {
    	  appendStyle("stop-opacity", style.stopOpacity, asAttribute, breakLine);
      }

      if (isSpecified(style, SVG.SPECIFIED_VIEWPORT_FILL) && style.viewportFill != null)
      {
    	  appendStyle("viewport-fill", style.viewportFill, asAttribute, breakLine);
      }

      if (isSpecified(style, SVG.SPECIFIED_VIEWPORT_FILL_OPACITY) && style.viewportFillOpacity != null)
      {
    	  appendStyle("viewport-fill-opacity", style.viewportFillOpacity, asAttribute, breakLine);
      }
      
      if (isSpecified(style, SVG.SPECIFIED_SOLID_COLOR) && style.solidColor != null)
      {
      	if (style.solidColor != null)
      	{
          	appendStyle("solid-color", style.solidColor, asAttribute, breakLine);
      	}
      }

      if (isSpecified(style, SVG.SPECIFIED_SOLID_OPACITY) && style.solidOpacity != null)
      {
      	appendStyle("solid-opacity", style.solidOpacity, asAttribute, breakLine);
      }


   }

   /**
    * Write style formated for attribute or for stylesheet
    * 
    * @param name  Name of the style attribute
    * @param value Value of the style attribute
    * @asAttribute Format for svg attribute (e.g. name="value") if TRUE, for style sheet (e.g name:value;) if FALSE
    * @breakLine   TRUE for add a breakline before (with attribute style) or after (with style sheet style) 
    */
   private void appendStyle(String name, String value, boolean asAttribute, boolean breakLine) throws IOException
   {
	   if (asAttribute)
	   {
		   writer.append((breakLine?"\n  ":" ")+name+"=\""+value+"\"");
	   }
	   else
	   {
		   writer.append((breakLine?"  ":"")+name+":"+value+";"+(breakLine?"\n":""));
	   }
   }

   /**
    * Write style formated for attribute or for stylesheet
    * 
    * @param name  Name of the style attribute
    * @param value Value of the style attribute
    * @asAttribute Format for svg attribute (e.g. name="value") if TRUE, for style sheet (e.g name:value;) if FALSE
    * @breakLine   TRUE for add a breakline before (with attribute style) or after (with style sheet style) 
    */
   private void appendStyle(String name, Object value, boolean asAttribute, boolean breakLine) throws IOException
   {
	   appendStyle(name, String.valueOf(value), asAttribute, breakLine);
   }

}
