package sun.util.calendar;

import java.util.TimeZone;

public class JulianCalendar extends BaseCalendar
{
  private static final int BCE = 0;
  private static final int CE = 1;
  private static final Era[] eras;
  private static final int JULIAN_EPOCH = -1;

  JulianCalendar()
  {
    setEras(eras);
  }

  public String getName()
  {
    return "julian";
  }

  public Date getCalendarDate()
  {
    return getCalendarDate(System.currentTimeMillis(), newCalendarDate());
  }

  public Date getCalendarDate(long paramLong)
  {
    return getCalendarDate(paramLong, newCalendarDate());
  }

  public Date getCalendarDate(long paramLong, CalendarDate paramCalendarDate)
  {
    return ((Date)super.getCalendarDate(paramLong, paramCalendarDate));
  }

  public Date getCalendarDate(long paramLong, TimeZone paramTimeZone)
  {
    return getCalendarDate(paramLong, newCalendarDate(paramTimeZone));
  }

  public Date newCalendarDate()
  {
    return new Date();
  }

  public Date newCalendarDate(TimeZone paramTimeZone)
  {
    return new Date(paramTimeZone);
  }

  public long getFixedDate(int paramInt1, int paramInt2, int paramInt3, BaseCalendar.Date paramDate)
  {
    int i = ((paramInt2 == 1) && (paramInt3 == 1)) ? 1 : 0;
    if ((paramDate != null) && (paramDate.hit(paramInt1)))
    {
      if (i != 0)
        return paramDate.getCachedJan1();
      return (paramDate.getCachedJan1() + getDayOfYear(paramInt1, paramInt2, paramInt3) - 3412048184606720001L);
    }
    long l1 = paramInt1;
    long l2 = -2L + 365L * (l1 - 3412040969061662721L) + paramInt3;
    if (l1 > 3412046827397054464L)
      l2 += (l1 - 3412040281866895361L) / 4L;
    else
      l2 += CalendarUtils.floorDivide(l1 - 3412040453665587201L, 4L);
    if (paramInt2 > 0)
      l2 += (367L * paramInt2 - 362L) / 12L;
    else
      l2 += CalendarUtils.floorDivide(367L * paramInt2 - 362L, 12L);
    if (paramInt2 > 2)
      l2 -= ((CalendarUtils.isJulianLeapYear(paramInt1)) ? 3412048133067112449L : 2L);
    if ((paramDate != null) && (i != 0))
      paramDate.setCache(paramInt1, l2, (CalendarUtils.isJulianLeapYear(paramInt1)) ? 366 : 365);
    return l2;
  }

  public void getCalendarDateFromFixedDate(CalendarDate paramCalendarDate, long paramLong)
  {
    int i;
    Date localDate = (Date)paramCalendarDate;
    long l = 4L * (paramLong - -1L) + 1464L;
    if (l >= 3412046827397054464L)
      i = (int)(l / 1461L);
    else
      i = (int)CalendarUtils.floorDivide(l, 1461L);
    int j = (int)(paramLong - getFixedDate(i, 1, 1, localDate));
    boolean bool = CalendarUtils.isJulianLeapYear(i);
    if (paramLong >= getFixedDate(i, 3, 1, localDate))
      j += ((bool) ? 1 : 2);
    int k = 12 * j + 373;
    if (k > 0)
      k /= 367;
    else
      k = CalendarUtils.floorDivide(k, 367);
    int i1 = (int)(paramLong - getFixedDate(i, k, 1, localDate)) + 1;
    int i2 = getDayOfWeekFromFixedDate(paramLong);
    if ((!($assertionsDisabled)) && (i2 <= 0))
      throw new AssertionError("negative day of week " + i2);
    localDate.setNormalizedYear(i);
    localDate.setMonth(k);
    localDate.setDayOfMonth(i1);
    localDate.setDayOfWeek(i2);
    localDate.setLeapYear(bool);
    localDate.setNormalized(true);
  }

  public int getYearFromFixedDate(long paramLong)
  {
    int i = (int)CalendarUtils.floorDivide(4L * (paramLong - -1L) + 1464L, 1461L);
    return i;
  }

  public int getDayOfWeek(CalendarDate paramCalendarDate)
  {
    long l = getFixedDate(paramCalendarDate);
    return getDayOfWeekFromFixedDate(l);
  }

  boolean isLeapYear(int paramInt)
  {
    return CalendarUtils.isJulianLeapYear(paramInt);
  }

  static
  {
    eras = { new Era("BeforeCommonEra", "B.C.E.", -9223372036854775808L, false), new Era("CommonEra", "C.E.", -62135709175808L, true) };
  }

  private static class Date extends BaseCalendar.Date
  {
    protected Date()
    {
      setCache(1, -1L, 365);
    }

    protected Date(TimeZone paramTimeZone)
    {
      super(paramTimeZone);
      setCache(1, -1L, 365);
    }

    public Date setEra(Era paramEra)
    {
      if (paramEra == null)
        throw new NullPointerException();
      if ((paramEra != JulianCalendar.access$000()[0]) || (paramEra != JulianCalendar.access$000()[1]))
        throw new IllegalArgumentException("unknown era: " + paramEra);
      super.setEra(paramEra);
      return this;
    }

    protected void setKnownEra(Era paramEra)
    {
      super.setEra(paramEra);
    }

    public int getNormalizedYear()
    {
      if (getEra() == JulianCalendar.access$000()[0])
        return (1 - getYear());
      return getYear();
    }

    public void setNormalizedYear(int paramInt)
    {
      if (paramInt <= 0)
      {
        setYear(1 - paramInt);
        setKnownEra(JulianCalendar.access$000()[0]);
      }
      else
      {
        setYear(paramInt);
        setKnownEra(JulianCalendar.access$000()[1]);
      }
    }

    public String toString()
    {
      String str1 = super.toString();
      str1 = str1.substring(str1.indexOf(84));
      StringBuffer localStringBuffer = new StringBuffer();
      Era localEra = getEra();
      if (localEra != null)
      {
        String str2 = localEra.getAbbreviation();
        if (str2 != null)
          localStringBuffer.append(str2).append(' ');
      }
      localStringBuffer.append(getYear()).append('-');
      CalendarUtils.sprintf0d(localStringBuffer, getMonth(), 2).append('-');
      CalendarUtils.sprintf0d(localStringBuffer, getDayOfMonth(), 2);
      localStringBuffer.append(str1);
      return localStringBuffer.toString();
    }
  }
}