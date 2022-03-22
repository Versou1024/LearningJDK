package sun.management.counter.perf;

import java.io.ObjectStreamException;
import java.nio.LongBuffer;
import sun.management.counter.AbstractCounter;
import sun.management.counter.LongArrayCounter;
import sun.management.counter.Units;
import sun.management.counter.Variability;

public class PerfLongArrayCounter extends AbstractCounter
  implements LongArrayCounter
{
  LongBuffer lb;

  PerfLongArrayCounter(String paramString, Units paramUnits, Variability paramVariability, int paramInt1, int paramInt2, LongBuffer paramLongBuffer)
  {
    super(paramString, paramUnits, paramVariability, paramInt1, paramInt2);
    this.lb = paramLongBuffer;
  }

  public Object getValue()
  {
    return longArrayValue();
  }

  public long[] longArrayValue()
  {
    this.lb.position(0);
    long[] arrayOfLong = new long[this.lb.limit()];
    this.lb.get(arrayOfLong);
    return arrayOfLong;
  }

  public long longAt(int paramInt)
  {
    this.lb.position(paramInt);
    return this.lb.get();
  }

  protected Object writeReplace()
    throws ObjectStreamException
  {
    return new LongArrayCounterSnapshot(getName(), getUnits(), getVariability(), getFlags(), getVectorLength(), longArrayValue());
  }
}