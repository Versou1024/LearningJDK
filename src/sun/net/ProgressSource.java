package sun.net;

import java.net.URL;

public class ProgressSource
{
  private URL url;
  private String method;
  private String contentType;
  private int progress;
  private int lastProgress;
  private int expected;
  private State state;
  private boolean connected;
  private int threshold;
  private ProgressMonitor progressMonitor;

  public ProgressSource(URL paramURL, String paramString)
  {
    this(paramURL, paramString, -1);
  }

  public ProgressSource(URL paramURL, String paramString, int paramInt)
  {
    this.progress = 0;
    this.lastProgress = 0;
    this.expected = -1;
    this.connected = false;
    this.threshold = 8192;
    this.url = paramURL;
    this.method = paramString;
    this.contentType = "content/unknown";
    this.progress = 0;
    this.lastProgress = 0;
    this.expected = paramInt;
    this.state = State.NEW;
    this.progressMonitor = ProgressMonitor.getDefault();
    this.threshold = this.progressMonitor.getProgressUpdateThreshold();
  }

  public boolean connected()
  {
    if (!(this.connected))
    {
      this.connected = true;
      this.state = State.CONNECTED;
      return false;
    }
    return true;
  }

  public void close()
  {
    this.state = State.DELETE;
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

  public void setContentType(String paramString)
  {
    this.contentType = paramString;
  }

  public int getProgress()
  {
    return this.progress;
  }

  public int getExpected()
  {
    return this.expected;
  }

  public State getState()
  {
    return this.state;
  }

  public void beginTracking()
  {
    this.progressMonitor.registerSource(this);
  }

  public void finishTracking()
  {
    this.progressMonitor.unregisterSource(this);
  }

  public void updateProgress(int paramInt1, int paramInt2)
  {
    this.lastProgress = this.progress;
    this.progress = paramInt1;
    this.expected = paramInt2;
    if (!(connected()))
      this.state = State.CONNECTED;
    else
      this.state = State.UPDATE;
    if (this.lastProgress / this.threshold != this.progress / this.threshold)
      this.progressMonitor.updateProgress(this);
    if ((this.expected != -1) && (this.progress >= this.expected) && (this.progress != 0))
      close();
  }

  public Object clone()
    throws CloneNotSupportedException
  {
    return super.clone();
  }

  public String toString()
  {
    return super.getClass().getName() + "[url=" + this.url + ", method=" + this.method + ", state=" + this.state + ", content-type=" + this.contentType + ", progress=" + this.progress + ", expected=" + this.expected + "]";
  }

  public static enum State
  {
    NEW, CONNECTED, UPDATE, DELETE;
  }
}