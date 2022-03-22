package sun.applet;

import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;
import sun.awt.image.URLImageSource;
import sun.misc.Ref;

class AppletImageRef extends Ref
{
  URL url;

  AppletImageRef(URL paramURL)
  {
    this.url = paramURL;
  }

  public void flush()
  {
    super.flush();
  }

  public Object reconstitute()
  {
    Image localImage = Toolkit.getDefaultToolkit().createImage(new URLImageSource(this.url));
    return localImage;
  }
}