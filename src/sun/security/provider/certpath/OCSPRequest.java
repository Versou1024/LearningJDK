package sun.security.provider.certpath;

import java.io.IOException;
import java.security.cert.CertPathValidatorException;
import sun.security.util.Debug;
import sun.security.util.DerOutputStream;
import sun.security.x509.SerialNumber;
import sun.security.x509.X509CertImpl;

class OCSPRequest
{
  private static final Debug debug = Debug.getInstance("certpath");
  private static final boolean dump = 0;
  private SerialNumber serialNumber;
  private X509CertImpl issuerCert;
  private CertId certId = null;

  OCSPRequest(X509CertImpl paramX509CertImpl1, X509CertImpl paramX509CertImpl2)
    throws CertPathValidatorException
  {
    if (paramX509CertImpl2 == null)
      throw new CertPathValidatorException("Null IssuerCertificate");
    this.issuerCert = paramX509CertImpl2;
    this.serialNumber = paramX509CertImpl1.getSerialNumberObject();
  }

  byte[] encodeBytes()
    throws IOException
  {
    DerOutputStream localDerOutputStream1 = new DerOutputStream();
    DerOutputStream localDerOutputStream2 = new DerOutputStream();
    SingleRequest localSingleRequest = null;
    try
    {
      localSingleRequest = new SingleRequest(this.issuerCert, this.serialNumber, null);
    }
    catch (Exception localException)
    {
      throw new IOException("Error encoding OCSP request");
    }
    this.certId = SingleRequest.access$100(localSingleRequest);
    SingleRequest.access$200(localSingleRequest, localDerOutputStream2);
    localDerOutputStream1.write(48, localDerOutputStream2);
    DerOutputStream localDerOutputStream3 = new DerOutputStream();
    localDerOutputStream3.write(48, localDerOutputStream1);
    DerOutputStream localDerOutputStream4 = new DerOutputStream();
    localDerOutputStream4.write(48, localDerOutputStream3);
    byte[] arrayOfByte = localDerOutputStream4.toByteArray();
    return arrayOfByte;
  }

  CertId getCertId()
  {
    return this.certId;
  }

  private static class SingleRequest
  {
    private CertId certId;

    private SingleRequest(X509CertImpl paramX509CertImpl, SerialNumber paramSerialNumber)
      throws Exception
    {
      this.certId = new CertId(paramX509CertImpl, paramSerialNumber);
    }

    private void encode(DerOutputStream paramDerOutputStream)
      throws IOException
    {
      DerOutputStream localDerOutputStream = new DerOutputStream();
      this.certId.encode(localDerOutputStream);
      paramDerOutputStream.write(48, localDerOutputStream);
    }

    private CertId getCertId()
    {
      return this.certId;
    }
  }
}