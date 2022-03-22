package sun.java2d.pipe;

import java.awt.Rectangle;
import java.awt.geom.PathIterator;
import sun.dc.path.FastPathProducer;
import sun.dc.path.PathConsumer;
import sun.dc.path.PathException;

public final class ShapeSpanIterator
  implements SpanIterator, PathConsumer
{
  long pData;

  public static native void initIDs();

  public ShapeSpanIterator(boolean paramBoolean)
  {
    setNormalize(paramBoolean);
  }

  public void appendPath(PathIterator paramPathIterator)
  {
    float[] arrayOfFloat = new float[6];
    setRule(paramPathIterator.getWindingRule());
    while (!(paramPathIterator.isDone()))
    {
      addSegment(paramPathIterator.currentSegment(arrayOfFloat), arrayOfFloat);
      paramPathIterator.next();
    }
    endPath();
  }

  public native void appendPoly(int[] paramArrayOfInt1, int[] paramArrayOfInt2, int paramInt1, int paramInt2, int paramInt3);

  private native void setNormalize(boolean paramBoolean);

  public void setOutputAreaXYWH(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    setOutputAreaXYXY(paramInt1, paramInt2, Region.dimAdd(paramInt1, paramInt3), Region.dimAdd(paramInt2, paramInt4));
  }

  public native void setOutputAreaXYXY(int paramInt1, int paramInt2, int paramInt3, int paramInt4);

  public void setOutputArea(Rectangle paramRectangle)
  {
    setOutputAreaXYWH(paramRectangle.x, paramRectangle.y, paramRectangle.width, paramRectangle.height);
  }

  public void setOutputArea(Region paramRegion)
  {
    setOutputAreaXYXY(paramRegion.lox, paramRegion.loy, paramRegion.hix, paramRegion.hiy);
  }

  public native void setRule(int paramInt);

  public native void addSegment(int paramInt, float[] paramArrayOfFloat);

  public native void getPathBox(int[] paramArrayOfInt);

  public native void intersectClipBox(int paramInt1, int paramInt2, int paramInt3, int paramInt4);

  public native boolean nextSpan(int[] paramArrayOfInt);

  public native void skipDownTo(int paramInt);

  public native long getNativeIterator();

  public native void dispose();

  public PathConsumer getConsumer()
  {
    return null;
  }

  public void beginPath()
  {
  }

  public native void beginSubpath(float paramFloat1, float paramFloat2);

  public native void appendLine(float paramFloat1, float paramFloat2);

  public native void appendQuadratic(float paramFloat1, float paramFloat2, float paramFloat3, float paramFloat4);

  public native void appendCubic(float paramFloat1, float paramFloat2, float paramFloat3, float paramFloat4, float paramFloat5, float paramFloat6);

  public void closedSubpath()
  {
  }

  public native void endPath();

  public void useProxy(FastPathProducer paramFastPathProducer)
    throws PathException
  {
    paramFastPathProducer.sendTo(this);
  }

  public native long getCPathConsumer();

  static
  {
    initIDs();
  }
}