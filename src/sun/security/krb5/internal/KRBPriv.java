package sun.security.krb5.internal;

import java.io.IOException;
import java.math.BigInteger;
import sun.security.krb5.Asn1Exception;
import sun.security.krb5.EncryptedData;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class KRBPriv
{
  public int pvno;
  public int msgType;
  public EncryptedData encPart;

  public KRBPriv(EncryptedData paramEncryptedData)
  {
    this.pvno = 5;
    this.msgType = 21;
    this.encPart = paramEncryptedData;
  }

  public KRBPriv(byte[] paramArrayOfByte)
    throws Asn1Exception, sun.security.krb5.internal.KrbApErrException, IOException
  {
    init(new DerValue(paramArrayOfByte));
  }

  public KRBPriv(DerValue paramDerValue)
    throws Asn1Exception, sun.security.krb5.internal.KrbApErrException, IOException
  {
    init(paramDerValue);
  }

  private void init(DerValue paramDerValue)
    throws Asn1Exception, sun.security.krb5.internal.KrbApErrException, IOException
  {
    if (((paramDerValue.getTag() & 0x1F) != 21) || (paramDerValue.isApplication() != true) || (paramDerValue.isConstructed() != true))
      throw new Asn1Exception(906);
    DerValue localDerValue1 = paramDerValue.getData().getDerValue();
    if (localDerValue1.getTag() != 48)
      throw new Asn1Exception(906);
    DerValue localDerValue2 = localDerValue1.getData().getDerValue();
    if ((localDerValue2.getTag() & 0x1F) == 0)
    {
      this.pvno = localDerValue2.getData().getBigInteger().intValue();
      if (this.pvno == 5)
        break label128;
      throw new sun.security.krb5.internal.KrbApErrException(39);
    }
    throw new Asn1Exception(906);
    label128: localDerValue2 = localDerValue1.getData().getDerValue();
    if ((localDerValue2.getTag() & 0x1F) == 1)
    {
      this.msgType = localDerValue2.getData().getBigInteger().intValue();
      if (this.msgType == 21)
        break label191;
      throw new sun.security.krb5.internal.KrbApErrException(40);
    }
    throw new Asn1Exception(906);
    label191: this.encPart = EncryptedData.parse(localDerValue1.getData(), 3, false);
    if (localDerValue1.getData().available() > 0)
      throw new Asn1Exception(906);
  }

  public byte[] asn1Encode()
    throws Asn1Exception, IOException
  {
    DerOutputStream localDerOutputStream1 = new DerOutputStream();
    localDerOutputStream1.putInteger(BigInteger.valueOf(this.pvno));
    DerOutputStream localDerOutputStream2 = new DerOutputStream();
    localDerOutputStream2.write(DerValue.createTag(-128, true, 0), localDerOutputStream1);
    localDerOutputStream1 = new DerOutputStream();
    localDerOutputStream1.putInteger(BigInteger.valueOf(this.msgType));
    localDerOutputStream2.write(DerValue.createTag(-128, true, 1), localDerOutputStream1);
    localDerOutputStream2.write(DerValue.createTag(-128, true, 3), this.encPart.asn1Encode());
    localDerOutputStream1 = new DerOutputStream();
    localDerOutputStream1.write(48, localDerOutputStream2);
    localDerOutputStream2 = new DerOutputStream();
    localDerOutputStream2.write(DerValue.createTag(64, true, 21), localDerOutputStream1);
    return localDerOutputStream2.toByteArray();
  }
}