package sun.java2d.opengl;

import java.awt.LinearGradientPaint;
import java.awt.MultipleGradientPaint;
import java.awt.MultipleGradientPaint.ColorSpaceType;
import java.awt.MultipleGradientPaint.CycleMethod;
import java.awt.TexturePaint;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import sun.java2d.SunGraphics2D;
import sun.java2d.SurfaceData;
import sun.java2d.loops.CompositeType;

abstract class OGLPaints
{
  private static Map<Integer, OGLPaints> impls = new HashMap(4, 1F);

  static boolean isValid(SunGraphics2D paramSunGraphics2D)
  {
    OGLPaints localOGLPaints = (OGLPaints)impls.get(Integer.valueOf(paramSunGraphics2D.paintState));
    return ((localOGLPaints != null) && (localOGLPaints.isPaintValid(paramSunGraphics2D)));
  }

  abstract boolean isPaintValid(SunGraphics2D paramSunGraphics2D);

  static
  {
    impls.put(Integer.valueOf(2), new Gradient(null));
    impls.put(Integer.valueOf(3), new LinearGradient(null));
    impls.put(Integer.valueOf(4), new RadialGradient(null));
    impls.put(Integer.valueOf(5), new Texture(null));
  }

  private static class Gradient extends OGLPaints
  {
    boolean isPaintValid(SunGraphics2D paramSunGraphics2D)
    {
      return true;
    }
  }

  private static class LinearGradient extends OGLPaints.MultiGradient
  {
    boolean isPaintValid(SunGraphics2D paramSunGraphics2D)
    {
      LinearGradientPaint localLinearGradientPaint = (LinearGradientPaint)paramSunGraphics2D.paint;
      if ((localLinearGradientPaint.getFractions().length == 2) && (localLinearGradientPaint.getCycleMethod() != MultipleGradientPaint.CycleMethod.REPEAT) && (localLinearGradientPaint.getColorSpace() != MultipleGradientPaint.ColorSpaceType.LINEAR_RGB))
        return true;
      return super.isPaintValid(paramSunGraphics2D);
    }
  }

  private static abstract class MultiGradient extends OGLPaints
  {
    boolean isPaintValid(SunGraphics2D paramSunGraphics2D)
    {
      MultipleGradientPaint localMultipleGradientPaint = (MultipleGradientPaint)paramSunGraphics2D.paint;
      if (localMultipleGradientPaint.getFractions().length > 12)
        return false;
      OGLSurfaceData localOGLSurfaceData = (OGLSurfaceData)paramSunGraphics2D.surfaceData;
      OGLGraphicsConfig localOGLGraphicsConfig = localOGLSurfaceData.getOGLGraphicsConfig();
      return (localOGLGraphicsConfig.isCapPresent(524288));
    }
  }

  private static class RadialGradient extends OGLPaints.MultiGradient
  {
  }

  private static class Texture extends OGLPaints
  {
    boolean isPaintValid(SunGraphics2D paramSunGraphics2D)
    {
      TexturePaint localTexturePaint = (TexturePaint)paramSunGraphics2D.paint;
      OGLSurfaceData localOGLSurfaceData1 = (OGLSurfaceData)paramSunGraphics2D.surfaceData;
      BufferedImage localBufferedImage = localTexturePaint.getImage();
      if (!(localOGLSurfaceData1.isTexNonPow2Available()))
      {
        int i = localBufferedImage.getWidth();
        int j = localBufferedImage.getHeight();
        if (((i & i - 1) != 0) || ((j & j - 1) != 0))
          return false;
      }
      SurfaceData localSurfaceData = SurfaceData.getSourceSurfaceData(localBufferedImage, localOGLSurfaceData1, CompositeType.SrcOver, null, false);
      if (!(localSurfaceData instanceof OGLSurfaceData))
      {
        localSurfaceData = SurfaceData.getSourceSurfaceData(localBufferedImage, localOGLSurfaceData1, CompositeType.SrcOver, null, false);
        if (!(localSurfaceData instanceof OGLSurfaceData))
          return false;
      }
      OGLSurfaceData localOGLSurfaceData2 = (OGLSurfaceData)localSurfaceData;
      return (localOGLSurfaceData2.getType() == 3);
    }
  }
}