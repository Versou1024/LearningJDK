package sun.dc.pr;

import sun.dc.path.FastPathProducer;
import sun.dc.path.PathConsumer;
import sun.dc.path.PathError;
import sun.dc.path.PathException;
import sun.java2d.Disposer;
import sun.java2d.DisposerRecord;

public class Rasterizer
{
  public static final int EOFILL = 1;
  public static final int NZFILL = 2;
  public static final int STROKE = 3;
  public static final int ROUND = 10;
  public static final int SQUARE = 20;
  public static final int BUTT = 30;
  public static final int BEVEL = 40;
  public static final int MITER = 50;
  public static final int TILE_SIZE = 1 << PathFiller.tileSizeL2S;
  public static final int TILE_SIZE_L2S = PathFiller.tileSizeL2S;
  public static final int MAX_ALPHA = 1000000;
  public static final int MAX_MITER = 10;
  public static final int MAX_WN = 63;
  public static final int TILE_IS_ALL_0 = 0;
  public static final int TILE_IS_ALL_1 = 1;
  public static final int TILE_IS_GENERAL = 2;
  private static final int BEG = 1;
  private static final int PAC_FILL = 2;
  private static final int PAC_STROKE = 3;
  private static final int PATH = 4;
  private static final int SUBPATH = 5;
  private static final int RAS = 6;
  private int state = 1;
  private PathFiller filler = new PathFiller();
  private PathStroker stroker = new PathStroker(???.filler);
  private PathDasher dasher = new PathDasher(???.stroker);
  private PathConsumer curPC;

  public Rasterizer()
  {
    Disposer.addRecord(this, new ConsumerDisposer(this.filler, this.stroker, this.dasher));
  }

  public void setUsage(int paramInt)
    throws sun.dc.pr.PRError
  {
    if (this.state != 1)
      throw new sun.dc.pr.PRError("setUsage: unexpected");
    if (paramInt == 1)
    {
      this.filler.setFillMode(1);
      this.curPC = this.filler;
      this.state = 2;
    }
    else if (paramInt == 2)
    {
      this.filler.setFillMode(2);
      this.curPC = this.filler;
      this.state = 2;
    }
    else if (paramInt == 3)
    {
      this.curPC = this.stroker;
      this.filler.setFillMode(2);
      this.stroker.setPenDiameter(1F);
      this.stroker.setPenT4(null);
      this.stroker.setCaps(10);
      this.stroker.setCorners(10, 0F);
      this.state = 3;
    }
    else
    {
      throw new sun.dc.pr.PRError("setUsage: unknown usage type");
    }
  }

  public void setPenDiameter(float paramFloat)
    throws sun.dc.pr.PRError
  {
    if (this.state != 3)
      throw new sun.dc.pr.PRError("setPenDiameter: unexpected");
    this.stroker.setPenDiameter(paramFloat);
  }

  public void setPenT4(float[] paramArrayOfFloat)
    throws sun.dc.pr.PRError
  {
    if (this.state != 3)
      throw new sun.dc.pr.PRError("setPenT4: unexpected");
    this.stroker.setPenT4(paramArrayOfFloat);
  }

  public void setPenFitting(float paramFloat, int paramInt)
    throws sun.dc.pr.PRError
  {
    if (this.state != 3)
      throw new sun.dc.pr.PRError("setPenFitting: unexpected");
    this.stroker.setPenFitting(paramFloat, paramInt);
  }

  public void setPenDisplacement(float paramFloat1, float paramFloat2)
    throws sun.dc.pr.PRError
  {
    if (this.state != 3)
      throw new sun.dc.pr.PRError("setPenDisplacement: unexpected");
    float[] arrayOfFloat = { 1F, 0F, 0F, 1F, paramFloat1, paramFloat2 };
    this.stroker.setOutputT6(arrayOfFloat);
  }

  public void setCaps(int paramInt)
    throws sun.dc.pr.PRError
  {
    if (this.state != 3)
      throw new sun.dc.pr.PRError("setCaps: unexpected");
    this.stroker.setCaps(paramInt);
  }

  public void setCorners(int paramInt, float paramFloat)
    throws sun.dc.pr.PRError
  {
    if (this.state != 3)
      throw new sun.dc.pr.PRError("setCorners: unexpected");
    this.stroker.setCorners(paramInt, paramFloat);
  }

  public void setDash(float[] paramArrayOfFloat, float paramFloat)
    throws sun.dc.pr.PRError
  {
    if (this.state != 3)
      throw new sun.dc.pr.PRError("setDash: unexpected");
    this.dasher.setDash(paramArrayOfFloat, paramFloat);
    this.curPC = this.dasher;
  }

  public void setDashT4(float[] paramArrayOfFloat)
    throws sun.dc.pr.PRError
  {
    if (this.state != 3)
      throw new sun.dc.pr.PRError("setDashT4: unexpected");
    this.dasher.setDashT4(paramArrayOfFloat);
  }

  public void beginPath(float[] paramArrayOfFloat)
    throws sun.dc.pr.PRError
  {
    beginPath();
  }

  public void beginPath()
    throws sun.dc.pr.PRError
  {
    if ((this.state != 2) && (this.state != 3))
      throw new sun.dc.pr.PRError("beginPath: unexpected");
    try
    {
      this.curPC.beginPath();
      this.state = 4;
    }
    catch (PathError localPathError)
    {
      throw new sun.dc.pr.PRError(localPathError.getMessage());
    }
  }

  public void beginSubpath(float paramFloat1, float paramFloat2)
    throws sun.dc.pr.PRError
  {
    if ((this.state != 4) && (this.state != 5))
      throw new sun.dc.pr.PRError("beginSubpath: unexpected");
    try
    {
      this.curPC.beginSubpath(paramFloat1, paramFloat2);
      this.state = 5;
    }
    catch (PathError localPathError)
    {
      throw new sun.dc.pr.PRError(localPathError.getMessage());
    }
  }

  public void appendLine(float paramFloat1, float paramFloat2)
    throws sun.dc.pr.PRError
  {
    if (this.state != 5)
      throw new sun.dc.pr.PRError("appendLine: unexpected");
    try
    {
      this.curPC.appendLine(paramFloat1, paramFloat2);
    }
    catch (PathError localPathError)
    {
      throw new sun.dc.pr.PRError(localPathError.getMessage());
    }
  }

  public void appendQuadratic(float paramFloat1, float paramFloat2, float paramFloat3, float paramFloat4)
    throws sun.dc.pr.PRError
  {
    if (this.state != 5)
      throw new sun.dc.pr.PRError("appendQuadratic: unexpected");
    try
    {
      this.curPC.appendQuadratic(paramFloat1, paramFloat2, paramFloat3, paramFloat4);
    }
    catch (PathError localPathError)
    {
      throw new sun.dc.pr.PRError(localPathError.getMessage());
    }
  }

  public void appendCubic(float paramFloat1, float paramFloat2, float paramFloat3, float paramFloat4, float paramFloat5, float paramFloat6)
    throws sun.dc.pr.PRError
  {
    if (this.state != 5)
      throw new sun.dc.pr.PRError("appendCubic: unexpected");
    try
    {
      this.curPC.appendCubic(paramFloat1, paramFloat2, paramFloat3, paramFloat4, paramFloat5, paramFloat6);
    }
    catch (PathError localPathError)
    {
      throw new sun.dc.pr.PRError(localPathError.getMessage());
    }
  }

  public void closedSubpath()
    throws sun.dc.pr.PRError
  {
    if (this.state != 5)
      throw new sun.dc.pr.PRError("closedSubpath: unexpected");
    try
    {
      this.curPC.closedSubpath();
    }
    catch (PathError localPathError)
    {
      throw new sun.dc.pr.PRError(localPathError.getMessage());
    }
  }

  public void endPath()
    throws sun.dc.pr.PRError, sun.dc.pr.PRException
  {
    if ((this.state != 4) && (this.state != 5))
      throw new sun.dc.pr.PRError("endPath: unexpected");
    try
    {
      this.curPC.endPath();
      this.state = 6;
    }
    catch (PathError localPathError)
    {
      throw new sun.dc.pr.PRError(localPathError.getMessage());
    }
    catch (PathException localPathException)
    {
      throw new sun.dc.pr.PRException(localPathException.getMessage());
    }
  }

  public void useProxy(FastPathProducer paramFastPathProducer)
    throws sun.dc.pr.PRError, sun.dc.pr.PRException
  {
    if ((this.state != 2) && (this.state != 3))
      throw new sun.dc.pr.PRError("useProxy: unexpected");
    try
    {
      this.curPC.useProxy(paramFastPathProducer);
      this.state = 6;
    }
    catch (PathError localPathError)
    {
      throw new sun.dc.pr.PRError(localPathError.getMessage());
    }
    catch (PathException localPathException)
    {
      throw new sun.dc.pr.PRException(localPathException.getMessage());
    }
  }

  public void getAlphaBox(int[] paramArrayOfInt)
    throws sun.dc.pr.PRError
  {
    this.filler.getAlphaBox(paramArrayOfInt);
  }

  public void setOutputArea(float paramFloat1, float paramFloat2, int paramInt1, int paramInt2)
    throws sun.dc.pr.PRError, sun.dc.pr.PRException
  {
    this.filler.setOutputArea(paramFloat1, paramFloat2, paramInt1, paramInt2);
  }

  public int getTileState()
    throws sun.dc.pr.PRError
  {
    return this.filler.getTileState();
  }

  public void writeAlpha(byte[] paramArrayOfByte, int paramInt1, int paramInt2, int paramInt3)
    throws sun.dc.pr.PRError, sun.dc.pr.PRException, InterruptedException
  {
    this.filler.writeAlpha(paramArrayOfByte, paramInt1, paramInt2, paramInt3);
  }

  public void writeAlpha(char[] paramArrayOfChar, int paramInt1, int paramInt2, int paramInt3)
    throws sun.dc.pr.PRError, sun.dc.pr.PRException, InterruptedException
  {
    this.filler.writeAlpha(paramArrayOfChar, paramInt1, paramInt2, paramInt3);
  }

  public void nextTile()
    throws sun.dc.pr.PRError
  {
    this.filler.nextTile();
  }

  public void reset()
  {
    this.state = 1;
    this.filler.reset();
    this.stroker.reset();
    this.dasher.reset();
  }

  private static class ConsumerDisposer
  implements DisposerRecord
  {
    PathConsumer filler;
    PathConsumer stroker;
    PathConsumer dasher;

    public ConsumerDisposer(PathConsumer paramPathConsumer1, PathConsumer paramPathConsumer2, PathConsumer paramPathConsumer3)
    {
      this.filler = paramPathConsumer1;
      this.stroker = paramPathConsumer2;
      this.dasher = paramPathConsumer3;
    }

    public void dispose()
    {
      this.filler.dispose();
      this.stroker.dispose();
      this.dasher.dispose();
    }
  }
}