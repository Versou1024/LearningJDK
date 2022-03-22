package sun.security.x509;

import java.io.IOException;
import java.io.OutputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.CertificateParsingException;
import java.util.Date;
import java.util.Enumeration;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class PrivateKeyUsageExtension extends Extension
  implements CertAttrSet
{
  public static final String IDENT = "x509.info.extensions.PrivateKeyUsage";
  public static final String NAME = "PrivateKeyUsage";
  public static final String NOT_BEFORE = "not_before";
  public static final String NOT_AFTER = "not_after";
  private static final byte TAG_BEFORE = 0;
  private static final byte TAG_AFTER = 1;
  private Date notBefore = null;
  private Date notAfter = null;

  private void encodeThis()
    throws IOException
  {
    DerOutputStream localDerOutputStream3;
    if ((this.notBefore == null) && (this.notAfter == null))
    {
      this.extensionValue = null;
      return;
    }
    DerOutputStream localDerOutputStream1 = new DerOutputStream();
    DerOutputStream localDerOutputStream2 = new DerOutputStream();
    if (this.notBefore != null)
    {
      localDerOutputStream3 = new DerOutputStream();
      localDerOutputStream3.putGeneralizedTime(this.notBefore);
      localDerOutputStream2.writeImplicit(DerValue.createTag(-128, false, 0), localDerOutputStream3);
    }
    if (this.notAfter != null)
    {
      localDerOutputStream3 = new DerOutputStream();
      localDerOutputStream3.putGeneralizedTime(this.notAfter);
      localDerOutputStream2.writeImplicit(DerValue.createTag(-128, false, 1), localDerOutputStream3);
    }
    localDerOutputStream1.write(48, localDerOutputStream2);
    this.extensionValue = localDerOutputStream1.toByteArray();
  }

  public PrivateKeyUsageExtension(Date paramDate1, Date paramDate2)
    throws IOException
  {
    this.notBefore = paramDate1;
    this.notAfter = paramDate2;
    this.extensionId = PKIXExtensions.PrivateKeyUsage_Id;
    this.critical = false;
    encodeThis();
  }

  public PrivateKeyUsageExtension(Boolean paramBoolean, Object paramObject)
    throws CertificateException, IOException
  {
    this.extensionId = PKIXExtensions.PrivateKeyUsage_Id;
    this.critical = paramBoolean.booleanValue();
    this.extensionValue = ((byte[])(byte[])paramObject);
    DerInputStream localDerInputStream = new DerInputStream(this.extensionValue);
    DerValue[] arrayOfDerValue = localDerInputStream.getSequence(2);
    for (int i = 0; i < arrayOfDerValue.length; ++i)
    {
      DerValue localDerValue = arrayOfDerValue[i];
      if ((localDerValue.isContextSpecific(0)) && (!(localDerValue.isConstructed())))
      {
        if (this.notBefore != null)
          throw new CertificateParsingException("Duplicate notBefore in PrivateKeyUsage.");
        localDerValue.resetTag(24);
        localDerInputStream = new DerInputStream(localDerValue.toByteArray());
        this.notBefore = localDerInputStream.getGeneralizedTime();
      }
      else if ((localDerValue.isContextSpecific(1)) && (!(localDerValue.isConstructed())))
      {
        if (this.notAfter != null)
          throw new CertificateParsingException("Duplicate notAfter in PrivateKeyUsage.");
        localDerValue.resetTag(24);
        localDerInputStream = new DerInputStream(localDerValue.toByteArray());
        this.notAfter = localDerInputStream.getGeneralizedTime();
      }
      else
      {
        throw new IOException("Invalid encoding of PrivateKeyUsageExtension");
      }
    }
  }

  public String toString()
  {
    return super.toString() + "PrivateKeyUsage: [\n" + ((this.notBefore == null) ? "" : new StringBuilder().append("From: ").append(this.notBefore.toString()).append(", ").toString()) + ((this.notAfter == null) ? "" : new StringBuilder().append("To: ").append(this.notAfter.toString()).toString()) + "]\n";
  }

  public void valid()
    throws CertificateNotYetValidException, CertificateExpiredException
  {
    Date localDate = new Date();
    valid(localDate);
  }

  public void valid(Date paramDate)
    throws CertificateNotYetValidException, CertificateExpiredException
  {
    if (this.notBefore.after(paramDate))
      throw new CertificateNotYetValidException("NotBefore: " + this.notBefore.toString());
    if (this.notAfter.before(paramDate))
      throw new CertificateExpiredException("NotAfter: " + this.notAfter.toString());
  }

  public void encode(OutputStream paramOutputStream)
    throws IOException
  {
    DerOutputStream localDerOutputStream = new DerOutputStream();
    if (this.extensionValue == null)
    {
      this.extensionId = PKIXExtensions.PrivateKeyUsage_Id;
      this.critical = false;
      encodeThis();
    }
    super.encode(localDerOutputStream);
    paramOutputStream.write(localDerOutputStream.toByteArray());
  }

  public void set(String paramString, Object paramObject)
    throws CertificateException, IOException
  {
    if (!(paramObject instanceof Date))
      throw new CertificateException("Attribute must be of type Date.");
    if (paramString.equalsIgnoreCase("not_before"))
      this.notBefore = ((Date)paramObject);
    else if (paramString.equalsIgnoreCase("not_after"))
      this.notAfter = ((Date)paramObject);
    else
      throw new CertificateException("Attribute name not recognized by CertAttrSet:PrivateKeyUsage.");
    encodeThis();
  }

  public Object get(String paramString)
    throws CertificateException
  {
    if (paramString.equalsIgnoreCase("not_before"))
      return new Date(this.notBefore.getTime());
    if (paramString.equalsIgnoreCase("not_after"))
      return new Date(this.notAfter.getTime());
    throw new CertificateException("Attribute name not recognized by CertAttrSet:PrivateKeyUsage.");
  }

  public void delete(String paramString)
    throws CertificateException, IOException
  {
    if (paramString.equalsIgnoreCase("not_before"))
      this.notBefore = null;
    else if (paramString.equalsIgnoreCase("not_after"))
      this.notAfter = null;
    else
      throw new CertificateException("Attribute name not recognized by CertAttrSet:PrivateKeyUsage.");
    encodeThis();
  }

  public Enumeration getElements()
  {
    AttributeNameEnumeration localAttributeNameEnumeration = new AttributeNameEnumeration();
    localAttributeNameEnumeration.addElement("not_before");
    localAttributeNameEnumeration.addElement("not_after");
    return localAttributeNameEnumeration.elements();
  }

  public String getName()
  {
    return "PrivateKeyUsage";
  }
}