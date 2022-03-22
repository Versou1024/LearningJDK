package sun.management.counter.perf;

import java.io.ObjectStreamException;
import java.nio.ByteBuffer;
import sun.management.counter.AbstractCounter;
import sun.management.counter.ByteArrayCounter;
import sun.management.counter.Units;
import sun.management.counter.Variability;

public class PerfByteArrayCounter extends AbstractCounter
  implements ByteArrayCounter
{
  ByteBuffer bb;

  PerfByteArrayCounter(String paramString, Units paramUnits, Variability paramVariability, int paramInt1, int paramInt2, ByteBuffer paramByteBuffer)
  {
    super(paramString, paramUnits, paramVariability, paramInt1, paramInt2);
    this.bb = paramByteBuffer;
  }

  public Object getValue()
  {
    return byteArrayValue();
  }

  public byte[] byteArrayValue()
  {
    this.bb.position(0);
    byte[] arrayOfByte = new byte[this.bb.limit()];
    this.bb.get(arrayOfByte);
    return arrayOfByte;
  }

  public byte byteAt(int paramInt)
  {
    this.bb.position(paramInt);
    return this.bb.get();
  }

  public String toString()
  {
    String str = getName() + ": " + new String(byteArrayValue()) + " " + getUnits();
    if (isInternal())
      return str + " [INTERNAL]";
    return str;
  }

  protected Object writeReplace()
    throws ObjectStreamException
  {
    return new ByteArrayCounterSnapshot(getName(), getUnits(), getVariability(), getFlags(), getVectorLength(), byteArrayValue());
  }
}