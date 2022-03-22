package sun.management;

import java.lang.management.LockInfo;

public abstract interface LockDataConverterMXBean
{
  public abstract void setLockInfo(LockInfo paramLockInfo);

  public abstract LockInfo getLockInfo();

  public abstract void setLockedSynchronizers(LockInfo[] paramArrayOfLockInfo);

  public abstract LockInfo[] getLockedSynchronizers();
}