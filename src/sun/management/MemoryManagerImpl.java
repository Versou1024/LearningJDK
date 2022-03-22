package sun.management;

import java.lang.management.MemoryManagerMXBean;
import java.lang.management.MemoryPoolMXBean;

class MemoryManagerImpl
  implements MemoryManagerMXBean
{
  private final String name;
  private final boolean isValid;
  private MemoryPoolMXBean[] pools;

  MemoryManagerImpl(String paramString)
  {
    this.name = paramString;
    this.isValid = true;
    this.pools = null;
  }

  public String getName()
  {
    return this.name;
  }

  public boolean isValid()
  {
    return this.isValid;
  }

  public String[] getMemoryPoolNames()
  {
    MemoryPoolMXBean[] arrayOfMemoryPoolMXBean = getMemoryPools();
    String[] arrayOfString = new String[arrayOfMemoryPoolMXBean.length];
    for (int i = 0; i < arrayOfMemoryPoolMXBean.length; ++i)
      arrayOfString[i] = arrayOfMemoryPoolMXBean[i].getName();
    return arrayOfString;
  }

  synchronized MemoryPoolMXBean[] getMemoryPools()
  {
    if (this.pools == null)
      this.pools = getMemoryPools0();
    return this.pools;
  }

  private native MemoryPoolMXBean[] getMemoryPools0();
}