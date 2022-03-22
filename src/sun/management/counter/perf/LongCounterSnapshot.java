package sun.management.counter.perf;

import sun.management.counter.AbstractCounter;
import sun.management.counter.LongCounter;
import sun.management.counter.Units;
import sun.management.counter.Variability;

class LongCounterSnapshot extends AbstractCounter
  implements LongCounter
{
  long value;

  LongCounterSnapshot(String paramString, Units paramUnits, Variability paramVariability, int paramInt, long paramLong)
  {
    super(paramString, paramUnits, paramVariability, paramInt);
    this.value = paramLong;
  }

  public Object getValue()
  {
    return new Long(this.value);
  }

  public long longValue()
  {
    return this.value;
  }
}