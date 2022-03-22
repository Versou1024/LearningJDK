package sun.management;

import com.sun.management.HotSpotDiagnosticMXBean;
import com.sun.management.OSMBeanFactory;
import java.lang.management.ClassLoadingMXBean;
import java.lang.management.CompilationMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementPermission;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryManagerMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.security.AccessController;
import java.security.Permission;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.LogManager;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.NotificationEmitter;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.RuntimeOperationsException;
import javax.management.StandardEmitterMBean;
import javax.management.StandardMBean;
import sun.misc.VM;
import sun.security.action.LoadLibraryAction;

public class ManagementFactory
{
  private static VMManagement jvm;
  private static boolean mbeansCreated = false;
  private static ClassLoadingImpl classMBean = null;
  private static MemoryImpl memoryMBean = null;
  private static ThreadImpl threadMBean = null;
  private static RuntimeImpl runtimeMBean = null;
  private static CompilationImpl compileMBean = null;
  private static OperatingSystemImpl osMBean = null;
  private static HotSpotDiagnostic hsDiagMBean = null;
  private static HotspotRuntime hsRuntimeMBean = null;
  private static HotspotClassLoading hsClassMBean = null;
  private static HotspotThread hsThreadMBean = null;
  private static HotspotCompilation hsCompileMBean = null;
  private static HotspotMemory hsMemoryMBean = null;
  private static Permission monitorPermission = new ManagementPermission("monitor");
  private static Permission controlPermission = new ManagementPermission("control");
  private static final String HOTSPOT_DIAGNOSTIC_MXBEAN_NAME = "com.sun.management:type=HotSpotDiagnostic";
  private static final String HOTSPOT_CLASS_LOADING_MBEAN_NAME = "sun.management:type=HotspotClassLoading";
  private static final String HOTSPOT_COMPILATION_MBEAN_NAME = "sun.management:type=HotspotCompilation";
  private static final String HOTSPOT_MEMORY_MBEAN_NAME = "sun.management:type=HotspotMemory";
  private static final String HOTSPOT_RUNTIME_MBEAN_NAME = "sun.management:type=HotspotRuntime";
  private static final String HOTSPOT_THREAD_MBEAN_NAME = "sun.management:type=HotspotThreading";
  private static final String HOTSPOT_INTERNAL_MBEAN_NAME = "sun.management:type=HotspotInternal";
  private static ObjectName hsInternalObjName = null;
  private static final int JMM_THREAD_STATE_FLAG_MASK = -1048576;
  private static final int JMM_THREAD_STATE_FLAG_SUSPENDED = 1048576;
  private static final int JMM_THREAD_STATE_FLAG_NATIVE = 4194304;

  public static synchronized ClassLoadingMXBean getClassLoadingMXBean()
  {
    if (classMBean == null)
      classMBean = new ClassLoadingImpl(jvm);
    return classMBean;
  }

  public static synchronized MemoryMXBean getMemoryMXBean()
  {
    if (memoryMBean == null)
      memoryMBean = new MemoryImpl(jvm);
    return memoryMBean;
  }

  public static synchronized ThreadMXBean getThreadMXBean()
  {
    if (threadMBean == null)
      threadMBean = new ThreadImpl(jvm);
    return threadMBean;
  }

  public static synchronized RuntimeMXBean getRuntimeMXBean()
  {
    if (runtimeMBean == null)
      runtimeMBean = new RuntimeImpl(jvm);
    return runtimeMBean;
  }

  public static synchronized CompilationMXBean getCompilationMXBean()
  {
    if ((compileMBean == null) && (jvm.getCompilerName() != null))
      compileMBean = new CompilationImpl(jvm);
    return compileMBean;
  }

  public static synchronized OperatingSystemMXBean getOperatingSystemMXBean()
  {
    if (osMBean == null)
      osMBean = (OperatingSystemImpl)OSMBeanFactory.getOperatingSystemMXBean(jvm);
    return osMBean;
  }

  public static List<MemoryPoolMXBean> getMemoryPoolMXBeans()
  {
    MemoryPoolMXBean[] arrayOfMemoryPoolMXBean = MemoryImpl.getMemoryPools();
    ArrayList localArrayList = new ArrayList(arrayOfMemoryPoolMXBean.length);
    for (int i = 0; i < arrayOfMemoryPoolMXBean.length; ++i)
    {
      MemoryPoolMXBean localMemoryPoolMXBean = arrayOfMemoryPoolMXBean[i];
      localArrayList.add(localMemoryPoolMXBean);
    }
    return localArrayList;
  }

  public static List<MemoryManagerMXBean> getMemoryManagerMXBeans()
  {
    MemoryManagerMXBean[] arrayOfMemoryManagerMXBean = MemoryImpl.getMemoryManagers();
    ArrayList localArrayList = new ArrayList(arrayOfMemoryManagerMXBean.length);
    for (int i = 0; i < arrayOfMemoryManagerMXBean.length; ++i)
    {
      MemoryManagerMXBean localMemoryManagerMXBean = arrayOfMemoryManagerMXBean[i];
      localArrayList.add(localMemoryManagerMXBean);
    }
    return localArrayList;
  }

  public static List<GarbageCollectorMXBean> getGarbageCollectorMXBeans()
  {
    MemoryManagerMXBean[] arrayOfMemoryManagerMXBean = MemoryImpl.getMemoryManagers();
    ArrayList localArrayList = new ArrayList(arrayOfMemoryManagerMXBean.length);
    for (int i = 0; i < arrayOfMemoryManagerMXBean.length; ++i)
      if (arrayOfMemoryManagerMXBean[i] instanceof GarbageCollectorMXBean)
      {
        GarbageCollectorMXBean localGarbageCollectorMXBean = (GarbageCollectorMXBean)arrayOfMemoryManagerMXBean[i];
        localArrayList.add(localGarbageCollectorMXBean);
      }
    return localArrayList;
  }

  public static synchronized HotSpotDiagnosticMXBean getDiagnosticMXBean()
  {
    if (hsDiagMBean == null)
      hsDiagMBean = new HotSpotDiagnostic();
    return hsDiagMBean;
  }

  public static synchronized HotspotRuntimeMBean getHotspotRuntimeMBean()
  {
    if (hsRuntimeMBean == null)
      hsRuntimeMBean = new HotspotRuntime(jvm);
    return hsRuntimeMBean;
  }

  public static synchronized HotspotClassLoadingMBean getHotspotClassLoadingMBean()
  {
    if (hsClassMBean == null)
      hsClassMBean = new HotspotClassLoading(jvm);
    return hsClassMBean;
  }

  public static synchronized HotspotThreadMBean getHotspotThreadMBean()
  {
    if (hsThreadMBean == null)
      hsThreadMBean = new HotspotThread(jvm);
    return hsThreadMBean;
  }

  public static synchronized HotspotMemoryMBean getHotspotMemoryMBean()
  {
    if (hsMemoryMBean == null)
      hsMemoryMBean = new HotspotMemory(jvm);
    return hsMemoryMBean;
  }

  public static synchronized HotspotCompilationMBean getHotspotCompilationMBean()
  {
    if (hsCompileMBean == null)
      hsCompileMBean = new HotspotCompilation(jvm);
    return hsCompileMBean;
  }

  static void checkAccess(Permission paramPermission)
    throws SecurityException
  {
    SecurityManager localSecurityManager = System.getSecurityManager();
    if (localSecurityManager != null)
      localSecurityManager.checkPermission(paramPermission);
  }

  static void checkMonitorAccess()
    throws SecurityException
  {
    checkAccess(monitorPermission);
  }

  static void checkControlAccess()
    throws SecurityException
  {
    checkAccess(controlPermission);
  }

  private static void addMXBean(MBeanServer paramMBeanServer, Object paramObject, String paramString, NotificationEmitter paramNotificationEmitter)
  {
    Object localObject;
    if (paramNotificationEmitter == null)
      localObject = new StandardMBean(paramObject, null, true);
    else
      localObject = new StandardEmitterMBean(paramObject, null, true, paramNotificationEmitter);
    addMBean(paramMBeanServer, localObject, paramString, false);
  }

  private static void addMBean(MBeanServer paramMBeanServer, Object paramObject, String paramString)
  {
    addMBean(paramMBeanServer, paramObject, paramString, false);
  }

  private static void addMBean(MBeanServer paramMBeanServer, Object paramObject, String paramString, boolean paramBoolean)
  {
    ObjectName localObjectName;
    try
    {
      localObjectName = new ObjectName(paramString);
      MBeanServer localMBeanServer = paramMBeanServer;
      Object localObject = paramObject;
      boolean bool = paramBoolean;
      AccessController.doPrivileged(new PrivilegedExceptionAction(localMBeanServer, localObject, localObjectName, bool)
      {
        public Object run()
          throws InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException
        {
          ObjectInstance localObjectInstance;
          try
          {
            localObjectInstance = this.val$mbs0.registerMBean(this.val$mbean0, this.val$objName);
            return null;
          }
          catch (InstanceAlreadyExistsException localInstanceAlreadyExistsException)
          {
            if (!(this.val$ignore))
              throw localInstanceAlreadyExistsException;
          }
          return null;
        }
      });
    }
    catch (PrivilegedActionException localPrivilegedActionException)
    {
      throw Util.newException(localPrivilegedActionException.getException());
    }
    catch (MalformedObjectNameException localMalformedObjectNameException)
    {
      throw Util.newException(localMalformedObjectNameException);
    }
  }

  public static MBeanServer createPlatformMBeanServer()
  {
    MBeanServer localMBeanServer = MBeanServerFactory.createMBeanServer();
    addMXBean(localMBeanServer, getClassLoadingMXBean(), "java.lang:type=ClassLoading", null);
    addMXBean(localMBeanServer, getMemoryMXBean(), "java.lang:type=Memory", (NotificationEmitter)getMemoryMXBean());
    addMXBean(localMBeanServer, getOperatingSystemMXBean(), "java.lang:type=OperatingSystem", null);
    addMXBean(localMBeanServer, getRuntimeMXBean(), "java.lang:type=Runtime", null);
    addMXBean(localMBeanServer, getThreadMXBean(), "java.lang:type=Threading", null);
    addMXBean(localMBeanServer, getDiagnosticMXBean(), "com.sun.management:type=HotSpotDiagnostic", null);
    if (getCompilationMXBean() != null)
      addMXBean(localMBeanServer, getCompilationMXBean(), "java.lang:type=Compilation", null);
    addMemoryManagers(localMBeanServer);
    addMemoryPools(localMBeanServer);
    addMXBean(localMBeanServer, LogManager.getLoggingMXBean(), "java.util.logging:type=Logging", null);
    return localMBeanServer;
  }

  static synchronized ObjectName getHotspotInternalObjectName()
  {
    if (hsInternalObjName == null)
      try
      {
        hsInternalObjName = new ObjectName("sun.management:type=HotspotInternal");
      }
      catch (MalformedObjectNameException localMalformedObjectNameException)
      {
        throw Util.newException(localMalformedObjectNameException);
      }
    return hsInternalObjName;
  }

  static void registerInternalMBeans(MBeanServer paramMBeanServer)
  {
    addMBean(paramMBeanServer, getHotspotClassLoadingMBean(), "sun.management:type=HotspotClassLoading", true);
    addMBean(paramMBeanServer, getHotspotMemoryMBean(), "sun.management:type=HotspotMemory", true);
    addMBean(paramMBeanServer, getHotspotRuntimeMBean(), "sun.management:type=HotspotRuntime", true);
    addMBean(paramMBeanServer, getHotspotThreadMBean(), "sun.management:type=HotspotThreading", true);
    if (getCompilationMXBean() != null)
      addMBean(paramMBeanServer, getHotspotCompilationMBean(), "sun.management:type=HotspotCompilation", true);
  }

  private static void unregisterMBean(MBeanServer paramMBeanServer, String paramString)
  {
    ObjectName localObjectName;
    try
    {
      localObjectName = new ObjectName(paramString);
      MBeanServer localMBeanServer = paramMBeanServer;
      AccessController.doPrivileged(new PrivilegedExceptionAction(localMBeanServer, localObjectName)
      {
        public Object run()
          throws MBeanRegistrationException, RuntimeOperationsException
        {
          try
          {
            this.val$mbs0.unregisterMBean(this.val$objName);
          }
          catch (InstanceNotFoundException localInstanceNotFoundException)
          {
          }
          return null;
        }
      });
    }
    catch (PrivilegedActionException localPrivilegedActionException)
    {
      throw Util.newException(localPrivilegedActionException.getException());
    }
    catch (MalformedObjectNameException localMalformedObjectNameException)
    {
      throw Util.newException(localMalformedObjectNameException);
    }
  }

  static void unregisterInternalMBeans(MBeanServer paramMBeanServer)
  {
    unregisterMBean(paramMBeanServer, "sun.management:type=HotspotClassLoading");
    unregisterMBean(paramMBeanServer, "sun.management:type=HotspotMemory");
    unregisterMBean(paramMBeanServer, "sun.management:type=HotspotRuntime");
    unregisterMBean(paramMBeanServer, "sun.management:type=HotspotThreading");
    if (getCompilationMXBean() != null)
      unregisterMBean(paramMBeanServer, "sun.management:type=HotspotCompilation");
  }

  private static synchronized void addMemoryPools(MBeanServer paramMBeanServer)
  {
    MemoryPoolMXBean[] arrayOfMemoryPoolMXBean = MemoryImpl.getMemoryPools();
    for (int i = 0; i < arrayOfMemoryPoolMXBean.length; ++i)
    {
      String str = Util.getMBeanObjectName(arrayOfMemoryPoolMXBean[i]);
      addMXBean(paramMBeanServer, arrayOfMemoryPoolMXBean[i], str, null);
    }
  }

  private static synchronized void addMemoryManagers(MBeanServer paramMBeanServer)
  {
    MemoryManagerMXBean[] arrayOfMemoryManagerMXBean = MemoryImpl.getMemoryManagers();
    for (int i = 0; i < arrayOfMemoryManagerMXBean.length; ++i)
    {
      String str = Util.getMBeanObjectName(arrayOfMemoryManagerMXBean[i]);
      addMXBean(paramMBeanServer, arrayOfMemoryManagerMXBean[i], str, null);
    }
  }

  private static MemoryPoolMXBean createMemoryPool(String paramString, boolean paramBoolean, long paramLong1, long paramLong2)
  {
    return new MemoryPoolImpl(paramString, paramBoolean, paramLong1, paramLong2);
  }

  private static MemoryManagerMXBean createMemoryManager(String paramString)
  {
    return new MemoryManagerImpl(paramString);
  }

  private static GarbageCollectorMXBean createGarbageCollector(String paramString1, String paramString2)
  {
    return new GarbageCollectorImpl(paramString1);
  }

  public static boolean isThreadSuspended(int paramInt)
  {
    return ((paramInt & 0x100000) != 0);
  }

  public static boolean isThreadRunningNative(int paramInt)
  {
    return ((paramInt & 0x400000) != 0);
  }

  public static Thread.State toThreadState(int paramInt)
  {
    int i = paramInt & 0xFFFFF;
    return VM.toThreadState(i);
  }

  static
  {
    AccessController.doPrivileged(new LoadLibraryAction("management"));
    jvm = new VMManagementImpl();
  }
}