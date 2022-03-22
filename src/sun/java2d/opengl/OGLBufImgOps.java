package sun.java2d.opengl;

import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.LookupOp;
import java.awt.image.RescaleOp;
import sun.java2d.SunGraphics2D;
import sun.java2d.SurfaceData;
import sun.java2d.loops.CompositeType;
import sun.java2d.pipe.BufferedBufImgOps;

class OGLBufImgOps extends BufferedBufImgOps
{
  static boolean renderImageWithOp(SunGraphics2D paramSunGraphics2D, BufferedImage paramBufferedImage, BufferedImageOp paramBufferedImageOp, int paramInt1, int paramInt2)
  {
    if (paramBufferedImageOp instanceof ConvolveOp)
    {
      if (isConvolveOpValid((ConvolveOp)paramBufferedImageOp))
        break label61;
      return false;
    }
    if (paramBufferedImageOp instanceof RescaleOp)
    {
      if (isRescaleOpValid((RescaleOp)paramBufferedImageOp, paramBufferedImage))
        break label61;
      return false;
    }
    if (paramBufferedImageOp instanceof LookupOp)
    {
      if (isLookupOpValid((LookupOp)paramBufferedImageOp, paramBufferedImage))
        break label61;
      return false;
    }
    return false;
    label61: SurfaceData localSurfaceData1 = paramSunGraphics2D.surfaceData;
    if ((!(localSurfaceData1 instanceof OGLSurfaceData)) || (paramSunGraphics2D.interpolationType == 3) || (paramSunGraphics2D.compositeState > 1))
      return false;
    SurfaceData localSurfaceData2 = SurfaceData.getSourceSurfaceData(paramBufferedImage, localSurfaceData1, CompositeType.SrcOver, null, false);
    if (!(localSurfaceData2 instanceof OGLSurfaceData))
    {
      localSurfaceData2 = SurfaceData.getSourceSurfaceData(paramBufferedImage, localSurfaceData1, CompositeType.SrcOver, null, false);
      if (!(localSurfaceData2 instanceof OGLSurfaceData))
        return false;
    }
    OGLSurfaceData localOGLSurfaceData = (OGLSurfaceData)localSurfaceData2;
    OGLGraphicsConfig localOGLGraphicsConfig = localOGLSurfaceData.getOGLGraphicsConfig();
    if ((localOGLSurfaceData.getType() != 3) || (!(localOGLGraphicsConfig.isCapPresent(262144))))
      return false;
    int i = paramBufferedImage.getWidth();
    int j = paramBufferedImage.getHeight();
    OGLBlitLoops.IsoBlit(localSurfaceData2, localSurfaceData1, paramBufferedImage, paramBufferedImageOp, paramSunGraphics2D.composite, paramSunGraphics2D.getCompClip(), paramSunGraphics2D.transform, paramSunGraphics2D.interpolationType, 0, 0, i, j, paramInt1, paramInt2, paramInt1 + i, paramInt2 + j, true);
    return true;
  }
}