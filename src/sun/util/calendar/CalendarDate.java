package sun.util.calendar;

import java.util.Locale;
import java.util.TimeZone;

public abstract class CalendarDate
  implements Cloneable
{
  public static final int FIELD_UNDEFINED = -2147483648;
  public static final long TIME_UNDEFINED = -9223372036854775808L;
  private Era era;
  private int year;
  private int month;
  private int dayOfMonth;
  private int dayOfWeek;
  private boolean leapYear;
  private int hours;
  private int minutes;
  private int seconds;
  private int millis;
  private long fraction;
  private boolean normalized;
  private TimeZone zoneinfo;
  private int zoneOffset;
  private int daylightSaving;
  private boolean forceStandardTime;
  private Locale locale;

  protected CalendarDate()
  {
    this(TimeZone.getDefault());
  }

  protected CalendarDate(TimeZone paramTimeZone)
  {
    this.dayOfWeek = -2147483648;
    this.zoneinfo = paramTimeZone;
  }

  public Era getEra()
  {
    return this.era;
  }

  public CalendarDate setEra(Era paramEra)
  {
    if (this.era == paramEra)
      return this;
    this.era = paramEra;
    this.normalized = false;
    return this;
  }

  public int getYear()
  {
    return this.year;
  }

  public CalendarDate setYear(int paramInt)
  {
    if (this.year != paramInt)
    {
      this.year = paramInt;
      this.normalized = false;
    }
    return this;
  }

  public CalendarDate addYear(int paramInt)
  {
    if (paramInt != 0)
    {
      this.year += paramInt;
      this.normalized = false;
    }
    return this;
  }

  public boolean isLeapYear()
  {
    return this.leapYear;
  }

  void setLeapYear(boolean paramBoolean)
  {
    this.leapYear = paramBoolean;
  }

  public int getMonth()
  {
    return this.month;
  }

  public CalendarDate setMonth(int paramInt)
  {
    if (this.month != paramInt)
    {
      this.month = paramInt;
      this.normalized = false;
    }
    return this;
  }

  public CalendarDate addMonth(int paramInt)
  {
    if (paramInt != 0)
    {
      this.month += paramInt;
      this.normalized = false;
    }
    return this;
  }

  public int getDayOfMonth()
  {
    return this.dayOfMonth;
  }

  public CalendarDate setDayOfMonth(int paramInt)
  {
    if (this.dayOfMonth != paramInt)
    {
      this.dayOfMonth = paramInt;
      this.normalized = false;
    }
    return this;
  }

  public CalendarDate addDayOfMonth(int paramInt)
  {
    if (paramInt != 0)
    {
      this.dayOfMonth += paramInt;
      this.normalized = false;
    }
    return this;
  }

  public int getDayOfWeek()
  {
    if (!(isNormalized()))
      this.dayOfWeek = -2147483648;
    return this.dayOfWeek;
  }

  public int getHours()
  {
    return this.hours;
  }

  public CalendarDate setHours(int paramInt)
  {
    if (this.hours != paramInt)
    {
      this.hours = paramInt;
      this.normalized = false;
    }
    return this;
  }

  public CalendarDate addHours(int paramInt)
  {
    if (paramInt != 0)
    {
      this.hours += paramInt;
      this.normalized = false;
    }
    return this;
  }

  public int getMinutes()
  {
    return this.minutes;
  }

  public CalendarDate setMinutes(int paramInt)
  {
    if (this.minutes != paramInt)
    {
      this.minutes = paramInt;
      this.normalized = false;
    }
    return this;
  }

  public CalendarDate addMinutes(int paramInt)
  {
    if (paramInt != 0)
    {
      this.minutes += paramInt;
      this.normalized = false;
    }
    return this;
  }

  public int getSeconds()
  {
    return this.seconds;
  }

  public CalendarDate setSeconds(int paramInt)
  {
    if (this.seconds != paramInt)
    {
      this.seconds = paramInt;
      this.normalized = false;
    }
    return this;
  }

  public CalendarDate addSeconds(int paramInt)
  {
    if (paramInt != 0)
    {
      this.seconds += paramInt;
      this.normalized = false;
    }
    return this;
  }

  public int getMillis()
  {
    return this.millis;
  }

  public CalendarDate setMillis(int paramInt)
  {
    if (this.millis != paramInt)
    {
      this.millis = paramInt;
      this.normalized = false;
    }
    return this;
  }

  public CalendarDate addMillis(int paramInt)
  {
    if (paramInt != 0)
    {
      this.millis += paramInt;
      this.normalized = false;
    }
    return this;
  }

  public long getTimeOfDay()
  {
    if (!(isNormalized()))
      return (this.fraction = -9223372036854775808L);
    return this.fraction;
  }

  public CalendarDate setDate(int paramInt1, int paramInt2, int paramInt3)
  {
    setYear(paramInt1);
    setMonth(paramInt2);
    setDayOfMonth(paramInt3);
    return this;
  }

  public CalendarDate addDate(int paramInt1, int paramInt2, int paramInt3)
  {
    addYear(paramInt1);
    addMonth(paramInt2);
    addDayOfMonth(paramInt3);
    return this;
  }

  public CalendarDate setTimeOfDay(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    setHours(paramInt1);
    setMinutes(paramInt2);
    setSeconds(paramInt3);
    setMillis(paramInt4);
    return this;
  }

  public CalendarDate addTimeOfDay(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    addHours(paramInt1);
    addMinutes(paramInt2);
    addSeconds(paramInt3);
    addMillis(paramInt4);
    return this;
  }

  protected void setTimeOfDay(long paramLong)
  {
    this.fraction = paramLong;
  }

  public boolean isNormalized()
  {
    return this.normalized;
  }

  public boolean isStandardTime()
  {
    return this.forceStandardTime;
  }

  public void setStandardTime(boolean paramBoolean)
  {
    this.forceStandardTime = paramBoolean;
  }

  public boolean isDaylightTime()
  {
    if (isStandardTime())
      return false;
    return (this.daylightSaving != 0);
  }

  protected void setLocale(Locale paramLocale)
  {
    this.locale = paramLocale;
  }

  public TimeZone getZone()
  {
    return this.zoneinfo;
  }

  public CalendarDate setZone(TimeZone paramTimeZone)
  {
    this.zoneinfo = paramTimeZone;
    return this;
  }

  public boolean isSameDate(CalendarDate paramCalendarDate)
  {
    return ((getDayOfWeek() == paramCalendarDate.getDayOfWeek()) && (getMonth() == paramCalendarDate.getMonth()) && (getYear() == paramCalendarDate.getYear()) && (getEra() == paramCalendarDate.getEra()));
  }

  public boolean equals(Object paramObject)
  {
    if (!(paramObject instanceof CalendarDate))
      return false;
    CalendarDate localCalendarDate = (CalendarDate)paramObject;
    if (isNormalized() != localCalendarDate.isNormalized())
      return false;
    int i = (this.zoneinfo != null) ? 1 : 0;
    int j = (localCalendarDate.zoneinfo != null) ? 1 : 0;
    if (i != j)
      return false;
    if ((i != 0) && (!(this.zoneinfo.equals(localCalendarDate.zoneinfo))))
      return false;
    return ((getEra() == localCalendarDate.getEra()) && (this.year == localCalendarDate.year) && (this.month == localCalendarDate.month) && (this.dayOfMonth == localCalendarDate.dayOfMonth) && (this.hours == localCalendarDate.hours) && (this.minutes == localCalendarDate.minutes) && (this.seconds == localCalendarDate.seconds) && (this.millis == localCalendarDate.millis) && (this.zoneOffset == localCalendarDate.zoneOffset));
  }

  public int hashCode()
  {
    long l = (((this.year - 1970L) * 12L + this.month - 1) * 30L + this.dayOfMonth) * 24L;
    l = (((l + this.hours) * 60L + this.minutes) * 60L + this.seconds) * 1000L + this.millis;
    l -= this.zoneOffset;
    int i = (isNormalized()) ? 1 : 0;
    int j = 0;
    Era localEra = getEra();
    if (localEra != null)
      j = localEra.hashCode();
    int k = (this.zoneinfo != null) ? this.zoneinfo.hashCode() : 0;
    return ((int)l * (int)(l >> 32) ^ j ^ i ^ k);
  }

  public Object clone()
  {
    try
    {
      return super.clone();
    }
    catch (CloneNotSupportedException localCloneNotSupportedException)
    {
      throw new InternalError();
    }
  }

  public String toString()
  {
    StringBuilder localStringBuilder = new StringBuilder();
    CalendarUtils.sprintf0d(localStringBuilder, this.year, 4).append('-');
    CalendarUtils.sprintf0d(localStringBuilder, this.month, 2).append('-');
    CalendarUtils.sprintf0d(localStringBuilder, this.dayOfMonth, 2).append('T');
    CalendarUtils.sprintf0d(localStringBuilder, this.hours, 2).append(':');
    CalendarUtils.sprintf0d(localStringBuilder, this.minutes, 2).append(':');
    CalendarUtils.sprintf0d(localStringBuilder, this.seconds, 2).append('.');
    CalendarUtils.sprintf0d(localStringBuilder, this.millis, 3);
    if (this.zoneOffset == 0)
    {
      localStringBuilder.append('Z');
    }
    else if (this.zoneOffset != -2147483648)
    {
      int i;
      char c;
      if (this.zoneOffset > 0)
      {
        i = this.zoneOffset;
        c = '+';
      }
      else
      {
        i = -this.zoneOffset;
        c = '-';
      }
      i /= 60000;
      localStringBuilder.append(c);
      CalendarUtils.sprintf0d(localStringBuilder, i / 60, 2);
      CalendarUtils.sprintf0d(localStringBuilder, i % 60, 2);
    }
    else
    {
      localStringBuilder.append(" local time");
    }
    return localStringBuilder.toString();
  }

  protected void setDayOfWeek(int paramInt)
  {
    this.dayOfWeek = paramInt;
  }

  protected void setNormalized(boolean paramBoolean)
  {
    this.normalized = paramBoolean;
  }

  public int getZoneOffset()
  {
    return this.zoneOffset;
  }

  protected void setZoneOffset(int paramInt)
  {
    this.zoneOffset = paramInt;
  }

  public int getDaylightSaving()
  {
    return this.daylightSaving;
  }

  protected void setDaylightSaving(int paramInt)
  {
    this.daylightSaving = paramInt;
  }
}