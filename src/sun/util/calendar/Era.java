package sun.util.calendar;

import java.util.Locale;
import java.util.TimeZone;

public final class Era
{
  private final String name;
  private final String abbr;
  private final long since;
  private final CalendarDate sinceDate;
  private final boolean localTime;
  private int hash = 0;

  public Era(String paramString1, String paramString2, long paramLong, boolean paramBoolean)
  {
    this.name = paramString1;
    this.abbr = paramString2;
    this.since = paramLong;
    this.localTime = paramBoolean;
    Gregorian localGregorian = CalendarSystem.getGregorianCalendar();
    Gregorian.Date localDate = localGregorian.newCalendarDate(null);
    localGregorian.getCalendarDate(paramLong, localDate);
    this.sinceDate = new ImmutableGregorianDate(localDate);
  }

  public String getName()
  {
    return this.name;
  }

  public String getDisplayName(Locale paramLocale)
  {
    return this.name;
  }

  public String getAbbreviation()
  {
    return this.abbr;
  }

  public String getDiaplayAbbreviation(Locale paramLocale)
  {
    return this.abbr;
  }

  public long getSince(TimeZone paramTimeZone)
  {
    if ((paramTimeZone == null) || (!(this.localTime)))
      return this.since;
    int i = paramTimeZone.getOffset(this.since);
    return (this.since - i);
  }

  public CalendarDate getSinceDate()
  {
    return this.sinceDate;
  }

  public boolean isLocalTime()
  {
    return this.localTime;
  }

  public boolean equals(Object paramObject)
  {
    if (!(paramObject instanceof Era))
      return false;
    Era localEra = (Era)paramObject;
    return ((this.name.equals(localEra.name)) && (this.abbr.equals(localEra.abbr)) && (this.since == localEra.since) && (this.localTime == localEra.localTime));
  }

  public int hashCode()
  {
    if (this.hash == 0)
      this.hash = (this.name.hashCode() ^ this.abbr.hashCode() ^ (int)this.since ^ (int)(this.since >> 32) ^ ((this.localTime) ? 1 : 0));
    return this.hash;
  }

  public String toString()
  {
    StringBuilder localStringBuilder = new StringBuilder();
    localStringBuilder.append('[');
    localStringBuilder.append(getName()).append(" (");
    localStringBuilder.append(getAbbreviation()).append(')');
    localStringBuilder.append(" since ").append(getSinceDate());
    if (this.localTime)
    {
      localStringBuilder.setLength(localStringBuilder.length() - 1);
      localStringBuilder.append(" local time");
    }
    localStringBuilder.append(']');
    return localStringBuilder.toString();
  }
}