package sun.util.resources;

import java.io.File;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.ResourceBundle.Control;
import java.util.StringTokenizer;
import sun.security.action.GetPropertyAction;
import sun.util.LocaleDataMetaInfo;

public class LocaleData
{
  private static final String localeDataJarName = "localedata.jar";

  public static Locale[] getAvailableLocales()
  {
    return ((Locale[])AvailableLocales.localeList.clone());
  }

  public static ResourceBundle getCalendarData(Locale paramLocale)
  {
    return getBundle("sun.util.resources.CalendarData", paramLocale);
  }

  public static ResourceBundle getCurrencyNames(Locale paramLocale)
  {
    return getBundle("sun.util.resources.CurrencyNames", paramLocale);
  }

  public static OpenListResourceBundle getLocaleNames(Locale paramLocale)
  {
    return ((OpenListResourceBundle)getBundle("sun.util.resources.LocaleNames", paramLocale));
  }

  public static OpenListResourceBundle getTimeZoneNames(Locale paramLocale)
  {
    return ((OpenListResourceBundle)getBundle("sun.util.resources.TimeZoneNames", paramLocale));
  }

  public static ResourceBundle getCollationData(Locale paramLocale)
  {
    return getBundle("sun.text.resources.CollationData", paramLocale);
  }

  public static ResourceBundle getDateFormatData(Locale paramLocale)
  {
    return getBundle("sun.text.resources.FormatData", paramLocale);
  }

  public static ResourceBundle getNumberFormatData(Locale paramLocale)
  {
    return getBundle("sun.text.resources.FormatData", paramLocale);
  }

  private static ResourceBundle getBundle(String paramString, Locale paramLocale)
  {
    return ((ResourceBundle)AccessController.doPrivileged(new PrivilegedAction(paramString, paramLocale)
    {
      public Object run()
      {
        return ResourceBundle.getBundle(this.val$baseName, this.val$locale, LocaleData.LocaleDataResourceBundleControl.getRBControlInstance());
      }
    }));
  }

  private static boolean isNonEuroLangSupported()
  {
    String str1 = File.separator;
    String str2 = ((String)AccessController.doPrivileged(new GetPropertyAction("java.home"))) + str1 + "lib" + str1 + "ext" + str1 + "localedata.jar";
    File localFile = new File(str2);
    boolean bool = ((Boolean)AccessController.doPrivileged(new PrivilegedAction(localFile)
    {
      public Object run()
      {
        return Boolean.valueOf(this.val$f.exists());
      }
    })).booleanValue();
    return bool;
  }

  private static Locale[] createLocaleList()
  {
    String str1 = LocaleDataMetaInfo.getSupportedLocaleString("sun.text.resources.FormatData");
    if (str1.length() == 0)
      return null;
    int i = str1.indexOf("|");
    StringTokenizer localStringTokenizer = null;
    if (isNonEuroLangSupported())
      localStringTokenizer = new StringTokenizer(str1.substring(0, i) + str1.substring(i + 1));
    else
      localStringTokenizer = new StringTokenizer(str1.substring(0, i));
    Locale[] arrayOfLocale = new Locale[localStringTokenizer.countTokens()];
    for (int j = 0; j < arrayOfLocale.length; ++j)
    {
      String str2 = localStringTokenizer.nextToken();
      int k = 0;
      int l = str2.indexOf(95);
      String str3 = "";
      String str4 = "";
      String str5 = "";
      if (l == -1)
      {
        str3 = str2;
      }
      else
      {
        str3 = str2.substring(0, l);
        k = str2.indexOf(95, l + 1);
        if (k == -1)
        {
          str4 = str2.substring(l + 1);
        }
        else
        {
          str4 = str2.substring(l + 1, k);
          if (k < str2.length())
            str5 = str2.substring(k + 1);
        }
      }
      arrayOfLocale[j] = new Locale(str3, str4, str5);
    }
    return arrayOfLocale;
  }

  private static class AvailableLocales
  {
    static final Locale[] localeList = LocaleData.access$000();
  }

  static class LocaleDataResourceBundleControl extends ResourceBundle.Control
  {
    private static LocaleDataResourceBundleControl rbControlInstance = new LocaleDataResourceBundleControl();

    public static LocaleDataResourceBundleControl getRBControlInstance()
    {
      return rbControlInstance;
    }

    public List<Locale> getCandidateLocales(String paramString, Locale paramLocale)
    {
      List localList = super.getCandidateLocales(paramString, paramLocale);
      String str1 = LocaleDataMetaInfo.getSupportedLocaleString(paramString);
      if (str1.length() == 0)
        return localList;
      Iterator localIterator = localList.iterator();
      while (localIterator.hasNext())
      {
        String str2 = ((Locale)localIterator.next()).toString();
        if ((str2.length() != 0) && (str1.indexOf(" " + str2 + " ") == -1))
          localIterator.remove();
      }
      return localList;
    }

    public Locale getFallbackLocale(String paramString, Locale paramLocale)
    {
      if ((paramString == null) || (paramLocale == null))
        throw new NullPointerException();
      return null;
    }
  }
}