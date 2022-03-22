package sun.util.calendar;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TimeZone;
import sun.security.action.GetPropertyAction;

public class LocalGregorianCalendar extends BaseCalendar
{
  private String name;
  private Era[] eras;

  static LocalGregorianCalendar getLocalGregorianCalendar(String paramString)
  {
    Properties localProperties = null;
    try
    {
      String str1 = (String)AccessController.doPrivileged(new GetPropertyAction("java.home"));
      localObject1 = str1 + File.separator + "lib" + File.separator + "calendars.properties";
      localProperties = (Properties)AccessController.doPrivileged(new PrivilegedExceptionAction((String)localObject1)
      {
        public Object run()
          throws IOException
        {
          Properties localProperties = new Properties();
          localProperties.load(new FileInputStream(this.val$fname));
          return localProperties;
        }
      });
    }
    catch (PrivilegedActionException localPrivilegedActionException)
    {
      throw new RuntimeException(localPrivilegedActionException.getException());
    }
    String str2 = localProperties.getProperty("calendar." + paramString + ".eras");
    if (str2 == null)
      return null;
    Object localObject1 = new ArrayList();
    StringTokenizer localStringTokenizer1 = new StringTokenizer(str2, ";");
    while (localStringTokenizer1.hasMoreTokens())
    {
      localObject2 = localStringTokenizer1.nextToken().trim();
      StringTokenizer localStringTokenizer2 = new StringTokenizer((String)localObject2, ",");
      Object localObject3 = null;
      boolean bool = true;
      long l = 3412047669210644480L;
      Object localObject4 = null;
      while (localStringTokenizer2.hasMoreTokens())
      {
        localObject5 = localStringTokenizer2.nextToken();
        int i = ((String)localObject5).indexOf(61);
        if (i == -1)
          return null;
        String str3 = ((String)localObject5).substring(0, i);
        String str4 = ((String)localObject5).substring(i + 1);
        if ("name".equals(str3))
          localObject3 = str4;
        else if ("since".equals(str3))
          if (str4.endsWith("u"))
          {
            bool = false;
            l = Long.parseLong(str4.substring(0, str4.length() - 1));
          }
          else
          {
            l = Long.parseLong(str4);
          }
        else if ("abbr".equals(str3))
          localObject4 = str4;
        else
          throw new RuntimeException("Unknown key word: " + str3);
      }
      Object localObject5 = new Era(localObject3, localObject4, l, bool);
      ((List)localObject1).add(localObject5);
    }
    Object localObject2 = new Era[((List)localObject1).size()];
    ((List)localObject1).toArray(localObject2);
    return ((LocalGregorianCalendar)(LocalGregorianCalendar)(LocalGregorianCalendar)new LocalGregorianCalendar(paramString, localObject2));
  }

  private LocalGregorianCalendar(String paramString, Era[] paramArrayOfEra)
  {
    this.name = paramString;
    this.eras = paramArrayOfEra;
    setEras(paramArrayOfEra);
  }

  public String getName()
  {
    return this.name;
  }

  public Date getCalendarDate()
  {
    return getCalendarDate(System.currentTimeMillis(), newCalendarDate());
  }

  public Date getCalendarDate(long paramLong)
  {
    return getCalendarDate(paramLong, newCalendarDate());
  }

  public Date getCalendarDate(long paramLong, TimeZone paramTimeZone)
  {
    return getCalendarDate(paramLong, newCalendarDate(paramTimeZone));
  }

  public Date getCalendarDate(long paramLong, CalendarDate paramCalendarDate)
  {
    Date localDate = (Date)super.getCalendarDate(paramLong, paramCalendarDate);
    return adjustYear(localDate, paramLong, localDate.getZoneOffset());
  }

  private Date adjustYear(Date paramDate, long paramLong, int paramInt)
  {
    for (int i = this.eras.length - 1; i >= 0; --i)
    {
      Era localEra = this.eras[i];
      long l = localEra.getSince(null);
      if (localEra.isLocalTime())
        l -= paramInt;
      if (paramLong >= l)
      {
        paramDate.setLocalEra(localEra);
        int j = paramDate.getNormalizedYear() - localEra.getSinceDate().getYear() + 1;
        paramDate.setLocalYear(j);
        break;
      }
    }
    if (i < 0)
    {
      paramDate.setLocalEra(null);
      paramDate.setLocalYear(paramDate.getNormalizedYear());
    }
    paramDate.setNormalized(true);
    return paramDate;
  }

  public Date newCalendarDate()
  {
    return new Date();
  }

  public Date newCalendarDate(TimeZone paramTimeZone)
  {
    return new Date(paramTimeZone);
  }

  public boolean validate(CalendarDate paramCalendarDate)
  {
    Date localDate = (Date)paramCalendarDate;
    Era localEra = localDate.getEra();
    if (localEra != null)
    {
      if (!(validateEra(localEra)))
        return false;
      localDate.setNormalizedYear(localEra.getSinceDate().getYear() + localDate.getYear());
    }
    else
    {
      localDate.setNormalizedYear(localDate.getYear());
    }
    return super.validate(localDate);
  }

  private boolean validateEra(Era paramEra)
  {
    for (int i = 0; i < this.eras.length; ++i)
      if (paramEra == this.eras[i])
        return true;
    return false;
  }

  public boolean normalize(CalendarDate paramCalendarDate)
  {
    if (paramCalendarDate.isNormalized())
      return true;
    normalizeYear(paramCalendarDate);
    Date localDate = (Date)paramCalendarDate;
    super.normalize(localDate);
    int i = 0;
    long l1 = 3412047291253522432L;
    int j = localDate.getNormalizedYear();
    Era localEra = null;
    for (int k = this.eras.length - 1; k >= 0; --k)
    {
      localEra = this.eras[k];
      if (localEra.isLocalTime())
      {
        long l2;
        int l;
        CalendarDate localCalendarDate = localEra.getSinceDate();
        int i1 = localCalendarDate.getYear();
        if (j > i1)
          break;
        if (j == i1)
        {
          int i2 = localDate.getMonth();
          int i3 = localCalendarDate.getMonth();
          if (i2 > i3)
            break;
          if (i2 == i3)
          {
            int i4 = localDate.getDayOfMonth();
            int i5 = localCalendarDate.getDayOfMonth();
            if (i4 > i5)
              break;
            if (i4 == i5)
            {
              long l3 = localDate.getTimeOfDay();
              long l4 = localCalendarDate.getTimeOfDay();
              if (l3 >= l4)
                break;
              --k;
              break;
            }
          }
        }
      }
      else
      {
        if (i == 0)
        {
          l1 = super.getTime(paramCalendarDate);
          i = 1;
        }
        l2 = localEra.getSince(paramCalendarDate.getZone());
        if (l1 >= l2)
          break;
      }
    }
    if (k >= 0)
    {
      localDate.setLocalEra(localEra);
      l = localDate.getNormalizedYear() - localEra.getSinceDate().getYear() + 1;
      localDate.setLocalYear(l);
    }
    else
    {
      localDate.setEra(null);
      localDate.setLocalYear(j);
      localDate.setNormalizedYear(j);
    }
    localDate.setNormalized(true);
    return true;
  }

  void normalizeMonth(CalendarDate paramCalendarDate)
  {
    normalizeYear(paramCalendarDate);
    super.normalizeMonth(paramCalendarDate);
  }

  void normalizeYear(CalendarDate paramCalendarDate)
  {
    Date localDate = (Date)paramCalendarDate;
    Era localEra = localDate.getEra();
    if ((localEra == null) || (!(validateEra(localEra))))
      localDate.setNormalizedYear(localDate.getYear());
    else
      localDate.setNormalizedYear(localEra.getSinceDate().getYear() + localDate.getYear() - 1);
  }

  public boolean isLeapYear(int paramInt)
  {
    return CalendarUtils.isGregorianLeapYear(paramInt);
  }

  public boolean isLeapYear(Era paramEra, int paramInt)
  {
    if (paramEra == null)
      return isLeapYear(paramInt);
    int i = paramEra.getSinceDate().getYear() + paramInt - 1;
    return isLeapYear(i);
  }

  public void getCalendarDateFromFixedDate(CalendarDate paramCalendarDate, long paramLong)
  {
    Date localDate = (Date)paramCalendarDate;
    super.getCalendarDateFromFixedDate(localDate, paramLong);
    adjustYear(localDate, (paramLong - 719163L) * 86400000L, 0);
  }

  public static class Date extends BaseCalendar.Date
  {
    private int gregorianYear = -2147483648;

    protected Date()
    {
    }

    protected Date(TimeZone paramTimeZone)
    {
      super(paramTimeZone);
    }

    public Date setEra(Era paramEra)
    {
      if (getEra() != paramEra)
      {
        super.setEra(paramEra);
        this.gregorianYear = -2147483648;
      }
      return this;
    }

    public Date addYear(int paramInt)
    {
      super.addYear(paramInt);
      this.gregorianYear += paramInt;
      return this;
    }

    public Date setYear(int paramInt)
    {
      if (getYear() != paramInt)
      {
        super.setYear(paramInt);
        this.gregorianYear = -2147483648;
      }
      return this;
    }

    public int getNormalizedYear()
    {
      return this.gregorianYear;
    }

    public void setNormalizedYear(int paramInt)
    {
      this.gregorianYear = paramInt;
    }

    void setLocalEra(Era paramEra)
    {
      super.setEra(paramEra);
    }

    void setLocalYear(int paramInt)
    {
      super.setYear(paramInt);
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
          localStringBuffer.append(str2);
      }
      localStringBuffer.append(getYear()).append('.');
      CalendarUtils.sprintf0d(localStringBuffer, getMonth(), 2).append('.');
      CalendarUtils.sprintf0d(localStringBuffer, getDayOfMonth(), 2);
      localStringBuffer.append(str1);
      return localStringBuffer.toString();
    }
  }
}