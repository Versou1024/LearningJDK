package sun.security.jca;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.security.AccessController;
import java.security.GeneralSecurityException;
import java.security.PrivilegedAction;
import java.security.Provider;
import java.security.ProviderException;
import sun.security.util.Debug;
import sun.security.util.PropertyExpander;

final class ProviderConfig
{
  private static final Debug debug = Debug.getInstance("jca", "ProviderConfig");
  private static final String P11_SOL_NAME = "sun.security.pkcs11.SunPKCS11";
  private static final String P11_SOL_ARG = "${java.home}/lib/security/sunpkcs11-solaris.cfg";
  private static final int MAX_LOAD_TRIES = 30;
  private static final Class[] CL_STRING = { String.class };
  private static volatile Object LOCK = new Object();
  private final String className;
  private final String argument;
  private int tries;
  private volatile Provider provider;
  private boolean isLoading;

  private static Object getLock()
  {
    Object localObject1 = LOCK;
    if (localObject1 instanceof ClassLoader)
      return localObject1;
    Object localObject2 = AccessController.doPrivileged(new PrivilegedAction()
    {
      public Object run()
      {
        return ClassLoader.getSystemClassLoader();
      }
    });
    if (localObject2 != null)
    {
      LOCK = localObject2;
      localObject1 = localObject2;
    }
    return localObject1;
  }

  ProviderConfig(String paramString1, String paramString2)
  {
    if ((paramString1.equals("sun.security.pkcs11.SunPKCS11")) && (paramString2.equals("${java.home}/lib/security/sunpkcs11-solaris.cfg")))
      checkSunPKCS11Solaris();
    this.className = paramString1;
    this.argument = expand(paramString2);
  }

  ProviderConfig(String paramString)
  {
    this(paramString, "");
  }

  ProviderConfig(Provider paramProvider)
  {
    this.className = paramProvider.getClass().getName();
    this.argument = "";
    this.provider = paramProvider;
  }

  private void checkSunPKCS11Solaris()
  {
    Object localObject = AccessController.doPrivileged(new PrivilegedAction(this)
    {
      public Object run()
      {
        File localFile = new File("/usr/lib/libpkcs11.so");
        if (!(localFile.exists()))
          return Boolean.FALSE;
        if ("false".equalsIgnoreCase(System.getProperty("sun.security.pkcs11.enable-solaris")))
          return Boolean.FALSE;
        return Boolean.TRUE;
      }
    });
    if (localObject == Boolean.FALSE)
      this.tries = 30;
  }

  private boolean hasArgument()
  {
    return (this.argument.length() != 0);
  }

  private boolean shouldLoad()
  {
    return (this.tries < 30);
  }

  private void disableLoad()
  {
    this.tries = 30;
  }

  boolean isLoaded()
  {
    return (this.provider != null);
  }

  public boolean equals(Object paramObject)
  {
    if (this == paramObject)
      return true;
    if (!(paramObject instanceof ProviderConfig))
      return false;
    ProviderConfig localProviderConfig = (ProviderConfig)paramObject;
    return ((this.className.equals(localProviderConfig.className)) && (this.argument.equals(localProviderConfig.argument)));
  }

  public int hashCode()
  {
    return (this.className.hashCode() + this.argument.hashCode());
  }

  public String toString()
  {
    if (hasArgument())
      return this.className + "('" + this.argument + "')";
    return this.className;
  }

  Provider getProvider()
  {
    Provider localProvider = this.provider;
    if (localProvider != null)
      return localProvider;
    if (!(shouldLoad()))
      return null;
    synchronized (getLock())
    {
      localProvider = this.provider;
      if (localProvider == null)
        break label39;
      return localProvider;
      label39: if (!(this.isLoading))
        break label93;
      if (debug == null)
        break label89;
      debug.println("Recursion loading provider: " + this);
      new Exception("Call trace").printStackTrace();
      label89: return null;
      try
      {
        label93: this.isLoading = true;
        this.tries += 1;
        localProvider = doLoadProvider();
      }
      finally
      {
        this.isLoading = false;
      }
      this.provider = localProvider;
    }
    return localProvider;
  }

  private Provider doLoadProvider()
  {
    Object localObject = AccessController.doPrivileged(new PrivilegedAction(this)
    {
      public Object run()
      {
        Object localObject1;
        if (ProviderConfig.access$000() != null)
          ProviderConfig.access$000().println("Loading provider: " + this.this$0);
        try
        {
          Object localObject2;
          ClassLoader localClassLoader = ClassLoader.getSystemClassLoader();
          if (localClassLoader != null)
            localObject1 = localClassLoader.loadClass(ProviderConfig.access$100(this.this$0));
          else
            localObject1 = Class.forName(ProviderConfig.access$100(this.this$0));
          if (!(ProviderConfig.access$200(this.this$0)))
          {
            localObject2 = ((Class)localObject1).newInstance();
          }
          else
          {
            Constructor localConstructor = ((Class)localObject1).getConstructor(ProviderConfig.access$300());
            localObject2 = localConstructor.newInstance(new String[] { ProviderConfig.access$400(this.this$0) });
          }
          if (localObject2 instanceof Provider)
          {
            if (ProviderConfig.access$000() != null)
              ProviderConfig.access$000().println("Loaded provider " + localObject2);
            return ((Provider)localObject2);
          }
          if (ProviderConfig.access$000() != null)
            ProviderConfig.access$000().println(ProviderConfig.access$100(this.this$0) + " is not a provider");
          ProviderConfig.access$500(this.this$0);
          return null;
        }
        catch (Exception localException)
        {
          if (localException instanceof InvocationTargetException)
            localObject1 = ((InvocationTargetException)localException).getCause();
          else
            localObject1 = localException;
          if (ProviderConfig.access$000() != null)
          {
            ProviderConfig.access$000().println("Error loading provider " + this);
            ((Throwable)localObject1).printStackTrace();
          }
          if (localObject1 instanceof ProviderException)
            throw ((ProviderException)localObject1);
          if (localObject1 instanceof UnsupportedOperationException)
            ProviderConfig.access$500(this.this$0);
        }
        return null;
      }
    });
    return ((Provider)localObject);
  }

  private static String expand(String paramString)
  {
    if (!(paramString.contains("${")))
      return paramString;
    Object localObject = AccessController.doPrivileged(new PrivilegedAction(paramString)
    {
      public Object run()
      {
        try
        {
          return PropertyExpander.expand(this.val$value);
        }
        catch (GeneralSecurityException localGeneralSecurityException)
        {
          throw new ProviderException(localGeneralSecurityException);
        }
      }
    });
    return ((String)localObject);
  }
}