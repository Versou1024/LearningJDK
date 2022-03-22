package sun.tools.jar;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.CRC32;

final class CRC32OutputStream extends OutputStream
{
  CRC32 crc;
  int n = 0;

  CRC32OutputStream(CRC32 paramCRC32)
  {
    this.crc = paramCRC32;
  }

  public void write(int paramInt)
    throws IOException
  {
    this.crc.update(paramInt);
    this.n += 1;
  }

  public void write(byte[] paramArrayOfByte)
    throws IOException
  {
    this.crc.update(paramArrayOfByte, 0, paramArrayOfByte.length);
    this.n += paramArrayOfByte.length;
  }

  public void write(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
    throws IOException
  {
    this.crc.update(paramArrayOfByte, paramInt1, paramInt2);
    this.n += paramInt2 - paramInt1;
  }
}