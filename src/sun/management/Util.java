package sun.management;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.MemoryManagerMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.util.List;

class Util
{
  private static String[] EMPTY_STRING_ARRAY = new String[0];

  static String getMBeanObjectName(MemoryPoolMXBean paramMemoryPoolMXBean)
  {
    return "java.lang:type=MemoryPool,name=" + paramMemoryPoolMXBean.getName();
  }

  static String getMBeanObjectName(MemoryManagerMXBean paramMemoryManagerMXBean)
  {
    if (paramMemoryManagerMXBean instanceof GarbageCollectorMXBean)
      return getMBeanObjectName((GarbageCollectorMXBean)paramMemoryManagerMXBean);
    return "java.lang:type=MemoryManager,name=" + paramMemoryManagerMXBean.getName();
  }

  static String getMBeanObjectName(GarbageCollectorMXBean paramGarbageCollectorMXBean)
  {
    return "java.lang:type=GarbageCollector,name=" + paramGarbageCollectorMXBean.getName();
  }

  static RuntimeException newException(Exception paramException)
  {
    RuntimeException localRuntimeException = new RuntimeException(paramException.getMessage());
    localRuntimeException.initCause(paramException);
    return localRuntimeException;
  }

  static InternalError newInternalError(Exception paramException)
  {
    InternalError localInternalError = new InternalError(paramException.getMessage());
    localInternalError.initCause(paramException);
    return localInternalError;
  }

  static AssertionError newAssertionError(Exception paramException)
  {
    AssertionError localAssertionError = new AssertionError(paramException.getMessage());
    localAssertionError.initCause(paramException);
    return localAssertionError;
  }

  static String[] toStringArray(List<String> paramList)
  {
    return ((String[])(String[])paramList.toArray(EMPTY_STRING_ARRAY));
  }
}