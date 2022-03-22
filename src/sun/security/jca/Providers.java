package sun.security.jca;

import java.security.AccessController;
import java.security.Policy;
import java.security.PrivilegedAction;
import sun.security.util.Debug;

public class Providers
{
  private static final ThreadLocal<ProviderList> threadLists = new InheritableThreadLocal();
  private static volatile int threadListsUsed;
  private static volatile ProviderList providerList = ProviderList.EMPTY;
  private static volatile boolean policyInitialized;
  private static final String[] jarClassNames;

  public static Object startJarVerification()
  {
    ProviderList localProviderList1 = getProviderList();
    ProviderList localProviderList2 = localProviderList1.getJarList(jarClassNames);
    return beginThreadProviderList(localProviderList2);
  }

  public static void stopJarVerification(Object paramObject)
  {
    endThreadProviderList((ProviderList)paramObject);
  }

  public static ProviderList getProviderList()
  {
    if (!(policyInitialized))
      try
      {
        if (System.getSecurityManager() != null)
          AccessController.doPrivileged(new PrivilegedAction()
          {
            public Void run()
            {
              Policy.getPolicy();
              return null;
            }
          });
        policyInitialized = true;
      }
      catch (Exception localException)
      {
        if (ProviderList.debug != null)
        {
          ProviderList.debug.println("policy init failed");
          localException.printStackTrace();
        }
      }
    ProviderList localProviderList = getThreadProviderList();
    if (localProviderList == null)
      localProviderList = getSystemProviderList();
    return localProviderList;
  }

  public static void setProviderList(ProviderList paramProviderList)
  {
    if (getThreadProviderList() == null)
      setSystemProviderList(paramProviderList);
    else
      changeThreadProviderList(paramProviderList);
  }

  public static synchronized ProviderList getFullProviderList()
  {
    Object localObject = getThreadProviderList();
    if (localObject != null)
    {
      localProviderList = ((ProviderList)localObject).removeInvalid();
      if (localProviderList != localObject)
      {
        changeThreadProviderList(localProviderList);
        localObject = localProviderList;
      }
      return localObject;
    }
    localObject = getSystemProviderList();
    ProviderList localProviderList = ((ProviderList)localObject).removeInvalid();
    if (localProviderList != localObject)
    {
      setSystemProviderList(localProviderList);
      localObject = localProviderList;
    }
    return ((ProviderList)localObject);
  }

  private static ProviderList getSystemProviderList()
  {
    return providerList;
  }

  private static void setSystemProviderList(ProviderList paramProviderList)
  {
    providerList = paramProviderList;
  }

  public static ProviderList getThreadProviderList()
  {
    if (threadListsUsed == 0)
      return null;
    return ((ProviderList)threadLists.get());
  }

  private static void changeThreadProviderList(ProviderList paramProviderList)
  {
    threadLists.set(paramProviderList);
  }

  public static synchronized ProviderList beginThreadProviderList(ProviderList paramProviderList)
  {
    if (ProviderList.debug != null)
      ProviderList.debug.println("ThreadLocal providers: " + paramProviderList);
    ProviderList localProviderList = (ProviderList)threadLists.get();
    threadListsUsed += 1;
    threadLists.set(paramProviderList);
    return localProviderList;
  }

  public static synchronized void endThreadProviderList(ProviderList paramProviderList)
  {
    if (paramProviderList == null)
    {
      if (ProviderList.debug != null)
        ProviderList.debug.println("Disabling ThreadLocal providers");
      threadLists.remove();
    }
    else
    {
      if (ProviderList.debug != null)
        ProviderList.debug.println("Restoring previous ThreadLocal providers: " + paramProviderList);
      threadLists.set(paramProviderList);
    }
    threadListsUsed -= 1;
  }

  static
  {
    providerList = ProviderList.fromSecurityProperties();
    jarClassNames = { "sun.security.provider.Sun", "sun.security.rsa.SunRsaSign" };
  }
}