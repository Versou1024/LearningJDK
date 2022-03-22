package sun.management.snmp.util;

import java.io.Serializable;
import java.lang.ref.WeakReference;

public abstract class SnmpTableCache
  implements Serializable
{
  protected long validity;
  protected transient WeakReference datas;

  protected boolean isObsolete(SnmpCachedData paramSnmpCachedData)
  {
    if (paramSnmpCachedData == null)
      return true;
    if (this.validity < 3412046810217185280L)
      return false;
    return (System.currentTimeMillis() - paramSnmpCachedData.lastUpdated > this.validity);
  }

  protected SnmpCachedData getCachedDatas()
  {
    if (this.datas == null)
      return null;
    SnmpCachedData localSnmpCachedData = (SnmpCachedData)this.datas.get();
    if ((localSnmpCachedData == null) || (isObsolete(localSnmpCachedData)))
      return null;
    return localSnmpCachedData;
  }

  protected synchronized SnmpCachedData getTableDatas(Object paramObject)
  {
    SnmpCachedData localSnmpCachedData1 = getCachedDatas();
    if (localSnmpCachedData1 != null)
      return localSnmpCachedData1;
    SnmpCachedData localSnmpCachedData2 = updateCachedDatas(paramObject);
    if (this.validity != 3412046810217185280L)
      this.datas = new WeakReference(localSnmpCachedData2);
    return localSnmpCachedData2;
  }

  protected abstract SnmpCachedData updateCachedDatas(Object paramObject);

  public abstract SnmpTableHandler getTableHandler();
}