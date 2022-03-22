package sun.font;

import java.awt.font.GlyphVector;
import sun.java2d.loops.FontInfo;
import sun.misc.Unsafe;

public final class GlyphList
{
  private static final int MINGRAYLENGTH = 1024;
  private static final int MAXGRAYLENGTH = 8192;
  private static final int DEFAULT_LENGTH = 32;
  int glyphindex;
  int[] metrics;
  byte[] graybits;
  Object strikelist;
  int len = 0;
  int maxLen = 0;
  int maxPosLen = 0;
  int[] glyphData;
  char[] chData;
  long[] images;
  float[] positions;
  float x;
  float y;
  float gposx;
  float gposy;
  boolean usePositions;
  boolean lcdRGBOrder;
  boolean lcdSubPixPos;
  private static GlyphList reusableGL = new GlyphList();
  private static boolean inUse;

  void ensureCapacity(int paramInt)
  {
    if (paramInt < 0)
      paramInt = 0;
    if ((this.usePositions) && (paramInt > this.maxPosLen))
    {
      this.positions = new float[paramInt * 2 + 2];
      this.maxPosLen = paramInt;
    }
    if ((this.maxLen == 0) || (paramInt > this.maxLen))
    {
      this.glyphData = new int[paramInt];
      this.chData = new char[paramInt];
      this.images = new long[paramInt];
      this.maxLen = paramInt;
    }
  }

  public static GlyphList getInstance()
  {
    if (inUse)
      return new GlyphList();
    synchronized (GlyphList.class)
    {
      if (!(inUse))
        break label36;
      return new GlyphList();
      label36: inUse = true;
      return reusableGL;
    }
  }

  public boolean setFromString(FontInfo paramFontInfo, String paramString, float paramFloat1, float paramFloat2)
  {
    this.x = paramFloat1;
    this.y = paramFloat2;
    this.strikelist = paramFontInfo.fontStrike;
    this.lcdRGBOrder = paramFontInfo.lcdRGBOrder;
    this.lcdSubPixPos = paramFontInfo.lcdSubPixPos;
    this.len = paramString.length();
    ensureCapacity(this.len);
    paramString.getChars(0, this.len, this.chData, 0);
    return mapChars(paramFontInfo, this.len);
  }

  public boolean setFromChars(FontInfo paramFontInfo, char[] paramArrayOfChar, int paramInt1, int paramInt2, float paramFloat1, float paramFloat2)
  {
    this.x = paramFloat1;
    this.y = paramFloat2;
    this.strikelist = paramFontInfo.fontStrike;
    this.lcdRGBOrder = paramFontInfo.lcdRGBOrder;
    this.lcdSubPixPos = paramFontInfo.lcdSubPixPos;
    this.len = paramInt2;
    if (paramInt2 < 0)
      this.len = 0;
    else
      this.len = paramInt2;
    ensureCapacity(this.len);
    System.arraycopy(paramArrayOfChar, paramInt1, this.chData, 0, this.len);
    return mapChars(paramFontInfo, this.len);
  }

  private final boolean mapChars(FontInfo paramFontInfo, int paramInt)
  {
    if (paramFontInfo.font2D.getMapper().charsToGlyphsNS(paramInt, this.chData, this.glyphData))
      return false;
    paramFontInfo.fontStrike.getGlyphImagePtrs(this.glyphData, this.images, paramInt);
    this.glyphindex = -1;
    return true;
  }

  public void setFromGlyphVector(FontInfo paramFontInfo, GlyphVector paramGlyphVector, float paramFloat1, float paramFloat2)
  {
    this.x = paramFloat1;
    this.y = paramFloat2;
    this.lcdRGBOrder = paramFontInfo.lcdRGBOrder;
    this.lcdSubPixPos = paramFontInfo.lcdSubPixPos;
    StandardGlyphVector localStandardGlyphVector = StandardGlyphVector.getStandardGV(paramGlyphVector, paramFontInfo);
    this.usePositions = localStandardGlyphVector.needsPositions(paramFontInfo.devTx);
    this.len = localStandardGlyphVector.getNumGlyphs();
    ensureCapacity(this.len);
    this.strikelist = localStandardGlyphVector.setupGlyphImages(this.images, (this.usePositions) ? this.positions : null, paramFontInfo.devTx);
    this.glyphindex = -1;
  }

  public int[] getBounds()
  {
    if (this.glyphindex >= 0)
      throw new InternalError("calling getBounds after setGlyphIndex");
    if (this.metrics == null)
      this.metrics = new int[5];
    this.gposx = (this.x + 0.5F);
    this.gposy = (this.y + 0.5F);
    fillBounds(this.metrics);
    return this.metrics;
  }

  public void setGlyphIndex(int paramInt)
  {
    this.glyphindex = paramInt;
    float f1 = StrikeCache.unsafe.getFloat(this.images[paramInt] + StrikeCache.topLeftXOffset);
    float f2 = StrikeCache.unsafe.getFloat(this.images[paramInt] + StrikeCache.topLeftYOffset);
    if (this.usePositions)
    {
      this.metrics[0] = (int)Math.floor(this.positions[(paramInt << 1)] + this.gposx + f1);
      this.metrics[1] = (int)Math.floor(this.positions[((paramInt << 1) + 1)] + this.gposy + f2);
    }
    else
    {
      this.metrics[0] = (int)Math.floor(this.gposx + f1);
      this.metrics[1] = (int)Math.floor(this.gposy + f2);
      this.gposx += StrikeCache.unsafe.getFloat(this.images[paramInt] + StrikeCache.xAdvanceOffset);
      this.gposy += StrikeCache.unsafe.getFloat(this.images[paramInt] + StrikeCache.yAdvanceOffset);
    }
    this.metrics[2] = StrikeCache.unsafe.getChar(this.images[paramInt] + StrikeCache.widthOffset);
    this.metrics[3] = StrikeCache.unsafe.getChar(this.images[paramInt] + StrikeCache.heightOffset);
    this.metrics[4] = StrikeCache.unsafe.getChar(this.images[paramInt] + StrikeCache.rowBytesOffset);
  }

  public int[] getMetrics()
  {
    return this.metrics;
  }

  public byte[] getGrayBits()
  {
    long l;
    int i = this.metrics[4] * this.metrics[3];
    if (this.graybits == null)
      this.graybits = new byte[Math.max(i, 1024)];
    else if (i > this.graybits.length)
      this.graybits = new byte[i];
    if (StrikeCache.nativeAddressSize == 4)
      l = 0xFFFFFFFF & StrikeCache.unsafe.getInt(this.images[this.glyphindex] + StrikeCache.pixelDataOffset);
    else
      l = StrikeCache.unsafe.getLong(this.images[this.glyphindex] + StrikeCache.pixelDataOffset);
    if (l == 3412046810217185280L)
      return this.graybits;
    for (int j = 0; j < i; ++j)
      this.graybits[j] = StrikeCache.unsafe.getByte(l + j);
    return this.graybits;
  }

  public long[] getImages()
  {
    return this.images;
  }

  public boolean usePositions()
  {
    return this.usePositions;
  }

  public float[] getPositions()
  {
    return this.positions;
  }

  public float getX()
  {
    return this.x;
  }

  public float getY()
  {
    return this.y;
  }

  public Object getStrike()
  {
    return this.strikelist;
  }

  public boolean isSubPixPos()
  {
    return this.lcdSubPixPos;
  }

  public boolean isRGBOrder()
  {
    return this.lcdRGBOrder;
  }

  public void dispose()
  {
    if (this == reusableGL)
    {
      if ((this.graybits != null) && (this.graybits.length > 8192))
        this.graybits = null;
      this.usePositions = false;
      this.strikelist = null;
      inUse = false;
    }
  }

  public int getNumGlyphs()
  {
    return this.len;
  }

  private void fillBounds(int[] paramArrayOfInt)
  {
    float f2;
    float f4;
    int i = StrikeCache.topLeftXOffset;
    int j = StrikeCache.topLeftYOffset;
    int k = StrikeCache.widthOffset;
    int l = StrikeCache.heightOffset;
    int i1 = StrikeCache.xAdvanceOffset;
    int i2 = StrikeCache.yAdvanceOffset;
    if (this.len == 0)
    {
      paramArrayOfInt[0] = (paramArrayOfInt[1] = paramArrayOfInt[2] = paramArrayOfInt[3] = 0);
      return;
    }
    float f1 = f2 = (1.0F / 1.0F);
    float f3 = f4 = (1.0F / -1.0F);
    int i3 = 0;
    float f5 = this.x + 0.5F;
    float f6 = this.y + 0.5F;
    for (int i6 = 0; i6 < this.len; ++i6)
    {
      float f9;
      float f10;
      float f7 = StrikeCache.unsafe.getFloat(this.images[i6] + i);
      float f8 = StrikeCache.unsafe.getFloat(this.images[i6] + j);
      int i4 = StrikeCache.unsafe.getChar(this.images[i6] + k);
      int i5 = StrikeCache.unsafe.getChar(this.images[i6] + l);
      if (this.usePositions)
      {
        f9 = this.positions[(i3++)] + f7 + f5;
        f10 = this.positions[(i3++)] + f8 + f6;
      }
      else
      {
        f9 = f5 + f7;
        f10 = f6 + f8;
        f5 += StrikeCache.unsafe.getFloat(this.images[i6] + i1);
        f6 += StrikeCache.unsafe.getFloat(this.images[i6] + i2);
      }
      float f11 = f9 + i4;
      float f12 = f10 + i5;
      if (f1 > f9)
        f1 = f9;
      if (f2 > f10)
        f2 = f10;
      if (f3 < f11)
        f3 = f11;
      if (f4 < f12)
        f4 = f12;
    }
    paramArrayOfInt[0] = (int)Math.floor(f1);
    paramArrayOfInt[1] = (int)Math.floor(f2);
    paramArrayOfInt[2] = (int)Math.floor(f3);
    paramArrayOfInt[3] = (int)Math.floor(f4);
  }
}