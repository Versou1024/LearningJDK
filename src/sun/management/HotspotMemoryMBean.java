package sun.management;

import java.util.List;
import sun.management.counter.Counter;

public abstract interface HotspotMemoryMBean
{
  public abstract List<Counter> getInternalMemoryCounters();
}