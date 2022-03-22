package sun.security.x509;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class IssuerAlternativeNameExtension extends Extension
  implements CertAttrSet
{
  public static final String IDENT = "x509.info.extensions.IssuerAlternativeName";
  public static final String NAME = "IssuerAlternativeName";
  public static final String ISSUER_NAME = "issuer_name";
  GeneralNames names = null;

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

  public IssuerAlternativeNameExtension(GeneralNames paramGeneralNames)
    throws IOException
  {
    this.names = paramGeneralNames;
    this.extensionId = PKIXExtensions.IssuerAlternativeName_Id;
    this.critical = false;
    encodeThis();
  }

  public IssuerAlternativeNameExtension()
  {
    this.extensionId = PKIXExtensions.IssuerAlternativeName_Id;
    this.critical = false;
    this.names = new GeneralNames();
  }

  public IssuerAlternativeNameExtension(Boolean paramBoolean, Object paramObject)
    throws IOException
  {
    this.extensionId = PKIXExtensions.IssuerAlternativeName_Id;
    this.critical = paramBoolean.booleanValue();
    this.extensionValue = ((byte[])(byte[])paramObject);
    DerValue localDerValue = new DerValue(this.extensionValue);
    if (localDerValue.data == null)
    {
      this.names = new GeneralNames();
      return;
    }
    this.names = new GeneralNames(localDerValue);
  }

  public String toString()
  {
    String str = super.toString() + "IssuerAlternativeName [\n";
    if (this.names == null)
    {
      str = str + "  null\n";
    }
    else
    {
      Iterator localIterator = this.names.names().iterator();
      while (localIterator.hasNext())
      {
        GeneralName localGeneralName = (GeneralName)localIterator.next();
        str = str + "  " + localGeneralName + "\n";
      }
    }
    str = str + "]\n";
    return str;
  }

  public void encode(OutputStream paramOutputStream)
    throws IOException
  {
    DerOutputStream localDerOutputStream = new DerOutputStream();
    if (this.extensionValue == null)
    {
      this.extensionId = PKIXExtensions.IssuerAlternativeName_Id;
      this.critical = false;
      encodeThis();
    }
    super.encode(localDerOutputStream);
    paramOutputStream.write(localDerOutputStream.toByteArray());
  }

  public void set(String paramString, Object paramObject)
    throws IOException
  {
    if (paramString.equalsIgnoreCase("issuer_name"))
    {
      if (!(paramObject instanceof GeneralNames))
        throw new IOException("Attribute value should be of type GeneralNames.");
      this.names = ((GeneralNames)paramObject);
    }
    else
    {
      throw new IOException("Attribute name not recognized by CertAttrSet:IssuerAlternativeName.");
    }
    encodeThis();
  }

  public Object get(String paramString)
    throws IOException
  {
    if (paramString.equalsIgnoreCase("issuer_name"))
      return this.names;
    throw new IOException("Attribute name not recognized by CertAttrSet:IssuerAlternativeName.");
  }

  public void delete(String paramString)
    throws IOException
  {
    if (paramString.equalsIgnoreCase("issuer_name"))
      this.names = null;
    else
      throw new IOException("Attribute name not recognized by CertAttrSet:IssuerAlternativeName.");
    encodeThis();
  }

  public Enumeration getElements()
  {
    AttributeNameEnumeration localAttributeNameEnumeration = new AttributeNameEnumeration();
    localAttributeNameEnumeration.addElement("issuer_name");
    return localAttributeNameEnumeration.elements();
  }

  public String getName()
  {
    return "IssuerAlternativeName";
  }
}