package sun.management.snmp.jvminstr;

import java.net.InetAddress;

public abstract interface NotificationTarget
{
  public abstract InetAddress getAddress();

  public abstract int getPort();

  public abstract String getCommunity();
}