package sun.misc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilePermission;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.SocketPermission;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.security.AccessControlException;
import java.security.AccessController;
import java.security.CodeSigner;
import java.security.Permission;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import sun.net.www.ParseUtil;
import sun.security.action.GetPropertyAction;

public class URLClassPath
{
  static final String USER_AGENT_JAVA_VERSION = "UA-Java-Version";
  static final String JAVA_VERSION = (String)AccessController.doPrivileged(new GetPropertyAction("java.version"));
  private static final boolean DEBUG = AccessController.doPrivileged(new GetPropertyAction("sun.misc.URLClassPath.debug")) != null;
  private ArrayList path;
  Stack urls;
  ArrayList loaders;
  HashMap lmap;
  private URLStreamHandler jarHandler;

  public URLClassPath(URL[] paramArrayOfURL, URLStreamHandlerFactory paramURLStreamHandlerFactory)
  {
    this.path = new ArrayList();
    this.urls = new Stack();
    this.loaders = new ArrayList();
    this.lmap = new HashMap();
    for (int i = 0; i < paramArrayOfURL.length; ++i)
      this.path.add(paramArrayOfURL[i]);
    push(paramArrayOfURL);
    if (paramURLStreamHandlerFactory != null)
      this.jarHandler = paramURLStreamHandlerFactory.createURLStreamHandler("jar");
  }

  public URLClassPath(URL[] paramArrayOfURL)
  {
    this(paramArrayOfURL, null);
  }

  public void addURL(URL paramURL)
  {
    synchronized (this.urls)
    {
      if (!(this.path.contains(paramURL)))
        break label21;
      return;
      label21: this.urls.add(0, paramURL);
      this.path.add(paramURL);
    }
  }

  public URL[] getURLs()
  {
    synchronized (this.urls)
    {
      return ((URL[])(URL[])this.path.toArray(new URL[this.path.size()]));
    }
  }

  public URL findResource(String paramString, boolean paramBoolean)
  {
    for (int i = 0; (localLoader = getLoader(i)) != null; ++i)
    {
      Loader localLoader;
      URL localURL = localLoader.findResource(paramString, paramBoolean);
      if (localURL != null)
        return localURL;
    }
    return null;
  }

  public Resource getResource(String paramString, boolean paramBoolean)
  {
    if (DEBUG)
      System.err.println("URLClassPath.getResource(\"" + paramString + "\")");
    for (int i = 0; (localLoader = getLoader(i)) != null; ++i)
    {
      Loader localLoader;
      Resource localResource = localLoader.getResource(paramString, paramBoolean);
      if (localResource != null)
        return localResource;
    }
    return null;
  }

  public Enumeration findResources(String paramString, boolean paramBoolean)
  {
    return new Enumeration(this, paramString, paramBoolean)
    {
      private int index = 0;
      private URL url = null;

      private boolean next()
      {
        if (this.url != null)
          return true;
        do
        {
          URLClassPath.Loader localLoader;
          if ((localLoader = URLClassPath.access$000(this.this$0, this.index++)) == null)
            break label57;
          this.url = localLoader.findResource(this.val$name, this.val$check);
        }
        while (this.url == null);
        return true;
        label57: return false;
      }

      public boolean hasMoreElements()
      {
        return next();
      }

      public Object nextElement()
      {
        if (!(next()))
          throw new NoSuchElementException();
        URL localURL = this.url;
        this.url = null;
        return localURL;
      }
    };
  }

  public Resource getResource(String paramString)
  {
    return getResource(paramString, true);
  }

  public Enumeration getResources(String paramString, boolean paramBoolean)
  {
    return new Enumeration(this, paramString, paramBoolean)
    {
      private int index = 0;
      private Resource res = null;

      private boolean next()
      {
        if (this.res != null)
          return true;
        do
        {
          URLClassPath.Loader localLoader;
          if ((localLoader = URLClassPath.access$000(this.this$0, this.index++)) == null)
            break label57;
          this.res = localLoader.getResource(this.val$name, this.val$check);
        }
        while (this.res == null);
        return true;
        label57: return false;
      }

      public boolean hasMoreElements()
      {
        return next();
      }

      public Object nextElement()
      {
        if (!(next()))
          throw new NoSuchElementException();
        Resource localResource = this.res;
        this.res = null;
        return localResource;
      }
    };
  }

  public Enumeration getResources(String paramString)
  {
    return getResources(paramString, true);
  }

  private synchronized Loader getLoader(int paramInt)
  {
    while (true)
    {
      label0: label34: URL localURL;
      while (true)
      {
        if (this.loaders.size() >= paramInt + 1)
          break label124;
        synchronized (this.urls)
        {
          if (!(this.urls.empty()))
            break label34;
          return null;
          localURL = (URL)this.urls.pop();
        }
        if (!(this.lmap.containsKey(localURL)))
          break;
      }
      try
      {
        ??? = getLoader(localURL);
        URL[] arrayOfURL = ((Loader)???).getClassPath();
        if (arrayOfURL != null)
          push(arrayOfURL);
      }
      catch (IOException localIOException)
      {
        break label0:
      }
      this.loaders.add(???);
      this.lmap.put(localURL, ???);
    }
    label124: return ((Loader)(Loader)this.loaders.get(paramInt));
  }

  private Loader getLoader(URL paramURL)
    throws IOException
  {
    try
    {
      return ((Loader)AccessController.doPrivileged(new PrivilegedExceptionAction(this, paramURL)
      {
        public Object run()
          throws IOException
        {
          String str = this.val$url.getFile();
          if ((str != null) && (str.endsWith("/")))
          {
            if ("file".equals(this.val$url.getProtocol()))
              return new URLClassPath.FileLoader(this.val$url);
            return new URLClassPath.Loader(this.val$url);
          }
          return new URLClassPath.JarLoader(this.val$url, URLClassPath.access$100(this.this$0), this.this$0.lmap);
        }
      }));
    }
    catch (PrivilegedActionException localPrivilegedActionException)
    {
      throw ((IOException)localPrivilegedActionException.getException());
    }
  }

  private void push(URL[] paramArrayOfURL)
  {
    synchronized (this.urls)
    {
      for (int i = paramArrayOfURL.length - 1; i >= 0; --i)
        this.urls.push(paramArrayOfURL[i]);
    }
  }

  public static URL[] pathToURLs(String paramString)
  {
    Object localObject2;
    StringTokenizer localStringTokenizer = new StringTokenizer(paramString, File.pathSeparator);
    Object localObject1 = new URL[localStringTokenizer.countTokens()];
    int i = 0;
    while (localStringTokenizer.hasMoreTokens())
    {
      localObject2 = new File(localStringTokenizer.nextToken());
      try
      {
        localObject2 = new File(((File)localObject2).getCanonicalPath());
      }
      catch (IOException localIOException1)
      {
      }
      try
      {
        localObject1[(i++)] = ParseUtil.fileToEncodedURL((File)localObject2);
      }
      catch (IOException localIOException2)
      {
      }
    }
    if (localObject1.length != i)
    {
      localObject2 = new URL[i];
      System.arraycopy(localObject1, 0, localObject2, 0, i);
      localObject1 = localObject2;
    }
    return ((URL)(URL)localObject1);
  }

  public URL checkURL(URL paramURL)
  {
    try
    {
      check(paramURL);
    }
    catch (Exception localException)
    {
      return null;
    }
    return paramURL;
  }

  static void check(URL paramURL)
    throws IOException
  {
    SecurityManager localSecurityManager = System.getSecurityManager();
    if (localSecurityManager != null)
    {
      URLConnection localURLConnection = paramURL.openConnection();
      Permission localPermission = localURLConnection.getPermission();
      if (localPermission != null)
        try
        {
          localSecurityManager.checkPermission(localPermission);
        }
        catch (SecurityException localSecurityException)
        {
          if ((localPermission instanceof FilePermission) && (localPermission.getActions().indexOf("read") != -1))
          {
            localSecurityManager.checkRead(localPermission.getName());
          }
          else if ((localPermission instanceof SocketPermission) && (localPermission.getActions().indexOf("connect") != -1))
          {
            URL localURL = paramURL;
            if (localURLConnection instanceof JarURLConnection)
              localURL = ((JarURLConnection)localURLConnection).getJarFileURL();
            localSecurityManager.checkConnect(localURL.getHost(), localURL.getPort());
          }
          else
          {
            throw localSecurityException;
          }
        }
    }
  }

  private static class FileLoader extends URLClassPath.Loader
  {
    private File dir;

    FileLoader(URL paramURL)
      throws IOException
    {
      super(paramURL);
      if (!("file".equals(paramURL.getProtocol())))
        throw new IllegalArgumentException("url");
      String str = paramURL.getFile().replace('/', File.separatorChar);
      str = ParseUtil.decode(str);
      this.dir = new File(str).getCanonicalFile();
    }

    URL findResource(String paramString, boolean paramBoolean)
    {
      Resource localResource = getResource(paramString, paramBoolean);
      if (localResource != null)
        return localResource.getURL();
      return null;
    }

    Resource getResource(String paramString, boolean paramBoolean)
    {
      URL localURL2;
      try
      {
        localURL2 = new URL(getBaseURL(), ".");
        URL localURL1 = new URL(getBaseURL(), ParseUtil.encodePath(paramString, false));
        if (!(localURL1.getFile().startsWith(localURL2.getFile())))
          return null;
        if (paramBoolean)
          URLClassPath.check(localURL1);
        if (paramString.indexOf("..") != -1)
        {
          localFile = new File(this.dir, paramString.replace('/', File.separatorChar)).getCanonicalFile();
          if (localFile.getPath().startsWith(this.dir.getPath()))
            break label134;
          return null;
        }
        File localFile = new File(this.dir, paramString.replace('/', File.separatorChar));
        if (localFile.exists())
          label134: return new Resource(this, paramString, localURL1, localFile)
          {
            public String getName()
            {
              return this.val$name;
            }

            public URL getURL()
            {
              return this.val$url;
            }

            public URL getCodeSourceURL()
            {
              return this.this$0.getBaseURL();
            }

            public InputStream getInputStream()
              throws IOException
            {
              return new FileInputStream(this.val$file);
            }

            public int getContentLength()
              throws IOException
            {
              return (int)this.val$file.length();
            }
          };
      }
      catch (Exception localException)
      {
        return null;
      }
      return null;
    }
  }

  static class JarLoader extends URLClassPath.Loader
  {
    private JarFile jar;
    private URL csu;
    private JarIndex index;
    private MetaIndex metaIndex;
    private URLStreamHandler handler;
    private HashMap lmap;

    JarLoader(URL paramURL, URLStreamHandler paramURLStreamHandler, HashMap paramHashMap)
      throws IOException
    {
      super(new URL("jar", "", -1, paramURL + "!/", paramURLStreamHandler));
      this.csu = paramURL;
      this.handler = paramURLStreamHandler;
      this.lmap = paramHashMap;
      if (!(isOptimizable(paramURL)))
      {
        ensureOpen();
      }
      else
      {
        String str = paramURL.getFile();
        if (str != null)
        {
          str = ParseUtil.decode(str);
          File localFile = new File(str);
          this.metaIndex = MetaIndex.forJar(localFile);
          if ((this.metaIndex != null) && (!(localFile.exists())))
            this.metaIndex = null;
        }
        if (this.metaIndex == null)
          ensureOpen();
      }
    }

    JarFile getJarFile()
    {
      return this.jar;
    }

    private boolean isOptimizable(URL paramURL)
    {
      return "file".equals(paramURL.getProtocol());
    }

    private void ensureOpen()
      throws IOException
    {
      if (this.jar == null)
        try
        {
          AccessController.doPrivileged(new PrivilegedExceptionAction(this)
          {
            public Object run()
              throws IOException
            {
              if (URLClassPath.access$300())
              {
                System.err.println("Opening " + URLClassPath.JarLoader.access$400(this.this$0));
                Thread.dumpStack();
              }
              URLClassPath.JarLoader.access$502(this.this$0, URLClassPath.JarLoader.access$600(this.this$0, URLClassPath.JarLoader.access$400(this.this$0)));
              URLClassPath.JarLoader.access$702(this.this$0, JarIndex.getJarIndex(URLClassPath.JarLoader.access$500(this.this$0), URLClassPath.JarLoader.access$800(this.this$0)));
              if (URLClassPath.JarLoader.access$700(this.this$0) != null)
              {
                String[] arrayOfString = URLClassPath.JarLoader.access$700(this.this$0).getJarFiles();
                for (int i = 0; i < arrayOfString.length; ++i)
                  try
                  {
                    URL localURL = new URL(URLClassPath.JarLoader.access$400(this.this$0), arrayOfString[i]);
                    if (!(URLClassPath.JarLoader.access$900(this.this$0).containsKey(localURL)))
                      URLClassPath.JarLoader.access$900(this.this$0).put(localURL, null);
                  }
                  catch (MalformedURLException localMalformedURLException)
                  {
                  }
              }
              return null;
            }
          });
        }
        catch (PrivilegedActionException localPrivilegedActionException)
        {
          throw ((IOException)localPrivilegedActionException.getException());
        }
    }

    private JarFile getJarFile(URL paramURL)
      throws IOException
    {
      if (isOptimizable(paramURL))
      {
        localObject = new FileURLMapper(paramURL);
        if (!(((FileURLMapper)localObject).exists()))
          throw new FileNotFoundException(((FileURLMapper)localObject).getPath());
        return new JarFile(((FileURLMapper)localObject).getPath());
      }
      Object localObject = getBaseURL().openConnection();
      ((URLConnection)localObject).setRequestProperty("UA-Java-Version", URLClassPath.JAVA_VERSION);
      return ((JarFile)((JarURLConnection)localObject).getJarFile());
    }

    JarIndex getIndex()
    {
      try
      {
        ensureOpen();
      }
      catch (IOException localIOException)
      {
        throw ((InternalError)new InternalError().initCause(localIOException));
      }
      return this.index;
    }

    Resource checkResource(String paramString, boolean paramBoolean, JarEntry paramJarEntry)
    {
      URL localURL;
      try
      {
        localURL = new URL(getBaseURL(), ParseUtil.encodePath(paramString, false));
        if (paramBoolean)
          URLClassPath.check(localURL);
      }
      catch (MalformedURLException localMalformedURLException)
      {
        return null;
      }
      catch (IOException localIOException)
      {
        return null;
      }
      catch (AccessControlException localAccessControlException)
      {
        return null;
      }
      return new Resource(this, paramString, localURL, paramJarEntry)
      {
        public String getName()
        {
          return this.val$name;
        }

        public URL getURL()
        {
          return this.val$url;
        }

        public URL getCodeSourceURL()
        {
          return URLClassPath.JarLoader.access$400(this.this$0);
        }

        public InputStream getInputStream()
          throws IOException
        {
          return URLClassPath.JarLoader.access$500(this.this$0).getInputStream(this.val$entry);
        }

        public int getContentLength()
        {
          return (int)this.val$entry.getSize();
        }

        public Manifest getManifest()
          throws IOException
        {
          return URLClassPath.JarLoader.access$500(this.this$0).getManifest();
        }

        public Certificate[] getCertificates()
        {
          return this.val$entry.getCertificates();
        }

        public CodeSigner[] getCodeSigners()
        {
          return this.val$entry.getCodeSigners();
        }
      };
    }

    boolean validIndex(String paramString)
    {
      int i;
      String str2;
      String str1 = paramString;
      if ((i = paramString.lastIndexOf("/")) != -1)
        str1 = paramString.substring(0, i);
      Enumeration localEnumeration = this.jar.entries();
      do
      {
        if (!(localEnumeration.hasMoreElements()))
          break label92;
        ZipEntry localZipEntry = (ZipEntry)localEnumeration.nextElement();
        str2 = localZipEntry.getName();
        if ((i = str2.lastIndexOf("/")) != -1)
          str2 = str2.substring(0, i);
      }
      while (!(str2.equals(str1)));
      return true;
      label92: return false;
    }

    URL findResource(String paramString, boolean paramBoolean)
    {
      Resource localResource = getResource(paramString, paramBoolean);
      if (localResource != null)
        return localResource.getURL();
      return null;
    }

    Resource getResource(String paramString, boolean paramBoolean)
    {
      if ((this.metaIndex != null) && (!(this.metaIndex.mayContain(paramString))))
        return null;
      try
      {
        ensureOpen();
      }
      catch (IOException localIOException)
      {
        throw ((InternalError)new InternalError().initCause(localIOException));
      }
      JarEntry localJarEntry = this.jar.getJarEntry(paramString);
      if (localJarEntry != null)
        return checkResource(paramString, paramBoolean, localJarEntry);
      if (this.index == null)
        return null;
      HashSet localHashSet = new HashSet();
      return getResource(paramString, paramBoolean, localHashSet);
    }

    Resource getResource(String paramString, boolean paramBoolean, Set paramSet)
    {
      int i = 0;
      int j = 0;
      LinkedList localLinkedList = null;
      if ((localLinkedList = this.index.get(paramString)) == null)
        return null;
      do
      {
        Object[] arrayOfObject = localLinkedList.toArray();
        int k = localLinkedList.size();
        while (true)
        {
          label39: Resource localResource;
          JarLoader localJarLoader;
          while (true)
          {
            int l;
            do
            {
              URL localURL;
              if (j >= k)
                break label321;
              String str = (String)arrayOfObject[(j++)];
              try
              {
                localURL = new URL(this.csu, str);
                if ((localJarLoader = (JarLoader)this.lmap.get(localURL)) == null)
                {
                  localJarLoader = (JarLoader)AccessController.doPrivileged(new PrivilegedExceptionAction(this, localURL)
                  {
                    public Object run()
                      throws IOException
                    {
                      return new URLClassPath.JarLoader(this.val$url, URLClassPath.JarLoader.access$1000(this.this$0), URLClassPath.JarLoader.access$900(this.this$0));
                    }
                  });
                  JarIndex localJarIndex = localJarLoader.getIndex();
                  if (localJarIndex != null)
                  {
                    int i1 = str.lastIndexOf("/");
                    localJarIndex.merge(this.index, (i1 == -1) ? null : str.substring(0, i1 + 1));
                  }
                  this.lmap.put(localURL, localJarLoader);
                }
              }
              catch (PrivilegedActionException localPrivilegedActionException)
              {
                break label39:
              }
              catch (MalformedURLException localMalformedURLException)
              {
                break label39:
              }
              l = (!(paramSet.add(localURL))) ? 1 : 0;
              if (l == 0)
              {
                try
                {
                  localJarLoader.ensureOpen();
                }
                catch (IOException localIOException)
                {
                  throw ((InternalError)new InternalError().initCause(localIOException));
                }
                JarEntry localJarEntry = localJarLoader.jar.getJarEntry(paramString);
                if (localJarEntry != null)
                  return localJarLoader.checkResource(paramString, paramBoolean, localJarEntry);
                if (!(localJarLoader.validIndex(paramString)))
                  throw new InvalidJarIndexException("Invalid index");
              }
            }
            while ((l != 0) || (localJarLoader == this));
            if (localJarLoader.getIndex() != null)
              break;
          }
          if ((localResource = localJarLoader.getResource(paramString, paramBoolean, paramSet)) != null)
            return localResource;
        }
        label321: localLinkedList = this.index.get(paramString);
      }
      while (j < localLinkedList.size());
      return null;
    }

    URL[] getClassPath()
      throws IOException
    {
      if (this.index != null)
        return null;
      if (this.metaIndex != null)
        return null;
      ensureOpen();
      parseExtensionsDependencies();
      if (SharedSecrets.javaUtilJarAccess().jarFileHasClassPathAttribute(this.jar))
      {
        Manifest localManifest = this.jar.getManifest();
        if (localManifest != null)
        {
          Attributes localAttributes = localManifest.getMainAttributes();
          if (localAttributes != null)
          {
            String str = localAttributes.getValue(Attributes.Name.CLASS_PATH);
            if (str != null)
              return parseClassPath(this.csu, str);
          }
        }
      }
      return null;
    }

    private void parseExtensionsDependencies()
      throws IOException
    {
      ExtensionDependency.checkExtensionsDependencies(this.jar);
    }

    private URL[] parseClassPath(URL paramURL, String paramString)
      throws MalformedURLException
    {
      StringTokenizer localStringTokenizer = new StringTokenizer(paramString);
      URL[] arrayOfURL = new URL[localStringTokenizer.countTokens()];
      for (int i = 0; localStringTokenizer.hasMoreTokens(); ++i)
      {
        String str = localStringTokenizer.nextToken();
        arrayOfURL[i] = new URL(paramURL, str);
      }
      return arrayOfURL;
    }
  }

  private static class Loader
  {
    private final URL base;

    Loader(URL paramURL)
    {
      this.base = paramURL;
    }

    URL getBaseURL()
    {
      return this.base;
    }

    URL findResource(String paramString, boolean paramBoolean)
    {
      URL localURL;
      try
      {
        localURL = new URL(this.base, ParseUtil.encodePath(paramString, false));
      }
      catch (MalformedURLException localMalformedURLException)
      {
        throw new IllegalArgumentException("name");
      }
      try
      {
        Object localObject;
        if (paramBoolean)
          URLClassPath.check(localURL);
        URLConnection localURLConnection = localURL.openConnection();
        if (localURLConnection instanceof HttpURLConnection)
        {
          localObject = (HttpURLConnection)localURLConnection;
          ((HttpURLConnection)localObject).setRequestMethod("HEAD");
          if (((HttpURLConnection)localObject).getResponseCode() >= 400)
            return null;
        }
        else
        {
          localObject = localURL.openStream();
          ((InputStream)localObject).close();
        }
        return localURL;
      }
      catch (Exception localException)
      {
      }
      return ((URL)null);
    }

    Resource getResource(String paramString, boolean paramBoolean)
    {
      URL localURL;
      URLConnection localURLConnection;
      try
      {
        localURL = new URL(this.base, ParseUtil.encodePath(paramString, false));
      }
      catch (MalformedURLException localMalformedURLException)
      {
        throw new IllegalArgumentException("name");
      }
      try
      {
        if (paramBoolean)
          URLClassPath.check(localURL);
        localURLConnection = localURL.openConnection();
        InputStream localInputStream = localURLConnection.getInputStream();
      }
      catch (Exception localException)
      {
        return null;
      }
      return new Resource(this, paramString, localURL, localURLConnection)
      {
        public String getName()
        {
          return this.val$name;
        }

        public URL getURL()
        {
          return this.val$url;
        }

        public URL getCodeSourceURL()
        {
          return URLClassPath.Loader.access$200(this.this$0);
        }

        public InputStream getInputStream()
          throws IOException
        {
          return this.val$uc.getInputStream();
        }

        public int getContentLength()
          throws IOException
        {
          return this.val$uc.getContentLength();
        }
      };
    }

    Resource getResource(String paramString)
    {
      return getResource(paramString, true);
    }

    URL[] getClassPath()
      throws IOException
    {
      return null;
    }
  }
}