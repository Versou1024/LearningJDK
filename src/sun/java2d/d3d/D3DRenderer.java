package sun.java2d.d3d;

import java.awt.geom.Path2D.Float;
import sun.java2d.SunGraphics2D;
import sun.java2d.loops.GraphicsPrimitive;
import sun.java2d.pipe.BufferedRenderPipe;
import sun.java2d.pipe.ParallelogramPipe;
import sun.java2d.pipe.RenderBuffer;
import sun.java2d.pipe.RenderQueue;
import sun.java2d.pipe.SpanIterator;

class D3DRenderer extends BufferedRenderPipe
{
  D3DRenderer(RenderQueue paramRenderQueue)
  {
    super(paramRenderQueue);
  }

  protected void validateContext(SunGraphics2D paramSunGraphics2D)
  {
    int i = (paramSunGraphics2D.paint.getTransparency() == 1) ? 1 : 0;
    D3DSurfaceData localD3DSurfaceData = (D3DSurfaceData)paramSunGraphics2D.surfaceData;
    D3DContext.validateContext(localD3DSurfaceData, localD3DSurfaceData, paramSunGraphics2D.getCompClip(), paramSunGraphics2D.composite, null, paramSunGraphics2D.paint, paramSunGraphics2D, i);
  }

  protected void validateContextAA(SunGraphics2D paramSunGraphics2D)
  {
    int i = 0;
    D3DSurfaceData localD3DSurfaceData = (D3DSurfaceData)paramSunGraphics2D.surfaceData;
    D3DContext.validateContext(localD3DSurfaceData, localD3DSurfaceData, paramSunGraphics2D.getCompClip(), paramSunGraphics2D.composite, null, paramSunGraphics2D.paint, paramSunGraphics2D, i);
  }

  void copyArea(SunGraphics2D paramSunGraphics2D, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6)
  {
    this.rq.lock();
    try
    {
      int i = (paramSunGraphics2D.surfaceData.getTransparency() == 1) ? 1 : 0;
      D3DSurfaceData localD3DSurfaceData = (D3DSurfaceData)paramSunGraphics2D.surfaceData;
      D3DContext.validateContext(localD3DSurfaceData, localD3DSurfaceData, paramSunGraphics2D.getCompClip(), paramSunGraphics2D.composite, null, null, null, i);
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

  D3DRenderer traceWrap()
  {
    return new Tracer(this, this);
  }

  private class Tracer extends D3DRenderer
  {
    private D3DRenderer d3dr;

    Tracer(, D3DRenderer paramD3DRenderer2)
    {
      super(D3DRenderer.access$000(paramD3DRenderer2));
      this.d3dr = paramD3DRenderer2;
    }

    public ParallelogramPipe getAAParallelogramPipe()
    {
      ParallelogramPipe localParallelogramPipe = this.d3dr.getAAParallelogramPipe();
      return new ParallelogramPipe(this, localParallelogramPipe)
      {
        public void fillParallelogram(, double paramDouble1, double paramDouble2, double paramDouble3, double paramDouble4, double paramDouble5, double paramDouble6)
        {
          GraphicsPrimitive.tracePrimitive("D3DFillAAParallelogram");
          this.val$realpipe.fillParallelogram(paramSunGraphics2D, paramDouble1, paramDouble2, paramDouble3, paramDouble4, paramDouble5, paramDouble6);
        }

        public void drawParallelogram(, double paramDouble1, double paramDouble2, double paramDouble3, double paramDouble4, double paramDouble5, double paramDouble6, double paramDouble7, double paramDouble8)
        {
          GraphicsPrimitive.tracePrimitive("D3DDrawAAParallelogram");
          this.val$realpipe.drawParallelogram(paramSunGraphics2D, paramDouble1, paramDouble2, paramDouble3, paramDouble4, paramDouble5, paramDouble6, paramDouble7, paramDouble8);
        }
      };
    }

    protected void validateContext()
    {
      this.d3dr.validateContext(paramSunGraphics2D);
    }

    public void drawLine(, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
    {
      GraphicsPrimitive.tracePrimitive("D3DDrawLine");
      this.d3dr.drawLine(paramSunGraphics2D, paramInt1, paramInt2, paramInt3, paramInt4);
    }

    public void drawRect(, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
    {
      GraphicsPrimitive.tracePrimitive("D3DDrawRect");
      this.d3dr.drawRect(paramSunGraphics2D, paramInt1, paramInt2, paramInt3, paramInt4);
    }

    protected void drawPoly(, int[] paramArrayOfInt1, int[] paramArrayOfInt2, int paramInt, boolean paramBoolean)
    {
      GraphicsPrimitive.tracePrimitive("D3DDrawPoly");
      D3DRenderer.access$100(this.d3dr, paramSunGraphics2D, paramArrayOfInt1, paramArrayOfInt2, paramInt, paramBoolean);
    }

    public void fillRect(, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
    {
      GraphicsPrimitive.tracePrimitive("D3DFillRect");
      this.d3dr.fillRect(paramSunGraphics2D, paramInt1, paramInt2, paramInt3, paramInt4);
    }

    protected void drawPath(, Path2D.Float paramFloat, int paramInt1, int paramInt2)
    {
      GraphicsPrimitive.tracePrimitive("D3DDrawPath");
      D3DRenderer.access$200(this.d3dr, paramSunGraphics2D, paramFloat, paramInt1, paramInt2);
    }

    protected void fillPath(, Path2D.Float paramFloat, int paramInt1, int paramInt2)
    {
      GraphicsPrimitive.tracePrimitive("D3DFillPath");
      D3DRenderer.access$300(this.d3dr, paramSunGraphics2D, paramFloat, paramInt1, paramInt2);
    }

    protected void fillSpans(, SpanIterator paramSpanIterator, int paramInt1, int paramInt2)
    {
      GraphicsPrimitive.tracePrimitive("D3DFillSpans");
      D3DRenderer.access$400(this.d3dr, paramSunGraphics2D, paramSpanIterator, paramInt1, paramInt2);
    }

    public void fillParallelogram(, double paramDouble1, double paramDouble2, double paramDouble3, double paramDouble4, double paramDouble5, double paramDouble6)
    {
      GraphicsPrimitive.tracePrimitive("D3DFillParallelogram");
      this.d3dr.fillParallelogram(paramSunGraphics2D, paramDouble1, paramDouble2, paramDouble3, paramDouble4, paramDouble5, paramDouble6);
    }

    public void drawParallelogram(, double paramDouble1, double paramDouble2, double paramDouble3, double paramDouble4, double paramDouble5, double paramDouble6, double paramDouble7, double paramDouble8)
    {
      GraphicsPrimitive.tracePrimitive("D3DDrawParallelogram");
      this.d3dr.drawParallelogram(paramSunGraphics2D, paramDouble1, paramDouble2, paramDouble3, paramDouble4, paramDouble5, paramDouble6, paramDouble7, paramDouble8);
    }

    public void copyArea(, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6)
    {
      GraphicsPrimitive.tracePrimitive("D3DCopyArea");
      this.d3dr.copyArea(paramSunGraphics2D, paramInt1, paramInt2, paramInt3, paramInt4, paramInt5, paramInt6);
    }
  }
}