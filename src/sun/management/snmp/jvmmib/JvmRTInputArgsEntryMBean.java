package sun.management.snmp.jvmmib;

import com.sun.jmx.snmp.SnmpStatusException;

public abstract interface JvmRTInputArgsEntryMBean
{
  public abstract String getJvmRTInputArgsItem()
    throws SnmpStatusException;

  public abstract Integer getJvmRTInputArgsIndex()
    throws SnmpStatusException;
}