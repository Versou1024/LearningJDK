package sun.text.normalizer;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public final class ICUBinary
{
  private static final byte MAGIC1 = -38;
  private static final byte MAGIC2 = 39;
  private static final byte BIG_ENDIAN_ = 1;
  private static final byte CHAR_SET_ = 0;
  private static final byte CHAR_SIZE_ = 2;
  private static final String MAGIC_NUMBER_AUTHENTICATION_FAILED_ = "ICU data file error: Not an ICU data file";
  private static final String HEADER_AUTHENTICATION_FAILED_ = "ICU data file error: Header authentication failed, please check if you have a valid ICU data file";

  public static final byte[] readHeader(InputStream paramInputStream, byte[] paramArrayOfByte, Authenticate paramAuthenticate)
    throws IOException
  {
    DataInputStream localDataInputStream = new DataInputStream(paramInputStream);
    int i = localDataInputStream.readChar();
    int j = 2;
    int k = localDataInputStream.readByte();
    ++j;
    int l = localDataInputStream.readByte();
    ++j;
    if ((k != -38) || (l != 39))
      throw new IOException("ICU data file error: Not an ICU data file");
    localDataInputStream.readChar();
    j += 2;
    localDataInputStream.readChar();
    j += 2;
    int i1 = localDataInputStream.readByte();
    ++j;
    int i2 = localDataInputStream.readByte();
    ++j;
    int i3 = localDataInputStream.readByte();
    ++j;
    localDataInputStream.readByte();
    ++j;
    byte[] arrayOfByte1 = new byte[4];
    localDataInputStream.readFully(arrayOfByte1);
    j += 4;
    byte[] arrayOfByte2 = new byte[4];
    localDataInputStream.readFully(arrayOfByte2);
    j += 4;
    byte[] arrayOfByte3 = new byte[4];
    localDataInputStream.readFully(arrayOfByte3);
    if (i < (j += 4))
      throw new IOException("Internal Error: Header size error");
    localDataInputStream.skipBytes(i - j);
    if ((i1 != 1) || (i2 != 0) || (i3 != 2) || (!(Arrays.equals(paramArrayOfByte, arrayOfByte1))) || ((paramAuthenticate != null) && (!(paramAuthenticate.isDataVersionAcceptable(arrayOfByte2)))))
      throw new IOException("ICU data file error: Header authentication failed, please check if you have a valid ICU data file");
    return arrayOfByte3;
  }

  public static abstract interface Authenticate
  {
    public abstract boolean isDataVersionAcceptable(byte[] paramArrayOfByte);
  }
}