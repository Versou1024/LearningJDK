package sun.font;

import java.nio.ByteBuffer;
import java.util.Locale;
import java.util.logging.Logger;

public class TrueTypeGlyphMapper extends CharToGlyphMapper
{
  static final char REVERSE_SOLIDUS = 92;
  static final char JA_YEN = 165;
  static final char JA_FULLWIDTH_TILDE_CHAR = 65374;
  static final char JA_WAVE_DASH_CHAR = 12316;
  static final boolean isJAlocale = Locale.JAPAN.equals(Locale.getDefault());
  private final boolean needsJAremapping;
  private boolean remapJAWaveDash;
  TrueTypeFont font;
  CMap cmap;
  int numGlyphs;

  public TrueTypeGlyphMapper(TrueTypeFont paramTrueTypeFont)
  {
    this.font = paramTrueTypeFont;
    try
    {
      this.cmap = CMap.initialize(paramTrueTypeFont);
    }
    catch (Exception localException)
    {
      this.cmap = null;
    }
    if (this.cmap == null)
      handleBadCMAP();
    this.missingGlyph = 0;
    ByteBuffer localByteBuffer = paramTrueTypeFont.getTableBuffer(1835104368);
    this.numGlyphs = localByteBuffer.getChar(4);
    if ((FontManager.isSolaris) && (isJAlocale) && (paramTrueTypeFont.supportsJA()))
    {
      this.needsJAremapping = true;
      if ((FontManager.isSolaris8) && (getGlyphFromCMAP(12316) == this.missingGlyph))
        this.remapJAWaveDash = true;
    }
    else
    {
      this.needsJAremapping = false;
    }
  }

  public int getNumGlyphs()
  {
    return this.numGlyphs;
  }

  private char getGlyphFromCMAP(int paramInt)
  {
    int i;
    try
    {
      i = this.cmap.getGlyph(paramInt);
      if ((i < this.numGlyphs) || (i >= 65534))
        return i;
      if (FontManager.logging)
        FontManager.logger.warning(this.font + " out of range glyph id=" + Integer.toHexString(i) + " for char " + Integer.toHexString(paramInt));
      return (char)this.missingGlyph;
    }
    catch (Exception localException)
    {
      handleBadCMAP();
    }
    return (char)this.missingGlyph;
  }

  private void handleBadCMAP()
  {
    if (FontManager.logging)
      FontManager.logger.severe("Null Cmap for " + this.font + "substituting for this font");
    FontManager.deRegisterBadFont(this.font);
    this.cmap = CMap.theNullCmap;
  }

  private final char remapJAChar(char paramChar)
  {
    switch (paramChar)
    {
    case '\\':
      return 165;
    case '〜':
      if (!(this.remapJAWaveDash))
        break label42;
      return 65374;
    }
    label42: return paramChar;
  }

  private final int remapJAIntChar(int paramInt)
  {
    switch (paramInt)
    {
    case 92:
      return 165;
    case 12316:
      if (!(this.remapJAWaveDash))
        break label42;
      return 65374;
    }
    label42: return paramInt;
  }

  public int charToGlyph(char paramChar)
  {
    if (this.needsJAremapping)
      paramChar = remapJAChar(paramChar);
    int i = getGlyphFromCMAP(paramChar);
    if ((this.font.checkUseNatives()) && (i < this.font.glyphToCharMap.length))
      this.font.glyphToCharMap[i] = paramChar;
    return i;
  }

  public int charToGlyph(int paramInt)
  {
    if (this.needsJAremapping)
      paramInt = remapJAIntChar(paramInt);
    int i = getGlyphFromCMAP(paramInt);
    if ((this.font.checkUseNatives()) && (i < this.font.glyphToCharMap.length))
      this.font.glyphToCharMap[i] = (char)paramInt;
    return i;
  }

  public void charsToGlyphs(int paramInt, int[] paramArrayOfInt1, int[] paramArrayOfInt2)
  {
    for (int i = 0; i < paramInt; ++i)
    {
      if (this.needsJAremapping)
        paramArrayOfInt2[i] = getGlyphFromCMAP(remapJAIntChar(paramArrayOfInt1[i]));
      else
        paramArrayOfInt2[i] = getGlyphFromCMAP(paramArrayOfInt1[i]);
      if ((this.font.checkUseNatives()) && (paramArrayOfInt2[i] < this.font.glyphToCharMap.length))
        this.font.glyphToCharMap[paramArrayOfInt2[i]] = (char)paramArrayOfInt1[i];
    }
  }

  public void charsToGlyphs(int paramInt, char[] paramArrayOfChar, int[] paramArrayOfInt)
  {
    for (int i = 0; i < paramInt; ++i)
    {
      int j;
      if (this.needsJAremapping)
        j = remapJAChar(paramArrayOfChar[i]);
      else
        j = paramArrayOfChar[i];
      if ((j >= 55296) && (j <= 56319) && (i < paramInt - 1))
      {
        int k = paramArrayOfChar[(i + 1)];
        if ((k >= 56320) && (k <= 57343))
        {
          j = (j - 55296) * 1024 + k - 56320 + 65536;
          paramArrayOfInt[i] = getGlyphFromCMAP(j);
          paramArrayOfInt[(++i)] = 65535;
        }
      }
      else
      {
        paramArrayOfInt[i] = getGlyphFromCMAP(j);
        if ((this.font.checkUseNatives()) && (paramArrayOfInt[i] < this.font.glyphToCharMap.length))
          this.font.glyphToCharMap[paramArrayOfInt[i]] = (char)j;
      }
    }
  }

  public boolean charsToGlyphsNS(int paramInt, char[] paramArrayOfChar, int[] paramArrayOfInt)
  {
    for (int i = 0; i < paramInt; ++i)
    {
      int j;
      if (this.needsJAremapping)
        j = remapJAChar(paramArrayOfChar[i]);
      else
        j = paramArrayOfChar[i];
      if ((j >= 55296) && (j <= 56319) && (i < paramInt - 1))
      {
        int k = paramArrayOfChar[(i + 1)];
        if ((k >= 56320) && (k <= 57343))
        {
          j = (j - 55296) * 1024 + k - 56320 + 65536;
          paramArrayOfInt[(i + 1)] = 65535;
        }
      }
      paramArrayOfInt[i] = getGlyphFromCMAP(j);
      if ((this.font.checkUseNatives()) && (paramArrayOfInt[i] < this.font.glyphToCharMap.length))
        this.font.glyphToCharMap[paramArrayOfInt[i]] = (char)j;
      if (j < 1424)
        break label317:
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
      label317: if (j >= 65536)
        ++i;
    }
    return false;
  }

  boolean hasSupplementaryChars()
  {
    return ((this.cmap instanceof CMap.CMapFormat8) || (this.cmap instanceof CMap.CMapFormat10) || (this.cmap instanceof CMap.CMapFormat12));
  }
}