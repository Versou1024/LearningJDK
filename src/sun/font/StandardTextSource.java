package sun.font;

import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;

public class StandardTextSource extends TextSource
{
  char[] chars;
  int start;
  int len;
  int cstart;
  int clen;
  int level;
  int flags;
  Font font;
  FontRenderContext frc;
  CoreMetrics cm;

  public StandardTextSource(char[] paramArrayOfChar, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6, Font paramFont, FontRenderContext paramFontRenderContext, CoreMetrics paramCoreMetrics)
  {
    if (paramArrayOfChar == null)
      throw new IllegalArgumentException("bad chars: null");
    if (paramInt3 < 0)
      throw new IllegalArgumentException("bad cstart: " + paramInt3);
    if (paramInt1 < paramInt3)
      throw new IllegalArgumentException("bad start: " + paramInt1 + " for cstart: " + paramInt3);
    if (paramInt4 < 0)
      throw new IllegalArgumentException("bad clen: " + paramInt4);
    if (paramInt3 + paramInt4 > paramArrayOfChar.length)
      throw new IllegalArgumentException("bad clen: " + paramInt4 + " cstart: " + paramInt3 + " for array len: " + paramArrayOfChar.length);
    if (paramInt2 < 0)
      throw new IllegalArgumentException("bad len: " + paramInt2);
    if (paramInt1 + paramInt2 > paramInt3 + paramInt4)
      throw new IllegalArgumentException("bad len: " + paramInt2 + " start: " + paramInt1 + " for cstart: " + paramInt3 + " clen: " + paramInt4);
    if (paramFont == null)
      throw new IllegalArgumentException("bad font: null");
    if (paramFontRenderContext == null)
      throw new IllegalArgumentException("bad frc: null");
    this.chars = paramArrayOfChar;
    this.start = paramInt1;
    this.len = paramInt2;
    this.cstart = paramInt3;
    this.clen = paramInt4;
    this.level = paramInt5;
    this.flags = paramInt6;
    this.font = paramFont;
    this.frc = paramFontRenderContext;
    if (paramCoreMetrics != null)
    {
      this.cm = paramCoreMetrics;
    }
    else
    {
      LineMetrics localLineMetrics = paramFont.getLineMetrics(paramArrayOfChar, paramInt3, paramInt4, paramFontRenderContext);
      this.cm = ((FontLineMetrics)localLineMetrics).cm;
    }
  }

  public StandardTextSource(char[] paramArrayOfChar, int paramInt1, int paramInt2, int paramInt3, int paramInt4, Font paramFont, FontRenderContext paramFontRenderContext, CoreMetrics paramCoreMetrics)
  {
    this(paramArrayOfChar, paramInt1, paramInt2, paramInt1, paramInt2, paramInt3, paramInt4, paramFont, paramFontRenderContext, paramCoreMetrics);
  }

  public StandardTextSource(char[] paramArrayOfChar, int paramInt1, int paramInt2, Font paramFont, FontRenderContext paramFontRenderContext)
  {
    this(paramArrayOfChar, 0, paramArrayOfChar.length, 0, paramArrayOfChar.length, paramInt1, paramInt2, paramFont, paramFontRenderContext, null);
  }

  public StandardTextSource(String paramString, int paramInt1, int paramInt2, Font paramFont, FontRenderContext paramFontRenderContext)
  {
    this(paramString.toCharArray(), 0, paramString.length(), 0, paramString.length(), paramInt1, paramInt2, paramFont, paramFontRenderContext, null);
  }

  public char[] getChars()
  {
    return this.chars;
  }

  public int getStart()
  {
    return this.start;
  }

  public int getLength()
  {
    return this.len;
  }

  public int getContextStart()
  {
    return this.cstart;
  }

  public int getContextLength()
  {
    return this.clen;
  }

  public int getLayoutFlags()
  {
    return this.flags;
  }

  public int getBidiLevel()
  {
    return this.level;
  }

  public Font getFont()
  {
    return this.font;
  }

  public FontRenderContext getFRC()
  {
    return this.frc;
  }

  public CoreMetrics getCoreMetrics()
  {
    return this.cm;
  }

  public TextSource getSubSource(int paramInt1, int paramInt2, int paramInt3)
  {
    if ((paramInt1 < 0) || (paramInt2 < 0) || (paramInt1 + paramInt2 > this.len))
      throw new IllegalArgumentException("bad start (" + paramInt1 + ") or length (" + paramInt2 + ")");
    int i = this.level;
    if (paramInt3 != 2)
    {
      int j = ((this.flags & 0x8) == 0) ? 1 : 0;
      if ((((paramInt3 != 0) || (j == 0))) && (((paramInt3 != 1) || (j != 0))))
        throw new IllegalArgumentException("direction flag is invalid");
      i = (j != 0) ? 0 : 1;
    }
    return new StandardTextSource(this.chars, this.start + paramInt1, paramInt2, this.cstart, this.clen, i, this.flags, this.font, this.frc, this.cm);
  }

  public String toString()
  {
    return toString(true);
  }

  public String toString(boolean paramBoolean)
  {
    int i;
    int j;
    StringBuffer localStringBuffer = new StringBuffer(toString());
    localStringBuffer.append("[start:");
    localStringBuffer.append(this.start);
    localStringBuffer.append(", len:");
    localStringBuffer.append(this.len);
    localStringBuffer.append(", cstart:");
    localStringBuffer.append(this.cstart);
    localStringBuffer.append(", clen:");
    localStringBuffer.append(this.clen);
    localStringBuffer.append(", chars:\"");
    if (paramBoolean == true)
    {
      i = this.cstart;
      j = this.cstart + this.clen;
    }
    else
    {
      i = this.start;
      j = this.start + this.len;
    }
    for (int k = i; k < j; ++k)
    {
      if (k > i)
        localStringBuffer.append(" ");
      localStringBuffer.append(Integer.toHexString(this.chars[k]));
    }
    localStringBuffer.append("\"");
    localStringBuffer.append(", level:");
    localStringBuffer.append(this.level);
    localStringBuffer.append(", flags:");
    localStringBuffer.append(this.flags);
    localStringBuffer.append(", font:");
    localStringBuffer.append(this.font);
    localStringBuffer.append(", frc:");
    localStringBuffer.append(this.frc);
    localStringBuffer.append(", cm:");
    localStringBuffer.append(this.cm);
    localStringBuffer.append("]");
    return localStringBuffer.toString();
  }
}