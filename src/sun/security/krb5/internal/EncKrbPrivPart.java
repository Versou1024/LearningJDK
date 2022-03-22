package sun.security.krb5.internal;

import B;
import java.io.IOException;
import java.math.BigInteger;
import sun.security.krb5.Asn1Exception;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class EncKrbPrivPart
{
  public byte[] userData = null;
  public KerberosTime timestamp;
  public Integer usec;
  public Integer seqNumber;
  public HostAddress sAddress;
  public HostAddress rAddress;

  public EncKrbPrivPart(byte[] paramArrayOfByte, KerberosTime paramKerberosTime, Integer paramInteger1, Integer paramInteger2, HostAddress paramHostAddress1, HostAddress paramHostAddress2)
  {
    if (paramArrayOfByte != null)
      this.userData = ((byte[])(byte[])paramArrayOfByte.clone());
    this.timestamp = paramKerberosTime;
    this.usec = paramInteger1;
    this.seqNumber = paramInteger2;
    this.sAddress = paramHostAddress1;
    this.rAddress = paramHostAddress2;
  }

  public EncKrbPrivPart(byte[] paramArrayOfByte)
    throws Asn1Exception, IOException
  {
    init(new DerValue(paramArrayOfByte));
  }

  public EncKrbPrivPart(DerValue paramDerValue)
    throws Asn1Exception, IOException
  {
    init(paramDerValue);
  }

  private void init(DerValue paramDerValue)
    throws Asn1Exception, IOException
  {
    if (((paramDerValue.getTag() & 0x1F) != 28) || (paramDerValue.isApplication() != true) || (paramDerValue.isConstructed() != true))
      throw new Asn1Exception(906);
    DerValue localDerValue1 = paramDerValue.getData().getDerValue();
    if (localDerValue1.getTag() != 48)
      throw new Asn1Exception(906);
    DerValue localDerValue2 = localDerValue1.getData().getDerValue();
    if ((localDerValue2.getTag() & 0x1F) == 0)
      this.userData = localDerValue2.getData().getOctetString();
    else
      throw new Asn1Exception(906);
    this.timestamp = KerberosTime.parse(localDerValue1.getData(), 1, true);
    if ((localDerValue1.getData().peekByte() & 0x1F) == 2)
    {
      localDerValue2 = localDerValue1.getData().getDerValue();
      this.usec = new Integer(localDerValue2.getData().getBigInteger().intValue());
    }
    else
    {
      this.usec = null;
    }
    if ((localDerValue1.getData().peekByte() & 0x1F) == 3)
    {
      localDerValue2 = localDerValue1.getData().getDerValue();
      this.seqNumber = new Integer(localDerValue2.getData().getBigInteger().intValue());
    }
    else
    {
      this.seqNumber = null;
    }
    this.sAddress = HostAddress.parse(localDerValue1.getData(), 4, false);
    if (localDerValue1.getData().available() > 0)
      this.rAddress = HostAddress.parse(localDerValue1.getData(), 5, true);
    if (localDerValue1.getData().available() > 0)
      throw new Asn1Exception(906);
  }

  public byte[] asn1Encode()
    throws Asn1Exception, IOException
  {
    DerOutputStream localDerOutputStream1 = new DerOutputStream();
    DerOutputStream localDerOutputStream2 = new DerOutputStream();
    localDerOutputStream1.putOctetString(this.userData);
    localDerOutputStream2.write(DerValue.createTag(-128, true, 0), localDerOutputStream1);
    if (this.timestamp != null)
      localDerOutputStream2.write(DerValue.createTag(-128, true, 1), this.timestamp.asn1Encode());
    if (this.usec != null)
    {
      localDerOutputStream1 = new DerOutputStream();
      localDerOutputStream1.putInteger(BigInteger.valueOf(this.usec.intValue()));
      localDerOutputStream2.write(DerValue.createTag(-128, true, 2), localDerOutputStream1);
    }
    if (this.seqNumber != null)
    {
      localDerOutputStream1 = new DerOutputStream();
      localDerOutputStream1.putInteger(BigInteger.valueOf(this.seqNumber.longValue()));
      localDerOutputStream2.write(DerValue.createTag(-128, true, 3), localDerOutputStream1);
    }
    localDerOutputStream2.write(DerValue.createTag(-128, true, 4), this.sAddress.asn1Encode());
    if (this.rAddress != null)
      localDerOutputStream2.write(DerValue.createTag(-128, true, 5), this.rAddress.asn1Encode());
    localDerOutputStream1 = new DerOutputStream();
    localDerOutputStream1.write(48, localDerOutputStream2);
    localDerOutputStream2 = new DerOutputStream();
    localDerOutputStream2.write(DerValue.createTag(64, true, 28), localDerOutputStream1);
    return localDerOutputStream2.toByteArray();
  }
}