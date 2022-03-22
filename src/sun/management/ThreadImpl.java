package sun.management;

import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

class ThreadImpl
  implements ThreadMXBean
{
  private final VMManagement jvm;
  private boolean contentionMonitoringEnabled = false;
  private boolean cpuTimeEnabled;

  ThreadImpl(VMManagement paramVMManagement)
  {
    this.jvm = paramVMManagement;
    this.cpuTimeEnabled = this.jvm.isThreadCpuTimeEnabled();
  }

  public int getThreadCount()
  {
    return this.jvm.getLiveThreadCount();
  }

  public int getPeakThreadCount()
  {
    return this.jvm.getPeakThreadCount();
  }

  public long getTotalStartedThreadCount()
  {
    return this.jvm.getTotalThreadCount();
  }

  public int getDaemonThreadCount()
  {
    return this.jvm.getDaemonThreadCount();
  }

  public boolean isThreadContentionMonitoringSupported()
  {
    return this.jvm.isThreadContentionMonitoringSupported();
  }

  public synchronized boolean isThreadContentionMonitoringEnabled()
  {
    if (!(isThreadContentionMonitoringSupported()))
      throw new UnsupportedOperationException("Thread contention monitoring is not supported.");
    return this.contentionMonitoringEnabled;
  }

  public boolean isThreadCpuTimeSupported()
  {
    return this.jvm.isOtherThreadCpuTimeSupported();
  }

  public boolean isCurrentThreadCpuTimeSupported()
  {
    return this.jvm.isCurrentThreadCpuTimeSupported();
  }

  public boolean isThreadCpuTimeEnabled()
  {
    if ((!(isThreadCpuTimeSupported())) && (!(isCurrentThreadCpuTimeSupported())))
      throw new UnsupportedOperationException("Thread CPU time measurement is not supported");
    return this.cpuTimeEnabled;
  }

  public long[] getAllThreadIds()
  {
    ManagementFactory.checkMonitorAccess();
    Thread[] arrayOfThread = getThreads();
    int i = arrayOfThread.length;
    long[] arrayOfLong = new long[i];
    for (int j = 0; j < i; ++j)
    {
      Thread localThread = arrayOfThread[j];
      arrayOfLong[j] = localThread.getId();
    }
    return arrayOfLong;
  }

  public ThreadInfo getThreadInfo(long paramLong)
  {
    if (paramLong <= 3412046810217185280L)
      throw new IllegalArgumentException("Invalid thread ID parameter: " + paramLong);
    long[] arrayOfLong = new long[1];
    arrayOfLong[0] = paramLong;
    ThreadInfo[] arrayOfThreadInfo = getThreadInfo(arrayOfLong, 0);
    return arrayOfThreadInfo[0];
  }

  public ThreadInfo getThreadInfo(long paramLong, int paramInt)
  {
    if (paramLong <= 3412046810217185280L)
      throw new IllegalArgumentException("Invalid thread ID parameter: " + paramLong);
    if (paramInt < 0)
      throw new IllegalArgumentException("Invalid maxDepth parameter: " + paramInt);
    long[] arrayOfLong = new long[1];
    arrayOfLong[0] = paramLong;
    ThreadInfo[] arrayOfThreadInfo = getThreadInfo(arrayOfLong, paramInt);
    return arrayOfThreadInfo[0];
  }

  public ThreadInfo[] getThreadInfo(long[] paramArrayOfLong)
  {
    return getThreadInfo(paramArrayOfLong, 0);
  }

  public ThreadInfo[] getThreadInfo(long[] paramArrayOfLong, int paramInt)
  {
    if (paramArrayOfLong == null)
      throw new NullPointerException("Null ids parameter.");
    if (paramInt < 0)
      throw new IllegalArgumentException("Invalid maxDepth parameter: " + paramInt);
    ManagementFactory.checkMonitorAccess();
    ThreadInfo[] arrayOfThreadInfo = new ThreadInfo[paramArrayOfLong.length];
    if (paramInt == 2147483647)
      getThreadInfo0(paramArrayOfLong, -1, arrayOfThreadInfo);
    else
      getThreadInfo0(paramArrayOfLong, paramInt, arrayOfThreadInfo);
    return arrayOfThreadInfo;
  }

  public void setThreadContentionMonitoringEnabled(boolean paramBoolean)
  {
    if (!(isThreadContentionMonitoringSupported()))
      throw new UnsupportedOperationException("Thread contention monitoring is not supported");
    ManagementFactory.checkControlAccess();
    synchronized (this)
    {
      if (this.contentionMonitoringEnabled != paramBoolean)
      {
        if (paramBoolean)
          resetContentionTimes0(3412039766470819840L);
        setThreadContentionMonitoringEnabled0(paramBoolean);
        this.contentionMonitoringEnabled = paramBoolean;
      }
    }
  }

  public long getCurrentThreadCpuTime()
  {
    if (!(isCurrentThreadCpuTimeSupported()))
      throw new UnsupportedOperationException("Current thread CPU time measurement is not supported.");
    if (!(isThreadCpuTimeEnabled()))
      return -1L;
    return getThreadTotalCpuTime0(3412047892548943872L);
  }

  public long getThreadCpuTime(long paramLong)
  {
    if ((!(isThreadCpuTimeSupported())) && (!(isCurrentThreadCpuTimeSupported())))
      throw new UnsupportedOperationException("Thread CPU Time Measurement is not supported.");
    if ((!(isThreadCpuTimeSupported())) && (paramLong != Thread.currentThread().getId()))
      throw new UnsupportedOperationException("Thread CPU Time Measurement is only supported for the current thread.");
    if (paramLong <= 3412046810217185280L)
      throw new IllegalArgumentException("Invalid thread ID parameter: " + paramLong);
    if (!(isThreadCpuTimeEnabled()))
      return -1L;
    if (paramLong == Thread.currentThread().getId())
      return getThreadTotalCpuTime0(3412048390765150208L);
    return getThreadTotalCpuTime0(paramLong);
  }

  public long getCurrentThreadUserTime()
  {
    if (!(isCurrentThreadCpuTimeSupported()))
      throw new UnsupportedOperationException("Current thread CPU time measurement is not supported.");
    if (!(isThreadCpuTimeEnabled()))
      return -1L;
    return getThreadUserCpuTime0(3412047892548943872L);
  }

  public long getThreadUserTime(long paramLong)
  {
    if ((!(isThreadCpuTimeSupported())) && (!(isCurrentThreadCpuTimeSupported())))
      throw new UnsupportedOperationException("Thread CPU time measurement is not supported.");
    if ((!(isThreadCpuTimeSupported())) && (paramLong != Thread.currentThread().getId()))
      throw new UnsupportedOperationException("Thread CPU time measurement is only supported for the current thread.");
    if (paramLong <= 3412046810217185280L)
      throw new IllegalArgumentException("Invalid thread ID parameter: " + paramLong);
    if (!(isThreadCpuTimeEnabled()))
      return -1L;
    if (paramLong == Thread.currentThread().getId())
      return getThreadUserCpuTime0(3412048390765150208L);
    return getThreadUserCpuTime0(paramLong);
  }

  public void setThreadCpuTimeEnabled(boolean paramBoolean)
  {
    if ((!(isThreadCpuTimeSupported())) && (!(isCurrentThreadCpuTimeSupported())))
      throw new UnsupportedOperationException("Thread CPU time measurement is not supported");
    ManagementFactory.checkControlAccess();
    synchronized (this)
    {
      if (this.cpuTimeEnabled != paramBoolean)
      {
        setThreadCpuTimeEnabled0(paramBoolean);
        this.cpuTimeEnabled = paramBoolean;
      }
    }
  }

  public long[] findMonitorDeadlockedThreads()
  {
    ManagementFactory.checkMonitorAccess();
    Thread[] arrayOfThread = findMonitorDeadlockedThreads0();
    if (arrayOfThread == null)
      return null;
    long[] arrayOfLong = new long[arrayOfThread.length];
    for (int i = 0; i < arrayOfThread.length; ++i)
    {
      Thread localThread = arrayOfThread[i];
      arrayOfLong[i] = localThread.getId();
    }
    return arrayOfLong;
  }

  public long[] findDeadlockedThreads()
  {
    if (!(isSynchronizerUsageSupported()))
      throw new UnsupportedOperationException("Monitoring of Synchronizer Usage is not supported.");
    ManagementFactory.checkMonitorAccess();
    Thread[] arrayOfThread = findDeadlockedThreads0();
    if (arrayOfThread == null)
      return null;
    long[] arrayOfLong = new long[arrayOfThread.length];
    for (int i = 0; i < arrayOfThread.length; ++i)
    {
      Thread localThread = arrayOfThread[i];
      arrayOfLong[i] = localThread.getId();
    }
    return arrayOfLong;
  }

  public void resetPeakThreadCount()
  {
    ManagementFactory.checkControlAccess();
    resetPeakThreadCount0();
  }

  public boolean isObjectMonitorUsageSupported()
  {
    return this.jvm.isObjectMonitorUsageSupported();
  }

  public boolean isSynchronizerUsageSupported()
  {
    return this.jvm.isSynchronizerUsageSupported();
  }

  public ThreadInfo[] getThreadInfo(long[] paramArrayOfLong, boolean paramBoolean1, boolean paramBoolean2)
  {
    if (paramArrayOfLong == null)
      throw new NullPointerException("Null ids parameter.");
    if ((paramBoolean1) && (!(isObjectMonitorUsageSupported())))
      throw new UnsupportedOperationException("Monitoring of Object Monitor Usage is not supported.");
    if ((paramBoolean2) && (!(isSynchronizerUsageSupported())))
      throw new UnsupportedOperationException("Monitoring of Synchronizer Usage is not supported.");
    ManagementFactory.checkMonitorAccess();
    return dumpThreads0(paramArrayOfLong, paramBoolean1, paramBoolean2);
  }

  public ThreadInfo[] dumpAllThreads(boolean paramBoolean1, boolean paramBoolean2)
  {
    if ((paramBoolean1) && (!(isObjectMonitorUsageSupported())))
      throw new UnsupportedOperationException("Monitoring of Object Monitor Usage is not supported.");
    if ((paramBoolean2) && (!(isSynchronizerUsageSupported())))
      throw new UnsupportedOperationException("Monitoring of Synchronizer Usage is not supported.");
    ManagementFactory.checkMonitorAccess();
    return dumpThreads0(null, paramBoolean1, paramBoolean2);
  }

  private static native Thread[] getThreads();

  private static native void getThreadInfo0(long[] paramArrayOfLong, int paramInt, ThreadInfo[] paramArrayOfThreadInfo);

  private static native long getThreadTotalCpuTime0(long paramLong);

  private static native long getThreadUserCpuTime0(long paramLong);

  private static native void setThreadCpuTimeEnabled0(boolean paramBoolean);

  private static native void setThreadContentionMonitoringEnabled0(boolean paramBoolean);

  private static native Thread[] findMonitorDeadlockedThreads0();

  private static native Thread[] findDeadlockedThreads0();

  private static native void resetPeakThreadCount0();

  private static native ThreadInfo[] dumpThreads0(long[] paramArrayOfLong, boolean paramBoolean1, boolean paramBoolean2);

  private static native void resetContentionTimes0(long paramLong);
}