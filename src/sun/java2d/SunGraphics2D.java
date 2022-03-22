package sun.java2d;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.RenderingHints.Key;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.TexturePaint;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.awt.geom.Rectangle2D.Float;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorModel;
import java.awt.image.ImageObserver;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.awt.image.renderable.RenderContext;
import java.awt.image.renderable.RenderableImage;
import java.text.AttributedCharacterIterator;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import sun.awt.ConstrainableGraphics;
import sun.awt.SunHints;
import sun.awt.SunHints.Key;
import sun.awt.SunHints.Value;
import sun.font.Font2D;
import sun.font.FontDesignMetrics;
import sun.font.FontManager;
import sun.java2d.loops.Blit;
import sun.java2d.loops.CompositeType;
import sun.java2d.loops.FontInfo;
import sun.java2d.loops.MaskFill;
import sun.java2d.loops.RenderLoops;
import sun.java2d.loops.SurfaceType;
import sun.java2d.loops.XORComposite;
import sun.java2d.pipe.DrawImagePipe;
import sun.java2d.pipe.LoopPipe;
import sun.java2d.pipe.PixelDrawPipe;
import sun.java2d.pipe.PixelFillPipe;
import sun.java2d.pipe.Region;
import sun.java2d.pipe.ShapeDrawPipe;
import sun.java2d.pipe.ShapeSpanIterator;
import sun.java2d.pipe.TextPipe;
import sun.java2d.pipe.ValidatePipe;
import sun.misc.PerformanceLogger;

public final class SunGraphics2D extends Graphics2D
  implements ConstrainableGraphics, Cloneable, DestSurfaceProvider
{
  public static final int PAINT_CUSTOM = 6;
  public static final int PAINT_TEXTURE = 5;
  public static final int PAINT_RAD_GRADIENT = 4;
  public static final int PAINT_LIN_GRADIENT = 3;
  public static final int PAINT_GRADIENT = 2;
  public static final int PAINT_ALPHACOLOR = 1;
  public static final int PAINT_OPAQUECOLOR = 0;
  public static final int COMP_CUSTOM = 3;
  public static final int COMP_XOR = 2;
  public static final int COMP_ALPHA = 1;
  public static final int COMP_ISCOPY = 0;
  public static final int STROKE_CUSTOM = 3;
  public static final int STROKE_WIDE = 2;
  public static final int STROKE_THINDASHED = 1;
  public static final int STROKE_THIN = 0;
  public static final int TRANSFORM_GENERIC = 4;
  public static final int TRANSFORM_TRANSLATESCALE = 3;
  public static final int TRANSFORM_ANY_TRANSLATE = 2;
  public static final int TRANSFORM_INT_TRANSLATE = 1;
  public static final int TRANSFORM_ISIDENT = 0;
  public static final int CLIP_SHAPE = 2;
  public static final int CLIP_RECTANGULAR = 1;
  public static final int CLIP_DEVICE = 0;
  public int eargb;
  public int pixel;
  public SurfaceData surfaceData;
  public PixelDrawPipe drawpipe;
  public PixelFillPipe fillpipe;
  public DrawImagePipe imagepipe;
  public ShapeDrawPipe shapepipe;
  public TextPipe textpipe;
  public MaskFill alphafill;
  public RenderLoops loops;
  public CompositeType imageComp;
  public int paintState;
  public int compositeState;
  public int strokeState;
  public int transformState;
  public int clipState;
  public Color foregroundColor;
  public Color backgroundColor;
  public AffineTransform transform;
  public int transX;
  public int transY;
  protected static final Stroke defaultStroke = new BasicStroke();
  protected static final Composite defaultComposite = AlphaComposite.SrcOver;
  private static final Font defaultFont = new Font("Dialog", 0, 12);
  public Paint paint;
  public Stroke stroke;
  public Composite composite;
  protected Font font;
  protected FontMetrics fontMetrics;
  public int renderHint;
  public int antialiasHint;
  public int textAntialiasHint;
  private int fractionalMetricsHint;
  public int lcdTextContrast;
  private static int lcdTextContrastDefaultValue = 140;
  private int interpolationHint;
  public int strokeHint;
  public int interpolationType;
  public java.awt.RenderingHints hints;
  public Region constrainClip;
  public int constrainX;
  public int constrainY;
  public Region clipRegion;
  public Shape usrClip;
  protected Region devClip;
  private boolean validFontInfo;
  private FontInfo fontInfo;
  private FontInfo glyphVectorFontInfo;
  private FontRenderContext glyphVectorFRC;
  private static final int slowTextTransformMask = 120;
  protected static ValidatePipe invalidpipe;
  private static final double[] IDENT_MATRIX;
  private static final AffineTransform IDENT_ATX;
  private static final int MINALLOCATED = 8;
  private static final int TEXTARRSIZE = 17;
  private static double[][] textTxArr;
  private static AffineTransform[] textAtArr;
  static final int NON_UNIFORM_SCALE_MASK = 36;
  public static final double MinPenSizeAASquared = 0.03999999538064003D;
  public static final double MinPenSizeSquared = 1.0000000010000001D;
  static final int NON_RECTILINEAR_TRANSFORM_MASK = 48;
  Blit lastCAblit;
  Composite lastCAcomp;
  private FontRenderContext cachedFRC;

  public SunGraphics2D(SurfaceData paramSurfaceData, Color paramColor1, Color paramColor2, Font paramFont)
  {
    this.surfaceData = paramSurfaceData;
    this.foregroundColor = paramColor1;
    this.backgroundColor = paramColor2;
    this.transform = new AffineTransform();
    this.stroke = defaultStroke;
    this.composite = defaultComposite;
    this.paint = this.foregroundColor;
    this.imageComp = CompositeType.SrcOverNoEa;
    this.renderHint = 0;
    this.antialiasHint = 1;
    this.textAntialiasHint = 0;
    this.fractionalMetricsHint = 1;
    this.lcdTextContrast = lcdTextContrastDefaultValue;
    this.interpolationHint = -1;
    this.strokeHint = 0;
    this.interpolationType = 1;
    validateColor();
    this.font = paramFont;
    if (this.font == null)
      this.font = defaultFont;
    this.loops = paramSurfaceData.getRenderLoops(this);
    setDevClip(paramSurfaceData.getBounds());
    invalidatePipe();
  }

  protected Object clone()
  {
    SunGraphics2D localSunGraphics2D;
    try
    {
      localSunGraphics2D = (SunGraphics2D)clone();
      localSunGraphics2D.transform = new AffineTransform(this.transform);
      if (this.hints != null)
        localSunGraphics2D.hints = ((java.awt.RenderingHints)this.hints.clone());
      if (this.fontInfo != null)
        if (this.validFontInfo)
          localSunGraphics2D.fontInfo = ((FontInfo)this.fontInfo.clone());
        else
          localSunGraphics2D.fontInfo = null;
      if (this.glyphVectorFontInfo != null)
      {
        localSunGraphics2D.glyphVectorFontInfo = ((FontInfo)this.glyphVectorFontInfo.clone());
        localSunGraphics2D.glyphVectorFRC = this.glyphVectorFRC;
      }
      return localSunGraphics2D;
    }
    catch (CloneNotSupportedException localCloneNotSupportedException)
    {
    }
    return null;
  }

  public Graphics create()
  {
    return ((Graphics)clone());
  }

  public void setDevClip(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    Region localRegion = this.constrainClip;
    if (localRegion == null)
      this.devClip = Region.getInstanceXYWH(paramInt1, paramInt2, paramInt3, paramInt4);
    else
      this.devClip = localRegion.getIntersectionXYWH(paramInt1, paramInt2, paramInt3, paramInt4);
    validateCompClip();
  }

  public void setDevClip(Rectangle paramRectangle)
  {
    setDevClip(paramRectangle.x, paramRectangle.y, paramRectangle.width, paramRectangle.height);
  }

  public void constrain(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    if ((paramInt1 | paramInt2) != 0)
      translate(paramInt1, paramInt2);
    if (this.transformState >= 3)
    {
      clipRect(0, 0, paramInt3, paramInt4);
      return;
    }
    paramInt1 = this.constrainX = this.transX;
    paramInt2 = this.constrainY = this.transY;
    paramInt3 = Region.dimAdd(paramInt1, paramInt3);
    paramInt4 = Region.dimAdd(paramInt2, paramInt4);
    Region localRegion = this.constrainClip;
    if (localRegion == null)
    {
      localRegion = Region.getInstanceXYXY(paramInt1, paramInt2, paramInt3, paramInt4);
    }
    else
    {
      localRegion = localRegion.getIntersectionXYXY(paramInt1, paramInt2, paramInt3, paramInt4);
      if (localRegion == this.constrainClip)
        return;
    }
    this.constrainClip = localRegion;
    if (!(this.devClip.isInsideQuickCheck(localRegion)))
    {
      this.devClip = this.devClip.getIntersection(localRegion);
      validateCompClip();
    }
  }

  protected void invalidatePipe()
  {
    this.drawpipe = invalidpipe;
    this.fillpipe = invalidpipe;
    this.shapepipe = invalidpipe;
    this.textpipe = invalidpipe;
    this.imagepipe = invalidpipe;
  }

  public void validatePipe()
  {
    this.surfaceData.validatePipe(this);
  }

  Shape intersectShapes(Shape paramShape1, Shape paramShape2, boolean paramBoolean1, boolean paramBoolean2)
  {
    if ((paramShape1 instanceof Rectangle) && (paramShape2 instanceof Rectangle))
      return ((Rectangle)paramShape1).intersection((Rectangle)paramShape2);
    if (paramShape1 instanceof Rectangle2D)
      return intersectRectShape((Rectangle2D)paramShape1, paramShape2, paramBoolean1, paramBoolean2);
    if (paramShape2 instanceof Rectangle2D)
      return intersectRectShape((Rectangle2D)paramShape2, paramShape1, paramBoolean2, paramBoolean1);
    return intersectByArea(paramShape1, paramShape2, paramBoolean1, paramBoolean2);
  }

  Shape intersectRectShape(Rectangle2D paramRectangle2D, Shape paramShape, boolean paramBoolean1, boolean paramBoolean2)
  {
    if (paramShape instanceof Rectangle2D)
    {
      Object localObject;
      Rectangle2D localRectangle2D = (Rectangle2D)paramShape;
      if (!(paramBoolean1))
        localObject = paramRectangle2D;
      else if (!(paramBoolean2))
        localObject = localRectangle2D;
      else
        localObject = new Rectangle2D.Float();
      double d1 = Math.max(paramRectangle2D.getX(), localRectangle2D.getX());
      double d2 = Math.min(paramRectangle2D.getX() + paramRectangle2D.getWidth(), localRectangle2D.getX() + localRectangle2D.getWidth());
      double d3 = Math.max(paramRectangle2D.getY(), localRectangle2D.getY());
      double d4 = Math.min(paramRectangle2D.getY() + paramRectangle2D.getHeight(), localRectangle2D.getY() + localRectangle2D.getHeight());
      if ((d2 - d1 < 0D) || (d4 - d3 < 0D))
        ((Rectangle2D)localObject).setFrameFromDiagonal(0D, 0D, 0D, 0D);
      else
        ((Rectangle2D)localObject).setFrameFromDiagonal(d1, d3, d2, d4);
      return localObject;
    }
    if (paramRectangle2D.contains(paramShape.getBounds2D()))
    {
      if (paramBoolean2)
        paramShape = cloneShape(paramShape);
      return paramShape;
    }
    return ((Shape)intersectByArea(paramRectangle2D, paramShape, paramBoolean1, paramBoolean2));
  }

  protected static Shape cloneShape(Shape paramShape)
  {
    return new GeneralPath(paramShape);
  }

  Shape intersectByArea(Shape paramShape1, Shape paramShape2, boolean paramBoolean1, boolean paramBoolean2)
  {
    Area localArea1;
    Area localArea2;
    if ((!(paramBoolean1)) && (paramShape1 instanceof Area))
    {
      localArea1 = (Area)paramShape1;
    }
    else if ((!(paramBoolean2)) && (paramShape2 instanceof Area))
    {
      localArea1 = (Area)paramShape2;
      paramShape2 = paramShape1;
    }
    else
    {
      localArea1 = new Area(paramShape1);
    }
    if (paramShape2 instanceof Area)
      localArea2 = (Area)paramShape2;
    else
      localArea2 = new Area(paramShape2);
    localArea1.intersect(localArea2);
    if (localArea1.isRectangular())
      return localArea1.getBounds();
    return localArea1;
  }

  public Region getCompClip()
  {
    if (!(this.surfaceData.isValid()))
      revalidateAll();
    return this.clipRegion;
  }

  public Font getFont()
  {
    if (this.font == null)
      this.font = defaultFont;
    return this.font;
  }

  public FontInfo checkFontInfo(FontInfo paramFontInfo, Font paramFont, FontRenderContext paramFontRenderContext)
  {
    int i;
    AffineTransform localAffineTransform1;
    int l;
    double d3;
    if (paramFontInfo == null)
      paramFontInfo = new FontInfo();
    float f = paramFont.getSize2D();
    AffineTransform localAffineTransform2 = null;
    if (paramFont.isTransformed())
    {
      localAffineTransform2 = paramFont.getTransform();
      localAffineTransform2.scale(f, f);
      i = localAffineTransform2.getType();
      paramFontInfo.originX = (float)localAffineTransform2.getTranslateX();
      paramFontInfo.originY = (float)localAffineTransform2.getTranslateY();
      localAffineTransform2.translate(-paramFontInfo.originX, -paramFontInfo.originY);
      if (this.transformState >= 3)
      {
        this.transform.getMatrix(paramFontInfo.devTx = new double[4]);
        localAffineTransform1 = new AffineTransform(paramFontInfo.devTx);
        localAffineTransform2.preConcatenate(localAffineTransform1);
      }
      else
      {
        paramFontInfo.devTx = IDENT_MATRIX;
        localAffineTransform1 = IDENT_ATX;
      }
      localAffineTransform2.getMatrix(paramFontInfo.glyphTx = new double[4]);
      double d1 = localAffineTransform2.getShearX();
      d3 = localAffineTransform2.getScaleY();
      if (d1 != 0D)
        d3 = Math.sqrt(d1 * d1 + d3 * d3);
      paramFontInfo.pixelHeight = (int)(Math.abs(d3) + 0.5D);
    }
    else
    {
      i = 0;
      paramFontInfo.originX = (paramFontInfo.originY = 0F);
      if (this.transformState >= 3)
      {
        this.transform.getMatrix(paramFontInfo.devTx = new double[4]);
        localAffineTransform1 = new AffineTransform(paramFontInfo.devTx);
        paramFontInfo.glyphTx = new double[4];
        for (int j = 0; j < 4; ++j)
          paramFontInfo.glyphTx[j] = (paramFontInfo.devTx[j] * f);
        localAffineTransform2 = new AffineTransform(paramFontInfo.glyphTx);
        double d2 = this.transform.getShearX();
        d3 = this.transform.getScaleY();
        if (d2 != 0D)
          d3 = Math.sqrt(d2 * d2 + d3 * d3);
        paramFontInfo.pixelHeight = (int)(Math.abs(d3 * f) + 0.5D);
      }
      else
      {
        k = (int)f;
        if ((f == k) && (k >= 8) && (k < 17))
        {
          paramFontInfo.glyphTx = textTxArr[k];
          localAffineTransform2 = textAtArr[k];
          paramFontInfo.pixelHeight = k;
        }
        else
        {
          paramFontInfo.pixelHeight = (int)(f + 0.5D);
        }
        if (localAffineTransform2 == null)
        {
          paramFontInfo.glyphTx = { f, 0D, 0D, f };
          localAffineTransform2 = new AffineTransform(paramFontInfo.glyphTx);
        }
        paramFontInfo.devTx = IDENT_MATRIX;
        localAffineTransform1 = IDENT_ATX;
      }
    }
    paramFontInfo.font2D = FontManager.getFont2D(paramFont);
    int k = this.fractionalMetricsHint;
    if (k == 0)
      k = 1;
    paramFontInfo.lcdSubPixPos = false;
    if (paramFontRenderContext == null)
      l = this.textAntialiasHint;
    else
      l = ((SunHints.Value)paramFontRenderContext.getAntiAliasingHint()).getIndex();
    if (l == 0)
      if (this.antialiasHint == 2)
        l = 2;
      else
        l = 1;
    else if (l == 3)
      if (paramFontInfo.font2D.useAAForPtSize(paramFontInfo.pixelHeight))
        l = 2;
      else
        l = 1;
    else if (l >= 4)
      if (!(this.surfaceData.canRenderLCDText(this)))
      {
        l = 2;
      }
      else
      {
        paramFontInfo.lcdRGBOrder = true;
        if (l == 5)
        {
          l = 4;
          paramFontInfo.lcdRGBOrder = false;
        }
        else if (l == 7)
        {
          l = 6;
          paramFontInfo.lcdRGBOrder = false;
        }
        paramFontInfo.lcdSubPixPos = ((k == 2) && (l == 4));
      }
    paramFontInfo.aaHint = l;
    paramFontInfo.fontStrike = paramFontInfo.font2D.getStrike(paramFont, localAffineTransform1, localAffineTransform2, l, k);
    return paramFontInfo;
  }

  public static boolean isRotated(double[] paramArrayOfDouble)
  {
    return ((paramArrayOfDouble[0] != paramArrayOfDouble[3]) || (paramArrayOfDouble[1] != 0D) || (paramArrayOfDouble[2] != 0D) || (paramArrayOfDouble[0] <= 0D));
  }

  public void setFont(Font paramFont)
  {
    if ((paramFont != null) && (paramFont != this.font))
    {
      if ((this.textAntialiasHint == 3) && (this.textpipe != invalidpipe))
      {
        if ((this.transformState <= 2) && (!(paramFont.isTransformed())) && (this.fontInfo != null))
          if (((this.fontInfo.aaHint == 2) ? 1 : false) == FontManager.getFont2D(paramFont).useAAForPtSize(paramFont.getSize()))
            break label89;
        this.textpipe = invalidpipe;
      }
      label89: this.font = paramFont;
      this.fontMetrics = null;
      this.validFontInfo = false;
    }
  }

  public FontInfo getFontInfo()
  {
    if (!(this.validFontInfo))
    {
      this.fontInfo = checkFontInfo(this.fontInfo, this.font, null);
      this.validFontInfo = true;
    }
    return this.fontInfo;
  }

  public FontInfo getGVFontInfo(Font paramFont, FontRenderContext paramFontRenderContext)
  {
    if ((this.glyphVectorFontInfo != null) && (this.glyphVectorFontInfo.font == paramFont) && (this.glyphVectorFRC == paramFontRenderContext))
      return this.glyphVectorFontInfo;
    this.glyphVectorFRC = paramFontRenderContext;
    return (this.glyphVectorFontInfo = checkFontInfo(this.glyphVectorFontInfo, paramFont, paramFontRenderContext));
  }

  public FontMetrics getFontMetrics()
  {
    if (this.fontMetrics != null)
      return this.fontMetrics;
    return (this.fontMetrics = FontDesignMetrics.getMetrics(this.font, getFontRenderContext()));
  }

  public FontMetrics getFontMetrics(Font paramFont)
  {
    if ((this.fontMetrics != null) && (paramFont == this.font))
      return this.fontMetrics;
    FontDesignMetrics localFontDesignMetrics = FontDesignMetrics.getMetrics(paramFont, getFontRenderContext());
    if (this.font == paramFont)
      this.fontMetrics = localFontDesignMetrics;
    return localFontDesignMetrics;
  }

  public boolean hit(Rectangle paramRectangle, Shape paramShape, boolean paramBoolean)
  {
    if (paramBoolean)
      paramShape = this.stroke.createStrokedShape(paramShape);
    paramShape = transformShape(paramShape);
    if ((this.constrainX | this.constrainY) != 0)
    {
      paramRectangle = new Rectangle(paramRectangle);
      paramRectangle.translate(this.constrainX, this.constrainY);
    }
    return paramShape.intersects(paramRectangle);
  }

  public ColorModel getDeviceColorModel()
  {
    return this.surfaceData.getColorModel();
  }

  public GraphicsConfiguration getDeviceConfiguration()
  {
    return this.surfaceData.getDeviceConfiguration();
  }

  public final SurfaceData getSurfaceData()
  {
    return this.surfaceData;
  }

  public void setComposite(Composite paramComposite)
  {
    int i;
    CompositeType localCompositeType;
    if (this.composite == paramComposite)
      return;
    if (paramComposite instanceof AlphaComposite)
    {
      AlphaComposite localAlphaComposite = (AlphaComposite)paramComposite;
      localCompositeType = CompositeType.forAlphaComposite(localAlphaComposite);
      if (localCompositeType == CompositeType.SrcOverNoEa)
        if ((this.paintState == 0) || ((this.paintState > 1) && (this.paint.getTransparency() == 1)))
          i = 0;
        else
          i = 1;
      else if ((localCompositeType == CompositeType.SrcNoEa) || (localCompositeType == CompositeType.Src) || (localCompositeType == CompositeType.Clear))
        i = 0;
      else if ((this.surfaceData.getTransparency() == 1) && (localCompositeType == CompositeType.SrcIn))
        i = 0;
      else
        i = 1;
    }
    else if (paramComposite instanceof XORComposite)
    {
      i = 2;
      localCompositeType = CompositeType.Xor;
    }
    else
    {
      if (paramComposite == null)
        throw new IllegalArgumentException("null Composite");
      this.surfaceData.checkCustomComposite();
      i = 3;
      localCompositeType = CompositeType.General;
    }
    if ((this.compositeState != i) || (this.imageComp != localCompositeType))
    {
      this.compositeState = i;
      this.imageComp = localCompositeType;
      invalidatePipe();
    }
    this.composite = paramComposite;
    if (this.paintState <= 1)
      validateColor();
  }

  public void setPaint(Paint paramPaint)
  {
    if (paramPaint instanceof Color)
    {
      setColor((Color)paramPaint);
      return;
    }
    if ((paramPaint == null) || (this.paint == paramPaint))
      return;
    this.paint = paramPaint;
    if (this.imageComp == CompositeType.SrcOverNoEa)
      if (paramPaint.getTransparency() == 1)
        if (this.compositeState != 0)
          this.compositeState = 0;
      else if (this.compositeState == 0)
        this.compositeState = 1;
    Class localClass = paramPaint.getClass();
    if (localClass == GradientPaint.class)
      this.paintState = 2;
    else if (localClass == LinearGradientPaint.class)
      this.paintState = 3;
    else if (localClass == RadialGradientPaint.class)
      this.paintState = 4;
    else if (localClass == TexturePaint.class)
      this.paintState = 5;
    else
      this.paintState = 6;
    invalidatePipe();
  }

  private void validateBasicStroke(BasicStroke paramBasicStroke)
  {
    int i = (this.antialiasHint == 2) ? 1 : 0;
    if (this.transformState < 3)
    {
      if (i != 0)
        if (paramBasicStroke.getLineWidth() <= 0.19999998807907104F)
          if (paramBasicStroke.getDashArray() == null)
            this.strokeState = 0;
          else
            this.strokeState = 1;
        else
          this.strokeState = 2;
      else if (paramBasicStroke == defaultStroke)
        this.strokeState = 0;
      else if (paramBasicStroke.getLineWidth() <= 1F)
        if (paramBasicStroke.getDashArray() == null)
          this.strokeState = 0;
        else
          this.strokeState = 1;
      else
        this.strokeState = 2;
    }
    else
    {
      double d1;
      if ((this.transform.getType() & 0x24) == 0)
      {
        d1 = Math.abs(this.transform.getDeterminant());
      }
      else
      {
        double d2 = this.transform.getScaleX();
        double d3 = this.transform.getShearX();
        double d4 = this.transform.getShearY();
        double d5 = this.transform.getScaleY();
        double d6 = d2 * d2 + d4 * d4;
        double d7 = 2.0D * (d2 * d3 + d4 * d5);
        double d8 = d3 * d3 + d5 * d5;
        double d9 = Math.sqrt(d7 * d7 + (d6 - d8) * (d6 - d8));
        d1 = (d6 + d8 + d9) / 2.0D;
      }
      if (paramBasicStroke != defaultStroke)
        d1 *= paramBasicStroke.getLineWidth() * paramBasicStroke.getLineWidth();
      if (d1 <= ((i != 0) ? 0.03999999538064003D : 1.0000000010000001D))
        if (paramBasicStroke.getDashArray() == null)
          this.strokeState = 0;
        else
          this.strokeState = 1;
      else
        this.strokeState = 2;
    }
  }

  public void setStroke(Stroke paramStroke)
  {
    if (paramStroke == null)
      throw new IllegalArgumentException("null Stroke");
    int i = this.strokeState;
    this.stroke = paramStroke;
    if (paramStroke instanceof BasicStroke)
      validateBasicStroke((BasicStroke)paramStroke);
    else
      this.strokeState = 3;
    if (this.strokeState != i)
      invalidatePipe();
  }

  public void setRenderingHint(RenderingHints.Key paramKey, Object paramObject)
  {
    if (!(paramKey.isCompatibleValue(paramObject)))
      throw new IllegalArgumentException(paramObject + " is not compatible with " + paramKey);
    if (paramKey instanceof SunHints.Key)
    {
      int i;
      int l;
      int j = 0;
      int k = 1;
      SunHints.Key localKey = (SunHints.Key)paramKey;
      if (localKey == SunHints.KEY_TEXT_ANTIALIAS_LCD_CONTRAST)
        l = ((Integer)paramObject).intValue();
      else
        l = ((SunHints.Value)paramObject).getIndex();
      switch (localKey.getIndex())
      {
      case 0:
        i = (this.renderHint != l) ? 1 : 0;
        if (i != 0)
        {
          this.renderHint = l;
          if (this.interpolationHint == -1)
            this.interpolationType = ((l == 2) ? 2 : 1);
        }
        break;
      case 1:
        i = (this.antialiasHint != l) ? 1 : 0;
        this.antialiasHint = l;
        if (i != 0)
        {
          j = ((this.textAntialiasHint == 0) || (this.textAntialiasHint >= 4)) ? 1 : 0;
          if (this.strokeState != 3)
            validateBasicStroke((BasicStroke)this.stroke);
        }
        break;
      case 2:
        i = (this.textAntialiasHint != l) ? 1 : 0;
        j = i;
        this.textAntialiasHint = l;
        break;
      case 3:
        i = (this.fractionalMetricsHint != l) ? 1 : 0;
        j = i;
        this.fractionalMetricsHint = l;
        break;
      case 100:
        i = 0;
        this.lcdTextContrast = l;
        break;
      case 5:
        this.interpolationHint = l;
        switch (l)
        {
        case 2:
          l = 3;
          break;
        case 1:
          l = 2;
          break;
        case 0:
        default:
          l = 1;
        }
        i = (this.interpolationType != l) ? 1 : 0;
        this.interpolationType = l;
        break;
      case 8:
        i = (this.strokeHint != l) ? 1 : 0;
        this.strokeHint = l;
        break;
      default:
        k = 0;
        i = 0;
      }
      if (k != 0)
      {
        if (i != 0)
        {
          invalidatePipe();
          if (j != 0)
          {
            this.fontMetrics = null;
            this.cachedFRC = null;
            this.validFontInfo = false;
            this.glyphVectorFontInfo = null;
          }
        }
        if (this.hints != null)
          this.hints.put(paramKey, paramObject);
        return;
      }
    }
    if (this.hints == null)
      this.hints = makeHints(null);
    this.hints.put(paramKey, paramObject);
  }

  public Object getRenderingHint(RenderingHints.Key paramKey)
  {
    if (this.hints != null)
      return this.hints.get(paramKey);
    if (!(paramKey instanceof SunHints.Key))
      return null;
    int i = ((SunHints.Key)paramKey).getIndex();
    switch (i)
    {
    case 0:
      return SunHints.Value.get(0, this.renderHint);
    case 1:
      return SunHints.Value.get(1, this.antialiasHint);
    case 2:
      return SunHints.Value.get(2, this.textAntialiasHint);
    case 3:
      return SunHints.Value.get(3, this.fractionalMetricsHint);
    case 100:
      return new Integer(this.lcdTextContrast);
    case 5:
      switch (this.interpolationHint)
      {
      case 0:
        return SunHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
      case 1:
        return SunHints.VALUE_INTERPOLATION_BILINEAR;
      case 2:
        return SunHints.VALUE_INTERPOLATION_BICUBIC;
      }
      return null;
    case 8:
      return SunHints.Value.get(8, this.strokeHint);
    }
    return null;
  }

  public void setRenderingHints(Map<?, ?> paramMap)
  {
    this.hints = null;
    this.renderHint = 0;
    this.antialiasHint = 1;
    this.textAntialiasHint = 0;
    this.fractionalMetricsHint = 1;
    this.lcdTextContrast = lcdTextContrastDefaultValue;
    this.interpolationHint = -1;
    this.interpolationType = 1;
    int i = 0;
    Iterator localIterator = paramMap.keySet().iterator();
    while (localIterator.hasNext())
    {
      Object localObject = localIterator.next();
      if ((localObject == SunHints.KEY_RENDERING) || (localObject == SunHints.KEY_ANTIALIASING) || (localObject == SunHints.KEY_TEXT_ANTIALIASING) || (localObject == SunHints.KEY_FRACTIONALMETRICS) || (localObject == SunHints.KEY_TEXT_ANTIALIAS_LCD_CONTRAST) || (localObject == SunHints.KEY_STROKE_CONTROL) || (localObject == SunHints.KEY_INTERPOLATION))
        setRenderingHint((RenderingHints.Key)localObject, paramMap.get(localObject));
      else
        i = 1;
    }
    if (i != 0)
      this.hints = makeHints(paramMap);
    invalidatePipe();
  }

  public void addRenderingHints(Map<?, ?> paramMap)
  {
    int i = 0;
    Iterator localIterator = paramMap.keySet().iterator();
    while (localIterator.hasNext())
    {
      Object localObject = localIterator.next();
      if ((localObject == SunHints.KEY_RENDERING) || (localObject == SunHints.KEY_ANTIALIASING) || (localObject == SunHints.KEY_TEXT_ANTIALIASING) || (localObject == SunHints.KEY_FRACTIONALMETRICS) || (localObject == SunHints.KEY_TEXT_ANTIALIAS_LCD_CONTRAST) || (localObject == SunHints.KEY_STROKE_CONTROL) || (localObject == SunHints.KEY_INTERPOLATION))
        setRenderingHint((RenderingHints.Key)localObject, paramMap.get(localObject));
      else
        i = 1;
    }
    if (i != 0)
      if (this.hints == null)
        this.hints = makeHints(paramMap);
      else
        this.hints.putAll(paramMap);
  }

  public java.awt.RenderingHints getRenderingHints()
  {
    if (this.hints == null)
      return makeHints(null);
    return ((java.awt.RenderingHints)this.hints.clone());
  }

  java.awt.RenderingHints makeHints(Map paramMap)
  {
    Object localObject;
    java.awt.RenderingHints localRenderingHints = new java.awt.RenderingHints(paramMap);
    localRenderingHints.put(SunHints.KEY_RENDERING, SunHints.Value.get(0, this.renderHint));
    localRenderingHints.put(SunHints.KEY_ANTIALIASING, SunHints.Value.get(1, this.antialiasHint));
    localRenderingHints.put(SunHints.KEY_TEXT_ANTIALIASING, SunHints.Value.get(2, this.textAntialiasHint));
    localRenderingHints.put(SunHints.KEY_FRACTIONALMETRICS, SunHints.Value.get(3, this.fractionalMetricsHint));
    localRenderingHints.put(SunHints.KEY_TEXT_ANTIALIAS_LCD_CONTRAST, new Integer(this.lcdTextContrast));
    switch (this.interpolationHint)
    {
    case 0:
      localObject = SunHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
      break;
    case 1:
      localObject = SunHints.VALUE_INTERPOLATION_BILINEAR;
      break;
    case 2:
      localObject = SunHints.VALUE_INTERPOLATION_BICUBIC;
      break;
    default:
      localObject = null;
    }
    if (localObject != null)
      localRenderingHints.put(SunHints.KEY_INTERPOLATION, localObject);
    localRenderingHints.put(SunHints.KEY_STROKE_CONTROL, SunHints.Value.get(8, this.strokeHint));
    return localRenderingHints;
  }

  public void translate(double paramDouble1, double paramDouble2)
  {
    this.transform.translate(paramDouble1, paramDouble2);
    invalidateTransform();
  }

  public void rotate(double paramDouble)
  {
    this.transform.rotate(paramDouble);
    invalidateTransform();
  }

  public void rotate(double paramDouble1, double paramDouble2, double paramDouble3)
  {
    this.transform.rotate(paramDouble1, paramDouble2, paramDouble3);
    invalidateTransform();
  }

  public void scale(double paramDouble1, double paramDouble2)
  {
    this.transform.scale(paramDouble1, paramDouble2);
    invalidateTransform();
  }

  public void shear(double paramDouble1, double paramDouble2)
  {
    this.transform.shear(paramDouble1, paramDouble2);
    invalidateTransform();
  }

  public void transform(AffineTransform paramAffineTransform)
  {
    this.transform.concatenate(paramAffineTransform);
    invalidateTransform();
  }

  public void translate(int paramInt1, int paramInt2)
  {
    this.transform.translate(paramInt1, paramInt2);
    if (this.transformState <= 1)
    {
      this.transX += paramInt1;
      this.transY += paramInt2;
      this.transformState = (((this.transX | this.transY) == 0) ? 0 : 1);
    }
    else
    {
      invalidateTransform();
    }
  }

  public void setTransform(AffineTransform paramAffineTransform)
  {
    if ((this.constrainX | this.constrainY) == 0)
    {
      this.transform.setTransform(paramAffineTransform);
    }
    else
    {
      this.transform.setToTranslation(this.constrainX, this.constrainY);
      this.transform.concatenate(paramAffineTransform);
    }
    invalidateTransform();
  }

  protected void invalidateTransform()
  {
    int i = this.transform.getType();
    int j = this.transformState;
    if (i == 0)
    {
      this.transformState = 0;
      this.transX = (this.transY = 0);
    }
    else if (i == 1)
    {
      double d1 = this.transform.getTranslateX();
      double d2 = this.transform.getTranslateY();
      this.transX = (int)Math.floor(d1 + 0.5D);
      this.transY = (int)Math.floor(d2 + 0.5D);
      if ((d1 == this.transX) && (d2 == this.transY))
        this.transformState = 1;
      else
        this.transformState = 2;
    }
    else if ((i & 0x78) == 0)
    {
      this.transformState = 3;
      this.transX = (this.transY = 0);
    }
    else
    {
      this.transformState = 4;
      this.transX = (this.transY = 0);
    }
    if ((this.transformState >= 3) || (j >= 3))
    {
      this.cachedFRC = null;
      this.validFontInfo = false;
      this.fontMetrics = null;
      this.glyphVectorFontInfo = null;
      if (this.transformState != j)
        invalidatePipe();
    }
    if (this.strokeState != 3)
      validateBasicStroke((BasicStroke)this.stroke);
  }

  public AffineTransform getTransform()
  {
    if ((this.constrainX | this.constrainY) == 0)
      return new AffineTransform(this.transform);
    AffineTransform localAffineTransform = AffineTransform.getTranslateInstance(-this.constrainX, -this.constrainY);
    localAffineTransform.concatenate(this.transform);
    return localAffineTransform;
  }

  public AffineTransform cloneTransform()
  {
    return new AffineTransform(this.transform);
  }

  public Paint getPaint()
  {
    return this.paint;
  }

  public Composite getComposite()
  {
    return this.composite;
  }

  public Color getColor()
  {
    return this.foregroundColor;
  }

  final void validateColor()
  {
    int i;
    if (this.imageComp == CompositeType.Clear)
    {
      i = 0;
    }
    else
    {
      i = this.foregroundColor.getRGB();
      if ((this.compositeState <= 1) && (this.imageComp != CompositeType.SrcNoEa) && (this.imageComp != CompositeType.SrcOverNoEa))
      {
        AlphaComposite localAlphaComposite = (AlphaComposite)this.composite;
        int j = Math.round(localAlphaComposite.getAlpha() * (i >>> 24));
        i = i & 0xFFFFFF | j << 24;
      }
    }
    this.eargb = i;
    this.pixel = this.surfaceData.pixelFor(i);
  }

  public void setColor(Color paramColor)
  {
    if ((paramColor == null) || (paramColor == this.paint))
      return;
    this.paint = (this.foregroundColor = paramColor);
    validateColor();
    if (this.eargb >> 24 == -1)
    {
      if (this.paintState == 0)
        return;
      this.paintState = 0;
      if (this.imageComp == CompositeType.SrcOverNoEa)
        this.compositeState = 0;
    }
    else
    {
      if (this.paintState == 1)
        return;
      this.paintState = 1;
      if (this.imageComp == CompositeType.SrcOverNoEa)
        this.compositeState = 1;
    }
    invalidatePipe();
  }

  public void setBackground(Color paramColor)
  {
    this.backgroundColor = paramColor;
  }

  public Color getBackground()
  {
    return this.backgroundColor;
  }

  public Stroke getStroke()
  {
    return this.stroke;
  }

  public Rectangle getClipBounds()
  {
    Rectangle localRectangle;
    if (this.clipState == 0)
    {
      localRectangle = null;
    }
    else if (this.transformState <= 1)
    {
      if (this.usrClip instanceof Rectangle)
        localRectangle = new Rectangle((Rectangle)this.usrClip);
      else
        localRectangle = this.usrClip.getBounds();
      localRectangle.translate(-this.transX, -this.transY);
    }
    else
    {
      localRectangle = getClip().getBounds();
    }
    return localRectangle;
  }

  public Rectangle getClipBounds(Rectangle paramRectangle)
  {
    if (this.clipState != 0)
      if (this.transformState <= 1)
      {
        if (this.usrClip instanceof Rectangle)
          paramRectangle.setBounds((Rectangle)this.usrClip);
        else
          paramRectangle.setBounds(this.usrClip.getBounds());
        paramRectangle.translate(-this.transX, -this.transY);
      }
      else
      {
        paramRectangle.setBounds(getClip().getBounds());
      }
    else if (paramRectangle == null)
      throw new NullPointerException("null rectangle parameter");
    return paramRectangle;
  }

  public boolean hitClip(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    if ((paramInt3 <= 0) || (paramInt4 <= 0))
      return false;
    if (this.transformState > 1)
    {
      double[] arrayOfDouble = { paramInt1, paramInt2, paramInt1 + paramInt3, paramInt2, paramInt1, paramInt2 + paramInt4, paramInt1 + paramInt3, paramInt2 + paramInt4 };
      this.transform.transform(arrayOfDouble, 0, arrayOfDouble, 0, 4);
      paramInt1 = (int)Math.floor(Math.min(Math.min(arrayOfDouble[0], arrayOfDouble[2]), Math.min(arrayOfDouble[4], arrayOfDouble[6])));
      paramInt2 = (int)Math.floor(Math.min(Math.min(arrayOfDouble[1], arrayOfDouble[3]), Math.min(arrayOfDouble[5], arrayOfDouble[7])));
      paramInt3 = (int)Math.ceil(Math.max(Math.max(arrayOfDouble[0], arrayOfDouble[2]), Math.max(arrayOfDouble[4], arrayOfDouble[6])));
      paramInt4 = (int)Math.ceil(Math.max(Math.max(arrayOfDouble[1], arrayOfDouble[3]), Math.max(arrayOfDouble[5], arrayOfDouble[7])));
    }
    else
    {
      paramInt1 += this.transX;
      paramInt2 += this.transY;
      paramInt3 += paramInt1;
      paramInt4 += paramInt2;
    }
    return (getCompClip().intersectsQuickCheckXYXY(paramInt1, paramInt2, paramInt3, paramInt4));
  }

  protected void validateCompClip()
  {
    int i = this.clipState;
    if (this.usrClip == null)
    {
      this.clipState = 0;
      this.clipRegion = this.devClip;
    }
    else if (this.usrClip instanceof Rectangle2D)
    {
      this.clipState = 1;
      if (this.usrClip instanceof Rectangle)
        this.clipRegion = this.devClip.getIntersection((Rectangle)this.usrClip);
      else
        this.clipRegion = this.devClip.getIntersection(this.usrClip.getBounds());
    }
    else
    {
      PathIterator localPathIterator = this.usrClip.getPathIterator(null);
      int[] arrayOfInt = new int[4];
      ShapeSpanIterator localShapeSpanIterator = LoopPipe.getFillSSI(this);
      try
      {
        localShapeSpanIterator.setOutputArea(this.devClip);
        localShapeSpanIterator.appendPath(localPathIterator);
        localShapeSpanIterator.getPathBox(arrayOfInt);
        Region localRegion = Region.getInstance(arrayOfInt);
        localRegion.appendSpans(localShapeSpanIterator);
        this.clipRegion = localRegion;
        this.clipState = ((localRegion.isRectangular()) ? 1 : 2);
      }
      finally
      {
        localShapeSpanIterator.dispose();
      }
    }
    if ((i != this.clipState) && (((this.clipState == 2) || (i == 2))))
      invalidatePipe();
  }

  protected Shape transformShape(Shape paramShape)
  {
    if (paramShape == null)
      return null;
    if (this.transformState > 1)
      return transformShape(this.transform, paramShape);
    return transformShape(this.transX, this.transY, paramShape);
  }

  public Shape untransformShape(Shape paramShape)
  {
    if (paramShape == null)
      return null;
    if (this.transformState > 1)
      try
      {
        return transformShape(this.transform.createInverse(), paramShape);
      }
      catch (NoninvertibleTransformException localNoninvertibleTransformException)
      {
        return null;
      }
    return transformShape(-this.transX, -this.transY, paramShape);
  }

  protected static Shape transformShape(int paramInt1, int paramInt2, Shape paramShape)
  {
    if (paramShape == null)
      return null;
    if (paramShape instanceof Rectangle)
    {
      localObject = paramShape.getBounds();
      ((Rectangle)localObject).translate(paramInt1, paramInt2);
      return localObject;
    }
    if (paramShape instanceof Rectangle2D)
    {
      localObject = (Rectangle2D)paramShape;
      return new Rectangle2D.Double(((Rectangle2D)localObject).getX() + paramInt1, ((Rectangle2D)localObject).getY() + paramInt2, ((Rectangle2D)localObject).getWidth(), ((Rectangle2D)localObject).getHeight());
    }
    if ((paramInt1 == 0) && (paramInt2 == 0))
      return cloneShape(paramShape);
    Object localObject = AffineTransform.getTranslateInstance(paramInt1, paramInt2);
    return ((Shape)((AffineTransform)localObject).createTransformedShape(paramShape));
  }

  protected static Shape transformShape(AffineTransform paramAffineTransform, Shape paramShape)
  {
    if (paramShape == null)
      return null;
    if ((paramShape instanceof Rectangle2D) && ((paramAffineTransform.getType() & 0x30) == 0))
    {
      Object localObject = (Rectangle2D)paramShape;
      double[] arrayOfDouble = new double[4];
      arrayOfDouble[0] = ((Rectangle2D)localObject).getX();
      arrayOfDouble[1] = ((Rectangle2D)localObject).getY();
      arrayOfDouble[2] = (arrayOfDouble[0] + ((Rectangle2D)localObject).getWidth());
      arrayOfDouble[3] = (arrayOfDouble[1] + ((Rectangle2D)localObject).getHeight());
      paramAffineTransform.transform(arrayOfDouble, 0, arrayOfDouble, 0, 2);
      localObject = new Rectangle2D.Float();
      ((Rectangle2D)localObject).setFrameFromDiagonal(arrayOfDouble[0], arrayOfDouble[1], arrayOfDouble[2], arrayOfDouble[3]);
      return localObject;
    }
    if (paramAffineTransform.isIdentity())
      return cloneShape(paramShape);
    return ((Shape)paramAffineTransform.createTransformedShape(paramShape));
  }

  public void clipRect(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    clip(new Rectangle(paramInt1, paramInt2, paramInt3, paramInt4));
  }

  public void setClip(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    setClip(new Rectangle(paramInt1, paramInt2, paramInt3, paramInt4));
  }

  public Shape getClip()
  {
    return untransformShape(this.usrClip);
  }

  public void setClip(Shape paramShape)
  {
    this.usrClip = transformShape(paramShape);
    validateCompClip();
  }

  public void clip(Shape paramShape)
  {
    paramShape = transformShape(paramShape);
    if (this.usrClip != null)
      paramShape = intersectShapes(this.usrClip, paramShape, true, true);
    this.usrClip = paramShape;
    validateCompClip();
  }

  public void setPaintMode()
  {
    setComposite(AlphaComposite.SrcOver);
  }

  public void setXORMode(Color paramColor)
  {
    if (paramColor == null)
      throw new IllegalArgumentException("null XORColor");
    setComposite(new XORComposite(paramColor, this.surfaceData));
  }

  public void copyArea(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6)
  {
    try
    {
      doCopyArea(paramInt1, paramInt2, paramInt3, paramInt4, paramInt5, paramInt6);
    }
    catch (InvalidPipeException localInvalidPipeException1)
    {
      revalidateAll();
      try
      {
        doCopyArea(paramInt1, paramInt2, paramInt3, paramInt4, paramInt5, paramInt6);
      }
      catch (InvalidPipeException localInvalidPipeException2)
      {
      }
    }
  }

  private void doCopyArea(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6)
  {
    int i;
    int j;
    if ((paramInt3 <= 0) || (paramInt4 <= 0))
      return;
    SurfaceData localSurfaceData = this.surfaceData;
    if (localSurfaceData.copyArea(this, paramInt1, paramInt2, paramInt3, paramInt4, paramInt5, paramInt6))
      return;
    if (this.transformState >= 3)
      throw new InternalError("transformed copyArea not implemented yet");
    Region localRegion = getCompClip();
    Composite localComposite = this.composite;
    if (this.lastCAcomp != localComposite)
    {
      localObject = localSurfaceData.getSurfaceType();
      CompositeType localCompositeType = this.imageComp;
      if ((CompositeType.SrcOverNoEa.equals(localCompositeType)) && (localSurfaceData.getTransparency() == 1))
        localCompositeType = CompositeType.SrcNoEa;
      this.lastCAblit = Blit.locate((SurfaceType)localObject, localCompositeType, (SurfaceType)localObject);
      this.lastCAcomp = localComposite;
    }
    paramInt1 += this.transX;
    paramInt2 += this.transY;
    Object localObject = this.lastCAblit;
    if ((paramInt6 == 0) && (paramInt5 > 0) && (paramInt5 < paramInt3))
    {
      while (paramInt3 > 0)
      {
        i = Math.min(paramInt3, paramInt5);
        paramInt3 -= i;
        j = paramInt1 + paramInt3;
        ((Blit)localObject).Blit(localSurfaceData, localSurfaceData, localComposite, localRegion, j, paramInt2, j + paramInt5, paramInt2 + paramInt6, i, paramInt4);
      }
      return;
    }
    if ((paramInt6 > 0) && (paramInt6 < paramInt4) && (paramInt5 > -paramInt3) && (paramInt5 < paramInt3))
    {
      while (paramInt4 > 0)
      {
        i = Math.min(paramInt4, paramInt6);
        paramInt4 -= i;
        j = paramInt2 + paramInt4;
        ((Blit)localObject).Blit(localSurfaceData, localSurfaceData, localComposite, localRegion, paramInt1, j, paramInt1 + paramInt5, j + paramInt6, paramInt3, i);
      }
      return;
    }
    ((Blit)localObject).Blit(localSurfaceData, localSurfaceData, localComposite, localRegion, paramInt1, paramInt2, paramInt1 + paramInt5, paramInt2 + paramInt6, paramInt3, paramInt4);
  }

  public void drawLine(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    try
    {
      this.drawpipe.drawLine(this, paramInt1, paramInt2, paramInt3, paramInt4);
    }
    catch (InvalidPipeException localInvalidPipeException1)
    {
      revalidateAll();
      try
      {
        this.drawpipe.drawLine(this, paramInt1, paramInt2, paramInt3, paramInt4);
      }
      catch (InvalidPipeException localInvalidPipeException2)
      {
      }
    }
  }

  public void drawRoundRect(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6)
  {
    try
    {
      this.drawpipe.drawRoundRect(this, paramInt1, paramInt2, paramInt3, paramInt4, paramInt5, paramInt6);
    }
    catch (InvalidPipeException localInvalidPipeException1)
    {
      revalidateAll();
      try
      {
        this.drawpipe.drawRoundRect(this, paramInt1, paramInt2, paramInt3, paramInt4, paramInt5, paramInt6);
      }
      catch (InvalidPipeException localInvalidPipeException2)
      {
      }
    }
  }

  public void fillRoundRect(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6)
  {
    try
    {
      this.fillpipe.fillRoundRect(this, paramInt1, paramInt2, paramInt3, paramInt4, paramInt5, paramInt6);
    }
    catch (InvalidPipeException localInvalidPipeException1)
    {
      revalidateAll();
      try
      {
        this.fillpipe.fillRoundRect(this, paramInt1, paramInt2, paramInt3, paramInt4, paramInt5, paramInt6);
      }
      catch (InvalidPipeException localInvalidPipeException2)
      {
      }
    }
  }

  public void drawOval(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    try
    {
      this.drawpipe.drawOval(this, paramInt1, paramInt2, paramInt3, paramInt4);
    }
    catch (InvalidPipeException localInvalidPipeException1)
    {
      revalidateAll();
      try
      {
        this.drawpipe.drawOval(this, paramInt1, paramInt2, paramInt3, paramInt4);
      }
      catch (InvalidPipeException localInvalidPipeException2)
      {
      }
    }
  }

  public void fillOval(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    try
    {
      this.fillpipe.fillOval(this, paramInt1, paramInt2, paramInt3, paramInt4);
    }
    catch (InvalidPipeException localInvalidPipeException1)
    {
      revalidateAll();
      try
      {
        this.fillpipe.fillOval(this, paramInt1, paramInt2, paramInt3, paramInt4);
      }
      catch (InvalidPipeException localInvalidPipeException2)
      {
      }
    }
  }

  public void drawArc(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6)
  {
    try
    {
      this.drawpipe.drawArc(this, paramInt1, paramInt2, paramInt3, paramInt4, paramInt5, paramInt6);
    }
    catch (InvalidPipeException localInvalidPipeException1)
    {
      revalidateAll();
      try
      {
        this.drawpipe.drawArc(this, paramInt1, paramInt2, paramInt3, paramInt4, paramInt5, paramInt6);
      }
      catch (InvalidPipeException localInvalidPipeException2)
      {
      }
    }
  }

  public void fillArc(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6)
  {
    try
    {
      this.fillpipe.fillArc(this, paramInt1, paramInt2, paramInt3, paramInt4, paramInt5, paramInt6);
    }
    catch (InvalidPipeException localInvalidPipeException1)
    {
      revalidateAll();
      try
      {
        this.fillpipe.fillArc(this, paramInt1, paramInt2, paramInt3, paramInt4, paramInt5, paramInt6);
      }
      catch (InvalidPipeException localInvalidPipeException2)
      {
      }
    }
  }

  public void drawPolyline(int[] paramArrayOfInt1, int[] paramArrayOfInt2, int paramInt)
  {
    try
    {
      this.drawpipe.drawPolyline(this, paramArrayOfInt1, paramArrayOfInt2, paramInt);
    }
    catch (InvalidPipeException localInvalidPipeException1)
    {
      revalidateAll();
      try
      {
        this.drawpipe.drawPolyline(this, paramArrayOfInt1, paramArrayOfInt2, paramInt);
      }
      catch (InvalidPipeException localInvalidPipeException2)
      {
      }
    }
  }

  public void drawPolygon(int[] paramArrayOfInt1, int[] paramArrayOfInt2, int paramInt)
  {
    try
    {
      this.drawpipe.drawPolygon(this, paramArrayOfInt1, paramArrayOfInt2, paramInt);
    }
    catch (InvalidPipeException localInvalidPipeException1)
    {
      revalidateAll();
      try
      {
        this.drawpipe.drawPolygon(this, paramArrayOfInt1, paramArrayOfInt2, paramInt);
      }
      catch (InvalidPipeException localInvalidPipeException2)
      {
      }
    }
  }

  public void fillPolygon(int[] paramArrayOfInt1, int[] paramArrayOfInt2, int paramInt)
  {
    try
    {
      this.fillpipe.fillPolygon(this, paramArrayOfInt1, paramArrayOfInt2, paramInt);
    }
    catch (InvalidPipeException localInvalidPipeException1)
    {
      revalidateAll();
      try
      {
        this.fillpipe.fillPolygon(this, paramArrayOfInt1, paramArrayOfInt2, paramInt);
      }
      catch (InvalidPipeException localInvalidPipeException2)
      {
      }
    }
  }

  public void drawRect(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    try
    {
      this.drawpipe.drawRect(this, paramInt1, paramInt2, paramInt3, paramInt4);
    }
    catch (InvalidPipeException localInvalidPipeException1)
    {
      revalidateAll();
      try
      {
        this.drawpipe.drawRect(this, paramInt1, paramInt2, paramInt3, paramInt4);
      }
      catch (InvalidPipeException localInvalidPipeException2)
      {
      }
    }
  }

  public void fillRect(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    try
    {
      this.fillpipe.fillRect(this, paramInt1, paramInt2, paramInt3, paramInt4);
    }
    catch (InvalidPipeException localInvalidPipeException1)
    {
      revalidateAll();
      try
      {
        this.fillpipe.fillRect(this, paramInt1, paramInt2, paramInt3, paramInt4);
      }
      catch (InvalidPipeException localInvalidPipeException2)
      {
      }
    }
  }

  private void revalidateAll()
  {
    try
    {
      this.surfaceData = this.surfaceData.getReplacement();
      if (this.surfaceData == null)
        this.surfaceData = NullSurfaceData.theInstance;
      setDevClip(this.surfaceData.getBounds());
      if (this.paintState <= 1)
        validateColor();
      if (this.composite instanceof XORComposite)
      {
        Color localColor = ((XORComposite)this.composite).getXorColor();
        setComposite(new XORComposite(localColor, this.surfaceData));
      }
      validatePipe();
    }
  }

  public void clearRect(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    Composite localComposite = this.composite;
    Paint localPaint = this.paint;
    setComposite(AlphaComposite.Src);
    setColor(getBackground());
    validatePipe();
    fillRect(paramInt1, paramInt2, paramInt3, paramInt4);
    setPaint(localPaint);
    setComposite(localComposite);
  }

  public void draw(Shape paramShape)
  {
    try
    {
      this.shapepipe.draw(this, paramShape);
    }
    catch (InvalidPipeException localInvalidPipeException1)
    {
      revalidateAll();
      try
      {
        this.shapepipe.draw(this, paramShape);
      }
      catch (InvalidPipeException localInvalidPipeException2)
      {
      }
    }
  }

  public void fill(Shape paramShape)
  {
    try
    {
      this.shapepipe.fill(this, paramShape);
    }
    catch (InvalidPipeException localInvalidPipeException1)
    {
      revalidateAll();
      try
      {
        this.shapepipe.fill(this, paramShape);
      }
      catch (InvalidPipeException localInvalidPipeException2)
      {
      }
    }
  }

  private static boolean isIntegerTranslation(AffineTransform paramAffineTransform)
  {
    if (paramAffineTransform.isIdentity())
      return true;
    if (paramAffineTransform.getType() == 1)
    {
      double d1 = paramAffineTransform.getTranslateX();
      double d2 = paramAffineTransform.getTranslateY();
      return ((d1 == (int)d1) && (d2 == (int)d2));
    }
    return false;
  }

  private static int getTileIndex(int paramInt1, int paramInt2, int paramInt3)
  {
    paramInt1 -= paramInt2;
    if (paramInt1 < 0)
      paramInt1 += 1 - paramInt3;
    return (paramInt1 / paramInt3);
  }

  // ERROR //
  private static Rectangle getImageRegion(RenderedImage paramRenderedImage, Region paramRegion, AffineTransform paramAffineTransform1, AffineTransform paramAffineTransform2, int paramInt1, int paramInt2)
  {
    // Byte code:
    //   0: new 522	java/awt/Rectangle
    //   3: dup
    //   4: aload_0
    //   5: invokeinterface 1360 1 0
    //   10: aload_0
    //   11: invokeinterface 1361 1 0
    //   16: aload_0
    //   17: invokeinterface 1366 1 0
    //   22: aload_0
    //   23: invokeinterface 1359 1 0
    //   28: invokespecial 1160	java/awt/Rectangle:<init>	(IIII)V
    //   31: astore 6
    //   33: aconst_null
    //   34: astore 7
    //   36: bipush 8
    //   38: newarray double
    //   40: astore 8
    //   42: aload 8
    //   44: iconst_0
    //   45: aload 8
    //   47: iconst_2
    //   48: aload_1
    //   49: invokevirtual 1332	sun/java2d/pipe/Region:getLoX	()I
    //   52: i2d
    //   53: dup2_x2
    //   54: dastore
    //   55: dastore
    //   56: aload 8
    //   58: iconst_4
    //   59: aload 8
    //   61: bipush 6
    //   63: aload_1
    //   64: invokevirtual 1330	sun/java2d/pipe/Region:getHiX	()I
    //   67: i2d
    //   68: dup2_x2
    //   69: dastore
    //   70: dastore
    //   71: aload 8
    //   73: iconst_1
    //   74: aload 8
    //   76: iconst_5
    //   77: aload_1
    //   78: invokevirtual 1333	sun/java2d/pipe/Region:getLoY	()I
    //   81: i2d
    //   82: dup2_x2
    //   83: dastore
    //   84: dastore
    //   85: aload 8
    //   87: iconst_3
    //   88: aload 8
    //   90: bipush 7
    //   92: aload_1
    //   93: invokevirtual 1331	sun/java2d/pipe/Region:getHiY	()I
    //   96: i2d
    //   97: dup2_x2
    //   98: dastore
    //   99: dastore
    //   100: aload_2
    //   101: aload 8
    //   103: iconst_0
    //   104: aload 8
    //   106: iconst_0
    //   107: iconst_4
    //   108: invokevirtual 1194	java/awt/geom/AffineTransform:inverseTransform	([DI[DII)V
    //   111: aload_3
    //   112: aload 8
    //   114: iconst_0
    //   115: aload 8
    //   117: iconst_0
    //   118: iconst_4
    //   119: invokevirtual 1194	java/awt/geom/AffineTransform:inverseTransform	([DI[DII)V
    //   122: aload 8
    //   124: iconst_0
    //   125: daload
    //   126: dup2
    //   127: dstore 11
    //   129: dstore 9
    //   131: aload 8
    //   133: iconst_1
    //   134: daload
    //   135: dup2
    //   136: dstore 15
    //   138: dstore 13
    //   140: iconst_2
    //   141: istore 17
    //   143: iload 17
    //   145: bipush 8
    //   147: if_icmpge +80 -> 227
    //   150: aload 8
    //   152: iload 17
    //   154: iinc 17 1
    //   157: daload
    //   158: dstore 18
    //   160: dload 18
    //   162: dload 9
    //   164: dcmpg
    //   165: ifge +10 -> 175
    //   168: dload 18
    //   170: dstore 9
    //   172: goto +15 -> 187
    //   175: dload 18
    //   177: dload 11
    //   179: dcmpl
    //   180: ifle +7 -> 187
    //   183: dload 18
    //   185: dstore 11
    //   187: aload 8
    //   189: iload 17
    //   191: iinc 17 1
    //   194: daload
    //   195: dstore 18
    //   197: dload 18
    //   199: dload 13
    //   201: dcmpg
    //   202: ifge +10 -> 212
    //   205: dload 18
    //   207: dstore 13
    //   209: goto +15 -> 224
    //   212: dload 18
    //   214: dload 15
    //   216: dcmpl
    //   217: ifle +7 -> 224
    //   220: dload 18
    //   222: dstore 15
    //   224: goto -81 -> 143
    //   227: dload 9
    //   229: d2i
    //   230: iload 4
    //   232: isub
    //   233: istore 17
    //   235: dload 11
    //   237: dload 9
    //   239: dsub
    //   240: iconst_2
    //   241: iload 4
    //   243: imul
    //   244: i2d
    //   245: dadd
    //   246: d2i
    //   247: istore 18
    //   249: dload 13
    //   251: d2i
    //   252: iload 5
    //   254: isub
    //   255: istore 19
    //   257: dload 15
    //   259: dload 13
    //   261: dsub
    //   262: iconst_2
    //   263: iload 5
    //   265: imul
    //   266: i2d
    //   267: dadd
    //   268: d2i
    //   269: istore 20
    //   271: new 522	java/awt/Rectangle
    //   274: dup
    //   275: iload 17
    //   277: iload 19
    //   279: iload 18
    //   281: iload 20
    //   283: invokespecial 1160	java/awt/Rectangle:<init>	(IIII)V
    //   286: astore 21
    //   288: aload 21
    //   290: aload 6
    //   292: invokevirtual 1163	java/awt/Rectangle:intersection	(Ljava/awt/Rectangle;)Ljava/awt/Rectangle;
    //   295: astore 7
    //   297: goto +9 -> 306
    //   300: astore 8
    //   302: aload 6
    //   304: astore 7
    //   306: aload 7
    //   308: areturn
    //
    // Exception table:
    //   from	to	target	type
    //   36	297	300	java/awt/geom/NoninvertibleTransformException
  }

  public void drawRenderedImage(RenderedImage paramRenderedImage, AffineTransform paramAffineTransform)
  {
    if (paramRenderedImage == null)
      return;
    if (paramRenderedImage instanceof BufferedImage)
    {
      BufferedImage localBufferedImage1 = (BufferedImage)paramRenderedImage;
      drawImage(localBufferedImage1, paramAffineTransform, null);
      return;
    }
    int i = ((this.transformState <= 1) && (isIntegerTranslation(paramAffineTransform))) ? 1 : 0;
    int j = (i != 0) ? 0 : 3;
    Rectangle localRectangle = getImageRegion(paramRenderedImage, getCompClip(), this.transform, paramAffineTransform, j, j);
    if ((localRectangle.width <= 0) || (localRectangle.height <= 0))
      return;
    if (i != 0)
    {
      drawTranslatedRenderedImage(paramRenderedImage, localRectangle, (int)paramAffineTransform.getTranslateX(), (int)paramAffineTransform.getTranslateY());
      return;
    }
    Raster localRaster = paramRenderedImage.getData(localRectangle);
    WritableRaster localWritableRaster = Raster.createWritableRaster(localRaster.getSampleModel(), localRaster.getDataBuffer(), null);
    int k = localRaster.getMinX();
    int l = localRaster.getMinY();
    int i1 = localRaster.getWidth();
    int i2 = localRaster.getHeight();
    int i3 = k - localRaster.getSampleModelTranslateX();
    int i4 = l - localRaster.getSampleModelTranslateY();
    if ((i3 != 0) || (i4 != 0) || (i1 != localWritableRaster.getWidth()) || (i2 != localWritableRaster.getHeight()))
      localWritableRaster = localWritableRaster.createWritableChild(i3, i4, i1, i2, 0, 0, null);
    AffineTransform localAffineTransform = (AffineTransform)paramAffineTransform.clone();
    localAffineTransform.translate(k, l);
    ColorModel localColorModel = paramRenderedImage.getColorModel();
    BufferedImage localBufferedImage2 = new BufferedImage(localColorModel, localWritableRaster, localColorModel.isAlphaPremultiplied(), null);
    drawImage(localBufferedImage2, localAffineTransform, null);
  }

  private boolean clipTo(Rectangle paramRectangle1, Rectangle paramRectangle2)
  {
    int i = Math.max(paramRectangle1.x, paramRectangle2.x);
    int j = Math.min(paramRectangle1.x + paramRectangle1.width, paramRectangle2.x + paramRectangle2.width);
    int k = Math.max(paramRectangle1.y, paramRectangle2.y);
    int l = Math.min(paramRectangle1.y + paramRectangle1.height, paramRectangle2.y + paramRectangle2.height);
    if ((j - i < 0) || (l - k < 0))
    {
      paramRectangle1.width = -1;
      paramRectangle1.height = -1;
      return false;
    }
    paramRectangle1.x = i;
    paramRectangle1.y = k;
    paramRectangle1.width = (j - i);
    paramRectangle1.height = (l - k);
    return true;
  }

  private void drawTranslatedRenderedImage(RenderedImage paramRenderedImage, Rectangle paramRectangle, int paramInt1, int paramInt2)
  {
    int i = paramRenderedImage.getTileGridXOffset();
    int j = paramRenderedImage.getTileGridYOffset();
    int k = paramRenderedImage.getTileWidth();
    int l = paramRenderedImage.getTileHeight();
    int i1 = getTileIndex(paramRectangle.x, i, k);
    int i2 = getTileIndex(paramRectangle.y, j, l);
    int i3 = getTileIndex(paramRectangle.x + paramRectangle.width - 1, i, k);
    int i4 = getTileIndex(paramRectangle.y + paramRectangle.height - 1, j, l);
    ColorModel localColorModel = paramRenderedImage.getColorModel();
    Rectangle localRectangle = new Rectangle();
    for (int i5 = i2; i5 <= i4; ++i5)
      for (int i6 = i1; i6 <= i3; ++i6)
      {
        Raster localRaster = paramRenderedImage.getTile(i6, i5);
        localRectangle.x = (i6 * k + i);
        localRectangle.y = (i5 * l + j);
        localRectangle.width = k;
        localRectangle.height = l;
        clipTo(localRectangle, paramRectangle);
        WritableRaster localWritableRaster = null;
        if (localRaster instanceof WritableRaster)
          localWritableRaster = (WritableRaster)localRaster;
        else
          localWritableRaster = Raster.createWritableRaster(localRaster.getSampleModel(), localRaster.getDataBuffer(), null);
        localWritableRaster = localWritableRaster.createWritableChild(localRectangle.x, localRectangle.y, localRectangle.width, localRectangle.height, 0, 0, null);
        BufferedImage localBufferedImage = new BufferedImage(localColorModel, localWritableRaster, localColorModel.isAlphaPremultiplied(), null);
        copyImage(localBufferedImage, localRectangle.x + paramInt1, localRectangle.y + paramInt2, 0, 0, localRectangle.width, localRectangle.height, null, null);
      }
  }

  public void drawRenderableImage(RenderableImage paramRenderableImage, AffineTransform paramAffineTransform)
  {
    AffineTransform localAffineTransform3;
    if (paramRenderableImage == null)
      return;
    AffineTransform localAffineTransform1 = this.transform;
    AffineTransform localAffineTransform2 = new AffineTransform(paramAffineTransform);
    localAffineTransform2.concatenate(localAffineTransform1);
    RenderContext localRenderContext = new RenderContext(localAffineTransform2);
    try
    {
      localAffineTransform3 = localAffineTransform1.createInverse();
    }
    catch (NoninvertibleTransformException localNoninvertibleTransformException)
    {
      localRenderContext = new RenderContext(localAffineTransform1);
      localAffineTransform3 = new AffineTransform();
    }
    RenderedImage localRenderedImage = paramRenderableImage.createRendering(localRenderContext);
    drawRenderedImage(localRenderedImage, localAffineTransform3);
  }

  protected Rectangle transformBounds(Rectangle paramRectangle, AffineTransform paramAffineTransform)
  {
    if (paramAffineTransform.isIdentity())
      return paramRectangle;
    Shape localShape = transformShape(paramAffineTransform, paramRectangle);
    return localShape.getBounds();
  }

  public void drawString(String paramString, int paramInt1, int paramInt2)
  {
    if (paramString == null)
      throw new NullPointerException("String is null");
    if (this.font.hasLayoutAttributes())
    {
      new TextLayout(paramString, this.font, getFontRenderContext()).draw(this, paramInt1, paramInt2);
      return;
    }
    try
    {
      this.textpipe.drawString(this, paramString, paramInt1, paramInt2);
    }
    catch (InvalidPipeException localInvalidPipeException1)
    {
      revalidateAll();
      try
      {
        this.textpipe.drawString(this, paramString, paramInt1, paramInt2);
      }
      catch (InvalidPipeException localInvalidPipeException2)
      {
      }
    }
  }

  public void drawString(String paramString, float paramFloat1, float paramFloat2)
  {
    if (paramString == null)
      throw new NullPointerException("String is null");
    if (this.font.hasLayoutAttributes())
    {
      new TextLayout(paramString, this.font, getFontRenderContext()).draw(this, paramFloat1, paramFloat2);
      return;
    }
    try
    {
      this.textpipe.drawString(this, paramString, paramFloat1, paramFloat2);
    }
    catch (InvalidPipeException localInvalidPipeException1)
    {
      revalidateAll();
      try
      {
        this.textpipe.drawString(this, paramString, paramFloat1, paramFloat2);
      }
      catch (InvalidPipeException localInvalidPipeException2)
      {
      }
    }
  }

  public void drawString(AttributedCharacterIterator paramAttributedCharacterIterator, int paramInt1, int paramInt2)
  {
    if (paramAttributedCharacterIterator == null)
      throw new NullPointerException("AttributedCharacterIterator is null");
    TextLayout localTextLayout = new TextLayout(paramAttributedCharacterIterator, getFontRenderContext());
    localTextLayout.draw(this, paramInt1, paramInt2);
  }

  public void drawString(AttributedCharacterIterator paramAttributedCharacterIterator, float paramFloat1, float paramFloat2)
  {
    if (paramAttributedCharacterIterator == null)
      throw new NullPointerException("AttributedCharacterIterator is null");
    TextLayout localTextLayout = new TextLayout(paramAttributedCharacterIterator, getFontRenderContext());
    localTextLayout.draw(this, paramFloat1, paramFloat2);
  }

  public void drawGlyphVector(GlyphVector paramGlyphVector, float paramFloat1, float paramFloat2)
  {
    if (paramGlyphVector == null)
      throw new NullPointerException("GlyphVector is null");
    try
    {
      this.textpipe.drawGlyphVector(this, paramGlyphVector, paramFloat1, paramFloat2);
    }
    catch (InvalidPipeException localInvalidPipeException1)
    {
      revalidateAll();
      try
      {
        this.textpipe.drawGlyphVector(this, paramGlyphVector, paramFloat1, paramFloat2);
      }
      catch (InvalidPipeException localInvalidPipeException2)
      {
      }
    }
  }

  public void drawChars(char[] paramArrayOfChar, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    if (paramArrayOfChar == null)
      throw new NullPointerException("char data is null");
    if ((paramInt1 < 0) || (paramInt2 < 0) || (paramInt1 + paramInt2 > paramArrayOfChar.length))
      throw new ArrayIndexOutOfBoundsException("bad offset/length");
    if (this.font.hasLayoutAttributes())
    {
      new TextLayout(new String(paramArrayOfChar, paramInt1, paramInt2), this.font, getFontRenderContext()).draw(this, paramInt3, paramInt4);
      return;
    }
    try
    {
      this.textpipe.drawChars(this, paramArrayOfChar, paramInt1, paramInt2, paramInt3, paramInt4);
    }
    catch (InvalidPipeException localInvalidPipeException1)
    {
      revalidateAll();
      try
      {
        this.textpipe.drawChars(this, paramArrayOfChar, paramInt1, paramInt2, paramInt3, paramInt4);
      }
      catch (InvalidPipeException localInvalidPipeException2)
      {
      }
    }
  }

  public void drawBytes(byte[] paramArrayOfByte, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    if (paramArrayOfByte == null)
      throw new NullPointerException("byte data is null");
    if ((paramInt1 < 0) || (paramInt2 < 0) || (paramInt1 + paramInt2 > paramArrayOfByte.length))
      throw new ArrayIndexOutOfBoundsException("bad offset/length");
    char[] arrayOfChar = new char[paramInt2];
    int i = paramInt2;
    while (i-- > 0)
      arrayOfChar[i] = (char)(paramArrayOfByte[(i + paramInt1)] & 0xFF);
    if (this.font.hasLayoutAttributes())
    {
      new TextLayout(new String(arrayOfChar), this.font, getFontRenderContext()).draw(this, paramInt3, paramInt4);
      return;
    }
    try
    {
      this.textpipe.drawChars(this, arrayOfChar, 0, paramInt2, paramInt3, paramInt4);
    }
    catch (InvalidPipeException localInvalidPipeException1)
    {
      revalidateAll();
      try
      {
        this.textpipe.drawChars(this, arrayOfChar, 0, paramInt2, paramInt3, paramInt4);
      }
      catch (InvalidPipeException localInvalidPipeException2)
      {
      }
    }
  }

  public boolean drawImage(Image paramImage, int paramInt1, int paramInt2, int paramInt3, int paramInt4, ImageObserver paramImageObserver)
  {
    return drawImage(paramImage, paramInt1, paramInt2, paramInt3, paramInt4, null, paramImageObserver);
  }

  public boolean copyImage(Image paramImage, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6, Color paramColor, ImageObserver paramImageObserver)
  {
    try
    {
      return this.imagepipe.copyImage(this, paramImage, paramInt1, paramInt2, paramInt3, paramInt4, paramInt5, paramInt6, paramColor, paramImageObserver);
    }
    catch (InvalidPipeException localInvalidPipeException1)
    {
      revalidateAll();
      try
      {
        return this.imagepipe.copyImage(this, paramImage, paramInt1, paramInt2, paramInt3, paramInt4, paramInt5, paramInt6, paramColor, paramImageObserver);
      }
      catch (InvalidPipeException localInvalidPipeException2)
      {
      }
    }
    return false;
  }

  public boolean drawImage(Image paramImage, int paramInt1, int paramInt2, int paramInt3, int paramInt4, Color paramColor, ImageObserver paramImageObserver)
  {
    if (paramImage == null)
      return true;
    if ((paramInt3 == 0) || (paramInt4 == 0))
      return true;
    if ((paramInt3 == paramImage.getWidth(null)) && (paramInt4 == paramImage.getHeight(null)))
      return copyImage(paramImage, paramInt1, paramInt2, 0, 0, paramInt3, paramInt4, paramColor, paramImageObserver);
    try
    {
      return this.imagepipe.scaleImage(this, paramImage, paramInt1, paramInt2, paramInt3, paramInt4, paramColor, paramImageObserver);
    }
    catch (InvalidPipeException localInvalidPipeException1)
    {
      revalidateAll();
      try
      {
        return this.imagepipe.scaleImage(this, paramImage, paramInt1, paramInt2, paramInt3, paramInt4, paramColor, paramImageObserver);
      }
      catch (InvalidPipeException localInvalidPipeException2)
      {
      }
    }
    return false;
  }

  public boolean drawImage(Image paramImage, int paramInt1, int paramInt2, ImageObserver paramImageObserver)
  {
    return drawImage(paramImage, paramInt1, paramInt2, null, paramImageObserver);
  }

  public boolean drawImage(Image paramImage, int paramInt1, int paramInt2, Color paramColor, ImageObserver paramImageObserver)
  {
    if (paramImage == null)
      return true;
    try
    {
      return this.imagepipe.copyImage(this, paramImage, paramInt1, paramInt2, paramColor, paramImageObserver);
    }
    catch (InvalidPipeException localInvalidPipeException1)
    {
      revalidateAll();
      try
      {
        return this.imagepipe.copyImage(this, paramImage, paramInt1, paramInt2, paramColor, paramImageObserver);
      }
      catch (InvalidPipeException localInvalidPipeException2)
      {
      }
    }
    return false;
  }

  public boolean drawImage(Image paramImage, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6, int paramInt7, int paramInt8, ImageObserver paramImageObserver)
  {
    return drawImage(paramImage, paramInt1, paramInt2, paramInt3, paramInt4, paramInt5, paramInt6, paramInt7, paramInt8, null, paramImageObserver);
  }

  public boolean drawImage(Image paramImage, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6, int paramInt7, int paramInt8, Color paramColor, ImageObserver paramImageObserver)
  {
    if (paramImage == null)
      return true;
    if ((paramInt1 == paramInt3) || (paramInt2 == paramInt4) || (paramInt5 == paramInt7) || (paramInt6 == paramInt8))
      return true;
    if ((paramInt7 - paramInt5 == paramInt3 - paramInt1) && (paramInt8 - paramInt6 == paramInt4 - paramInt2))
    {
      int i;
      int j;
      int k;
      int l;
      int i1;
      int i2;
      if (paramInt7 > paramInt5)
      {
        i1 = paramInt7 - paramInt5;
        i = paramInt5;
        k = paramInt1;
      }
      else
      {
        i1 = paramInt5 - paramInt7;
        i = paramInt7;
        k = paramInt3;
      }
      if (paramInt8 > paramInt6)
      {
        i2 = paramInt8 - paramInt6;
        j = paramInt6;
        l = paramInt2;
      }
      else
      {
        i2 = paramInt6 - paramInt8;
        j = paramInt8;
        l = paramInt4;
      }
      return copyImage(paramImage, k, l, i, j, i1, i2, paramColor, paramImageObserver);
    }
    try
    {
      return this.imagepipe.scaleImage(this, paramImage, paramInt1, paramInt2, paramInt3, paramInt4, paramInt5, paramInt6, paramInt7, paramInt8, paramColor, paramImageObserver);
    }
    catch (InvalidPipeException localInvalidPipeException1)
    {
      revalidateAll();
      try
      {
        return this.imagepipe.scaleImage(this, paramImage, paramInt1, paramInt2, paramInt3, paramInt4, paramInt5, paramInt6, paramInt7, paramInt8, paramColor, paramImageObserver);
      }
      catch (InvalidPipeException localInvalidPipeException2)
      {
      }
    }
    return false;
  }

  public boolean drawImage(Image paramImage, AffineTransform paramAffineTransform, ImageObserver paramImageObserver)
  {
    if (paramImage == null)
      return true;
    if ((paramAffineTransform == null) || (paramAffineTransform.isIdentity()))
      return drawImage(paramImage, 0, 0, null, paramImageObserver);
    try
    {
      return this.imagepipe.transformImage(this, paramImage, paramAffineTransform, paramImageObserver);
    }
    catch (InvalidPipeException localInvalidPipeException1)
    {
      revalidateAll();
      try
      {
        return this.imagepipe.transformImage(this, paramImage, paramAffineTransform, paramImageObserver);
      }
      catch (InvalidPipeException localInvalidPipeException2)
      {
      }
    }
    return false;
  }

  public void drawImage(BufferedImage paramBufferedImage, BufferedImageOp paramBufferedImageOp, int paramInt1, int paramInt2)
  {
    if (paramBufferedImage == null)
      return;
    try
    {
      this.imagepipe.transformImage(this, paramBufferedImage, paramBufferedImageOp, paramInt1, paramInt2);
    }
    catch (InvalidPipeException localInvalidPipeException1)
    {
      revalidateAll();
      try
      {
        this.imagepipe.transformImage(this, paramBufferedImage, paramBufferedImageOp, paramInt1, paramInt2);
      }
      catch (InvalidPipeException localInvalidPipeException2)
      {
      }
    }
  }

  public FontRenderContext getFontRenderContext()
  {
    if (this.cachedFRC == null)
    {
      int i = this.textAntialiasHint;
      if ((i == 0) && (this.antialiasHint == 2))
        i = 2;
      AffineTransform localAffineTransform = null;
      if (this.transformState >= 3)
        if ((this.transform.getTranslateX() == 0D) && (this.transform.getTranslateY() == 0D))
          localAffineTransform = this.transform;
        else
          localAffineTransform = new AffineTransform(this.transform.getScaleX(), this.transform.getShearY(), this.transform.getShearX(), this.transform.getScaleY(), 0D, 0D);
      this.cachedFRC = new FontRenderContext(localAffineTransform, SunHints.Value.get(2, i), SunHints.Value.get(3, this.fractionalMetricsHint));
    }
    return this.cachedFRC;
  }

  public void dispose()
  {
    this.surfaceData = NullSurfaceData.theInstance;
    invalidatePipe();
  }

  public void finalize()
  {
  }

  public Object getDestination()
  {
    return this.surfaceData.getDestination();
  }

  public Surface getDestSurface()
  {
    return this.surfaceData;
  }

  static
  {
    if (PerformanceLogger.loggingEnabled())
      PerformanceLogger.setTime("SunGraphics2D static initialization");
    invalidpipe = new ValidatePipe();
    IDENT_MATRIX = { 1D, 0D, 0D, 1D };
    IDENT_ATX = new AffineTransform();
    textTxArr = new double[17][];
    textAtArr = new AffineTransform[17];
    for (int i = 8; i < 17; ++i)
    {
      textTxArr[i] = { i, 0D, 0D, i };
      textAtArr[i] = new AffineTransform(textTxArr[i]);
    }
  }
}