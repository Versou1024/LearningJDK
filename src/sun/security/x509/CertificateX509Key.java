package sun.security.x509;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.PublicKey;
import java.util.Enumeration;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class CertificateX509Key
  implements CertAttrSet
{
  public static final String IDENT = "x509.info.key";
  public static final String NAME = "key";
  public static final String KEY = "value";
  private PublicKey key;

  public CertificateX509Key(PublicKey paramPublicKey)
  {
    this.key = paramPublicKey;
  }

  public CertificateX509Key(DerInputStream paramDerInputStream)
    throws IOException
  {
    DerValue localDerValue = paramDerInputStream.getDerValue();
    this.key = X509Key.parse(localDerValue);
  }

  public CertificateX509Key(InputStream paramInputStream)
    throws IOException
  {
    DerValue localDerValue = new DerValue(paramInputStream);
    this.key = X509Key.parse(localDerValue);
  }

  public String toString()
  {
    if (this.key == null)
      return "";
    return this.key.toString();
  }

  public void encode(OutputStream paramOutputStream)
    throws IOException
  {
    DerOutputStream localDerOutputStream = new DerOutputStream();
    localDerOutputStream.write(this.key.getEncoded());
    paramOutputStream.write(localDerOutputStream.toByteArray());
  }

  public void set(String paramString, Object paramObject)
    throws IOException
  {
    if (paramString.equalsIgnoreCase("value"))
      this.key = ((PublicKey)paramObject);
    else
      throw new IOException("Attribute name not recognized by CertAttrSet: CertificateX509Key.");
  }

  public Object get(String paramString)
    throws IOException
  {
    if (paramString.equalsIgnoreCase("value"))
      return this.key;
    throw new IOException("Attribute name not recognized by CertAttrSet: CertificateX509Key.");
  }

  public void delete(String paramString)
    throws IOException
  {
    if (paramString.equalsIgnoreCase("value"))
      this.key = null;
    else
      throw new IOException("Attribute name not recognized by CertAttrSet: CertificateX509Key.");
  }

  public Enumeration getElements()
  {
    AttributeNameEnumeration localAttributeNameEnumeration = new AttributeNameEnumeration();
    localAttributeNameEnumeration.addElement("value");
    return localAttributeNameEnumeration.elements();
  }

  public String getName()
  {
    return "key";
  }
}