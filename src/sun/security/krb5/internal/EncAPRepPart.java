package sun.security.krb5.internal;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Vector;
import sun.security.krb5.Asn1Exception;
import sun.security.krb5.EncryptionKey;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class EncAPRepPart
{
  public KerberosTime ctime;
  public int cusec;
  EncryptionKey subKey;
  Integer seqNumber;

  public EncAPRepPart(KerberosTime paramKerberosTime, int paramInt, EncryptionKey paramEncryptionKey, Integer paramInteger)
  {
    this.ctime = paramKerberosTime;
    this.cusec = paramInt;
    this.subKey = paramEncryptionKey;
    this.seqNumber = paramInteger;
  }

  public EncAPRepPart(byte[] paramArrayOfByte)
    throws Asn1Exception, IOException
  {
    init(new DerValue(paramArrayOfByte));
  }

  public EncAPRepPart(DerValue paramDerValue)
    throws Asn1Exception, IOException
  {
    init(paramDerValue);
  }

  private void init(DerValue paramDerValue)
    throws Asn1Exception, IOException
  {
    if (((paramDerValue.getTag() & 0x1F) != 27) || (paramDerValue.isApplication() != true) || (paramDerValue.isConstructed() != true))
      throw new Asn1Exception(906);
    DerValue localDerValue1 = paramDerValue.getData().getDerValue();
    if (localDerValue1.getTag() != 48)
      throw new Asn1Exception(906);
    this.ctime = KerberosTime.parse(localDerValue1.getData(), 0, true);
    DerValue localDerValue2 = localDerValue1.getData().getDerValue();
    if ((localDerValue2.getTag() & 0x1F) == 1)
      this.cusec = localDerValue2.getData().getBigInteger().intValue();
    else
      throw new Asn1Exception(906);
    if (localDerValue1.getData().available() > 0)
    {
      this.subKey = EncryptionKey.parse(localDerValue1.getData(), 2, true);
    }
    else
    {
      this.subKey = null;
      this.seqNumber = null;
    }
    if (localDerValue1.getData().available() > 0)
    {
      localDerValue2 = localDerValue1.getData().getDerValue();
      if ((localDerValue2.getTag() & 0x1F) != 3)
        throw new Asn1Exception(906);
      this.seqNumber = new Integer(localDerValue2.getData().getBigInteger().intValue());
    }
    else
    {
      this.seqNumber = null;
    }
    if (localDerValue1.getData().available() > 0)
      throw new Asn1Exception(906);
  }

  public byte[] asn1Encode()
    throws Asn1Exception, IOException
  {
    Vector localVector = new Vector();
    DerOutputStream localDerOutputStream1 = new DerOutputStream();
    localVector.addElement(new DerValue(DerValue.createTag(-128, true, 0), this.ctime.asn1Encode()));
    localDerOutputStream1.putInteger(BigInteger.valueOf(this.cusec));
    localVector.addElement(new DerValue(DerValue.createTag(-128, true, 1), localDerOutputStream1.toByteArray()));
    if (this.subKey != null)
      localVector.addElement(new DerValue(DerValue.createTag(-128, true, 2), this.subKey.asn1Encode()));
    if (this.seqNumber != null)
    {
      localDerOutputStream1 = new DerOutputStream();
      localDerOutputStream1.putInteger(BigInteger.valueOf(this.seqNumber.longValue()));
      localVector.addElement(new DerValue(DerValue.createTag(-128, true, 3), localDerOutputStream1.toByteArray()));
    }
    DerValue[] arrayOfDerValue = new DerValue[localVector.size()];
    localVector.copyInto(arrayOfDerValue);
    localDerOutputStream1 = new DerOutputStream();
    localDerOutputStream1.putSequence(arrayOfDerValue);
    DerOutputStream localDerOutputStream2 = new DerOutputStream();
    localDerOutputStream2.write(DerValue.createTag(64, true, 27), localDerOutputStream1);
    return localDerOutputStream2.toByteArray();
  }

  public final EncryptionKey getSubKey()
  {
    return this.subKey;
  }

  public final Integer getSeqNumber()
  {
    return this.seqNumber;
  }
}