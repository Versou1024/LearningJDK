package sun.java2d.opengl;

import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.Rectangle;
import sun.java2d.SunGraphics2D;
import sun.java2d.SurfaceData;
import sun.java2d.pipe.Region;

class OGLUtilities
{
  public static final int UNDEFINED = 0;
  public static final int WINDOW = 1;
  public static final int PBUFFER = 2;
  public static final int TEXTURE = 3;
  public static final int FLIP_BACKBUFFER = 4;
  public static final int FBOBJECT = 5;

  public static boolean isQueueFlusherThread()
  {
    return OGLRenderQueue.isQueueFlusherThread();
  }

  public static boolean invokeWithOGLContextCurrent(Graphics paramGraphics, Runnable paramRunnable)
  {
    OGLRenderQueue localOGLRenderQueue = OGLRenderQueue.getInstance();
    localOGLRenderQueue.lock();
    try
    {
      if (paramGraphics != null)
      {
        if (!(paramGraphics instanceof SunGraphics2D))
        {
          int i = 0;
          return i;
        }
        SurfaceData localSurfaceData = ((SunGraphics2D)paramGraphics).surfaceData;
        if (!(localSurfaceData instanceof OGLSurfaceData))
        {
          int j = 0;
          return j;
        }
        OGLContext.validateContext((OGLSurfaceData)localSurfaceData);
      }
      localOGLRenderQueue.flushAndInvokeNow(paramRunnable);
      OGLContext.invalidateCurrentContext();
    }
    finally
    {
      localOGLRenderQueue.unlock();
    }
    return true;
  }

  public static boolean invokeWithOGLSharedContextCurrent(GraphicsConfiguration paramGraphicsConfiguration, Runnable paramRunnable)
  {
    if (!(paramGraphicsConfiguration instanceof OGLGraphicsConfig))
      return false;
    OGLRenderQueue localOGLRenderQueue = OGLRenderQueue.getInstance();
    localOGLRenderQueue.lock();
    try
    {
      OGLContext.setScratchSurface((OGLGraphicsConfig)paramGraphicsConfiguration);
      localOGLRenderQueue.flushAndInvokeNow(paramRunnable);
      OGLContext.invalidateCurrentContext();
    }
    finally
    {
      localOGLRenderQueue.unlock();
    }
    return true;
  }

  public static Rectangle getOGLViewport(Graphics paramGraphics, int paramInt1, int paramInt2)
  {
    if (!(paramGraphics instanceof SunGraphics2D))
      return null;
    SunGraphics2D localSunGraphics2D = (SunGraphics2D)paramGraphics;
    SurfaceData localSurfaceData = localSunGraphics2D.surfaceData;
    int i = localSunGraphics2D.transX;
    int j = localSunGraphics2D.transY;
    Rectangle localRectangle = localSurfaceData.getBounds();
    int k = i;
    int l = localRectangle.height - j + paramInt2;
    return new Rectangle(k, l, paramInt1, paramInt2);
  }

  public static Rectangle getOGLScissorBox(Graphics paramGraphics)
  {
    if (!(paramGraphics instanceof SunGraphics2D))
      return null;
    SunGraphics2D localSunGraphics2D = (SunGraphics2D)paramGraphics;
    SurfaceData localSurfaceData = localSunGraphics2D.surfaceData;
    Region localRegion = localSunGraphics2D.getCompClip();
    if (!(localRegion.isRectangular()))
      return null;
    int i = localRegion.getLoX();
    int j = localRegion.getLoY();
    int k = localRegion.getWidth();
    int l = localRegion.getHeight();
    Rectangle localRectangle = localSurfaceData.getBounds();
    int i1 = i;
    int i2 = localRectangle.height - j + l;
    return new Rectangle(i1, i2, k, l);
  }

  public static Object getOGLSurfaceIdentifier(Graphics paramGraphics)
  {
    if (!(paramGraphics instanceof SunGraphics2D))
      return null;
    return ((SunGraphics2D)paramGraphics).surfaceData;
  }

  public static int getOGLSurfaceType(Graphics paramGraphics)
  {
    if (!(paramGraphics instanceof SunGraphics2D))
      return 0;
    SurfaceData localSurfaceData = ((SunGraphics2D)paramGraphics).surfaceData;
    if (!(localSurfaceData instanceof OGLSurfaceData))
      return 0;
    return ((OGLSurfaceData)localSurfaceData).getType();
  }

  public static int getOGLTextureType(Graphics paramGraphics)
  {
    if (!(paramGraphics instanceof SunGraphics2D))
      return 0;
    SurfaceData localSurfaceData = ((SunGraphics2D)paramGraphics).surfaceData;
    if (!(localSurfaceData instanceof OGLSurfaceData))
      return 0;
    return ((OGLSurfaceData)localSurfaceData).getTextureTarget();
  }
}