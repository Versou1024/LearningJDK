package sun.management;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.TreeMap;
import sun.management.counter.Counter;
import sun.management.counter.LongCounter;
import sun.management.counter.StringCounter;

class HotspotCompilation
  implements HotspotCompilationMBean
{
  private VMManagement jvm;
  private static final String JAVA_CI = "java.ci.";
  private static final String COM_SUN_CI = "com.sun.ci.";
  private static final String SUN_CI = "sun.ci.";
  private static final String CI_COUNTER_NAME_PATTERN = "java.ci.|com.sun.ci.|sun.ci.";
  private LongCounter compilerThreads;
  private LongCounter totalCompiles;
  private LongCounter totalBailouts;
  private LongCounter totalInvalidates;
  private LongCounter nmethodCodeSize;
  private LongCounter nmethodSize;
  private StringCounter lastMethod;
  private LongCounter lastSize;
  private LongCounter lastType;
  private StringCounter lastFailedMethod;
  private LongCounter lastFailedType;
  private StringCounter lastInvalidatedMethod;
  private LongCounter lastInvalidatedType;
  private CompilerThreadInfo[] threads;
  private int numActiveThreads;
  private Map counters;

  HotspotCompilation(VMManagement paramVMManagement)
  {
    this.jvm = paramVMManagement;
    initCompilerCounters();
  }

  private Counter lookup(String paramString)
  {
    Counter localCounter = null;
    if ((localCounter = (Counter)this.counters.get("sun.ci." + paramString)) != null)
      return localCounter;
    if ((localCounter = (Counter)this.counters.get("com.sun.ci." + paramString)) != null)
      return localCounter;
    if ((localCounter = (Counter)this.counters.get("java.ci." + paramString)) != null)
      return localCounter;
    throw new InternalError("Counter " + paramString + " does not exist");
  }

  private void initCompilerCounters()
  {
    ListIterator localListIterator = getInternalCompilerCounters().listIterator();
    this.counters = new TreeMap();
    while (localListIterator.hasNext())
    {
      Counter localCounter = (Counter)localListIterator.next();
      this.counters.put(localCounter.getName(), localCounter);
    }
    this.compilerThreads = ((LongCounter)lookup("threads"));
    this.totalCompiles = ((LongCounter)lookup("totalCompiles"));
    this.totalBailouts = ((LongCounter)lookup("totalBailouts"));
    this.totalInvalidates = ((LongCounter)lookup("totalInvalidates"));
    this.nmethodCodeSize = ((LongCounter)lookup("nmethodCodeSize"));
    this.nmethodSize = ((LongCounter)lookup("nmethodSize"));
    this.lastMethod = ((StringCounter)lookup("lastMethod"));
    this.lastSize = ((LongCounter)lookup("lastSize"));
    this.lastType = ((LongCounter)lookup("lastType"));
    this.lastFailedMethod = ((StringCounter)lookup("lastFailedMethod"));
    this.lastFailedType = ((LongCounter)lookup("lastFailedType"));
    this.lastInvalidatedMethod = ((StringCounter)lookup("lastInvalidatedMethod"));
    this.lastInvalidatedType = ((LongCounter)lookup("lastInvalidatedType"));
    this.numActiveThreads = (int)this.compilerThreads.longValue();
    this.threads = new CompilerThreadInfo[this.numActiveThreads + 1];
    if (this.counters.containsKey("sun.ci.adapterThread.compiles"))
    {
      this.threads[0] = new CompilerThreadInfo(this, "adapterThread", 0);
      this.numActiveThreads += 1;
    }
    else
    {
      this.threads[0] = null;
    }
    for (int i = 1; i < this.threads.length; ++i)
      this.threads[i] = new CompilerThreadInfo(this, "compilerThread", i - 1);
  }

  public int getCompilerThreadCount()
  {
    return this.numActiveThreads;
  }

  public long getTotalCompileCount()
  {
    return this.totalCompiles.longValue();
  }

  public long getBailoutCompileCount()
  {
    return this.totalBailouts.longValue();
  }

  public long getInvalidatedCompileCount()
  {
    return this.totalInvalidates.longValue();
  }

  public long getCompiledMethodCodeSize()
  {
    return this.nmethodCodeSize.longValue();
  }

  public long getCompiledMethodSize()
  {
    return this.nmethodSize.longValue();
  }

  public List getCompilerThreadStats()
  {
    ArrayList localArrayList = new ArrayList(this.threads.length);
    int i = 0;
    if (this.threads[0] == null);
    for (i = 1; i < this.threads.length; ++i)
      localArrayList.add(this.threads[i].getCompilerThreadStat());
    return localArrayList;
  }

  public MethodInfo getLastCompile()
  {
    return new MethodInfo(this.lastMethod.stringValue(), (int)this.lastType.longValue(), (int)this.lastSize.longValue());
  }

  public MethodInfo getFailedCompile()
  {
    return new MethodInfo(this.lastFailedMethod.stringValue(), (int)this.lastFailedType.longValue(), -1);
  }

  public MethodInfo getInvalidatedCompile()
  {
    return new MethodInfo(this.lastInvalidatedMethod.stringValue(), (int)this.lastInvalidatedType.longValue(), -1);
  }

  public List getInternalCompilerCounters()
  {
    return this.jvm.getInternalCounters("java.ci.|com.sun.ci.|sun.ci.");
  }

  private class CompilerThreadInfo
  {
    int index;
    String name;
    StringCounter method;
    LongCounter type;
    LongCounter compiles;
    LongCounter time;

    CompilerThreadInfo(, String paramString, int paramInt)
    {
      String str = paramString + "." + paramInt + ".";
      this.name = paramString + "-" + paramInt;
      this.method = ((StringCounter)HotspotCompilation.access$000(paramHotspotCompilation, str + "method"));
      this.type = ((LongCounter)HotspotCompilation.access$000(paramHotspotCompilation, str + "type"));
      this.compiles = ((LongCounter)HotspotCompilation.access$000(paramHotspotCompilation, str + "compiles"));
      this.time = ((LongCounter)HotspotCompilation.access$000(paramHotspotCompilation, str + "time"));
    }

    CompilerThreadInfo(, String paramString)
    {
      String str = paramString + ".";
      this.name = paramString;
      this.method = ((StringCounter)HotspotCompilation.access$000(paramHotspotCompilation, str + "method"));
      this.type = ((LongCounter)HotspotCompilation.access$000(paramHotspotCompilation, str + "type"));
      this.compiles = ((LongCounter)HotspotCompilation.access$000(paramHotspotCompilation, str + "compiles"));
      this.time = ((LongCounter)HotspotCompilation.access$000(paramHotspotCompilation, str + "time"));
    }

    CompilerThreadStat getCompilerThreadStat()
    {
      MethodInfo localMethodInfo = new MethodInfo(this.method.stringValue(), (int)this.type.longValue(), -1);
      return new CompilerThreadStat(this.name, this.compiles.longValue(), this.time.longValue(), localMethodInfo);
    }
  }
}