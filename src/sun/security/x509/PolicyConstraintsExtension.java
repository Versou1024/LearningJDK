package sun.security.x509;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class PolicyConstraintsExtension extends Extension
  implements CertAttrSet
{
  public static final String IDENT = "x509.info.extensions.PolicyConstraints";
  public static final String NAME = "PolicyConstraints";
  public static final String REQUIRE = "require";
  public static final String INHIBIT = "inhibit";
  private static final byte TAG_REQUIRE = 0;
  private static final byte TAG_INHIBIT = 1;
  private int require;
  private int inhibit;

  private void encodeThis()
    throws IOException
  {
    DerOutputStream localDerOutputStream3;
    if ((this.require == -1) && (this.inhibit == -1))
    {
      this.extensionValue = null;
      return;
    }
    DerOutputStream localDerOutputStream1 = new DerOutputStream();
    DerOutputStream localDerOutputStream2 = new DerOutputStream();
    if (this.require != -1)
    {
      localDerOutputStream3 = new DerOutputStream();
      localDerOutputStream3.putInteger(this.require);
      localDerOutputStream1.writeImplicit(DerValue.createTag(-128, false, 0), localDerOutputStream3);
    }
    if (this.inhibit != -1)
    {
      localDerOutputStream3 = new DerOutputStream();
      localDerOutputStream3.putInteger(this.inhibit);
      localDerOutputStream1.writeImplicit(DerValue.createTag(-128, false, 1), localDerOutputStream3);
    }
    localDerOutputStream2.write(48, localDerOutputStream1);
    this.extensionValue = localDerOutputStream2.toByteArray();
  }

  public PolicyConstraintsExtension(int paramInt1, int paramInt2)
    throws IOException
  {
    this(Boolean.FALSE, paramInt1, paramInt2);
  }

  public PolicyConstraintsExtension(Boolean paramBoolean, int paramInt1, int paramInt2)
    throws IOException
  {
    this.require = -1;
    this.inhibit = -1;
    this.require = paramInt1;
    this.inhibit = paramInt2;
    this.extensionId = PKIXExtensions.PolicyConstraints_Id;
    this.critical = paramBoolean.booleanValue();
    encodeThis();
  }

  public PolicyConstraintsExtension(Boolean paramBoolean, Object paramObject)
    throws IOException
  {
    this.require = -1;
    this.inhibit = -1;
    this.extensionId = PKIXExtensions.PolicyConstraints_Id;
    this.critical = paramBoolean.booleanValue();
    this.extensionValue = ((byte[])(byte[])paramObject);
    DerValue localDerValue1 = new DerValue(this.extensionValue);
    if (localDerValue1.tag != 48)
      throw new IOException("Sequence tag missing for PolicyConstraint.");
    DerInputStream localDerInputStream = localDerValue1.data;
    while ((localDerInputStream != null) && (localDerInputStream.available() != 0))
    {
      DerValue localDerValue2 = localDerInputStream.getDerValue();
      if ((localDerValue2.isContextSpecific(0)) && (!(localDerValue2.isConstructed())))
      {
        if (this.require != -1)
          throw new IOException("Duplicate requireExplicitPolicyfound in the PolicyConstraintsExtension");
        localDerValue2.resetTag(2);
        this.require = localDerValue2.getInteger();
      }
      else if ((localDerValue2.isContextSpecific(1)) && (!(localDerValue2.isConstructed())))
      {
        if (this.inhibit != -1)
          throw new IOException("Duplicate inhibitPolicyMappingfound in the PolicyConstraintsExtension");
        localDerValue2.resetTag(2);
        this.inhibit = localDerValue2.getInteger();
      }
      else
      {
        throw new IOException("Invalid encoding of PolicyConstraint");
      }
    }
  }

  public String toString()
  {
    String str = super.toString() + "PolicyConstraints: [" + "  Require: ";
    if (this.require == -1)
      str = str + "unspecified;";
    else
      str = str + this.require + ";";
    str = str + "\tInhibit: ";
    if (this.inhibit == -1)
      str = str + "unspecified";
    else
      str = str + this.inhibit;
    str = str + " ]\n";
    return str;
  }

  public void encode(OutputStream paramOutputStream)
    throws IOException
  {
    DerOutputStream localDerOutputStream = new DerOutputStream();
    if (this.extensionValue == null)
    {
      this.extensionId = PKIXExtensions.PolicyConstraints_Id;
      this.critical = false;
      encodeThis();
    }
    super.encode(localDerOutputStream);
    paramOutputStream.write(localDerOutputStream.toByteArray());
  }

  public void set(String paramString, Object paramObject)
    throws IOException
  {
    if (!(paramObject instanceof Integer))
      throw new IOException("Attribute value should be of type Integer.");
    if (paramString.equalsIgnoreCase("require"))
      this.require = ((Integer)paramObject).intValue();
    else if (paramString.equalsIgnoreCase("inhibit"))
      this.inhibit = ((Integer)paramObject).intValue();
    else
      throw new IOException("Attribute name [" + paramString + "]" + " not recognized by " + "CertAttrSet:PolicyConstraints.");
    encodeThis();
  }

  public Object get(String paramString)
    throws IOException
  {
    if (paramString.equalsIgnoreCase("require"))
      return new Integer(this.require);
    if (paramString.equalsIgnoreCase("inhibit"))
      return new Integer(this.inhibit);
    throw new IOException("Attribute name not recognized by CertAttrSet:PolicyConstraints.");
  }

  public void delete(String paramString)
    throws IOException
  {
    if (paramString.equalsIgnoreCase("require"))
      this.require = -1;
    else if (paramString.equalsIgnoreCase("inhibit"))
      this.inhibit = -1;
    else
      throw new IOException("Attribute name not recognized by CertAttrSet:PolicyConstraints.");
    encodeThis();
  }

  public Enumeration getElements()
  {
    AttributeNameEnumeration localAttributeNameEnumeration = new AttributeNameEnumeration();
    localAttributeNameEnumeration.addElement("require");
    localAttributeNameEnumeration.addElement("inhibit");
    return localAttributeNameEnumeration.elements();
  }

  public String getName()
  {
    return "PolicyConstraints";
  }
}