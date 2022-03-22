package sun.java2d.opengl;

import java.awt.Composite;
import sun.font.GlyphList;
import sun.java2d.SunGraphics2D;
import sun.java2d.loops.GraphicsPrimitive;
import sun.java2d.pipe.BufferedTextPipe;
import sun.java2d.pipe.RenderQueue;

class OGLTextRenderer extends BufferedTextPipe
{
  OGLTextRenderer(RenderQueue paramRenderQueue)
  {
    super(paramRenderQueue);
  }

  protected native void drawGlyphList(int paramInt1, boolean paramBoolean1, boolean paramBoolean2, boolean paramBoolean3, int paramInt2, float paramFloat1, float paramFloat2, long[] paramArrayOfLong, float[] paramArrayOfFloat);

  protected void validateContext(SunGraphics2D paramSunGraphics2D, Composite paramComposite)
  {
    OGLSurfaceData localOGLSurfaceData = (OGLSurfaceData)paramSunGraphics2D.surfaceData;
    OGLContext.validateContext(localOGLSurfaceData, localOGLSurfaceData, paramSunGraphics2D.getCompClip(), paramComposite, null, paramSunGraphics2D.paint, paramSunGraphics2D, 0);
  }

  OGLTextRenderer traceWrap()
  {
    return new Tracer(this);
  }

  private static class Tracer extends OGLTextRenderer
  {
    Tracer(OGLTextRenderer paramOGLTextRenderer)
    {
      super(OGLTextRenderer.access$000(paramOGLTextRenderer));
    }

    protected void drawGlyphList(SunGraphics2D paramSunGraphics2D, GlyphList paramGlyphList)
    {
      GraphicsPrimitive.tracePrimitive("OGLDrawGlyphs");
      super.drawGlyphList(paramSunGraphics2D, paramGlyphList);
    }
  }
}