package sun.awt.image;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class FileImageSource extends InputStreamImageSource
{
  String imagefile;

  public FileImageSource(String paramString)
  {
    SecurityManager localSecurityManager = System.getSecurityManager();
    if (localSecurityManager != null)
      localSecurityManager.checkRead(paramString);
    this.imagefile = paramString;
  }

  final boolean checkSecurity(Object paramObject, boolean paramBoolean)
  {
    return true;
  }

  protected ImageDecoder getDecoder()
  {
    BufferedInputStream localBufferedInputStream;
    try
    {
      localBufferedInputStream = new BufferedInputStream(new FileInputStream(this.imagefile));
    }
    catch (FileNotFoundException localFileNotFoundException)
    {
      return null;
    }
    return getDecoder(localBufferedInputStream);
  }
}