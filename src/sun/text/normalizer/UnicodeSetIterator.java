package sun.text.normalizer;

import java.util.Iterator;
import java.util.TreeSet;

public class UnicodeSetIterator
{
  public static int IS_STRING = -1;
  public int codepoint;
  public int codepointEnd;
  public String string;
  private UnicodeSet set;
  private int endRange = 0;
  private int range = 0;
  protected int endElement;
  protected int nextElement;
  private Iterator stringIterator = null;

  public UnicodeSetIterator(UnicodeSet paramUnicodeSet)
  {
    reset(paramUnicodeSet);
  }

  public boolean nextRange()
  {
    if (this.nextElement <= this.endElement)
    {
      this.codepointEnd = this.endElement;
      this.codepoint = this.nextElement;
      this.nextElement = (this.endElement + 1);
      return true;
    }
    if (this.range < this.endRange)
    {
      loadRange(++this.range);
      this.codepointEnd = this.endElement;
      this.codepoint = this.nextElement;
      this.nextElement = (this.endElement + 1);
      return true;
    }
    if (this.stringIterator == null)
      return false;
    this.codepoint = IS_STRING;
    this.string = ((String)this.stringIterator.next());
    if (!(this.stringIterator.hasNext()))
      this.stringIterator = null;
    return true;
  }

  public void reset(UnicodeSet paramUnicodeSet)
  {
    this.set = paramUnicodeSet;
    reset();
  }

  public void reset()
  {
    this.endRange = (this.set.getRangeCount() - 1);
    this.range = 0;
    this.endElement = -1;
    this.nextElement = 0;
    if (this.endRange >= 0)
      loadRange(this.range);
    this.stringIterator = null;
    if (this.set.strings != null)
    {
      this.stringIterator = this.set.strings.iterator();
      if (!(this.stringIterator.hasNext()))
        this.stringIterator = null;
    }
  }

  protected void loadRange(int paramInt)
  {
    this.nextElement = this.set.getRangeStart(paramInt);
    this.endElement = this.set.getRangeEnd(paramInt);
  }
}