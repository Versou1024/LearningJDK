package sun.net.www.protocol.mailto;

import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

public class Handler extends URLStreamHandler
{
  public synchronized URLConnection openConnection(URL paramURL)
  {
    return new MailToURLConnection(paramURL);
  }

  public void parseURL(URL paramURL, String paramString, int paramInt1, int paramInt2)
  {
    String str1 = paramURL.getProtocol();
    String str2 = "";
    int i = paramURL.getPort();
    String str3 = "";
    if (paramInt1 < paramInt2)
      str3 = paramString.substring(paramInt1, paramInt2);
    int j = 0;
    if ((str3 == null) || (str3.equals("")))
    {
      j = 1;
    }
    else
    {
      int k = 1;
      for (int l = 0; l < str3.length(); ++l)
        if (!(Character.isWhitespace(str3.charAt(l))))
          k = 0;
      if (k != 0)
        j = 1;
    }
    if (j != 0)
      throw new RuntimeException("No email address");
    setURL(paramURL, str1, str2, i, str3, null);
  }
}