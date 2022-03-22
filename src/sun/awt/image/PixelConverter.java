package sun.awt.image;

import java.awt.image.ColorModel;

public class PixelConverter
{
  public static final PixelConverter instance = new PixelConverter();
  protected int alphaMask = 0;

  public int rgbToPixel(int paramInt, ColorModel paramColorModel)
  {
    Object localObject = paramColorModel.getDataElements(paramInt, null);
    switch (paramColorModel.getTransferType())
    {
    case 0:
      byte[] arrayOfByte = (byte[])(byte[])localObject;
      int i = 0;
      switch (arrayOfByte.length)
      {
      default:
        i = arrayOfByte[3] << 24;
      case 3:
        i |= (arrayOfByte[2] & 0xFF) << 16;
      case 2:
        i |= (arrayOfByte[1] & 0xFF) << 8;
      case 1:
      }
      i |= arrayOfByte[0] & 0xFF;
      return i;
    case 1:
    case 2:
      short[] arrayOfShort = (short[])(short[])localObject;
      return (((arrayOfShort.length > 1) ? arrayOfShort[1] << 16 : 0) | arrayOfShort[0] & 0xFFFF);
    case 3:
      return ((int[])(int[])localObject)[0];
    }
    return paramInt;
  }

  public int pixelToRgb(int paramInt, ColorModel paramColorModel)
  {
    return paramInt;
  }

  public final int getAlphaMask()
  {
    return this.alphaMask;
  }

  public static class Argb extends PixelConverter
  {
    public static final PixelConverter instance = new Argb();

    private Argb()
    {
      this.alphaMask = -16777216;
    }

    public int rgbToPixel(int paramInt, ColorModel paramColorModel)
    {
      return paramInt;
    }

    public int pixelToRgb(int paramInt, ColorModel paramColorModel)
    {
      return paramInt;
    }
  }

  public static class ArgbBm extends PixelConverter
  {
    public static final PixelConverter instance = new ArgbBm();

    public int rgbToPixel(int paramInt, ColorModel paramColorModel)
    {
      return (paramInt | paramInt >> 31 << 24);
    }

    public int pixelToRgb(int paramInt, ColorModel paramColorModel)
    {
      return (paramInt << 7 >> 7);
    }
  }

  public static class ArgbPre extends PixelConverter
  {
    public static final PixelConverter instance = new ArgbPre();

    private ArgbPre()
    {
      this.alphaMask = -16777216;
    }

    public int rgbToPixel(int paramInt, ColorModel paramColorModel)
    {
      if (paramInt >> 24 == -1)
        return paramInt;
      int i = paramInt >>> 24;
      int j = paramInt >> 16 & 0xFF;
      int k = paramInt >> 8 & 0xFF;
      int l = paramInt & 0xFF;
      int i1 = i + (i >> 7);
      j = j * i1 >> 8;
      k = k * i1 >> 8;
      l = l * i1 >> 8;
      return (i << 24 | j << 16 | k << 8 | l);
    }

    public int pixelToRgb(int paramInt, ColorModel paramColorModel)
    {
      int i = paramInt >>> 24;
      if ((i == 255) || (i == 0))
        return paramInt;
      int j = paramInt >> 16 & 0xFF;
      int k = paramInt >> 8 & 0xFF;
      int l = paramInt & 0xFF;
      j = ((j << 8) - j) / i;
      k = ((k << 8) - k) / i;
      l = ((l << 8) - l) / i;
      return (i << 24 | j << 16 | k << 8 | l);
    }
  }

  public static class Bgrx extends PixelConverter
  {
    public static final PixelConverter instance = new Bgrx();

    public int rgbToPixel(int paramInt, ColorModel paramColorModel)
    {
      return (paramInt << 24 | (paramInt & 0xFF00) << 8 | paramInt >> 8 & 0xFF00);
    }

    public int pixelToRgb(int paramInt, ColorModel paramColorModel)
    {
      return (0xFF000000 | (paramInt & 0xFF00) << 8 | paramInt >> 8 & 0xFF00 | paramInt >>> 24);
    }
  }

  public static class ByteGray extends PixelConverter
  {
    static final double RED_MULT = 0.29899999999999999D;
    static final double GRN_MULT = 0.58699999999999997D;
    static final double BLU_MULT = 0.114D;
    public static final PixelConverter instance = new ByteGray();

    public int rgbToPixel(int paramInt, ColorModel paramColorModel)
    {
      int i = paramInt >> 16 & 0xFF;
      int j = paramInt >> 8 & 0xFF;
      int k = paramInt & 0xFF;
      return (int)(i * 0.29899999999999999D + j * 0.58699999999999997D + k * 0.114D + 0.5D);
    }

    public int pixelToRgb(int paramInt, ColorModel paramColorModel)
    {
      return (((0xFF00 | paramInt) << 8 | paramInt) << 8 | paramInt);
    }
  }

  public static class Rgba extends PixelConverter
  {
    public static final PixelConverter instance = new Rgba();

    private Rgba()
    {
      this.alphaMask = 255;
    }

    public int rgbToPixel(int paramInt, ColorModel paramColorModel)
    {
      return (paramInt << 8 | paramInt >>> 24);
    }

    public int pixelToRgb(int paramInt, ColorModel paramColorModel)
    {
      return (paramInt << 24 | paramInt >>> 8);
    }
  }

  public static class RgbaPre extends PixelConverter
  {
    public static final PixelConverter instance = new RgbaPre();

    private RgbaPre()
    {
      this.alphaMask = 255;
    }

    public int rgbToPixel(int paramInt, ColorModel paramColorModel)
    {
      if (paramInt >> 24 == -1)
        return (paramInt << 8 | paramInt >>> 24);
      int i = paramInt >>> 24;
      int j = paramInt >> 16 & 0xFF;
      int k = paramInt >> 8 & 0xFF;
      int l = paramInt & 0xFF;
      int i1 = i + (i >> 7);
      j = j * i1 >> 8;
      k = k * i1 >> 8;
      l = l * i1 >> 8;
      return (j << 24 | k << 16 | l << 8 | i);
    }

    public int pixelToRgb(int paramInt, ColorModel paramColorModel)
    {
      int i = paramInt & 0xFF;
      if ((i == 255) || (i == 0))
        return (paramInt >>> 8 | paramInt << 24);
      int j = paramInt >>> 24;
      int k = paramInt >> 16 & 0xFF;
      int l = paramInt >> 8 & 0xFF;
      j = ((j << 8) - j) / i;
      k = ((k << 8) - k) / i;
      l = ((l << 8) - l) / i;
      return (j << 24 | k << 16 | l << 8 | i);
    }
  }

  public static class Rgbx extends PixelConverter
  {
    public static final PixelConverter instance = new Rgbx();

    public int rgbToPixel(int paramInt, ColorModel paramColorModel)
    {
      return (paramInt << 8);
    }

    public int pixelToRgb(int paramInt, ColorModel paramColorModel)
    {
      return (0xFF000000 | paramInt >> 8);
    }
  }

  public static class Ushort4444Argb extends PixelConverter
  {
    public static final PixelConverter instance = new Ushort4444Argb();

    private Ushort4444Argb()
    {
      this.alphaMask = 61440;
    }

    public int rgbToPixel(int paramInt, ColorModel paramColorModel)
    {
      int i = paramInt >> 16 & 0xF000;
      int j = paramInt >> 12 & 0xF00;
      int k = paramInt >> 8 & 0xF0;
      int l = paramInt >> 4 & 0xF;
      return (i | j | k | l);
    }

    public int pixelToRgb(int paramInt, ColorModel paramColorModel)
    {
      int i = paramInt & 0xF000;
      i = (paramInt << 16 | paramInt << 12) & 0xFF000000;
      int j = paramInt & 0xF00;
      j = (paramInt << 12 | paramInt << 8) & 0xFF0000;
      int k = paramInt & 0xF0;
      k = (paramInt << 8 | paramInt << 4) & 0xFF00;
      int l = paramInt & 0xF;
      l = (paramInt << 4 | paramInt << 0) & 0xFF;
      return (i | j | k | l);
    }
  }

  public static class Ushort555Rgb extends PixelConverter
  {
    public static final PixelConverter instance = new Ushort555Rgb();

    public int rgbToPixel(int paramInt, ColorModel paramColorModel)
    {
      return (paramInt >> 9 & 0x7C00 | paramInt >> 6 & 0x3E0 | paramInt >> 3 & 0x1F);
    }

    public int pixelToRgb(int paramInt, ColorModel paramColorModel)
    {
      int i = paramInt >> 10 & 0x1F;
      i = i << 3 | i >> 2;
      int j = paramInt >> 5 & 0x1F;
      j = j << 3 | j >> 2;
      int k = paramInt & 0x1F;
      k = k << 3 | k >> 2;
      return (0xFF000000 | i << 16 | j << 8 | k);
    }
  }

  public static class Ushort555Rgbx extends PixelConverter
  {
    public static final PixelConverter instance = new Ushort555Rgbx();

    public int rgbToPixel(int paramInt, ColorModel paramColorModel)
    {
      return (paramInt >> 8 & 0xF800 | paramInt >> 5 & 0x7C0 | paramInt >> 2 & 0x3E);
    }

    public int pixelToRgb(int paramInt, ColorModel paramColorModel)
    {
      int i = paramInt >> 11 & 0x1F;
      i = i << 3 | i >> 2;
      int j = paramInt >> 6 & 0x1F;
      j = j << 3 | j >> 2;
      int k = paramInt >> 1 & 0x1F;
      k = k << 3 | k >> 2;
      return (0xFF000000 | i << 16 | j << 8 | k);
    }
  }

  public static class Ushort565Rgb extends PixelConverter
  {
    public static final PixelConverter instance = new Ushort565Rgb();

    public int rgbToPixel(int paramInt, ColorModel paramColorModel)
    {
      return (paramInt >> 8 & 0xF800 | paramInt >> 5 & 0x7E0 | paramInt >> 3 & 0x1F);
    }

    public int pixelToRgb(int paramInt, ColorModel paramColorModel)
    {
      int i = paramInt >> 11 & 0x1F;
      i = i << 3 | i >> 2;
      int j = paramInt >> 5 & 0x3F;
      j = j << 2 | j >> 4;
      int k = paramInt & 0x1F;
      k = k << 3 | k >> 2;
      return (0xFF000000 | i << 16 | j << 8 | k);
    }
  }

  public static class UshortGray extends PixelConverter.ByteGray
  {
    static final double SHORT_MULT = 257.0D;
    static final double USHORT_RED_MULT = 76.843000000000004D;
    static final double USHORT_GRN_MULT = 150.85899999999998D;
    static final double USHORT_BLU_MULT = 29.298000000000002D;
    public static final PixelConverter instance = new UshortGray();

    private UshortGray()
    {
      super(null);
    }

    public int rgbToPixel(int paramInt, ColorModel paramColorModel)
    {
      int i = paramInt >> 16 & 0xFF;
      int j = paramInt >> 8 & 0xFF;
      int k = paramInt & 0xFF;
      return (int)(i * 76.843000000000004D + j * 150.85899999999998D + k * 29.298000000000002D + 0.5D);
    }

    public int pixelToRgb(int paramInt, ColorModel paramColorModel)
    {
      paramInt >>= 8;
      return (((0xFF00 | paramInt) << 8 | paramInt) << 8 | paramInt);
    }
  }

  public static class Xbgr extends PixelConverter
  {
    public static final PixelConverter instance = new Xbgr();

    public int rgbToPixel(int paramInt, ColorModel paramColorModel)
    {
      return ((paramInt & 0xFF) << 16 | paramInt & 0xFF00 | paramInt >> 16 & 0xFF);
    }

    public int pixelToRgb(int paramInt, ColorModel paramColorModel)
    {
      return (0xFF000000 | (paramInt & 0xFF) << 16 | paramInt & 0xFF00 | paramInt >> 16 & 0xFF);
    }
  }

  public static class Xrgb extends PixelConverter
  {
    public static final PixelConverter instance = new Xrgb();

    public int rgbToPixel(int paramInt, ColorModel paramColorModel)
    {
      return paramInt;
    }

    public int pixelToRgb(int paramInt, ColorModel paramColorModel)
    {
      return (0xFF000000 | paramInt);
    }
  }
}