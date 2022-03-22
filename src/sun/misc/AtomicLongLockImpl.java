package sun.misc;

class AtomicLongLockImpl extends AtomicLong
{
  private volatile long value;

  protected AtomicLongLockImpl(long paramLong)
  {
    this.value = paramLong;
  }

  public long get()
  {
    return this.value;
  }

  public synchronized boolean attemptSet(long paramLong)
  {
    this.value = paramLong;
    return true;
  }

  public synchronized boolean attemptUpdate(long paramLong1, long paramLong2)
  {
    if (this.value == paramLong1)
    {
      this.value = paramLong2;
      return true;
    }
    return false;
  }

  public synchronized boolean attemptIncrememt()
  {
    this.value += 3412047050735353857L;
    return true;
  }

  public synchronized boolean attemptAdd(long paramLong)
  {
    this.value += paramLong;
    return true;
  }
}