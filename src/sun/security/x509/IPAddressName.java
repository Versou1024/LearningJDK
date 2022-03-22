package sun.security.x509;

import B;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Arrays;
import sun.misc.HexDumpEncoder;
import sun.security.util.BitArray;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class IPAddressName
  implements GeneralNameInterface
{
  private byte[] address;
  private boolean isIPv4;
  private String name;
  private static final int MASKSIZE = 16;

  public IPAddressName(DerValue paramDerValue)
    throws IOException
  {
    this(paramDerValue.getOctetString());
  }

  public IPAddressName(byte[] paramArrayOfByte)
    throws IOException
  {
    if ((paramArrayOfByte.length == 4) || (paramArrayOfByte.length == 8))
      this.isIPv4 = true;
    else if ((paramArrayOfByte.length == 16) || (paramArrayOfByte.length == 32))
      this.isIPv4 = false;
    else
      throw new IOException("Invalid IPAddressName");
    this.address = paramArrayOfByte;
  }

  public IPAddressName(String paramString)
    throws IOException
  {
    if ((paramString == null) || (paramString.length() == 0))
      throw new IOException("IPAddress cannot be null or empty");
    if (paramString.charAt(paramString.length() - 1) == '/')
      throw new IOException("Invalid IPAddress: " + paramString);
    if (paramString.indexOf(58) >= 0)
    {
      parseIPv6(paramString);
      this.isIPv4 = false;
    }
    else if (paramString.indexOf(46) >= 0)
    {
      parseIPv4(paramString);
      this.isIPv4 = true;
    }
    else
    {
      throw new IOException("Invalid IPAddress: " + paramString);
    }
  }

  private void parseIPv4(String paramString)
    throws IOException
  {
    int i = paramString.indexOf(47);
    if (i == -1)
    {
      this.address = InetAddress.getByName(paramString).getAddress();
    }
    else
    {
      this.address = new byte[8];
      byte[] arrayOfByte1 = InetAddress.getByName(paramString.substring(i + 1)).getAddress();
      byte[] arrayOfByte2 = InetAddress.getByName(paramString.substring(0, i)).getAddress();
      System.arraycopy(arrayOfByte2, 0, this.address, 0, 4);
      System.arraycopy(arrayOfByte1, 0, this.address, 4, 4);
    }
  }

  private void parseIPv6(String paramString)
    throws IOException
  {
    int i = paramString.indexOf(47);
    if (i == -1)
    {
      this.address = InetAddress.getByName(paramString).getAddress();
    }
    else
    {
      this.address = new byte[32];
      byte[] arrayOfByte1 = InetAddress.getByName(paramString.substring(0, i)).getAddress();
      System.arraycopy(arrayOfByte1, 0, this.address, 0, 16);
      int j = Integer.parseInt(paramString.substring(i + 1));
      if (j > 128)
        throw new IOException("IPv6Address prefix is longer than 128");
      BitArray localBitArray = new BitArray(128);
      for (int k = 0; k < j; ++k)
        localBitArray.set(k, true);
      byte[] arrayOfByte2 = localBitArray.toByteArray();
      for (int l = 0; l < 16; ++l)
        this.address[(16 + l)] = arrayOfByte2[l];
    }
  }

  public int getType()
  {
    return 7;
  }

  public void encode(DerOutputStream paramDerOutputStream)
    throws IOException
  {
    paramDerOutputStream.putOctetString(this.address);
  }

  public String toString()
  {
    HexDumpEncoder localHexDumpEncoder;
    try
    {
      return "IPAddress: " + getName();
    }
    catch (IOException localIOException)
    {
      localHexDumpEncoder = new HexDumpEncoder();
    }
    return "IPAddress: " + localHexDumpEncoder.encodeBuffer(this.address);
  }

  public String getName()
    throws IOException
  {
    byte[] arrayOfByte1;
    byte[] arrayOfByte2;
    if (this.name != null)
      return this.name;
    if (this.isIPv4)
    {
      arrayOfByte1 = new byte[4];
      System.arraycopy(this.address, 0, arrayOfByte1, 0, 4);
      this.name = InetAddress.getByAddress(arrayOfByte1).getHostAddress();
      if (this.address.length == 8)
      {
        arrayOfByte2 = new byte[4];
        System.arraycopy(this.address, 4, arrayOfByte2, 0, 4);
        this.name = this.name + "/" + InetAddress.getByAddress(arrayOfByte2).getHostAddress();
      }
    }
    else
    {
      arrayOfByte1 = new byte[16];
      System.arraycopy(this.address, 0, arrayOfByte1, 0, 16);
      this.name = InetAddress.getByAddress(arrayOfByte1).getHostAddress();
      if (this.address.length == 32)
      {
        arrayOfByte2 = new byte[16];
        for (int i = 16; i < 32; ++i)
          arrayOfByte2[(i - 16)] = this.address[i];
        BitArray localBitArray = new BitArray(128, arrayOfByte2);
        for (int j = 0; j < 128; ++j)
          if (!(localBitArray.get(j)))
            break;
        this.name = this.name + "/" + j;
        while (j < 128)
        {
          if (localBitArray.get(j))
            throw new IOException("Invalid IPv6 subdomain - set bit " + j + " not contiguous");
          ++j;
        }
      }
    }
    return this.name;
  }

  public byte[] getBytes()
  {
    return ((byte[])(byte[])this.address.clone());
  }

  public boolean equals(Object paramObject)
  {
    if (this == paramObject)
      return true;
    if (!(paramObject instanceof IPAddressName))
      return false;
    byte[] arrayOfByte1 = ((IPAddressName)paramObject).getBytes();
    if (arrayOfByte1.length != this.address.length)
      return false;
    if ((this.address.length == 8) || (this.address.length == 32))
    {
      int i = this.address.length / 2;
      byte[] arrayOfByte2 = new byte[i];
      byte[] arrayOfByte3 = new byte[i];
      for (int j = 0; j < i; ++j)
      {
        arrayOfByte2[j] = (byte)(this.address[j] & this.address[(j + i)]);
        arrayOfByte3[j] = (byte)(arrayOfByte1[j] & arrayOfByte1[(j + i)]);
        if (arrayOfByte2[j] != arrayOfByte3[j])
          return false;
      }
      for (j = i; j < this.address.length; ++j)
        if (this.address[j] != arrayOfByte1[j])
          return false;
      return true;
    }
    return Arrays.equals(arrayOfByte1, this.address);
  }

  public int hashCode()
  {
    int i = 0;
    for (int j = 0; j < this.address.length; ++j)
      i += this.address[j] * j;
    return i;
  }

  public int constrains(GeneralNameInterface paramGeneralNameInterface)
    throws UnsupportedOperationException
  {
    int i;
    if (paramGeneralNameInterface == null)
    {
      i = -1;
    }
    else if (paramGeneralNameInterface.getType() != 7)
    {
      i = -1;
    }
    else if (((IPAddressName)paramGeneralNameInterface).equals(this))
    {
      i = 0;
    }
    else
    {
      byte[] arrayOfByte = ((IPAddressName)paramGeneralNameInterface).getBytes();
      if ((arrayOfByte.length == 4) && (this.address.length == 4))
      {
        i = 3;
      }
      else
      {
        int j;
        int k;
        if (((arrayOfByte.length == 8) && (this.address.length == 8)) || ((arrayOfByte.length == 32) && (this.address.length == 32)))
        {
          j = 1;
          k = 1;
          int l = 0;
          int i1 = 0;
          int i2 = this.address.length / 2;
          for (int i3 = 0; i3 < i2; ++i3)
          {
            if ((byte)(this.address[i3] & this.address[(i3 + i2)]) != this.address[i3])
              l = 1;
            if ((byte)(arrayOfByte[i3] & arrayOfByte[(i3 + i2)]) != arrayOfByte[i3])
              i1 = 1;
            if (((byte)(this.address[(i3 + i2)] & arrayOfByte[(i3 + i2)]) != this.address[(i3 + i2)]) || ((byte)(this.address[i3] & this.address[(i3 + i2)]) != (byte)(arrayOfByte[i3] & this.address[(i3 + i2)])))
              j = 0;
            if (((byte)(arrayOfByte[(i3 + i2)] & this.address[(i3 + i2)]) != arrayOfByte[(i3 + i2)]) || ((byte)(arrayOfByte[i3] & arrayOfByte[(i3 + i2)]) != (byte)(this.address[i3] & arrayOfByte[(i3 + i2)])))
              k = 0;
          }
          if ((l != 0) || (i1 != 0))
            if ((l != 0) && (i1 != 0))
              i = 0;
            else if (l != 0)
              i = 2;
            else
              i = 1;
          else if (j != 0)
            i = 1;
          else if (k != 0)
            i = 2;
          else
            i = 3;
        }
        else if ((arrayOfByte.length == 8) || (arrayOfByte.length == 32))
        {
          j = 0;
          k = arrayOfByte.length / 2;
          while (j < k)
          {
            if ((this.address[j] & arrayOfByte[(j + k)]) != arrayOfByte[j])
              break;
            ++j;
          }
          if (j == k)
            i = 2;
          else
            i = 3;
        }
        else if ((this.address.length == 8) || (this.address.length == 32))
        {
          j = 0;
          k = this.address.length / 2;
          while (j < k)
          {
            if ((arrayOfByte[j] & this.address[(j + k)]) != this.address[j])
              break;
            ++j;
          }
          if (j == k)
            i = 1;
          else
            i = 3;
        }
        else
        {
          i = 3;
        }
      }
    }
    return i;
  }

  public int subtreeDepth()
    throws UnsupportedOperationException
  {
    throw new UnsupportedOperationException("subtreeDepth() not defined for IPAddressName");
  }
}