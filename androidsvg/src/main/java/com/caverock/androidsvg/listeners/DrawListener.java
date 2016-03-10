package com.caverock.androidsvg.listeners;

import java.util.ArrayList;
import org.xml.sax.SAXException;
import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGAndroidRenderer;
import com.caverock.androidsvg.SVGImageView;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.EditText;

public class DrawListener implements OnTouchListener
{

   protected Matrix             mReverseImageMatrix = new Matrix();
   protected SVGImageView       mImageView          = null;
   protected SVG.SvgElementBase mShape              = null;
   protected SVGAndroidRenderer mRenderer           = null;
   protected PointF             mPoint              = new PointF();
   protected Context            mContext            = null;

   public DrawListener(SVGImageView imageView)
   {
      setImageView(imageView);
   }

   public DrawListener(SVGImageView imageView, Context context)
   {
      this(imageView);
      setContext(context);
   }

   public void setShape(SVG.SvgElementBase shape)
   {
      this.mShape = shape;
   }

   public void setImageView(SVGImageView imageView)
   {
      this.mImageView = imageView;
      this.mRenderer = imageView.getRenderer();
   }

   public void setContext(Context context)
   {
      this.mContext = context;
   }

   @SuppressLint("ClickableViewAccessibility")
@Override
   public boolean onTouch(View v, MotionEvent event)
   {
      if (event.getPointerCount() != 1) {
         return v.onTouchEvent(event);
      }

      float[] point = new float[] { event.getX(), event.getY() };
      mImageView.getImageMatrixRevert().mapPoints(point);
      float x = point[0];
      float y = point[1];

      switch (event.getAction() & MotionEvent.ACTION_MASK) {
         case MotionEvent.ACTION_DOWN:
            return startShape(x, y);

         case MotionEvent.ACTION_MOVE:
            return changeShape(x, y);

         case MotionEvent.ACTION_UP:
            return stopShape(x, y);

         default:
            return v.onTouchEvent(event);

      }

   }

   protected boolean startShape(float x, float y)
   {
      if (mShape instanceof SVG.Line) {
         return startShape((SVG.Line) mShape, x, y);
      }
      else if (mShape instanceof SVG.Circle) {
         return startShape((SVG.Circle) mShape, x, y);
      }
      else if (mShape instanceof SVG.Ellipse) {
         return startShape((SVG.Ellipse) mShape, x, y);
      }
      else if (mShape instanceof SVG.Path) {
         return startShape((SVG.Path) mShape, x, y);
      }
      else if (mShape instanceof SVG.PolyLine) {
         return startShape((SVG.PolyLine) mShape, x, y);
      }
      else if (mShape instanceof SVG.Polygon) {
         return startShape((SVG.Polygon) mShape, x, y);
      }
      else if (mShape instanceof SVG.Rect) {
         return startShape((SVG.Rect) mShape, x, y);
      }
      else if (mShape instanceof SVG.TextPositionedContainer) {
         return startShape((SVG.TextPositionedContainer) mShape, x, y);
      }
      return false;
   }

   protected boolean changeShape(float x, float y)
   {
      if (mShape instanceof SVG.Line) {
         return changeShape((SVG.Line) mShape, x, y);
      }
      else if (mShape instanceof SVG.Circle) {
         return changeShape((SVG.Circle) mShape, x, y);
      }
      else if (mShape instanceof SVG.Ellipse) {
         return changeShape((SVG.Ellipse) mShape, x, y);
      }
      else if (mShape instanceof SVG.Path) {
         return changeShape((SVG.Path) mShape, x, y);
      }
      else if (mShape instanceof SVG.PolyLine) {
         return changeShape((SVG.PolyLine) mShape, x, y);
      }
      else if (mShape instanceof SVG.Polygon) {
         return changeShape((SVG.Polygon) mShape, x, y);
      }
      else if (mShape instanceof SVG.Rect) {
         return changeShape((SVG.Rect) mShape, x, y);
      }
      else if (mShape instanceof SVG.TextPositionedContainer) {
         return changeShape((SVG.TextPositionedContainer) mShape, x, y);
      }
      return false;
   }

   protected boolean stopShape(float x, float y)
   {
      if (mShape instanceof SVG.Line) {
         return stopShape((SVG.Line) mShape, x, y);
      }
      else if (mShape instanceof SVG.Circle) {
         return stopShape((SVG.Circle) mShape, x, y);
      }
      else if (mShape instanceof SVG.Ellipse) {
         return stopShape((SVG.Ellipse) mShape, x, y);
      }
      else if (mShape instanceof SVG.Path) {
         return stopShape((SVG.Path) mShape, x, y);
      }
      else if (mShape instanceof SVG.PolyLine) {
         return stopShape((SVG.PolyLine) mShape, x, y);
      }
      else if (mShape instanceof SVG.Polygon) {
         return stopShape((SVG.Polygon) mShape, x, y);
      }
      else if (mShape instanceof SVG.Rect) {
         return stopShape((SVG.Rect) mShape, x, y);
      }
      else if (mShape instanceof SVG.TextPositionedContainer) {
         return stopShape((SVG.TextPositionedContainer) mShape, x, y);
      }
      return false;
   }

   // ----------------------
   // Line

   protected boolean startShape(SVG.Line line, float x, float y)
   {
      if (line.x1 == null)
         line.x1 = new SVG.Length(0);
      if (line.y1 == null)
         line.y1 = new SVG.Length(0);
      if (line.x2 == null)
         line.x2 = new SVG.Length(0);
      if (line.y2 == null)
         line.y2 = new SVG.Length(0);
      line.x1.setValue(mRenderer, x);
      line.y1.setValue(mRenderer, y);
      line.x2.setValue(mRenderer, x);
      line.y2.setValue(mRenderer, y);
      line.appendToDocument(mImageView.getSVG());
      return true;
   }

   protected boolean changeShape(SVG.Line line, float x, float y)
   {
      line.x2.setValue(mRenderer, x);
      line.y2.setValue(mRenderer, y);
      mImageView.invalidate();
      return true;
   }

   protected boolean stopShape(SVG.Line line, float x, float y)
   {
      try {
         mShape = mShape.clone();
      }
      catch (CloneNotSupportedException e) {
         e.printStackTrace();
      }
      return true;
   }

   // ----------------------
   // Circle

   protected boolean startShape(SVG.Circle circle, float x, float y)
   {
      if (circle.cx == null)
         circle.cx = new SVG.Length(0);
      if (circle.cy == null)
         circle.cy = new SVG.Length(0);
      if (circle.r == null)
         circle.r = new SVG.Length(0);
      circle.cx.setValue(mRenderer, x);
      circle.cy.setValue(mRenderer, y);
      circle.r.setValue(mRenderer, 0);
      circle.appendToDocument(mImageView.getSVG());
      return true;
   }

   protected boolean changeShape(SVG.Circle circle, float x, float y)
   {
      circle.r.setValue(mRenderer,
            Math.abs(Math.min(circle.cx.value - x, circle.cx.value - y)));
      mImageView.invalidate();
      return true;
   }

   protected boolean stopShape(SVG.Circle circle, float x, float y)
   {
      try {
         mShape = mShape.clone();
      }
      catch (CloneNotSupportedException e) {
         e.printStackTrace();
      }
      return true;
   }

   // ----------------------
   // Ellipse

   protected boolean startShape(SVG.Ellipse ellipse, float x, float y)
   {
      if (ellipse.cx == null)
         ellipse.cx = new SVG.Length(0);
      if (ellipse.cy == null)
         ellipse.cy = new SVG.Length(0);
      if (ellipse.rx == null)
         ellipse.rx = new SVG.Length(0);
      if (ellipse.ry == null)
         ellipse.ry = new SVG.Length(0);
      ellipse.cx.setValue(mRenderer, x);
      ellipse.cy.setValue(mRenderer, y);
      ellipse.rx.setValue(mRenderer, 0);
      ellipse.ry.setValue(mRenderer, 0);
      mPoint.x = x;
      mPoint.y = y;
      ellipse.appendToDocument(mImageView.getSVG());
      return true;
   }

   protected boolean changeShape(SVG.Ellipse ellipse, float x, float y)
   {
      ellipse.rx.setValue(mRenderer, Math.abs(mPoint.x - x) / 2);
      ellipse.ry.setValue(mRenderer, Math.abs(mPoint.y - y) / 2);
      ellipse.cx.setValue(mRenderer, (mPoint.x + x) / 2);
      ellipse.cy.setValue(mRenderer, (mPoint.y + y) / 2);
      mImageView.invalidate();
      return true;
   }

   protected boolean stopShape(SVG.Ellipse ellipse, float x, float y)
   {
      try {
         mShape = mShape.clone();
      }
      catch (CloneNotSupportedException e) {
         e.printStackTrace();
      }
      return true;
   }

   // ----------------------
   // Path

   protected boolean startShape(SVG.Path path, float x, float y)
   {
      if (path.d == null)
         path.d = new SVG.PathDefinition();
      path.d.moveTo(x, y);
      path.appendToDocument(mImageView.getSVG());
      mPoint.x = x;
      mPoint.y = y;
      return true;
   }

   protected boolean changeShape(SVG.Path path, float x, float y)
   {
      float lastX = mPoint.x;
      float lastY = mPoint.y;
      path.d.quadTo(lastX, lastY, (x + lastX) / 2, (y + lastY) / 2);
      mImageView.invalidate();
      mPoint.x = x;
      mPoint.y = y;
      return true;
   }

   protected boolean stopShape(SVG.Path path, float x, float y)
   {
      try {
         mShape = mShape.clone();
      }
      catch (CloneNotSupportedException e) {
         e.printStackTrace();
      }
      mPoint.x = x;
      mPoint.y = y;
      return true;
   }

   // ----------------------
   // Rect

   protected boolean startShape(SVG.Rect rect, float x, float y)
   {
      if (rect.x == null)
         rect.x = new SVG.Length(0);
      if (rect.y == null)
         rect.y = new SVG.Length(0);
      if (rect.width == null)
         rect.width = new SVG.Length(0);
      if (rect.height == null)
         rect.height = new SVG.Length(0);
      rect.x.setValue(mRenderer, x);
      rect.y.setValue(mRenderer, y);
      mPoint.x = x;
      mPoint.y = y;
      rect.appendToDocument(mImageView.getSVG());
      return true;
   }

   protected boolean changeShape(SVG.Rect rect, float x, float y)
   {
      float x1 = mPoint.x;
      float y1 = mPoint.y;
      float x2 = x;
      float y2 = y;

      float l = Math.min(x1, x2);
      float t = Math.min(y1, y2);
      float w = Math.max(x1, x2) - l;
      float h = Math.max(y1, y2) - t;

      rect.x.setValue(mRenderer, l);
      rect.y.setValue(mRenderer, t);
      rect.width.setValue(mRenderer, w);
      rect.height.setValue(mRenderer, h);
      mImageView.invalidate();
      return true;
   }

   protected boolean stopShape(SVG.Rect rect, float x, float y)
   {
      try {
         mShape = mShape.clone();
      }
      catch (CloneNotSupportedException e) {
         e.printStackTrace();
      }
      return true;
   }

   // ----------------------
   // PolyLine et Polygon

   protected boolean startShape(SVG.PolyLine polyLine, float x, float y)
   {
      if (polyLine.points == null) {
         polyLine.points = new float[4];
         polyLine.points[0] = x;
         polyLine.points[1] = y;
         polyLine.points[2] = x;
         polyLine.points[3] = y;
         polyLine.appendToDocument(mImageView.getSVG());
      }
      else {
         int size = polyLine.points.length;
         float[] points = new float[size + 2];
         System.arraycopy(polyLine.points, 0, points, 0, size);
         polyLine.points = points;
         polyLine.points[size] = x;
         polyLine.points[size + 1] = y;
      }
      return true;
   }

   protected boolean changeShape(SVG.PolyLine polyLine, float x, float y)
   {
      int size = polyLine.points.length;
      polyLine.points[size - 2] = x;
      polyLine.points[size - 1] = y;
      mImageView.invalidate();
      return true;
   }

   protected boolean stopShape(SVG.PolyLine polyLine, float x, float y)
   {
      return true;
   }

   // ----------------------
   // Text, TSpan, TRef

   protected boolean startShape(SVG.TextPositionedContainer text, float x,
         float y)
   {

      if (text.x == null)
         text.x = new ArrayList<SVG.Length>();
      if (text.y == null)
         text.y = new ArrayList<SVG.Length>();
      if (text.x.size() == 0)
         text.x.add(new SVG.Length(0));
      if (text.y.size() == 0)
         text.y.add(new SVG.Length(0));
      text.x.get(0).setValue(mRenderer, x);
      text.y.get(0).setValue(mRenderer, y);
      text.appendToDocument(mImageView.getSVG());

      return true;
   }

   protected boolean changeShape(SVG.TextPositionedContainer text, float x,
         float y)
   {
      text.x.get(0).setValue(mRenderer, x);
      text.y.get(0).setValue(mRenderer, y);
      return true;
   }

   protected boolean stopShape(final SVG.TextPositionedContainer text, float x,
         float y)
   {
      if (text.getChildren().size() == 0 && mContext != null) {
         final EditText input = new EditText(mContext);
         input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL);
         AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
         builder.setTitle("Text");
         builder.setView(input);
         builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
               dialog.dismiss();
               try {
                  mShape = mShape.clone();
               }
               catch (CloneNotSupportedException e) {
                  e.printStackTrace();
               }
               SVG.TextSequence string = new SVG.TextSequence(input.getText()
                     .toString());
               try {
                  text.addChild(string);
               }
               catch (SAXException e) {
                  e.printStackTrace();
               }
               onTextChanged();
            }
         });
         builder.setNegativeButton("Cancel",
               new DialogInterface.OnClickListener() {
                  @Override
                  public void onClick(DialogInterface dialog, int which)
                  {
                     dialog.dismiss();
                  }
               });
         builder.show();
      }
      return true;
   }

   public void onTextChanged()
   {
      mImageView.invalidate();
   };
}
