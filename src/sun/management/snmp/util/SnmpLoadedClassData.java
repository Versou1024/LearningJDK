package sun.management.snmp.util;

import com.sun.jmx.snmp.SnmpOid;
import com.sun.jmx.snmp.SnmpStatusException;
import java.util.TreeMap;

public final class SnmpLoadedClassData extends SnmpCachedData
{
  public SnmpLoadedClassData(long paramLong, TreeMap paramTreeMap)
  {
    super(paramLong, paramTreeMap, false);
  }

  public final Object getData(SnmpOid paramSnmpOid)
  {
    int i = 0;
    try
    {
      i = (int)paramSnmpOid.getOidArc(0);
    }
    catch (SnmpStatusException localSnmpStatusException)
    {
      return null;
    }
    if (i >= this.datas.length)
      return null;
    return this.datas[i];
  }

  public final SnmpOid getNext(SnmpOid paramSnmpOid)
  {
    int i = 0;
    if ((paramSnmpOid == null) && (this.datas != null) && (this.datas.length >= 1))
      return new SnmpOid(3412048270506065920L);
    try
    {
      i = (int)paramSnmpOid.getOidArc(0);
    }
    catch (SnmpStatusException localSnmpStatusException)
    {
      return null;
    }
    if (i < this.datas.length - 1)
      return new SnmpOid(i + 1);
    return null;
  }

  public final boolean contains(SnmpOid paramSnmpOid)
  {
    int i = 0;
    try
    {
      i = (int)paramSnmpOid.getOidArc(0);
    }
    catch (SnmpStatusException localSnmpStatusException)
    {
      return false;
    }
    return (i < this.datas.length);
  }
}