package sun.awt.image;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;

public class ByteArrayImageSource extends InputStreamImageSource
{
  byte[] imagedata;
  int imageoffset;
  int imagelength;

  public ByteArrayImageSource(byte[] paramArrayOfByte)
  {
    this(paramArrayOfByte, 0, paramArrayOfByte.length);
  }

  public ByteArrayImageSource(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
  {
    this.imagedata = paramArrayOfByte;
    this.imageoffset = paramInt1;
    this.imagelength = paramInt2;
  }

  final boolean checkSecurity(Object paramObject, boolean paramBoolean)
  {
    return true;
  }

  protected ImageDecoder getDecoder()
  {
    BufferedInputStream localBufferedInputStream = new BufferedInputStream(new ByteArrayInputStream(this.imagedata, this.imageoffset, this.imagelength));
    return getDecoder(localBufferedInputStream);
  }
}