package sun.font;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D.Float;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Float;
import java.util.Map;

public class Decoration
{
  private static final int VALUES_MASK = AttributeValues.getMask(new EAttribute[] { EAttribute.EFOREGROUND, EAttribute.EBACKGROUND, EAttribute.ESWAP_COLORS, EAttribute.ESTRIKETHROUGH, EAttribute.EUNDERLINE, EAttribute.EINPUT_METHOD_HIGHLIGHT, EAttribute.EINPUT_METHOD_UNDERLINE });
  private static final Decoration PLAIN = new Decoration();

  public static Decoration getPlainDecoration()
  {
    return PLAIN;
  }

  public static Decoration getDecoration(AttributeValues paramAttributeValues)
  {
    if ((paramAttributeValues == null) || (!(paramAttributeValues.anyDefined(VALUES_MASK))))
      return PLAIN;
    paramAttributeValues = paramAttributeValues.applyIMHighlight();
    return new DecorationImpl(paramAttributeValues.getForeground(), paramAttributeValues.getBackground(), paramAttributeValues.getSwapColors(), paramAttributeValues.getStrikethrough(), Underline.getUnderline(paramAttributeValues.getUnderline()), Underline.getUnderline(paramAttributeValues.getInputMethodUnderline()));
  }

  public static Decoration getDecoration(Map paramMap)
  {
    if (paramMap == null)
      return PLAIN;
    return getDecoration(AttributeValues.fromMap(paramMap));
  }

  public void drawTextAndDecorations(Label paramLabel, Graphics2D paramGraphics2D, float paramFloat1, float paramFloat2)
  {
    paramLabel.handleDraw(paramGraphics2D, paramFloat1, paramFloat2);
  }

  public Rectangle2D getVisualBounds(Label paramLabel)
  {
    return paramLabel.handleGetVisualBounds();
  }

  public Rectangle2D getCharVisualBounds(Label paramLabel, int paramInt)
  {
    return paramLabel.handleGetCharVisualBounds(paramInt);
  }

  Shape getOutline(Label paramLabel, float paramFloat1, float paramFloat2)
  {
    return paramLabel.handleGetOutline(paramFloat1, paramFloat2);
  }

  private static final class DecorationImpl extends Decoration
  {
    private Paint fgPaint = null;
    private Paint bgPaint = null;
    private boolean swapColors = false;
    private boolean strikethrough = false;
    private Underline stdUnderline = null;
    private Underline imUnderline = null;

    DecorationImpl(Paint paramPaint1, Paint paramPaint2, boolean paramBoolean1, boolean paramBoolean2, Underline paramUnderline1, Underline paramUnderline2)
    {
      super(null);
      this.fgPaint = paramPaint1;
      this.bgPaint = paramPaint2;
      this.swapColors = paramBoolean1;
      this.strikethrough = paramBoolean2;
      this.stdUnderline = paramUnderline1;
      this.imUnderline = paramUnderline2;
    }

    private static boolean areEqual(Object paramObject1, Object paramObject2)
    {
      if (paramObject1 == null)
        return (paramObject2 == null);
      return paramObject1.equals(paramObject2);
    }

    public boolean equals(Object paramObject)
    {
      if (paramObject == this)
        return true;
      if (paramObject == null)
        return false;
      DecorationImpl localDecorationImpl = null;
      try
      {
        localDecorationImpl = (DecorationImpl)paramObject;
      }
      catch (ClassCastException localClassCastException)
      {
        return false;
      }
      if ((this.swapColors != localDecorationImpl.swapColors) || (this.strikethrough != localDecorationImpl.strikethrough))
        return false;
      if (!(areEqual(this.stdUnderline, localDecorationImpl.stdUnderline)))
        return false;
      if (!(areEqual(this.fgPaint, localDecorationImpl.fgPaint)))
        return false;
      if (!(areEqual(this.bgPaint, localDecorationImpl.bgPaint)))
        return false;
      return areEqual(this.imUnderline, localDecorationImpl.imUnderline);
    }

    public int hashCode()
    {
      int i = 1;
      if (this.strikethrough)
        i |= 2;
      if (this.swapColors)
        i |= 4;
      if (this.stdUnderline != null)
        i += this.stdUnderline.hashCode();
      return i;
    }

    private float getUnderlineMaxY(CoreMetrics paramCoreMetrics)
    {
      float f2;
      float f1 = 0F;
      if (this.stdUnderline != null)
      {
        f2 = paramCoreMetrics.underlineOffset;
        f2 += this.stdUnderline.getLowerDrawLimit(paramCoreMetrics.underlineThickness);
        f1 = Math.max(f1, f2);
      }
      if (this.imUnderline != null)
      {
        f2 = paramCoreMetrics.underlineOffset;
        f2 += this.imUnderline.getLowerDrawLimit(paramCoreMetrics.underlineThickness);
        f1 = Math.max(f1, f2);
      }
      return f1;
    }

    private void drawTextAndEmbellishments(Decoration.Label paramLabel, Graphics2D paramGraphics2D, float paramFloat1, float paramFloat2)
    {
      paramLabel.handleDraw(paramGraphics2D, paramFloat1, paramFloat2);
      if ((!(this.strikethrough)) && (this.stdUnderline == null) && (this.imUnderline == null))
        return;
      float f1 = paramFloat1;
      float f2 = f1 + (float)paramLabel.getLogicalBounds().getWidth();
      CoreMetrics localCoreMetrics = paramLabel.getCoreMetrics();
      if (this.strikethrough)
      {
        Stroke localStroke = paramGraphics2D.getStroke();
        paramGraphics2D.setStroke(new BasicStroke(localCoreMetrics.strikethroughThickness));
        f4 = paramFloat2 + localCoreMetrics.strikethroughOffset;
        paramGraphics2D.draw(new Line2D.Float(f1, f4, f2, f4));
        paramGraphics2D.setStroke(localStroke);
      }
      float f3 = localCoreMetrics.underlineOffset;
      float f4 = localCoreMetrics.underlineThickness;
      if (this.stdUnderline != null)
        this.stdUnderline.drawUnderline(paramGraphics2D, f4, f1, f2, paramFloat2 + f3);
      if (this.imUnderline != null)
        this.imUnderline.drawUnderline(paramGraphics2D, f4, f1, f2, paramFloat2 + f3);
    }

    public void drawTextAndDecorations(Decoration.Label paramLabel, Graphics2D paramGraphics2D, float paramFloat1, float paramFloat2)
    {
      if ((this.fgPaint == null) && (this.bgPaint == null) && (!(this.swapColors)))
      {
        drawTextAndEmbellishments(paramLabel, paramGraphics2D, paramFloat1, paramFloat2);
      }
      else
      {
        Object localObject1;
        Paint localPaint2;
        Object localObject2;
        Paint localPaint1 = paramGraphics2D.getPaint();
        if (this.swapColors)
        {
          localPaint2 = (this.fgPaint == null) ? localPaint1 : this.fgPaint;
          if (this.bgPaint == null)
            if (localPaint2 instanceof Color)
            {
              localObject2 = (Color)localPaint2;
              int i = 33 * ((Color)localObject2).getRed() + 53 * ((Color)localObject2).getGreen() + 14 * ((Color)localObject2).getBlue();
              localObject1 = (i > 18500) ? Color.BLACK : Color.WHITE;
            }
            else
            {
              localObject1 = Color.WHITE;
            }
          else
            localObject1 = this.bgPaint;
        }
        else
        {
          localObject1 = (this.fgPaint == null) ? localPaint1 : this.fgPaint;
          localPaint2 = this.bgPaint;
        }
        if (localPaint2 != null)
        {
          localObject2 = paramLabel.getLogicalBounds();
          localObject2 = new Rectangle2D.Float(paramFloat1 + (float)((Rectangle2D)localObject2).getX(), paramFloat2 + (float)((Rectangle2D)localObject2).getY(), (float)((Rectangle2D)localObject2).getWidth(), (float)((Rectangle2D)localObject2).getHeight());
          paramGraphics2D.setPaint(localPaint2);
          paramGraphics2D.fill((Shape)localObject2);
        }
        paramGraphics2D.setPaint((Paint)localObject1);
        drawTextAndEmbellishments(paramLabel, paramGraphics2D, paramFloat1, paramFloat2);
        paramGraphics2D.setPaint(localPaint1);
      }
    }

    public Rectangle2D getVisualBounds(Decoration.Label paramLabel)
    {
      Rectangle2D localRectangle2D1 = paramLabel.handleGetVisualBounds();
      if ((this.swapColors) || (this.bgPaint != null) || (this.stdUnderline != null) || (this.imUnderline != null))
      {
        float f1 = 0F;
        Rectangle2D localRectangle2D2 = paramLabel.getLogicalBounds();
        float f2 = 0F;
        float f3 = 0F;
        if ((this.swapColors) || (this.bgPaint != null))
        {
          f2 = (float)localRectangle2D2.getY();
          f3 = f2 + (float)localRectangle2D2.getHeight();
        }
        f3 = Math.max(f3, getUnderlineMaxY(paramLabel.getCoreMetrics()));
        Rectangle2D.Float localFloat = new Rectangle2D.Float(f1, f2, (float)localRectangle2D2.getWidth(), f3 - f2);
        localRectangle2D1.add(localFloat);
      }
      return localRectangle2D1;
    }

    Shape getOutline(Decoration.Label paramLabel, float paramFloat1, float paramFloat2)
    {
      Object localObject2;
      if ((!(this.strikethrough)) && (this.stdUnderline == null) && (this.imUnderline == null))
        return paramLabel.handleGetOutline(paramFloat1, paramFloat2);
      CoreMetrics localCoreMetrics = paramLabel.getCoreMetrics();
      float f1 = localCoreMetrics.underlineThickness;
      Rectangle2D localRectangle2D = paramLabel.getLogicalBounds();
      float f2 = paramFloat1;
      float f3 = f2 + (float)localRectangle2D.getWidth();
      Object localObject1 = null;
      if (this.stdUnderline != null)
      {
        localObject2 = this.stdUnderline.getUnderlineShape(f1, f2, f3, paramFloat2);
        localObject1 = new Area((Shape)localObject2);
      }
      if (this.strikethrough)
      {
        localObject2 = new BasicStroke(localCoreMetrics.strikethroughThickness);
        float f4 = paramFloat2 + localCoreMetrics.strikethroughOffset;
        Line2D.Float localFloat = new Line2D.Float(f2, f4, f3, f4);
        Area localArea2 = new Area(((Stroke)localObject2).createStrokedShape(localFloat));
        if (localObject1 == null)
          localObject1 = localArea2;
        else
          ((Area)localObject1).add(localArea2);
      }
      if (this.imUnderline != null)
      {
        localObject2 = this.imUnderline.getUnderlineShape(f1, f2, f3, paramFloat2);
        Area localArea1 = new Area((Shape)localObject2);
        if (localObject1 == null)
          localObject1 = localArea1;
        else
          ((Area)localObject1).add(localArea1);
      }
      ((Area)localObject1).add(new Area(paramLabel.handleGetOutline(paramFloat1, paramFloat2)));
      return ((Shape)(Shape)new GeneralPath((Shape)localObject1));
    }

    public String toString()
    {
      StringBuffer localStringBuffer = new StringBuffer();
      localStringBuffer.append(toString());
      localStringBuffer.append("[");
      if (this.fgPaint != null)
        localStringBuffer.append("fgPaint: " + this.fgPaint);
      if (this.bgPaint != null)
        localStringBuffer.append(" bgPaint: " + this.bgPaint);
      if (this.swapColors)
        localStringBuffer.append(" swapColors: true");
      if (this.strikethrough)
        localStringBuffer.append(" strikethrough: true");
      if (this.stdUnderline != null)
        localStringBuffer.append(" stdUnderline: " + this.stdUnderline);
      if (this.imUnderline != null)
        localStringBuffer.append(" imUnderline: " + this.imUnderline);
      localStringBuffer.append("]");
      return localStringBuffer.toString();
    }
  }

  public static abstract interface Label
  {
    public abstract CoreMetrics getCoreMetrics();

    public abstract Rectangle2D getLogicalBounds();

    public abstract void handleDraw(Graphics2D paramGraphics2D, float paramFloat1, float paramFloat2);

    public abstract Rectangle2D handleGetCharVisualBounds(int paramInt);

    public abstract Rectangle2D handleGetVisualBounds();

    public abstract Shape handleGetOutline(float paramFloat1, float paramFloat2);
  }
}