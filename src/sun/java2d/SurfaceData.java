package sun.java2d;

import java.awt.AWTPermission;
import java.awt.Color;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.security.Permission;
import sun.awt.image.SurfaceManager;
import sun.java2d.loops.CompositeType;
import sun.java2d.loops.DrawGlyphList;
import sun.java2d.loops.DrawGlyphListAA;
import sun.java2d.loops.DrawGlyphListLCD;
import sun.java2d.loops.DrawLine;
import sun.java2d.loops.DrawPath;
import sun.java2d.loops.DrawPolygons;
import sun.java2d.loops.DrawRect;
import sun.java2d.loops.FillPath;
import sun.java2d.loops.FillRect;
import sun.java2d.loops.FillSpans;
import sun.java2d.loops.FontInfo;
import sun.java2d.loops.MaskFill;
import sun.java2d.loops.RenderCache;
import sun.java2d.loops.RenderLoops;
import sun.java2d.loops.SurfaceType;
import sun.java2d.pipe.AATextRenderer;
import sun.java2d.pipe.AlphaColorPipe;
import sun.java2d.pipe.AlphaPaintPipe;
import sun.java2d.pipe.CompositePipe;
import sun.java2d.pipe.DrawImage;
import sun.java2d.pipe.DrawImagePipe;
import sun.java2d.pipe.DuctusShapeRenderer;
import sun.java2d.pipe.GeneralCompositePipe;
import sun.java2d.pipe.LCDTextRenderer;
import sun.java2d.pipe.LoopPipe;
import sun.java2d.pipe.OutlineTextRenderer;
import sun.java2d.pipe.PixelToShapeConverter;
import sun.java2d.pipe.SolidTextRenderer;
import sun.java2d.pipe.SpanClipRenderer;
import sun.java2d.pipe.SpanShapeRenderer;
import sun.java2d.pipe.SpanShapeRenderer.Composite;
import sun.java2d.pipe.TextPipe;
import sun.java2d.pipe.TextRenderer;

public abstract class SurfaceData
  implements Transparency, DisposerTarget, Surface
{
  private long pData;
  private boolean valid;
  private boolean surfaceLost;
  private SurfaceType surfaceType;
  private ColorModel colorModel;
  private boolean dirty;
  private boolean needsBackup = true;
  private int numCopies;
  private Object disposerReferent = new Object();
  protected static final LoopPipe colorPrimitives;
  public static final TextPipe outlineTextRenderer;
  public static final TextPipe solidTextRenderer;
  public static final TextPipe aaTextRenderer;
  public static final TextPipe lcdTextRenderer;
  protected static final CompositePipe colorPipe;
  protected static final PixelToShapeConverter colorViaShape;
  protected static final TextPipe colorText;
  protected static final CompositePipe clipColorPipe;
  protected static final TextPipe clipColorText;
  protected static final CompositePipe paintPipe;
  protected static final SpanShapeRenderer paintShape;
  protected static final PixelToShapeConverter paintViaShape;
  protected static final TextPipe paintText;
  protected static final CompositePipe clipPaintPipe;
  protected static final TextPipe clipPaintText;
  protected static final CompositePipe compPipe;
  protected static final SpanShapeRenderer compShape;
  protected static final PixelToShapeConverter compViaShape;
  protected static final TextPipe compText;
  protected static final CompositePipe clipCompPipe;
  protected static final TextPipe clipCompText;
  protected static final DrawImagePipe imagepipe;
  static final int LCDLOOP_UNKNOWN = 0;
  static final int LCDLOOP_FOUND = 1;
  static final int LCDLOOP_NOTFOUND = 2;
  int haveLCDLoop;
  private static RenderCache loopcache;
  static Permission compPermission;

  private static native void initIDs();

  protected SurfaceData(SurfaceType paramSurfaceType, ColorModel paramColorModel)
  {
    this.colorModel = paramColorModel;
    this.surfaceType = paramSurfaceType;
    this.valid = true;
  }

  protected SurfaceData()
  {
    this.valid = true;
  }

  public static SurfaceData getSourceSurfaceData(Image paramImage, SurfaceData paramSurfaceData, CompositeType paramCompositeType, Color paramColor, boolean paramBoolean)
  {
    SurfaceManager localSurfaceManager = SurfaceManager.getManager(paramImage);
    return localSurfaceManager.getSourceSurfaceData(paramSurfaceData, paramCompositeType, paramColor, paramBoolean);
  }

  public static SurfaceData getDestSurfaceData(Image paramImage)
  {
    SurfaceManager localSurfaceManager = SurfaceManager.getManager(paramImage);
    return localSurfaceManager.getDestSurfaceData();
  }

  public static SurfaceData restoreContents(Image paramImage)
  {
    SurfaceManager localSurfaceManager = SurfaceManager.getManager(paramImage);
    return localSurfaceManager.restoreContents();
  }

  public void markDirty()
  {
    if (!(this.dirty))
    {
      setDirty(true);
      this.needsBackup = true;
      this.numCopies = 0;
    }
  }

  private synchronized void setDirty(boolean paramBoolean)
  {
    this.dirty = paramBoolean;
    if (this.pData != 3412046672778231808L)
      setDirtyNative(this, paramBoolean);
  }

  public synchronized boolean isDirty()
  {
    return this.dirty;
  }

  private static native void setDirtyNative(SurfaceData paramSurfaceData, boolean paramBoolean);

  public void setNeedsBackup(boolean paramBoolean)
  {
    this.needsBackup = paramBoolean;
    if (paramBoolean)
      this.numCopies = 0;
    else
      setDirty(false);
  }

  public boolean needsBackup()
  {
    return this.needsBackup;
  }

  public void setSurfaceLost(boolean paramBoolean)
  {
    this.surfaceLost = paramBoolean;
  }

  public boolean isSurfaceLost()
  {
    return this.surfaceLost;
  }

  public final int getNumCopies()
  {
    return this.numCopies;
  }

  public int increaseNumCopies()
  {
    if (this.dirty)
      setDirty(false);
    this.numCopies += 1;
    return this.numCopies;
  }

  public final boolean isValid()
  {
    return this.valid;
  }

  public Object getDisposerReferent()
  {
    return this.disposerReferent;
  }

  public long getNativeOps()
  {
    return this.pData;
  }

  public void invalidate()
  {
    this.valid = false;
  }

  public abstract SurfaceData getReplacement();

  public boolean canRenderLCDText(SunGraphics2D paramSunGraphics2D)
  {
    if ((paramSunGraphics2D.compositeState <= 0) && (paramSunGraphics2D.paintState <= 1) && (paramSunGraphics2D.clipState <= 1) && (paramSunGraphics2D.antialiasHint != 2))
    {
      if (this.haveLCDLoop == 0)
      {
        DrawGlyphListLCD localDrawGlyphListLCD = DrawGlyphListLCD.locate(SurfaceType.AnyColor, CompositeType.SrcNoEa, getSurfaceType());
        this.haveLCDLoop = ((localDrawGlyphListLCD != null) ? 1 : 2);
      }
      return (this.haveLCDLoop == 1);
    }
    return false;
  }

  public void validatePipe(SunGraphics2D paramSunGraphics2D)
  {
    paramSunGraphics2D.imagepipe = imagepipe;
    if (paramSunGraphics2D.compositeState == 2)
    {
      if (paramSunGraphics2D.paintState > 1)
      {
        paramSunGraphics2D.drawpipe = paintViaShape;
        paramSunGraphics2D.fillpipe = paintViaShape;
        paramSunGraphics2D.shapepipe = paintShape;
        paramSunGraphics2D.textpipe = outlineTextRenderer;
      }
      else
      {
        if (paramSunGraphics2D.clipState == 2)
        {
          paramSunGraphics2D.drawpipe = colorViaShape;
          paramSunGraphics2D.fillpipe = colorViaShape;
          paramSunGraphics2D.textpipe = outlineTextRenderer;
        }
        else
        {
          if (paramSunGraphics2D.transformState >= 3)
          {
            paramSunGraphics2D.drawpipe = colorViaShape;
            paramSunGraphics2D.fillpipe = colorViaShape;
          }
          else
          {
            if (paramSunGraphics2D.strokeState != 0)
              paramSunGraphics2D.drawpipe = colorViaShape;
            else
              paramSunGraphics2D.drawpipe = colorPrimitives;
            paramSunGraphics2D.fillpipe = colorPrimitives;
          }
          paramSunGraphics2D.textpipe = solidTextRenderer;
        }
        paramSunGraphics2D.shapepipe = colorPrimitives;
        paramSunGraphics2D.loops = getRenderLoops(paramSunGraphics2D);
      }
    }
    else if (paramSunGraphics2D.compositeState == 3)
    {
      if (paramSunGraphics2D.antialiasHint == 2)
      {
        if (paramSunGraphics2D.clipState == 2)
        {
          paramSunGraphics2D.drawpipe = AA.clipCompViaShape;
          paramSunGraphics2D.fillpipe = AA.clipCompViaShape;
          paramSunGraphics2D.shapepipe = AA.clipCompShape;
          paramSunGraphics2D.textpipe = clipCompText;
        }
        else
        {
          paramSunGraphics2D.drawpipe = AA.compViaShape;
          paramSunGraphics2D.fillpipe = AA.compViaShape;
          paramSunGraphics2D.shapepipe = AA.compShape;
          paramSunGraphics2D.textpipe = compText;
        }
      }
      else
      {
        paramSunGraphics2D.drawpipe = compViaShape;
        paramSunGraphics2D.fillpipe = compViaShape;
        paramSunGraphics2D.shapepipe = compShape;
        if (paramSunGraphics2D.clipState == 2)
          paramSunGraphics2D.textpipe = clipCompText;
        else
          paramSunGraphics2D.textpipe = compText;
      }
    }
    else if (paramSunGraphics2D.antialiasHint == 2)
    {
      paramSunGraphics2D.alphafill = getMaskFill(paramSunGraphics2D);
      if (paramSunGraphics2D.alphafill != null)
      {
        if (paramSunGraphics2D.clipState == 2)
        {
          paramSunGraphics2D.drawpipe = AA.clipColorViaShape;
          paramSunGraphics2D.fillpipe = AA.clipColorViaShape;
          paramSunGraphics2D.shapepipe = AA.clipColorShape;
          paramSunGraphics2D.textpipe = clipColorText;
        }
        else
        {
          paramSunGraphics2D.drawpipe = AA.colorViaShape;
          paramSunGraphics2D.fillpipe = AA.colorViaShape;
          paramSunGraphics2D.shapepipe = AA.colorShape;
          paramSunGraphics2D.textpipe = colorText;
        }
      }
      else if (paramSunGraphics2D.clipState == 2)
      {
        paramSunGraphics2D.drawpipe = AA.clipPaintViaShape;
        paramSunGraphics2D.fillpipe = AA.clipPaintViaShape;
        paramSunGraphics2D.shapepipe = AA.clipPaintShape;
        paramSunGraphics2D.textpipe = clipPaintText;
      }
      else
      {
        paramSunGraphics2D.drawpipe = AA.paintViaShape;
        paramSunGraphics2D.fillpipe = AA.paintViaShape;
        paramSunGraphics2D.shapepipe = AA.paintShape;
        paramSunGraphics2D.textpipe = paintText;
      }
    }
    else if ((paramSunGraphics2D.paintState > 1) || (paramSunGraphics2D.compositeState > 0) || (paramSunGraphics2D.clipState == 2))
    {
      paramSunGraphics2D.drawpipe = paintViaShape;
      paramSunGraphics2D.fillpipe = paintViaShape;
      paramSunGraphics2D.shapepipe = paintShape;
      paramSunGraphics2D.alphafill = getMaskFill(paramSunGraphics2D);
      if (paramSunGraphics2D.alphafill != null)
        if (paramSunGraphics2D.clipState == 2)
          paramSunGraphics2D.textpipe = clipColorText;
        else
          paramSunGraphics2D.textpipe = colorText;
      else if (paramSunGraphics2D.clipState == 2)
        paramSunGraphics2D.textpipe = clipPaintText;
      else
        paramSunGraphics2D.textpipe = paintText;
    }
    else
    {
      if (paramSunGraphics2D.transformState >= 3)
      {
        paramSunGraphics2D.drawpipe = colorViaShape;
        paramSunGraphics2D.fillpipe = colorViaShape;
      }
      else
      {
        if (paramSunGraphics2D.strokeState != 0)
          paramSunGraphics2D.drawpipe = colorViaShape;
        else
          paramSunGraphics2D.drawpipe = colorPrimitives;
        paramSunGraphics2D.fillpipe = colorPrimitives;
      }
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
      paramSunGraphics2D.shapepipe = colorPrimitives;
      paramSunGraphics2D.loops = getRenderLoops(paramSunGraphics2D);
    }
  }

  private static SurfaceType getPaintSurfaceType(SunGraphics2D paramSunGraphics2D)
  {
    switch (paramSunGraphics2D.paintState)
    {
    case 0:
      return SurfaceType.OpaqueColor;
    case 1:
      return SurfaceType.AnyColor;
    case 2:
      if (paramSunGraphics2D.paint.getTransparency() == 1)
        return SurfaceType.OpaqueGradientPaint;
      return SurfaceType.GradientPaint;
    case 3:
      if (paramSunGraphics2D.paint.getTransparency() == 1)
        return SurfaceType.OpaqueLinearGradientPaint;
      return SurfaceType.LinearGradientPaint;
    case 4:
      if (paramSunGraphics2D.paint.getTransparency() == 1)
        return SurfaceType.OpaqueRadialGradientPaint;
      return SurfaceType.RadialGradientPaint;
    case 5:
      if (paramSunGraphics2D.paint.getTransparency() == 1)
        return SurfaceType.OpaqueTexturePaint;
      return SurfaceType.TexturePaint;
    case 6:
    }
    return SurfaceType.AnyPaint;
  }

  protected MaskFill getMaskFill(SunGraphics2D paramSunGraphics2D)
  {
    return MaskFill.getFromCache(getPaintSurfaceType(paramSunGraphics2D), paramSunGraphics2D.imageComp, getSurfaceType());
  }

  public RenderLoops getRenderLoops(SunGraphics2D paramSunGraphics2D)
  {
    SurfaceType localSurfaceType1 = getPaintSurfaceType(paramSunGraphics2D);
    CompositeType localCompositeType = (paramSunGraphics2D.compositeState == 0) ? CompositeType.SrcNoEa : paramSunGraphics2D.imageComp;
    SurfaceType localSurfaceType2 = paramSunGraphics2D.getSurfaceData().getSurfaceType();
    Object localObject = loopcache.get(localSurfaceType1, localCompositeType, localSurfaceType2);
    if (localObject != null)
      return ((RenderLoops)localObject);
    RenderLoops localRenderLoops = makeRenderLoops(localSurfaceType1, localCompositeType, localSurfaceType2);
    loopcache.put(localSurfaceType1, localCompositeType, localSurfaceType2, localRenderLoops);
    return localRenderLoops;
  }

  public static RenderLoops makeRenderLoops(SurfaceType paramSurfaceType1, CompositeType paramCompositeType, SurfaceType paramSurfaceType2)
  {
    RenderLoops localRenderLoops = new RenderLoops();
    localRenderLoops.drawLineLoop = DrawLine.locate(paramSurfaceType1, paramCompositeType, paramSurfaceType2);
    localRenderLoops.fillRectLoop = FillRect.locate(paramSurfaceType1, paramCompositeType, paramSurfaceType2);
    localRenderLoops.drawRectLoop = DrawRect.locate(paramSurfaceType1, paramCompositeType, paramSurfaceType2);
    localRenderLoops.drawPolygonsLoop = DrawPolygons.locate(paramSurfaceType1, paramCompositeType, paramSurfaceType2);
    localRenderLoops.drawPathLoop = DrawPath.locate(paramSurfaceType1, paramCompositeType, paramSurfaceType2);
    localRenderLoops.fillPathLoop = FillPath.locate(paramSurfaceType1, paramCompositeType, paramSurfaceType2);
    localRenderLoops.fillSpansLoop = FillSpans.locate(paramSurfaceType1, paramCompositeType, paramSurfaceType2);
    localRenderLoops.drawGlyphListLoop = DrawGlyphList.locate(paramSurfaceType1, paramCompositeType, paramSurfaceType2);
    localRenderLoops.drawGlyphListAALoop = DrawGlyphListAA.locate(paramSurfaceType1, paramCompositeType, paramSurfaceType2);
    localRenderLoops.drawGlyphListLCDLoop = DrawGlyphListLCD.locate(paramSurfaceType1, paramCompositeType, paramSurfaceType2);
    return localRenderLoops;
  }

  public abstract GraphicsConfiguration getDeviceConfiguration();

  public final SurfaceType getSurfaceType()
  {
    return this.surfaceType;
  }

  public final ColorModel getColorModel()
  {
    return this.colorModel;
  }

  public int getTransparency()
  {
    return getColorModel().getTransparency();
  }

  public abstract Raster getRaster(int paramInt1, int paramInt2, int paramInt3, int paramInt4);

  public boolean useTightBBoxes()
  {
    return true;
  }

  public int pixelFor(int paramInt)
  {
    return this.surfaceType.pixelFor(paramInt, this.colorModel);
  }

  public int pixelFor(Color paramColor)
  {
    return pixelFor(paramColor.getRGB());
  }

  public int rgbFor(int paramInt)
  {
    return this.surfaceType.rgbFor(paramInt, this.colorModel);
  }

  public abstract Rectangle getBounds();

  protected void checkCustomComposite()
  {
    SecurityManager localSecurityManager = System.getSecurityManager();
    if (localSecurityManager != null)
    {
      if (compPermission == null)
        compPermission = new AWTPermission("readDisplayPixels");
      localSecurityManager.checkPermission(compPermission);
    }
  }

  protected static native boolean isOpaqueGray(IndexColorModel paramIndexColorModel);

  public static boolean isNull(SurfaceData paramSurfaceData)
  {
    return ((paramSurfaceData == null) || (paramSurfaceData == NullSurfaceData.theInstance));
  }

  public boolean copyArea(SunGraphics2D paramSunGraphics2D, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6)
  {
    return false;
  }

  public void flush()
  {
  }

  public abstract Object getDestination();

  static
  {
    initIDs();
    colorPrimitives = new LoopPipe();
    outlineTextRenderer = new OutlineTextRenderer();
    solidTextRenderer = new SolidTextRenderer();
    aaTextRenderer = new AATextRenderer();
    lcdTextRenderer = new LCDTextRenderer();
    colorPipe = new AlphaColorPipe();
    colorViaShape = new PixelToShapeConverter(colorPrimitives);
    colorText = new TextRenderer(colorPipe);
    clipColorPipe = new SpanClipRenderer(colorPipe);
    clipColorText = new TextRenderer(clipColorPipe);
    paintPipe = new AlphaPaintPipe();
    paintShape = new SpanShapeRenderer.Composite(paintPipe);
    paintViaShape = new PixelToShapeConverter(paintShape);
    paintText = new TextRenderer(paintPipe);
    clipPaintPipe = new SpanClipRenderer(paintPipe);
    clipPaintText = new TextRenderer(clipPaintPipe);
    compPipe = new GeneralCompositePipe();
    compShape = new SpanShapeRenderer.Composite(compPipe);
    compViaShape = new PixelToShapeConverter(compShape);
    compText = new TextRenderer(compPipe);
    clipCompPipe = new SpanClipRenderer(compPipe);
    clipCompText = new TextRenderer(clipCompPipe);
    imagepipe = new DrawImage();
    loopcache = new RenderCache(30);
  }

  protected static class AA
  {
    public static final DuctusShapeRenderer colorShape = new DuctusShapeRenderer(SurfaceData.colorPipe);
    public static final PixelToShapeConverter colorViaShape = new PixelToShapeConverter(colorShape);
    public static final DuctusShapeRenderer clipColorShape = new DuctusShapeRenderer(SurfaceData.clipColorPipe);
    public static final PixelToShapeConverter clipColorViaShape = new PixelToShapeConverter(clipColorShape);
    public static final DuctusShapeRenderer paintShape = new DuctusShapeRenderer(SurfaceData.paintPipe);
    public static final PixelToShapeConverter paintViaShape = new PixelToShapeConverter(paintShape);
    public static final DuctusShapeRenderer clipPaintShape = new DuctusShapeRenderer(SurfaceData.clipPaintPipe);
    public static final PixelToShapeConverter clipPaintViaShape = new PixelToShapeConverter(clipPaintShape);
    public static final DuctusShapeRenderer compShape = new DuctusShapeRenderer(SurfaceData.compPipe);
    public static final PixelToShapeConverter compViaShape = new PixelToShapeConverter(compShape);
    public static final DuctusShapeRenderer clipCompShape = new DuctusShapeRenderer(SurfaceData.clipCompPipe);
    public static final PixelToShapeConverter clipCompViaShape = new PixelToShapeConverter(clipCompShape);
  }
}