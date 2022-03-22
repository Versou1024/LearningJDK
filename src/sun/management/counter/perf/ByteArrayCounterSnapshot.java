package sun.management.counter.perf;

import sun.management.counter.AbstractCounter;
import sun.management.counter.ByteArrayCounter;
import sun.management.counter.Units;
import sun.management.counter.Variability;

class ByteArrayCounterSnapshot extends AbstractCounter
  implements ByteArrayCounter
{
  byte[] value;

  ByteArrayCounterSnapshot(String paramString, Units paramUnits, Variability paramVariability, int paramInt1, int paramInt2, byte[] paramArrayOfByte)
  {
    super(paramString, paramUnits, paramVariability, paramInt1, paramInt2);
    this.value = paramArrayOfByte;
  }

  public Object getValue()
  {
    return this.value;
  }

  public byte[] byteArrayValue()
  {
    return this.value;
  }

  public byte byteAt(int paramInt)
  {
    return this.value[paramInt];
  }
}