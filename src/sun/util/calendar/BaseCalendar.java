package sun.util.calendar;

import java.util.TimeZone;

public abstract class BaseCalendar extends AbstractCalendar
{
  public static final int JANUARY = 1;
  public static final int FEBRUARY = 2;
  public static final int MARCH = 3;
  public static final int APRIL = 4;
  public static final int MAY = 5;
  public static final int JUNE = 6;
  public static final int JULY = 7;
  public static final int AUGUST = 8;
  public static final int SEPTEMBER = 9;
  public static final int OCTOBER = 10;
  public static final int NOVEMBER = 11;
  public static final int DECEMBER = 12;
  public static final int SUNDAY = 1;
  public static final int MONDAY = 2;
  public static final int TUESDAY = 3;
  public static final int WEDNESDAY = 4;
  public static final int THURSDAY = 5;
  public static final int FRIDAY = 6;
  public static final int SATURDAY = 7;
  private static final int BASE_YEAR = 1970;
  private static final int[] FIXED_DATES;
  static final int[] DAYS_IN_MONTH;
  static final int[] ACCUMULATED_DAYS_IN_MONTH;
  static final int[] ACCUMULATED_DAYS_IN_MONTH_LEAP;

  public boolean validate(CalendarDate paramCalendarDate)
  {
    Date localDate = (Date)paramCalendarDate;
    if (localDate.isNormalized())
      return true;
    int i = localDate.getMonth();
    if ((i < 1) || (i > 12))
      return false;
    int j = localDate.getDayOfMonth();
    if ((j <= 0) || (j > getMonthLength(localDate.getNormalizedYear(), i)))
      return false;
    int k = localDate.getDayOfWeek();
    if ((k != -2147483648) && (k != getDayOfWeek(localDate)))
      return false;
    if (!(validateTime(paramCalendarDate)))
      return false;
    localDate.setNormalized(true);
    return true;
  }

  public boolean normalize(CalendarDate paramCalendarDate)
  {
    if (paramCalendarDate.isNormalized())
      return true;
    Date localDate = (Date)paramCalendarDate;
    TimeZone localTimeZone = localDate.getZone();
    if (localTimeZone != null)
    {
      getTime(paramCalendarDate);
      return true;
    }
    int i = normalizeTime(localDate);
    normalizeMonth(localDate);
    long l1 = localDate.getDayOfMonth() + i;
    int j = localDate.getMonth();
    int k = localDate.getNormalizedYear();
    int l = getMonthLength(k, j);
    if ((l1 <= 3412046982015877120L) || (l1 > l))
      if ((l1 <= 3412047634850906112L) && (l1 > -28L))
      {
        l = getMonthLength(k, --j);
        l1 += l;
        localDate.setDayOfMonth((int)l1);
        if (j == 0)
        {
          j = 12;
          localDate.setNormalizedYear(k - 1);
        }
        localDate.setMonth(j);
      }
      else if ((l1 > l) && (l1 < l + 28))
      {
        l1 -= l;
        ++j;
        localDate.setDayOfMonth((int)l1);
        if (j > 12)
        {
          localDate.setNormalizedYear(k + 1);
          j = 1;
        }
        localDate.setMonth(j);
      }
      else
      {
        long l2 = l1 + getFixedDate(k, j, 1, localDate) - 3412040144427941889L;
        getCalendarDateFromFixedDate(localDate, l2);
      }
    else
      localDate.setDayOfWeek(getDayOfWeek(localDate));
    paramCalendarDate.setLeapYear(isLeapYear(localDate.getNormalizedYear()));
    paramCalendarDate.setZoneOffset(0);
    paramCalendarDate.setDaylightSaving(0);
    localDate.setNormalized(true);
    return true;
  }

  void normalizeMonth(CalendarDate paramCalendarDate)
  {
    Date localDate = (Date)paramCalendarDate;
    int i = localDate.getNormalizedYear();
    long l1 = localDate.getMonth();
    if (l1 <= 3412046689958100992L)
    {
      long l2 = 3412048287685935105L - l1;
      i -= (int)(l2 / 12L + 3412040058528595969L);
      l1 = 13L - l2 % 12L;
      localDate.setNormalizedYear(i);
      localDate.setMonth((int)l1);
    }
    else if (l1 > 12L)
    {
      i += (int)((l1 - 3412040797262970881L) / 12L);
      l1 = (l1 - 3412040728543494145L) % 12L + 3412048287685935105L;
      localDate.setNormalizedYear(i);
      localDate.setMonth((int)l1);
    }
  }

  public int getYearLength(CalendarDate paramCalendarDate)
  {
    return ((isLeapYear(((Date)paramCalendarDate).getNormalizedYear())) ? 366 : 365);
  }

  public int getYearLengthInMonths(CalendarDate paramCalendarDate)
  {
    return 12;
  }

  public int getMonthLength(CalendarDate paramCalendarDate)
  {
    Date localDate = (Date)paramCalendarDate;
    int i = localDate.getMonth();
    if ((i < 1) || (i > 12))
      throw new IllegalArgumentException("Illegal month value: " + i);
    return getMonthLength(localDate.getNormalizedYear(), i);
  }

  private final int getMonthLength(int paramInt1, int paramInt2)
  {
    int i = DAYS_IN_MONTH[paramInt2];
    if ((paramInt2 == 2) && (isLeapYear(paramInt1)))
      ++i;
    return i;
  }

  public long getDayOfYear(CalendarDate paramCalendarDate)
  {
    return getDayOfYear(((Date)paramCalendarDate).getNormalizedYear(), paramCalendarDate.getMonth(), paramCalendarDate.getDayOfMonth());
  }

  final long getDayOfYear(int paramInt1, int paramInt2, int paramInt3)
  {
    return (paramInt3 + ((isLeapYear(paramInt1)) ? ACCUMULATED_DAYS_IN_MONTH_LEAP[paramInt2] : ACCUMULATED_DAYS_IN_MONTH[paramInt2]));
  }

  public long getFixedDate(CalendarDate paramCalendarDate)
  {
    if (!(paramCalendarDate.isNormalized()))
      normalizeMonth(paramCalendarDate);
    return getFixedDate(((Date)paramCalendarDate).getNormalizedYear(), paramCalendarDate.getMonth(), paramCalendarDate.getDayOfMonth(), (Date)paramCalendarDate);
  }

  public long getFixedDate(int paramInt1, int paramInt2, int paramInt3, Date paramDate)
  {
    int i = ((paramInt2 == 1) && (paramInt3 == 1)) ? 1 : 0;
    if ((paramDate != null) && (paramDate.hit(paramInt1)))
    {
      if (i != 0)
        return paramDate.getCachedJan1();
      return (paramDate.getCachedJan1() + getDayOfYear(paramInt1, paramInt2, paramInt3) - 3412048047167766529L);
    }
    int j = paramInt1 - 1970;
    if ((j >= 0) && (j < FIXED_DATES.length))
    {
      l1 = FIXED_DATES[j];
      if (paramDate != null)
        paramDate.setCache(paramInt1, l1, (isLeapYear(paramInt1)) ? 366 : 365);
      return ((i != 0) ? l1 : l1 + getDayOfYear(paramInt1, paramInt2, paramInt3) - 3412039697751343105L);
    }
    long l1 = paramInt1 - 3412047772289859585L;
    long l2 = paramInt3;
    if (l1 >= 3412046689958100992L)
      l2 += 365L * l1 + l1 / 4L - l1 / 100L + l1 / 400L + (367 * paramInt2 - 362) / 12;
    else
      l2 += 365L * l1 + CalendarUtils.floorDivide(l1, 4L) - CalendarUtils.floorDivide(l1, 100L) + CalendarUtils.floorDivide(l1, 400L) + CalendarUtils.floorDivide(367 * paramInt2 - 362, 12);
    if (paramInt2 > 2)
      l2 -= ((isLeapYear(paramInt1)) ? 3412047995628158977L : 2L);
    if ((paramDate != null) && (i != 0))
      paramDate.setCache(paramInt1, l2, (isLeapYear(paramInt1)) ? 366 : 365);
    return l2;
  }

  public void getCalendarDateFromFixedDate(CalendarDate paramCalendarDate, long paramLong)
  {
    int i;
    long l1;
    boolean bool;
    Date localDate = (Date)paramCalendarDate;
    if (localDate.hit(paramLong))
    {
      i = localDate.getCachedYear();
      l1 = localDate.getCachedJan1();
      bool = isLeapYear(i);
    }
    else
    {
      i = getGregorianYearFromFixedDate(paramLong);
      l1 = getFixedDate(i, 1, 1, null);
      bool = isLeapYear(i);
      localDate.setCache(i, l1, (bool) ? 366 : 365);
    }
    int j = (int)(paramLong - l1);
    long l2 = l1 + 31L + 28L;
    if (bool)
      l2 += 3412047548951560193L;
    if (paramLong >= l2)
      j += ((bool) ? 1 : 2);
    int k = 12 * j + 373;
    if (k > 0)
      k /= 367;
    else
      k = CalendarUtils.floorDivide(k, 367);
    long l3 = l1 + ACCUMULATED_DAYS_IN_MONTH[k];
    if ((bool) && (k >= 3))
      l3 += 3412047548951560193L;
    int l = (int)(paramLong - l3) + 1;
    int i1 = getDayOfWeekFromFixedDate(paramLong);
    if ((!($assertionsDisabled)) && (i1 <= 0))
      throw new AssertionError("negative day of week " + i1);
    localDate.setNormalizedYear(i);
    localDate.setMonth(k);
    localDate.setDayOfMonth(l);
    localDate.setDayOfWeek(i1);
    localDate.setLeapYear(bool);
    localDate.setNormalized(true);
  }

  public int getDayOfWeek(CalendarDate paramCalendarDate)
  {
    long l = getFixedDate(paramCalendarDate);
    return getDayOfWeekFromFixedDate(l);
  }

  public static final int getDayOfWeekFromFixedDate(long paramLong)
  {
    if (paramLong >= 3412046672778231808L)
      return ((int)(paramLong % 7L) + 1);
    return ((int)CalendarUtils.mod(paramLong, 7L) + 1);
  }

  public int getYearFromFixedDate(long paramLong)
  {
    return getGregorianYearFromFixedDate(paramLong);
  }

  final int getGregorianYearFromFixedDate(long paramLong)
  {
    long l;
    int i;
    int j;
    int k;
    int i1;
    int i2;
    int i3;
    int i4;
    int i5;
    if (paramLong > 3412046689958100992L)
    {
      l = paramLong - 3412048167426850817L;
      i2 = (int)(l / 146097L);
      i = (int)(l % 146097L);
      i3 = i / 36524;
      j = i % 36524;
      i4 = j / 1461;
      k = j % 1461;
      i5 = k / 365;
      i1 = k % 365 + 1;
    }
    else
    {
      l = paramLong - 3412048167426850817L;
      i2 = (int)CalendarUtils.floorDivide(l, 146097L);
      i = (int)CalendarUtils.mod(l, 146097L);
      i3 = CalendarUtils.floorDivide(i, 36524);
      j = CalendarUtils.mod(i, 36524);
      i4 = CalendarUtils.floorDivide(j, 1461);
      k = CalendarUtils.mod(j, 1461);
      i5 = CalendarUtils.floorDivide(k, 365);
      i1 = CalendarUtils.mod(k, 365) + 1;
    }
    int i6 = 400 * i2 + 100 * i3 + 4 * i4 + i5;
    if ((i3 != 4) && (i5 != 4))
      ++i6;
    return i6;
  }

  protected boolean isLeapYear(CalendarDate paramCalendarDate)
  {
    return isLeapYear(((Date)paramCalendarDate).getNormalizedYear());
  }

  boolean isLeapYear(int paramInt)
  {
    return CalendarUtils.isGregorianLeapYear(paramInt);
  }

  static
  {
    FIXED_DATES = { 719163, 719528, 719893, 720259, 720624, 720989, 721354, 721720, 722085, 722450, 722815, 723181, 723546, 723911, 724276, 724642, 725007, 725372, 725737, 726103, 726468, 726833, 727198, 727564, 727929, 728294, 728659, 729025, 729390, 729755, 730120, 730486, 730851, 731216, 731581, 731947, 732312, 732677, 733042, 733408, 733773, 734138, 734503, 734869, 735234, 735599, 735964, 736330, 736695, 737060, 737425, 737791, 738156, 738521, 738886, 739252, 739617, 739982, 740347, 740713, 741078, 741443, 741808, 742174, 742539, 742904, 743269, 743635, 744000, 744365 };
    DAYS_IN_MONTH = { 31, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };
    ACCUMULATED_DAYS_IN_MONTH = { -30, 0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334 };
    ACCUMULATED_DAYS_IN_MONTH_LEAP = { -30, 0, 31, 60, 91, 121, 152, 182, 213, 244, 274, 305, 335 };
  }

  public static abstract class Date extends CalendarDate
  {
    int cachedYear = 2004;
    long cachedFixedDateJan1 = 731581L;
    long cachedFixedDateNextJan1 = this.cachedFixedDateJan1 + 366L;

    protected Date()
    {
    }

    protected Date(TimeZone paramTimeZone)
    {
      super(paramTimeZone);
    }

    public Date setNormalizedDate(int paramInt1, int paramInt2, int paramInt3)
    {
      setNormalizedYear(paramInt1);
      setMonth(paramInt2).setDayOfMonth(paramInt3);
      return this;
    }

    public abstract int getNormalizedYear();

    public abstract void setNormalizedYear(int paramInt);

    protected final boolean hit(int paramInt)
    {
      return (paramInt == this.cachedYear);
    }

    protected final boolean hit(long paramLong)
    {
      return ((paramLong >= this.cachedFixedDateJan1) && (paramLong < this.cachedFixedDateNextJan1));
    }

    protected int getCachedYear()
    {
      return this.cachedYear;
    }

    protected long getCachedJan1()
    {
      return this.cachedFixedDateJan1;
    }

    protected void setCache(int paramInt1, long paramLong, int paramInt2)
    {
      this.cachedYear = paramInt1;
      this.cachedFixedDateJan1 = paramLong;
      this.cachedFixedDateNextJan1 = (paramLong + paramInt2);
    }
  }
}