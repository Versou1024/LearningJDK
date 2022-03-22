package sun.font;

import D;
import F;
import I;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphJustificationInfo;
import java.awt.font.GlyphMetrics;
import java.awt.font.GlyphVector;
import java.awt.font.LineMetrics;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Float;
import java.lang.ref.SoftReference;
import java.text.CharacterIterator;
import sun.java2d.loops.FontInfo;

public class StandardGlyphVector extends GlyphVector
{
  private Font font;
  private FontRenderContext frc;
  private int[] glyphs;
  private int[] userGlyphs;
  private float[] positions;
  private int[] charIndices;
  private int flags;
  private static final int UNINITIALIZED_FLAGS = -1;
  private GlyphTransformInfo gti;
  private AffineTransform ftx;
  private AffineTransform dtx;
  private AffineTransform invdtx;
  private AffineTransform frctx;
  private Font2D font2D;
  private SoftReference fsref;
  private SoftReference lbcacheRef;
  private SoftReference vbcacheRef;
  public static final int FLAG_USES_VERTICAL_BASELINE = 128;
  public static final int FLAG_USES_VERTICAL_METRICS = 256;
  public static final int FLAG_USES_ALTERNATE_ORIENTATION = 512;

  public StandardGlyphVector(Font paramFont, String paramString, FontRenderContext paramFontRenderContext)
  {
    init(paramFont, paramString.toCharArray(), 0, paramString.length(), paramFontRenderContext, -1);
  }

  public StandardGlyphVector(Font paramFont, char[] paramArrayOfChar, FontRenderContext paramFontRenderContext)
  {
    init(paramFont, paramArrayOfChar, 0, paramArrayOfChar.length, paramFontRenderContext, -1);
  }

  public StandardGlyphVector(Font paramFont, char[] paramArrayOfChar, int paramInt1, int paramInt2, FontRenderContext paramFontRenderContext)
  {
    init(paramFont, paramArrayOfChar, paramInt1, paramInt2, paramFontRenderContext, -1);
  }

  private float getTracking(Font paramFont)
  {
    if (paramFont.hasLayoutAttributes())
    {
      AttributeValues localAttributeValues = ((AttributeMap)paramFont.getAttributes()).getValues();
      return localAttributeValues.getTracking();
    }
    return 0F;
  }

  public StandardGlyphVector(Font paramFont, FontRenderContext paramFontRenderContext, int[] paramArrayOfInt1, float[] paramArrayOfFloat, int[] paramArrayOfInt2, int paramInt)
  {
    initGlyphVector(paramFont, paramFontRenderContext, paramArrayOfInt1, paramArrayOfFloat, paramArrayOfInt2, paramInt);
    float f1 = getTracking(paramFont);
    if (f1 != 0F)
    {
      f1 *= paramFont.getSize2D();
      java.awt.geom.Point2D.Float localFloat = new java.awt.geom.Point2D.Float(f1, 0F);
      if (paramFont.isTransformed())
      {
        localObject = paramFont.getTransform();
        ((AffineTransform)localObject).deltaTransform(localFloat, localFloat);
      }
      Object localObject = FontManager.getFont2D(paramFont);
      FontStrike localFontStrike = ((Font2D)localObject).getStrike(paramFont, paramFontRenderContext);
      float[] arrayOfFloat = { localFloat.x, localFloat.y };
      for (int i = 0; i < arrayOfFloat.length; ++i)
      {
        float f2 = arrayOfFloat[i];
        if (f2 != 0F)
        {
          float f3 = 0F;
          int j = i;
          int k = 0;
          while (k < paramArrayOfInt1.length)
          {
            if (localFontStrike.getGlyphAdvance(paramArrayOfInt1[(k++)]) != 0F)
            {
              paramArrayOfFloat[j] += f3;
              f3 += f2;
            }
            j += 2;
          }
          paramArrayOfFloat[(paramArrayOfFloat.length - 2 + i)] += f3;
        }
      }
    }
  }

  public void initGlyphVector(Font paramFont, FontRenderContext paramFontRenderContext, int[] paramArrayOfInt1, float[] paramArrayOfFloat, int[] paramArrayOfInt2, int paramInt)
  {
    this.font = paramFont;
    this.frc = paramFontRenderContext;
    this.glyphs = paramArrayOfInt1;
    this.userGlyphs = paramArrayOfInt1;
    this.positions = paramArrayOfFloat;
    this.charIndices = paramArrayOfInt2;
    this.flags = paramInt;
    initFontData();
  }

  public StandardGlyphVector(Font paramFont, CharacterIterator paramCharacterIterator, FontRenderContext paramFontRenderContext)
  {
    int i = paramCharacterIterator.getBeginIndex();
    char[] arrayOfChar = new char[paramCharacterIterator.getEndIndex() - i];
    for (int j = paramCharacterIterator.first(); j != 65535; j = paramCharacterIterator.next())
      arrayOfChar[(paramCharacterIterator.getIndex() - i)] = j;
    init(paramFont, arrayOfChar, 0, arrayOfChar.length, paramFontRenderContext, -1);
  }

  public StandardGlyphVector(Font paramFont, int[] paramArrayOfInt, FontRenderContext paramFontRenderContext)
  {
    this.font = paramFont;
    this.frc = paramFontRenderContext;
    this.flags = -1;
    initFontData();
    this.userGlyphs = paramArrayOfInt;
    this.glyphs = getValidatedGlyphs(this.userGlyphs);
  }

  public static StandardGlyphVector getStandardGV(GlyphVector paramGlyphVector, FontInfo paramFontInfo)
  {
    if (paramFontInfo.aaHint == 2)
    {
      Object localObject = paramGlyphVector.getFontRenderContext().getAntiAliasingHint();
      if ((localObject != RenderingHints.VALUE_TEXT_ANTIALIAS_ON) && (localObject != RenderingHints.VALUE_TEXT_ANTIALIAS_GASP))
      {
        FontRenderContext localFontRenderContext = paramGlyphVector.getFontRenderContext();
        localFontRenderContext = new FontRenderContext(localFontRenderContext.getTransform(), RenderingHints.VALUE_TEXT_ANTIALIAS_ON, localFontRenderContext.getFractionalMetricsHint());
        return new StandardGlyphVector(paramGlyphVector, localFontRenderContext);
      }
    }
    if (paramGlyphVector instanceof StandardGlyphVector)
      return ((StandardGlyphVector)paramGlyphVector);
    return new StandardGlyphVector(paramGlyphVector, paramGlyphVector.getFontRenderContext());
  }

  public Font getFont()
  {
    return this.font;
  }

  public FontRenderContext getFontRenderContext()
  {
    return this.frc;
  }

  public void performDefaultLayout()
  {
    this.positions = null;
    if (getTracking(this.font) == 0F)
      clearFlags(2);
  }

  public int getNumGlyphs()
  {
    return this.glyphs.length;
  }

  public int getGlyphCode(int paramInt)
  {
    return this.userGlyphs[paramInt];
  }

  public int[] getGlyphCodes(int paramInt1, int paramInt2, int[] paramArrayOfInt)
  {
    if (paramInt2 < 0)
      throw new IllegalArgumentException("count = " + paramInt2);
    if (paramInt1 < 0)
      throw new IndexOutOfBoundsException("start = " + paramInt1);
    if (paramInt1 > this.glyphs.length - paramInt2)
      throw new IndexOutOfBoundsException("start + count = " + (paramInt1 + paramInt2));
    if (paramArrayOfInt == null)
      paramArrayOfInt = new int[paramInt2];
    for (int i = 0; i < paramInt2; ++i)
      paramArrayOfInt[i] = this.userGlyphs[(i + paramInt1)];
    return paramArrayOfInt;
  }

  public int getGlyphCharIndex(int paramInt)
  {
    if ((paramInt < 0) && (paramInt >= this.glyphs.length))
      throw new IndexOutOfBoundsException("" + paramInt);
    if (this.charIndices == null)
    {
      if ((getLayoutFlags() & 0x4) != 0)
        return (this.glyphs.length - 1 - paramInt);
      return paramInt;
    }
    return this.charIndices[paramInt];
  }

  public int[] getGlyphCharIndices(int paramInt1, int paramInt2, int[] paramArrayOfInt)
  {
    if ((paramInt1 < 0) || (paramInt2 < 0) || (paramInt2 > this.glyphs.length - paramInt1))
      throw new IndexOutOfBoundsException("" + paramInt1 + ", " + paramInt2);
    if (paramArrayOfInt == null)
      paramArrayOfInt = new int[paramInt2];
    if (this.charIndices == null)
    {
      int l;
      if ((getLayoutFlags() & 0x4) != 0)
      {
        int i = 0;
        for (l = this.glyphs.length - 1 - paramInt1; i < paramInt2; --l)
        {
          paramArrayOfInt[i] = l;
          ++i;
        }
      }
      else
      {
        int j = 0;
        for (l = paramInt1; j < paramInt2; ++l)
        {
          paramArrayOfInt[j] = l;
          ++j;
        }
      }
    }
    else
    {
      for (int k = 0; k < paramInt2; ++k)
        paramArrayOfInt[k] = this.charIndices[(k + paramInt1)];
    }
    return paramArrayOfInt;
  }

  public Rectangle2D getLogicalBounds()
  {
    setFRCTX();
    initPositions();
    LineMetrics localLineMetrics = this.font.getLineMetrics("", this.frc);
    float f1 = 0F;
    float f2 = -localLineMetrics.getAscent();
    float f3 = 0F;
    float f4 = localLineMetrics.getDescent() + localLineMetrics.getLeading();
    if (this.glyphs.length > 0)
      f3 = this.positions[(this.positions.length - 2)];
    return new Rectangle2D.Float(f1, f2, f3 - f1, f4 - f2);
  }

  public Rectangle2D getVisualBounds()
  {
    if (this.glyphs.length == 0)
      return new Rectangle2D.Float(0F, 0F, 0F, 0F);
    Rectangle2D localRectangle2D = getGlyphVisualBounds(0).getBounds2D();
    for (int i = 1; i < this.glyphs.length; ++i)
      Rectangle2D.union(localRectangle2D, getGlyphVisualBounds(i).getBounds2D(), localRectangle2D);
    return localRectangle2D;
  }

  public Rectangle getPixelBounds(FontRenderContext paramFontRenderContext, float paramFloat1, float paramFloat2)
  {
    return getGlyphsPixelBounds(paramFontRenderContext, paramFloat1, paramFloat2, 0, this.glyphs.length);
  }

  public Shape getOutline()
  {
    return getGlyphsOutline(0, this.glyphs.length, 0F, 0F);
  }

  public Shape getOutline(float paramFloat1, float paramFloat2)
  {
    return getGlyphsOutline(0, this.glyphs.length, paramFloat1, paramFloat2);
  }

  public Shape getGlyphOutline(int paramInt)
  {
    return getGlyphsOutline(paramInt, 1, 0F, 0F);
  }

  public Shape getGlyphOutline(int paramInt, float paramFloat1, float paramFloat2)
  {
    return getGlyphsOutline(paramInt, 1, paramFloat1, paramFloat2);
  }

  public Point2D getGlyphPosition(int paramInt)
  {
    initPositions();
    paramInt *= 2;
    return new java.awt.geom.Point2D.Float(this.positions[paramInt], this.positions[(paramInt + 1)]);
  }

  public void setGlyphPosition(int paramInt, Point2D paramPoint2D)
  {
    initPositions();
    int i = paramInt << 1;
    this.positions[i] = (float)paramPoint2D.getX();
    this.positions[(i + 1)] = (float)paramPoint2D.getY();
    clearCaches(paramInt);
    addFlags(2);
  }

  public AffineTransform getGlyphTransform(int paramInt)
  {
    if ((paramInt < 0) || (paramInt >= this.glyphs.length))
      throw new IndexOutOfBoundsException("ix = " + paramInt);
    if (this.gti != null)
      return this.gti.getGlyphTransform(paramInt);
    return null;
  }

  public void setGlyphTransform(int paramInt, AffineTransform paramAffineTransform)
  {
    if ((paramInt < 0) || (paramInt >= this.glyphs.length))
      throw new IndexOutOfBoundsException("ix = " + paramInt);
    if (this.gti == null)
    {
      if ((paramAffineTransform == null) || (paramAffineTransform.isIdentity()))
        return;
      this.gti = new GlyphTransformInfo(this);
    }
    this.gti.setGlyphTransform(paramInt, paramAffineTransform);
    if (this.gti.transformCount() == 0)
      this.gti = null;
  }

  public int getLayoutFlags()
  {
    if (this.flags == -1)
    {
      this.flags = 0;
      if ((this.charIndices != null) && (this.glyphs.length > 1))
      {
        int i = 1;
        int j = 1;
        int k = this.charIndices.length;
        for (int l = 0; (l < this.charIndices.length) && (((i != 0) || (j != 0))); ++l)
        {
          int i1 = this.charIndices[l];
          i = ((i != 0) && (i1 == l)) ? 1 : 0;
          j = ((j != 0) && (i1 == --k)) ? 1 : 0;
        }
        if (j != 0)
          this.flags |= 4;
        if ((j == 0) && (i == 0))
          this.flags |= 8;
      }
    }
    return this.flags;
  }

  public float[] getGlyphPositions(int paramInt1, int paramInt2, float[] paramArrayOfFloat)
  {
    if (paramInt2 < 0)
      throw new IllegalArgumentException("count = " + paramInt2);
    if (paramInt1 < 0)
      throw new IndexOutOfBoundsException("start = " + paramInt1);
    if (paramInt1 > this.glyphs.length + 1 - paramInt2)
      throw new IndexOutOfBoundsException("start + count = " + (paramInt1 + paramInt2));
    return internalGetGlyphPositions(paramInt1, paramInt2, 0, paramArrayOfFloat);
  }

  public Shape getGlyphLogicalBounds(int paramInt)
  {
    if ((paramInt < 0) || (paramInt >= this.glyphs.length))
      throw new IndexOutOfBoundsException("ix = " + paramInt);
    if (this.lbcacheRef != null)
      if ((arrayOfShape = (Shape[])(Shape[])this.lbcacheRef.get()) != null)
        break label86;
    Shape[] arrayOfShape = new Shape[this.glyphs.length];
    this.lbcacheRef = new SoftReference(arrayOfShape);
    label86: Object localObject = arrayOfShape[paramInt];
    if (localObject == null)
    {
      setFRCTX();
      initPositions();
      ADL localADL = new ADL();
      GlyphStrike localGlyphStrike = getGlyphStrike(paramInt);
      localGlyphStrike.getADL(localADL);
      java.awt.geom.Point2D.Float localFloat = localGlyphStrike.strike.getGlyphMetrics(this.glyphs[paramInt]);
      float f1 = localFloat.x;
      float f2 = localFloat.y;
      float f3 = localADL.descentX + localADL.leadingX + localADL.ascentX;
      float f4 = localADL.descentY + localADL.leadingY + localADL.ascentY;
      float f5 = this.positions[(paramInt * 2)] + localGlyphStrike.dx - localADL.ascentX;
      float f6 = this.positions[(paramInt * 2 + 1)] + localGlyphStrike.dy - localADL.ascentY;
      GeneralPath localGeneralPath = new GeneralPath();
      localGeneralPath.moveTo(f5, f6);
      localGeneralPath.lineTo(f5 + f1, f6 + f2);
      localGeneralPath.lineTo(f5 + f1 + f3, f6 + f2 + f4);
      localGeneralPath.lineTo(f5 + f3, f6 + f4);
      localGeneralPath.closePath();
      localObject = new DelegatingShape(localGeneralPath);
      arrayOfShape[paramInt] = localObject;
    }
    return ((Shape)localObject);
  }

  public Shape getGlyphVisualBounds(int paramInt)
  {
    if ((paramInt < 0) || (paramInt >= this.glyphs.length))
      throw new IndexOutOfBoundsException("ix = " + paramInt);
    if (this.vbcacheRef != null)
      if ((arrayOfShape = (Shape[])(Shape[])this.vbcacheRef.get()) != null)
        break label86;
    Shape[] arrayOfShape = new Shape[this.glyphs.length];
    this.vbcacheRef = new SoftReference(arrayOfShape);
    label86: Object localObject = arrayOfShape[paramInt];
    if (localObject == null)
    {
      localObject = new DelegatingShape(getGlyphOutlineBounds(paramInt));
      arrayOfShape[paramInt] = localObject;
    }
    return ((Shape)localObject);
  }

  public Rectangle getGlyphPixelBounds(int paramInt, FontRenderContext paramFontRenderContext, float paramFloat1, float paramFloat2)
  {
    return getGlyphsPixelBounds(paramFontRenderContext, paramFloat1, paramFloat2, paramInt, 1);
  }

  public GlyphMetrics getGlyphMetrics(int paramInt)
  {
    if ((paramInt < 0) || (paramInt >= this.glyphs.length))
      throw new IndexOutOfBoundsException("ix = " + paramInt);
    Rectangle2D localRectangle2D = getGlyphVisualBounds(paramInt).getBounds2D();
    Point2D localPoint2D = getGlyphPosition(paramInt);
    localRectangle2D.setRect(localRectangle2D.getMinX() - localPoint2D.getX(), localRectangle2D.getMinY() - localPoint2D.getY(), localRectangle2D.getWidth(), localRectangle2D.getHeight());
    java.awt.geom.Point2D.Float localFloat = getGlyphStrike(paramInt).strike.getGlyphMetrics(this.glyphs[paramInt]);
    GlyphMetrics localGlyphMetrics = new GlyphMetrics(true, localFloat.x, localFloat.y, localRectangle2D, 0);
    return localGlyphMetrics;
  }

  public GlyphJustificationInfo getGlyphJustificationInfo(int paramInt)
  {
    if ((paramInt < 0) || (paramInt >= this.glyphs.length))
      throw new IndexOutOfBoundsException("ix = " + paramInt);
    return null;
  }

  public boolean equals(GlyphVector paramGlyphVector)
  {
    if (this == paramGlyphVector)
      return true;
    if (paramGlyphVector == null)
      return false;
    try
    {
      StandardGlyphVector localStandardGlyphVector = (StandardGlyphVector)paramGlyphVector;
      if (this.glyphs.length != localStandardGlyphVector.glyphs.length)
        return false;
      for (int i = 0; i < this.glyphs.length; ++i)
        if (this.glyphs[i] != localStandardGlyphVector.glyphs[i])
          return false;
      if (!(this.font.equals(localStandardGlyphVector.font)))
        return false;
      if (!(this.frc.equals(localStandardGlyphVector.frc)))
        return false;
      if (((localStandardGlyphVector.positions == null) ? 1 : 0) != ((this.positions == null) ? 1 : 0))
        if (this.positions == null)
          initPositions();
        else
          localStandardGlyphVector.initPositions();
      if (this.positions != null)
        for (i = 0; i < this.positions.length; ++i)
          if (this.positions[i] != localStandardGlyphVector.positions[i])
            return false;
      if (this.gti == null)
        return (localStandardGlyphVector.gti == null);
      return this.gti.equals(localStandardGlyphVector.gti);
    }
    catch (ClassCastException localClassCastException)
    {
    }
    return false;
  }

  public int hashCode()
  {
    return (this.font.hashCode() ^ this.glyphs.length);
  }

  public boolean equals(Object paramObject)
  {
    try
    {
      return equals((GlyphVector)paramObject);
    }
    catch (ClassCastException localClassCastException)
    {
    }
    return false;
  }

  public StandardGlyphVector copy()
  {
    return ((StandardGlyphVector)clone());
  }

  public Object clone()
  {
    StandardGlyphVector localStandardGlyphVector;
    try
    {
      localStandardGlyphVector = (StandardGlyphVector)clone();
      localStandardGlyphVector.clearCaches();
      if (this.positions != null)
        localStandardGlyphVector.positions = ((float[])(float[])this.positions.clone());
      if (this.gti != null)
        localStandardGlyphVector.gti = new GlyphTransformInfo(localStandardGlyphVector, this.gti);
      return localStandardGlyphVector;
    }
    catch (CloneNotSupportedException localCloneNotSupportedException)
    {
    }
    return this;
  }

  public void setGlyphPositions(float[] paramArrayOfFloat, int paramInt1, int paramInt2, int paramInt3)
  {
    if (paramInt3 < 0)
      throw new IllegalArgumentException("count = " + paramInt3);
    initPositions();
    int i = paramInt2 * 2;
    int j = i + paramInt3 * 2;
    for (int k = paramInt1; i < j; ++k)
    {
      this.positions[i] = paramArrayOfFloat[k];
      ++i;
    }
    clearCaches();
    addFlags(2);
  }

  public void setGlyphPositions(float[] paramArrayOfFloat)
  {
    int i = this.glyphs.length * 2 + 2;
    if (paramArrayOfFloat.length != i)
      throw new IllegalArgumentException("srcPositions.length != " + i);
    this.positions = ((float[])(float[])paramArrayOfFloat.clone());
    clearCaches();
    addFlags(2);
  }

  public float[] getGlyphPositions(float[] paramArrayOfFloat)
  {
    return internalGetGlyphPositions(0, this.glyphs.length + 1, 0, paramArrayOfFloat);
  }

  public AffineTransform[] getGlyphTransforms(int paramInt1, int paramInt2, AffineTransform[] paramArrayOfAffineTransform)
  {
    if ((paramInt1 < 0) || (paramInt2 < 0) || (paramInt1 + paramInt2 > this.glyphs.length))
      throw new IllegalArgumentException("start: " + paramInt1 + " count: " + paramInt2);
    if (this.gti == null)
      return null;
    if (paramArrayOfAffineTransform == null)
      paramArrayOfAffineTransform = new AffineTransform[paramInt2];
    int i = 0;
    while (i < paramInt2)
    {
      paramArrayOfAffineTransform[i] = this.gti.getGlyphTransform(paramInt1);
      ++i;
      ++paramInt1;
    }
    return paramArrayOfAffineTransform;
  }

  public AffineTransform[] getGlyphTransforms()
  {
    return getGlyphTransforms(0, this.glyphs.length, null);
  }

  public void setGlyphTransforms(AffineTransform[] paramArrayOfAffineTransform, int paramInt1, int paramInt2, int paramInt3)
  {
    int i = paramInt2;
    int j = paramInt2 + paramInt3;
    while (i < j)
    {
      setGlyphTransform(i, paramArrayOfAffineTransform[(paramInt1 + i)]);
      ++i;
    }
  }

  public void setGlyphTransforms(AffineTransform[] paramArrayOfAffineTransform)
  {
    setGlyphTransforms(paramArrayOfAffineTransform, 0, 0, this.glyphs.length);
  }

  public float[] getGlyphInfo()
  {
    setFRCTX();
    initPositions();
    float[] arrayOfFloat = new float[this.glyphs.length * 8];
    int i = 0;
    for (int j = 0; i < this.glyphs.length; j += 8)
    {
      float f1 = this.positions[(i * 2)];
      float f2 = this.positions[(i * 2 + 1)];
      arrayOfFloat[j] = f1;
      arrayOfFloat[(j + 1)] = f2;
      int k = this.glyphs[i];
      GlyphStrike localGlyphStrike = getGlyphStrike(i);
      java.awt.geom.Point2D.Float localFloat = localGlyphStrike.strike.getGlyphMetrics(k);
      arrayOfFloat[(j + 2)] = localFloat.x;
      arrayOfFloat[(j + 3)] = localFloat.y;
      Rectangle2D localRectangle2D = getGlyphVisualBounds(i).getBounds2D();
      arrayOfFloat[(j + 4)] = (float)localRectangle2D.getMinX();
      arrayOfFloat[(j + 5)] = (float)localRectangle2D.getMinY();
      arrayOfFloat[(j + 6)] = (float)localRectangle2D.getWidth();
      arrayOfFloat[(j + 7)] = (float)localRectangle2D.getHeight();
      ++i;
    }
    return arrayOfFloat;
  }

  public void pixellate(FontRenderContext paramFontRenderContext, Point2D paramPoint2D, Point paramPoint)
  {
    if (paramFontRenderContext == null)
      paramFontRenderContext = this.frc;
    AffineTransform localAffineTransform = paramFontRenderContext.getTransform();
    localAffineTransform.transform(paramPoint2D, paramPoint2D);
    paramPoint.x = (int)paramPoint2D.getX();
    paramPoint.y = (int)paramPoint2D.getY();
    paramPoint2D.setLocation(paramPoint.x, paramPoint.y);
    try
    {
      localAffineTransform.inverseTransform(paramPoint2D, paramPoint2D);
    }
    catch (NoninvertibleTransformException localNoninvertibleTransformException)
    {
      throw new IllegalArgumentException("must be able to invert frc transform");
    }
  }

  boolean needsPositions(double[] paramArrayOfDouble)
  {
    return ((this.gti != null) || ((getLayoutFlags() & 0x2) != 0) || (!(matchTX(paramArrayOfDouble, this.frctx))));
  }

  Object setupGlyphImages(long[] paramArrayOfLong, float[] paramArrayOfFloat, double[] paramArrayOfDouble)
  {
    initPositions();
    setRenderTransform(paramArrayOfDouble);
    if (this.gti != null)
      return this.gti.setupGlyphImages(paramArrayOfLong, paramArrayOfFloat, this.dtx);
    GlyphStrike localGlyphStrike = getDefaultStrike();
    localGlyphStrike.strike.getGlyphImagePtrs(this.glyphs, paramArrayOfLong, this.glyphs.length);
    if (paramArrayOfFloat != null)
      if (this.dtx.isIdentity())
        System.arraycopy(this.positions, 0, paramArrayOfFloat, 0, this.glyphs.length * 2);
      else
        this.dtx.transform(this.positions, 0, paramArrayOfFloat, 0, this.glyphs.length);
    return localGlyphStrike;
  }

  private static boolean matchTX(double[] paramArrayOfDouble, AffineTransform paramAffineTransform)
  {
    return ((paramArrayOfDouble[0] == paramAffineTransform.getScaleX()) && (paramArrayOfDouble[1] == paramAffineTransform.getShearY()) && (paramArrayOfDouble[2] == paramAffineTransform.getShearX()) && (paramArrayOfDouble[3] == paramAffineTransform.getScaleY()));
  }

  private static AffineTransform getNonTranslateTX(AffineTransform paramAffineTransform)
  {
    if ((paramAffineTransform.getTranslateX() != 0D) || (paramAffineTransform.getTranslateY() != 0D))
      paramAffineTransform = new AffineTransform(paramAffineTransform.getScaleX(), paramAffineTransform.getShearY(), paramAffineTransform.getShearX(), paramAffineTransform.getScaleY(), 0D, 0D);
    return paramAffineTransform;
  }

  private static boolean equalNonTranslateTX(AffineTransform paramAffineTransform1, AffineTransform paramAffineTransform2)
  {
    return ((paramAffineTransform1.getScaleX() == paramAffineTransform2.getScaleX()) && (paramAffineTransform1.getShearY() == paramAffineTransform2.getShearY()) && (paramAffineTransform1.getShearX() == paramAffineTransform2.getShearX()) && (paramAffineTransform1.getScaleY() == paramAffineTransform2.getScaleY()));
  }

  private void setRenderTransform(double[] paramArrayOfDouble)
  {
    if ((!($assertionsDisabled)) && (paramArrayOfDouble.length != 4))
      throw new AssertionError();
    if (!(matchTX(paramArrayOfDouble, this.dtx)))
      resetDTX(new AffineTransform(paramArrayOfDouble));
  }

  private final void setDTX(AffineTransform paramAffineTransform)
  {
    if (!(equalNonTranslateTX(this.dtx, paramAffineTransform)))
      resetDTX(getNonTranslateTX(paramAffineTransform));
  }

  private final void setFRCTX()
  {
    if (!(equalNonTranslateTX(this.frctx, this.dtx)))
      resetDTX(getNonTranslateTX(this.frctx));
  }

  private final void resetDTX(AffineTransform paramAffineTransform)
  {
    this.fsref = null;
    this.dtx = paramAffineTransform;
    this.invdtx = null;
    if (!(this.dtx.isIdentity()))
      try
      {
        this.invdtx = this.dtx.createInverse();
      }
      catch (NoninvertibleTransformException localNoninvertibleTransformException)
      {
      }
    if (this.gti != null)
      this.gti.strikesRef = null;
  }

  private StandardGlyphVector(GlyphVector paramGlyphVector, FontRenderContext paramFontRenderContext)
  {
    this.font = paramGlyphVector.getFont();
    this.frc = paramFontRenderContext;
    initFontData();
    int i = paramGlyphVector.getNumGlyphs();
    this.userGlyphs = paramGlyphVector.getGlyphCodes(0, i, null);
    if (paramGlyphVector instanceof StandardGlyphVector)
      this.glyphs = this.userGlyphs;
    else
      this.glyphs = getValidatedGlyphs(this.userGlyphs);
    this.flags = (paramGlyphVector.getLayoutFlags() & 0xF);
    if ((this.flags & 0x2) != 0)
      this.positions = paramGlyphVector.getGlyphPositions(0, i + 1, null);
    if ((this.flags & 0x8) != 0)
      this.charIndices = paramGlyphVector.getGlyphCharIndices(0, i, null);
    if ((this.flags & 0x1) != 0)
    {
      AffineTransform[] arrayOfAffineTransform = new AffineTransform[i];
      for (int j = 0; j < i; ++j)
        arrayOfAffineTransform[j] = paramGlyphVector.getGlyphTransform(j);
      setGlyphTransforms(arrayOfAffineTransform);
    }
  }

  int[] getValidatedGlyphs(int[] paramArrayOfInt)
  {
    int i = paramArrayOfInt.length;
    int[] arrayOfInt = new int[i];
    for (int j = 0; j < i; ++j)
      if ((paramArrayOfInt[j] == 65534) || (paramArrayOfInt[j] == 65535))
        arrayOfInt[j] = paramArrayOfInt[j];
      else
        arrayOfInt[j] = this.font2D.getValidatedGlyphCode(paramArrayOfInt[j]);
    return arrayOfInt;
  }

  private void init(Font paramFont, char[] paramArrayOfChar, int paramInt1, int paramInt2, FontRenderContext paramFontRenderContext, int paramInt3)
  {
    if ((paramInt1 < 0) || (paramInt2 < 0) || (paramInt1 + paramInt2 > paramArrayOfChar.length))
      throw new ArrayIndexOutOfBoundsException("start or count out of bounds");
    this.font = paramFont;
    this.frc = paramFontRenderContext;
    this.flags = paramInt3;
    if (getTracking(paramFont) != 0F)
      addFlags(2);
    if (paramInt1 != 0)
    {
      char[] arrayOfChar = new char[paramInt2];
      System.arraycopy(paramArrayOfChar, paramInt1, arrayOfChar, 0, paramInt2);
      paramArrayOfChar = arrayOfChar;
    }
    initFontData();
    this.glyphs = new int[paramInt2];
    this.userGlyphs = this.glyphs;
    this.font2D.getMapper().charsToGlyphs(paramInt2, paramArrayOfChar, this.glyphs);
  }

  private void initFontData()
  {
    this.font2D = FontManager.getFont2D(this.font);
    float f = this.font.getSize2D();
    if (this.font.isTransformed())
    {
      this.ftx = this.font.getTransform();
      if ((this.ftx.getTranslateX() != 0D) || (this.ftx.getTranslateY() != 0D))
        addFlags(2);
      this.ftx.setTransform(this.ftx.getScaleX(), this.ftx.getShearY(), this.ftx.getShearX(), this.ftx.getScaleY(), 0D, 0D);
      this.ftx.scale(f, f);
    }
    else
    {
      this.ftx = AffineTransform.getScaleInstance(f, f);
    }
    this.frctx = this.frc.getTransform();
    resetDTX(getNonTranslateTX(this.frctx));
  }

  private float[] internalGetGlyphPositions(int paramInt1, int paramInt2, int paramInt3, float[] paramArrayOfFloat)
  {
    if (paramArrayOfFloat == null)
      paramArrayOfFloat = new float[paramInt3 + paramInt2 * 2];
    initPositions();
    int i = paramInt3;
    int j = paramInt3 + paramInt2 * 2;
    for (int k = paramInt1 * 2; i < j; ++k)
    {
      paramArrayOfFloat[i] = this.positions[k];
      ++i;
    }
    return paramArrayOfFloat;
  }

  private Rectangle2D getGlyphOutlineBounds(int paramInt)
  {
    setFRCTX();
    initPositions();
    return getGlyphStrike(paramInt).getGlyphOutlineBounds(this.glyphs[paramInt], this.positions[(paramInt * 2)], this.positions[(paramInt * 2 + 1)]);
  }

  private Shape getGlyphsOutline(int paramInt1, int paramInt2, float paramFloat1, float paramFloat2)
  {
    setFRCTX();
    initPositions();
    GeneralPath localGeneralPath = new GeneralPath(1);
    int i = paramInt1;
    int j = paramInt1 + paramInt2;
    for (int k = paramInt1 * 2; i < j; k += 2)
    {
      float f1 = paramFloat1 + this.positions[k];
      float f2 = paramFloat2 + this.positions[(k + 1)];
      getGlyphStrike(i).appendGlyphOutline(this.glyphs[i], localGeneralPath, f1, f2);
      ++i;
    }
    return localGeneralPath;
  }

  private Rectangle getGlyphsPixelBounds(FontRenderContext paramFontRenderContext, float paramFloat1, float paramFloat2, int paramInt1, int paramInt2)
  {
    initPositions();
    AffineTransform localAffineTransform = null;
    if ((paramFontRenderContext == null) || (paramFontRenderContext.equals(this.frc)))
      localAffineTransform = this.frctx;
    else
      localAffineTransform = paramFontRenderContext.getTransform();
    setDTX(localAffineTransform);
    if (this.gti != null)
      return this.gti.getGlyphsPixelBounds(localAffineTransform, paramFloat1, paramFloat2, paramInt1, paramInt2);
    FontStrike localFontStrike = getDefaultStrike().strike;
    Rectangle localRectangle1 = null;
    Rectangle localRectangle2 = new Rectangle();
    java.awt.geom.Point2D.Float localFloat = new java.awt.geom.Point2D.Float();
    int i = paramInt1 * 2;
    while (true)
    {
      while (true)
      {
        do
        {
          if (--paramInt2 < 0)
            break label210;
          localFloat.x = (paramFloat1 + this.positions[(i++)]);
          localFloat.y = (paramFloat2 + this.positions[(i++)]);
          localAffineTransform.transform(localFloat, localFloat);
          localFontStrike.getGlyphImageBounds(this.glyphs[(paramInt1++)], localFloat, localRectangle2);
        }
        while (localRectangle2.isEmpty());
        if (localRectangle1 != null)
          break;
        localRectangle1 = new Rectangle(localRectangle2);
      }
      localRectangle1.add(localRectangle2);
    }
    label210: return ((localRectangle1 != null) ? localRectangle1 : localRectangle2);
  }

  private void clearCaches(int paramInt)
  {
    Shape[] arrayOfShape;
    if (this.lbcacheRef != null)
    {
      arrayOfShape = (Shape[])(Shape[])this.lbcacheRef.get();
      if (arrayOfShape != null)
        arrayOfShape[paramInt] = null;
    }
    if (this.vbcacheRef != null)
    {
      arrayOfShape = (Shape[])(Shape[])this.vbcacheRef.get();
      if (arrayOfShape != null)
        arrayOfShape[paramInt] = null;
    }
  }

  private void clearCaches()
  {
    this.lbcacheRef = null;
    this.vbcacheRef = null;
  }

  private void initPositions()
  {
    if (this.positions == null)
    {
      setFRCTX();
      this.positions = new float[this.glyphs.length * 2 + 2];
      java.awt.geom.Point2D.Float localFloat1 = null;
      float f = getTracking(this.font);
      if (f != 0F)
      {
        f *= this.font.getSize2D();
        localFloat1 = new java.awt.geom.Point2D.Float(f, 0F);
      }
      java.awt.geom.Point2D.Float localFloat2 = new java.awt.geom.Point2D.Float(0F, 0F);
      if (this.font.isTransformed())
      {
        AffineTransform localAffineTransform = this.font.getTransform();
        localAffineTransform.transform(localFloat2, localFloat2);
        this.positions[0] = localFloat2.x;
        this.positions[1] = localFloat2.y;
        if (localFloat1 != null)
          localAffineTransform.deltaTransform(localFloat1, localFloat1);
      }
      int i = 0;
      for (int j = 2; i < this.glyphs.length; j += 2)
      {
        getGlyphStrike(i).addDefaultGlyphAdvance(this.glyphs[i], localFloat2);
        if (localFloat1 != null)
        {
          localFloat2.x += localFloat1.x;
          localFloat2.y += localFloat1.y;
        }
        this.positions[j] = localFloat2.x;
        this.positions[(j + 1)] = localFloat2.y;
        ++i;
      }
    }
  }

  private void addFlags(int paramInt)
  {
    this.flags = (getLayoutFlags() | paramInt);
  }

  private void clearFlags(int paramInt)
  {
    this.flags = (getLayoutFlags() & (paramInt ^ 0xFFFFFFFF));
  }

  private GlyphStrike getGlyphStrike(int paramInt)
  {
    if (this.gti == null)
      return getDefaultStrike();
    return this.gti.getStrike(paramInt);
  }

  private GlyphStrike getDefaultStrike()
  {
    GlyphStrike localGlyphStrike = null;
    if (this.fsref != null)
      localGlyphStrike = (GlyphStrike)this.fsref.get();
    if (localGlyphStrike == null)
    {
      localGlyphStrike = GlyphStrike.create(this, this.dtx, null);
      this.fsref = new SoftReference(localGlyphStrike);
    }
    return localGlyphStrike;
  }

  public String toString()
  {
    return appendString(null).toString();
  }

  StringBuffer appendString(StringBuffer paramStringBuffer)
  {
    if (paramStringBuffer == null)
      paramStringBuffer = new StringBuffer();
    try
    {
      paramStringBuffer.append("SGV{font: ");
      paramStringBuffer.append(this.font.toString());
      paramStringBuffer.append(", frc: ");
      paramStringBuffer.append(this.frc.toString());
      paramStringBuffer.append(", glyphs: (");
      paramStringBuffer.append(this.glyphs.length);
      paramStringBuffer.append(")[");
      for (int i = 0; i < this.glyphs.length; ++i)
      {
        if (i > 0)
          paramStringBuffer.append(", ");
        paramStringBuffer.append(Integer.toHexString(this.glyphs[i]));
      }
      paramStringBuffer.append("]");
      if (this.positions != null)
      {
        paramStringBuffer.append(", positions: (");
        paramStringBuffer.append(this.positions.length);
        paramStringBuffer.append(")[");
        for (i = 0; i < this.positions.length; i += 2)
        {
          if (i > 0)
            paramStringBuffer.append(", ");
          paramStringBuffer.append(this.positions[i]);
          paramStringBuffer.append("@");
          paramStringBuffer.append(this.positions[(i + 1)]);
        }
        paramStringBuffer.append("]");
      }
      if (this.charIndices != null)
      {
        paramStringBuffer.append(", indices: (");
        paramStringBuffer.append(this.charIndices.length);
        paramStringBuffer.append(")[");
        for (i = 0; i < this.charIndices.length; ++i)
        {
          if (i > 0)
            paramStringBuffer.append(", ");
          paramStringBuffer.append(this.charIndices[i]);
        }
        paramStringBuffer.append("]");
      }
      paramStringBuffer.append(", flags:");
      if (getLayoutFlags() == 0)
      {
        paramStringBuffer.append(" default");
      }
      else
      {
        if ((this.flags & 0x1) != 0)
          paramStringBuffer.append(" tx");
        if ((this.flags & 0x2) != 0)
          paramStringBuffer.append(" pos");
        if ((this.flags & 0x4) != 0)
          paramStringBuffer.append(" rtl");
        if ((this.flags & 0x8) != 0)
          paramStringBuffer.append(" complex");
      }
    }
    catch (Exception localException)
    {
      paramStringBuffer.append(" " + localException.getMessage());
    }
    paramStringBuffer.append("}");
    return paramStringBuffer;
  }

  static class ADL
  {
    public float ascentX;
    public float ascentY;
    public float descentX;
    public float descentY;
    public float leadingX;
    public float leadingY;

    public String toString()
    {
      return toStringBuffer(null).toString();
    }

    protected StringBuffer toStringBuffer(StringBuffer paramStringBuffer)
    {
      if (paramStringBuffer == null)
        paramStringBuffer = new StringBuffer();
      paramStringBuffer.append("ax: ");
      paramStringBuffer.append(this.ascentX);
      paramStringBuffer.append(" ay: ");
      paramStringBuffer.append(this.ascentY);
      paramStringBuffer.append(" dx: ");
      paramStringBuffer.append(this.descentX);
      paramStringBuffer.append(" dy: ");
      paramStringBuffer.append(this.descentY);
      paramStringBuffer.append(" lx: ");
      paramStringBuffer.append(this.leadingX);
      paramStringBuffer.append(" ly: ");
      paramStringBuffer.append(this.leadingY);
      return paramStringBuffer;
    }
  }

  public static final class GlyphStrike
  {
    StandardGlyphVector sgv;
    FontStrike strike;
    float dx;
    float dy;

    static GlyphStrike create(StandardGlyphVector paramStandardGlyphVector, AffineTransform paramAffineTransform1, AffineTransform paramAffineTransform2)
    {
      float f1 = 0F;
      float f2 = 0F;
      AffineTransform localAffineTransform = StandardGlyphVector.access$700(paramStandardGlyphVector);
      if ((!(paramAffineTransform1.isIdentity())) || (paramAffineTransform2 != null))
      {
        localAffineTransform = new AffineTransform(StandardGlyphVector.access$700(paramStandardGlyphVector));
        if (paramAffineTransform2 != null)
        {
          localAffineTransform.preConcatenate(paramAffineTransform2);
          f1 = (float)localAffineTransform.getTranslateX();
          f2 = (float)localAffineTransform.getTranslateY();
        }
        if (!(paramAffineTransform1.isIdentity()))
          localAffineTransform.preConcatenate(paramAffineTransform1);
      }
      int i = 1;
      Object localObject = StandardGlyphVector.access$800(paramStandardGlyphVector).getAntiAliasingHint();
      if ((localObject == RenderingHints.VALUE_TEXT_ANTIALIAS_GASP) && (!(localAffineTransform.isIdentity())) && ((localAffineTransform.getType() & 0xFFFFFFFE) != 0))
      {
        double d1 = localAffineTransform.getShearX();
        if (d1 != 0D)
        {
          double d2 = localAffineTransform.getScaleY();
          i = (int)Math.sqrt(d1 * d1 + d2 * d2);
        }
        else
        {
          i = (int)Math.abs(localAffineTransform.getScaleY());
        }
      }
      int j = FontStrikeDesc.getAAHintIntVal(localObject, StandardGlyphVector.access$900(paramStandardGlyphVector), i);
      int k = FontStrikeDesc.getFMHintIntVal(StandardGlyphVector.access$800(paramStandardGlyphVector).getFractionalMetricsHint());
      FontStrikeDesc localFontStrikeDesc = new FontStrikeDesc(paramAffineTransform1, localAffineTransform, StandardGlyphVector.access$1000(paramStandardGlyphVector).getStyle(), j, k);
      FontStrike localFontStrike = StandardGlyphVector.access$900(paramStandardGlyphVector).getStrike(localFontStrikeDesc);
      return new GlyphStrike(paramStandardGlyphVector, localFontStrike, f1, f2);
    }

    private GlyphStrike(StandardGlyphVector paramStandardGlyphVector, FontStrike paramFontStrike, float paramFloat1, float paramFloat2)
    {
      this.sgv = paramStandardGlyphVector;
      this.strike = paramFontStrike;
      this.dx = paramFloat1;
      this.dy = paramFloat2;
    }

    void getADL(StandardGlyphVector.ADL paramADL)
    {
      StrikeMetrics localStrikeMetrics = this.strike.getFontMetrics();
      java.awt.geom.Point2D.Float localFloat = null;
      if (StandardGlyphVector.access$1000(this.sgv).isTransformed())
      {
        localFloat = new java.awt.geom.Point2D.Float();
        localFloat.x = (float)StandardGlyphVector.access$1000(this.sgv).getTransform().getTranslateX();
        localFloat.y = (float)StandardGlyphVector.access$1000(this.sgv).getTransform().getTranslateY();
      }
      paramADL.ascentX = (-localStrikeMetrics.ascentX);
      paramADL.ascentY = (-localStrikeMetrics.ascentY);
      paramADL.descentX = localStrikeMetrics.descentX;
      paramADL.descentY = localStrikeMetrics.descentY;
      paramADL.leadingX = localStrikeMetrics.leadingX;
      paramADL.leadingY = localStrikeMetrics.leadingY;
    }

    void getGlyphPosition(int paramInt1, int paramInt2, float[] paramArrayOfFloat1, float[] paramArrayOfFloat2)
    {
      paramArrayOfFloat2[paramInt2] = (paramArrayOfFloat1[paramInt2] + this.dx);
      paramArrayOfFloat2[(++paramInt2)] = (paramArrayOfFloat1[paramInt2] + this.dy);
    }

    void addDefaultGlyphAdvance(int paramInt, java.awt.geom.Point2D.Float paramFloat)
    {
      java.awt.geom.Point2D.Float localFloat = this.strike.getGlyphMetrics(paramInt);
      paramFloat.x += localFloat.x + this.dx;
      paramFloat.y += localFloat.y + this.dy;
    }

    Rectangle2D getGlyphOutlineBounds(int paramInt, float paramFloat1, float paramFloat2)
    {
      Object localObject = null;
      if (StandardGlyphVector.access$1100(this.sgv) == null)
      {
        localObject = new Rectangle2D.Float();
        ((Rectangle2D)localObject).setRect(this.strike.getGlyphOutlineBounds(paramInt));
      }
      else
      {
        GeneralPath localGeneralPath = this.strike.getGlyphOutline(paramInt, 0F, 0F);
        localGeneralPath.transform(StandardGlyphVector.access$1100(this.sgv));
        localObject = localGeneralPath.getBounds2D();
      }
      ((Rectangle2D)localObject).setRect(((Rectangle2D)localObject).getMinX() + paramFloat1 + this.dx, ((Rectangle2D)localObject).getMinY() + paramFloat2 + this.dy, ((Rectangle2D)localObject).getWidth(), ((Rectangle2D)localObject).getHeight());
      return ((Rectangle2D)localObject);
    }

    void appendGlyphOutline(int paramInt, GeneralPath paramGeneralPath, float paramFloat1, float paramFloat2)
    {
      GeneralPath localGeneralPath = null;
      if (StandardGlyphVector.access$1100(this.sgv) == null)
      {
        localGeneralPath = this.strike.getGlyphOutline(paramInt, paramFloat1 + this.dx, paramFloat2 + this.dy);
      }
      else
      {
        localGeneralPath = this.strike.getGlyphOutline(paramInt, 0F, 0F);
        localGeneralPath.transform(StandardGlyphVector.access$1100(this.sgv));
        localGeneralPath.transform(AffineTransform.getTranslateInstance(paramFloat1 + this.dx, paramFloat2 + this.dy));
      }
      PathIterator localPathIterator = localGeneralPath.getPathIterator(null);
      paramGeneralPath.append(localPathIterator, false);
    }
  }

  static final class GlyphTransformInfo
  {
    StandardGlyphVector sgv;
    int[] indices;
    double[] transforms;
    SoftReference strikesRef;
    boolean haveAllStrikes;

    GlyphTransformInfo(StandardGlyphVector paramStandardGlyphVector)
    {
      this.sgv = paramStandardGlyphVector;
    }

    GlyphTransformInfo(StandardGlyphVector paramStandardGlyphVector, GlyphTransformInfo paramGlyphTransformInfo)
    {
      this.sgv = paramStandardGlyphVector;
      this.indices = ((paramGlyphTransformInfo.indices == null) ? null : (int[])(int[])paramGlyphTransformInfo.indices.clone());
      this.transforms = ((paramGlyphTransformInfo.transforms == null) ? null : (double[])(double[])paramGlyphTransformInfo.transforms.clone());
      this.strikesRef = null;
    }

    public boolean equals(GlyphTransformInfo paramGlyphTransformInfo)
    {
      if (paramGlyphTransformInfo == null)
        return false;
      if (paramGlyphTransformInfo == this)
        return true;
      if (this.indices.length != paramGlyphTransformInfo.indices.length)
        return false;
      if (this.transforms.length != paramGlyphTransformInfo.transforms.length)
        return false;
      for (int i = 0; i < this.indices.length; ++i)
      {
        int j = this.indices[i];
        int k = paramGlyphTransformInfo.indices[i];
        if (((j == 0) ? 1 : 0) != ((k == 0) ? 1 : 0))
          return false;
        if (j != 0)
        {
          j *= 6;
          k *= 6;
          for (int l = 6; l > 0; --l)
            if (this.indices[(--j)] != paramGlyphTransformInfo.indices[(--k)])
              return false;
        }
      }
      return true;
    }

    void setGlyphTransform(int paramInt, AffineTransform paramAffineTransform)
    {
      double[] arrayOfDouble1 = new double[6];
      int i = 1;
      if ((paramAffineTransform == null) || (paramAffineTransform.isIdentity()))
      {
        arrayOfDouble1[0] = (arrayOfDouble1[3] = 1D);
      }
      else
      {
        i = 0;
        paramAffineTransform.getMatrix(arrayOfDouble1);
      }
      if (this.indices == null)
      {
        if (i != 0)
          return;
        this.indices = new int[StandardGlyphVector.access$000(this.sgv).length];
        this.indices[paramInt] = 1;
        label156: this.transforms = arrayOfDouble1;
      }
      else
      {
        int i1;
        int j = 0;
        int k = -1;
        if (i != 0)
        {
          k = 0;
        }
        else
        {
          j = 1;
          for (l = 0; l < this.transforms.length; l += 6)
          {
            for (i1 = 0; i1 < 6; ++i1)
              if (this.transforms[(l + i1)] != arrayOfDouble1[i1])
                break label156:
            j = 0;
            break;
          }
          k = l / 6 + 1;
        }
        int l = this.indices[paramInt];
        if (k != l)
        {
          i1 = 0;
          if (l != 0)
          {
            i1 = 1;
            for (int i2 = 0; i2 < this.indices.length; ++i2)
              if ((this.indices[i2] == l) && (i2 != paramInt))
              {
                i1 = 0;
                break;
              }
          }
          if ((i1 != 0) && (j != 0))
          {
            k = l;
            System.arraycopy(arrayOfDouble1, 0, this.transforms, (k - 1) * 6, 6);
          }
          else
          {
            double[] arrayOfDouble2;
            if (i1 != 0)
            {
              if (this.transforms.length == 6)
              {
                this.indices = null;
                this.transforms = null;
                StandardGlyphVector.access$100(this.sgv, paramInt);
                StandardGlyphVector.access$200(this.sgv, 1);
                this.strikesRef = null;
                return;
              }
              arrayOfDouble2 = new double[this.transforms.length - 6];
              System.arraycopy(this.transforms, 0, arrayOfDouble2, 0, (l - 1) * 6);
              System.arraycopy(this.transforms, l * 6, arrayOfDouble2, (l - 1) * 6, this.transforms.length - l * 6);
              this.transforms = arrayOfDouble2;
              for (int i3 = 0; i3 < this.indices.length; ++i3)
                if (this.indices[i3] > l)
                  this.indices[i3] -= 1;
              if (k > l)
                --k;
            }
            else if (j != 0)
            {
              arrayOfDouble2 = new double[this.transforms.length + 6];
              System.arraycopy(this.transforms, 0, arrayOfDouble2, 0, this.transforms.length);
              System.arraycopy(arrayOfDouble1, 0, arrayOfDouble2, this.transforms.length, 6);
              this.transforms = arrayOfDouble2;
            }
          }
          this.indices[paramInt] = k;
        }
      }
      StandardGlyphVector.access$100(this.sgv, paramInt);
      StandardGlyphVector.access$300(this.sgv, 1);
      this.strikesRef = null;
    }

    AffineTransform getGlyphTransform(int paramInt)
    {
      int i = this.indices[paramInt];
      if (i == 0)
        return null;
      int j = (i - 1) * 6;
      return new AffineTransform(this.transforms[(j + 0)], this.transforms[(j + 1)], this.transforms[(j + 2)], this.transforms[(j + 3)], this.transforms[(j + 4)], this.transforms[(j + 5)]);
    }

    int transformCount()
    {
      if (this.transforms == null)
        return 0;
      return (this.transforms.length / 6);
    }

    Object setupGlyphImages(long[] paramArrayOfLong, float[] paramArrayOfFloat, AffineTransform paramAffineTransform)
    {
      int i = StandardGlyphVector.access$000(this.sgv).length;
      StandardGlyphVector.GlyphStrike[] arrayOfGlyphStrike = getAllStrikes();
      for (int j = 0; j < i; ++j)
      {
        StandardGlyphVector.GlyphStrike localGlyphStrike = arrayOfGlyphStrike[this.indices[j]];
        int k = StandardGlyphVector.access$000(this.sgv)[j];
        paramArrayOfLong[j] = localGlyphStrike.strike.getGlyphImagePtr(k);
        localGlyphStrike.getGlyphPosition(k, j * 2, StandardGlyphVector.access$400(this.sgv), paramArrayOfFloat);
      }
      paramAffineTransform.transform(paramArrayOfFloat, 0, paramArrayOfFloat, 0, i);
      return arrayOfGlyphStrike;
    }

    Rectangle getGlyphsPixelBounds(AffineTransform paramAffineTransform, float paramFloat1, float paramFloat2, int paramInt1, int paramInt2)
    {
      Rectangle localRectangle1 = null;
      Rectangle localRectangle2 = new Rectangle();
      java.awt.geom.Point2D.Float localFloat = new java.awt.geom.Point2D.Float();
      int i = paramInt1 * 2;
      while (--paramInt2 >= 0)
      {
        StandardGlyphVector.GlyphStrike localGlyphStrike = getStrike(paramInt1);
        localFloat.x = (paramFloat1 + StandardGlyphVector.access$400(this.sgv)[(i++)] + localGlyphStrike.dx);
        localFloat.y = (paramFloat2 + StandardGlyphVector.access$400(this.sgv)[(i++)] + localGlyphStrike.dy);
        paramAffineTransform.transform(localFloat, localFloat);
        localGlyphStrike.strike.getGlyphImageBounds(StandardGlyphVector.access$000(this.sgv)[(paramInt1++)], localFloat, localRectangle2);
        if (!(localRectangle2.isEmpty()))
          if (localRectangle1 == null)
            localRectangle1 = new Rectangle(localRectangle2);
          else
            localRectangle1.add(localRectangle2);
      }
      return ((localRectangle1 != null) ? localRectangle1 : localRectangle2);
    }

    StandardGlyphVector.GlyphStrike getStrike(int paramInt)
    {
      if (this.indices != null)
      {
        StandardGlyphVector.GlyphStrike[] arrayOfGlyphStrike = getStrikeArray();
        return getStrikeAtIndex(arrayOfGlyphStrike, this.indices[paramInt]);
      }
      return StandardGlyphVector.access$500(this.sgv);
    }

    private StandardGlyphVector.GlyphStrike[] getAllStrikes()
    {
      if (this.indices == null)
        return null;
      StandardGlyphVector.GlyphStrike[] arrayOfGlyphStrike = getStrikeArray();
      if (!(this.haveAllStrikes))
      {
        for (int i = 0; i < arrayOfGlyphStrike.length; ++i)
          getStrikeAtIndex(arrayOfGlyphStrike, i);
        this.haveAllStrikes = true;
      }
      return arrayOfGlyphStrike;
    }

    private StandardGlyphVector.GlyphStrike[] getStrikeArray()
    {
      StandardGlyphVector.GlyphStrike[] arrayOfGlyphStrike = null;
      if (this.strikesRef != null)
        arrayOfGlyphStrike = (StandardGlyphVector.GlyphStrike[])(StandardGlyphVector.GlyphStrike[])this.strikesRef.get();
      if (arrayOfGlyphStrike == null)
      {
        this.haveAllStrikes = false;
        arrayOfGlyphStrike = new StandardGlyphVector.GlyphStrike[transformCount() + 1];
        this.strikesRef = new SoftReference(arrayOfGlyphStrike);
      }
      return arrayOfGlyphStrike;
    }

    private StandardGlyphVector.GlyphStrike getStrikeAtIndex(StandardGlyphVector.GlyphStrike[] paramArrayOfGlyphStrike, int paramInt)
    {
      StandardGlyphVector.GlyphStrike localGlyphStrike = paramArrayOfGlyphStrike[paramInt];
      if (localGlyphStrike == null)
      {
        if (paramInt == 0)
        {
          localGlyphStrike = StandardGlyphVector.access$500(this.sgv);
        }
        else
        {
          int i = (paramInt - 1) * 6;
          AffineTransform localAffineTransform = new AffineTransform(this.transforms[i], this.transforms[(i + 1)], this.transforms[(i + 2)], this.transforms[(i + 3)], this.transforms[(i + 4)], this.transforms[(i + 5)]);
          localGlyphStrike = StandardGlyphVector.GlyphStrike.create(this.sgv, StandardGlyphVector.access$600(this.sgv), localAffineTransform);
        }
        paramArrayOfGlyphStrike[paramInt] = localGlyphStrike;
      }
      return localGlyphStrike;
    }
  }
}