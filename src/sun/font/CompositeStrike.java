package sun.font;

import java.awt.Rectangle;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D.Float;
import java.awt.geom.Rectangle2D.Float;

public final class CompositeStrike extends FontStrike
{
  static final int SLOTMASK = 16777215;
  private CompositeFont compFont;
  private PhysicalStrike[] strikes;
  int numGlyphs = 0;

  CompositeStrike(CompositeFont paramCompositeFont, FontStrikeDesc paramFontStrikeDesc)
  {
    this.compFont = paramCompositeFont;
    this.desc = paramFontStrikeDesc;
    this.disposer = new FontStrikeDisposer(this.compFont, paramFontStrikeDesc);
    if (paramFontStrikeDesc.style != this.compFont.style)
    {
      this.algoStyle = true;
      if (((paramFontStrikeDesc.style & 0x1) == 1) && ((this.compFont.style & 0x1) == 0))
        this.boldness = 1.3300000429153442F;
      if (((paramFontStrikeDesc.style & 0x2) == 2) && ((this.compFont.style & 0x2) == 0))
        this.italic = 0.69999998807907104F;
    }
    this.strikes = new PhysicalStrike[this.compFont.numSlots];
  }

  PhysicalStrike getStrikeForGlyph(int paramInt)
  {
    return getStrikeForSlot(paramInt >>> 24);
  }

  PhysicalStrike getStrikeForSlot(int paramInt)
  {
    PhysicalStrike localPhysicalStrike = this.strikes[paramInt];
    if (localPhysicalStrike == null)
    {
      localPhysicalStrike = (PhysicalStrike)(PhysicalStrike)this.compFont.getSlotFont(paramInt).getStrike(this.desc);
      this.strikes[paramInt] = localPhysicalStrike;
    }
    return localPhysicalStrike;
  }

  public int getNumGlyphs()
  {
    return this.compFont.getNumGlyphs();
  }

  StrikeMetrics getFontMetrics()
  {
    if (this.strikeMetrics == null)
    {
      StrikeMetrics localStrikeMetrics = new StrikeMetrics();
      for (int i = 0; i < this.compFont.numMetricsSlots; ++i)
        localStrikeMetrics.merge(getStrikeForSlot(i).getFontMetrics());
      this.strikeMetrics = localStrikeMetrics;
    }
    return this.strikeMetrics;
  }

  void getGlyphImagePtrs(int[] paramArrayOfInt, long[] paramArrayOfLong, int paramInt)
  {
    PhysicalStrike localPhysicalStrike = getStrikeForSlot(0);
    int i = localPhysicalStrike.getSlot0GlyphImagePtrs(paramArrayOfInt, paramArrayOfLong, paramInt);
    if (i == paramInt)
      return;
    for (int j = i; j < paramInt; ++j)
    {
      localPhysicalStrike = getStrikeForGlyph(paramArrayOfInt[j]);
      paramArrayOfLong[j] = localPhysicalStrike.getGlyphImagePtr(paramArrayOfInt[j] & 0xFFFFFF);
    }
  }

  long getGlyphImagePtr(int paramInt)
  {
    PhysicalStrike localPhysicalStrike = getStrikeForGlyph(paramInt);
    return localPhysicalStrike.getGlyphImagePtr(paramInt & 0xFFFFFF);
  }

  void getGlyphImageBounds(int paramInt, Point2D.Float paramFloat, Rectangle paramRectangle)
  {
    PhysicalStrike localPhysicalStrike = getStrikeForGlyph(paramInt);
    localPhysicalStrike.getGlyphImageBounds(paramInt & 0xFFFFFF, paramFloat, paramRectangle);
  }

  Point2D.Float getGlyphMetrics(int paramInt)
  {
    PhysicalStrike localPhysicalStrike = getStrikeForGlyph(paramInt);
    return localPhysicalStrike.getGlyphMetrics(paramInt & 0xFFFFFF);
  }

  Point2D.Float getCharMetrics(char paramChar)
  {
    return getGlyphMetrics(this.compFont.getMapper().charToGlyph(paramChar));
  }

  float getGlyphAdvance(int paramInt)
  {
    PhysicalStrike localPhysicalStrike = getStrikeForGlyph(paramInt);
    return localPhysicalStrike.getGlyphAdvance(paramInt & 0xFFFFFF);
  }

  float getCodePointAdvance(int paramInt)
  {
    return getGlyphAdvance(this.compFont.getMapper().charToGlyph(paramInt));
  }

  Rectangle2D.Float getGlyphOutlineBounds(int paramInt)
  {
    PhysicalStrike localPhysicalStrike = getStrikeForGlyph(paramInt);
    return localPhysicalStrike.getGlyphOutlineBounds(paramInt & 0xFFFFFF);
  }

  GeneralPath getGlyphOutline(int paramInt, float paramFloat1, float paramFloat2)
  {
    PhysicalStrike localPhysicalStrike = getStrikeForGlyph(paramInt);
    GeneralPath localGeneralPath = localPhysicalStrike.getGlyphOutline(paramInt & 0xFFFFFF, paramFloat1, paramFloat2);
    if (localGeneralPath == null)
      return new GeneralPath();
    return localGeneralPath;
  }

  GeneralPath getGlyphVectorOutline(int[] paramArrayOfInt, float paramFloat1, float paramFloat2)
  {
    Object localObject = null;
    int i = 0;
    while (i < paramArrayOfInt.length)
    {
      int j = i;
      int k = paramArrayOfInt[i] >>> 24;
      while ((i < paramArrayOfInt.length) && (paramArrayOfInt[(i + 1)] >>> 24 == k))
        ++i;
      int l = i - j + 1;
      int[] arrayOfInt = new int[l];
      for (int i1 = 0; i1 < l; ++i1)
        arrayOfInt[i1] = (paramArrayOfInt[i1] & 0xFFFFFF);
      GeneralPath localGeneralPath = getStrikeForSlot(k).getGlyphVectorOutline(arrayOfInt, paramFloat1, paramFloat2);
      if (localObject == null)
        localObject = localGeneralPath;
      else if (localGeneralPath != null)
        localObject.append(localGeneralPath, false);
    }
    if (localObject == null)
      return new GeneralPath();
    return localObject;
  }
}