package sun.awt.image;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

class PNGFilterInputStream extends FilterInputStream
{
  PNGImageDecoder owner;
  public InputStream underlyingInputStream = this.in;

  public PNGFilterInputStream(PNGImageDecoder paramPNGImageDecoder, InputStream paramInputStream)
  {
    super(paramInputStream);
    this.owner = paramPNGImageDecoder;
  }

  public int available()
    throws IOException
  {
    return (this.owner.limit - this.owner.pos + this.in.available());
  }

  public boolean markSupported()
  {
    return false;
  }

  public int read()
    throws IOException
  {
    if ((this.owner.chunkLength <= 0) && (!(this.owner.getData())))
      return -1;
    this.owner.chunkLength -= 1;
    return (this.owner.inbuf[(this.owner.chunkStart++)] & 0xFF);
  }

  public int read(byte[] paramArrayOfByte)
    throws IOException
  {
    return read(paramArrayOfByte, 0, paramArrayOfByte.length);
  }

  public int read(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
    throws IOException
  {
    if ((this.owner.chunkLength <= 0) && (!(this.owner.getData())))
      return -1;
    if (this.owner.chunkLength < paramInt2)
      paramInt2 = this.owner.chunkLength;
    System.arraycopy(this.owner.inbuf, this.owner.chunkStart, paramArrayOfByte, paramInt1, paramInt2);
    this.owner.chunkLength -= paramInt2;
    this.owner.chunkStart += paramInt2;
    return paramInt2;
  }

  public long skip(long paramLong)
    throws IOException
  {
    for (int i = 0; (i < paramLong) && (read() >= 0); ++i);
    return i;
  }
}