package sun.jkernel;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.security.AllPermission;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Map<Ljava.lang.String;Ljava.lang.String;>;
import java.util.Map<Ljava.lang.String;Ljava.util.Map<Ljava.lang.String;Ljava.lang.String;>;>;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import sun.misc.Launcher;

public class DownloadManager
{
  public static final String KERNEL_DOWNLOAD_URL_PROPERTY = "kernel.download.url";
  public static final String KERNEL_DOWNLOAD_ENABLED_PROPERTY = "kernel.download.enabled";
  public static final String KERNEL_DOWNLOAD_DIALOG_PROPERTY = "kernel.download.dialog";
  public static final String KERNEL_DEBUG_PROPERTY = "kernel.debug";
  public static final String KERNEL_NOMERGE_PROPERTY = "kernel.nomerge";
  public static final String KERNEL_SIMULTANEOUS_DOWNLOADS_PROPERTY = "kernel.simultaneous.downloads";
  public static final int KERNEL_STATIC_MODTIME = 10000000;
  public static final String RESOURCE_URL = "internal-resource/";
  public static final String REQUESTED_BUNDLES_PATH = "lib" + File.separator + "bundles" + File.separator + "requested.list";
  private static final boolean disableDownloadDialog = "false".equals(System.getProperty("kernel.download.dialog"));
  static boolean debug = "true".equals(System.getProperty("kernel.debug"));
  private static OutputStream errorStream;
  private static OutputStream logStream;
  static String MUTEX_PREFIX;
  static boolean complete;
  private static int _isJBrokerStarted = -1;
  private static Properties bundleURLs;
  public static final String JAVA_HOME = System.getProperty("java.home");
  public static final String USER_HOME = System.getProperty("user.home");
  public static final String JAVA_VERSION = System.getProperty("java.version");
  static final int BUFFER_SIZE = 2048;
  static volatile boolean jkernelLibLoaded = false;
  public static String DEFAULT_DOWNLOAD_URL = "http://javadl.sun.com/webapps/download/GetList/" + System.getProperty("java.runtime.version") + "-kernel/windows-i586/";
  private static final String CUSTOM_PREFIX = "custom";
  private static final String KERNEL_PATH_SUFFIX = "-kernel";
  public static final String JAR_PATH_PROPERTY = "jarpath";
  public static final String SIZE_PROPERTY = "size";
  public static final String DEPENDENCIES_PROPERTY = "dependencies";
  public static final String INSTALL_PROPERTY = "install";
  private static boolean reportErrors = true;
  static final int ERROR_UNSPECIFIED = 0;
  static final int ERROR_DISK_FULL = 1;
  static final int ERROR_MALFORMED_BUNDLE_PROPERTIES = 2;
  static final int ERROR_DOWNLOADING_BUNDLE_PROPERTIES = 3;
  static final int ERROR_MALFORMED_URL = 4;
  static ThreadLocal<Integer> downloading = new ThreadLocal()
  {
    protected Integer initialValue()
    {
      return Integer.valueOf(0);
    }
  };
  private static File[] additionalBootStrapPaths = new File[0];
  private static String[] bundleNames;
  private static String[] criticalBundleNames;
  private static String downloadURL;
  private static boolean visitorIdDetermined;
  private static String visitorId;
  public static String CHECK_VALUES_FILE = "check_value.properties";
  static String CHECK_VALUES_DIR = "sun/jkernel/";
  static String CHECK_VALUES_PATH = CHECK_VALUES_DIR + CHECK_VALUES_FILE;
  private static Map<String, Map<String, String>> bundleProperties;
  private static Map<String, String> resourceMap;
  private static Map<String, String> fileMap;
  private static boolean extDirDetermined;
  private static boolean extDirIncluded;

  static synchronized void loadJKernelLibrary()
  {
    if (!(jkernelLibLoaded))
      try
      {
        System.loadLibrary("jkernel");
        jkernelLibLoaded = true;
        debug = getDebugProperty();
      }
      catch (Exception localException)
      {
        throw new Error(localException);
      }
  }

  static String appendTransactionId(String paramString)
  {
    StringBuilder localStringBuilder = new StringBuilder(paramString);
    String str = getVisitorId();
    if (str != null)
    {
      if (paramString.indexOf("?") == -1)
        localStringBuilder.append('?');
      else
        localStringBuilder.append('&');
      localStringBuilder.append("transactionId=");
      localStringBuilder.append(getVisitorId());
    }
    return localStringBuilder.toString();
  }

  static synchronized String getBaseDownloadURL()
  {
    if (downloadURL == null)
    {
      log("Determining download URL...");
      loadJKernelLibrary();
      downloadURL = System.getProperty("kernel.download.url");
      log("System property kernel.download.url = " + downloadURL);
      if (downloadURL == null)
      {
        downloadURL = getUrlFromRegistry();
        log("getUrlFromRegistry = " + downloadURL);
      }
      if (downloadURL == null)
        downloadURL = DEFAULT_DOWNLOAD_URL;
      log("Final download URL: " + downloadURL);
    }
    return downloadURL;
  }

  static Map<String, String> readTreeMap(InputStream paramInputStream)
    throws IOException
  {
    HashMap localHashMap = new HashMap();
    BufferedInputStream localBufferedInputStream = new BufferedInputStream(paramInputStream);
    ArrayList localArrayList = new ArrayList();
    StringBuilder localStringBuilder1 = new StringBuilder();
    while (true)
    {
      int i = localBufferedInputStream.read();
      if (i == -1)
        break;
      if (i < 32)
      {
        if (localArrayList.size() > 0)
          localArrayList.set(localArrayList.size() - 1, localStringBuilder1.toString());
        localStringBuilder1.setLength(0);
        if (i > localArrayList.size())
          throw new InternalError("current token level is " + (localArrayList.size() - 1) + " but encountered token " + "level " + i);
        if (i == localArrayList.size())
        {
          localArrayList.add(null);
        }
        else
        {
          StringBuilder localStringBuilder3 = new StringBuilder();
          for (int k = 1; k < localArrayList.size(); ++k)
          {
            if (k > 1)
              localStringBuilder3.append('/');
            localStringBuilder3.append((String)localArrayList.get(k));
          }
          localHashMap.put(localStringBuilder3.toString(), localArrayList.get(0));
          while (i < localArrayList.size())
            localArrayList.remove(i);
          localArrayList.add(null);
        }
      }
      else if (i < 254)
      {
        localStringBuilder1.append((char)i);
      }
      else if (i == 255)
      {
        localStringBuilder1.append(".class");
      }
      else
      {
        throw new InternalError("internal error processing resource_map (can't-happen error)");
      }
    }
    if (localArrayList.size() > 0)
      localArrayList.set(localArrayList.size() - 1, localStringBuilder1.toString());
    StringBuilder localStringBuilder2 = new StringBuilder();
    for (int j = 1; j < localArrayList.size(); ++j)
    {
      if (j > 1)
        localStringBuilder2.append('/');
      localStringBuilder2.append((String)localArrayList.get(j));
    }
    if (!(localArrayList.isEmpty()))
      localHashMap.put(localStringBuilder2.toString(), localArrayList.get(0));
    localBufferedInputStream.close();
    return Collections.unmodifiableMap(localHashMap);
  }

  public static Map<String, String> getResourceMap()
    throws IOException
  {
    if (resourceMap == null)
    {
      Object localObject = DownloadManager.class.getResourceAsStream("resource_map");
      if (localObject != null)
      {
        localObject = new BufferedInputStream((InputStream)localObject);
        try
        {
          resourceMap = readTreeMap((InputStream)localObject);
          ((InputStream)localObject).close();
        }
        catch (IOException localIOException)
        {
          resourceMap = new HashMap();
          complete = true;
          log("Can't find resource_map, forcing complete to true");
        }
        ((InputStream)localObject).close();
      }
      else
      {
        resourceMap = new HashMap();
        complete = true;
        log("Can't find resource_map, forcing complete to true");
      }
      int i = 1;
      while (true)
      {
        String str = "custom" + i;
        File localFile = new File(getBundlePath(), str + ".jar");
        if (!(localFile.exists()))
          break;
        JarFile localJarFile = new JarFile(localFile);
        Enumeration localEnumeration = localJarFile.entries();
        while (localEnumeration.hasMoreElements())
        {
          JarEntry localJarEntry = (JarEntry)localEnumeration.nextElement();
          if (!(localJarEntry.isDirectory()))
            resourceMap.put(localJarEntry.getName(), str);
        }
        ++i;
      }
    }
    return ((Map<String, String>)resourceMap);
  }

  public static Map<String, String> getFileMap()
    throws IOException
  {
    if (fileMap == null)
    {
      Object localObject = DownloadManager.class.getResourceAsStream("file_map");
      if (localObject != null)
      {
        localObject = new BufferedInputStream((InputStream)localObject);
        try
        {
          fileMap = readTreeMap((InputStream)localObject);
          ((InputStream)localObject).close();
        }
        catch (IOException localIOException)
        {
          fileMap = new HashMap();
          complete = true;
          log("Can't find file_map, forcing complete to true");
        }
        ((InputStream)localObject).close();
      }
      else
      {
        fileMap = new HashMap();
        complete = true;
        log("Can't find file_map, forcing complete to true");
      }
    }
    return ((Map<String, String>)fileMap);
  }

  private static synchronized Map<String, Map<String, String>> getBundleProperties()
    throws IOException
  {
    if (bundleProperties == null)
    {
      Object localObject = DownloadManager.class.getResourceAsStream("bundle.properties");
      if (localObject == null)
      {
        complete = true;
        log("Can't find bundle.properties, forcing complete to true");
        return null;
      }
      localObject = new BufferedInputStream((InputStream)localObject);
      Properties localProperties = new Properties();
      localProperties.load((InputStream)localObject);
      bundleProperties = new HashMap();
      Iterator localIterator = localProperties.entrySet().iterator();
      while (localIterator.hasNext())
      {
        Map.Entry localEntry = (Map.Entry)localIterator.next();
        String str1 = (String)localEntry.getKey();
        String[] arrayOfString1 = ((String)localEntry.getValue()).split("\\|");
        HashMap localHashMap = new HashMap();
        String[] arrayOfString2 = arrayOfString1;
        int i = arrayOfString2.length;
        for (int j = 0; j < i; ++j)
        {
          String str2 = arrayOfString2[j];
          int k = str2.indexOf("=");
          if (k == -1)
            throw new InternalError("error parsing bundle.properties: " + str2);
          localHashMap.put(str2.substring(0, k).trim(), str2.substring(k + 1).trim());
        }
        bundleProperties.put(str1, localHashMap);
      }
      ((InputStream)localObject).close();
    }
    return ((Map<String, Map<String, String>>)bundleProperties);
  }

  static String getBundleProperty(String paramString1, String paramString2)
  {
    Map localMap;
    try
    {
      localMap = getBundleProperties();
      Object localObject = (localMap != null) ? (Map)localMap.get(paramString1) : null;
      return ((localObject != null) ? (String)localObject.get(paramString2) : null);
    }
    catch (IOException localIOException)
    {
      throw new RuntimeException(localIOException);
    }
  }

  static String[] getBundleNames()
    throws IOException
  {
    if (bundleNames == null)
    {
      HashSet localHashSet = new HashSet();
      Map localMap1 = getResourceMap();
      if (localMap1 != null)
        localHashSet.addAll(localMap1.values());
      Map localMap2 = getFileMap();
      if (localMap2 != null)
        localHashSet.addAll(localMap2.values());
      bundleNames = (String[])localHashSet.toArray(new String[localHashSet.size()]);
    }
    return bundleNames;
  }

  private static String[] getCriticalBundleNames()
    throws IOException
  {
    if (criticalBundleNames == null)
    {
      HashSet localHashSet = new HashSet();
      Map localMap = getFileMap();
      if (localMap != null)
        localHashSet.addAll(localMap.values());
      criticalBundleNames = (String[])localHashSet.toArray(new String[localHashSet.size()]);
    }
    return criticalBundleNames;
  }

  public static void send(InputStream paramInputStream, OutputStream paramOutputStream)
    throws IOException
  {
    byte[] arrayOfByte = new byte[2048];
    while ((i = paramInputStream.read(arrayOfByte)) > 0)
    {
      int i;
      paramOutputStream.write(arrayOfByte, 0, i);
    }
  }

  static void performCompletionIfNeeded()
  {
    if (debug)
      log("DownloadManager.performCompletionIfNeeded: checking (" + complete + ", " + System.getProperty("kernel.nomerge") + ")");
    if ((complete) || ("true".equals(System.getProperty("kernel.nomerge"))))
      return;
    Bundle.loadReceipts();
    try
    {
      if (debug)
      {
        ArrayList localArrayList = new ArrayList(Arrays.asList(getCriticalBundleNames()));
        localArrayList.removeAll(Bundle.receipts);
        log("DownloadManager.performCompletionIfNeeded: still need " + localArrayList.size() + " bundles (" + localArrayList + ")");
      }
      if (Bundle.receipts.containsAll(Arrays.asList(getCriticalBundleNames())))
      {
        log("DownloadManager.performCompletionIfNeeded: running");
        new Thread("JarMerger")
        {
          public void run()
          {
            DownloadManager.access$200();
          }
        }
        .start();
      }
    }
    catch (IOException localIOException)
    {
      throw new RuntimeException(localIOException);
    }
  }

  public static Bundle getBundleForResource(String paramString)
    throws IOException
  {
    String str = (String)getResourceMap().get(paramString);
    return ((str != null) ? Bundle.getBundle(str) : null);
  }

  private static Bundle getBundleForFile(String paramString)
    throws IOException
  {
    String str = (String)getFileMap().get(paramString);
    return ((str != null) ? Bundle.getBundle(str) : null);
  }

  static File getBundlePath()
  {
    return new File(JAVA_HOME, "lib" + File.separatorChar + "bundles");
  }

  private static String getAppDataLocalLow()
  {
    return USER_HOME + "\\appdata\\locallow\\";
  }

  public static String getKernelJREDir()
  {
    return "kerneljre" + JAVA_VERSION;
  }

  static File getLocalLowTempBundlePath()
  {
    return new File(getLocalLowKernelJava() + "-bundles");
  }

  static String getLocalLowKernelJava()
  {
    return getAppDataLocalLow() + getKernelJREDir();
  }

  public static synchronized File[] getAdditionalBootStrapPaths()
  {
    return ((additionalBootStrapPaths != null) ? additionalBootStrapPaths : new File[0]);
  }

  private static void addEntryToBootClassPath(File paramFile)
  {
    synchronized (Launcher.class)
    {
      synchronized (DownloadManager.class)
      {
        File[] arrayOfFile = new File[additionalBootStrapPaths.length + 1];
        System.arraycopy(additionalBootStrapPaths, 0, arrayOfFile, 0, additionalBootStrapPaths.length);
        arrayOfFile[(arrayOfFile.length - 1)] = paramFile;
        additionalBootStrapPaths = arrayOfFile;
        Launcher.flushBootstrapClassPath();
      }
    }
  }

  private static synchronized boolean extDirIsIncluded()
  {
    if (!(extDirDetermined))
    {
      extDirDetermined = true;
      String str1 = System.getProperty("java.ext.dirs");
      String str2 = JAVA_HOME + File.separator + "lib" + File.separator + "ext";
      for (int i = 0; i < str1.length(); i = j + 1)
      {
        int j = str1.indexOf(File.pathSeparator, i);
        if (j == -1)
          j = str1.length();
        String str3 = str1.substring(i, j);
        if (str3.equals(str2))
        {
          extDirIncluded = true;
          break;
        }
      }
    }
    return extDirIncluded;
  }

  private static String doGetBootClassPathEntryForResource(String paramString)
  {
    boolean bool1 = false;
    Bundle localBundle1 = null;
    try
    {
      localBundle1 = getBundleForResource(paramString);
      if (localBundle1 != null)
      {
        File localFile1 = localBundle1.getJarPath();
        boolean bool2 = localFile1.getParentFile().getName().equals("ext");
        if ((bool2) && (!(extDirIsIncluded())))
          return null;
        if (getBundleProperty(localBundle1.getName(), "jarpath") == null)
        {
          Bundle localBundle2 = Bundle.getBundle("merged");
          if ((localBundle2 != null) && (localBundle2.isInstalled()))
          {
            File localFile3;
            if (paramString.endsWith(".class"))
              localFile3 = localBundle2.getJarPath();
            else
              localFile3 = new File(localBundle2.getJarPath().getPath().replaceAll("merged-rt.jar", "merged-resources.jar"));
            addEntryToBootClassPath(localFile3);
            return localFile3.getPath();
          }
        }
        if (!(localBundle1.isInstalled()))
        {
          localBundle1.queueDependencies(true);
          log("On-demand downloading " + localBundle1.getName() + " for resource " + paramString + "...");
          localBundle1.install();
          log(localBundle1 + " install finished.");
        }
        log("Double-checking " + localBundle1 + " state...");
        if (!(localBundle1.isInstalled()))
          throw new IllegalStateException("Expected state of " + localBundle1 + " to be INSTALLED");
        if (bool2)
        {
          Launcher.addURLToExtClassLoader(localFile1.toURL());
          return null;
        }
        if ("javaws".equals(localBundle1.getName()))
        {
          Launcher.addURLToAppClassLoader(localFile1.toURL());
          log("Returning null for javaws");
          return null;
        }
        if ("core".equals(localBundle1.getName()))
          return null;
        addEntryToBootClassPath(localFile1);
        return localFile1.getPath();
      }
      return null;
    }
    catch (Throwable localThrowable)
    {
      do
      {
        bool1 = handleException(localThrowable);
        log("Error downloading bundle for " + paramString + ":");
        log(localThrowable);
        if ((localThrowable instanceof IOException) && (localBundle1 != null))
        {
          if (localBundle1.getJarPath() != null)
          {
            File localFile2 = new File(localBundle1.getJarPath() + ".pack");
            localFile2.delete();
            localBundle1.getJarPath().delete();
          }
          if (localBundle1.getLocalPath() != null)
            localBundle1.getLocalPath().delete();
          localBundle1.setState(0);
        }
      }
      while (bool1);
    }
    return null;
  }

  static boolean handleException(Throwable paramThrowable)
  {
    if (paramThrowable instanceof IOException)
    {
      int i = 0;
      if (paramThrowable.getMessage().indexOf("not enough space") != -1)
        i = 1;
      return askUserToRetryDownloadOrQuit(i);
    }
    return false;
  }

  static synchronized void flushBundleURLs()
  {
    bundleURLs = null;
  }

  static synchronized Properties getBundleURLs(boolean paramBoolean)
    throws IOException
  {
    if (bundleURLs == null)
    {
      log("Entering DownloadManager.getBundleURLs");
      String str1 = getBaseDownloadURL();
      String str2 = appendTransactionId(str1);
      File localFile1 = null;
      if (isWindowsVista())
        localFile1 = getLocalLowTempBundlePath();
      else
        localFile1 = getBundlePath();
      File localFile2 = new File(localFile1, "urls." + getCurrentProcessId() + ".properties");
      try
      {
        log("Downloading from " + str2 + " to " + localFile2);
        downloadFromURL(str2, localFile2, "", paramBoolean);
        bundleURLs = new Properties();
        if (localFile2.exists())
        {
          addToTotalDownloadSize((int)localFile2.length());
          Object localObject1 = new FileInputStream(localFile2);
          localObject1 = new BufferedInputStream((InputStream)localObject1);
          bundleURLs.load((InputStream)localObject1);
          ((InputStream)localObject1).close();
          if (bundleURLs.isEmpty())
            fatalError(2);
        }
        else
        {
          fatalError(3);
        }
      }
      finally
      {
        if (!(debug))
          localFile2.delete();
      }
      log("Leaving DownloadManager.getBundleURLs");
    }
    return ((Properties)bundleURLs);
  }

  public static String getBootClassPathEntryForResource(String paramString)
  {
    if (debug)
      log("Entering getBootClassPathEntryForResource(" + paramString + ")");
    if ((isJREComplete()) || (downloading == null) || (paramString.startsWith("sun/jkernel")))
    {
      if (debug)
        log("Bailing: " + isJREComplete() + ", " + (downloading == null));
      return null;
    }
    incrementDownloadCount();
    try
    {
      String str1 = (String)java.security.AccessController.doPrivileged(new PrivilegedAction(paramString)
      {
        public Object run()
        {
          return DownloadManager.access$300(this.val$resourceName);
        }
      });
      log("getBootClassPathEntryForResource(" + paramString + ") == " + str1);
      String str2 = str1;
      return str2;
    }
    finally
    {
      decrementDownloadCount();
    }
  }

  public static String getBootClassPathEntryForClass(String paramString)
  {
    return getBootClassPathEntryForResource(paramString.replace('.', '/') + ".class");
  }

  private static boolean doDownloadFile(String paramString)
    throws IOException
  {
    Bundle localBundle = getBundleForFile(paramString);
    if (localBundle != null)
    {
      localBundle.queueDependencies(true);
      log("On-demand downloading " + localBundle.getName() + " for file " + paramString + "...");
      localBundle.install();
      return true;
    }
    return false;
  }

  // ERROR //
  public static boolean downloadFile(String paramString)
    throws IOException
  {
    // Byte code:
    //   0: invokestatic 1152	sun/jkernel/DownloadManager:isJREComplete	()Z
    //   3: ifne +9 -> 12
    //   6: getstatic 1008	sun/jkernel/DownloadManager:downloading	Ljava/lang/ThreadLocal;
    //   9: ifnonnull +5 -> 14
    //   12: iconst_0
    //   13: ireturn
    //   14: invokestatic 1143	sun/jkernel/DownloadManager:incrementDownloadCount	()V
    //   17: new 665	sun/jkernel/DownloadManager$5
    //   20: dup
    //   21: aload_0
    //   22: invokespecial 1203	sun/jkernel/DownloadManager$5:<init>	(Ljava/lang/String;)V
    //   25: invokestatic 1101	java/security/AccessController:doPrivileged	(Ljava/security/PrivilegedAction;)Ljava/lang/Object;
    //   28: astore_1
    //   29: aload_1
    //   30: instanceof 622
    //   33: ifeq +16 -> 49
    //   36: aload_1
    //   37: checkcast 622	java/lang/Boolean
    //   40: invokevirtual 1052	java/lang/Boolean:booleanValue	()Z
    //   43: istore_2
    //   44: jsr +16 -> 60
    //   47: iload_2
    //   48: ireturn
    //   49: aload_1
    //   50: checkcast 618	IOException
    //   53: athrow
    //   54: astore_3
    //   55: jsr +5 -> 60
    //   58: aload_3
    //   59: athrow
    //   60: astore 4
    //   62: invokestatic 1140	sun/jkernel/DownloadManager:decrementDownloadCount	()V
    //   65: ret 4
    //
    // Exception table:
    //   from	to	target	type
    //   17	47	54	finally
    //   49	58	54	finally
  }

  static void incrementDownloadCount()
  {
    downloading.set(Integer.valueOf(((Integer)downloading.get()).intValue() + 1));
  }

  static void decrementDownloadCount()
  {
    downloading.set(Integer.valueOf(((Integer)downloading.get()).intValue() - 1));
  }

  public static boolean isCurrentThreadDownloading()
  {
    return (((Integer)downloading.get()).intValue() > 0);
  }

  public static boolean isJREComplete()
  {
    return complete;
  }

  static void doBackgroundDownloads(boolean paramBoolean)
  {
    if (!(complete))
    {
      if ((!(paramBoolean)) && (!(debug)))
        reportErrors = false;
      try
      {
        Bundle localBundle1 = Bundle.getBundle("javax_swing_core");
        if (!(localBundle1.isInstalled()))
          localBundle1.install(paramBoolean, false, false);
        String[] arrayOfString = getCriticalBundleNames();
        int i = arrayOfString.length;
        for (int j = 0; j < i; ++j)
        {
          String str = arrayOfString[j];
          Bundle localBundle2 = Bundle.getBundle(str);
          if (!(localBundle2.isInstalled()))
            localBundle2.install(paramBoolean, false, true);
        }
        shutdown();
      }
      catch (IOException localIOException)
      {
        log(localIOException);
      }
    }
  }

  static void copyReceiptFile(File paramFile1, File paramFile2)
    throws IOException
  {
    DataInputStream localDataInputStream = new DataInputStream(new BufferedInputStream(new FileInputStream(paramFile1)));
    FileOutputStream localFileOutputStream = new FileOutputStream(paramFile2);
    for (String str = localDataInputStream.readLine(); str != null; str = localDataInputStream.readLine())
      localFileOutputStream.write(str + '\n'.getBytes("utf-8"));
    localDataInputStream.close();
    localFileOutputStream.close();
  }

  private static void downloadRequestedBundles()
  {
    log("Checking for requested bundles...");
    try
    {
      File localFile = new File(JAVA_HOME, REQUESTED_BUNDLES_PATH);
      if (localFile.exists())
      {
        FileInputStream localFileInputStream = new FileInputStream(localFile);
        ByteArrayOutputStream localByteArrayOutputStream = new ByteArrayOutputStream();
        send(localFileInputStream, localByteArrayOutputStream);
        localFileInputStream.close();
        String str = new String(localByteArrayOutputStream.toByteArray(), "utf-8");
        ArrayList localArrayList = new ArrayList();
        StringBuilder localStringBuilder = new StringBuilder();
        for (int i = 0; i < str.length(); ++i)
        {
          char c = str.charAt(i);
          if ((c == ',') || (Character.isWhitespace(c)))
            if (localStringBuilder.length() > 0)
            {
              localArrayList.add(localStringBuilder.toString());
              localStringBuilder.setLength(0);
            }
          else
            localStringBuilder.append(c);
        }
        if (localStringBuilder.length() > 0)
          localArrayList.add(localStringBuilder.toString());
        log("Requested bundles: " + localArrayList);
        for (i = 0; i < localArrayList.size(); ++i)
        {
          Bundle localBundle = Bundle.getBundle((String)localArrayList.get(i));
          if ((localBundle != null) && (!(localBundle.isInstalled())))
          {
            log("Downloading " + localBundle + " due to requested.list");
            localBundle.install(true, false, false);
          }
        }
      }
    }
    catch (IOException localIOException)
    {
      log(localIOException);
    }
  }

  static void fatalError(int paramInt)
  {
    fatalError(paramInt, null);
  }

  static void fatalError(int paramInt, String paramString)
  {
    for (int i = 0; i < Bundle.THREADS; ++i)
      bundleInstallComplete();
    if (reportErrors)
      displayError(paramInt, paramString);
    i = ((Boolean.getBoolean("java.awt.headless")) || (System.getProperty("javaplugin.version") != null)) ? 1 : 0;
    KernelError localKernelError = new KernelError("Java Kernel bundle download failed");
    if (i != 0)
      throw localKernelError;
    log(localKernelError);
    System.exit(1);
  }

  private static void startBackgroundDownloadWithBroker()
  {
    String str2;
    if (!(BackgroundDownloader.getBackgroundDownloadProperty()))
      return;
    if (!(launchBrokerProcess()))
      return;
    String str1 = getBaseDownloadURL();
    if ((str1 == null) || (str1.equals(DEFAULT_DOWNLOAD_URL)))
      str2 = " ";
    else
      str2 = str1;
    startBackgroundDownloadWithBrokerImpl(str1);
  }

  private static void startBackgroundDownloads()
  {
    if ((!(complete)) && (BackgroundDownloader.getBackgroundMutex().acquire(0)))
    {
      BackgroundDownloader.getBackgroundMutex().release();
      if (isWindowsVista())
        startBackgroundDownloadWithBroker();
      else
        BackgroundDownloader.startBackgroundDownloads();
    }
  }

  static native void addToTotalDownloadSize(int paramInt);

  static void downloadFromURL(String paramString1, File paramFile, String paramString2, boolean paramBoolean)
  {
    downloadFromURLImpl(paramString1, paramFile, paramString2, (disableDownloadDialog) ? false : paramBoolean);
  }

  private static native void downloadFromURLImpl(String paramString1, File paramFile, String paramString2, boolean paramBoolean);

  static native String getUrlFromRegistry();

  static native String getVisitorId0();

  static synchronized String getVisitorId()
  {
    if (!(visitorIdDetermined))
    {
      visitorIdDetermined = true;
      visitorId = getVisitorId0();
    }
    return visitorId;
  }

  public static native void displayError(int paramInt, String paramString);

  public static native boolean askUserToRetryDownloadOrQuit(int paramInt);

  static native boolean isWindowsVista();

  private static native void startBackgroundDownloadWithBrokerImpl(String paramString);

  private static int isJBrokerStarted()
  {
    if (_isJBrokerStarted == -1)
      _isJBrokerStarted = (isJBrokerRunning()) ? 1 : 0;
    return _isJBrokerStarted;
  }

  private static native boolean isJBrokerRunning();

  private static native boolean isIEProtectedMode();

  private static native boolean launchJBroker(String paramString);

  static native void bundleInstallStart();

  static native void bundleInstallComplete();

  private static native boolean moveFileWithBrokerImpl(String paramString1, String paramString2);

  private static native boolean moveDirWithBrokerImpl(String paramString1, String paramString2);

  static boolean moveFileWithBroker(String paramString)
  {
    if (!(launchBrokerProcess()))
      return false;
    return moveFileWithBrokerImpl(paramString, USER_HOME);
  }

  static boolean moveDirWithBroker(String paramString)
  {
    if (!(launchBrokerProcess()))
      return false;
    return moveDirWithBrokerImpl(paramString, USER_HOME);
  }

  private static synchronized boolean launchBrokerProcess()
  {
    if (isJBrokerStarted() == 0)
    {
      boolean bool = launchJBroker(JAVA_HOME);
      _isJBrokerStarted = (bool) ? 1 : 0;
      return bool;
    }
    return true;
  }

  private static void copyAll(File paramFile1, File paramFile2, Set paramSet)
    throws IOException
  {
    if (!(paramSet.contains(paramFile1.getName())))
    {
      Object localObject;
      if (paramFile1.isDirectory())
      {
        localObject = paramFile1.listFiles();
        if (localObject != null)
          for (int i = 0; i < localObject.length; ++i)
            copyAll(localObject[i], new File(paramFile2, localObject[i].getName()), paramSet);
      }
      else
      {
        paramFile2.getParentFile().mkdirs();
        localObject = new FileInputStream(paramFile1);
        FileOutputStream localFileOutputStream = new FileOutputStream(paramFile2);
        send((InputStream)localObject, localFileOutputStream);
        ((FileInputStream)localObject).close();
        localFileOutputStream.close();
      }
    }
  }

  public static void dumpOutput(Process paramProcess)
  {
    6 local6 = new Thread("outputReader", paramProcess)
    {
      public void run()
      {
        InputStream localInputStream;
        try
        {
          localInputStream = this.val$p.getInputStream();
          DownloadManager.send(localInputStream, System.out);
        }
        catch (IOException localIOException)
        {
          DownloadManager.log(localIOException);
        }
      }
    };
    local6.start();
    7 local7 = new Thread("errorReader", paramProcess)
    {
      public void run()
      {
        InputStream localInputStream;
        try
        {
          localInputStream = this.val$p.getErrorStream();
          DownloadManager.send(localInputStream, System.err);
        }
        catch (IOException localIOException)
        {
          DownloadManager.log(localIOException);
        }
      }
    };
    local7.start();
  }

  private static void createMergedJars()
  {
    File localFile1;
    log("DownloadManager.createMergedJars");
    if (isWindowsVista())
      localFile1 = getLocalLowTempBundlePath();
    else
      localFile1 = getBundlePath();
    File localFile2 = new File(localFile1, "tmp");
    if (new File(getBundlePath(), "tmp" + File.separator + "finished").exists())
      return;
    log("DownloadManager.createMergedJars: running");
    localFile2.mkdirs();
    int i = 0;
    do
      try
      {
        Bundle.getBundle("merged").install(false, false, true);
        File localFile3 = new File(localFile2, "finished");
        new FileOutputStream(localFile3).close();
        if ((isWindowsVista()) && (!(moveFileWithBroker(getKernelJREDir() + "-bundles\\tmp\\finished"))))
          throw new IOException("unable to create 'finished' file");
        log("DownloadManager.createMergedJars: created " + localFile3);
        if (isWindowsVista())
        {
          File localFile4 = getLocalLowTempBundlePath();
          File[] arrayOfFile = localFile4.listFiles();
          if (arrayOfFile != null)
            for (int j = 0; j < arrayOfFile.length; ++j)
              arrayOfFile[j].delete();
          localFile4.delete();
          log("Finished cleanup, " + localFile4 + ".exists(): " + localFile4.exists());
        }
      }
      catch (IOException localIOException)
      {
        log(localIOException);
      }
    while (i != 0);
    log("DownloadManager.createMergedJars: finished");
  }

  private static void shutdown()
  {
    ExecutorService localExecutorService;
    try
    {
      localExecutorService = Bundle.getThreadPool();
      localExecutorService.shutdown();
      localExecutorService.awaitTermination(86400L, TimeUnit.SECONDS);
    }
    catch (InterruptedException localInterruptedException)
    {
    }
  }

  static native boolean getDebugKey();

  public static boolean getDebugProperty()
  {
    boolean bool = getDebugKey();
    if (System.getProperty("kernel.debug") != null)
      bool = Boolean.valueOf(System.getProperty("kernel.debug")).booleanValue();
    return bool;
  }

  static void println(String paramString)
  {
    if (System.err != null)
      System.err.println(paramString);
    else
      try
      {
        if (errorStream == null)
          errorStream = new FileOutputStream(FileDescriptor.err);
        errorStream.write(paramString + System.getProperty("line.separator").getBytes("utf-8"));
      }
      catch (IOException localIOException)
      {
        throw new RuntimeException(localIOException);
      }
  }

  static void log(String paramString)
  {
    if (debug)
    {
      println(paramString);
      try
      {
        if (logStream == null)
        {
          loadJKernelLibrary();
          File localFile = (isWindowsVista()) ? getLocalLowTempBundlePath() : getBundlePath();
          localFile = new File(localFile, "kernel." + getCurrentProcessId() + ".log");
          logStream = new FileOutputStream(localFile);
        }
        logStream.write(paramString + System.getProperty("line.separator").getBytes("utf-8"));
        logStream.flush();
      }
      catch (IOException localIOException)
      {
      }
    }
  }

  static void log(Throwable paramThrowable)
  {
    ByteArrayOutputStream localByteArrayOutputStream = new ByteArrayOutputStream();
    PrintStream localPrintStream = new PrintStream(localByteArrayOutputStream);
    paramThrowable.printStackTrace(localPrintStream);
    localPrintStream.close();
    log(localByteArrayOutputStream.toString(0));
  }

  private static void printMap(Map paramMap)
  {
    int i = 0;
    HashSet localHashSet = new HashSet();
    Iterator localIterator = paramMap.entrySet().iterator();
    while (localIterator.hasNext())
    {
      Map.Entry localEntry = (Map.Entry)localIterator.next();
      String str1 = (String)localEntry.getKey();
      String str2 = (String)localEntry.getValue();
      System.out.println(str1 + ": " + str2);
      Integer localInteger1 = Integer.valueOf(System.identityHashCode(str1));
      if (!(localHashSet.contains(localInteger1)))
      {
        localHashSet.add(localInteger1);
        i += str1.length();
      }
      Integer localInteger2 = Integer.valueOf(System.identityHashCode(str2));
      if (!(localHashSet.contains(localInteger2)))
      {
        localHashSet.add(localInteger2);
        i += str2.length();
      }
    }
    System.out.println(i + " bytes");
  }

  private static void dumpMaps()
    throws IOException
  {
    System.out.println("Resources:");
    System.out.println("----------");
    printMap(getResourceMap());
    System.out.println();
    System.out.println("Files:");
    System.out.println("----------");
    printMap(getFileMap());
  }

  private static void processDownload(String paramString)
    throws IOException
  {
    if (paramString.equals("all"))
    {
      debug = true;
      doBackgroundDownloads(true);
    }
    else
    {
      Bundle localBundle = Bundle.getBundle(paramString);
      if (localBundle == null)
      {
        println("Unknown bundle: " + paramString);
        System.exit(1);
      }
      else
      {
        localBundle.install();
      }
    }
  }

  static native int getCurrentProcessId();

  public static void main(String[] paramArrayOfString)
    throws Exception
  {
    Object localObject;
    java.security.AccessController.checkPermission(new AllPermission());
    int i = 0;
    if ((paramArrayOfString.length == 2) && (paramArrayOfString[0].equals("-install")))
    {
      File localFile;
      i = 1;
      localObject = new Bundle()
      {
        protected void updateState()
        {
          this.state = 2;
        }
      };
      int j = 0;
      do
      {
        ++j;
        localFile = new File(getBundlePath(), "custom" + j + ".jar");
      }
      while (localFile.exists());
      ((Bundle)localObject).setName("custom" + j);
      ((Bundle)localObject).setLocalPath(new File(paramArrayOfString[1]));
      ((Bundle)localObject).setJarPath(localFile);
      ((Bundle)localObject).setDeleteOnInstall(false);
      ((Bundle)localObject).install();
    }
    else if ((paramArrayOfString.length == 2) && (paramArrayOfString[0].equals("-download")))
    {
      i = 1;
      processDownload(paramArrayOfString[1]);
    }
    else if ((paramArrayOfString.length == 1) && (paramArrayOfString[0].equals("-dumpmaps")))
    {
      i = 1;
      dumpMaps();
    }
    else if ((paramArrayOfString.length == 2) && (paramArrayOfString[0].equals("-sha1")))
    {
      i = 1;
      System.out.println(BundleCheck.getInstance(new File(paramArrayOfString[1])));
    }
    else if ((paramArrayOfString.length == 1) && (paramArrayOfString[0].equals("-downloadtest")))
    {
      i = 1;
      localObject = File.createTempFile("download", ".test");
      while (true)
      {
        ((File)localObject).delete();
        downloadFromURL(getBaseDownloadURL(), (File)localObject, "URLS", true);
        System.out.println("Downloaded " + ((File)localObject).length() + " bytes");
      }
    }
    if (i == 0)
    {
      System.out.println("usage: DownloadManager -install <path>.zip |");
      System.out.println("       DownloadManager -download <bundle_name> |");
      System.out.println("       DownloadManager -dumpmaps");
      System.exit(1);
    }
  }

  static
  {
    java.security.AccessController.doPrivileged(new PrivilegedAction()
    {
      public Object run()
      {
        if (DownloadManager.debug)
          DownloadManager.println("DownloadManager startup");
        DownloadManager.MUTEX_PREFIX = "jkernel";
        int i = (!("false".equals(System.getProperty("kernel.download.enabled")))) ? 1 : 0;
        DownloadManager.complete = (!(DownloadManager.getBundlePath().exists())) || (i == 0);
        if (!(DownloadManager.complete))
        {
          DownloadManager.loadJKernelLibrary();
          DownloadManager.log("Log opened");
          if (DownloadManager.isWindowsVista())
            DownloadManager.getLocalLowTempBundlePath().mkdirs();
          new Thread(this)
          {
            public void run()
            {
              DownloadManager.access$000();
            }
          }
          .start();
          try
          {
            String str;
            if (DownloadManager.isWindowsVista())
              str = DownloadManager.USER_HOME + "\\appdata\\locallow\\dummy.kernel";
            else
              str = DownloadManager.USER_HOME + "\\dummy.kernel";
            File localFile = new File(str);
            FileOutputStream localFileOutputStream = new FileOutputStream(localFile, true);
            localFileOutputStream.close();
            localFile.deleteOnExit();
          }
          catch (IOException localIOException)
          {
            DownloadManager.log(localIOException);
          }
          new Thread(this, "BundleDownloader")
          {
            public void run()
            {
              DownloadManager.access$100();
            }
          }
          .start();
        }
        return null;
      }
    });
  }

  private static class StreamMonitor
  implements Runnable
  {
    private InputStream istream;

    public StreamMonitor(InputStream paramInputStream)
    {
      this.istream = new BufferedInputStream(paramInputStream);
      new Thread(this).start();
    }

    public void run()
    {
      byte[] arrayOfByte = new byte[4096];
      try
      {
        for (int i = this.istream.read(arrayOfByte); i != -1; i = this.istream.read(arrayOfByte));
      }
      catch (IOException localIOException1)
      {
        try
        {
          this.istream.close();
        }
        catch (IOException localIOException2)
        {
        }
      }
    }
  }
}