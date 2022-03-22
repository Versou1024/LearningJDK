package sun.security.x509;

import Z;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import sun.security.util.BitArray;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class KeyUsageExtension extends Extension
  implements CertAttrSet
{
  public static final String IDENT = "x509.info.extensions.KeyUsage";
  public static final String NAME = "KeyUsage";
  public static final String DIGITAL_SIGNATURE = "digital_signature";
  public static final String NON_REPUDIATION = "non_repudiation";
  public static final String KEY_ENCIPHERMENT = "key_encipherment";
  public static final String DATA_ENCIPHERMENT = "data_encipherment";
  public static final String KEY_AGREEMENT = "key_agreement";
  public static final String KEY_CERTSIGN = "key_certsign";
  public static final String CRL_SIGN = "crl_sign";
  public static final String ENCIPHER_ONLY = "encipher_only";
  public static final String DECIPHER_ONLY = "decipher_only";
  private boolean[] bitString;

  private void encodeThis()
    throws IOException
  {
    DerOutputStream localDerOutputStream = new DerOutputStream();
    localDerOutputStream.putUnalignedBitString(new BitArray(this.bitString));
    this.extensionValue = localDerOutputStream.toByteArray();
  }

  private boolean isSet(int paramInt)
  {
    return this.bitString[paramInt];
  }

  private void set(int paramInt, boolean paramBoolean)
  {
    if (paramInt >= this.bitString.length)
    {
      boolean[] arrayOfBoolean = new boolean[paramInt + 1];
      System.arraycopy(this.bitString, 0, arrayOfBoolean, 0, this.bitString.length);
      this.bitString = arrayOfBoolean;
    }
    this.bitString[paramInt] = paramBoolean;
  }

  public KeyUsageExtension(byte[] paramArrayOfByte)
    throws IOException
  {
    this.bitString = new BitArray(paramArrayOfByte.length * 8, paramArrayOfByte).toBooleanArray();
    this.extensionId = PKIXExtensions.KeyUsage_Id;
    this.critical = true;
    encodeThis();
  }

  public KeyUsageExtension(boolean[] paramArrayOfBoolean)
    throws IOException
  {
    this.bitString = paramArrayOfBoolean;
    this.extensionId = PKIXExtensions.KeyUsage_Id;
    this.critical = true;
    encodeThis();
  }

  public KeyUsageExtension(BitArray paramBitArray)
    throws IOException
  {
    this.bitString = paramBitArray.toBooleanArray();
    this.extensionId = PKIXExtensions.KeyUsage_Id;
    this.critical = true;
    encodeThis();
  }

  public KeyUsageExtension(Boolean paramBoolean, Object paramObject)
    throws IOException
  {
    this.extensionId = PKIXExtensions.KeyUsage_Id;
    this.critical = paramBoolean.booleanValue();
    byte[] arrayOfByte = (byte[])(byte[])paramObject;
    if (arrayOfByte[0] == 4)
      this.extensionValue = new DerValue(arrayOfByte).getOctetString();
    else
      this.extensionValue = arrayOfByte;
    DerValue localDerValue = new DerValue(this.extensionValue);
    this.bitString = localDerValue.getUnalignedBitString().toBooleanArray();
  }

  public KeyUsageExtension()
  {
    this.extensionId = PKIXExtensions.KeyUsage_Id;
    this.critical = true;
    this.bitString = new boolean[0];
  }

  public void set(String paramString, Object paramObject)
    throws IOException
  {
    if (!(paramObject instanceof Boolean))
      throw new IOException("Attribute must be of type Boolean.");
    boolean bool = ((Boolean)paramObject).booleanValue();
    if (paramString.equalsIgnoreCase("digital_signature"))
      set(0, bool);
    else if (paramString.equalsIgnoreCase("non_repudiation"))
      set(1, bool);
    else if (paramString.equalsIgnoreCase("key_encipherment"))
      set(2, bool);
    else if (paramString.equalsIgnoreCase("data_encipherment"))
      set(3, bool);
    else if (paramString.equalsIgnoreCase("key_agreement"))
      set(4, bool);
    else if (paramString.equalsIgnoreCase("key_certsign"))
      set(5, bool);
    else if (paramString.equalsIgnoreCase("crl_sign"))
      set(6, bool);
    else if (paramString.equalsIgnoreCase("encipher_only"))
      set(7, bool);
    else if (paramString.equalsIgnoreCase("decipher_only"))
      set(8, bool);
    else
      throw new IOException("Attribute name not recognized by CertAttrSet:KeyUsage.");
    encodeThis();
  }

  public Object get(String paramString)
    throws IOException
  {
    if (paramString.equalsIgnoreCase("digital_signature"))
      return Boolean.valueOf(isSet(0));
    if (paramString.equalsIgnoreCase("non_repudiation"))
      return Boolean.valueOf(isSet(1));
    if (paramString.equalsIgnoreCase("key_encipherment"))
      return Boolean.valueOf(isSet(2));
    if (paramString.equalsIgnoreCase("data_encipherment"))
      return Boolean.valueOf(isSet(3));
    if (paramString.equalsIgnoreCase("key_agreement"))
      return Boolean.valueOf(isSet(4));
    if (paramString.equalsIgnoreCase("key_certsign"))
      return Boolean.valueOf(isSet(5));
    if (paramString.equalsIgnoreCase("crl_sign"))
      return Boolean.valueOf(isSet(6));
    if (paramString.equalsIgnoreCase("encipher_only"))
      return Boolean.valueOf(isSet(7));
    if (paramString.equalsIgnoreCase("decipher_only"))
      return Boolean.valueOf(isSet(8));
    throw new IOException("Attribute name not recognized by CertAttrSet:KeyUsage.");
  }

  public void delete(String paramString)
    throws IOException
  {
    if (paramString.equalsIgnoreCase("digital_signature"))
      set(0, false);
    else if (paramString.equalsIgnoreCase("non_repudiation"))
      set(1, false);
    else if (paramString.equalsIgnoreCase("key_encipherment"))
      set(2, false);
    else if (paramString.equalsIgnoreCase("data_encipherment"))
      set(3, false);
    else if (paramString.equalsIgnoreCase("key_agreement"))
      set(4, false);
    else if (paramString.equalsIgnoreCase("key_certsign"))
      set(5, false);
    else if (paramString.equalsIgnoreCase("crl_sign"))
      set(6, false);
    else if (paramString.equalsIgnoreCase("encipher_only"))
      set(7, false);
    else if (paramString.equalsIgnoreCase("decipher_only"))
      set(8, false);
    else
      throw new IOException("Attribute name not recognized by CertAttrSet:KeyUsage.");
    encodeThis();
  }

  public String toString()
  {
    String str = super.toString() + "KeyUsage [\n";
    try
    {
      if (isSet(0))
        str = str + "  DigitalSignature\n";
      if (isSet(1))
        str = str + "  Non_repudiation\n";
      if (isSet(2))
        str = str + "  Key_Encipherment\n";
      if (isSet(3))
        str = str + "  Data_Encipherment\n";
      if (isSet(4))
        str = str + "  Key_Agreement\n";
      if (isSet(5))
        str = str + "  Key_CertSign\n";
      if (isSet(6))
        str = str + "  Crl_Sign\n";
      if (isSet(7))
        str = str + "  Encipher_Only\n";
      if (isSet(8))
        str = str + "  Decipher_Only\n";
    }
    catch (ArrayIndexOutOfBoundsException localArrayIndexOutOfBoundsException)
    {
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
      this.extensionId = PKIXExtensions.KeyUsage_Id;
      this.critical = true;
      encodeThis();
    }
    super.encode(localDerOutputStream);
    paramOutputStream.write(localDerOutputStream.toByteArray());
  }

  public Enumeration getElements()
  {
    AttributeNameEnumeration localAttributeNameEnumeration = new AttributeNameEnumeration();
    localAttributeNameEnumeration.addElement("digital_signature");
    localAttributeNameEnumeration.addElement("non_repudiation");
    localAttributeNameEnumeration.addElement("key_encipherment");
    localAttributeNameEnumeration.addElement("data_encipherment");
    localAttributeNameEnumeration.addElement("key_agreement");
    localAttributeNameEnumeration.addElement("key_certsign");
    localAttributeNameEnumeration.addElement("crl_sign");
    localAttributeNameEnumeration.addElement("encipher_only");
    localAttributeNameEnumeration.addElement("decipher_only");
    return localAttributeNameEnumeration.elements();
  }

  public boolean[] getBits()
  {
    return ((boolean[])(boolean[])this.bitString.clone());
  }

  public String getName()
  {
    return "KeyUsage";
  }
}