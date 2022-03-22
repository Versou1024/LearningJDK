package sun.management.snmp.jvmmib;

import com.sun.jmx.snmp.SnmpStatusException;

public abstract interface JvmOSMBean
{
  public abstract Integer getJvmOSProcessorCount()
    throws SnmpStatusException;

  public abstract String getJvmOSVersion()
    throws SnmpStatusException;

  public abstract String getJvmOSArch()
    throws SnmpStatusException;

  public abstract String getJvmOSName()
    throws SnmpStatusException;
}