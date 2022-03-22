package sun.swing;

import java.util.ArrayList;
import java.util.List;

public class BakedArrayList extends ArrayList
{
  private int _hashCode;

  public BakedArrayList(int paramInt)
  {
    super(paramInt);
  }

  public BakedArrayList(List paramList)
  {
    this(paramList.size());
    int i = 0;
    int j = paramList.size();
    while (i < j)
    {
      add(paramList.get(i));
      ++i;
    }
    cacheHashCode();
  }

  public void cacheHashCode()
  {
    this._hashCode = 1;
    for (int i = size() - 1; i >= 0; --i)
      this._hashCode = (31 * this._hashCode + get(i).hashCode());
  }

  public int hashCode()
  {
    return this._hashCode;
  }

  public boolean equals(Object paramObject)
  {
    BakedArrayList localBakedArrayList = (BakedArrayList)paramObject;
    int i = size();
    if (localBakedArrayList.size() != i)
      return false;
    do
      if (i-- <= 0)
        break label45;
    while (get(i).equals(localBakedArrayList.get(i)));
    return false;
    label45: return true;
  }
}