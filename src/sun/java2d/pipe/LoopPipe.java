package sun.java2d.pipe;

import java.awt.BasicStroke;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D.Float;
import java.awt.geom.Ellipse2D.Float;
import java.awt.geom.Path2D.Float;
import java.awt.geom.PathIterator;
import java.awt.geom.RoundRectangle2D.Float;
import sun.dc.path.PathConsumer;
import sun.dc.path.PathException;
import sun.java2d.SunGraphics2D;
import sun.java2d.SurfaceData;
import sun.java2d.loops.DrawLine;
import sun.java2d.loops.DrawPath;
import sun.java2d.loops.DrawPolygons;
import sun.java2d.loops.DrawRect;
import sun.java2d.loops.FillPath;
import sun.java2d.loops.FillRect;
import sun.java2d.loops.FillSpans;
import sun.java2d.loops.RenderLoops;

public class LoopPipe
  implements PixelDrawPipe, PixelFillPipe, ShapeDrawPipe
{
  public void drawLine(SunGraphics2D paramSunGraphics2D, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    int i = paramSunGraphics2D.transX;
    int j = paramSunGraphics2D.transY;
    paramSunGraphics2D.loops.drawLineLoop.DrawLine(paramSunGraphics2D, paramSunGraphics2D.getSurfaceData(), paramInt1 + i, paramInt2 + j, paramInt3 + i, paramInt4 + j);
  }

  public void drawRect(SunGraphics2D paramSunGraphics2D, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    paramSunGraphics2D.loops.drawRectLoop.DrawRect(paramSunGraphics2D, paramSunGraphics2D.getSurfaceData(), paramInt1 + paramSunGraphics2D.transX, paramInt2 + paramSunGraphics2D.transY, paramInt3, paramInt4);
  }

  public void drawRoundRect(SunGraphics2D paramSunGraphics2D, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6)
  {
    paramSunGraphics2D.shapepipe.draw(paramSunGraphics2D, new RoundRectangle2D.Float(paramInt1, paramInt2, paramInt3, paramInt4, paramInt5, paramInt6));
  }

  public void drawOval(SunGraphics2D paramSunGraphics2D, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    paramSunGraphics2D.shapepipe.draw(paramSunGraphics2D, new Ellipse2D.Float(paramInt1, paramInt2, paramInt3, paramInt4));
  }

  public void drawArc(SunGraphics2D paramSunGraphics2D, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6)
  {
    paramSunGraphics2D.shapepipe.draw(paramSunGraphics2D, new Arc2D.Float(paramInt1, paramInt2, paramInt3, paramInt4, paramInt5, paramInt6, 0));
  }

  public void drawPolyline(SunGraphics2D paramSunGraphics2D, int[] paramArrayOfInt1, int[] paramArrayOfInt2, int paramInt)
  {
    int[] arrayOfInt = { paramInt };
    paramSunGraphics2D.loops.drawPolygonsLoop.DrawPolygons(paramSunGraphics2D, paramSunGraphics2D.getSurfaceData(), paramArrayOfInt1, paramArrayOfInt2, arrayOfInt, 1, paramSunGraphics2D.transX, paramSunGraphics2D.transY, false);
  }

  public void drawPolygon(SunGraphics2D paramSunGraphics2D, int[] paramArrayOfInt1, int[] paramArrayOfInt2, int paramInt)
  {
    int[] arrayOfInt = { paramInt };
    paramSunGraphics2D.loops.drawPolygonsLoop.DrawPolygons(paramSunGraphics2D, paramSunGraphics2D.getSurfaceData(), paramArrayOfInt1, paramArrayOfInt2, arrayOfInt, 1, paramSunGraphics2D.transX, paramSunGraphics2D.transY, true);
  }

  public void fillRect(SunGraphics2D paramSunGraphics2D, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    paramSunGraphics2D.loops.fillRectLoop.FillRect(paramSunGraphics2D, paramSunGraphics2D.getSurfaceData(), paramInt1 + paramSunGraphics2D.transX, paramInt2 + paramSunGraphics2D.transY, paramInt3, paramInt4);
  }

  public void fillRoundRect(SunGraphics2D paramSunGraphics2D, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6)
  {
    paramSunGraphics2D.shapepipe.fill(paramSunGraphics2D, new RoundRectangle2D.Float(paramInt1, paramInt2, paramInt3, paramInt4, paramInt5, paramInt6));
  }

  public void fillOval(SunGraphics2D paramSunGraphics2D, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    paramSunGraphics2D.shapepipe.fill(paramSunGraphics2D, new Ellipse2D.Float(paramInt1, paramInt2, paramInt3, paramInt4));
  }

  public void fillArc(SunGraphics2D paramSunGraphics2D, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6)
  {
    paramSunGraphics2D.shapepipe.fill(paramSunGraphics2D, new Arc2D.Float(paramInt1, paramInt2, paramInt3, paramInt4, paramInt5, paramInt6, 2));
  }

  public void fillPolygon(SunGraphics2D paramSunGraphics2D, int[] paramArrayOfInt1, int[] paramArrayOfInt2, int paramInt)
  {
    ShapeSpanIterator localShapeSpanIterator = getFillSSI(paramSunGraphics2D);
    try
    {
      localShapeSpanIterator.setOutputArea(paramSunGraphics2D.getCompClip());
      localShapeSpanIterator.appendPoly(paramArrayOfInt1, paramArrayOfInt2, paramInt, paramSunGraphics2D.transX, paramSunGraphics2D.transY);
      fillSpans(paramSunGraphics2D, localShapeSpanIterator);
    }
    finally
    {
      localShapeSpanIterator.dispose();
    }
  }

  public void draw(SunGraphics2D paramSunGraphics2D, Shape paramShape)
  {
    if (paramSunGraphics2D.strokeState == 0)
    {
      int i;
      int j;
      if (paramSunGraphics2D.transformState <= 1)
      {
        if (paramShape instanceof Path2D.Float)
          localObject1 = (Path2D.Float)paramShape;
        else
          localObject1 = new Path2D.Float(paramShape);
        i = paramSunGraphics2D.transX;
        j = paramSunGraphics2D.transY;
      }
      else
      {
        localObject1 = new Path2D.Float(paramShape, paramSunGraphics2D.transform);
        i = 0;
        j = 0;
      }
      paramSunGraphics2D.loops.drawPathLoop.DrawPath(paramSunGraphics2D, paramSunGraphics2D.getSurfaceData(), i, j, (Path2D.Float)localObject1);
      return;
    }
    if (paramSunGraphics2D.strokeState == 3)
    {
      fill(paramSunGraphics2D, paramSunGraphics2D.stroke.createStrokedShape(paramShape));
      return;
    }
    Object localObject1 = getStrokeSpans(paramSunGraphics2D, paramShape);
    try
    {
      fillSpans(paramSunGraphics2D, (SpanIterator)localObject1);
    }
    finally
    {
      ((ShapeSpanIterator)localObject1).dispose();
    }
  }

  public static ShapeSpanIterator getFillSSI(SunGraphics2D paramSunGraphics2D)
  {
    boolean bool = (paramSunGraphics2D.stroke instanceof BasicStroke) && (paramSunGraphics2D.strokeHint != 2);
    return new ShapeSpanIterator(bool);
  }

  public static ShapeSpanIterator getStrokeSpans(SunGraphics2D paramSunGraphics2D, Shape paramShape)
  {
    ShapeSpanIterator localShapeSpanIterator = new ShapeSpanIterator(false);
    try
    {
      localShapeSpanIterator.setOutputArea(paramSunGraphics2D.getCompClip());
      localShapeSpanIterator.setRule(1);
      BasicStroke localBasicStroke = (BasicStroke)paramSunGraphics2D.stroke;
      AffineTransform localAffineTransform = (paramSunGraphics2D.transformState >= 3) ? paramSunGraphics2D.transform : null;
      boolean bool1 = paramSunGraphics2D.strokeState <= 1;
      PathConsumer localPathConsumer = DuctusRenderer.createStroker(localShapeSpanIterator, localBasicStroke, bool1, localAffineTransform);
      try
      {
        localAffineTransform = (paramSunGraphics2D.transformState == 0) ? null : paramSunGraphics2D.transform;
        PathIterator localPathIterator = paramShape.getPathIterator(localAffineTransform);
        boolean bool2 = paramSunGraphics2D.strokeHint != 2;
        DuctusRenderer.feedConsumer(localPathIterator, localPathConsumer, bool2, 0.25F);
      }
      catch (PathException localPathException)
      {
      }
      finally
      {
        DuctusRenderer.disposeStroker(localPathConsumer, localShapeSpanIterator);
      }
    }
    catch (Throwable localThrowable)
    {
      localShapeSpanIterator.dispose();
      localShapeSpanIterator = null;
      throw new InternalError("Unable to Stroke shape (" + localThrowable.getMessage() + ")");
    }
    return localShapeSpanIterator;
  }

  public void fill(SunGraphics2D paramSunGraphics2D, Shape paramShape)
  {
    if (paramSunGraphics2D.strokeState == 0)
    {
      int i;
      int j;
      if (paramSunGraphics2D.transformState <= 1)
      {
        if (paramShape instanceof Path2D.Float)
          localObject1 = (Path2D.Float)paramShape;
        else
          localObject1 = new Path2D.Float(paramShape);
        i = paramSunGraphics2D.transX;
        j = paramSunGraphics2D.transY;
      }
      else
      {
        localObject1 = new Path2D.Float(paramShape, paramSunGraphics2D.transform);
        i = 0;
        j = 0;
      }
      paramSunGraphics2D.loops.fillPathLoop.FillPath(paramSunGraphics2D, paramSunGraphics2D.getSurfaceData(), i, j, (Path2D.Float)localObject1);
      return;
    }
    Object localObject1 = getFillSSI(paramSunGraphics2D);
    try
    {
      ((ShapeSpanIterator)localObject1).setOutputArea(paramSunGraphics2D.getCompClip());
      AffineTransform localAffineTransform = (paramSunGraphics2D.transformState == 0) ? null : paramSunGraphics2D.transform;
      ((ShapeSpanIterator)localObject1).appendPath(paramShape.getPathIterator(localAffineTransform));
      fillSpans(paramSunGraphics2D, (SpanIterator)localObject1);
    }
    finally
    {
      ((ShapeSpanIterator)localObject1).dispose();
    }
  }

  private static void fillSpans(SunGraphics2D paramSunGraphics2D, SpanIterator paramSpanIterator)
  {
    if (paramSunGraphics2D.clipState == 2)
    {
      paramSpanIterator = paramSunGraphics2D.clipRegion.filter(paramSpanIterator);
    }
    else
    {
      localObject = paramSunGraphics2D.loops.fillSpansLoop;
      if (localObject != null)
      {
        ((FillSpans)localObject).FillSpans(paramSunGraphics2D, paramSunGraphics2D.getSurfaceData(), paramSpanIterator);
        return;
      }
    }
    Object localObject = new int[4];
    SurfaceData localSurfaceData = paramSunGraphics2D.getSurfaceData();
    while (paramSpanIterator.nextSpan(localObject))
    {
      int i = localObject[0];
      int j = localObject[1];
      int k = localObject[2] - i;
      int l = localObject[3] - j;
      paramSunGraphics2D.loops.fillRectLoop.FillRect(paramSunGraphics2D, localSurfaceData, i, j, k, l);
    }
  }
}