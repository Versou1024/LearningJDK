package sun.management.snmp.jvmmib;

import com.sun.jmx.snmp.SnmpStatusException;

public abstract interface JvmCompilationMBean
{
  public abstract EnumJvmJITCompilerTimeMonitoring getJvmJITCompilerTimeMonitoring()
    throws SnmpStatusException;

  public abstract Long getJvmJITCompilerTimeMs()
    throws SnmpStatusException;

  public abstract String getJvmJITCompilerName()
    throws SnmpStatusException;
}