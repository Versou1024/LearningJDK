package sun.awt.windows;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D.Float;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D.Double;
import java.awt.geom.Point2D.Float;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Float;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.util.Hashtable;
import sun.awt.FontConfiguration;
import sun.awt.PlatformFont;
import sun.awt.image.ByteComponentRaster;
import sun.awt.image.BytePackedRaster;
import sun.font.CompositeFont;
import sun.font.Font2D;
import sun.font.FontManager;
import sun.java2d.SunGraphicsEnvironment;
import sun.print.PathGraphics;
import sun.print.ProxyGraphics2D;

class WPathGraphics extends PathGraphics
{
  private static final int DEFAULT_USER_RES = 72;
  private static final float MIN_DEVICE_LINEWIDTH = 1.2000000476837158F;
  private static final float MAX_THINLINE_INCHES = 0.014000000432133675F;
  private Font lastFont;
  private Font lastDeviceSizeFont;
  private int lastAngle;
  private float lastScaledFontSize;
  private float lastAverageWidthScale;
  private FontKey1 fontKey1;
  private FontKey2 fontKey2;

  WPathGraphics(Graphics2D paramGraphics2D, PrinterJob paramPrinterJob, Printable paramPrintable, PageFormat paramPageFormat, int paramInt, boolean paramBoolean)
  {
    super(paramGraphics2D, paramPrinterJob, paramPrintable, paramPageFormat, paramInt, paramBoolean);
  }

  public Graphics create()
  {
    return new WPathGraphics((Graphics2D)getDelegate().create(), getPrinterJob(), getPrintable(), getPageFormat(), getPageIndex(), canDoRedraws());
  }

  public void draw(Shape paramShape)
  {
    Stroke localStroke = getStroke();
    if (localStroke instanceof BasicStroke)
    {
      BasicStroke localBasicStroke2 = null;
      BasicStroke localBasicStroke1 = (BasicStroke)localStroke;
      float f2 = localBasicStroke1.getLineWidth();
      Point2D.Float localFloat1 = new Point2D.Float(f2, f2);
      AffineTransform localAffineTransform1 = getTransform();
      localAffineTransform1.deltaTransform(localFloat1, localFloat1);
      float f1 = Math.min(Math.abs(localFloat1.x), Math.abs(localFloat1.y));
      if (f1 < 1.2000000476837158F)
      {
        Point2D.Float localFloat2 = new Point2D.Float(1.2000000476837158F, 1.2000000476837158F);
        try
        {
          AffineTransform localAffineTransform2 = localAffineTransform1.createInverse();
          localAffineTransform2.deltaTransform(localFloat2, localFloat2);
          float f3 = Math.max(Math.abs(localFloat2.x), Math.abs(localFloat2.y));
          localBasicStroke2 = new BasicStroke(f3, localBasicStroke1.getEndCap(), localBasicStroke1.getLineJoin(), localBasicStroke1.getMiterLimit(), localBasicStroke1.getDashArray(), localBasicStroke1.getDashPhase());
          setStroke(localBasicStroke2);
        }
        catch (NoninvertibleTransformException localNoninvertibleTransformException)
        {
        }
      }
      super.draw(paramShape);
      if (localBasicStroke2 != null)
        setStroke(localBasicStroke1);
    }
    else
    {
      super.draw(paramShape);
    }
  }

  public void drawString(String paramString, int paramInt1, int paramInt2)
  {
    drawString(paramString, paramInt1, paramInt2);
  }

  public void drawString(String paramString, float paramFloat1, float paramFloat2)
  {
    drawString(paramString, paramFloat1, paramFloat2, getFont(), getFontRenderContext(), 0F);
  }

  protected int platformFontCount(Font paramFont, String paramString)
  {
    AffineTransform localAffineTransform1 = getTransform();
    AffineTransform localAffineTransform2 = new AffineTransform(localAffineTransform1);
    localAffineTransform2.concatenate(getFont().getTransform());
    int i = localAffineTransform2.getType();
    int j = ((i != 32) && ((i & 0x40) == 0)) ? 1 : 0;
    if (j == 0)
      return 0;
    WPrinterJob localWPrinterJob = (WPrinterJob)getPrinterJob();
    Font2D localFont2D = FontManager.getFont2D(paramFont);
    if (localFont2D instanceof CompositeFont)
    {
      if ((!(((CompositeFont)localFont2D).isStdComposite())) || (FontManager.usingAlternateCompositeFonts()))
      {
        Font localFont = new Font(paramFont.getFamily(), paramFont.getStyle(), 12);
        if (localFont.canDisplayUpTo(paramString) == -1)
          return ((localWPrinterJob.setFont(localFont, 0, 1F)) ? 1 : 0);
        return 0;
      }
      return ((localWPrinterJob.setLogicalFont(paramFont, 0, 1F)) ? 1 : 0);
    }
    return ((localWPrinterJob.setFont(paramFont, 0, 1F)) ? 1 : 0);
  }

  public void drawString(String paramString, float paramFloat1, float paramFloat2, Font paramFont, FontRenderContext paramFontRenderContext, float paramFloat3)
  {
    if (paramString.length() == 0)
      return;
    if ((paramFont.hasLayoutAttributes()) && (!(this.printingGlyphVector)))
    {
      localObject1 = new TextLayout(paramString, paramFont, paramFontRenderContext);
      ((TextLayout)localObject1).draw(this, paramFloat1, paramFloat2);
      return;
    }
    Object localObject1 = getFont();
    if (!(((Font)localObject1).equals(paramFont)))
      setFont(paramFont);
    else
      localObject1 = null;
    int i = 0;
    AffineTransform localAffineTransform1 = getTransform();
    AffineTransform localAffineTransform2 = new AffineTransform(localAffineTransform1);
    localAffineTransform2.concatenate(getFont().getTransform());
    int j = localAffineTransform2.getType();
    int k = ((j != 32) && ((j & 0x40) == 0)) ? 1 : 0;
    boolean bool1 = stringNeedsShaping(paramString);
    if ((!(WPrinterJob.shapeTextProp)) && (k != 0) && (!(bool1)))
    {
      Font localFont1;
      Point2D.Float localFloat = new Point2D.Float(paramFloat1, paramFloat2);
      if (getFont().isTransformed())
      {
        localObject2 = getFont().getTransform();
        f1 = (float)((AffineTransform)localObject2).getTranslateX();
        float f2 = (float)((AffineTransform)localObject2).getTranslateY();
        if (Math.abs(f1) < 0.000010000000000000001D)
          f1 = 0F;
        if (Math.abs(f2) < 0.000010000000000000001D)
          f2 = 0F;
        localFloat.x += f1;
        localFloat.y += f2;
      }
      localAffineTransform1.transform(localFloat, localFloat);
      Object localObject2 = getFont();
      float f1 = ((Font)localObject2).getSize2D();
      Point2D.Double localDouble1 = new Point2D.Double(0D, 1D);
      localAffineTransform2.deltaTransform(localDouble1, localDouble1);
      double d1 = Math.sqrt(localDouble1.x * localDouble1.x + localDouble1.y * localDouble1.y);
      float f3 = (float)(f1 * d1);
      Point2D.Double localDouble2 = new Point2D.Double(1D, 0D);
      localAffineTransform2.deltaTransform(localDouble2, localDouble2);
      double d2 = Math.sqrt(localDouble2.x * localDouble2.x + localDouble2.y * localDouble2.y);
      float f4 = (float)(f1 * d2);
      float f5 = (float)(d2 / d1);
      if ((f5 > 0.99900001287460327F) && (f5 < 1.0010000467300415F))
        f5 = 1F;
      double d3 = Math.toDegrees(Math.atan2(localDouble2.y, localDouble2.x));
      if (d3 < 0D)
        d3 += 360.0D;
      if (d3 != 0D)
        d3 = 360.0D - d3;
      int l = (int)Math.round(d3 * 10.0D);
      WPrinterJob localWPrinterJob = (WPrinterJob)getPrinterJob();
      if ((localObject2 != null) && (this.lastFont != null) && (this.lastDeviceSizeFont != null) && (f3 == this.lastScaledFontSize) && (f5 == this.lastAverageWidthScale) && (((Font)localObject2).equals(this.lastFont)) && (l == this.lastAngle))
      {
        localFont1 = this.lastDeviceSizeFont;
      }
      else
      {
        if (this.fontKey1 == null)
          this.fontKey1 = new FontKey1();
        this.fontKey1.init((Font)localObject2, l, f3, f5);
        localFont1 = (Font)localWPrinterJob.fontCache1.get(this.fontKey1);
        if (localFont1 == null)
        {
          localFont1 = ((Font)localObject2).deriveFont(f3);
          if (localWPrinterJob.fontCache1.size() > 500)
            localWPrinterJob.fontCache1 = new Hashtable();
          localWPrinterJob.fontCache1.put(this.fontKey1.copy(), localFont1);
        }
        this.lastAngle = l;
        this.lastScaledFontSize = f3;
        this.lastAverageWidthScale = f5;
        this.lastDeviceSizeFont = localFont1;
        this.lastFont = ((Font)localObject2);
      }
      Font2D localFont2D = FontManager.getFont2D(localFont1);
      int i1 = ((localFont2D instanceof CompositeFont) && (!(((CompositeFont)localFont2D).isStdComposite()))) ? 1 : 0;
      Font localFont2 = null;
      boolean bool2 = false;
      boolean bool3 = localWPrinterJob.setFont(localFont1, l, f5);
      if ((bool3) && (i1 != 0))
      {
        Font localFont3 = new Font(localFont1.getFamily(), localFont1.getStyle(), 12);
        bool3 = localFont3.canDisplayUpTo(paramString) == -1;
      }
      if ((!(bool3)) && (SunGraphicsEnvironment.isLogicalFont(localFont1)))
      {
        if ((!(FontManager.usingAlternateCompositeFonts())) && (i1 == 0))
        {
          localFont2 = localFont1;
          if (FontConfiguration.isLogicalFontFaceName(localFont2.getFontName()))
          {
            int i2 = localFont2D.getStyle() | localFont2.getStyle();
            if (this.fontKey2 == null)
              this.fontKey2 = new FontKey2();
            this.fontKey2.init(localFont1.getFamily(), i2, localFont1.getSize(), l, f5);
            localFont2 = (Font)localWPrinterJob.fontCache2.get(this.fontKey2);
            if (localFont2 == null)
            {
              localFont2 = new Font(localFont1.getFamily(), i2, localFont1.getSize());
              if (localWPrinterJob.fontCache2.size() > 500)
                localWPrinterJob.fontCache2 = new Hashtable();
              localWPrinterJob.fontCache2.put(this.fontKey2.copy(), localFont2);
            }
          }
          bool2 = localWPrinterJob.setLogicalFont(localFont2, l, f5);
        }
        if (bool2)
          try
          {
            if (((PlatformFont)localFont2.getPeer()).makeMultiCharsetString(paramString, false) == null)
              bool2 = false;
          }
          catch (Exception localException)
          {
            bool2 = false;
          }
      }
      if ((bool3) || (bool2))
      {
        try
        {
          localWPrinterJob.setTextColor((Color)getPaint());
        }
        catch (ClassCastException localClassCastException)
        {
          if (localObject1 != null)
            setFont((Font)localObject1);
          throw new IllegalArgumentException("Expected a Color instance");
        }
        if (getClip() != null)
          deviceClip(getClip().getPathIterator(localAffineTransform1));
        localWPrinterJob.textOut(paramString, localFloat.x, localFloat.y, (bool2) ? localFont2 : null);
        i = 1;
      }
    }
    if (i == 0)
    {
      if (localObject1 != null)
      {
        setFont((Font)localObject1);
        localObject1 = null;
      }
      super.drawString(paramString, paramFloat1, paramFloat2, paramFont, paramFontRenderContext, paramFloat3);
    }
    if (localObject1 != null)
      setFont((Font)localObject1);
  }

  private boolean stringNeedsShaping(String paramString)
  {
    int i = 0;
    char[] arrayOfChar = paramString.toCharArray();
    for (int k = 0; k < arrayOfChar.length; ++k)
    {
      int j = arrayOfChar[k];
      if ((j & 0xFE00) == 0)
        break label118:
      if ((j >= 1424) && (j <= 1535))
      {
        i = 1;
        break;
      }
      if ((j >= 1536) && (j <= 1791))
      {
        i = 1;
        break;
      }
      if ((j >= 8234) && (j <= 8238))
      {
        i = 1;
        break;
      }
      if ((j >= 8298) && (j <= 8303))
      {
        i = 1;
        label118: break;
      }
    }
    return i;
  }

  protected boolean drawImageToPlatform(Image paramImage, AffineTransform paramAffineTransform, Color paramColor, int paramInt1, int paramInt2, int paramInt3, int paramInt4, boolean paramBoolean)
  {
    BufferedImage localBufferedImage1 = getBufferedImage(paramImage);
    if (localBufferedImage1 == null)
      return true;
    WPrinterJob localWPrinterJob = (WPrinterJob)getPrinterJob();
    AffineTransform localAffineTransform1 = getTransform();
    if (paramAffineTransform == null)
      paramAffineTransform = new AffineTransform();
    localAffineTransform1.concatenate(paramAffineTransform);
    double[] arrayOfDouble = new double[6];
    localAffineTransform1.getMatrix(arrayOfDouble);
    Point2D.Float localFloat1 = new Point2D.Float(1F, 0F);
    Point2D.Float localFloat2 = new Point2D.Float(0F, 1F);
    localAffineTransform1.deltaTransform(localFloat1, localFloat1);
    localAffineTransform1.deltaTransform(localFloat2, localFloat2);
    Point2D.Float localFloat3 = new Point2D.Float(0F, 0F);
    double d1 = localFloat1.distance(localFloat3);
    double d2 = localFloat2.distance(localFloat3);
    double d3 = localWPrinterJob.getXRes();
    double d4 = localWPrinterJob.getYRes();
    double d5 = d3 / 72.0D;
    double d6 = d4 / 72.0D;
    int i = localAffineTransform1.getType();
    int j = ((i & 0x30) != 0) ? 1 : 0;
    if (j != 0)
    {
      if (d1 > d5)
        d1 = d5;
      if (d2 > d6)
        d2 = d6;
    }
    if ((d1 != 0D) && (d2 != 0D))
    {
      AffineTransform localAffineTransform2 = new AffineTransform(arrayOfDouble[0] / d1, arrayOfDouble[1] / d2, arrayOfDouble[2] / d1, arrayOfDouble[3] / d2, arrayOfDouble[4] / d1, arrayOfDouble[5] / d2);
      Rectangle2D.Float localFloat = new Rectangle2D.Float(paramInt1, paramInt2, paramInt3, paramInt4);
      Shape localShape1 = localAffineTransform2.createTransformedShape(localFloat);
      Rectangle2D localRectangle2D1 = localShape1.getBounds2D();
      localRectangle2D1.setRect(localRectangle2D1.getX(), localRectangle2D1.getY(), localRectangle2D1.getWidth() + 0.001D, localRectangle2D1.getHeight() + 0.001D);
      int k = (int)localRectangle2D1.getWidth();
      int l = (int)localRectangle2D1.getHeight();
      if ((k > 0) && (l > 0))
      {
        label440: int i6;
        int i8;
        byte[] arrayOfByte;
        int i1 = 1;
        if ((!(paramBoolean)) && (hasTransparentPixels(localBufferedImage1)))
        {
          i1 = 0;
          if (isBitmaskTransparency(localBufferedImage1))
          {
            if (paramColor == null)
            {
              if (!(drawBitmaskImage(localBufferedImage1, paramAffineTransform, paramColor, paramInt1, paramInt2, paramInt3, paramInt4)))
                break label440;
              return true;
            }
            if (paramColor.getTransparency() == 1)
              i1 = 1;
          }
          if (!(canDoRedraws()))
            i1 = 1;
        }
        else
        {
          paramColor = null;
        }
        if ((((paramInt1 + paramInt3 > localBufferedImage1.getWidth(null)) || (paramInt2 + paramInt4 > localBufferedImage1.getHeight(null)))) && (canDoRedraws()))
          i1 = 0;
        if (i1 == 0)
        {
          localAffineTransform1.getMatrix(arrayOfDouble);
          AffineTransform localAffineTransform3 = new AffineTransform(arrayOfDouble[0] / d5, arrayOfDouble[1] / d6, arrayOfDouble[2] / d5, arrayOfDouble[3] / d6, arrayOfDouble[4] / d5, arrayOfDouble[5] / d6);
          localObject1 = new Rectangle2D.Float(paramInt1, paramInt2, paramInt3, paramInt4);
          localObject2 = localAffineTransform1.createTransformedShape((Shape)localObject1);
          Rectangle2D localRectangle2D2 = ((Shape)localObject2).getBounds2D();
          localRectangle2D2.setRect(localRectangle2D2.getX(), localRectangle2D2.getY(), localRectangle2D2.getWidth() + 0.001D, localRectangle2D2.getHeight() + 0.001D);
          int i4 = (int)localRectangle2D2.getWidth();
          i6 = (int)localRectangle2D2.getHeight();
          i8 = i4 * i6 * 3;
          i9 = 8388608;
          double d7 = (d3 < d4) ? d3 : d4;
          int i10 = (int)d7;
          double d8 = 1D;
          while ((i8 > i9) && (i10 > 72))
          {
            d8 *= 2.0D;
            i10 /= 2;
            i8 /= 4;
          }
          if (i10 < 72)
            d8 = d7 / 72.0D;
          localRectangle2D2.setRect(localRectangle2D2.getX() / d8, localRectangle2D2.getY() / d8, localRectangle2D2.getWidth() / d8, localRectangle2D2.getHeight() / d8);
          localWPrinterJob.saveState(getTransform(), getClip(), localRectangle2D2, d8, d8);
          return true;
        }
        int i2 = 5;
        Object localObject1 = null;
        Object localObject2 = localBufferedImage1.getColorModel();
        int i3 = localBufferedImage1.getType();
        if ((localObject2 instanceof IndexColorModel) && (((ColorModel)localObject2).getPixelSize() <= 8) && (((i3 == 12) || (i3 == 13))))
        {
          localObject1 = (IndexColorModel)localObject2;
          i2 = i3;
          if ((i3 == 12) && (((ColorModel)localObject2).getPixelSize() == 2))
          {
            int[] arrayOfInt = new int[16];
            ((IndexColorModel)localObject1).getRGBs(arrayOfInt);
            i6 = (((IndexColorModel)localObject1).getTransparency() != 1) ? 1 : 0;
            i8 = ((IndexColorModel)localObject1).getTransparentPixel();
            localObject1 = new IndexColorModel(4, 16, arrayOfInt, 0, i6, i8, 0);
          }
        }
        int i5 = (int)localRectangle2D1.getWidth();
        int i7 = (int)localRectangle2D1.getHeight();
        BufferedImage localBufferedImage2 = null;
        int i9 = 1;
        if (i9 != 0)
        {
          if (localObject1 == null)
            localBufferedImage2 = new BufferedImage(i5, i7, i2);
          else
            localBufferedImage2 = new BufferedImage(i5, i7, i2, (IndexColorModel)localObject1);
          localObject3 = localBufferedImage2.createGraphics();
          ((Graphics2D)localObject3).clipRect(0, 0, localBufferedImage2.getWidth(), localBufferedImage2.getHeight());
          ((Graphics2D)localObject3).translate(-localRectangle2D1.getX(), -localRectangle2D1.getY());
          ((Graphics2D)localObject3).transform(localAffineTransform2);
          if (paramColor == null)
            paramColor = Color.white;
          ((Graphics2D)localObject3).drawImage(localBufferedImage1, paramInt1, paramInt2, paramInt1 + paramInt3, paramInt2 + paramInt4, paramInt1, paramInt2, paramInt1 + paramInt3, paramInt2 + paramInt4, paramColor, null);
          ((Graphics2D)localObject3).dispose();
        }
        else
        {
          localBufferedImage2 = localBufferedImage1;
        }
        Object localObject3 = new Rectangle2D.Float((float)(localRectangle2D1.getX() * d1), (float)(localRectangle2D1.getY() * d2), (float)(localRectangle2D1.getWidth() * d1), (float)(localRectangle2D1.getHeight() * d2));
        WritableRaster localWritableRaster = localBufferedImage2.getRaster();
        if (localWritableRaster instanceof ByteComponentRaster)
          arrayOfByte = ((ByteComponentRaster)localWritableRaster).getDataStorage();
        else if (localWritableRaster instanceof BytePackedRaster)
          arrayOfByte = ((BytePackedRaster)localWritableRaster).getDataStorage();
        else
          return false;
        Shape localShape2 = getClip();
        clip(paramAffineTransform.createTransformedShape(localFloat));
        deviceClip(getClip().getPathIterator(getTransform()));
        localWPrinterJob.drawDIBImage(arrayOfByte, ((Rectangle2D.Float)localObject3).x, ((Rectangle2D.Float)localObject3).y, (float)Math.rint(((Rectangle2D.Float)localObject3).width + 0.5D), (float)Math.rint(((Rectangle2D.Float)localObject3).height + 0.5D), 0F, 0F, localBufferedImage2.getWidth(), localBufferedImage2.getHeight(), (IndexColorModel)localObject1);
        setClip(localShape2);
      }
    }
    return true;
  }

  public void redrawRegion(Rectangle2D paramRectangle2D, double paramDouble1, double paramDouble2, Shape paramShape, AffineTransform paramAffineTransform)
    throws PrinterException
  {
    WPrinterJob localWPrinterJob = (WPrinterJob)getPrinterJob();
    Printable localPrintable = getPrintable();
    PageFormat localPageFormat = getPageFormat();
    int i = getPageIndex();
    BufferedImage localBufferedImage = new BufferedImage((int)paramRectangle2D.getWidth(), (int)paramRectangle2D.getHeight(), 5);
    Graphics2D localGraphics2D = localBufferedImage.createGraphics();
    ProxyGraphics2D localProxyGraphics2D = new ProxyGraphics2D(localGraphics2D, localWPrinterJob);
    localProxyGraphics2D.setColor(Color.white);
    localProxyGraphics2D.fillRect(0, 0, localBufferedImage.getWidth(), localBufferedImage.getHeight());
    localProxyGraphics2D.clipRect(0, 0, localBufferedImage.getWidth(), localBufferedImage.getHeight());
    localProxyGraphics2D.translate(-paramRectangle2D.getX(), -paramRectangle2D.getY());
    float f1 = (float)(localWPrinterJob.getXRes() / paramDouble1);
    float f2 = (float)(localWPrinterJob.getYRes() / paramDouble2);
    localProxyGraphics2D.scale(f1 / 72.0F, f2 / 72.0F);
    localProxyGraphics2D.translate(-localWPrinterJob.getPhysicalPrintableX(localPageFormat.getPaper()) / localWPrinterJob.getXRes() * 72.0D, -localWPrinterJob.getPhysicalPrintableY(localPageFormat.getPaper()) / localWPrinterJob.getYRes() * 72.0D);
    localProxyGraphics2D.transform(new AffineTransform(getPageFormat().getMatrix()));
    localProxyGraphics2D.setPaint(Color.black);
    localPrintable.print(localProxyGraphics2D, localPageFormat, i);
    localGraphics2D.dispose();
    deviceClip(paramShape.getPathIterator(paramAffineTransform));
    Rectangle2D.Float localFloat = new Rectangle2D.Float((float)(paramRectangle2D.getX() * paramDouble1), (float)(paramRectangle2D.getY() * paramDouble2), (float)(paramRectangle2D.getWidth() * paramDouble1), (float)(paramRectangle2D.getHeight() * paramDouble2));
    ByteComponentRaster localByteComponentRaster = (ByteComponentRaster)localBufferedImage.getRaster();
    localWPrinterJob.drawImage3ByteBGR(localByteComponentRaster.getDataStorage(), localFloat.x, localFloat.y, localFloat.width, localFloat.height, 0F, 0F, localBufferedImage.getWidth(), localBufferedImage.getHeight());
  }

  protected void deviceFill(PathIterator paramPathIterator, Color paramColor)
  {
    WPrinterJob localWPrinterJob = (WPrinterJob)getPrinterJob();
    convertToWPath(paramPathIterator);
    localWPrinterJob.selectSolidBrush(paramColor);
    localWPrinterJob.fillPath();
  }

  protected void deviceClip(PathIterator paramPathIterator)
  {
    WPrinterJob localWPrinterJob = (WPrinterJob)getPrinterJob();
    convertToWPath(paramPathIterator);
    localWPrinterJob.selectClipPath();
  }

  protected void deviceFrameRect(int paramInt1, int paramInt2, int paramInt3, int paramInt4, Color paramColor)
  {
    AffineTransform localAffineTransform = getTransform();
    int i = localAffineTransform.getType();
    int j = ((i & 0x30) != 0) ? 1 : 0;
    if (j != 0)
    {
      draw(new Rectangle2D.Float(paramInt1, paramInt2, paramInt3, paramInt4));
      return;
    }
    Stroke localStroke = getStroke();
    if (localStroke instanceof BasicStroke)
    {
      BasicStroke localBasicStroke = (BasicStroke)localStroke;
      int k = localBasicStroke.getEndCap();
      int l = localBasicStroke.getLineJoin();
      if ((k == 2) && (l == 0) && (localBasicStroke.getMiterLimit() == 10.0F))
      {
        float f1 = localBasicStroke.getLineWidth();
        Point2D.Float localFloat1 = new Point2D.Float(f1, f1);
        localAffineTransform.deltaTransform(localFloat1, localFloat1);
        float f2 = Math.min(Math.abs(localFloat1.x), Math.abs(localFloat1.y));
        Point2D.Float localFloat2 = new Point2D.Float(paramInt1, paramInt2);
        localAffineTransform.transform(localFloat2, localFloat2);
        Point2D.Float localFloat3 = new Point2D.Float(paramInt1 + paramInt3, paramInt2 + paramInt4);
        localAffineTransform.transform(localFloat3, localFloat3);
        float f3 = (float)(localFloat3.getX() - localFloat2.getX());
        float f4 = (float)(localFloat3.getY() - localFloat2.getY());
        WPrinterJob localWPrinterJob = (WPrinterJob)getPrinterJob();
        if (localWPrinterJob.selectStylePen(k, l, f2, paramColor) == true)
        {
          localWPrinterJob.frameRect((float)localFloat2.getX(), (float)localFloat2.getY(), f3, f4);
        }
        else
        {
          double d = Math.min(localWPrinterJob.getXRes(), localWPrinterJob.getYRes());
          if (f2 / d < 0.014000000432133675D)
          {
            localWPrinterJob.selectPen(f2, paramColor);
            localWPrinterJob.frameRect((float)localFloat2.getX(), (float)localFloat2.getY(), f3, f4);
          }
          else
          {
            draw(new Rectangle2D.Float(paramInt1, paramInt2, paramInt3, paramInt4));
          }
        }
      }
      else
      {
        draw(new Rectangle2D.Float(paramInt1, paramInt2, paramInt3, paramInt4));
      }
    }
  }

  protected void deviceFillRect(int paramInt1, int paramInt2, int paramInt3, int paramInt4, Color paramColor)
  {
    AffineTransform localAffineTransform = getTransform();
    int i = localAffineTransform.getType();
    int j = ((i & 0x30) != 0) ? 1 : 0;
    if (j != 0)
    {
      fill(new Rectangle2D.Float(paramInt1, paramInt2, paramInt3, paramInt4));
      return;
    }
    Point2D.Float localFloat1 = new Point2D.Float(paramInt1, paramInt2);
    localAffineTransform.transform(localFloat1, localFloat1);
    Point2D.Float localFloat2 = new Point2D.Float(paramInt1 + paramInt3, paramInt2 + paramInt4);
    localAffineTransform.transform(localFloat2, localFloat2);
    float f1 = (float)(localFloat2.getX() - localFloat1.getX());
    float f2 = (float)(localFloat2.getY() - localFloat1.getY());
    WPrinterJob localWPrinterJob = (WPrinterJob)getPrinterJob();
    localWPrinterJob.fillRect((float)localFloat1.getX(), (float)localFloat1.getY(), f1, f2, paramColor);
  }

  protected void deviceDrawLine(int paramInt1, int paramInt2, int paramInt3, int paramInt4, Color paramColor)
  {
    Stroke localStroke = getStroke();
    if (localStroke instanceof BasicStroke)
    {
      BasicStroke localBasicStroke = (BasicStroke)localStroke;
      if (localBasicStroke.getDashArray() != null)
      {
        draw(new Line2D.Float(paramInt1, paramInt2, paramInt3, paramInt4));
        return;
      }
      float f1 = localBasicStroke.getLineWidth();
      Point2D.Float localFloat1 = new Point2D.Float(f1, f1);
      AffineTransform localAffineTransform = getTransform();
      localAffineTransform.deltaTransform(localFloat1, localFloat1);
      float f2 = Math.min(Math.abs(localFloat1.x), Math.abs(localFloat1.y));
      Point2D.Float localFloat2 = new Point2D.Float(paramInt1, paramInt2);
      localAffineTransform.transform(localFloat2, localFloat2);
      Point2D.Float localFloat3 = new Point2D.Float(paramInt3, paramInt4);
      localAffineTransform.transform(localFloat3, localFloat3);
      int i = localBasicStroke.getEndCap();
      int j = localBasicStroke.getLineJoin();
      if ((localFloat3.getX() == localFloat2.getX()) && (localFloat3.getY() == localFloat2.getY()))
        i = 1;
      WPrinterJob localWPrinterJob = (WPrinterJob)getPrinterJob();
      if (localWPrinterJob.selectStylePen(i, j, f2, paramColor))
      {
        localWPrinterJob.moveTo((float)localFloat2.getX(), (float)localFloat2.getY());
        localWPrinterJob.lineTo((float)localFloat3.getX(), (float)localFloat3.getY());
      }
      else
      {
        double d = Math.min(localWPrinterJob.getXRes(), localWPrinterJob.getYRes());
        if ((i == 1) || ((((paramInt1 == paramInt3) || (paramInt2 == paramInt4))) && (f2 / d < 0.014000000432133675D)))
        {
          localWPrinterJob.selectPen(f2, paramColor);
          localWPrinterJob.moveTo((float)localFloat2.getX(), (float)localFloat2.getY());
          localWPrinterJob.lineTo((float)localFloat3.getX(), (float)localFloat3.getY());
        }
        else
        {
          draw(new Line2D.Float(paramInt1, paramInt2, paramInt3, paramInt4));
        }
      }
    }
  }

  private void convertToWPath(PathIterator paramPathIterator)
  {
    int j;
    float[] arrayOfFloat = new float[6];
    WPrinterJob localWPrinterJob = (WPrinterJob)getPrinterJob();
    if (paramPathIterator.getWindingRule() == 0)
      j = 1;
    else
      j = 2;
    localWPrinterJob.setPolyFillMode(j);
    localWPrinterJob.beginPath();
    while (!(paramPathIterator.isDone()))
    {
      int i = paramPathIterator.currentSegment(arrayOfFloat);
      switch (i)
      {
      case 0:
        localWPrinterJob.moveTo(arrayOfFloat[0], arrayOfFloat[1]);
        break;
      case 1:
        localWPrinterJob.lineTo(arrayOfFloat[0], arrayOfFloat[1]);
        break;
      case 2:
        int k = localWPrinterJob.getPenX();
        int l = localWPrinterJob.getPenY();
        float f1 = k + (arrayOfFloat[0] - k) * 2F / 3.0F;
        float f2 = l + (arrayOfFloat[1] - l) * 2F / 3.0F;
        float f3 = arrayOfFloat[2] - (arrayOfFloat[2] - arrayOfFloat[0]) * 2F / 3.0F;
        float f4 = arrayOfFloat[3] - (arrayOfFloat[3] - arrayOfFloat[1]) * 2F / 3.0F;
        localWPrinterJob.polyBezierTo(f1, f2, f3, f4, arrayOfFloat[2], arrayOfFloat[3]);
        break;
      case 3:
        localWPrinterJob.polyBezierTo(arrayOfFloat[0], arrayOfFloat[1], arrayOfFloat[2], arrayOfFloat[3], arrayOfFloat[4], arrayOfFloat[5]);
        break;
      case 4:
        localWPrinterJob.closeFigure();
      }
      paramPathIterator.next();
    }
    localWPrinterJob.endPath();
  }
}