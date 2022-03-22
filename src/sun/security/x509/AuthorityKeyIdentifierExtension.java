package sun.security.x509;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class AuthorityKeyIdentifierExtension extends Extension
  implements CertAttrSet
{
  public static final String IDENT = "x509.info.extensions.AuthorityKeyIdentifier";
  public static final String NAME = "AuthorityKeyIdentifier";
  public static final String KEY_ID = "key_id";
  public static final String AUTH_NAME = "auth_name";
  public static final String SERIAL_NUMBER = "serial_number";
  private static final byte TAG_ID = 0;
  private static final byte TAG_NAMES = 1;
  private static final byte TAG_SERIAL_NUM = 2;
  private KeyIdentifier id = null;
  private GeneralNames names = null;
  private SerialNumber serialNum = null;

  private void encodeThis()
    throws IOException
  {
    DerOutputStream localDerOutputStream3;
    if ((this.id == null) && (this.names == null) && (this.serialNum == null))
    {
      this.extensionValue = null;
      return;
    }
    DerOutputStream localDerOutputStream1 = new DerOutputStream();
    DerOutputStream localDerOutputStream2 = new DerOutputStream();
    if (this.id != null)
    {
      localDerOutputStream3 = new DerOutputStream();
      this.id.encode(localDerOutputStream3);
      localDerOutputStream2.writeImplicit(DerValue.createTag(-128, false, 0), localDerOutputStream3);
    }
    try
    {
      if (this.names != null)
      {
        localDerOutputStream3 = new DerOutputStream();
        this.names.encode(localDerOutputStream3);
        localDerOutputStream2.writeImplicit(DerValue.createTag(-128, true, 1), localDerOutputStream3);
      }
    }
    catch (Exception localException)
    {
      throw new IOException(localException.toString());
    }
    if (this.serialNum != null)
    {
      DerOutputStream localDerOutputStream4 = new DerOutputStream();
      this.serialNum.encode(localDerOutputStream4);
      localDerOutputStream2.writeImplicit(DerValue.createTag(-128, false, 2), localDerOutputStream4);
    }
    localDerOutputStream1.write(48, localDerOutputStream2);
    this.extensionValue = localDerOutputStream1.toByteArray();
  }

  public AuthorityKeyIdentifierExtension(KeyIdentifier paramKeyIdentifier, GeneralNames paramGeneralNames, SerialNumber paramSerialNumber)
    throws IOException
  {
    this.id = paramKeyIdentifier;
    this.names = paramGeneralNames;
    this.serialNum = paramSerialNumber;
    this.extensionId = PKIXExtensions.AuthorityKey_Id;
    this.critical = false;
    encodeThis();
  }

  public AuthorityKeyIdentifierExtension(Boolean paramBoolean, Object paramObject)
    throws IOException
  {
    this.extensionId = PKIXExtensions.AuthorityKey_Id;
    this.critical = paramBoolean.booleanValue();
    this.extensionValue = ((byte[])(byte[])paramObject);
    DerValue localDerValue1 = new DerValue(this.extensionValue);
    if (localDerValue1.tag != 48)
      throw new IOException("Invalid encoding for AuthorityKeyIdentifierExtension.");
    while ((localDerValue1.data != null) && (localDerValue1.data.available() != 0))
    {
      DerValue localDerValue2 = localDerValue1.data.getDerValue();
      if ((localDerValue2.isContextSpecific(0)) && (!(localDerValue2.isConstructed())))
      {
        if (this.id != null)
          throw new IOException("Duplicate KeyIdentifier in AuthorityKeyIdentifier.");
        localDerValue2.resetTag(4);
        this.id = new KeyIdentifier(localDerValue2);
      }
      else if ((localDerValue2.isContextSpecific(1)) && (localDerValue2.isConstructed()))
      {
        if (this.names != null)
          throw new IOException("Duplicate GeneralNames in AuthorityKeyIdentifier.");
        localDerValue2.resetTag(48);
        this.names = new GeneralNames(localDerValue2);
      }
      else if ((localDerValue2.isContextSpecific(2)) && (!(localDerValue2.isConstructed())))
      {
        if (this.serialNum != null)
          throw new IOException("Duplicate SerialNumber in AuthorityKeyIdentifier.");
        localDerValue2.resetTag(2);
        this.serialNum = new SerialNumber(localDerValue2);
      }
      else
      {
        throw new IOException("Invalid encoding of AuthorityKeyIdentifierExtension.");
      }
    }
  }

  public String toString()
  {
    String str = super.toString() + "AuthorityKeyIdentifier [\n";
    if (this.id != null)
      str = str + this.id.toString() + "\n";
    if (this.names != null)
      str = str + this.names.toString() + "\n";
    if (this.serialNum != null)
      str = str + this.serialNum.toString() + "\n";
    return str + "]\n";
  }

  public void encode(OutputStream paramOutputStream)
    throws IOException
  {
    DerOutputStream localDerOutputStream = new DerOutputStream();
    if (this.extensionValue == null)
    {
      this.extensionId = PKIXExtensions.AuthorityKey_Id;
      this.critical = false;
      encodeThis();
    }
    super.encode(localDerOutputStream);
    paramOutputStream.write(localDerOutputStream.toByteArray());
  }

  public void set(String paramString, Object paramObject)
    throws IOException
  {
    if (paramString.equalsIgnoreCase("key_id"))
    {
      if (!(paramObject instanceof KeyIdentifier))
        throw new IOException("Attribute value should be of type KeyIdentifier.");
      this.id = ((KeyIdentifier)paramObject);
    }
    else if (paramString.equalsIgnoreCase("auth_name"))
    {
      if (!(paramObject instanceof GeneralNames))
        throw new IOException("Attribute value should be of type GeneralNames.");
      this.names = ((GeneralNames)paramObject);
    }
    else if (paramString.equalsIgnoreCase("serial_number"))
    {
      if (!(paramObject instanceof SerialNumber))
        throw new IOException("Attribute value should be of type SerialNumber.");
      this.serialNum = ((SerialNumber)paramObject);
    }
    else
    {
      throw new IOException("Attribute name not recognized by CertAttrSet:AuthorityKeyIdentifier.");
    }
    encodeThis();
  }

  public Object get(String paramString)
    throws IOException
  {
    if (paramString.equalsIgnoreCase("key_id"))
      return this.id;
    if (paramString.equalsIgnoreCase("auth_name"))
      return this.names;
    if (paramString.equalsIgnoreCase("serial_number"))
      return this.serialNum;
    throw new IOException("Attribute name not recognized by CertAttrSet:AuthorityKeyIdentifier.");
  }

  public void delete(String paramString)
    throws IOException
  {
    if (paramString.equalsIgnoreCase("key_id"))
      this.id = null;
    else if (paramString.equalsIgnoreCase("auth_name"))
      this.names = null;
    else if (paramString.equalsIgnoreCase("serial_number"))
      this.serialNum = null;
    else
      throw new IOException("Attribute name not recognized by CertAttrSet:AuthorityKeyIdentifier.");
    encodeThis();
  }

  public Enumeration getElements()
  {
    AttributeNameEnumeration localAttributeNameEnumeration = new AttributeNameEnumeration();
    localAttributeNameEnumeration.addElement("key_id");
    localAttributeNameEnumeration.addElement("auth_name");
    localAttributeNameEnumeration.addElement("serial_number");
    return localAttributeNameEnumeration.elements();
  }

  public String getName()
  {
    return "AuthorityKeyIdentifier";
  }
}