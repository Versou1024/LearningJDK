package sun.security.x509;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class CertificateIssuerUniqueIdentity
  implements CertAttrSet
{
  private UniqueIdentity id;
  public static final String IDENT = "x509.info.issuerID";
  public static final String NAME = "issuerID";
  public static final String ID = "id";

  public CertificateIssuerUniqueIdentity(UniqueIdentity paramUniqueIdentity)
  {
    this.id = paramUniqueIdentity;
  }

  public CertificateIssuerUniqueIdentity(DerInputStream paramDerInputStream)
    throws IOException
  {
    this.id = new UniqueIdentity(paramDerInputStream);
  }

  public CertificateIssuerUniqueIdentity(InputStream paramInputStream)
    throws IOException
  {
    DerValue localDerValue = new DerValue(paramInputStream);
    this.id = new UniqueIdentity(localDerValue);
  }

  public CertificateIssuerUniqueIdentity(DerValue paramDerValue)
    throws IOException
  {
    this.id = new UniqueIdentity(paramDerValue);
  }

  public String toString()
  {
    if (this.id == null)
      return "";
    return this.id.toString();
  }

  public void encode(OutputStream paramOutputStream)
    throws IOException
  {
    DerOutputStream localDerOutputStream = new DerOutputStream();
    this.id.encode(localDerOutputStream, DerValue.createTag(-128, false, 1));
    paramOutputStream.write(localDerOutputStream.toByteArray());
  }

  public void set(String paramString, Object paramObject)
    throws IOException
  {
    if (!(paramObject instanceof UniqueIdentity))
      throw new IOException("Attribute must be of type UniqueIdentity.");
    if (paramString.equalsIgnoreCase("id"))
      this.id = ((UniqueIdentity)paramObject);
    else
      throw new IOException("Attribute name not recognized by CertAttrSet: CertificateIssuerUniqueIdentity.");
  }

  public Object get(String paramString)
    throws IOException
  {
    if (paramString.equalsIgnoreCase("id"))
      return this.id;
    throw new IOException("Attribute name not recognized by CertAttrSet: CertificateIssuerUniqueIdentity.");
  }

  public void delete(String paramString)
    throws IOException
  {
    if (paramString.equalsIgnoreCase("id"))
      this.id = null;
    else
      throw new IOException("Attribute name not recognized by CertAttrSet: CertificateIssuerUniqueIdentity.");
  }

  public Enumeration getElements()
  {
    AttributeNameEnumeration localAttributeNameEnumeration = new AttributeNameEnumeration();
    localAttributeNameEnumeration.addElement("id");
    return localAttributeNameEnumeration.elements();
  }

  public String getName()
  {
    return "issuerID";
  }
}