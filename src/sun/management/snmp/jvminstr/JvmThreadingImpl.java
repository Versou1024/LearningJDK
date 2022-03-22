package sun.management.snmp.jvminstr;

import com.sun.jmx.snmp.SnmpStatusException;
import com.sun.jmx.snmp.agent.SnmpMib;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import javax.management.MBeanServer;
import sun.management.snmp.jvmmib.EnumJvmThreadContentionMonitoring;
import sun.management.snmp.jvmmib.EnumJvmThreadCpuTimeMonitoring;
import sun.management.snmp.jvmmib.JvmThreadingMBean;
import sun.management.snmp.util.MibLogger;

public class JvmThreadingImpl
  implements JvmThreadingMBean
{
  static final EnumJvmThreadCpuTimeMonitoring JvmThreadCpuTimeMonitoringUnsupported = new EnumJvmThreadCpuTimeMonitoring("unsupported");
  static final EnumJvmThreadCpuTimeMonitoring JvmThreadCpuTimeMonitoringEnabled = new EnumJvmThreadCpuTimeMonitoring("enabled");
  static final EnumJvmThreadCpuTimeMonitoring JvmThreadCpuTimeMonitoringDisabled = new EnumJvmThreadCpuTimeMonitoring("disabled");
  static final EnumJvmThreadContentionMonitoring JvmThreadContentionMonitoringUnsupported = new EnumJvmThreadContentionMonitoring("unsupported");
  static final EnumJvmThreadContentionMonitoring JvmThreadContentionMonitoringEnabled = new EnumJvmThreadContentionMonitoring("enabled");
  static final EnumJvmThreadContentionMonitoring JvmThreadContentionMonitoringDisabled = new EnumJvmThreadContentionMonitoring("disabled");
  private long jvmThreadPeakCountReset = 3412045659165949952L;
  static final MibLogger log = new MibLogger(JvmThreadingImpl.class);

  public JvmThreadingImpl(SnmpMib paramSnmpMib)
  {
    log.debug("JvmThreadingImpl", "Constructor");
  }

  public JvmThreadingImpl(SnmpMib paramSnmpMib, MBeanServer paramMBeanServer)
  {
    log.debug("JvmThreadingImpl", "Constructor with server");
  }

  static ThreadMXBean getThreadMXBean()
  {
    return ManagementFactory.getThreadMXBean();
  }

  public EnumJvmThreadCpuTimeMonitoring getJvmThreadCpuTimeMonitoring()
    throws SnmpStatusException
  {
    ThreadMXBean localThreadMXBean = getThreadMXBean();
    if (!(localThreadMXBean.isThreadCpuTimeSupported()))
    {
      log.debug("getJvmThreadCpuTimeMonitoring", "Unsupported ThreadCpuTimeMonitoring");
      return JvmThreadCpuTimeMonitoringUnsupported;
    }
    try
    {
      if (localThreadMXBean.isThreadCpuTimeEnabled())
      {
        log.debug("getJvmThreadCpuTimeMonitoring", "Enabled ThreadCpuTimeMonitoring");
        return JvmThreadCpuTimeMonitoringEnabled;
      }
      log.debug("getJvmThreadCpuTimeMonitoring", "Disabled ThreadCpuTimeMonitoring");
      return JvmThreadCpuTimeMonitoringDisabled;
    }
    catch (UnsupportedOperationException localUnsupportedOperationException)
    {
      log.debug("getJvmThreadCpuTimeMonitoring", "Newly unsupported ThreadCpuTimeMonitoring");
    }
    return JvmThreadCpuTimeMonitoringUnsupported;
  }

  public void setJvmThreadCpuTimeMonitoring(EnumJvmThreadCpuTimeMonitoring paramEnumJvmThreadCpuTimeMonitoring)
    throws SnmpStatusException
  {
    ThreadMXBean localThreadMXBean = getThreadMXBean();
    if (JvmThreadCpuTimeMonitoringEnabled.intValue() == paramEnumJvmThreadCpuTimeMonitoring.intValue())
      localThreadMXBean.setThreadCpuTimeEnabled(true);
    else
      localThreadMXBean.setThreadCpuTimeEnabled(false);
  }

  public void checkJvmThreadCpuTimeMonitoring(EnumJvmThreadCpuTimeMonitoring paramEnumJvmThreadCpuTimeMonitoring)
    throws SnmpStatusException
  {
    if (JvmThreadCpuTimeMonitoringUnsupported.intValue() == paramEnumJvmThreadCpuTimeMonitoring.intValue())
    {
      log.debug("checkJvmThreadCpuTimeMonitoring", "Try to set to illegal unsupported value");
      throw new SnmpStatusException(10);
    }
    if ((JvmThreadCpuTimeMonitoringEnabled.intValue() == paramEnumJvmThreadCpuTimeMonitoring.intValue()) || (JvmThreadCpuTimeMonitoringDisabled.intValue() == paramEnumJvmThreadCpuTimeMonitoring.intValue()))
    {
      ThreadMXBean localThreadMXBean = getThreadMXBean();
      if (localThreadMXBean.isThreadCpuTimeSupported())
        return;
      log.debug("checkJvmThreadCpuTimeMonitoring", "Unsupported operation, can't set state");
      throw new SnmpStatusException(12);
    }
    log.debug("checkJvmThreadCpuTimeMonitoring", "unknown enum value ");
    throw new SnmpStatusException(10);
  }

  public EnumJvmThreadContentionMonitoring getJvmThreadContentionMonitoring()
    throws SnmpStatusException
  {
    ThreadMXBean localThreadMXBean = getThreadMXBean();
    if (!(localThreadMXBean.isThreadContentionMonitoringSupported()))
    {
      log.debug("getJvmThreadContentionMonitoring", "Unsupported ThreadContentionMonitoring");
      return JvmThreadContentionMonitoringUnsupported;
    }
    if (localThreadMXBean.isThreadContentionMonitoringEnabled())
    {
      log.debug("getJvmThreadContentionMonitoring", "Enabled ThreadContentionMonitoring");
      return JvmThreadContentionMonitoringEnabled;
    }
    log.debug("getJvmThreadContentionMonitoring", "Disabled ThreadContentionMonitoring");
    return JvmThreadContentionMonitoringDisabled;
  }

  public void setJvmThreadContentionMonitoring(EnumJvmThreadContentionMonitoring paramEnumJvmThreadContentionMonitoring)
    throws SnmpStatusException
  {
    ThreadMXBean localThreadMXBean = getThreadMXBean();
    if (JvmThreadContentionMonitoringEnabled.intValue() == paramEnumJvmThreadContentionMonitoring.intValue())
      localThreadMXBean.setThreadContentionMonitoringEnabled(true);
    else
      localThreadMXBean.setThreadContentionMonitoringEnabled(false);
  }

  public void checkJvmThreadContentionMonitoring(EnumJvmThreadContentionMonitoring paramEnumJvmThreadContentionMonitoring)
    throws SnmpStatusException
  {
    if (JvmThreadContentionMonitoringUnsupported.intValue() == paramEnumJvmThreadContentionMonitoring.intValue())
    {
      log.debug("checkJvmThreadContentionMonitoring", "Try to set to illegal unsupported value");
      throw new SnmpStatusException(10);
    }
    if ((JvmThreadContentionMonitoringEnabled.intValue() == paramEnumJvmThreadContentionMonitoring.intValue()) || (JvmThreadContentionMonitoringDisabled.intValue() == paramEnumJvmThreadContentionMonitoring.intValue()))
    {
      ThreadMXBean localThreadMXBean = getThreadMXBean();
      if (localThreadMXBean.isThreadContentionMonitoringSupported())
        return;
      log.debug("checkJvmThreadContentionMonitoring", "Unsupported operation, can't set state");
      throw new SnmpStatusException(12);
    }
    log.debug("checkJvmThreadContentionMonitoring", "Try to set to unknown value");
    throw new SnmpStatusException(10);
  }

  public Long getJvmThreadTotalStartedCount()
    throws SnmpStatusException
  {
    return new Long(getThreadMXBean().getTotalStartedThreadCount());
  }

  public Long getJvmThreadPeakCount()
    throws SnmpStatusException
  {
    return new Long(getThreadMXBean().getPeakThreadCount());
  }

  public Long getJvmThreadDaemonCount()
    throws SnmpStatusException
  {
    return new Long(getThreadMXBean().getDaemonThreadCount());
  }

  public Long getJvmThreadCount()
    throws SnmpStatusException
  {
    return new Long(getThreadMXBean().getThreadCount());
  }

  public synchronized Long getJvmThreadPeakCountReset()
    throws SnmpStatusException
  {
    return new Long(this.jvmThreadPeakCountReset);
  }

  public synchronized void setJvmThreadPeakCountReset(Long paramLong)
    throws SnmpStatusException
  {
    long l1 = paramLong.longValue();
    if (l1 > this.jvmThreadPeakCountReset)
    {
      long l2 = System.currentTimeMillis();
      getThreadMXBean().resetPeakThreadCount();
      this.jvmThreadPeakCountReset = l2;
      log.debug("setJvmThreadPeakCountReset", "jvmThreadPeakCountReset=" + l2);
    }
  }

  public void checkJvmThreadPeakCountReset(Long paramLong)
    throws SnmpStatusException
  {
  }
}