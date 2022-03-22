package sun.java2d.d3d;

import java.awt.GraphicsConfiguration;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.LookupOp;
import java.awt.image.RescaleOp;
import sun.java2d.SunGraphics2D;
import sun.java2d.SurfaceData;
import sun.java2d.loops.CompositeType;
import sun.java2d.pipe.BufferedBufImgOps;

class D3DBufImgOps extends BufferedBufImgOps
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
    if ((!(localSurfaceData1 instanceof D3DSurfaceData)) || (paramSunGraphics2D.interpolationType == 3) || (paramSunGraphics2D.compositeState > 1))
      return false;
    SurfaceData localSurfaceData2 = SurfaceData.getSourceSurfaceData(paramBufferedImage, localSurfaceData1, CompositeType.SrcOver, null, false);
    if (!(localSurfaceData2 instanceof D3DSurfaceData))
    {
      localSurfaceData2 = SurfaceData.getSourceSurfaceData(paramBufferedImage, localSurfaceData1, CompositeType.SrcOver, null, false);
      if (!(localSurfaceData2 instanceof D3DSurfaceData))
        return false;
    }
    D3DSurfaceData localD3DSurfaceData = (D3DSurfaceData)localSurfaceData2;
    D3DGraphicsDevice localD3DGraphicsDevice = (D3DGraphicsDevice)localD3DSurfaceData.getDeviceConfiguration().getDevice();
    if ((localD3DSurfaceData.getType() != 3) || (!(localD3DGraphicsDevice.isCapPresent(65536))))
      return false;
    int i = paramBufferedImage.getWidth();
    int j = paramBufferedImage.getHeight();
    D3DBlitLoops.IsoBlit(localSurfaceData2, localSurfaceData1, paramBufferedImage, paramBufferedImageOp, paramSunGraphics2D.composite, paramSunGraphics2D.getCompClip(), paramSunGraphics2D.transform, paramSunGraphics2D.interpolationType, 0, 0, i, j, paramInt1, paramInt2, paramInt1 + i, paramInt2 + j, true);
    return true;
  }
}