package sun.management.snmp.jvminstr;

import com.sun.jmx.snmp.SnmpStatusException;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.lang.management.MemoryUsage;
import java.util.Map;
import sun.management.snmp.jvmmib.EnumJvmMemPoolCollectThreshdSupport;
import sun.management.snmp.jvmmib.EnumJvmMemPoolState;
import sun.management.snmp.jvmmib.EnumJvmMemPoolThreshdSupport;
import sun.management.snmp.jvmmib.EnumJvmMemPoolType;
import sun.management.snmp.jvmmib.JvmMemPoolEntryMBean;
import sun.management.snmp.util.JvmContextFactory;
import sun.management.snmp.util.MibLogger;

public class JvmMemPoolEntryImpl
  implements JvmMemPoolEntryMBean
{
  protected final int jvmMemPoolIndex;
  static final String memoryTag = "jvmMemPoolEntry.getUsage";
  static final String peakMemoryTag = "jvmMemPoolEntry.getPeakUsage";
  static final String collectMemoryTag = "jvmMemPoolEntry.getCollectionUsage";
  static final MemoryUsage ZEROS = new MemoryUsage(3412046466619801600L, 3412046466619801600L, 3412046466619801600L, 3412046466619801600L);
  final String entryMemoryTag;
  final String entryPeakMemoryTag;
  final String entryCollectMemoryTag;
  final MemoryPoolMXBean pool;
  private long jvmMemPoolPeakReset = 3412045521726996480L;
  private static final EnumJvmMemPoolState JvmMemPoolStateValid = new EnumJvmMemPoolState("valid");
  private static final EnumJvmMemPoolState JvmMemPoolStateInvalid = new EnumJvmMemPoolState("invalid");
  private static final EnumJvmMemPoolType EnumJvmMemPoolTypeHeap = new EnumJvmMemPoolType("heap");
  private static final EnumJvmMemPoolType EnumJvmMemPoolTypeNonHeap = new EnumJvmMemPoolType("nonheap");
  private static final EnumJvmMemPoolThreshdSupport EnumJvmMemPoolThreshdSupported = new EnumJvmMemPoolThreshdSupport("supported");
  private static final EnumJvmMemPoolThreshdSupport EnumJvmMemPoolThreshdUnsupported = new EnumJvmMemPoolThreshdSupport("unsupported");
  private static final EnumJvmMemPoolCollectThreshdSupport EnumJvmMemPoolCollectThreshdSupported = new EnumJvmMemPoolCollectThreshdSupport("supported");
  private static final EnumJvmMemPoolCollectThreshdSupport EnumJvmMemPoolCollectThreshdUnsupported = new EnumJvmMemPoolCollectThreshdSupport("unsupported");
  static final MibLogger log = new MibLogger(JvmMemPoolEntryImpl.class);

  MemoryUsage getMemoryUsage()
  {
    Map localMap;
    try
    {
      localMap = JvmContextFactory.getUserData();
      if (localMap != null)
      {
        MemoryUsage localMemoryUsage1 = (MemoryUsage)localMap.get(this.entryMemoryTag);
        if (localMemoryUsage1 != null)
        {
          log.debug("getMemoryUsage", this.entryMemoryTag + " found in cache.");
          return localMemoryUsage1;
        }
        MemoryUsage localMemoryUsage2 = this.pool.getUsage();
        if (localMemoryUsage2 == null)
          localMemoryUsage2 = ZEROS;
        localMap.put(this.entryMemoryTag, localMemoryUsage2);
        return localMemoryUsage2;
      }
      log.trace("getMemoryUsage", "ERROR: should never come here!");
      return this.pool.getUsage();
    }
    catch (RuntimeException localRuntimeException)
    {
      log.trace("getMemoryUsage", "Failed to get MemoryUsage: " + localRuntimeException);
      log.debug("getMemoryUsage", localRuntimeException);
      throw localRuntimeException;
    }
  }

  MemoryUsage getPeakMemoryUsage()
  {
    Map localMap;
    try
    {
      localMap = JvmContextFactory.getUserData();
      if (localMap != null)
      {
        MemoryUsage localMemoryUsage1 = (MemoryUsage)localMap.get(this.entryPeakMemoryTag);
        if (localMemoryUsage1 != null)
        {
          if (log.isDebugOn())
            log.debug("getPeakMemoryUsage", this.entryPeakMemoryTag + " found in cache.");
          return localMemoryUsage1;
        }
        MemoryUsage localMemoryUsage2 = this.pool.getPeakUsage();
        if (localMemoryUsage2 == null)
          localMemoryUsage2 = ZEROS;
        localMap.put(this.entryPeakMemoryTag, localMemoryUsage2);
        return localMemoryUsage2;
      }
      log.trace("getPeakMemoryUsage", "ERROR: should never come here!");
      return ZEROS;
    }
    catch (RuntimeException localRuntimeException)
    {
      log.trace("getPeakMemoryUsage", "Failed to get MemoryUsage: " + localRuntimeException);
      log.debug("getPeakMemoryUsage", localRuntimeException);
      throw localRuntimeException;
    }
  }

  MemoryUsage getCollectMemoryUsage()
  {
    Map localMap;
    try
    {
      localMap = JvmContextFactory.getUserData();
      if (localMap != null)
      {
        MemoryUsage localMemoryUsage1 = (MemoryUsage)localMap.get(this.entryCollectMemoryTag);
        if (localMemoryUsage1 != null)
        {
          if (log.isDebugOn())
            log.debug("getCollectMemoryUsage", this.entryCollectMemoryTag + " found in cache.");
          return localMemoryUsage1;
        }
        MemoryUsage localMemoryUsage2 = this.pool.getCollectionUsage();
        if (localMemoryUsage2 == null)
          localMemoryUsage2 = ZEROS;
        localMap.put(this.entryCollectMemoryTag, localMemoryUsage2);
        return localMemoryUsage2;
      }
      log.trace("getCollectMemoryUsage", "ERROR: should never come here!");
      return ZEROS;
    }
    catch (RuntimeException localRuntimeException)
    {
      log.trace("getPeakMemoryUsage", "Failed to get MemoryUsage: " + localRuntimeException);
      log.debug("getPeakMemoryUsage", localRuntimeException);
      throw localRuntimeException;
    }
  }

  public JvmMemPoolEntryImpl(MemoryPoolMXBean paramMemoryPoolMXBean, int paramInt)
  {
    this.pool = paramMemoryPoolMXBean;
    this.jvmMemPoolIndex = paramInt;
    this.entryMemoryTag = "jvmMemPoolEntry.getUsage." + paramInt;
    this.entryPeakMemoryTag = "jvmMemPoolEntry.getPeakUsage." + paramInt;
    this.entryCollectMemoryTag = "jvmMemPoolEntry.getCollectionUsage." + paramInt;
  }

  public Long getJvmMemPoolMaxSize()
    throws SnmpStatusException
  {
    long l = getMemoryUsage().getMax();
    if (l > -1L)
      return new Long(l);
    return JvmMemoryImpl.Long0;
  }

  public Long getJvmMemPoolUsed()
    throws SnmpStatusException
  {
    long l = getMemoryUsage().getUsed();
    if (l > -1L)
      return new Long(l);
    return JvmMemoryImpl.Long0;
  }

  public Long getJvmMemPoolInitSize()
    throws SnmpStatusException
  {
    long l = getMemoryUsage().getInit();
    if (l > -1L)
      return new Long(l);
    return JvmMemoryImpl.Long0;
  }

  public Long getJvmMemPoolCommitted()
    throws SnmpStatusException
  {
    long l = getMemoryUsage().getCommitted();
    if (l > -1L)
      return new Long(l);
    return JvmMemoryImpl.Long0;
  }

  public Long getJvmMemPoolPeakMaxSize()
    throws SnmpStatusException
  {
    long l = getPeakMemoryUsage().getMax();
    if (l > -1L)
      return new Long(l);
    return JvmMemoryImpl.Long0;
  }

  public Long getJvmMemPoolPeakUsed()
    throws SnmpStatusException
  {
    long l = getPeakMemoryUsage().getUsed();
    if (l > -1L)
      return new Long(l);
    return JvmMemoryImpl.Long0;
  }

  public Long getJvmMemPoolPeakCommitted()
    throws SnmpStatusException
  {
    long l = getPeakMemoryUsage().getCommitted();
    if (l > -1L)
      return new Long(l);
    return JvmMemoryImpl.Long0;
  }

  public Long getJvmMemPoolCollectMaxSize()
    throws SnmpStatusException
  {
    long l = getCollectMemoryUsage().getMax();
    if (l > -1L)
      return new Long(l);
    return JvmMemoryImpl.Long0;
  }

  public Long getJvmMemPoolCollectUsed()
    throws SnmpStatusException
  {
    long l = getCollectMemoryUsage().getUsed();
    if (l > -1L)
      return new Long(l);
    return JvmMemoryImpl.Long0;
  }

  public Long getJvmMemPoolCollectCommitted()
    throws SnmpStatusException
  {
    long l = getCollectMemoryUsage().getCommitted();
    if (l > -1L)
      return new Long(l);
    return JvmMemoryImpl.Long0;
  }

  public Long getJvmMemPoolThreshold()
    throws SnmpStatusException
  {
    if (!(this.pool.isUsageThresholdSupported()))
      return JvmMemoryImpl.Long0;
    long l = this.pool.getUsageThreshold();
    if (l > -1L)
      return new Long(l);
    return JvmMemoryImpl.Long0;
  }

  public void setJvmMemPoolThreshold(Long paramLong)
    throws SnmpStatusException
  {
    long l = paramLong.longValue();
    if (l < 3412046672778231808L)
      throw new SnmpStatusException(10);
    this.pool.setUsageThreshold(l);
  }

  public void checkJvmMemPoolThreshold(Long paramLong)
    throws SnmpStatusException
  {
    if (!(this.pool.isUsageThresholdSupported()))
      throw new SnmpStatusException(12);
    long l = paramLong.longValue();
    if (l < 3412046672778231808L)
      throw new SnmpStatusException(10);
  }

  public EnumJvmMemPoolThreshdSupport getJvmMemPoolThreshdSupport()
    throws SnmpStatusException
  {
    if (this.pool.isUsageThresholdSupported())
      return EnumJvmMemPoolThreshdSupported;
    return EnumJvmMemPoolThreshdUnsupported;
  }

  public Long getJvmMemPoolThreshdCount()
    throws SnmpStatusException
  {
    if (!(this.pool.isUsageThresholdSupported()))
      return JvmMemoryImpl.Long0;
    long l = this.pool.getUsageThresholdCount();
    if (l > -1L)
      return new Long(l);
    return JvmMemoryImpl.Long0;
  }

  public Long getJvmMemPoolCollectThreshold()
    throws SnmpStatusException
  {
    if (!(this.pool.isCollectionUsageThresholdSupported()))
      return JvmMemoryImpl.Long0;
    long l = this.pool.getCollectionUsageThreshold();
    if (l > -1L)
      return new Long(l);
    return JvmMemoryImpl.Long0;
  }

  public void setJvmMemPoolCollectThreshold(Long paramLong)
    throws SnmpStatusException
  {
    long l = paramLong.longValue();
    if (l < 3412046672778231808L)
      throw new SnmpStatusException(10);
    this.pool.setCollectionUsageThreshold(l);
  }

  public void checkJvmMemPoolCollectThreshold(Long paramLong)
    throws SnmpStatusException
  {
    if (!(this.pool.isCollectionUsageThresholdSupported()))
      throw new SnmpStatusException(12);
    long l = paramLong.longValue();
    if (l < 3412046672778231808L)
      throw new SnmpStatusException(10);
  }

  public EnumJvmMemPoolCollectThreshdSupport getJvmMemPoolCollectThreshdSupport()
    throws SnmpStatusException
  {
    if (this.pool.isCollectionUsageThresholdSupported())
      return EnumJvmMemPoolCollectThreshdSupported;
    return EnumJvmMemPoolCollectThreshdUnsupported;
  }

  public Long getJvmMemPoolCollectThreshdCount()
    throws SnmpStatusException
  {
    if (!(this.pool.isCollectionUsageThresholdSupported()))
      return JvmMemoryImpl.Long0;
    long l = this.pool.getCollectionUsageThresholdCount();
    if (l > -1L)
      return new Long(l);
    return JvmMemoryImpl.Long0;
  }

  public static EnumJvmMemPoolType jvmMemPoolType(MemoryType paramMemoryType)
    throws SnmpStatusException
  {
    if (paramMemoryType.equals(MemoryType.HEAP))
      return EnumJvmMemPoolTypeHeap;
    if (paramMemoryType.equals(MemoryType.NON_HEAP))
      return EnumJvmMemPoolTypeNonHeap;
    throw new SnmpStatusException(10);
  }

  public EnumJvmMemPoolType getJvmMemPoolType()
    throws SnmpStatusException
  {
    return jvmMemPoolType(this.pool.getType());
  }

  public String getJvmMemPoolName()
    throws SnmpStatusException
  {
    return JVM_MANAGEMENT_MIB_IMPL.validJavaObjectNameTC(this.pool.getName());
  }

  public Integer getJvmMemPoolIndex()
    throws SnmpStatusException
  {
    return new Integer(this.jvmMemPoolIndex);
  }

  public EnumJvmMemPoolState getJvmMemPoolState()
    throws SnmpStatusException
  {
    if (this.pool.isValid())
      return JvmMemPoolStateValid;
    return JvmMemPoolStateInvalid;
  }

  public synchronized Long getJvmMemPoolPeakReset()
    throws SnmpStatusException
  {
    return new Long(this.jvmMemPoolPeakReset);
  }

  public synchronized void setJvmMemPoolPeakReset(Long paramLong)
    throws SnmpStatusException
  {
    long l1 = paramLong.longValue();
    if (l1 > this.jvmMemPoolPeakReset)
    {
      long l2 = System.currentTimeMillis();
      this.pool.resetPeakUsage();
      this.jvmMemPoolPeakReset = l2;
      log.debug("setJvmMemPoolPeakReset", "jvmMemPoolPeakReset=" + l2);
    }
  }

  public void checkJvmMemPoolPeakReset(Long paramLong)
    throws SnmpStatusException
  {
  }
}