package sun.awt.image;

import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;

class GifFrame
{
  private static final boolean verbose = 0;
  private static IndexColorModel trans_model;
  static final int DISPOSAL_NONE = 0;
  static final int DISPOSAL_SAVE = 1;
  static final int DISPOSAL_BGCOLOR = 2;
  static final int DISPOSAL_PREVIOUS = 3;
  GifImageDecoder decoder;
  int disposal_method;
  int delay;
  IndexColorModel model;
  int x;
  int y;
  int width;
  int height;
  boolean initialframe;

  public GifFrame(GifImageDecoder paramGifImageDecoder, int paramInt1, int paramInt2, boolean paramBoolean, IndexColorModel paramIndexColorModel, int paramInt3, int paramInt4, int paramInt5, int paramInt6)
  {
    this.decoder = paramGifImageDecoder;
    this.disposal_method = paramInt1;
    this.delay = paramInt2;
    this.model = paramIndexColorModel;
    this.initialframe = paramBoolean;
    this.x = paramInt3;
    this.y = paramInt4;
    this.width = paramInt5;
    this.height = paramInt6;
  }

  private void setPixels(int paramInt1, int paramInt2, int paramInt3, int paramInt4, ColorModel paramColorModel, byte[] paramArrayOfByte, int paramInt5, int paramInt6)
  {
    this.decoder.setPixels(paramInt1, paramInt2, paramInt3, paramInt4, paramColorModel, paramArrayOfByte, paramInt5, paramInt6);
  }

  public boolean dispose()
  {
    if (this.decoder.imageComplete(2, false) == 0)
      return false;
    if (this.delay > 0)
      try
      {
        Thread.sleep(this.delay);
      }
      catch (InterruptedException localInterruptedException)
      {
        return false;
      }
    Thread.yield();
    int i = this.decoder.global_width;
    int j = this.decoder.global_height;
    if (this.x < 0)
    {
      this.width += this.x;
      this.x = 0;
    }
    if (this.x + this.width > i)
      this.width = (i - this.x);
    if (this.width <= 0)
    {
      this.disposal_method = 0;
    }
    else
    {
      if (this.y < 0)
      {
        this.height += this.y;
        this.y = 0;
      }
      if (this.y + this.height > j)
        this.height = (j - this.y);
      if (this.height <= 0)
        this.disposal_method = 0;
    }
    switch (this.disposal_method)
    {
    case 3:
      byte[] arrayOfByte1 = this.decoder.saved_image;
      IndexColorModel localIndexColorModel = this.decoder.saved_model;
      if (arrayOfByte1 == null)
        break label452;
      setPixels(this.x, this.y, this.width, this.height, localIndexColorModel, arrayOfByte1, this.y * i + this.x, i);
      break;
    case 2:
      int k;
      int l;
      if (this.model.getTransparentPixel() < 0)
      {
        this.model = trans_model;
        if (this.model == null)
        {
          this.model = new IndexColorModel(8, 1, new byte[4], 0, true);
          trans_model = this.model;
        }
        k = 0;
      }
      else
      {
        k = (byte)this.model.getTransparentPixel();
      }
      byte[] arrayOfByte2 = new byte[this.width];
      if (k != 0)
        for (l = 0; l < this.width; ++l)
          arrayOfByte2[l] = k;
      if (this.decoder.saved_image != null)
        for (l = 0; l < i * j; ++l)
          this.decoder.saved_image[l] = k;
      setPixels(this.x, this.y, this.width, this.height, this.model, arrayOfByte2, 0, 0);
      break;
    case 1:
      this.decoder.saved_model = this.model;
    }
    label452: return true;
  }
}