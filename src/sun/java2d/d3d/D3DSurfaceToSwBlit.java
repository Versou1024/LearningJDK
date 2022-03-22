package sun.java2d.d3d;

import java.awt.Composite;
import sun.java2d.SurfaceData;
import sun.java2d.loops.Blit;
import sun.java2d.loops.CompositeType;
import sun.java2d.loops.SurfaceType;
import sun.java2d.pipe.Region;
import sun.java2d.pipe.RenderBuffer;

class D3DSurfaceToSwBlit extends Blit
{
  private int typeval;

  D3DSurfaceToSwBlit(SurfaceType paramSurfaceType, int paramInt)
  {
    super(D3DSurfaceData.D3DSurface, CompositeType.SrcNoEa, paramSurfaceType);
    this.typeval = paramInt;
  }

  public void Blit(SurfaceData paramSurfaceData1, SurfaceData paramSurfaceData2, Composite paramComposite, Region paramRegion, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6)
  {
    D3DRenderQueue localD3DRenderQueue = D3DRenderQueue.getInstance();
    localD3DRenderQueue.lock();
    try
    {
      localD3DRenderQueue.addReference(paramSurfaceData2);
      RenderBuffer localRenderBuffer = localD3DRenderQueue.getBuffer();
      D3DContext.setScratchSurface(((D3DSurfaceData)paramSurfaceData1).getContext());
      localD3DRenderQueue.ensureCapacityAndAlignment(48, 32);
      localRenderBuffer.putInt(34);
      localRenderBuffer.putInt(paramInt1).putInt(paramInt2);
      localRenderBuffer.putInt(paramInt3).putInt(paramInt4);
      localRenderBuffer.putInt(paramInt5).putInt(paramInt6);
      localRenderBuffer.putInt(this.typeval);
      localRenderBuffer.putLong(paramSurfaceData1.getNativeOps());
      localRenderBuffer.putLong(paramSurfaceData2.getNativeOps());
      localD3DRenderQueue.flushNow();
    }
    finally
    {
      localD3DRenderQueue.unlock();
    }
  }
}