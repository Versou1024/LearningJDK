package sun.applet;

import java.awt.Image;
import java.net.URL;
import sun.misc.Ref;

public class AppletResourceLoader
{
  public static Image getImage(URL paramURL)
  {
    return AppletViewer.getCachedImage(paramURL);
  }

  public static Ref getImageRef(URL paramURL)
  {
    return AppletViewer.getCachedImageRef(paramURL);
  }

  public static void flushImages()
  {
    AppletViewer.flushImageCache();
  }
}