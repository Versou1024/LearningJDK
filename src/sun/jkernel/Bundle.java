package sun.jkernel;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpRetryException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.zip.GZIPInputStream;

public class Bundle
{
  private static final String BUNDLE_JAR_ENTRY_NAME = "classes.jar";
  protected static final int NOT_DOWNLOADED = 0;
  protected static final int QUEUED = 1;
  protected static final int DOWNLOADED = 2;
  protected static final int INSTALLED = 3;
  private static ExecutorService threadPool;
  static final int THREADS;
  private static Mutex receiptsMutex;
  private static Map<String, Bundle> bundles;
  static Set<String> receipts;
  private static int bytesDownloaded;
  private static File receiptPath;
  private static int receiptsSize;
  private String name;
  private File localPath;
  private File jarPath;
  private File lowJarPath;
  private File lowJavaPath = null;
  protected int state;
  protected boolean deleteOnInstall = true;

  public static native boolean extraCompress(String paramString1, String paramString2)
    throws IOException;

  public static native boolean extraUncompress(String paramString1, String paramString2)
    throws IOException;

  private static Mutex getReceiptsMutex()
  {
    if (receiptsMutex == null)
      receiptsMutex = Mutex.create(DownloadManager.MUTEX_PREFIX + "receipts");
    return receiptsMutex;
  }

  static synchronized void loadReceipts()
  {
    getReceiptsMutex().acquire();
    try
    {
      if (receiptPath.exists())
      {
        int i = (int)receiptPath.length();
        if (i != receiptsSize)
        {
          DataInputStream localDataInputStream = null;
          try
          {
            receipts.clear();
            Object localObject1 = DownloadManager.getBundleNames();
            int j = localObject1.length;
            for (int k = 0; k < j; ++k)
            {
              String str = localObject1[k];
              if ("true".equals(DownloadManager.getBundleProperty(str, "install")))
                receipts.add(str);
            }
            if (receiptPath.exists())
            {
              localDataInputStream = new DataInputStream(new BufferedInputStream(new FileInputStream(receiptPath)));
              while ((localObject1 = localDataInputStream.readLine()) != null)
                receipts.add(((String)localObject1).trim());
            }
            receiptsSize = i;
          }
          catch (IOException localIOException1)
          {
            DownloadManager.log(localIOException1);
          }
          finally
          {
            if (localDataInputStream != null)
              try
              {
                localDataInputStream.close();
              }
              catch (IOException localIOException2)
              {
                DownloadManager.log(localIOException2);
              }
          }
        }
      }
    }
    finally
    {
      getReceiptsMutex().release();
    }
  }

  public static synchronized Bundle getBundle(String paramString)
    throws IOException
  {
    Bundle localBundle = (Bundle)bundles.get(paramString);
    if ((localBundle == null) && (((paramString.equals("merged")) || (Arrays.asList(DownloadManager.getBundleNames()).contains(paramString)))))
    {
      localBundle = new Bundle();
      localBundle.name = paramString;
      if (DownloadManager.isWindowsVista())
      {
        localBundle.localPath = new File(DownloadManager.getLocalLowTempBundlePath(), paramString + ".zip");
        localBundle.lowJavaPath = new File(DownloadManager.getLocalLowKernelJava() + paramString);
      }
      else
      {
        localBundle.localPath = new File(DownloadManager.getBundlePath(), paramString + ".zip");
      }
      String str = DownloadManager.getBundleProperty(paramString, "jarpath");
      if (str != null)
      {
        if (DownloadManager.isWindowsVista())
          localBundle.lowJarPath = new File(DownloadManager.getLocalLowKernelJava() + paramString, str);
        localBundle.jarPath = new File(DownloadManager.JAVA_HOME, str);
      }
      else
      {
        if (DownloadManager.isWindowsVista())
          localBundle.lowJarPath = new File(DownloadManager.getLocalLowKernelJava() + paramString + "\\lib\\bundles", paramString + ".jar");
        localBundle.jarPath = new File(DownloadManager.getBundlePath(), paramString + ".jar");
      }
      bundles.put(paramString, localBundle);
    }
    return localBundle;
  }

  public String getName()
  {
    return this.name;
  }

  public void setName(String paramString)
  {
    this.name = paramString;
  }

  public File getLocalPath()
  {
    return this.localPath;
  }

  public void setLocalPath(File paramFile)
  {
    this.localPath = paramFile;
  }

  public File getJarPath()
  {
    return this.jarPath;
  }

  public void setJarPath(File paramFile)
  {
    this.jarPath = paramFile;
  }

  public int getSize()
  {
    return Integer.valueOf(DownloadManager.getBundleProperty(getName(), "size")).intValue();
  }

  public boolean getDeleteOnInstall()
  {
    return this.deleteOnInstall;
  }

  public void setDeleteOnInstall(boolean paramBoolean)
  {
    this.deleteOnInstall = paramBoolean;
  }

  protected void updateState()
  {
    synchronized (Bundle.class)
    {
      loadReceipts();
      if ((receipts.contains(this.name)) || ("true".equals(DownloadManager.getBundleProperty(this.name, "install"))))
        this.state = 3;
      else if (this.localPath.exists())
        this.state = 2;
    }
  }

  private String getURL(boolean paramBoolean)
    throws IOException
  {
    Properties localProperties = DownloadManager.getBundleURLs(paramBoolean);
    String str = localProperties.getProperty(this.name + ".zip");
    if (str == null)
    {
      str = localProperties.getProperty(this.name);
      if (str == null)
      {
        DownloadManager.log("Unable to determine bundle URL for " + this);
        DownloadManager.log("Bundle URLs: " + localProperties);
        throw new NullPointerException("Unable to determine URL for bundle: " + this);
      }
    }
    return str;
  }

  private void download(boolean paramBoolean)
  {
    if (DownloadManager.isJREComplete())
      return;
    Mutex localMutex = Mutex.create(DownloadManager.MUTEX_PREFIX + this.name + ".download");
    localMutex.acquire();
    try
    {
      boolean bool;
      long l1 = System.currentTimeMillis();
      do
      {
        bool = false;
        updateState();
        if ((this.state == 2) || (this.state == 3))
        {
          jsr 735;
          return;
        }
        File localFile1 = null;
        try
        {
          String str;
          localFile1 = new File(this.localPath + ".tmp");
          if (DownloadManager.getBaseDownloadURL().equals("internal-resource/"))
          {
            str = "/" + this.name + ".zip";
            localObject1 = super.getClass().getResourceAsStream(str);
            if (localObject1 == null)
              throw new IOException("could not locate resource: " + str);
            FileOutputStream localFileOutputStream = new FileOutputStream(localFile1);
            DownloadManager.send((InputStream)localObject1, localFileOutputStream);
            ((InputStream)localObject1).close();
            localFileOutputStream.close();
          }
          else
          {
            try
            {
              str = getURL(paramBoolean);
              DownloadManager.log("Downloading from: " + str);
              DownloadManager.downloadFromURL(str, localFile1, this.name.replace('_', '.'), paramBoolean);
            }
            catch (HttpRetryException localHttpRetryException)
            {
              DownloadManager.flushBundleURLs();
              localObject1 = getURL(paramBoolean);
              DownloadManager.log("Retrying at new URL: " + ((String)localObject1));
              DownloadManager.downloadFromURL((String)localObject1, localFile1, this.name.replace('_', '.'), paramBoolean);
            }
          }
          if ((!(localFile1.exists())) || (localFile1.length() == 3412048459484626944L))
          {
            if (paramBoolean)
              DownloadManager.complete = true;
            DownloadManager.fatalError(0);
          }
          BundleCheck localBundleCheck = BundleCheck.getInstance(localFile1);
          Object localObject1 = BundleCheck.getInstance(this.name);
          if (((BundleCheck)localObject1).equals(localBundleCheck))
          {
            long l2 = localFile1.length();
            this.localPath.delete();
            File localFile2 = new File(localFile1.getPath() + ".jar0");
            if (!(extraUncompress(localFile1.getPath(), localFile2.getPath())))
            {
              if (DownloadManager.debug)
                DownloadManager.log("Uncompressing with GZIP");
              GZIPInputStream localGZIPInputStream = new GZIPInputStream(new BufferedInputStream(new FileInputStream(localFile1), 2048));
              BufferedOutputStream localBufferedOutputStream = new BufferedOutputStream(new FileOutputStream(localFile2), 2048);
              DownloadManager.send(localGZIPInputStream, localBufferedOutputStream);
              localGZIPInputStream.close();
              localBufferedOutputStream.close();
              if (!(localFile2.renameTo(this.localPath)))
                throw new IOException("unable to rename " + localFile2 + " to " + this.localPath);
            }
            else
            {
              if (DownloadManager.debug)
                DownloadManager.log("Uncompressing with LZMA");
              if (!(localFile2.renameTo(this.localPath)))
                throw new IOException("unable to rename " + localFile2 + " to " + this.localPath);
            }
            this.state = 2;
            bytesDownloaded = (int)(bytesDownloaded + l2);
            long l3 = System.currentTimeMillis() - l1;
            DownloadManager.log("Downloaded " + this.name + " in " + l3 + "ms.  Downloaded " + bytesDownloaded + " bytes this session.");
          }
          else
          {
            localFile1.delete();
            DownloadManager.log("DownloadManager: Security check failed for bundle " + this.name);
            if (paramBoolean)
              bool = DownloadManager.askUserToRetryDownloadOrQuit(0);
            if (!(bool))
              throw new RuntimeException("Failed bundle security check and user canceled");
          }
        }
        catch (IOException localIOException)
        {
          DownloadManager.log(localIOException);
        }
      }
      while (bool);
    }
    finally
    {
      localMutex.release();
    }
  }

  void queueDependencies(boolean paramBoolean)
  {
    String str;
    try
    {
      str = DownloadManager.getBundleProperty(this.name, "dependencies");
      if (str != null)
      {
        StringTokenizer localStringTokenizer = new StringTokenizer(str, " ,");
        while (localStringTokenizer.hasMoreTokens())
        {
          Bundle localBundle = getBundle(localStringTokenizer.nextToken());
          if ((localBundle != null) && (!(localBundle.isInstalled())))
          {
            if (DownloadManager.debug)
              DownloadManager.log("Queueing " + localBundle.name + " as a dependency of " + this.name + "...");
            localBundle.install(paramBoolean, true, false);
          }
        }
      }
    }
    catch (IOException localIOException)
    {
      DownloadManager.log(localIOException);
    }
  }

  static synchronized ExecutorService getThreadPool()
  {
    if (threadPool == null)
      threadPool = Executors.newFixedThreadPool(THREADS, new ThreadFactory()
      {
        public Thread newThread(Runnable paramRunnable)
        {
          Thread localThread = new Thread(paramRunnable);
          localThread.setDaemon(true);
          return localThread;
        }
      });
    return threadPool;
  }

  private void unpackBundle()
    throws IOException
  {
    File localFile1 = null;
    if (DownloadManager.isWindowsVista())
    {
      localFile1 = this.lowJarPath;
      localObject1 = localFile1.getParentFile();
      if (localObject1 != null)
        ((File)localObject1).mkdirs();
    }
    else
    {
      localFile1 = this.jarPath;
    }
    DownloadManager.log("Unpacking " + this + " to " + localFile1);
    Object localObject1 = new FileInputStream(this.localPath);
    2 local2 = new JarInputStream(this, (InputStream)localObject1)
    {
      public void close()
        throws IOException
      {
      }
    };
    try
    {
      File localFile2 = null;
      while ((localJarEntry = local2.getNextJarEntry()) != null)
      {
        JarEntry localJarEntry;
        File localFile3;
        Object localObject2;
        String str = localJarEntry.getName();
        if (str.equals("classes.pack"))
        {
          localFile3 = new File(localFile1 + ".pack");
          localFile3.getParentFile().mkdirs();
          DownloadManager.log("Writing temporary .pack file " + localFile3);
          localObject2 = new FileOutputStream(localFile3);
          try
          {
            DownloadManager.send(local2, (OutputStream)localObject2);
          }
          finally
          {
            ((OutputStream)localObject2).close();
          }
          localFile2 = new File(localFile1 + ".tmp");
          DownloadManager.log("Writing temporary .jar file " + localFile2);
          unpack(localFile3, localFile2);
          localFile3.delete();
        }
        else if (!(str.startsWith("META-INF")))
        {
          if (DownloadManager.isWindowsVista())
            localFile3 = new File(this.lowJavaPath, str.replace('/', File.separatorChar));
          else
            localFile3 = new File(DownloadManager.JAVA_HOME, str.replace('/', File.separatorChar));
          if (str.equals("classes.jar"))
            localFile3 = localFile1;
          localObject2 = new File(localFile3 + ".tmp");
          boolean bool = localFile3.exists();
          if (!(bool))
          {
            DownloadManager.log(localFile3 + ".mkdirs()");
            localFile3.getParentFile().mkdirs();
          }
          try
          {
            DownloadManager.log("Using temporary file " + localObject2);
            FileOutputStream localFileOutputStream = new FileOutputStream((File)localObject2);
            try
            {
              byte[] arrayOfByte = new byte[2048];
              while ((i = local2.read(arrayOfByte)) > 0)
              {
                int i;
                localFileOutputStream.write(arrayOfByte, 0, i);
              }
            }
            finally
            {
              localFileOutputStream.close();
            }
            if (bool)
              localFile3.delete();
            DownloadManager.log("Renaming from " + localObject2 + " to " + localFile3);
            if (!(((File)localObject2).renameTo(localFile3)))
              throw new IOException("unable to rename " + localObject2 + " to " + localFile3);
          }
          catch (IOException localIOException)
          {
            if (!(bool))
              throw localIOException;
          }
        }
      }
      if (localFile2 != null)
        if (localFile1.exists())
          localFile2.delete();
        else if (!(localFile2.renameTo(localFile1)))
          throw new IOException("unable to rename " + localFile2 + " to " + localFile1);
      if (DownloadManager.isWindowsVista())
      {
        DownloadManager.log("Using broker to move " + this.name);
        if (!(DownloadManager.moveDirWithBroker(DownloadManager.getKernelJREDir() + this.name)))
          throw new IOException("unable to create " + this.name);
        DownloadManager.log("Broker finished " + this.name);
      }
      DownloadManager.log("Finished unpacking " + this);
    }
    finally
    {
      ((InputStream)localObject1).close();
    }
    if (this.deleteOnInstall)
      this.localPath.delete();
  }

  public static void unpack(File paramFile1, File paramFile2)
    throws IOException
  {
    Process localProcess = Runtime.getRuntime().exec(DownloadManager.JAVA_HOME + File.separator + "bin" + File.separator + "unpack200 -Hoff \"" + paramFile1 + "\" \"" + paramFile2 + "\"");
    try
    {
      localProcess.waitFor();
    }
    catch (InterruptedException localInterruptedException)
    {
    }
  }

  public void install()
    throws IOException
  {
    install(true, false, true);
  }

  public synchronized void install(boolean paramBoolean1, boolean paramBoolean2, boolean paramBoolean3)
    throws IOException
  {
    if (DownloadManager.isJREComplete())
      return;
    if ((this.state == 0) || (this.state == 1))
    {
      if (this.state != 1)
      {
        DownloadManager.addToTotalDownloadSize(getSize());
        this.state = 1;
      }
      if (getThreadPool().isShutdown())
      {
        if ((this.state == 0) || (this.state == 1))
          doInstall(paramBoolean1, paramBoolean2);
      }
      else
      {
        Future localFuture = getThreadPool().submit(new Runnable(this, paramBoolean2, paramBoolean1)
        {
          public void run()
          {
            try
            {
              if ((this.this$0.state == 0) || (this.this$0.state == 1) || ((!(this.val$downloadOnly)) && (this.this$0.state == 2)))
                Bundle.access$000(this.this$0, this.val$showProgress, this.val$downloadOnly);
            }
            catch (IOException localIOException)
            {
            }
          }
        });
        queueDependencies(paramBoolean1);
        if (paramBoolean3)
          try
          {
            localFuture.get();
          }
          catch (Exception localException)
          {
            throw new Error(localException);
          }
      }
    }
    else if ((this.state == 2) && (!(paramBoolean2)))
    {
      doInstall(paramBoolean1, false);
    }
  }

  private void doInstall(boolean paramBoolean1, boolean paramBoolean2)
    throws IOException
  {
    Mutex localMutex = Mutex.create(DownloadManager.MUTEX_PREFIX + this.name + ".install");
    DownloadManager.bundleInstallStart();
    try
    {
      localMutex.acquire();
      updateState();
      if ((this.state == 0) || (this.state == 1))
        download(paramBoolean1);
      if ((this.state == 2) && (paramBoolean2))
      {
        jsr 132;
        return;
      }
      if (this.state == 3)
      {
        jsr 120;
        return;
      }
      if (this.state != 2)
        DownloadManager.fatalError(0);
      DownloadManager.log("Calling unpackBundle for " + this);
      unpackBundle();
      DownloadManager.log("Writing receipt for " + this);
      writeReceipt();
      updateState();
      DownloadManager.log("Finished installing " + this + ", state=" + this.state);
    }
    finally
    {
      if (this.lowJavaPath != null)
        this.lowJavaPath.delete();
      localMutex.release();
      DownloadManager.bundleInstallComplete();
    }
  }

  synchronized void setState(int paramInt)
  {
    this.state = paramInt;
  }

  public boolean isInstalled()
  {
    synchronized (Bundle.class)
    {
      updateState();
      return ((this.state == 3) ? 1 : false);
    }
  }

  private void writeReceipt()
  {
    getReceiptsMutex().acquire();
    File localFile = null;
    try
    {
      try
      {
        FileOutputStream localFileOutputStream;
        receipts.add(this.name);
        if (DownloadManager.isWindowsVista())
        {
          localFile = new File(DownloadManager.getLocalLowTempBundlePath(), "receipts");
          if (receiptPath.exists())
            DownloadManager.copyReceiptFile(receiptPath, localFile);
          localFileOutputStream = new FileOutputStream(localFile, receiptPath.exists());
          localFileOutputStream.write(this.name + System.getProperty("line.separator").getBytes("utf-8"));
          localFileOutputStream.close();
          if (!(DownloadManager.moveFileWithBroker(DownloadManager.getKernelJREDir() + "-bundles" + File.separator + "receipts")))
            throw new IOException("failed to write receipts");
        }
        else
        {
          localFile = receiptPath;
          localFileOutputStream = new FileOutputStream(localFile, true);
          localFileOutputStream.write(this.name + System.getProperty("line.separator").getBytes("utf-8"));
          localFileOutputStream.close();
        }
      }
      catch (IOException localIOException)
      {
        DownloadManager.log(localIOException);
      }
    }
    finally
    {
      getReceiptsMutex().release();
    }
  }

  public String toString()
  {
    return "Bundle[" + this.name + "]";
  }

  static
  {
    if (!(DownloadManager.jkernelLibLoaded))
      System.loadLibrary("jkernel");
    String str = System.getProperty("kernel.simultaneous.downloads");
    if (str != null)
      THREADS = Integer.parseInt(str.trim());
    else
      THREADS = 1;
    bundles = new HashMap();
    receipts = new HashSet();
    receiptPath = new File(DownloadManager.getBundlePath(), "receipts");
  }
}