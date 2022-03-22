package sun.org.mozilla.javascript.internal;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

final class NativeDate extends IdScriptableObject
{
  static final long serialVersionUID = -8307438915861678966L;
  private static final Object DATE_TAG = new Object();
  private static final String js_NaN_date_str = "Invalid Date";
  private static final double HalfTimeDomain = 8640000000000000.0D;
  private static final double HoursPerDay = 24.0D;
  private static final double MinutesPerHour = 60.0D;
  private static final double SecondsPerMinute = 60.0D;
  private static final double msPerSecond = 1000.0D;
  private static final double MinutesPerDay = 1440.0D;
  private static final double SecondsPerDay = 86400.0D;
  private static final double SecondsPerHour = 3600.0D;
  private static final double msPerDay = 86400000.0D;
  private static final double msPerHour = 3600000.0D;
  private static final double msPerMinute = 60000.0D;
  private static final boolean TZO_WORKAROUND = 0;
  private static final int MAXARGS = 7;
  private static final int ConstructorId_now = -3;
  private static final int ConstructorId_parse = -2;
  private static final int ConstructorId_UTC = -1;
  private static final int Id_constructor = 1;
  private static final int Id_toString = 2;
  private static final int Id_toTimeString = 3;
  private static final int Id_toDateString = 4;
  private static final int Id_toLocaleString = 5;
  private static final int Id_toLocaleTimeString = 6;
  private static final int Id_toLocaleDateString = 7;
  private static final int Id_toUTCString = 8;
  private static final int Id_toSource = 9;
  private static final int Id_valueOf = 10;
  private static final int Id_getTime = 11;
  private static final int Id_getYear = 12;
  private static final int Id_getFullYear = 13;
  private static final int Id_getUTCFullYear = 14;
  private static final int Id_getMonth = 15;
  private static final int Id_getUTCMonth = 16;
  private static final int Id_getDate = 17;
  private static final int Id_getUTCDate = 18;
  private static final int Id_getDay = 19;
  private static final int Id_getUTCDay = 20;
  private static final int Id_getHours = 21;
  private static final int Id_getUTCHours = 22;
  private static final int Id_getMinutes = 23;
  private static final int Id_getUTCMinutes = 24;
  private static final int Id_getSeconds = 25;
  private static final int Id_getUTCSeconds = 26;
  private static final int Id_getMilliseconds = 27;
  private static final int Id_getUTCMilliseconds = 28;
  private static final int Id_getTimezoneOffset = 29;
  private static final int Id_setTime = 30;
  private static final int Id_setMilliseconds = 31;
  private static final int Id_setUTCMilliseconds = 32;
  private static final int Id_setSeconds = 33;
  private static final int Id_setUTCSeconds = 34;
  private static final int Id_setMinutes = 35;
  private static final int Id_setUTCMinutes = 36;
  private static final int Id_setHours = 37;
  private static final int Id_setUTCHours = 38;
  private static final int Id_setDate = 39;
  private static final int Id_setUTCDate = 40;
  private static final int Id_setMonth = 41;
  private static final int Id_setUTCMonth = 42;
  private static final int Id_setFullYear = 43;
  private static final int Id_setUTCFullYear = 44;
  private static final int Id_setYear = 45;
  private static final int MAX_PROTOTYPE_ID = 45;
  private static final int Id_toGMTString = 8;
  private static DateFormat timeZoneFormatter;
  private static DateFormat localeDateTimeFormatter;
  private static DateFormat localeDateFormatter;
  private static DateFormat localeTimeFormatter;
  private double date;

  static void init(Context paramContext, Scriptable paramScriptable, boolean paramBoolean)
  {
    NativeDate localNativeDate = new NativeDate(paramContext);
    localNativeDate.date = ScriptRuntime.NaN;
    localNativeDate.exportAsJSClass(45, paramScriptable, paramBoolean);
  }

  private NativeDate(Context paramContext)
  {
    paramContext.setTimeZone();
    paramContext.setLocalTZA();
  }

  public String getClassName()
  {
    return "Date";
  }

  public Object getDefaultValue(Class paramClass)
  {
    if (paramClass == null)
      paramClass = ScriptRuntime.StringClass;
    return super.getDefaultValue(paramClass);
  }

  double getJSTimeValue()
  {
    return this.date;
  }

  protected void fillConstructorProperties(IdFunctionObject paramIdFunctionObject)
  {
    addIdFunctionProperty(paramIdFunctionObject, DATE_TAG, -3, "now", 0);
    addIdFunctionProperty(paramIdFunctionObject, DATE_TAG, -2, "parse", 1);
    addIdFunctionProperty(paramIdFunctionObject, DATE_TAG, -1, "UTC", 1);
    super.fillConstructorProperties(paramIdFunctionObject);
  }

  protected void initPrototypeId(int paramInt)
  {
    String str;
    int i;
    switch (paramInt)
    {
    case 1:
      i = 1;
      str = "constructor";
      break;
    case 2:
      i = 0;
      str = "toString";
      break;
    case 3:
      i = 0;
      str = "toTimeString";
      break;
    case 4:
      i = 0;
      str = "toDateString";
      break;
    case 5:
      i = 0;
      str = "toLocaleString";
      break;
    case 6:
      i = 0;
      str = "toLocaleTimeString";
      break;
    case 7:
      i = 0;
      str = "toLocaleDateString";
      break;
    case 8:
      i = 0;
      str = "toUTCString";
      break;
    case 9:
      i = 0;
      str = "toSource";
      break;
    case 10:
      i = 0;
      str = "valueOf";
      break;
    case 11:
      i = 0;
      str = "getTime";
      break;
    case 12:
      i = 0;
      str = "getYear";
      break;
    case 13:
      i = 0;
      str = "getFullYear";
      break;
    case 14:
      i = 0;
      str = "getUTCFullYear";
      break;
    case 15:
      i = 0;
      str = "getMonth";
      break;
    case 16:
      i = 0;
      str = "getUTCMonth";
      break;
    case 17:
      i = 0;
      str = "getDate";
      break;
    case 18:
      i = 0;
      str = "getUTCDate";
      break;
    case 19:
      i = 0;
      str = "getDay";
      break;
    case 20:
      i = 0;
      str = "getUTCDay";
      break;
    case 21:
      i = 0;
      str = "getHours";
      break;
    case 22:
      i = 0;
      str = "getUTCHours";
      break;
    case 23:
      i = 0;
      str = "getMinutes";
      break;
    case 24:
      i = 0;
      str = "getUTCMinutes";
      break;
    case 25:
      i = 0;
      str = "getSeconds";
      break;
    case 26:
      i = 0;
      str = "getUTCSeconds";
      break;
    case 27:
      i = 0;
      str = "getMilliseconds";
      break;
    case 28:
      i = 0;
      str = "getUTCMilliseconds";
      break;
    case 29:
      i = 0;
      str = "getTimezoneOffset";
      break;
    case 30:
      i = 1;
      str = "setTime";
      break;
    case 31:
      i = 1;
      str = "setMilliseconds";
      break;
    case 32:
      i = 1;
      str = "setUTCMilliseconds";
      break;
    case 33:
      i = 2;
      str = "setSeconds";
      break;
    case 34:
      i = 2;
      str = "setUTCSeconds";
      break;
    case 35:
      i = 3;
      str = "setMinutes";
      break;
    case 36:
      i = 3;
      str = "setUTCMinutes";
      break;
    case 37:
      i = 4;
      str = "setHours";
      break;
    case 38:
      i = 4;
      str = "setUTCHours";
      break;
    case 39:
      i = 1;
      str = "setDate";
      break;
    case 40:
      i = 1;
      str = "setUTCDate";
      break;
    case 41:
      i = 2;
      str = "setMonth";
      break;
    case 42:
      i = 2;
      str = "setUTCMonth";
      break;
    case 43:
      i = 3;
      str = "setFullYear";
      break;
    case 44:
      i = 3;
      str = "setUTCFullYear";
      break;
    case 45:
      i = 1;
      str = "setYear";
      break;
    default:
      throw new IllegalArgumentException(String.valueOf(paramInt));
    }
    initPrototypeMethod(DATE_TAG, paramInt, str, i);
  }

  public Object execIdCall(IdFunctionObject paramIdFunctionObject, Context paramContext, Scriptable paramScriptable1, Scriptable paramScriptable2, Object[] paramArrayOfObject)
  {
    if (!(paramIdFunctionObject.hasTag(DATE_TAG)))
      return super.execIdCall(paramIdFunctionObject, paramContext, paramScriptable1, paramScriptable2, paramArrayOfObject);
    int i = paramIdFunctionObject.methodId();
    switch (i)
    {
    case -3:
      return ScriptRuntime.wrapNumber(now());
    case -2:
      localObject = ScriptRuntime.toString(paramArrayOfObject, 0);
      return ScriptRuntime.wrapNumber(date_parseString(paramContext, (String)localObject));
    case -1:
      return ScriptRuntime.wrapNumber(jsStaticFunction_UTC(paramArrayOfObject));
    case 1:
      if (paramScriptable2 != null)
        return date_format(paramContext, now(), 2);
      return jsConstructor(paramContext, paramArrayOfObject);
    case 0:
    }
    if (!(paramScriptable2 instanceof NativeDate))
      throw incompatibleCallError(paramIdFunctionObject);
    Object localObject = (NativeDate)paramScriptable2;
    double d1 = ((NativeDate)localObject).date;
    switch (i)
    {
    case 2:
    case 3:
    case 4:
      if (d1 == d1)
        return date_format(paramContext, d1, i);
      return "Invalid Date";
    case 5:
    case 6:
    case 7:
      if (d1 == d1)
        return toLocale_helper(d1, i);
      return "Invalid Date";
    case 8:
      if (d1 == d1)
        return js_toUTCString(d1);
      return "Invalid Date";
    case 9:
      return "(new Date(" + ScriptRuntime.toString(d1) + "))";
    case 10:
    case 11:
      return ScriptRuntime.wrapNumber(d1);
    case 12:
    case 13:
    case 14:
      if (d1 == d1)
      {
        if (i != 14)
          d1 = LocalTime(paramContext, d1);
        d1 = YearFromTime(d1);
        if (i == 12)
          if (paramContext.hasFeature(1))
            if ((1900.0D <= d1) && (d1 < 2000.0D))
              d1 -= 1900.0D;
          else
            d1 -= 1900.0D;
      }
      return ScriptRuntime.wrapNumber(d1);
    case 15:
    case 16:
      if (d1 == d1)
      {
        if (i == 15)
          d1 = LocalTime(paramContext, d1);
        d1 = MonthFromTime(d1);
      }
      return ScriptRuntime.wrapNumber(d1);
    case 17:
    case 18:
      if (d1 == d1)
      {
        if (i == 17)
          d1 = LocalTime(paramContext, d1);
        d1 = DateFromTime(d1);
      }
      return ScriptRuntime.wrapNumber(d1);
    case 19:
    case 20:
      if (d1 == d1)
      {
        if (i == 19)
          d1 = LocalTime(paramContext, d1);
        d1 = WeekDay(d1);
      }
      return ScriptRuntime.wrapNumber(d1);
    case 21:
    case 22:
      if (d1 == d1)
      {
        if (i == 21)
          d1 = LocalTime(paramContext, d1);
        d1 = HourFromTime(d1);
      }
      return ScriptRuntime.wrapNumber(d1);
    case 23:
    case 24:
      if (d1 == d1)
      {
        if (i == 23)
          d1 = LocalTime(paramContext, d1);
        d1 = MinFromTime(d1);
      }
      return ScriptRuntime.wrapNumber(d1);
    case 25:
    case 26:
      if (d1 == d1)
      {
        if (i == 25)
          d1 = LocalTime(paramContext, d1);
        d1 = SecFromTime(d1);
      }
      return ScriptRuntime.wrapNumber(d1);
    case 27:
    case 28:
      if (d1 == d1)
      {
        if (i == 27)
          d1 = LocalTime(paramContext, d1);
        d1 = msFromTime(d1);
      }
      return ScriptRuntime.wrapNumber(d1);
    case 29:
      if (d1 == d1)
        d1 = (d1 - LocalTime(paramContext, d1)) / 60000.0D;
      return ScriptRuntime.wrapNumber(d1);
    case 30:
      d1 = TimeClip(ScriptRuntime.toNumber(paramArrayOfObject, 0));
      ((NativeDate)localObject).date = d1;
      return ScriptRuntime.wrapNumber(d1);
    case 31:
    case 32:
    case 33:
    case 34:
    case 35:
    case 36:
    case 37:
    case 38:
      d1 = makeTime(paramContext, d1, paramArrayOfObject, i);
      ((NativeDate)localObject).date = d1;
      return ScriptRuntime.wrapNumber(d1);
    case 39:
    case 40:
    case 41:
    case 42:
    case 43:
    case 44:
      d1 = makeDate(paramContext, d1, paramArrayOfObject, i);
      ((NativeDate)localObject).date = d1;
      return ScriptRuntime.wrapNumber(d1);
    case 45:
      double d2 = ScriptRuntime.toNumber(paramArrayOfObject, 0);
      if ((d2 != d2) || (Double.isInfinite(d2)))
      {
        d1 = ScriptRuntime.NaN;
      }
      else
      {
        if (d1 != d1)
          d1 = 0D;
        else
          d1 = LocalTime(paramContext, d1);
        if ((d2 >= 0D) && (d2 <= 99.0D))
          d2 += 1900.0D;
        double d3 = MakeDay(d2, MonthFromTime(d1), DateFromTime(d1));
        d1 = MakeDate(d3, TimeWithinDay(d1));
        d1 = internalUTC(paramContext, d1);
        d1 = TimeClip(d1);
      }
      ((NativeDate)localObject).date = d1;
      return ScriptRuntime.wrapNumber(d1);
    }
    throw new IllegalArgumentException(String.valueOf(i));
  }

  private static double Day(double paramDouble)
  {
    return Math.floor(paramDouble / 86400000.0D);
  }

  private static double TimeWithinDay(double paramDouble)
  {
    double d = paramDouble % 86400000.0D;
    if (d < 0D)
      d += 86400000.0D;
    return d;
  }

  private static boolean IsLeapYear(int paramInt)
  {
    return ((paramInt % 4 == 0) && (((paramInt % 100 != 0) || (paramInt % 400 == 0))));
  }

  private static double DayFromYear(double paramDouble)
  {
    return (365.0D * (paramDouble - 1970.0D) + Math.floor((paramDouble - 1969.0D) / 4.0D) - Math.floor((paramDouble - 1901.0D) / 100.0D) + Math.floor((paramDouble - 1601.0D) / 400.0D));
  }

  private static double TimeFromYear(double paramDouble)
  {
    return (DayFromYear(paramDouble) * 86400000.0D);
  }

  private static int YearFromTime(double paramDouble)
  {
    int l;
    int i = (int)Math.floor(paramDouble / 86400000.0D / 366.0D) + 1970;
    int j = (int)Math.floor(paramDouble / 86400000.0D / 365.0D) + 1970;
    if (j < i)
    {
      int i1 = i;
      i = j;
      j = i1;
    }
    do
    {
      while (true)
      {
        if (j <= i)
          break label98;
        l = (j + i) / 2;
        if (TimeFromYear(l) <= paramDouble)
          break;
        int k = l - 1;
      }
      i = l + 1;
    }
    while (TimeFromYear(i) <= paramDouble);
    return l;
    label98: return i;
  }

  private static boolean InLeapYear(double paramDouble)
  {
    return IsLeapYear(YearFromTime(paramDouble));
  }

  private static double DayFromMonth(int paramInt1, int paramInt2)
  {
    int i = paramInt1 * 30;
    if (paramInt1 >= 7)
      i += paramInt1 / 2 - 1;
    else if (paramInt1 >= 2)
      i += (paramInt1 - 1) / 2 - 1;
    else
      i += paramInt1;
    if ((paramInt1 >= 2) && (IsLeapYear(paramInt2)))
      ++i;
    return i;
  }

  private static int MonthFromTime(double paramDouble)
  {
    int l;
    int i = YearFromTime(paramDouble);
    int j = (int)(Day(paramDouble) - DayFromYear(i));
    if ((j -= 59) < 0)
      return ((j < -28) ? 0 : 1);
    if (IsLeapYear(i))
    {
      if (j == 0)
        return 1;
      --j;
    }
    int k = j / 30;
    switch (k)
    {
    case 0:
      return 2;
    case 1:
      l = 31;
      break;
    case 2:
      l = 61;
      break;
    case 3:
      l = 92;
      break;
    case 4:
      l = 122;
      break;
    case 5:
      l = 153;
      break;
    case 6:
      l = 184;
      break;
    case 7:
      l = 214;
      break;
    case 8:
      l = 245;
      break;
    case 9:
      l = 275;
      break;
    case 10:
      return 11;
    default:
      throw Kit.codeBug();
    }
    return ((j >= l) ? k + 2 : k + 1);
  }

  private static int DateFromTime(double paramDouble)
  {
    int k;
    int l;
    int i = YearFromTime(paramDouble);
    int j = (int)(Day(paramDouble) - DayFromYear(i));
    if ((j -= 59) < 0)
      return ((j < -28) ? j + 31 + 28 + 1 : j + 28 + 1);
    if (IsLeapYear(i))
    {
      if (j == 0)
        return 29;
      --j;
    }
    switch (j / 30)
    {
    case 0:
      return (j + 1);
    case 1:
      k = 31;
      l = 31;
      break;
    case 2:
      k = 30;
      l = 61;
      break;
    case 3:
      k = 31;
      l = 92;
      break;
    case 4:
      k = 30;
      l = 122;
      break;
    case 5:
      k = 31;
      l = 153;
      break;
    case 6:
      k = 31;
      l = 184;
      break;
    case 7:
      k = 30;
      l = 214;
      break;
    case 8:
      k = 31;
      l = 245;
      break;
    case 9:
      k = 30;
      l = 275;
      break;
    case 10:
      return (j - 275 + 1);
    default:
      throw Kit.codeBug();
    }
    j -= l;
    if (j < 0)
      j += k;
    return (j + 1);
  }

  private static int WeekDay(double paramDouble)
  {
    double d = Day(paramDouble) + 4.0D;
    d %= 7.0D;
    if (d < 0D)
      d += 7.0D;
    return (int)d;
  }

  private static double now()
  {
    return System.currentTimeMillis();
  }

  private static double DaylightSavingTA(Context paramContext, double paramDouble)
  {
    if ((paramDouble < 0D) || (paramDouble > 2145916800000.0D))
    {
      int i = EquivalentYear(YearFromTime(paramDouble));
      double d = MakeDay(i, MonthFromTime(paramDouble), DateFromTime(paramDouble));
      paramDouble = MakeDate(d, TimeWithinDay(paramDouble));
    }
    Date localDate = new Date(()paramDouble);
    if (paramContext.getTimeZone().inDaylightTime(localDate))
      return 3600000.0D;
    return 0D;
  }

  private static int EquivalentYear(int paramInt)
  {
    int i = (int)DayFromYear(paramInt) + 4;
    i %= 7;
    if (i < 0)
      i += 7;
    if (IsLeapYear(paramInt))
      switch (i)
      {
      case 0:
        return 1984;
      case 1:
        return 1996;
      case 2:
        return 1980;
      case 3:
        return 1992;
      case 4:
        return 1976;
      case 5:
        return 1988;
      case 6:
        return 1972;
      }
    else
      switch (i)
      {
      case 0:
        return 1978;
      case 1:
        return 1973;
      case 2:
        return 1974;
      case 3:
        return 1975;
      case 4:
        return 1981;
      case 5:
        return 1971;
      case 6:
        return 1977;
      }
    throw Kit.codeBug();
  }

  private static double LocalTime(Context paramContext, double paramDouble)
  {
    return (paramDouble + paramContext.getLocalTZA() + DaylightSavingTA(paramContext, paramDouble));
  }

  private static double internalUTC(Context paramContext, double paramDouble)
  {
    return (paramDouble - paramContext.getLocalTZA() - DaylightSavingTA(paramContext, paramDouble - paramContext.getLocalTZA()));
  }

  private static int HourFromTime(double paramDouble)
  {
    double d = Math.floor(paramDouble / 3600000.0D) % 24.0D;
    if (d < 0D)
      d += 24.0D;
    return (int)d;
  }

  private static int MinFromTime(double paramDouble)
  {
    double d = Math.floor(paramDouble / 60000.0D) % 60.0D;
    if (d < 0D)
      d += 60.0D;
    return (int)d;
  }

  private static int SecFromTime(double paramDouble)
  {
    double d = Math.floor(paramDouble / 1000.0D) % 60.0D;
    if (d < 0D)
      d += 60.0D;
    return (int)d;
  }

  private static int msFromTime(double paramDouble)
  {
    double d = paramDouble % 1000.0D;
    if (d < 0D)
      d += 1000.0D;
    return (int)d;
  }

  private static double MakeTime(double paramDouble1, double paramDouble2, double paramDouble3, double paramDouble4)
  {
    return (((paramDouble1 * 60.0D + paramDouble2) * 60.0D + paramDouble3) * 1000.0D + paramDouble4);
  }

  private static double MakeDay(double paramDouble1, double paramDouble2, double paramDouble3)
  {
    paramDouble1 += Math.floor(paramDouble2 / 12.0D);
    paramDouble2 %= 12.0D;
    if (paramDouble2 < 0D)
      paramDouble2 += 12.0D;
    double d1 = Math.floor(TimeFromYear(paramDouble1) / 86400000.0D);
    double d2 = DayFromMonth((int)paramDouble2, (int)paramDouble1);
    return (d1 + d2 + paramDouble3 - 1D);
  }

  private static double MakeDate(double paramDouble1, double paramDouble2)
  {
    return (paramDouble1 * 86400000.0D + paramDouble2);
  }

  private static double TimeClip(double paramDouble)
  {
    if ((paramDouble != paramDouble) || (paramDouble == (1.0D / 0.0D)) || (paramDouble == (-1.0D / 0.0D)) || (Math.abs(paramDouble) > 8640000000000000.0D))
      return ScriptRuntime.NaN;
    if (paramDouble > 0D)
      return Math.floor(paramDouble + 0D);
    return Math.ceil(paramDouble + 0D);
  }

  private static double date_msecFromDate(double paramDouble1, double paramDouble2, double paramDouble3, double paramDouble4, double paramDouble5, double paramDouble6, double paramDouble7)
  {
    double d1 = MakeDay(paramDouble1, paramDouble2, paramDouble3);
    double d2 = MakeTime(paramDouble4, paramDouble5, paramDouble6, paramDouble7);
    double d3 = MakeDate(d1, d2);
    return d3;
  }

  private static double jsStaticFunction_UTC(Object[] paramArrayOfObject)
  {
    double[] arrayOfDouble = new double[7];
    for (int i = 0; i < 7; ++i)
      if (i < paramArrayOfObject.length)
      {
        d = ScriptRuntime.toNumber(paramArrayOfObject[i]);
        if ((d != d) || (Double.isInfinite(d)))
          return ScriptRuntime.NaN;
        arrayOfDouble[i] = ScriptRuntime.toInteger(paramArrayOfObject[i]);
      }
      else
      {
        arrayOfDouble[i] = 0D;
      }
    if ((arrayOfDouble[0] >= 0D) && (arrayOfDouble[0] <= 99.0D))
      arrayOfDouble[0] += 1900.0D;
    if (arrayOfDouble[2] < 1D)
      arrayOfDouble[2] = 1D;
    double d = date_msecFromDate(arrayOfDouble[0], arrayOfDouble[1], arrayOfDouble[2], arrayOfDouble[3], arrayOfDouble[4], arrayOfDouble[5], arrayOfDouble[6]);
    d = TimeClip(d);
    return d;
  }

  private static double date_parseString(Context paramContext, String paramString)
  {
    int i = -1;
    int j = -1;
    int k = -1;
    int l = -1;
    int i1 = -1;
    int i2 = -1;
    int i3 = 0;
    int i4 = 0;
    int i5 = 0;
    int i6 = -1;
    double d1 = -1.0D;
    int i7 = 0;
    int i8 = 0;
    int i9 = 0;
    i8 = paramString.length();
    while (true)
    {
      while (true)
      {
        while (true)
        {
          while (true)
          {
            while (true)
            {
              do
              {
                do
                {
                  if (i5 >= i8)
                    break label1049;
                  i3 = paramString.charAt(i5);
                  ++i5;
                  if ((i3 > 32) && (i3 != 44) && (i3 != 45))
                    break label130;
                }
                while (i5 >= i8);
                i4 = paramString.charAt(i5);
              }
              while ((i3 != 45) || (48 > i4) || (i4 > 57));
              i7 = i3;
            }
            label130: if (i3 != 40)
              break;
            i10 = 1;
            do
              while (true)
              {
                if (i5 >= i8);
                i3 = paramString.charAt(i5);
                ++i5;
                if (i3 != 40)
                  break;
                ++i10;
              }
            while ((i3 != 41) || (--i10 > 0));
          }
          if ((48 > i3) || (i3 > 57))
            break;
          i6 = i3 - 48;
          while (i5 < i8)
          {
            if (('0' > (i3 = paramString.charAt(i5))) || (i3 > 57))
              break;
            i6 = i6 * 10 + i3 - 48;
            ++i5;
          }
          if ((i7 == 43) || (i7 == 45))
          {
            i9 = 1;
            if (i6 < 24)
              i6 *= 60;
            else
              i6 = i6 % 100 + i6 / 100 * 60;
            if (i7 == 43)
              i6 = -i6;
            if ((d1 != 0D) && (d1 != -1.0D))
              return ScriptRuntime.NaN;
            d1 = i6;
          }
          else
          {
            if ((i6 >= 70) || ((i7 == 47) && (j >= 0) && (k >= 0) && (i < 0)))
            {
              if (i >= 0)
                return ScriptRuntime.NaN;
              if ((i3 <= 32) || (i3 == 44) || (i3 == 47) || (i5 >= i8))
              {
                i = (i6 < 100) ? i6 + 1900 : i6;
                break label629:
              }
              return ScriptRuntime.NaN;
            }
            if (i3 == 58)
            {
              if (l < 0)
              {
                l = i6;
                break label629:
              }
              if (i1 < 0)
              {
                i1 = i6;
                break label629:
              }
              return ScriptRuntime.NaN;
            }
            if (i3 == 47)
            {
              if (j < 0)
              {
                j = i6 - 1;
                break label629:
              }
              if (k < 0)
              {
                k = i6;
                break label629:
              }
              return ScriptRuntime.NaN;
            }
            if ((i5 < i8) && (i3 != 44) && (i3 > 32) && (i3 != 45))
              return ScriptRuntime.NaN;
            if ((i9 != 0) && (i6 < 60))
              if (d1 < 0D)
                d1 -= i6;
              else
                d1 += i6;
            else if ((l >= 0) && (i1 < 0))
              i1 = i6;
            else if ((i1 >= 0) && (i2 < 0))
              i2 = i6;
            else if (k < 0)
              k = i6;
            else
              return ScriptRuntime.NaN;
          }
          label629: i7 = 0;
        }
        if ((i3 != 47) && (i3 != 58) && (i3 != 43) && (i3 != 45))
          break;
        i7 = i3;
      }
      int i10 = i5 - 1;
      while (i5 < i8)
      {
        i3 = paramString.charAt(i5);
        if ((65 > i3) || (i3 > 90))
        {
          if (97 > i3)
            break;
          if (i3 > 122)
            break;
        }
        ++i5;
      }
      int i11 = i5 - i10;
      if (i11 < 2)
        return ScriptRuntime.NaN;
      String str = "am;pm;monday;tuesday;wednesday;thursday;friday;saturday;sunday;january;february;march;april;may;june;july;august;september;october;november;december;gmt;ut;utc;est;edt;cst;cdt;mst;mdt;pst;pdt;";
      int i12 = 0;
      int i13 = 0;
      while (true)
      {
        int i14 = str.indexOf(59, i13);
        if (i14 < 0)
          return ScriptRuntime.NaN;
        if (str.regionMatches(true, i13, paramString, i10, i11))
          break;
        i13 = i14 + 1;
        ++i12;
      }
      if (i12 < 2)
      {
        if ((l > 12) || (l < 0))
          return ScriptRuntime.NaN;
        if (i12 == 0)
          if (l == 12)
            l = 0;
        else if (l != 12)
          l += 12;
      }
      else
      {
        if ((i12 -= 2) < 7)
          continue;
        if ((i12 -= 7) < 12)
        {
          if (j < 0)
          {
            j = i12;
            continue;
          }
          return ScriptRuntime.NaN;
        }
        switch (i12 -= 12)
        {
        case 0:
          d1 = 0D;
          break;
        case 1:
          d1 = 0D;
          break;
        case 2:
          d1 = 0D;
          break;
        case 3:
          d1 = 300.0D;
          break;
        case 4:
          d1 = 240.0D;
          break;
        case 5:
          d1 = 360.0D;
          break;
        case 6:
          d1 = 300.0D;
          break;
        case 7:
          d1 = 420.0D;
          break;
        case 8:
          d1 = 360.0D;
          break;
        case 9:
          d1 = 480.0D;
          break;
        case 10:
          d1 = 420.0D;
          break;
        default:
          Kit.codeBug();
        }
      }
    }
    if ((i < 0) || (j < 0) || (k < 0))
      label1049: return ScriptRuntime.NaN;
    if (i2 < 0)
      i2 = 0;
    if (i1 < 0)
      i1 = 0;
    if (l < 0)
      l = 0;
    double d2 = date_msecFromDate(i, j, k, l, i1, i2, 0D);
    if (d1 == -1.0D)
      return internalUTC(paramContext, d2);
    return (d2 + d1 * 60000.0D);
  }

  private static String date_format(Context paramContext, double paramDouble, int paramInt)
  {
    int i;
    StringBuffer localStringBuffer = new StringBuffer(60);
    double d1 = LocalTime(paramContext, paramDouble);
    if (paramInt != 3)
    {
      appendWeekDayName(localStringBuffer, WeekDay(d1));
      localStringBuffer.append(' ');
      appendMonthName(localStringBuffer, MonthFromTime(d1));
      localStringBuffer.append(' ');
      append0PaddedUint(localStringBuffer, DateFromTime(d1), 2);
      localStringBuffer.append(' ');
      i = YearFromTime(d1);
      if (i < 0)
      {
        localStringBuffer.append('-');
        i = -i;
      }
      append0PaddedUint(localStringBuffer, i, 4);
      if (paramInt != 4)
        localStringBuffer.append(' ');
    }
    if (paramInt != 4)
    {
      append0PaddedUint(localStringBuffer, HourFromTime(d1), 2);
      localStringBuffer.append(':');
      append0PaddedUint(localStringBuffer, MinFromTime(d1), 2);
      localStringBuffer.append(':');
      append0PaddedUint(localStringBuffer, SecFromTime(d1), 2);
      i = (int)Math.floor((paramContext.getLocalTZA() + DaylightSavingTA(paramContext, paramDouble)) / 60000.0D);
      int j = i / 60 * 100 + i % 60;
      if (j > 0)
      {
        localStringBuffer.append(" GMT+");
      }
      else
      {
        localStringBuffer.append(" GMT-");
        j = -j;
      }
      append0PaddedUint(localStringBuffer, j, 4);
      if (timeZoneFormatter == null)
        timeZoneFormatter = new SimpleDateFormat("zzz");
      if ((paramDouble < 0D) || (paramDouble > 2145916800000.0D))
      {
        int k = EquivalentYear(YearFromTime(d1));
        double d2 = MakeDay(k, MonthFromTime(paramDouble), DateFromTime(paramDouble));
        paramDouble = MakeDate(d2, TimeWithinDay(paramDouble));
      }
      localStringBuffer.append(" (");
      Date localDate = new Date(()paramDouble);
      localStringBuffer.append(timeZoneFormatter.format(localDate));
      localStringBuffer.append(')');
    }
    return localStringBuffer.toString();
  }

  private static Object jsConstructor(Context paramContext, Object[] paramArrayOfObject)
  {
    NativeDate localNativeDate = new NativeDate(paramContext);
    if (paramArrayOfObject.length == 0)
    {
      localNativeDate.date = now();
      return localNativeDate;
    }
    if (paramArrayOfObject.length == 1)
    {
      double d1;
      localObject = paramArrayOfObject[0];
      if (localObject instanceof Scriptable)
        localObject = ((Scriptable)localObject).getDefaultValue(null);
      if (localObject instanceof String)
        d1 = date_parseString(paramContext, (String)localObject);
      else
        d1 = ScriptRuntime.toNumber(localObject);
      localNativeDate.date = TimeClip(d1);
      return localNativeDate;
    }
    Object localObject = new double[7];
    for (int i = 0; i < 7; ++i)
      if (i < paramArrayOfObject.length)
      {
        double d2 = ScriptRuntime.toNumber(paramArrayOfObject[i]);
        if ((d2 != d2) || (Double.isInfinite(d2)))
        {
          localNativeDate.date = ScriptRuntime.NaN;
          return localNativeDate;
        }
        localObject[i] = ScriptRuntime.toInteger(paramArrayOfObject[i]);
      }
      else
      {
        localObject[i] = 0D;
      }
    if ((localObject[0] >= 0D) && (localObject[0] <= 99.0D))
      localObject[0] += 1900.0D;
    if (localObject[2] < 1D)
      localObject[2] = 1D;
    double d3 = MakeDay(localObject[0], localObject[1], localObject[2]);
    double d4 = MakeTime(localObject[3], localObject[4], localObject[5], localObject[6]);
    d4 = MakeDate(d3, d4);
    d4 = internalUTC(paramContext, d4);
    localNativeDate.date = TimeClip(d4);
    return localNativeDate;
  }

  private static String toLocale_helper(double paramDouble, int paramInt)
  {
    DateFormat localDateFormat;
    switch (paramInt)
    {
    case 5:
      if (localeDateTimeFormatter == null)
        localeDateTimeFormatter = DateFormat.getDateTimeInstance(1, 1);
      localDateFormat = localeDateTimeFormatter;
      break;
    case 6:
      if (localeTimeFormatter == null)
        localeTimeFormatter = DateFormat.getTimeInstance(1);
      localDateFormat = localeTimeFormatter;
      break;
    case 7:
      if (localeDateFormatter == null)
        localeDateFormatter = DateFormat.getDateInstance(1);
      localDateFormat = localeDateFormatter;
      break;
    default:
      localDateFormat = null;
    }
    return localDateFormat.format(new Date(()paramDouble));
  }

  private static String js_toUTCString(double paramDouble)
  {
    StringBuffer localStringBuffer = new StringBuffer(60);
    appendWeekDayName(localStringBuffer, WeekDay(paramDouble));
    localStringBuffer.append(", ");
    append0PaddedUint(localStringBuffer, DateFromTime(paramDouble), 2);
    localStringBuffer.append(' ');
    appendMonthName(localStringBuffer, MonthFromTime(paramDouble));
    localStringBuffer.append(' ');
    int i = YearFromTime(paramDouble);
    if (i < 0)
    {
      localStringBuffer.append('-');
      i = -i;
    }
    append0PaddedUint(localStringBuffer, i, 4);
    localStringBuffer.append(' ');
    append0PaddedUint(localStringBuffer, HourFromTime(paramDouble), 2);
    localStringBuffer.append(':');
    append0PaddedUint(localStringBuffer, MinFromTime(paramDouble), 2);
    localStringBuffer.append(':');
    append0PaddedUint(localStringBuffer, SecFromTime(paramDouble), 2);
    localStringBuffer.append(" GMT");
    return localStringBuffer.toString();
  }

  private static void append0PaddedUint(StringBuffer paramStringBuffer, int paramInt1, int paramInt2)
  {
    if (paramInt1 < 0)
      Kit.codeBug();
    int i = 1;
    --paramInt2;
    if (paramInt1 >= 10)
    {
      if (paramInt1 < 1000000000)
        while (true)
        {
          int j = i * 10;
          if (paramInt1 < j)
            break label55:
          --paramInt2;
          i = j;
        }
      paramInt2 -= 9;
      i = 1000000000;
    }
    while (paramInt2 > 0)
    {
      label55: paramStringBuffer.append('0');
      --paramInt2;
    }
    while (i != 1)
    {
      paramStringBuffer.append((char)(48 + paramInt1 / i));
      paramInt1 %= i;
      i /= 10;
    }
    paramStringBuffer.append((char)(48 + paramInt1));
  }

  private static void appendMonthName(StringBuffer paramStringBuffer, int paramInt)
  {
    String str = "JanFebMarAprMayJunJulAugSepOctNovDec";
    paramInt *= 3;
    for (int i = 0; i != 3; ++i)
      paramStringBuffer.append(str.charAt(paramInt + i));
  }

  private static void appendWeekDayName(StringBuffer paramStringBuffer, int paramInt)
  {
    String str = "SunMonTueWedThuFriSat";
    paramInt *= 3;
    for (int i = 0; i != 3; ++i)
      paramStringBuffer.append(str.charAt(paramInt + i));
  }

  private static double makeTime(Context paramContext, double paramDouble, Object[] paramArrayOfObject, int paramInt)
  {
    int i;
    double d1;
    double d2;
    double d3;
    double d4;
    double d5;
    int j = 1;
    switch (paramInt)
    {
    case 32:
      j = 0;
    case 31:
      i = 1;
      break;
    case 34:
      j = 0;
    case 33:
      i = 2;
      break;
    case 36:
      j = 0;
    case 35:
      i = 3;
      break;
    case 38:
      j = 0;
    case 37:
      i = 4;
      break;
    default:
      Kit.codeBug();
      i = 0;
    }
    double[] arrayOfDouble = new double[4];
    if (paramDouble != paramDouble)
      return paramDouble;
    if (paramArrayOfObject.length == 0)
      paramArrayOfObject = ScriptRuntime.padArguments(paramArrayOfObject, 1);
    for (int k = 0; (k < paramArrayOfObject.length) && (k < i); ++k)
    {
      arrayOfDouble[k] = ScriptRuntime.toNumber(paramArrayOfObject[k]);
      if ((arrayOfDouble[k] != arrayOfDouble[k]) || (Double.isInfinite(arrayOfDouble[k])))
        return ScriptRuntime.NaN;
      arrayOfDouble[k] = ScriptRuntime.toInteger(arrayOfDouble[k]);
    }
    if (j != 0)
      d5 = LocalTime(paramContext, paramDouble);
    else
      d5 = paramDouble;
    k = 0;
    int l = paramArrayOfObject.length;
    if ((i >= 4) && (k < l))
      d1 = arrayOfDouble[(k++)];
    else
      d1 = HourFromTime(d5);
    if ((i >= 3) && (k < l))
      d2 = arrayOfDouble[(k++)];
    else
      d2 = MinFromTime(d5);
    if ((i >= 2) && (k < l))
      d3 = arrayOfDouble[(k++)];
    else
      d3 = SecFromTime(d5);
    if ((i >= 1) && (k < l))
      d4 = arrayOfDouble[(k++)];
    else
      d4 = msFromTime(d5);
    double d6 = MakeTime(d1, d2, d3, d4);
    double d7 = MakeDate(Day(d5), d6);
    if (j != 0)
      d7 = internalUTC(paramContext, d7);
    paramDouble = TimeClip(d7);
    return paramDouble;
  }

  private static double makeDate(Context paramContext, double paramDouble, Object[] paramArrayOfObject, int paramInt)
  {
    int i;
    double d1;
    double d2;
    double d4;
    int j = 1;
    switch (paramInt)
    {
    case 40:
      j = 0;
    case 39:
      i = 1;
      break;
    case 42:
      j = 0;
    case 41:
      i = 2;
      break;
    case 44:
      j = 0;
    case 43:
      i = 3;
      break;
    default:
      Kit.codeBug();
      i = 0;
    }
    double[] arrayOfDouble = new double[3];
    if (paramArrayOfObject.length == 0)
      paramArrayOfObject = ScriptRuntime.padArguments(paramArrayOfObject, 1);
    for (int k = 0; (k < paramArrayOfObject.length) && (k < i); ++k)
    {
      arrayOfDouble[k] = ScriptRuntime.toNumber(paramArrayOfObject[k]);
      if ((arrayOfDouble[k] != arrayOfDouble[k]) || (Double.isInfinite(arrayOfDouble[k])))
        return ScriptRuntime.NaN;
      arrayOfDouble[k] = ScriptRuntime.toInteger(arrayOfDouble[k]);
    }
    if (paramDouble != paramDouble)
    {
      if (paramArrayOfObject.length < 3)
        return ScriptRuntime.NaN;
      d4 = 0D;
    }
    else if (j != 0)
    {
      d4 = LocalTime(paramContext, paramDouble);
    }
    else
    {
      d4 = paramDouble;
    }
    k = 0;
    int l = paramArrayOfObject.length;
    if ((i >= 3) && (k < l))
      d1 = arrayOfDouble[(k++)];
    else
      d1 = YearFromTime(d4);
    if ((i >= 2) && (k < l))
      d2 = arrayOfDouble[(k++)];
    else
      d2 = MonthFromTime(d4);
    if ((i >= 1) && (k < l))
      d3 = arrayOfDouble[(k++)];
    else
      d3 = DateFromTime(d4);
    double d3 = MakeDay(d1, d2, d3);
    double d5 = MakeDate(d3, TimeWithinDay(d4));
    if (j != 0)
      d5 = internalUTC(paramContext, d5);
    paramDouble = TimeClip(d5);
    return paramDouble;
  }

  protected int findPrototypeId(String paramString)
  {
    int j;
    int i = 0;
    String str = null;
    switch (paramString.length())
    {
    case 6:
      str = "getDay";
      i = 19;
      break;
    case 7:
      switch (paramString.charAt(3))
      {
      case 'D':
        j = paramString.charAt(0);
        if (j == 103)
        {
          str = "getDate";
          i = 17;
        }
        else if (j == 115)
        {
          str = "setDate";
          i = 39;
        }
        break;
      case 'T':
        j = paramString.charAt(0);
        if (j == 103)
        {
          str = "getTime";
          i = 11;
        }
        else if (j == 115)
        {
          str = "setTime";
          i = 30;
        }
        break;
      case 'Y':
        j = paramString.charAt(0);
        if (j == 103)
        {
          str = "getYear";
          i = 12;
        }
        else if (j == 115)
        {
          str = "setYear";
          i = 45;
        }
        break;
      case 'u':
        str = "valueOf";
        i = 10;
        break label1111:
      }
      break;
    case 8:
      switch (paramString.charAt(3))
      {
      case 'H':
        j = paramString.charAt(0);
        if (j == 103)
        {
          str = "getHours";
          i = 21;
        }
        else if (j == 115)
        {
          str = "setHours";
          i = 37;
        }
        break;
      case 'M':
        j = paramString.charAt(0);
        if (j == 103)
        {
          str = "getMonth";
          i = 15;
        }
        else if (j == 115)
        {
          str = "setMonth";
          i = 41;
        }
        break;
      case 'o':
        str = "toSource";
        i = 9;
        break;
      case 't':
        str = "toString";
        i = 2;
        break label1111:
      }
      break;
    case 9:
      str = "getUTCDay";
      i = 20;
      break;
    case 10:
      j = paramString.charAt(3);
      if (j == 77)
      {
        j = paramString.charAt(0);
        if (j == 103)
        {
          str = "getMinutes";
          i = 23;
        }
        else if (j == 115)
        {
          str = "setMinutes";
          i = 35;
        }
      }
      else if (j == 83)
      {
        j = paramString.charAt(0);
        if (j == 103)
        {
          str = "getSeconds";
          i = 25;
        }
        else if (j == 115)
        {
          str = "setSeconds";
          i = 33;
        }
      }
      else if (j == 85)
      {
        j = paramString.charAt(0);
        if (j == 103)
        {
          str = "getUTCDate";
          i = 18;
        }
        else if (j == 115)
        {
          str = "setUTCDate";
          i = 40;
        }
      }
      break;
    case 11:
      switch (paramString.charAt(3))
      {
      case 'F':
        j = paramString.charAt(0);
        if (j == 103)
        {
          str = "getFullYear";
          i = 13;
        }
        else if (j == 115)
        {
          str = "setFullYear";
          i = 43;
        }
        break;
      case 'M':
        str = "toGMTString";
        i = 8;
        break;
      case 'T':
        str = "toUTCString";
        i = 8;
        break;
      case 'U':
        j = paramString.charAt(0);
        if (j == 103)
        {
          j = paramString.charAt(9);
          if (j == 114)
          {
            str = "getUTCHours";
            i = 22;
          }
          else if (j == 116)
          {
            str = "getUTCMonth";
            i = 16;
          }
        }
        else if (j == 115)
        {
          j = paramString.charAt(9);
          if (j == 114)
          {
            str = "setUTCHours";
            i = 38;
          }
          else if (j == 116)
          {
            str = "setUTCMonth";
            i = 42;
          }
        }
        break;
      case 's':
        str = "constructor";
        i = 1;
        break label1111:
      }
      break;
    case 12:
      j = paramString.charAt(2);
      if (j == 68)
      {
        str = "toDateString";
        i = 4;
      }
      else if (j == 84)
      {
        str = "toTimeString";
        i = 3;
      }
      break;
    case 13:
      j = paramString.charAt(0);
      if (j == 103)
      {
        j = paramString.charAt(6);
        if (j == 77)
        {
          str = "getUTCMinutes";
          i = 24;
        }
        else if (j == 83)
        {
          str = "getUTCSeconds";
          i = 26;
        }
      }
      else if (j == 115)
      {
        j = paramString.charAt(6);
        if (j == 77)
        {
          str = "setUTCMinutes";
          i = 36;
        }
        else if (j == 83)
        {
          str = "setUTCSeconds";
          i = 34;
        }
      }
      break;
    case 14:
      j = paramString.charAt(0);
      if (j == 103)
      {
        str = "getUTCFullYear";
        i = 14;
      }
      else if (j == 115)
      {
        str = "setUTCFullYear";
        i = 44;
      }
      else if (j == 116)
      {
        str = "toLocaleString";
        i = 5;
      }
      break;
    case 15:
      j = paramString.charAt(0);
      if (j == 103)
      {
        str = "getMilliseconds";
        i = 27;
      }
      else if (j == 115)
      {
        str = "setMilliseconds";
        i = 31;
      }
      break;
    case 17:
      str = "getTimezoneOffset";
      i = 29;
      break;
    case 18:
      j = paramString.charAt(0);
      if (j == 103)
      {
        str = "getUTCMilliseconds";
        i = 28;
      }
      else if (j == 115)
      {
        str = "setUTCMilliseconds";
        i = 32;
      }
      else if (j == 116)
      {
        j = paramString.charAt(8);
        if (j == 68)
        {
          str = "toLocaleDateString";
          i = 7;
        }
        else if (j == 84)
        {
          str = "toLocaleTimeString";
          i = 6;
        }
      }
    case 16:
    }
    if ((str != null) && (str != paramString) && (!(str.equals(paramString))))
      label1111: i = 0;
    return i;
  }
}