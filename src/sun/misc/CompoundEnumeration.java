package sun.misc;

import java.util.Enumeration;
import java.util.NoSuchElementException;

public class CompoundEnumeration<E>
  implements Enumeration<E>
{
  private Enumeration[] enums;
  private int index = 0;

  public CompoundEnumeration(Enumeration[] paramArrayOfEnumeration)
  {
    this.enums = paramArrayOfEnumeration;
  }

  private boolean next()
  {
    while (this.index < this.enums.length)
    {
      if ((this.enums[this.index] != null) && (this.enums[this.index].hasMoreElements()))
        return true;
      this.index += 1;
    }
    return false;
  }

  public boolean hasMoreElements()
  {
    return next();
  }

  public E nextElement()
  {
    if (!(next()))
      throw new NoSuchElementException();
    return this.enums[this.index].nextElement();
  }
}