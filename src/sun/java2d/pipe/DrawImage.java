package sun.java2d.pipe;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorModel;
import java.awt.image.ImageObserver;
import java.awt.image.IndexColorModel;
import java.awt.image.VolatileImage;
import sun.awt.image.BytePackedRaster;
import sun.awt.image.ImageRepresentation;
import sun.awt.image.RemoteOffScreenImage;
import sun.awt.image.ToolkitImage;
import sun.java2d.InvalidPipeException;
import sun.java2d.SunGraphics2D;
import sun.java2d.SurfaceData;
import sun.java2d.loops.Blit;
import sun.java2d.loops.BlitBg;
import sun.java2d.loops.CompositeType;
import sun.java2d.loops.MaskBlit;
import sun.java2d.loops.ScaledBlit;
import sun.java2d.loops.SurfaceType;
import sun.java2d.loops.TransformHelper;

public class DrawImage
  implements DrawImagePipe
{
  private static final double MAX_TX_ERROR = 0.0001D;

  public boolean copyImage(SunGraphics2D paramSunGraphics2D, Image paramImage, int paramInt1, int paramInt2, Color paramColor)
  {
    int i = paramImage.getWidth(null);
    int j = paramImage.getHeight(null);
    if (isSimpleTranslate(paramSunGraphics2D))
      return renderImageCopy(paramSunGraphics2D, paramImage, paramColor, paramInt1 + paramSunGraphics2D.transX, paramInt2 + paramSunGraphics2D.transY, 0, 0, i, j);
    AffineTransform localAffineTransform = paramSunGraphics2D.transform;
    if ((paramInt1 | paramInt2) != 0)
    {
      localAffineTransform = new AffineTransform(localAffineTransform);
      localAffineTransform.translate(paramInt1, paramInt2);
    }
    transformImage(paramSunGraphics2D, paramImage, localAffineTransform, paramSunGraphics2D.interpolationType, 0, 0, i, j, paramColor);
    return true;
  }

  public boolean copyImage(SunGraphics2D paramSunGraphics2D, Image paramImage, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6, Color paramColor)
  {
    if (isSimpleTranslate(paramSunGraphics2D))
      return renderImageCopy(paramSunGraphics2D, paramImage, paramColor, paramInt1 + paramSunGraphics2D.transX, paramInt2 + paramSunGraphics2D.transY, paramInt3, paramInt4, paramInt5, paramInt6);
    scaleImage(paramSunGraphics2D, paramImage, paramInt1, paramInt2, paramInt1 + paramInt5, paramInt2 + paramInt6, paramInt3, paramInt4, paramInt3 + paramInt5, paramInt4 + paramInt6, paramColor);
    return true;
  }

  public boolean scaleImage(SunGraphics2D paramSunGraphics2D, Image paramImage, int paramInt1, int paramInt2, int paramInt3, int paramInt4, Color paramColor)
  {
    int i = paramImage.getWidth(null);
    int j = paramImage.getHeight(null);
    if ((paramInt3 > 0) && (paramInt4 > 0) && (isSimpleTranslate(paramSunGraphics2D)))
    {
      double d1 = paramInt1 + paramSunGraphics2D.transX;
      double d2 = paramInt2 + paramSunGraphics2D.transY;
      double d3 = d1 + paramInt3;
      double d4 = d2 + paramInt4;
      if (renderImageScale(paramSunGraphics2D, paramImage, paramColor, paramSunGraphics2D.interpolationType, 0, 0, i, j, d1, d2, d3, d4))
        return true;
    }
    AffineTransform localAffineTransform = paramSunGraphics2D.transform;
    if (((paramInt1 | paramInt2) != 0) || (paramInt3 != i) || (paramInt4 != j))
    {
      localAffineTransform = new AffineTransform(localAffineTransform);
      localAffineTransform.translate(paramInt1, paramInt2);
      localAffineTransform.scale(paramInt3 / i, paramInt4 / j);
    }
    transformImage(paramSunGraphics2D, paramImage, localAffineTransform, paramSunGraphics2D.interpolationType, 0, 0, i, j, paramColor);
    return true;
  }

  protected void transformImage(SunGraphics2D paramSunGraphics2D, Image paramImage, int paramInt1, int paramInt2, AffineTransform paramAffineTransform, int paramInt3)
  {
    int l;
    int i = paramAffineTransform.getType();
    int j = paramImage.getWidth(null);
    int k = paramImage.getHeight(null);
    if ((paramSunGraphics2D.transformState <= 2) && (((i == 0) || (i == 1))))
    {
      double d1 = paramAffineTransform.getTranslateX();
      double d2 = paramAffineTransform.getTranslateY();
      d1 += paramSunGraphics2D.transform.getTranslateX();
      d2 += paramSunGraphics2D.transform.getTranslateY();
      int i1 = (int)Math.floor(d1 + 0.5D);
      int i2 = (int)Math.floor(d2 + 0.5D);
      if ((paramInt3 == 1) || ((closeToInteger(i1, d1)) && (closeToInteger(i2, d2))))
      {
        renderImageCopy(paramSunGraphics2D, paramImage, null, paramInt1 + i1, paramInt2 + i2, 0, 0, j, k);
        return;
      }
      l = 0;
    }
    else if ((paramSunGraphics2D.transformState <= 3) && ((i & 0x78) == 0))
    {
      localObject = { 0D, 0D, j, k };
      paramAffineTransform.transform(localObject, 0, localObject, 0, 2);
      localObject[0] += paramInt1;
      localObject[1] += paramInt2;
      localObject[2] += paramInt1;
      localObject[3] += paramInt2;
      paramSunGraphics2D.transform.transform(localObject, 0, localObject, 0, 2);
      if (tryCopyOrScale(paramSunGraphics2D, paramImage, 0, 0, j, k, null, paramInt3, localObject))
        return;
      l = 0;
    }
    else
    {
      l = 1;
    }
    Object localObject = new AffineTransform(paramSunGraphics2D.transform);
    ((AffineTransform)localObject).translate(paramInt1, paramInt2);
    ((AffineTransform)localObject).concatenate(paramAffineTransform);
    if (l != 0)
      transformImage(paramSunGraphics2D, paramImage, (AffineTransform)localObject, paramInt3, 0, 0, j, k, null);
    else
      renderImageXform(paramSunGraphics2D, paramImage, (AffineTransform)localObject, paramInt3, 0, 0, j, k, null);
  }

  protected void transformImage(SunGraphics2D paramSunGraphics2D, Image paramImage, AffineTransform paramAffineTransform, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, Color paramColor)
  {
    double[] arrayOfDouble = new double[6];
    arrayOfDouble[2] = (paramInt4 - paramInt2);
    int tmp28_21 = 5;
    tmp28_21[(arrayOfDouble[tmp28_21] = paramInt5 - paramInt3)] = 3;
    paramAffineTransform.transform(arrayOfDouble, 0, arrayOfDouble, 0, 3);
    if ((Math.abs(arrayOfDouble[0] - arrayOfDouble[4]) < 0.0001D) && (Math.abs(arrayOfDouble[3] - arrayOfDouble[5]) < 0.0001D) && (tryCopyOrScale(paramSunGraphics2D, paramImage, paramInt2, paramInt3, paramInt4, paramInt5, paramColor, paramInt1, arrayOfDouble)))
      return;
    renderImageXform(paramSunGraphics2D, paramImage, paramAffineTransform, paramInt1, paramInt2, paramInt3, paramInt4, paramInt5, paramColor);
  }

  protected boolean tryCopyOrScale(SunGraphics2D paramSunGraphics2D, Image paramImage, int paramInt1, int paramInt2, int paramInt3, int paramInt4, Color paramColor, int paramInt5, double[] paramArrayOfDouble)
  {
    double d1 = paramArrayOfDouble[0];
    double d2 = paramArrayOfDouble[1];
    double d3 = paramArrayOfDouble[2] - d1;
    double d4 = paramArrayOfDouble[3] - d2;
    if ((closeToInteger(paramInt3 - paramInt1, d3)) && (closeToInteger(paramInt4 - paramInt2, d4)))
    {
      int i = (int)Math.floor(d1 + 0.5D);
      int j = (int)Math.floor(d2 + 0.5D);
      if ((paramInt5 == 1) || ((closeToInteger(i, d1)) && (closeToInteger(j, d2))))
      {
        renderImageCopy(paramSunGraphics2D, paramImage, paramColor, i, j, paramInt1, paramInt2, paramInt3 - paramInt1, paramInt4 - paramInt2);
        return true;
      }
    }
    return ((d3 > 0D) && (d4 > 0D) && (renderImageScale(paramSunGraphics2D, paramImage, paramColor, paramInt5, paramInt1, paramInt2, paramInt3, paramInt4, paramArrayOfDouble[0], paramArrayOfDouble[1], paramArrayOfDouble[2], paramArrayOfDouble[3])));
  }

  BufferedImage makeBufferedImage(Image paramImage, Color paramColor, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5)
  {
    BufferedImage localBufferedImage = new BufferedImage(paramInt4 - paramInt2, paramInt5 - paramInt3, paramInt1);
    Graphics2D localGraphics2D = localBufferedImage.createGraphics();
    localGraphics2D.setComposite(AlphaComposite.Src);
    if (paramColor != null)
    {
      localGraphics2D.setColor(paramColor);
      localGraphics2D.fillRect(0, 0, paramInt4 - paramInt2, paramInt5 - paramInt3);
      localGraphics2D.setComposite(AlphaComposite.SrcOver);
    }
    localGraphics2D.drawImage(paramImage, -paramInt2, -paramInt3, null);
    localGraphics2D.dispose();
    return localBufferedImage;
  }

  protected void renderImageXform(SunGraphics2D paramSunGraphics2D, Image paramImage, AffineTransform paramAffineTransform, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, Color paramColor)
  {
    AffineTransform localAffineTransform;
    double d3;
    double d4;
    MaskBlit localMaskBlit1;
    Blit localBlit;
    SurfaceData localSurfaceData1 = SurfaceData.getSourceSurfaceData(paramImage, paramSunGraphics2D.surfaceData, paramSunGraphics2D.imageComp, paramColor, true);
    if (localSurfaceData1 == null)
    {
      paramImage = getBufferedImage(paramImage);
      localSurfaceData1 = SurfaceData.getSourceSurfaceData(paramImage, paramSunGraphics2D.surfaceData, paramSunGraphics2D.imageComp, paramColor, true);
      if (localSurfaceData1 == null)
        return;
    }
    if (isBgOperation(localSurfaceData1, paramColor))
    {
      paramImage = makeBufferedImage(paramImage, paramColor, 1, paramInt2, paramInt3, paramInt4, paramInt5);
      paramInt4 -= paramInt2;
      paramInt5 -= paramInt3;
      paramInt2 = paramInt3 = 0;
      localSurfaceData1 = SurfaceData.getSourceSurfaceData(paramImage, paramSunGraphics2D.surfaceData, paramSunGraphics2D.imageComp, null, true);
    }
    SurfaceType localSurfaceType1 = localSurfaceData1.getSurfaceType();
    TransformHelper localTransformHelper = TransformHelper.getFromCache(localSurfaceType1);
    if (localTransformHelper == null)
    {
      int i = (localSurfaceData1.getTransparency() == 1) ? 1 : 2;
      paramImage = makeBufferedImage(paramImage, null, i, paramInt2, paramInt3, paramInt4, paramInt5);
      paramInt4 -= paramInt2;
      paramInt5 -= paramInt3;
      paramInt2 = paramInt3 = 0;
      localSurfaceData1 = SurfaceData.getSourceSurfaceData(paramImage, paramSunGraphics2D.surfaceData, paramSunGraphics2D.imageComp, null, true);
      localSurfaceType1 = localSurfaceData1.getSurfaceType();
      localTransformHelper = TransformHelper.getFromCache(localSurfaceType1);
    }
    try
    {
      localAffineTransform = paramAffineTransform.createInverse();
    }
    catch (NoninvertibleTransformException localNoninvertibleTransformException)
    {
      return;
    }
    double[] arrayOfDouble = new double[8];
    6[(arrayOfDouble[6] = paramInt4 - paramInt2)] = 2;
    7[(arrayOfDouble[7] = paramInt5 - paramInt3)] = 5;
    paramAffineTransform.transform(arrayOfDouble, 0, arrayOfDouble, 0, 4);
    double d1 = d3 = arrayOfDouble[0];
    double d2 = d4 = arrayOfDouble[1];
    for (int j = 2; j < arrayOfDouble.length; j += 2)
    {
      double d5 = arrayOfDouble[j];
      if (d1 > d5);
      if (d3 < d5)
        d3 = (d1 = d5) ? arrayOfDouble : d5;
      d5 = arrayOfDouble[(j + 1)];
      if (d2 > d5);
      if (d4 < d5)
        d4 = (d2 = d5) ? arrayOfDouble : d5;
    }
    j = (int)Math.floor(d1);
    int k = (int)Math.floor(d2);
    int l = (int)Math.ceil(d3);
    int i1 = (int)Math.ceil(d4);
    Region localRegion = paramSunGraphics2D.getCompClip();
    SurfaceType localSurfaceType2 = paramSunGraphics2D.surfaceData.getSurfaceType();
    if (paramSunGraphics2D.compositeState <= 1)
    {
      localMaskBlit1 = MaskBlit.getFromCache(SurfaceType.IntArgbPre, paramSunGraphics2D.imageComp, localSurfaceType2);
      if (localMaskBlit1.getNativePrim() != 3412047325613260800L)
      {
        localTransformHelper.Transform(localMaskBlit1, localSurfaceData1, paramSunGraphics2D.surfaceData, paramSunGraphics2D.composite, localRegion, localAffineTransform, paramInt1, paramInt2, paramInt3, paramInt4, paramInt5, j, k, l, i1, null, 0, 0);
        return;
      }
      localBlit = null;
    }
    else
    {
      localMaskBlit1 = null;
      localBlit = Blit.getFromCache(SurfaceType.IntArgbPre, paramSunGraphics2D.imageComp, localSurfaceType2);
    }
    BufferedImage localBufferedImage = new BufferedImage(l - j, i1 - k, 2);
    SurfaceData localSurfaceData2 = SurfaceData.getDestSurfaceData(localBufferedImage);
    SurfaceType localSurfaceType3 = localSurfaceData2.getSurfaceType();
    MaskBlit localMaskBlit2 = MaskBlit.getFromCache(SurfaceType.IntArgbPre, CompositeType.SrcNoEa, localSurfaceType3);
    int[] arrayOfInt = new int[(i1 - k) * 2 + 2];
    localTransformHelper.Transform(localMaskBlit2, localSurfaceData1, localSurfaceData2, AlphaComposite.Src, null, localAffineTransform, paramInt1, paramInt2, paramInt3, paramInt4, paramInt5, 0, 0, l - j, i1 - k, arrayOfInt, j, k);
    int i2 = 2;
    for (int i3 = arrayOfInt[0]; i3 < arrayOfInt[1]; ++i3)
    {
      int i4 = arrayOfInt[(i2++)];
      int i5 = arrayOfInt[(i2++)];
      if (i4 >= i5)
        break label767:
      label767: if (localMaskBlit1 != null)
        localMaskBlit1.MaskBlit(localSurfaceData2, paramSunGraphics2D.surfaceData, paramSunGraphics2D.composite, localRegion, i4, i3, j + i4, k + i3, i5 - i4, 1, null, 0, 0);
      else
        localBlit.Blit(localSurfaceData2, paramSunGraphics2D.surfaceData, paramSunGraphics2D.composite, localRegion, i4, i3, j + i4, k + i3, i5 - i4, 1);
    }
  }

  protected boolean renderImageCopy(SunGraphics2D paramSunGraphics2D, Image paramImage, Color paramColor, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6)
  {
    Region localRegion = paramSunGraphics2D.getCompClip();
    SurfaceData localSurfaceData = SurfaceData.getSourceSurfaceData(paramImage, paramSunGraphics2D.surfaceData, paramSunGraphics2D.imageComp, paramColor, false);
    if (localSurfaceData == null)
      return false;
    int i = 0;
    while (true)
      try
      {
        SurfaceType localSurfaceType1 = localSurfaceData.getSurfaceType();
        SurfaceType localSurfaceType2 = paramSunGraphics2D.surfaceData.getSurfaceType();
        blitSurfaceData(paramSunGraphics2D, localRegion, localSurfaceData, paramSunGraphics2D.surfaceData, localSurfaceType1, localSurfaceType2, paramInt3, paramInt4, paramInt1, paramInt2, paramInt5, paramInt6, paramColor);
        return true;
      }
      catch (NullPointerException localNullPointerException)
      {
        if ((!(SurfaceData.isNull(paramSunGraphics2D.surfaceData))) && (!(SurfaceData.isNull(localSurfaceData))))
          throw localNullPointerException;
        return false;
      }
      catch (InvalidPipeException localInvalidPipeException)
      {
        ++i;
        localSurfaceData = localSurfaceData.getReplacement();
        localRegion = paramSunGraphics2D.getCompClip();
        if ((SurfaceData.isNull(paramSunGraphics2D.surfaceData)) || (SurfaceData.isNull(localSurfaceData)) || (i > 1))
          return false;
      }
  }

  protected boolean renderImageScale(SunGraphics2D paramSunGraphics2D, Image paramImage, Color paramColor, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, double paramDouble1, double paramDouble2, double paramDouble3, double paramDouble4)
  {
    if (paramInt1 != 1)
      return false;
    Region localRegion = paramSunGraphics2D.getCompClip();
    SurfaceData localSurfaceData = SurfaceData.getSourceSurfaceData(paramImage, paramSunGraphics2D.surfaceData, paramSunGraphics2D.imageComp, paramColor, true);
    if ((localSurfaceData == null) || (isBgOperation(localSurfaceData, paramColor)))
      return false;
    int i = 0;
    while (true)
      try
      {
        SurfaceType localSurfaceType1 = localSurfaceData.getSurfaceType();
        SurfaceType localSurfaceType2 = paramSunGraphics2D.surfaceData.getSurfaceType();
        return scaleSurfaceData(paramSunGraphics2D, localRegion, localSurfaceData, paramSunGraphics2D.surfaceData, localSurfaceType1, localSurfaceType2, paramInt2, paramInt3, paramInt4, paramInt5, paramDouble1, paramDouble2, paramDouble3, paramDouble4);
      }
      catch (NullPointerException localNullPointerException)
      {
        if (!(SurfaceData.isNull(paramSunGraphics2D.surfaceData)))
          throw localNullPointerException;
        return false;
      }
      catch (InvalidPipeException localInvalidPipeException)
      {
        ++i;
        localSurfaceData = localSurfaceData.getReplacement();
        localRegion = paramSunGraphics2D.getCompClip();
        if ((SurfaceData.isNull(paramSunGraphics2D.surfaceData)) || (SurfaceData.isNull(localSurfaceData)) || (i > 1))
          return false;
      }
  }

  public boolean scaleImage(SunGraphics2D paramSunGraphics2D, Image paramImage, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6, int paramInt7, int paramInt8, Color paramColor)
  {
    int i;
    int j;
    int k;
    int l;
    int i1;
    int i2;
    int i3;
    int i4;
    int i5;
    int i6;
    int i7 = 0;
    int i8 = 0;
    int i9 = 0;
    int i10 = 0;
    if (paramInt7 > paramInt5)
    {
      i = paramInt7 - paramInt5;
      i1 = paramInt5;
    }
    else
    {
      i7 = 1;
      i = paramInt5 - paramInt7;
      i1 = paramInt7;
    }
    if (paramInt8 > paramInt6)
    {
      j = paramInt8 - paramInt6;
      i3 = paramInt6;
    }
    else
    {
      i8 = 1;
      j = paramInt6 - paramInt8;
      i3 = paramInt8;
    }
    if (paramInt3 > paramInt1)
    {
      k = paramInt3 - paramInt1;
      i5 = paramInt1;
    }
    else
    {
      k = paramInt1 - paramInt3;
      i9 = 1;
      i5 = paramInt3;
    }
    if (paramInt4 > paramInt2)
    {
      l = paramInt4 - paramInt2;
      i6 = paramInt2;
    }
    else
    {
      l = paramInt2 - paramInt4;
      i10 = 1;
      i6 = paramInt4;
    }
    if ((i <= 0) || (j <= 0))
      return true;
    if ((i7 == i9) && (i8 == i10) && (isSimpleTranslate(paramSunGraphics2D)))
    {
      double d1 = i5 + paramSunGraphics2D.transX;
      double d3 = i6 + paramSunGraphics2D.transY;
      double d5 = d1 + k;
      double d6 = d3 + l;
      if (renderImageScale(paramSunGraphics2D, paramImage, paramColor, paramSunGraphics2D.interpolationType, i1, i3, i1 + i, i3 + j, d1, d3, d5, d6))
        return true;
    }
    AffineTransform localAffineTransform = new AffineTransform(paramSunGraphics2D.transform);
    localAffineTransform.translate(paramInt1, paramInt2);
    double d2 = (paramInt3 - paramInt1) / (paramInt7 - paramInt5);
    double d4 = (paramInt4 - paramInt2) / (paramInt8 - paramInt6);
    localAffineTransform.scale(d2, d4);
    localAffineTransform.translate(i1 - paramInt5, i3 - paramInt6);
    int i11 = paramImage.getWidth(null);
    int i12 = paramImage.getHeight(null);
    i += i1;
    j += i3;
    if (i > i11)
      i = i11;
    if (j > i12)
      j = i12;
    if (i1 < 0)
    {
      localAffineTransform.translate(-i1, 0D);
      i2 = 0;
    }
    if (i3 < 0)
    {
      localAffineTransform.translate(0D, -i3);
      i4 = 0;
    }
    if ((i2 >= i) || (i4 >= j))
      return true;
    transformImage(paramSunGraphics2D, paramImage, localAffineTransform, paramSunGraphics2D.interpolationType, i2, i4, i, j, paramColor);
    return true;
  }

  public static boolean closeToInteger(int paramInt, double paramDouble)
  {
    return (Math.abs(paramDouble - paramInt) < 0.0001D);
  }

  public static boolean isSimpleTranslate(SunGraphics2D paramSunGraphics2D)
  {
    int i = paramSunGraphics2D.transformState;
    if (i <= 1)
      return true;
    if (i >= 3)
      return false;
    return (paramSunGraphics2D.interpolationType == 1);
  }

  protected static boolean isBgOperation(SurfaceData paramSurfaceData, Color paramColor)
  {
    return ((paramSurfaceData == null) || ((paramColor != null) && (paramSurfaceData.getTransparency() != 1)));
  }

  protected BufferedImage getBufferedImage(Image paramImage)
  {
    if (paramImage instanceof RemoteOffScreenImage)
      return ((RemoteOffScreenImage)paramImage).getSnapshot();
    if (paramImage instanceof BufferedImage)
      return ((BufferedImage)paramImage);
    return ((VolatileImage)paramImage).getSnapshot();
  }

  private ColorModel getTransformColorModel(SunGraphics2D paramSunGraphics2D, BufferedImage paramBufferedImage, AffineTransform paramAffineTransform)
  {
    Object localObject2;
    ColorModel localColorModel = paramBufferedImage.getColorModel();
    Object localObject1 = localColorModel;
    if (paramAffineTransform.isIdentity())
      return localObject1;
    int i = paramAffineTransform.getType();
    int j = ((i & (0x18 | 0x20)) != 0) ? 1 : 0;
    if ((j == 0) && (i != 1) && (i != 0))
    {
      localObject2 = new double[4];
      paramAffineTransform.getMatrix(localObject2);
      j = ((localObject2[0] != (int)localObject2[0]) || (localObject2[3] != (int)localObject2[3])) ? 1 : 0;
    }
    if (paramSunGraphics2D.renderHint != 2)
      if (localColorModel instanceof IndexColorModel)
      {
        localObject2 = paramBufferedImage.getRaster();
        IndexColorModel localIndexColorModel = (IndexColorModel)localColorModel;
        label297: if ((j != 0) && (localColorModel.getTransparency() == 1))
          if (localObject2 instanceof BytePackedRaster)
          {
            localObject1 = ColorModel.getRGBdefault();
          }
          else
          {
            double[] arrayOfDouble = new double[6];
            paramAffineTransform.getMatrix(arrayOfDouble);
            if ((arrayOfDouble[1] == 0D) && (arrayOfDouble[2] == 0D) && (arrayOfDouble[4] == 0D) && (arrayOfDouble[5] == 0D))
              break label297:
            int k = localIndexColorModel.getMapSize();
            if (k < 256)
            {
              int[] arrayOfInt = new int[k + 1];
              localIndexColorModel.getRGBs(arrayOfInt);
              arrayOfInt[k] = 0;
              localObject1 = new IndexColorModel(localIndexColorModel.getPixelSize(), k + 1, arrayOfInt, 0, true, k, 0);
            }
            else
            {
              localObject1 = ColorModel.getRGBdefault();
            }
          }
      }
      else if ((j != 0) && (localColorModel.getTransparency() == 1))
      {
        localObject1 = ColorModel.getRGBdefault();
      }
    else if ((localColorModel instanceof IndexColorModel) || ((j != 0) && (localColorModel.getTransparency() == 1)))
      localObject1 = ColorModel.getRGBdefault();
    return ((ColorModel)(ColorModel)localObject1);
  }

  protected void blitSurfaceData(SunGraphics2D paramSunGraphics2D, Region paramRegion, SurfaceData paramSurfaceData1, SurfaceData paramSurfaceData2, SurfaceType paramSurfaceType1, SurfaceType paramSurfaceType2, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6, Color paramColor)
  {
    Object localObject;
    if ((paramInt5 <= 0) || (paramInt6 <= 0))
      return;
    CompositeType localCompositeType = paramSunGraphics2D.imageComp;
    if ((CompositeType.SrcOverNoEa.equals(localCompositeType)) && (((paramSurfaceData1.getTransparency() == 1) || ((paramColor != null) && (paramColor.getTransparency() == 1)))))
      localCompositeType = CompositeType.SrcNoEa;
    if (!(isBgOperation(paramSurfaceData1, paramColor)))
    {
      localObject = Blit.getFromCache(paramSurfaceType1, localCompositeType, paramSurfaceType2);
      ((Blit)localObject).Blit(paramSurfaceData1, paramSurfaceData2, paramSunGraphics2D.composite, paramRegion, paramInt1, paramInt2, paramInt3, paramInt4, paramInt5, paramInt6);
    }
    else
    {
      localObject = BlitBg.getFromCache(paramSurfaceType1, localCompositeType, paramSurfaceType2);
      ((BlitBg)localObject).BlitBg(paramSurfaceData1, paramSurfaceData2, paramSunGraphics2D.composite, paramRegion, paramColor.getRGB(), paramInt1, paramInt2, paramInt3, paramInt4, paramInt5, paramInt6);
    }
  }

  protected boolean scaleSurfaceData(SunGraphics2D paramSunGraphics2D, Region paramRegion, SurfaceData paramSurfaceData1, SurfaceData paramSurfaceData2, SurfaceType paramSurfaceType1, SurfaceType paramSurfaceType2, int paramInt1, int paramInt2, int paramInt3, int paramInt4, double paramDouble1, double paramDouble2, double paramDouble3, double paramDouble4)
  {
    CompositeType localCompositeType = paramSunGraphics2D.imageComp;
    if ((CompositeType.SrcOverNoEa.equals(localCompositeType)) && (paramSurfaceData1.getTransparency() == 1))
      localCompositeType = CompositeType.SrcNoEa;
    ScaledBlit localScaledBlit = ScaledBlit.getFromCache(paramSurfaceType1, localCompositeType, paramSurfaceType2);
    if (localScaledBlit != null)
    {
      localScaledBlit.Scale(paramSurfaceData1, paramSurfaceData2, paramSunGraphics2D.composite, paramRegion, paramInt1, paramInt2, paramInt3, paramInt4, paramDouble1, paramDouble2, paramDouble3, paramDouble4);
      return true;
    }
    return false;
  }

  protected static boolean imageReady(ToolkitImage paramToolkitImage, ImageObserver paramImageObserver)
  {
    if (paramToolkitImage.hasError())
    {
      if (paramImageObserver != null)
        paramImageObserver.imageUpdate(paramToolkitImage, 192, -1, -1, -1, -1);
      return false;
    }
    return true;
  }

  public boolean copyImage(SunGraphics2D paramSunGraphics2D, Image paramImage, int paramInt1, int paramInt2, Color paramColor, ImageObserver paramImageObserver)
  {
    if (!(paramImage instanceof ToolkitImage))
      return copyImage(paramSunGraphics2D, paramImage, paramInt1, paramInt2, paramColor);
    ToolkitImage localToolkitImage = (ToolkitImage)paramImage;
    if (!(imageReady(localToolkitImage, paramImageObserver)))
      return false;
    ImageRepresentation localImageRepresentation = localToolkitImage.getImageRep();
    return localImageRepresentation.drawToBufImage(paramSunGraphics2D, localToolkitImage, paramInt1, paramInt2, paramColor, paramImageObserver);
  }

  public boolean copyImage(SunGraphics2D paramSunGraphics2D, Image paramImage, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6, Color paramColor, ImageObserver paramImageObserver)
  {
    if (!(paramImage instanceof ToolkitImage))
      return copyImage(paramSunGraphics2D, paramImage, paramInt1, paramInt2, paramInt3, paramInt4, paramInt5, paramInt6, paramColor);
    ToolkitImage localToolkitImage = (ToolkitImage)paramImage;
    if (!(imageReady(localToolkitImage, paramImageObserver)))
      return false;
    ImageRepresentation localImageRepresentation = localToolkitImage.getImageRep();
    return localImageRepresentation.drawToBufImage(paramSunGraphics2D, localToolkitImage, paramInt1, paramInt2, paramInt1 + paramInt5, paramInt2 + paramInt6, paramInt3, paramInt4, paramInt3 + paramInt5, paramInt4 + paramInt6, paramColor, paramImageObserver);
  }

  public boolean scaleImage(SunGraphics2D paramSunGraphics2D, Image paramImage, int paramInt1, int paramInt2, int paramInt3, int paramInt4, Color paramColor, ImageObserver paramImageObserver)
  {
    if (!(paramImage instanceof ToolkitImage))
      return scaleImage(paramSunGraphics2D, paramImage, paramInt1, paramInt2, paramInt3, paramInt4, paramColor);
    ToolkitImage localToolkitImage = (ToolkitImage)paramImage;
    if (!(imageReady(localToolkitImage, paramImageObserver)))
      return false;
    ImageRepresentation localImageRepresentation = localToolkitImage.getImageRep();
    return localImageRepresentation.drawToBufImage(paramSunGraphics2D, localToolkitImage, paramInt1, paramInt2, paramInt3, paramInt4, paramColor, paramImageObserver);
  }

  public boolean scaleImage(SunGraphics2D paramSunGraphics2D, Image paramImage, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6, int paramInt7, int paramInt8, Color paramColor, ImageObserver paramImageObserver)
  {
    if (!(paramImage instanceof ToolkitImage))
      return scaleImage(paramSunGraphics2D, paramImage, paramInt1, paramInt2, paramInt3, paramInt4, paramInt5, paramInt6, paramInt7, paramInt8, paramColor);
    ToolkitImage localToolkitImage = (ToolkitImage)paramImage;
    if (!(imageReady(localToolkitImage, paramImageObserver)))
      return false;
    ImageRepresentation localImageRepresentation = localToolkitImage.getImageRep();
    return localImageRepresentation.drawToBufImage(paramSunGraphics2D, localToolkitImage, paramInt1, paramInt2, paramInt3, paramInt4, paramInt5, paramInt6, paramInt7, paramInt8, paramColor, paramImageObserver);
  }

  public boolean transformImage(SunGraphics2D paramSunGraphics2D, Image paramImage, AffineTransform paramAffineTransform, ImageObserver paramImageObserver)
  {
    if (!(paramImage instanceof ToolkitImage))
    {
      transformImage(paramSunGraphics2D, paramImage, 0, 0, paramAffineTransform, paramSunGraphics2D.interpolationType);
      return true;
    }
    ToolkitImage localToolkitImage = (ToolkitImage)paramImage;
    if (!(imageReady(localToolkitImage, paramImageObserver)))
      return false;
    ImageRepresentation localImageRepresentation = localToolkitImage.getImageRep();
    return localImageRepresentation.drawToBufImage(paramSunGraphics2D, localToolkitImage, paramAffineTransform, paramImageObserver);
  }

  public void transformImage(SunGraphics2D paramSunGraphics2D, BufferedImage paramBufferedImage, BufferedImageOp paramBufferedImageOp, int paramInt1, int paramInt2)
  {
    if (paramBufferedImageOp != null)
    {
      if (paramBufferedImageOp instanceof AffineTransformOp)
      {
        AffineTransformOp localAffineTransformOp = (AffineTransformOp)paramBufferedImageOp;
        transformImage(paramSunGraphics2D, paramBufferedImage, paramInt1, paramInt2, localAffineTransformOp.getTransform(), localAffineTransformOp.getInterpolationType());
        return;
      }
      paramBufferedImage = paramBufferedImageOp.filter(paramBufferedImage, null);
    }
    copyImage(paramSunGraphics2D, paramBufferedImage, paramInt1, paramInt2, null);
  }
}