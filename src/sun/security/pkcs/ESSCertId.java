package sun.security.pkcs;

import java.io.IOException;
import sun.misc.HexDumpEncoder;
import sun.security.util.DerInputStream;
import sun.security.util.DerValue;
import sun.security.x509.GeneralNames;
import sun.security.x509.SerialNumber;

class ESSCertId
{
  private static volatile HexDumpEncoder hexDumper;
  private byte[] certHash;
  private GeneralNames issuer;
  private SerialNumber serialNumber;

  ESSCertId(DerValue paramDerValue)
    throws IOException
  {
    this.certHash = paramDerValue.data.getDerValue().toByteArray();
    if (paramDerValue.data.available() > 0)
    {
      DerValue localDerValue = paramDerValue.data.getDerValue();
      this.issuer = new GeneralNames(localDerValue.data.getDerValue());
      this.serialNumber = new SerialNumber(localDerValue.data.getDerValue());
    }
  }

  public String toString()
  {
    StringBuffer localStringBuffer = new StringBuffer();
    localStringBuffer.append("[\n\tCertificate hash (SHA-1):\n");
    if (hexDumper == null)
      hexDumper = new HexDumpEncoder();
    localStringBuffer.append(hexDumper.encode(this.certHash));
    if ((this.issuer != null) && (this.serialNumber != null))
    {
      localStringBuffer.append("\n\tIssuer: " + this.issuer + "\n");
      localStringBuffer.append("\t" + this.serialNumber);
    }
    localStringBuffer.append("\n]");
    return localStringBuffer.toString();
  }
}