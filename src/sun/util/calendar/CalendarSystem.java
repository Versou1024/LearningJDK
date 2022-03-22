package sun.util.calendar;

import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public abstract class CalendarSystem
{
  private static volatile boolean initialized = false;
  private static ConcurrentMap<String, String> names;
  private static ConcurrentMap<String, CalendarSystem> calendars;
  private static final String PACKAGE_NAME = "sun.util.calendar.";
  private static final String[] namePairs = { "gregorian", "Gregorian", "japanese", "LocalGregorianCalendar", "julian", "JulianCalendar" };
  private static final Gregorian GREGORIAN_INSTANCE = new Gregorian();

  private static void initNames()
  {
    ConcurrentHashMap localConcurrentHashMap = new ConcurrentHashMap();
    StringBuilder localStringBuilder = new StringBuilder();
    for (int i = 0; i < namePairs.length; i += 2)
    {
      localStringBuilder.setLength(0);
      String str = "sun.util.calendar." + namePairs[(i + 1)];
      localConcurrentHashMap.put(namePairs[i], str);
    }
    synchronized (CalendarSystem.class)
    {
      if (!(initialized))
      {
        names = localConcurrentHashMap;
        calendars = new ConcurrentHashMap();
        initialized = true;
      }
    }
  }

  public static Gregorian getGregorianCalendar()
  {
    return GREGORIAN_INSTANCE;
  }

  public static CalendarSystem forName(String paramString)
  {
    if ("gregorian".equals(paramString))
      return GREGORIAN_INSTANCE;
    if (!(initialized))
      initNames();
    java.lang.Object localObject = (CalendarSystem)calendars.get(paramString);
    if (localObject != null)
      return localObject;
    String str = (String)names.get(paramString);
    if (str == null)
      return null;
    if (str.endsWith("LocalGregorianCalendar"))
      localObject = LocalGregorianCalendar.getLocalGregorianCalendar(paramString);
    else
      try
      {
        Class localClass = Class.forName(str);
        localObject = (CalendarSystem)localClass.newInstance();
      }
      catch (Exception localException)
      {
        throw new RuntimeException("internal error", localException);
      }
    if (localObject == null)
      return null;
    CalendarSystem localCalendarSystem = (CalendarSystem)calendars.putIfAbsent(paramString, localObject);
    return ((localCalendarSystem == null) ? localObject : (CalendarSystem)localCalendarSystem);
  }

  public abstract String getName();

  public abstract CalendarDate getCalendarDate();

  public abstract CalendarDate getCalendarDate(long paramLong);

  public abstract CalendarDate getCalendarDate(long paramLong, CalendarDate paramCalendarDate);

  public abstract CalendarDate getCalendarDate(long paramLong, TimeZone paramTimeZone);

  public abstract CalendarDate newCalendarDate();

  public abstract CalendarDate newCalendarDate(TimeZone paramTimeZone);

  public abstract long getTime(CalendarDate paramCalendarDate);

  public abstract int getYearLength(CalendarDate paramCalendarDate);

  public abstract int getYearLengthInMonths(CalendarDate paramCalendarDate);

  public abstract int getMonthLength(CalendarDate paramCalendarDate);

  public abstract int getWeekLength();

  public abstract Era getEra(String paramString);

  public abstract Era[] getEras();

  public abstract void setEra(CalendarDate paramCalendarDate, String paramString);

  public abstract CalendarDate getNthDayOfWeek(int paramInt1, int paramInt2, CalendarDate paramCalendarDate);

  public abstract CalendarDate setTimeOfDay(CalendarDate paramCalendarDate, int paramInt);

  public abstract boolean validate(CalendarDate paramCalendarDate);

  public abstract boolean normalize(CalendarDate paramCalendarDate);
}