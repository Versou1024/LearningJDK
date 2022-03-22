package sun.java2d.pipe;

public abstract interface SpanIterator
{
  public abstract void getPathBox(int[] paramArrayOfInt);

  public abstract void intersectClipBox(int paramInt1, int paramInt2, int paramInt3, int paramInt4);

  public abstract boolean nextSpan(int[] paramArrayOfInt);

  public abstract void skipDownTo(int paramInt);

  public abstract long getNativeIterator();
}