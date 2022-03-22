package sun.security.jca;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.Provider.Service;
import java.util.Iterator;
import java.util.List;

public class GetInstance
{
  public static Provider.Service getService(String paramString1, String paramString2)
    throws NoSuchAlgorithmException
  {
    ProviderList localProviderList = Providers.getProviderList();
    Provider.Service localService = localProviderList.getService(paramString1, paramString2);
    if (localService == null)
      throw new NoSuchAlgorithmException(paramString2 + " " + paramString1 + " not available");
    return localService;
  }

  public static Provider.Service getService(String paramString1, String paramString2, String paramString3)
    throws NoSuchAlgorithmException, NoSuchProviderException
  {
    if ((paramString3 == null) || (paramString3.length() == 0))
      throw new IllegalArgumentException("missing provider");
    Provider localProvider = Providers.getProviderList().getProvider(paramString3);
    if (localProvider == null)
      throw new NoSuchProviderException("no such provider: " + paramString3);
    Provider.Service localService = localProvider.getService(paramString1, paramString2);
    if (localService == null)
      throw new NoSuchAlgorithmException("no such algorithm: " + paramString2 + " for provider " + paramString3);
    return localService;
  }

  public static Provider.Service getService(String paramString1, String paramString2, Provider paramProvider)
    throws NoSuchAlgorithmException
  {
    if (paramProvider == null)
      throw new IllegalArgumentException("missing provider");
    Provider.Service localService = paramProvider.getService(paramString1, paramString2);
    if (localService == null)
      throw new NoSuchAlgorithmException("no such algorithm: " + paramString2 + " for provider " + paramProvider.getName());
    return localService;
  }

  public static List<Provider.Service> getServices(String paramString1, String paramString2)
  {
    ProviderList localProviderList = Providers.getProviderList();
    return localProviderList.getServices(paramString1, paramString2);
  }

  @Deprecated
  public static List<Provider.Service> getServices(String paramString, List<String> paramList)
  {
    ProviderList localProviderList = Providers.getProviderList();
    return localProviderList.getServices(paramString, paramList);
  }

  public static List<Provider.Service> getServices(List<ServiceId> paramList)
  {
    ProviderList localProviderList = Providers.getProviderList();
    return localProviderList.getServices(paramList);
  }

  public static Instance getInstance(String paramString1, Class paramClass, String paramString2)
    throws NoSuchAlgorithmException
  {
    ProviderList localProviderList = Providers.getProviderList();
    Provider.Service localService1 = localProviderList.getService(paramString1, paramString2);
    if (localService1 == null)
      throw new NoSuchAlgorithmException(paramString2 + " " + paramString1 + " not available");
    try
    {
      return getInstance(localService1, paramClass);
    }
    catch (NoSuchAlgorithmException localIterator)
    {
      Provider.Service localService2;
      Object localObject = localNoSuchAlgorithmException1;
      Iterator localIterator = localProviderList.getServices(paramString1, paramString2).iterator();
      while (localIterator.hasNext())
      {
        localService2 = (Provider.Service)localIterator.next();
        if (localService2 != localService1)
          break;
      }
      try
      {
        return getInstance(localService2, paramClass);
      }
      catch (NoSuchAlgorithmException localNoSuchAlgorithmException2)
      {
        while (true)
          localObject = localNoSuchAlgorithmException2;
        throw ((Throwable)localObject);
      }
    }
  }

  public static Instance getInstance(String paramString1, Class paramClass, String paramString2, Object paramObject)
    throws NoSuchAlgorithmException
  {
    Provider.Service localService;
    List localList = getServices(paramString1, paramString2);
    Object localObject = null;
    Iterator localIterator = localList.iterator();
    if (localIterator.hasNext())
      localService = (Provider.Service)localIterator.next();
    try
    {
      return getInstance(localService, paramClass, paramObject);
    }
    catch (NoSuchAlgorithmException localNoSuchAlgorithmException)
    {
      while (true)
        localObject = localNoSuchAlgorithmException;
      if (localObject != null)
        throw localObject;
      throw new NoSuchAlgorithmException(paramString2 + " " + paramString1 + " not available");
    }
  }

  public static Instance getInstance(String paramString1, Class paramClass, String paramString2, String paramString3)
    throws NoSuchAlgorithmException, NoSuchProviderException
  {
    return getInstance(getService(paramString1, paramString2, paramString3), paramClass);
  }

  public static Instance getInstance(String paramString1, Class paramClass, String paramString2, Object paramObject, String paramString3)
    throws NoSuchAlgorithmException, NoSuchProviderException
  {
    return getInstance(getService(paramString1, paramString2, paramString3), paramClass, paramObject);
  }

  public static Instance getInstance(String paramString1, Class paramClass, String paramString2, Provider paramProvider)
    throws NoSuchAlgorithmException
  {
    return getInstance(getService(paramString1, paramString2, paramProvider), paramClass);
  }

  public static Instance getInstance(String paramString1, Class paramClass, String paramString2, Object paramObject, Provider paramProvider)
    throws NoSuchAlgorithmException
  {
    return getInstance(getService(paramString1, paramString2, paramProvider), paramClass, paramObject);
  }

  public static Instance getInstance(Provider.Service paramService, Class paramClass)
    throws NoSuchAlgorithmException
  {
    Object localObject = paramService.newInstance(null);
    checkSuperClass(paramService, localObject.getClass(), paramClass);
    return new Instance(paramService.getProvider(), localObject, null);
  }

  public static Instance getInstance(Provider.Service paramService, Class paramClass, Object paramObject)
    throws NoSuchAlgorithmException
  {
    Object localObject = paramService.newInstance(paramObject);
    checkSuperClass(paramService, localObject.getClass(), paramClass);
    return new Instance(paramService.getProvider(), localObject, null);
  }

  public static void checkSuperClass(Provider.Service paramService, Class paramClass1, Class paramClass2)
    throws NoSuchAlgorithmException
  {
    if (paramClass2 == null)
      return;
    if (!(paramClass2.isAssignableFrom(paramClass1)))
      throw new NoSuchAlgorithmException("class configured for " + paramService.getType() + ": " + paramService.getClassName() + " not a " + paramService.getType());
  }

  public static final class Instance
  {
    public final Provider provider;
    public final Object impl;

    private Instance(Provider paramProvider, Object paramObject)
    {
      this.provider = paramProvider;
      this.impl = paramObject;
    }

    public Object[] toArray()
    {
      return { this.impl, this.provider };
    }
  }
}