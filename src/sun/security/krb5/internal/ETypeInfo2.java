package sun.security.krb5.internal;

import B;
import java.io.IOException;
import java.math.BigInteger;
import sun.security.krb5.Asn1Exception;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class ETypeInfo2
{
  private int etype;
  private String saltStr = null;
  private byte[] s2kparams = null;
  private static final byte TAG_TYPE = 0;
  private static final byte TAG_VALUE1 = 1;
  private static final byte TAG_VALUE2 = 2;

  private ETypeInfo2()
  {
  }

  public ETypeInfo2(int paramInt, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2)
  {
    this.etype = paramInt;
    if (paramArrayOfByte1 != null)
      this.saltStr = new String(paramArrayOfByte1);
    if (paramArrayOfByte2 != null)
      this.s2kparams = ((byte[])(byte[])paramArrayOfByte2.clone());
  }

  public Object clone()
  {
    ETypeInfo2 localETypeInfo2 = new ETypeInfo2();
    localETypeInfo2.etype = this.etype;
    localETypeInfo2.saltStr = this.saltStr;
    if (this.s2kparams != null)
    {
      localETypeInfo2.s2kparams = new byte[this.s2kparams.length];
      System.arraycopy(this.s2kparams, 0, localETypeInfo2.s2kparams, 0, this.s2kparams.length);
    }
    return localETypeInfo2;
  }

  public ETypeInfo2(DerValue paramDerValue)
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
        this.saltStr = localDerValue.getData().getGeneralString();
    }
    if (paramDerValue.getData().available() > 0)
    {
      localDerValue = paramDerValue.getData().getDerValue();
      if ((localDerValue.getTag() & 0x1F) == 2)
        this.s2kparams = localDerValue.getData().getOctetString();
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
    if (this.saltStr != null)
    {
      localDerOutputStream2 = new DerOutputStream();
      localDerOutputStream2.putGeneralString(this.saltStr);
      localDerOutputStream1.write(DerValue.createTag(-128, true, 1), localDerOutputStream2);
    }
    if (this.s2kparams != null)
    {
      localDerOutputStream2 = new DerOutputStream();
      localDerOutputStream2.putOctetString(this.s2kparams);
      localDerOutputStream1.write(DerValue.createTag(-128, true, 2), localDerOutputStream2);
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
    return ((this.saltStr == null) ? null : (byte[])this.saltStr.getBytes());
  }

  public byte[] getParams()
  {
    return ((this.s2kparams == null) ? null : (byte[])(byte[])this.s2kparams.clone());
  }
}