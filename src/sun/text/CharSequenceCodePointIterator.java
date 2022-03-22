package sun.text;

final class CharSequenceCodePointIterator extends CodePointIterator
{
  private CharSequence text;
  private int index;

  public CharSequenceCodePointIterator(CharSequence paramCharSequence)
  {
    this.text = paramCharSequence;
  }

  public void setToStart()
  {
    this.index = 0;
  }

  public void setToLimit()
  {
    this.index = this.text.length();
  }

  public int next()
  {
    if (this.index < this.text.length())
    {
      char c1 = this.text.charAt(this.index++);
      if ((Character.isHighSurrogate(c1)) && (this.index < this.text.length()))
      {
        char c2 = this.text.charAt(this.index + 1);
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
    if (this.index > 0)
    {
      char c1 = this.text.charAt(--this.index);
      if ((Character.isLowSurrogate(c1)) && (this.index > 0))
      {
        char c2 = this.text.charAt(this.index - 1);
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