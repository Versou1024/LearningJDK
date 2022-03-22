package sun.java2d.opengl;

import java.awt.AlphaComposite;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.security.AccessController;
import sun.awt.image.PixelConverter.ArgbPre;
import sun.java2d.SunGraphics2D;
import sun.java2d.SurfaceData;
import sun.java2d.loops.CompositeType;
import sun.java2d.loops.GraphicsPrimitive;
import sun.java2d.loops.MaskFill;
import sun.java2d.loops.SurfaceType;
import sun.java2d.pipe.ParallelogramPipe;
import sun.java2d.pipe.PixelToParallelogramConverter;
import sun.java2d.pipe.RenderBuffer;
import sun.java2d.pipe.TextPipe;
import sun.java2d.pipe.hw.AccelSurface;
import sun.security.action.GetPropertyAction;

public abstract class OGLSurfaceData extends SurfaceData
  implements AccelSurface
{
  public static final int PBUFFER = 2;
  public static final int FBOBJECT = 5;
  public static final int PF_INT_ARGB = 0;
  public static final int PF_INT_ARGB_PRE = 1;
  public static final int PF_INT_RGB = 2;
  public static final int PF_INT_RGBX = 3;
  public static final int PF_INT_BGR = 4;
  public static final int PF_INT_BGRX = 5;
  public static final int PF_USHORT_565_RGB = 6;
  public static final int PF_USHORT_555_RGB = 7;
  public static final int PF_USHORT_555_RGBX = 8;
  public static final int PF_BYTE_GRAY = 9;
  public static final int PF_USHORT_GRAY = 10;
  public static final int PF_3BYTE_BGR = 11;
  private static final String DESC_OPENGL_SURFACE = "OpenGL Surface";
  private static final String DESC_OPENGL_SURFACE_RTT = "OpenGL Surface (render-to-texture)";
  private static final String DESC_OPENGL_TEXTURE = "OpenGL Texture";
  static final SurfaceType OpenGLSurface = SurfaceType.Any.deriveSubType("OpenGL Surface", PixelConverter.ArgbPre.instance);
  static final SurfaceType OpenGLSurfaceRTT = OpenGLSurface.deriveSubType("OpenGL Surface (render-to-texture)");
  static final SurfaceType OpenGLTexture = SurfaceType.Any.deriveSubType("OpenGL Texture");
  private static boolean isFBObjectEnabled;
  private static boolean isLCDShaderEnabled;
  private static boolean isBIOpShaderEnabled;
  private static boolean isGradShaderEnabled;
  private OGLGraphicsConfig graphicsConfig;
  protected int type;
  private int nativeWidth;
  private int nativeHeight;
  protected static OGLRenderer oglRenderPipe;
  protected static PixelToParallelogramConverter oglTxRenderPipe;
  protected static ParallelogramPipe oglAAPgramPipe;
  protected static OGLTextRenderer oglTextPipe;
  protected static OGLDrawImage oglImagePipe;

  protected native boolean initTexture(long paramLong, boolean paramBoolean1, boolean paramBoolean2, boolean paramBoolean3, int paramInt1, int paramInt2);

  protected native boolean initFBObject(long paramLong, boolean paramBoolean1, boolean paramBoolean2, boolean paramBoolean3, int paramInt1, int paramInt2);

  protected native boolean initFlipBackbuffer(long paramLong);

  protected abstract boolean initPbuffer(long paramLong1, long paramLong2, boolean paramBoolean, int paramInt1, int paramInt2);

  private native int getTextureTarget(long paramLong);

  private native int getTextureID(long paramLong);

  protected OGLSurfaceData(OGLGraphicsConfig paramOGLGraphicsConfig, ColorModel paramColorModel, int paramInt)
  {
    super(getCustomSurfaceType(paramInt), paramColorModel);
    this.graphicsConfig = paramOGLGraphicsConfig;
    this.type = paramInt;
  }

  private static SurfaceType getCustomSurfaceType(int paramInt)
  {
    switch (paramInt)
    {
    case 3:
      return OpenGLTexture;
    case 5:
      return OpenGLSurfaceRTT;
    case 2:
    case 4:
    }
    return OpenGLSurface;
  }

  private void initSurfaceNow(int paramInt1, int paramInt2)
  {
    boolean bool1 = getTransparency() == 1;
    boolean bool2 = false;
    switch (this.type)
    {
    case 2:
      bool2 = initPbuffer(getNativeOps(), this.graphicsConfig.getNativeConfigInfo(), bool1, paramInt1, paramInt2);
      break;
    case 3:
      bool2 = initTexture(getNativeOps(), bool1, isTexNonPow2Available(), isTexRectAvailable(), paramInt1, paramInt2);
      break;
    case 5:
      bool2 = initFBObject(getNativeOps(), bool1, isTexNonPow2Available(), isTexRectAvailable(), paramInt1, paramInt2);
      break;
    case 4:
      bool2 = initFlipBackbuffer(getNativeOps());
    }
    if (!(bool2))
      throw new OutOfMemoryError("can't create offscreen surface");
  }

  protected void initSurface(int paramInt1, int paramInt2)
  {
    OGLRenderQueue localOGLRenderQueue = OGLRenderQueue.getInstance();
    localOGLRenderQueue.lock();
    try
    {
      switch (this.type)
      {
      case 2:
      case 3:
      case 5:
        OGLContext.setScratchSurface(this.graphicsConfig);
      case 4:
      }
      localOGLRenderQueue.flushAndInvokeNow(new java.lang.Runnable(this, paramInt1, paramInt2)
      {
        public void run()
        {
          OGLSurfaceData.access$000(this.this$0, this.val$width, this.val$height);
        }
      });
    }
    finally
    {
      localOGLRenderQueue.unlock();
    }
  }

  public final OGLContext getContext()
  {
    return this.graphicsConfig.getContext();
  }

  final OGLGraphicsConfig getOGLGraphicsConfig()
  {
    return this.graphicsConfig;
  }

  public final int getType()
  {
    return this.type;
  }

  public final int getTextureTarget()
  {
    return getTextureTarget(getNativeOps());
  }

  public final int getTextureID()
  {
    return getTextureID(getNativeOps());
  }

  public long getNativeResource(int paramInt)
  {
    if (paramInt == 3)
      return getTextureID();
    return 3412046827397054464L;
  }

  public Raster getRaster(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    throw new InternalError("not implemented yet");
  }

  public boolean canRenderLCDText(SunGraphics2D paramSunGraphics2D)
  {
    return ((this.graphicsConfig.isCapPresent(131072)) && (paramSunGraphics2D.compositeState <= 0) && (paramSunGraphics2D.paintState <= 0) && (paramSunGraphics2D.surfaceData.getTransparency() == 1) && (paramSunGraphics2D.antialiasHint != 2));
  }

  public void validatePipe(SunGraphics2D paramSunGraphics2D)
  {
    Object localObject;
    int i = 0;
    if (((paramSunGraphics2D.compositeState <= 0) && (paramSunGraphics2D.paintState <= 1)) || ((paramSunGraphics2D.compositeState == 1) && (paramSunGraphics2D.paintState <= 1) && (((AlphaComposite)paramSunGraphics2D.composite).getRule() == 3)) || ((paramSunGraphics2D.compositeState == 2) && (paramSunGraphics2D.paintState <= 1)))
    {
      localObject = oglTextPipe;
    }
    else
    {
      super.validatePipe(paramSunGraphics2D);
      localObject = paramSunGraphics2D.textpipe;
      i = 1;
    }
    PixelToParallelogramConverter localPixelToParallelogramConverter1 = null;
    OGLRenderer localOGLRenderer = null;
    if (paramSunGraphics2D.antialiasHint != 2)
      if (paramSunGraphics2D.paintState <= 1)
      {
        if (paramSunGraphics2D.compositeState <= 2)
        {
          localPixelToParallelogramConverter1 = oglTxRenderPipe;
          localOGLRenderer = oglRenderPipe;
        }
      }
      else if ((paramSunGraphics2D.compositeState <= 1) && (OGLPaints.isValid(paramSunGraphics2D)))
      {
        localPixelToParallelogramConverter1 = oglTxRenderPipe;
        localOGLRenderer = oglRenderPipe;
      }
    else if (paramSunGraphics2D.paintState <= 1)
      if ((this.graphicsConfig.isCapPresent(256)) && (((paramSunGraphics2D.imageComp == CompositeType.SrcOverNoEa) || (paramSunGraphics2D.imageComp == CompositeType.SrcOver))))
      {
        if (i == 0)
        {
          super.validatePipe(paramSunGraphics2D);
          i = 1;
        }
        PixelToParallelogramConverter localPixelToParallelogramConverter2 = new PixelToParallelogramConverter(paramSunGraphics2D.shapepipe, oglAAPgramPipe, 0.125D, 0.499D, false);
        paramSunGraphics2D.drawpipe = localPixelToParallelogramConverter2;
        paramSunGraphics2D.fillpipe = localPixelToParallelogramConverter2;
        paramSunGraphics2D.shapepipe = localPixelToParallelogramConverter2;
      }
      else if (paramSunGraphics2D.compositeState == 2)
      {
        localPixelToParallelogramConverter1 = oglTxRenderPipe;
        localOGLRenderer = oglRenderPipe;
      }
    if (localPixelToParallelogramConverter1 != null)
    {
      if (paramSunGraphics2D.transformState >= 3)
      {
        paramSunGraphics2D.drawpipe = localPixelToParallelogramConverter1;
        paramSunGraphics2D.fillpipe = localPixelToParallelogramConverter1;
      }
      else if (paramSunGraphics2D.strokeState != 0)
      {
        paramSunGraphics2D.drawpipe = localPixelToParallelogramConverter1;
        paramSunGraphics2D.fillpipe = localOGLRenderer;
      }
      else
      {
        paramSunGraphics2D.drawpipe = localOGLRenderer;
        paramSunGraphics2D.fillpipe = localOGLRenderer;
      }
      paramSunGraphics2D.shapepipe = localPixelToParallelogramConverter1;
    }
    else if (i == 0)
    {
      super.validatePipe(paramSunGraphics2D);
    }
    paramSunGraphics2D.textpipe = ((TextPipe)localObject);
    paramSunGraphics2D.imagepipe = oglImagePipe;
  }

  protected MaskFill getMaskFill(SunGraphics2D paramSunGraphics2D)
  {
    if ((paramSunGraphics2D.paintState > 1) && (((!(OGLPaints.isValid(paramSunGraphics2D))) || (!(this.graphicsConfig.isCapPresent(16))))))
      return null;
    return super.getMaskFill(paramSunGraphics2D);
  }

  public boolean copyArea(SunGraphics2D paramSunGraphics2D, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6)
  {
    if ((paramSunGraphics2D.transformState < 3) && (paramSunGraphics2D.compositeState < 2))
    {
      paramInt1 += paramSunGraphics2D.transX;
      paramInt2 += paramSunGraphics2D.transY;
      oglRenderPipe.copyArea(paramSunGraphics2D, paramInt1, paramInt2, paramInt3, paramInt4, paramInt5, paramInt6);
      return true;
    }
    return false;
  }

  public void flush()
  {
    OGLRenderQueue localOGLRenderQueue = OGLRenderQueue.getInstance();
    localOGLRenderQueue.lock();
    try
    {
      OGLContext.setScratchSurface(this.graphicsConfig);
      RenderBuffer localRenderBuffer = localOGLRenderQueue.getBuffer();
      localOGLRenderQueue.ensureCapacityAndAlignment(12, 4);
      localRenderBuffer.putInt(72);
      localRenderBuffer.putLong(getNativeOps());
      localOGLRenderQueue.flushNow();
    }
    finally
    {
      localOGLRenderQueue.unlock();
    }
  }

  static void dispose(long paramLong1, long paramLong2)
  {
    OGLRenderQueue localOGLRenderQueue = OGLRenderQueue.getInstance();
    localOGLRenderQueue.lock();
    try
    {
      OGLContext.setScratchSurface(paramLong2);
      RenderBuffer localRenderBuffer = localOGLRenderQueue.getBuffer();
      localOGLRenderQueue.ensureCapacityAndAlignment(12, 4);
      localRenderBuffer.putInt(73);
      localRenderBuffer.putLong(paramLong1);
      localOGLRenderQueue.flushNow();
    }
    finally
    {
      localOGLRenderQueue.unlock();
    }
  }

  static void swapBuffers(long paramLong)
  {
    OGLRenderQueue localOGLRenderQueue = OGLRenderQueue.getInstance();
    localOGLRenderQueue.lock();
    try
    {
      RenderBuffer localRenderBuffer = localOGLRenderQueue.getBuffer();
      localOGLRenderQueue.ensureCapacityAndAlignment(12, 4);
      localRenderBuffer.putInt(80);
      localRenderBuffer.putLong(paramLong);
      localOGLRenderQueue.flushNow();
    }
    finally
    {
      localOGLRenderQueue.unlock();
    }
  }

  boolean isTexNonPow2Available()
  {
    return this.graphicsConfig.isCapPresent(32);
  }

  boolean isTexRectAvailable()
  {
    return this.graphicsConfig.isCapPresent(1048576);
  }

  public Rectangle getNativeBounds()
  {
    OGLRenderQueue localOGLRenderQueue = OGLRenderQueue.getInstance();
    localOGLRenderQueue.lock();
    try
    {
      Rectangle localRectangle = new Rectangle(this.nativeWidth, this.nativeHeight);
      return localRectangle;
    }
    finally
    {
      localOGLRenderQueue.unlock();
    }
  }

  static
  {
    if (!(GraphicsEnvironment.isHeadless()))
    {
      String str1 = (String)AccessController.doPrivileged(new GetPropertyAction("sun.java2d.opengl.fbobject"));
      isFBObjectEnabled = !("false".equals(str1));
      String str2 = (String)AccessController.doPrivileged(new GetPropertyAction("sun.java2d.opengl.lcdshader"));
      isLCDShaderEnabled = !("false".equals(str2));
      String str3 = (String)AccessController.doPrivileged(new GetPropertyAction("sun.java2d.opengl.biopshader"));
      isBIOpShaderEnabled = !("false".equals(str3));
      String str4 = (String)AccessController.doPrivileged(new GetPropertyAction("sun.java2d.opengl.gradshader"));
      isGradShaderEnabled = !("false".equals(str4));
      OGLRenderQueue localOGLRenderQueue = OGLRenderQueue.getInstance();
      oglImagePipe = new OGLDrawImage();
      oglTextPipe = new OGLTextRenderer(localOGLRenderQueue);
      oglRenderPipe = new OGLRenderer(localOGLRenderQueue);
      if (GraphicsPrimitive.tracingEnabled())
        oglTextPipe = oglTextPipe.traceWrap();
      oglAAPgramPipe = oglRenderPipe.getAAParallelogramPipe();
      oglTxRenderPipe = new PixelToParallelogramConverter(oglRenderPipe, oglRenderPipe, 1D, 0.25D, true);
      OGLBlitLoops.register();
      OGLMaskFill.register();
      OGLMaskBlit.register();
    }
  }
}