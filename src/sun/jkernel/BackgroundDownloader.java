package sun.jkernel;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

public class BackgroundDownloader
{
  public static final String BACKGROUND_DOWNLOAD_PROPERTY = "kernel.background.download";
  public static final String PID_PATH = "tmp" + File.separator + "background.pid";
  private static final int WAIT_TIME = 10000;
  private static Mutex backgroundMutex;

  static synchronized Mutex getBackgroundMutex()
  {
    if (backgroundMutex == null)
      backgroundMutex = Mutex.create(DownloadManager.MUTEX_PREFIX + "background");
    return backgroundMutex;
  }

  private static void doBackgroundDownloads()
  {
    if (DownloadManager.isJREComplete())
      return;
    if (getBackgroundMutex().acquire(0))
    {
      try
      {
        writePid();
        Thread.sleep(10000L);
        DownloadManager.doBackgroundDownloads(false);
        DownloadManager.performCompletionIfNeeded();
      }
      catch (InterruptedException localInterruptedException)
      {
      }
      finally
      {
        getBackgroundMutex().release();
      }
    }
    else
    {
      System.err.println("Unable to acquire background download mutex.");
      System.exit(1);
    }
  }

  private static void writePid()
  {
    File localFile;
    try
    {
      localFile = new File(DownloadManager.getBundlePath(), PID_PATH);
      localFile.getParentFile().mkdirs();
      PrintStream localPrintStream = new PrintStream(new FileOutputStream(localFile));
      localFile.deleteOnExit();
      localPrintStream.println(DownloadManager.getCurrentProcessId());
      localPrintStream.close();
    }
    catch (IOException localIOException)
    {
      localIOException.printStackTrace();
      System.exit(1);
    }
  }

  private static void send(InputStream paramInputStream, OutputStream paramOutputStream)
    throws IOException
  {
    byte[] arrayOfByte = new byte[2048];
    while ((i = paramInputStream.read(arrayOfByte)) > 0)
    {
      int i;
      paramOutputStream.write(arrayOfByte, 0, i);
    }
  }

  public static boolean getBackgroundDownloadProperty()
  {
    boolean bool = getBackgroundDownloadKey();
    if (System.getProperty("kernel.background.download") != null)
      bool = Boolean.valueOf(System.getProperty("kernel.background.download")).booleanValue();
    return bool;
  }

  static native boolean getBackgroundDownloadKey();

  static void startBackgroundDownloads()
  {
    if (!(getBackgroundDownloadProperty()))
      return;
    if (System.err == null)
      try
      {
        Thread.sleep(1000L);
      }
      catch (InterruptedException localInterruptedException)
      {
        return;
      }
    try
    {
      String str1 = "-Dkernel.background.download=false -Xmx256m";
      String str2 = DownloadManager.getBaseDownloadURL();
      if ((str2 != null) && (!(str2.equals(DownloadManager.DEFAULT_DOWNLOAD_URL))))
        str1 = str1 + " -Dkernel.download.url=" + str2;
      str1 = str1 + " sun.jkernel.BackgroundDownloader";
      Process localProcess = Runtime.getRuntime().exec("\"" + new File(System.getProperty("java.home"), new StringBuilder().append("bin").append(File.separator).append("java.exe").toString()) + "\" " + str1);
      1 local1 = new Thread("kernelOutputReader", localProcess)
      {
        public void run()
        {
          InputStream localInputStream;
          try
          {
            localInputStream = this.val$jvm.getInputStream();
            BackgroundDownloader.access$000(localInputStream, new PrintStream(new ByteArrayOutputStream()));
          }
          catch (IOException localIOException)
          {
            localIOException.printStackTrace();
          }
        }
      };
      local1.setDaemon(true);
      local1.start();
      2 local2 = new Thread("kernelErrorReader", localProcess)
      {
        public void run()
        {
          InputStream localInputStream;
          try
          {
            localInputStream = this.val$jvm.getErrorStream();
            BackgroundDownloader.access$000(localInputStream, new PrintStream(new ByteArrayOutputStream()));
          }
          catch (IOException localIOException)
          {
            localIOException.printStackTrace();
          }
        }
      };
      local2.setDaemon(true);
      local2.start();
    }
    catch (Exception localException)
    {
      localException.printStackTrace();
    }
  }

  public static void main(String[] paramArrayOfString)
  {
    doBackgroundDownloads();
  }
}