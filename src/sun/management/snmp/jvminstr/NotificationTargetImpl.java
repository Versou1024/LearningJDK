package sun.management.snmp.jvminstr;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class NotificationTargetImpl
  implements NotificationTarget
{
  private InetAddress address;
  private int port;
  private String community;

  public NotificationTargetImpl(String paramString)
    throws IllegalArgumentException, UnknownHostException
  {
    parseTarget(paramString);
  }

  public NotificationTargetImpl(String paramString1, int paramInt, String paramString2)
    throws UnknownHostException
  {
    this(InetAddress.getByName(paramString1), paramInt, paramString2);
  }

  public NotificationTargetImpl(InetAddress paramInetAddress, int paramInt, String paramString)
  {
    this.address = paramInetAddress;
    this.port = paramInt;
    this.community = paramString;
  }

  private void parseTarget(String paramString)
    throws IllegalArgumentException, UnknownHostException
  {
    String str;
    int j;
    if ((paramString == null) || (paramString.length() == 0))
      throw new IllegalArgumentException("Invalid target [" + paramString + "]");
    if (paramString.startsWith("["))
    {
      i = paramString.indexOf("]");
      j = paramString.lastIndexOf(":");
      if (i == -1)
        throw new IllegalArgumentException("Host starts with [ but does not end with ]");
      str = paramString.substring(1, i);
      this.port = Integer.parseInt(paramString.substring(i + 2, j));
      if (!(isNumericIPv6Address(str)))
        throw new IllegalArgumentException("Address inside [...] must be numeric IPv6 address");
      if (str.startsWith("["))
        throw new IllegalArgumentException("More than one [[...]]");
    }
    else
    {
      i = paramString.indexOf(":");
      j = paramString.lastIndexOf(":");
      if (i == -1)
        throw new IllegalArgumentException("Missing port separator \":\"");
      str = paramString.substring(0, i);
      this.port = Integer.parseInt(paramString.substring(i + 1, j));
    }
    this.address = InetAddress.getByName(str);
    int i = paramString.lastIndexOf(":");
    this.community = paramString.substring(i + 1, paramString.length());
  }

  private static boolean isNumericIPv6Address(String paramString)
  {
    return (paramString.indexOf(58) >= 0);
  }

  public String getCommunity()
  {
    return this.community;
  }

  public InetAddress getAddress()
  {
    return this.address;
  }

  public int getPort()
  {
    return this.port;
  }

  public String toString()
  {
    return "address : " + this.address + " port : " + this.port + " community : " + this.community;
  }
}