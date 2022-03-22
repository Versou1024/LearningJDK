package sun.tools.jar;

import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import sun.awt.image.ImageDecoder;
import sun.awt.image.URLImageSource;

public class JarImageSource extends URLImageSource
{
  String mimeType;
  String entryName;
  URL url;

  public JarImageSource(URL paramURL, String paramString)
  {
    super(paramURL);
    this.entryName = null;
    this.url = paramURL;
    this.mimeType = paramString;
  }

  public JarImageSource(URL paramURL, String paramString1, String paramString2)
  {
    this(paramURL, paramString2);
    this.entryName = paramString1;
  }

  protected ImageDecoder getDecoder()
  {
    InputStream localInputStream = null;
    try
    {
      JarURLConnection localJarURLConnection = (JarURLConnection)this.url.openConnection();
      JarFile localJarFile = localJarURLConnection.getJarFile();
      JarEntry localJarEntry = localJarURLConnection.getJarEntry();
      if ((this.entryName != null) && (localJarEntry == null))
        localJarEntry = localJarFile.getJarEntry(this.entryName);
      if ((localJarEntry == null) || ((localJarEntry != null) && (this.entryName != null) && (!(this.entryName.equals(localJarEntry.getName())))))
        return null;
      localInputStream = localJarFile.getInputStream(localJarEntry);
    }
    catch (IOException localIOException)
    {
      return null;
    }
    ImageDecoder localImageDecoder = decoderForType(localInputStream, this.mimeType);
    if (localImageDecoder == null)
      localImageDecoder = getDecoder(localInputStream);
    return localImageDecoder;
  }
}