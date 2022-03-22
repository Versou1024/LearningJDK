package sun.awt;

import java.awt.image.BufferedImage;

public abstract class UNIXToolkit extends SunToolkit
{
  public static final Object GTK_LOCK = new Object();

  public static boolean checkGTK()
  {
    return false;
  }

  public static void unloadGTK()
  {
  }

  public BufferedImage getStockIcon(int paramInt1, String paramString1, int paramInt2, int paramInt3, String paramString2)
  {
    return null;
  }
}