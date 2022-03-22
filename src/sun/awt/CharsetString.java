package sun.awt;

public class CharsetString
{
  public char[] charsetChars;
  public int offset;
  public int length;
  public FontDescriptor fontDescriptor;

  public CharsetString(char[] paramArrayOfChar, int paramInt1, int paramInt2, FontDescriptor paramFontDescriptor)
  {
    this.charsetChars = paramArrayOfChar;
    this.offset = paramInt1;
    this.length = paramInt2;
    this.fontDescriptor = paramFontDescriptor;
  }
}