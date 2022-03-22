package sun.management.snmp.jvminstr;

import com.sun.jmx.snmp.SnmpOid;
import com.sun.jmx.snmp.SnmpStatusException;
import com.sun.jmx.snmp.agent.SnmpMib;
import com.sun.jmx.snmp.agent.SnmpStandardObjectServer;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Map;
import java.util.TreeMap;
import sun.management.snmp.jvmmib.JvmThreadInstanceTableMeta;
import sun.management.snmp.util.JvmContextFactory;
import sun.management.snmp.util.MibLogger;
import sun.management.snmp.util.SnmpCachedData;
import sun.management.snmp.util.SnmpTableCache;
import sun.management.snmp.util.SnmpTableHandler;

public class JvmThreadInstanceTableMetaImpl extends JvmThreadInstanceTableMeta
{
  public static final int MAX_STACK_TRACE_DEPTH = 0;
  protected SnmpTableCache cache;
  static final MibLogger log = new MibLogger(JvmThreadInstanceTableMetaImpl.class);

  static SnmpOid makeOid(long paramLong)
  {
    long[] arrayOfLong = new long[8];
    arrayOfLong[0] = (paramLong >> 56 & 0xFF);
    arrayOfLong[1] = (paramLong >> 48 & 0xFF);
    arrayOfLong[2] = (paramLong >> 40 & 0xFF);
    arrayOfLong[3] = (paramLong >> 32 & 0xFF);
    arrayOfLong[4] = (paramLong >> 24 & 0xFF);
    arrayOfLong[5] = (paramLong >> 16 & 0xFF);
    arrayOfLong[6] = (paramLong >> 8 & 0xFF);
    arrayOfLong[7] = (paramLong & 0xFF);
    return new SnmpOid(arrayOfLong);
  }

  static long makeId(SnmpOid paramSnmpOid)
  {
    long l = 3412047291253522432L;
    long[] arrayOfLong = paramSnmpOid.longValue(false);
    l |= arrayOfLong[0] << 56;
    l |= arrayOfLong[1] << 48;
    l |= arrayOfLong[2] << 40;
    l |= arrayOfLong[3] << 32;
    l |= arrayOfLong[4] << 24;
    l |= arrayOfLong[5] << 16;
    l |= arrayOfLong[6] << 8;
    l |= arrayOfLong[7];
    return l;
  }

  public JvmThreadInstanceTableMetaImpl(SnmpMib paramSnmpMib, SnmpStandardObjectServer paramSnmpStandardObjectServer)
  {
    super(paramSnmpMib, paramSnmpStandardObjectServer);
    this.cache = new JvmThreadInstanceTableCache(this, ((JVM_MANAGEMENT_MIB_IMPL)paramSnmpMib).validity());
    log.debug("JvmThreadInstanceTableMetaImpl", "Create Thread meta");
  }

  protected SnmpOid getNextOid(Object paramObject)
    throws SnmpStatusException
  {
    log.debug("JvmThreadInstanceTableMetaImpl", "getNextOid");
    return getNextOid(null, paramObject);
  }

  protected SnmpOid getNextOid(SnmpOid paramSnmpOid, Object paramObject)
    throws SnmpStatusException
  {
    log.debug("getNextOid", "previous=" + paramSnmpOid);
    SnmpTableHandler localSnmpTableHandler = getHandler(paramObject);
    if (localSnmpTableHandler == null)
    {
      log.debug("getNextOid", "handler is null!");
      throw new SnmpStatusException(224);
    }
    SnmpOid localSnmpOid = paramSnmpOid;
    do
    {
      localSnmpOid = localSnmpTableHandler.getNext(localSnmpOid);
      if (localSnmpOid == null)
        break;
    }
    while (getJvmThreadInstance(paramObject, localSnmpOid) == null);
    log.debug("*** **** **** **** getNextOid", "next=" + localSnmpOid);
    if (localSnmpOid == null)
      throw new SnmpStatusException(224);
    return localSnmpOid;
  }

  protected boolean contains(SnmpOid paramSnmpOid, Object paramObject)
  {
    SnmpTableHandler localSnmpTableHandler = getHandler(paramObject);
    if (localSnmpTableHandler == null)
      return false;
    if (!(localSnmpTableHandler.contains(paramSnmpOid)))
      return false;
    JvmThreadInstanceEntryImpl localJvmThreadInstanceEntryImpl = getJvmThreadInstance(paramObject, paramSnmpOid);
    return (localJvmThreadInstanceEntryImpl != null);
  }

  public Object getEntry(SnmpOid paramSnmpOid)
    throws SnmpStatusException
  {
    log.debug("*** **** **** **** getEntry", "oid [" + paramSnmpOid + "]");
    if ((paramSnmpOid == null) || (paramSnmpOid.getLength() != 8))
    {
      log.debug("getEntry", "Invalid oid [" + paramSnmpOid + "]");
      throw new SnmpStatusException(224);
    }
    Map localMap = JvmContextFactory.getUserData();
    SnmpTableHandler localSnmpTableHandler = getHandler(localMap);
    if ((localSnmpTableHandler == null) || (!(localSnmpTableHandler.contains(paramSnmpOid))))
      throw new SnmpStatusException(224);
    JvmThreadInstanceEntryImpl localJvmThreadInstanceEntryImpl = getJvmThreadInstance(localMap, paramSnmpOid);
    if (localJvmThreadInstanceEntryImpl == null)
      throw new SnmpStatusException(224);
    return localJvmThreadInstanceEntryImpl;
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
      localSnmpTableHandler = (SnmpTableHandler)localMap.get("JvmThreadInstanceTable.handler");
      if (localSnmpTableHandler != null)
        return localSnmpTableHandler;
    }
    SnmpTableHandler localSnmpTableHandler = this.cache.getTableHandler();
    if ((localMap != null) && (localSnmpTableHandler != null))
      localMap.put("JvmThreadInstanceTable.handler", localSnmpTableHandler);
    return localSnmpTableHandler;
  }

  private ThreadInfo getThreadInfo(long paramLong)
  {
    return JvmThreadingImpl.getThreadMXBean().getThreadInfo(paramLong, 0);
  }

  private ThreadInfo getThreadInfo(SnmpOid paramSnmpOid)
  {
    return getThreadInfo(makeId(paramSnmpOid));
  }

  private JvmThreadInstanceEntryImpl getJvmThreadInstance(Object paramObject, SnmpOid paramSnmpOid)
  {
    JvmThreadInstanceEntryImpl localJvmThreadInstanceEntryImpl = null;
    String str = null;
    Map localMap = null;
    boolean bool = log.isDebugOn();
    if (paramObject instanceof Map)
    {
      localMap = (Map)paramObject;
      str = "JvmThreadInstanceTable.entry." + paramSnmpOid.toString();
      localJvmThreadInstanceEntryImpl = (JvmThreadInstanceEntryImpl)localMap.get(str);
    }
    if (localJvmThreadInstanceEntryImpl != null)
    {
      if (bool)
        log.debug("*** getJvmThreadInstance", "Entry found in cache: " + str);
      return localJvmThreadInstanceEntryImpl;
    }
    if (bool)
      log.debug("*** getJvmThreadInstance", "Entry [" + paramSnmpOid + "] is not in cache");
    ThreadInfo localThreadInfo = null;
    try
    {
      localThreadInfo = getThreadInfo(paramSnmpOid);
    }
    catch (RuntimeException localRuntimeException)
    {
      log.trace("*** getJvmThreadInstance", "Failed to get thread info for rowOid: " + paramSnmpOid);
      log.debug("*** getJvmThreadInstance", localRuntimeException);
    }
    if (localThreadInfo == null)
    {
      if (bool)
        log.debug("*** getJvmThreadInstance", "No entry by that oid [" + paramSnmpOid + "]");
      return null;
    }
    localJvmThreadInstanceEntryImpl = new JvmThreadInstanceEntryImpl(localThreadInfo, paramSnmpOid.toByte());
    if (localMap != null)
      localMap.put(str, localJvmThreadInstanceEntryImpl);
    if (bool)
      log.debug("*** getJvmThreadInstance", "Entry created for Thread OID [" + paramSnmpOid + "]");
    return localJvmThreadInstanceEntryImpl;
  }

  private static class JvmThreadInstanceTableCache extends SnmpTableCache
  {
    private final JvmThreadInstanceTableMetaImpl meta;

    JvmThreadInstanceTableCache(JvmThreadInstanceTableMetaImpl paramJvmThreadInstanceTableMetaImpl, long paramLong)
    {
      this.validity = paramLong;
      this.meta = paramJvmThreadInstanceTableMetaImpl;
    }

    public SnmpTableHandler getTableHandler()
    {
      Map localMap = JvmContextFactory.getUserData();
      return getTableDatas(localMap);
    }

    protected SnmpCachedData updateCachedDatas(Object paramObject)
    {
      long[] arrayOfLong = JvmThreadingImpl.getThreadMXBean().getAllThreadIds();
      long l = System.currentTimeMillis();
      SnmpOid[] arrayOfSnmpOid = new SnmpOid[arrayOfLong.length];
      TreeMap localTreeMap = new TreeMap(SnmpCachedData.oidComparator);
      for (int i = 0; i < arrayOfLong.length; ++i)
      {
        JvmThreadInstanceTableMetaImpl.log.debug("", "Making index for thread id [" + arrayOfLong[i] + "]");
        SnmpOid localSnmpOid = JvmThreadInstanceTableMetaImpl.makeOid(arrayOfLong[i]);
        localTreeMap.put(localSnmpOid, localSnmpOid);
      }
      return new SnmpCachedData(l, localTreeMap);
    }
  }
}