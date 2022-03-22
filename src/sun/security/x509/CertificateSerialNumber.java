package sun.security.x509;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.Enumeration;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class CertificateSerialNumber
  implements CertAttrSet
{
  public static final String IDENT = "x509.info.serialNumber";
  public static final String NAME = "serialNumber";
  public static final String NUMBER = "number";
  private SerialNumber serial;

  public CertificateSerialNumber(BigInteger paramBigInteger)
  {
    this.serial = new SerialNumber(paramBigInteger);
  }

  public CertificateSerialNumber(int paramInt)
  {
    this.serial = new SerialNumber(paramInt);
  }

  public CertificateSerialNumber(DerInputStream paramDerInputStream)
    throws IOException
  {
    this.serial = new SerialNumber(paramDerInputStream);
  }

  public CertificateSerialNumber(InputStream paramInputStream)
    throws IOException
  {
    this.serial = new SerialNumber(paramInputStream);
  }

  public CertificateSerialNumber(DerValue paramDerValue)
    throws IOException
  {
    this.serial = new SerialNumber(paramDerValue);
  }

  public String toString()
  {
    if (this.serial == null)
      return "";
    return this.serial.toString();
  }

  public void encode(OutputStream paramOutputStream)
    throws IOException
  {
    DerOutputStream localDerOutputStream = new DerOutputStream();
    this.serial.encode(localDerOutputStream);
    paramOutputStream.write(localDerOutputStream.toByteArray());
  }

  public void set(String paramString, Object paramObject)
    throws IOException
  {
    if (!(paramObject instanceof SerialNumber))
      throw new IOException("Attribute must be of type SerialNumber.");
    if (paramString.equalsIgnoreCase("number"))
      this.serial = ((SerialNumber)paramObject);
    else
      throw new IOException("Attribute name not recognized by CertAttrSet:CertificateSerialNumber.");
  }

  public Object get(String paramString)
    throws IOException
  {
    if (paramString.equalsIgnoreCase("number"))
      return this.serial;
    throw new IOException("Attribute name not recognized by CertAttrSet:CertificateSerialNumber.");
  }

  public void delete(String paramString)
    throws IOException
  {
    if (paramString.equalsIgnoreCase("number"))
      this.serial = null;
    else
      throw new IOException("Attribute name not recognized by CertAttrSet:CertificateSerialNumber.");
  }

  public Enumeration getElements()
  {
    AttributeNameEnumeration localAttributeNameEnumeration = new AttributeNameEnumeration();
    localAttributeNameEnumeration.addElement("number");
    return localAttributeNameEnumeration.elements();
  }

  public String getName()
  {
    return "serialNumber";
  }
}