package sun.text;

import java.text.CharacterIterator;

final class CharacterIteratorCodePointIterator extends CodePointIterator
{
  private CharacterIterator iter;

  public CharacterIteratorCodePointIterator(CharacterIterator paramCharacterIterator)
  {
    this.iter = paramCharacterIterator;
  }

  public void setToStart()
  {
    this.iter.setIndex(this.iter.getBeginIndex());
  }

  public void setToLimit()
  {
    this.iter.setIndex(this.iter.getEndIndex());
  }

  public int next()
  {
    int i = this.iter.current();
    if (i != 65535)
    {
      int j = this.iter.next();
      if ((Character.isHighSurrogate(i)) && (j != 65535) && (Character.isLowSurrogate(j)))
      {
        this.iter.next();
        return Character.toCodePoint(i, j);
      }
      return i;
    }
    return -1;
  }

  public int prev()
  {
    int i = this.iter.previous();
    if (i != 65535)
    {
      if (Character.isLowSurrogate(i))
      {
        char c = this.iter.previous();
        if (Character.isHighSurrogate(c))
          return Character.toCodePoint(c, i);
        this.iter.next();
      }
      return i;
    }
    return -1;
  }

  public int charIndex()
  {
    return this.iter.getIndex();
  }
}