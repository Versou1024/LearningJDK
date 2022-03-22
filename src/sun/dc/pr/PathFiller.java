package sun.dc.pr;

import java.security.AccessController;
import sun.dc.path.FastPathProducer;
import sun.dc.path.PathConsumer;
import sun.dc.path.PathError;
import sun.dc.path.PathException;
import sun.security.action.LoadLibraryAction;

public class PathFiller
  implements PathConsumer
{
  public static final int EOFILL = 1;
  public static final int NZFILL = 2;
  public static final int MAX_PATH = 1000000;
  public static final int TILE_IS_ALL_0 = 0;
  public static final int TILE_IS_ALL_1 = 1;
  public static final int TILE_IS_GENERAL = 2;
  static int tileSizeL2S;
  private static int tileSize;
  private static float tileSizeF;
  public static final float maxPathF = 1000000.0F;
  private long cData;

  public static final boolean validLoCoord(float paramFloat)
  {
    return (paramFloat >= -1000000.0F);
  }

  public static final boolean validHiCoord(float paramFloat)
  {
    return (paramFloat <= 1000000.0F);
  }

  public PathFiller()
  {
    cInitialize();
    reset();
  }

  public native void dispose();

  protected static void classFinalize()
    throws Throwable
  {
    cClassFinalize();
  }

  public PathConsumer getConsumer()
  {
    return null;
  }

  public native void setFillMode(int paramInt)
    throws sun.dc.pr.PRError;

  public native void beginPath()
    throws PathError;

  public native void beginSubpath(float paramFloat1, float paramFloat2)
    throws PathError;

  public native void appendLine(float paramFloat1, float paramFloat2)
    throws PathError;

  public native void appendQuadratic(float paramFloat1, float paramFloat2, float paramFloat3, float paramFloat4)
    throws PathError;

  public native void appendCubic(float paramFloat1, float paramFloat2, float paramFloat3, float paramFloat4, float paramFloat5, float paramFloat6)
    throws PathError;

  public native void closedSubpath()
    throws PathError;

  public native void endPath()
    throws PathError, PathException;

  public void useProxy(FastPathProducer paramFastPathProducer)
    throws PathError, PathException
  {
    paramFastPathProducer.sendTo(this);
  }

  public native long getCPathConsumer();

  public native void getAlphaBox(int[] paramArrayOfInt)
    throws sun.dc.pr.PRError;

  public native void setOutputArea(float paramFloat1, float paramFloat2, int paramInt1, int paramInt2)
    throws sun.dc.pr.PRError, sun.dc.pr.PRException;

  public native int getTileState()
    throws sun.dc.pr.PRError;

  public void writeAlpha(byte[] paramArrayOfByte, int paramInt1, int paramInt2, int paramInt3)
    throws sun.dc.pr.PRError, sun.dc.pr.PRException, InterruptedException
  {
    writeAlpha8(paramArrayOfByte, paramInt1, paramInt2, paramInt3);
  }

  public void writeAlpha(char[] paramArrayOfChar, int paramInt1, int paramInt2, int paramInt3)
    throws sun.dc.pr.PRError, sun.dc.pr.PRException, InterruptedException
  {
    writeAlpha16(paramArrayOfChar, paramInt1, paramInt2, paramInt3);
  }

  private native void writeAlpha8(byte[] paramArrayOfByte, int paramInt1, int paramInt2, int paramInt3)
    throws sun.dc.pr.PRError, sun.dc.pr.PRException;

  private native void writeAlpha16(char[] paramArrayOfChar, int paramInt1, int paramInt2, int paramInt3)
    throws sun.dc.pr.PRError, sun.dc.pr.PRException;

  public native void nextTile()
    throws sun.dc.pr.PRError;

  public native void reset();

  private static native void cClassInitialize();

  private static native void cClassFinalize();

  private native void cInitialize();

  static
  {
    AccessController.doPrivileged(new LoadLibraryAction("dcpr"));
    cClassInitialize();
  }
}