package sun.java2d.windows;

import java.awt.Composite;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Path2D.Float;
import sun.java2d.SunGraphics2D;
import sun.java2d.SurfaceData;
import sun.java2d.loops.GraphicsPrimitive;
import sun.java2d.pipe.LoopPipe;
import sun.java2d.pipe.PixelDrawPipe;
import sun.java2d.pipe.PixelFillPipe;
import sun.java2d.pipe.Region;
import sun.java2d.pipe.ShapeDrawPipe;
import sun.java2d.pipe.ShapeSpanIterator;
import sun.java2d.pipe.SpanIterator;

public class GDIRenderer
  implements PixelDrawPipe, PixelFillPipe, ShapeDrawPipe
{
  native void doDrawLine(SurfaceData paramSurfaceData, Region paramRegion, Composite paramComposite, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5);

  public void drawLine(SunGraphics2D paramSunGraphics2D, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    int i = paramSunGraphics2D.transX;
    int j = paramSunGraphics2D.transY;
    doDrawLine(paramSunGraphics2D.surfaceData, paramSunGraphics2D.getCompClip(), paramSunGraphics2D.composite, paramSunGraphics2D.eargb, paramInt1 + i, paramInt2 + j, paramInt3 + i, paramInt4 + j);
  }

  native void doDrawRect(SurfaceData paramSurfaceData, Region paramRegion, Composite paramComposite, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5);

  public void drawRect(SunGraphics2D paramSunGraphics2D, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    doDrawRect(paramSunGraphics2D.surfaceData, paramSunGraphics2D.getCompClip(), paramSunGraphics2D.composite, paramSunGraphics2D.eargb, paramInt1 + paramSunGraphics2D.transX, paramInt2 + paramSunGraphics2D.transY, paramInt3, paramInt4);
  }

  native void doDrawRoundRect(SurfaceData paramSurfaceData, Region paramRegion, Composite paramComposite, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6, int paramInt7);

  public void drawRoundRect(SunGraphics2D paramSunGraphics2D, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6)
  {
    doDrawRoundRect(paramSunGraphics2D.surfaceData, paramSunGraphics2D.getCompClip(), paramSunGraphics2D.composite, paramSunGraphics2D.eargb, paramInt1 + paramSunGraphics2D.transX, paramInt2 + paramSunGraphics2D.transY, paramInt3, paramInt4, paramInt5, paramInt6);
  }

  native void doDrawOval(SurfaceData paramSurfaceData, Region paramRegion, Composite paramComposite, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5);

  public void drawOval(SunGraphics2D paramSunGraphics2D, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    doDrawOval(paramSunGraphics2D.surfaceData, paramSunGraphics2D.getCompClip(), paramSunGraphics2D.composite, paramSunGraphics2D.eargb, paramInt1 + paramSunGraphics2D.transX, paramInt2 + paramSunGraphics2D.transY, paramInt3, paramInt4);
  }

  native void doDrawArc(SurfaceData paramSurfaceData, Region paramRegion, Composite paramComposite, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6, int paramInt7);

  public void drawArc(SunGraphics2D paramSunGraphics2D, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6)
  {
    doDrawArc(paramSunGraphics2D.surfaceData, paramSunGraphics2D.getCompClip(), paramSunGraphics2D.composite, paramSunGraphics2D.eargb, paramInt1 + paramSunGraphics2D.transX, paramInt2 + paramSunGraphics2D.transY, paramInt3, paramInt4, paramInt5, paramInt6);
  }

  native void doDrawPoly(SurfaceData paramSurfaceData, Region paramRegion, Composite paramComposite, int paramInt1, int paramInt2, int paramInt3, int[] paramArrayOfInt1, int[] paramArrayOfInt2, int paramInt4, boolean paramBoolean);

  public void drawPolyline(SunGraphics2D paramSunGraphics2D, int[] paramArrayOfInt1, int[] paramArrayOfInt2, int paramInt)
  {
    doDrawPoly(paramSunGraphics2D.surfaceData, paramSunGraphics2D.getCompClip(), paramSunGraphics2D.composite, paramSunGraphics2D.eargb, paramSunGraphics2D.transX, paramSunGraphics2D.transY, paramArrayOfInt1, paramArrayOfInt2, paramInt, false);
  }

  public void drawPolygon(SunGraphics2D paramSunGraphics2D, int[] paramArrayOfInt1, int[] paramArrayOfInt2, int paramInt)
  {
    doDrawPoly(paramSunGraphics2D.surfaceData, paramSunGraphics2D.getCompClip(), paramSunGraphics2D.composite, paramSunGraphics2D.eargb, paramSunGraphics2D.transX, paramSunGraphics2D.transY, paramArrayOfInt1, paramArrayOfInt2, paramInt, true);
  }

  native void doFillRect(SurfaceData paramSurfaceData, Region paramRegion, Composite paramComposite, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5);

  public void fillRect(SunGraphics2D paramSunGraphics2D, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    doFillRect(paramSunGraphics2D.surfaceData, paramSunGraphics2D.getCompClip(), paramSunGraphics2D.composite, paramSunGraphics2D.eargb, paramInt1 + paramSunGraphics2D.transX, paramInt2 + paramSunGraphics2D.transY, paramInt3, paramInt4);
  }

  native void doFillRoundRect(SurfaceData paramSurfaceData, Region paramRegion, Composite paramComposite, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6, int paramInt7);

  public void fillRoundRect(SunGraphics2D paramSunGraphics2D, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6)
  {
    doFillRoundRect(paramSunGraphics2D.surfaceData, paramSunGraphics2D.getCompClip(), paramSunGraphics2D.composite, paramSunGraphics2D.eargb, paramInt1 + paramSunGraphics2D.transX, paramInt2 + paramSunGraphics2D.transY, paramInt3, paramInt4, paramInt5, paramInt6);
  }

  native void doFillOval(SurfaceData paramSurfaceData, Region paramRegion, Composite paramComposite, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5);

  public void fillOval(SunGraphics2D paramSunGraphics2D, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    doFillOval(paramSunGraphics2D.surfaceData, paramSunGraphics2D.getCompClip(), paramSunGraphics2D.composite, paramSunGraphics2D.eargb, paramInt1 + paramSunGraphics2D.transX, paramInt2 + paramSunGraphics2D.transY, paramInt3, paramInt4);
  }

  native void doFillArc(SurfaceData paramSurfaceData, Region paramRegion, Composite paramComposite, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6, int paramInt7);

  public void fillArc(SunGraphics2D paramSunGraphics2D, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6)
  {
    doFillArc(paramSunGraphics2D.surfaceData, paramSunGraphics2D.getCompClip(), paramSunGraphics2D.composite, paramSunGraphics2D.eargb, paramInt1 + paramSunGraphics2D.transX, paramInt2 + paramSunGraphics2D.transY, paramInt3, paramInt4, paramInt5, paramInt6);
  }

  native void doFillPoly(SurfaceData paramSurfaceData, Region paramRegion, Composite paramComposite, int paramInt1, int paramInt2, int paramInt3, int[] paramArrayOfInt1, int[] paramArrayOfInt2, int paramInt4);

  public void fillPolygon(SunGraphics2D paramSunGraphics2D, int[] paramArrayOfInt1, int[] paramArrayOfInt2, int paramInt)
  {
    doFillPoly(paramSunGraphics2D.surfaceData, paramSunGraphics2D.getCompClip(), paramSunGraphics2D.composite, paramSunGraphics2D.eargb, paramSunGraphics2D.transX, paramSunGraphics2D.transY, paramArrayOfInt1, paramArrayOfInt2, paramInt);
  }

  native void doShape(SurfaceData paramSurfaceData, Region paramRegion, Composite paramComposite, int paramInt1, int paramInt2, int paramInt3, Path2D.Float paramFloat, boolean paramBoolean);

  void doShape(SunGraphics2D paramSunGraphics2D, Shape paramShape, boolean paramBoolean)
  {
    Path2D.Float localFloat;
    int i;
    int j;
    if (paramSunGraphics2D.transformState <= 1)
    {
      if (paramShape instanceof Path2D.Float)
        localFloat = (Path2D.Float)paramShape;
      else
        localFloat = new Path2D.Float(paramShape);
      i = paramSunGraphics2D.transX;
      j = paramSunGraphics2D.transY;
    }
    else
    {
      localFloat = new Path2D.Float(paramShape, paramSunGraphics2D.transform);
      i = 0;
      j = 0;
    }
    doShape(paramSunGraphics2D.surfaceData, paramSunGraphics2D.getCompClip(), paramSunGraphics2D.composite, paramSunGraphics2D.eargb, i, j, localFloat, paramBoolean);
  }

  public void doFillSpans(SunGraphics2D paramSunGraphics2D, SpanIterator paramSpanIterator)
  {
    int[] arrayOfInt = new int[4];
    SurfaceData localSurfaceData = paramSunGraphics2D.surfaceData;
    Region localRegion = paramSunGraphics2D.getCompClip();
    Composite localComposite = paramSunGraphics2D.composite;
    int i = paramSunGraphics2D.eargb;
    while (paramSpanIterator.nextSpan(arrayOfInt))
      doFillRect(localSurfaceData, localRegion, localComposite, i, arrayOfInt[0], arrayOfInt[1], arrayOfInt[2] - arrayOfInt[0], arrayOfInt[3] - arrayOfInt[1]);
  }

  public void draw(SunGraphics2D paramSunGraphics2D, Shape paramShape)
  {
    if (paramSunGraphics2D.strokeState == 0)
    {
      doShape(paramSunGraphics2D, paramShape, false);
    }
    else if (paramSunGraphics2D.strokeState < 3)
    {
      ShapeSpanIterator localShapeSpanIterator = LoopPipe.getStrokeSpans(paramSunGraphics2D, paramShape);
      try
      {
        doFillSpans(paramSunGraphics2D, localShapeSpanIterator);
      }
      finally
      {
        localShapeSpanIterator.dispose();
      }
    }
    else
    {
      doShape(paramSunGraphics2D, paramSunGraphics2D.stroke.createStrokedShape(paramShape), true);
    }
  }

  public void fill(SunGraphics2D paramSunGraphics2D, Shape paramShape)
  {
    doShape(paramSunGraphics2D, paramShape, true);
  }

  public native void devCopyArea(SurfaceData paramSurfaceData, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6);

  public GDIRenderer traceWrap()
  {
    return new Tracer();
  }

  public static class Tracer extends GDIRenderer
  {
    void doDrawLine(SurfaceData paramSurfaceData, Region paramRegion, Composite paramComposite, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5)
    {
      GraphicsPrimitive.tracePrimitive("GDIDrawLine");
      super.doDrawLine(paramSurfaceData, paramRegion, paramComposite, paramInt1, paramInt2, paramInt3, paramInt4, paramInt5);
    }

    void doDrawRect(SurfaceData paramSurfaceData, Region paramRegion, Composite paramComposite, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5)
    {
      GraphicsPrimitive.tracePrimitive("GDIDrawRect");
      super.doDrawRect(paramSurfaceData, paramRegion, paramComposite, paramInt1, paramInt2, paramInt3, paramInt4, paramInt5);
    }

    void doDrawRoundRect(SurfaceData paramSurfaceData, Region paramRegion, Composite paramComposite, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6, int paramInt7)
    {
      GraphicsPrimitive.tracePrimitive("GDIDrawRoundRect");
      super.doDrawRoundRect(paramSurfaceData, paramRegion, paramComposite, paramInt1, paramInt2, paramInt3, paramInt4, paramInt5, paramInt6, paramInt7);
    }

    void doDrawOval(SurfaceData paramSurfaceData, Region paramRegion, Composite paramComposite, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5)
    {
      GraphicsPrimitive.tracePrimitive("GDIDrawOval");
      super.doDrawOval(paramSurfaceData, paramRegion, paramComposite, paramInt1, paramInt2, paramInt3, paramInt4, paramInt5);
    }

    void doDrawArc(SurfaceData paramSurfaceData, Region paramRegion, Composite paramComposite, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6, int paramInt7)
    {
      GraphicsPrimitive.tracePrimitive("GDIDrawArc");
      super.doDrawArc(paramSurfaceData, paramRegion, paramComposite, paramInt1, paramInt2, paramInt3, paramInt4, paramInt5, paramInt6, paramInt7);
    }

    void doDrawPoly(SurfaceData paramSurfaceData, Region paramRegion, Composite paramComposite, int paramInt1, int paramInt2, int paramInt3, int[] paramArrayOfInt1, int[] paramArrayOfInt2, int paramInt4, boolean paramBoolean)
    {
      GraphicsPrimitive.tracePrimitive("GDIDrawPoly");
      super.doDrawPoly(paramSurfaceData, paramRegion, paramComposite, paramInt1, paramInt2, paramInt3, paramArrayOfInt1, paramArrayOfInt2, paramInt4, paramBoolean);
    }

    void doFillRect(SurfaceData paramSurfaceData, Region paramRegion, Composite paramComposite, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5)
    {
      GraphicsPrimitive.tracePrimitive("GDIFillRect");
      super.doFillRect(paramSurfaceData, paramRegion, paramComposite, paramInt1, paramInt2, paramInt3, paramInt4, paramInt5);
    }

    void doFillRoundRect(SurfaceData paramSurfaceData, Region paramRegion, Composite paramComposite, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6, int paramInt7)
    {
      GraphicsPrimitive.tracePrimitive("GDIFillRoundRect");
      super.doFillRoundRect(paramSurfaceData, paramRegion, paramComposite, paramInt1, paramInt2, paramInt3, paramInt4, paramInt5, paramInt6, paramInt7);
    }

    void doFillOval(SurfaceData paramSurfaceData, Region paramRegion, Composite paramComposite, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5)
    {
      GraphicsPrimitive.tracePrimitive("GDIFillOval");
      super.doFillOval(paramSurfaceData, paramRegion, paramComposite, paramInt1, paramInt2, paramInt3, paramInt4, paramInt5);
    }

    void doFillArc(SurfaceData paramSurfaceData, Region paramRegion, Composite paramComposite, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6, int paramInt7)
    {
      GraphicsPrimitive.tracePrimitive("GDIFillArc");
      super.doFillArc(paramSurfaceData, paramRegion, paramComposite, paramInt1, paramInt2, paramInt3, paramInt4, paramInt5, paramInt6, paramInt7);
    }

    void doFillPoly(SurfaceData paramSurfaceData, Region paramRegion, Composite paramComposite, int paramInt1, int paramInt2, int paramInt3, int[] paramArrayOfInt1, int[] paramArrayOfInt2, int paramInt4)
    {
      GraphicsPrimitive.tracePrimitive("GDIFillPoly");
      super.doFillPoly(paramSurfaceData, paramRegion, paramComposite, paramInt1, paramInt2, paramInt3, paramArrayOfInt1, paramArrayOfInt2, paramInt4);
    }

    void doShape(SurfaceData paramSurfaceData, Region paramRegion, Composite paramComposite, int paramInt1, int paramInt2, int paramInt3, Path2D.Float paramFloat, boolean paramBoolean)
    {
      GraphicsPrimitive.tracePrimitive((paramBoolean) ? "GDIFillShape" : "GDIDrawShape");
      super.doShape(paramSurfaceData, paramRegion, paramComposite, paramInt1, paramInt2, paramInt3, paramFloat, paramBoolean);
    }

    public void devCopyArea(SurfaceData paramSurfaceData, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6)
    {
      GraphicsPrimitive.tracePrimitive("GDICopyArea");
      super.devCopyArea(paramSurfaceData, paramInt1, paramInt2, paramInt3, paramInt4, paramInt5, paramInt6);
    }
  }
}