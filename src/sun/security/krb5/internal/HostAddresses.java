package sun.security.krb5.internal;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Vector;
import sun.security.krb5.Asn1Exception;
import sun.security.krb5.KrbException;
import sun.security.krb5.PrincipalName;
import sun.security.krb5.internal.ccache.CCacheOutputStream;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class HostAddresses
  implements Cloneable
{
  private static boolean DEBUG = Krb5.DEBUG;
  private HostAddress[] addresses = null;
  private volatile int hashCode = 0;

  public HostAddresses(HostAddress[] paramArrayOfHostAddress)
    throws IOException
  {
    if (paramArrayOfHostAddress != null)
    {
      this.addresses = new HostAddress[paramArrayOfHostAddress.length];
      for (int i = 0; i < paramArrayOfHostAddress.length; ++i)
      {
        if (paramArrayOfHostAddress[i] == null)
          throw new IOException("Cannot create a HostAddress");
        this.addresses[i] = ((HostAddress)paramArrayOfHostAddress[i].clone());
      }
    }
  }

  public HostAddresses()
    throws UnknownHostException
  {
    this.addresses = new HostAddress[1];
    this.addresses[0] = new HostAddress();
  }

  private HostAddresses(int paramInt)
  {
  }

  public HostAddresses(PrincipalName paramPrincipalName)
    throws UnknownHostException, KrbException
  {
    String[] arrayOfString = paramPrincipalName.getNameStrings();
    if ((paramPrincipalName.getNameType() != 3) || (arrayOfString.length < 2))
      throw new KrbException(60, "Bad name");
    String str = arrayOfString[1];
    InetAddress[] arrayOfInetAddress = InetAddress.getAllByName(str);
    HostAddress[] arrayOfHostAddress = new HostAddress[arrayOfInetAddress.length];
    for (int i = 0; i < arrayOfInetAddress.length; ++i)
      arrayOfHostAddress[i] = new HostAddress(arrayOfInetAddress[i]);
    this.addresses = arrayOfHostAddress;
  }

  public Object clone()
  {
    HostAddresses localHostAddresses = new HostAddresses(0);
    if (this.addresses != null)
    {
      localHostAddresses.addresses = new HostAddress[this.addresses.length];
      for (int i = 0; i < this.addresses.length; ++i)
        localHostAddresses.addresses[i] = ((HostAddress)this.addresses[i].clone());
    }
    return localHostAddresses;
  }

  public boolean inList(HostAddress paramHostAddress)
  {
    if (this.addresses != null)
      for (int i = 0; i < this.addresses.length; ++i)
        if (this.addresses[i].equals(paramHostAddress))
          return true;
    return false;
  }

  public int hashCode()
  {
    if (this.hashCode == 0)
    {
      int i = 17;
      if (this.addresses != null)
        for (int j = 0; j < this.addresses.length; ++j)
          i = 37 * i + this.addresses[j].hashCode();
      this.hashCode = i;
    }
    return this.hashCode;
  }

  public boolean equals(Object paramObject)
  {
    if (this == paramObject)
      return true;
    if (!(paramObject instanceof HostAddresses))
      return false;
    HostAddresses localHostAddresses = (HostAddresses)paramObject;
    if (((this.addresses == null) && (localHostAddresses.addresses != null)) || ((this.addresses != null) && (localHostAddresses.addresses == null)))
      return false;
    if ((this.addresses != null) && (localHostAddresses.addresses != null))
    {
      if (this.addresses.length != localHostAddresses.addresses.length)
        return false;
      for (int i = 0; i < this.addresses.length; ++i)
        if (!(this.addresses[i].equals(localHostAddresses.addresses[i])))
          return false;
    }
    return true;
  }

  public HostAddresses(DerValue paramDerValue)
    throws Asn1Exception, IOException
  {
    Vector localVector = new Vector();
    DerValue localDerValue = null;
    while (paramDerValue.getData().available() > 0)
    {
      localDerValue = paramDerValue.getData().getDerValue();
      localVector.addElement(new HostAddress(localDerValue));
    }
    if (localVector.size() > 0)
    {
      this.addresses = new HostAddress[localVector.size()];
      localVector.copyInto(this.addresses);
    }
  }

  public byte[] asn1Encode()
    throws Asn1Exception, IOException
  {
    DerOutputStream localDerOutputStream1 = new DerOutputStream();
    DerOutputStream localDerOutputStream2 = new DerOutputStream();
    if ((this.addresses != null) && (this.addresses.length > 0))
      for (int i = 0; i < this.addresses.length; ++i)
        localDerOutputStream1.write(this.addresses[i].asn1Encode());
    localDerOutputStream2.write(48, localDerOutputStream1);
    return localDerOutputStream2.toByteArray();
  }

  public static HostAddresses parse(DerInputStream paramDerInputStream, byte paramByte, boolean paramBoolean)
    throws Asn1Exception, IOException
  {
    if ((paramBoolean) && (((byte)paramDerInputStream.peekByte() & 0x1F) != paramByte))
      return null;
    DerValue localDerValue1 = paramDerInputStream.getDerValue();
    if (paramByte != (localDerValue1.getTag() & 0x1F))
      throw new Asn1Exception(906);
    DerValue localDerValue2 = localDerValue1.getData().getDerValue();
    return new HostAddresses(localDerValue2);
  }

  public void writeAddrs(CCacheOutputStream paramCCacheOutputStream)
    throws IOException
  {
    paramCCacheOutputStream.write32(this.addresses.length);
    for (int i = 0; i < this.addresses.length; ++i)
    {
      paramCCacheOutputStream.write16(this.addresses[i].addrType);
      paramCCacheOutputStream.write32(this.addresses[i].address.length);
      paramCCacheOutputStream.write(this.addresses[i].address, 0, this.addresses[i].address.length);
    }
  }

  public InetAddress[] getInetAddresses()
  {
    if ((this.addresses == null) || (this.addresses.length == 0))
      return null;
    ArrayList localArrayList = new ArrayList(this.addresses.length);
    for (int i = 0; i < this.addresses.length; ++i)
      try
      {
        if ((this.addresses[i].addrType == 2) || (this.addresses[i].addrType == 24))
          localArrayList.add(this.addresses[i].getInetAddress());
      }
      catch (UnknownHostException localUnknownHostException)
      {
        return null;
      }
    InetAddress[] arrayOfInetAddress = new InetAddress[localArrayList.size()];
    return ((InetAddress[])(InetAddress[])localArrayList.toArray(arrayOfInetAddress));
  }

  public static HostAddresses getLocalAddresses()
    throws IOException
  {
    String str = null;
    InetAddress[] arrayOfInetAddress = null;
    try
    {
      InetAddress localInetAddress = InetAddress.getLocalHost();
      str = localInetAddress.getHostName();
      arrayOfInetAddress = InetAddress.getAllByName(str);
      HostAddress[] arrayOfHostAddress = new HostAddress[arrayOfInetAddress.length];
      for (int i = 0; i < arrayOfInetAddress.length; ++i)
        arrayOfHostAddress[i] = new HostAddress(arrayOfInetAddress[i]);
      if (DEBUG)
      {
        System.out.println(">>> KrbKdcReq local addresses for " + str + " are: ");
        for (i = 0; i < arrayOfInetAddress.length; ++i)
        {
          System.out.println("\n\t" + arrayOfInetAddress[i]);
          if (arrayOfInetAddress[i] instanceof Inet4Address)
            System.out.println("IPv4 address");
          if (arrayOfInetAddress[i] instanceof Inet6Address)
            System.out.println("IPv6 address");
        }
      }
      return new HostAddresses(arrayOfHostAddress);
    }
    catch (Exception localException)
    {
      throw new IOException(localException.toString());
    }
  }

  public HostAddresses(InetAddress[] paramArrayOfInetAddress)
  {
    if (paramArrayOfInetAddress == null)
    {
      this.addresses = null;
      return;
    }
    this.addresses = new HostAddress[paramArrayOfInetAddress.length];
    for (int i = 0; i < paramArrayOfInetAddress.length; ++i)
      this.addresses[i] = new HostAddress(paramArrayOfInetAddress[i]);
  }
}