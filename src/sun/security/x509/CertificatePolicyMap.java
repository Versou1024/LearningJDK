package sun.security.x509;

import java.io.IOException;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class CertificatePolicyMap
{
  private CertificatePolicyId issuerDomain;
  private CertificatePolicyId subjectDomain;

  public CertificatePolicyMap(CertificatePolicyId paramCertificatePolicyId1, CertificatePolicyId paramCertificatePolicyId2)
  {
    this.issuerDomain = paramCertificatePolicyId1;
    this.subjectDomain = paramCertificatePolicyId2;
  }

  public CertificatePolicyMap(DerValue paramDerValue)
    throws IOException
  {
    if (paramDerValue.tag != 48)
      throw new IOException("Invalid encoding for CertificatePolicyMap");
    this.issuerDomain = new CertificatePolicyId(paramDerValue.data.getDerValue());
    this.subjectDomain = new CertificatePolicyId(paramDerValue.data.getDerValue());
  }

  public CertificatePolicyId getIssuerIdentifier()
  {
    return this.issuerDomain;
  }

  public CertificatePolicyId getSubjectIdentifier()
  {
    return this.subjectDomain;
  }

  public String toString()
  {
    String str = "CertificatePolicyMap: [\nIssuerDomain:" + this.issuerDomain.toString() + "SubjectDomain:" + this.subjectDomain.toString() + "]\n";
    return str;
  }

  public void encode(DerOutputStream paramDerOutputStream)
    throws IOException
  {
    DerOutputStream localDerOutputStream = new DerOutputStream();
    this.issuerDomain.encode(localDerOutputStream);
    this.subjectDomain.encode(localDerOutputStream);
    paramDerOutputStream.write(48, localDerOutputStream);
  }
}