package sun.security.krb5.internal;

import java.io.IOException;
import java.math.BigInteger;
import sun.security.krb5.Asn1Exception;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class PAEncTSEnc
{
  public KerberosTime pATimeStamp;
  public Integer pAUSec;

  public PAEncTSEnc(KerberosTime paramKerberosTime, Integer paramInteger)
  {
    this.pATimeStamp = paramKerberosTime;
    this.pAUSec = paramInteger;
  }

  public PAEncTSEnc()
  {
    KerberosTime localKerberosTime = new KerberosTime(true);
    this.pATimeStamp = localKerberosTime;
    this.pAUSec = new Integer(localKerberosTime.getMicroSeconds());
  }

  public PAEncTSEnc(DerValue paramDerValue)
    throws Asn1Exception, IOException
  {
    if (paramDerValue.getTag() != 48)
      throw new Asn1Exception(906);
    this.pATimeStamp = KerberosTime.parse(paramDerValue.getData(), 0, false);
    if (paramDerValue.getData().available() > 0)
    {
      DerValue localDerValue = paramDerValue.getData().getDerValue();
      if ((localDerValue.getTag() & 0x1F) == 1)
        this.pAUSec = new Integer(localDerValue.getData().getBigInteger().intValue());
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
    localDerOutputStream1.write(DerValue.createTag(-128, true, 0), this.pATimeStamp.asn1Encode());
    if (this.pAUSec != null)
    {
      localDerOutputStream2 = new DerOutputStream();
      localDerOutputStream2.putInteger(BigInteger.valueOf(this.pAUSec.intValue()));
      localDerOutputStream1.write(DerValue.createTag(-128, true, 1), localDerOutputStream2);
    }
    localDerOutputStream2 = new DerOutputStream();
    localDerOutputStream2.write(48, localDerOutputStream1);
    return localDerOutputStream2.toByteArray();
  }
}