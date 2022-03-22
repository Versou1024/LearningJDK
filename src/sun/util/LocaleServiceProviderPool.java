package sun.util;

import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.text.spi.BreakIteratorProvider;
import java.text.spi.CollatorProvider;
import java.text.spi.DateFormatProvider;
import java.text.spi.DateFormatSymbolsProvider;
import java.text.spi.DecimalFormatSymbolsProvider;
import java.text.spi.NumberFormatProvider;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.spi.CurrencyNameProvider;
import java.util.spi.LocaleNameProvider;
import java.util.spi.LocaleServiceProvider;
import java.util.spi.TimeZoneNameProvider;
import sun.util.resources.LocaleData;
import sun.util.resources.OpenListResourceBundle;

public final class LocaleServiceProviderPool
{
  private static Map<Class, LocaleServiceProviderPool> poolOfPools = new ConcurrentHashMap();
  private Set<LocaleServiceProvider> providers = new LinkedHashSet();
  private Map<Locale, LocaleServiceProvider> providersCache = new ConcurrentHashMap();
  private Set<Locale> availableLocales = null;
  private static List<Locale> availableJRELocales = null;
  private Set<Locale> providerLocales = null;

  public static LocaleServiceProviderPool getPool(Class<? extends LocaleServiceProvider> paramClass)
  {
    Object localObject = (LocaleServiceProviderPool)poolOfPools.get(paramClass);
    if (localObject == null)
    {
      LocaleServiceProviderPool localLocaleServiceProviderPool = new LocaleServiceProviderPool(paramClass);
      localObject = (LocaleServiceProviderPool)poolOfPools.put(paramClass, localLocaleServiceProviderPool);
      if (localObject == null)
        localObject = localLocaleServiceProviderPool;
    }
    return ((LocaleServiceProviderPool)localObject);
  }

  private LocaleServiceProviderPool(Class<? extends LocaleServiceProvider> paramClass)
  {
    try
    {
      AccessController.doPrivileged(new PrivilegedExceptionAction(this, paramClass)
      {
        public Object run()
        {
          Iterator localIterator = ServiceLoader.loadInstalled(this.val$c).iterator();
          while (localIterator.hasNext())
          {
            LocaleServiceProvider localLocaleServiceProvider = (LocaleServiceProvider)localIterator.next();
            LocaleServiceProviderPool.access$000(this.this$0).add(localLocaleServiceProvider);
          }
          return null;
        }
      });
    }
    catch (PrivilegedActionException localPrivilegedActionException)
    {
      Logger.getLogger("sun.util.LocaleServiceProviderPool").config(localPrivilegedActionException.toString());
    }
  }

  public static Locale[] getAllAvailableLocales()
  {
    return ((Locale[])AllAvailableLocales.allAvailableLocales.clone());
  }

  public synchronized Locale[] getAvailableLocales()
  {
    if (this.availableLocales == null)
    {
      this.availableLocales = new HashSet(getJRELocales());
      if (hasProviders())
        this.availableLocales.addAll(getProviderLocales());
    }
    Locale[] arrayOfLocale = new Locale[this.availableLocales.size()];
    this.availableLocales.toArray(arrayOfLocale);
    return arrayOfLocale;
  }

  private synchronized Set<Locale> getProviderLocales()
  {
    if (this.providerLocales == null)
    {
      this.providerLocales = new HashSet();
      if (hasProviders())
      {
        Iterator localIterator = this.providers.iterator();
        while (localIterator.hasNext())
        {
          LocaleServiceProvider localLocaleServiceProvider = (LocaleServiceProvider)localIterator.next();
          Locale[] arrayOfLocale1 = localLocaleServiceProvider.getAvailableLocales();
          Locale[] arrayOfLocale2 = arrayOfLocale1;
          int i = arrayOfLocale2.length;
          for (int j = 0; j < i; ++j)
          {
            Locale localLocale = arrayOfLocale2[j];
            this.providerLocales.add(localLocale);
          }
        }
      }
    }
    return this.providerLocales;
  }

  public boolean hasProviders()
  {
    return (!(this.providers.isEmpty()));
  }

  private synchronized List<Locale> getJRELocales()
  {
    if (availableJRELocales == null)
      availableJRELocales = Arrays.asList(LocaleData.getAvailableLocales());
    return availableJRELocales;
  }

  private boolean isJRESupported(Locale paramLocale)
  {
    List localList = getJRELocales();
    return localList.contains(paramLocale);
  }

  public <P, S> S getLocalizedObject(LocalizedObjectGetter<P, S> paramLocalizedObjectGetter, Locale paramLocale, Object[] paramArrayOfObject)
  {
    return getLocalizedObjectImpl(paramLocalizedObjectGetter, paramLocale, true, null, null, paramArrayOfObject);
  }

  public <P, S> S getLocalizedObject(LocalizedObjectGetter<P, S> paramLocalizedObjectGetter, Locale paramLocale, OpenListResourceBundle paramOpenListResourceBundle, String paramString, Object[] paramArrayOfObject)
  {
    return getLocalizedObjectImpl(paramLocalizedObjectGetter, paramLocale, false, paramOpenListResourceBundle, paramString, paramArrayOfObject);
  }

  private <P, S> S getLocalizedObjectImpl(LocalizedObjectGetter<P, S> paramLocalizedObjectGetter, Locale paramLocale, boolean paramBoolean, OpenListResourceBundle paramOpenListResourceBundle, String paramString, Object[] paramArrayOfObject)
  {
    if (hasProviders())
    {
      LocaleServiceProvider localLocaleServiceProvider;
      Locale localLocale1 = (paramOpenListResourceBundle != null) ? paramOpenListResourceBundle.getLocale() : null;
      Locale localLocale2 = paramLocale;
      Object localObject = null;
      while ((paramLocale = findProviderLocale(paramLocale, localLocale1)) != null)
      {
        localLocaleServiceProvider = findProvider(paramLocale);
        if (localLocaleServiceProvider != null)
        {
          localObject = paramLocalizedObjectGetter.getObject(localLocaleServiceProvider, localLocale2, paramString, paramArrayOfObject);
          if (localObject != null)
            return localObject;
          if (paramBoolean)
            Logger.getLogger("sun.util.LocaleServiceProviderPool").config("A locale sensitive service provider returned null for a localized objects,  which should not happen.  provider: " + localLocaleServiceProvider + " locale: " + localLocale2);
        }
        paramLocale = getParentLocale(paramLocale);
      }
      while (paramOpenListResourceBundle != null)
      {
        localLocale1 = paramOpenListResourceBundle.getLocale();
        if (paramOpenListResourceBundle.handleGetKeys().contains(paramString))
          return null;
        localLocaleServiceProvider = findProvider(localLocale1);
        if (localLocaleServiceProvider != null)
        {
          localObject = paramLocalizedObjectGetter.getObject(localLocaleServiceProvider, localLocale2, paramString, paramArrayOfObject);
          if (localObject != null)
            return localObject;
        }
        paramOpenListResourceBundle = paramOpenListResourceBundle.getParent();
      }
    }
    return null;
  }

  private LocaleServiceProvider findProvider(Locale paramLocale)
  {
    Object localObject;
    if (!(hasProviders()))
      return null;
    if (this.providersCache.containsKey(paramLocale))
    {
      localObject = (LocaleServiceProvider)this.providersCache.get(paramLocale);
      if (localObject != NullProvider.access$200())
        return localObject;
    }
    else
    {
      localObject = this.providers.iterator();
      while (((Iterator)localObject).hasNext())
      {
        LocaleServiceProvider localLocaleServiceProvider1 = (LocaleServiceProvider)((Iterator)localObject).next();
        Locale[] arrayOfLocale1 = localLocaleServiceProvider1.getAvailableLocales();
        Locale[] arrayOfLocale2 = arrayOfLocale1;
        int i = arrayOfLocale2.length;
        for (int j = 0; j < i; ++j)
        {
          Locale localLocale = arrayOfLocale2[j];
          if (paramLocale.equals(localLocale))
          {
            LocaleServiceProvider localLocaleServiceProvider2 = (LocaleServiceProvider)this.providersCache.put(paramLocale, localLocaleServiceProvider1);
            return ((localLocaleServiceProvider2 != null) ? localLocaleServiceProvider2 : localLocaleServiceProvider1);
          }
        }
      }
      this.providersCache.put(paramLocale, NullProvider.access$200());
    }
    return ((LocaleServiceProvider)null);
  }

  private Locale findProviderLocale(Locale paramLocale1, Locale paramLocale2)
  {
    Set localSet = getProviderLocales();
    for (Locale localLocale = paramLocale1; localLocale != null; localLocale = getParentLocale(localLocale))
    {
      if (paramLocale2 != null)
      {
        if (!(localLocale.equals(paramLocale2)))
          break label47;
        localLocale = null;
        break;
      }
      if (isJRESupported(localLocale))
      {
        localLocale = null;
        break;
      }
      if (localSet.contains(localLocale))
        label47: break;
    }
    return localLocale;
  }

  private static Locale getParentLocale(Locale paramLocale)
  {
    String str = paramLocale.getVariant();
    if (str != "")
    {
      int i = str.lastIndexOf(95);
      if (i != -1)
        return new Locale(paramLocale.getLanguage(), paramLocale.getCountry(), str.substring(0, i));
      return new Locale(paramLocale.getLanguage(), paramLocale.getCountry());
    }
    if (paramLocale.getCountry() != "")
      return new Locale(paramLocale.getLanguage());
    if (paramLocale.getLanguage() != "")
      return Locale.ROOT;
    return null;
  }

  private static class AllAvailableLocales
  {
    static final Locale[] allAvailableLocales;

    static
    {
      Class[] arrayOfClass1 = { BreakIteratorProvider.class, CollatorProvider.class, DateFormatProvider.class, DateFormatSymbolsProvider.class, DecimalFormatSymbolsProvider.class, NumberFormatProvider.class, CurrencyNameProvider.class, LocaleNameProvider.class, TimeZoneNameProvider.class };
      HashSet localHashSet = new HashSet(Arrays.asList(LocaleData.getAvailableLocales()));
      Class[] arrayOfClass2 = arrayOfClass1;
      int i = arrayOfClass2.length;
      for (int j = 0; j < i; ++j)
      {
        Class localClass = arrayOfClass2[j];
        LocaleServiceProviderPool localLocaleServiceProviderPool = LocaleServiceProviderPool.getPool(localClass);
        localHashSet.addAll(LocaleServiceProviderPool.access$100(localLocaleServiceProviderPool));
      }
      allAvailableLocales = (Locale[])localHashSet.toArray(new Locale[0]);
    }
  }

  public static abstract interface LocalizedObjectGetter<P, S>
  {
    public abstract S getObject(P paramP, Locale paramLocale, String paramString, Object[] paramArrayOfObject);
  }

  private static class NullProvider extends LocaleServiceProvider
  {
    private static final NullProvider INSTANCE = new NullProvider();

    public Locale[] getAvailableLocales()
    {
      throw new RuntimeException("Should not get called.");
    }
  }
}