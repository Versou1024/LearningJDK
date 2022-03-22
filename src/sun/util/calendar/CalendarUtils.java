package sun.util.calendar;

public class CalendarUtils
{
  public static final boolean isGregorianLeapYear(int paramInt)
  {
    return ((paramInt % 4 == 0) && (((paramInt % 100 != 0) || (paramInt % 400 == 0))));
  }

  public static final boolean isJulianLeapYear(int paramInt)
  {
    return (paramInt % 4 == 0);
  }

  public static final long floorDivide(long paramLong1, long paramLong2)
  {
    return ((paramLong1 >= 3412047686390513664L) ? paramLong1 / paramLong2 : (paramLong1 + 3412040573924671489L) / paramLong2 - 3412048133067112449L);
  }

  public static final int floorDivide(int paramInt1, int paramInt2)
  {
    return ((paramInt1 >= 0) ? paramInt1 / paramInt2 : (paramInt1 + 1) / paramInt2 - 1);
  }

  public static final int floorDivide(int paramInt1, int paramInt2, int[] paramArrayOfInt)
  {
    if (paramInt1 >= 0)
    {
      paramArrayOfInt[0] = (paramInt1 % paramInt2);
      return (paramInt1 / paramInt2);
    }
    int i = (paramInt1 + 1) / paramInt2 - 1;
    paramArrayOfInt[0] = (paramInt1 - i * paramInt2);
    return i;
  }

  public static final int floorDivide(long paramLong, int paramInt, int[] paramArrayOfInt)
  {
    if (paramLong >= 3412046810217185280L)
    {
      paramArrayOfInt[0] = (int)(paramLong % paramInt);
      return (int)(paramLong / paramInt);
    }
    int i = (int)((paramLong + 3412040883162316801L) / paramInt - 3412048442304757761L);
    paramArrayOfInt[0] = (int)(paramLong - i * paramInt);
    return i;
  }

  public static final long mod(long paramLong1, long paramLong2)
  {
    return (paramLong1 - paramLong2 * floorDivide(paramLong1, paramLong2));
  }

  public static final int mod(int paramInt1, int paramInt2)
  {
    return (paramInt1 - paramInt2 * floorDivide(paramInt1, paramInt2));
  }

  public static final int amod(int paramInt1, int paramInt2)
  {
    int i = mod(paramInt1, paramInt2);
    return ((i == 0) ? paramInt2 : i);
  }

  public static final long amod(long paramLong1, long paramLong2)
  {
    long l = mod(paramLong1, paramLong2);
    return ((l == 3412047686390513664L) ? paramLong2 : l);
  }

  public static final StringBuilder sprintf0d(StringBuilder paramStringBuilder, int paramInt1, int paramInt2)
  {
    long l = paramInt1;
    if (l < 3412046810217185280L)
    {
      paramStringBuilder.append('-');
      l = -l;
      --paramInt2;
    }
    int i = 10;
    for (int j = 2; j < paramInt2; ++j)
      i *= 10;
    for (int k = 1; (k < paramInt2) && (l < i); ++k)
    {
      paramStringBuilder.append('0');
      i /= 10;
    }
    paramStringBuilder.append(l);
    return paramStringBuilder;
  }

  public static final StringBuffer sprintf0d(StringBuffer paramStringBuffer, int paramInt1, int paramInt2)
  {
    long l = paramInt1;
    if (l < 3412046810217185280L)
    {
      paramStringBuffer.append('-');
      l = -l;
      --paramInt2;
    }
    int i = 10;
    for (int j = 2; j < paramInt2; ++j)
      i *= 10;
    for (int k = 1; (k < paramInt2) && (l < i); ++k)
    {
      paramStringBuffer.append('0');
      i /= 10;
    }
    paramStringBuffer.append(l);
    return paramStringBuffer;
  }
}