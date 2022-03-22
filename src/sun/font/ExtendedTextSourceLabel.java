package sun.font;

import F;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphJustificationInfo;
import java.awt.font.LineMetrics;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Float;
import java.io.PrintStream;
import java.util.Map;

class ExtendedTextSourceLabel extends ExtendedTextLabel
  implements Decoration.Label
{
  TextSource source;
  private Decoration decorator;
  private Font font;
  private AffineTransform baseTX;
  private CoreMetrics cm;
  Rectangle2D lb;
  Rectangle2D ab;
  Rectangle2D vb;
  Rectangle2D ib;
  StandardGlyphVector gv;
  float[] charinfo;
  private static final int posx = 0;
  private static final int posy = 1;
  private static final int advx = 2;
  private static final int advy = 3;
  private static final int visx = 4;
  private static final int visy = 5;
  private static final int visw = 6;
  private static final int vish = 7;
  private static final int numvals = 8;

  public ExtendedTextSourceLabel(TextSource paramTextSource, Decoration paramDecoration)
  {
    this.source = paramTextSource;
    this.decorator = paramDecoration;
    finishInit();
  }

  public ExtendedTextSourceLabel(TextSource paramTextSource, ExtendedTextSourceLabel paramExtendedTextSourceLabel, int paramInt)
  {
    this.source = paramTextSource;
    this.decorator = paramExtendedTextSourceLabel.decorator;
    finishInit();
  }

  private void finishInit()
  {
    this.font = this.source.getFont();
    Map localMap = this.font.getAttributes();
    this.baseTX = AttributeValues.getBaselineTransform(localMap);
    if (this.baseTX == null)
    {
      this.cm = this.source.getCoreMetrics();
    }
    else
    {
      AffineTransform localAffineTransform = AttributeValues.getCharTransform(localMap);
      if (localAffineTransform == null)
        localAffineTransform = new AffineTransform();
      this.font = this.font.deriveFont(localAffineTransform);
      LineMetrics localLineMetrics = this.font.getLineMetrics(this.source.getChars(), this.source.getStart(), this.source.getStart() + this.source.getLength(), this.source.getFRC());
      this.cm = CoreMetrics.get(localLineMetrics);
    }
  }

  public Rectangle2D getLogicalBounds()
  {
    return getLogicalBounds(0F, 0F);
  }

  public Rectangle2D getLogicalBounds(float paramFloat1, float paramFloat2)
  {
    if (this.lb == null)
      this.lb = createLogicalBounds();
    return new Rectangle2D.Float((float)(this.lb.getX() + paramFloat1), (float)(this.lb.getY() + paramFloat2), (float)this.lb.getWidth(), (float)this.lb.getHeight());
  }

  public float getAdvance()
  {
    if (this.lb == null)
      this.lb = createLogicalBounds();
    return (float)this.lb.getWidth();
  }

  public Rectangle2D getVisualBounds(float paramFloat1, float paramFloat2)
  {
    if (this.vb == null)
      this.vb = this.decorator.getVisualBounds(this);
    return new Rectangle2D.Float((float)(this.vb.getX() + paramFloat1), (float)(this.vb.getY() + paramFloat2), (float)this.vb.getWidth(), (float)this.vb.getHeight());
  }

  public Rectangle2D getAlignBounds(float paramFloat1, float paramFloat2)
  {
    if (this.ab == null)
      this.ab = createAlignBounds();
    return new Rectangle2D.Float((float)(this.ab.getX() + paramFloat1), (float)(this.ab.getY() + paramFloat2), (float)this.ab.getWidth(), (float)this.ab.getHeight());
  }

  public Rectangle2D getItalicBounds(float paramFloat1, float paramFloat2)
  {
    if (this.ib == null)
      this.ib = createItalicBounds();
    return new Rectangle2D.Float((float)(this.ib.getX() + paramFloat1), (float)(this.ib.getY() + paramFloat2), (float)this.ib.getWidth(), (float)this.ib.getHeight());
  }

  public Rectangle getPixelBounds(FontRenderContext paramFontRenderContext, float paramFloat1, float paramFloat2)
  {
    return getGV().getPixelBounds(paramFontRenderContext, paramFloat1, paramFloat2);
  }

  public boolean isSimple()
  {
    return ((this.decorator == Decoration.getPlainDecoration()) && (this.baseTX == null));
  }

  public AffineTransform getBaselineTransform()
  {
    return this.baseTX;
  }

  public Shape handleGetOutline(float paramFloat1, float paramFloat2)
  {
    return getGV().getOutline(paramFloat1, paramFloat2);
  }

  public Shape getOutline(float paramFloat1, float paramFloat2)
  {
    return this.decorator.getOutline(this, paramFloat1, paramFloat2);
  }

  public void handleDraw(Graphics2D paramGraphics2D, float paramFloat1, float paramFloat2)
  {
    paramGraphics2D.drawGlyphVector(getGV(), paramFloat1, paramFloat2);
  }

  public void draw(Graphics2D paramGraphics2D, float paramFloat1, float paramFloat2)
  {
    this.decorator.drawTextAndDecorations(this, paramGraphics2D, paramFloat1, paramFloat2);
  }

  protected Rectangle2D createLogicalBounds()
  {
    return getGV().getLogicalBounds();
  }

  public Rectangle2D handleGetVisualBounds()
  {
    return getGV().getVisualBounds();
  }

  protected Rectangle2D createAlignBounds()
  {
    float[] arrayOfFloat = getCharinfo();
    float f1 = 0F;
    float f2 = -this.cm.ascent;
    float f3 = 0F;
    float f4 = this.cm.ascent + this.cm.descent;
    int i = ((this.source.getLayoutFlags() & 0x8) == 0) ? 1 : 0;
    for (int j = arrayOfFloat.length - 8; (i != 0) && (j > 0) && (arrayOfFloat[(j + 6)] == 0F); j -= 8);
    if (j >= 0)
    {
      for (int k = 0; (k < j) && (((arrayOfFloat[(k + 2)] == 0F) || ((i == 0) && (arrayOfFloat[(k + 6)] == 0F)))); k += 8);
      f1 = Math.max(0F, arrayOfFloat[(k + 0)]);
      f3 = arrayOfFloat[(j + 0)] + arrayOfFloat[(j + 2)] - f1;
    }
    return new Rectangle2D.Float(f1, f2, f3, f4);
  }

  public Rectangle2D createItalicBounds()
  {
    float f1 = this.cm.italicAngle;
    Rectangle2D localRectangle2D = getLogicalBounds();
    float f2 = (float)localRectangle2D.getMinX();
    float f3 = -this.cm.ascent;
    float f4 = (float)localRectangle2D.getMaxX();
    float f5 = this.cm.descent;
    if (f1 != 0F)
      if (f1 > 0F)
      {
        f2 -= f1 * (f5 - this.cm.ssOffset);
        f4 -= f1 * (f3 - this.cm.ssOffset);
      }
      else
      {
        f2 -= f1 * (f3 - this.cm.ssOffset);
        f4 -= f1 * (f5 - this.cm.ssOffset);
      }
    return new Rectangle2D.Float(f2, f3, f4 - f2, f5 - f3);
  }

  private final StandardGlyphVector getGV()
  {
    if (this.gv == null)
      this.gv = createGV();
    return this.gv;
  }

  protected StandardGlyphVector createGV()
  {
    FontRenderContext localFontRenderContext = this.source.getFRC();
    int i = this.source.getLayoutFlags();
    char[] arrayOfChar = this.source.getChars();
    int j = this.source.getStart();
    int k = this.source.getLength();
    GlyphLayout localGlyphLayout = GlyphLayout.get(null);
    this.gv = localGlyphLayout.layout(this.font, localFontRenderContext, arrayOfChar, j, k, i, null);
    GlyphLayout.done(localGlyphLayout);
    return this.gv;
  }

  public int getNumCharacters()
  {
    return this.source.getLength();
  }

  public CoreMetrics getCoreMetrics()
  {
    return this.cm;
  }

  public float getCharX(int paramInt)
  {
    validate(paramInt);
    return getCharinfo()[(l2v(paramInt) * 8 + 0)];
  }

  public float getCharY(int paramInt)
  {
    validate(paramInt);
    return getCharinfo()[(l2v(paramInt) * 8 + 1)];
  }

  public float getCharAdvance(int paramInt)
  {
    validate(paramInt);
    return getCharinfo()[(l2v(paramInt) * 8 + 2)];
  }

  public Rectangle2D handleGetCharVisualBounds(int paramInt)
  {
    validate(paramInt);
    float[] arrayOfFloat = getCharinfo();
    paramInt = l2v(paramInt) * 8;
    return new Rectangle2D.Float(arrayOfFloat[(paramInt + 4)], arrayOfFloat[(paramInt + 5)], arrayOfFloat[(paramInt + 6)], arrayOfFloat[(paramInt + 7)]);
  }

  public Rectangle2D getCharVisualBounds(int paramInt, float paramFloat1, float paramFloat2)
  {
    Rectangle2D localRectangle2D = this.decorator.getCharVisualBounds(this, paramInt);
    if ((paramFloat1 != 0F) || (paramFloat2 != 0F))
      localRectangle2D.setRect(localRectangle2D.getX() + paramFloat1, localRectangle2D.getY() + paramFloat2, localRectangle2D.getWidth(), localRectangle2D.getHeight());
    return localRectangle2D;
  }

  private void validate(int paramInt)
  {
    if (paramInt < 0)
      throw new IllegalArgumentException("index " + paramInt + " < 0");
    if (paramInt >= this.source.getLength())
      throw new IllegalArgumentException("index " + paramInt + " < " + this.source.getLength());
  }

  public int logicalToVisual(int paramInt)
  {
    validate(paramInt);
    return l2v(paramInt);
  }

  public int visualToLogical(int paramInt)
  {
    validate(paramInt);
    return v2l(paramInt);
  }

  public int getLineBreakIndex(int paramInt, float paramFloat)
  {
    float[] arrayOfFloat = getCharinfo();
    int i = this.source.getLength();
    --paramInt;
    while ((paramFloat >= 0F) && (++paramInt < i))
    {
      float f = arrayOfFloat[(l2v(paramInt) * 8 + 2)];
      paramFloat -= f;
    }
    return paramInt;
  }

  public float getAdvanceBetween(int paramInt1, int paramInt2)
  {
    float f = 0F;
    float[] arrayOfFloat = getCharinfo();
    --paramInt1;
    while (++paramInt1 < paramInt2)
      f += arrayOfFloat[(l2v(paramInt1) * 8 + 2)];
    return f;
  }

  public boolean caretAtOffsetIsValid(int paramInt)
  {
    if ((paramInt == 0) || (paramInt == this.source.getLength()))
      return true;
    int i = this.source.getChars()[(this.source.getStart() + paramInt)];
    if ((i == 9) || (i == 10) || (i == 13))
      return true;
    int j = l2v(paramInt);
    return (getCharinfo()[(j * 8 + 2)] != 0F);
  }

  private final float[] getCharinfo()
  {
    if (this.charinfo == null)
      this.charinfo = createCharinfo();
    return this.charinfo;
  }

  protected float[] createCharinfo()
  {
    int i11;
    int i13;
    int i14;
    StandardGlyphVector localStandardGlyphVector = getGV();
    float[] arrayOfFloat = null;
    try
    {
      arrayOfFloat = localStandardGlyphVector.getGlyphInfo();
    }
    catch (Exception localException)
    {
      System.out.println(this.source);
    }
    int i = localStandardGlyphVector.getNumGlyphs();
    int[] arrayOfInt = localStandardGlyphVector.getGlyphCharIndices(0, i, null);
    int j = 0;
    if (j != 0)
    {
      System.err.println("number of glyphs: " + i);
      for (f1 = 0; f1 < i; ++f1)
        System.err.println("g: " + f1 + ", x: " + arrayOfFloat[(f1 * 8 + 0)] + ", a: " + arrayOfFloat[(f1 * 8 + 2)] + ", n: " + arrayOfInt[f1]);
    }
    float f1 = arrayOfInt[0];
    int k = f1;
    int l = 0;
    int i1 = 0;
    int i2 = 0;
    int i3 = 0;
    int i4 = 0;
    int i5 = i;
    int i6 = 8;
    int i7 = 1;
    int i8 = ((this.source.getLayoutFlags() & 0x1) == 0) ? 1 : 0;
    if (i8 == 0)
    {
      f1 = arrayOfInt[(i - 1)];
      k = f1;
      l = 0;
      i1 = arrayOfFloat.length - 8;
      i2 = 0;
      i3 = arrayOfFloat.length - 8;
      i4 = i - 1;
      i5 = -1;
      i6 = -8;
      i7 = -1;
    }
    float f2 = 0F;
    float f3 = 0F;
    float f4 = 0F;
    float f5 = 0F;
    float f6 = 0F;
    float f7 = 0F;
    float f8 = 0F;
    int i9 = 0;
    while (i4 != i5)
    {
      float f9;
      int i10 = 0;
      i11 = 0;
      f1 = arrayOfInt[i4];
      k = f1;
      i4 += i7;
      i3 += i6;
      while ((i4 != i5) && (((arrayOfFloat[(i3 + 2)] == 0F) || (f1 != l) || (arrayOfInt[i4] <= k) || (k - f1 > i11))))
      {
        if (i10 == 0)
        {
          int i12 = i3 - i6;
          f2 = arrayOfFloat[(i12 + 0)];
          f3 = f2 + arrayOfFloat[(i12 + 2)];
          f4 = arrayOfFloat[(i12 + 4)];
          f5 = arrayOfFloat[(i12 + 5)];
          f6 = f4 + arrayOfFloat[(i12 + 6)];
          f7 = f5 + arrayOfFloat[(i12 + 7)];
          i10 = 1;
        }
        ++i11;
        f9 = arrayOfFloat[(i3 + 2)];
        if (f9 != 0F)
        {
          f10 = arrayOfFloat[(i3 + 0)];
          f2 = Math.min(f2, f10);
          f3 = Math.max(f3, f10 + f9);
        }
        float f10 = arrayOfFloat[(i3 + 6)];
        if (f10 != 0F)
        {
          float f11 = arrayOfFloat[(i3 + 4)];
          float f12 = arrayOfFloat[(i3 + 5)];
          f4 = Math.min(f4, f11);
          f5 = Math.min(f5, f12);
          f6 = Math.max(f6, f11 + f10);
          f7 = Math.max(f7, f12 + arrayOfFloat[(i3 + 7)]);
        }
        f1 = Math.min(f1, arrayOfInt[i4]);
        k = Math.max(k, arrayOfInt[i4]);
        i4 += i7;
        i3 += i6;
      }
      if (j != 0)
        System.out.println("minIndex = " + f1 + ", maxIndex = " + k);
      l = k + 1;
      arrayOfFloat[(i1 + 1)] = f8;
      arrayOfFloat[(i1 + 3)] = 0F;
      if (i10 != 0)
      {
        arrayOfFloat[(i1 + 0)] = f2;
        arrayOfFloat[(i1 + 2)] = (f3 - f2);
        arrayOfFloat[(i1 + 4)] = f4;
        arrayOfFloat[(i1 + 5)] = f5;
        arrayOfFloat[(i1 + 6)] = (f6 - f4);
        arrayOfFloat[(i1 + 7)] = (f7 - f5);
        if (k - f1 < i11)
          i9 = 1;
        if (f1 < k)
        {
          if (i8 == 0)
            f3 = f2;
          f6 -= f4;
          f7 -= f5;
          f9 = f1;
          i14 = i1 / 8;
          while (f1 < k)
          {
            ++f1;
            i2 += i7;
            i1 += i6;
            if ((((i1 < 0) || (i1 >= arrayOfFloat.length))) && (j != 0))
              System.out.println("minIndex = " + f9 + ", maxIndex = " + k + ", cp = " + i14);
            arrayOfFloat[(i1 + 0)] = f3;
            arrayOfFloat[(i1 + 1)] = f8;
            arrayOfFloat[(i1 + 2)] = 0F;
            arrayOfFloat[(i1 + 3)] = 0F;
            arrayOfFloat[(i1 + 4)] = f4;
            arrayOfFloat[(i1 + 5)] = f5;
            arrayOfFloat[(i1 + 6)] = f6;
            arrayOfFloat[(i1 + 7)] = f7;
          }
        }
        i10 = 0;
      }
      else if (i9 != 0)
      {
        i13 = i3 - i6;
        arrayOfFloat[(i1 + 0)] = arrayOfFloat[(i13 + 0)];
        arrayOfFloat[(i1 + 2)] = arrayOfFloat[(i13 + 2)];
        arrayOfFloat[(i1 + 4)] = arrayOfFloat[(i13 + 4)];
        arrayOfFloat[(i1 + 5)] = arrayOfFloat[(i13 + 5)];
        arrayOfFloat[(i1 + 6)] = arrayOfFloat[(i13 + 6)];
        arrayOfFloat[(i1 + 7)] = arrayOfFloat[(i13 + 7)];
      }
      i1 += i6;
      i2 += i7;
    }
    if ((i9 != 0) && (i8 == 0))
    {
      i1 -= i6;
      System.arraycopy(arrayOfFloat, i1, arrayOfFloat, 0, arrayOfFloat.length - i1);
    }
    if (j != 0)
    {
      char[] arrayOfChar = this.source.getChars();
      i11 = this.source.getStart();
      i13 = this.source.getLength();
      System.out.println("char info for " + i13 + " characters");
      i14 = 0;
      while (i14 < i13 * 8)
        System.out.println(" ch: " + Integer.toHexString(arrayOfChar[(i11 + v2l(i14 / 8))]) + " x: " + arrayOfFloat[(i14++)] + " y: " + arrayOfFloat[(i14++)] + " xa: " + arrayOfFloat[(i14++)] + " ya: " + arrayOfFloat[(i14++)] + " l: " + arrayOfFloat[(i14++)] + " t: " + arrayOfFloat[(i14++)] + " w: " + arrayOfFloat[(i14++)] + " h: " + arrayOfFloat[(i14++)]);
    }
    return arrayOfFloat;
  }

  protected int l2v(int paramInt)
  {
    return (((this.source.getLayoutFlags() & 0x1) == 0) ? paramInt : this.source.getLength() - 1 - paramInt);
  }

  protected int v2l(int paramInt)
  {
    return (((this.source.getLayoutFlags() & 0x1) == 0) ? paramInt : this.source.getLength() - 1 - paramInt);
  }

  public TextLineComponent getSubset(int paramInt1, int paramInt2, int paramInt3)
  {
    return new ExtendedTextSourceLabel(this.source.getSubSource(paramInt1, paramInt2 - paramInt1, paramInt3), this.decorator);
  }

  public String toString()
  {
    return this.source.toString(false);
  }

  public int getNumJustificationInfos()
  {
    return getGV().getNumGlyphs();
  }

  public void getJustificationInfos(GlyphJustificationInfo[] paramArrayOfGlyphJustificationInfo, int paramInt1, int paramInt2, int paramInt3)
  {
    StandardGlyphVector localStandardGlyphVector = getGV();
    float[] arrayOfFloat = getCharinfo();
    float f = localStandardGlyphVector.getFont().getSize2D();
    GlyphJustificationInfo localGlyphJustificationInfo1 = new GlyphJustificationInfo(0F, false, 3, 0F, 0F, false, 3, 0F, 0F);
    GlyphJustificationInfo localGlyphJustificationInfo2 = new GlyphJustificationInfo(f, true, 1, 0F, f, true, 1, 0F, f / 4.0F);
    GlyphJustificationInfo localGlyphJustificationInfo3 = new GlyphJustificationInfo(f, true, 2, f, f, false, 3, 0F, 0F);
    char[] arrayOfChar = this.source.getChars();
    int i = this.source.getStart();
    int j = localStandardGlyphVector.getNumGlyphs();
    int k = 0;
    int l = j;
    int i1 = ((this.source.getLayoutFlags() & 0x1) == 0) ? 1 : 0;
    if ((paramInt2 != 0) || (paramInt3 != this.source.getLength()))
      if (i1 != 0)
      {
        k = paramInt2;
        l = paramInt3;
      }
      else
      {
        k = j - paramInt3;
        l = j - paramInt2;
      }
    for (int i2 = 0; i2 < j; ++i2)
    {
      GlyphJustificationInfo localGlyphJustificationInfo4 = null;
      if ((i2 >= k) && (i2 < l))
        if (arrayOfFloat[(i2 * 8 + 2)] == 0F)
        {
          localGlyphJustificationInfo4 = localGlyphJustificationInfo1;
        }
        else
        {
          int i3 = v2l(i2);
          int i4 = arrayOfChar[(i + i3)];
          if (Character.isWhitespace(i4))
            localGlyphJustificationInfo4 = localGlyphJustificationInfo2;
          else if (((i4 >= 19968) && (i4 < 40960)) || ((i4 >= 44032) && (i4 < 55216)) || ((i4 >= 63744) && (i4 < 64256)))
            localGlyphJustificationInfo4 = localGlyphJustificationInfo3;
          else
            localGlyphJustificationInfo4 = localGlyphJustificationInfo1;
        }
      paramArrayOfGlyphJustificationInfo[(paramInt1 + i2)] = localGlyphJustificationInfo4;
    }
  }

  public TextLineComponent applyJustificationDeltas(float[] paramArrayOfFloat, int paramInt, boolean[] paramArrayOfBoolean)
  {
    float[] arrayOfFloat1 = (float[])(float[])getCharinfo().clone();
    paramArrayOfBoolean[0] = false;
    StandardGlyphVector localStandardGlyphVector = (StandardGlyphVector)getGV().clone();
    float[] arrayOfFloat2 = localStandardGlyphVector.getGlyphPositions(null);
    int i = localStandardGlyphVector.getNumGlyphs();
    char[] arrayOfChar = this.source.getChars();
    int j = this.source.getStart();
    float f1 = 0F;
    for (int k = 0; k < i; ++k)
      if (Character.isWhitespace(arrayOfChar[(j + v2l(k))]))
      {
        arrayOfFloat2[(k * 2)] += f1;
        float f2 = paramArrayOfFloat[(paramInt + k * 2)] + paramArrayOfFloat[(paramInt + k * 2 + 1)];
        arrayOfFloat1[(k * 8 + 0)] += f1;
        arrayOfFloat1[(k * 8 + 4)] += f1;
        arrayOfFloat1[(k * 8 + 2)] += f2;
        f1 += f2;
      }
      else
      {
        f1 += paramArrayOfFloat[(paramInt + k * 2)];
        arrayOfFloat2[(k * 2)] += f1;
        arrayOfFloat1[(k * 8 + 0)] += f1;
        arrayOfFloat1[(k * 8 + 4)] += f1;
        f1 += paramArrayOfFloat[(paramInt + k * 2 + 1)];
      }
    arrayOfFloat2[(i * 2)] += f1;
    localStandardGlyphVector.setGlyphPositions(arrayOfFloat2);
    ExtendedTextSourceLabel localExtendedTextSourceLabel = new ExtendedTextSourceLabel(this.source, this.decorator);
    localExtendedTextSourceLabel.gv = localStandardGlyphVector;
    localExtendedTextSourceLabel.charinfo = arrayOfFloat1;
    return localExtendedTextSourceLabel;
  }
}