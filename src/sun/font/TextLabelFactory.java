package sun.font;

import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.text.Bidi;

public class TextLabelFactory
{
  private FontRenderContext frc;
  private char[] text;
  private Bidi bidi;
  private Bidi lineBidi;
  private int flags;
  private int lineStart;
  private int lineLimit;

  public TextLabelFactory(FontRenderContext paramFontRenderContext, char[] paramArrayOfChar, Bidi paramBidi, int paramInt)
  {
    this.frc = paramFontRenderContext;
    this.text = paramArrayOfChar;
    this.bidi = paramBidi;
    this.flags = paramInt;
    this.lineBidi = paramBidi;
    this.lineStart = 0;
    this.lineLimit = paramArrayOfChar.length;
  }

  public FontRenderContext getFontRenderContext()
  {
    return this.frc;
  }

  public char[] getText()
  {
    return this.text;
  }

  public Bidi getParagraphBidi()
  {
    return this.bidi;
  }

  public Bidi getLineBidi()
  {
    return this.lineBidi;
  }

  public int getLayoutFlags()
  {
    return this.flags;
  }

  public int getLineStart()
  {
    return this.lineStart;
  }

  public int getLineLimit()
  {
    return this.lineLimit;
  }

  public void setLineContext(int paramInt1, int paramInt2)
  {
    this.lineStart = paramInt1;
    this.lineLimit = paramInt2;
    if (this.bidi != null)
      this.lineBidi = this.bidi.createLineBidi(paramInt1, paramInt2);
  }

  public ExtendedTextLabel createExtended(Font paramFont, CoreMetrics paramCoreMetrics, Decoration paramDecoration, int paramInt1, int paramInt2)
  {
    if ((paramInt1 >= paramInt2) || (paramInt1 < this.lineStart) || (paramInt2 > this.lineLimit))
      throw new IllegalArgumentException("bad start: " + paramInt1 + " or limit: " + paramInt2);
    int i = (this.lineBidi == null) ? 0 : this.lineBidi.getLevelAt(paramInt1 - this.lineStart);
    int j = ((this.lineBidi == null) || (this.lineBidi.baseIsLeftToRight())) ? 0 : 1;
    int k = this.flags & 0xFFFFFFF6;
    if ((i & 0x1) != 0)
      k |= 1;
    if ((j & 0x1) != 0)
      k |= 8;
    StandardTextSource localStandardTextSource = new StandardTextSource(this.text, paramInt1, paramInt2 - paramInt1, this.lineStart, this.lineLimit - this.lineStart, i, k, paramFont, this.frc, paramCoreMetrics);
    return new ExtendedTextSourceLabel(localStandardTextSource, paramDecoration);
  }

  public TextLabel createSimple(Font paramFont, CoreMetrics paramCoreMetrics, int paramInt1, int paramInt2)
  {
    if ((paramInt1 >= paramInt2) || (paramInt1 < this.lineStart) || (paramInt2 > this.lineLimit))
      throw new IllegalArgumentException("bad start: " + paramInt1 + " or limit: " + paramInt2);
    int i = (this.lineBidi == null) ? 0 : this.lineBidi.getLevelAt(paramInt1 - this.lineStart);
    int j = ((this.lineBidi == null) || (this.lineBidi.baseIsLeftToRight())) ? 0 : 1;
    int k = this.flags & 0xFFFFFFF6;
    if ((i & 0x1) != 0)
      k |= 1;
    if ((j & 0x1) != 0)
      k |= 8;
    StandardTextSource localStandardTextSource = new StandardTextSource(this.text, paramInt1, paramInt2 - paramInt1, this.lineStart, this.lineLimit - this.lineStart, i, k, paramFont, this.frc, paramCoreMetrics);
    return new TextSourceLabel(localStandardTextSource);
  }
}