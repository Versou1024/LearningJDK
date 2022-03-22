package sun.print;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D.Float;
import java.awt.geom.Ellipse2D.Float;
import java.awt.geom.Line2D.Float;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Float;
import java.awt.geom.RoundRectangle2D.Float;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.ImageObserver;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.VolatileImage;
import java.awt.image.WritableRaster;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.lang.ref.SoftReference;
import java.text.AttributedCharacterIterator;
import java.util.Hashtable;
import java.util.Map;
import sun.awt.image.RemoteOffScreenImage;
import sun.awt.image.ToolkitImage;
import sun.font.CompositeFont;
import sun.font.Font2D;
import sun.font.Font2DHandle;
import sun.font.FontManager;
import sun.font.PhysicalFont;

public abstract class PathGraphics extends ProxyGraphics2D
{
  private Printable mPainter;
  private PageFormat mPageFormat;
  private int mPageIndex;
  private boolean mCanRedraw;
  protected boolean printingGlyphVector;
  protected static SoftReference<Hashtable<Font2DHandle, Object>> fontMapRef;

  protected PathGraphics(Graphics2D paramGraphics2D, PrinterJob paramPrinterJob, Printable paramPrintable, PageFormat paramPageFormat, int paramInt, boolean paramBoolean)
  {
    super(paramGraphics2D, paramPrinterJob);
    this.mPainter = paramPrintable;
    this.mPageFormat = paramPageFormat;
    this.mPageIndex = paramInt;
    this.mCanRedraw = paramBoolean;
  }

  protected Printable getPrintable()
  {
    return this.mPainter;
  }

  protected PageFormat getPageFormat()
  {
    return this.mPageFormat;
  }

  protected int getPageIndex()
  {
    return this.mPageIndex;
  }

  public boolean canDoRedraws()
  {
    return this.mCanRedraw;
  }

  public abstract void redrawRegion(Rectangle2D paramRectangle2D, double paramDouble1, double paramDouble2, Shape paramShape, AffineTransform paramAffineTransform)
    throws PrinterException;

  public void drawLine(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    Paint localPaint = getPaint();
    try
    {
      AffineTransform localAffineTransform = getTransform();
      if (getClip() != null)
        deviceClip(getClip().getPathIterator(localAffineTransform));
      deviceDrawLine(paramInt1, paramInt2, paramInt3, paramInt4, (Color)localPaint);
    }
    catch (ClassCastException localClassCastException)
    {
      throw new IllegalArgumentException("Expected a Color instance");
    }
  }

  public void drawRect(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    Paint localPaint = getPaint();
    try
    {
      AffineTransform localAffineTransform = getTransform();
      if (getClip() != null)
        deviceClip(getClip().getPathIterator(localAffineTransform));
      deviceFrameRect(paramInt1, paramInt2, paramInt3, paramInt4, (Color)localPaint);
    }
    catch (ClassCastException localClassCastException)
    {
      throw new IllegalArgumentException("Expected a Color instance");
    }
  }

  public void fillRect(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    Paint localPaint = getPaint();
    try
    {
      AffineTransform localAffineTransform = getTransform();
      if (getClip() != null)
        deviceClip(getClip().getPathIterator(localAffineTransform));
      deviceFillRect(paramInt1, paramInt2, paramInt3, paramInt4, (Color)localPaint);
    }
    catch (ClassCastException localClassCastException)
    {
      throw new IllegalArgumentException("Expected a Color instance");
    }
  }

  public void clearRect(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    fill(new Rectangle2D.Float(paramInt1, paramInt2, paramInt3, paramInt4), getBackground());
  }

  public void drawRoundRect(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6)
  {
    draw(new RoundRectangle2D.Float(paramInt1, paramInt2, paramInt3, paramInt4, paramInt5, paramInt6));
  }

  public void fillRoundRect(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6)
  {
    fill(new RoundRectangle2D.Float(paramInt1, paramInt2, paramInt3, paramInt4, paramInt5, paramInt6));
  }

  public void drawOval(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    draw(new Ellipse2D.Float(paramInt1, paramInt2, paramInt3, paramInt4));
  }

  public void fillOval(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    fill(new Ellipse2D.Float(paramInt1, paramInt2, paramInt3, paramInt4));
  }

  public void drawArc(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6)
  {
    draw(new Arc2D.Float(paramInt1, paramInt2, paramInt3, paramInt4, paramInt5, paramInt6, 0));
  }

  public void fillArc(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6)
  {
    fill(new Arc2D.Float(paramInt1, paramInt2, paramInt3, paramInt4, paramInt5, paramInt6, 2));
  }

  public void drawPolyline(int[] paramArrayOfInt1, int[] paramArrayOfInt2, int paramInt)
  {
    if (paramInt > 0)
    {
      float f1 = paramArrayOfInt1[0];
      float f2 = paramArrayOfInt2[0];
      for (int i = 1; i < paramInt; ++i)
      {
        float f3 = paramArrayOfInt1[i];
        float f4 = paramArrayOfInt2[i];
        draw(new Line2D.Float(f1, f2, f3, f4));
        f1 = f3;
        f2 = f4;
      }
    }
  }

  public void drawPolygon(int[] paramArrayOfInt1, int[] paramArrayOfInt2, int paramInt)
  {
    draw(new Polygon(paramArrayOfInt1, paramArrayOfInt2, paramInt));
  }

  public void drawPolygon(Polygon paramPolygon)
  {
    draw(paramPolygon);
  }

  public void fillPolygon(int[] paramArrayOfInt1, int[] paramArrayOfInt2, int paramInt)
  {
    fill(new Polygon(paramArrayOfInt1, paramArrayOfInt2, paramInt));
  }

  public void fillPolygon(Polygon paramPolygon)
  {
    fill(paramPolygon);
  }

  public void drawString(String paramString, int paramInt1, int paramInt2)
  {
    drawString(paramString, paramInt1, paramInt2);
  }

  public void drawString(String paramString, float paramFloat1, float paramFloat2)
  {
    if (paramString.length() == 0)
      return;
    TextLayout localTextLayout = new TextLayout(paramString, getFont(), getFontRenderContext());
    localTextLayout.draw(this, paramFloat1, paramFloat2);
  }

  protected void drawString(String paramString, float paramFloat1, float paramFloat2, Font paramFont, FontRenderContext paramFontRenderContext, float paramFloat3)
  {
    TextLayout localTextLayout = new TextLayout(paramString, paramFont, paramFontRenderContext);
    Shape localShape = localTextLayout.getOutline(AffineTransform.getTranslateInstance(paramFloat1, paramFloat2));
    fill(localShape);
  }

  public void drawString(AttributedCharacterIterator paramAttributedCharacterIterator, int paramInt1, int paramInt2)
  {
    drawString(paramAttributedCharacterIterator, paramInt1, paramInt2);
  }

  public void drawString(AttributedCharacterIterator paramAttributedCharacterIterator, float paramFloat1, float paramFloat2)
  {
    if (paramAttributedCharacterIterator == null)
      throw new NullPointerException("attributedcharacteriterator is null");
    TextLayout localTextLayout = new TextLayout(paramAttributedCharacterIterator, getFontRenderContext());
    localTextLayout.draw(this, paramFloat1, paramFloat2);
  }

  public void drawGlyphVector(GlyphVector paramGlyphVector, float paramFloat1, float paramFloat2)
  {
    if (this.printingGlyphVector)
    {
      if ((!($assertionsDisabled)) && (this.printingGlyphVector))
        throw new AssertionError();
      fill(paramGlyphVector.getOutline(paramFloat1, paramFloat2));
      return;
    }
    try
    {
      this.printingGlyphVector = true;
      if ((RasterPrinterJob.shapeTextProp) || (!(printedSimpleGlyphVector(paramGlyphVector, paramFloat1, paramFloat2))))
        fill(paramGlyphVector.getOutline(paramFloat1, paramFloat2));
    }
    finally
    {
      this.printingGlyphVector = false;
    }
  }

  protected int platformFontCount(Font paramFont, String paramString)
  {
    return 0;
  }

  boolean printedSimpleGlyphVector(GlyphVector paramGlyphVector, float paramFloat1, float paramFloat2)
  {
    Hashtable localHashtable;
    int k;
    int l;
    int i = paramGlyphVector.getLayoutFlags();
    if ((i != 0) && (i != 2))
      return false;
    Font localFont = paramGlyphVector.getFont();
    Font2D localFont2D = FontManager.getFont2D(localFont);
    if (localFont2D.handle.font2D != localFont2D)
      return false;
    synchronized (PathGraphics.class)
    {
      localHashtable = (Hashtable)fontMapRef.get();
      if (localHashtable == null)
      {
        localHashtable = new Hashtable();
        fontMapRef = new SoftReference(localHashtable);
      }
    }
    int j = paramGlyphVector.getNumGlyphs();
    int[] arrayOfInt1 = paramGlyphVector.getGlyphCodes(0, j, null);
    char[] arrayOfChar1 = null;
    Object localObject2 = (char[][])null;
    CompositeFont localCompositeFont = null;
    synchronized (localHashtable)
    {
      if (!(localFont2D instanceof CompositeFont))
        break label299;
      localCompositeFont = (CompositeFont)localFont2D;
      k = localCompositeFont.getNumSlots();
      localObject2 = (char[][])(char[][])localHashtable.get(localFont2D.handle);
      if (localObject2 == null)
      {
        localObject2 = new char[k][];
        localHashtable.put(localFont2D.handle, localObject2);
      }
      for (l = 0; l < j; ++l)
      {
        int i1 = arrayOfInt1[l] >>> 24;
        if (i1 >= k)
          return false;
        if (localObject2[i1] == null)
        {
          PhysicalFont localPhysicalFont = localCompositeFont.getSlotFont(i1);
          char[] arrayOfChar2 = (char[])(char[])localHashtable.get(localPhysicalFont.handle);
          if (arrayOfChar2 == null)
            arrayOfChar2 = getGlyphToCharMapForFont(localPhysicalFont);
          localObject2[i1] = arrayOfChar2;
        }
      }
      break label342:
      label299: arrayOfChar1 = (char[])(char[])localHashtable.get(localFont2D.handle);
      if (arrayOfChar1 != null)
        break label342;
      arrayOfChar1 = getGlyphToCharMapForFont(localFont2D);
      label342: localHashtable.put(localFont2D.handle, arrayOfChar1);
    }
    ??? = new char[j];
    if (localCompositeFont != null)
      for (k = 0; k < j; ++k)
      {
        l = arrayOfInt1[k];
        Object localObject4 = localObject2[(l >>> 24)];
        l &= 16777215;
        if (localObject4 == null)
          return false;
        if (l == 65535)
        {
          i3 = 10;
        }
        else
        {
          if ((l < 0) || (l >= localObject4.length))
            return false;
          i3 = localObject4[l];
        }
        if (i3 != 65535)
          ???[k] = i3;
        else
          return false;
      }
    else
      for (k = 0; k < j; ++k)
      {
        int i2;
        l = arrayOfInt1[k];
        if (l == 65535)
        {
          i2 = 10;
        }
        else
        {
          if ((l < 0) || (l >= arrayOfChar1.length))
            return false;
          i2 = arrayOfChar1[l];
        }
        if (i2 != 65535)
          ???[k] = i2;
        else
          return false;
      }
    FontRenderContext localFontRenderContext1 = paramGlyphVector.getFontRenderContext();
    GlyphVector localGlyphVector = localFont.createGlyphVector(localFontRenderContext1, ???);
    if (localGlyphVector.getNumGlyphs() != j)
      return false;
    int[] arrayOfInt2 = localGlyphVector.getGlyphCodes(0, j, null);
    for (int i3 = 0; i3 < j; ++i3)
      if (arrayOfInt1[i3] != arrayOfInt2[i3])
        return false;
    FontRenderContext localFontRenderContext2 = getFontRenderContext();
    boolean bool = localFontRenderContext1.equals(localFontRenderContext2);
    if ((!(bool)) && (localFontRenderContext1.usesFractionalMetrics() == localFontRenderContext2.usesFractionalMetrics()))
    {
      localObject6 = localFontRenderContext1.getTransform();
      AffineTransform localAffineTransform = getTransform();
      localObject7 = new double[4];
      double[] arrayOfDouble = new double[4];
      ((AffineTransform)localObject6).getMatrix(localObject7);
      localAffineTransform.getMatrix(arrayOfDouble);
      bool = true;
      for (int i6 = 0; i6 < 4; ++i6)
        if (localObject7[i6] != arrayOfDouble[i6])
        {
          bool = false;
          break;
        }
    }
    Object localObject6 = new String(???, 0, j);
    int i4 = platformFontCount(localFont, (String)localObject6);
    if (i4 == 0)
      return false;
    Object localObject7 = paramGlyphVector.getGlyphPositions(0, j, null);
    int i5 = (((i & 0x2) == 0) || (samePositions(localGlyphVector, arrayOfInt2, arrayOfInt1, localObject7))) ? 1 : 0;
    Point2D localPoint2D = paramGlyphVector.getGlyphPosition(j);
    float f1 = (float)localPoint2D.getX();
    int i7 = 0;
    if ((localFont.hasLayoutAttributes()) && (this.printingGlyphVector) && (i5 != 0))
    {
      Map localMap = localFont.getAttributes();
      Object localObject8 = localMap.get(TextAttribute.TRACKING);
      int i10 = ((localObject8 != null) && (localObject8 instanceof java.lang.Number) && (((java.lang.Number)localObject8).floatValue() != 0F)) ? 1 : 0;
      if (i10 != 0)
      {
        i5 = 0;
      }
      else
      {
        Rectangle2D localRectangle2D = localFont.getStringBounds((String)localObject6, localFontRenderContext1);
        float f2 = (float)localRectangle2D.getWidth();
        if (Math.abs(f2 - f1) > 0.000010000000000000001D)
          i7 = 1;
      }
    }
    if ((bool) && (i5 != 0) && (i7 == 0))
    {
      drawString((String)localObject6, paramFloat1, paramFloat2, localFont, localFontRenderContext1, 0F);
      return true;
    }
    if ((i4 == 1) && (canDrawStringToWidth()) && (i5 != 0))
    {
      drawString((String)localObject6, paramFloat1, paramFloat2, localFont, localFontRenderContext1, f1);
      return true;
    }
    for (int i8 = 0; i8 < ???.length; ++i8)
    {
      int i9 = ???[i8];
      if (((i9 >= 1424) && (i9 <= 4255)) || ((i9 >= 6016) && (i9 <= 6143)))
        return false;
    }
    for (i8 = 0; i8 < j; ++i8)
    {
      String str = new String(???, i8, 1);
      drawString(str, paramFloat1 + localObject7[(i8 * 2)], paramFloat2 + localObject7[(i8 * 2 + 1)], localFont, localFontRenderContext1, 0F);
    }
    return true;
  }

  private boolean samePositions(GlyphVector paramGlyphVector, int[] paramArrayOfInt1, int[] paramArrayOfInt2, float[] paramArrayOfFloat)
  {
    int i = paramGlyphVector.getNumGlyphs();
    float[] arrayOfFloat = paramGlyphVector.getGlyphPositions(0, i, null);
    if ((i != paramArrayOfInt1.length) || (paramArrayOfInt2.length != paramArrayOfInt1.length) || (paramArrayOfFloat.length != arrayOfFloat.length))
      return false;
    for (int j = 0; j < i; ++j)
      if ((paramArrayOfInt1[j] != paramArrayOfInt2[j]) || (arrayOfFloat[j] != paramArrayOfFloat[j]))
        return false;
    return true;
  }

  protected boolean canDrawStringToWidth()
  {
    return false;
  }

  private static char[] getGlyphToCharMapForFont(Font2D paramFont2D)
  {
    int i = paramFont2D.getNumGlyphs();
    int j = paramFont2D.getMissingGlyphCode();
    char[] arrayOfChar = new char[i];
    for (int l = 0; l < i; ++l)
      arrayOfChar[l] = 65535;
    for (l = 0; l < 65535; l = (char)(l + 1))
    {
      if ((l >= 55296) && (l <= 57343))
        break label97:
      int k = paramFont2D.charToGlyph(l);
      label97: if ((k != j) && (k < i) && (arrayOfChar[k] == 65535))
        arrayOfChar[k] = l;
    }
    return arrayOfChar;
  }

  public void draw(Shape paramShape)
  {
    fill(getStroke().createStrokedShape(paramShape));
  }

  public void fill(Shape paramShape)
  {
    Paint localPaint = getPaint();
    try
    {
      fill(paramShape, (Color)localPaint);
    }
    catch (ClassCastException localClassCastException)
    {
      throw new IllegalArgumentException("Expected a Color instance");
    }
  }

  public void fill(Shape paramShape, Color paramColor)
  {
    AffineTransform localAffineTransform = getTransform();
    if (getClip() != null)
      deviceClip(getClip().getPathIterator(localAffineTransform));
    deviceFill(paramShape.getPathIterator(localAffineTransform), paramColor);
  }

  protected abstract void deviceFill(PathIterator paramPathIterator, Color paramColor);

  protected abstract void deviceClip(PathIterator paramPathIterator);

  protected abstract void deviceFrameRect(int paramInt1, int paramInt2, int paramInt3, int paramInt4, Color paramColor);

  protected abstract void deviceDrawLine(int paramInt1, int paramInt2, int paramInt3, int paramInt4, Color paramColor);

  protected abstract void deviceFillRect(int paramInt1, int paramInt2, int paramInt3, int paramInt4, Color paramColor);

  protected BufferedImage getBufferedImage(Image paramImage)
  {
    if (paramImage instanceof RemoteOffScreenImage)
      return ((RemoteOffScreenImage)paramImage).getSnapshot();
    if (paramImage instanceof BufferedImage)
      return ((BufferedImage)paramImage);
    if (paramImage instanceof ToolkitImage)
      return ((ToolkitImage)paramImage).getBufferedImage();
    if (paramImage instanceof VolatileImage)
      return ((VolatileImage)paramImage).getSnapshot();
    return null;
  }

  protected boolean hasTransparentPixels(BufferedImage paramBufferedImage)
  {
    ColorModel localColorModel = paramBufferedImage.getColorModel();
    int i = (localColorModel.getTransparency() != 1) ? 1 : (localColorModel == null) ? 1 : 0;
    if ((i != 0) && (paramBufferedImage != null) && (((paramBufferedImage.getType() == 2) || (paramBufferedImage.getType() == 3))))
    {
      DataBuffer localDataBuffer = paramBufferedImage.getRaster().getDataBuffer();
      SampleModel localSampleModel = paramBufferedImage.getRaster().getSampleModel();
      if ((localDataBuffer instanceof DataBufferInt) && (localSampleModel instanceof SinglePixelPackedSampleModel))
      {
        SinglePixelPackedSampleModel localSinglePixelPackedSampleModel = (SinglePixelPackedSampleModel)localSampleModel;
        int[] arrayOfInt = ((DataBufferInt)localDataBuffer).getData();
        int j = paramBufferedImage.getMinX();
        int k = paramBufferedImage.getMinY();
        int l = paramBufferedImage.getWidth();
        int i1 = paramBufferedImage.getHeight();
        int i2 = localSinglePixelPackedSampleModel.getScanlineStride();
        int i3 = 0;
        for (int i4 = k; i4 < k + i1; ++i4)
        {
          int i5 = k * i2;
          for (int i6 = j; i6 < j + l; ++i6)
            if ((arrayOfInt[(i5 + i6)] & 0xFF000000) != -16777216)
            {
              i3 = 1;
              break;
            }
          if (i3 != 0)
            break;
        }
        if (i3 == 0)
          i = 0;
      }
    }
    return i;
  }

  protected boolean isBitmaskTransparency(BufferedImage paramBufferedImage)
  {
    ColorModel localColorModel = paramBufferedImage.getColorModel();
    return ((localColorModel != null) && (localColorModel.getTransparency() == 2));
  }

  protected boolean drawBitmaskImage(BufferedImage paramBufferedImage, AffineTransform paramAffineTransform, Color paramColor, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    int l;
    int i1;
    int i2;
    int i3;
    ColorModel localColorModel = paramBufferedImage.getColorModel();
    if (!(localColorModel instanceof IndexColorModel))
      return false;
    IndexColorModel localIndexColorModel = (IndexColorModel)localColorModel;
    if (localColorModel.getTransparency() != 2)
      return false;
    if ((paramColor != null) && (paramColor.getAlpha() < 128))
      return false;
    if ((paramAffineTransform.getType() & 0xFFFFFFF4) != 0)
      return false;
    if ((getTransform().getType() & 0xFFFFFFF4) != 0)
      return false;
    BufferedImage localBufferedImage = null;
    WritableRaster localWritableRaster = paramBufferedImage.getRaster();
    int i = localIndexColorModel.getTransparentPixel();
    byte[] arrayOfByte = new byte[localIndexColorModel.getMapSize()];
    localIndexColorModel.getAlphas(arrayOfByte);
    if (i >= 0)
      arrayOfByte[i] = 0;
    int j = localWritableRaster.getWidth();
    int k = localWritableRaster.getHeight();
    if ((paramInt1 > j) || (paramInt2 > k))
      return false;
    if (paramInt1 + paramInt3 > j)
    {
      l = j;
      i2 = l - paramInt1;
    }
    else
    {
      l = paramInt1 + paramInt3;
      i2 = paramInt3;
    }
    if (paramInt2 + paramInt4 > k)
    {
      i1 = k;
      i3 = i1 - paramInt2;
    }
    else
    {
      i1 = paramInt2 + paramInt4;
      i3 = paramInt4;
    }
    int[] arrayOfInt = new int[i2];
    for (int i4 = paramInt2; i4 < i1; ++i4)
    {
      int i6;
      int i5 = -1;
      localWritableRaster.getPixels(paramInt1, i4, i2, 1, arrayOfInt);
      for (int i7 = paramInt1; i7 < l; ++i7)
        if (arrayOfByte[arrayOfInt[(i7 - paramInt1)]] == 0)
          if (i5 >= 0)
          {
            localBufferedImage = paramBufferedImage.getSubimage(i5, i4, i7 - i5, 1);
            paramAffineTransform.translate(i5, i4);
            drawImageToPlatform(localBufferedImage, paramAffineTransform, paramColor, 0, 0, i7 - i5, 1, true);
            paramAffineTransform.translate(-i5, -i4);
            i6 = -1;
          }
        else if (i6 < 0)
          i6 = i7;
      if (i6 >= 0)
      {
        localBufferedImage = paramBufferedImage.getSubimage(i6, i4, l - i6, 1);
        paramAffineTransform.translate(i6, i4);
        drawImageToPlatform(localBufferedImage, paramAffineTransform, paramColor, 0, 0, l - i6, 1, true);
        paramAffineTransform.translate(-i6, -i4);
      }
    }
    return true;
  }

  protected abstract boolean drawImageToPlatform(Image paramImage, AffineTransform paramAffineTransform, Color paramColor, int paramInt1, int paramInt2, int paramInt3, int paramInt4, boolean paramBoolean);

  public boolean drawImage(Image paramImage, int paramInt1, int paramInt2, ImageObserver paramImageObserver)
  {
    return drawImage(paramImage, paramInt1, paramInt2, null, paramImageObserver);
  }

  public boolean drawImage(Image paramImage, int paramInt1, int paramInt2, int paramInt3, int paramInt4, ImageObserver paramImageObserver)
  {
    return drawImage(paramImage, paramInt1, paramInt2, paramInt3, paramInt4, null, paramImageObserver);
  }

  public boolean drawImage(Image paramImage, int paramInt1, int paramInt2, Color paramColor, ImageObserver paramImageObserver)
  {
    boolean bool;
    if (paramImage == null)
      return true;
    int i = paramImage.getWidth(null);
    int j = paramImage.getHeight(null);
    if ((i < 0) || (j < 0))
      bool = false;
    else
      bool = drawImage(paramImage, paramInt1, paramInt2, i, j, paramColor, paramImageObserver);
    return bool;
  }

  public boolean drawImage(Image paramImage, int paramInt1, int paramInt2, int paramInt3, int paramInt4, Color paramColor, ImageObserver paramImageObserver)
  {
    boolean bool;
    if (paramImage == null)
      return true;
    int i = paramImage.getWidth(null);
    int j = paramImage.getHeight(null);
    if ((i < 0) || (j < 0))
      bool = false;
    else
      bool = drawImage(paramImage, paramInt1, paramInt2, paramInt1 + paramInt3, paramInt2 + paramInt4, 0, 0, i, j, paramImageObserver);
    return bool;
  }

  public boolean drawImage(Image paramImage, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6, int paramInt7, int paramInt8, ImageObserver paramImageObserver)
  {
    return drawImage(paramImage, paramInt1, paramInt2, paramInt3, paramInt4, paramInt5, paramInt6, paramInt7, paramInt8, null, paramImageObserver);
  }

  public boolean drawImage(Image paramImage, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6, int paramInt7, int paramInt8, Color paramColor, ImageObserver paramImageObserver)
  {
    if (paramImage == null)
      return true;
    int i = paramInt7 - paramInt5;
    int j = paramInt8 - paramInt6;
    float f1 = (paramInt3 - paramInt1) / i;
    float f2 = (paramInt4 - paramInt2) / j;
    AffineTransform localAffineTransform = new AffineTransform(f1, 0F, 0F, f2, paramInt1 - paramInt5 * f1, paramInt2 - paramInt6 * f2);
    if (paramInt7 < paramInt5)
    {
      paramInt5 = paramInt7;
      i = -i;
    }
    if (paramInt8 < paramInt6)
    {
      paramInt6 = paramInt8;
      j = -j;
    }
    return drawImageToPlatform(paramImage, localAffineTransform, paramColor, paramInt5, paramInt6, i, j, false);
  }

  public boolean drawImage(Image paramImage, AffineTransform paramAffineTransform, ImageObserver paramImageObserver)
  {
    boolean bool;
    if (paramImage == null)
      return true;
    int i = paramImage.getWidth(null);
    int j = paramImage.getHeight(null);
    if ((i < 0) || (j < 0))
      bool = false;
    else
      bool = drawImageToPlatform(paramImage, paramAffineTransform, null, 0, 0, i, j, false);
    return bool;
  }

  public void drawImage(BufferedImage paramBufferedImage, BufferedImageOp paramBufferedImageOp, int paramInt1, int paramInt2)
  {
    if (paramBufferedImage == null)
      return;
    int i = paramBufferedImage.getWidth(null);
    int j = paramBufferedImage.getHeight(null);
    if (paramBufferedImageOp != null)
      paramBufferedImage = paramBufferedImageOp.filter(paramBufferedImage, null);
    if ((i <= 0) || (j <= 0))
      return;
    AffineTransform localAffineTransform = new AffineTransform(1F, 0F, 0F, 1F, paramInt1, paramInt2);
    drawImageToPlatform(paramBufferedImage, localAffineTransform, null, 0, 0, i, j, false);
  }

  public void drawRenderedImage(RenderedImage paramRenderedImage, AffineTransform paramAffineTransform)
  {
    if (paramRenderedImage == null)
      return;
    BufferedImage localBufferedImage = null;
    int i = paramRenderedImage.getWidth();
    int j = paramRenderedImage.getHeight();
    if ((i <= 0) || (j <= 0))
      return;
    if (paramRenderedImage instanceof BufferedImage)
    {
      localBufferedImage = (BufferedImage)paramRenderedImage;
    }
    else
    {
      localBufferedImage = new BufferedImage(i, j, 2);
      Graphics2D localGraphics2D = localBufferedImage.createGraphics();
      localGraphics2D.drawRenderedImage(paramRenderedImage, paramAffineTransform);
    }
    drawImageToPlatform(localBufferedImage, paramAffineTransform, null, 0, 0, i, j, false);
  }

  static
  {
    fontMapRef = new SoftReference(null);
  }
}