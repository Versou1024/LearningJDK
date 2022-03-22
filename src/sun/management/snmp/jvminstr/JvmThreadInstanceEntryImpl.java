package sun.management.snmp.jvminstr;

import com.sun.jmx.snmp.SnmpOid;
import com.sun.jmx.snmp.SnmpOidRecord;
import com.sun.jmx.snmp.SnmpOidTable;
import com.sun.jmx.snmp.SnmpStatusException;
import java.io.Serializable;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import sun.management.snmp.jvmmib.JVM_MANAGEMENT_MIBOidTable;
import sun.management.snmp.jvmmib.JvmThreadInstanceEntryMBean;
import sun.management.snmp.util.MibLogger;

public class JvmThreadInstanceEntryImpl
  implements JvmThreadInstanceEntryMBean, Serializable
{
  private final ThreadInfo info;
  private final Byte[] index;
  private static String jvmThreadInstIndexOid = null;
  static final MibLogger log = new MibLogger(JvmThreadInstanceEntryImpl.class);

  public JvmThreadInstanceEntryImpl(ThreadInfo paramThreadInfo, Byte[] paramArrayOfByte)
  {
    this.info = paramThreadInfo;
    this.index = paramArrayOfByte;
  }

  public static String getJvmThreadInstIndexOid()
    throws SnmpStatusException
  {
    if (jvmThreadInstIndexOid == null)
    {
      JVM_MANAGEMENT_MIBOidTable localJVM_MANAGEMENT_MIBOidTable = new JVM_MANAGEMENT_MIBOidTable();
      SnmpOidRecord localSnmpOidRecord = localJVM_MANAGEMENT_MIBOidTable.resolveVarName("jvmThreadInstIndex");
      jvmThreadInstIndexOid = localSnmpOidRecord.getOid();
    }
    return jvmThreadInstIndexOid;
  }

  public String getJvmThreadInstLockOwnerPtr()
    throws SnmpStatusException
  {
    long l = this.info.getLockOwnerId();
    if (l == -1L)
      return new String("0.0");
    SnmpOid localSnmpOid = JvmThreadInstanceTableMetaImpl.makeOid(l);
    return getJvmThreadInstIndexOid() + "." + localSnmpOid.toString();
  }

  private String validDisplayStringTC(String paramString)
  {
    return JVM_MANAGEMENT_MIB_IMPL.validDisplayStringTC(paramString);
  }

  private String validJavaObjectNameTC(String paramString)
  {
    return JVM_MANAGEMENT_MIB_IMPL.validJavaObjectNameTC(paramString);
  }

  private String validPathElementTC(String paramString)
  {
    return JVM_MANAGEMENT_MIB_IMPL.validPathElementTC(paramString);
  }

  public String getJvmThreadInstLockName()
    throws SnmpStatusException
  {
    return validJavaObjectNameTC(this.info.getLockName());
  }

  public String getJvmThreadInstName()
    throws SnmpStatusException
  {
    return validJavaObjectNameTC(this.info.getThreadName());
  }

  public Long getJvmThreadInstCpuTimeNs()
    throws SnmpStatusException
  {
    long l = 3412047153814568960L;
    ThreadMXBean localThreadMXBean = JvmThreadingImpl.getThreadMXBean();
    try
    {
      if (localThreadMXBean.isThreadCpuTimeSupported())
      {
        l = localThreadMXBean.getThreadCpuTime(this.info.getThreadId());
        log.debug("getJvmThreadInstCpuTimeNs", "Cpu time ns : " + l);
        if (l == -1L)
          l = 3412048459484626944L;
      }
    }
    catch (UnsatisfiedLinkError localUnsatisfiedLinkError)
    {
      log.debug("getJvmThreadInstCpuTimeNs", "Operation not supported: " + localUnsatisfiedLinkError);
    }
    return new Long(l);
  }

  public Long getJvmThreadInstBlockTimeMs()
    throws SnmpStatusException
  {
    long l = 3412047153814568960L;
    ThreadMXBean localThreadMXBean = JvmThreadingImpl.getThreadMXBean();
    if (localThreadMXBean.isThreadContentionMonitoringSupported())
    {
      l = this.info.getBlockedTime();
      if (l == -1L)
        l = 3412047892548943872L;
    }
    return new Long(l);
  }

  public Long getJvmThreadInstBlockCount()
    throws SnmpStatusException
  {
    return new Long(this.info.getBlockedCount());
  }

  public Long getJvmThreadInstWaitTimeMs()
    throws SnmpStatusException
  {
    long l = 3412047153814568960L;
    ThreadMXBean localThreadMXBean = JvmThreadingImpl.getThreadMXBean();
    if (localThreadMXBean.isThreadContentionMonitoringSupported())
    {
      l = this.info.getWaitedTime();
      if (l == -1L)
        l = 3412047892548943872L;
    }
    return new Long(l);
  }

  public Long getJvmThreadInstWaitCount()
    throws SnmpStatusException
  {
    return new Long(this.info.getWaitedCount());
  }

  public Byte[] getJvmThreadInstState()
    throws SnmpStatusException
  {
    return ThreadStateMap.getState(this.info);
  }

  public Long getJvmThreadInstId()
    throws SnmpStatusException
  {
    return new Long(this.info.getThreadId());
  }

  public Byte[] getJvmThreadInstIndex()
    throws SnmpStatusException
  {
    return this.index;
  }

  private String getJvmThreadInstStackTrace()
    throws SnmpStatusException
  {
    StackTraceElement[] arrayOfStackTraceElement = this.info.getStackTrace();
    StringBuffer localStringBuffer = new StringBuffer();
    int i = arrayOfStackTraceElement.length;
    log.debug("getJvmThreadInstStackTrace", "Stack size : " + i);
    for (int j = 0; j < i; ++j)
    {
      log.debug("getJvmThreadInstStackTrace", "Append " + arrayOfStackTraceElement[j].toString());
      localStringBuffer.append(arrayOfStackTraceElement[j].toString());
      if (j < i)
        localStringBuffer.append("\n");
    }
    return validPathElementTC(localStringBuffer.toString());
  }

  public static final class ThreadStateMap
  {
    public static final byte mask0 = 63;
    public static final byte mask1 = -128;

    private static void setBit(byte[] paramArrayOfByte, int paramInt, byte paramByte)
    {
      paramArrayOfByte[paramInt] = (byte)(paramArrayOfByte[paramInt] | paramByte);
    }

    public static void setNative(byte[] paramArrayOfByte)
    {
      setBit(paramArrayOfByte, 0, -128);
    }

    public static void setSuspended(byte[] paramArrayOfByte)
    {
      setBit(paramArrayOfByte, 0, 64);
    }

    public static void setState(byte[] paramArrayOfByte, Thread.State paramState)
    {
      switch (JvmThreadInstanceEntryImpl.1.$SwitchMap$java$lang$Thread$State[paramState.ordinal()])
      {
      case 1:
        setBit(paramArrayOfByte, 0, 8);
        return;
      case 2:
        setBit(paramArrayOfByte, 0, 32);
        return;
      case 3:
        setBit(paramArrayOfByte, 0, 16);
        return;
      case 4:
        setBit(paramArrayOfByte, 0, 4);
        return;
      case 5:
        setBit(paramArrayOfByte, 0, 1);
        return;
      case 6:
        setBit(paramArrayOfByte, 0, 2);
        return;
      }
    }

    public static void checkOther(byte[] paramArrayOfByte)
    {
      if (((paramArrayOfByte[0] & 0x3F) == 0) && ((paramArrayOfByte[1] & 0xFFFFFF80) == 0))
        setBit(paramArrayOfByte, 1, -128);
    }

    public static Byte[] getState(ThreadInfo paramThreadInfo)
    {
      byte[] arrayOfByte = { 0, 0 };
      try
      {
        Thread.State localState = paramThreadInfo.getThreadState();
        boolean bool1 = paramThreadInfo.isInNative();
        boolean bool2 = paramThreadInfo.isSuspended();
        JvmThreadInstanceEntryImpl.log.debug("getJvmThreadInstState", "[State=" + localState + ",isInNative=" + bool1 + ",isSuspended=" + bool2 + "]");
        setState(arrayOfByte, localState);
        if (bool1)
          setNative(arrayOfByte);
        if (bool2)
          setSuspended(arrayOfByte);
        checkOther(arrayOfByte);
      }
      catch (RuntimeException localRuntimeException)
      {
        arrayOfByte[0] = 0;
        arrayOfByte[1] = -128;
        JvmThreadInstanceEntryImpl.log.trace("getJvmThreadInstState", "Unexpected exception: " + localRuntimeException);
        JvmThreadInstanceEntryImpl.log.debug("getJvmThreadInstState", localRuntimeException);
      }
      Byte[] arrayOfByte1 = { new Byte(arrayOfByte[0]), new Byte(arrayOfByte[1]) };
      return arrayOfByte1;
    }

    public static final class Byte0
    {
      public static final byte inNative = -128;
      public static final byte suspended = 64;
      public static final byte newThread = 32;
      public static final byte runnable = 16;
      public static final byte blocked = 8;
      public static final byte terminated = 4;
      public static final byte waiting = 2;
      public static final byte timedWaiting = 1;
    }

    public static final class Byte1
    {
      public static final byte other = -128;
      public static final byte reserved10 = 64;
      public static final byte reserved11 = 32;
      public static final byte reserved12 = 16;
      public static final byte reserved13 = 8;
      public static final byte reserved14 = 4;
      public static final byte reserved15 = 2;
      public static final byte reserved16 = 1;
    }
  }
}