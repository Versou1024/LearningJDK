package sun.java2d.opengl;

import java.awt.Composite;
import java.lang.ref.WeakReference;
import sun.java2d.SurfaceData;
import sun.java2d.loops.Blit;
import sun.java2d.loops.CompositeType;
import sun.java2d.loops.SurfaceType;
import sun.java2d.pipe.Region;

class OGLGeneralBlit extends Blit
{
  private Blit performop;
  private WeakReference srcTmp;

  OGLGeneralBlit(SurfaceType paramSurfaceType, CompositeType paramCompositeType, Blit paramBlit)
  {
    super(SurfaceType.Any, paramCompositeType, paramSurfaceType);
    this.performop = paramBlit;
  }

  public synchronized void Blit(SurfaceData paramSurfaceData1, SurfaceData paramSurfaceData2, Composite paramComposite, Region paramRegion, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6)
  {
    Blit localBlit = Blit.getFromCache(paramSurfaceData1.getSurfaceType(), CompositeType.SrcNoEa, SurfaceType.IntArgbPre);
    SurfaceData localSurfaceData = null;
    if (this.srcTmp != null)
      localSurfaceData = (SurfaceData)this.srcTmp.get();
    paramSurfaceData1 = convertFrom(localBlit, paramSurfaceData1, paramInt1, paramInt2, paramInt5, paramInt6, localSurfaceData, 3);
    this.performop.Blit(paramSurfaceData1, paramSurfaceData2, paramComposite, paramRegion, 0, 0, paramInt3, paramInt4, paramInt5, paramInt6);
    if (paramSurfaceData1 != localSurfaceData)
      this.srcTmp = new WeakReference(paramSurfaceData1);
  }
}