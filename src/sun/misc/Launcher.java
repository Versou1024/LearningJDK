package sun.misc;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.AllPermission;
import java.security.PermissionCollection;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.ProtectionDomain;
import java.security.cert.Certificate;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;
import sun.jkernel.DownloadManager;
import sun.net.www.ParseUtil;
import sun.security.action.GetPropertyAction;

public class Launcher
{
  private static URLStreamHandlerFactory factory = new Factory(null);
  private static Launcher launcher = new Launcher();
  private ClassLoader loader;
  private static URLClassPath bootstrapClassPath;
  private static URLStreamHandler fileHandler;

  public static Launcher getLauncher()
  {
    return launcher;
  }

  public Launcher()
  {
    try
    {
      localExtClassLoader = ExtClassLoader.getExtClassLoader();
    }
    catch (IOException localIOException1)
    {
      throw new InternalError("Could not create extension class loader");
    }
    try
    {
      this.loader = AppClassLoader.getAppClassLoader(localExtClassLoader);
    }
    catch (IOException localIOException2)
    {
      throw new InternalError("Could not create application class loader");
    }
    Thread.currentThread().setContextClassLoader(this.loader);
    String str = System.getProperty("java.security.manager");
    if (str != null)
    {
      SecurityManager localSecurityManager = null;
      if (("".equals(str)) || ("default".equals(str)))
        localSecurityManager = new SecurityManager();
      else
        try
        {
          localSecurityManager = (SecurityManager)this.loader.loadClass(str).newInstance();
        }
        catch (IllegalAccessException localIllegalAccessException)
        {
        }
        catch (InstantiationException localInstantiationException)
        {
        }
        catch (ClassNotFoundException localClassNotFoundException)
        {
        }
        catch (ClassCastException localClassCastException)
        {
        }
      if (localSecurityManager != null)
        System.setSecurityManager(localSecurityManager);
      else
        throw new InternalError("Could not create SecurityManager: " + str);
    }
  }

  public ClassLoader getClassLoader()
  {
    return this.loader;
  }

  public static void addURLToAppClassLoader(URL paramURL)
  {
    AccessController.checkPermission(new AllPermission());
    ClassLoader localClassLoader = getLauncher().getClassLoader();
    ((AppClassLoader)localClassLoader).addAppURL(paramURL);
  }

  public static void addURLToExtClassLoader(URL paramURL)
  {
    AccessController.checkPermission(new AllPermission());
    ClassLoader localClassLoader = getLauncher().getClassLoader();
    ((ExtClassLoader)localClassLoader.getParent()).addExtURL(paramURL);
  }

  public static synchronized URLClassPath getBootstrapClassPath()
  {
    if (bootstrapClassPath == null)
    {
      URL[] arrayOfURL;
      String str = (String)AccessController.doPrivileged(new GetPropertyAction("sun.boot.class.path"));
      if (str != null)
      {
        localObject = str;
        arrayOfURL = (URL[])(URL[])AccessController.doPrivileged(new PrivilegedAction((String)localObject)
        {
          public Object run()
          {
            File[] arrayOfFile = Launcher.access$200(this.val$path);
            int i = arrayOfFile.length;
            HashSet localHashSet = new HashSet();
            for (int j = 0; j < i; ++j)
            {
              File localFile = arrayOfFile[j];
              if (!(localFile.isDirectory()))
                localFile = localFile.getParentFile();
              if ((localFile != null) && (localHashSet.add(localFile)))
                MetaIndex.registerDirectory(localFile);
            }
            return Launcher.access$300(arrayOfFile);
          }
        });
      }
      else
      {
        arrayOfURL = new URL[0];
      }
      bootstrapClassPath = new URLClassPath(arrayOfURL, factory);
      Object localObject = DownloadManager.getAdditionalBootStrapPaths();
      AccessController.doPrivileged(new PrivilegedAction(localObject)
      {
        public Object run()
        {
          for (int i = 0; i < this.val$additionalBootStrapPaths.length; ++i)
            Launcher.access$400().addURL(Launcher.getFileURL(this.val$additionalBootStrapPaths[i]));
          return null;
        }
      });
    }
    return ((URLClassPath)bootstrapClassPath);
  }

  public static synchronized void flushBootstrapClassPath()
  {
    bootstrapClassPath = null;
  }

  private static URL[] pathToURLs(File[] paramArrayOfFile)
  {
    URL[] arrayOfURL = new URL[paramArrayOfFile.length];
    for (int i = 0; i < paramArrayOfFile.length; ++i)
      arrayOfURL[i] = getFileURL(paramArrayOfFile[i]);
    return arrayOfURL;
  }

  private static File[] getClassPath(String paramString)
  {
    Object localObject;
    if (paramString != null)
    {
      int i = 0;
      int j = 1;
      int k = 0;
      for (int l = 0; (k = paramString.indexOf(File.pathSeparator, l)) != -1; l = k + 1)
        ++j;
      localObject = new File[j];
      for (l = k = 0; (k = paramString.indexOf(File.pathSeparator, l)) != -1; l = k + 1)
        if (k - l > 0)
          localObject[(i++)] = new File(paramString.substring(l, k));
        else
          localObject[(i++)] = new File(".");
      if (l < paramString.length())
        localObject[(i++)] = new File(paramString.substring(l));
      else
        localObject[(i++)] = new File(".");
      if (i != j)
      {
        File[] arrayOfFile = new File[i];
        System.arraycopy(localObject, 0, arrayOfFile, 0, i);
        localObject = arrayOfFile;
      }
    }
    else
    {
      localObject = new File[0];
    }
    return ((File)localObject);
  }

  static URL getFileURL(File paramFile)
  {
    try
    {
      paramFile = paramFile.getCanonicalFile();
    }
    catch (IOException localIOException)
    {
    }
    try
    {
      return ParseUtil.fileToEncodedURL(paramFile);
    }
    catch (MalformedURLException localMalformedURLException)
    {
      throw new InternalError();
    }
  }

  static class AppClassLoader extends URLClassLoader
  {
    public static ClassLoader getAppClassLoader(ClassLoader paramClassLoader)
      throws IOException
    {
      String str = System.getProperty("java.class.path");
      File[] arrayOfFile = (str == null) ? new File[0] : Launcher.access$200(str);
      return ((AppClassLoader)AccessController.doPrivileged(new PrivilegedAction(str, arrayOfFile, paramClassLoader)
      {
        public Object run()
        {
          URL[] arrayOfURL = (this.val$s == null) ? new URL[0] : Launcher.access$300(this.val$path);
          return new Launcher.AppClassLoader(arrayOfURL, this.val$extcl);
        }
      }));
    }

    AppClassLoader(URL[] paramArrayOfURL, ClassLoader paramClassLoader)
    {
      super(paramArrayOfURL, paramClassLoader, Launcher.access$100());
    }

    public synchronized Class loadClass(String paramString, boolean paramBoolean)
      throws ClassNotFoundException
    {
      DownloadManager.getBootClassPathEntryForClass(paramString);
      int i = paramString.lastIndexOf(46);
      if (i != -1)
      {
        SecurityManager localSecurityManager = System.getSecurityManager();
        if (localSecurityManager != null)
          localSecurityManager.checkPackageAccess(paramString.substring(0, i));
      }
      return super.loadClass(paramString, paramBoolean);
    }

    protected PermissionCollection getPermissions(java.security.CodeSource paramCodeSource)
    {
      PermissionCollection localPermissionCollection = super.getPermissions(paramCodeSource);
      localPermissionCollection.add(new RuntimePermission("exitVM"));
      return localPermissionCollection;
    }

    private void appendToClassPathForInstrumentation(String paramString)
    {
      if ((!($assertionsDisabled)) && (!(Thread.holdsLock(this))))
        throw new AssertionError();
      super.addURL(Launcher.getFileURL(new File(paramString)));
    }

    private static AccessControlContext getContext(File[] paramArrayOfFile)
      throws MalformedURLException
    {
      PathPermissions localPathPermissions = new PathPermissions(paramArrayOfFile);
      ProtectionDomain localProtectionDomain = new ProtectionDomain(new java.security.CodeSource(localPathPermissions.getCodeBase(), (Certificate[])null), localPathPermissions);
      AccessControlContext localAccessControlContext = new AccessControlContext(new ProtectionDomain[] { localProtectionDomain });
      return localAccessControlContext;
    }

    void addAppURL(URL paramURL)
    {
      super.addURL(paramURL);
    }
  }

  static class ExtClassLoader extends URLClassLoader
  {
    private File[] dirs;

    public static ExtClassLoader getExtClassLoader()
      throws IOException
    {
      File[] arrayOfFile = getExtDirs();
      try
      {
        return ((ExtClassLoader)AccessController.doPrivileged(new PrivilegedExceptionAction(arrayOfFile)
        {
          public Object run()
            throws IOException
          {
            int i = this.val$dirs.length;
            for (int j = 0; j < i; ++j)
              MetaIndex.registerDirectory(this.val$dirs[j]);
            return new Launcher.ExtClassLoader(this.val$dirs);
          }
        }));
      }
      catch (PrivilegedActionException localPrivilegedActionException)
      {
        throw ((IOException)localPrivilegedActionException.getException());
      }
    }

    void addExtURL(URL paramURL)
    {
      super.addURL(paramURL);
    }

    public ExtClassLoader(File[] paramArrayOfFile)
      throws IOException
    {
      super(getExtURLs(paramArrayOfFile), null, Launcher.access$100());
      this.dirs = paramArrayOfFile;
    }

    private static File[] getExtDirs()
    {
      File[] arrayOfFile;
      String str = System.getProperty("java.ext.dirs");
      if (str != null)
      {
        StringTokenizer localStringTokenizer = new StringTokenizer(str, File.pathSeparator);
        int i = localStringTokenizer.countTokens();
        arrayOfFile = new File[i];
        for (int j = 0; j < i; ++j)
          arrayOfFile[j] = new File(localStringTokenizer.nextToken());
      }
      else
      {
        arrayOfFile = new File[0];
      }
      return arrayOfFile;
    }

    private static URL[] getExtURLs(File[] paramArrayOfFile)
      throws IOException
    {
      Vector localVector = new Vector();
      for (int i = 0; i < paramArrayOfFile.length; ++i)
      {
        String[] arrayOfString = paramArrayOfFile[i].list();
        if (arrayOfString != null)
          for (int j = 0; j < arrayOfString.length; ++j)
            if (!(arrayOfString[j].equals("meta-index")))
            {
              File localFile = new File(paramArrayOfFile[i], arrayOfString[j]);
              localVector.add(Launcher.getFileURL(localFile));
            }
      }
      URL[] arrayOfURL = new URL[localVector.size()];
      localVector.copyInto(arrayOfURL);
      return arrayOfURL;
    }

    public String findLibrary(String paramString)
    {
      paramString = System.mapLibraryName(paramString);
      for (int i = 0; i < this.dirs.length; ++i)
      {
        String str = System.getProperty("os.arch");
        if (str != null)
        {
          localFile = new File(new File(this.dirs[i], str), paramString);
          if (localFile.exists())
            return localFile.getAbsolutePath();
        }
        File localFile = new File(this.dirs[i], paramString);
        if (localFile.exists())
          return localFile.getAbsolutePath();
      }
      return null;
    }

    protected Class findClass(String paramString)
      throws ClassNotFoundException
    {
      DownloadManager.getBootClassPathEntryForClass(paramString);
      return super.findClass(paramString);
    }

    private static AccessControlContext getContext(File[] paramArrayOfFile)
      throws IOException
    {
      PathPermissions localPathPermissions = new PathPermissions(paramArrayOfFile);
      ProtectionDomain localProtectionDomain = new ProtectionDomain(new java.security.CodeSource(localPathPermissions.getCodeBase(), (Certificate[])null), localPathPermissions);
      AccessControlContext localAccessControlContext = new AccessControlContext(new ProtectionDomain[] { localProtectionDomain });
      return localAccessControlContext;
    }
  }

  private static class Factory
  implements URLStreamHandlerFactory
  {
    private static String PREFIX = "sun.net.www.protocol";

    public URLStreamHandler createURLStreamHandler(String paramString)
    {
      String str = PREFIX + "." + paramString + ".Handler";
      try
      {
        Class localClass = Class.forName(str);
        return ((URLStreamHandler)localClass.newInstance());
      }
      catch (ClassNotFoundException localClassNotFoundException)
      {
        localClassNotFoundException.printStackTrace();
      }
      catch (InstantiationException localInstantiationException)
      {
        localInstantiationException.printStackTrace();
      }
      catch (IllegalAccessException localIllegalAccessException)
      {
        localIllegalAccessException.printStackTrace();
      }
      throw new InternalError("could not load " + paramString + "system protocol handler");
    }
  }
}