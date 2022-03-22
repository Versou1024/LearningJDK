package sun.security.x509;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class BasicConstraintsExtension extends Extension
  implements CertAttrSet
{
  public static final String IDENT = "x509.info.extensions.BasicConstraints";
  public static final String NAME = "BasicConstraints";
  public static final String IS_CA = "is_ca";
  public static final String PATH_LEN = "path_len";
  private boolean ca;
  private int pathLen;

  private void encodeThis()
    throws IOException
  {
    if ((!(this.ca)) && (this.pathLen < 0))
    {
      this.extensionValue = null;
      return;
    }
    DerOutputStream localDerOutputStream1 = new DerOutputStream();
    DerOutputStream localDerOutputStream2 = new DerOutputStream();
    if (this.ca)
      localDerOutputStream2.putBoolean(this.ca);
    if (this.pathLen >= 0)
      localDerOutputStream2.putInteger(this.pathLen);
    localDerOutputStream1.write(48, localDerOutputStream2);
    this.extensionValue = localDerOutputStream1.toByteArray();
  }

  public BasicConstraintsExtension(boolean paramBoolean, int paramInt)
    throws IOException
  {
    this(Boolean.valueOf(paramBoolean), paramBoolean, paramInt);
  }

  public BasicConstraintsExtension(Boolean paramBoolean, boolean paramBoolean1, int paramInt)
    throws IOException
  {
    this.ca = false;
    this.pathLen = -1;
    this.ca = paramBoolean1;
    this.pathLen = paramInt;
    this.extensionId = PKIXExtensions.BasicConstraints_Id;
    this.critical = paramBoolean.booleanValue();
    encodeThis();
  }

  public BasicConstraintsExtension(Boolean paramBoolean, Object paramObject)
    throws IOException
  {
    this.ca = false;
    this.pathLen = -1;
    this.extensionId = PKIXExtensions.BasicConstraints_Id;
    this.critical = paramBoolean.booleanValue();
    this.extensionValue = ((byte[])(byte[])paramObject);
    DerValue localDerValue1 = new DerValue(this.extensionValue);
    if (localDerValue1.tag != 48)
      throw new IOException("Invalid encoding of BasicConstraints");
    if (localDerValue1.data == null)
      return;
    DerValue localDerValue2 = localDerValue1.data.getDerValue();
    if (localDerValue2.tag != 1)
      return;
    this.ca = localDerValue2.getBoolean();
    if (localDerValue1.data.available() == 0)
    {
      this.pathLen = 2147483647;
      return;
    }
    localDerValue2 = localDerValue1.data.getDerValue();
    if (localDerValue2.tag != 2)
      throw new IOException("Invalid encoding of BasicConstraints");
    this.pathLen = localDerValue2.getInteger();
  }

  public String toString()
  {
    String str = super.toString() + "BasicConstraints:[\n";
    str = str + ((this.ca) ? "  CA:true" : "  CA:false") + "\n";
    if (this.pathLen >= 0)
      str = str + "  PathLen:" + this.pathLen + "\n";
    else
      str = str + "  PathLen: undefined\n";
    return str + "]\n";
  }

  public void encode(OutputStream paramOutputStream)
    throws IOException
  {
    DerOutputStream localDerOutputStream = new DerOutputStream();
    if (this.extensionValue == null)
    {
      this.extensionId = PKIXExtensions.BasicConstraints_Id;
      if (this.ca)
        this.critical = true;
      else
        this.critical = false;
      encodeThis();
    }
    super.encode(localDerOutputStream);
    paramOutputStream.write(localDerOutputStream.toByteArray());
  }

  public void set(String paramString, Object paramObject)
    throws IOException
  {
    if (paramString.equalsIgnoreCase("is_ca"))
    {
      if (!(paramObject instanceof Boolean))
        throw new IOException("Attribute value should be of type Boolean.");
      this.ca = ((Boolean)paramObject).booleanValue();
    }
    else if (paramString.equalsIgnoreCase("path_len"))
    {
      if (!(paramObject instanceof Integer))
        throw new IOException("Attribute value should be of type Integer.");
      this.pathLen = ((Integer)paramObject).intValue();
    }
    else
    {
      throw new IOException("Attribute name not recognized by CertAttrSet:BasicConstraints.");
    }
    encodeThis();
  }

  public Object get(String paramString)
    throws IOException
  {
    if (paramString.equalsIgnoreCase("is_ca"))
      return Boolean.valueOf(this.ca);
    if (paramString.equalsIgnoreCase("path_len"))
      return Integer.valueOf(this.pathLen);
    throw new IOException("Attribute name not recognized by CertAttrSet:BasicConstraints.");
  }

  public void delete(String paramString)
    throws IOException
  {
    if (paramString.equalsIgnoreCase("is_ca"))
      this.ca = false;
    else if (paramString.equalsIgnoreCase("path_len"))
      this.pathLen = -1;
    else
      throw new IOException("Attribute name not recognized by CertAttrSet:BasicConstraints.");
    encodeThis();
  }

  public Enumeration getElements()
  {
    AttributeNameEnumeration localAttributeNameEnumeration = new AttributeNameEnumeration();
    localAttributeNameEnumeration.addElement("is_ca");
    localAttributeNameEnumeration.addElement("path_len");
    return localAttributeNameEnumeration.elements();
  }

  public String getName()
  {
    return "BasicConstraints";
  }
}