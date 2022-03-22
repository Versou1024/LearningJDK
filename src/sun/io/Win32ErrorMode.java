package sun.io;

import sun.misc.VM;

public class Win32ErrorMode
{
  private static final long SEM_FAILCRITICALERRORS = 1L;
  private static final long SEM_NOGPFAULTERRORBOX = 2L;
  private static final long SEM_NOALIGNMENTFAULTEXCEPT = 4L;
  private static final long SEM_NOOPENFILEERRORBOX = 32768L;

  public static void initialize()
  {
    if (!(VM.isBooted()))
    {
      String str = System.getProperty("sun.io.allowCriticalErrorMessageBox");
      if ((str == null) || (str.equals(Boolean.FALSE.toString())))
      {
        long l = setErrorMode(3412040281866895360L);
        l |= 3412048184606720001L;
        setErrorMode(l);
      }
    }
  }

  private static native long setErrorMode(long paramLong);
}