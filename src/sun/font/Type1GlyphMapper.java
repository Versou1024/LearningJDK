package sun.font;

public final class Type1GlyphMapper extends CharToGlyphMapper
{
  Type1Font font;

  public Type1GlyphMapper(Type1Font paramType1Font)
  {
    this.font = paramType1Font;
    initMapper();
  }

  private void initMapper()
  {
    if (this.font.pScaler == 3412046672778231808L)
      this.font.getScaler();
    this.missingGlyph = this.font.getMissingGlyphCode(this.font.pScaler);
  }

  public int getNumGlyphs()
  {
    return this.font.getNumGlyphs(this.font.pScaler);
  }

  public int getMissingGlyphCode()
  {
    return this.missingGlyph;
  }

  public boolean canDisplay(char paramChar)
  {
    return (this.font.getGlyphCode(this.font.pScaler, paramChar) != this.missingGlyph);
  }

  public int charToGlyph(char paramChar)
  {
    return this.font.getGlyphCode(this.font.pScaler, paramChar);
  }

  public int charToGlyph(int paramInt)
  {
    if ((paramInt < 0) || (paramInt > 65535))
      return this.missingGlyph;
    return this.font.getGlyphCode(this.font.pScaler, (char)paramInt);
  }

  public void charsToGlyphs(int paramInt, char[] paramArrayOfChar, int[] paramArrayOfInt)
  {
    for (int i = 0; i < paramInt; ++i)
    {
      int j = paramArrayOfChar[i];
      if ((j >= 55296) && (j <= 56319) && (i < paramInt - 1))
      {
        int k = paramArrayOfChar[(i + 1)];
        if ((k >= 56320) && (k <= 57343))
        {
          j = (j - 55296) * 1024 + k - 56320 + 65536;
          paramArrayOfInt[(i + 1)] = 65535;
        }
      }
      if ((j < 0) || (j > 65535))
        paramArrayOfInt[i] = this.missingGlyph;
      else
        paramArrayOfInt[i] = this.font.getGlyphCode(this.font.pScaler, (char)j);
      if (j >= 65536)
        ++i;
    }
  }

  public void charsToGlyphs(int paramInt, int[] paramArrayOfInt1, int[] paramArrayOfInt2)
  {
    for (int i = 0; i < paramInt; ++i)
      if ((paramArrayOfInt1[i] < 0) || (paramArrayOfInt1[i] > 65535))
        paramArrayOfInt2[i] = this.missingGlyph;
      else
        paramArrayOfInt2[i] = this.font.getGlyphCode(this.font.pScaler, (char)paramArrayOfInt1[i]);
  }

  public boolean charsToGlyphsNS(int paramInt, char[] paramArrayOfChar, int[] paramArrayOfInt)
  {
    for (int i = 0; i < paramInt; ++i)
    {
      int j = paramArrayOfChar[i];
      if ((j >= 55296) && (j <= 56319) && (i < paramInt - 1))
      {
        int k = paramArrayOfChar[(i + 1)];
        if ((k >= 56320) && (k <= 57343))
        {
          j = (j - 55296) * 1024 + k - 56320 + 65536;
          paramArrayOfInt[(i + 1)] = 65535;
        }
      }
      if ((j < 0) || (j > 65535))
        paramArrayOfInt[i] = this.missingGlyph;
      else
        paramArrayOfInt[i] = this.font.getGlyphCode(this.font.pScaler, (char)j);
      if (j < 1424)
        break label291:
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
      label291: if (j >= 65536)
        ++i;
    }
    return false;
  }
}