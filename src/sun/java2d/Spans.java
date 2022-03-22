package sun.java2d;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

public class Spans
{
  private static final int kMaxAddsSinceSort = 256;
  private List mSpans = new Vector(256);
  private int mAddsSinceSort = 0;

  public void add(float paramFloat1, float paramFloat2)
  {
    if (this.mSpans != null)
    {
      this.mSpans.add(new Span(paramFloat1, paramFloat2));
      if (++this.mAddsSinceSort >= 256)
        sortAndCollapse();
    }
  }

  public void addInfinite()
  {
    this.mSpans = null;
  }

  public boolean intersects(float paramFloat1, float paramFloat2)
  {
    int i;
    if (this.mSpans != null)
    {
      if (this.mAddsSinceSort > 0)
        sortAndCollapse();
      int j = Collections.binarySearch(this.mSpans, new Span(paramFloat1, paramFloat2), SpanIntersection.instance);
      i = (j >= 0) ? 1 : 0;
    }
    else
    {
      i = 1;
    }
    return i;
  }

  private void sortAndCollapse()
  {
    Collections.sort(this.mSpans);
    this.mAddsSinceSort = 0;
    Iterator localIterator = this.mSpans.iterator();
    Object localObject = null;
    if (localIterator.hasNext())
      localObject = (Span)localIterator.next();
    while (localIterator.hasNext())
    {
      Span localSpan = (Span)localIterator.next();
      if (((Span)localObject).subsume(localSpan))
        localIterator.remove();
      else
        localObject = localSpan;
    }
  }

  static class Span
  implements Comparable
  {
    private float mStart;
    private float mEnd;

    Span(float paramFloat1, float paramFloat2)
    {
      this.mStart = paramFloat1;
      this.mEnd = paramFloat2;
    }

    final float getStart()
    {
      return this.mStart;
    }

    final float getEnd()
    {
      return this.mEnd;
    }

    final void setStart(float paramFloat)
    {
      this.mStart = paramFloat;
    }

    final void setEnd(float paramFloat)
    {
      this.mEnd = paramFloat;
    }

    boolean subsume(Span paramSpan)
    {
      boolean bool = contains(paramSpan.mStart);
      if ((bool) && (paramSpan.mEnd > this.mEnd))
        this.mEnd = paramSpan.mEnd;
      return bool;
    }

    boolean contains(float paramFloat)
    {
      return ((this.mStart <= paramFloat) && (paramFloat < this.mEnd));
    }

    public int compareTo(Object paramObject)
    {
      int i;
      Span localSpan = (Span)paramObject;
      float f = localSpan.getStart();
      if (this.mStart < f)
        i = -1;
      else if (this.mStart > f)
        i = 1;
      else
        i = 0;
      return i;
    }

    public String toString()
    {
      return "Span: " + this.mStart + " to " + this.mEnd;
    }
  }

  static class SpanIntersection
  implements Comparator
  {
    static final SpanIntersection instance = new SpanIntersection();

    public int compare(Object paramObject1, Object paramObject2)
    {
      int i;
      Spans.Span localSpan1 = (Spans.Span)paramObject1;
      Spans.Span localSpan2 = (Spans.Span)paramObject2;
      if (localSpan1.getEnd() <= localSpan2.getStart())
        i = -1;
      else if (localSpan1.getStart() >= localSpan2.getEnd())
        i = 1;
      else
        i = 0;
      return i;
    }
  }
}