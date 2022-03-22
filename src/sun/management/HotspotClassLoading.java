package sun.management;

import java.util.List;

class HotspotClassLoading
  implements HotspotClassLoadingMBean
{
  private VMManagement jvm;
  private static final String JAVA_CLS = "java.cls.";
  private static final String COM_SUN_CLS = "com.sun.cls.";
  private static final String SUN_CLS = "sun.cls.";
  private static final String CLS_COUNTER_NAME_PATTERN = "java.cls.|com.sun.cls.|sun.cls.";

  HotspotClassLoading(VMManagement paramVMManagement)
  {
    this.jvm = paramVMManagement;
  }

  public long getLoadedClassSize()
  {
    return this.jvm.getLoadedClassSize();
  }

  public long getUnloadedClassSize()
  {
    return this.jvm.getUnloadedClassSize();
  }

  public long getClassLoadingTime()
  {
    return this.jvm.getClassLoadingTime();
  }

  public long getMethodDataSize()
  {
    return this.jvm.getMethodDataSize();
  }

  public long getInitializedClassCount()
  {
    return this.jvm.getInitializedClassCount();
  }

  public long getClassInitializationTime()
  {
    return this.jvm.getClassInitializationTime();
  }

  public long getClassVerificationTime()
  {
    return this.jvm.getClassVerificationTime();
  }

  public List getInternalClassLoadingCounters()
  {
    return this.jvm.getInternalCounters("java.cls.|com.sun.cls.|sun.cls.");
  }
}