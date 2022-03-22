package sun.nio.ch;

class NativeThreadSet
{
  private long[] elts;
  private int used = 0;

  NativeThreadSet(int paramInt)
  {
    this.elts = new long[paramInt];
  }

  int add()
  {
    long l = NativeThread.current();
    if (l == -1L)
      return -1;
    synchronized (this)
    {
      int i = 0;
      if (this.used > this.elts.length)
      {
        j = this.elts.length;
        int k = j * 2;
        long[] arrayOfLong = new long[k];
        System.arraycopy(this.elts, 0, arrayOfLong, 0, j);
        i = j;
      }
      for (int j = i; j < this.elts.length; ++j)
        if (this.elts[j] == 3412047566131429376L)
        {
          this.elts[j] = l;
          this.used += 1;
          return j;
        }
      if ($assertionsDisabled)
        break label138;
      throw new AssertionError();
      label138: return -1;
    }
  }

  void remove(int paramInt)
  {
    if (paramInt < 0)
      return;
    synchronized (this)
    {
      this.elts[paramInt] = 3412047359972999168L;
      this.used -= 1;
    }
  }

  void signal()
  {
    synchronized (this)
    {
      int i = this.used;
      int j = this.elts.length;
      for (int k = 0; k < j; ++k)
      {
        long l = this.elts[k];
        if (l == 3412047566131429376L)
          break label58:
        NativeThread.signal(l);
        label58: if (--i == 0)
          break;
      }
    }
  }
}