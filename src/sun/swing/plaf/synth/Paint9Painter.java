package sun.swing.plaf.synth;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import sun.swing.CachedPainter;

public class Paint9Painter extends CachedPainter
{
  private static final Insets EMPTY_INSETS = new Insets(0, 0, 0, 0);
  public static final int PAINT_TOP_LEFT = 1;
  public static final int PAINT_TOP = 2;
  public static final int PAINT_TOP_RIGHT = 4;
  public static final int PAINT_LEFT = 8;
  public static final int PAINT_CENTER = 16;
  public static final int PAINT_RIGHT = 32;
  public static final int PAINT_BOTTOM_RIGHT = 64;
  public static final int PAINT_BOTTOM = 128;
  public static final int PAINT_BOTTOM_LEFT = 256;
  public static final int PAINT_ALL = 512;

  public static boolean validImage(Image paramImage)
  {
    return ((paramImage != null) && (paramImage.getWidth(null) > 0) && (paramImage.getHeight(null) > 0));
  }

  public Paint9Painter(int paramInt)
  {
    super(paramInt);
  }

  public void paint(Component paramComponent, Graphics paramGraphics, int paramInt1, int paramInt2, int paramInt3, int paramInt4, Image paramImage, Insets paramInsets1, Insets paramInsets2, PaintType paramPaintType, int paramInt5)
  {
    if (paramImage == null)
      return;
    super.paint(paramComponent, paramGraphics, paramInt1, paramInt2, paramInt3, paramInt4, new Object[] { paramImage, paramInsets1, paramInsets2, paramPaintType, Integer.valueOf(paramInt5) });
  }

  protected void paintToImage(Component paramComponent, Image paramImage, Graphics paramGraphics, int paramInt1, int paramInt2, Object[] paramArrayOfObject)
  {
    int i = 0;
    while (i < paramArrayOfObject.length)
    {
      Image localImage = (Image)paramArrayOfObject[(i++)];
      Insets localInsets1 = (Insets)paramArrayOfObject[(i++)];
      Insets localInsets2 = (Insets)paramArrayOfObject[(i++)];
      PaintType localPaintType = (PaintType)paramArrayOfObject[(i++)];
      int j = ((Integer)paramArrayOfObject[(i++)]).intValue();
      paint9(paramGraphics, 0, 0, paramInt1, paramInt2, localImage, localInsets1, localInsets2, localPaintType, j);
    }
  }

  protected void paint9(Graphics paramGraphics, int paramInt1, int paramInt2, int paramInt3, int paramInt4, Image paramImage, Insets paramInsets1, Insets paramInsets2, PaintType paramPaintType, int paramInt5)
  {
    if (!(validImage(paramImage)))
      return;
    if (paramInsets1 == null)
      paramInsets1 = EMPTY_INSETS;
    if (paramInsets2 == null)
      paramInsets2 = EMPTY_INSETS;
    int i = paramImage.getWidth(null);
    int j = paramImage.getHeight(null);
    if (paramPaintType == PaintType.CENTER)
    {
      paramGraphics.drawImage(paramImage, paramInt1 + (paramInt3 - i) / 2, paramInt2 + (paramInt4 - j) / 2, null);
    }
    else
    {
      int l;
      int i1;
      int i2;
      int i4;
      int i5;
      int i6;
      int i7;
      int i8;
      if (paramPaintType == PaintType.TILE)
      {
        int k = 0;
        i1 = paramInt2;
        i2 = paramInt2 + paramInt4;
        while (i1 < i2)
        {
          int i3 = 0;
          i5 = paramInt1;
          i6 = paramInt1 + paramInt3;
          while (i5 < i6)
          {
            i7 = Math.min(i6, i5 + i - i3);
            i8 = Math.min(i2, i1 + j - k);
            paramGraphics.drawImage(paramImage, i5, i1, i7, i8, i3, k, i3 + i7 - i5, k + i8 - i1, null);
            i5 += i - i3;
            i4 = 0;
          }
          i1 += j - k;
          l = 0;
        }
      }
      else
      {
        l = paramInsets1.top;
        i1 = paramInsets1.left;
        i2 = paramInsets1.bottom;
        i4 = paramInsets1.right;
        i5 = paramInsets2.top;
        i6 = paramInsets2.left;
        i7 = paramInsets2.bottom;
        i8 = paramInsets2.right;
        if (l + i2 > j)
          i7 = i5 = i2 = l = Math.max(0, j / 2);
        if (i1 + i4 > i)
          i6 = i8 = i1 = i4 = Math.max(0, i / 2);
        if (i5 + i7 > paramInt4)
          i5 = i7 = Math.max(0, paramInt4 / 2 - 1);
        if (i6 + i8 > paramInt3)
          i6 = i8 = Math.max(0, paramInt3 / 2 - 1);
        boolean bool = paramPaintType == PaintType.PAINT9_STRETCH;
        if ((paramInt5 & 0x200) != 0)
          paramInt5 = 0x1FF & (paramInt5 ^ 0xFFFFFFFF);
        if ((paramInt5 & 0x8) != 0)
          drawChunk(paramImage, paramGraphics, bool, paramInt1, paramInt2 + i5, paramInt1 + i6, paramInt2 + paramInt4 - i7, 0, l, i1, j - i2, false);
        if ((paramInt5 & 0x1) != 0)
          drawImage(paramImage, paramGraphics, paramInt1, paramInt2, paramInt1 + i6, paramInt2 + i5, 0, 0, i1, l);
        if ((paramInt5 & 0x2) != 0)
          drawChunk(paramImage, paramGraphics, bool, paramInt1 + i6, paramInt2, paramInt1 + paramInt3 - i8, paramInt2 + i5, i1, 0, i - i4, l, true);
        if ((paramInt5 & 0x4) != 0)
          drawImage(paramImage, paramGraphics, paramInt1 + paramInt3 - i8, paramInt2, paramInt1 + paramInt3, paramInt2 + i5, i - i4, 0, i, l);
        if ((paramInt5 & 0x20) != 0)
          drawChunk(paramImage, paramGraphics, bool, paramInt1 + paramInt3 - i8, paramInt2 + i5, paramInt1 + paramInt3, paramInt2 + paramInt4 - i7, i - i4, l, i, j - i2, false);
        if ((paramInt5 & 0x40) != 0)
          drawImage(paramImage, paramGraphics, paramInt1 + paramInt3 - i8, paramInt2 + paramInt4 - i7, paramInt1 + paramInt3, paramInt2 + paramInt4, i - i4, j - i2, i, j);
        if ((paramInt5 & 0x80) != 0)
          drawChunk(paramImage, paramGraphics, bool, paramInt1 + i6, paramInt2 + paramInt4 - i7, paramInt1 + paramInt3 - i8, paramInt2 + paramInt4, i1, j - i2, i - i4, j, true);
        if ((paramInt5 & 0x100) != 0)
          drawImage(paramImage, paramGraphics, paramInt1, paramInt2 + paramInt4 - i7, paramInt1 + i6, paramInt2 + paramInt4, 0, j - i2, i1, j);
        if ((paramInt5 & 0x10) != 0)
          drawImage(paramImage, paramGraphics, paramInt1 + i6, paramInt2 + i5, paramInt1 + paramInt3 - i8, paramInt2 + paramInt4 - i7, i1, l, i - i4, j - i2);
      }
    }
  }

  private void drawImage(Image paramImage, Graphics paramGraphics, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6, int paramInt7, int paramInt8)
  {
    if ((paramInt3 - paramInt1 <= 0) || (paramInt4 - paramInt2 <= 0) || (paramInt7 - paramInt5 <= 0) || (paramInt8 - paramInt6 <= 0))
      return;
    paramGraphics.drawImage(paramImage, paramInt1, paramInt2, paramInt3, paramInt4, paramInt5, paramInt6, paramInt7, paramInt8, null);
  }

  private void drawChunk(Image paramImage, Graphics paramGraphics, boolean paramBoolean1, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6, int paramInt7, int paramInt8, boolean paramBoolean2)
  {
    if ((paramInt3 - paramInt1 <= 0) || (paramInt4 - paramInt2 <= 0) || (paramInt7 - paramInt5 <= 0) || (paramInt8 - paramInt6 <= 0))
      return;
    if (paramBoolean1)
    {
      paramGraphics.drawImage(paramImage, paramInt1, paramInt2, paramInt3, paramInt4, paramInt5, paramInt6, paramInt7, paramInt8, null);
    }
    else
    {
      int k;
      int l;
      int i = paramInt7 - paramInt5;
      int j = paramInt8 - paramInt6;
      if (paramBoolean2)
      {
        k = i;
        l = 0;
      }
      else
      {
        k = 0;
        l = j;
      }
      while ((paramInt1 < paramInt3) && (paramInt2 < paramInt4))
      {
        int i1 = Math.min(paramInt3, paramInt1 + i);
        int i2 = Math.min(paramInt4, paramInt2 + j);
        paramGraphics.drawImage(paramImage, paramInt1, paramInt2, i1, i2, paramInt5, paramInt6, paramInt5 + i1 - paramInt1, paramInt6 + i2 - paramInt2, null);
        paramInt1 += k;
        paramInt2 += l;
      }
    }
  }

  protected Image createImage(Component paramComponent, int paramInt1, int paramInt2, GraphicsConfiguration paramGraphicsConfiguration, Object[] paramArrayOfObject)
  {
    if (paramGraphicsConfiguration == null)
      return new BufferedImage(paramInt1, paramInt2, 2);
    return paramGraphicsConfiguration.createCompatibleImage(paramInt1, paramInt2, 3);
  }

  public static enum PaintType
  {
    CENTER, TILE, PAINT9_STRETCH, PAINT9_TILE;
  }
}