package sun.management.snmp.jvminstr;

import com.sun.jmx.snmp.SnmpCounter64;
import com.sun.jmx.snmp.SnmpOid;
import com.sun.jmx.snmp.SnmpOidRecord;
import com.sun.jmx.snmp.SnmpOidTable;
import com.sun.jmx.snmp.SnmpParameters;
import com.sun.jmx.snmp.SnmpPeer;
import com.sun.jmx.snmp.SnmpString;
import com.sun.jmx.snmp.SnmpVarBind;
import com.sun.jmx.snmp.SnmpVarBindList;
import com.sun.jmx.snmp.agent.SnmpMibTable;
import com.sun.jmx.snmp.daemon.SnmpAdaptorServer;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryNotificationInfo;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanServer;
import javax.management.Notification;
import javax.management.NotificationEmitter;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import sun.management.snmp.jvmmib.JVM_MANAGEMENT_MIB;
import sun.management.snmp.jvmmib.JVM_MANAGEMENT_MIBOidTable;
import sun.management.snmp.jvmmib.JvmCompilationMeta;
import sun.management.snmp.jvmmib.JvmMemoryMeta;
import sun.management.snmp.jvmmib.JvmRuntimeMeta;
import sun.management.snmp.jvmmib.JvmThreadingMeta;
import sun.management.snmp.util.MibLogger;
import sun.management.snmp.util.SnmpCachedData;
import sun.management.snmp.util.SnmpTableHandler;

public class JVM_MANAGEMENT_MIB_IMPL extends JVM_MANAGEMENT_MIB
{
  private static final MibLogger log = new MibLogger(JVM_MANAGEMENT_MIB_IMPL.class);
  private static WeakReference tableRef;
  private ArrayList notificationTargets = new ArrayList();
  private final NotificationEmitter emitter = (NotificationEmitter)ManagementFactory.getMemoryMXBean();
  private final NotificationHandler handler = new NotificationHandler(???, null);
  private static final int DISPLAY_STRING_MAX_LENGTH = 255;
  private static final int JAVA_OBJECT_NAME_MAX_LENGTH = 1023;
  private static final int PATH_ELEMENT_MAX_LENGTH = 1023;
  private static final int ARG_VALUE_MAX_LENGTH = 1023;
  private static final int DEFAULT_CACHE_VALIDITY_PERIOD = 1000;

  public static SnmpOidTable getOidTable()
  {
    Object localObject = null;
    if (tableRef == null)
    {
      localObject = new JVM_MANAGEMENT_MIBOidTable();
      tableRef = new WeakReference(localObject);
      return localObject;
    }
    localObject = (SnmpOidTable)tableRef.get();
    if (localObject == null)
    {
      localObject = new JVM_MANAGEMENT_MIBOidTable();
      tableRef = new WeakReference(localObject);
    }
    return ((SnmpOidTable)localObject);
  }

  public JVM_MANAGEMENT_MIB_IMPL()
  {
    this.emitter.addNotificationListener(this.handler, null, null);
  }

  private synchronized void sendTrap(SnmpOid paramSnmpOid, SnmpVarBindList paramSnmpVarBindList)
  {
    Iterator localIterator = this.notificationTargets.iterator();
    SnmpAdaptorServer localSnmpAdaptorServer = (SnmpAdaptorServer)getSnmpAdaptor();
    if (localSnmpAdaptorServer == null)
    {
      log.error("sendTrap", "Cannot send trap: adaptor is null.");
      return;
    }
    if (!(localSnmpAdaptorServer.isActive()))
    {
      log.config("sendTrap", "Adaptor is not active: trap not sent.");
      return;
    }
    while (localIterator.hasNext())
    {
      NotificationTarget localNotificationTarget = null;
      try
      {
        localNotificationTarget = (NotificationTarget)localIterator.next();
        SnmpPeer localSnmpPeer = new SnmpPeer(localNotificationTarget.getAddress(), localNotificationTarget.getPort());
        SnmpParameters localSnmpParameters = new SnmpParameters();
        localSnmpParameters.setRdCommunity(localNotificationTarget.getCommunity());
        localSnmpPeer.setParams(localSnmpParameters);
        log.debug("handleNotification", "Sending trap to " + localNotificationTarget.getAddress() + ":" + localNotificationTarget.getPort());
        localSnmpAdaptorServer.snmpV2Trap(localSnmpPeer, paramSnmpOid, paramSnmpVarBindList, null);
      }
      catch (Exception localException)
      {
        log.error("sendTrap", "Exception occured while sending trap to [" + localNotificationTarget + "]. Exception : " + localException);
        log.debug("sendTrap", localException);
      }
    }
  }

  public synchronized void addTarget(NotificationTarget paramNotificationTarget)
    throws IllegalArgumentException
  {
    if (paramNotificationTarget == null)
      throw new IllegalArgumentException("Target is null");
    this.notificationTargets.add(paramNotificationTarget);
  }

  public void terminate()
  {
    try
    {
      this.emitter.removeNotificationListener(this.handler);
    }
    catch (ListenerNotFoundException localListenerNotFoundException)
    {
      log.error("terminate", "Listener Not found : " + localListenerNotFoundException);
    }
  }

  public synchronized void addTargets(List paramList)
    throws IllegalArgumentException
  {
    if (paramList == null)
      throw new IllegalArgumentException("Target list is null");
    this.notificationTargets.addAll(paramList);
  }

  protected Object createJvmMemoryMBean(String paramString1, String paramString2, ObjectName paramObjectName, MBeanServer paramMBeanServer)
  {
    if (paramMBeanServer != null)
      return new JvmMemoryImpl(this, paramMBeanServer);
    return new JvmMemoryImpl(this);
  }

  protected JvmMemoryMeta createJvmMemoryMetaNode(String paramString1, String paramString2, ObjectName paramObjectName, MBeanServer paramMBeanServer)
  {
    return new JvmMemoryMetaImpl(this, this.objectserver);
  }

  protected JvmThreadingMeta createJvmThreadingMetaNode(String paramString1, String paramString2, ObjectName paramObjectName, MBeanServer paramMBeanServer)
  {
    return new JvmThreadingMetaImpl(this, this.objectserver);
  }

  protected Object createJvmThreadingMBean(String paramString1, String paramString2, ObjectName paramObjectName, MBeanServer paramMBeanServer)
  {
    if (paramMBeanServer != null)
      return new JvmThreadingImpl(this, paramMBeanServer);
    return new JvmThreadingImpl(this);
  }

  protected JvmRuntimeMeta createJvmRuntimeMetaNode(String paramString1, String paramString2, ObjectName paramObjectName, MBeanServer paramMBeanServer)
  {
    return new JvmRuntimeMetaImpl(this, this.objectserver);
  }

  protected Object createJvmRuntimeMBean(String paramString1, String paramString2, ObjectName paramObjectName, MBeanServer paramMBeanServer)
  {
    if (paramMBeanServer != null)
      return new JvmRuntimeImpl(this, paramMBeanServer);
    return new JvmRuntimeImpl(this);
  }

  protected JvmCompilationMeta createJvmCompilationMetaNode(String paramString1, String paramString2, ObjectName paramObjectName, MBeanServer paramMBeanServer)
  {
    if (ManagementFactory.getCompilationMXBean() == null)
      return null;
    return super.createJvmCompilationMetaNode(paramString1, paramString2, paramObjectName, paramMBeanServer);
  }

  protected Object createJvmCompilationMBean(String paramString1, String paramString2, ObjectName paramObjectName, MBeanServer paramMBeanServer)
  {
    if (paramMBeanServer != null)
      return new JvmCompilationImpl(this, paramMBeanServer);
    return new JvmCompilationImpl(this);
  }

  protected Object createJvmOSMBean(String paramString1, String paramString2, ObjectName paramObjectName, MBeanServer paramMBeanServer)
  {
    if (paramMBeanServer != null)
      return new JvmOSImpl(this, paramMBeanServer);
    return new JvmOSImpl(this);
  }

  protected Object createJvmClassLoadingMBean(String paramString1, String paramString2, ObjectName paramObjectName, MBeanServer paramMBeanServer)
  {
    if (paramMBeanServer != null)
      return new JvmClassLoadingImpl(this, paramMBeanServer);
    return new JvmClassLoadingImpl(this);
  }

  static String validDisplayStringTC(String paramString)
  {
    if (paramString == null)
      return "";
    if (paramString.length() > 255)
      return paramString.substring(0, 255);
    return paramString;
  }

  static String validJavaObjectNameTC(String paramString)
  {
    if (paramString == null)
      return "";
    if (paramString.length() > 1023)
      return paramString.substring(0, 1023);
    return paramString;
  }

  static String validPathElementTC(String paramString)
  {
    if (paramString == null)
      return "";
    if (paramString.length() > 1023)
      return paramString.substring(0, 1023);
    return paramString;
  }

  static String validArgValueTC(String paramString)
  {
    if (paramString == null)
      return "";
    if (paramString.length() > 1023)
      return paramString.substring(0, 1023);
    return paramString;
  }

  private SnmpTableHandler getJvmMemPoolTableHandler(Object paramObject)
  {
    SnmpMibTable localSnmpMibTable = getRegisteredTableMeta("JvmMemPoolTable");
    if (!(localSnmpMibTable instanceof JvmMemPoolTableMetaImpl))
    {
      localObject = "Bad metadata class for JvmMemPoolTable: " + localSnmpMibTable.getClass().getName();
      log.error("getJvmMemPoolTableHandler", (String)localObject);
      return null;
    }
    Object localObject = (JvmMemPoolTableMetaImpl)localSnmpMibTable;
    return ((SnmpTableHandler)((JvmMemPoolTableMetaImpl)localObject).getHandler(paramObject));
  }

  private int findInCache(SnmpTableHandler paramSnmpTableHandler, String paramString)
  {
    if (!(paramSnmpTableHandler instanceof SnmpCachedData))
    {
      if (paramSnmpTableHandler != null)
      {
        localObject = "Bad class for JvmMemPoolTable datas: " + paramSnmpTableHandler.getClass().getName();
        log.error("getJvmMemPoolEntry", (String)localObject);
      }
      return -1;
    }
    Object localObject = (SnmpCachedData)paramSnmpTableHandler;
    int i = ((SnmpCachedData)localObject).datas.length;
    for (int j = 0; j < ((SnmpCachedData)localObject).datas.length; ++j)
    {
      MemoryPoolMXBean localMemoryPoolMXBean = (MemoryPoolMXBean)localObject.datas[j];
      if (paramString.equals(localMemoryPoolMXBean.getName()))
        return j;
    }
    return -1;
  }

  private SnmpOid getJvmMemPoolEntryIndex(SnmpTableHandler paramSnmpTableHandler, String paramString)
  {
    int i = findInCache(paramSnmpTableHandler, paramString);
    if (i < 0)
      return null;
    return ((SnmpCachedData)paramSnmpTableHandler).indexes[i];
  }

  private SnmpOid getJvmMemPoolEntryIndex(String paramString)
  {
    return getJvmMemPoolEntryIndex(getJvmMemPoolTableHandler(null), paramString);
  }

  public long validity()
  {
    return 1000L;
  }

  private class NotificationHandler
  implements NotificationListener
  {
    public void handleNotification(, Object paramObject)
    {
      JVM_MANAGEMENT_MIB_IMPL.access$000().debug("handleNotification", "Received notification [ " + paramNotification.getType() + "]");
      String str = paramNotification.getType();
      if ((str.equals("java.management.memory.threshold.exceeded")) || (str.equals("java.management.memory.collection.threshold.exceeded")))
      {
        MemoryNotificationInfo localMemoryNotificationInfo = MemoryNotificationInfo.from((CompositeData)paramNotification.getUserData());
        SnmpCounter64 localSnmpCounter641 = new SnmpCounter64(localMemoryNotificationInfo.getCount());
        SnmpCounter64 localSnmpCounter642 = new SnmpCounter64(localMemoryNotificationInfo.getUsage().getUsed());
        SnmpString localSnmpString = new SnmpString(localMemoryNotificationInfo.getPoolName());
        SnmpOid localSnmpOid1 = JVM_MANAGEMENT_MIB_IMPL.access$100(this.this$0, localMemoryNotificationInfo.getPoolName());
        if (localSnmpOid1 == null)
        {
          JVM_MANAGEMENT_MIB_IMPL.access$000().error("handleNotification", "Error: Can't find entry index for Memory Pool: " + localMemoryNotificationInfo.getPoolName() + ": " + "No trap emitted for " + str);
          return;
        }
        SnmpOid localSnmpOid2 = null;
        SnmpOidTable localSnmpOidTable = JVM_MANAGEMENT_MIB_IMPL.getOidTable();
        try
        {
          SnmpOid localSnmpOid3 = null;
          SnmpOid localSnmpOid4 = null;
          if (str.equals("java.management.memory.threshold.exceeded"))
          {
            localSnmpOid2 = new SnmpOid(localSnmpOidTable.resolveVarName("jvmLowMemoryPoolUsageNotif").getOid());
            localSnmpOid3 = new SnmpOid(localSnmpOidTable.resolveVarName("jvmMemPoolUsed").getOid() + "." + localSnmpOid1);
            localSnmpOid4 = new SnmpOid(localSnmpOidTable.resolveVarName("jvmMemPoolThreshdCount").getOid() + "." + localSnmpOid1);
          }
          else if (str.equals("java.management.memory.collection.threshold.exceeded"))
          {
            localSnmpOid2 = new SnmpOid(localSnmpOidTable.resolveVarName("jvmLowMemoryPoolCollectNotif").getOid());
            localSnmpOid3 = new SnmpOid(localSnmpOidTable.resolveVarName("jvmMemPoolCollectUsed").getOid() + "." + localSnmpOid1);
            localSnmpOid4 = new SnmpOid(localSnmpOidTable.resolveVarName("jvmMemPoolCollectThreshdCount").getOid() + "." + localSnmpOid1);
          }
          SnmpVarBindList localSnmpVarBindList = new SnmpVarBindList();
          SnmpOid localSnmpOid5 = new SnmpOid(localSnmpOidTable.resolveVarName("jvmMemPoolName").getOid() + "." + localSnmpOid1);
          SnmpVarBind localSnmpVarBind1 = new SnmpVarBind(localSnmpOid4, localSnmpCounter641);
          SnmpVarBind localSnmpVarBind2 = new SnmpVarBind(localSnmpOid3, localSnmpCounter642);
          SnmpVarBind localSnmpVarBind3 = new SnmpVarBind(localSnmpOid5, localSnmpString);
          localSnmpVarBindList.add(localSnmpVarBind3);
          localSnmpVarBindList.add(localSnmpVarBind1);
          localSnmpVarBindList.add(localSnmpVarBind2);
          JVM_MANAGEMENT_MIB_IMPL.access$200(this.this$0, localSnmpOid2, localSnmpVarBindList);
        }
        catch (Exception localException)
        {
          JVM_MANAGEMENT_MIB_IMPL.access$000().error("handleNotification", "Exception occured : " + localException);
        }
      }
    }
  }
}