package sun.security.pkcs;

import java.io.IOException;
import sun.security.util.DerInputStream;
import sun.security.util.DerValue;

public class SigningCertificateInfo
{
  private byte[] ber = null;
  private ESSCertId[] certId = null;

  public SigningCertificateInfo(byte[] paramArrayOfByte)
    throws IOException
  {
    parse(paramArrayOfByte);
  }

  public String toString()
  {
    StringBuffer localStringBuffer = new StringBuffer();
    localStringBuffer.append("[\n");
    for (int i = 0; i < this.certId.length; ++i)
      localStringBuffer.append(this.certId[i].toString());
    localStringBuffer.append("\n]");
    return localStringBuffer.toString();
  }

  public void parse(byte[] paramArrayOfByte)
    throws IOException
  {
    DerValue localDerValue = new DerValue(paramArrayOfByte);
    if (localDerValue.tag != 48)
      throw new IOException("Bad encoding for signingCertificate");
    DerValue[] arrayOfDerValue1 = localDerValue.data.getSequence(1);
    this.certId = new ESSCertId[arrayOfDerValue1.length];
    for (int i = 0; i < arrayOfDerValue1.length; ++i)
      this.certId[i] = new ESSCertId(arrayOfDerValue1[i]);
    if (localDerValue.data.available() > 0)
    {
      DerValue[] arrayOfDerValue2 = localDerValue.data.getSequence(1);
      for (int j = 0; j < arrayOfDerValue2.length; ++j);
    }
  }
}