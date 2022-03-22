package sun.management;

import java.util.List;
import sun.management.counter.Counter;

public abstract interface HotspotRuntimeMBean
{
  public abstract long getSafepointCount();

  public abstract long getTotalSafepointTime();

  public abstract long getSafepointSyncTime();

  public abstract List<Counter> getInternalRuntimeCounters();
}