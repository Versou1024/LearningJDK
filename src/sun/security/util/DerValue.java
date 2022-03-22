package sun.security.util;

import B;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.Date;

public class DerValue
{
  public static final byte TAG_UNIVERSAL = 0;
  public static final byte TAG_APPLICATION = 64;
  public static final byte TAG_CONTEXT = -128;
  public static final byte TAG_PRIVATE = -64;
  public byte tag;
  protected DerInputBuffer buffer;
  public DerInputStream data;
  private int length;
  public static final byte tag_Boolean = 1;
  public static final byte tag_Integer = 2;
  public static final byte tag_BitString = 3;
  public static final byte tag_OctetString = 4;
  public static final byte tag_Null = 5;
  public static final byte tag_ObjectId = 6;
  public static final byte tag_Enumerated = 10;
  public static final byte tag_UTF8String = 12;
  public static final byte tag_PrintableString = 19;
  public static final byte tag_T61String = 20;
  public static final byte tag_IA5String = 22;
  public static final byte tag_UtcTime = 23;
  public static final byte tag_GeneralizedTime = 24;
  public static final byte tag_GeneralString = 27;
  public static final byte tag_UniversalString = 28;
  public static final byte tag_BMPString = 30;
  public static final byte tag_Sequence = 48;
  public static final byte tag_SequenceOf = 48;
  public static final byte tag_Set = 49;
  public static final byte tag_SetOf = 49;

  public boolean isUniversal()
  {
    return ((this.tag & 0xC0) == 0);
  }

  public boolean isApplication()
  {
    return ((this.tag & 0xC0) == 64);
  }

  public boolean isContextSpecific()
  {
    return ((this.tag & 0xC0) == 128);
  }

  public boolean isContextSpecific(byte paramByte)
  {
    if (!(isContextSpecific()))
      return false;
    return ((this.tag & 0x1F) == paramByte);
  }

  boolean isPrivate()
  {
    return ((this.tag & 0xC0) == 192);
  }

  public boolean isConstructed()
  {
    return ((this.tag & 0x20) == 32);
  }

  public boolean isConstructed(byte paramByte)
  {
    if (!(isConstructed()))
      return false;
    return ((this.tag & 0x1F) == paramByte);
  }

  public DerValue(String paramString)
    throws IOException
  {
    int i = 1;
    for (int j = 0; j < paramString.length(); ++j)
      if (!(isPrintableStringChar(paramString.charAt(j))))
      {
        i = 0;
        break;
      }
    init((i != 0) ? 19 : 12, paramString);
  }

  public DerValue(byte paramByte, String paramString)
    throws IOException
  {
    init(paramByte, paramString);
  }

  public DerValue(byte paramByte, byte[] paramArrayOfByte)
  {
    this.tag = paramByte;
    this.buffer = new DerInputBuffer((byte[])(byte[])paramArrayOfByte.clone());
    this.length = paramArrayOfByte.length;
    this.data = new DerInputStream(this.buffer);
    this.data.mark(2147483647);
  }

  DerValue(DerInputBuffer paramDerInputBuffer)
    throws IOException
  {
    this.tag = (byte)paramDerInputBuffer.read();
    int i = (byte)paramDerInputBuffer.read();
    this.length = DerInputStream.getLength(i & 0xFF, paramDerInputBuffer);
    if (this.length == -1)
    {
      DerInputBuffer localDerInputBuffer = paramDerInputBuffer.dup();
      int j = localDerInputBuffer.available();
      int k = 2;
      byte[] arrayOfByte = new byte[j + k];
      arrayOfByte[0] = this.tag;
      arrayOfByte[1] = i;
      DataInputStream localDataInputStream = new DataInputStream(localDerInputBuffer);
      localDataInputStream.readFully(arrayOfByte, k, j);
      localDataInputStream.close();
      DerIndefLenConverter localDerIndefLenConverter = new DerIndefLenConverter();
      localDerInputBuffer = new DerInputBuffer(localDerIndefLenConverter.convert(arrayOfByte));
      if (this.tag != localDerInputBuffer.read())
        throw new IOException("Indefinite length encoding not supported");
      this.length = DerInputStream.getLength(localDerInputBuffer);
      this.buffer = localDerInputBuffer.dup();
      this.buffer.truncate(this.length);
      this.data = new DerInputStream(this.buffer);
      paramDerInputBuffer.skip(this.length + k);
    }
    else
    {
      this.buffer = paramDerInputBuffer.dup();
      this.buffer.truncate(this.length);
      this.data = new DerInputStream(this.buffer);
      paramDerInputBuffer.skip(this.length);
    }
  }

  public DerValue(byte[] paramArrayOfByte)
    throws IOException
  {
    init(true, new ByteArrayInputStream(paramArrayOfByte));
  }

  public DerValue(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
    throws IOException
  {
    init(true, new ByteArrayInputStream(paramArrayOfByte, paramInt1, paramInt2));
  }

  public DerValue(InputStream paramInputStream)
    throws IOException
  {
    init(false, paramInputStream);
  }

  private void init(byte paramByte, String paramString)
    throws IOException
  {
    String str = null;
    this.tag = paramByte;
    switch (paramByte)
    {
    case 19:
    case 22:
    case 27:
      str = "ASCII";
      break;
    case 20:
      str = "ISO-8859-1";
      break;
    case 30:
      str = "UnicodeBigUnmarked";
      break;
    case 12:
      str = "UTF8";
      break;
    case 13:
    case 14:
    case 15:
    case 16:
    case 17:
    case 18:
    case 21:
    case 23:
    case 24:
    case 25:
    case 26:
    case 28:
    case 29:
    default:
      throw new IllegalArgumentException("Unsupported DER string type");
    }
    byte[] arrayOfByte = paramString.getBytes(str);
    this.length = arrayOfByte.length;
    this.buffer = new DerInputBuffer(arrayOfByte);
    this.data = new DerInputStream(this.buffer);
    this.data.mark(2147483647);
  }

  private void init(boolean paramBoolean, InputStream paramInputStream)
    throws IOException
  {
    this.tag = (byte)paramInputStream.read();
    int i = (byte)paramInputStream.read();
    this.length = DerInputStream.getLength(i & 0xFF, paramInputStream);
    if (this.length == -1)
    {
      int j = paramInputStream.available();
      int k = 2;
      byte[] arrayOfByte2 = new byte[j + k];
      arrayOfByte2[0] = this.tag;
      arrayOfByte2[1] = i;
      DataInputStream localDataInputStream2 = new DataInputStream(paramInputStream);
      localDataInputStream2.readFully(arrayOfByte2, k, j);
      localDataInputStream2.close();
      DerIndefLenConverter localDerIndefLenConverter = new DerIndefLenConverter();
      paramInputStream = new ByteArrayInputStream(localDerIndefLenConverter.convert(arrayOfByte2));
      if (this.tag != paramInputStream.read())
        throw new IOException("Indefinite length encoding not supported");
      this.length = DerInputStream.getLength(paramInputStream);
    }
    if (this.length == 0)
      return;
    if ((paramBoolean) && (paramInputStream.available() != this.length))
      throw new IOException("extra data given to DerValue constructor");
    byte[] arrayOfByte1 = new byte[this.length];
    DataInputStream localDataInputStream1 = new DataInputStream(paramInputStream);
    localDataInputStream1.readFully(arrayOfByte1);
    this.buffer = new DerInputBuffer(arrayOfByte1);
    this.data = new DerInputStream(this.buffer);
  }

  public void encode(DerOutputStream paramDerOutputStream)
    throws IOException
  {
    paramDerOutputStream.write(this.tag);
    paramDerOutputStream.putLength(this.length);
    if (this.length > 0)
    {
      byte[] arrayOfByte = new byte[this.length];
      synchronized (this.buffer)
      {
        this.buffer.reset();
        if (this.buffer.read(arrayOfByte) != this.length)
          throw new IOException("short DER value read (encode)");
        paramDerOutputStream.write(arrayOfByte);
      }
    }
  }

  public final DerInputStream getData()
  {
    return this.data;
  }

  public final byte getTag()
  {
    return this.tag;
  }

  public boolean getBoolean()
    throws IOException
  {
    if (this.tag != 1)
      throw new IOException("DerValue.getBoolean, not a BOOLEAN " + this.tag);
    if (this.length != 1)
      throw new IOException("DerValue.getBoolean, invalid length " + this.length);
    return (this.buffer.read() != 0);
  }

  public ObjectIdentifier getOID()
    throws IOException
  {
    if (this.tag != 6)
      throw new IOException("DerValue.getOID, not an OID " + this.tag);
    return new ObjectIdentifier(this.buffer);
  }

  private byte[] append(byte[] paramArrayOfByte1, byte[] paramArrayOfByte2)
  {
    if (paramArrayOfByte1 == null)
      return paramArrayOfByte2;
    byte[] arrayOfByte = new byte[paramArrayOfByte1.length + paramArrayOfByte2.length];
    System.arraycopy(paramArrayOfByte1, 0, arrayOfByte, 0, paramArrayOfByte1.length);
    System.arraycopy(paramArrayOfByte2, 0, arrayOfByte, paramArrayOfByte1.length, paramArrayOfByte2.length);
    return arrayOfByte;
  }

  public byte[] getOctetString()
    throws IOException
  {
    if ((this.tag != 4) && (!(isConstructed(4))))
      throw new IOException("DerValue.getOctetString, not an Octet String: " + this.tag);
    byte[] arrayOfByte = new byte[this.length];
    if (this.buffer.read(arrayOfByte) != this.length)
      throw new IOException("short read on DerValue buffer");
    if (isConstructed())
    {
      DerInputStream localDerInputStream = new DerInputStream(arrayOfByte);
      for (arrayOfByte = null; localDerInputStream.available() != 0; arrayOfByte = append(arrayOfByte, localDerInputStream.getOctetString()));
    }
    return arrayOfByte;
  }

  public int getInteger()
    throws IOException
  {
    if (this.tag != 2)
      throw new IOException("DerValue.getInteger, not an int " + this.tag);
    return this.buffer.getInteger(this.data.available());
  }

  public BigInteger getBigInteger()
    throws IOException
  {
    if (this.tag != 2)
      throw new IOException("DerValue.getBigInteger, not an int " + this.tag);
    return this.buffer.getBigInteger(this.data.available(), false);
  }

  public BigInteger getPositiveBigInteger()
    throws IOException
  {
    if (this.tag != 2)
      throw new IOException("DerValue.getBigInteger, not an int " + this.tag);
    return this.buffer.getBigInteger(this.data.available(), true);
  }

  public int getEnumerated()
    throws IOException
  {
    if (this.tag != 10)
      throw new IOException("DerValue.getEnumerated, incorrect tag: " + this.tag);
    return this.buffer.getInteger(this.data.available());
  }

  public byte[] getBitString()
    throws IOException
  {
    if (this.tag != 3)
      throw new IOException("DerValue.getBitString, not a bit string " + this.tag);
    return this.buffer.getBitString();
  }

  public BitArray getUnalignedBitString()
    throws IOException
  {
    if (this.tag != 3)
      throw new IOException("DerValue.getBitString, not a bit string " + this.tag);
    return this.buffer.getUnalignedBitString();
  }

  public String getAsString()
    throws IOException
  {
    if (this.tag == 12)
      return getUTF8String();
    if (this.tag == 19)
      return getPrintableString();
    if (this.tag == 20)
      return getT61String();
    if (this.tag == 22)
      return getIA5String();
    if (this.tag == 30)
      return getBMPString();
    if (this.tag == 27)
      return getGeneralString();
    return null;
  }

  public byte[] getBitString(boolean paramBoolean)
    throws IOException
  {
    if ((!(paramBoolean)) && (this.tag != 3))
      throw new IOException("DerValue.getBitString, not a bit string " + this.tag);
    return this.buffer.getBitString();
  }

  public BitArray getUnalignedBitString(boolean paramBoolean)
    throws IOException
  {
    if ((!(paramBoolean)) && (this.tag != 3))
      throw new IOException("DerValue.getBitString, not a bit string " + this.tag);
    return this.buffer.getUnalignedBitString();
  }

  public byte[] getDataBytes()
    throws IOException
  {
    byte[] arrayOfByte = new byte[this.length];
    synchronized (this.data)
    {
      this.data.reset();
      this.data.getBytes(arrayOfByte);
    }
    return arrayOfByte;
  }

  public String getPrintableString()
    throws IOException
  {
    if (this.tag != 19)
      throw new IOException("DerValue.getPrintableString, not a string " + this.tag);
    return new String(getDataBytes(), "ASCII");
  }

  public String getT61String()
    throws IOException
  {
    if (this.tag != 20)
      throw new IOException("DerValue.getT61String, not T61 " + this.tag);
    return new String(getDataBytes(), "ISO-8859-1");
  }

  public String getIA5String()
    throws IOException
  {
    if (this.tag != 22)
      throw new IOException("DerValue.getIA5String, not IA5 " + this.tag);
    return new String(getDataBytes(), "ASCII");
  }

  public String getBMPString()
    throws IOException
  {
    if (this.tag != 30)
      throw new IOException("DerValue.getBMPString, not BMP " + this.tag);
    return new String(getDataBytes(), "UnicodeBigUnmarked");
  }

  public String getUTF8String()
    throws IOException
  {
    if (this.tag != 12)
      throw new IOException("DerValue.getUTF8String, not UTF-8 " + this.tag);
    return new String(getDataBytes(), "UTF8");
  }

  public String getGeneralString()
    throws IOException
  {
    if (this.tag != 27)
      throw new IOException("DerValue.getGeneralString, not GeneralString " + this.tag);
    return new String(getDataBytes(), "ASCII");
  }

  public Date getUTCTime()
    throws IOException
  {
    if (this.tag != 23)
      throw new IOException("DerValue.getUTCTime, not a UtcTime: " + this.tag);
    return this.buffer.getUTCTime(this.data.available());
  }

  public Date getGeneralizedTime()
    throws IOException
  {
    if (this.tag != 24)
      throw new IOException("DerValue.getGeneralizedTime, not a GeneralizedTime: " + this.tag);
    return this.buffer.getGeneralizedTime(this.data.available());
  }

  public boolean equals(Object paramObject)
  {
    if (paramObject instanceof DerValue)
      return equals((DerValue)paramObject);
    return false;
  }

  public boolean equals(DerValue paramDerValue)
  {
    this.data.reset();
    paramDerValue.data.reset();
    if (this == paramDerValue)
      return true;
    if (this.tag != paramDerValue.tag)
      return false;
    return this.buffer.equals(paramDerValue.buffer);
  }

  public String toString()
  {
    String str;
    try
    {
      str = getAsString();
      if (str != null)
        return "\"" + str + "\"";
      if (this.tag == 5)
        return "[DerValue, null]";
      if (this.tag == 6)
        return "OID." + getOID();
      return "[DerValue, tag = " + this.tag + ", length = " + this.length + "]";
    }
    catch (IOException localIOException)
    {
      throw new IllegalArgumentException("misformatted DER value");
    }
  }

  public byte[] toByteArray()
    throws IOException
  {
    DerOutputStream localDerOutputStream = new DerOutputStream();
    encode(localDerOutputStream);
    this.data.reset();
    return localDerOutputStream.toByteArray();
  }

  public DerInputStream toDerInputStream()
    throws IOException
  {
    if ((this.tag == 48) || (this.tag == 49))
      return new DerInputStream(this.buffer);
    throw new IOException("toDerInputStream rejects tag type " + this.tag);
  }

  public int length()
  {
    return this.length;
  }

  public static boolean isPrintableStringChar(char paramChar)
  {
    if (((paramChar >= 'a') && (paramChar <= 'z')) || ((paramChar >= 'A') && (paramChar <= 'Z')) || ((paramChar >= '0') && (paramChar <= '9')))
      return true;
    switch (paramChar)
    {
    case ' ':
    case '\'':
    case '(':
    case ')':
    case '+':
    case ',':
    case '-':
    case '.':
    case '/':
    case ':':
    case '=':
    case '?':
      return true;
    case '!':
    case '"':
    case '#':
    case '$':
    case '%':
    case '&':
    case '*':
    case '0':
    case '1':
    case '2':
    case '3':
    case '4':
    case '5':
    case '6':
    case '7':
    case '8':
    case '9':
    case ';':
    case '<':
    case '>':
    }
    return false;
  }

  public static byte createTag(byte paramByte1, boolean paramBoolean, byte paramByte2)
  {
    int i = (byte)(paramByte1 | paramByte2);
    if (paramBoolean)
      i = (byte)(i | 0x20);
    return i;
  }

  public void resetTag(byte paramByte)
  {
    this.tag = paramByte;
  }

  public int hashCode()
  {
    return toString().hashCode();
  }
}