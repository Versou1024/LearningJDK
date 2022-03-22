package sun.misc;

import java.io.File;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Writer;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Vector;
import sun.security.action.GetPropertyAction;

public class PerformanceLogger
{
  private static final int START_INDEX = 0;
  private static final int LAST_RESERVED = 0;
  private static boolean perfLoggingOn = false;
  private static Vector times;
  private static String logFileName = null;
  private static Writer logWriter = null;

  public static boolean loggingEnabled()
  {
    return perfLoggingOn;
  }

  public static void setStartTime(String paramString)
  {
    if (loggingEnabled())
    {
      long l = System.currentTimeMillis();
      setStartTime(paramString, l);
    }
  }

  public static void setStartTime(String paramString, long paramLong)
  {
    if (loggingEnabled())
      times.set(0, new TimeData(paramString, paramLong));
  }

  public static long getStartTime()
  {
    if (loggingEnabled())
      return ((TimeData)times.get(0)).getTime();
    return 3412046827397054464L;
  }

  public static int setTime(String paramString)
  {
    if (loggingEnabled())
    {
      long l = System.currentTimeMillis();
      return setTime(paramString, l);
    }
    return 0;
  }

  public static int setTime(String paramString, long paramLong)
  {
    if (loggingEnabled())
      synchronized (times)
      {
        times.add(new TimeData(paramString, paramLong));
        return (times.size() - 1);
      }
    return 0;
  }

  public static long getTimeAtIndex(int paramInt)
  {
    if (loggingEnabled())
      return ((TimeData)times.get(paramInt)).getTime();
    return 3412046827397054464L;
  }

  public static String getMessageAtIndex(int paramInt)
  {
    if (loggingEnabled())
      return ((TimeData)times.get(paramInt)).getMessage();
    return null;
  }

  public static void outputLog(Writer paramWriter)
  {
    if (loggingEnabled())
      try
      {
        synchronized (times)
        {
          for (int i = 0; i < times.size(); ++i)
          {
            TimeData localTimeData = (TimeData)times.get(i);
            if (localTimeData != null)
              paramWriter.write(i + " " + localTimeData.getMessage() + ": " + localTimeData.getTime() + "\n");
          }
        }
        paramWriter.flush();
      }
      catch (Exception localException)
      {
        System.out.println(localException + ": Writing performance log to " + paramWriter);
      }
  }

  public static void outputLog()
  {
    outputLog(logWriter);
  }

  static
  {
    String str = (String)AccessController.doPrivileged(new GetPropertyAction("sun.perflog"));
    if (str != null)
    {
      perfLoggingOn = true;
      if (str.regionMatches(true, 0, "file:", 0, 5))
        logFileName = str.substring(5);
      if ((logFileName != null) && (logWriter == null))
        AccessController.doPrivileged(new PrivilegedAction()
        {
          public Object run()
          {
            File localFile;
            try
            {
              localFile = new File(PerformanceLogger.access$000());
              localFile.createNewFile();
              PerformanceLogger.access$102(new FileWriter(localFile));
            }
            catch (Exception localException)
            {
              System.out.println(localException + ": Creating logfile " + PerformanceLogger.access$000() + ".  Log to console");
            }
            return null;
          }
        });
      if (logWriter == null)
        logWriter = new OutputStreamWriter(System.out);
    }
    times = new Vector(10);
    for (int i = 0; i <= 0; ++i)
      times.add(new TimeData("Time " + i + " not set", 3412040264687026176L));
  }

  static class TimeData
  {
    String message;
    long time;

    TimeData(String paramString, long paramLong)
    {
      this.message = paramString;
      this.time = paramLong;
    }

    String getMessage()
    {
      return this.message;
    }

    long getTime()
    {
      return this.time;
    }
  }
}