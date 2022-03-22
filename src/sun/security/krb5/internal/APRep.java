package sun.security.krb5.internal;

import java.io.IOException;
import java.math.BigInteger;
import sun.security.krb5.Asn1Exception;
import sun.security.krb5.EncryptedData;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class APRep
{
  public int pvno;
  public int msgType;
  public EncryptedData encPart;

  public APRep(EncryptedData paramEncryptedData)
  {
    this.pvno = 5;
    this.msgType = 15;
    this.encPart = paramEncryptedData;
  }

  public APRep(byte[] paramArrayOfByte)
    throws Asn1Exception, sun.security.krb5.internal.KrbApErrException, IOException
  {
    init(new DerValue(paramArrayOfByte));
  }

  public APRep(DerValue paramDerValue)
    throws Asn1Exception, sun.security.krb5.internal.KrbApErrException, IOException
  {
    init(paramDerValue);
  }

  private void init(DerValue paramDerValue)
    throws Asn1Exception, sun.security.krb5.internal.KrbApErrException, IOException
  {
    if (((paramDerValue.getTag() & 0x1F) != 15) || (paramDerValue.isApplication() != true) || (paramDerValue.isConstructed() != true))
      throw new Asn1Exception(906);
    DerValue localDerValue1 = paramDerValue.getData().getDerValue();
    if (localDerValue1.getTag() != 48)
      throw new Asn1Exception(906);
    DerValue localDerValue2 = localDerValue1.getData().getDerValue();
    if ((localDerValue2.getTag() & 0x1F) != 0)
      throw new Asn1Exception(906);
    this.pvno = localDerValue2.getData().getBigInteger().intValue();
    if (this.pvno != 5)
      throw new sun.security.krb5.internal.KrbApErrException(39);
    localDerValue2 = localDerValue1.getData().getDerValue();
    if ((localDerValue2.getTag() & 0x1F) != 1)
      throw new Asn1Exception(906);
    this.msgType = localDerValue2.getData().getBigInteger().intValue();
    if (this.msgType != 15)
      throw new sun.security.krb5.internal.KrbApErrException(40);
    this.encPart = EncryptedData.parse(localDerValue1.getData(), 2, false);
    if (localDerValue1.getData().available() > 0)
      throw new Asn1Exception(906);
  }

  public byte[] asn1Encode()
    throws Asn1Exception, IOException
  {
    DerOutputStream localDerOutputStream1 = new DerOutputStream();
    DerOutputStream localDerOutputStream2 = new DerOutputStream();
    localDerOutputStream2.putInteger(BigInteger.valueOf(this.pvno));
    localDerOutputStream1.write(DerValue.createTag(-128, true, 0), localDerOutputStream2);
    localDerOutputStream2 = new DerOutputStream();
    localDerOutputStream2.putInteger(BigInteger.valueOf(this.msgType));
    localDerOutputStream1.write(DerValue.createTag(-128, true, 1), localDerOutputStream2);
    localDerOutputStream1.write(DerValue.createTag(-128, true, 2), this.encPart.asn1Encode());
    localDerOutputStream2 = new DerOutputStream();
    localDerOutputStream2.write(48, localDerOutputStream1);
    DerOutputStream localDerOutputStream3 = new DerOutputStream();
    localDerOutputStream3.write(DerValue.createTag(64, true, 15), localDerOutputStream2);
    return localDerOutputStream3.toByteArray();
  }
}