package sun.java2d.pipe;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.LinearGradientPaint;
import java.awt.MultipleGradientPaint.ColorSpaceType;
import java.awt.MultipleGradientPaint.CycleMethod;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.TexturePaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import sun.awt.image.PixelConverter;
import sun.awt.image.PixelConverter.ArgbPre;
import sun.java2d.SunGraphics2D;
import sun.java2d.SurfaceData;
import sun.java2d.loops.CompositeType;

public class BufferedPaints
{
  public static final int MULTI_MAX_FRACTIONS = 12;

  static void setPaint(RenderQueue paramRenderQueue, SunGraphics2D paramSunGraphics2D, Paint paramPaint, int paramInt)
  {
    if (paramSunGraphics2D.paintState <= 1)
    {
      setColor(paramRenderQueue, paramSunGraphics2D.pixel);
    }
    else
    {
      boolean bool = (paramInt & 0x2) != 0;
      switch (paramSunGraphics2D.paintState)
      {
      case 2:
        setGradientPaint(paramRenderQueue, paramSunGraphics2D, (GradientPaint)paramPaint, bool);
        break;
      case 3:
        setLinearGradientPaint(paramRenderQueue, paramSunGraphics2D, (LinearGradientPaint)paramPaint, bool);
        break;
      case 4:
        setRadialGradientPaint(paramRenderQueue, paramSunGraphics2D, (RadialGradientPaint)paramPaint, bool);
        break;
      case 5:
        setTexturePaint(paramRenderQueue, paramSunGraphics2D, (TexturePaint)paramPaint, bool);
      }
    }
  }

  static void resetPaint(RenderQueue paramRenderQueue)
  {
    paramRenderQueue.ensureCapacity(4);
    RenderBuffer localRenderBuffer = paramRenderQueue.getBuffer();
    localRenderBuffer.putInt(100);
  }

  private static void setColor(RenderQueue paramRenderQueue, int paramInt)
  {
    paramRenderQueue.ensureCapacity(8);
    RenderBuffer localRenderBuffer = paramRenderQueue.getBuffer();
    localRenderBuffer.putInt(101);
    localRenderBuffer.putInt(paramInt);
  }

  private static void setGradientPaint(RenderQueue paramRenderQueue, AffineTransform paramAffineTransform, Color paramColor1, Color paramColor2, Point2D paramPoint2D1, Point2D paramPoint2D2, boolean paramBoolean1, boolean paramBoolean2)
  {
    double d4;
    double d5;
    double d6;
    PixelConverter localPixelConverter = PixelConverter.ArgbPre.instance;
    int i = localPixelConverter.rgbToPixel(paramColor1.getRGB(), null);
    int j = localPixelConverter.rgbToPixel(paramColor2.getRGB(), null);
    double d1 = paramPoint2D1.getX();
    double d2 = paramPoint2D1.getY();
    paramAffineTransform.translate(d1, d2);
    d1 = paramPoint2D2.getX() - d1;
    d2 = paramPoint2D2.getY() - d2;
    double d3 = Math.sqrt(d1 * d1 + d2 * d2);
    paramAffineTransform.rotate(d1, d2);
    paramAffineTransform.scale(2.0D * d3, 1D);
    paramAffineTransform.translate(-0.25D, 0D);
    try
    {
      paramAffineTransform.invert();
      d4 = paramAffineTransform.getScaleX();
      d5 = paramAffineTransform.getShearX();
      d6 = paramAffineTransform.getTranslateX();
    }
    catch (NoninvertibleTransformException localNoninvertibleTransformException)
    {
      d4 = d5 = d6 = 0D;
    }
    paramRenderQueue.ensureCapacityAndAlignment(44, 12);
    RenderBuffer localRenderBuffer = paramRenderQueue.getBuffer();
    localRenderBuffer.putInt(102);
    localRenderBuffer.putInt((paramBoolean2) ? 1 : 0);
    localRenderBuffer.putInt((paramBoolean1) ? 1 : 0);
    localRenderBuffer.putDouble(d4).putDouble(d5).putDouble(d6);
    localRenderBuffer.putInt(i).putInt(j);
  }

  private static void setGradientPaint(RenderQueue paramRenderQueue, SunGraphics2D paramSunGraphics2D, GradientPaint paramGradientPaint, boolean paramBoolean)
  {
    setGradientPaint(paramRenderQueue, (AffineTransform)paramSunGraphics2D.transform.clone(), paramGradientPaint.getColor1(), paramGradientPaint.getColor2(), paramGradientPaint.getPoint1(), paramGradientPaint.getPoint2(), paramGradientPaint.isCyclic(), paramBoolean);
  }

  private static void setTexturePaint(RenderQueue paramRenderQueue, SunGraphics2D paramSunGraphics2D, TexturePaint paramTexturePaint, boolean paramBoolean)
  {
    double d1;
    double d2;
    double d3;
    double d4;
    double d5;
    double d6;
    BufferedImage localBufferedImage = paramTexturePaint.getImage();
    SurfaceData localSurfaceData1 = paramSunGraphics2D.surfaceData;
    SurfaceData localSurfaceData2 = SurfaceData.getSourceSurfaceData(localBufferedImage, localSurfaceData1, CompositeType.SrcOver, null, false);
    int i = (paramSunGraphics2D.interpolationType != 1) ? 1 : 0;
    AffineTransform localAffineTransform = (AffineTransform)paramSunGraphics2D.transform.clone();
    Rectangle2D localRectangle2D = paramTexturePaint.getAnchorRect();
    localAffineTransform.translate(localRectangle2D.getX(), localRectangle2D.getY());
    localAffineTransform.scale(localRectangle2D.getWidth(), localRectangle2D.getHeight());
    try
    {
      localAffineTransform.invert();
      d1 = localAffineTransform.getScaleX();
      d2 = localAffineTransform.getShearX();
      d3 = localAffineTransform.getTranslateX();
      d4 = localAffineTransform.getShearY();
      d5 = localAffineTransform.getScaleY();
      d6 = localAffineTransform.getTranslateY();
    }
    catch (NoninvertibleTransformException localNoninvertibleTransformException)
    {
      d1 = d2 = d3 = d4 = d5 = d6 = 0D;
    }
    paramRenderQueue.ensureCapacityAndAlignment(68, 12);
    RenderBuffer localRenderBuffer = paramRenderQueue.getBuffer();
    localRenderBuffer.putInt(105);
    localRenderBuffer.putInt((paramBoolean) ? 1 : 0);
    localRenderBuffer.putInt((i != 0) ? 1 : 0);
    localRenderBuffer.putLong(localSurfaceData2.getNativeOps());
    localRenderBuffer.putDouble(d1).putDouble(d2).putDouble(d3);
    localRenderBuffer.putDouble(d4).putDouble(d5).putDouble(d6);
  }

  private static int convertSRGBtoLinearRGB(int paramInt)
  {
    float f2;
    float f1 = paramInt / 255.0F;
    if (f1 <= 0.040449999272823334F)
      f2 = f1 / 12.920000076293945F;
    else
      f2 = (float)Math.pow((f1 + 0.055D) / 1.0549999999999999D, 2.3999999999999999D);
    return Math.round(f2 * 255.0F);
  }

  private static int colorToIntArgbPrePixel(Color paramColor, boolean paramBoolean)
  {
    int i = paramColor.getRGB();
    if ((!(paramBoolean)) && (i >> 24 == -1))
      return i;
    int j = i >>> 24;
    int k = i >> 16 & 0xFF;
    int l = i >> 8 & 0xFF;
    int i1 = i & 0xFF;
    if (paramBoolean)
    {
      k = convertSRGBtoLinearRGB(k);
      l = convertSRGBtoLinearRGB(l);
      i1 = convertSRGBtoLinearRGB(i1);
    }
    int i2 = j + (j >> 7);
    k = k * i2 >> 8;
    l = l * i2 >> 8;
    i1 = i1 * i2 >> 8;
    return (j << 24 | k << 16 | l << 8 | i1);
  }

  private static int[] convertToIntArgbPrePixels(Color[] paramArrayOfColor, boolean paramBoolean)
  {
    int[] arrayOfInt = new int[paramArrayOfColor.length];
    for (int i = 0; i < paramArrayOfColor.length; ++i)
      arrayOfInt[i] = colorToIntArgbPrePixel(paramArrayOfColor[i], paramBoolean);
    return arrayOfInt;
  }

  private static void setLinearGradientPaint(RenderQueue paramRenderQueue, SunGraphics2D paramSunGraphics2D, LinearGradientPaint paramLinearGradientPaint, boolean paramBoolean)
  {
    float f1;
    float f2;
    float f3;
    boolean bool1 = paramLinearGradientPaint.getColorSpace() == MultipleGradientPaint.ColorSpaceType.LINEAR_RGB;
    Color[] arrayOfColor = paramLinearGradientPaint.getColors();
    int i = arrayOfColor.length;
    Point2D localPoint2D1 = paramLinearGradientPaint.getStartPoint();
    Point2D localPoint2D2 = paramLinearGradientPaint.getEndPoint();
    AffineTransform localAffineTransform = paramLinearGradientPaint.getTransform();
    localAffineTransform.preConcatenate(paramSunGraphics2D.transform);
    if ((!(bool1)) && (i == 2) && (paramLinearGradientPaint.getCycleMethod() != MultipleGradientPaint.CycleMethod.REPEAT))
    {
      boolean bool2 = paramLinearGradientPaint.getCycleMethod() != MultipleGradientPaint.CycleMethod.NO_CYCLE;
      setGradientPaint(paramRenderQueue, localAffineTransform, arrayOfColor[0], arrayOfColor[1], localPoint2D1, localPoint2D2, bool2, paramBoolean);
      return;
    }
    int j = paramLinearGradientPaint.getCycleMethod().ordinal();
    float[] arrayOfFloat = paramLinearGradientPaint.getFractions();
    int[] arrayOfInt = convertToIntArgbPrePixels(arrayOfColor, bool1);
    double d1 = localPoint2D1.getX();
    double d2 = localPoint2D1.getY();
    localAffineTransform.translate(d1, d2);
    d1 = localPoint2D2.getX() - d1;
    d2 = localPoint2D2.getY() - d2;
    double d3 = Math.sqrt(d1 * d1 + d2 * d2);
    localAffineTransform.rotate(d1, d2);
    localAffineTransform.scale(d3, 1D);
    try
    {
      localAffineTransform.invert();
      f1 = (float)localAffineTransform.getScaleX();
      f2 = (float)localAffineTransform.getShearX();
      f3 = (float)localAffineTransform.getTranslateX();
    }
    catch (NoninvertibleTransformException localNoninvertibleTransformException)
    {
      f1 = f2 = f3 = 0F;
    }
    paramRenderQueue.ensureCapacity(32 + i * 4 * 2);
    RenderBuffer localRenderBuffer = paramRenderQueue.getBuffer();
    localRenderBuffer.putInt(103);
    localRenderBuffer.putInt((paramBoolean) ? 1 : 0);
    localRenderBuffer.putInt((bool1) ? 1 : 0);
    localRenderBuffer.putInt(j);
    localRenderBuffer.putInt(i);
    localRenderBuffer.putFloat(f1);
    localRenderBuffer.putFloat(f2);
    localRenderBuffer.putFloat(f3);
    localRenderBuffer.put(arrayOfFloat);
    localRenderBuffer.put(arrayOfInt);
  }

  private static void setRadialGradientPaint(RenderQueue paramRenderQueue, SunGraphics2D paramSunGraphics2D, RadialGradientPaint paramRadialGradientPaint, boolean paramBoolean)
  {
    boolean bool = paramRadialGradientPaint.getColorSpace() == MultipleGradientPaint.ColorSpaceType.LINEAR_RGB;
    int i = paramRadialGradientPaint.getCycleMethod().ordinal();
    float[] arrayOfFloat = paramRadialGradientPaint.getFractions();
    Color[] arrayOfColor = paramRadialGradientPaint.getColors();
    int j = arrayOfColor.length;
    int[] arrayOfInt = convertToIntArgbPrePixels(arrayOfColor, bool);
    Point2D localPoint2D1 = paramRadialGradientPaint.getCenterPoint();
    Point2D localPoint2D2 = paramRadialGradientPaint.getFocusPoint();
    float f = paramRadialGradientPaint.getRadius();
    double d1 = localPoint2D1.getX();
    double d2 = localPoint2D1.getY();
    double d3 = localPoint2D2.getX();
    double d4 = localPoint2D2.getY();
    AffineTransform localAffineTransform = paramRadialGradientPaint.getTransform();
    localAffineTransform.preConcatenate(paramSunGraphics2D.transform);
    localPoint2D2 = localAffineTransform.transform(localPoint2D2, localPoint2D2);
    localAffineTransform.translate(d1, d2);
    localAffineTransform.rotate(d3 - d1, d4 - d2);
    localAffineTransform.scale(f, f);
    try
    {
      localAffineTransform.invert();
    }
    catch (Exception localException)
    {
      localAffineTransform.setToScale(0D, 0D);
    }
    localPoint2D2 = localAffineTransform.transform(localPoint2D2, localPoint2D2);
    d3 = Math.min(localPoint2D2.getX(), 0.98999999999999999D);
    paramRenderQueue.ensureCapacity(48 + j * 4 * 2);
    RenderBuffer localRenderBuffer = paramRenderQueue.getBuffer();
    localRenderBuffer.putInt(104);
    localRenderBuffer.putInt((paramBoolean) ? 1 : 0);
    localRenderBuffer.putInt((bool) ? 1 : 0);
    localRenderBuffer.putInt(j);
    localRenderBuffer.putInt(i);
    localRenderBuffer.putFloat((float)localAffineTransform.getScaleX());
    localRenderBuffer.putFloat((float)localAffineTransform.getShearX());
    localRenderBuffer.putFloat((float)localAffineTransform.getTranslateX());
    localRenderBuffer.putFloat((float)localAffineTransform.getShearY());
    localRenderBuffer.putFloat((float)localAffineTransform.getScaleY());
    localRenderBuffer.putFloat((float)localAffineTransform.getTranslateY());
    localRenderBuffer.putFloat((float)d3);
    localRenderBuffer.put(arrayOfFloat);
    localRenderBuffer.put(arrayOfInt);
  }
}