package sun.security.x509;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class IssuingDistributionPointExtension extends Extension
  implements CertAttrSet
{
  public static final String IDENT = "x509.info.extensions.IssuingDistributionPoint";
  public static final String NAME = "IssuingDistributionPoint";
  public static final String POINT = "point";
  public static final String REASONS = "reasons";
  public static final String ONLY_USER_CERTS = "only_user_certs";
  public static final String ONLY_CA_CERTS = "only_ca_certs";
  public static final String ONLY_ATTRIBUTE_CERTS = "only_attribute_certs";
  public static final String INDIRECT_CRL = "indirect_crl";
  private DistributionPointName distributionPoint = null;
  private ReasonFlags revocationReasons = null;
  private boolean hasOnlyUserCerts = false;
  private boolean hasOnlyCACerts = false;
  private boolean hasOnlyAttributeCerts = false;
  private boolean isIndirectCRL = false;
  private static final byte TAG_DISTRIBUTION_POINT = 0;
  private static final byte TAG_ONLY_USER_CERTS = 1;
  private static final byte TAG_ONLY_CA_CERTS = 2;
  private static final byte TAG_ONLY_SOME_REASONS = 3;
  private static final byte TAG_INDIRECT_CRL = 4;
  private static final byte TAG_ONLY_ATTRIBUTE_CERTS = 5;

  public IssuingDistributionPointExtension(DistributionPointName paramDistributionPointName, ReasonFlags paramReasonFlags, boolean paramBoolean1, boolean paramBoolean2, boolean paramBoolean3, boolean paramBoolean4)
    throws IOException
  {
    if (((paramBoolean1) && (((paramBoolean2) || (paramBoolean3)))) || ((paramBoolean2) && (((paramBoolean1) || (paramBoolean3)))) || ((paramBoolean3) && (((paramBoolean1) || (paramBoolean2)))))
      throw new IllegalArgumentException("Only one of hasOnlyUserCerts, hasOnlyCACerts, hasOnlyAttributeCerts may be set to true");
    this.extensionId = PKIXExtensions.IssuingDistributionPoint_Id;
    this.critical = true;
    this.distributionPoint = paramDistributionPointName;
    this.revocationReasons = paramReasonFlags;
    this.hasOnlyUserCerts = paramBoolean1;
    this.hasOnlyCACerts = paramBoolean2;
    this.hasOnlyAttributeCerts = paramBoolean3;
    this.isIndirectCRL = paramBoolean4;
    encodeThis();
  }

  public IssuingDistributionPointExtension(Boolean paramBoolean, Object paramObject)
    throws IOException
  {
    this.extensionId = PKIXExtensions.IssuingDistributionPoint_Id;
    this.critical = paramBoolean.booleanValue();
    if (!(paramObject instanceof byte[]))
      throw new IOException("Illegal argument type");
    this.extensionValue = ((byte[])(byte[])paramObject);
    DerValue localDerValue1 = new DerValue(this.extensionValue);
    if (localDerValue1.tag != 48)
      throw new IOException("Invalid encoding for IssuingDistributionPointExtension.");
    if ((localDerValue1.data == null) || (localDerValue1.data.available() == 0))
      return;
    DerInputStream localDerInputStream = localDerValue1.data;
    while ((localDerInputStream != null) && (localDerInputStream.available() != 0))
    {
      DerValue localDerValue2 = localDerInputStream.getDerValue();
      if ((localDerValue2.isContextSpecific(0)) && (localDerValue2.isConstructed()))
      {
        this.distributionPoint = new DistributionPointName(localDerValue2.data.getDerValue());
      }
      else if ((localDerValue2.isContextSpecific(1)) && (!(localDerValue2.isConstructed())))
      {
        localDerValue2.resetTag(1);
        this.hasOnlyUserCerts = localDerValue2.getBoolean();
      }
      else if ((localDerValue2.isContextSpecific(2)) && (!(localDerValue2.isConstructed())))
      {
        localDerValue2.resetTag(1);
        this.hasOnlyCACerts = localDerValue2.getBoolean();
      }
      else if ((localDerValue2.isContextSpecific(3)) && (!(localDerValue2.isConstructed())))
      {
        this.revocationReasons = new ReasonFlags(localDerValue2);
      }
      else if ((localDerValue2.isContextSpecific(4)) && (!(localDerValue2.isConstructed())))
      {
        localDerValue2.resetTag(1);
        this.isIndirectCRL = localDerValue2.getBoolean();
      }
      else if ((localDerValue2.isContextSpecific(5)) && (!(localDerValue2.isConstructed())))
      {
        localDerValue2.resetTag(1);
        this.hasOnlyAttributeCerts = localDerValue2.getBoolean();
      }
      else
      {
        throw new IOException("Invalid encoding of IssuingDistributionPoint");
      }
    }
  }

  public String getName()
  {
    return "IssuingDistributionPoint";
  }

  public void encode(OutputStream paramOutputStream)
    throws IOException
  {
    DerOutputStream localDerOutputStream = new DerOutputStream();
    if (this.extensionValue == null)
    {
      this.extensionId = PKIXExtensions.IssuingDistributionPoint_Id;
      this.critical = false;
      encodeThis();
    }
    super.encode(localDerOutputStream);
    paramOutputStream.write(localDerOutputStream.toByteArray());
  }

  public void set(String paramString, Object paramObject)
    throws IOException
  {
    if (paramString.equalsIgnoreCase("point"))
    {
      if (!(paramObject instanceof DistributionPointName))
        throw new IOException("Attribute value should be of type DistributionPointName.");
      this.distributionPoint = ((DistributionPointName)paramObject);
    }
    else
    {
      if (paramString.equalsIgnoreCase("reasons"))
      {
        if (paramObject instanceof ReasonFlags)
          break label260;
        throw new IOException("Attribute value should be of type ReasonFlags.");
      }
      if (paramString.equalsIgnoreCase("indirect_crl"))
      {
        if (!(paramObject instanceof Boolean))
          throw new IOException("Attribute value should be of type Boolean.");
        this.isIndirectCRL = ((Boolean)paramObject).booleanValue();
      }
      else if (paramString.equalsIgnoreCase("only_user_certs"))
      {
        if (!(paramObject instanceof Boolean))
          throw new IOException("Attribute value should be of type Boolean.");
        this.hasOnlyUserCerts = ((Boolean)paramObject).booleanValue();
      }
      else if (paramString.equalsIgnoreCase("only_ca_certs"))
      {
        if (!(paramObject instanceof Boolean))
          throw new IOException("Attribute value should be of type Boolean.");
        this.hasOnlyCACerts = ((Boolean)paramObject).booleanValue();
      }
      else if (paramString.equalsIgnoreCase("only_attribute_certs"))
      {
        if (!(paramObject instanceof Boolean))
          throw new IOException("Attribute value should be of type Boolean.");
        this.hasOnlyAttributeCerts = ((Boolean)paramObject).booleanValue();
      }
      else
      {
        throw new IOException("Attribute name [" + paramString + "] not recognized by " + "CertAttrSet:IssuingDistributionPointExtension.");
      }
    }
    label260: encodeThis();
  }

  public Object get(String paramString)
    throws IOException
  {
    if (paramString.equalsIgnoreCase("point"))
      return this.distributionPoint;
    if (paramString.equalsIgnoreCase("indirect_crl"))
      return Boolean.valueOf(this.isIndirectCRL);
    if (paramString.equalsIgnoreCase("reasons"))
      return this.revocationReasons;
    if (paramString.equalsIgnoreCase("only_user_certs"))
      return Boolean.valueOf(this.hasOnlyUserCerts);
    if (paramString.equalsIgnoreCase("only_ca_certs"))
      return Boolean.valueOf(this.hasOnlyCACerts);
    if (paramString.equalsIgnoreCase("only_attribute_certs"))
      return Boolean.valueOf(this.hasOnlyAttributeCerts);
    throw new IOException("Attribute name [" + paramString + "] not recognized by " + "CertAttrSet:IssuingDistributionPointExtension.");
  }

  public void delete(String paramString)
    throws IOException
  {
    if (paramString.equalsIgnoreCase("point"))
      this.distributionPoint = null;
    else if (paramString.equalsIgnoreCase("indirect_crl"))
      this.isIndirectCRL = false;
    else if (paramString.equalsIgnoreCase("reasons"))
      this.revocationReasons = null;
    else if (paramString.equalsIgnoreCase("only_user_certs"))
      this.hasOnlyUserCerts = false;
    else if (paramString.equalsIgnoreCase("only_ca_certs"))
      this.hasOnlyCACerts = false;
    else if (paramString.equalsIgnoreCase("only_attribute_certs"))
      this.hasOnlyAttributeCerts = false;
    else
      throw new IOException("Attribute name [" + paramString + "] not recognized by " + "CertAttrSet:IssuingDistributionPointExtension.");
    encodeThis();
  }

  public Enumeration<String> getElements()
  {
    AttributeNameEnumeration localAttributeNameEnumeration = new AttributeNameEnumeration();
    localAttributeNameEnumeration.addElement("point");
    localAttributeNameEnumeration.addElement("reasons");
    localAttributeNameEnumeration.addElement("only_user_certs");
    localAttributeNameEnumeration.addElement("only_ca_certs");
    localAttributeNameEnumeration.addElement("only_attribute_certs");
    localAttributeNameEnumeration.addElement("indirect_crl");
    return localAttributeNameEnumeration.elements();
  }

  private void encodeThis()
    throws IOException
  {
    if ((this.distributionPoint == null) && (this.revocationReasons == null) && (!(this.hasOnlyUserCerts)) && (!(this.hasOnlyCACerts)) && (!(this.hasOnlyAttributeCerts)) && (!(this.isIndirectCRL)))
    {
      this.extensionValue = null;
      return;
    }
    DerOutputStream localDerOutputStream1 = new DerOutputStream();
    if (this.distributionPoint != null)
    {
      localDerOutputStream2 = new DerOutputStream();
      this.distributionPoint.encode(localDerOutputStream2);
      localDerOutputStream1.writeImplicit(DerValue.createTag(-128, true, 0), localDerOutputStream2);
    }
    if (this.hasOnlyUserCerts)
    {
      localDerOutputStream2 = new DerOutputStream();
      localDerOutputStream2.putBoolean(this.hasOnlyUserCerts);
      localDerOutputStream1.writeImplicit(DerValue.createTag(-128, false, 1), localDerOutputStream2);
    }
    if (this.hasOnlyCACerts)
    {
      localDerOutputStream2 = new DerOutputStream();
      localDerOutputStream2.putBoolean(this.hasOnlyCACerts);
      localDerOutputStream1.writeImplicit(DerValue.createTag(-128, false, 2), localDerOutputStream2);
    }
    if (this.revocationReasons != null)
    {
      localDerOutputStream2 = new DerOutputStream();
      this.revocationReasons.encode(localDerOutputStream2);
      localDerOutputStream1.writeImplicit(DerValue.createTag(-128, false, 3), localDerOutputStream2);
    }
    if (this.isIndirectCRL)
    {
      localDerOutputStream2 = new DerOutputStream();
      localDerOutputStream2.putBoolean(this.isIndirectCRL);
      localDerOutputStream1.writeImplicit(DerValue.createTag(-128, false, 4), localDerOutputStream2);
    }
    if (this.hasOnlyAttributeCerts)
    {
      localDerOutputStream2 = new DerOutputStream();
      localDerOutputStream2.putBoolean(this.hasOnlyAttributeCerts);
      localDerOutputStream1.writeImplicit(DerValue.createTag(-128, false, 5), localDerOutputStream2);
    }
    DerOutputStream localDerOutputStream2 = new DerOutputStream();
    localDerOutputStream2.write(48, localDerOutputStream1);
    this.extensionValue = localDerOutputStream2.toByteArray();
  }

  public String toString()
  {
    StringBuilder localStringBuilder = new StringBuilder(super.toString());
    localStringBuilder.append("IssuingDistributionPoint [\n  ");
    if (this.distributionPoint != null)
      localStringBuilder.append(this.distributionPoint);
    if (this.revocationReasons != null)
      localStringBuilder.append(this.revocationReasons);
    localStringBuilder.append((this.hasOnlyUserCerts) ? "  Only contains user certs: true" : "  Only contains user certs: false").append("\n");
    localStringBuilder.append((this.hasOnlyCACerts) ? "  Only contains CA certs: true" : "  Only contains CA certs: false").append("\n");
    localStringBuilder.append((this.hasOnlyAttributeCerts) ? "  Only contains attribute certs: true" : "  Only contains attribute certs: false").append("\n");
    localStringBuilder.append((this.isIndirectCRL) ? "  Indirect CRL: true" : "  Indirect CRL: false").append("\n");
    localStringBuilder.append("]\n");
    return localStringBuilder.toString();
  }
}