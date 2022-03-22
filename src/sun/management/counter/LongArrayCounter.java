package sun.management.counter;

public abstract interface LongArrayCounter extends Counter
{
  public abstract long[] longArrayValue();

  public abstract long longAt(int paramInt);
}