package sun.rmi.log;

import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;

public class LogOutputStream extends OutputStream
{
  private RandomAccessFile raf;

  public LogOutputStream(RandomAccessFile paramRandomAccessFile)
    throws IOException
  {
    this.raf = paramRandomAccessFile;
  }

  public void write(int paramInt)
    throws IOException
  {
    this.raf.write(paramInt);
  }

  public void write(byte[] paramArrayOfByte)
    throws IOException
  {
    this.raf.write(paramArrayOfByte);
  }

  public void write(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
    throws IOException
  {
    this.raf.write(paramArrayOfByte, paramInt1, paramInt2);
  }

  public final void close()
    throws IOException
  {
  }
}