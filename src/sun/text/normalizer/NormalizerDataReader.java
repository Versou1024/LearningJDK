package sun.text.normalizer;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

final class NormalizerDataReader
  implements ICUBinary.Authenticate
{
  private DataInputStream dataInputStream;
  private byte[] unicodeVersion = ICUBinary.readHeader(paramInputStream, DATA_FORMAT_ID, this);
  private static final byte[] DATA_FORMAT_ID = { 78, 111, 114, 109 };
  private static final byte[] DATA_FORMAT_VERSION = { 2, 2, 5, 2 };

  protected NormalizerDataReader(InputStream paramInputStream)
    throws IOException
  {
    this.dataInputStream = new DataInputStream(paramInputStream);
  }

  protected int[] readIndexes(int paramInt)
    throws IOException
  {
    int[] arrayOfInt = new int[paramInt];
    for (int i = 0; i < paramInt; ++i)
      arrayOfInt[i] = this.dataInputStream.readInt();
    return arrayOfInt;
  }

  protected void read(byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, byte[] paramArrayOfByte3, char[] paramArrayOfChar1, char[] paramArrayOfChar2)
    throws IOException
  {
    this.dataInputStream.read(paramArrayOfByte1);
    for (int i = 0; i < paramArrayOfChar1.length; ++i)
      paramArrayOfChar1[i] = this.dataInputStream.readChar();
    for (i = 0; i < paramArrayOfChar2.length; ++i)
      paramArrayOfChar2[i] = this.dataInputStream.readChar();
    this.dataInputStream.read(paramArrayOfByte2);
    this.dataInputStream.read(paramArrayOfByte3);
  }

  public byte[] getDataFormatVersion()
  {
    return DATA_FORMAT_VERSION;
  }

  public boolean isDataVersionAcceptable(byte[] paramArrayOfByte)
  {
    return ((paramArrayOfByte[0] == DATA_FORMAT_VERSION[0]) && (paramArrayOfByte[2] == DATA_FORMAT_VERSION[2]) && (paramArrayOfByte[3] == DATA_FORMAT_VERSION[3]));
  }

  public byte[] getUnicodeVersion()
  {
    return this.unicodeVersion;
  }
}