package sun.security.util;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.StringTokenizer;

public class PathList
{
  public static String appendPath(String paramString1, String paramString2)
  {
    if ((paramString1 == null) || (paramString1.length() == 0))
      return paramString2;
    if ((paramString2 == null) || (paramString2.length() == 0))
      return paramString1;
    return paramString1 + File.pathSeparator + paramString2;
  }

  public static URL[] pathToURLs(String paramString)
  {
    Object localObject2;
    StringTokenizer localStringTokenizer = new StringTokenizer(paramString, File.pathSeparator);
    Object localObject1 = new URL[localStringTokenizer.countTokens()];
    int i = 0;
    while (localStringTokenizer.hasMoreTokens())
    {
      localObject2 = fileToURL(new File(localStringTokenizer.nextToken()));
      if (localObject2 != null)
        localObject1[(i++)] = localObject2;
    }
    if (localObject1.length != i)
    {
      localObject2 = new URL[i];
      System.arraycopy(localObject1, 0, localObject2, 0, i);
      localObject1 = localObject2;
    }
    return ((URL)(URL)localObject1);
  }

  private static URL fileToURL(File paramFile)
  {
    try
    {
      str = paramFile.getCanonicalPath();
    }
    catch (IOException localIOException)
    {
      str = paramFile.getAbsolutePath();
    }
    String str = str.replace(File.separatorChar, '/');
    if (!(str.startsWith("/")))
      str = "/" + str;
    if (!(paramFile.isFile()))
      str = str + "/";
    try
    {
      return new URL("file", "", str);
    }
    catch (MalformedURLException localMalformedURLException)
    {
      throw new IllegalArgumentException("file");
    }
  }
}