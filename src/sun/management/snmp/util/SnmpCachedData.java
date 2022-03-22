package sun.management.snmp.util;

import com.sun.jmx.snmp.SnmpOid;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeMap;

public class SnmpCachedData
  implements SnmpTableHandler
{
  public static final Comparator oidComparator = new Comparator()
  {
    public int compare(Object paramObject1, Object paramObject2)
    {
      return ((SnmpOid)paramObject1).compareTo((SnmpOid)paramObject2);
    }

    public boolean equals(Object paramObject1, Object paramObject2)
    {
      if (paramObject1 == paramObject2)
        return true;
      return paramObject1.equals(paramObject2);
    }
  };
  public final long lastUpdated;
  public final SnmpOid[] indexes;
  public final Object[] datas;

  public SnmpCachedData(long paramLong, SnmpOid[] paramArrayOfSnmpOid, Object[] paramArrayOfObject)
  {
    this.lastUpdated = paramLong;
    this.indexes = paramArrayOfSnmpOid;
    this.datas = paramArrayOfObject;
  }

  public SnmpCachedData(long paramLong, TreeMap paramTreeMap)
  {
    this(paramLong, paramTreeMap, true);
  }

  public SnmpCachedData(long paramLong, TreeMap paramTreeMap, boolean paramBoolean)
  {
    int i = paramTreeMap.size();
    this.lastUpdated = paramLong;
    this.indexes = new SnmpOid[i];
    this.datas = new Object[i];
    if (paramBoolean)
    {
      paramTreeMap.keySet().toArray(this.indexes);
      paramTreeMap.values().toArray(this.datas);
    }
    else
    {
      paramTreeMap.values().toArray(this.datas);
    }
  }

  public final int find(SnmpOid paramSnmpOid)
  {
    return Arrays.binarySearch(this.indexes, paramSnmpOid, oidComparator);
  }

  public Object getData(SnmpOid paramSnmpOid)
  {
    int i = find(paramSnmpOid);
    if ((i < 0) || (i >= this.datas.length))
      return null;
    return this.datas[i];
  }

  public SnmpOid getNext(SnmpOid paramSnmpOid)
  {
    if (paramSnmpOid == null)
    {
      if (this.indexes.length > 0)
        return this.indexes[0];
      return null;
    }
    int i = find(paramSnmpOid);
    if (i > -1)
    {
      if (i < this.indexes.length - 1)
        return this.indexes[(i + 1)];
      return null;
    }
    int j = -i - 1;
    if ((j > -1) && (j < this.indexes.length))
      return this.indexes[j];
    return null;
  }

  public boolean contains(SnmpOid paramSnmpOid)
  {
    int i = find(paramSnmpOid);
    return ((i > -1) && (i < this.indexes.length));
  }
}