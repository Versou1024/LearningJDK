package sun.management.counter.perf;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import sun.management.counter.Counter;
import sun.management.counter.Units;

public class PerfInstrumentation
{
  private ByteBuffer buffer;
  private Prologue prologue;
  private long lastModificationTime;
  private long lastUsed;
  private int nextEntry;
  private SortedMap map;

  public PerfInstrumentation(ByteBuffer paramByteBuffer)
  {
    this.prologue = new Prologue(paramByteBuffer);
    this.buffer = paramByteBuffer;
    this.buffer.order(this.prologue.getByteOrder());
    int i = getMajorVersion();
    int j = getMinorVersion();
    if (i < 2)
      throw new InstrumentationException("Unsupported version: " + i + "." + j);
    rewind();
  }

  public int getMajorVersion()
  {
    return this.prologue.getMajorVersion();
  }

  public int getMinorVersion()
  {
    return this.prologue.getMinorVersion();
  }

  public long getModificationTimeStamp()
  {
    return this.prologue.getModificationTimeStamp();
  }

  void rewind()
  {
    this.buffer.rewind();
    this.buffer.position(this.prologue.getEntryOffset());
    this.nextEntry = this.buffer.position();
    this.map = new TreeMap();
  }

  boolean hasNext()
  {
    return (this.nextEntry < this.prologue.getUsed());
  }

  Counter getNextCounter()
  {
    if (!(hasNext()))
      return null;
    if (this.nextEntry % 4 != 0)
      throw new InstrumentationException("Entry index not properly aligned: " + this.nextEntry);
    if ((this.nextEntry < 0) || (this.nextEntry > this.buffer.limit()))
      throw new InstrumentationException("Entry index out of bounds: nextEntry = " + this.nextEntry + ", limit = " + this.buffer.limit());
    this.buffer.position(this.nextEntry);
    PerfDataEntry localPerfDataEntry = new PerfDataEntry(this.buffer);
    this.nextEntry += localPerfDataEntry.size();
    Object localObject = null;
    PerfDataType localPerfDataType = localPerfDataEntry.type();
    if (localPerfDataType == PerfDataType.BYTE)
    {
      if ((localPerfDataEntry.units() == Units.STRING) && (localPerfDataEntry.vectorLength() > 0))
      {
        localObject = new PerfStringCounter(localPerfDataEntry.name(), localPerfDataEntry.variability(), localPerfDataEntry.flags(), localPerfDataEntry.vectorLength(), localPerfDataEntry.byteData());
        break label363:
      }
      if (localPerfDataEntry.vectorLength() > 0)
      {
        localObject = new PerfByteArrayCounter(localPerfDataEntry.name(), localPerfDataEntry.units(), localPerfDataEntry.variability(), localPerfDataEntry.flags(), localPerfDataEntry.vectorLength(), localPerfDataEntry.byteData());
        break label363:
      }
      if ($assertionsDisabled)
        break label363;
      throw new AssertionError();
    }
    if (localPerfDataType == PerfDataType.LONG)
      if (localPerfDataEntry.vectorLength() == 0)
        localObject = new PerfLongCounter(localPerfDataEntry.name(), localPerfDataEntry.units(), localPerfDataEntry.variability(), localPerfDataEntry.flags(), localPerfDataEntry.longData());
      else
        localObject = new PerfLongArrayCounter(localPerfDataEntry.name(), localPerfDataEntry.units(), localPerfDataEntry.variability(), localPerfDataEntry.flags(), localPerfDataEntry.vectorLength(), localPerfDataEntry.longData());
    else if (!($assertionsDisabled))
      throw new AssertionError();
    label363: return ((Counter)localObject);
  }

  public synchronized List getAllCounters()
  {
    while (hasNext())
    {
      Counter localCounter = getNextCounter();
      if (localCounter != null)
        this.map.put(localCounter.getName(), localCounter);
    }
    return new ArrayList(this.map.values());
  }

  public synchronized List findByPattern(String paramString)
  {
    while (hasNext())
    {
      localObject = getNextCounter();
      if (localObject != null)
        this.map.put(((Counter)localObject).getName(), localObject);
    }
    Object localObject = Pattern.compile(paramString);
    Matcher localMatcher = ((Pattern)localObject).matcher("");
    ArrayList localArrayList = new ArrayList();
    Iterator localIterator = this.map.entrySet().iterator();
    while (localIterator.hasNext())
    {
      Map.Entry localEntry = (Map.Entry)localIterator.next();
      String str = (String)localEntry.getKey();
      localMatcher.reset(str);
      if (localMatcher.lookingAt())
        localArrayList.add((Counter)localEntry.getValue());
    }
    return ((List)localArrayList);
  }
}