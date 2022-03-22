package sun.management.snmp.util;

import com.sun.jmx.snmp.SnmpOid;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public abstract class SnmpNamedListTableCache extends SnmpListTableCache
{
  protected TreeMap names = new TreeMap();
  protected long last = 3412045659165949952L;
  boolean wrapped = false;
  static final MibLogger log = new MibLogger(SnmpNamedListTableCache.class);

  protected abstract String getKey(Object paramObject1, List paramList, int paramInt, Object paramObject2);

  protected SnmpOid makeIndex(Object paramObject1, List paramList, int paramInt, Object paramObject2)
  {
    if (++this.last > 4294967295L)
    {
      log.debug("makeIndex", "Index wrapping...");
      this.last = 3412047463052214272L;
      this.wrapped = true;
    }
    if (!(this.wrapped))
      return new SnmpOid(this.last);
    for (int i = 1; i < 4294967295L; ++i)
    {
      if (++this.last > 4294967295L)
        this.last = 3412047961268420609L;
      SnmpOid localSnmpOid = new SnmpOid(this.last);
      if (this.names == null)
        return localSnmpOid;
      if (this.names.containsValue(localSnmpOid))
        break label158:
      if (paramObject1 == null)
        return localSnmpOid;
      if (((Map)paramObject1).containsValue(localSnmpOid))
        break label158:
      label158: return localSnmpOid;
    }
    return null;
  }

  protected SnmpOid getIndex(Object paramObject1, List paramList, int paramInt, Object paramObject2)
  {
    String str = getKey(paramObject1, paramList, paramInt, paramObject2);
    Object localObject = ((this.names == null) || (str == null)) ? null : this.names.get(str);
    SnmpOid localSnmpOid = (localObject != null) ? (SnmpOid)localObject : makeIndex(paramObject1, paramList, paramInt, paramObject2);
    if ((paramObject1 != null) && (str != null) && (localSnmpOid != null))
      ((Map)paramObject1).put(str, localSnmpOid);
    log.debug("getIndex", "key=" + str + ", index=" + localSnmpOid);
    return localSnmpOid;
  }

  protected SnmpCachedData updateCachedDatas(Object paramObject, List paramList)
  {
    TreeMap localTreeMap = new TreeMap();
    SnmpCachedData localSnmpCachedData = super.updateCachedDatas(paramObject, paramList);
    this.names = localTreeMap;
    return localSnmpCachedData;
  }

  protected abstract List loadRawDatas(Map paramMap);

  protected abstract String getRawDatasKey();

  protected List getRawDatas(Map paramMap, String paramString)
  {
    List localList = null;
    if (paramMap != null)
      localList = (List)paramMap.get(paramString);
    if (localList == null)
    {
      localList = loadRawDatas(paramMap);
      if ((localList != null) && (paramMap != null))
        paramMap.put(paramString, localList);
    }
    return localList;
  }

  protected SnmpCachedData updateCachedDatas(Object paramObject)
  {
    Map localMap = (paramObject instanceof Map) ? (Map)paramObject : null;
    List localList = getRawDatas(localMap, getRawDatasKey());
    log.debug("updateCachedDatas", "rawDatas.size()=" + ((localList == null) ? "<no data>" : new StringBuilder().append("").append(localList.size()).toString()));
    TreeMap localTreeMap = new TreeMap();
    SnmpCachedData localSnmpCachedData = super.updateCachedDatas(localTreeMap, localList);
    this.names = localTreeMap;
    return localSnmpCachedData;
  }
}