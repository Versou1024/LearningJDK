package sun.text;

final class CharArrayCodePointIterator extends CodePointIterator
{
  private char[] text;
  private int start;
  private int limit;
  private int index;

  public CharArrayCodePointIterator(char[] paramArrayOfChar)
  {
    this.text = paramArrayOfChar;
    this.limit = paramArrayOfChar.length;
  }

  public CharArrayCodePointIterator(char[] paramArrayOfChar, int paramInt1, int paramInt2)
  {
    if ((paramInt1 < 0) || (paramInt2 < paramInt1) || (paramInt2 > paramArrayOfChar.length))
      throw new IllegalArgumentException();
    this.text = paramArrayOfChar;
    this.start = (this.index = paramInt1);
    this.limit = paramInt2;
  }

  public void setToStart()
  {
    this.index = this.start;
  }

  public void setToLimit()
  {
    this.index = this.limit;
  }

  public int next()
  {
    if (this.index < this.limit)
    {
      char c1 = this.text[(this.index++)];
      if ((Character.isHighSurrogate(c1)) && (this.index < this.limit))
      {
        char c2 = this.text[this.index];
        if (Character.isLowSurrogate(c2))
        {
          this.index += 1;
          return Character.toCodePoint(c1, c2);
        }
      }
      return c1;
    }
    return -1;
  }

  public int prev()
  {
    if (this.index > this.start)
    {
      char c1 = this.text[(--this.index)];
      if ((Character.isLowSurrogate(c1)) && (this.index > this.start))
      {
        char c2 = this.text[(this.index - 1)];
        if (Character.isHighSurrogate(c2))
        {
          this.index -= 1;
          return Character.toCodePoint(c2, c1);
        }
      }
      return c1;
    }
    return -1;
  }

  public int charIndex()
  {
    return this.index;
  }
}