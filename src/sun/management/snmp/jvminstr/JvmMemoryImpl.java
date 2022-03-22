package sun.management.snmp.jvminstr;

import com.sun.jmx.snmp.SnmpStatusException;
import com.sun.jmx.snmp.agent.SnmpMib;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryType;
import java.lang.management.MemoryUsage;
import java.util.Map;
import javax.management.MBeanServer;
import sun.management.snmp.jvmmib.EnumJvmMemoryGCCall;
import sun.management.snmp.jvmmib.EnumJvmMemoryGCVerboseLevel;
import sun.management.snmp.jvmmib.JvmMemoryMBean;
import sun.management.snmp.util.JvmContextFactory;
import sun.management.snmp.util.MibLogger;

public class JvmMemoryImpl
  implements JvmMemoryMBean
{
  static final EnumJvmMemoryGCCall JvmMemoryGCCallSupported = new EnumJvmMemoryGCCall("supported");
  static final EnumJvmMemoryGCCall JvmMemoryGCCallStart = new EnumJvmMemoryGCCall("start");
  static final EnumJvmMemoryGCCall JvmMemoryGCCallFailed = new EnumJvmMemoryGCCall("failed");
  static final EnumJvmMemoryGCCall JvmMemoryGCCallStarted = new EnumJvmMemoryGCCall("started");
  static final EnumJvmMemoryGCVerboseLevel JvmMemoryGCVerboseLevelVerbose = new EnumJvmMemoryGCVerboseLevel("verbose");
  static final EnumJvmMemoryGCVerboseLevel JvmMemoryGCVerboseLevelSilent = new EnumJvmMemoryGCVerboseLevel("silent");
  static final String heapMemoryTag = "jvmMemory.getHeapMemoryUsage";
  static final String nonHeapMemoryTag = "jvmMemory.getNonHeapMemoryUsage";
  static final Long Long0 = new Long(3412046604058755072L);
  static final MibLogger log = new MibLogger(JvmMemoryImpl.class);

  public JvmMemoryImpl(SnmpMib paramSnmpMib)
  {
  }

  public JvmMemoryImpl(SnmpMib paramSnmpMib, MBeanServer paramMBeanServer)
  {
  }

  private MemoryUsage getMemoryUsage(MemoryType paramMemoryType)
  {
    if (paramMemoryType == MemoryType.HEAP)
      return ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
    return ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage();
  }

  MemoryUsage getNonHeapMemoryUsage()
  {
    Map localMap;
    try
    {
      localMap = JvmContextFactory.getUserData();
      if (localMap != null)
      {
        MemoryUsage localMemoryUsage1 = (MemoryUsage)localMap.get("jvmMemory.getNonHeapMemoryUsage");
        if (localMemoryUsage1 != null)
        {
          log.debug("getNonHeapMemoryUsage", "jvmMemory.getNonHeapMemoryUsage found in cache.");
          return localMemoryUsage1;
        }
        MemoryUsage localMemoryUsage2 = getMemoryUsage(MemoryType.NON_HEAP);
        localMap.put("jvmMemory.getNonHeapMemoryUsage", localMemoryUsage2);
        return localMemoryUsage2;
      }
      log.trace("getNonHeapMemoryUsage", "ERROR: should never come here!");
      return getMemoryUsage(MemoryType.NON_HEAP);
    }
    catch (RuntimeException localRuntimeException)
    {
      log.trace("getNonHeapMemoryUsage", "Failed to get NonHeapMemoryUsage: " + localRuntimeException);
      log.debug("getNonHeapMemoryUsage", localRuntimeException);
      throw localRuntimeException;
    }
  }

  MemoryUsage getHeapMemoryUsage()
  {
    Map localMap;
    try
    {
      localMap = JvmContextFactory.getUserData();
      if (localMap != null)
      {
        MemoryUsage localMemoryUsage1 = (MemoryUsage)localMap.get("jvmMemory.getHeapMemoryUsage");
        if (localMemoryUsage1 != null)
        {
          log.debug("getHeapMemoryUsage", "jvmMemory.getHeapMemoryUsage found in cache.");
          return localMemoryUsage1;
        }
        MemoryUsage localMemoryUsage2 = getMemoryUsage(MemoryType.HEAP);
        localMap.put("jvmMemory.getHeapMemoryUsage", localMemoryUsage2);
        return localMemoryUsage2;
      }
      log.trace("getHeapMemoryUsage", "ERROR: should never come here!");
      return getMemoryUsage(MemoryType.HEAP);
    }
    catch (RuntimeException localRuntimeException)
    {
      log.trace("getHeapMemoryUsage", "Failed to get HeapMemoryUsage: " + localRuntimeException);
      log.debug("getHeapMemoryUsage", localRuntimeException);
      throw localRuntimeException;
    }
  }

  public Long getJvmMemoryNonHeapMaxSize()
    throws SnmpStatusException
  {
    long l = getNonHeapMemoryUsage().getMax();
    if (l > -1L)
      return new Long(l);
    return Long0;
  }

  public Long getJvmMemoryNonHeapCommitted()
    throws SnmpStatusException
  {
    long l = getNonHeapMemoryUsage().getCommitted();
    if (l > -1L)
      return new Long(l);
    return Long0;
  }

  public Long getJvmMemoryNonHeapUsed()
    throws SnmpStatusException
  {
    long l = getNonHeapMemoryUsage().getUsed();
    if (l > -1L)
      return new Long(l);
    return Long0;
  }

  public Long getJvmMemoryNonHeapInitSize()
    throws SnmpStatusException
  {
    long l = getNonHeapMemoryUsage().getInit();
    if (l > -1L)
      return new Long(l);
    return Long0;
  }

  public Long getJvmMemoryHeapMaxSize()
    throws SnmpStatusException
  {
    long l = getHeapMemoryUsage().getMax();
    if (l > -1L)
      return new Long(l);
    return Long0;
  }

  public EnumJvmMemoryGCCall getJvmMemoryGCCall()
    throws SnmpStatusException
  {
    Map localMap = JvmContextFactory.getUserData();
    if (localMap != null)
    {
      EnumJvmMemoryGCCall localEnumJvmMemoryGCCall = (EnumJvmMemoryGCCall)localMap.get("jvmMemory.getJvmMemoryGCCall");
      if (localEnumJvmMemoryGCCall != null)
        return localEnumJvmMemoryGCCall;
    }
    return JvmMemoryGCCallSupported;
  }

  public void setJvmMemoryGCCall(EnumJvmMemoryGCCall paramEnumJvmMemoryGCCall)
    throws SnmpStatusException
  {
    if (paramEnumJvmMemoryGCCall.intValue() == JvmMemoryGCCallStart.intValue())
    {
      Map localMap = JvmContextFactory.getUserData();
      try
      {
        ManagementFactory.getMemoryMXBean().gc();
        if (localMap != null)
          localMap.put("jvmMemory.getJvmMemoryGCCall", JvmMemoryGCCallStarted);
      }
      catch (Exception localException)
      {
        if (localMap != null)
          localMap.put("jvmMemory.getJvmMemoryGCCall", JvmMemoryGCCallFailed);
      }
      return;
    }
    throw new SnmpStatusException(10);
  }

  public void checkJvmMemoryGCCall(EnumJvmMemoryGCCall paramEnumJvmMemoryGCCall)
    throws SnmpStatusException
  {
    if (paramEnumJvmMemoryGCCall.intValue() != JvmMemoryGCCallStart.intValue())
      throw new SnmpStatusException(10);
  }

  public Long getJvmMemoryHeapCommitted()
    throws SnmpStatusException
  {
    long l = getHeapMemoryUsage().getCommitted();
    if (l > -1L)
      return new Long(l);
    return Long0;
  }

  public EnumJvmMemoryGCVerboseLevel getJvmMemoryGCVerboseLevel()
    throws SnmpStatusException
  {
    if (ManagementFactory.getMemoryMXBean().isVerbose())
      return JvmMemoryGCVerboseLevelVerbose;
    return JvmMemoryGCVerboseLevelSilent;
  }

  public void setJvmMemoryGCVerboseLevel(EnumJvmMemoryGCVerboseLevel paramEnumJvmMemoryGCVerboseLevel)
    throws SnmpStatusException
  {
    if (JvmMemoryGCVerboseLevelVerbose.intValue() == paramEnumJvmMemoryGCVerboseLevel.intValue())
      ManagementFactory.getMemoryMXBean().setVerbose(true);
    else
      ManagementFactory.getMemoryMXBean().setVerbose(false);
  }

  public void checkJvmMemoryGCVerboseLevel(EnumJvmMemoryGCVerboseLevel paramEnumJvmMemoryGCVerboseLevel)
    throws SnmpStatusException
  {
  }

  public Long getJvmMemoryHeapUsed()
    throws SnmpStatusException
  {
    long l = getHeapMemoryUsage().getUsed();
    if (l > -1L)
      return new Long(l);
    return Long0;
  }

  public Long getJvmMemoryHeapInitSize()
    throws SnmpStatusException
  {
    long l = getHeapMemoryUsage().getInit();
    if (l > -1L)
      return new Long(l);
    return Long0;
  }

  public Long getJvmMemoryPendingFinalCount()
    throws SnmpStatusException
  {
    long l = ManagementFactory.getMemoryMXBean().getObjectPendingFinalizationCount();
    if (l > -1L)
      return new Long((int)l);
    return new Long(3412047909728813056L);
  }
}