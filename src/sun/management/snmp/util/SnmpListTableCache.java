package sun.management.snmp.util;

import com.sun.jmx.snmp.SnmpOid;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

public abstract class SnmpListTableCache extends SnmpTableCache
{
  protected abstract SnmpOid getIndex(Object paramObject1, List paramList, int paramInt, Object paramObject2);

  protected Object getData(Object paramObject1, List paramList, int paramInt, Object paramObject2)
  {
    return paramObject2;
  }

  protected SnmpCachedData updateCachedDatas(Object paramObject, List paramList)
  {
    int i = (paramList == null) ? 0 : paramList.size();
    if (i == 0)
      return null;
    long l = System.currentTimeMillis();
    Iterator localIterator = paramList.iterator();
    TreeMap localTreeMap = new TreeMap(SnmpCachedData.oidComparator);
    for (int j = 0; localIterator.hasNext(); ++j)
    {
      Object localObject1 = localIterator.next();
      SnmpOid localSnmpOid = getIndex(paramObject, paramList, j, localObject1);
      Object localObject2 = getData(paramObject, paramList, j, localObject1);
      if (localSnmpOid == null)
        break label110:
      label110: localTreeMap.put(localSnmpOid, localObject2);
    }
    return new SnmpCachedData(l, localTreeMap);
  }
}