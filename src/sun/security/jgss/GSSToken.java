package sun.security.jgss;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

public abstract class GSSToken
{
  public static final void writeLittleEndian(int paramInt, byte[] paramArrayOfByte)
  {
    writeLittleEndian(paramInt, paramArrayOfByte, 0);
  }

  public static final void writeLittleEndian(int paramInt1, byte[] paramArrayOfByte, int paramInt2)
  {
    paramArrayOfByte[(paramInt2++)] = (byte)paramInt1;
    paramArrayOfByte[(paramInt2++)] = (byte)(paramInt1 >>> 8);
    paramArrayOfByte[(paramInt2++)] = (byte)(paramInt1 >>> 16);
    paramArrayOfByte[(paramInt2++)] = (byte)(paramInt1 >>> 24);
  }

  public static final void writeBigEndian(int paramInt, byte[] paramArrayOfByte)
  {
    writeBigEndian(paramInt, paramArrayOfByte, 0);
  }

  public static final void writeBigEndian(int paramInt1, byte[] paramArrayOfByte, int paramInt2)
  {
    paramArrayOfByte[(paramInt2++)] = (byte)(paramInt1 >>> 24);
    paramArrayOfByte[(paramInt2++)] = (byte)(paramInt1 >>> 16);
    paramArrayOfByte[(paramInt2++)] = (byte)(paramInt1 >>> 8);
    paramArrayOfByte[(paramInt2++)] = (byte)paramInt1;
  }

  public static final int readLittleEndian(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
  {
    int i = 0;
    int j = 0;
    while (paramInt2 > 0)
    {
      i += ((paramArrayOfByte[paramInt1] & 0xFF) << j);
      j += 8;
      ++paramInt1;
      --paramInt2;
    }
    return i;
  }

  public static final int readBigEndian(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
  {
    int i = 0;
    int j = (paramInt2 - 1) * 8;
    while (paramInt2 > 0)
    {
      i += ((paramArrayOfByte[paramInt1] & 0xFF) << j);
      j -= 8;
      ++paramInt1;
      --paramInt2;
    }
    return i;
  }

  public static final void writeInt(int paramInt, OutputStream paramOutputStream)
    throws IOException
  {
    paramOutputStream.write(paramInt >>> 8);
    paramOutputStream.write(paramInt);
  }

  public static final int writeInt(int paramInt1, byte[] paramArrayOfByte, int paramInt2)
  {
    paramArrayOfByte[(paramInt2++)] = (byte)(paramInt1 >>> 8);
    paramArrayOfByte[(paramInt2++)] = (byte)paramInt1;
    return paramInt2;
  }

  public static final int readInt(InputStream paramInputStream)
    throws IOException
  {
    return ((0xFF & paramInputStream.read()) << 8 | 0xFF & paramInputStream.read());
  }

  public static final int readInt(byte[] paramArrayOfByte, int paramInt)
  {
    return ((0xFF & paramArrayOfByte[paramInt]) << 8 | 0xFF & paramArrayOfByte[(paramInt + 1)]);
  }

  public static final void readFully(InputStream paramInputStream, byte[] paramArrayOfByte)
    throws IOException
  {
    readFully(paramInputStream, paramArrayOfByte, 0, paramArrayOfByte.length);
  }

  public static final void readFully(InputStream paramInputStream, byte[] paramArrayOfByte, int paramInt1, int paramInt2)
    throws IOException
  {
    while (paramInt2 > 0)
    {
      int i = paramInputStream.read(paramArrayOfByte, paramInt1, paramInt2);
      if (i == -1)
        throw new EOFException("Cannot read all " + paramInt2 + " bytes needed to form this token!");
      paramInt1 += i;
      paramInt2 -= i;
    }
  }

  public static final void debug(String paramString)
  {
    System.err.print(paramString);
  }

  public static final String getHexBytes(byte[] paramArrayOfByte)
  {
    return getHexBytes(paramArrayOfByte, 0, paramArrayOfByte.length);
  }

  public static final String getHexBytes(byte[] paramArrayOfByte, int paramInt)
  {
    return getHexBytes(paramArrayOfByte, 0, paramInt);
  }

  public static final String getHexBytes(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
  {
    StringBuffer localStringBuffer = new StringBuffer();
    for (int i = paramInt1; i < paramInt1 + paramInt2; ++i)
    {
      int j = paramArrayOfByte[i] >> 4 & 0xF;
      int k = paramArrayOfByte[i] & 0xF;
      localStringBuffer.append(Integer.toHexString(j));
      localStringBuffer.append(Integer.toHexString(k));
      localStringBuffer.append(' ');
    }
    return localStringBuffer.toString();
  }
}