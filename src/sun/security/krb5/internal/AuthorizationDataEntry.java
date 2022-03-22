package sun.security.krb5.internal;

import java.io.IOException;
import java.math.BigInteger;
import sun.security.krb5.Asn1Exception;
import sun.security.krb5.internal.ccache.CCacheOutputStream;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class AuthorizationDataEntry
  implements Cloneable
{
  public int adType;
  public byte[] adData;

  private AuthorizationDataEntry()
  {
  }

  public AuthorizationDataEntry(int paramInt, byte[] paramArrayOfByte)
  {
    this.adType = paramInt;
    this.adData = paramArrayOfByte;
  }

  public Object clone()
  {
    AuthorizationDataEntry localAuthorizationDataEntry = new AuthorizationDataEntry();
    localAuthorizationDataEntry.adType = this.adType;
    if (this.adData != null)
    {
      localAuthorizationDataEntry.adData = new byte[this.adData.length];
      System.arraycopy(this.adData, 0, localAuthorizationDataEntry.adData, 0, this.adData.length);
    }
    return localAuthorizationDataEntry;
  }

  public AuthorizationDataEntry(DerValue paramDerValue)
    throws Asn1Exception, IOException
  {
    if (paramDerValue.getTag() != 48)
      throw new Asn1Exception(906);
    DerValue localDerValue = paramDerValue.getData().getDerValue();
    if ((localDerValue.getTag() & 0x1F) == 0)
      this.adType = localDerValue.getData().getBigInteger().intValue();
    else
      throw new Asn1Exception(906);
    localDerValue = paramDerValue.getData().getDerValue();
    if ((localDerValue.getTag() & 0x1F) == 1)
      this.adData = localDerValue.getData().getOctetString();
    else
      throw new Asn1Exception(906);
    if (paramDerValue.getData().available() > 0)
      throw new Asn1Exception(906);
  }

  public byte[] asn1Encode()
    throws Asn1Exception, IOException
  {
    DerOutputStream localDerOutputStream1 = new DerOutputStream();
    DerOutputStream localDerOutputStream2 = new DerOutputStream();
    localDerOutputStream2.putInteger(this.adType);
    localDerOutputStream1.write(DerValue.createTag(-128, true, 0), localDerOutputStream2);
    localDerOutputStream2 = new DerOutputStream();
    localDerOutputStream2.putOctetString(this.adData);
    localDerOutputStream1.write(DerValue.createTag(-128, true, 1), localDerOutputStream2);
    localDerOutputStream2 = new DerOutputStream();
    localDerOutputStream2.write(48, localDerOutputStream1);
    return localDerOutputStream2.toByteArray();
  }

  public void writeEntry(CCacheOutputStream paramCCacheOutputStream)
    throws IOException
  {
    paramCCacheOutputStream.write16(this.adType);
    paramCCacheOutputStream.write32(this.adData.length);
    paramCCacheOutputStream.write(this.adData, 0, this.adData.length);
  }

  public String toString()
  {
    return "adType=" + this.adType + " adData.length=" + this.adData.length;
  }
}