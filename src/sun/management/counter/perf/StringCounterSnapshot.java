package sun.management.counter.perf;

import sun.management.counter.AbstractCounter;
import sun.management.counter.StringCounter;
import sun.management.counter.Units;
import sun.management.counter.Variability;

class StringCounterSnapshot extends AbstractCounter
  implements StringCounter
{
  String value;

  StringCounterSnapshot(String paramString1, Units paramUnits, Variability paramVariability, int paramInt, String paramString2)
  {
    super(paramString1, paramUnits, paramVariability, paramInt);
    this.value = paramString2;
  }

  public Object getValue()
  {
    return this.value;
  }

  public String stringValue()
  {
    return this.value;
  }
}