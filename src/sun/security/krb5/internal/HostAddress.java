package sun.security.krb5.internal;

import B;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import sun.security.krb5.Asn1Exception;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class HostAddress
  implements Cloneable
{
  int addrType;
  byte[] address = null;
  private static InetAddress localInetAddress;
  private static final boolean DEBUG = Krb5.DEBUG;
  private volatile int hashCode = 0;

  private HostAddress(int paramInt)
  {
  }

  public Object clone()
  {
    HostAddress localHostAddress = new HostAddress(0);
    localHostAddress.addrType = this.addrType;
    if (this.address != null)
      localHostAddress.address = ((byte[])(byte[])this.address.clone());
    return localHostAddress;
  }

  public int hashCode()
  {
    if (this.hashCode == 0)
    {
      int i = 17;
      i = 37 * i + this.addrType;
      if (this.address != null)
        for (int j = 0; j < this.address.length; ++j)
          i = 37 * i + this.address[j];
      this.hashCode = i;
    }
    return this.hashCode;
  }

  public boolean equals(Object paramObject)
  {
    if (this == paramObject)
      return true;
    if (!(paramObject instanceof HostAddress))
      return false;
    HostAddress localHostAddress = (HostAddress)paramObject;
    if ((this.addrType != localHostAddress.addrType) || ((this.address != null) && (localHostAddress.address == null)) || ((this.address == null) && (localHostAddress.address != null)))
      return false;
    if ((this.address != null) && (localHostAddress.address != null))
    {
      if (this.address.length != localHostAddress.address.length)
        return false;
      for (int i = 0; i < this.address.length; ++i)
        if (this.address[i] != localHostAddress.address[i])
          return false;
    }
    return true;
  }

  private static synchronized InetAddress getLocalInetAddress()
    throws UnknownHostException
  {
    if (localInetAddress == null)
      localInetAddress = InetAddress.getLocalHost();
    if (localInetAddress == null)
      throw new UnknownHostException();
    return localInetAddress;
  }

  public InetAddress getInetAddress()
    throws UnknownHostException
  {
    if ((this.addrType == 2) || (this.addrType == 24))
      return InetAddress.getByAddress(this.address);
    return null;
  }

  private int getAddrType(InetAddress paramInetAddress)
  {
    int i = 0;
    if (paramInetAddress instanceof Inet4Address)
      i = 2;
    else if (paramInetAddress instanceof Inet6Address)
      i = 24;
    return i;
  }

  public HostAddress()
    throws UnknownHostException
  {
    InetAddress localInetAddress1 = getLocalInetAddress();
    this.addrType = getAddrType(localInetAddress1);
    this.address = localInetAddress1.getAddress();
  }

  public HostAddress(int paramInt, byte[] paramArrayOfByte)
    throws sun.security.krb5.internal.KrbApErrException, UnknownHostException
  {
    switch (paramInt)
    {
    case 2:
      if (paramArrayOfByte.length != 4)
        throw new sun.security.krb5.internal.KrbApErrException(0, "Invalid Internet address");
    case 5:
      if (paramArrayOfByte.length != 2)
        throw new sun.security.krb5.internal.KrbApErrException(0, "Invalid CHAOSnet address");
    case 7:
      break;
    case 6:
      if (paramArrayOfByte.length != 6)
        throw new sun.security.krb5.internal.KrbApErrException(0, "Invalid XNS address");
    case 16:
      if (paramArrayOfByte.length != 3)
        throw new sun.security.krb5.internal.KrbApErrException(0, "Invalid DDP address");
    case 12:
      if (paramArrayOfByte.length != 2)
        throw new sun.security.krb5.internal.KrbApErrException(0, "Invalid DECnet Phase IV address");
    case 24:
      if (paramArrayOfByte.length != 16)
        throw new sun.security.krb5.internal.KrbApErrException(0, "Invalid Internet IPv6 address");
    case 3:
    case 4:
    case 8:
    case 9:
    case 10:
    case 11:
    case 13:
    case 14:
    case 15:
    case 17:
    case 18:
    case 19:
    case 20:
    case 21:
    case 22:
    case 23:
    }
    this.addrType = paramInt;
    if (paramArrayOfByte != null)
      this.address = ((byte[])(byte[])paramArrayOfByte.clone());
    if ((DEBUG) && (((this.addrType == 2) || (this.addrType == 24))))
      System.out.println("Host address is " + InetAddress.getByAddress(this.address));
  }

  public HostAddress(InetAddress paramInetAddress)
  {
    this.addrType = getAddrType(paramInetAddress);
    this.address = paramInetAddress.getAddress();
  }

  public HostAddress(DerValue paramDerValue)
    throws Asn1Exception, IOException
  {
    DerValue localDerValue = paramDerValue.getData().getDerValue();
    if ((localDerValue.getTag() & 0x1F) == 0)
      this.addrType = localDerValue.getData().getBigInteger().intValue();
    else
      throw new Asn1Exception(906);
    localDerValue = paramDerValue.getData().getDerValue();
    if ((localDerValue.getTag() & 0x1F) == 1)
      this.address = localDerValue.getData().getOctetString();
    else
      throw new Asn1Exception(906);
    if (paramDerValue.getData().available() > 0)
      throw new Asn1Exception(906);
  }

  public byte[] asn1Encode()
    throws Asn1Exception, IOException
  {
    DerOutputStream localDerOutputStream1 = new DerOutputStream();
    DerOutputStream localDerOutputStream2 = new DerOutputStream();
    localDerOutputStream2.putInteger(this.addrType);
    localDerOutputStream1.write(DerValue.createTag(-128, true, 0), localDerOutputStream2);
    localDerOutputStream2 = new DerOutputStream();
    localDerOutputStream2.putOctetString(this.address);
    localDerOutputStream1.write(DerValue.createTag(-128, true, 1), localDerOutputStream2);
    localDerOutputStream2 = new DerOutputStream();
    localDerOutputStream2.write(48, localDerOutputStream1);
    return localDerOutputStream2.toByteArray();
  }

  public static HostAddress parse(DerInputStream paramDerInputStream, byte paramByte, boolean paramBoolean)
    throws Asn1Exception, IOException
  {
    if ((paramBoolean) && (((byte)paramDerInputStream.peekByte() & 0x1F) != paramByte))
      return null;
    DerValue localDerValue1 = paramDerInputStream.getDerValue();
    if (paramByte != (localDerValue1.getTag() & 0x1F))
      throw new Asn1Exception(906);
    DerValue localDerValue2 = localDerValue1.getData().getDerValue();
    return new HostAddress(localDerValue2);
  }
}