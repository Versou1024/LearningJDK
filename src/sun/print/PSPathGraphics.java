package sun.print;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Line2D.Float;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D.Float;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Float;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import sun.awt.image.ByteComponentRaster;

class PSPathGraphics extends PathGraphics
{
  private static final int DEFAULT_USER_RES = 72;

  PSPathGraphics(Graphics2D paramGraphics2D, PrinterJob paramPrinterJob, Printable paramPrintable, PageFormat paramPageFormat, int paramInt, boolean paramBoolean)
  {
    super(paramGraphics2D, paramPrinterJob, paramPrintable, paramPageFormat, paramInt, paramBoolean);
  }

  public Graphics create()
  {
    return new PSPathGraphics((Graphics2D)getDelegate().create(), getPrinterJob(), getPrintable(), getPageFormat(), getPageIndex(), canDoRedraws());
  }

  public void fill(Shape paramShape, Color paramColor)
  {
    deviceFill(paramShape.getPathIterator(new AffineTransform()), paramColor);
  }

  public void drawString(String paramString, int paramInt1, int paramInt2)
  {
    drawString(paramString, paramInt1, paramInt2);
  }

  public void drawString(String paramString, float paramFloat1, float paramFloat2)
  {
    drawString(paramString, paramFloat1, paramFloat2, getFont(), getFontRenderContext(), 0F);
  }

  protected boolean canDrawStringToWidth()
  {
    return true;
  }

  protected int platformFontCount(Font paramFont, String paramString)
  {
    PSPrinterJob localPSPrinterJob = (PSPrinterJob)getPrinterJob();
    return localPSPrinterJob.platformFontCount(paramFont, paramString);
  }

  protected void drawString(String paramString, float paramFloat1, float paramFloat2, Font paramFont, FontRenderContext paramFontRenderContext, float paramFloat3)
  {
    if (paramString.length() == 0)
      return;
    if ((paramFont.hasLayoutAttributes()) && (!(this.printingGlyphVector)))
    {
      localObject = new TextLayout(paramString, paramFont, paramFontRenderContext);
      ((TextLayout)localObject).draw(this, paramFloat1, paramFloat2);
      return;
    }
    Object localObject = getFont();
    if (!(((Font)localObject).equals(paramFont)))
      setFont(paramFont);
    else
      localObject = null;
    boolean bool1 = false;
    float f1 = 0F;
    float f2 = 0F;
    boolean bool2 = getFont().isTransformed();
    if (bool2)
    {
      AffineTransform localAffineTransform = getFont().getTransform();
      int j = localAffineTransform.getType();
      if (j == 1)
      {
        f1 = (float)localAffineTransform.getTranslateX();
        f2 = (float)localAffineTransform.getTranslateY();
        if (Math.abs(f1) < 0.000010000000000000001D)
          f1 = 0F;
        if (Math.abs(f2) < 0.000010000000000000001D)
          f2 = 0F;
        bool2 = false;
      }
    }
    int i = (!(bool2)) ? 1 : 0;
    if ((!(PSPrinterJob.shapeTextProp)) && (i != 0))
    {
      PSPrinterJob localPSPrinterJob = (PSPrinterJob)getPrinterJob();
      if (localPSPrinterJob.setFont(getFont()))
      {
        try
        {
          localPSPrinterJob.setColor((Color)getPaint());
        }
        catch (ClassCastException localClassCastException)
        {
          if (localObject != null)
            setFont((Font)localObject);
          throw new IllegalArgumentException("Expected a Color instance");
        }
        localPSPrinterJob.setTransform(getTransform());
        localPSPrinterJob.setClip(getClip());
        bool1 = localPSPrinterJob.textOut(this, paramString, paramFloat1 + f1, paramFloat2 + f2, paramFont, paramFontRenderContext, paramFloat3);
      }
    }
    if (!(bool1))
    {
      if (localObject != null)
      {
        setFont((Font)localObject);
        localObject = null;
      }
      super.drawString(paramString, paramFloat1, paramFloat2, paramFont, paramFontRenderContext, paramFloat3);
    }
    if (localObject != null)
      setFont((Font)localObject);
  }

  protected boolean drawImageToPlatform(Image paramImage, AffineTransform paramAffineTransform, Color paramColor, int paramInt1, int paramInt2, int paramInt3, int paramInt4, boolean paramBoolean)
  {
    BufferedImage localBufferedImage = getBufferedImage(paramImage);
    if (localBufferedImage == null)
      return true;
    PSPrinterJob localPSPrinterJob = (PSPrinterJob)getPrinterJob();
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
    double d3 = localPSPrinterJob.getXRes();
    double d4 = localPSPrinterJob.getYRes();
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
      Rectangle2D.Float localFloat4 = new Rectangle2D.Float(paramInt1, paramInt2, paramInt3, paramInt4);
      Shape localShape1 = localAffineTransform2.createTransformedShape(localFloat4);
      Rectangle2D localRectangle2D = localShape1.getBounds2D();
      localRectangle2D.setRect(localRectangle2D.getX(), localRectangle2D.getY(), localRectangle2D.getWidth() + 0.001D, localRectangle2D.getHeight() + 0.001D);
      int k = (int)localRectangle2D.getWidth();
      int l = (int)localRectangle2D.getHeight();
      if ((k > 0) && (l > 0))
      {
        int i1 = 1;
        if ((!(paramBoolean)) && (hasTransparentPixels(localBufferedImage)))
        {
          i1 = 0;
          if (isBitmaskTransparency(localBufferedImage))
          {
            if (paramColor == null)
            {
              if (!(drawBitmaskImage(localBufferedImage, paramAffineTransform, paramColor, paramInt1, paramInt2, paramInt3, paramInt4)))
                break label440;
              return true;
            }
            if (paramColor.getTransparency() == 1)
              i1 = 1;
          }
          if (!(canDoRedraws()))
            label440: i1 = 1;
        }
        else
        {
          paramColor = null;
        }
        if ((((paramInt1 + paramInt3 > localBufferedImage.getWidth(null)) || (paramInt2 + paramInt4 > localBufferedImage.getHeight(null)))) && (canDoRedraws()))
          i1 = 0;
        if (i1 == 0)
        {
          localAffineTransform1.getMatrix(arrayOfDouble);
          localObject1 = new AffineTransform(arrayOfDouble[0] / d5, arrayOfDouble[1] / d6, arrayOfDouble[2] / d5, arrayOfDouble[3] / d6, arrayOfDouble[4] / d5, arrayOfDouble[5] / d6);
          localObject2 = new Rectangle2D.Float(paramInt1, paramInt2, paramInt3, paramInt4);
          localShape2 = localAffineTransform1.createTransformedShape((Shape)localObject2);
          localObject3 = localShape2.getBounds2D();
          ((Rectangle2D)localObject3).setRect(((Rectangle2D)localObject3).getX(), ((Rectangle2D)localObject3).getY(), ((Rectangle2D)localObject3).getWidth() + 0.001D, ((Rectangle2D)localObject3).getHeight() + 0.001D);
          int i2 = (int)((Rectangle2D)localObject3).getWidth();
          int i3 = (int)((Rectangle2D)localObject3).getHeight();
          int i4 = i2 * i3 * 3;
          int i5 = 8388608;
          double d7 = (d3 < d4) ? d3 : d4;
          int i6 = (int)d7;
          double d8 = 1D;
          while ((i4 > i5) && (i6 > 72))
          {
            d8 *= 2.0D;
            i6 /= 2;
            i4 /= 4;
          }
          if (i6 < 72)
            d8 = d7 / 72.0D;
          ((Rectangle2D)localObject3).setRect(((Rectangle2D)localObject3).getX() / d8, ((Rectangle2D)localObject3).getY() / d8, ((Rectangle2D)localObject3).getWidth() / d8, ((Rectangle2D)localObject3).getHeight() / d8);
          localPSPrinterJob.saveState(getTransform(), getClip(), (Rectangle2D)localObject3, d8, d8);
          return true;
        }
        Object localObject1 = new BufferedImage((int)localRectangle2D.getWidth(), (int)localRectangle2D.getHeight(), 5);
        Object localObject2 = ((BufferedImage)localObject1).createGraphics();
        ((Graphics2D)localObject2).clipRect(0, 0, ((BufferedImage)localObject1).getWidth(), ((BufferedImage)localObject1).getHeight());
        ((Graphics2D)localObject2).translate(-localRectangle2D.getX(), -localRectangle2D.getY());
        ((Graphics2D)localObject2).transform(localAffineTransform2);
        if (paramColor == null)
          paramColor = Color.white;
        ((Graphics2D)localObject2).drawImage(localBufferedImage, paramInt1, paramInt2, paramInt1 + paramInt3, paramInt2 + paramInt4, paramInt1, paramInt2, paramInt1 + paramInt3, paramInt2 + paramInt4, paramColor, null);
        Shape localShape2 = getClip();
        Object localObject3 = getTransform().createTransformedShape(localShape2);
        AffineTransform localAffineTransform3 = AffineTransform.getScaleInstance(d1, d2);
        Shape localShape3 = localAffineTransform3.createTransformedShape(localShape1);
        Area localArea1 = new Area(localShape3);
        Area localArea2 = new Area((Shape)localObject3);
        localArea1.intersect(localArea2);
        localPSPrinterJob.setClip(localArea1);
        Rectangle2D.Float localFloat5 = new Rectangle2D.Float((float)(localRectangle2D.getX() * d1), (float)(localRectangle2D.getY() * d2), (float)(localRectangle2D.getWidth() * d1), (float)(localRectangle2D.getHeight() * d2));
        ByteComponentRaster localByteComponentRaster = (ByteComponentRaster)((BufferedImage)localObject1).getRaster();
        localPSPrinterJob.drawImageBGR(localByteComponentRaster.getDataStorage(), localFloat5.x, localFloat5.y, (float)Math.rint(localFloat5.width + 0.5D), (float)Math.rint(localFloat5.height + 0.5D), 0F, 0F, ((BufferedImage)localObject1).getWidth(), ((BufferedImage)localObject1).getHeight(), ((BufferedImage)localObject1).getWidth(), ((BufferedImage)localObject1).getHeight());
        localPSPrinterJob.setClip(getTransform().createTransformedShape(localShape2));
        ((Graphics2D)localObject2).dispose();
      }
    }
    return true;
  }

  public void redrawRegion(Rectangle2D paramRectangle2D, double paramDouble1, double paramDouble2, Shape paramShape, AffineTransform paramAffineTransform)
    throws PrinterException
  {
    PSPrinterJob localPSPrinterJob = (PSPrinterJob)getPrinterJob();
    Printable localPrintable = getPrintable();
    PageFormat localPageFormat = getPageFormat();
    int i = getPageIndex();
    BufferedImage localBufferedImage = new BufferedImage((int)paramRectangle2D.getWidth(), (int)paramRectangle2D.getHeight(), 5);
    Graphics2D localGraphics2D = localBufferedImage.createGraphics();
    ProxyGraphics2D localProxyGraphics2D = new ProxyGraphics2D(localGraphics2D, localPSPrinterJob);
    localProxyGraphics2D.setColor(Color.white);
    localProxyGraphics2D.fillRect(0, 0, localBufferedImage.getWidth(), localBufferedImage.getHeight());
    localProxyGraphics2D.clipRect(0, 0, localBufferedImage.getWidth(), localBufferedImage.getHeight());
    localProxyGraphics2D.translate(-paramRectangle2D.getX(), -paramRectangle2D.getY());
    float f1 = (float)(localPSPrinterJob.getXRes() / paramDouble1);
    float f2 = (float)(localPSPrinterJob.getYRes() / paramDouble2);
    localProxyGraphics2D.scale(f1 / 72.0F, f2 / 72.0F);
    localProxyGraphics2D.translate(-localPSPrinterJob.getPhysicalPrintableX(localPageFormat.getPaper()) / localPSPrinterJob.getXRes() * 72.0D, -localPSPrinterJob.getPhysicalPrintableY(localPageFormat.getPaper()) / localPSPrinterJob.getYRes() * 72.0D);
    localProxyGraphics2D.transform(new AffineTransform(getPageFormat().getMatrix()));
    localProxyGraphics2D.setPaint(Color.black);
    localPrintable.print(localProxyGraphics2D, localPageFormat, i);
    localGraphics2D.dispose();
    localPSPrinterJob.setClip(paramAffineTransform.createTransformedShape(paramShape));
    Rectangle2D.Float localFloat = new Rectangle2D.Float((float)(paramRectangle2D.getX() * paramDouble1), (float)(paramRectangle2D.getY() * paramDouble2), (float)(paramRectangle2D.getWidth() * paramDouble1), (float)(paramRectangle2D.getHeight() * paramDouble2));
    ByteComponentRaster localByteComponentRaster = (ByteComponentRaster)localBufferedImage.getRaster();
    localPSPrinterJob.drawImageBGR(localByteComponentRaster.getDataStorage(), localFloat.x, localFloat.y, localFloat.width, localFloat.height, 0F, 0F, localBufferedImage.getWidth(), localBufferedImage.getHeight(), localBufferedImage.getWidth(), localBufferedImage.getHeight());
  }

  protected void deviceFill(PathIterator paramPathIterator, Color paramColor)
  {
    PSPrinterJob localPSPrinterJob = (PSPrinterJob)getPrinterJob();
    localPSPrinterJob.deviceFill(paramPathIterator, paramColor, getTransform(), getClip());
  }

  protected void deviceFrameRect(int paramInt1, int paramInt2, int paramInt3, int paramInt4, Color paramColor)
  {
    draw(new Rectangle2D.Float(paramInt1, paramInt2, paramInt3, paramInt4));
  }

  protected void deviceDrawLine(int paramInt1, int paramInt2, int paramInt3, int paramInt4, Color paramColor)
  {
    draw(new Line2D.Float(paramInt1, paramInt2, paramInt3, paramInt4));
  }

  protected void deviceFillRect(int paramInt1, int paramInt2, int paramInt3, int paramInt4, Color paramColor)
  {
    fill(new Rectangle2D.Float(paramInt1, paramInt2, paramInt3, paramInt4));
  }

  protected void deviceClip(PathIterator paramPathIterator)
  {
  }
}