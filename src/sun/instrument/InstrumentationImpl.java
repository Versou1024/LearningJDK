package sun.instrument;

import java.lang.instrument.ClassDefinition;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;
import java.util.jar.JarFile;

public class InstrumentationImpl
  implements Instrumentation
{
  private final TransformerManager mTransformerManager = new TransformerManager(false);
  private TransformerManager mRetransfomableTransformerManager = null;
  private final long mNativeAgent;
  private final boolean mEnvironmentSupportsRedefineClasses;
  private volatile boolean mEnvironmentSupportsRetransformClassesKnown;
  private volatile boolean mEnvironmentSupportsRetransformClasses;
  private final boolean mEnvironmentSupportsNativeMethodPrefix;

  private InstrumentationImpl(long paramLong, boolean paramBoolean1, boolean paramBoolean2)
  {
    this.mNativeAgent = paramLong;
    this.mEnvironmentSupportsRedefineClasses = paramBoolean1;
    this.mEnvironmentSupportsRetransformClassesKnown = false;
    this.mEnvironmentSupportsRetransformClasses = false;
    this.mEnvironmentSupportsNativeMethodPrefix = paramBoolean2;
  }

  public void addTransformer(ClassFileTransformer paramClassFileTransformer)
  {
    addTransformer(paramClassFileTransformer, false);
  }

  public synchronized void addTransformer(ClassFileTransformer paramClassFileTransformer, boolean paramBoolean)
  {
    if (paramClassFileTransformer == null)
      throw new NullPointerException("null passed as 'transformer' in addTransformer");
    if (paramBoolean)
    {
      if (!(isRetransformClassesSupported()))
        throw new UnsupportedOperationException("adding retransformable transformers is not supported in this environment");
      if (this.mRetransfomableTransformerManager == null)
        this.mRetransfomableTransformerManager = new TransformerManager(true);
      this.mRetransfomableTransformerManager.addTransformer(paramClassFileTransformer);
      if (this.mRetransfomableTransformerManager.getTransformerCount() == 1)
        setHasRetransformableTransformers(this.mNativeAgent, true);
    }
    else
    {
      this.mTransformerManager.addTransformer(paramClassFileTransformer);
    }
  }

  public synchronized boolean removeTransformer(ClassFileTransformer paramClassFileTransformer)
  {
    if (paramClassFileTransformer == null)
      throw new NullPointerException("null passed as 'transformer' in removeTransformer");
    TransformerManager localTransformerManager = findTransformerManager(paramClassFileTransformer);
    if (localTransformerManager != null)
    {
      localTransformerManager.removeTransformer(paramClassFileTransformer);
      if ((localTransformerManager.isRetransformable()) && (localTransformerManager.getTransformerCount() == 0))
        setHasRetransformableTransformers(this.mNativeAgent, false);
      return true;
    }
    return false;
  }

  public boolean isModifiableClass(Class<?> paramClass)
  {
    if (paramClass == null)
      throw new NullPointerException("null passed as 'theClass' in isModifiableClass");
    return isModifiableClass0(this.mNativeAgent, paramClass);
  }

  public boolean isRetransformClassesSupported()
  {
    if (!(this.mEnvironmentSupportsRetransformClassesKnown))
    {
      this.mEnvironmentSupportsRetransformClasses = isRetransformClassesSupported0(this.mNativeAgent);
      this.mEnvironmentSupportsRetransformClassesKnown = true;
    }
    return this.mEnvironmentSupportsRetransformClasses;
  }

  public void retransformClasses(Class<?>[] paramArrayOfClass)
  {
    if (!(isRetransformClassesSupported()))
      throw new UnsupportedOperationException("retransformClasses is not supported in this environment");
    retransformClasses0(this.mNativeAgent, paramArrayOfClass);
  }

  public boolean isRedefineClassesSupported()
  {
    return this.mEnvironmentSupportsRedefineClasses;
  }

  public void redefineClasses(ClassDefinition[] paramArrayOfClassDefinition)
    throws ClassNotFoundException
  {
    if (!(isRedefineClassesSupported()))
      throw new UnsupportedOperationException("redefineClasses is not supported in this environment");
    if (paramArrayOfClassDefinition == null)
      throw new NullPointerException("null passed as 'definitions' in redefineClasses");
    for (int i = 0; i < paramArrayOfClassDefinition.length; ++i)
      if (paramArrayOfClassDefinition[i] == null)
        throw new NullPointerException("element of 'definitions' is null in redefineClasses");
    if (paramArrayOfClassDefinition.length == 0)
      return;
    redefineClasses0(this.mNativeAgent, paramArrayOfClassDefinition);
  }

  public Class[] getAllLoadedClasses()
  {
    return getAllLoadedClasses0(this.mNativeAgent);
  }

  public Class[] getInitiatedClasses(ClassLoader paramClassLoader)
  {
    return getInitiatedClasses0(this.mNativeAgent, paramClassLoader);
  }

  public long getObjectSize(Object paramObject)
  {
    if (paramObject == null)
      throw new NullPointerException("null passed as 'objectToSize' in getObjectSize");
    return getObjectSize0(this.mNativeAgent, paramObject);
  }

  public void appendToBootstrapClassLoaderSearch(JarFile paramJarFile)
  {
    appendToClassLoaderSearch0(this.mNativeAgent, paramJarFile.getName(), true);
  }

  public void appendToSystemClassLoaderSearch(JarFile paramJarFile)
  {
    appendToClassLoaderSearch0(this.mNativeAgent, paramJarFile.getName(), false);
  }

  public boolean isNativeMethodPrefixSupported()
  {
    return this.mEnvironmentSupportsNativeMethodPrefix;
  }

  public synchronized void setNativeMethodPrefix(ClassFileTransformer paramClassFileTransformer, String paramString)
  {
    if (!(isNativeMethodPrefixSupported()))
      throw new UnsupportedOperationException("setNativeMethodPrefix is not supported in this environment");
    if (paramClassFileTransformer == null)
      throw new NullPointerException("null passed as 'transformer' in setNativeMethodPrefix");
    TransformerManager localTransformerManager = findTransformerManager(paramClassFileTransformer);
    if (localTransformerManager == null)
      throw new IllegalArgumentException("transformer not registered in setNativeMethodPrefix");
    localTransformerManager.setNativeMethodPrefix(paramClassFileTransformer, paramString);
    String[] arrayOfString = localTransformerManager.getNativeMethodPrefixes();
    setNativeMethodPrefixes(this.mNativeAgent, arrayOfString, localTransformerManager.isRetransformable());
  }

  private TransformerManager findTransformerManager(ClassFileTransformer paramClassFileTransformer)
  {
    if (this.mTransformerManager.includesTransformer(paramClassFileTransformer))
      return this.mTransformerManager;
    if ((this.mRetransfomableTransformerManager != null) && (this.mRetransfomableTransformerManager.includesTransformer(paramClassFileTransformer)))
      return this.mRetransfomableTransformerManager;
    return null;
  }

  private native boolean isModifiableClass0(long paramLong, Class<?> paramClass);

  private native boolean isRetransformClassesSupported0(long paramLong);

  private native void setHasRetransformableTransformers(long paramLong, boolean paramBoolean);

  private native void retransformClasses0(long paramLong, Class<?>[] paramArrayOfClass);

  private native void redefineClasses0(long paramLong, ClassDefinition[] paramArrayOfClassDefinition)
    throws ClassNotFoundException;

  private native Class[] getAllLoadedClasses0(long paramLong);

  private native Class[] getInitiatedClasses0(long paramLong, ClassLoader paramClassLoader);

  private native long getObjectSize0(long paramLong, Object paramObject);

  private native void appendToClassLoaderSearch0(long paramLong, String paramString, boolean paramBoolean);

  private native void setNativeMethodPrefixes(long paramLong, String[] paramArrayOfString, boolean paramBoolean);

  private static void setAccessible(AccessibleObject paramAccessibleObject, boolean paramBoolean)
  {
    AccessController.doPrivileged(new PrivilegedAction(paramAccessibleObject, paramBoolean)
    {
      public Object run()
      {
        this.val$ao.setAccessible(this.val$accessible);
        return null;
      }
    });
  }

  private void loadClassAndStartAgent(String paramString1, String paramString2, String paramString3)
    throws Throwable
  {
    ClassLoader localClassLoader = ClassLoader.getSystemClassLoader();
    Class localClass = localClassLoader.loadClass(paramString1);
    Method localMethod = null;
    Object localObject = null;
    int i = 0;
    try
    {
      localMethod = localClass.getMethod(paramString2, new Class[] { String.class, Instrumentation.class });
      i = 1;
    }
    catch (NoSuchMethodException localNoSuchMethodException1)
    {
      localObject = localNoSuchMethodException1;
    }
    if (localMethod == null)
      try
      {
        localMethod = localClass.getMethod(paramString2, new Class[] { String.class });
      }
      catch (NoSuchMethodException localNoSuchMethodException2)
      {
        throw localObject;
      }
    setAccessible(localMethod, true);
    if (i != 0)
      localMethod.invoke(null, new Object[] { paramString3, this });
    else
      localMethod.invoke(null, new Object[] { paramString3 });
    setAccessible(localMethod, false);
  }

  private void loadClassAndCallPremain(String paramString1, String paramString2)
    throws Throwable
  {
    loadClassAndStartAgent(paramString1, "premain", paramString2);
  }

  private void loadClassAndCallAgentmain(String paramString1, String paramString2)
    throws Throwable
  {
    loadClassAndStartAgent(paramString1, "agentmain", paramString2);
  }

  private byte[] transform(ClassLoader paramClassLoader, String paramString, Class paramClass, ProtectionDomain paramProtectionDomain, byte[] paramArrayOfByte, boolean paramBoolean)
  {
    TransformerManager localTransformerManager = (paramBoolean) ? this.mRetransfomableTransformerManager : this.mTransformerManager;
    if (localTransformerManager == null)
      return null;
    return localTransformerManager.transform(paramClassLoader, paramString, paramClass, paramProtectionDomain, paramArrayOfByte);
  }

  static
  {
    System.loadLibrary("instrument");
  }
}