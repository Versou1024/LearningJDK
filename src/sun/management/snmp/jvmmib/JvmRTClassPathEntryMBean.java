package sun.management.snmp.jvmmib;

import com.sun.jmx.snmp.SnmpStatusException;

public abstract interface JvmRTClassPathEntryMBean
{
  public abstract String getJvmRTClassPathItem()
    throws SnmpStatusException;

  public abstract Integer getJvmRTClassPathIndex()
    throws SnmpStatusException;
}