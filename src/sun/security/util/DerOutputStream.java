package sun.security.util;

import [B;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.TimeZone;

public class DerOutputStream extends ByteArrayOutputStream
  implements DerEncoder
{
  private static ByteArrayLexOrder lexOrder = new ByteArrayLexOrder();
  private static ByteArrayTagOrder tagOrder = new ByteArrayTagOrder();

  public DerOutputStream(int paramInt)
  {
    super(paramInt);
  }

  public DerOutputStream()
  {
  }

  public void write(byte paramByte, byte[] paramArrayOfByte)
    throws IOException
  {
    write(paramByte);
    putLength(paramArrayOfByte.length);
    write(paramArrayOfByte, 0, paramArrayOfByte.length);
  }

  public void write(byte paramByte, DerOutputStream paramDerOutputStream)
    throws IOException
  {
    write(paramByte);
    putLength(paramDerOutputStream.count);
    write(paramDerOutputStream.buf, 0, paramDerOutputStream.count);
  }

  public void writeImplicit(byte paramByte, DerOutputStream paramDerOutputStream)
    throws IOException
  {
    write(paramByte);
    write(paramDerOutputStream.buf, 1, paramDerOutputStream.count - 1);
  }

  public void putDerValue(DerValue paramDerValue)
    throws IOException
  {
    paramDerValue.encode(this);
  }

  public void putBoolean(boolean paramBoolean)
    throws IOException
  {
    write(1);
    putLength(1);
    if (paramBoolean)
      write(255);
    else
      write(0);
  }

  public void putEnumerated(int paramInt)
    throws IOException
  {
    write(10);
    putIntegerContents(paramInt);
  }

  public void putInteger(BigInteger paramBigInteger)
    throws IOException
  {
    write(2);
    byte[] arrayOfByte = paramBigInteger.toByteArray();
    putLength(arrayOfByte.length);
    write(arrayOfByte, 0, arrayOfByte.length);
  }

  public void putInteger(Integer paramInteger)
    throws IOException
  {
    putInteger(paramInteger.intValue());
  }

  public void putInteger(int paramInt)
    throws IOException
  {
    write(2);
    putIntegerContents(paramInt);
  }

  private void putIntegerContents(int paramInt)
    throws IOException
  {
    byte[] arrayOfByte = new byte[4];
    int i = 0;
    arrayOfByte[3] = (byte)(paramInt & 0xFF);
    arrayOfByte[2] = (byte)((paramInt & 0xFF00) >>> 8);
    arrayOfByte[1] = (byte)((paramInt & 0xFF0000) >>> 16);
    arrayOfByte[0] = (byte)((paramInt & 0xFF000000) >>> 24);
    if (arrayOfByte[0] == 255)
      for (j = 0; (j < 3) && (arrayOfByte[j] == 255) && ((arrayOfByte[(j + 1)] & 0x80) == 128); ++j)
        ++i;
    else if (arrayOfByte[0] == 0)
      for (j = 0; (j < 3) && (arrayOfByte[j] == 0) && ((arrayOfByte[(j + 1)] & 0x80) == 0); ++j)
        ++i;
    putLength(4 - i);
    for (int j = i; j < 4; ++j)
      write(arrayOfByte[j]);
  }

  public void putBitString(byte[] paramArrayOfByte)
    throws IOException
  {
    write(3);
    putLength(paramArrayOfByte.length + 1);
    write(0);
    write(paramArrayOfByte);
  }

  public void putUnalignedBitString(BitArray paramBitArray)
    throws IOException
  {
    byte[] arrayOfByte = paramBitArray.toByteArray();
    write(3);
    putLength(arrayOfByte.length + 1);
    write(arrayOfByte.length * 8 - paramBitArray.length());
    write(arrayOfByte);
  }

  public void putOctetString(byte[] paramArrayOfByte)
    throws IOException
  {
    write(4, paramArrayOfByte);
  }

  public void putNull()
    throws IOException
  {
    write(5);
    putLength(0);
  }

  public void putOID(ObjectIdentifier paramObjectIdentifier)
    throws IOException
  {
    paramObjectIdentifier.encode(this);
  }

  public void putSequence(DerValue[] paramArrayOfDerValue)
    throws IOException
  {
    DerOutputStream localDerOutputStream = new DerOutputStream();
    for (int i = 0; i < paramArrayOfDerValue.length; ++i)
      paramArrayOfDerValue[i].encode(localDerOutputStream);
    write(48, localDerOutputStream);
  }

  public void putSet(DerValue[] paramArrayOfDerValue)
    throws IOException
  {
    DerOutputStream localDerOutputStream = new DerOutputStream();
    for (int i = 0; i < paramArrayOfDerValue.length; ++i)
      paramArrayOfDerValue[i].encode(localDerOutputStream);
    write(49, localDerOutputStream);
  }

  public void putOrderedSetOf(byte paramByte, DerEncoder[] paramArrayOfDerEncoder)
    throws IOException
  {
    putOrderedSet(paramByte, paramArrayOfDerEncoder, lexOrder);
  }

  public void putOrderedSet(byte paramByte, DerEncoder[] paramArrayOfDerEncoder)
    throws IOException
  {
    putOrderedSet(paramByte, paramArrayOfDerEncoder, tagOrder);
  }

  private void putOrderedSet(byte paramByte, DerEncoder[] paramArrayOfDerEncoder, Comparator paramComparator)
    throws IOException
  {
    DerOutputStream[] arrayOfDerOutputStream = new DerOutputStream[paramArrayOfDerEncoder.length];
    for (int i = 0; i < paramArrayOfDerEncoder.length; ++i)
    {
      arrayOfDerOutputStream[i] = new DerOutputStream();
      paramArrayOfDerEncoder[i].derEncode(arrayOfDerOutputStream[i]);
    }
    [B[] arrayOf[B = new byte[arrayOfDerOutputStream.length][];
    for (int j = 0; j < arrayOfDerOutputStream.length; ++j)
      arrayOf[B[j] = arrayOfDerOutputStream[j].toByteArray();
    Arrays.sort(arrayOf[B, paramComparator);
    DerOutputStream localDerOutputStream = new DerOutputStream();
    for (int k = 0; k < arrayOfDerOutputStream.length; ++k)
      localDerOutputStream.write(arrayOf[B[k]);
    write(paramByte, localDerOutputStream);
  }

  public void putUTF8String(String paramString)
    throws IOException
  {
    writeString(paramString, 12, "UTF8");
  }

  public void putPrintableString(String paramString)
    throws IOException
  {
    writeString(paramString, 19, "ASCII");
  }

  public void putT61String(String paramString)
    throws IOException
  {
    writeString(paramString, 20, "ISO-8859-1");
  }

  public void putIA5String(String paramString)
    throws IOException
  {
    writeString(paramString, 22, "ASCII");
  }

  public void putBMPString(String paramString)
    throws IOException
  {
    writeString(paramString, 30, "UnicodeBigUnmarked");
  }

  public void putGeneralString(String paramString)
    throws IOException
  {
    writeString(paramString, 27, "ASCII");
  }

  private void writeString(String paramString1, byte paramByte, String paramString2)
    throws IOException
  {
    byte[] arrayOfByte = paramString1.getBytes(paramString2);
    write(paramByte);
    putLength(arrayOfByte.length);
    write(arrayOfByte);
  }

  public void putUTCTime(Date paramDate)
    throws IOException
  {
    putTime(paramDate, 23);
  }

  public void putGeneralizedTime(Date paramDate)
    throws IOException
  {
    putTime(paramDate, 24);
  }

  private void putTime(Date paramDate, byte paramByte)
    throws IOException
  {
    TimeZone localTimeZone = TimeZone.getTimeZone("GMT");
    String str = null;
    if (paramByte == 23)
    {
      str = "yyMMddHHmmss'Z'";
    }
    else
    {
      paramByte = 24;
      str = "yyyyMMddHHmmss'Z'";
    }
    SimpleDateFormat localSimpleDateFormat = new SimpleDateFormat(str);
    localSimpleDateFormat.setTimeZone(localTimeZone);
    byte[] arrayOfByte = localSimpleDateFormat.format(paramDate).getBytes("ISO-8859-1");
    write(paramByte);
    putLength(arrayOfByte.length);
    write(arrayOfByte);
  }

  public void putLength(int paramInt)
    throws IOException
  {
    if (paramInt < 128)
    {
      write((byte)paramInt);
    }
    else if (paramInt < 256)
    {
      write(-127);
      write((byte)paramInt);
    }
    else if (paramInt < 65536)
    {
      write(-126);
      write((byte)(paramInt >> 8));
      write((byte)paramInt);
    }
    else if (paramInt < 16777216)
    {
      write(-125);
      write((byte)(paramInt >> 16));
      write((byte)(paramInt >> 8));
      write((byte)paramInt);
    }
    else
    {
      write(-124);
      write((byte)(paramInt >> 24));
      write((byte)(paramInt >> 16));
      write((byte)(paramInt >> 8));
      write((byte)paramInt);
    }
  }

  public void putTag(byte paramByte1, boolean paramBoolean, byte paramByte2)
  {
    int i = (byte)(paramByte1 | paramByte2);
    if (paramBoolean)
      i = (byte)(i | 0x20);
    write(i);
  }

  public void derEncode(OutputStream paramOutputStream)
    throws IOException
  {
    paramOutputStream.write(toByteArray());
  }
}