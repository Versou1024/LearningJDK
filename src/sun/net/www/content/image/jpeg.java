package sun.net.www.content.image;

import java.awt.Image;
import java.awt.Toolkit;
import java.io.IOException;
import java.net.ContentHandler;
import java.net.URLConnection;
import sun.awt.image.URLImageSource;

public class jpeg extends ContentHandler
{
  public Object getContent(URLConnection paramURLConnection)
    throws IOException
  {
    return new URLImageSource(paramURLConnection);
  }

  public Object getContent(URLConnection paramURLConnection, Class[] paramArrayOfClass)
    throws IOException
  {
    for (int i = 0; i < paramArrayOfClass.length; ++i)
    {
      if (paramArrayOfClass[i].isAssignableFrom(URLImageSource.class))
        return new URLImageSource(paramURLConnection);
      if (paramArrayOfClass[i].isAssignableFrom(Image.class))
      {
        Toolkit localToolkit = Toolkit.getDefaultToolkit();
        return localToolkit.createImage(new URLImageSource(paramURLConnection));
      }
    }
    return null;
  }
}