package sun.java2d.opengl;

import java.awt.Composite;
import sun.java2d.SurfaceData;
import sun.java2d.loops.Blit;
import sun.java2d.loops.CompositeType;
import sun.java2d.loops.SurfaceType;
import sun.java2d.pipe.Region;
import sun.java2d.pipe.RenderBuffer;

class OGLSurfaceToSwBlit extends Blit
{
  private int typeval;

  OGLSurfaceToSwBlit(SurfaceType paramSurfaceType, int paramInt)
  {
    super(OGLSurfaceData.OpenGLSurface, CompositeType.SrcNoEa, paramSurfaceType);
    this.typeval = paramInt;
  }

  public void Blit(SurfaceData paramSurfaceData1, SurfaceData paramSurfaceData2, Composite paramComposite, Region paramRegion, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6)
  {
    OGLRenderQueue localOGLRenderQueue = OGLRenderQueue.getInstance();
    localOGLRenderQueue.lock();
    try
    {
      localOGLRenderQueue.addReference(paramSurfaceData2);
      RenderBuffer localRenderBuffer = localOGLRenderQueue.getBuffer();
      OGLContext.validateContext((OGLSurfaceData)paramSurfaceData1);
      localOGLRenderQueue.ensureCapacityAndAlignment(48, 32);
      localRenderBuffer.putInt(34);
      localRenderBuffer.putInt(paramInt1).putInt(paramInt2);
      localRenderBuffer.putInt(paramInt3).putInt(paramInt4);
      localRenderBuffer.putInt(paramInt5).putInt(paramInt6);
      localRenderBuffer.putInt(this.typeval);
      localRenderBuffer.putLong(paramSurfaceData1.getNativeOps());
      localRenderBuffer.putLong(paramSurfaceData2.getNativeOps());
      localOGLRenderQueue.flushNow();
    }
    finally
    {
      localOGLRenderQueue.unlock();
    }
  }
}