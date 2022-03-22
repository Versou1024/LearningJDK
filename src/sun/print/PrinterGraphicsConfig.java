package sun.print;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

public class PrinterGraphicsConfig extends GraphicsConfiguration
{
  GraphicsDevice gd;
  ColorModel model;
  Raster raster;
  int pageWidth;
  int pageHeight;
  AffineTransform deviceTransform;

  public PrinterGraphicsConfig(String paramString, AffineTransform paramAffineTransform, int paramInt1, int paramInt2)
  {
    BufferedImage localBufferedImage = new BufferedImage(1, 1, 5);
    this.model = localBufferedImage.getColorModel();
    this.raster = localBufferedImage.getRaster().createCompatibleWritableRaster(1, 1);
    this.pageWidth = paramInt1;
    this.pageHeight = paramInt2;
    this.deviceTransform = paramAffineTransform;
    this.gd = new PrinterGraphicsDevice(this, paramString);
  }

  public GraphicsDevice getDevice()
  {
    return this.gd;
  }

  public BufferedImage createCompatibleImage(int paramInt1, int paramInt2)
  {
    WritableRaster localWritableRaster = this.raster.createCompatibleWritableRaster(paramInt1, paramInt2);
    return new BufferedImage(this.model, localWritableRaster, this.model.isAlphaPremultiplied(), null);
  }

  public ColorModel getColorModel()
  {
    return this.model;
  }

  public ColorModel getColorModel(int paramInt)
  {
    if (this.model.getTransparency() == paramInt)
      return this.model;
    switch (paramInt)
    {
    case 1:
      return new DirectColorModel(24, 16711680, 65280, 255);
    case 2:
      return new DirectColorModel(25, 16711680, 65280, 255, 16777216);
    case 3:
      return ColorModel.getRGBdefault();
    }
    return null;
  }

  public AffineTransform getDefaultTransform()
  {
    return new AffineTransform(this.deviceTransform);
  }

  public AffineTransform getNormalizingTransform()
  {
    return new AffineTransform();
  }

  public Rectangle getBounds()
  {
    return new Rectangle(0, 0, this.pageWidth, this.pageHeight);
  }
}