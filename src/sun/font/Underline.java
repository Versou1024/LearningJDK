package sun.font;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.TextAttribute;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D.Float;
import java.util.Hashtable;

abstract class Underline
{
  private static final float DEFAULT_THICKNESS = 1.0F;
  private static final boolean USE_THICKNESS = 1;
  private static final boolean IGNORE_THICKNESS = 0;
  private static final Hashtable UNDERLINES = new Hashtable(6);
  private static final Underline[] UNDERLINE_LIST;

  abstract void drawUnderline(Graphics2D paramGraphics2D, float paramFloat1, float paramFloat2, float paramFloat3, float paramFloat4);

  abstract float getLowerDrawLimit(float paramFloat);

  abstract Shape getUnderlineShape(float paramFloat1, float paramFloat2, float paramFloat3, float paramFloat4);

  static Underline getUnderline(Object paramObject)
  {
    if (paramObject == null)
      return null;
    return ((Underline)UNDERLINES.get(paramObject));
  }

  static Underline getUnderline(int paramInt)
  {
    return ((paramInt < 0) ? null : UNDERLINE_LIST[paramInt]);
  }

  static
  {
    Underline[] arrayOfUnderline = new Underline[6];
    arrayOfUnderline[0] = new StandardUnderline(0F, 1F, null, true);
    UNDERLINES.put(TextAttribute.UNDERLINE_ON, arrayOfUnderline[0]);
    arrayOfUnderline[1] = new StandardUnderline(1F, 1F, null, false);
    UNDERLINES.put(TextAttribute.UNDERLINE_LOW_ONE_PIXEL, arrayOfUnderline[1]);
    arrayOfUnderline[2] = new StandardUnderline(1F, 2F, null, false);
    UNDERLINES.put(TextAttribute.UNDERLINE_LOW_TWO_PIXEL, arrayOfUnderline[2]);
    arrayOfUnderline[3] = new StandardUnderline(1F, 1F, new float[] { 1F, 1F }, false);
    UNDERLINES.put(TextAttribute.UNDERLINE_LOW_DOTTED, arrayOfUnderline[3]);
    arrayOfUnderline[4] = new IMGrayUnderline();
    UNDERLINES.put(TextAttribute.UNDERLINE_LOW_GRAY, arrayOfUnderline[4]);
    arrayOfUnderline[5] = new StandardUnderline(1F, 1F, new float[] { 4.0F, 4.0F }, false);
    UNDERLINES.put(TextAttribute.UNDERLINE_LOW_DASHED, arrayOfUnderline[5]);
    UNDERLINE_LIST = arrayOfUnderline;
  }

  private static class IMGrayUnderline extends Underline
  {
    private BasicStroke stroke = new BasicStroke(1F, 0, 0, 10.0F, new float[] { 1F, 1F }, 0F);

    void drawUnderline(Graphics2D paramGraphics2D, float paramFloat1, float paramFloat2, float paramFloat3, float paramFloat4)
    {
      Stroke localStroke = paramGraphics2D.getStroke();
      paramGraphics2D.setStroke(this.stroke);
      Line2D.Float localFloat = new Line2D.Float(paramFloat2, paramFloat4, paramFloat3, paramFloat4);
      paramGraphics2D.draw(localFloat);
      localFloat.y1 += 1F;
      localFloat.y2 += 1F;
      localFloat.x1 += 1F;
      paramGraphics2D.draw(localFloat);
      paramGraphics2D.setStroke(localStroke);
    }

    float getLowerDrawLimit(float paramFloat)
    {
      return 2F;
    }

    Shape getUnderlineShape(float paramFloat1, float paramFloat2, float paramFloat3, float paramFloat4)
    {
      GeneralPath localGeneralPath = new GeneralPath();
      Line2D.Float localFloat = new Line2D.Float(paramFloat2, paramFloat4, paramFloat3, paramFloat4);
      localGeneralPath.append(this.stroke.createStrokedShape(localFloat), false);
      localFloat.y1 += 1F;
      localFloat.y2 += 1F;
      localFloat.x1 += 1F;
      localGeneralPath.append(this.stroke.createStrokedShape(localFloat), false);
      return localGeneralPath;
    }
  }

  private static final class StandardUnderline extends Underline
  {
    private float shift;
    private float thicknessMultiplier;
    private float[] dashPattern;
    private boolean useThickness;
    private BasicStroke cachedStroke;

    StandardUnderline(float paramFloat1, float paramFloat2, float[] paramArrayOfFloat, boolean paramBoolean)
    {
      this.shift = paramFloat1;
      this.thicknessMultiplier = paramFloat2;
      this.dashPattern = paramArrayOfFloat;
      this.useThickness = paramBoolean;
      this.cachedStroke = null;
    }

    private BasicStroke createStroke(float paramFloat)
    {
      if (this.dashPattern == null)
        return new BasicStroke(paramFloat);
      return new BasicStroke(paramFloat, 0, 0, 10.0F, this.dashPattern, 0F);
    }

    private float getLineThickness(float paramFloat)
    {
      if (this.useThickness)
        return (paramFloat * this.thicknessMultiplier);
      return (1F * this.thicknessMultiplier);
    }

    private Stroke getStroke(float paramFloat)
    {
      float f = getLineThickness(paramFloat);
      BasicStroke localBasicStroke = this.cachedStroke;
      if ((localBasicStroke == null) || (localBasicStroke.getLineWidth() != f))
      {
        localBasicStroke = createStroke(f);
        this.cachedStroke = localBasicStroke;
      }
      return localBasicStroke;
    }

    void drawUnderline(Graphics2D paramGraphics2D, float paramFloat1, float paramFloat2, float paramFloat3, float paramFloat4)
    {
      Stroke localStroke = paramGraphics2D.getStroke();
      paramGraphics2D.setStroke(getStroke(paramFloat1));
      paramGraphics2D.draw(new Line2D.Float(paramFloat2, paramFloat4 + this.shift, paramFloat3, paramFloat4 + this.shift));
      paramGraphics2D.setStroke(localStroke);
    }

    float getLowerDrawLimit(float paramFloat)
    {
      return (this.shift + getLineThickness(paramFloat));
    }

    Shape getUnderlineShape(float paramFloat1, float paramFloat2, float paramFloat3, float paramFloat4)
    {
      Stroke localStroke = getStroke(paramFloat1);
      Line2D.Float localFloat = new Line2D.Float(paramFloat2, paramFloat4 + this.shift, paramFloat3, paramFloat4 + this.shift);
      return localStroke.createStrokedShape(localFloat);
    }
  }
}