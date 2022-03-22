package sun.management.snmp.jvminstr;

import com.sun.jmx.snmp.SnmpStatusException;
import com.sun.jmx.snmp.agent.SnmpMib;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.List;
import java.util.Map;
import javax.management.MBeanServer;
import sun.management.snmp.jvmmib.EnumJvmRTBootClassPathSupport;
import sun.management.snmp.jvmmib.JvmRuntimeMBean;
import sun.management.snmp.util.JvmContextFactory;

public class JvmRuntimeImpl
  implements JvmRuntimeMBean
{
  static final EnumJvmRTBootClassPathSupport JvmRTBootClassPathSupportSupported = new EnumJvmRTBootClassPathSupport("supported");
  static final EnumJvmRTBootClassPathSupport JvmRTBootClassPathSupportUnSupported = new EnumJvmRTBootClassPathSupport("unsupported");

  public JvmRuntimeImpl(SnmpMib paramSnmpMib)
  {
  }

  public JvmRuntimeImpl(SnmpMib paramSnmpMib, MBeanServer paramMBeanServer)
  {
  }

  static RuntimeMXBean getRuntimeMXBean()
  {
    return ManagementFactory.getRuntimeMXBean();
  }

  private static String validDisplayStringTC(String paramString)
  {
    return JVM_MANAGEMENT_MIB_IMPL.validDisplayStringTC(paramString);
  }

  private static String validPathElementTC(String paramString)
  {
    return JVM_MANAGEMENT_MIB_IMPL.validPathElementTC(paramString);
  }

  private static String validJavaObjectNameTC(String paramString)
  {
    return JVM_MANAGEMENT_MIB_IMPL.validJavaObjectNameTC(paramString);
  }

  static String[] splitPath(String paramString)
  {
    String[] arrayOfString = paramString.split(File.pathSeparator);
    return arrayOfString;
  }

  static String[] getClassPath(Object paramObject)
  {
    Map localMap = (Map)((paramObject instanceof Map) ? paramObject : null);
    if (localMap != null)
    {
      arrayOfString = (String[])(String[])localMap.get("JvmRuntime.getClassPath");
      if (arrayOfString != null)
        return arrayOfString;
    }
    String[] arrayOfString = splitPath(getRuntimeMXBean().getClassPath());
    if (localMap != null)
      localMap.put("JvmRuntime.getClassPath", arrayOfString);
    return arrayOfString;
  }

  static String[] getBootClassPath(Object paramObject)
  {
    if (!(getRuntimeMXBean().isBootClassPathSupported()))
      return new String[0];
    Map localMap = (Map)((paramObject instanceof Map) ? paramObject : null);
    if (localMap != null)
    {
      arrayOfString = (String[])(String[])localMap.get("JvmRuntime.getBootClassPath");
      if (arrayOfString != null)
        return arrayOfString;
    }
    String[] arrayOfString = splitPath(getRuntimeMXBean().getBootClassPath());
    if (localMap != null)
      localMap.put("JvmRuntime.getBootClassPath", arrayOfString);
    return arrayOfString;
  }

  static String[] getLibraryPath(Object paramObject)
  {
    Map localMap = (Map)((paramObject instanceof Map) ? paramObject : null);
    if (localMap != null)
    {
      arrayOfString = (String[])(String[])localMap.get("JvmRuntime.getLibraryPath");
      if (arrayOfString != null)
        return arrayOfString;
    }
    String[] arrayOfString = splitPath(getRuntimeMXBean().getLibraryPath());
    if (localMap != null)
      localMap.put("JvmRuntime.getLibraryPath", arrayOfString);
    return arrayOfString;
  }

  static String[] getInputArguments(Object paramObject)
  {
    Map localMap = (Map)((paramObject instanceof Map) ? paramObject : null);
    if (localMap != null)
    {
      localObject = (String[])(String[])localMap.get("JvmRuntime.getInputArguments");
      if (localObject != null)
        return localObject;
    }
    Object localObject = getRuntimeMXBean().getInputArguments();
    String[] arrayOfString = (String[])(String[])((List)localObject).toArray(new String[0]);
    if (localMap != null)
      localMap.put("JvmRuntime.getInputArguments", arrayOfString);
    return ((String)arrayOfString);
  }

  public String getJvmRTSpecVendor()
    throws SnmpStatusException
  {
    return validDisplayStringTC(getRuntimeMXBean().getSpecVendor());
  }

  public String getJvmRTSpecName()
    throws SnmpStatusException
  {
    return validDisplayStringTC(getRuntimeMXBean().getSpecName());
  }

  public String getJvmRTVMVersion()
    throws SnmpStatusException
  {
    return validDisplayStringTC(getRuntimeMXBean().getVmVersion());
  }

  public String getJvmRTVMVendor()
    throws SnmpStatusException
  {
    return validDisplayStringTC(getRuntimeMXBean().getVmVendor());
  }

  public String getJvmRTManagementSpecVersion()
    throws SnmpStatusException
  {
    return validDisplayStringTC(getRuntimeMXBean().getManagementSpecVersion());
  }

  public String getJvmRTVMName()
    throws SnmpStatusException
  {
    return validJavaObjectNameTC(getRuntimeMXBean().getVmName());
  }

  public Integer getJvmRTInputArgsCount()
    throws SnmpStatusException
  {
    String[] arrayOfString = getInputArguments(JvmContextFactory.getUserData());
    return new Integer(arrayOfString.length);
  }

  public EnumJvmRTBootClassPathSupport getJvmRTBootClassPathSupport()
    throws SnmpStatusException
  {
    if (getRuntimeMXBean().isBootClassPathSupported())
      return JvmRTBootClassPathSupportSupported;
    return JvmRTBootClassPathSupportUnSupported;
  }

  public Long getJvmRTUptimeMs()
    throws SnmpStatusException
  {
    return new Long(getRuntimeMXBean().getUptime());
  }

  public Long getJvmRTStartTimeMs()
    throws SnmpStatusException
  {
    return new Long(getRuntimeMXBean().getStartTime());
  }

  public String getJvmRTSpecVersion()
    throws SnmpStatusException
  {
    return validDisplayStringTC(getRuntimeMXBean().getSpecVersion());
  }

  public String getJvmRTName()
    throws SnmpStatusException
  {
    return validDisplayStringTC(getRuntimeMXBean().getName());
  }
}