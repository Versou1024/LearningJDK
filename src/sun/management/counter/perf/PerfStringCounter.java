package sun.management.counter.perf;

import java.io.ObjectStreamException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import sun.management.counter.StringCounter;
import sun.management.counter.Units;
import sun.management.counter.Variability;

public class PerfStringCounter extends PerfByteArrayCounter
  implements StringCounter
{
  PerfStringCounter(String paramString, Variability paramVariability, int paramInt, ByteBuffer paramByteBuffer)
  {
    this(paramString, paramVariability, paramInt, paramByteBuffer.limit(), paramByteBuffer);
  }

  PerfStringCounter(String paramString, Variability paramVariability, int paramInt1, int paramInt2, ByteBuffer paramByteBuffer)
  {
    super(paramString, Units.STRING, paramVariability, paramInt1, paramInt2, paramByteBuffer);
  }

  public boolean isVector()
  {
    return false;
  }

  public int getVectorLength()
  {
    return 0;
  }

  public Object getValue()
  {
    return stringValue();
  }

  public String stringValue()
  {
    String str = "";
    byte[] arrayOfByte = byteArrayValue();
    if ((arrayOfByte == null) || (arrayOfByte.length <= 1))
      return str;
    for (int i = 0; (i < arrayOfByte.length) && (arrayOfByte[i] != 0); ++i);
    try
    {
      str = new String(arrayOfByte, 0, i, "UTF-8");
    }
    catch (UnsupportedEncodingException localUnsupportedEncodingException)
    {
      str = "ERROR";
    }
    return str;
  }

  protected Object writeReplace()
    throws ObjectStreamException
  {
    return new StringCounterSnapshot(getName(), getUnits(), getVariability(), getFlags(), stringValue());
  }
}