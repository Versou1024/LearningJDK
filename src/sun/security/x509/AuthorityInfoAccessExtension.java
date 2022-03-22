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

public class AuthorityInfoAccessExtension extends Extension
  implements CertAttrSet
{
  public static final String IDENT = "x509.info.extensions.AuthorityInfoAccess";
  public static final String NAME = "AuthorityInfoAccess";
  public static final String DESCRIPTIONS = "descriptions";
  private List<AccessDescription> accessDescriptions;

  public AuthorityInfoAccessExtension(List<AccessDescription> paramList)
    throws IOException
  {
    this.extensionId = PKIXExtensions.AuthInfoAccess_Id;
    this.critical = false;
    this.accessDescriptions = paramList;
    encodeThis();
  }

  public AuthorityInfoAccessExtension(Boolean paramBoolean, Object paramObject)
    throws IOException
  {
    this.extensionId = PKIXExtensions.AuthInfoAccess_Id;
    this.critical = paramBoolean.booleanValue();
    if (!(paramObject instanceof byte[]))
      throw new IOException("Illegal argument type");
    this.extensionValue = ((byte[])(byte[])paramObject);
    DerValue localDerValue1 = new DerValue(this.extensionValue);
    if (localDerValue1.tag != 48)
      throw new IOException("Invalid encoding for AuthorityInfoAccessExtension.");
    this.accessDescriptions = new ArrayList();
    while (localDerValue1.data.available() != 0)
    {
      DerValue localDerValue2 = localDerValue1.data.getDerValue();
      AccessDescription localAccessDescription = new AccessDescription(localDerValue2);
      this.accessDescriptions.add(localAccessDescription);
    }
  }

  public List<AccessDescription> getAccessDescriptions()
  {
    return this.accessDescriptions;
  }

  public String getName()
  {
    return "AuthorityInfoAccess";
  }

  public void encode(OutputStream paramOutputStream)
    throws IOException
  {
    DerOutputStream localDerOutputStream = new DerOutputStream();
    if (this.extensionValue == null)
    {
      this.extensionId = PKIXExtensions.AuthInfoAccess_Id;
      this.critical = false;
      encodeThis();
    }
    super.encode(localDerOutputStream);
    paramOutputStream.write(localDerOutputStream.toByteArray());
  }

  public void set(String paramString, Object paramObject)
    throws IOException
  {
    if (paramString.equalsIgnoreCase("descriptions"))
    {
      if (!(paramObject instanceof List))
        throw new IOException("Attribute value should be of type List.");
      this.accessDescriptions = ((List)paramObject);
    }
    else
    {
      throw new IOException("Attribute name [" + paramString + "] not recognized by " + "CertAttrSet:AuthorityInfoAccessExtension.");
    }
    encodeThis();
  }

  public Object get(String paramString)
    throws IOException
  {
    if (paramString.equalsIgnoreCase("descriptions"))
      return this.accessDescriptions;
    throw new IOException("Attribute name [" + paramString + "] not recognized by " + "CertAttrSet:AuthorityInfoAccessExtension.");
  }

  public void delete(String paramString)
    throws IOException
  {
    if (paramString.equalsIgnoreCase("descriptions"))
      this.accessDescriptions = new ArrayList();
    else
      throw new IOException("Attribute name [" + paramString + "] not recognized by " + "CertAttrSet:AuthorityInfoAccessExtension.");
    encodeThis();
  }

  public Enumeration<String> getElements()
  {
    AttributeNameEnumeration localAttributeNameEnumeration = new AttributeNameEnumeration();
    localAttributeNameEnumeration.addElement("descriptions");
    return localAttributeNameEnumeration.elements();
  }

  private void encodeThis()
    throws IOException
  {
    if (this.accessDescriptions.isEmpty())
    {
      this.extensionValue = null;
    }
    else
    {
      DerOutputStream localDerOutputStream = new DerOutputStream();
      Object localObject = this.accessDescriptions.iterator();
      while (((Iterator)localObject).hasNext())
      {
        AccessDescription localAccessDescription = (AccessDescription)((Iterator)localObject).next();
        localAccessDescription.encode(localDerOutputStream);
      }
      localObject = new DerOutputStream();
      ((DerOutputStream)localObject).write(48, localDerOutputStream);
      this.extensionValue = ((DerOutputStream)localObject).toByteArray();
    }
  }

  public String toString()
  {
    return super.toString() + "AuthorityInfoAccess [\n  " + this.accessDescriptions + "\n]\n";
  }
}