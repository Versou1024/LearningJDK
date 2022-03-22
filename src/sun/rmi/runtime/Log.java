package sun.rmi.runtime;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.rmi.server.LogStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;
import sun.security.action.GetPropertyAction;

public abstract class Log
{
  public static final Level BRIEF = Level.FINE;
  public static final Level VERBOSE = Level.FINER;
  private static final LogFactory logFactory;

  public abstract boolean isLoggable(Level paramLevel);

  public abstract void log(Level paramLevel, String paramString);

  public abstract void log(Level paramLevel, String paramString, Throwable paramThrowable);

  public abstract void setOutputStream(OutputStream paramOutputStream);

  public abstract PrintStream getPrintStream();

  public static Log getLog(String paramString1, String paramString2, int paramInt)
  {
    Level localLevel;
    if (paramInt < 0)
      localLevel = null;
    else if (paramInt == 0)
      localLevel = Level.OFF;
    else if ((paramInt > 0) && (paramInt <= 10))
      localLevel = BRIEF;
    else if ((paramInt > 10) && (paramInt <= 20))
      localLevel = VERBOSE;
    else
      localLevel = Level.FINEST;
    return logFactory.createLog(paramString1, paramString2, localLevel);
  }

  public static Log getLog(String paramString1, String paramString2, boolean paramBoolean)
  {
    Level localLevel = (paramBoolean) ? VERBOSE : null;
    return logFactory.createLog(paramString1, paramString2, localLevel);
  }

  private static String[] getSource()
  {
    StackTraceElement[] arrayOfStackTraceElement = new Exception().getStackTrace();
    return { arrayOfStackTraceElement[3].getClassName(), arrayOfStackTraceElement[3].getMethodName() };
  }

  static
  {
    boolean bool = Boolean.valueOf((String)AccessController.doPrivileged(new GetPropertyAction("sun.rmi.log.useOld"))).booleanValue();
    logFactory = new LoggerLogFactory();
  }

  private static class InternalStreamHandler extends StreamHandler
  {
    InternalStreamHandler(OutputStream paramOutputStream)
    {
      super(paramOutputStream, new SimpleFormatter());
    }

    public void publish(LogRecord paramLogRecord)
    {
      super.publish(paramLogRecord);
      flush();
    }

    public void close()
    {
      flush();
    }
  }

  private static abstract interface LogFactory
  {
    public abstract Log createLog(String paramString1, String paramString2, Level paramLevel);
  }

  private static class LogStreamLog extends Log
  {
    private final LogStream stream;
    private int levelValue = Level.OFF.intValue();

    private LogStreamLog(LogStream paramLogStream, Level paramLevel)
    {
      if ((paramLogStream != null) && (paramLevel != null))
        this.levelValue = paramLevel.intValue();
      this.stream = paramLogStream;
    }

    public synchronized boolean isLoggable(Level paramLevel)
    {
      return (paramLevel.intValue() >= this.levelValue);
    }

    public void log(Level paramLevel, String paramString)
    {
      if (isLoggable(paramLevel))
      {
        String[] arrayOfString = Log.access$200();
        this.stream.println(unqualifiedName(arrayOfString[0]) + "." + arrayOfString[1] + ": " + paramString);
      }
    }

    public void log(Level paramLevel, String paramString, Throwable paramThrowable)
    {
      if (isLoggable(paramLevel))
        synchronized (this.stream)
        {
          String[] arrayOfString = Log.access$200();
          this.stream.println(unqualifiedName(arrayOfString[0]) + "." + arrayOfString[1] + ": " + paramString);
          paramThrowable.printStackTrace(this.stream);
        }
    }

    public PrintStream getPrintStream()
    {
      return this.stream;
    }

    public synchronized void setOutputStream(OutputStream paramOutputStream)
    {
      if (paramOutputStream != null)
      {
        if (VERBOSE.intValue() < this.levelValue)
          this.levelValue = VERBOSE.intValue();
        this.stream.setOutputStream(paramOutputStream);
      }
      else
      {
        this.levelValue = Level.OFF.intValue();
      }
    }

    private static String unqualifiedName(String paramString)
    {
      int i = paramString.lastIndexOf(".");
      if (i >= 0)
        paramString = paramString.substring(i + 1);
      paramString = paramString.replace('$', '.');
      return paramString;
    }
  }

  private static class LogStreamLogFactory
  implements Log.LogFactory
  {
    public Log createLog(String paramString1, String paramString2, Level paramLevel)
    {
      LogStream localLogStream = null;
      if (paramString2 != null)
        localLogStream = LogStream.log(paramString2);
      return new Log.LogStreamLog(localLogStream, paramLevel, null);
    }
  }

  private static class LoggerLog extends Log
  {
    private static final Handler alternateConsole = (Handler)AccessController.doPrivileged(new PrivilegedAction()
    {
      public Object run()
      {
        Log.InternalStreamHandler localInternalStreamHandler = new Log.InternalStreamHandler(System.err);
        localInternalStreamHandler.setLevel(Level.ALL);
        return localInternalStreamHandler;
      }
    });
    private Log.InternalStreamHandler copyHandler = null;
    private final Logger logger;
    private Log.LoggerPrintStream loggerSandwich;

    private LoggerLog(Logger paramLogger, Level paramLevel)
    {
      this.logger = paramLogger;
      if (paramLevel != null)
        AccessController.doPrivileged(new PrivilegedAction(this, paramLogger, paramLevel)
        {
          public Object run()
          {
            if (!(this.val$logger.isLoggable(this.val$level)))
              this.val$logger.setLevel(this.val$level);
            this.val$logger.addHandler(Log.LoggerLog.access$100());
            return null;
          }
        });
    }

    public boolean isLoggable(Level paramLevel)
    {
      return this.logger.isLoggable(paramLevel);
    }

    public void log(Level paramLevel, String paramString)
    {
      if (isLoggable(paramLevel))
      {
        String[] arrayOfString = Log.access$200();
        this.logger.logp(paramLevel, arrayOfString[0], arrayOfString[1], Thread.currentThread().getName() + ": " + paramString);
      }
    }

    public void log(Level paramLevel, String paramString, Throwable paramThrowable)
    {
      if (isLoggable(paramLevel))
      {
        String[] arrayOfString = Log.access$200();
        this.logger.logp(paramLevel, arrayOfString[0], arrayOfString[1], Thread.currentThread().getName() + ": " + paramString, paramThrowable);
      }
    }

    public synchronized void setOutputStream(OutputStream paramOutputStream)
    {
      if (paramOutputStream != null)
      {
        if (!(this.logger.isLoggable(VERBOSE)))
          this.logger.setLevel(VERBOSE);
        this.copyHandler = new Log.InternalStreamHandler(paramOutputStream);
        this.copyHandler.setLevel(Log.VERBOSE);
        this.logger.addHandler(this.copyHandler);
      }
      else
      {
        if (this.copyHandler != null)
          this.logger.removeHandler(this.copyHandler);
        this.copyHandler = null;
      }
    }

    public synchronized PrintStream getPrintStream()
    {
      if (this.loggerSandwich == null)
        this.loggerSandwich = new Log.LoggerPrintStream(this.logger, null);
      return this.loggerSandwich;
    }
  }

  private static class LoggerLogFactory
  implements Log.LogFactory
  {
    public Log createLog(String paramString1, String paramString2, Level paramLevel)
    {
      Logger localLogger = Logger.getLogger(paramString1);
      return new Log.LoggerLog(localLogger, paramLevel, null);
    }
  }

  private static class LoggerPrintStream extends PrintStream
  {
    private final Logger logger;
    private int last = -1;
    private final ByteArrayOutputStream bufOut = (ByteArrayOutputStream)this.out;

    private LoggerPrintStream(Logger paramLogger)
    {
      super(new ByteArrayOutputStream());
      this.logger = paramLogger;
    }

    public void write(int paramInt)
    {
      if ((this.last == 13) && (paramInt == 10))
      {
        this.last = -1;
        return;
      }
      if ((paramInt == 10) || (paramInt == 13))
        try
        {
          String str = Thread.currentThread().getName() + ": " + this.bufOut.toString();
          this.logger.logp(Level.INFO, "LogStream", "print", str);
        }
        finally
        {
          this.bufOut.reset();
        }
      else
        super.write(paramInt);
      this.last = paramInt;
    }

    public void write(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
    {
      if (paramInt2 < 0)
        throw new ArrayIndexOutOfBoundsException(paramInt2);
      for (int i = 0; i < paramInt2; ++i)
        write(paramArrayOfByte[(paramInt1 + i)]);
    }

    public String toString()
    {
      return "RMI";
    }
  }
}