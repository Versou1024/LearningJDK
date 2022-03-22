package sun.security.krb5.internal;

import java.io.IOException;
import java.math.BigInteger;
import sun.security.krb5.Asn1Exception;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class LastReqEntry
{
  private int lrType;
  private KerberosTime lrValue;

  private LastReqEntry()
  {
  }

  public LastReqEntry(int paramInt, KerberosTime paramKerberosTime)
  {
    this.lrType = paramInt;
    this.lrValue = paramKerberosTime;
  }

  public LastReqEntry(DerValue paramDerValue)
    throws Asn1Exception, IOException
  {
    if (paramDerValue.getTag() != 48)
      throw new Asn1Exception(906);
    DerValue localDerValue = paramDerValue.getData().getDerValue();
    if ((localDerValue.getTag() & 0x1F) == 0)
      this.lrType = localDerValue.getData().getBigInteger().intValue();
    else
      throw new Asn1Exception(906);
    this.lrValue = KerberosTime.parse(paramDerValue.getData(), 1, false);
    if (paramDerValue.getData().available() > 0)
      throw new Asn1Exception(906);
  }

  public byte[] asn1Encode()
    throws Asn1Exception, IOException
  {
    DerOutputStream localDerOutputStream1 = new DerOutputStream();
    DerOutputStream localDerOutputStream2 = new DerOutputStream();
    localDerOutputStream2.putInteger(this.lrType);
    localDerOutputStream1.write(DerValue.createTag(-128, true, 0), localDerOutputStream2);
    localDerOutputStream1.write(DerValue.createTag(-128, true, 1), this.lrValue.asn1Encode());
    localDerOutputStream2 = new DerOutputStream();
    localDerOutputStream2.write(48, localDerOutputStream1);
    return localDerOutputStream2.toByteArray();
  }

  public Object clone()
  {
    LastReqEntry localLastReqEntry = new LastReqEntry();
    localLastReqEntry.lrType = this.lrType;
    localLastReqEntry.lrValue = ((KerberosTime)this.lrValue.clone());
    return localLastReqEntry;
  }
}