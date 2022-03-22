package sun.security.krb5.internal;

import B;
import java.io.IOException;
import java.math.BigInteger;
import sun.security.krb5.Asn1Exception;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class MethodData
{
  private int methodType;
  private byte[] methodData = null;

  public MethodData(int paramInt, byte[] paramArrayOfByte)
  {
    this.methodType = paramInt;
    if (paramArrayOfByte != null)
      this.methodData = ((byte[])(byte[])paramArrayOfByte.clone());
  }

  public MethodData(DerValue paramDerValue)
    throws Asn1Exception, IOException
  {
    if (paramDerValue.getTag() != 48)
      throw new Asn1Exception(906);
    DerValue localDerValue = paramDerValue.getData().getDerValue();
    if ((localDerValue.getTag() & 0x1F) == 0)
    {
      BigInteger localBigInteger = localDerValue.getData().getBigInteger();
      this.methodType = localBigInteger.intValue();
    }
    else
    {
      throw new Asn1Exception(906);
    }
    if (paramDerValue.getData().available() > 0)
    {
      localDerValue = paramDerValue.getData().getDerValue();
      if ((localDerValue.getTag() & 0x1F) == 1)
        this.methodData = localDerValue.getData().getOctetString();
      else
        throw new Asn1Exception(906);
    }
    if (paramDerValue.getData().available() > 0)
      throw new Asn1Exception(906);
  }

  public byte[] asn1Encode()
    throws Asn1Exception, IOException
  {
    DerOutputStream localDerOutputStream1 = new DerOutputStream();
    DerOutputStream localDerOutputStream2 = new DerOutputStream();
    localDerOutputStream2.putInteger(BigInteger.valueOf(this.methodType));
    localDerOutputStream1.write(DerValue.createTag(-128, true, 0), localDerOutputStream2);
    if (this.methodData != null)
    {
      localDerOutputStream2 = new DerOutputStream();
      localDerOutputStream2.putOctetString(this.methodData);
      localDerOutputStream1.write(DerValue.createTag(-128, true, 1), localDerOutputStream2);
    }
    localDerOutputStream2 = new DerOutputStream();
    localDerOutputStream2.write(48, localDerOutputStream1);
    return localDerOutputStream2.toByteArray();
  }
}