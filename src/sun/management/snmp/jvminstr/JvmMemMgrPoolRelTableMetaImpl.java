package sun.management.snmp.jvminstr;

import com.sun.jmx.snmp.SnmpOid;
import com.sun.jmx.snmp.SnmpStatusException;
import com.sun.jmx.snmp.agent.SnmpMib;
import com.sun.jmx.snmp.agent.SnmpStandardObjectServer;
import java.io.Serializable;
import java.lang.management.MemoryManagerMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import sun.management.snmp.jvmmib.JvmMemMgrPoolRelTableMeta;
import sun.management.snmp.util.JvmContextFactory;
import sun.management.snmp.util.MibLogger;
import sun.management.snmp.util.SnmpCachedData;
import sun.management.snmp.util.SnmpTableCache;
import sun.management.snmp.util.SnmpTableHandler;

public class JvmMemMgrPoolRelTableMetaImpl extends JvmMemMgrPoolRelTableMeta
  implements Serializable
{
  protected SnmpTableCache cache;
  private transient JvmMemManagerTableMetaImpl managers = null;
  private transient JvmMemPoolTableMetaImpl pools = null;
  static final MibLogger log = new MibLogger(JvmMemMgrPoolRelTableMetaImpl.class);

  public JvmMemMgrPoolRelTableMetaImpl(SnmpMib paramSnmpMib, SnmpStandardObjectServer paramSnmpStandardObjectServer)
  {
    super(paramSnmpMib, paramSnmpStandardObjectServer);
    this.cache = new JvmMemMgrPoolRelTableCache(this, ((JVM_MANAGEMENT_MIB_IMPL)paramSnmpMib).validity());
  }

  private final JvmMemManagerTableMetaImpl getManagers(SnmpMib paramSnmpMib)
  {
    if (this.managers == null)
      this.managers = ((JvmMemManagerTableMetaImpl)paramSnmpMib.getRegisteredTableMeta("JvmMemManagerTable"));
    return this.managers;
  }

  private final JvmMemPoolTableMetaImpl getPools(SnmpMib paramSnmpMib)
  {
    if (this.pools == null)
      this.pools = ((JvmMemPoolTableMetaImpl)paramSnmpMib.getRegisteredTableMeta("JvmMemPoolTable"));
    return this.pools;
  }

  protected SnmpTableHandler getManagerHandler(Object paramObject)
  {
    JvmMemManagerTableMetaImpl localJvmMemManagerTableMetaImpl = getManagers(this.theMib);
    return localJvmMemManagerTableMetaImpl.getHandler(paramObject);
  }

  protected SnmpTableHandler getPoolHandler(Object paramObject)
  {
    JvmMemPoolTableMetaImpl localJvmMemPoolTableMetaImpl = getPools(this.theMib);
    return localJvmMemPoolTableMetaImpl.getHandler(paramObject);
  }

  protected SnmpOid getNextOid(Object paramObject)
    throws SnmpStatusException
  {
    return getNextOid(null, paramObject);
  }

  protected SnmpOid getNextOid(SnmpOid paramSnmpOid, Object paramObject)
    throws SnmpStatusException
  {
    boolean bool = log.isDebugOn();
    if (bool)
      log.debug("getNextOid", "previous=" + paramSnmpOid);
    SnmpTableHandler localSnmpTableHandler = getHandler(paramObject);
    if (localSnmpTableHandler == null)
    {
      if (bool)
        log.debug("getNextOid", "handler is null!");
      throw new SnmpStatusException(224);
    }
    SnmpOid localSnmpOid = localSnmpTableHandler.getNext(paramSnmpOid);
    if (bool)
      log.debug("getNextOid", "next=" + localSnmpOid);
    if (localSnmpOid == null)
      throw new SnmpStatusException(224);
    return localSnmpOid;
  }

  protected boolean contains(SnmpOid paramSnmpOid, Object paramObject)
  {
    SnmpTableHandler localSnmpTableHandler = getHandler(paramObject);
    if (localSnmpTableHandler == null)
      return false;
    return localSnmpTableHandler.contains(paramSnmpOid);
  }

  public Object getEntry(SnmpOid paramSnmpOid)
    throws SnmpStatusException
  {
    if ((paramSnmpOid == null) || (paramSnmpOid.getLength() < 2))
      throw new SnmpStatusException(224);
    Map localMap = JvmContextFactory.getUserData();
    long l1 = paramSnmpOid.getOidArc(0);
    long l2 = paramSnmpOid.getOidArc(1);
    String str = "JvmMemMgrPoolRelTable.entry." + l1 + "." + l2;
    if (localMap != null)
    {
      localObject1 = localMap.get(str);
      if (localObject1 != null)
        return localObject1;
    }
    Object localObject1 = getHandler(localMap);
    if (localObject1 == null)
      throw new SnmpStatusException(224);
    Object localObject2 = ((SnmpTableHandler)localObject1).getData(paramSnmpOid);
    if (!(localObject2 instanceof JvmMemMgrPoolRelEntryImpl))
      throw new SnmpStatusException(224);
    JvmMemMgrPoolRelEntryImpl localJvmMemMgrPoolRelEntryImpl = (JvmMemMgrPoolRelEntryImpl)localObject2;
    if ((localMap != null) && (localJvmMemMgrPoolRelEntryImpl != null))
      localMap.put(str, localJvmMemMgrPoolRelEntryImpl);
    return localJvmMemMgrPoolRelEntryImpl;
  }

  protected SnmpTableHandler getHandler(Object paramObject)
  {
    Map localMap;
    if (paramObject instanceof Map)
      localMap = (Map)paramObject;
    else
      localMap = null;
    if (localMap != null)
    {
      localSnmpTableHandler = (SnmpTableHandler)localMap.get("JvmMemMgrPoolRelTable.handler");
      if (localSnmpTableHandler != null)
        return localSnmpTableHandler;
    }
    SnmpTableHandler localSnmpTableHandler = this.cache.getTableHandler();
    if ((localMap != null) && (localSnmpTableHandler != null))
      localMap.put("JvmMemMgrPoolRelTable.handler", localSnmpTableHandler);
    return localSnmpTableHandler;
  }

  private static class JvmMemMgrPoolRelTableCache extends SnmpTableCache
  {
    private final JvmMemMgrPoolRelTableMetaImpl meta;

    JvmMemMgrPoolRelTableCache(JvmMemMgrPoolRelTableMetaImpl paramJvmMemMgrPoolRelTableMetaImpl, long paramLong)
    {
      this.validity = paramLong;
      this.meta = paramJvmMemMgrPoolRelTableMetaImpl;
    }

    public SnmpTableHandler getTableHandler()
    {
      Map localMap = JvmContextFactory.getUserData();
      return getTableDatas(localMap);
    }

    private static Map buildPoolIndexMap(SnmpTableHandler paramSnmpTableHandler)
    {
      if (paramSnmpTableHandler instanceof SnmpCachedData)
        return buildPoolIndexMap((SnmpCachedData)paramSnmpTableHandler);
      HashMap localHashMap = new HashMap();
      SnmpOid localSnmpOid = null;
      while (true)
      {
        String str;
        while (true)
        {
          MemoryPoolMXBean localMemoryPoolMXBean;
          while (true)
          {
            if ((localSnmpOid = paramSnmpTableHandler.getNext(localSnmpOid)) == null)
              break label84;
            localMemoryPoolMXBean = (MemoryPoolMXBean)paramSnmpTableHandler.getData(localSnmpOid);
            if (localMemoryPoolMXBean != null)
              break;
          }
          str = localMemoryPoolMXBean.getName();
          if (str != null)
            break;
        }
        localHashMap.put(str, localSnmpOid);
      }
      label84: return localHashMap;
    }

    private static Map buildPoolIndexMap(SnmpCachedData paramSnmpCachedData)
    {
      if (paramSnmpCachedData == null)
        return Collections.EMPTY_MAP;
      SnmpOid[] arrayOfSnmpOid = paramSnmpCachedData.indexes;
      Object[] arrayOfObject = paramSnmpCachedData.datas;
      int i = arrayOfSnmpOid.length;
      HashMap localHashMap = new HashMap(i);
      for (int j = 0; j < i; ++j)
      {
        SnmpOid localSnmpOid = arrayOfSnmpOid[j];
        if (localSnmpOid == null)
          break label100:
        MemoryPoolMXBean localMemoryPoolMXBean = (MemoryPoolMXBean)arrayOfObject[j];
        if (localMemoryPoolMXBean == null)
          break label100:
        String str = localMemoryPoolMXBean.getName();
        if (str == null)
          break label100:
        label100: localHashMap.put(str, localSnmpOid);
      }
      return localHashMap;
    }

    protected SnmpCachedData updateCachedDatas(Object paramObject)
    {
      SnmpTableHandler localSnmpTableHandler1 = this.meta.getManagerHandler(paramObject);
      SnmpTableHandler localSnmpTableHandler2 = this.meta.getPoolHandler(paramObject);
      long l = System.currentTimeMillis();
      Map localMap = buildPoolIndexMap(localSnmpTableHandler2);
      TreeMap localTreeMap = new TreeMap(SnmpCachedData.oidComparator);
      updateTreeMap(localTreeMap, paramObject, localSnmpTableHandler1, localSnmpTableHandler2, localMap);
      return new SnmpCachedData(l, localTreeMap);
    }

    protected String[] getMemoryPools(Object paramObject, MemoryManagerMXBean paramMemoryManagerMXBean, long paramLong)
    {
      String str = "JvmMemManager." + paramLong + ".getMemoryPools";
      String[] arrayOfString = null;
      if (paramObject instanceof Map)
      {
        arrayOfString = (String[])(String[])((Map)paramObject).get(str);
        if (arrayOfString != null)
          return arrayOfString;
      }
      if (paramMemoryManagerMXBean != null)
        arrayOfString = paramMemoryManagerMXBean.getMemoryPoolNames();
      if ((arrayOfString != null) && (paramObject instanceof Map))
        ((Map)paramObject).put(str, arrayOfString);
      return arrayOfString;
    }

    protected void updateTreeMap(TreeMap paramTreeMap, Object paramObject, MemoryManagerMXBean paramMemoryManagerMXBean, SnmpOid paramSnmpOid, Map paramMap)
    {
      long l1;
      try
      {
        l1 = paramSnmpOid.getOidArc(0);
      }
      catch (SnmpStatusException localSnmpStatusException1)
      {
        JvmMemMgrPoolRelTableMetaImpl.log.debug("updateTreeMap", "Bad MemoryManager OID index: " + paramSnmpOid);
        JvmMemMgrPoolRelTableMetaImpl.log.debug("updateTreeMap", localSnmpStatusException1);
        return;
      }
      String[] arrayOfString = getMemoryPools(paramObject, paramMemoryManagerMXBean, l1);
      if ((arrayOfString == null) || (arrayOfString.length < 1))
        return;
      String str1 = paramMemoryManagerMXBean.getName();
      for (int i = 0; i < arrayOfString.length; ++i)
      {
        long l2;
        String str2 = arrayOfString[i];
        if (str2 == null)
          break label235:
        SnmpOid localSnmpOid1 = (SnmpOid)paramMap.get(str2);
        if (localSnmpOid1 == null)
          break label235:
        try
        {
          l2 = localSnmpOid1.getOidArc(0);
        }
        catch (SnmpStatusException localSnmpStatusException2)
        {
          JvmMemMgrPoolRelTableMetaImpl.log.debug("updateTreeMap", "Bad MemoryPool OID index: " + localSnmpOid1);
          JvmMemMgrPoolRelTableMetaImpl.log.debug("updateTreeMap", localSnmpStatusException2);
          break label235:
        }
        long[] arrayOfLong = { l1, l2 };
        SnmpOid localSnmpOid2 = new SnmpOid(arrayOfLong);
        label235: paramTreeMap.put(localSnmpOid2, new JvmMemMgrPoolRelEntryImpl(str1, str2, (int)l1, (int)l2));
      }
    }

    protected void updateTreeMap(TreeMap paramTreeMap, Object paramObject, SnmpTableHandler paramSnmpTableHandler1, SnmpTableHandler paramSnmpTableHandler2, Map paramMap)
    {
      if (paramSnmpTableHandler1 instanceof SnmpCachedData)
      {
        updateTreeMap(paramTreeMap, paramObject, (SnmpCachedData)paramSnmpTableHandler1, paramSnmpTableHandler2, paramMap);
        return;
      }
      SnmpOid localSnmpOid = null;
      while (true)
      {
        MemoryManagerMXBean localMemoryManagerMXBean;
        while (true)
        {
          if ((localSnmpOid = paramSnmpTableHandler1.getNext(localSnmpOid)) == null)
            return;
          localMemoryManagerMXBean = (MemoryManagerMXBean)paramSnmpTableHandler1.getData(localSnmpOid);
          if (localMemoryManagerMXBean != null)
            break;
        }
        updateTreeMap(paramTreeMap, paramObject, localMemoryManagerMXBean, localSnmpOid, paramMap);
      }
    }

    protected void updateTreeMap(TreeMap paramTreeMap, Object paramObject, SnmpCachedData paramSnmpCachedData, SnmpTableHandler paramSnmpTableHandler, Map paramMap)
    {
      SnmpOid[] arrayOfSnmpOid = paramSnmpCachedData.indexes;
      Object[] arrayOfObject = paramSnmpCachedData.datas;
      int i = arrayOfSnmpOid.length;
      for (int j = i - 1; j > -1; --j)
      {
        MemoryManagerMXBean localMemoryManagerMXBean = (MemoryManagerMXBean)arrayOfObject[j];
        if (localMemoryManagerMXBean == null)
          break label62:
        label62: updateTreeMap(paramTreeMap, paramObject, localMemoryManagerMXBean, arrayOfSnmpOid[j], paramMap);
      }
    }
  }
}