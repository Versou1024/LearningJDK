package sun.applet;

public class AppletSecurityException extends SecurityException
{
  private String key;
  private Object[] msgobj;
  private static AppletMessageHandler amh = new AppletMessageHandler("appletsecurityexception");

  public AppletSecurityException(String paramString)
  {
    super(paramString);
    this.key = null;
    this.msgobj = null;
    this.key = paramString;
  }

  public AppletSecurityException(String paramString1, String paramString2)
  {
    this(paramString1);
    this.msgobj = new Object[1];
    this.msgobj[0] = paramString2;
  }

  public AppletSecurityException(String paramString1, String paramString2, String paramString3)
  {
    this(paramString1);
    this.msgobj = new Object[2];
    this.msgobj[0] = paramString2;
    this.msgobj[1] = paramString3;
  }

  public String getLocalizedMessage()
  {
    if (this.msgobj != null)
      return amh.getMessage(this.key, this.msgobj);
    return amh.getMessage(this.key);
  }
}