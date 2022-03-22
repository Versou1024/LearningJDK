package sun.net.www.protocol.http;

import java.net.URL;

@Deprecated
public abstract interface HttpAuthenticator
{
  public abstract boolean schemeSupported(String paramString);

  public abstract String authString(URL paramURL, String paramString1, String paramString2);
}