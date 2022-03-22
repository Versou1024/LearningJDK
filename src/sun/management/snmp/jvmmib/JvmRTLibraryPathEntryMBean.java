package sun.management.snmp.jvmmib;

import com.sun.jmx.snmp.SnmpStatusException;

public abstract interface JvmRTLibraryPathEntryMBean
{
  public abstract String getJvmRTLibraryPathItem()
    throws SnmpStatusException;

  public abstract Integer getJvmRTLibraryPathIndex()
    throws SnmpStatusException;
}