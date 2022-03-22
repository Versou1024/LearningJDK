package sun.security.x509;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import sun.security.util.Debug;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;

public class InhibitAnyPolicyExtension extends Extension
  implements CertAttrSet
{
  private static final Debug debug = Debug.getInstance("certpath");
  public static final String IDENT = "x509.info.extensions.InhibitAnyPolicy";
  public static ObjectIdentifier AnyPolicy_Id;
  public static final String NAME = "InhibitAnyPolicy";
  public static final String SKIP_CERTS = "skip_certs";
  private int skipCerts = 2147483647;

  private void encodeThis()
    throws IOException
  {
    DerOutputStream localDerOutputStream = new DerOutputStream();
    localDerOutputStream.putInteger(this.skipCerts);
    this.extensionValue = localDerOutputStream.toByteArray();
  }

  public InhibitAnyPolicyExtension(int paramInt)
    throws IOException
  {
    if (paramInt < -1)
      throw new IOException("Invalid value for skipCerts");
    if (paramInt == -1)
      this.skipCerts = 2147483647;
    else
      this.skipCerts = paramInt;
    this.extensionId = PKIXExtensions.InhibitAnyPolicy_Id;
    this.critical = true;
    encodeThis();
  }

  public InhibitAnyPolicyExtension(Boolean paramBoolean, Object paramObject)
    throws IOException
  {
    this.extensionId = PKIXExtensions.InhibitAnyPolicy_Id;
    if (!(paramBoolean.booleanValue()))
      throw new IOException("Criticality cannot be false for InhibitAnyPolicy");
    this.critical = paramBoolean.booleanValue();
    this.extensionValue = ((byte[])(byte[])paramObject);
    DerValue localDerValue = new DerValue(this.extensionValue);
    if (localDerValue.tag != 2)
      throw new IOException("Invalid encoding of InhibitAnyPolicy: data not integer");
    if (localDerValue.data == null)
      throw new IOException("Invalid encoding of InhibitAnyPolicy: null data");
    int i = localDerValue.getInteger();
    if (i < -1)
      throw new IOException("Invalid value for skipCerts");
    if (i == -1)
      this.skipCerts = 2147483647;
    else
      this.skipCerts = i;
  }

  public String toString()
  {
    String str = super.toString() + "InhibitAnyPolicy: " + this.skipCerts + "\n";
    return str;
  }

  public void encode(OutputStream paramOutputStream)
    throws IOException
  {
    DerOutputStream localDerOutputStream = new DerOutputStream();
    if (this.extensionValue == null)
    {
      this.extensionId = PKIXExtensions.InhibitAnyPolicy_Id;
      this.critical = true;
      encodeThis();
    }
    super.encode(localDerOutputStream);
    paramOutputStream.write(localDerOutputStream.toByteArray());
  }

  public void set(String paramString, Object paramObject)
    throws IOException
  {
    if (paramString.equalsIgnoreCase("skip_certs"))
    {
      if (!(paramObject instanceof Integer))
        throw new IOException("Attribute value should be of type Integer.");
      int i = ((Integer)paramObject).intValue();
      if (i < -1)
        throw new IOException("Invalid value for skipCerts");
      if (i == -1)
        this.skipCerts = 2147483647;
      else
        this.skipCerts = i;
    }
    else
    {
      throw new IOException("Attribute name not recognized by CertAttrSet:InhibitAnyPolicy.");
    }
    encodeThis();
  }

  public Object get(String paramString)
    throws IOException
  {
    if (paramString.equalsIgnoreCase("skip_certs"))
      return new Integer(this.skipCerts);
    throw new IOException("Attribute name not recognized by CertAttrSet:InhibitAnyPolicy.");
  }

  public void delete(String paramString)
    throws IOException
  {
    if (paramString.equalsIgnoreCase("skip_certs"))
      throw new IOException("Attribute skip_certs may not be deleted.");
    throw new IOException("Attribute name not recognized by CertAttrSet:InhibitAnyPolicy.");
  }

  public Enumeration getElements()
  {
    AttributeNameEnumeration localAttributeNameEnumeration = new AttributeNameEnumeration();
    localAttributeNameEnumeration.addElement("skip_certs");
    return localAttributeNameEnumeration.elements();
  }

  public String getName()
  {
    return "InhibitAnyPolicy";
  }

  static
  {
    try
    {
      AnyPolicy_Id = new ObjectIdentifier("2.5.29.32.0");
    }
    catch (IOException localIOException)
    {
    }
  }
}