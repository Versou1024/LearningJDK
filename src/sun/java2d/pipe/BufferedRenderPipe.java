package sun.java2d.pipe;

import java.awt.Polygon;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D.Float;
import java.awt.geom.Ellipse2D.Float;
import java.awt.geom.Path2D.Float;
import java.awt.geom.RoundRectangle2D.Float;
import sun.java2d.SunGraphics2D;
import sun.java2d.loops.ProcessPath;
import sun.java2d.loops.ProcessPath.DrawHandler;

public abstract class BufferedRenderPipe
  implements PixelDrawPipe, PixelFillPipe, ShapeDrawPipe, ParallelogramPipe
{
  ParallelogramPipe aapgrampipe = new AAParallelogramPipe(this, null);
  static final int BYTES_PER_POLY_POINT = 8;
  static final int BYTES_PER_SCANLINE = 12;
  static final int BYTES_PER_SPAN = 16;
  protected RenderQueue rq;
  protected RenderBuffer buf;
  private BufferedDrawHandler drawHandler;

  public BufferedRenderPipe(RenderQueue paramRenderQueue)
  {
    this.rq = paramRenderQueue;
    this.buf = paramRenderQueue.getBuffer();
    this.drawHandler = new BufferedDrawHandler(this);
  }

  public ParallelogramPipe getAAParallelogramPipe()
  {
    return this.aapgrampipe;
  }

  protected abstract void validateContext(SunGraphics2D paramSunGraphics2D);

  protected abstract void validateContextAA(SunGraphics2D paramSunGraphics2D);

  public void drawLine(SunGraphics2D paramSunGraphics2D, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    int i = paramSunGraphics2D.transX;
    int j = paramSunGraphics2D.transY;
    this.rq.lock();
    try
    {
      validateContext(paramSunGraphics2D);
      this.rq.ensureCapacity(20);
      this.buf.putInt(10);
      this.buf.putInt(paramInt1 + i);
      this.buf.putInt(paramInt2 + j);
      this.buf.putInt(paramInt3 + i);
      this.buf.putInt(paramInt4 + j);
    }
    finally
    {
      this.rq.unlock();
    }
  }

  public void drawRect(SunGraphics2D paramSunGraphics2D, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    this.rq.lock();
    try
    {
      validateContext(paramSunGraphics2D);
      this.rq.ensureCapacity(20);
      this.buf.putInt(11);
      this.buf.putInt(paramInt1 + paramSunGraphics2D.transX);
      this.buf.putInt(paramInt2 + paramSunGraphics2D.transY);
      this.buf.putInt(paramInt3);
      this.buf.putInt(paramInt4);
    }
    finally
    {
      this.rq.unlock();
    }
  }

  public void fillRect(SunGraphics2D paramSunGraphics2D, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    this.rq.lock();
    try
    {
      validateContext(paramSunGraphics2D);
      this.rq.ensureCapacity(20);
      this.buf.putInt(20);
      this.buf.putInt(paramInt1 + paramSunGraphics2D.transX);
      this.buf.putInt(paramInt2 + paramSunGraphics2D.transY);
      this.buf.putInt(paramInt3);
      this.buf.putInt(paramInt4);
    }
    finally
    {
      this.rq.unlock();
    }
  }

  public void drawRoundRect(SunGraphics2D paramSunGraphics2D, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6)
  {
    draw(paramSunGraphics2D, new RoundRectangle2D.Float(paramInt1, paramInt2, paramInt3, paramInt4, paramInt5, paramInt6));
  }

  public void fillRoundRect(SunGraphics2D paramSunGraphics2D, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6)
  {
    fill(paramSunGraphics2D, new RoundRectangle2D.Float(paramInt1, paramInt2, paramInt3, paramInt4, paramInt5, paramInt6));
  }

  public void drawOval(SunGraphics2D paramSunGraphics2D, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    draw(paramSunGraphics2D, new Ellipse2D.Float(paramInt1, paramInt2, paramInt3, paramInt4));
  }

  public void fillOval(SunGraphics2D paramSunGraphics2D, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    fill(paramSunGraphics2D, new Ellipse2D.Float(paramInt1, paramInt2, paramInt3, paramInt4));
  }

  public void drawArc(SunGraphics2D paramSunGraphics2D, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6)
  {
    draw(paramSunGraphics2D, new Arc2D.Float(paramInt1, paramInt2, paramInt3, paramInt4, paramInt5, paramInt6, 0));
  }

  public void fillArc(SunGraphics2D paramSunGraphics2D, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6)
  {
    fill(paramSunGraphics2D, new Arc2D.Float(paramInt1, paramInt2, paramInt3, paramInt4, paramInt5, paramInt6, 2));
  }

  protected void drawPoly(SunGraphics2D paramSunGraphics2D, int[] paramArrayOfInt1, int[] paramArrayOfInt2, int paramInt, boolean paramBoolean)
  {
    if ((paramArrayOfInt1 == null) || (paramArrayOfInt2 == null))
      throw new NullPointerException("coordinate array");
    if ((paramArrayOfInt1.length < paramInt) || (paramArrayOfInt2.length < paramInt))
      throw new ArrayIndexOutOfBoundsException("coordinate array");
    if (paramInt < 2)
      return;
    if ((paramInt == 2) && (!(paramBoolean)))
    {
      drawLine(paramSunGraphics2D, paramArrayOfInt1[0], paramArrayOfInt2[0], paramArrayOfInt1[1], paramArrayOfInt2[1]);
      return;
    }
    this.rq.lock();
    try
    {
      validateContext(paramSunGraphics2D);
      int i = paramInt * 8;
      int j = 20 + i;
      if (j <= this.buf.capacity())
      {
        if (j > this.buf.remaining())
          this.rq.flushNow();
        this.buf.putInt(12);
        this.buf.putInt(paramInt);
        this.buf.putInt((paramBoolean) ? 1 : 0);
        this.buf.putInt(paramSunGraphics2D.transX);
        this.buf.putInt(paramSunGraphics2D.transY);
        this.buf.put(paramArrayOfInt1, 0, paramInt);
        this.buf.put(paramArrayOfInt2, 0, paramInt);
      }
      else
      {
        this.rq.flushAndInvokeNow(new Runnable(this, paramArrayOfInt1, paramArrayOfInt2, paramInt, paramBoolean, paramSunGraphics2D)
        {
          public void run()
          {
            this.this$0.drawPoly(this.val$xPoints, this.val$yPoints, this.val$nPoints, this.val$isClosed, this.val$sg2d.transX, this.val$sg2d.transY);
          }
        });
      }
    }
    finally
    {
      this.rq.unlock();
    }
  }

  protected abstract void drawPoly(int[] paramArrayOfInt1, int[] paramArrayOfInt2, int paramInt1, boolean paramBoolean, int paramInt2, int paramInt3);

  public void drawPolyline(SunGraphics2D paramSunGraphics2D, int[] paramArrayOfInt1, int[] paramArrayOfInt2, int paramInt)
  {
    drawPoly(paramSunGraphics2D, paramArrayOfInt1, paramArrayOfInt2, paramInt, false);
  }

  public void drawPolygon(SunGraphics2D paramSunGraphics2D, int[] paramArrayOfInt1, int[] paramArrayOfInt2, int paramInt)
  {
    drawPoly(paramSunGraphics2D, paramArrayOfInt1, paramArrayOfInt2, paramInt, true);
  }

  public void fillPolygon(SunGraphics2D paramSunGraphics2D, int[] paramArrayOfInt1, int[] paramArrayOfInt2, int paramInt)
  {
    fill(paramSunGraphics2D, new Polygon(paramArrayOfInt1, paramArrayOfInt2, paramInt));
  }

  protected void drawPath(SunGraphics2D paramSunGraphics2D, Path2D.Float paramFloat, int paramInt1, int paramInt2)
  {
    this.rq.lock();
    try
    {
      validateContext(paramSunGraphics2D);
      this.drawHandler.validate(paramSunGraphics2D);
      ProcessPath.drawPath(this.drawHandler, paramFloat, paramInt1, paramInt2);
    }
    finally
    {
      this.rq.unlock();
    }
  }

  protected void fillPath(SunGraphics2D paramSunGraphics2D, Path2D.Float paramFloat, int paramInt1, int paramInt2)
  {
    this.rq.lock();
    try
    {
      validateContext(paramSunGraphics2D);
      this.drawHandler.validate(paramSunGraphics2D);
      this.drawHandler.startFillPath();
      ProcessPath.fillPath(this.drawHandler, paramFloat, paramInt1, paramInt2);
      this.drawHandler.endFillPath();
    }
    finally
    {
      this.rq.unlock();
    }
  }

  private native int fillSpans(RenderQueue paramRenderQueue, long paramLong1, int paramInt1, int paramInt2, SpanIterator paramSpanIterator, long paramLong2, int paramInt3, int paramInt4);

  protected void fillSpans(SunGraphics2D paramSunGraphics2D, SpanIterator paramSpanIterator, int paramInt1, int paramInt2)
  {
    this.rq.lock();
    try
    {
      validateContext(paramSunGraphics2D);
      this.rq.ensureCapacity(24);
      int i = fillSpans(this.rq, this.buf.getAddress(), this.buf.position(), this.buf.capacity(), paramSpanIterator, paramSpanIterator.getNativeIterator(), paramInt1, paramInt2);
      this.buf.position(i);
    }
    finally
    {
      this.rq.unlock();
    }
  }

  public void fillParallelogram(SunGraphics2D paramSunGraphics2D, double paramDouble1, double paramDouble2, double paramDouble3, double paramDouble4, double paramDouble5, double paramDouble6)
  {
    this.rq.lock();
    try
    {
      validateContext(paramSunGraphics2D);
      this.rq.ensureCapacity(28);
      this.buf.putInt(22);
      this.buf.putFloat((float)paramDouble1);
      this.buf.putFloat((float)paramDouble2);
      this.buf.putFloat((float)paramDouble3);
      this.buf.putFloat((float)paramDouble4);
      this.buf.putFloat((float)paramDouble5);
      this.buf.putFloat((float)paramDouble6);
    }
    finally
    {
      this.rq.unlock();
    }
  }

  public void drawParallelogram(SunGraphics2D paramSunGraphics2D, double paramDouble1, double paramDouble2, double paramDouble3, double paramDouble4, double paramDouble5, double paramDouble6, double paramDouble7, double paramDouble8)
  {
    this.rq.lock();
    try
    {
      validateContext(paramSunGraphics2D);
      this.rq.ensureCapacity(36);
      this.buf.putInt(15);
      this.buf.putFloat((float)paramDouble1);
      this.buf.putFloat((float)paramDouble2);
      this.buf.putFloat((float)paramDouble3);
      this.buf.putFloat((float)paramDouble4);
      this.buf.putFloat((float)paramDouble5);
      this.buf.putFloat((float)paramDouble6);
      this.buf.putFloat((float)paramDouble7);
      this.buf.putFloat((float)paramDouble8);
    }
    finally
    {
      this.rq.unlock();
    }
  }

  public void draw(SunGraphics2D paramSunGraphics2D, Shape paramShape)
  {
    Object localObject1;
    if (paramSunGraphics2D.strokeState == 0)
    {
      int i;
      int j;
      if ((paramShape instanceof Polygon) && (paramSunGraphics2D.transformState < 3))
      {
        localObject1 = (Polygon)paramShape;
        drawPolygon(paramSunGraphics2D, ((Polygon)localObject1).xpoints, ((Polygon)localObject1).ypoints, ((Polygon)localObject1).npoints);
        return;
      }
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
      drawPath(paramSunGraphics2D, (Path2D.Float)localObject1, i, j);
    }
    else if (paramSunGraphics2D.strokeState < 3)
    {
      localObject1 = LoopPipe.getStrokeSpans(paramSunGraphics2D, paramShape);
      try
      {
        fillSpans(paramSunGraphics2D, (SpanIterator)localObject1, 0, 0);
      }
      finally
      {
        ((ShapeSpanIterator)localObject1).dispose();
      }
    }
    else
    {
      fill(paramSunGraphics2D, paramSunGraphics2D.stroke.createStrokedShape(paramShape));
    }
  }

  public void fill(SunGraphics2D paramSunGraphics2D, Shape paramShape)
  {
    int i;
    int j;
    Object localObject1;
    if (paramSunGraphics2D.strokeState == 0)
    {
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
      fillPath(paramSunGraphics2D, (Path2D.Float)localObject1, i, j);
      return;
    }
    if (paramSunGraphics2D.transformState <= 1)
    {
      localObject1 = null;
      i = paramSunGraphics2D.transX;
      j = paramSunGraphics2D.transY;
    }
    else
    {
      localObject1 = paramSunGraphics2D.transform;
      i = j = 0;
    }
    ShapeSpanIterator localShapeSpanIterator = LoopPipe.getFillSSI(paramSunGraphics2D);
    try
    {
      Region localRegion = paramSunGraphics2D.getCompClip();
      localShapeSpanIterator.setOutputAreaXYXY(localRegion.getLoX() - i, localRegion.getLoY() - j, localRegion.getHiX() - i, localRegion.getHiY() - j);
      localShapeSpanIterator.appendPath(paramShape.getPathIterator((AffineTransform)localObject1));
      fillSpans(paramSunGraphics2D, localShapeSpanIterator, i, j);
    }
    finally
    {
      localShapeSpanIterator.dispose();
    }
  }

  private class AAParallelogramPipe
  implements ParallelogramPipe
  {
    public void fillParallelogram(, double paramDouble1, double paramDouble2, double paramDouble3, double paramDouble4, double paramDouble5, double paramDouble6)
    {
      this.this$0.rq.lock();
      try
      {
        this.this$0.validateContextAA(paramSunGraphics2D);
        this.this$0.rq.ensureCapacity(28);
        this.this$0.buf.putInt(23);
        this.this$0.buf.putFloat((float)paramDouble1);
        this.this$0.buf.putFloat((float)paramDouble2);
        this.this$0.buf.putFloat((float)paramDouble3);
        this.this$0.buf.putFloat((float)paramDouble4);
        this.this$0.buf.putFloat((float)paramDouble5);
        this.this$0.buf.putFloat((float)paramDouble6);
      }
      finally
      {
        this.this$0.rq.unlock();
      }
    }

    public void drawParallelogram(, double paramDouble1, double paramDouble2, double paramDouble3, double paramDouble4, double paramDouble5, double paramDouble6, double paramDouble7, double paramDouble8)
    {
      this.this$0.rq.lock();
      try
      {
        this.this$0.validateContextAA(paramSunGraphics2D);
        this.this$0.rq.ensureCapacity(36);
        this.this$0.buf.putInt(16);
        this.this$0.buf.putFloat((float)paramDouble1);
        this.this$0.buf.putFloat((float)paramDouble2);
        this.this$0.buf.putFloat((float)paramDouble3);
        this.this$0.buf.putFloat((float)paramDouble4);
        this.this$0.buf.putFloat((float)paramDouble5);
        this.this$0.buf.putFloat((float)paramDouble6);
        this.this$0.buf.putFloat((float)paramDouble7);
        this.this$0.buf.putFloat((float)paramDouble8);
      }
      finally
      {
        this.this$0.rq.unlock();
      }
    }
  }

  private class BufferedDrawHandler extends ProcessPath.DrawHandler
  {
    private int scanlineCount;
    private int scanlineCountIndex;
    private int remainingScanlines;

    BufferedDrawHandler()
    {
      super(0, 0, 0, 0);
    }

    void validate()
    {
      Region localRegion = paramSunGraphics2D.getCompClip();
      setBounds(localRegion.getLoX(), localRegion.getLoY(), localRegion.getHiX(), localRegion.getHiY(), paramSunGraphics2D.strokeHint);
    }

    public void drawLine(, int paramInt2, int paramInt3, int paramInt4)
    {
      this.this$0.rq.ensureCapacity(20);
      this.this$0.buf.putInt(10);
      this.this$0.buf.putInt(paramInt1);
      this.this$0.buf.putInt(paramInt2);
      this.this$0.buf.putInt(paramInt3);
      this.this$0.buf.putInt(paramInt4);
    }

    public void drawPixel(, int paramInt2)
    {
      this.this$0.rq.ensureCapacity(12);
      this.this$0.buf.putInt(13);
      this.this$0.buf.putInt(paramInt1);
      this.this$0.buf.putInt(paramInt2);
    }

    private void resetFillPath()
    {
      this.this$0.buf.putInt(14);
      this.scanlineCountIndex = this.this$0.buf.position();
      this.this$0.buf.putInt(0);
      this.scanlineCount = 0;
      this.remainingScanlines = (this.this$0.buf.remaining() / 12);
    }

    private void updateScanlineCount()
    {
      this.this$0.buf.putInt(this.scanlineCountIndex, this.scanlineCount);
    }

    public void startFillPath()
    {
      this.this$0.rq.ensureCapacity(20);
      resetFillPath();
    }

    public void drawScanline(, int paramInt2, int paramInt3)
    {
      if (this.remainingScanlines == 0)
      {
        updateScanlineCount();
        this.this$0.rq.flushNow();
        resetFillPath();
      }
      this.this$0.buf.putInt(paramInt1);
      this.this$0.buf.putInt(paramInt2);
      this.this$0.buf.putInt(paramInt3);
      this.scanlineCount += 1;
      this.remainingScanlines -= 1;
    }

    public void endFillPath()
    {
      updateScanlineCount();
    }
  }
}