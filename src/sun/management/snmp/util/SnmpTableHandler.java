package sun.management.snmp.util;

import com.sun.jmx.snmp.SnmpOid;

public abstract interface SnmpTableHandler
{
  public abstract Object getData(SnmpOid paramSnmpOid);

  public abstract SnmpOid getNext(SnmpOid paramSnmpOid);

  public abstract boolean contains(SnmpOid paramSnmpOid);
}