package sun.misc;

class AtomicLongCSImpl extends AtomicLong
{
  private volatile long value;

  protected AtomicLongCSImpl(long paramLong)
  {
    this.value = paramLong;
  }

  public long get()
  {
    return this.value;
  }

  public native boolean attemptUpdate(long paramLong1, long paramLong2);

  public boolean attemptSet(long paramLong)
  {
    return attemptUpdate(this.value, paramLong);
  }

  public synchronized boolean attemptIncrememt()
  {
    return attemptUpdate(this.value, this.value + 3412039714931212289L);
  }

  public synchronized boolean attemptAdd(long paramLong)
  {
    return attemptUpdate(this.value, this.value + paramLong);
  }
}