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
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D.Float;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Float;
import java.awt.geom.RoundRectangle2D.Float;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.awt.print.PrinterGraphics;
import java.awt.print.PrinterJob;
import java.text.AttributedCharacterIterator;
import java.util.Map;
import sun.java2d.Spans;

public class PeekGraphics extends Graphics2D
  implements PrinterGraphics, ImageObserver, Cloneable
{
  Graphics2D mGraphics;
  PrinterJob mPrinterJob;
  private Spans mDrawingArea = new Spans();
  private PeekMetrics mPrintMetrics = new PeekMetrics();
  private boolean mAWTDrawingOnly = false;

  public PeekGraphics(Graphics2D paramGraphics2D, PrinterJob paramPrinterJob)
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

  public void setAWTDrawingOnly()
  {
    this.mAWTDrawingOnly = true;
  }

  public boolean getAWTDrawingOnly()
  {
    return this.mAWTDrawingOnly;
  }

  public Spans getDrawingArea()
  {
    return this.mDrawingArea;
  }

  public GraphicsConfiguration getDeviceConfiguration()
  {
    return ((RasterPrinterJob)this.mPrinterJob).getPrinterGraphicsConfig();
  }

  public Graphics create()
  {
    PeekGraphics localPeekGraphics = null;
    try
    {
      localPeekGraphics = (PeekGraphics)clone();
      localPeekGraphics.mGraphics = ((Graphics2D)this.mGraphics.create());
    }
    catch (CloneNotSupportedException localCloneNotSupportedException)
    {
    }
    return localPeekGraphics;
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
  }

  public void drawLine(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    addStrokeShape(new Line2D.Float(paramInt1, paramInt2, paramInt3, paramInt4));
    this.mPrintMetrics.draw(this);
  }

  public void fillRect(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    addDrawingRect(new Rectangle2D.Float(paramInt1, paramInt2, paramInt3, paramInt4));
    this.mPrintMetrics.fill(this);
  }

  public void clearRect(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    Rectangle2D.Float localFloat = new Rectangle2D.Float(paramInt1, paramInt2, paramInt3, paramInt4);
    addDrawingRect(localFloat);
    this.mPrintMetrics.clear(this);
  }

  public void drawRoundRect(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6)
  {
    addStrokeShape(new RoundRectangle2D.Float(paramInt1, paramInt2, paramInt3, paramInt4, paramInt5, paramInt6));
    this.mPrintMetrics.draw(this);
  }

  public void fillRoundRect(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6)
  {
    Rectangle2D.Float localFloat = new Rectangle2D.Float(paramInt1, paramInt2, paramInt3, paramInt4);
    addDrawingRect(localFloat);
    this.mPrintMetrics.fill(this);
  }

  public void drawOval(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    addStrokeShape(new Rectangle2D.Float(paramInt1, paramInt2, paramInt3, paramInt4));
    this.mPrintMetrics.draw(this);
  }

  public void fillOval(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    Rectangle2D.Float localFloat = new Rectangle2D.Float(paramInt1, paramInt2, paramInt3, paramInt4);
    addDrawingRect(localFloat);
    this.mPrintMetrics.fill(this);
  }

  public void drawArc(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6)
  {
    addStrokeShape(new Rectangle2D.Float(paramInt1, paramInt2, paramInt3, paramInt4));
    this.mPrintMetrics.draw(this);
  }

  public void fillArc(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6)
  {
    Rectangle2D.Float localFloat = new Rectangle2D.Float(paramInt1, paramInt2, paramInt3, paramInt4);
    addDrawingRect(localFloat);
    this.mPrintMetrics.fill(this);
  }

  public void drawPolyline(int[] paramArrayOfInt1, int[] paramArrayOfInt2, int paramInt)
  {
    if (paramInt > 0)
    {
      int i = paramArrayOfInt1[0];
      int j = paramArrayOfInt2[0];
      for (int k = 1; k < paramInt; ++k)
      {
        drawLine(i, j, paramArrayOfInt1[k], paramArrayOfInt2[k]);
        i = paramArrayOfInt1[k];
        j = paramArrayOfInt2[k];
      }
    }
  }

  public void drawPolygon(int[] paramArrayOfInt1, int[] paramArrayOfInt2, int paramInt)
  {
    if (paramInt > 0)
    {
      drawPolyline(paramArrayOfInt1, paramArrayOfInt2, paramInt);
      drawLine(paramArrayOfInt1[(paramInt - 1)], paramArrayOfInt2[(paramInt - 1)], paramArrayOfInt1[0], paramArrayOfInt2[0]);
    }
  }

  public void fillPolygon(int[] paramArrayOfInt1, int[] paramArrayOfInt2, int paramInt)
  {
    if (paramInt > 0)
    {
      int i = paramArrayOfInt1[0];
      int j = paramArrayOfInt2[0];
      int k = paramArrayOfInt1[0];
      int l = paramArrayOfInt2[0];
      for (int i1 = 1; i1 < paramInt; ++i1)
      {
        if (paramArrayOfInt1[i1] < i)
          i = paramArrayOfInt1[i1];
        else if (paramArrayOfInt1[i1] > k)
          k = paramArrayOfInt1[i1];
        if (paramArrayOfInt2[i1] < j)
          j = paramArrayOfInt2[i1];
        else if (paramArrayOfInt2[i1] > l)
          l = paramArrayOfInt2[i1];
      }
      addDrawingRect(i, j, k - i, l - j);
    }
    this.mPrintMetrics.fill(this);
  }

  public void drawString(String paramString, int paramInt1, int paramInt2)
  {
    if (paramString.length() == 0)
      return;
    drawTextLayout(new TextLayout(paramString, getFont(), getFontRenderContext()), paramInt1, paramInt2);
  }

  public void drawString(AttributedCharacterIterator paramAttributedCharacterIterator, int paramInt1, int paramInt2)
  {
    if (paramAttributedCharacterIterator == null)
      throw new NullPointerException("attributedcharacteriterator is null");
    drawTextLayout(new TextLayout(paramAttributedCharacterIterator, getFontRenderContext()), paramInt1, paramInt2);
  }

  public void drawString(AttributedCharacterIterator paramAttributedCharacterIterator, float paramFloat1, float paramFloat2)
  {
    if (paramAttributedCharacterIterator == null)
      throw new NullPointerException("attributedcharacteriterator is null");
    drawTextLayout(new TextLayout(paramAttributedCharacterIterator, getFontRenderContext()), paramFloat1, paramFloat2);
  }

  public boolean drawImage(Image paramImage, int paramInt1, int paramInt2, ImageObserver paramImageObserver)
  {
    if (paramImage == null)
      return true;
    ImageWaiter localImageWaiter = new ImageWaiter(this, paramImage);
    addDrawingRect(paramInt1, paramInt2, localImageWaiter.getWidth(), localImageWaiter.getHeight());
    this.mPrintMetrics.drawImage(this, paramImage);
    return this.mGraphics.drawImage(paramImage, paramInt1, paramInt2, paramImageObserver);
  }

  public boolean drawImage(Image paramImage, int paramInt1, int paramInt2, int paramInt3, int paramInt4, ImageObserver paramImageObserver)
  {
    if (paramImage == null)
      return true;
    addDrawingRect(paramInt1, paramInt2, paramInt3, paramInt4);
    this.mPrintMetrics.drawImage(this, paramImage);
    return this.mGraphics.drawImage(paramImage, paramInt1, paramInt2, paramInt3, paramInt4, paramImageObserver);
  }

  public boolean drawImage(Image paramImage, int paramInt1, int paramInt2, Color paramColor, ImageObserver paramImageObserver)
  {
    if (paramImage == null)
      return true;
    ImageWaiter localImageWaiter = new ImageWaiter(this, paramImage);
    addDrawingRect(paramInt1, paramInt2, localImageWaiter.getWidth(), localImageWaiter.getHeight());
    this.mPrintMetrics.drawImage(this, paramImage);
    return this.mGraphics.drawImage(paramImage, paramInt1, paramInt2, paramColor, paramImageObserver);
  }

  public boolean drawImage(Image paramImage, int paramInt1, int paramInt2, int paramInt3, int paramInt4, Color paramColor, ImageObserver paramImageObserver)
  {
    if (paramImage == null)
      return true;
    addDrawingRect(paramInt1, paramInt2, paramInt3, paramInt4);
    this.mPrintMetrics.drawImage(this, paramImage);
    return this.mGraphics.drawImage(paramImage, paramInt1, paramInt2, paramInt3, paramInt4, paramColor, paramImageObserver);
  }

  public boolean drawImage(Image paramImage, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6, int paramInt7, int paramInt8, ImageObserver paramImageObserver)
  {
    if (paramImage == null)
      return true;
    int i = paramInt3 - paramInt1;
    int j = paramInt4 - paramInt2;
    addDrawingRect(paramInt1, paramInt2, i, j);
    this.mPrintMetrics.drawImage(this, paramImage);
    return this.mGraphics.drawImage(paramImage, paramInt1, paramInt2, paramInt3, paramInt4, paramInt5, paramInt6, paramInt7, paramInt8, paramImageObserver);
  }

  public boolean drawImage(Image paramImage, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6, int paramInt7, int paramInt8, Color paramColor, ImageObserver paramImageObserver)
  {
    if (paramImage == null)
      return true;
    int i = paramInt3 - paramInt1;
    int j = paramInt4 - paramInt2;
    addDrawingRect(paramInt1, paramInt2, i, j);
    this.mPrintMetrics.drawImage(this, paramImage);
    return this.mGraphics.drawImage(paramImage, paramInt1, paramInt2, paramInt3, paramInt4, paramInt5, paramInt6, paramInt7, paramInt8, paramColor, paramImageObserver);
  }

  public void drawRenderedImage(RenderedImage paramRenderedImage, AffineTransform paramAffineTransform)
  {
    if (paramRenderedImage == null)
      return;
    this.mPrintMetrics.drawImage(this, paramRenderedImage);
    this.mDrawingArea.addInfinite();
  }

  public void drawRenderableImage(RenderableImage paramRenderableImage, AffineTransform paramAffineTransform)
  {
    if (paramRenderableImage == null)
      return;
    this.mPrintMetrics.drawImage(this, paramRenderableImage);
    this.mDrawingArea.addInfinite();
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
    addStrokeShape(paramShape);
    this.mPrintMetrics.draw(this);
  }

  public boolean drawImage(Image paramImage, AffineTransform paramAffineTransform, ImageObserver paramImageObserver)
  {
    if (paramImage == null)
      return true;
    this.mDrawingArea.addInfinite();
    this.mPrintMetrics.drawImage(this, paramImage);
    return this.mGraphics.drawImage(paramImage, paramAffineTransform, paramImageObserver);
  }

  public void drawImage(BufferedImage paramBufferedImage, BufferedImageOp paramBufferedImageOp, int paramInt1, int paramInt2)
  {
    if (paramBufferedImage == null)
      return;
    this.mPrintMetrics.drawImage(this, paramBufferedImage);
    this.mDrawingArea.addInfinite();
  }

  public void drawString(String paramString, float paramFloat1, float paramFloat2)
  {
    if (paramString.length() == 0)
      return;
    drawTextLayout(new TextLayout(paramString, getFont(), getFontRenderContext()), paramFloat1, paramFloat2);
  }

  public void drawGlyphVector(GlyphVector paramGlyphVector, float paramFloat1, float paramFloat2)
  {
    Rectangle2D localRectangle2D = paramGlyphVector.getLogicalBounds();
    addDrawingRect(localRectangle2D, paramFloat1, paramFloat2);
    this.mPrintMetrics.drawText(this);
  }

  void drawTextLayout(TextLayout paramTextLayout, float paramFloat1, float paramFloat2)
  {
    addDrawingRect(paramTextLayout.getBounds(), paramFloat1, paramFloat2);
    this.mPrintMetrics.drawText(this, paramTextLayout);
  }

  public void fill(Shape paramShape)
  {
    addDrawingRect(paramShape.getBounds());
    this.mPrintMetrics.fill(this);
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

  public boolean hitsDrawingArea(Rectangle paramRectangle)
  {
    return this.mDrawingArea.intersects((float)paramRectangle.getMinY(), (float)paramRectangle.getMaxY());
  }

  public PeekMetrics getMetrics()
  {
    return this.mPrintMetrics;
  }

  private void addDrawingRect(Rectangle2D paramRectangle2D, float paramFloat1, float paramFloat2)
  {
    addDrawingRect((float)(paramRectangle2D.getX() + paramFloat1), (float)(paramRectangle2D.getY() + paramFloat2), (float)paramRectangle2D.getWidth(), (float)paramRectangle2D.getHeight());
  }

  private void addDrawingRect(float paramFloat1, float paramFloat2, float paramFloat3, float paramFloat4)
  {
    Rectangle2D.Float localFloat = new Rectangle2D.Float(paramFloat1, paramFloat2, paramFloat3, paramFloat4);
    addDrawingRect(localFloat);
  }

  private void addDrawingRect(Rectangle2D paramRectangle2D)
  {
    AffineTransform localAffineTransform = getTransform();
    Shape localShape = localAffineTransform.createTransformedShape(paramRectangle2D);
    Rectangle2D localRectangle2D = localShape.getBounds2D();
    this.mDrawingArea.add((float)localRectangle2D.getMinY(), (float)localRectangle2D.getMaxY());
  }

  private void addStrokeShape(Shape paramShape)
  {
    Shape localShape = getStroke().createStrokedShape(paramShape);
    addDrawingRect(localShape.getBounds2D());
  }

  public synchronized boolean imageUpdate(Image paramImage, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5)
  {
    int i = 0;
    if ((paramInt1 & 0x3) != 0)
    {
      i = 1;
      notify();
    }
    return i;
  }

  private synchronized int getImageWidth(Image paramImage)
  {
    if (paramImage.getWidth(this) == -1)
      try
      {
        wait();
      }
      catch (InterruptedException localInterruptedException)
      {
      }
    return paramImage.getWidth(this);
  }

  private synchronized int getImageHeight(Image paramImage)
  {
    if (paramImage.getHeight(this) == -1)
      try
      {
        wait();
      }
      catch (InterruptedException localInterruptedException)
      {
      }
    return paramImage.getHeight(this);
  }

  protected class ImageWaiter
  implements ImageObserver
  {
    private int mWidth;
    private int mHeight;
    private boolean badImage = false;

    ImageWaiter(, Image paramImage)
    {
      waitForDimensions(paramImage);
    }

    public int getWidth()
    {
      return this.mWidth;
    }

    public int getHeight()
    {
      return this.mHeight;
    }

    private synchronized void waitForDimensions()
    {
      this.mHeight = paramImage.getHeight(this);
      for (this.mWidth = paramImage.getWidth(this); (!(this.badImage)) && (((this.mWidth < 0) || (this.mHeight < 0))); this.mWidth = paramImage.getWidth(this))
      {
        try
        {
          Thread.sleep(50L);
        }
        catch (InterruptedException localInterruptedException)
        {
        }
        this.mHeight = paramImage.getHeight(this);
      }
      if (this.badImage)
      {
        this.mHeight = 0;
        this.mWidth = 0;
      }
    }

    public synchronized boolean imageUpdate(, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5)
    {
      int i = ((paramInt1 & 0xC2) != 0) ? 1 : 0;
      this.badImage = ((paramInt1 & 0xC0) != 0);
      return i;
    }
  }
}