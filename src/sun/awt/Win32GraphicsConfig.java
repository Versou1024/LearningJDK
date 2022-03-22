package sun.awt;

import java.awt.AWTException;
import java.awt.BufferCapabilities;
import java.awt.BufferCapabilities.FlipContents;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.VolatileImage;
import java.awt.image.WritableRaster;
import sun.awt.image.OffScreenImage;
import sun.awt.image.SunVolatileImage;
import sun.awt.windows.WComponentPeer;
import sun.java2d.SurfaceData;
import sun.java2d.loops.CompositeType;
import sun.java2d.loops.RenderLoops;
import sun.java2d.loops.SurfaceType;
import sun.java2d.windows.GDIWindowSurfaceData;

public class Win32GraphicsConfig extends GraphicsConfiguration
  implements DisplayChangedListener
{
  protected Win32GraphicsDevice screen;
  protected int visual;
  protected RenderLoops solidloops;
  private SurfaceType sTypeOrig = null;

  private static native void initIDs();

  public static Win32GraphicsConfig getConfig(Win32GraphicsDevice paramWin32GraphicsDevice, int paramInt)
  {
    return new Win32GraphicsConfig(paramWin32GraphicsDevice, paramInt);
  }

  @Deprecated
  public Win32GraphicsConfig(GraphicsDevice paramGraphicsDevice, int paramInt)
  {
    this.screen = ((Win32GraphicsDevice)paramGraphicsDevice);
    this.visual = paramInt;
    ((Win32GraphicsDevice)paramGraphicsDevice).addDisplayChangedListener(this);
  }

  public GraphicsDevice getDevice()
  {
    return this.screen;
  }

  public int getVisual()
  {
    return this.visual;
  }

  public synchronized RenderLoops getSolidLoops(SurfaceType paramSurfaceType)
  {
    if ((this.solidloops == null) || (this.sTypeOrig != paramSurfaceType))
    {
      this.solidloops = SurfaceData.makeRenderLoops(SurfaceType.OpaqueColor, CompositeType.SrcNoEa, paramSurfaceType);
      this.sTypeOrig = paramSurfaceType;
    }
    return this.solidloops;
  }

  public BufferedImage createCompatibleImage(int paramInt1, int paramInt2)
  {
    ColorModel localColorModel = getColorModel();
    WritableRaster localWritableRaster = localColorModel.createCompatibleWritableRaster(paramInt1, paramInt2);
    return new BufferedImage(localColorModel, localWritableRaster, localColorModel.isAlphaPremultiplied(), null);
  }

  public synchronized ColorModel getColorModel()
  {
    return this.screen.getColorModel();
  }

  public ColorModel getDeviceColorModel()
  {
    return this.screen.getDynamicColorModel();
  }

  public ColorModel getColorModel(int paramInt)
  {
    switch (paramInt)
    {
    case 1:
      return getColorModel();
    case 2:
      return new DirectColorModel(25, 16711680, 65280, 255, 16777216);
    case 3:
      return ColorModel.getRGBdefault();
    }
    return null;
  }

  public AffineTransform getDefaultTransform()
  {
    return new AffineTransform();
  }

  public AffineTransform getNormalizingTransform()
  {
    Win32GraphicsEnvironment localWin32GraphicsEnvironment = (Win32GraphicsEnvironment)GraphicsEnvironment.getLocalGraphicsEnvironment();
    double d1 = localWin32GraphicsEnvironment.getXResolution() / 72.0D;
    double d2 = localWin32GraphicsEnvironment.getYResolution() / 72.0D;
    return new AffineTransform(d1, 0D, 0D, d2, 0D, 0D);
  }

  public String toString()
  {
    return toString() + "[dev=" + this.screen + ",pixfmt=" + this.visual + "]";
  }

  private native Rectangle getBounds(int paramInt);

  public Rectangle getBounds()
  {
    return getBounds(this.screen.getScreen());
  }

  public synchronized void displayChanged()
  {
    this.solidloops = null;
  }

  public void paletteChanged()
  {
  }

  public SurfaceData createSurfaceData(WComponentPeer paramWComponentPeer, int paramInt)
  {
    return GDIWindowSurfaceData.createData(paramWComponentPeer);
  }

  public Image createAcceleratedImage(Component paramComponent, int paramInt1, int paramInt2)
  {
    ColorModel localColorModel = getColorModel(1);
    WritableRaster localWritableRaster = localColorModel.createCompatibleWritableRaster(paramInt1, paramInt2);
    return new OffScreenImage(paramComponent, localColorModel, localWritableRaster, localColorModel.isAlphaPremultiplied());
  }

  public void assertOperationSupported(Component paramComponent, int paramInt, BufferCapabilities paramBufferCapabilities)
    throws AWTException
  {
    throw new AWTException("The operation requested is not supported");
  }

  public VolatileImage createBackBuffer(WComponentPeer paramWComponentPeer)
  {
    Component localComponent = (Component)paramWComponentPeer.getTarget();
    return new SunVolatileImage(localComponent, localComponent.getWidth(), localComponent.getHeight(), Boolean.TRUE);
  }

  public void flip(WComponentPeer paramWComponentPeer, Component paramComponent, VolatileImage paramVolatileImage, int paramInt1, int paramInt2, int paramInt3, int paramInt4, BufferCapabilities.FlipContents paramFlipContents)
  {
    Graphics localGraphics;
    if ((paramFlipContents == BufferCapabilities.FlipContents.COPIED) || (paramFlipContents == BufferCapabilities.FlipContents.UNDEFINED))
    {
      localGraphics = paramWComponentPeer.getGraphics();
      try
      {
        localGraphics.drawImage(paramVolatileImage, paramInt1, paramInt2, paramInt3, paramInt4, paramInt1, paramInt2, paramInt3, paramInt4, null);
      }
      finally
      {
        localGraphics.dispose();
      }
    }
    else if (paramFlipContents == BufferCapabilities.FlipContents.BACKGROUND)
    {
      localGraphics = paramVolatileImage.getGraphics();
      try
      {
        localGraphics.setColor(paramComponent.getBackground());
        localGraphics.fillRect(0, 0, paramVolatileImage.getWidth(), paramVolatileImage.getHeight());
      }
      finally
      {
        localGraphics.dispose();
      }
    }
  }

  static
  {
    initIDs();
  }
}