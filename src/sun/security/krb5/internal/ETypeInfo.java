package sun.security.krb5.internal;

import B;
import java.io.IOException;
import java.math.BigInteger;
import sun.security.krb5.Asn1Exception;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class ETypeInfo
{
  private int etype;
  private byte[] salt = null;
  private static final byte TAG_TYPE = 0;
  private static final byte TAG_VALUE = 1;

  private ETypeInfo()
  {
  }

  public ETypeInfo(int paramInt, byte[] paramArrayOfByte)
  {
    this.etype = paramInt;
    if (paramArrayOfByte != null)
      this.salt = ((byte[])(byte[])paramArrayOfByte.clone());
  }

  public Object clone()
  {
    ETypeInfo localETypeInfo = new ETypeInfo();
    localETypeInfo.etype = this.etype;
    if (this.salt != null)
    {
      localETypeInfo.salt = new byte[this.salt.length];
      System.arraycopy(this.salt, 0, localETypeInfo.salt, 0, this.salt.length);
    }
    return localETypeInfo;
  }

  public ETypeInfo(DerValue paramDerValue)
    throws Asn1Exception, IOException
  {
    DerValue localDerValue = null;
    if (paramDerValue.getTag() != 48)
      throw new Asn1Exception(906);
    localDerValue = paramDerValue.getData().getDerValue();
    if ((localDerValue.getTag() & 0x1F) == 0)
      this.etype = localDerValue.getData().getBigInteger().intValue();
    else
      throw new Asn1Exception(906);
    if (paramDerValue.getData().available() > 0)
    {
      localDerValue = paramDerValue.getData().getDerValue();
      if ((localDerValue.getTag() & 0x1F) == 1)
        this.salt = localDerValue.getData().getOctetString();
    }
    if (paramDerValue.getData().available() > 0)
      throw new Asn1Exception(906);
  }

  public byte[] asn1Encode()
    throws Asn1Exception, IOException
  {
    DerOutputStream localDerOutputStream1 = new DerOutputStream();
    DerOutputStream localDerOutputStream2 = new DerOutputStream();
    localDerOutputStream2.putInteger(this.etype);
    localDerOutputStream1.write(DerValue.createTag(-128, true, 0), localDerOutputStream2);
    if (this.salt != null)
    {
      localDerOutputStream2 = new DerOutputStream();
      localDerOutputStream2.putOctetString(this.salt);
      localDerOutputStream1.write(DerValue.createTag(-128, true, 1), localDerOutputStream2);
    }
    localDerOutputStream2 = new DerOutputStream();
    localDerOutputStream2.write(48, localDerOutputStream1);
    return localDerOutputStream2.toByteArray();
  }

  public int getEType()
  {
    return this.etype;
  }

  public byte[] getSalt()
  {
    return ((this.salt == null) ? null : (byte[])(byte[])this.salt.clone());
  }
}