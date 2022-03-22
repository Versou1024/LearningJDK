package sun.security.x509;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class CertificatePolicySet
{
  private final Vector<CertificatePolicyId> ids;

  public CertificatePolicySet(Vector<CertificatePolicyId> paramVector)
  {
    this.ids = paramVector;
  }

  public CertificatePolicySet(DerInputStream paramDerInputStream)
    throws IOException
  {
    this.ids = new Vector();
    DerValue[] arrayOfDerValue = paramDerInputStream.getSequence(5);
    for (int i = 0; i < arrayOfDerValue.length; ++i)
    {
      CertificatePolicyId localCertificatePolicyId = new CertificatePolicyId(arrayOfDerValue[i]);
      this.ids.addElement(localCertificatePolicyId);
    }
  }

  public String toString()
  {
    String str = "CertificatePolicySet:[\n" + this.ids.toString() + "]\n";
    return str;
  }

  public void encode(DerOutputStream paramDerOutputStream)
    throws IOException
  {
    DerOutputStream localDerOutputStream = new DerOutputStream();
    for (int i = 0; i < this.ids.size(); ++i)
      ((CertificatePolicyId)this.ids.elementAt(i)).encode(localDerOutputStream);
    paramDerOutputStream.write(48, localDerOutputStream);
  }

  public List<CertificatePolicyId> getCertPolicyIds()
  {
    return Collections.unmodifiableList(this.ids);
  }
}