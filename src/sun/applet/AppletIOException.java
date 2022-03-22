package sun.applet;

import java.io.IOException;

public class AppletIOException extends IOException
{
  private String key;
  private Object msgobj;
  private static AppletMessageHandler amh = new AppletMessageHandler("appletioexception");

  public AppletIOException(String paramString)
  {
    super(paramString);
    this.key = null;
    this.msgobj = null;
    this.key = paramString;
  }

  public AppletIOException(String paramString, Object paramObject)
  {
    this(paramString);
    this.msgobj = paramObject;
  }

  public String getLocalizedMessage()
  {
    if (this.msgobj != null)
      return amh.getMessage(this.key, this.msgobj);
    return amh.getMessage(this.key);
  }
}