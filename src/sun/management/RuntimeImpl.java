package sun.management;

import java.lang.management.RuntimeMXBean;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

class RuntimeImpl
  implements RuntimeMXBean
{
  private final VMManagement jvm;
  private final long vmStartupTime;

  RuntimeImpl(VMManagement paramVMManagement)
  {
    this.jvm = paramVMManagement;
    this.vmStartupTime = this.jvm.getStartupTime();
  }

  public String getName()
  {
    return this.jvm.getVmId();
  }

  public String getManagementSpecVersion()
  {
    return this.jvm.getManagementVersion();
  }

  public String getVmName()
  {
    return this.jvm.getVmName();
  }

  public String getVmVendor()
  {
    return this.jvm.getVmVendor();
  }

  public String getVmVersion()
  {
    return this.jvm.getVmVersion();
  }

  public String getSpecName()
  {
    return this.jvm.getVmSpecName();
  }

  public String getSpecVendor()
  {
    return this.jvm.getVmSpecVendor();
  }

  public String getSpecVersion()
  {
    return this.jvm.getVmSpecVersion();
  }

  public String getClassPath()
  {
    return this.jvm.getClassPath();
  }

  public String getLibraryPath()
  {
    return this.jvm.getLibraryPath();
  }

  public String getBootClassPath()
  {
    if (!(isBootClassPathSupported()))
      throw new UnsupportedOperationException("Boot class path mechanism is not supported");
    ManagementFactory.checkMonitorAccess();
    return this.jvm.getBootClassPath();
  }

  public List<String> getInputArguments()
  {
    ManagementFactory.checkMonitorAccess();
    return this.jvm.getVmArguments();
  }

  public long getUptime()
  {
    long l = System.currentTimeMillis();
    return (l - this.vmStartupTime);
  }

  public long getStartTime()
  {
    return this.vmStartupTime;
  }

  public boolean isBootClassPathSupported()
  {
    return this.jvm.isBootClassPathSupported();
  }

  public Map<String, String> getSystemProperties()
  {
    Properties localProperties = System.getProperties();
    HashMap localHashMap = new HashMap();
    Set localSet = localProperties.stringPropertyNames();
    Iterator localIterator = localSet.iterator();
    while (localIterator.hasNext())
    {
      String str1 = (String)localIterator.next();
      String str2 = localProperties.getProperty(str1);
      localHashMap.put(str1, str2);
    }
    return localHashMap;
  }
}