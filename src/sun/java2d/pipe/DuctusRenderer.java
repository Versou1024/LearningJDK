package sun.java2d.pipe;

import java.awt.BasicStroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.io.PrintStream;
import sun.dc.path.PathConsumer;
import sun.dc.path.PathException;
import sun.dc.pr.PRException;
import sun.dc.pr.PathDasher;
import sun.dc.pr.PathStroker;
import sun.dc.pr.Rasterizer;

public class DuctusRenderer
{
  public static final float PenUnits = 0.0099999997764825821F;
  public static final int MinPenUnits = 100;
  public static final int MinPenUnitsAA = 20;
  public static final float MinPenSizeAA = 0.19999998807907104F;
  static final int[] RasterizerCaps = { 30, 10, 20 };
  static final int[] RasterizerCorners = { 50, 10, 40 };
  private static Rasterizer theRasterizer;
  private static byte[] theTile;
  static final float UPPER_BND = 170141173319264430000000000000000000000.0F;
  static final float LOWER_BND = -170141173319264430000000000000000000000.0F;

  public static synchronized Rasterizer getRasterizer()
  {
    Rasterizer localRasterizer = theRasterizer;
    if (localRasterizer == null)
      localRasterizer = new Rasterizer();
    else
      theRasterizer = null;
    return localRasterizer;
  }

  public static synchronized void dropRasterizer(Rasterizer paramRasterizer)
  {
    paramRasterizer.reset();
    theRasterizer = paramRasterizer;
  }

  public static synchronized byte[] getAlphaTile()
  {
    byte[] arrayOfByte = theTile;
    if (arrayOfByte == null)
    {
      int i = Rasterizer.TILE_SIZE;
      arrayOfByte = new byte[i * i];
    }
    else
    {
      theTile = null;
    }
    return arrayOfByte;
  }

  public static synchronized void dropAlphaTile(byte[] paramArrayOfByte)
  {
    theTile = paramArrayOfByte;
  }

  public static synchronized void getAlpha(Rasterizer paramRasterizer, byte[] paramArrayOfByte, int paramInt1, int paramInt2, int paramInt3)
    throws PRException
  {
    try
    {
      paramRasterizer.writeAlpha(paramArrayOfByte, paramInt1, paramInt2, paramInt3);
    }
    catch (InterruptedException localInterruptedException)
    {
      Thread.currentThread().interrupt();
    }
  }

  public static PathConsumer createStroker(PathConsumer paramPathConsumer, BasicStroke paramBasicStroke, boolean paramBoolean, AffineTransform paramAffineTransform)
  {
    PathStroker localPathStroker = new PathStroker(paramPathConsumer);
    paramPathConsumer = localPathStroker;
    float[] arrayOfFloat1 = null;
    if (!(paramBoolean))
    {
      localPathStroker.setPenDiameter(paramBasicStroke.getLineWidth());
      if (paramAffineTransform != null)
        arrayOfFloat1 = getTransformMatrix(paramAffineTransform);
      localPathStroker.setPenT4(arrayOfFloat1);
      localPathStroker.setPenFitting(0.0099999997764825821F, 100);
    }
    localPathStroker.setCaps(RasterizerCaps[paramBasicStroke.getEndCap()]);
    localPathStroker.setCorners(RasterizerCorners[paramBasicStroke.getLineJoin()], paramBasicStroke.getMiterLimit());
    float[] arrayOfFloat2 = paramBasicStroke.getDashArray();
    if (arrayOfFloat2 != null)
    {
      PathDasher localPathDasher = new PathDasher(localPathStroker);
      localPathDasher.setDash(arrayOfFloat2, paramBasicStroke.getDashPhase());
      if ((paramAffineTransform != null) && (arrayOfFloat1 == null))
        arrayOfFloat1 = getTransformMatrix(paramAffineTransform);
      localPathDasher.setDashT4(arrayOfFloat1);
      paramPathConsumer = localPathDasher;
    }
    return paramPathConsumer;
  }

  static float[] getTransformMatrix(AffineTransform paramAffineTransform)
  {
    float[] arrayOfFloat = new float[4];
    double[] arrayOfDouble = new double[6];
    paramAffineTransform.getMatrix(arrayOfDouble);
    for (int i = 0; i < 4; ++i)
      arrayOfFloat[i] = (float)arrayOfDouble[i];
    return arrayOfFloat;
  }

  public static void disposeStroker(PathConsumer paramPathConsumer1, PathConsumer paramPathConsumer2)
  {
    while ((paramPathConsumer1 != null) && (paramPathConsumer1 != paramPathConsumer2))
    {
      PathConsumer localPathConsumer = paramPathConsumer1.getConsumer();
      paramPathConsumer1.dispose();
      paramPathConsumer1 = localPathConsumer;
    }
  }

  public static void feedConsumer(PathIterator paramPathIterator, PathConsumer paramPathConsumer, boolean paramBoolean, float paramFloat)
    throws PathException
  {
    paramPathConsumer.beginPath();
    int i = 0;
    int j = 0;
    int k = 0;
    float f1 = 0F;
    float f2 = 0F;
    float[] arrayOfFloat = new float[6];
    float f3 = 0.5F - paramFloat;
    float f4 = 0F;
    float f5 = 0F;
    while (!(paramPathIterator.isDone()))
    {
      int l = paramPathIterator.currentSegment(arrayOfFloat);
      if (i == 1)
      {
        i = 0;
        if (l != 0)
        {
          paramPathConsumer.beginSubpath(f1, f2);
          k = 1;
        }
      }
      if (paramBoolean)
      {
        int i1;
        switch (l)
        {
        case 3:
          i1 = 4;
          break;
        case 2:
          i1 = 2;
          break;
        case 0:
        case 1:
          i1 = 0;
          break;
        case 4:
        default:
          i1 = -1;
        }
        if (i1 >= 0)
        {
          float f6 = arrayOfFloat[i1];
          float f7 = arrayOfFloat[(i1 + 1)];
          float f8 = (float)Math.floor(f6 + f3) + paramFloat;
          float f9 = (float)Math.floor(f7 + f3) + paramFloat;
          arrayOfFloat[i1] = f8;
          arrayOfFloat[(i1 + 1)] = f9;
          f8 -= f6;
          f9 -= f7;
          switch (l)
          {
          case 3:
            arrayOfFloat[0] += f4;
            arrayOfFloat[1] += f5;
            arrayOfFloat[2] += f8;
            arrayOfFloat[3] += f9;
            break;
          case 2:
            arrayOfFloat[0] += (f8 + f4) / 2F;
            arrayOfFloat[1] += (f9 + f5) / 2F;
          case 0:
          case 1:
          case 4:
          }
          f4 = f8;
          f5 = f9;
        }
      }
      switch (l)
      {
      case 0:
        if ((arrayOfFloat[0] < 170141173319264430000000000000000000000.0F) && (arrayOfFloat[0] > -170141173319264430000000000000000000000.0F) && (arrayOfFloat[1] < 170141173319264430000000000000000000000.0F) && (arrayOfFloat[1] > -170141173319264430000000000000000000000.0F))
        {
          f1 = arrayOfFloat[0];
          f2 = arrayOfFloat[1];
          paramPathConsumer.beginSubpath(f1, f2);
          k = 1;
          j = 0;
        }
        else
        {
          j = 1;
        }
        break;
      case 1:
        if ((arrayOfFloat[0] < 170141173319264430000000000000000000000.0F) && (arrayOfFloat[0] > -170141173319264430000000000000000000000.0F) && (arrayOfFloat[1] < 170141173319264430000000000000000000000.0F) && (arrayOfFloat[1] > -170141173319264430000000000000000000000.0F))
          if (j != 0)
          {
            paramPathConsumer.beginSubpath(arrayOfFloat[0], arrayOfFloat[1]);
            k = 1;
            j = 0;
          }
          else
          {
            paramPathConsumer.appendLine(arrayOfFloat[0], arrayOfFloat[1]);
          }
        break;
      case 2:
        if ((arrayOfFloat[2] < 170141173319264430000000000000000000000.0F) && (arrayOfFloat[2] > -170141173319264430000000000000000000000.0F) && (arrayOfFloat[3] < 170141173319264430000000000000000000000.0F) && (arrayOfFloat[3] > -170141173319264430000000000000000000000.0F))
          if (j != 0)
          {
            paramPathConsumer.beginSubpath(arrayOfFloat[2], arrayOfFloat[3]);
            k = 1;
            j = 0;
          }
          else if ((arrayOfFloat[0] < 170141173319264430000000000000000000000.0F) && (arrayOfFloat[0] > -170141173319264430000000000000000000000.0F) && (arrayOfFloat[1] < 170141173319264430000000000000000000000.0F) && (arrayOfFloat[1] > -170141173319264430000000000000000000000.0F))
          {
            paramPathConsumer.appendQuadratic(arrayOfFloat[0], arrayOfFloat[1], arrayOfFloat[2], arrayOfFloat[3]);
          }
          else
          {
            paramPathConsumer.appendLine(arrayOfFloat[2], arrayOfFloat[3]);
          }
        break;
      case 3:
        if ((arrayOfFloat[4] < 170141173319264430000000000000000000000.0F) && (arrayOfFloat[4] > -170141173319264430000000000000000000000.0F) && (arrayOfFloat[5] < 170141173319264430000000000000000000000.0F) && (arrayOfFloat[5] > -170141173319264430000000000000000000000.0F))
          if (j != 0)
          {
            paramPathConsumer.beginSubpath(arrayOfFloat[4], arrayOfFloat[5]);
            k = 1;
            j = 0;
          }
          else if ((arrayOfFloat[0] < 170141173319264430000000000000000000000.0F) && (arrayOfFloat[0] > -170141173319264430000000000000000000000.0F) && (arrayOfFloat[1] < 170141173319264430000000000000000000000.0F) && (arrayOfFloat[1] > -170141173319264430000000000000000000000.0F) && (arrayOfFloat[2] < 170141173319264430000000000000000000000.0F) && (arrayOfFloat[2] > -170141173319264430000000000000000000000.0F) && (arrayOfFloat[3] < 170141173319264430000000000000000000000.0F) && (arrayOfFloat[3] > -170141173319264430000000000000000000000.0F))
          {
            paramPathConsumer.appendCubic(arrayOfFloat[0], arrayOfFloat[1], arrayOfFloat[2], arrayOfFloat[3], arrayOfFloat[4], arrayOfFloat[5]);
          }
          else
          {
            paramPathConsumer.appendLine(arrayOfFloat[4], arrayOfFloat[5]);
          }
        break;
      case 4:
        if (k != 0)
        {
          paramPathConsumer.closedSubpath();
          k = 0;
          i = 1;
        }
      }
      paramPathIterator.next();
    }
    paramPathConsumer.endPath();
  }

  public static Rasterizer createShapeRasterizer(PathIterator paramPathIterator, AffineTransform paramAffineTransform, BasicStroke paramBasicStroke, boolean paramBoolean1, boolean paramBoolean2, float paramFloat)
  {
    Rasterizer localRasterizer = getRasterizer();
    if (paramBasicStroke != null)
    {
      float[] arrayOfFloat1 = null;
      localRasterizer.setUsage(3);
      if (paramBoolean1)
      {
        localRasterizer.setPenDiameter(0.19999998807907104F);
      }
      else
      {
        localRasterizer.setPenDiameter(paramBasicStroke.getLineWidth());
        if (paramAffineTransform != null)
        {
          arrayOfFloat1 = getTransformMatrix(paramAffineTransform);
          localRasterizer.setPenT4(arrayOfFloat1);
        }
        localRasterizer.setPenFitting(0.0099999997764825821F, 20);
      }
      localRasterizer.setCaps(RasterizerCaps[paramBasicStroke.getEndCap()]);
      localRasterizer.setCorners(RasterizerCorners[paramBasicStroke.getLineJoin()], paramBasicStroke.getMiterLimit());
      float[] arrayOfFloat2 = paramBasicStroke.getDashArray();
      if (arrayOfFloat2 != null)
      {
        localRasterizer.setDash(arrayOfFloat2, paramBasicStroke.getDashPhase());
        if ((paramAffineTransform != null) && (arrayOfFloat1 == null))
          arrayOfFloat1 = getTransformMatrix(paramAffineTransform);
        localRasterizer.setDashT4(arrayOfFloat1);
      }
    }
    else
    {
      localRasterizer.setUsage((paramPathIterator.getWindingRule() == 0) ? 1 : 2);
    }
    localRasterizer.beginPath();
    int i = 0;
    int j = 0;
    int k = 0;
    float f1 = 0F;
    float f2 = 0F;
    float[] arrayOfFloat3 = new float[6];
    float f3 = 0.5F - paramFloat;
    float f4 = 0F;
    float f5 = 0F;
    while (!(paramPathIterator.isDone()))
    {
      int l = paramPathIterator.currentSegment(arrayOfFloat3);
      if (i == 1)
      {
        i = 0;
        if (l != 0)
        {
          localRasterizer.beginSubpath(f1, f2);
          k = 1;
        }
      }
      if (paramBoolean2)
      {
        int i1;
        switch (l)
        {
        case 3:
          i1 = 4;
          break;
        case 2:
          i1 = 2;
          break;
        case 0:
        case 1:
          i1 = 0;
          break;
        case 4:
        default:
          i1 = -1;
        }
        if (i1 >= 0)
        {
          float f6 = arrayOfFloat3[i1];
          float f7 = arrayOfFloat3[(i1 + 1)];
          float f8 = (float)Math.floor(f6 + f3) + paramFloat;
          float f9 = (float)Math.floor(f7 + f3) + paramFloat;
          arrayOfFloat3[i1] = f8;
          arrayOfFloat3[(i1 + 1)] = f9;
          f8 -= f6;
          f9 -= f7;
          switch (l)
          {
          case 3:
            arrayOfFloat3[0] += f4;
            arrayOfFloat3[1] += f5;
            arrayOfFloat3[2] += f8;
            arrayOfFloat3[3] += f9;
            break;
          case 2:
            arrayOfFloat3[0] += (f8 + f4) / 2F;
            arrayOfFloat3[1] += (f9 + f5) / 2F;
          case 0:
          case 1:
          case 4:
          }
          f4 = f8;
          f5 = f9;
        }
      }
      switch (l)
      {
      case 0:
        if ((arrayOfFloat3[0] < 170141173319264430000000000000000000000.0F) && (arrayOfFloat3[0] > -170141173319264430000000000000000000000.0F) && (arrayOfFloat3[1] < 170141173319264430000000000000000000000.0F) && (arrayOfFloat3[1] > -170141173319264430000000000000000000000.0F))
        {
          f1 = arrayOfFloat3[0];
          f2 = arrayOfFloat3[1];
          localRasterizer.beginSubpath(f1, f2);
          k = 1;
          j = 0;
        }
        else
        {
          j = 1;
        }
        break;
      case 1:
        if ((arrayOfFloat3[0] < 170141173319264430000000000000000000000.0F) && (arrayOfFloat3[0] > -170141173319264430000000000000000000000.0F) && (arrayOfFloat3[1] < 170141173319264430000000000000000000000.0F) && (arrayOfFloat3[1] > -170141173319264430000000000000000000000.0F))
          if (j != 0)
          {
            localRasterizer.beginSubpath(arrayOfFloat3[0], arrayOfFloat3[1]);
            k = 1;
            j = 0;
          }
          else
          {
            localRasterizer.appendLine(arrayOfFloat3[0], arrayOfFloat3[1]);
          }
        break;
      case 2:
        if ((arrayOfFloat3[2] < 170141173319264430000000000000000000000.0F) && (arrayOfFloat3[2] > -170141173319264430000000000000000000000.0F) && (arrayOfFloat3[3] < 170141173319264430000000000000000000000.0F) && (arrayOfFloat3[3] > -170141173319264430000000000000000000000.0F))
          if (j != 0)
          {
            localRasterizer.beginSubpath(arrayOfFloat3[2], arrayOfFloat3[3]);
            k = 1;
            j = 0;
          }
          else if ((arrayOfFloat3[0] < 170141173319264430000000000000000000000.0F) && (arrayOfFloat3[0] > -170141173319264430000000000000000000000.0F) && (arrayOfFloat3[1] < 170141173319264430000000000000000000000.0F) && (arrayOfFloat3[1] > -170141173319264430000000000000000000000.0F))
          {
            localRasterizer.appendQuadratic(arrayOfFloat3[0], arrayOfFloat3[1], arrayOfFloat3[2], arrayOfFloat3[3]);
          }
          else
          {
            localRasterizer.appendLine(arrayOfFloat3[2], arrayOfFloat3[3]);
          }
        break;
      case 3:
        if ((arrayOfFloat3[4] < 170141173319264430000000000000000000000.0F) && (arrayOfFloat3[4] > -170141173319264430000000000000000000000.0F) && (arrayOfFloat3[5] < 170141173319264430000000000000000000000.0F) && (arrayOfFloat3[5] > -170141173319264430000000000000000000000.0F))
          if (j != 0)
          {
            localRasterizer.beginSubpath(arrayOfFloat3[4], arrayOfFloat3[5]);
            k = 1;
            j = 0;
          }
          else if ((arrayOfFloat3[0] < 170141173319264430000000000000000000000.0F) && (arrayOfFloat3[0] > -170141173319264430000000000000000000000.0F) && (arrayOfFloat3[1] < 170141173319264430000000000000000000000.0F) && (arrayOfFloat3[1] > -170141173319264430000000000000000000000.0F) && (arrayOfFloat3[2] < 170141173319264430000000000000000000000.0F) && (arrayOfFloat3[2] > -170141173319264430000000000000000000000.0F) && (arrayOfFloat3[3] < 170141173319264430000000000000000000000.0F) && (arrayOfFloat3[3] > -170141173319264430000000000000000000000.0F))
          {
            localRasterizer.appendCubic(arrayOfFloat3[0], arrayOfFloat3[1], arrayOfFloat3[2], arrayOfFloat3[3], arrayOfFloat3[4], arrayOfFloat3[5]);
          }
          else
          {
            localRasterizer.appendLine(arrayOfFloat3[4], arrayOfFloat3[5]);
          }
        break;
      case 4:
        if (k != 0)
        {
          localRasterizer.closedSubpath();
          k = 0;
          i = 1;
        }
      }
      paramPathIterator.next();
    }
    try
    {
      localRasterizer.endPath();
    }
    catch (PRException localPRException)
    {
      System.err.println("DuctusRenderer.createShapeRasterizer: " + localPRException);
    }
    return localRasterizer;
  }
}