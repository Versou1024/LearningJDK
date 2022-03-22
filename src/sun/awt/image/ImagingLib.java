package sun.awt.image;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ByteLookupTable;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.awt.image.LookupOp;
import java.awt.image.LookupTable;
import java.awt.image.Raster;
import java.awt.image.RasterOp;
import java.awt.image.WritableRaster;
import java.io.PrintStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import sun.java2d.SurfaceData;

public class ImagingLib
{
  static boolean useLib = true;
  static boolean verbose = false;
  private static final int NUM_NATIVE_OPS = 3;
  private static final int LOOKUP_OP = 0;
  private static final int AFFINE_OP = 1;
  private static final int CONVOLVE_OP = 2;
  private static Class[] nativeOpClass = new Class[3];

  private static native boolean init();

  public static native int transformBI(BufferedImage paramBufferedImage1, BufferedImage paramBufferedImage2, double[] paramArrayOfDouble, int paramInt);

  public static native int transformRaster(Raster paramRaster1, Raster paramRaster2, double[] paramArrayOfDouble, int paramInt);

  public static native int convolveBI(BufferedImage paramBufferedImage1, BufferedImage paramBufferedImage2, Kernel paramKernel, int paramInt);

  public static native int convolveRaster(Raster paramRaster1, Raster paramRaster2, Kernel paramKernel, int paramInt);

  public static native int lookupByteBI(BufferedImage paramBufferedImage1, BufferedImage paramBufferedImage2, byte[][] paramArrayOfByte);

  public static native int lookupByteRaster(Raster paramRaster1, Raster paramRaster2, byte[][] paramArrayOfByte);

  private static int getNativeOpIndex(Class paramClass)
  {
    int i = -1;
    for (int j = 0; j < 3; ++j)
      if (paramClass == nativeOpClass[j])
      {
        i = j;
        break;
      }
    return i;
  }

  public static WritableRaster filter(RasterOp paramRasterOp, Raster paramRaster, WritableRaster paramWritableRaster)
  {
    Object localObject;
    if (!(useLib))
      return null;
    if (paramWritableRaster == null)
      paramWritableRaster = paramRasterOp.createCompatibleDestRaster(paramRaster);
    WritableRaster localWritableRaster = null;
    switch (getNativeOpIndex(paramRasterOp.getClass()))
    {
    case 0:
      LookupTable localLookupTable = ((LookupOp)paramRasterOp).getTable();
      if (localLookupTable.getOffset() != 0)
        return null;
      if (localLookupTable instanceof ByteLookupTable)
      {
        localObject = (ByteLookupTable)localLookupTable;
        if (lookupByteRaster(paramRaster, paramWritableRaster, ((ByteLookupTable)localObject).getTable()) > 0)
          localWritableRaster = paramWritableRaster;
      }
      break;
    case 1:
      localObject = (AffineTransformOp)paramRasterOp;
      double[] arrayOfDouble = new double[6];
      ((AffineTransformOp)localObject).getTransform().getMatrix(arrayOfDouble);
      if (transformRaster(paramRaster, paramWritableRaster, arrayOfDouble, ((AffineTransformOp)localObject).getInterpolationType()) > 0)
        localWritableRaster = paramWritableRaster;
      break;
    case 2:
      ConvolveOp localConvolveOp = (ConvolveOp)paramRasterOp;
      if (convolveRaster(paramRaster, paramWritableRaster, localConvolveOp.getKernel(), localConvolveOp.getEdgeCondition()) > 0)
        localWritableRaster = paramWritableRaster;
    }
    if (localWritableRaster instanceof SunWritableRaster)
      ((SunWritableRaster)localWritableRaster).notifyChanged();
    return ((WritableRaster)localWritableRaster);
  }

  public static BufferedImage filter(BufferedImageOp paramBufferedImageOp, BufferedImage paramBufferedImage1, BufferedImage paramBufferedImage2)
  {
    Object localObject1;
    Object localObject2;
    if (verbose)
      System.out.println("in filter and op is " + paramBufferedImageOp + "bufimage is " + paramBufferedImage1 + " and " + paramBufferedImage2);
    if (!(useLib))
      return null;
    if (paramBufferedImage2 == null)
      paramBufferedImage2 = paramBufferedImageOp.createCompatibleDestImage(paramBufferedImage1, null);
    BufferedImage localBufferedImage = null;
    switch (getNativeOpIndex(paramBufferedImageOp.getClass()))
    {
    case 0:
      localObject1 = ((LookupOp)paramBufferedImageOp).getTable();
      if (((LookupTable)localObject1).getOffset() != 0)
        return null;
      if (localObject1 instanceof ByteLookupTable)
      {
        localObject2 = (ByteLookupTable)localObject1;
        if (lookupByteBI(paramBufferedImage1, paramBufferedImage2, ((ByteLookupTable)localObject2).getTable()) > 0)
          localBufferedImage = paramBufferedImage2;
      }
      break;
    case 1:
      localObject2 = (AffineTransformOp)paramBufferedImageOp;
      double[] arrayOfDouble = new double[6];
      AffineTransform localAffineTransform = ((AffineTransformOp)localObject2).getTransform();
      ((AffineTransformOp)localObject2).getTransform().getMatrix(arrayOfDouble);
      if (transformBI(paramBufferedImage1, paramBufferedImage2, arrayOfDouble, ((AffineTransformOp)localObject2).getInterpolationType()) > 0)
        localBufferedImage = paramBufferedImage2;
      break;
    case 2:
      ConvolveOp localConvolveOp = (ConvolveOp)paramBufferedImageOp;
      if (convolveBI(paramBufferedImage1, paramBufferedImage2, localConvolveOp.getKernel(), localConvolveOp.getEdgeCondition()) > 0)
        localBufferedImage = paramBufferedImage2;
    }
    if (localBufferedImage != null)
    {
      localObject1 = SurfaceManager.getManager(localBufferedImage);
      localObject2 = ((SurfaceManager)localObject1).getDestSurfaceData();
      ((SurfaceData)localObject2).setNeedsBackup(true);
    }
    return ((BufferedImage)(BufferedImage)localBufferedImage);
  }

  static
  {
    1 local1 = new PrivilegedAction()
    {
      public Boolean run()
      {
        String str = System.getProperty("os.arch");
        if ((str == null) || (!(str.startsWith("sparc"))))
          try
          {
            System.loadLibrary("mlib_image");
          }
          catch (UnsatisfiedLinkError localUnsatisfiedLinkError)
          {
            return Boolean.FALSE;
          }
        boolean bool = ImagingLib.access$000();
        return Boolean.valueOf(bool);
      }
    };
    useLib = ((Boolean)AccessController.doPrivileged(local1)).booleanValue();
    try
    {
      nativeOpClass[0] = Class.forName("java.awt.image.LookupOp");
    }
    catch (ClassNotFoundException localClassNotFoundException1)
    {
      System.err.println("Could not find class: " + localClassNotFoundException1);
    }
    try
    {
      nativeOpClass[1] = Class.forName("java.awt.image.AffineTransformOp");
    }
    catch (ClassNotFoundException localClassNotFoundException2)
    {
      System.err.println("Could not find class: " + localClassNotFoundException2);
    }
    try
    {
      nativeOpClass[2] = Class.forName("java.awt.image.ConvolveOp");
    }
    catch (ClassNotFoundException localClassNotFoundException3)
    {
      System.err.println("Could not find class: " + localClassNotFoundException3);
    }
  }
}