package sun.security.krb5.internal;

import B;
import java.io.IOException;
import java.math.BigInteger;
import sun.security.krb5.Asn1Exception;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class PAData
{
  private int pADataType;
  private byte[] pADataValue = null;
  private static final byte TAG_PATYPE = 1;
  private static final byte TAG_PAVALUE = 2;

  private PAData()
  {
  }

  public PAData(int paramInt, byte[] paramArrayOfByte)
  {
    this.pADataType = paramInt;
    if (paramArrayOfByte != null)
      this.pADataValue = ((byte[])(byte[])paramArrayOfByte.clone());
  }

  public Object clone()
  {
    PAData localPAData = new PAData();
    localPAData.pADataType = this.pADataType;
    if (this.pADataValue != null)
    {
      localPAData.pADataValue = new byte[this.pADataValue.length];
      System.arraycopy(this.pADataValue, 0, localPAData.pADataValue, 0, this.pADataValue.length);
    }
    return localPAData;
  }

  public PAData(DerValue paramDerValue)
    throws Asn1Exception, IOException
  {
    DerValue localDerValue = null;
    if (paramDerValue.getTag() != 48)
      throw new Asn1Exception(906);
    localDerValue = paramDerValue.getData().getDerValue();
    if ((localDerValue.getTag() & 0x1F) == 1)
      this.pADataType = localDerValue.getData().getBigInteger().intValue();
    else
      throw new Asn1Exception(906);
    localDerValue = paramDerValue.getData().getDerValue();
    if ((localDerValue.getTag() & 0x1F) == 2)
      this.pADataValue = localDerValue.getData().getOctetString();
    if (paramDerValue.getData().available() > 0)
      throw new Asn1Exception(906);
  }

  public byte[] asn1Encode()
    throws Asn1Exception, IOException
  {
    DerOutputStream localDerOutputStream1 = new DerOutputStream();
    DerOutputStream localDerOutputStream2 = new DerOutputStream();
    localDerOutputStream2.putInteger(this.pADataType);
    localDerOutputStream1.write(DerValue.createTag(-128, true, 1), localDerOutputStream2);
    localDerOutputStream2 = new DerOutputStream();
    localDerOutputStream2.putOctetString(this.pADataValue);
    localDerOutputStream1.write(DerValue.createTag(-128, true, 2), localDerOutputStream2);
    localDerOutputStream2 = new DerOutputStream();
    localDerOutputStream2.write(48, localDerOutputStream1);
    return localDerOutputStream2.toByteArray();
  }

  public int getType()
  {
    return this.pADataType;
  }

  public byte[] getValue()
  {
    return ((this.pADataValue == null) ? null : (byte[])(byte[])this.pADataValue.clone());
  }
}