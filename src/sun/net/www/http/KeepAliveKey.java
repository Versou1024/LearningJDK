package sun.net.www.http;

import java.net.URL;

class KeepAliveKey
{
  private String protocol = null;
  private String host = null;
  private int port = 0;
  private Object obj = null;

  public KeepAliveKey(URL paramURL, Object paramObject)
  {
    this.protocol = paramURL.getProtocol();
    this.host = paramURL.getHost();
    this.port = paramURL.getPort();
    this.obj = paramObject;
  }

  public boolean equals(Object paramObject)
  {
    if (!(paramObject instanceof KeepAliveKey))
      return false;
    KeepAliveKey localKeepAliveKey = (KeepAliveKey)paramObject;
    return ((this.host.equals(localKeepAliveKey.host)) && (this.port == localKeepAliveKey.port) && (this.protocol.equals(localKeepAliveKey.protocol)) && (this.obj == localKeepAliveKey.obj));
  }

  public int hashCode()
  {
    String str = this.protocol + this.host + this.port;
    return ((this.obj == null) ? str.hashCode() : str.hashCode() + this.obj.hashCode());
  }
}