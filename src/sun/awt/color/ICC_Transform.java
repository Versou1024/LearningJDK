package sun.awt.color;

import java.awt.color.ColorSpace;
import java.awt.color.ICC_Profile;
import java.awt.color.ProfileDataException;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DirectColorModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;

public class ICC_Transform
{
  long ID;
  public static final int Any = -1;
  public static final int In = 1;
  public static final int Out = 2;
  public static final int Gamut = 3;
  public static final int Simulation = 4;

  public ICC_Transform()
  {
  }

  public ICC_Transform(ICC_Profile paramICC_Profile, int paramInt1, int paramInt2)
  {
    if (paramICC_Profile == null)
      CMM.checkStatus(503);
    CMM.checkStatus(CMM.cmmGetTransform(paramICC_Profile, paramInt1, paramInt2, this));
  }

  public ICC_Transform(ICC_Transform[] paramArrayOfICC_Transform)
  {
    int i = CMM.cmmCombineTransforms(paramArrayOfICC_Transform, this);
    if ((i != 0) || (this.ID == 3412046964836007936L))
      throw new ProfileDataException("Invalid profile sequence");
  }

  long getID()
  {
    return this.ID;
  }

  public void finalize()
  {
    CMM.checkStatus(CMM.cmmFreeTransform(this.ID));
  }

  public int getNumInComponents()
  {
    int[] arrayOfInt = new int[2];
    CMM.checkStatus(CMM.cmmGetNumComponents(this.ID, arrayOfInt));
    return arrayOfInt[0];
  }

  public int getNumOutComponents()
  {
    int[] arrayOfInt = new int[2];
    CMM.checkStatus(CMM.cmmGetNumComponents(this.ID, arrayOfInt));
    return arrayOfInt[1];
  }

  public void colorConvert(BufferedImage paramBufferedImage1, BufferedImage paramBufferedImage2)
  {
    CMMImageLayout localCMMImageLayout2;
    float[] arrayOfFloat5;
    Object localObject3;
    Object localObject4;
    Object localObject5;
    float[] arrayOfFloat6;
    float[] arrayOfFloat7;
    int i6;
    pelArrayInfo localpelArrayInfo;
    int i7;
    int i9;
    int i11;
    CMMImageLayout localCMMImageLayout1 = getImageLayout(paramBufferedImage1);
    if (localCMMImageLayout1 != null)
    {
      localCMMImageLayout2 = getImageLayout(paramBufferedImage2);
      if (localCMMImageLayout2 != null)
      {
        synchronized (this)
        {
          CMM.checkStatus(CMM.cmmColorConvert(this.ID, localCMMImageLayout1, localCMMImageLayout2));
        }
        return;
      }
    }
    ??? = paramBufferedImage1.getRaster();
    WritableRaster localWritableRaster = paramBufferedImage2.getRaster();
    ColorModel localColorModel1 = paramBufferedImage1.getColorModel();
    ColorModel localColorModel2 = paramBufferedImage2.getColorModel();
    int i = paramBufferedImage1.getWidth();
    int j = paramBufferedImage1.getHeight();
    int k = localColorModel1.getNumColorComponents();
    int l = localColorModel2.getNumColorComponents();
    int i1 = 8;
    float f = 255.0F;
    for (int i2 = 0; i2 < k; ++i2)
      if (localColorModel1.getComponentSize(i2) > 8)
      {
        i1 = 16;
        f = 65535.0F;
      }
    for (i2 = 0; i2 < l; ++i2)
      if (localColorModel2.getComponentSize(i2) > 8)
      {
        i1 = 16;
        f = 65535.0F;
      }
    float[] arrayOfFloat1 = new float[k];
    float[] arrayOfFloat2 = new float[k];
    ColorSpace localColorSpace = localColorModel1.getColorSpace();
    for (int i3 = 0; i3 < k; ++i3)
    {
      arrayOfFloat1[i3] = localColorSpace.getMinValue(i3);
      arrayOfFloat2[i3] = (f / (localColorSpace.getMaxValue(i3) - arrayOfFloat1[i3]));
    }
    localColorSpace = localColorModel2.getColorSpace();
    float[] arrayOfFloat3 = new float[l];
    float[] arrayOfFloat4 = new float[l];
    for (int i4 = 0; i4 < l; ++i4)
    {
      arrayOfFloat3[i4] = localColorSpace.getMinValue(i4);
      arrayOfFloat4[i4] = ((localColorSpace.getMaxValue(i4) - arrayOfFloat3[i4]) / f);
    }
    boolean bool = localColorModel2.hasAlpha();
    int i5 = ((localColorModel1.hasAlpha()) && (bool)) ? 1 : 0;
    if (bool)
      arrayOfFloat5 = new float[l + 1];
    else
      arrayOfFloat5 = new float[l];
    if (i1 == 8)
    {
      localObject3 = new byte[i * k];
      localObject4 = new byte[i * l];
      arrayOfFloat7 = null;
      if (i5 != 0)
        arrayOfFloat7 = new float[i];
      localpelArrayInfo = new pelArrayInfo(this, localObject3, localObject4);
      localCMMImageLayout1 = new CMMImageLayout(localObject3, localpelArrayInfo.nPels, localpelArrayInfo.nSrc);
      localCMMImageLayout2 = new CMMImageLayout(localObject4, localpelArrayInfo.nPels, localpelArrayInfo.nDest);
      for (i7 = 0; i7 < j; ++i7)
      {
        localObject5 = null;
        arrayOfFloat6 = null;
        i6 = 0;
        for (int i8 = 0; i8 < i; ++i8)
        {
          localObject5 = ((Raster)???).getDataElements(i8, i7, localObject5);
          arrayOfFloat6 = localColorModel1.getNormalizedComponents(localObject5, arrayOfFloat6, 0);
          for (i11 = 0; i11 < k; ++i11)
            localObject3[(i6++)] = (byte)(int)((arrayOfFloat6[i11] - arrayOfFloat1[i11]) * arrayOfFloat2[i11] + 0.5F);
          if (i5 != 0)
            arrayOfFloat7[i8] = arrayOfFloat6[k];
        }
        synchronized (this)
        {
          CMM.checkStatus(CMM.cmmColorConvert(this.ID, localCMMImageLayout1, localCMMImageLayout2));
        }
        localObject5 = null;
        i6 = 0;
        for (i9 = 0; i9 < i; ++i9)
        {
          for (i11 = 0; i11 < l; ++i11)
            arrayOfFloat5[i11] = ((localObject4[(i6++)] & 0xFF) * arrayOfFloat4[i11] + arrayOfFloat3[i11]);
          if (i5 != 0)
            arrayOfFloat5[l] = arrayOfFloat7[i9];
          else if (bool)
            arrayOfFloat5[l] = 1F;
          localObject5 = localColorModel2.getDataElements(arrayOfFloat5, 0, localObject5);
          localWritableRaster.setDataElements(i9, i7, localObject5);
        }
      }
    }
    else
    {
      localObject3 = new short[i * k];
      localObject4 = new short[i * l];
      arrayOfFloat7 = null;
      if (i5 != 0)
        arrayOfFloat7 = new float[i];
      localpelArrayInfo = new pelArrayInfo(this, localObject3, localObject4);
      localCMMImageLayout1 = new CMMImageLayout(localObject3, localpelArrayInfo.nPels, localpelArrayInfo.nSrc);
      localCMMImageLayout2 = new CMMImageLayout(localObject4, localpelArrayInfo.nPels, localpelArrayInfo.nDest);
      for (i7 = 0; i7 < j; ++i7)
      {
        localObject5 = null;
        arrayOfFloat6 = null;
        i6 = 0;
        for (i9 = 0; i9 < i; ++i9)
        {
          localObject5 = ((Raster)???).getDataElements(i9, i7, localObject5);
          arrayOfFloat6 = localColorModel1.getNormalizedComponents(localObject5, arrayOfFloat6, 0);
          for (i11 = 0; i11 < k; ++i11)
            localObject3[(i6++)] = (short)(int)((arrayOfFloat6[i11] - arrayOfFloat1[i11]) * arrayOfFloat2[i11] + 0.5F);
          if (i5 != 0)
            arrayOfFloat7[i9] = arrayOfFloat6[k];
        }
        synchronized (this)
        {
          CMM.checkStatus(CMM.cmmColorConvert(this.ID, localCMMImageLayout1, localCMMImageLayout2));
        }
        localObject5 = null;
        i6 = 0;
        for (int i10 = 0; i10 < i; ++i10)
        {
          for (i11 = 0; i11 < l; ++i11)
            arrayOfFloat5[i11] = ((localObject4[(i6++)] & 0xFFFF) * arrayOfFloat4[i11] + arrayOfFloat3[i11]);
          if (i5 != 0)
            arrayOfFloat5[l] = arrayOfFloat7[i10];
          else if (bool)
            arrayOfFloat5[l] = 1F;
          localObject5 = localColorModel2.getDataElements(arrayOfFloat5, 0, localObject5);
          localWritableRaster.setDataElements(i10, i7, localObject5);
        }
      }
    }
  }

  private CMMImageLayout getImageLayout(BufferedImage paramBufferedImage)
  {
    SampleModel localSampleModel;
    int j;
    int k;
    switch (paramBufferedImage.getType())
    {
    case 1:
    case 2:
    case 4:
      return new CMMImageLayout(paramBufferedImage);
    case 5:
    case 6:
      localObject = (ComponentColorModel)paramBufferedImage.getColorModel();
      if ((localObject.getClass() == ComponentColorModel.class) || (checkMinMaxScaling((ComponentColorModel)localObject)))
        return new CMMImageLayout(paramBufferedImage);
      return null;
    case 10:
      localObject = (ComponentColorModel)paramBufferedImage.getColorModel();
      if (((ComponentColorModel)localObject).getComponentSize(0) != 8)
        return null;
      if ((localObject.getClass() == ComponentColorModel.class) || (checkMinMaxScaling((ComponentColorModel)localObject)))
        return new CMMImageLayout(paramBufferedImage);
      return null;
    case 11:
      localObject = (ComponentColorModel)paramBufferedImage.getColorModel();
      if (((ComponentColorModel)localObject).getComponentSize(0) != 16)
        return null;
      if ((localObject.getClass() == ComponentColorModel.class) || (checkMinMaxScaling((ComponentColorModel)localObject)))
        return new CMMImageLayout(paramBufferedImage);
      return null;
    case 3:
    case 7:
    case 8:
    case 9:
    }
    Object localObject = paramBufferedImage.getColorModel();
    if (localObject instanceof DirectColorModel)
    {
      int i3;
      int i4;
      int i5;
      localSampleModel = paramBufferedImage.getSampleModel();
      if (!(localSampleModel instanceof SinglePixelPackedSampleModel))
        return null;
      if (((ColorModel)localObject).getTransferType() != 3)
        return null;
      if ((((ColorModel)localObject).hasAlpha()) && (((ColorModel)localObject).isAlphaPremultiplied()))
        return null;
      DirectColorModel localDirectColorModel = (DirectColorModel)localObject;
      j = localDirectColorModel.getRedMask();
      k = localDirectColorModel.getGreenMask();
      int l = localDirectColorModel.getBlueMask();
      int i1 = localDirectColorModel.getAlphaMask();
      int i2 = i3 = i4 = i5 = -1;
      int i6 = 0;
      int i7 = 3;
      if (i1 != 0)
        i7 = 4;
      int i8 = 0;
      int i9 = -16777216;
      while (i8 < 4)
      {
        if (j == i9)
        {
          i2 = i8;
          ++i6;
        }
        else if (k == i9)
        {
          i3 = i8;
          ++i6;
        }
        else if (l == i9)
        {
          i4 = i8;
          ++i6;
        }
        else if (i1 == i9)
        {
          i5 = i8;
          ++i6;
        }
        ++i8;
        i9 >>>= 8;
      }
      if (i6 != i7)
        return null;
      return new CMMImageLayout(paramBufferedImage, (SinglePixelPackedSampleModel)localSampleModel, i2, i3, i4, i5);
    }
    if (localObject instanceof ComponentColorModel)
    {
      localSampleModel = paramBufferedImage.getSampleModel();
      if (!(localSampleModel instanceof ComponentSampleModel))
        return null;
      if ((((ColorModel)localObject).hasAlpha()) && (((ColorModel)localObject).isAlphaPremultiplied()))
        return null;
      int i = ((ColorModel)localObject).getNumComponents();
      if (localSampleModel.getNumBands() != i)
        return null;
      j = ((ColorModel)localObject).getTransferType();
      if (j == 0)
        for (k = 0; k < i; ++k)
          if (((ColorModel)localObject).getComponentSize(k) != 8)
            return null;
      else if (j == 1)
        for (k = 0; k < i; ++k)
          if (((ColorModel)localObject).getComponentSize(k) != 16)
            return null;
      else
        return null;
      ComponentColorModel localComponentColorModel = (ComponentColorModel)localObject;
      if ((localComponentColorModel.getClass() == ComponentColorModel.class) || (checkMinMaxScaling(localComponentColorModel)))
        return new CMMImageLayout(paramBufferedImage, (ComponentSampleModel)localSampleModel);
      return null;
    }
    return ((CMMImageLayout)null);
  }

  private boolean checkMinMaxScaling(ComponentColorModel paramComponentColorModel)
  {
    float[] arrayOfFloat1;
    float[] arrayOfFloat2;
    float f1;
    int i = paramComponentColorModel.getNumComponents();
    int j = paramComponentColorModel.getNumColorComponents();
    int[] arrayOfInt = paramComponentColorModel.getComponentSize();
    boolean bool = paramComponentColorModel.hasAlpha();
    switch (paramComponentColorModel.getTransferType())
    {
    case 0:
      localObject = new byte[i];
      for (k = 0; k < j; ++k)
        localObject[k] = 0;
      if (bool)
        localObject[j] = (byte)((1 << arrayOfInt[j]) - 1);
      arrayOfFloat1 = paramComponentColorModel.getNormalizedComponents(localObject, null, 0);
      for (k = 0; k < j; ++k)
        localObject[k] = (byte)((1 << arrayOfInt[k]) - 1);
      arrayOfFloat2 = paramComponentColorModel.getNormalizedComponents(localObject, null, 0);
      f1 = 256.0F;
      break;
    case 1:
      localObject = new short[i];
      for (k = 0; k < j; ++k)
        localObject[k] = 0;
      if (bool)
        localObject[j] = (short)(byte)((1 << arrayOfInt[j]) - 1);
      arrayOfFloat1 = paramComponentColorModel.getNormalizedComponents(localObject, null, 0);
      for (k = 0; k < j; ++k)
        localObject[k] = (short)(byte)((1 << arrayOfInt[k]) - 1);
      arrayOfFloat2 = paramComponentColorModel.getNormalizedComponents(localObject, null, 0);
      f1 = 65536.0F;
      break;
    default:
      return false;
    }
    Object localObject = paramComponentColorModel.getColorSpace();
    for (int k = 0; k < j; ++k)
    {
      float f2 = ((ColorSpace)localObject).getMinValue(k);
      float f3 = ((ColorSpace)localObject).getMaxValue(k);
      float f4 = (f3 - f2) / f1;
      f2 -= arrayOfFloat1[k];
      if (f2 < 0F)
        f2 = -f2;
      f3 -= arrayOfFloat2[k];
      if (f3 < 0F)
        f3 = -f3;
      if ((f2 > f4) || (f3 > f4))
        return false;
    }
    return true;
  }

  public void colorConvert(Raster paramRaster, WritableRaster paramWritableRaster, float[] paramArrayOfFloat1, float[] paramArrayOfFloat2, float[] paramArrayOfFloat3, float[] paramArrayOfFloat4)
  {
    int k;
    int l;
    SampleModel localSampleModel1 = paramRaster.getSampleModel();
    SampleModel localSampleModel2 = paramWritableRaster.getSampleModel();
    int i = paramRaster.getTransferType();
    int j = paramWritableRaster.getTransferType();
    if ((i == 4) || (i == 5))
      k = 1;
    else
      k = 0;
    if ((j == 4) || (j == 5))
      l = 1;
    else
      l = 0;
    int i1 = paramRaster.getWidth();
    int i2 = paramRaster.getHeight();
    int i3 = paramRaster.getNumBands();
    int i4 = paramWritableRaster.getNumBands();
    float[] arrayOfFloat1 = new float[i3];
    float[] arrayOfFloat2 = new float[i4];
    float[] arrayOfFloat3 = new float[i3];
    float[] arrayOfFloat4 = new float[i4];
    for (int i5 = 0; i5 < i3; ++i5)
      if (k != 0)
      {
        arrayOfFloat1[i5] = (65535.0F / (paramArrayOfFloat2[i5] - paramArrayOfFloat1[i5]));
        arrayOfFloat3[i5] = paramArrayOfFloat1[i5];
      }
      else
      {
        if (i == 2)
          arrayOfFloat1[i5] = 2.000030517578125F;
        else
          arrayOfFloat1[i5] = (65535.0F / ((1 << localSampleModel1.getSampleSize(i5)) - 1));
        arrayOfFloat3[i5] = 0F;
      }
    for (i5 = 0; i5 < i4; ++i5)
      if (l != 0)
      {
        arrayOfFloat2[i5] = ((paramArrayOfFloat4[i5] - paramArrayOfFloat3[i5]) / 65535.0F);
        arrayOfFloat4[i5] = paramArrayOfFloat3[i5];
      }
      else
      {
        if (j == 2)
          arrayOfFloat2[i5] = 0.49999237060546875F;
        else
          arrayOfFloat2[i5] = (((1 << localSampleModel2.getSampleSize(i5)) - 1) / 65535.0F);
        arrayOfFloat4[i5] = 0F;
      }
    i5 = paramRaster.getMinY();
    int i6 = paramWritableRaster.getMinY();
    short[] arrayOfShort1 = new short[i1 * i3];
    short[] arrayOfShort2 = new short[i1 * i4];
    pelArrayInfo localpelArrayInfo = new pelArrayInfo(this, arrayOfShort1, arrayOfShort2);
    CMMImageLayout localCMMImageLayout1 = new CMMImageLayout(arrayOfShort1, localpelArrayInfo.nPels, localpelArrayInfo.nSrc);
    CMMImageLayout localCMMImageLayout2 = new CMMImageLayout(arrayOfShort2, localpelArrayInfo.nPels, localpelArrayInfo.nDest);
    int i10 = 0;
    while (i10 < i2)
    {
      float f;
      int i13;
      int i7 = paramRaster.getMinX();
      int i9 = 0;
      int i11 = 0;
      while (i11 < i1)
      {
        for (i13 = 0; i13 < i3; ++i13)
        {
          f = paramRaster.getSampleFloat(i7, i5, i13);
          arrayOfShort1[(i9++)] = (short)(int)((f - arrayOfFloat3[i13]) * arrayOfFloat1[i13] + 0.5F);
        }
        ++i11;
        ++i7;
      }
      synchronized (this)
      {
        CMM.checkStatus(CMM.cmmColorConvert(this.ID, localCMMImageLayout1, localCMMImageLayout2));
      }
      int i8 = paramWritableRaster.getMinX();
      i9 = 0;
      int i12 = 0;
      while (i12 < i1)
      {
        for (i13 = 0; i13 < i4; ++i13)
        {
          f = (arrayOfShort2[(i9++)] & 0xFFFF) * arrayOfFloat2[i13] + arrayOfFloat4[i13];
          paramWritableRaster.setSample(i8, i6, i13, f);
        }
        ++i12;
        ++i8;
      }
      ++i10;
      ++i5;
      ++i6;
    }
  }

  public void colorConvert(Raster paramRaster, WritableRaster paramWritableRaster)
  {
    CMMImageLayout localCMMImageLayout2;
    int i7;
    int i8;
    int i9;
    Object localObject3;
    Object localObject4;
    int i10;
    pelArrayInfo localpelArrayInfo;
    int i11;
    int i13;
    int i15;
    CMMImageLayout localCMMImageLayout1 = getImageLayout(paramRaster);
    if (localCMMImageLayout1 != null)
    {
      localCMMImageLayout2 = getImageLayout(paramWritableRaster);
      if (localCMMImageLayout2 != null)
      {
        synchronized (this)
        {
          CMM.checkStatus(CMM.cmmColorConvert(this.ID, localCMMImageLayout1, localCMMImageLayout2));
        }
        return;
      }
    }
    ??? = paramRaster.getSampleModel();
    SampleModel localSampleModel = paramWritableRaster.getSampleModel();
    int i = paramRaster.getTransferType();
    int j = paramWritableRaster.getTransferType();
    int k = paramRaster.getWidth();
    int l = paramRaster.getHeight();
    int i1 = paramRaster.getNumBands();
    int i2 = paramWritableRaster.getNumBands();
    int i3 = 8;
    float f = 255.0F;
    for (int i4 = 0; i4 < i1; ++i4)
      if (((SampleModel)???).getSampleSize(i4) > 8)
      {
        i3 = 16;
        f = 65535.0F;
      }
    for (i4 = 0; i4 < i2; ++i4)
      if (localSampleModel.getSampleSize(i4) > 8)
      {
        i3 = 16;
        f = 65535.0F;
      }
    float[] arrayOfFloat1 = new float[i1];
    float[] arrayOfFloat2 = new float[i2];
    for (int i5 = 0; i5 < i1; ++i5)
      if (i == 2)
        arrayOfFloat1[i5] = (f / 32767.0F);
      else
        arrayOfFloat1[i5] = (f / ((1 << ((SampleModel)???).getSampleSize(i5)) - 1));
    for (i5 = 0; i5 < i2; ++i5)
      if (j == 2)
        arrayOfFloat2[i5] = (32767.0F / f);
      else
        arrayOfFloat2[i5] = (((1 << localSampleModel.getSampleSize(i5)) - 1) / f);
    i5 = paramRaster.getMinY();
    int i6 = paramWritableRaster.getMinY();
    if (i3 == 8)
    {
      localObject3 = new byte[k * i1];
      localObject4 = new byte[k * i2];
      localpelArrayInfo = new pelArrayInfo(this, localObject3, localObject4);
      localCMMImageLayout1 = new CMMImageLayout(localObject3, localpelArrayInfo.nPels, localpelArrayInfo.nSrc);
      localCMMImageLayout2 = new CMMImageLayout(localObject4, localpelArrayInfo.nPels, localpelArrayInfo.nDest);
      i11 = 0;
      while (i11 < l)
      {
        i7 = paramRaster.getMinX();
        i10 = 0;
        int i12 = 0;
        while (i12 < k)
        {
          for (i15 = 0; i15 < i1; ++i15)
          {
            i9 = paramRaster.getSample(i7, i5, i15);
            localObject3[(i10++)] = (byte)(int)(i9 * arrayOfFloat1[i15] + 0.5F);
          }
          ++i12;
          ++i7;
        }
        synchronized (this)
        {
          CMM.checkStatus(CMM.cmmColorConvert(this.ID, localCMMImageLayout1, localCMMImageLayout2));
        }
        i8 = paramWritableRaster.getMinX();
        i10 = 0;
        i13 = 0;
        while (i13 < k)
        {
          for (i15 = 0; i15 < i2; ++i15)
          {
            i9 = (int)((localObject4[(i10++)] & 0xFF) * arrayOfFloat2[i15] + 0.5F);
            paramWritableRaster.setSample(i8, i6, i15, i9);
          }
          ++i13;
          ++i8;
        }
        ++i11;
        ++i5;
        ++i6;
      }
    }
    else
    {
      localObject3 = new short[k * i1];
      localObject4 = new short[k * i2];
      localpelArrayInfo = new pelArrayInfo(this, localObject3, localObject4);
      localCMMImageLayout1 = new CMMImageLayout(localObject3, localpelArrayInfo.nPels, localpelArrayInfo.nSrc);
      localCMMImageLayout2 = new CMMImageLayout(localObject4, localpelArrayInfo.nPels, localpelArrayInfo.nDest);
      i11 = 0;
      while (i11 < l)
      {
        i7 = paramRaster.getMinX();
        i10 = 0;
        i13 = 0;
        while (i13 < k)
        {
          for (i15 = 0; i15 < i1; ++i15)
          {
            i9 = paramRaster.getSample(i7, i5, i15);
            localObject3[(i10++)] = (short)(int)(i9 * arrayOfFloat1[i15] + 0.5F);
          }
          ++i13;
          ++i7;
        }
        synchronized (this)
        {
          CMM.checkStatus(CMM.cmmColorConvert(this.ID, localCMMImageLayout1, localCMMImageLayout2));
        }
        i8 = paramWritableRaster.getMinX();
        i10 = 0;
        int i14 = 0;
        while (i14 < k)
        {
          for (i15 = 0; i15 < i2; ++i15)
          {
            i9 = (int)((localObject4[(i10++)] & 0xFFFF) * arrayOfFloat2[i15] + 0.5F);
            paramWritableRaster.setSample(i8, i6, i15, i9);
          }
          ++i14;
          ++i8;
        }
        ++i11;
        ++i5;
        ++i6;
      }
    }
  }

  private CMMImageLayout getImageLayout(Raster paramRaster)
  {
    SampleModel localSampleModel = paramRaster.getSampleModel();
    if (localSampleModel instanceof ComponentSampleModel)
    {
      int k;
      int i = paramRaster.getNumBands();
      int j = localSampleModel.getTransferType();
      if (j == 0)
        for (k = 0; k < i; ++k)
          if (localSampleModel.getSampleSize(k) != 8)
            return null;
      else if (j == 1)
        for (k = 0; k < i; ++k)
          if (localSampleModel.getSampleSize(k) != 16)
            return null;
      else
        return null;
      return new CMMImageLayout(paramRaster, (ComponentSampleModel)localSampleModel);
    }
    return null;
  }

  public short[] colorConvert(short[] paramArrayOfShort1, short[] paramArrayOfShort2)
  {
    short[] arrayOfShort;
    pelArrayInfo localpelArrayInfo = new pelArrayInfo(this, paramArrayOfShort1, paramArrayOfShort2);
    if (paramArrayOfShort2 != null)
      arrayOfShort = paramArrayOfShort2;
    else
      arrayOfShort = new short[localpelArrayInfo.destSize];
    CMMImageLayout localCMMImageLayout1 = new CMMImageLayout(paramArrayOfShort1, localpelArrayInfo.nPels, localpelArrayInfo.nSrc);
    CMMImageLayout localCMMImageLayout2 = new CMMImageLayout(arrayOfShort, localpelArrayInfo.nPels, localpelArrayInfo.nDest);
    synchronized (this)
    {
      CMM.checkStatus(CMM.cmmColorConvert(this.ID, localCMMImageLayout1, localCMMImageLayout2));
    }
    return arrayOfShort;
  }

  public byte[] colorConvert(byte[] paramArrayOfByte1, byte[] paramArrayOfByte2)
  {
    byte[] arrayOfByte;
    pelArrayInfo localpelArrayInfo = new pelArrayInfo(this, paramArrayOfByte1, paramArrayOfByte2);
    if (paramArrayOfByte2 != null)
      arrayOfByte = paramArrayOfByte2;
    else
      arrayOfByte = new byte[localpelArrayInfo.destSize];
    CMMImageLayout localCMMImageLayout1 = new CMMImageLayout(paramArrayOfByte1, localpelArrayInfo.nPels, localpelArrayInfo.nSrc);
    CMMImageLayout localCMMImageLayout2 = new CMMImageLayout(arrayOfByte, localpelArrayInfo.nPels, localpelArrayInfo.nDest);
    synchronized (this)
    {
      CMM.checkStatus(CMM.cmmColorConvert(this.ID, localCMMImageLayout1, localCMMImageLayout2));
    }
    return arrayOfByte;
  }

  static
  {
    if (ProfileDeferralMgr.deferring)
      ProfileDeferralMgr.activateProfiles();
  }
}