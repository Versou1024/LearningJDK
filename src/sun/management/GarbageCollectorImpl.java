package sun.management;

import com.sun.management.GarbageCollectorMXBean;
import com.sun.management.GcInfo;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.util.List;
import java.util.ListIterator;

class GarbageCollectorImpl extends MemoryManagerImpl
  implements GarbageCollectorMXBean
{
  private String[] poolNames = null;
  private GcInfoBuilder gcInfoBuilder;

  GarbageCollectorImpl(String paramString)
  {
    super(paramString);
  }

  public native long getCollectionCount();

  public native long getCollectionTime();

  synchronized String[] getAllPoolNames()
  {
    if (this.poolNames == null)
    {
      List localList = ManagementFactory.getMemoryPoolMXBeans();
      this.poolNames = new String[localList.size()];
      int i = 0;
      ListIterator localListIterator = localList.listIterator();
      while (localListIterator.hasNext())
      {
        MemoryPoolMXBean localMemoryPoolMXBean = (MemoryPoolMXBean)localListIterator.next();
        this.poolNames[i] = localMemoryPoolMXBean.getName();
        ++i;
      }
    }
    return this.poolNames;
  }

  public GcInfo getLastGcInfo()
  {
    synchronized (this)
    {
      if (this.gcInfoBuilder == null)
        this.gcInfoBuilder = new GcInfoBuilder(this, getAllPoolNames());
    }
    ??? = this.gcInfoBuilder.getLastGcInfo();
    return ((GcInfo)???);
  }
}