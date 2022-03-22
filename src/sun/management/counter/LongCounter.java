package sun.management.counter;

public abstract interface LongCounter extends Counter
{
  public abstract long longValue();
}