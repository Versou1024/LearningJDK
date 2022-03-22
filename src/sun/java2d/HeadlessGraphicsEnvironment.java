package sun.java2d;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Locale;
import sun.awt.FontConfiguration;

public class HeadlessGraphicsEnvironment extends GraphicsEnvironment
  implements FontSupport
{
  private GraphicsEnvironment ge;
  private FontSupport fontSupport;

  public HeadlessGraphicsEnvironment(GraphicsEnvironment paramGraphicsEnvironment)
  {
    this.ge = paramGraphicsEnvironment;
    if (paramGraphicsEnvironment instanceof FontSupport)
      this.fontSupport = ((FontSupport)paramGraphicsEnvironment);
  }

  public GraphicsDevice[] getScreenDevices()
    throws HeadlessException
  {
    throw new HeadlessException();
  }

  public GraphicsDevice getDefaultScreenDevice()
    throws HeadlessException
  {
    throw new HeadlessException();
  }

  public Point getCenterPoint()
    throws HeadlessException
  {
    throw new HeadlessException();
  }

  public Rectangle getMaximumWindowBounds()
    throws HeadlessException
  {
    throw new HeadlessException();
  }

  public Graphics2D createGraphics(BufferedImage paramBufferedImage)
  {
    return this.ge.createGraphics(paramBufferedImage);
  }

  public Font[] getAllFonts()
  {
    return this.ge.getAllFonts();
  }

  public String[] getAvailableFontFamilyNames()
  {
    return this.ge.getAvailableFontFamilyNames();
  }

  public String[] getAvailableFontFamilyNames(Locale paramLocale)
  {
    return this.ge.getAvailableFontFamilyNames(paramLocale);
  }

  public FontConfiguration getFontConfiguration()
  {
    if (this.fontSupport != null)
      return this.fontSupport.getFontConfiguration();
    return null;
  }

  public GraphicsEnvironment getSunGraphicsEnvironment()
  {
    return this.ge;
  }
}