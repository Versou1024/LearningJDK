package sun.security.krb5.internal;

import java.io.IOException;
import java.math.BigInteger;
import sun.security.krb5.Asn1Exception;
import sun.security.krb5.EncryptedData;
import sun.security.krb5.RealmException;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class APReq
{
  public int pvno;
  public int msgType;
  public APOptions apOptions;
  public Ticket ticket;
  public EncryptedData authenticator;

  public APReq(APOptions paramAPOptions, Ticket paramTicket, EncryptedData paramEncryptedData)
  {
    this.pvno = 5;
    this.msgType = 14;
    this.apOptions = paramAPOptions;
    this.ticket = paramTicket;
    this.authenticator = paramEncryptedData;
  }

  public APReq(byte[] paramArrayOfByte)
    throws Asn1Exception, IOException, sun.security.krb5.internal.KrbApErrException, RealmException
  {
    init(new DerValue(paramArrayOfByte));
  }

  public APReq(DerValue paramDerValue)
    throws Asn1Exception, IOException, sun.security.krb5.internal.KrbApErrException, RealmException
  {
    init(paramDerValue);
  }

  private void init(DerValue paramDerValue)
    throws Asn1Exception, IOException, sun.security.krb5.internal.KrbApErrException, RealmException
  {
    if (((paramDerValue.getTag() & 0x1F) != 14) || (paramDerValue.isApplication() != true) || (paramDerValue.isConstructed() != true))
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
    if (this.msgType != 14)
      throw new sun.security.krb5.internal.KrbApErrException(40);
    this.apOptions = APOptions.parse(localDerValue1.getData(), 2, false);
    this.ticket = Ticket.parse(localDerValue1.getData(), 3, false);
    this.authenticator = EncryptedData.parse(localDerValue1.getData(), 4, false);
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
    localDerOutputStream1.write(DerValue.createTag(-128, true, 2), this.apOptions.asn1Encode());
    localDerOutputStream1.write(DerValue.createTag(-128, true, 3), this.ticket.asn1Encode());
    localDerOutputStream1.write(DerValue.createTag(-128, true, 4), this.authenticator.asn1Encode());
    localDerOutputStream2 = new DerOutputStream();
    localDerOutputStream2.write(48, localDerOutputStream1);
    DerOutputStream localDerOutputStream3 = new DerOutputStream();
    localDerOutputStream3.write(DerValue.createTag(64, true, 14), localDerOutputStream2);
    return localDerOutputStream3.toByteArray();
  }
}