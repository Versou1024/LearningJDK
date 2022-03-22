package sun.management.snmp.jvminstr;

import com.sun.jmx.snmp.SnmpOid;
import com.sun.jmx.snmp.SnmpStatusException;
import com.sun.jmx.snmp.agent.SnmpMib;
import com.sun.jmx.snmp.agent.SnmpStandardObjectServer;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.MemoryManagerMXBean;
import java.util.Map;
import sun.management.snmp.jvmmib.JvmMemGCTableMeta;
import sun.management.snmp.util.JvmContextFactory;
import sun.management.snmp.util.MibLogger;
import sun.management.snmp.util.SnmpCachedData;
import sun.management.snmp.util.SnmpTableHandler;

public class JvmMemGCTableMetaImpl extends JvmMemGCTableMeta
{
  private transient JvmMemManagerTableMetaImpl managers = null;
  private static GCTableFilter filter = new GCTableFilter();
  static final MibLogger log = new MibLogger(JvmMemGCTableMetaImpl.class);

  public JvmMemGCTableMetaImpl(SnmpMib paramSnmpMib, SnmpStandardObjectServer paramSnmpStandardObjectServer)
  {
    super(paramSnmpMib, paramSnmpStandardObjectServer);
  }

  private final JvmMemManagerTableMetaImpl getManagers(SnmpMib paramSnmpMib)
  {
    if (this.managers == null)
      this.managers = ((JvmMemManagerTableMetaImpl)paramSnmpMib.getRegisteredTableMeta("JvmMemManagerTable"));
    return this.managers;
  }

  protected SnmpTableHandler getHandler(Object paramObject)
  {
    JvmMemManagerTableMetaImpl localJvmMemManagerTableMetaImpl = getManagers(this.theMib);
    return localJvmMemManagerTableMetaImpl.getHandler(paramObject);
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
    try
    {
      if (bool)
        log.debug("getNextOid", "previous=" + paramSnmpOid);
      SnmpTableHandler localSnmpTableHandler = getHandler(paramObject);
      if (localSnmpTableHandler == null)
      {
        if (bool)
          log.debug("getNextOid", "handler is null!");
        throw new SnmpStatusException(224);
      }
      SnmpOid localSnmpOid = filter.getNext(localSnmpTableHandler, paramSnmpOid);
      if (bool)
        log.debug("getNextOid", "next=" + localSnmpOid);
      if (localSnmpOid == null)
        throw new SnmpStatusException(224);
      return localSnmpOid;
    }
    catch (RuntimeException localRuntimeException)
    {
      if (bool)
        log.debug("getNextOid", localRuntimeException);
      throw localRuntimeException;
    }
  }

  protected boolean contains(SnmpOid paramSnmpOid, Object paramObject)
  {
    SnmpTableHandler localSnmpTableHandler = getHandler(paramObject);
    if (localSnmpTableHandler == null)
      return false;
    return filter.contains(localSnmpTableHandler, paramSnmpOid);
  }

  public Object getEntry(SnmpOid paramSnmpOid)
    throws SnmpStatusException
  {
    if (paramSnmpOid == null)
      throw new SnmpStatusException(224);
    Map localMap = JvmContextFactory.getUserData();
    long l = paramSnmpOid.getOidArc(0);
    String str = "JvmMemGCTable.entry." + l;
    if (localMap != null)
    {
      localObject1 = localMap.get(str);
      if (localObject1 != null)
        return localObject1;
    }
    Object localObject1 = getHandler(localMap);
    if (localObject1 == null)
      throw new SnmpStatusException(224);
    Object localObject2 = filter.getData((SnmpTableHandler)localObject1, paramSnmpOid);
    if (localObject2 == null)
      throw new SnmpStatusException(224);
    JvmMemGCEntryImpl localJvmMemGCEntryImpl = new JvmMemGCEntryImpl((GarbageCollectorMXBean)localObject2, (int)l);
    if ((localMap != null) && (localJvmMemGCEntryImpl != null))
      localMap.put(str, localJvmMemGCEntryImpl);
    return localJvmMemGCEntryImpl;
  }

  protected static class GCTableFilter
  {
    public SnmpOid getNext(SnmpCachedData paramSnmpCachedData, SnmpOid paramSnmpOid)
    {
      boolean bool = JvmMemGCTableMetaImpl.log.isDebugOn();
      int i = (paramSnmpOid == null) ? -1 : paramSnmpCachedData.find(paramSnmpOid);
      if (bool)
        JvmMemGCTableMetaImpl.log.debug("GCTableFilter", "oid=" + paramSnmpOid + " at insertion=" + i);
      if (i > -1)
        j = i + 1;
      for (int j = -i - 1; j < paramSnmpCachedData.indexes.length; ++j)
      {
        if (bool)
          JvmMemGCTableMetaImpl.log.debug("GCTableFilter", "next=" + j);
        Object localObject = paramSnmpCachedData.datas[j];
        if (bool)
          JvmMemGCTableMetaImpl.log.debug("GCTableFilter", "value[" + j + "]=" + ((MemoryManagerMXBean)localObject).getName());
        if (localObject instanceof GarbageCollectorMXBean)
        {
          if (bool)
            JvmMemGCTableMetaImpl.log.debug("GCTableFilter", ((MemoryManagerMXBean)localObject).getName() + " is a  GarbageCollectorMXBean.");
          return paramSnmpCachedData.indexes[j];
        }
        if (bool)
          JvmMemGCTableMetaImpl.log.debug("GCTableFilter", ((MemoryManagerMXBean)localObject).getName() + " is not a  GarbageCollectorMXBean: " + localObject.getClass().getName());
      }
      return null;
    }

    public SnmpOid getNext(SnmpTableHandler paramSnmpTableHandler, SnmpOid paramSnmpOid)
    {
      if (paramSnmpTableHandler instanceof SnmpCachedData)
        return getNext((SnmpCachedData)paramSnmpTableHandler, paramSnmpOid);
      SnmpOid localSnmpOid = paramSnmpOid;
      do
      {
        localSnmpOid = paramSnmpTableHandler.getNext(localSnmpOid);
        Object localObject = paramSnmpTableHandler.getData(localSnmpOid);
        if (localObject instanceof GarbageCollectorMXBean)
          return localSnmpOid;
      }
      while (localSnmpOid != null);
      return null;
    }

    public Object getData(SnmpTableHandler paramSnmpTableHandler, SnmpOid paramSnmpOid)
    {
      Object localObject = paramSnmpTableHandler.getData(paramSnmpOid);
      if (localObject instanceof GarbageCollectorMXBean)
        return localObject;
      return null;
    }

    public boolean contains(SnmpTableHandler paramSnmpTableHandler, SnmpOid paramSnmpOid)
    {
      return (paramSnmpTableHandler.getData(paramSnmpOid) instanceof GarbageCollectorMXBean);
    }
  }
}