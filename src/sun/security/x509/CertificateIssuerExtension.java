package sun.security.x509;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class CertificateIssuerExtension extends Extension
  implements CertAttrSet
{
  public static final String NAME = "CertificateIssuer";
  public static final String ISSUER = "issuer";
  private GeneralNames names;

  private void encodeThis()
    throws IOException
  {
    if ((this.names == null) || (this.names.isEmpty()))
    {
      this.extensionValue = null;
      return;
    }
    DerOutputStream localDerOutputStream = new DerOutputStream();
    this.names.encode(localDerOutputStream);
    this.extensionValue = localDerOutputStream.toByteArray();
  }

  public CertificateIssuerExtension(GeneralNames paramGeneralNames)
    throws IOException
  {
    this.extensionId = PKIXExtensions.CertificateIssuer_Id;
    this.critical = true;
    this.names = paramGeneralNames;
    encodeThis();
  }

  public CertificateIssuerExtension(Boolean paramBoolean, Object paramObject)
    throws IOException
  {
    this.extensionId = PKIXExtensions.CertificateIssuer_Id;
    this.critical = paramBoolean.booleanValue();
    this.extensionValue = ((byte[])(byte[])paramObject);
    DerValue localDerValue = new DerValue(this.extensionValue);
    this.names = new GeneralNames(localDerValue);
  }

  public void set(String paramString, Object paramObject)
    throws IOException
  {
    if (paramString.equalsIgnoreCase("issuer"))
    {
      if (!(paramObject instanceof GeneralNames))
        throw new IOException("Attribute value must be of type GeneralNames");
      this.names = ((GeneralNames)paramObject);
    }
    else
    {
      throw new IOException("Attribute name not recognized by CertAttrSet:CertificateIssuer");
    }
    encodeThis();
  }

  public Object get(String paramString)
    throws IOException
  {
    if (paramString.equalsIgnoreCase("issuer"))
      return this.names;
    throw new IOException("Attribute name not recognized by CertAttrSet:CertificateIssuer");
  }

  public void delete(String paramString)
    throws IOException
  {
    if (paramString.equalsIgnoreCase("issuer"))
      this.names = null;
    else
      throw new IOException("Attribute name not recognized by CertAttrSet:CertificateIssuer");
    encodeThis();
  }

  public String toString()
  {
    return super.toString() + "Certificate Issuer [\n" + String.valueOf(this.names) + "]\n";
  }

  public void encode(OutputStream paramOutputStream)
    throws IOException
  {
    DerOutputStream localDerOutputStream = new DerOutputStream();
    if (this.extensionValue == null)
    {
      this.extensionId = PKIXExtensions.CertificateIssuer_Id;
      this.critical = true;
      encodeThis();
    }
    super.encode(localDerOutputStream);
    paramOutputStream.write(localDerOutputStream.toByteArray());
  }

  public Enumeration getElements()
  {
    AttributeNameEnumeration localAttributeNameEnumeration = new AttributeNameEnumeration();
    localAttributeNameEnumeration.addElement("issuer");
    return localAttributeNameEnumeration.elements();
  }

  public String getName()
  {
    return "CertificateIssuer";
  }
}