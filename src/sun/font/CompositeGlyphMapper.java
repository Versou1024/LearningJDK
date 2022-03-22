package sun.font;

public final class CompositeGlyphMapper extends CharToGlyphMapper
{
  public static final int SLOTMASK = -16777216;
  public static final int GLYPHMASK = 16777215;
  public static final int NBLOCKS = 216;
  public static final int BLOCKSZ = 256;
  public static final int MAXUNICODE = 55296;
  CompositeFont font;
  CharToGlyphMapper[] slotMappers;
  int[][] glyphMaps;
  private boolean hasExcludes;

  public CompositeGlyphMapper(CompositeFont paramCompositeFont)
  {
    this.font = paramCompositeFont;
    initMapper();
    this.hasExcludes = ((paramCompositeFont.exclusionRanges != null) && (paramCompositeFont.maxIndices != null));
  }

  public final int compositeGlyphCode(int paramInt1, int paramInt2)
  {
    return (paramInt1 << 24 | paramInt2 & 0xFFFFFF);
  }

  private final void initMapper()
  {
    if (this.missingGlyph == -1)
    {
      if (this.glyphMaps == null)
        this.glyphMaps = new int[216][];
      this.slotMappers = new CharToGlyphMapper[this.font.numSlots];
      this.missingGlyph = this.font.getSlotFont(0).getMissingGlyphCode();
      this.missingGlyph = compositeGlyphCode(0, this.missingGlyph);
    }
  }

  private int getCachedGlyphCode(int paramInt)
  {
    int[] arrayOfInt;
    if (paramInt >= 55296)
      return -1;
    if ((arrayOfInt = this.glyphMaps[(paramInt >> 8)]) == null)
      return -1;
    return arrayOfInt[(paramInt & 0xFF)];
  }

  private void setCachedGlyphCode(int paramInt1, int paramInt2)
  {
    if (paramInt1 >= 55296)
      return;
    int i = paramInt1 >> 8;
    if (this.glyphMaps[i] == null)
    {
      this.glyphMaps[i] = new int[256];
      for (int j = 0; j < 256; ++j)
        this.glyphMaps[i][j] = -1;
    }
    this.glyphMaps[i][(paramInt1 & 0xFF)] = paramInt2;
  }

  private final CharToGlyphMapper getSlotMapper(int paramInt)
  {
    CharToGlyphMapper localCharToGlyphMapper = this.slotMappers[paramInt];
    if (localCharToGlyphMapper == null)
    {
      localCharToGlyphMapper = this.font.getSlotFont(paramInt).getMapper();
      this.slotMappers[paramInt] = localCharToGlyphMapper;
    }
    return localCharToGlyphMapper;
  }

  private final int convertToGlyph(int paramInt)
  {
    for (int i = 0; i < this.font.numSlots; ++i)
      if ((!(this.hasExcludes)) || (!(this.font.isExcludedChar(i, paramInt))))
      {
        CharToGlyphMapper localCharToGlyphMapper = getSlotMapper(i);
        int j = localCharToGlyphMapper.charToGlyph(paramInt);
        if (j != localCharToGlyphMapper.getMissingGlyphCode())
        {
          j = compositeGlyphCode(i, j);
          setCachedGlyphCode(paramInt, j);
          return j;
        }
      }
    return this.missingGlyph;
  }

  public int getNumGlyphs()
  {
    int i = 0;
    for (int j = 0; j < 1; ++j)
    {
      CharToGlyphMapper localCharToGlyphMapper = this.slotMappers[j];
      if (localCharToGlyphMapper == null)
      {
        localCharToGlyphMapper = this.font.getSlotFont(j).getMapper();
        this.slotMappers[j] = localCharToGlyphMapper;
      }
      i += localCharToGlyphMapper.getNumGlyphs();
    }
    return i;
  }

  public int charToGlyph(int paramInt)
  {
    int i = getCachedGlyphCode(paramInt);
    if (i == -1)
      i = convertToGlyph(paramInt);
    return i;
  }

  public int charToGlyph(int paramInt1, int paramInt2)
  {
    if (paramInt2 >= 0)
    {
      CharToGlyphMapper localCharToGlyphMapper = getSlotMapper(paramInt2);
      int i = localCharToGlyphMapper.charToGlyph(paramInt1);
      if (i != localCharToGlyphMapper.getMissingGlyphCode())
        return compositeGlyphCode(paramInt2, i);
    }
    return charToGlyph(paramInt1);
  }

  public int charToGlyph(char paramChar)
  {
    int i = getCachedGlyphCode(paramChar);
    if (i == -1)
      i = convertToGlyph(paramChar);
    return i;
  }

  public boolean charsToGlyphsNS(int paramInt, char[] paramArrayOfChar, int[] paramArrayOfInt)
  {
    for (int i = 0; i < paramInt; ++i)
    {
      int j = paramArrayOfChar[i];
      if ((j >= 55296) && (j <= 56319) && (i < paramInt - 1))
      {
        k = paramArrayOfChar[(i + 1)];
        if ((k >= 56320) && (k <= 57343))
        {
          j = (j - 55296) * 1024 + k - 56320 + 65536;
          paramArrayOfInt[(i + 1)] = 65535;
        }
      }
      int k = paramArrayOfInt[i] = getCachedGlyphCode(j);
      if (k == -1)
        paramArrayOfInt[i] = convertToGlyph(j);
      if (j < 1424)
        break label276:
      if (j <= 1535)
        return true;
      if ((j >= 1536) && (j <= 1791))
        return true;
      if ((j >= 2304) && (j <= 3455))
        return true;
      if ((j >= 3584) && (j <= 3711))
        return true;
      if ((j >= 6016) && (j <= 6143))
        return true;
      if ((j >= 8204) && (j <= 8205))
        return true;
      if ((j >= 8234) && (j <= 8238))
        return true;
      if ((j >= 8298) && (j <= 8303))
        return true;
      label276: if (j >= 65536)
        ++i;
    }
    return false;
  }

  public void charsToGlyphs(int paramInt, char[] paramArrayOfChar, int[] paramArrayOfInt)
  {
    for (int i = 0; i < paramInt; ++i)
    {
      int k;
      int j = paramArrayOfChar[i];
      if ((j >= 55296) && (j <= 56319) && (i < paramInt - 1))
      {
        k = paramArrayOfChar[(i + 1)];
        if ((k >= 56320) && (k <= 57343))
        {
          j = (j - 55296) * 1024 + k - 56320 + 65536;
          int l = paramArrayOfInt[i] = getCachedGlyphCode(j);
          if (l == -1)
            paramArrayOfInt[i] = convertToGlyph(j);
          paramArrayOfInt[(++i)] = 65535;
        }
      }
      else
      {
        k = paramArrayOfInt[i] = getCachedGlyphCode(j);
        if (k == -1)
          paramArrayOfInt[i] = convertToGlyph(j);
      }
    }
  }

  public void charsToGlyphs(int paramInt, int[] paramArrayOfInt1, int[] paramArrayOfInt2)
  {
    for (int i = 0; i < paramInt; ++i)
    {
      int j = paramArrayOfInt1[i];
      paramArrayOfInt2[i] = getCachedGlyphCode(j);
      if (paramArrayOfInt2[i] == -1)
        paramArrayOfInt2[i] = convertToGlyph(j);
    }
  }
}