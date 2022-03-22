package sun.java2d.opengl;

import java.awt.geom.Path2D.Float;
import sun.java2d.SunGraphics2D;
import sun.java2d.loops.GraphicsPrimitive;
import sun.java2d.pipe.BufferedRenderPipe;
import sun.java2d.pipe.ParallelogramPipe;
import sun.java2d.pipe.RenderBuffer;
import sun.java2d.pipe.RenderQueue;
import sun.java2d.pipe.SpanIterator;

class OGLRenderer extends BufferedRenderPipe
{
  OGLRenderer(RenderQueue paramRenderQueue)
  {
    super(paramRenderQueue);
  }

  protected void validateContext(SunGraphics2D paramSunGraphics2D)
  {
    int i = (paramSunGraphics2D.paint.getTransparency() == 1) ? 1 : 0;
    OGLSurfaceData localOGLSurfaceData = (OGLSurfaceData)paramSunGraphics2D.surfaceData;
    OGLContext.validateContext(localOGLSurfaceData, localOGLSurfaceData, paramSunGraphics2D.getCompClip(), paramSunGraphics2D.composite, null, paramSunGraphics2D.paint, paramSunGraphics2D, i);
  }

  protected void validateContextAA(SunGraphics2D paramSunGraphics2D)
  {
    int i = 0;
    OGLSurfaceData localOGLSurfaceData = (OGLSurfaceData)paramSunGraphics2D.surfaceData;
    OGLContext.validateContext(localOGLSurfaceData, localOGLSurfaceData, paramSunGraphics2D.getCompClip(), paramSunGraphics2D.composite, null, paramSunGraphics2D.paint, paramSunGraphics2D, i);
  }

  void copyArea(SunGraphics2D paramSunGraphics2D, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6)
  {
    this.rq.lock();
    try
    {
      int i = (paramSunGraphics2D.surfaceData.getTransparency() == 1) ? 1 : 0;
      OGLSurfaceData localOGLSurfaceData = (OGLSurfaceData)paramSunGraphics2D.surfaceData;
      OGLContext.validateContext(localOGLSurfaceData, localOGLSurfaceData, paramSunGraphics2D.getCompClip(), paramSunGraphics2D.composite, null, null, null, i);
      this.rq.ensureCapacity(28);
      this.buf.putInt(30);
      this.buf.putInt(paramInt1).putInt(paramInt2).putInt(paramInt3).putInt(paramInt4);
      this.buf.putInt(paramInt5).putInt(paramInt6);
    }
    finally
    {
      this.rq.unlock();
    }
  }

  protected native void drawPoly(int[] paramArrayOfInt1, int[] paramArrayOfInt2, int paramInt1, boolean paramBoolean, int paramInt2, int paramInt3);

  OGLRenderer traceWrap()
  {
    return new Tracer(this, this);
  }

  private class Tracer extends OGLRenderer
  {
    private OGLRenderer oglr;

    Tracer(, OGLRenderer paramOGLRenderer2)
    {
      super(OGLRenderer.access$000(paramOGLRenderer2));
      this.oglr = paramOGLRenderer2;
    }

    public ParallelogramPipe getAAParallelogramPipe()
    {
      ParallelogramPipe localParallelogramPipe = this.oglr.getAAParallelogramPipe();
      return new ParallelogramPipe(this, localParallelogramPipe)
      {
        public void fillParallelogram(, double paramDouble1, double paramDouble2, double paramDouble3, double paramDouble4, double paramDouble5, double paramDouble6)
        {
          GraphicsPrimitive.tracePrimitive("OGLFillAAParallelogram");
          this.val$realpipe.fillParallelogram(paramSunGraphics2D, paramDouble1, paramDouble2, paramDouble3, paramDouble4, paramDouble5, paramDouble6);
        }

        public void drawParallelogram(, double paramDouble1, double paramDouble2, double paramDouble3, double paramDouble4, double paramDouble5, double paramDouble6, double paramDouble7, double paramDouble8)
        {
          GraphicsPrimitive.tracePrimitive("OGLDrawAAParallelogram");
          this.val$realpipe.drawParallelogram(paramSunGraphics2D, paramDouble1, paramDouble2, paramDouble3, paramDouble4, paramDouble5, paramDouble6, paramDouble7, paramDouble8);
        }
      };
    }

    protected void validateContext()
    {
      this.oglr.validateContext(paramSunGraphics2D);
    }

    public void drawLine(, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
    {
      GraphicsPrimitive.tracePrimitive("OGLDrawLine");
      this.oglr.drawLine(paramSunGraphics2D, paramInt1, paramInt2, paramInt3, paramInt4);
    }

    public void drawRect(, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
    {
      GraphicsPrimitive.tracePrimitive("OGLDrawRect");
      this.oglr.drawRect(paramSunGraphics2D, paramInt1, paramInt2, paramInt3, paramInt4);
    }

    protected void drawPoly(, int[] paramArrayOfInt1, int[] paramArrayOfInt2, int paramInt, boolean paramBoolean)
    {
      GraphicsPrimitive.tracePrimitive("OGLDrawPoly");
      OGLRenderer.access$100(this.oglr, paramSunGraphics2D, paramArrayOfInt1, paramArrayOfInt2, paramInt, paramBoolean);
    }

    public void fillRect(, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
    {
      GraphicsPrimitive.tracePrimitive("OGLFillRect");
      this.oglr.fillRect(paramSunGraphics2D, paramInt1, paramInt2, paramInt3, paramInt4);
    }

    protected void drawPath(, Path2D.Float paramFloat, int paramInt1, int paramInt2)
    {
      GraphicsPrimitive.tracePrimitive("OGLDrawPath");
      OGLRenderer.access$200(this.oglr, paramSunGraphics2D, paramFloat, paramInt1, paramInt2);
    }

    protected void fillPath(, Path2D.Float paramFloat, int paramInt1, int paramInt2)
    {
      GraphicsPrimitive.tracePrimitive("OGLFillPath");
      OGLRenderer.access$300(this.oglr, paramSunGraphics2D, paramFloat, paramInt1, paramInt2);
    }

    protected void fillSpans(, SpanIterator paramSpanIterator, int paramInt1, int paramInt2)
    {
      GraphicsPrimitive.tracePrimitive("OGLFillSpans");
      OGLRenderer.access$400(this.oglr, paramSunGraphics2D, paramSpanIterator, paramInt1, paramInt2);
    }

    public void fillParallelogram(, double paramDouble1, double paramDouble2, double paramDouble3, double paramDouble4, double paramDouble5, double paramDouble6)
    {
      GraphicsPrimitive.tracePrimitive("OGLFillParallelogram");
      this.oglr.fillParallelogram(paramSunGraphics2D, paramDouble1, paramDouble2, paramDouble3, paramDouble4, paramDouble5, paramDouble6);
    }

    public void drawParallelogram(, double paramDouble1, double paramDouble2, double paramDouble3, double paramDouble4, double paramDouble5, double paramDouble6, double paramDouble7, double paramDouble8)
    {
      GraphicsPrimitive.tracePrimitive("OGLDrawParallelogram");
      this.oglr.drawParallelogram(paramSunGraphics2D, paramDouble1, paramDouble2, paramDouble3, paramDouble4, paramDouble5, paramDouble6, paramDouble7, paramDouble8);
    }

    public void copyArea(, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6)
    {
      GraphicsPrimitive.tracePrimitive("OGLCopyArea");
      this.oglr.copyArea(paramSunGraphics2D, paramInt1, paramInt2, paramInt3, paramInt4, paramInt5, paramInt6);
    }
  }
}