package sun.management.counter.perf;

import sun.management.counter.AbstractCounter;
import sun.management.counter.LongArrayCounter;
import sun.management.counter.Units;
import sun.management.counter.Variability;

class LongArrayCounterSnapshot extends AbstractCounter
  implements LongArrayCounter
{
  long[] value;

  LongArrayCounterSnapshot(String paramString, Units paramUnits, Variability paramVariability, int paramInt1, int paramInt2, long[] paramArrayOfLong)
  {
    super(paramString, paramUnits, paramVariability, paramInt1, paramInt2);
    this.value = paramArrayOfLong;
  }

  public Object getValue()
  {
    return this.value;
  }

  public long[] longArrayValue()
  {
    return this.value;
  }

  public long longAt(int paramInt)
  {
    return this.value[paramInt];
  }
}