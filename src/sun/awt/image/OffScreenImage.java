package sun.awt.image;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.SystemColor;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ImageProducer;
import java.awt.image.WritableRaster;
import sun.java2d.SunGraphics2D;
import sun.java2d.SurfaceManagerFactory;

public class OffScreenImage extends BufferedImage
{
  protected Component c;
  private OffScreenImageSource osis;
  private Font defaultFont;

  public OffScreenImage(Component paramComponent, int paramInt1, int paramInt2)
  {
    this(paramComponent, paramInt1, paramInt2, 6);
  }

  public OffScreenImage(Component paramComponent, int paramInt1, int paramInt2, int paramInt3)
  {
    super(paramInt1, paramInt2, paramInt3);
    this.c = paramComponent;
    initSurfaceManager(paramInt1, paramInt2);
  }

  public OffScreenImage(Component paramComponent, ColorModel paramColorModel, WritableRaster paramWritableRaster, boolean paramBoolean)
  {
    super(paramColorModel, paramWritableRaster, paramBoolean, null);
    this.c = paramComponent;
    initSurfaceManager(paramWritableRaster.getWidth(), paramWritableRaster.getHeight());
  }

  public Graphics getGraphics()
  {
    return createGraphics();
  }

  public Graphics2D createGraphics()
  {
    if (this.c == null)
    {
      localObject1 = GraphicsEnvironment.getLocalGraphicsEnvironment();
      return ((GraphicsEnvironment)localObject1).createGraphics(this);
    }
    Object localObject1 = this.c.getBackground();
    if (localObject1 == null)
      localObject1 = SystemColor.window;
    Object localObject2 = this.c.getForeground();
    if (localObject2 == null)
      localObject2 = SystemColor.windowText;
    Font localFont = this.c.getFont();
    if (localFont == null)
    {
      if (this.defaultFont == null)
        this.defaultFont = new Font("Dialog", 0, 12);
      localFont = this.defaultFont;
    }
    SurfaceManager localSurfaceManager = SurfaceManager.getManager(this);
    return ((Graphics2D)(Graphics2D)new SunGraphics2D(localSurfaceManager.getDestSurfaceData(), (Color)localObject2, (Color)localObject1, localFont));
  }

  protected SurfaceManager createSurfaceManager()
  {
    return SurfaceManagerFactory.createCachingManager(this);
  }

  private void initSurfaceManager(int paramInt1, int paramInt2)
  {
    SurfaceManager localSurfaceManager = createSurfaceManager();
    SurfaceManager.setManager(this, localSurfaceManager);
    Graphics2D localGraphics2D = createGraphics();
    try
    {
      localGraphics2D.clearRect(0, 0, paramInt1, paramInt2);
    }
    finally
    {
      localGraphics2D.dispose();
    }
  }

  public ImageProducer getSource()
  {
    if (this.osis == null)
      this.osis = new OffScreenImageSource(this);
    return this.osis;
  }
}