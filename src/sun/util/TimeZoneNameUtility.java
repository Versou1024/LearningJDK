package sun.util;

import java.lang.ref.SoftReference;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.spi.TimeZoneNameProvider;
import sun.util.calendar.ZoneInfo;
import sun.util.resources.LocaleData;
import sun.util.resources.OpenListResourceBundle;

public final class TimeZoneNameUtility
{
  private static ConcurrentHashMap<Locale, SoftReference<OpenListResourceBundle>> cachedBundles = new ConcurrentHashMap();
  private static ConcurrentHashMap<Locale, SoftReference<String[][]>> cachedZoneData = new ConcurrentHashMap();

  public static final String[][] getZoneStrings(Locale paramLocale)
  {
    SoftReference localSoftReference = (SoftReference)cachedZoneData.get(paramLocale);
    if (localSoftReference != null)
      if ((arrayOfString = (String[][])localSoftReference.get()) != null)
        break label50;
    String[][] arrayOfString = loadZoneStrings(paramLocale);
    localSoftReference = new SoftReference(arrayOfString);
    cachedZoneData.put(paramLocale, localSoftReference);
    label50: return arrayOfString;
  }

  private static final String[][] loadZoneStrings(Locale paramLocale)
  {
    LinkedList localLinkedList = new LinkedList();
    OpenListResourceBundle localOpenListResourceBundle = getBundle(paramLocale);
    Enumeration localEnumeration = localOpenListResourceBundle.getKeys();
    String[] arrayOfString = null;
    while (localEnumeration.hasMoreElements())
    {
      localObject = (String)localEnumeration.nextElement();
      arrayOfString = retrieveDisplayNames(localOpenListResourceBundle, (String)localObject, paramLocale);
      if (arrayOfString != null)
        localLinkedList.add(arrayOfString);
    }
    Object localObject = new String[localLinkedList.size()][];
    return ((String)(String[][])localLinkedList.toArray(localObject));
  }

  public static final String[] retrieveDisplayNames(String paramString, Locale paramLocale)
  {
    OpenListResourceBundle localOpenListResourceBundle = getBundle(paramLocale);
    return retrieveDisplayNames(localOpenListResourceBundle, paramString, paramLocale);
  }

  private static final String[] retrieveDisplayNames(OpenListResourceBundle paramOpenListResourceBundle, String paramString, Locale paramLocale)
  {
    LocaleServiceProviderPool localLocaleServiceProviderPool = LocaleServiceProviderPool.getPool(TimeZoneNameProvider.class);
    String[] arrayOfString = null;
    if (localLocaleServiceProviderPool.hasProviders())
      arrayOfString = (String[])localLocaleServiceProviderPool.getLocalizedObject(TimeZoneNameGetter.access$000(), paramLocale, paramOpenListResourceBundle, paramString, new Object[0]);
    if (arrayOfString == null)
      try
      {
        arrayOfString = paramOpenListResourceBundle.getStringArray(paramString);
      }
      catch (MissingResourceException localMissingResourceException)
      {
      }
    return arrayOfString;
  }

  private static final OpenListResourceBundle getBundle(Locale paramLocale)
  {
    SoftReference localSoftReference = (SoftReference)cachedBundles.get(paramLocale);
    if (localSoftReference != null)
      if ((localOpenListResourceBundle = (OpenListResourceBundle)localSoftReference.get()) != null)
        break label50;
    OpenListResourceBundle localOpenListResourceBundle = LocaleData.getTimeZoneNames(paramLocale);
    localSoftReference = new SoftReference(localOpenListResourceBundle);
    cachedBundles.put(paramLocale, localSoftReference);
    label50: return localOpenListResourceBundle;
  }

  private static class TimeZoneNameGetter
  implements LocaleServiceProviderPool.LocalizedObjectGetter<TimeZoneNameProvider, String[]>
  {
    private static final TimeZoneNameGetter INSTANCE;

    public String[] getObject(TimeZoneNameProvider paramTimeZoneNameProvider, Locale paramLocale, String paramString, Object[] paramArrayOfObject)
    {
      if ((!($assertionsDisabled)) && (paramArrayOfObject.length != 0))
        throw new AssertionError();
      String[] arrayOfString = null;
      Object localObject1 = paramString;
      if (((String)localObject1).equals("GMT"))
      {
        arrayOfString = buildZoneStrings(paramTimeZoneNameProvider, paramLocale, (String)localObject1);
      }
      else
      {
        Map localMap = ZoneInfo.getAliasTable();
        if (localMap != null)
        {
          if (localMap.containsKey(localObject1))
          {
            for (Object localObject2 = localObject1; (localObject1 = (String)localMap.get(localObject1)) != null; localObject2 = localObject1);
            localObject1 = localObject2;
          }
          arrayOfString = buildZoneStrings(paramTimeZoneNameProvider, paramLocale, (String)localObject1);
          if (arrayOfString == null)
            arrayOfString = examineAliases(paramTimeZoneNameProvider, paramLocale, (String)localObject1, localMap, localMap.entrySet());
        }
      }
      if (arrayOfString != null)
        arrayOfString[0] = paramString;
      return ((String)arrayOfString);
    }

    private static String[] examineAliases(TimeZoneNameProvider paramTimeZoneNameProvider, Locale paramLocale, String paramString, Map<String, String> paramMap, Set<Map.Entry<String, String>> paramSet)
    {
      if (paramMap.containsValue(paramString))
      {
        Iterator localIterator = paramSet.iterator();
        while (localIterator.hasNext())
        {
          Map.Entry localEntry = (Map.Entry)localIterator.next();
          if (((String)localEntry.getValue()).equals(paramString))
          {
            String str = (String)localEntry.getKey();
            String[] arrayOfString = buildZoneStrings(paramTimeZoneNameProvider, paramLocale, str);
            if (arrayOfString != null)
              return arrayOfString;
            arrayOfString = examineAliases(paramTimeZoneNameProvider, paramLocale, str, paramMap, paramSet);
            if (arrayOfString != null)
              return arrayOfString;
          }
        }
      }
      return null;
    }

    private static String[] buildZoneStrings(TimeZoneNameProvider paramTimeZoneNameProvider, Locale paramLocale, String paramString)
    {
      String[] arrayOfString = new String[5];
      for (int i = 1; i <= 4; ++i)
      {
        arrayOfString[i] = paramTimeZoneNameProvider.getDisplayName(paramString, (i >= 3) ? 1 : false, i % 2, paramLocale);
        if ((i >= 3) && (arrayOfString[i] == null))
          arrayOfString[i] = arrayOfString[(i - 2)];
      }
      if (arrayOfString[1] == null)
        arrayOfString = null;
      return arrayOfString;
    }

    static
    {
      INSTANCE = new TimeZoneNameGetter();
    }
  }
}