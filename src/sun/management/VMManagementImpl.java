package sun.management;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import sun.management.counter.perf.PerfInstrumentation;
import sun.misc.Perf;
import sun.misc.Perf.GetPerfAction;
import sun.security.action.GetPropertyAction;

class VMManagementImpl
  implements VMManagement
{
  private static String version = getVersion0();
  private static boolean compTimeMonitoringSupport;
  private static boolean threadContentionMonitoringSupport;
  private static boolean currentThreadCpuTimeSupport;
  private static boolean otherThreadCpuTimeSupport;
  private static boolean bootClassPathSupport;
  private static boolean objectMonitorUsageSupport;
  private static boolean synchronizerUsageSupport;
  private List<String> vmArgs = null;
  private List<String> unmodifiableVmArgsList = null;
  private PerfInstrumentation perfInstr = null;
  private boolean noPerfData = false;

  private static native String getVersion0();

  private static native void initOptionalSupportFields();

  public boolean isCompilationTimeMonitoringSupported()
  {
    return compTimeMonitoringSupport;
  }

  public boolean isThreadContentionMonitoringSupported()
  {
    return threadContentionMonitoringSupport;
  }

  public boolean isCurrentThreadCpuTimeSupported()
  {
    return currentThreadCpuTimeSupport;
  }

  public boolean isOtherThreadCpuTimeSupported()
  {
    return otherThreadCpuTimeSupport;
  }

  public boolean isBootClassPathSupported()
  {
    return bootClassPathSupport;
  }

  public boolean isObjectMonitorUsageSupported()
  {
    return objectMonitorUsageSupport;
  }

  public boolean isSynchronizerUsageSupported()
  {
    return synchronizerUsageSupport;
  }

  public native boolean isThreadContentionMonitoringEnabled();

  public native boolean isThreadCpuTimeEnabled();

  public int getLoadedClassCount()
  {
    long l = getTotalClassCount() - getUnloadedClassCount();
    return (int)l;
  }

  public native long getTotalClassCount();

  public native long getUnloadedClassCount();

  public native boolean getVerboseClass();

  public native boolean getVerboseGC();

  public String getManagementVersion()
  {
    return version;
  }

  public String getVmId()
  {
    int i = getProcessId();
    String str = "localhost";
    try
    {
      str = InetAddress.getLocalHost().getHostName();
    }
    catch (UnknownHostException localUnknownHostException)
    {
    }
    return i + "@" + str;
  }

  private native int getProcessId();

  public String getVmName()
  {
    return System.getProperty("java.vm.name");
  }

  public String getVmVendor()
  {
    return System.getProperty("java.vm.vendor");
  }

  public String getVmVersion()
  {
    return System.getProperty("java.vm.version");
  }

  public String getVmSpecName()
  {
    return System.getProperty("java.vm.specification.name");
  }

  public String getVmSpecVendor()
  {
    return System.getProperty("java.vm.specification.vendor");
  }

  public String getVmSpecVersion()
  {
    return System.getProperty("java.vm.specification.version");
  }

  public String getClassPath()
  {
    return System.getProperty("java.class.path");
  }

  public String getLibraryPath()
  {
    return System.getProperty("java.library.path");
  }

  public String getBootClassPath()
  {
    GetPropertyAction localGetPropertyAction = new GetPropertyAction("sun.boot.class.path");
    String str = (String)AccessController.doPrivileged(localGetPropertyAction);
    return str;
  }

  public synchronized List<String> getVmArguments()
  {
    if (this.vmArgs == null)
    {
      this.vmArgs = new ArrayList();
      String str = getVmArguments0();
      if (str != null)
      {
        StringTokenizer localStringTokenizer = new StringTokenizer(str, " ");
        while (localStringTokenizer.hasMoreTokens())
          this.vmArgs.add(localStringTokenizer.nextToken());
      }
      this.unmodifiableVmArgsList = Collections.unmodifiableList(this.vmArgs);
    }
    return this.unmodifiableVmArgsList;
  }

  public native String getVmArguments0();

  public native long getStartupTime();

  public native int getAvailableProcessors();

  public String getCompilerName()
  {
    String str = (String)AccessController.doPrivileged(new PrivilegedAction(this)
    {
      public Object run()
      {
        return System.getProperty("sun.management.compiler");
      }
    });
    return str;
  }

  public native long getTotalCompileTime();

  public native long getTotalThreadCount();

  public native int getLiveThreadCount();

  public native int getPeakThreadCount();

  public native int getDaemonThreadCount();

  public String getOsName()
  {
    return System.getProperty("os.name");
  }

  public String getOsArch()
  {
    return System.getProperty("os.arch");
  }

  public String getOsVersion()
  {
    return System.getProperty("os.version");
  }

  public native long getSafepointCount();

  public native long getTotalSafepointTime();

  public native long getSafepointSyncTime();

  public native long getTotalApplicationNonStoppedTime();

  public native long getLoadedClassSize();

  public native long getUnloadedClassSize();

  public native long getClassLoadingTime();

  public native long getMethodDataSize();

  public native long getInitializedClassCount();

  public native long getClassInitializationTime();

  public native long getClassVerificationTime();

  private synchronized PerfInstrumentation getPerfInstrumentation()
  {
    if ((this.noPerfData) || (this.perfInstr != null))
      return this.perfInstr;
    Perf localPerf = (Perf)AccessController.doPrivileged(new Perf.GetPerfAction());
    try
    {
      ByteBuffer localByteBuffer = localPerf.attach(0, "r");
      if (localByteBuffer.capacity() == 0)
      {
        this.noPerfData = true;
        return null;
      }
      this.perfInstr = new PerfInstrumentation(localByteBuffer);
    }
    catch (IllegalArgumentException localIllegalArgumentException)
    {
      this.noPerfData = true;
    }
    catch (IOException localIOException)
    {
      throw new InternalError(localIOException.getMessage());
    }
    return this.perfInstr;
  }

  public List getInternalCounters(String paramString)
  {
    PerfInstrumentation localPerfInstrumentation = getPerfInstrumentation();
    if (localPerfInstrumentation != null)
      return localPerfInstrumentation.findByPattern(paramString);
    return Collections.EMPTY_LIST;
  }

  static
  {
    if (version == null)
      throw new InternalError("Invalid Management Version");
    initOptionalSupportFields();
  }
}