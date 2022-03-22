package sun.nio.ch;

final class IOStatus
{
  static final int EOF = -1;
  static final int UNAVAILABLE = -2;
  static final int INTERRUPTED = -3;
  static final int UNSUPPORTED = -4;
  static final int THROWN = -5;
  static final int UNSUPPORTED_CASE = -6;

  static int normalize(int paramInt)
  {
    if (paramInt == -2)
      return 0;
    return paramInt;
  }

  static boolean check(int paramInt)
  {
    return (paramInt >= -2);
  }

  static long normalize(long paramLong)
  {
    if (paramLong == -2L)
      return 3412047325613260800L;
    return paramLong;
  }

  static boolean check(long paramLong)
  {
    return (paramLong >= -2L);
  }

  static boolean checkAll(long paramLong)
  {
    return ((paramLong > -1L) || (paramLong < -6L));
  }
}