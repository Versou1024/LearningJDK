package sun.rmi.log;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.lang.reflect.Constructor;
import java.security.AccessController;
import java.security.PrivilegedAction;
import sun.security.action.GetBooleanAction;
import sun.security.action.GetPropertyAction;

public class ReliableLog
{
  public static final int PreferredMajorVersion = 0;
  public static final int PreferredMinorVersion = 2;
  private boolean Debug;
  private static String snapshotPrefix = "Snapshot.";
  private static String logfilePrefix = "Logfile.";
  private static String versionFile = "Version_Number";
  private static String newVersionFile = "New_Version_Number";
  private static int intBytes = 4;
  private static long diskPageSize = 512L;
  private File dir;
  private int version;
  private String logName;
  private LogFile log;
  private long snapshotBytes;
  private long logBytes;
  private int logEntries;
  private long lastSnapshot;
  private long lastLog;
  private LogHandler handler;
  private final byte[] intBuf;
  private int majorFormatVersion;
  private int minorFormatVersion;
  private static final Constructor<? extends LogFile> logClassConstructor = getLogClassConstructor();

  public ReliableLog(String paramString, LogHandler paramLogHandler, boolean paramBoolean)
    throws IOException
  {
    this.Debug = false;
    this.version = 0;
    this.logName = null;
    this.log = null;
    this.snapshotBytes = 3412046827397054464L;
    this.logBytes = 3412046827397054464L;
    this.logEntries = 0;
    this.lastSnapshot = 3412046827397054464L;
    this.lastLog = 3412046827397054464L;
    this.intBuf = new byte[4];
    this.majorFormatVersion = 0;
    this.minorFormatVersion = 0;
    this.Debug = ((Boolean)AccessController.doPrivileged(new GetBooleanAction("sun.rmi.log.debug"))).booleanValue();
    this.dir = new File(paramString);
    if ((((!(this.dir.exists())) || (!(this.dir.isDirectory())))) && (!(this.dir.mkdir())))
      throw new IOException("could not create directory for log: " + paramString);
    this.handler = paramLogHandler;
    this.lastSnapshot = 3412046827397054464L;
    this.lastLog = 3412046827397054464L;
    getVersion();
    if (this.version == 0)
      try
      {
        snapshot(paramLogHandler.initialSnapshot());
      }
      catch (IOException localIOException)
      {
        throw localIOException;
      }
      catch (Exception localException)
      {
        throw new IOException("initial snapshot failed with exception: " + localException);
      }
  }

  public ReliableLog(String paramString, LogHandler paramLogHandler)
    throws IOException
  {
    this(paramString, paramLogHandler, false);
  }

  // ERROR //
  public synchronized Object recover()
    throws IOException
  {
    // Byte code:
    //   0: aload_0
    //   1: getfield 385	sun/rmi/log/ReliableLog:Debug	Z
    //   4: ifeq +11 -> 15
    //   7: getstatic 374	java/lang/System:err	Ljava/io/PrintStream;
    //   10: ldc 24
    //   12: invokevirtual 418	java/io/PrintStream:println	(Ljava/lang/String;)V
    //   15: aload_0
    //   16: getfield 379	sun/rmi/log/ReliableLog:version	I
    //   19: ifne +5 -> 24
    //   22: aconst_null
    //   23: areturn
    //   24: aload_0
    //   25: getstatic 391	sun/rmi/log/ReliableLog:snapshotPrefix	Ljava/lang/String;
    //   28: invokespecial 464	sun/rmi/log/ReliableLog:versionName	(Ljava/lang/String;)Ljava/lang/String;
    //   31: astore_2
    //   32: new 224	java/io/File
    //   35: dup
    //   36: aload_2
    //   37: invokespecial 408	java/io/File:<init>	(Ljava/lang/String;)V
    //   40: astore_3
    //   41: new 219	java/io/BufferedInputStream
    //   44: dup
    //   45: new 225	java/io/FileInputStream
    //   48: dup
    //   49: aload_3
    //   50: invokespecial 409	java/io/FileInputStream:<init>	(Ljava/io/File;)V
    //   53: invokespecial 396	java/io/BufferedInputStream:<init>	(Ljava/io/InputStream;)V
    //   56: astore 4
    //   58: aload_0
    //   59: getfield 385	sun/rmi/log/ReliableLog:Debug	Z
    //   62: ifeq +28 -> 90
    //   65: getstatic 374	java/lang/System:err	Ljava/io/PrintStream;
    //   68: new 237	java/lang/StringBuilder
    //   71: dup
    //   72: invokespecial 430	java/lang/StringBuilder:<init>	()V
    //   75: ldc 26
    //   77: invokevirtual 434	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   80: aload_2
    //   81: invokevirtual 434	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   84: invokevirtual 431	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   87: invokevirtual 418	java/io/PrintStream:println	(Ljava/lang/String;)V
    //   90: aload_0
    //   91: getfield 394	sun/rmi/log/ReliableLog:handler	Lsun/rmi/log/LogHandler;
    //   94: aload 4
    //   96: invokevirtual 439	sun/rmi/log/LogHandler:recover	(Ljava/io/InputStream;)Ljava/lang/Object;
    //   99: astore_1
    //   100: goto +71 -> 171
    //   103: astore 5
    //   105: aload 5
    //   107: athrow
    //   108: astore 5
    //   110: aload_0
    //   111: getfield 385	sun/rmi/log/ReliableLog:Debug	Z
    //   114: ifeq +29 -> 143
    //   117: getstatic 374	java/lang/System:err	Ljava/io/PrintStream;
    //   120: new 237	java/lang/StringBuilder
    //   123: dup
    //   124: invokespecial 430	java/lang/StringBuilder:<init>	()V
    //   127: ldc 27
    //   129: invokevirtual 434	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   132: aload 5
    //   134: invokevirtual 433	java/lang/StringBuilder:append	(Ljava/lang/Object;)Ljava/lang/StringBuilder;
    //   137: invokevirtual 431	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   140: invokevirtual 418	java/io/PrintStream:println	(Ljava/lang/String;)V
    //   143: new 227	IOException
    //   146: dup
    //   147: new 237	java/lang/StringBuilder
    //   150: dup
    //   151: invokespecial 430	java/lang/StringBuilder:<init>	()V
    //   154: ldc 17
    //   156: invokevirtual 434	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   159: aload 5
    //   161: invokevirtual 433	java/lang/StringBuilder:append	(Ljava/lang/Object;)Ljava/lang/StringBuilder;
    //   164: invokevirtual 431	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   167: invokespecial 414	IOException:<init>	(Ljava/lang/String;)V
    //   170: athrow
    //   171: aload_0
    //   172: aload_3
    //   173: invokevirtual 402	java/io/File:length	()J
    //   176: putfield 384	sun/rmi/log/ReliableLog:snapshotBytes	J
    //   179: aload 4
    //   181: invokevirtual 417	java/io/InputStream:close	()V
    //   184: goto +13 -> 197
    //   187: astore 6
    //   189: aload 4
    //   191: invokevirtual 417	java/io/InputStream:close	()V
    //   194: aload 6
    //   196: athrow
    //   197: aload_0
    //   198: aload_1
    //   199: invokespecial 462	sun/rmi/log/ReliableLog:recoverUpdates	(Ljava/lang/Object;)Ljava/lang/Object;
    //   202: areturn
    //
    // Exception table:
    //   from	to	target	type
    //   90	100	103	IOException
    //   90	100	108	java/lang/Exception
    //   90	179	187	finally
    //   187	189	187	finally
  }

  public synchronized void update(Object paramObject)
    throws IOException
  {
    update(paramObject, true);
  }

  public synchronized void update(Object paramObject, boolean paramBoolean)
    throws IOException
  {
    if (this.log == null)
      throw new IOException("log is inaccessible, it may have been corrupted or closed");
    long l1 = this.log.getFilePointer();
    boolean bool = this.log.checkSpansBoundary(l1);
    writeInt(this.log, (bool) ? -2147483648 : 0);
    try
    {
      this.handler.writeUpdate(new LogOutputStream(this.log), paramObject);
    }
    catch (IOException localIOException)
    {
      throw localIOException;
    }
    catch (Exception localException)
    {
      throw ((IOException)new IOException("write update failed").initCause(localException));
    }
    this.log.sync();
    long l2 = this.log.getFilePointer();
    int i = (int)(l2 - l1 - intBytes);
    this.log.seek(l1);
    if (bool)
    {
      writeInt(this.log, i | 0x80000000);
      this.log.sync();
      this.log.seek(l1);
      this.log.writeByte(i >> 24);
      this.log.sync();
    }
    else
    {
      writeInt(this.log, i);
      this.log.sync();
    }
    this.log.seek(l2);
    this.logBytes = l2;
    this.lastLog = System.currentTimeMillis();
    this.logEntries += 1;
  }

  private static Constructor<? extends LogFile> getLogClassConstructor()
  {
    String str = (String)AccessController.doPrivileged(new GetPropertyAction("sun.rmi.log.class"));
    if (str != null)
      try
      {
        ClassLoader localClassLoader = (ClassLoader)AccessController.doPrivileged(new PrivilegedAction()
        {
          public ClassLoader run()
          {
            return ClassLoader.getSystemClassLoader();
          }
        });
        Class localClass = localClassLoader.loadClass(str);
        if (LogFile.class.isAssignableFrom(localClass))
          return localClass.getConstructor(new Class[] { String.class, String.class });
      }
      catch (Exception localException)
      {
        System.err.println("Exception occurred:");
        localException.printStackTrace();
      }
    return null;
  }

  // ERROR //
  public synchronized void snapshot(Object paramObject)
    throws IOException
  {
    // Byte code:
    //   0: aload_0
    //   1: getfield 379	sun/rmi/log/ReliableLog:version	I
    //   4: istore_2
    //   5: aload_0
    //   6: invokespecial 450	sun/rmi/log/ReliableLog:incrVersion	()V
    //   9: aload_0
    //   10: getstatic 391	sun/rmi/log/ReliableLog:snapshotPrefix	Ljava/lang/String;
    //   13: invokespecial 464	sun/rmi/log/ReliableLog:versionName	(Ljava/lang/String;)Ljava/lang/String;
    //   16: astore_3
    //   17: new 224	java/io/File
    //   20: dup
    //   21: aload_3
    //   22: invokespecial 408	java/io/File:<init>	(Ljava/lang/String;)V
    //   25: astore 4
    //   27: new 226	java/io/FileOutputStream
    //   30: dup
    //   31: aload 4
    //   33: invokespecial 412	java/io/FileOutputStream:<init>	(Ljava/io/File;)V
    //   36: astore 5
    //   38: aload_0
    //   39: getfield 394	sun/rmi/log/ReliableLog:handler	Lsun/rmi/log/LogHandler;
    //   42: aload 5
    //   44: aload_1
    //   45: invokevirtual 440	sun/rmi/log/LogHandler:snapshot	(Ljava/io/OutputStream;Ljava/lang/Object;)V
    //   48: goto +57 -> 105
    //   51: astore 6
    //   53: aload 6
    //   55: athrow
    //   56: astore 6
    //   58: new 227	IOException
    //   61: dup
    //   62: new 237	java/lang/StringBuilder
    //   65: dup
    //   66: invokespecial 430	java/lang/StringBuilder:<init>	()V
    //   69: ldc 31
    //   71: invokevirtual 434	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   74: aload 6
    //   76: invokevirtual 428	java/lang/Object:getClass	()Ljava/lang/Class;
    //   79: invokevirtual 421	java/lang/Class:getName	()Ljava/lang/String;
    //   82: invokevirtual 434	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   85: ldc 5
    //   87: invokevirtual 434	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   90: aload 6
    //   92: invokevirtual 425	java/lang/Exception:getMessage	()Ljava/lang/String;
    //   95: invokevirtual 434	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   98: invokevirtual 431	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   101: invokespecial 414	IOException:<init>	(Ljava/lang/String;)V
    //   104: athrow
    //   105: aload_0
    //   106: invokestatic 435	java/lang/System:currentTimeMillis	()J
    //   109: putfield 382	sun/rmi/log/ReliableLog:lastSnapshot	J
    //   112: aload 5
    //   114: invokevirtual 411	java/io/FileOutputStream:close	()V
    //   117: aload_0
    //   118: aload 4
    //   120: invokevirtual 402	java/io/File:length	()J
    //   123: putfield 384	sun/rmi/log/ReliableLog:snapshotBytes	J
    //   126: goto +22 -> 148
    //   129: astore 7
    //   131: aload 5
    //   133: invokevirtual 411	java/io/FileOutputStream:close	()V
    //   136: aload_0
    //   137: aload 4
    //   139: invokevirtual 402	java/io/File:length	()J
    //   142: putfield 384	sun/rmi/log/ReliableLog:snapshotBytes	J
    //   145: aload 7
    //   147: athrow
    //   148: aload_0
    //   149: iconst_1
    //   150: invokespecial 454	sun/rmi/log/ReliableLog:openLogFile	(Z)V
    //   153: aload_0
    //   154: iconst_1
    //   155: invokespecial 455	sun/rmi/log/ReliableLog:writeVersionFile	(Z)V
    //   158: aload_0
    //   159: invokespecial 446	sun/rmi/log/ReliableLog:commitToNewVersion	()V
    //   162: aload_0
    //   163: iload_2
    //   164: invokespecial 453	sun/rmi/log/ReliableLog:deleteSnapshot	(I)V
    //   167: aload_0
    //   168: iload_2
    //   169: invokespecial 452	sun/rmi/log/ReliableLog:deleteLogFile	(I)V
    //   172: return
    //
    // Exception table:
    //   from	to	target	type
    //   38	48	51	IOException
    //   38	48	56	java/lang/Exception
    //   38	112	129	finally
    //   129	131	129	finally
  }

  public synchronized void close()
    throws IOException
  {
    if (this.log == null)
      return;
    try
    {
      this.log.close();
    }
    finally
    {
      this.log = null;
    }
  }

  public long snapshotSize()
  {
    return this.snapshotBytes;
  }

  public long logSize()
  {
    return this.logBytes;
  }

  private void writeInt(DataOutput paramDataOutput, int paramInt)
    throws IOException
  {
    this.intBuf[0] = (byte)(paramInt >> 24);
    this.intBuf[1] = (byte)(paramInt >> 16);
    this.intBuf[2] = (byte)(paramInt >> 8);
    this.intBuf[3] = (byte)paramInt;
    paramDataOutput.write(this.intBuf);
  }

  private String fName(String paramString)
  {
    return this.dir.getPath() + File.separator + paramString;
  }

  private String versionName(String paramString)
  {
    return versionName(paramString, 0);
  }

  private String versionName(String paramString, int paramInt)
  {
    paramInt = (paramInt == 0) ? this.version : paramInt;
    return fName(paramString) + String.valueOf(paramInt);
  }

  private void incrVersion()
  {
    do
      this.version += 1;
    while (this.version == 0);
  }

  private void deleteFile(String paramString)
    throws IOException
  {
    File localFile = new File(paramString);
    if (!(localFile.delete()))
      throw new IOException("couldn't remove file: " + paramString);
  }

  private void deleteNewVersionFile()
    throws IOException
  {
    deleteFile(fName(newVersionFile));
  }

  private void deleteSnapshot(int paramInt)
    throws IOException
  {
    if (paramInt == 0)
      return;
    deleteFile(versionName(snapshotPrefix, paramInt));
  }

  private void deleteLogFile(int paramInt)
    throws IOException
  {
    if (paramInt == 0)
      return;
    deleteFile(versionName(logfilePrefix, paramInt));
  }

  private void openLogFile(boolean paramBoolean)
    throws IOException
  {
    try
    {
      close();
    }
    catch (IOException localIOException)
    {
    }
    this.logName = versionName(logfilePrefix);
    try
    {
      this.log = ((logClassConstructor == null) ? new LogFile(this.logName, "rw") : (LogFile)logClassConstructor.newInstance(new Object[] { this.logName, "rw" }));
    }
    catch (Exception localException)
    {
      throw ((IOException)new IOException("unable to construct LogFile instance").initCause(localException));
    }
    if (paramBoolean)
      initializeLogFile();
  }

  private void initializeLogFile()
    throws IOException
  {
    this.log.setLength(3412047170994438144L);
    this.majorFormatVersion = 0;
    writeInt(this.log, 0);
    this.minorFormatVersion = 2;
    writeInt(this.log, 2);
    this.logBytes = (intBytes * 2);
    this.logEntries = 0;
  }

  private void writeVersionFile(boolean paramBoolean)
    throws IOException
  {
    String str;
    if (paramBoolean)
      str = newVersionFile;
    else
      str = versionFile;
    DataOutputStream localDataOutputStream = new DataOutputStream(new FileOutputStream(fName(str)));
    writeInt(localDataOutputStream, this.version);
    localDataOutputStream.close();
  }

  private void createFirstVersion()
    throws IOException
  {
    this.version = 0;
    writeVersionFile(false);
  }

  private void commitToNewVersion()
    throws IOException
  {
    writeVersionFile(false);
    deleteNewVersionFile();
  }

  private int readVersion(String paramString)
    throws IOException
  {
    DataInputStream localDataInputStream = new DataInputStream(new FileInputStream(paramString));
    try
    {
      int i = localDataInputStream.readInt();
      return i;
    }
    finally
    {
      localDataInputStream.close();
    }
  }

  private void getVersion()
    throws IOException
  {
    try
    {
      this.version = readVersion(fName(newVersionFile));
      commitToNewVersion();
    }
    catch (IOException localIOException1)
    {
      try
      {
        deleteNewVersionFile();
      }
      catch (IOException localIOException2)
      {
      }
      try
      {
        this.version = readVersion(fName(versionFile));
      }
      catch (IOException localIOException3)
      {
        createFirstVersion();
      }
    }
  }

  private Object recoverUpdates(Object paramObject)
    throws IOException
  {
    this.logBytes = 3412046827397054464L;
    this.logEntries = 0;
    if (this.version == 0)
      return paramObject;
    String str = versionName(logfilePrefix);
    BufferedInputStream localBufferedInputStream = new BufferedInputStream(new FileInputStream(str));
    DataInputStream localDataInputStream = new DataInputStream(localBufferedInputStream);
    if (this.Debug)
      System.err.println("log.debug: reading updates from " + str);
    try
    {
      this.majorFormatVersion = localDataInputStream.readInt();
      this.logBytes += intBytes;
      this.minorFormatVersion = localDataInputStream.readInt();
      this.logBytes += intBytes;
    }
    catch (EOFException localEOFException1)
    {
      openLogFile(true);
      localBufferedInputStream = null;
    }
    if (this.majorFormatVersion != 0)
    {
      if (this.Debug)
        System.err.println("log.debug: major version mismatch: " + this.majorFormatVersion + "." + this.minorFormatVersion);
      throw new IOException("Log file " + this.logName + " has a " + "version " + this.majorFormatVersion + "." + this.minorFormatVersion + " format, and this implementation " + " understands only version " + 0 + "." + 2);
    }
    try
    {
      while (localBufferedInputStream != null)
      {
        int i = 0;
        try
        {
          i = localDataInputStream.readInt();
        }
        catch (EOFException localEOFException2)
        {
          if (this.Debug)
            System.err.println("log.debug: log was sync'd cleanly");
          break label509:
        }
        if (i <= 0)
        {
          if (!(this.Debug))
            break;
          System.err.println("log.debug: last update incomplete, updateLen = 0x" + Integer.toHexString(i));
          break;
        }
        if (localBufferedInputStream.available() < i)
        {
          if (!(this.Debug))
            break;
          System.err.println("log.debug: log was truncated");
          break;
        }
        if (this.Debug)
          System.err.println("log.debug: rdUpdate size " + i);
        try
        {
          paramObject = this.handler.readUpdate(new LogInputStream(localBufferedInputStream, i), paramObject);
        }
        catch (IOException localIOException)
        {
          throw localIOException;
        }
        catch (Exception localException)
        {
          localException.printStackTrace();
          throw new IOException("read update failed with exception: " + localException);
        }
        this.logBytes += intBytes + i;
        this.logEntries += 1;
      }
    }
    finally
    {
      if (localBufferedInputStream != null)
        label509: localBufferedInputStream.close();
    }
    if (this.Debug)
      System.err.println("log.debug: recovered updates: " + this.logEntries);
    openLogFile(false);
    if (this.log == null)
      throw new IOException("rmid's log is inaccessible, it may have been corrupted or closed");
    this.log.seek(this.logBytes);
    this.log.setLength(this.logBytes);
    return paramObject;
  }

  public static class LogFile extends RandomAccessFile
  {
    private final FileDescriptor fd = getFD();

    public LogFile(String paramString1, String paramString2)
      throws FileNotFoundException, IOException
    {
      super(paramString1, paramString2);
    }

    protected void sync()
      throws IOException
    {
      this.fd.sync();
    }

    protected boolean checkSpansBoundary(long paramLong)
    {
      return (paramLong % 512L > 508L);
    }
  }
}