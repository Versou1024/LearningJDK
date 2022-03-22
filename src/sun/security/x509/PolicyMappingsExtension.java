package sun.security.x509;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class PolicyMappingsExtension extends Extension
  implements CertAttrSet
{
  public static final String IDENT = "x509.info.extensions.PolicyMappings";
  public static final String NAME = "PolicyMappings";
  public static final String MAP = "map";
  private List<CertificatePolicyMap> maps;

  private void encodeThis()
    throws IOException
  {
    if ((this.maps == null) || (this.maps.isEmpty()))
    {
      this.extensionValue = null;
      return;
    }
    DerOutputStream localDerOutputStream1 = new DerOutputStream();
    DerOutputStream localDerOutputStream2 = new DerOutputStream();
    Iterator localIterator = this.maps.iterator();
    while (localIterator.hasNext())
    {
      CertificatePolicyMap localCertificatePolicyMap = (CertificatePolicyMap)localIterator.next();
      localCertificatePolicyMap.encode(localDerOutputStream2);
    }
    localDerOutputStream1.write(48, localDerOutputStream2);
    this.extensionValue = localDerOutputStream1.toByteArray();
  }

  public PolicyMappingsExtension(List<CertificatePolicyMap> paramList)
    throws IOException
  {
    this.maps = paramList;
    this.extensionId = PKIXExtensions.PolicyMappings_Id;
    this.critical = false;
    encodeThis();
  }

  public PolicyMappingsExtension()
  {
    this.extensionId = PKIXExtensions.KeyUsage_Id;
    this.critical = false;
    this.maps = new ArrayList();
  }

  public PolicyMappingsExtension(Boolean paramBoolean, Object paramObject)
    throws IOException
  {
    this.extensionId = PKIXExtensions.PolicyMappings_Id;
    this.critical = paramBoolean.booleanValue();
    this.extensionValue = ((byte[])(byte[])paramObject);
    DerValue localDerValue1 = new DerValue(this.extensionValue);
    if (localDerValue1.tag != 48)
      throw new IOException("Invalid encoding for PolicyMappingsExtension.");
    this.maps = new ArrayList();
    while (localDerValue1.data.available() != 0)
    {
      DerValue localDerValue2 = localDerValue1.data.getDerValue();
      CertificatePolicyMap localCertificatePolicyMap = new CertificatePolicyMap(localDerValue2);
      this.maps.add(localCertificatePolicyMap);
    }
  }

  public String toString()
  {
    if (this.maps == null)
      return "";
    String str = super.toString() + "PolicyMappings [\n" + this.maps.toString() + "]\n";
    return str;
  }

  public void encode(OutputStream paramOutputStream)
    throws IOException
  {
    DerOutputStream localDerOutputStream = new DerOutputStream();
    if (this.extensionValue == null)
    {
      this.extensionId = PKIXExtensions.PolicyMappings_Id;
      this.critical = false;
      encodeThis();
    }
    super.encode(localDerOutputStream);
    paramOutputStream.write(localDerOutputStream.toByteArray());
  }

  public void set(String paramString, Object paramObject)
    throws IOException
  {
    if (paramString.equalsIgnoreCase("map"))
    {
      if (!(paramObject instanceof List))
        throw new IOException("Attribute value should be of type List.");
      this.maps = ((List)paramObject);
    }
    else
    {
      throw new IOException("Attribute name not recognized by CertAttrSet:PolicyMappingsExtension.");
    }
    encodeThis();
  }

  public Object get(String paramString)
    throws IOException
  {
    if (paramString.equalsIgnoreCase("map"))
      return this.maps;
    throw new IOException("Attribute name not recognized by CertAttrSet:PolicyMappingsExtension.");
  }

  public void delete(String paramString)
    throws IOException
  {
    if (paramString.equalsIgnoreCase("map"))
      this.maps = null;
    else
      throw new IOException("Attribute name not recognized by CertAttrSet:PolicyMappingsExtension.");
    encodeThis();
  }

  public Enumeration<String> getElements()
  {
    AttributeNameEnumeration localAttributeNameEnumeration = new AttributeNameEnumeration();
    localAttributeNameEnumeration.addElement("map");
    return localAttributeNameEnumeration.elements();
  }

  public String getName()
  {
    return "PolicyMappings";
  }
}