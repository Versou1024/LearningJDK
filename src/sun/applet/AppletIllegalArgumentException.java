package sun.applet;

public class AppletIllegalArgumentException extends IllegalArgumentException
{
  private String key = null;
  private static AppletMessageHandler amh = new AppletMessageHandler("appletillegalargumentexception");

  public AppletIllegalArgumentException(String paramString)
  {
    super(paramString);
    this.key = paramString;
  }

  public String getLocalizedMessage()
  {
    return amh.getMessage(this.key);
  }
}