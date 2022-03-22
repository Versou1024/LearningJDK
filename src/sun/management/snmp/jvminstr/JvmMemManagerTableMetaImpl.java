package sun.management.snmp.jvminstr;

import com.sun.jmx.snmp.SnmpOid;
import com.sun.jmx.snmp.SnmpStatusException;
import com.sun.jmx.snmp.agent.SnmpMib;
import com.sun.jmx.snmp.agent.SnmpStandardObjectServer;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryManagerMXBean;
import java.util.List;
import java.util.Map;
import sun.management.snmp.jvmmib.JvmMemManagerTableMeta;
import sun.management.snmp.util.JvmContextFactory;
import sun.management.snmp.util.MibLogger;
import sun.management.snmp.util.SnmpNamedListTableCache;
import sun.management.snmp.util.SnmpTableCache;
import sun.management.snmp.util.SnmpTableHandler;

public class JvmMemManagerTableMetaImpl extends JvmMemManagerTableMeta
{
  protected SnmpTableCache cache;
  static final MibLogger log = new MibLogger(JvmMemManagerTableMetaImpl.class);

  public JvmMemManagerTableMetaImpl(SnmpMib paramSnmpMib, SnmpStandardObjectServer paramSnmpStandardObjectServer)
  {
    super(paramSnmpMib, paramSnmpStandardObjectServer);
    this.cache = new JvmMemManagerTableCache(((JVM_MANAGEMENT_MIB_IMPL)paramSnmpMib).validity());
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
    if (paramSnmpOid == null)
      throw new SnmpStatusException(224);
    Map localMap = JvmContextFactory.getUserData();
    long l = paramSnmpOid.getOidArc(0);
    String str = "JvmMemManagerTable.entry." + l;
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
    if (localObject2 == null)
      throw new SnmpStatusException(224);
    JvmMemManagerEntryImpl localJvmMemManagerEntryImpl = new JvmMemManagerEntryImpl((MemoryManagerMXBean)localObject2, (int)l);
    if ((localMap != null) && (localJvmMemManagerEntryImpl != null))
      localMap.put(str, localJvmMemManagerEntryImpl);
    return localJvmMemManagerEntryImpl;
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
      localSnmpTableHandler = (SnmpTableHandler)localMap.get("JvmMemManagerTable.handler");
      if (localSnmpTableHandler != null)
        return localSnmpTableHandler;
    }
    SnmpTableHandler localSnmpTableHandler = this.cache.getTableHandler();
    if ((localMap != null) && (localSnmpTableHandler != null))
      localMap.put("JvmMemManagerTable.handler", localSnmpTableHandler);
    return localSnmpTableHandler;
  }

  private static class JvmMemManagerTableCache extends SnmpNamedListTableCache
  {
    JvmMemManagerTableCache(long paramLong)
    {
      this.validity = paramLong;
    }

    protected String getKey(Object paramObject1, List paramList, int paramInt, Object paramObject2)
    {
      if (paramObject2 == null)
        return null;
      String str = ((MemoryManagerMXBean)paramObject2).getName();
      JvmMemManagerTableMetaImpl.log.debug("getKey", "key=" + str);
      return str;
    }

    public SnmpTableHandler getTableHandler()
    {
      Map localMap = JvmContextFactory.getUserData();
      return getTableDatas(localMap);
    }

    protected String getRawDatasKey()
    {
      return "JvmMemManagerTable.getMemoryManagers";
    }

    protected List loadRawDatas(Map paramMap)
    {
      return ManagementFactory.getMemoryManagerMXBeans();
    }
  }
}