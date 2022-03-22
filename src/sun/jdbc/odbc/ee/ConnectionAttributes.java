package sun.jdbc.odbc.ee;

import java.util.Properties;

public class ConnectionAttributes
{
  private String url = null;
  private String user = null;
  private String password = null;
  private String charSet = null;
  private int loginTimeout = 0;

  public ConnectionAttributes(String paramString1, String paramString2, String paramString3, String paramString4, int paramInt)
  {
    this.url = "jdbc:odbc:" + paramString1;
    this.user = paramString2;
    this.password = paramString3;
    this.charSet = paramString4;
    this.loginTimeout = paramInt;
  }

  public String getUser()
  {
    return this.user;
  }

  public String getPassword()
  {
    return this.password;
  }

  public String getUrl()
  {
    return this.url;
  }

  public String getCharSet()
  {
    return this.charSet;
  }

  public int getLoginTimeout()
  {
    return this.loginTimeout;
  }

  public Properties getProperties()
  {
    Properties localProperties = new Properties();
    if (this.charSet != null)
      localProperties.put("charSet", this.charSet);
    if (this.user != null)
      localProperties.put("user", this.user);
    if (this.password != null)
      localProperties.put("password", this.password);
    if (this.url != null)
      localProperties.put("url", this.url);
    localProperties.put("loginTimeout", "" + this.loginTimeout);
    return localProperties;
  }
}