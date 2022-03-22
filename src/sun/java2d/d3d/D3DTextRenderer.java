package sun.java2d.d3d;

import java.awt.Composite;
import sun.font.GlyphList;
import sun.java2d.SunGraphics2D;
import sun.java2d.loops.GraphicsPrimitive;
import sun.java2d.pipe.BufferedTextPipe;
import sun.java2d.pipe.RenderQueue;

class D3DTextRenderer extends BufferedTextPipe
{
  D3DTextRenderer(RenderQueue paramRenderQueue)
  {
    super(paramRenderQueue);
  }

  protected native void drawGlyphList(int paramInt1, boolean paramBoolean1, boolean paramBoolean2, boolean paramBoolean3, int paramInt2, float paramFloat1, float paramFloat2, long[] paramArrayOfLong, float[] paramArrayOfFloat);

  protected void validateContext(SunGraphics2D paramSunGraphics2D, Composite paramComposite)
  {
    D3DSurfaceData localD3DSurfaceData = (D3DSurfaceData)paramSunGraphics2D.surfaceData;
    D3DContext.validateContext(localD3DSurfaceData, localD3DSurfaceData, paramSunGraphics2D.getCompClip(), paramComposite, null, paramSunGraphics2D.paint, paramSunGraphics2D, 0);
  }

  D3DTextRenderer traceWrap()
  {
    return new Tracer(this);
  }

  private static class Tracer extends D3DTextRenderer
  {
    Tracer(D3DTextRenderer paramD3DTextRenderer)
    {
      super(D3DTextRenderer.access$000(paramD3DTextRenderer));
    }

    protected void drawGlyphList(SunGraphics2D paramSunGraphics2D, GlyphList paramGlyphList)
    {
      GraphicsPrimitive.tracePrimitive("D3DDrawGlyphs");
      super.drawGlyphList(paramSunGraphics2D, paramGlyphList);
    }
  }
}