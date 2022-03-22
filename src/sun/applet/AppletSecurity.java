package sun.applet;

import java.lang.reflect.Field;
import java.net.URLClassLoader;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import sun.awt.AWTSecurityManager;
import sun.awt.AppContext;
import sun.security.util.SecurityConstants;

public class AppletSecurity extends AWTSecurityManager
{
  private AppContext mainAppContext;
  private static Field facc = null;
  private static Field fcontext = null;
  private HashSet restrictedPackages = new HashSet();
  private boolean inThreadGroupCheck = false;

  public AppletSecurity()
  {
    reset();
    this.mainAppContext = AppContext.getAppContext();
  }

  public void reset()
  {
    this.restrictedPackages.clear();
    AccessController.doPrivileged(new PrivilegedAction(this)
    {
      public Object run()
      {
        Enumeration localEnumeration = System.getProperties().propertyNames();
        while (localEnumeration.hasMoreElements())
        {
          String str1 = (String)localEnumeration.nextElement();
          if ((str1 != null) && (str1.startsWith("package.restrict.access.")))
          {
            String str2 = System.getProperty(str1);
            if ((str2 != null) && (str2.equalsIgnoreCase("true")))
            {
              String str3 = str1.substring(24);
              AppletSecurity.access$000(this.this$0).add(str3);
            }
          }
        }
        return null;
      }
    });
  }

  private AppletClassLoader currentAppletClassLoader()
  {
    ClassLoader localClassLoader1 = currentClassLoader();
    if ((localClassLoader1 == null) || (localClassLoader1 instanceof AppletClassLoader))
      return ((AppletClassLoader)localClassLoader1);
    Class[] arrayOfClass = getClassContext();
    for (int i = 0; i < arrayOfClass.length; ++i)
    {
      localClassLoader1 = arrayOfClass[i].getClassLoader();
      if (localClassLoader1 instanceof AppletClassLoader)
        return ((AppletClassLoader)localClassLoader1);
    }
    for (i = 0; i < arrayOfClass.length; ++i)
    {
      ClassLoader localClassLoader2 = arrayOfClass[i].getClassLoader();
      if (localClassLoader2 instanceof URLClassLoader)
      {
        localClassLoader1 = (ClassLoader)AccessController.doPrivileged(new PrivilegedAction(this, localClassLoader2)
        {
          public Object run()
          {
            AccessControlContext localAccessControlContext = null;
            ProtectionDomain[] arrayOfProtectionDomain = null;
            try
            {
              localAccessControlContext = (AccessControlContext)AppletSecurity.access$100().get(this.val$currentLoader);
              if (localAccessControlContext == null)
                return null;
              arrayOfProtectionDomain = (ProtectionDomain[])(ProtectionDomain[])AppletSecurity.access$200().get(localAccessControlContext);
              if (arrayOfProtectionDomain == null)
                return null;
            }
            catch (Exception localException)
            {
              throw new UnsupportedOperationException(localException);
            }
            for (int i = 0; i < arrayOfProtectionDomain.length; ++i)
            {
              ClassLoader localClassLoader = arrayOfProtectionDomain[i].getClassLoader();
              if (localClassLoader instanceof AppletClassLoader)
                return localClassLoader;
            }
            return null;
          }
        });
        if (localClassLoader1 != null)
          return ((AppletClassLoader)localClassLoader1);
      }
    }
    localClassLoader1 = Thread.currentThread().getContextClassLoader();
    if (localClassLoader1 instanceof AppletClassLoader)
      return ((AppletClassLoader)localClassLoader1);
    return ((AppletClassLoader)null);
  }

  protected boolean inThreadGroup(ThreadGroup paramThreadGroup)
  {
    if (currentAppletClassLoader() == null)
      return false;
    return getThreadGroup().parentOf(paramThreadGroup);
  }

  protected boolean inThreadGroup(Thread paramThread)
  {
    return inThreadGroup(paramThread.getThreadGroup());
  }

  public void checkAccess(Thread paramThread)
  {
    if ((paramThread.getState() != Thread.State.TERMINATED) && (!(inThreadGroup(paramThread))))
      checkPermission(SecurityConstants.MODIFY_THREAD_PERMISSION);
  }

  public synchronized void checkAccess(ThreadGroup paramThreadGroup)
  {
    if (this.inThreadGroupCheck)
      checkPermission(SecurityConstants.MODIFY_THREADGROUP_PERMISSION);
    else
      try
      {
        this.inThreadGroupCheck = true;
        if (!(inThreadGroup(paramThreadGroup)))
          checkPermission(SecurityConstants.MODIFY_THREADGROUP_PERMISSION);
      }
      finally
      {
        this.inThreadGroupCheck = false;
      }
  }

  public void checkPackageAccess(String paramString)
  {
    super.checkPackageAccess(paramString);
    Iterator localIterator = this.restrictedPackages.iterator();
    while (localIterator.hasNext())
    {
      String str = (String)localIterator.next();
      if ((paramString.equals(str)) || (paramString.startsWith(str + ".")))
        checkPermission(new RuntimePermission("accessClassInPackage." + paramString));
    }
  }

  public void checkAwtEventQueueAccess()
  {
    AppContext localAppContext = AppContext.getAppContext();
    AppletClassLoader localAppletClassLoader = currentAppletClassLoader();
    if ((localAppContext == this.mainAppContext) && (localAppletClassLoader != null))
      super.checkAwtEventQueueAccess();
  }

  public ThreadGroup getThreadGroup()
  {
    AppletClassLoader localAppletClassLoader = currentAppletClassLoader();
    ThreadGroup localThreadGroup = (localAppletClassLoader == null) ? null : localAppletClassLoader.getThreadGroup();
    if (localThreadGroup != null)
      return localThreadGroup;
    return super.getThreadGroup();
  }

  public AppContext getAppContext()
  {
    AppletClassLoader localAppletClassLoader = currentAppletClassLoader();
    if (localAppletClassLoader == null)
      return null;
    AppContext localAppContext = localAppletClassLoader.getAppContext();
    if (localAppContext == null)
      throw new SecurityException("Applet classloader has invalid AppContext");
    return localAppContext;
  }

  static
  {
    try
    {
      facc = URLClassLoader.class.getDeclaredField("acc");
      facc.setAccessible(true);
      fcontext = AccessControlContext.class.getDeclaredField("context");
      fcontext.setAccessible(true);
    }
    catch (NoSuchFieldException localNoSuchFieldException)
    {
      throw new UnsupportedOperationException(localNoSuchFieldException);
    }
  }
}