package sun.management.snmp.jvmmib;

import com.sun.jmx.snmp.SnmpStatusException;

public abstract interface JvmRTBootClassPathEntryMBean
{
  public abstract String getJvmRTBootClassPathItem()
    throws SnmpStatusException;

  public abstract Integer getJvmRTBootClassPathIndex()
    throws SnmpStatusException;
}