package sun.misc;

import [I;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class VM
{
  private static boolean suspended = false;

  @Deprecated
  public static final int STATE_GREEN = 1;

  @Deprecated
  public static final int STATE_YELLOW = 2;

  @Deprecated
  public static final int STATE_RED = 3;
  private static volatile boolean booted = false;
  private static long directMemory = 67108864L;
  private static boolean defaultAllowArraySyntax = false;
  private static boolean allowArraySyntax = defaultAllowArraySyntax;
  private static volatile int finalRefCount = 0;
  private static volatile int peakFinalRefCount = 0;
  private static Map<Integer, Thread.State> threadStateMap = null;
  private static Map<Integer, java.lang.String> threadStateNames = null;

  @Deprecated
  public static boolean threadsSuspended()
  {
    return suspended;
  }

  public static boolean allowThreadSuspension(ThreadGroup paramThreadGroup, boolean paramBoolean)
  {
    return paramThreadGroup.allowThreadSuspension(paramBoolean);
  }

  @Deprecated
  public static boolean suspendThreads()
  {
    suspended = true;
    return true;
  }

  @Deprecated
  public static void unsuspendThreads()
  {
    suspended = false;
  }

  @Deprecated
  public static void unsuspendSomeThreads()
  {
  }

  @Deprecated
  public static final int getState()
  {
    return 1;
  }

  @Deprecated
  public static void registerVMNotification(VMNotification paramVMNotification)
  {
  }

  @Deprecated
  public static void asChange(int paramInt1, int paramInt2)
  {
  }

  @Deprecated
  public static void asChange_otherthread(int paramInt1, int paramInt2)
  {
  }

  public static void booted()
  {
    booted = true;
  }

  public static boolean isBooted()
  {
    return booted;
  }

  public static long maxDirectMemory()
  {
    if (booted)
      return directMemory;
    Properties localProperties = System.getProperties();
    java.lang.String str = (java.lang.String)localProperties.remove("sun.nio.MaxDirectMemorySize");
    System.setProperties(localProperties);
    if (str != null)
      if (str.equals("-1"))
      {
        directMemory = Runtime.getRuntime().maxMemory();
      }
      else
      {
        long l = Long.parseLong(str);
        if (l > -1L)
          directMemory = l;
      }
    return directMemory;
  }

  public static boolean allowArraySyntax()
  {
    if (!(booted))
    {
      java.lang.String str = System.getProperty("sun.lang.ClassLoader.allowArraySyntax");
      allowArraySyntax = (str == null) ? defaultAllowArraySyntax : Boolean.parseBoolean(str);
    }
    return allowArraySyntax;
  }

  public static void initializeOSEnvironment()
  {
    if (!(booted))
      OSEnvironment.initialize();
  }

  public static int getFinalRefCount()
  {
    return finalRefCount;
  }

  public static int getPeakFinalRefCount()
  {
    return peakFinalRefCount;
  }

  public static void addFinalRefCount(int paramInt)
  {
    finalRefCount += paramInt;
    if (finalRefCount > peakFinalRefCount)
      peakFinalRefCount = finalRefCount;
  }

  public static Thread.State toThreadState(int paramInt)
  {
    initThreadStateMap();
    Thread.State localState = (Thread.State)threadStateMap.get(Integer.valueOf(paramInt));
    if (localState == null)
      localState = Thread.State.RUNNABLE;
    return localState;
  }

  private static synchronized void initThreadStateMap()
  {
    if (threadStateMap != null)
      return;
    Thread.State[] arrayOfState = Thread.State.values();
    [I[] arrayOf[I = new int[arrayOfState.length][];
    [Ljava.lang.String[] arrayOfString; = new java.lang.String[arrayOfState.length][];
    getThreadStateValues(arrayOf[I, arrayOfString;);
    threadStateMap = new HashMap();
    threadStateNames = new HashMap();
    for (int i = 0; i < arrayOfState.length; ++i)
    {
      java.lang.String str = arrayOfState[i].name();
      [I local[I = null;
      [Ljava.lang.String localString; = null;
      for (int j = 0; j < arrayOfState.length; ++j)
        if (arrayOfString;[j][0].startsWith(str))
        {
          local[I = arrayOf[I[j];
          localString; = arrayOfString;[j];
        }
      if (local[I == null)
        throw new InternalError("No VM thread state mapped to " + str);
      if (local[I.length != localString;.length)
        throw new InternalError("VM thread state values and names  mapped to " + str + ": length not matched");
      for (j = 0; j < local[I.length; ++j)
      {
        threadStateMap.put(Integer.valueOf(local[I[j]), arrayOfState[i]);
        threadStateNames.put(Integer.valueOf(local[I[j]), localString;[j]);
      }
    }
  }

  private static native void getThreadStateValues(int[][] paramArrayOfInt, java.lang.String[][] paramArrayOfString);

  private static native void initialize();

  static
  {
    initialize();
  }
}