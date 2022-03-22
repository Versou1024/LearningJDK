package sun.management;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class HotspotThread
  implements HotspotThreadMBean
{
  private VMManagement jvm;
  private static final String JAVA_THREADS = "java.threads.";
  private static final String COM_SUN_THREADS = "com.sun.threads.";
  private static final String SUN_THREADS = "sun.threads.";
  private static final String THREADS_COUNTER_NAME_PATTERN = "java.threads.|com.sun.threads.|sun.threads.";

  HotspotThread(VMManagement paramVMManagement)
  {
    this.jvm = paramVMManagement;
  }

  public native int getInternalThreadCount();

  public Map getInternalThreadCpuTimes()
  {
    int i = getInternalThreadCount();
    if (i == 0)
      return Collections.EMPTY_MAP;
    String[] arrayOfString = new String[i];
    long[] arrayOfLong = new long[i];
    int j = getInternalThreadTimes0(arrayOfString, arrayOfLong);
    HashMap localHashMap = new HashMap(j);
    for (int k = 0; k < j; ++k)
      localHashMap.put(arrayOfString[k], new Long(arrayOfLong[k]));
    return localHashMap;
  }

  public native int getInternalThreadTimes0(String[] paramArrayOfString, long[] paramArrayOfLong);

  public List getInternalThreadingCounters()
  {
    return this.jvm.getInternalCounters("java.threads.|com.sun.threads.|sun.threads.");
  }
}