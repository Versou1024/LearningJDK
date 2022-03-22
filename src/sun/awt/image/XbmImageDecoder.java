package sun.awt.image;

import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

public class XbmImageDecoder extends ImageDecoder
{
  private static byte[] XbmColormap = { -1, -1, -1, 0, 0, 0 };
  private static int XbmHints = 30;

  public XbmImageDecoder(InputStreamImageSource paramInputStreamImageSource, InputStream paramInputStream)
  {
    super(paramInputStreamImageSource, paramInputStream);
    if (!(this.input instanceof BufferedInputStream))
      this.input = new BufferedInputStream(this.input, 80);
  }

  private static void error(String paramString)
    throws sun.awt.image.ImageFormatException
  {
    throw new sun.awt.image.ImageFormatException(paramString);
  }

  public void produceImage()
    throws IOException, sun.awt.image.ImageFormatException
  {
    char[] arrayOfChar = new char[80];
    int j = 0;
    int k = 0;
    int l = 0;
    int i1 = 0;
    int i2 = 0;
    int i3 = 0;
    int i4 = 1;
    byte[] arrayOfByte = null;
    Object localObject = null;
    while (true)
    {
      int i;
      label116: 
      do
        while (true)
        {
          do
          {
            if (this.aborted)
              break label639;
            if ((i = this.input.read()) == -1)
              break label639;
            if ((((97 > i) || (i > 122))) && (((65 > i) || (i > 90))) && (((48 > i) || (i > 57))) && (i != 35) && (i != 95))
              break label116;
          }
          while (j >= 78);
          arrayOfChar[(j++)] = (char)i;
        }
      while (j <= 0);
      int i5 = j;
      j = 0;
      if (i4 != 0)
      {
        if ((i5 != 7) || (arrayOfChar[0] != '#') || (arrayOfChar[1] != 'd') || (arrayOfChar[2] != 'e') || (arrayOfChar[3] != 'f') || (arrayOfChar[4] != 'i') || (arrayOfChar[5] != 'n') || (arrayOfChar[6] != 'e'))
          error("Not an XBM file");
        i4 = 0;
      }
      if (arrayOfChar[(i5 - 1)] == 'h')
      {
        k = 1;
      }
      else if ((arrayOfChar[(i5 - 1)] == 't') && (i5 > 1) && (arrayOfChar[(i5 - 2)] == 'h'))
      {
        k = 2;
      }
      else
      {
        int i6;
        int i7;
        if ((i5 > 2) && (k < 0) && (arrayOfChar[0] == '0') && (arrayOfChar[1] == 'x'))
        {
          i6 = 0;
          for (i7 = 2; i7 < i5; ++i7)
          {
            i = arrayOfChar[i7];
            if ((48 <= i) && (i <= 57))
              i -= 48;
            else if ((65 <= i) && (i <= 90))
              i = i - 65 + 10;
            else if ((97 <= i) && (i <= 122))
              i = i - 97 + 10;
            else
              i = 0;
            i6 = i6 * 16 + i;
          }
          i7 = 1;
          while (i7 <= 128)
          {
            if (i2 < i1)
              if ((i6 & i7) != 0)
                arrayOfByte[i2] = 1;
              else
                arrayOfByte[i2] = 0;
            ++i2;
            i7 <<= 1;
          }
          if (i2 >= i1)
          {
            if (setPixels(0, i3, i1, 1, (ColorModel)localObject, arrayOfByte, 0, i1) <= 0)
              return;
            i2 = 0;
            if (i3++ >= l)
              break;
          }
        }
        else
        {
          i6 = 0;
          for (i7 = 0; i7 < i5; ++i7)
            if (('0' <= (i = arrayOfChar[i7])) && (i <= 57))
            {
              i6 = i6 * 10 + i - 48;
            }
            else
            {
              i6 = -1;
              break;
            }
          if ((i6 > 0) && (k > 0))
          {
            if (k == 1)
              i1 = i6;
            else
              l = i6;
            if ((i1 == 0) || (l == 0))
            {
              k = 0;
            }
            else
            {
              localObject = new IndexColorModel(8, 2, XbmColormap, 0, false, 0);
              setDimensions(i1, l);
              setColorModel((ColorModel)localObject);
              setHints(XbmHints);
              headerComplete();
              arrayOfByte = new byte[i1];
              k = -1;
            }
          }
        }
      }
    }
    label639: this.input.close();
    imageComplete(3, true);
  }
}