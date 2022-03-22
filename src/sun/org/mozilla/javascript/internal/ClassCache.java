package sun.org.mozilla.javascript.internal;

import java.util.Hashtable;

public class ClassCache
{
  private static final Object AKEY = new Object();
  private volatile boolean cachingIsEnabled = true;
  Hashtable classTable = new Hashtable();
  Hashtable javaAdapterGeneratedClasses = new Hashtable();
  ScriptableObject scope;
  private Hashtable interfaceAdapterCache;
  private int generatedClassSerial;

  public static ClassCache get(Scriptable paramScriptable)
  {
    ClassCache localClassCache = (ClassCache)ScriptableObject.getTopScopeValue(paramScriptable, AKEY);
    if (localClassCache == null)
      localClassCache = new ClassCache();
    return localClassCache;
  }

  public boolean associate(ScriptableObject paramScriptableObject)
  {
    if (paramScriptableObject.getParentScope() != null)
      throw new IllegalArgumentException();
    if (this == paramScriptableObject.associateValue(AKEY, this))
    {
      this.scope = paramScriptableObject;
      return true;
    }
    return false;
  }

  public synchronized void clearCaches()
  {
    this.classTable = new Hashtable();
    this.javaAdapterGeneratedClasses = new Hashtable();
    this.interfaceAdapterCache = null;
  }

  public final boolean isCachingEnabled()
  {
    return this.cachingIsEnabled;
  }

  public synchronized void setCachingEnabled(boolean paramBoolean)
  {
    if (paramBoolean == this.cachingIsEnabled)
      return;
    if (!(paramBoolean))
      clearCaches();
    this.cachingIsEnabled = paramBoolean;
  }

  /**
   * @deprecated
   */
  public boolean isInvokerOptimizationEnabled()
  {
    return false;
  }

  /**
   * @deprecated
   */
  public synchronized void setInvokerOptimizationEnabled(boolean paramBoolean)
  {
  }

  public final synchronized int newClassSerialNumber()
  {
    return (++this.generatedClassSerial);
  }

  Object getInterfaceAdapter(Class paramClass)
  {
    Object localObject;
    Hashtable localHashtable = this.interfaceAdapterCache;
    if (localHashtable == null)
      localObject = null;
    else
      localObject = localHashtable.get(paramClass);
    return localObject;
  }

  synchronized void cacheInterfaceAdapter(Class paramClass, Object paramObject)
  {
    if (this.cachingIsEnabled)
    {
      if (this.interfaceAdapterCache == null)
        this.interfaceAdapterCache = new Hashtable();
      this.interfaceAdapterCache.put(paramClass, paramObject);
    }
  }
}