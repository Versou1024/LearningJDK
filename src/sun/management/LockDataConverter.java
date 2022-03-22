package sun.management;

import java.lang.management.LockInfo;
import java.lang.management.ThreadInfo;
import javax.management.Attribute;
import javax.management.StandardMBean;
import javax.management.openmbean.CompositeData;

class LockDataConverter extends StandardMBean
  implements LockDataConverterMXBean
{
  private LockInfo lockInfo;
  private LockInfo[] lockedSyncs;

  LockDataConverter()
  {
    super(LockDataConverterMXBean.class, true);
    this.lockInfo = null;
    this.lockedSyncs = null;
  }

  LockDataConverter(ThreadInfo paramThreadInfo)
  {
    super(LockDataConverterMXBean.class, true);
    this.lockInfo = paramThreadInfo.getLockInfo();
    this.lockedSyncs = paramThreadInfo.getLockedSynchronizers();
  }

  public void setLockInfo(LockInfo paramLockInfo)
  {
    this.lockInfo = paramLockInfo;
  }

  public LockInfo getLockInfo()
  {
    return this.lockInfo;
  }

  public void setLockedSynchronizers(LockInfo[] paramArrayOfLockInfo)
  {
    this.lockedSyncs = paramArrayOfLockInfo;
  }

  public LockInfo[] getLockedSynchronizers()
  {
    return this.lockedSyncs;
  }

  CompositeData toLockInfoCompositeData()
  {
    try
    {
      return ((CompositeData)getAttribute("LockInfo"));
    }
    catch (Exception localException)
    {
      throw Util.newInternalError(localException);
    }
  }

  CompositeData[] toLockedSynchronizersCompositeData()
  {
    try
    {
      return ((CompositeData[])(CompositeData[])getAttribute("LockedSynchronizers"));
    }
    catch (Exception localException)
    {
      throw Util.newInternalError(localException);
    }
  }

  LockInfo toLockInfo(CompositeData paramCompositeData)
  {
    try
    {
      setAttribute(new Attribute("LockInfo", paramCompositeData));
    }
    catch (Exception localException)
    {
      throw Util.newInternalError(localException);
    }
    return getLockInfo();
  }

  LockInfo[] toLockedSynchronizers(CompositeData[] paramArrayOfCompositeData)
  {
    try
    {
      setAttribute(new Attribute("LockedSynchronizers", paramArrayOfCompositeData));
    }
    catch (Exception localException)
    {
      throw Util.newInternalError(localException);
    }
    return getLockedSynchronizers();
  }

  static CompositeData toLockInfoCompositeData(LockInfo paramLockInfo)
  {
    LockDataConverter localLockDataConverter = new LockDataConverter();
    localLockDataConverter.setLockInfo(paramLockInfo);
    return localLockDataConverter.toLockInfoCompositeData();
  }
}