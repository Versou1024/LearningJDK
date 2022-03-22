package sun.security.x509;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.Enumeration;
import sun.security.util.Debug;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;

public class CRLNumberExtension extends Extension
  implements CertAttrSet
{
  public static final String NAME = "CRLNumber";
  public static final String NUMBER = "value";
  private static final String LABEL = "CRL Number";
  private BigInteger crlNumber;
  private String extensionName;
  private String extensionLabel;

  private void encodeThis()
    throws IOException
  {
    if (this.crlNumber == null)
    {
      this.extensionValue = null;
      return;
    }
    DerOutputStream localDerOutputStream = new DerOutputStream();
    localDerOutputStream.putInteger(this.crlNumber);
    this.extensionValue = localDerOutputStream.toByteArray();
  }

  public CRLNumberExtension(int paramInt)
    throws IOException
  {
    this(PKIXExtensions.CRLNumber_Id, false, BigInteger.valueOf(paramInt), "CRLNumber", "CRL Number");
  }

  public CRLNumberExtension(BigInteger paramBigInteger)
    throws IOException
  {
    this(PKIXExtensions.CRLNumber_Id, false, paramBigInteger, "CRLNumber", "CRL Number");
  }

  protected CRLNumberExtension(ObjectIdentifier paramObjectIdentifier, boolean paramBoolean, BigInteger paramBigInteger, String paramString1, String paramString2)
    throws IOException
  {
    this.crlNumber = null;
    this.extensionId = paramObjectIdentifier;
    this.critical = paramBoolean;
    this.crlNumber = paramBigInteger;
    this.extensionName = paramString1;
    this.extensionLabel = paramString2;
    encodeThis();
  }

  public CRLNumberExtension(Boolean paramBoolean, Object paramObject)
    throws IOException
  {
    this(PKIXExtensions.CRLNumber_Id, paramBoolean, paramObject, "CRLNumber", "CRL Number");
  }

  protected CRLNumberExtension(ObjectIdentifier paramObjectIdentifier, Boolean paramBoolean, Object paramObject, String paramString1, String paramString2)
    throws IOException
  {
    this.crlNumber = null;
    this.extensionId = paramObjectIdentifier;
    this.critical = paramBoolean.booleanValue();
    this.extensionValue = ((byte[])(byte[])paramObject);
    DerValue localDerValue = new DerValue(this.extensionValue);
    this.crlNumber = localDerValue.getBigInteger();
    this.extensionName = paramString1;
    this.extensionLabel = paramString2;
  }

  public void set(String paramString, Object paramObject)
    throws IOException
  {
    if (paramString.equalsIgnoreCase("value"))
    {
      if (!(paramObject instanceof BigInteger))
        throw new IOException("Attribute must be of type BigInteger.");
      this.crlNumber = ((BigInteger)paramObject);
    }
    else
    {
      throw new IOException("Attribute name not recognized by CertAttrSet:" + this.extensionName + ".");
    }
    encodeThis();
  }

  public Object get(String paramString)
    throws IOException
  {
    if (paramString.equalsIgnoreCase("value"))
    {
      if (this.crlNumber == null)
        return null;
      return this.crlNumber;
    }
    throw new IOException("Attribute name not recognized by CertAttrSet:" + this.extensionName + ".");
  }

  public void delete(String paramString)
    throws IOException
  {
    if (paramString.equalsIgnoreCase("value"))
      this.crlNumber = null;
    else
      throw new IOException("Attribute name not recognized by CertAttrSet:" + this.extensionName + ".");
    encodeThis();
  }

  public String toString()
  {
    String str = super.toString() + this.extensionLabel + ": " + ((this.crlNumber == null) ? "" : Debug.toHexString(this.crlNumber)) + "\n";
    return str;
  }

  public void encode(OutputStream paramOutputStream)
    throws IOException
  {
    DerOutputStream localDerOutputStream = new DerOutputStream();
    encode(paramOutputStream, PKIXExtensions.CRLNumber_Id, true);
  }

  protected void encode(OutputStream paramOutputStream, ObjectIdentifier paramObjectIdentifier, boolean paramBoolean)
    throws IOException
  {
    DerOutputStream localDerOutputStream = new DerOutputStream();
    if (this.extensionValue == null)
    {
      this.extensionId = paramObjectIdentifier;
      this.critical = paramBoolean;
      encodeThis();
    }
    super.encode(localDerOutputStream);
    paramOutputStream.write(localDerOutputStream.toByteArray());
  }

  public Enumeration getElements()
  {
    AttributeNameEnumeration localAttributeNameEnumeration = new AttributeNameEnumeration();
    localAttributeNameEnumeration.addElement("value");
    return localAttributeNameEnumeration.elements();
  }

  public String getName()
  {
    return this.extensionName;
  }
}