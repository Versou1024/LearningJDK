package sun.awt.image;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;

public class BufferedImageDevice extends GraphicsDevice
{
  GraphicsConfiguration gc;

  public BufferedImageDevice(BufferedImageGraphicsConfig paramBufferedImageGraphicsConfig)
  {
    this.gc = paramBufferedImageGraphicsConfig;
  }

  public int getType()
  {
    return 2;
  }

  public String getIDstring()
  {
    return "BufferedImage";
  }

  public GraphicsConfiguration[] getConfigurations()
  {
    return { this.gc };
  }

  public GraphicsConfiguration getDefaultConfiguration()
  {
    return this.gc;
  }
}