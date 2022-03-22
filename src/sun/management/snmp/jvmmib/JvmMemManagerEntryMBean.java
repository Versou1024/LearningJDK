package sun.management.snmp.jvmmib;

import com.sun.jmx.snmp.SnmpStatusException;

public abstract interface JvmMemManagerEntryMBean
{
  public abstract EnumJvmMemManagerState getJvmMemManagerState()
    throws SnmpStatusException;

  public abstract String getJvmMemManagerName()
    throws SnmpStatusException;

  public abstract Integer getJvmMemManagerIndex()
    throws SnmpStatusException;
}