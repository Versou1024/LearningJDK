package sun.util;

import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TimeZone;
import sun.util.resources.LocaleData;

public class BuddhistCalendar extends GregorianCalendar
{
  private static final long serialVersionUID = -8527488697350388578L;
  private static final int buddhistOffset = 543;
  private transient int yearOffset = 543;

  public BuddhistCalendar()
  {
  }

  public BuddhistCalendar(TimeZone paramTimeZone)
  {
    super(paramTimeZone);
  }

  public BuddhistCalendar(Locale paramLocale)
  {
    super(paramLocale);
  }

  public BuddhistCalendar(TimeZone paramTimeZone, Locale paramLocale)
  {
    super(paramTimeZone, paramLocale);
  }

  public boolean equals(Object paramObject)
  {
    return ((paramObject instanceof BuddhistCalendar) && (super.equals(paramObject)));
  }

  public int hashCode()
  {
    return (super.hashCode() ^ 0x21F);
  }

  public int get(int paramInt)
  {
    if (paramInt == 1)
      return (super.get(paramInt) + this.yearOffset);
    return super.get(paramInt);
  }

  public void set(int paramInt1, int paramInt2)
  {
    if (paramInt1 == 1)
      super.set(paramInt1, paramInt2 - this.yearOffset);
    else
      super.set(paramInt1, paramInt2);
  }

  public void add(int paramInt1, int paramInt2)
  {
    int i = this.yearOffset;
    this.yearOffset = 0;
    try
    {
      super.add(paramInt1, paramInt2);
    }
    finally
    {
      this.yearOffset = i;
    }
  }

  public void roll(int paramInt1, int paramInt2)
  {
    int i = this.yearOffset;
    this.yearOffset = 0;
    try
    {
      super.roll(paramInt1, paramInt2);
    }
    finally
    {
      this.yearOffset = i;
    }
  }

  public String getDisplayName(int paramInt1, int paramInt2, Locale paramLocale)
  {
    if (paramInt1 != 0)
      return super.getDisplayName(paramInt1, paramInt2, paramLocale);
    if ((paramInt1 < 0) || (paramInt1 >= this.fields.length) || (paramInt2 < 1) || (paramInt2 > 2))
      throw new IllegalArgumentException();
    if (paramLocale == null)
      throw new NullPointerException();
    ResourceBundle localResourceBundle = LocaleData.getDateFormatData(paramLocale);
    String[] arrayOfString = localResourceBundle.getStringArray(getKey(paramInt2));
    return arrayOfString[get(paramInt1)];
  }

  public Map<String, Integer> getDisplayNames(int paramInt1, int paramInt2, Locale paramLocale)
  {
    if (paramInt1 != 0)
      return super.getDisplayNames(paramInt1, paramInt2, paramLocale);
    if ((paramInt1 < 0) || (paramInt1 >= this.fields.length) || (paramInt2 < 0) || (paramInt2 > 2))
      throw new IllegalArgumentException();
    if (paramLocale == null)
      throw new NullPointerException();
    if (paramInt2 == 0)
    {
      Map localMap1 = getDisplayNamesImpl(paramInt1, 1, paramLocale);
      Map localMap2 = getDisplayNamesImpl(paramInt1, 2, paramLocale);
      if (localMap1 == null)
        return localMap2;
      if (localMap2 != null)
        localMap1.putAll(localMap2);
      return localMap1;
    }
    return getDisplayNamesImpl(paramInt1, paramInt2, paramLocale);
  }

  private Map<String, Integer> getDisplayNamesImpl(int paramInt1, int paramInt2, Locale paramLocale)
  {
    ResourceBundle localResourceBundle = LocaleData.getDateFormatData(paramLocale);
    String[] arrayOfString = localResourceBundle.getStringArray(getKey(paramInt2));
    HashMap localHashMap = new HashMap(4);
    for (int i = 0; i < arrayOfString.length; ++i)
      localHashMap.put(arrayOfString[i], Integer.valueOf(i));
    return localHashMap;
  }

  private String getKey(int paramInt)
  {
    StringBuilder localStringBuilder = new StringBuilder();
    localStringBuilder.append(BuddhistCalendar.class.getName());
    if (paramInt == 1)
      localStringBuilder.append(".short");
    localStringBuilder.append(".Eras");
    return localStringBuilder.toString();
  }

  public int getActualMaximum(int paramInt)
  {
    int i = this.yearOffset;
    this.yearOffset = 0;
    try
    {
      int j = super.getActualMaximum(paramInt);
      return j;
    }
    finally
    {
      this.yearOffset = i;
    }
  }

  public String toString()
  {
    String str = super.toString();
    if (!(isSet(1)))
      return str;
    int i = str.indexOf("YEAR=");
    if (i == -1)
      return str;
    i += "YEAR=".length();
    StringBuilder localStringBuilder = new StringBuilder(str.substring(0, i));
    while (Character.isDigit(str.charAt(i++)));
    int j = internalGet(1) + 543;
    localStringBuilder.append(j).append(str.substring(i - 1));
    return localStringBuilder.toString();
  }
}