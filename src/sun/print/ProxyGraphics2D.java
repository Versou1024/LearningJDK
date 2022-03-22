package sun.print;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.RenderingHints.Key;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderContext;
import java.awt.image.renderable.RenderableImage;
import java.awt.print.PrinterGraphics;
import java.awt.print.PrinterJob;
import java.text.AttributedCharacterIterator;
import java.util.Map;

public class ProxyGraphics2D extends Graphics2D
  implements PrinterGraphics
{
  Graphics2D mGraphics;
  PrinterJob mPrinterJob;

  public ProxyGraphics2D(Graphics2D paramGraphics2D, PrinterJob paramPrinterJob)
  {
    this.mGraphics = paramGraphics2D;
    this.mPrinterJob = paramPrinterJob;
  }

  public Graphics2D getDelegate()
  {
    return this.mGraphics;
  }

  public void setDelegate(Graphics2D paramGraphics2D)
  {
    this.mGraphics = paramGraphics2D;
  }

  public PrinterJob getPrinterJob()
  {
    return this.mPrinterJob;
  }

  public GraphicsConfiguration getDeviceConfiguration()
  {
    return ((RasterPrinterJob)this.mPrinterJob).getPrinterGraphicsConfig();
  }

  public Graphics create()
  {
    return new ProxyGraphics2D((Graphics2D)this.mGraphics.create(), this.mPrinterJob);
  }

  public void translate(int paramInt1, int paramInt2)
  {
    this.mGraphics.translate(paramInt1, paramInt2);
  }

  public void translate(double paramDouble1, double paramDouble2)
  {
    this.mGraphics.translate(paramDouble1, paramDouble2);
  }

  public void rotate(double paramDouble)
  {
    this.mGraphics.rotate(paramDouble);
  }

  public void rotate(double paramDouble1, double paramDouble2, double paramDouble3)
  {
    this.mGraphics.rotate(paramDouble1, paramDouble2, paramDouble3);
  }

  public void scale(double paramDouble1, double paramDouble2)
  {
    this.mGraphics.scale(paramDouble1, paramDouble2);
  }

  public void shear(double paramDouble1, double paramDouble2)
  {
    this.mGraphics.shear(paramDouble1, paramDouble2);
  }

  public Color getColor()
  {
    return this.mGraphics.getColor();
  }

  public void setColor(Color paramColor)
  {
    this.mGraphics.setColor(paramColor);
  }

  public void setPaintMode()
  {
    this.mGraphics.setPaintMode();
  }

  public void setXORMode(Color paramColor)
  {
    this.mGraphics.setXORMode(paramColor);
  }

  public Font getFont()
  {
    return this.mGraphics.getFont();
  }

  public void setFont(Font paramFont)
  {
    this.mGraphics.setFont(paramFont);
  }

  public FontMetrics getFontMetrics(Font paramFont)
  {
    return this.mGraphics.getFontMetrics(paramFont);
  }

  public FontRenderContext getFontRenderContext()
  {
    return this.mGraphics.getFontRenderContext();
  }

  public Rectangle getClipBounds()
  {
    return this.mGraphics.getClipBounds();
  }

  public void clipRect(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    this.mGraphics.clipRect(paramInt1, paramInt2, paramInt3, paramInt4);
  }

  public void setClip(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    this.mGraphics.setClip(paramInt1, paramInt2, paramInt3, paramInt4);
  }

  public Shape getClip()
  {
    return this.mGraphics.getClip();
  }

  public void setClip(Shape paramShape)
  {
    this.mGraphics.setClip(paramShape);
  }

  public void copyArea(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6)
  {
    this.mGraphics.copyArea(paramInt1, paramInt2, paramInt3, paramInt4, paramInt5, paramInt6);
  }

  public void drawLine(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    this.mGraphics.drawLine(paramInt1, paramInt2, paramInt3, paramInt4);
  }

  public void fillRect(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    this.mGraphics.fillRect(paramInt1, paramInt2, paramInt3, paramInt4);
  }

  public void clearRect(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    this.mGraphics.clearRect(paramInt1, paramInt2, paramInt3, paramInt4);
  }

  public void drawRoundRect(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6)
  {
    this.mGraphics.drawRoundRect(paramInt1, paramInt2, paramInt3, paramInt4, paramInt5, paramInt6);
  }

  public void fillRoundRect(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6)
  {
    this.mGraphics.fillRoundRect(paramInt1, paramInt2, paramInt3, paramInt4, paramInt5, paramInt6);
  }

  public void drawOval(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    this.mGraphics.drawOval(paramInt1, paramInt2, paramInt3, paramInt4);
  }

  public void fillOval(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    this.mGraphics.fillOval(paramInt1, paramInt2, paramInt3, paramInt4);
  }

  public void drawArc(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6)
  {
    this.mGraphics.drawArc(paramInt1, paramInt2, paramInt3, paramInt4, paramInt5, paramInt6);
  }

  public void fillArc(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6)
  {
    this.mGraphics.fillArc(paramInt1, paramInt2, paramInt3, paramInt4, paramInt5, paramInt6);
  }

  public void drawPolyline(int[] paramArrayOfInt1, int[] paramArrayOfInt2, int paramInt)
  {
    this.mGraphics.drawPolyline(paramArrayOfInt1, paramArrayOfInt2, paramInt);
  }

  public void drawPolygon(int[] paramArrayOfInt1, int[] paramArrayOfInt2, int paramInt)
  {
    this.mGraphics.drawPolygon(paramArrayOfInt1, paramArrayOfInt2, paramInt);
  }

  public void fillPolygon(int[] paramArrayOfInt1, int[] paramArrayOfInt2, int paramInt)
  {
    this.mGraphics.fillPolygon(paramArrayOfInt1, paramArrayOfInt2, paramInt);
  }

  public void drawString(String paramString, int paramInt1, int paramInt2)
  {
    this.mGraphics.drawString(paramString, paramInt1, paramInt2);
  }

  public void drawString(AttributedCharacterIterator paramAttributedCharacterIterator, int paramInt1, int paramInt2)
  {
    this.mGraphics.drawString(paramAttributedCharacterIterator, paramInt1, paramInt2);
  }

  public void drawString(AttributedCharacterIterator paramAttributedCharacterIterator, float paramFloat1, float paramFloat2)
  {
    this.mGraphics.drawString(paramAttributedCharacterIterator, paramFloat1, paramFloat2);
  }

  public boolean drawImage(Image paramImage, int paramInt1, int paramInt2, ImageObserver paramImageObserver)
  {
    return this.mGraphics.drawImage(paramImage, paramInt1, paramInt2, paramImageObserver);
  }

  public boolean drawImage(Image paramImage, int paramInt1, int paramInt2, int paramInt3, int paramInt4, ImageObserver paramImageObserver)
  {
    return this.mGraphics.drawImage(paramImage, paramInt1, paramInt2, paramInt3, paramInt4, paramImageObserver);
  }

  public boolean drawImage(Image paramImage, int paramInt1, int paramInt2, Color paramColor, ImageObserver paramImageObserver)
  {
    boolean bool;
    if (paramImage == null)
      return true;
    if (needToCopyBgColorImage(paramImage))
    {
      BufferedImage localBufferedImage = getBufferedImageCopy(paramImage, paramColor);
      bool = this.mGraphics.drawImage(localBufferedImage, paramInt1, paramInt2, null);
    }
    else
    {
      bool = this.mGraphics.drawImage(paramImage, paramInt1, paramInt2, paramColor, paramImageObserver);
    }
    return bool;
  }

  public boolean drawImage(Image paramImage, int paramInt1, int paramInt2, int paramInt3, int paramInt4, Color paramColor, ImageObserver paramImageObserver)
  {
    boolean bool;
    if (paramImage == null)
      return true;
    if (needToCopyBgColorImage(paramImage))
    {
      BufferedImage localBufferedImage = getBufferedImageCopy(paramImage, paramColor);
      bool = this.mGraphics.drawImage(localBufferedImage, paramInt1, paramInt2, paramInt3, paramInt4, null);
    }
    else
    {
      bool = this.mGraphics.drawImage(paramImage, paramInt1, paramInt2, paramInt3, paramInt4, paramColor, paramImageObserver);
    }
    return bool;
  }

  public boolean drawImage(Image paramImage, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6, int paramInt7, int paramInt8, ImageObserver paramImageObserver)
  {
    return this.mGraphics.drawImage(paramImage, paramInt1, paramInt2, paramInt3, paramInt4, paramInt5, paramInt6, paramInt7, paramInt8, paramImageObserver);
  }

  public boolean drawImage(Image paramImage, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6, int paramInt7, int paramInt8, Color paramColor, ImageObserver paramImageObserver)
  {
    boolean bool;
    if (paramImage == null)
      return true;
    if (needToCopyBgColorImage(paramImage))
    {
      BufferedImage localBufferedImage = getBufferedImageCopy(paramImage, paramColor);
      bool = this.mGraphics.drawImage(localBufferedImage, paramInt1, paramInt2, paramInt3, paramInt4, paramInt6, paramInt6, paramInt7, paramInt8, null);
    }
    else
    {
      bool = this.mGraphics.drawImage(paramImage, paramInt1, paramInt2, paramInt3, paramInt4, paramInt6, paramInt6, paramInt7, paramInt8, paramColor, paramImageObserver);
    }
    return bool;
  }

  private boolean needToCopyBgColorImage(Image paramImage)
  {
    AffineTransform localAffineTransform = getTransform();
    return ((localAffineTransform.getType() & 0x30) != 0);
  }

  private BufferedImage getBufferedImageCopy(Image paramImage, Color paramColor)
  {
    BufferedImage localBufferedImage = null;
    int i = paramImage.getWidth(null);
    int j = paramImage.getHeight(null);
    if ((i > 0) && (j > 0))
    {
      int k;
      if (paramImage instanceof BufferedImage)
      {
        localObject = (BufferedImage)paramImage;
        k = ((BufferedImage)localObject).getType();
      }
      else
      {
        k = 2;
      }
      localBufferedImage = new BufferedImage(i, j, k);
      Object localObject = localBufferedImage.createGraphics();
      ((Graphics)localObject).drawImage(paramImage, 0, 0, paramColor, null);
      ((Graphics)localObject).dispose();
    }
    else
    {
      localBufferedImage = null;
    }
    return ((BufferedImage)localBufferedImage);
  }

  public void drawRenderedImage(RenderedImage paramRenderedImage, AffineTransform paramAffineTransform)
  {
    this.mGraphics.drawRenderedImage(paramRenderedImage, paramAffineTransform);
  }

  public void drawRenderableImage(RenderableImage paramRenderableImage, AffineTransform paramAffineTransform)
  {
    AffineTransform localAffineTransform3;
    if (paramRenderableImage == null)
      return;
    AffineTransform localAffineTransform1 = getTransform();
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

  public void dispose()
  {
    this.mGraphics.dispose();
  }

  public void finalize()
  {
  }

  public void draw(Shape paramShape)
  {
    this.mGraphics.draw(paramShape);
  }

  public boolean drawImage(Image paramImage, AffineTransform paramAffineTransform, ImageObserver paramImageObserver)
  {
    return this.mGraphics.drawImage(paramImage, paramAffineTransform, paramImageObserver);
  }

  public void drawImage(BufferedImage paramBufferedImage, BufferedImageOp paramBufferedImageOp, int paramInt1, int paramInt2)
  {
    this.mGraphics.drawImage(paramBufferedImage, paramBufferedImageOp, paramInt1, paramInt2);
  }

  public void drawString(String paramString, float paramFloat1, float paramFloat2)
  {
    this.mGraphics.drawString(paramString, paramFloat1, paramFloat2);
  }

  public void drawGlyphVector(GlyphVector paramGlyphVector, float paramFloat1, float paramFloat2)
  {
    this.mGraphics.drawGlyphVector(paramGlyphVector, paramFloat1, paramFloat2);
  }

  public void fill(Shape paramShape)
  {
    this.mGraphics.fill(paramShape);
  }

  public boolean hit(Rectangle paramRectangle, Shape paramShape, boolean paramBoolean)
  {
    return this.mGraphics.hit(paramRectangle, paramShape, paramBoolean);
  }

  public void setComposite(Composite paramComposite)
  {
    this.mGraphics.setComposite(paramComposite);
  }

  public void setPaint(Paint paramPaint)
  {
    this.mGraphics.setPaint(paramPaint);
  }

  public void setStroke(Stroke paramStroke)
  {
    this.mGraphics.setStroke(paramStroke);
  }

  public void setRenderingHint(RenderingHints.Key paramKey, Object paramObject)
  {
    this.mGraphics.setRenderingHint(paramKey, paramObject);
  }

  public Object getRenderingHint(RenderingHints.Key paramKey)
  {
    return this.mGraphics.getRenderingHint(paramKey);
  }

  public void setRenderingHints(Map<?, ?> paramMap)
  {
    this.mGraphics.setRenderingHints(paramMap);
  }

  public void addRenderingHints(Map<?, ?> paramMap)
  {
    this.mGraphics.addRenderingHints(paramMap);
  }

  public RenderingHints getRenderingHints()
  {
    return this.mGraphics.getRenderingHints();
  }

  public void transform(AffineTransform paramAffineTransform)
  {
    this.mGraphics.transform(paramAffineTransform);
  }

  public void setTransform(AffineTransform paramAffineTransform)
  {
    this.mGraphics.setTransform(paramAffineTransform);
  }

  public AffineTransform getTransform()
  {
    return this.mGraphics.getTransform();
  }

  public Paint getPaint()
  {
    return this.mGraphics.getPaint();
  }

  public Composite getComposite()
  {
    return this.mGraphics.getComposite();
  }

  public void setBackground(Color paramColor)
  {
    this.mGraphics.setBackground(paramColor);
  }

  public Color getBackground()
  {
    return this.mGraphics.getBackground();
  }

  public Stroke getStroke()
  {
    return this.mGraphics.getStroke();
  }

  public void clip(Shape paramShape)
  {
    this.mGraphics.clip(paramShape);
  }
}