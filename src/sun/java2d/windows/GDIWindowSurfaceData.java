package sun.java2d.windows;

import java.awt.GraphicsConfiguration;
import java.awt.Rectangle;
import java.awt.color.ColorSpace;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import sun.awt.Win32GraphicsConfig;
import sun.awt.Win32GraphicsDevice;
import sun.awt.windows.WComponentPeer;
import sun.awt.windows.WFileDialogPeer;
import sun.awt.windows.WPrintDialogPeer;
import sun.java2d.InvalidPipeException;
import sun.java2d.ScreenUpdateManager;
import sun.java2d.SunGraphics2D;
import sun.java2d.SurfaceData;
import sun.java2d.loops.CompositeType;
import sun.java2d.loops.FontInfo;
import sun.java2d.loops.GraphicsPrimitive;
import sun.java2d.loops.RenderLoops;
import sun.java2d.loops.SurfaceType;
import sun.java2d.loops.XORComposite;
import sun.java2d.pipe.PixelToShapeConverter;
import sun.java2d.pipe.Region;

public class GDIWindowSurfaceData extends SurfaceData
{
  private WComponentPeer peer;
  private Win32GraphicsConfig graphicsConfig;
  private RenderLoops solidloops;
  public static final String DESC_GDI = "GDI";
  public static final SurfaceType AnyGdi = SurfaceType.IntRgb.deriveSubType("GDI");
  public static final SurfaceType IntRgbGdi = SurfaceType.IntRgb.deriveSubType("GDI");
  public static final SurfaceType Ushort565RgbGdi = SurfaceType.Ushort565Rgb.deriveSubType("GDI");
  public static final SurfaceType Ushort555RgbGdi = SurfaceType.Ushort555Rgb.deriveSubType("GDI");
  public static final SurfaceType ThreeByteBgrGdi = SurfaceType.ThreeByteBgr.deriveSubType("GDI");
  protected static GDIRenderer gdiPipe;
  protected static PixelToShapeConverter gdiTxPipe;

  private static native void initIDs(Class paramClass);

  public static SurfaceType getSurfaceType(ColorModel paramColorModel)
  {
    switch (paramColorModel.getPixelSize())
    {
    case 24:
    case 32:
      if (paramColorModel instanceof DirectColorModel)
      {
        if (((DirectColorModel)paramColorModel).getRedMask() == 16711680)
          return IntRgbGdi;
        return SurfaceType.IntRgbx;
      }
      return ThreeByteBgrGdi;
    case 15:
      return Ushort555RgbGdi;
    case 16:
      if ((paramColorModel instanceof DirectColorModel) && (((DirectColorModel)paramColorModel).getBlueMask() == 62))
        return SurfaceType.Ushort555Rgbx;
      return Ushort565RgbGdi;
    case 8:
      if ((paramColorModel.getColorSpace().getType() == 6) && (paramColorModel instanceof ComponentColorModel))
        return SurfaceType.ByteGray;
      if ((paramColorModel instanceof IndexColorModel) && (isOpaqueGray((IndexColorModel)paramColorModel)))
        return SurfaceType.Index8Gray;
      return SurfaceType.ByteIndexedOpaque;
    }
    throw new InvalidPipeException("Unsupported bit depth: " + paramColorModel.getPixelSize());
  }

  public static GDIWindowSurfaceData createData(WComponentPeer paramWComponentPeer)
  {
    SurfaceType localSurfaceType = getSurfaceType(paramWComponentPeer.getDeviceColorModel());
    return new GDIWindowSurfaceData(paramWComponentPeer, localSurfaceType);
  }

  public Raster getRaster(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    throw new InternalError("not implemented yet");
  }

  public void validatePipe(SunGraphics2D paramSunGraphics2D)
  {
    if ((paramSunGraphics2D.antialiasHint != 2) && (paramSunGraphics2D.paintState <= 1) && (((paramSunGraphics2D.compositeState <= 0) || (paramSunGraphics2D.compositeState == 2))))
    {
      if (paramSunGraphics2D.clipState == 2)
        super.validatePipe(paramSunGraphics2D);
      else
        switch (paramSunGraphics2D.textAntialiasHint)
        {
        case 0:
        case 1:
          paramSunGraphics2D.textpipe = solidTextRenderer;
          break;
        case 2:
          paramSunGraphics2D.textpipe = aaTextRenderer;
          break;
        default:
          switch (paramSunGraphics2D.getFontInfo().aaHint)
          {
          case 4:
          case 6:
            paramSunGraphics2D.textpipe = lcdTextRenderer;
            break;
          case 2:
            paramSunGraphics2D.textpipe = aaTextRenderer;
            break;
          case 3:
          case 5:
          default:
            paramSunGraphics2D.textpipe = solidTextRenderer;
          }
        }
      paramSunGraphics2D.imagepipe = imagepipe;
      if (paramSunGraphics2D.transformState >= 3)
      {
        paramSunGraphics2D.drawpipe = gdiTxPipe;
        paramSunGraphics2D.fillpipe = gdiTxPipe;
      }
      else if (paramSunGraphics2D.strokeState != 0)
      {
        paramSunGraphics2D.drawpipe = gdiTxPipe;
        paramSunGraphics2D.fillpipe = gdiPipe;
      }
      else
      {
        paramSunGraphics2D.drawpipe = gdiPipe;
        paramSunGraphics2D.fillpipe = gdiPipe;
      }
      paramSunGraphics2D.shapepipe = gdiPipe;
      paramSunGraphics2D.loops = getRenderLoops(paramSunGraphics2D);
    }
    else
    {
      super.validatePipe(paramSunGraphics2D);
    }
  }

  public RenderLoops getRenderLoops(SunGraphics2D paramSunGraphics2D)
  {
    if ((paramSunGraphics2D.paintState <= 1) && (paramSunGraphics2D.compositeState <= 0))
      return this.solidloops;
    return super.getRenderLoops(paramSunGraphics2D);
  }

  public GraphicsConfiguration getDeviceConfiguration()
  {
    return this.graphicsConfig;
  }

  private native void initOps(WComponentPeer paramWComponentPeer, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5);

  private GDIWindowSurfaceData(WComponentPeer paramWComponentPeer, SurfaceType paramSurfaceType)
  {
    super(paramSurfaceType, paramWComponentPeer.getDeviceColorModel());
    ColorModel localColorModel = paramWComponentPeer.getDeviceColorModel();
    this.peer = paramWComponentPeer;
    int i = 0;
    int j = 0;
    int k = 0;
    switch (localColorModel.getPixelSize())
    {
    case 24:
    case 32:
      if (localColorModel instanceof DirectColorModel)
        l = 32;
      else
        l = 24;
      break;
    default:
      l = localColorModel.getPixelSize();
    }
    if (localColorModel instanceof DirectColorModel)
    {
      localObject = (DirectColorModel)localColorModel;
      i = ((DirectColorModel)localObject).getRedMask();
      j = ((DirectColorModel)localObject).getGreenMask();
      k = ((DirectColorModel)localObject).getBlueMask();
    }
    this.graphicsConfig = ((Win32GraphicsConfig)paramWComponentPeer.getGraphicsConfiguration());
    this.solidloops = this.graphicsConfig.getSolidLoops(paramSurfaceType);
    if ((paramWComponentPeer instanceof WFileDialogPeer) || (paramWComponentPeer instanceof WPrintDialogPeer))
      return;
    Object localObject = (Win32GraphicsDevice)this.graphicsConfig.getDevice();
    initOps(paramWComponentPeer, l, i, j, k, ((Win32GraphicsDevice)localObject).getScreen());
  }

  public SurfaceData getReplacement()
  {
    ScreenUpdateManager localScreenUpdateManager = ScreenUpdateManager.getInstance();
    return localScreenUpdateManager.getReplacementScreenSurface(this.peer, this);
  }

  public Rectangle getBounds()
  {
    Rectangle localRectangle = this.peer.getBounds();
    localRectangle.x = (localRectangle.y = 0);
    return localRectangle;
  }

  public boolean copyArea(SunGraphics2D paramSunGraphics2D, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6)
  {
    CompositeType localCompositeType = paramSunGraphics2D.imageComp;
    if ((paramSunGraphics2D.transformState < 3) && (paramSunGraphics2D.clipState != 2) && (((CompositeType.SrcOverNoEa.equals(localCompositeType)) || (CompositeType.SrcNoEa.equals(localCompositeType)))))
    {
      paramInt1 += paramSunGraphics2D.transX;
      paramInt2 += paramSunGraphics2D.transY;
      int i = paramInt1 + paramInt5;
      int j = paramInt2 + paramInt6;
      int k = i + paramInt3;
      int l = j + paramInt4;
      Region localRegion = paramSunGraphics2D.getCompClip();
      if (i < localRegion.getLoX())
        i = localRegion.getLoX();
      if (j < localRegion.getLoY())
        j = localRegion.getLoY();
      if (k > localRegion.getHiX())
        k = localRegion.getHiX();
      if (l > localRegion.getHiY())
        l = localRegion.getHiY();
      if ((i < k) && (j < l))
        gdiPipe.devCopyArea(this, i - paramInt5, j - paramInt6, paramInt5, paramInt6, k - i, l - j);
      return true;
    }
    return false;
  }

  private native void invalidateSD();

  public void invalidate()
  {
    if (isValid())
    {
      invalidateSD();
      super.invalidate();
    }
  }

  public Object getDestination()
  {
    return this.peer.getTarget();
  }

  public WComponentPeer getPeer()
  {
    return this.peer;
  }

  static
  {
    initIDs(XORComposite.class);
    if (WindowsFlags.isGdiBlitEnabled())
      GDIBlitLoops.register();
    gdiPipe = new GDIRenderer();
    if (GraphicsPrimitive.tracingEnabled())
      gdiPipe = gdiPipe.traceWrap();
    gdiTxPipe = new PixelToShapeConverter(gdiPipe);
  }
}