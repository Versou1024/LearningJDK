package sun.net;

import java.net.URL;
import java.util.EventObject;

public class ProgressEvent extends EventObject
{
  private URL url;
  private String contentType;
  private String method;
  private int progress;
  private int expected;
  private ProgressSource.State state;

  public ProgressEvent(ProgressSource paramProgressSource, URL paramURL, String paramString1, String paramString2, ProgressSource.State paramState, int paramInt1, int paramInt2)
  {
    super(paramProgressSource);
    this.url = paramURL;
    this.method = paramString1;
    this.contentType = paramString2;
    this.progress = paramInt1;
    this.expected = paramInt2;
    this.state = paramState;
  }

  public URL getURL()
  {
    return this.url;
  }

  public String getMethod()
  {
    return this.method;
  }

  public String getContentType()
  {
    return this.contentType;
  }

  public int getProgress()
  {
    return this.progress;
  }

  public int getExpected()
  {
    return this.expected;
  }

  public ProgressSource.State getState()
  {
    return this.state;
  }

  public String toString()
  {
    return getClass().getName() + "[url=" + this.url + ", method=" + this.method + ", state=" + this.state + ", content-type=" + this.contentType + ", progress=" + this.progress + ", expected=" + this.expected + "]";
  }
}