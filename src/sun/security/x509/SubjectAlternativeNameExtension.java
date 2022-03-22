package sun.security.x509;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class SubjectAlternativeNameExtension extends Extension
  implements CertAttrSet
{
  public static final String IDENT = "x509.info.extensions.SubjectAlternativeName";
  public static final String NAME = "SubjectAlternativeName";
  public static final String SUBJECT_NAME = "subject_name";
  GeneralNames names;

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

  public SubjectAlternativeNameExtension(GeneralNames paramGeneralNames)
    throws IOException
  {
    this(Boolean.FALSE, paramGeneralNames);
  }

  public SubjectAlternativeNameExtension(Boolean paramBoolean, GeneralNames paramGeneralNames)
    throws IOException
  {
    this.names = null;
    this.names = paramGeneralNames;
    this.extensionId = PKIXExtensions.SubjectAlternativeName_Id;
    this.critical = paramBoolean.booleanValue();
    encodeThis();
  }

  public SubjectAlternativeNameExtension()
  {
    this.names = null;
    this.extensionId = PKIXExtensions.SubjectAlternativeName_Id;
    this.critical = false;
    this.names = new GeneralNames();
  }

  public SubjectAlternativeNameExtension(Boolean paramBoolean, Object paramObject)
    throws IOException
  {
    this.names = null;
    this.extensionId = PKIXExtensions.SubjectAlternativeName_Id;
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
    String str = super.toString() + "SubjectAlternativeName [\n";
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
      this.extensionId = PKIXExtensions.SubjectAlternativeName_Id;
      this.critical = false;
      encodeThis();
    }
    super.encode(localDerOutputStream);
    paramOutputStream.write(localDerOutputStream.toByteArray());
  }

  public void set(String paramString, Object paramObject)
    throws IOException
  {
    if (paramString.equalsIgnoreCase("subject_name"))
    {
      if (!(paramObject instanceof GeneralNames))
        throw new IOException("Attribute value should be of type GeneralNames.");
      this.names = ((GeneralNames)paramObject);
    }
    else
    {
      throw new IOException("Attribute name not recognized by CertAttrSet:SubjectAlternativeName.");
    }
    encodeThis();
  }

  public Object get(String paramString)
    throws IOException
  {
    if (paramString.equalsIgnoreCase("subject_name"))
      return this.names;
    throw new IOException("Attribute name not recognized by CertAttrSet:SubjectAlternativeName.");
  }

  public void delete(String paramString)
    throws IOException
  {
    if (paramString.equalsIgnoreCase("subject_name"))
      this.names = null;
    else
      throw new IOException("Attribute name not recognized by CertAttrSet:SubjectAlternativeName.");
    encodeThis();
  }

  public Enumeration getElements()
  {
    AttributeNameEnumeration localAttributeNameEnumeration = new AttributeNameEnumeration();
    localAttributeNameEnumeration.addElement("subject_name");
    return localAttributeNameEnumeration.elements();
  }

  public String getName()
  {
    return "SubjectAlternativeName";
  }
}