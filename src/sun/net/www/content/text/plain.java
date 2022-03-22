package sun.net.www.content.text;

import java.io.IOException;
import java.io.InputStream;
import java.net.ContentHandler;
import java.net.URLConnection;

public class plain extends ContentHandler
{
  public Object getContent(URLConnection paramURLConnection)
  {
    InputStream localInputStream;
    try
    {
      localInputStream = paramURLConnection.getInputStream();
      return new PlainTextInputStream(paramURLConnection.getInputStream());
    }
    catch (IOException localIOException)
    {
    }
    return "Error reading document:\n" + localIOException.toString();
  }
}