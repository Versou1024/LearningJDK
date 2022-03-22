package sun.applet;

import B;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FilePermission;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.SocketPermission;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.CodeSource;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.cert.Certificate;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.NoSuchElementException;
import sun.awt.AppContext;
import sun.net.www.ParseUtil;

public class AppletClassLoader extends URLClassLoader
{
  private URL base;
  private CodeSource codesource;
  private AccessControlContext acc;
  private boolean exceptionStatus = false;
  private final Object threadGroupSynchronizer = new Object();
  private final Object grabReleaseSynchronizer = new Object();
  private boolean codebaseLookup = true;
  private Object syncResourceAsStream = new Object();
  private Object syncResourceAsStreamFromJar = new Object();
  private boolean resourceAsStreamInCall = false;
  private boolean resourceAsStreamFromJarInCall = false;
  private AppletThreadGroup threadGroup;
  private AppContext appContext;
  int usageCount = 0;
  private HashMap jdk11AppletInfo = new HashMap();
  private HashMap jdk12AppletInfo = new HashMap();
  private static AppletMessageHandler mh = new AppletMessageHandler("appletclassloader");

  protected AppletClassLoader(URL paramURL)
  {
    super(new URL[0]);
    this.base = paramURL;
    this.codesource = new CodeSource(paramURL, (Certificate[])null);
    this.acc = AccessController.getContext();
  }

  void setCodebaseLookup(boolean paramBoolean)
  {
    this.codebaseLookup = paramBoolean;
  }

  URL getBaseURL()
  {
    return this.base;
  }

  public URL[] getURLs()
  {
    URL[] arrayOfURL1 = super.getURLs();
    URL[] arrayOfURL2 = new URL[arrayOfURL1.length + 1];
    System.arraycopy(arrayOfURL1, 0, arrayOfURL2, 0, arrayOfURL1.length);
    arrayOfURL2[(arrayOfURL2.length - 1)] = this.base;
    return arrayOfURL2;
  }

  protected void addJar(String paramString)
    throws IOException
  {
    URL localURL;
    try
    {
      localURL = new URL(this.base, paramString);
    }
    catch (MalformedURLException localMalformedURLException)
    {
      throw new IllegalArgumentException("name");
    }
    addURL(localURL);
  }

  public synchronized Class loadClass(String paramString, boolean paramBoolean)
    throws ClassNotFoundException
  {
    int i = paramString.lastIndexOf(46);
    if (i != -1)
    {
      SecurityManager localSecurityManager = System.getSecurityManager();
      if (localSecurityManager != null)
        localSecurityManager.checkPackageAccess(paramString.substring(0, i));
    }
    try
    {
      return super.loadClass(paramString, paramBoolean);
    }
    catch (ClassNotFoundException localClassNotFoundException)
    {
      throw localClassNotFoundException;
    }
    catch (RuntimeException localRuntimeException)
    {
      throw localRuntimeException;
    }
    catch (Error localError)
    {
      throw localError;
    }
  }

  protected Class findClass(String paramString)
    throws ClassNotFoundException
  {
    int i = paramString.indexOf(";");
    String str1 = "";
    if (i != -1)
    {
      str1 = paramString.substring(i, paramString.length());
      paramString = paramString.substring(0, i);
    }
    try
    {
      return super.findClass(paramString);
    }
    catch (ClassNotFoundException str2)
    {
      if (!(this.codebaseLookup))
        throw new ClassNotFoundException(paramString);
      String str2 = ParseUtil.encodePath(paramString.replace('.', '/'), false);
      String str3 = str2 + ".class" + str1;
      try
      {
        byte[] arrayOfByte = (byte[])(byte[])AccessController.doPrivileged(new PrivilegedExceptionAction(this, str3)
        {
          public Object run()
            throws IOException
          {
            URL localURL;
            try
            {
              localURL = new URL(AppletClassLoader.access$000(this.this$0), this.val$path);
              if ((AppletClassLoader.access$000(this.this$0).getProtocol().equals(localURL.getProtocol())) && (AppletClassLoader.access$000(this.this$0).getHost().equals(localURL.getHost())) && (AppletClassLoader.access$000(this.this$0).getPort() == localURL.getPort()))
                return AppletClassLoader.access$100(localURL);
              return null;
            }
            catch (Exception localException)
            {
            }
            return null;
          }
        }
        , this.acc);
        if (arrayOfByte != null)
          return defineClass(paramString, arrayOfByte, 0, arrayOfByte.length, this.codesource);
        throw new ClassNotFoundException(paramString);
      }
      catch (PrivilegedActionException localPrivilegedActionException)
      {
        throw new ClassNotFoundException(paramString, localPrivilegedActionException.getException());
      }
    }
  }

  protected PermissionCollection getPermissions(CodeSource paramCodeSource)
  {
    Permission localPermission1;
    PermissionCollection localPermissionCollection = super.getPermissions(paramCodeSource);
    URL localURL = paramCodeSource.getLocation();
    String str1 = null;
    try
    {
      localPermission1 = localURL.openConnection().getPermission();
    }
    catch (IOException localIOException1)
    {
      localPermission1 = null;
    }
    if (localPermission1 instanceof FilePermission)
    {
      str1 = localPermission1.getName();
    }
    else if ((localPermission1 == null) && (localURL.getProtocol().equals("file")))
    {
      str1 = localURL.getFile().replace('/', File.separatorChar);
      str1 = ParseUtil.decode(str1);
    }
    if (str1 != null)
    {
      Permission localPermission2;
      String str2;
      if (!(str1.endsWith(File.separator)))
      {
        int i = str1.lastIndexOf(File.separatorChar);
        if (i != -1)
        {
          str1 = str1.substring(0, i + 1) + "-";
          localPermissionCollection.add(new FilePermission(str1, "read"));
        }
      }
      localPermissionCollection.add(new SocketPermission("localhost", "connect,accept"));
      AccessController.doPrivileged(new PrivilegedAction(this, localPermissionCollection)
      {
        public Object run()
        {
          String str;
          try
          {
            str = InetAddress.getLocalHost().getHostName();
            this.val$perms.add(new SocketPermission(str, "connect,accept"));
          }
          catch (UnknownHostException localUnknownHostException)
          {
          }
          return null;
        }
      });
      try
      {
        localPermission2 = this.base.openConnection().getPermission();
      }
      catch (IOException localIOException2)
      {
        localPermission2 = null;
      }
      if (localPermission2 instanceof FilePermission)
      {
        str2 = localPermission2.getName();
        if (str2.endsWith(File.separator))
          str2 = str2 + "-";
        localPermissionCollection.add(new FilePermission(str2, "read"));
      }
      else if ((localPermission2 == null) && (this.base.getProtocol().equals("file")))
      {
        str2 = this.base.getFile().replace('/', File.separatorChar);
        str2 = ParseUtil.decode(str2);
        if (str2.endsWith(File.separator))
          str2 = str2 + "-";
        localPermissionCollection.add(new FilePermission(str2, "read"));
      }
    }
    return localPermissionCollection;
  }

  private static byte[] getBytes(URL paramURL)
    throws IOException
  {
    Object localObject1;
    URLConnection localURLConnection = paramURL.openConnection();
    if (localURLConnection instanceof HttpURLConnection)
    {
      HttpURLConnection localHttpURLConnection = (HttpURLConnection)localURLConnection;
      int j = localHttpURLConnection.getResponseCode();
      if (j >= 400)
        throw new IOException("open HTTP connection failed.");
    }
    int i = localURLConnection.getContentLength();
    BufferedInputStream localBufferedInputStream = new BufferedInputStream(localURLConnection.getInputStream());
    try
    {
      byte[] arrayOfByte;
      if (i != -1)
      {
        localObject1 = new byte[i];
        while (true)
        {
          if (i <= 0)
            break label207;
          k = localBufferedInputStream.read(localObject1, localObject1.length - i, i);
          if (k == -1)
            throw new IOException("unexpected EOF");
          i -= k;
        }
      }
      localObject1 = new byte[8192];
      int k = 0;
      while (true)
      {
        do
        {
          if ((i = localBufferedInputStream.read(localObject1, k, localObject1.length - k)) == -1)
            break label178;
          k += i;
        }
        while (k < localObject1.length);
        arrayOfByte = new byte[k * 2];
        System.arraycopy(localObject1, 0, arrayOfByte, 0, k);
        localObject1 = arrayOfByte;
      }
      if (k != localObject1.length)
      {
        label178: arrayOfByte = new byte[k];
        System.arraycopy(localObject1, 0, arrayOfByte, 0, k);
        label207: localObject1 = arrayOfByte;
      }
    }
    finally
    {
      localBufferedInputStream.close();
    }
    return ((B)localObject1);
  }

  public InputStream getResourceAsStream(String paramString)
  {
    if (paramString == null)
      throw new NullPointerException("name");
    try
    {
      InputStream localInputStream = null;
      synchronized (this.syncResourceAsStream)
      {
        this.resourceAsStreamInCall = true;
        localInputStream = super.getResourceAsStream(paramString);
        this.resourceAsStreamInCall = false;
      }
      if ((this.codebaseLookup == true) && (localInputStream == null))
      {
        ??? = new URL(this.base, ParseUtil.encodePath(paramString, false));
        localInputStream = ((URL)???).openStream();
      }
      return localInputStream;
    }
    catch (Exception localException)
    {
    }
    return ((InputStream)null);
  }

  public InputStream getResourceAsStreamFromJar(String paramString)
  {
    if (paramString == null)
      throw new NullPointerException("name");
    try
    {
      InputStream localInputStream = null;
      synchronized (this.syncResourceAsStreamFromJar)
      {
        this.resourceAsStreamFromJarInCall = true;
        localInputStream = super.getResourceAsStream(paramString);
        this.resourceAsStreamFromJarInCall = false;
      }
      return localInputStream;
    }
    catch (Exception localException)
    {
    }
    return null;
  }

  public URL findResource(String paramString)
  {
    URL localURL = super.findResource(paramString);
    if (paramString.startsWith("META-INF/"))
      return localURL;
    if (!(this.codebaseLookup))
      return localURL;
    if (localURL == null)
    {
      boolean bool1 = false;
      synchronized (this.syncResourceAsStreamFromJar)
      {
        bool1 = this.resourceAsStreamFromJarInCall;
      }
      if (bool1)
        return null;
      boolean bool2 = false;
      synchronized (this.syncResourceAsStream)
      {
        bool2 = this.resourceAsStreamInCall;
      }
      if (!(bool2))
        try
        {
          localURL = new URL(this.base, ParseUtil.encodePath(paramString, false));
          if (!(resourceExists(localURL)))
            localURL = null;
        }
        catch (Exception localException)
        {
          localURL = null;
        }
    }
    return localURL;
  }

  private boolean resourceExists(URL paramURL)
  {
    int i = 1;
    try
    {
      Object localObject;
      URLConnection localURLConnection = paramURL.openConnection();
      if (localURLConnection instanceof HttpURLConnection)
      {
        localObject = (HttpURLConnection)localURLConnection;
        ((HttpURLConnection)localObject).setRequestMethod("HEAD");
        int j = ((HttpURLConnection)localObject).getResponseCode();
        if (j == 200)
          return true;
        if (j >= 400)
          return false;
      }
      else
      {
        localObject = localURLConnection.getInputStream();
        ((InputStream)localObject).close();
      }
    }
    catch (Exception localException)
    {
      i = 0;
    }
    return i;
  }

  public Enumeration findResources(String paramString)
    throws IOException
  {
    Enumeration localEnumeration = super.findResources(paramString);
    if (paramString.startsWith("META-INF/"))
      return localEnumeration;
    if (!(this.codebaseLookup))
      return localEnumeration;
    URL localURL1 = new URL(this.base, ParseUtil.encodePath(paramString, false));
    if (!(resourceExists(localURL1)))
      localURL1 = null;
    URL localURL2 = localURL1;
    return new Enumeration(this, localEnumeration, localURL2)
    {
      private boolean done;

      public Object nextElement()
      {
        if (!(this.done))
        {
          if (this.val$e.hasMoreElements())
            return this.val$e.nextElement();
          this.done = true;
          if (this.val$url != null)
            return this.val$url;
        }
        throw new NoSuchElementException();
      }

      public boolean hasMoreElements()
      {
        return ((!(this.done)) && (((this.val$e.hasMoreElements()) || (this.val$url != null))));
      }
    };
  }

  Class loadCode(String paramString)
    throws ClassNotFoundException
  {
    paramString = paramString.replace('/', '.');
    paramString = paramString.replace(File.separatorChar, '.');
    String str1 = null;
    int i = paramString.indexOf(";");
    if (i != -1)
    {
      str1 = paramString.substring(i, paramString.length());
      paramString = paramString.substring(0, i);
    }
    String str2 = paramString;
    if ((paramString.endsWith(".class")) || (paramString.endsWith(".java")))
      paramString = paramString.substring(0, paramString.lastIndexOf(46));
    try
    {
      if (str1 != null)
        paramString = paramString + str1;
      return loadClass(paramString);
    }
    catch (ClassNotFoundException localClassNotFoundException)
    {
      if (str1 != null)
        str2 = str2 + str1;
    }
    return loadClass(str2);
  }

  public ThreadGroup getThreadGroup()
  {
    synchronized (this.threadGroupSynchronizer)
    {
      if ((this.threadGroup == null) || (this.threadGroup.isDestroyed()))
        AccessController.doPrivileged(new PrivilegedAction(this)
        {
          public Object run()
          {
            AppletClassLoader.access$202(this.this$0, new AppletThreadGroup(AppletClassLoader.access$000(this.this$0) + "-threadGroup"));
            AppContextCreator localAppContextCreator = new AppContextCreator(AppletClassLoader.access$200(this.this$0));
            localAppContextCreator.setContextClassLoader(this.this$0);
            synchronized (localAppContextCreator.syncObject)
            {
              localAppContextCreator.start();
              try
              {
                localAppContextCreator.syncObject.wait();
              }
              catch (InterruptedException localInterruptedException)
              {
              }
              AppletClassLoader.access$302(this.this$0, localAppContextCreator.appContext);
            }
            return null;
          }
        });
      return this.threadGroup;
    }
  }

  public AppContext getAppContext()
  {
    return this.appContext;
  }

  public void grab()
  {
    synchronized (this.grabReleaseSynchronizer)
    {
      this.usageCount += 1;
    }
    getThreadGroup();
  }

  protected void setExceptionStatus()
  {
    this.exceptionStatus = true;
  }

  public boolean getExceptionStatus()
  {
    return this.exceptionStatus;
  }

  protected void release()
  {
    AppContext localAppContext = null;
    synchronized (this.grabReleaseSynchronizer)
    {
      if (this.usageCount > 1)
        this.usageCount -= 1;
      else
        localAppContext = resetAppContext();
    }
    if (localAppContext != null)
      try
      {
        localAppContext.dispose();
      }
      catch (IllegalThreadStateException localIllegalThreadStateException)
      {
      }
  }

  protected AppContext resetAppContext()
  {
    AppContext localAppContext = null;
    synchronized (this.threadGroupSynchronizer)
    {
      localAppContext = this.appContext;
      this.usageCount = 0;
      this.appContext = null;
      this.threadGroup = null;
    }
    return localAppContext;
  }

  void setJDK11Target(Class paramClass, boolean paramBoolean)
  {
    this.jdk11AppletInfo.put(paramClass.toString(), Boolean.valueOf(paramBoolean));
  }

  void setJDK12Target(Class paramClass, boolean paramBoolean)
  {
    this.jdk12AppletInfo.put(paramClass.toString(), Boolean.valueOf(paramBoolean));
  }

  Boolean isJDK11Target(Class paramClass)
  {
    return ((Boolean)this.jdk11AppletInfo.get(paramClass.toString()));
  }

  Boolean isJDK12Target(Class paramClass)
  {
    return ((Boolean)this.jdk12AppletInfo.get(paramClass.toString()));
  }

  private static void printError(String paramString, Throwable paramThrowable)
  {
    String str = null;
    if (paramThrowable == null)
      str = mh.getMessage("filenotfound", paramString);
    else if (paramThrowable instanceof IOException)
      str = mh.getMessage("fileioexception", paramString);
    else if (paramThrowable instanceof ClassFormatError)
      str = mh.getMessage("fileformat", paramString);
    else if (paramThrowable instanceof ThreadDeath)
      str = mh.getMessage("filedeath", paramString);
    else if (paramThrowable instanceof Error)
      str = mh.getMessage("fileerror", paramThrowable.toString(), paramString);
    if (str != null)
      System.err.println(str);
  }
}