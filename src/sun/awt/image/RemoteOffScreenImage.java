package sun.awt.image;

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import sun.java2d.SurfaceData;

public class RemoteOffScreenImage extends OffScreenImage
{
  protected int bufImageTypeSw = getType();

  private static native void initIDs();

  public RemoteOffScreenImage(Component paramComponent, ColorModel paramColorModel, WritableRaster paramWritableRaster, boolean paramBoolean)
  {
    super(paramComponent, paramColorModel, paramWritableRaster, paramBoolean);
  }

  private native void setRasterNative(WritableRaster paramWritableRaster);

  protected void createNativeRaster()
  {
    SurfaceManager localSurfaceManager = SurfaceManager.getManager(this);
    SurfaceData localSurfaceData = localSurfaceManager.getDestSurfaceData();
    WritableRasterNative localWritableRasterNative = WritableRasterNative.createNativeRaster(getColorModel(), localSurfaceData, getWidth(), getHeight());
    setRasterNative(localWritableRasterNative);
  }

  public BufferedImage getSnapshot()
  {
    BufferedImage localBufferedImage;
    if (this.bufImageTypeSw > 0)
    {
      localBufferedImage = new BufferedImage(getWidth(), getHeight(), this.bufImageTypeSw);
    }
    else
    {
      localObject = getColorModel();
      WritableRaster localWritableRaster = ((ColorModel)localObject).createCompatibleWritableRaster(getWidth(), getHeight());
      localBufferedImage = new BufferedImage((ColorModel)localObject, localWritableRaster, ((ColorModel)localObject).isAlphaPremultiplied(), null);
    }
    Object localObject = localBufferedImage.createGraphics();
    ((Graphics2D)localObject).drawImage(this, 0, 0, null);
    ((Graphics2D)localObject).dispose();
    return ((BufferedImage)localBufferedImage);
  }

  static
  {
    initIDs();
  }
}