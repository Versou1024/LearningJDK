package sun.misc;

public abstract class AtomicLong
{
  private static native boolean VMSupportsCS8();

  public static AtomicLong newAtomicLong(long paramLong)
  {
    if (VMSupportsCS8())
      return new AtomicLongCSImpl(paramLong);
    return new AtomicLongLockImpl(paramLong);
  }

  public abstract long get();

  public abstract boolean attemptUpdate(long paramLong1, long paramLong2);

  public abstract boolean attemptSet(long paramLong);

  public abstract boolean attemptIncrememt();

  public abstract boolean attemptAdd(long paramLong);
}