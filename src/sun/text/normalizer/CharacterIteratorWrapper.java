package sun.text.normalizer;

import java.text.CharacterIterator;

public class CharacterIteratorWrapper extends UCharacterIterator
{
  private CharacterIterator iterator;

  public CharacterIteratorWrapper(CharacterIterator paramCharacterIterator)
  {
    if (paramCharacterIterator == null)
      throw new IllegalArgumentException();
    this.iterator = paramCharacterIterator;
  }

  public int current()
  {
    int i = this.iterator.current();
    if (i == 65535)
      return -1;
    return i;
  }

  public int getLength()
  {
    return (this.iterator.getEndIndex() - this.iterator.getBeginIndex());
  }

  public int getIndex()
  {
    return this.iterator.getIndex();
  }

  public int next()
  {
    int i = this.iterator.current();
    this.iterator.next();
    if (i == 65535)
      return -1;
    return i;
  }

  public int previous()
  {
    int i = this.iterator.previous();
    if (i == 65535)
      return -1;
    return i;
  }

  public void setIndex(int paramInt)
  {
    this.iterator.setIndex(paramInt);
  }

  public int getText(char[] paramArrayOfChar, int paramInt)
  {
    int i = this.iterator.getEndIndex() - this.iterator.getBeginIndex();
    int j = this.iterator.getIndex();
    if ((paramInt < 0) || (paramInt + i > paramArrayOfChar.length))
      throw new IndexOutOfBoundsException(Integer.toString(i));
    for (int k = this.iterator.first(); k != 65535; k = this.iterator.next())
      paramArrayOfChar[(paramInt++)] = k;
    this.iterator.setIndex(j);
    return i;
  }

  public Object clone()
  {
    CharacterIteratorWrapper localCharacterIteratorWrapper;
    try
    {
      localCharacterIteratorWrapper = (CharacterIteratorWrapper)super.clone();
      localCharacterIteratorWrapper.iterator = ((CharacterIterator)this.iterator.clone());
      return localCharacterIteratorWrapper;
    }
    catch (CloneNotSupportedException localCloneNotSupportedException)
    {
    }
    return null;
  }
}