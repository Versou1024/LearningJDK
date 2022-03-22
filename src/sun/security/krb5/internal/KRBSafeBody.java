package sun.security.krb5.internal;

import B;
import java.io.IOException;
import java.math.BigInteger;
import sun.security.krb5.Asn1Exception;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class KRBSafeBody
{
  public byte[] userData = null;
  public KerberosTime timestamp;
  public Integer usec;
  public Integer seqNumber;
  public HostAddress sAddress;
  public HostAddress rAddress;

  public KRBSafeBody(byte[] paramArrayOfByte, KerberosTime paramKerberosTime, Integer paramInteger1, Integer paramInteger2, HostAddress paramHostAddress1, HostAddress paramHostAddress2)
  {
    if (paramArrayOfByte != null)
      this.userData = ((byte[])(byte[])paramArrayOfByte.clone());
    this.timestamp = paramKerberosTime;
    this.usec = paramInteger1;
    this.seqNumber = paramInteger2;
    this.sAddress = paramHostAddress1;
    this.rAddress = paramHostAddress2;
  }

  public KRBSafeBody(DerValue paramDerValue)
    throws Asn1Exception, IOException
  {
    if (paramDerValue.getTag() != 48)
      throw new Asn1Exception(906);
    DerValue localDerValue = paramDerValue.getData().getDerValue();
    if ((localDerValue.getTag() & 0x1F) == 0)
      this.userData = localDerValue.getData().getOctetString();
    else
      throw new Asn1Exception(906);
    this.timestamp = KerberosTime.parse(paramDerValue.getData(), 1, true);
    if ((paramDerValue.getData().peekByte() & 0x1F) == 2)
    {
      localDerValue = paramDerValue.getData().getDerValue();
      this.usec = new Integer(localDerValue.getData().getBigInteger().intValue());
    }
    if ((paramDerValue.getData().peekByte() & 0x1F) == 3)
    {
      localDerValue = paramDerValue.getData().getDerValue();
      this.seqNumber = new Integer(localDerValue.getData().getBigInteger().intValue());
    }
    this.sAddress = HostAddress.parse(paramDerValue.getData(), 4, false);
    if (paramDerValue.getData().available() > 0)
      this.rAddress = HostAddress.parse(paramDerValue.getData(), 5, true);
    if (paramDerValue.getData().available() > 0)
      throw new Asn1Exception(906);
  }

  public byte[] asn1Encode()
    throws Asn1Exception, IOException
  {
    DerOutputStream localDerOutputStream1 = new DerOutputStream();
    DerOutputStream localDerOutputStream2 = new DerOutputStream();
    localDerOutputStream2.putOctetString(this.userData);
    localDerOutputStream1.write(DerValue.createTag(-128, true, 0), localDerOutputStream2);
    if (this.timestamp != null)
      localDerOutputStream1.write(DerValue.createTag(-128, true, 1), this.timestamp.asn1Encode());
    if (this.usec != null)
    {
      localDerOutputStream2 = new DerOutputStream();
      localDerOutputStream2.putInteger(BigInteger.valueOf(this.usec.intValue()));
      localDerOutputStream1.write(DerValue.createTag(-128, true, 2), localDerOutputStream2);
    }
    if (this.seqNumber != null)
    {
      localDerOutputStream2 = new DerOutputStream();
      localDerOutputStream2.putInteger(BigInteger.valueOf(this.seqNumber.longValue()));
      localDerOutputStream1.write(DerValue.createTag(-128, true, 3), localDerOutputStream2);
    }
    localDerOutputStream1.write(DerValue.createTag(-128, true, 4), this.sAddress.asn1Encode());
    if (this.rAddress != null)
      localDerOutputStream2 = new DerOutputStream();
    localDerOutputStream2.write(48, localDerOutputStream1);
    return localDerOutputStream2.toByteArray();
  }

  public static KRBSafeBody parse(DerInputStream paramDerInputStream, byte paramByte, boolean paramBoolean)
    throws Asn1Exception, IOException
  {
    if ((paramBoolean) && (((byte)paramDerInputStream.peekByte() & 0x1F) != paramByte))
      return null;
    DerValue localDerValue1 = paramDerInputStream.getDerValue();
    if (paramByte != (localDerValue1.getTag() & 0x1F))
      throw new Asn1Exception(906);
    DerValue localDerValue2 = localDerValue1.getData().getDerValue();
    return new KRBSafeBody(localDerValue2);
  }
}