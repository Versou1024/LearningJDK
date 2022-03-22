package sun.security.x509;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;

public class CertificateAlgorithmId
  implements CertAttrSet
{
  private AlgorithmId algId;
  public static final String IDENT = "x509.info.algorithmID";
  public static final String NAME = "algorithmID";
  public static final String ALGORITHM = "algorithm";

  public CertificateAlgorithmId(AlgorithmId paramAlgorithmId)
  {
    this.algId = paramAlgorithmId;
  }

  public CertificateAlgorithmId(DerInputStream paramDerInputStream)
    throws IOException
  {
    DerValue localDerValue = paramDerInputStream.getDerValue();
    this.algId = AlgorithmId.parse(localDerValue);
  }

  public CertificateAlgorithmId(InputStream paramInputStream)
    throws IOException
  {
    DerValue localDerValue = new DerValue(paramInputStream);
    this.algId = AlgorithmId.parse(localDerValue);
  }

  public String toString()
  {
    if (this.algId == null)
      return "";
    return this.algId.toString() + ", OID = " + this.algId.getOID().toString() + "\n";
  }

  public void encode(OutputStream paramOutputStream)
    throws IOException
  {
    DerOutputStream localDerOutputStream = new DerOutputStream();
    this.algId.encode(localDerOutputStream);
    paramOutputStream.write(localDerOutputStream.toByteArray());
  }

  public void set(String paramString, Object paramObject)
    throws IOException
  {
    if (!(paramObject instanceof AlgorithmId))
      throw new IOException("Attribute must be of type AlgorithmId.");
    if (paramString.equalsIgnoreCase("algorithm"))
      this.algId = ((AlgorithmId)paramObject);
    else
      throw new IOException("Attribute name not recognized by CertAttrSet:CertificateAlgorithmId.");
  }

  public Object get(String paramString)
    throws IOException
  {
    if (paramString.equalsIgnoreCase("algorithm"))
      return this.algId;
    throw new IOException("Attribute name not recognized by CertAttrSet:CertificateAlgorithmId.");
  }

  public void delete(String paramString)
    throws IOException
  {
    if (paramString.equalsIgnoreCase("algorithm"))
      this.algId = null;
    else
      throw new IOException("Attribute name not recognized by CertAttrSet:CertificateAlgorithmId.");
  }

  public Enumeration getElements()
  {
    AttributeNameEnumeration localAttributeNameEnumeration = new AttributeNameEnumeration();
    localAttributeNameEnumeration.addElement("algorithm");
    return localAttributeNameEnumeration.elements();
  }

  public String getName()
  {
    return "algorithmID";
  }
}