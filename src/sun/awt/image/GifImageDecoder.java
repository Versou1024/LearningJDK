package sun.awt.image;

import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;

public class GifImageDecoder extends ImageDecoder
{
  private static final boolean verbose = 0;
  private static final int IMAGESEP = 44;
  private static final int EXBLOCK = 33;
  private static final int EX_GRAPHICS_CONTROL = 249;
  private static final int EX_COMMENT = 254;
  private static final int EX_APPLICATION = 255;
  private static final int TERMINATOR = 59;
  private static final int TRANSPARENCYMASK = 1;
  private static final int INTERLACEMASK = 64;
  private static final int COLORMAPMASK = 128;
  int num_global_colors;
  byte[] global_colormap;
  int trans_pixel = -1;
  IndexColorModel global_model;
  Hashtable props = new Hashtable();
  byte[] saved_image;
  IndexColorModel saved_model;
  int global_width;
  int global_height;
  int global_bgpixel;
  GifFrame curframe;
  private static final int normalflags = 30;
  private static final int interlaceflags = 29;
  private short[] prefix = new short[4096];
  private byte[] suffix = new byte[4096];
  private byte[] outCode = new byte[4097];

  public GifImageDecoder(InputStreamImageSource paramInputStreamImageSource, InputStream paramInputStream)
  {
    super(paramInputStreamImageSource, paramInputStream);
  }

  private static void error(String paramString)
    throws sun.awt.image.ImageFormatException
  {
    throw new sun.awt.image.ImageFormatException(paramString);
  }

  private int readBytes(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
  {
    if (paramInt2 > 0)
      try
      {
        int i = this.input.read(paramArrayOfByte, paramInt1, paramInt2);
        if (i < 0)
          break label42:
        paramInt1 += i;
        paramInt2 -= i;
      }
      catch (IOException localIOException)
      {
      }
    label42: return paramInt2;
  }

  private static final int ExtractByte(byte[] paramArrayOfByte, int paramInt)
  {
    return (paramArrayOfByte[paramInt] & 0xFF);
  }

  private static final int ExtractWord(byte[] paramArrayOfByte, int paramInt)
  {
    return (paramArrayOfByte[paramInt] & 0xFF | (paramArrayOfByte[(paramInt + 1)] & 0xFF) << 8);
  }

  public void produceImage()
    throws IOException, sun.awt.image.ImageFormatException
  {
    try
    {
      readHeader();
      int i = 0;
      int j = 0;
      int k = -1;
      int l = 0;
      int i1 = -1;
      int i2 = 0;
      int i3 = 0;
      while (!(this.aborted))
      {
        int i4;
        switch (i4 = this.input.read())
        {
        case 33:
          switch (i4 = this.input.read())
          {
          case 249:
            byte[] arrayOfByte1 = new byte[6];
            if (readBytes(arrayOfByte1, 0, 6) != 0)
              return;
            if ((arrayOfByte1[0] != 4) || (arrayOfByte1[5] != 0))
              return;
            i1 = ExtractWord(arrayOfByte1, 2) * 10;
            if ((i1 > 0) && (i3 == 0))
            {
              i3 = 1;
              ImageFetcher.startingAnimation();
            }
            l = arrayOfByte1[1] >> 2 & 0x7;
            if ((arrayOfByte1[1] & 0x1) != 0)
              this.trans_pixel = ExtractByte(arrayOfByte1, 4);
            else
              this.trans_pixel = -1;
            break;
          case 254:
          case 255:
          default:
            int i5 = 0;
            String str = "";
            while (true)
            {
              int i6 = this.input.read();
              if (i6 <= 0)
                break;
              byte[] arrayOfByte2 = new byte[i6];
              if (readBytes(arrayOfByte2, 0, i6) != 0)
                return;
              if (i4 == 254)
              {
                str = str + new String(arrayOfByte2, 0);
              }
              else if (i4 == 255)
              {
                if (i5 != 0)
                  if ((i6 == 3) && (arrayOfByte2[0] == 1))
                    if (i2 != 0)
                    {
                      ExtractWord(arrayOfByte2, 1);
                    }
                    else
                    {
                      k = ExtractWord(arrayOfByte2, 1);
                      i2 = 1;
                    }
                  else
                    i5 = 0;
                if ("NETSCAPE2.0".equals(new String(arrayOfByte2, 0)))
                  i5 = 1;
              }
            }
            if (i4 == 254)
              this.props.put("comment", str);
            if ((i5 != 0) && (i3 == 0))
            {
              i3 = 1;
              ImageFetcher.startingAnimation();
            }
            break;
          case -1:
            return;
          }
          break;
        case 44:
          if (i3 == 0)
            this.input.mark(0);
          try
          {
            if (!(readImage((i == 0) ? 1 : false, l, i1)))
            {
              close();
              return;
            }
          }
          catch (Exception localException)
          {
            close();
            return;
          }
          ++j;
          ++i;
          break;
        case -1:
        default:
          if (j == 0)
            return;
        case 59:
          if ((k == 0) || (k-- >= 0))
            try
            {
              if (this.curframe != null)
              {
                this.curframe.dispose();
                this.curframe = null;
              }
              this.input.reset();
              this.saved_image = null;
              this.saved_model = null;
              j = 0;
            }
            catch (IOException localIOException)
            {
              close();
              return;
            }
          imageComplete(3, true);
          return;
        }
      }
    }
    finally
    {
      close();
    }
  }

  private void readHeader()
    throws IOException, sun.awt.image.ImageFormatException
  {
    byte[] arrayOfByte = new byte[13];
    if (readBytes(arrayOfByte, 0, 13) != 0)
      throw new IOException();
    if ((arrayOfByte[0] != 71) || (arrayOfByte[1] != 73) || (arrayOfByte[2] != 70))
      error("not a GIF file.");
    this.global_width = ExtractWord(arrayOfByte, 6);
    this.global_height = ExtractWord(arrayOfByte, 8);
    int i = ExtractByte(arrayOfByte, 10);
    if ((i & 0x80) == 0)
    {
      this.num_global_colors = 2;
      this.global_bgpixel = 0;
      this.global_colormap = new byte[6];
      this.global_colormap[0] = (this.global_colormap[1] = this.global_colormap[2] = 0);
      this.global_colormap[3] = (this.global_colormap[4] = this.global_colormap[5] = -1);
    }
    else
    {
      this.num_global_colors = (1 << (i & 0x7) + 1);
      this.global_bgpixel = ExtractByte(arrayOfByte, 11);
      if (arrayOfByte[12] != 0)
        this.props.put("aspectratio", "" + ((ExtractByte(arrayOfByte, 12) + 15) / 64.0D));
      this.global_colormap = new byte[this.num_global_colors * 3];
      if (readBytes(this.global_colormap, 0, this.num_global_colors * 3) != 0)
        throw new IOException();
    }
    this.input.mark(2147483647);
  }

  private static native void initIDs();

  private native boolean parseImage(int paramInt1, int paramInt2, int paramInt3, int paramInt4, boolean paramBoolean, int paramInt5, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, IndexColorModel paramIndexColorModel);

  private int sendPixels(int paramInt1, int paramInt2, int paramInt3, int paramInt4, byte[] paramArrayOfByte, ColorModel paramColorModel)
  {
    int i;
    int k;
    if (paramInt2 < 0)
    {
      paramInt4 += paramInt2;
      paramInt2 = 0;
    }
    if (paramInt2 + paramInt4 > this.global_height)
      paramInt4 = this.global_height - paramInt2;
    if (paramInt4 <= 0)
      return 1;
    if (paramInt1 < 0)
    {
      i = -paramInt1;
      paramInt3 += paramInt1;
      k = 0;
    }
    else
    {
      i = 0;
      k = paramInt1;
    }
    if (k + paramInt3 > this.global_width)
      paramInt3 = this.global_width - k;
    if (paramInt3 <= 0)
      return 1;
    int j = i + paramInt3;
    int l = paramInt2 * this.global_width + k;
    int i1 = (this.curframe.disposal_method == 1) ? 1 : 0;
    if ((this.trans_pixel >= 0) && (!(this.curframe.initialframe)))
    {
      if ((this.saved_image != null) && (paramColorModel.equals(this.saved_model)))
      {
        i2 = i;
        while (i2 < j)
        {
          i4 = paramArrayOfByte[i2];
          if ((i4 & 0xFF) == this.trans_pixel)
            paramArrayOfByte[i2] = this.saved_image[l];
          else if (i1 != 0)
            this.saved_image[l] = i4;
          ++i2;
          ++l;
        }
        break label393:
      }
      int i2 = -1;
      int i4 = 1;
      int i5 = i;
      while (i5 < j)
      {
        int i6 = paramArrayOfByte[i5];
        if ((i6 & 0xFF) == this.trans_pixel)
        {
          if (i2 >= 0)
          {
            i4 = setPixels(paramInt1 + i2, paramInt2, i5 - i2, 1, paramColorModel, paramArrayOfByte, i2, 0);
            if (i4 == 0)
              break;
          }
          i3 = -1;
        }
        else
        {
          if (i3 < 0)
            i3 = i5;
          if (i1 != 0)
            this.saved_image[l] = i6;
        }
        ++i5;
        ++l;
      }
      if (i3 >= 0)
        i4 = setPixels(paramInt1 + i3, paramInt2, j - i3, 1, paramColorModel, paramArrayOfByte, i3, 0);
      return i4;
    }
    if (i1 != 0)
      System.arraycopy(paramArrayOfByte, i, this.saved_image, l, paramInt3);
    label393: int i3 = setPixels(k, paramInt2, paramInt3, paramInt4, paramColorModel, paramArrayOfByte, i, 0);
    return i3;
  }

  private boolean readImage(boolean paramBoolean, int paramInt1, int paramInt2)
    throws IOException
  {
    if ((this.curframe != null) && (!(this.curframe.dispose())))
    {
      abort();
      return false;
    }
    long l = 3412047291253522432L;
    byte[] arrayOfByte1 = new byte[259];
    if (readBytes(arrayOfByte1, 0, 10) != 0)
      throw new IOException();
    int i = ExtractWord(arrayOfByte1, 0);
    int j = ExtractWord(arrayOfByte1, 2);
    int k = ExtractWord(arrayOfByte1, 4);
    int i1 = ExtractWord(arrayOfByte1, 6);
    if ((k == 0) && (this.global_width != 0))
      k = this.global_width - i;
    if ((i1 == 0) && (this.global_height != 0))
      i1 = this.global_height - j;
    boolean bool1 = (arrayOfByte1[8] & 0x40) != 0;
    IndexColorModel localIndexColorModel = this.global_model;
    if ((arrayOfByte1[8] & 0x80) != 0)
    {
      i2 = 1 << (arrayOfByte1[8] & 0x7) + 1;
      arrayOfByte2 = new byte[i2 * 3];
      arrayOfByte2[0] = arrayOfByte1[9];
      if (readBytes(arrayOfByte2, 1, i2 * 3 - 1) != 0)
        throw new IOException();
      if (readBytes(arrayOfByte1, 9, 1) != 0)
        throw new IOException();
      if (this.trans_pixel >= i2)
      {
        i2 = this.trans_pixel + 1;
        arrayOfByte2 = grow_colormap(arrayOfByte2, i2);
      }
      localIndexColorModel = new IndexColorModel(8, i2, arrayOfByte2, 0, false, this.trans_pixel);
    }
    else if ((localIndexColorModel == null) || (this.trans_pixel != localIndexColorModel.getTransparentPixel()))
    {
      if (this.trans_pixel >= this.num_global_colors)
      {
        this.num_global_colors = (this.trans_pixel + 1);
        this.global_colormap = grow_colormap(this.global_colormap, this.num_global_colors);
      }
      localIndexColorModel = new IndexColorModel(8, this.num_global_colors, this.global_colormap, 0, false, this.trans_pixel);
      this.global_model = localIndexColorModel;
    }
    if (paramBoolean)
    {
      if (this.global_width == 0)
        this.global_width = k;
      if (this.global_height == 0)
        this.global_height = i1;
      setDimensions(this.global_width, this.global_height);
      setProperties(this.props);
      setColorModel(localIndexColorModel);
      headerComplete();
    }
    if ((paramInt1 == 1) && (this.saved_image == null))
    {
      this.saved_image = new byte[this.global_width * this.global_height];
      if ((i1 < this.global_height) && (localIndexColorModel != null))
      {
        i2 = (byte)localIndexColorModel.getTransparentPixel();
        if (i2 >= 0)
        {
          arrayOfByte2 = new byte[this.global_width];
          for (int i3 = 0; i3 < this.global_width; ++i3)
            arrayOfByte2[i3] = i2;
          setPixels(0, 0, this.global_width, j, localIndexColorModel, arrayOfByte2, 0, 0);
          setPixels(0, j + i1, this.global_width, this.global_height - i1 - j, localIndexColorModel, arrayOfByte2, 0, 0);
        }
      }
    }
    int i2 = (bool1) ? 29 : 30;
    setHints(i2);
    this.curframe = new GifFrame(this, paramInt1, paramInt2, false, localIndexColorModel, i, j, k, i1);
    byte[] arrayOfByte2 = new byte[k];
    boolean bool2 = parseImage(i, j, k, i1, bool1, ExtractByte(arrayOfByte1, 9), arrayOfByte1, arrayOfByte2, localIndexColorModel);
    if (!(bool2))
      abort();
    return bool2;
  }

  public static byte[] grow_colormap(byte[] paramArrayOfByte, int paramInt)
  {
    byte[] arrayOfByte = new byte[paramInt * 3];
    System.arraycopy(paramArrayOfByte, 0, arrayOfByte, 0, paramArrayOfByte.length);
    return arrayOfByte;
  }

  static
  {
    NativeLibLoader.loadLibraries();
    initIDs();
  }
}