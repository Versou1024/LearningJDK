package sun.java2d.pipe.hw;

import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import sun.awt.image.SunVolatileImage;

public class AccelTypedVolatileImage extends SunVolatileImage
{
  public AccelTypedVolatileImage(GraphicsConfiguration paramGraphicsConfiguration, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    super(null, paramGraphicsConfiguration, paramInt1, paramInt2, null, paramInt3, null, paramInt4);
  }

  public Graphics2D createGraphics()
  {
    if (getForcedAccelSurfaceType() == 3)
      throw new UnsupportedOperationException("Can't render to a non-RT Texture");
    return super.createGraphics();
  }
}