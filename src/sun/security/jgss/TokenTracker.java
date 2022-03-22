package sun.security.jgss;

import java.util.LinkedList;
import org.ietf.jgss.MessageProp;

public class TokenTracker
{
  static final int MAX_INTERVALS = 5;
  private int initNumber;
  private int windowStart;
  private int expectedNumber;
  private int windowStartIndex = 0;
  private LinkedList list = new LinkedList();

  public TokenTracker(int paramInt)
  {
    this.initNumber = paramInt;
    this.windowStart = paramInt;
    this.expectedNumber = paramInt;
    Entry localEntry = new Entry(this, paramInt - 1);
    this.list.add(localEntry);
  }

  private int getIntervalIndex(int paramInt)
  {
    Entry localEntry = null;
    for (int i = this.list.size() - 1; i >= 0; --i)
    {
      localEntry = (Entry)this.list.get(i);
      if (localEntry.compareTo(paramInt) <= 0)
        break;
    }
    return i;
  }

  public final synchronized void getProps(int paramInt, MessageProp paramMessageProp)
  {
    boolean bool1 = false;
    boolean bool2 = false;
    boolean bool3 = false;
    boolean bool4 = false;
    int i = getIntervalIndex(paramInt);
    Entry localEntry = null;
    if (i != -1)
      localEntry = (Entry)this.list.get(i);
    if (paramInt == this.expectedNumber)
      this.expectedNumber += 1;
    else if ((localEntry != null) && (localEntry.contains(paramInt)))
      bool4 = true;
    else if (this.expectedNumber >= this.initNumber)
      if (paramInt > this.expectedNumber)
        bool1 = true;
      else if (paramInt >= this.windowStart)
        bool3 = true;
      else if (paramInt >= this.initNumber)
        bool2 = true;
      else
        bool1 = true;
    else if (paramInt > this.expectedNumber)
      if (paramInt < this.initNumber)
        bool1 = true;
      else if (this.windowStart >= this.initNumber)
        if (paramInt >= this.windowStart)
          bool3 = true;
        else
          bool2 = true;
      else
        bool2 = true;
    else if (this.windowStart > this.expectedNumber)
      bool3 = true;
    else if (paramInt < this.windowStart)
      bool2 = true;
    else
      bool3 = true;
    if ((!(bool4)) && (!(bool2)))
      add(paramInt, i);
    if (bool1)
      this.expectedNumber = (paramInt + 1);
    paramMessageProp.setSupplementaryStates(bool4, bool2, bool3, bool1, 0, null);
  }

  private void add(int paramInt1, int paramInt2)
  {
    Entry localEntry1;
    Entry localEntry2 = null;
    Entry localEntry3 = null;
    int i = 0;
    int j = 0;
    if (paramInt2 != -1)
    {
      localEntry2 = (Entry)this.list.get(paramInt2);
      if (paramInt1 == localEntry2.getEnd() + 1)
      {
        localEntry2.setEnd(paramInt1);
        i = 1;
      }
    }
    int k = paramInt2 + 1;
    if (k < this.list.size())
    {
      localEntry3 = (Entry)this.list.get(k);
      if (paramInt1 == localEntry3.getStart() - 1)
      {
        if (i == 0)
        {
          localEntry3.setStart(paramInt1);
        }
        else
        {
          localEntry3.setStart(localEntry2.getStart());
          this.list.remove(paramInt2);
          if (this.windowStartIndex > paramInt2)
            this.windowStartIndex -= 1;
        }
        j = 1;
      }
    }
    if ((j != 0) || (i != 0))
      return;
    if (this.list.size() < 5)
    {
      localEntry1 = new Entry(this, paramInt1);
      if (paramInt2 < this.windowStartIndex)
        this.windowStartIndex += 1;
    }
    else
    {
      int l = this.windowStartIndex;
      if (this.windowStartIndex == this.list.size() - 1)
        this.windowStartIndex = 0;
      localEntry1 = (Entry)this.list.remove(l);
      this.windowStart = ((Entry)this.list.get(this.windowStartIndex)).getStart();
      localEntry1.setStart(paramInt1);
      localEntry1.setEnd(paramInt1);
      if (paramInt2 >= l)
        --paramInt2;
      else if (l != this.windowStartIndex)
        if (paramInt2 == -1)
          this.windowStart = paramInt1;
      else
        this.windowStartIndex += 1;
    }
    this.list.add(paramInt2 + 1, localEntry1);
  }

  public String toString()
  {
    StringBuffer localStringBuffer = new StringBuffer("TokenTracker: ");
    localStringBuffer.append(" initNumber=").append(this.initNumber);
    localStringBuffer.append(" windowStart=").append(this.windowStart);
    localStringBuffer.append(" expectedNumber=").append(this.expectedNumber);
    localStringBuffer.append(" windowStartIndex=").append(this.windowStartIndex);
    localStringBuffer.append("\n\tIntervals are: {");
    for (int i = 0; i < this.list.size(); ++i)
    {
      if (i != 0)
        localStringBuffer.append(", ");
      localStringBuffer.append(this.list.get(i).toString());
    }
    localStringBuffer.append('}');
    return localStringBuffer.toString();
  }

  class Entry
  {
    private int start;
    private int end;

    Entry(, int paramInt)
    {
      this.start = paramInt;
      this.end = paramInt;
    }

    final int compareTo()
    {
      if (this.start > paramInt)
        return 1;
      if (this.end < paramInt)
        return -1;
      return 0;
    }

    final boolean contains()
    {
      return ((paramInt >= this.start) && (paramInt <= this.end));
    }

    final void append()
    {
      if (paramInt == this.end + 1)
        this.end = paramInt;
    }

    final void setInterval(, int paramInt2)
    {
      this.start = paramInt1;
      this.end = paramInt2;
    }

    final void setEnd()
    {
      this.end = paramInt;
    }

    final void setStart()
    {
      this.start = paramInt;
    }

    final int getStart()
    {
      return this.start;
    }

    final int getEnd()
    {
      return this.end;
    }

    public String toString()
    {
      return "[" + this.start + ", " + this.end + "]";
    }
  }
}