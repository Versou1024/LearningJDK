package sun.security.x509;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class CRLReasonCodeExtension extends Extension
  implements CertAttrSet
{
  public static final String NAME = "CRLReasonCode";
  public static final String REASON = "reason";
  public static final int UNSPECIFIED = 0;
  public static final int KEY_COMPROMISE = 1;
  public static final int CA_COMPROMISE = 2;
  public static final int AFFLIATION_CHANGED = 3;
  public static final int SUPERSEDED = 4;
  public static final int CESSATION_OF_OPERATION = 5;
  public static final int CERTIFICATE_HOLD = 6;
  public static final int REMOVE_FROM_CRL = 8;
  public static final int PRIVILEGE_WITHDRAWN = 9;
  public static final int AA_COMPROMISE = 10;
  private int reasonCode = 0;

  private void encodeThis()
    throws IOException
  {
    if (this.reasonCode == 0)
    {
      this.extensionValue = null;
      return;
    }
    DerOutputStream localDerOutputStream = new DerOutputStream();
    localDerOutputStream.putEnumerated(this.reasonCode);
    this.extensionValue = localDerOutputStream.toByteArray();
  }

  public CRLReasonCodeExtension(int paramInt)
    throws IOException
  {
    this.reasonCode = paramInt;
    this.extensionId = PKIXExtensions.ReasonCode_Id;
    this.critical = false;
    encodeThis();
  }

  public CRLReasonCodeExtension(boolean paramBoolean, int paramInt)
    throws IOException
  {
    this.extensionId = PKIXExtensions.ReasonCode_Id;
    this.critical = paramBoolean;
    this.reasonCode = paramInt;
    encodeThis();
  }

  public CRLReasonCodeExtension(Boolean paramBoolean, Object paramObject)
    throws IOException
  {
    this.extensionId = PKIXExtensions.ReasonCode_Id;
    this.critical = paramBoolean.booleanValue();
    this.extensionValue = ((byte[])(byte[])paramObject);
    DerValue localDerValue = new DerValue(this.extensionValue);
    this.reasonCode = localDerValue.getEnumerated();
  }

  public void set(String paramString, Object paramObject)
    throws IOException
  {
    if (!(paramObject instanceof Integer))
      throw new IOException("Attribute must be of type Integer.");
    if (paramString.equalsIgnoreCase("reason"))
      this.reasonCode = ((Integer)paramObject).intValue();
    else
      throw new IOException("Name not supported by CRLReasonCodeExtension");
    encodeThis();
  }

  public Object get(String paramString)
    throws IOException
  {
    if (paramString.equalsIgnoreCase("reason"))
      return new Integer(this.reasonCode);
    throw new IOException("Name not supported by CRLReasonCodeExtension");
  }

  public void delete(String paramString)
    throws IOException
  {
    if (paramString.equalsIgnoreCase("reason"))
      this.reasonCode = 0;
    else
      throw new IOException("Name not supported by CRLReasonCodeExtension");
    encodeThis();
  }

  public String toString()
  {
    String str = super.toString() + "    Reason Code: ";
    switch (this.reasonCode)
    {
    case 0:
      str = str + "Unspecified";
      break;
    case 1:
      str = str + "Key Compromise";
      break;
    case 2:
      str = str + "CA Compromise";
      break;
    case 3:
      str = str + "Affiliation Changed";
      break;
    case 4:
      str = str + "Superseded";
      break;
    case 5:
      str = str + "Cessation Of Operation";
      break;
    case 6:
      str = str + "Certificate Hold";
      break;
    case 8:
      str = str + "Remove from CRL";
      break;
    case 9:
      str = str + "Privilege Withdrawn";
      break;
    case 10:
      str = str + "AA Compromise";
      break;
    case 7:
    default:
      str = str + "Unrecognized reason code (" + this.reasonCode + ")";
    }
    return str;
  }

  public void encode(OutputStream paramOutputStream)
    throws IOException
  {
    DerOutputStream localDerOutputStream = new DerOutputStream();
    if (this.extensionValue == null)
    {
      this.extensionId = PKIXExtensions.ReasonCode_Id;
      this.critical = false;
      encodeThis();
    }
    super.encode(localDerOutputStream);
    paramOutputStream.write(localDerOutputStream.toByteArray());
  }

  public Enumeration getElements()
  {
    AttributeNameEnumeration localAttributeNameEnumeration = new AttributeNameEnumeration();
    localAttributeNameEnumeration.addElement("reason");
    return localAttributeNameEnumeration.elements();
  }

  public String getName()
  {
    return "CRLReasonCode";
  }
}