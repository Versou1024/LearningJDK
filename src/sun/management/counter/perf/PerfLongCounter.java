package sun.management.counter.perf;

import java.io.ObjectStreamException;
import java.nio.LongBuffer;
import sun.management.counter.AbstractCounter;
import sun.management.counter.LongCounter;
import sun.management.counter.Units;
import sun.management.counter.Variability;

public class PerfLongCounter extends AbstractCounter
  implements LongCounter
{
  LongBuffer lb;

  PerfLongCounter(String paramString, Units paramUnits, Variability paramVariability, int paramInt, LongBuffer paramLongBuffer)
  {
    super(paramString, paramUnits, paramVariability, paramInt);
    this.lb = paramLongBuffer;
  }

  public Object getValue()
  {
    return new Long(this.lb.get(0));
  }

  public long longValue()
  {
    return this.lb.get(0);
  }

  protected Object writeReplace()
    throws ObjectStreamException
  {
    return new LongCounterSnapshot(getName(), getUnits(), getVariability(), getFlags(), longValue());
  }
}