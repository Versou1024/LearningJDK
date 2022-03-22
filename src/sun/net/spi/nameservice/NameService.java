package sun.net.spi.nameservice;

import java.net.InetAddress;
import java.net.UnknownHostException;

public abstract interface NameService
{
  public abstract InetAddress[] lookupAllHostAddr(String paramString)
    throws UnknownHostException;

  public abstract String getHostByAddr(byte[] paramArrayOfByte)
    throws UnknownHostException;
}