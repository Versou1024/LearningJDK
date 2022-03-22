package sun.jdbc.odbc;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class JdbcOdbcUtils
{
  public long convertFromGMT(Date paramDate, Calendar paramCalendar)
  {
    long l = paramCalendar.getTimeZone().getRawOffset();
    return (paramDate.getTime() + l);
  }

  public long convertToGMT(Date paramDate, Calendar paramCalendar)
  {
    long l = paramCalendar.getTimeZone().getRawOffset();
    return (paramDate.getTime() - l);
  }
}