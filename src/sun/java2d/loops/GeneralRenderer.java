package sun.java2d.loops;

import java.awt.Color;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import sun.font.GlyphList;
import sun.java2d.SunGraphics2D;
import sun.java2d.SurfaceData;
import sun.java2d.pipe.Region;

public final class GeneralRenderer
{
  static final int OUTCODE_TOP = 1;
  static final int OUTCODE_BOTTOM = 2;
  static final int OUTCODE_LEFT = 4;
  static final int OUTCODE_RIGHT = 8;

  public static void register()
  {
    GeneralRenderer localGeneralRenderer = GeneralRenderer.class;
    GraphicsPrimitive[] arrayOfGraphicsPrimitive = { new GraphicsPrimitiveProxy(localGeneralRenderer, "SetFillRectANY", FillRect.methodSignature, FillRect.primTypeID, SurfaceType.AnyColor, CompositeType.SrcNoEa, SurfaceType.Any), new GraphicsPrimitiveProxy(localGeneralRenderer, "SetFillPathANY", FillPath.methodSignature, FillPath.primTypeID, SurfaceType.AnyColor, CompositeType.SrcNoEa, SurfaceType.Any), new GraphicsPrimitiveProxy(localGeneralRenderer, "SetFillSpansANY", FillSpans.methodSignature, FillSpans.primTypeID, SurfaceType.AnyColor, CompositeType.SrcNoEa, SurfaceType.Any), new GraphicsPrimitiveProxy(localGeneralRenderer, "SetDrawLineANY", DrawLine.methodSignature, DrawLine.primTypeID, SurfaceType.AnyColor, CompositeType.SrcNoEa, SurfaceType.Any), new GraphicsPrimitiveProxy(localGeneralRenderer, "SetDrawPolygonsANY", DrawPolygons.methodSignature, DrawPolygons.primTypeID, SurfaceType.AnyColor, CompositeType.SrcNoEa, SurfaceType.Any), new GraphicsPrimitiveProxy(localGeneralRenderer, "SetDrawPathANY", DrawPath.methodSignature, DrawPath.primTypeID, SurfaceType.AnyColor, CompositeType.SrcNoEa, SurfaceType.Any), new GraphicsPrimitiveProxy(localGeneralRenderer, "SetDrawRectANY", DrawRect.methodSignature, DrawRect.primTypeID, SurfaceType.AnyColor, CompositeType.SrcNoEa, SurfaceType.Any), new GraphicsPrimitiveProxy(localGeneralRenderer, "XorFillRectANY", FillRect.methodSignature, FillRect.primTypeID, SurfaceType.AnyColor, CompositeType.Xor, SurfaceType.Any), new GraphicsPrimitiveProxy(localGeneralRenderer, "XorFillPathANY", FillPath.methodSignature, FillPath.primTypeID, SurfaceType.AnyColor, CompositeType.Xor, SurfaceType.Any), new GraphicsPrimitiveProxy(localGeneralRenderer, "XorFillSpansANY", FillSpans.methodSignature, FillSpans.primTypeID, SurfaceType.AnyColor, CompositeType.Xor, SurfaceType.Any), new GraphicsPrimitiveProxy(localGeneralRenderer, "XorDrawLineANY", DrawLine.methodSignature, DrawLine.primTypeID, SurfaceType.AnyColor, CompositeType.Xor, SurfaceType.Any), new GraphicsPrimitiveProxy(localGeneralRenderer, "XorDrawPolygonsANY", DrawPolygons.methodSignature, DrawPolygons.primTypeID, SurfaceType.AnyColor, CompositeType.Xor, SurfaceType.Any), new GraphicsPrimitiveProxy(localGeneralRenderer, "XorDrawPathANY", DrawPath.methodSignature, DrawPath.primTypeID, SurfaceType.AnyColor, CompositeType.Xor, SurfaceType.Any), new GraphicsPrimitiveProxy(localGeneralRenderer, "XorDrawRectANY", DrawRect.methodSignature, DrawRect.primTypeID, SurfaceType.AnyColor, CompositeType.Xor, SurfaceType.Any), new GraphicsPrimitiveProxy(localGeneralRenderer, "XorDrawGlyphListANY", DrawGlyphList.methodSignature, DrawGlyphList.primTypeID, SurfaceType.AnyColor, CompositeType.Xor, SurfaceType.Any), new GraphicsPrimitiveProxy(localGeneralRenderer, "XorDrawGlyphListAAANY", DrawGlyphListAA.methodSignature, DrawGlyphListAA.primTypeID, SurfaceType.AnyColor, CompositeType.Xor, SurfaceType.Any) };
    GraphicsPrimitiveMgr.register(arrayOfGraphicsPrimitive);
  }

  static void doDrawPoly(SurfaceData paramSurfaceData, PixelWriter paramPixelWriter, int[] paramArrayOfInt1, int[] paramArrayOfInt2, int paramInt1, int paramInt2, Region paramRegion, int paramInt3, int paramInt4, boolean paramBoolean)
  {
    int k;
    int l;
    int[] arrayOfInt = null;
    if (paramInt2 <= 0)
      return;
    int i = k = paramArrayOfInt1[paramInt1] + paramInt3;
    int j = l = paramArrayOfInt2[paramInt1] + paramInt4;
    while (--paramInt2 > 0)
    {
      int i1 = paramArrayOfInt1[(++paramInt1)] + paramInt3;
      int i2 = paramArrayOfInt2[paramInt1] + paramInt4;
      arrayOfInt = doDrawLine(paramSurfaceData, paramPixelWriter, arrayOfInt, paramRegion, k, l, i1, i2);
      k = i1;
      l = i2;
    }
    if ((paramBoolean) && (((k != i) || (l != j))))
      arrayOfInt = doDrawLine(paramSurfaceData, paramPixelWriter, arrayOfInt, paramRegion, k, l, i, j);
  }

  static void doSetRect(SurfaceData paramSurfaceData, PixelWriter paramPixelWriter, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    WritableRaster localWritableRaster = (WritableRaster)paramSurfaceData.getRaster(paramInt1, paramInt2, paramInt3 - paramInt1, paramInt4 - paramInt2);
    paramPixelWriter.setRaster(localWritableRaster);
    while (paramInt2 < paramInt4)
    {
      for (int i = paramInt1; i < paramInt3; ++i)
        paramPixelWriter.writePixel(i, paramInt2);
      ++paramInt2;
    }
  }

  static int[] doDrawLine(SurfaceData paramSurfaceData, PixelWriter paramPixelWriter, int[] paramArrayOfInt, Region paramRegion, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    if (paramArrayOfInt == null)
      paramArrayOfInt = new int[8];
    paramArrayOfInt[0] = paramInt1;
    paramArrayOfInt[1] = paramInt2;
    paramArrayOfInt[2] = paramInt3;
    paramArrayOfInt[3] = paramInt4;
    if (!(adjustLine(paramArrayOfInt, paramRegion.getLoX(), paramRegion.getLoY(), paramRegion.getHiX(), paramRegion.getHiY())))
      return paramArrayOfInt;
    int i = paramArrayOfInt[0];
    int j = paramArrayOfInt[1];
    int k = paramArrayOfInt[2];
    int l = paramArrayOfInt[3];
    WritableRaster localWritableRaster = (WritableRaster)paramSurfaceData.getRaster(Math.min(i, k), Math.min(j, l), Math.abs(i - k) + 1, Math.abs(j - l) + 1);
    paramPixelWriter.setRaster(localWritableRaster);
    if (i == k)
    {
      if (j > l)
        do
          paramPixelWriter.writePixel(i, j);
        while (--j >= l);
      else
        do
          paramPixelWriter.writePixel(i, j);
        while (++j <= l);
    }
    else if (j == l)
    {
      if (i > k)
        do
          paramPixelWriter.writePixel(i, j);
        while (--i >= k);
      else
        do
          paramPixelWriter.writePixel(i, j);
        while (++i <= k);
    }
    else
    {
      int i5;
      int i6;
      int i7;
      int i8;
      int i9;
      int i11;
      int i12;
      int i1 = paramArrayOfInt[4];
      int i2 = paramArrayOfInt[5];
      int i3 = paramArrayOfInt[6];
      int i4 = paramArrayOfInt[7];
      if (i3 >= i4)
      {
        i11 = 1;
        i9 = i4 * 2;
        i8 = i3 * 2;
        i6 = (i1 < 0) ? -1 : 1;
        i7 = (i2 < 0) ? -1 : 1;
        i3 = -i3;
        i5 = k - i;
      }
      else
      {
        i11 = 0;
        i9 = i3 * 2;
        i8 = i4 * 2;
        i6 = (i2 < 0) ? -1 : 1;
        i7 = (i1 < 0) ? -1 : 1;
        i4 = -i4;
        i5 = l - j;
      }
      int i10 = -(i8 / 2);
      if (j != paramInt2)
      {
        i12 = j - paramInt2;
        if (i12 < 0)
          i12 = -i12;
        i10 += i12 * i3 * 2;
      }
      if (i != paramInt1)
      {
        i12 = i - paramInt1;
        if (i12 < 0)
          i12 = -i12;
        i10 += i12 * i4 * 2;
      }
      if (i5 < 0)
        i5 = -i5;
      if (i11 != 0)
        do
        {
          paramPixelWriter.writePixel(i, j);
          i += i6;
          i10 += i9;
          if (i10 >= 0)
          {
            j += i7;
            i10 -= i8;
          }
        }
        while (--i5 >= 0);
      else
        do
        {
          paramPixelWriter.writePixel(i, j);
          j += i6;
          i10 += i9;
          if (i10 >= 0)
          {
            i += i7;
            i10 -= i8;
          }
        }
        while (--i5 >= 0);
    }
    return paramArrayOfInt;
  }

  public static void doDrawRect(PixelWriter paramPixelWriter, SunGraphics2D paramSunGraphics2D, SurfaceData paramSurfaceData, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    if ((paramInt3 < 0) || (paramInt4 < 0))
      return;
    int i = Region.dimAdd(Region.dimAdd(paramInt1, paramInt3), 1);
    int j = Region.dimAdd(Region.dimAdd(paramInt2, paramInt4), 1);
    Region localRegion = paramSunGraphics2D.getCompClip().getBoundsIntersectionXYXY(paramInt1, paramInt2, i, j);
    if (localRegion.isEmpty())
      return;
    int k = localRegion.getLoX();
    int l = localRegion.getLoY();
    int i1 = localRegion.getHiX();
    int i2 = localRegion.getHiY();
    if ((paramInt3 < 2) || (paramInt4 < 2))
    {
      doSetRect(paramSurfaceData, paramPixelWriter, k, l, i1, i2);
      return;
    }
    if (l == paramInt2)
      doSetRect(paramSurfaceData, paramPixelWriter, k, l, i1, l + 1);
    if (k == paramInt1)
      doSetRect(paramSurfaceData, paramPixelWriter, k, l + 1, k + 1, i2 - 1);
    if (i1 == i)
      doSetRect(paramSurfaceData, paramPixelWriter, i1 - 1, l + 1, i1, i2 - 1);
    if (i2 == j)
      doSetRect(paramSurfaceData, paramPixelWriter, k, i2 - 1, i1, i2);
  }

  static void doDrawGlyphList(SurfaceData paramSurfaceData, PixelWriter paramPixelWriter, GlyphList paramGlyphList, Region paramRegion)
  {
    int[] arrayOfInt1 = paramGlyphList.getBounds();
    paramRegion.clipBoxToBounds(arrayOfInt1);
    int i = arrayOfInt1[0];
    int j = arrayOfInt1[1];
    int k = arrayOfInt1[2];
    int l = arrayOfInt1[3];
    WritableRaster localWritableRaster = (WritableRaster)paramSurfaceData.getRaster(i, j, k - i, l - j);
    paramPixelWriter.setRaster(localWritableRaster);
    int i1 = paramGlyphList.getNumGlyphs();
    for (int i2 = 0; i2 < i1; ++i2)
    {
      paramGlyphList.setGlyphIndex(i2);
      int[] arrayOfInt2 = paramGlyphList.getMetrics();
      int i3 = arrayOfInt2[0];
      int i4 = arrayOfInt2[1];
      int i5 = arrayOfInt2[2];
      int i6 = i3 + i5;
      int i7 = i4 + arrayOfInt2[3];
      int i8 = 0;
      if (i3 < i)
      {
        i8 = i - i3;
        i3 = i;
      }
      if (i4 < j)
      {
        i8 += (j - i4) * i5;
        i4 = j;
      }
      if (i6 > k)
        i6 = k;
      if (i7 > l)
        i7 = l;
      if ((i6 > i3) && (i7 > i4))
      {
        byte[] arrayOfByte = paramGlyphList.getGrayBits();
        i5 -= i6 - i3;
        for (int i9 = i4; i9 < i7; ++i9)
        {
          for (int i10 = i3; i10 < i6; ++i10)
            if (arrayOfByte[(i8++)] < 0)
              paramPixelWriter.writePixel(i10, i9);
          i8 += i5;
        }
      }
    }
  }

  static int outcode(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6)
  {
    int i;
    if (paramInt2 < paramInt4)
      i = 1;
    else if (paramInt2 > paramInt6)
      i = 2;
    else
      i = 0;
    if (paramInt1 < paramInt3)
      i |= 4;
    else if (paramInt1 > paramInt5)
      i |= 8;
    return i;
  }

  public static boolean adjustLine(int[] paramArrayOfInt, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    int i3;
    int i = paramInt3 - 1;
    int j = paramInt4 - 1;
    int k = paramArrayOfInt[0];
    int l = paramArrayOfInt[1];
    int i1 = paramArrayOfInt[2];
    int i2 = paramArrayOfInt[3];
    if ((i < paramInt1) || (j < paramInt2))
      return false;
    if (k == i1)
    {
      if ((k < paramInt1) || (k > i))
        return false;
      if (l > i2)
      {
        i3 = l;
        l = i2;
        i2 = i3;
      }
      if (l < paramInt2)
        l = paramInt2;
      if (i2 > j)
        i2 = j;
      if (l > i2)
        return false;
      paramArrayOfInt[1] = l;
      label774: paramArrayOfInt[3] = i2;
    }
    else if (l == i2)
    {
      if ((l < paramInt2) || (l > j))
        return false;
      if (k > i1)
      {
        i3 = k;
        k = i1;
        i1 = i3;
      }
      if (k < paramInt1)
        k = paramInt1;
      if (i1 > i)
        i1 = i;
      if (k > i1)
        return false;
      paramArrayOfInt[0] = k;
      paramArrayOfInt[2] = i1;
    }
    else
    {
      int i5 = i1 - k;
      int i6 = i2 - l;
      int i7 = (i5 < 0) ? -i5 : i5;
      int i8 = (i6 < 0) ? -i6 : i6;
      int i9 = (i7 >= i8) ? 1 : 0;
      i3 = outcode(k, l, paramInt1, paramInt2, i, j);
      int i4 = outcode(i1, i2, paramInt1, paramInt2, i, j);
      while (true)
      {
        int i10;
        int i11;
        while (true)
        {
          if ((i3 | i4) == 0)
            break label774;
          if ((i3 & i4) != 0)
            return false;
          if (i3 == 0)
            break;
          if (0 != (i3 & 0x3))
          {
            if (0 != (i3 & 0x1))
              l = paramInt2;
            else
              l = j;
            i11 = l - paramArrayOfInt[1];
            if (i11 < 0)
              i11 = -i11;
            i10 = 2 * i11 * i7 + i8;
            if (i9 != 0)
              i10 += i8 - i7 - 1;
            i10 /= 2 * i8;
            if (i5 < 0)
              i10 = -i10;
            k = paramArrayOfInt[0] + i10;
          }
          else if (0 != (i3 & 0xC))
          {
            if (0 != (i3 & 0x4))
              k = paramInt1;
            else
              k = i;
            i10 = k - paramArrayOfInt[0];
            if (i10 < 0)
              i10 = -i10;
            i11 = 2 * i10 * i8 + i7;
            if (i9 == 0)
              i11 += i7 - i8 - 1;
            i11 /= 2 * i7;
            if (i6 < 0)
              i11 = -i11;
            l = paramArrayOfInt[1] + i11;
          }
          i3 = outcode(k, l, paramInt1, paramInt2, i, j);
        }
        if (0 != (i4 & 0x3))
        {
          if (0 != (i4 & 0x1))
            i2 = paramInt2;
          else
            i2 = j;
          i11 = i2 - paramArrayOfInt[3];
          if (i11 < 0)
            i11 = -i11;
          i10 = 2 * i11 * i7 + i8;
          if (i9 != 0)
            i10 += i8 - i7;
          else
            --i10;
          i10 /= 2 * i8;
          if (i5 > 0)
            i10 = -i10;
          i1 = paramArrayOfInt[2] + i10;
        }
        else if (0 != (i4 & 0xC))
        {
          if (0 != (i4 & 0x4))
            i1 = paramInt1;
          else
            i1 = i;
          i10 = i1 - paramArrayOfInt[2];
          if (i10 < 0)
            i10 = -i10;
          i11 = 2 * i10 * i8 + i7;
          if (i9 != 0)
            --i11;
          else
            i11 += i7 - i8;
          i11 /= 2 * i7;
          if (i6 > 0)
            i11 = -i11;
          i2 = paramArrayOfInt[3] + i11;
        }
        i4 = outcode(i1, i2, paramInt1, paramInt2, i, j);
      }
      paramArrayOfInt[0] = k;
      paramArrayOfInt[1] = l;
      paramArrayOfInt[2] = i1;
      paramArrayOfInt[3] = i2;
      paramArrayOfInt[4] = i5;
      paramArrayOfInt[5] = i6;
      paramArrayOfInt[6] = i7;
      paramArrayOfInt[7] = i8;
    }
    return true;
  }

  static PixelWriter createSolidPixelWriter(SunGraphics2D paramSunGraphics2D, SurfaceData paramSurfaceData)
  {
    ColorModel localColorModel = paramSurfaceData.getColorModel();
    Object localObject = localColorModel.getDataElements(paramSunGraphics2D.eargb, null);
    return new SolidPixelWriter(localObject);
  }

  static PixelWriter createXorPixelWriter(SunGraphics2D paramSunGraphics2D, SurfaceData paramSurfaceData)
  {
    ColorModel localColorModel = paramSurfaceData.getColorModel();
    Object localObject1 = localColorModel.getDataElements(paramSunGraphics2D.eargb, null);
    XORComposite localXORComposite = (XORComposite)paramSunGraphics2D.getComposite();
    int i = localXORComposite.getXorColor().getRGB();
    Object localObject2 = localColorModel.getDataElements(i, null);
    switch (localColorModel.getTransferType())
    {
    case 0:
      return new XorPixelWriter.ByteData(localObject1, localObject2);
    case 1:
    case 2:
      return new XorPixelWriter.ShortData(localObject1, localObject2);
    case 3:
      return new XorPixelWriter.IntData(localObject1, localObject2);
    case 4:
      return new XorPixelWriter.FloatData(localObject1, localObject2);
    case 5:
      return new XorPixelWriter.DoubleData(localObject1, localObject2);
    }
    throw new InternalError("Unsupported XOR pixel type");
  }
}