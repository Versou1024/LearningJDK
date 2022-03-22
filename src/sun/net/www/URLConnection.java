package sun.net.www;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class URLConnection extends java.net.URLConnection
{
  private String contentType;
  private int contentLength = -1;
  protected MessageHeader properties = new MessageHeader();
  private static Map proxiedHosts = new HashMap();

  public URLConnection(URL paramURL)
  {
    super(paramURL);
  }

  public MessageHeader getProperties()
  {
    return this.properties;
  }

  public void setProperties(MessageHeader paramMessageHeader)
  {
    this.properties = paramMessageHeader;
  }

  public void setRequestProperty(String paramString1, String paramString2)
  {
    if (this.connected)
      throw new IllegalAccessError("Already connected");
    if (paramString1 == null)
      throw new NullPointerException("key cannot be null");
    this.properties.set(paramString1, paramString2);
  }

  public void addRequestProperty(String paramString1, String paramString2)
  {
    if (this.connected)
      throw new IllegalStateException("Already connected");
    if (paramString1 == null)
      throw new NullPointerException("key is null");
  }

  public String getRequestProperty(String paramString)
  {
    if (this.connected)
      throw new IllegalStateException("Already connected");
    return null;
  }

  public Map<String, List<String>> getRequestProperties()
  {
    if (this.connected)
      throw new IllegalStateException("Already connected");
    return Collections.EMPTY_MAP;
  }

  public String getHeaderField(String paramString)
  {
    try
    {
      getInputStream();
    }
    catch (Exception localException)
    {
      return null;
    }
    return ((this.properties == null) ? null : this.properties.findValue(paramString));
  }

  public String getHeaderFieldKey(int paramInt)
  {
    try
    {
      getInputStream();
    }
    catch (Exception localException)
    {
      return null;
    }
    MessageHeader localMessageHeader = this.properties;
    return ((localMessageHeader == null) ? null : localMessageHeader.getKey(paramInt));
  }

  public String getHeaderField(int paramInt)
  {
    try
    {
      getInputStream();
    }
    catch (Exception localException)
    {
      return null;
    }
    MessageHeader localMessageHeader = this.properties;
    return ((localMessageHeader == null) ? null : localMessageHeader.getValue(paramInt));
  }

  public String getContentType()
  {
    if (this.contentType == null)
      this.contentType = getHeaderField("content-type");
    if (this.contentType == null)
    {
      String str1 = null;
      try
      {
        str1 = guessContentTypeFromStream(getInputStream());
      }
      catch (IOException localIOException)
      {
      }
      String str2 = this.properties.findValue("content-encoding");
      if (str1 == null)
      {
        str1 = this.properties.findValue("content-type");
        if (str1 == null)
          if (this.url.getFile().endsWith("/"))
            str1 = "text/html";
          else
            str1 = guessContentTypeFromName(this.url.getFile());
      }
      if ((str1 == null) || ((str2 != null) && (!(str2.equalsIgnoreCase("7bit"))) && (!(str2.equalsIgnoreCase("8bit"))) && (!(str2.equalsIgnoreCase("binary")))))
        str1 = "content/unknown";
      setContentType(str1);
    }
    return this.contentType;
  }

  public void setContentType(String paramString)
  {
    this.contentType = paramString;
    this.properties.set("content-type", paramString);
  }

  public int getContentLength()
  {
    try
    {
      getInputStream();
    }
    catch (Exception localException1)
    {
      return -1;
    }
    int i = this.contentLength;
    if (i < 0)
      try
      {
        i = Integer.parseInt(this.properties.findValue("content-length"));
        setContentLength(i);
      }
      catch (Exception localException2)
      {
      }
    return i;
  }

  protected void setContentLength(int paramInt)
  {
    this.contentLength = paramInt;
    this.properties.set("content-length", String.valueOf(paramInt));
  }

  public boolean canCache()
  {
    return (this.url.getFile().indexOf(63) < 0);
  }

  public void close()
  {
    this.url = null;
  }

  public static synchronized void setProxiedHost(String paramString)
  {
    proxiedHosts.put(paramString.toLowerCase(), null);
  }

  public static synchronized boolean isProxiedHost(String paramString)
  {
    return proxiedHosts.containsKey(paramString.toLowerCase());
  }
}