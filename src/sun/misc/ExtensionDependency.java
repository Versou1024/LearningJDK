package sun.misc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import sun.net.www.ParseUtil;
import sun.security.action.GetPropertyAction;

public class ExtensionDependency
{
  private static Vector providers;
  static final boolean DEBUG = 0;

  public static synchronized void addExtensionInstallationProvider(ExtensionInstallationProvider paramExtensionInstallationProvider)
  {
    if (providers == null)
      providers = new Vector();
    providers.add(paramExtensionInstallationProvider);
  }

  public static synchronized void removeExtensionInstallationProvider(ExtensionInstallationProvider paramExtensionInstallationProvider)
  {
    providers.remove(paramExtensionInstallationProvider);
  }

  public static boolean checkExtensionsDependencies(JarFile paramJarFile)
  {
    if (providers == null)
      return true;
    try
    {
      ExtensionDependency localExtensionDependency = new ExtensionDependency();
      return localExtensionDependency.checkExtensions(paramJarFile);
    }
    catch (ExtensionInstallationException localExtensionInstallationException)
    {
      debug(localExtensionInstallationException.getMessage());
    }
    return false;
  }

  protected boolean checkExtensions(JarFile paramJarFile)
    throws sun.misc.ExtensionInstallationException
  {
    Manifest localManifest;
    try
    {
      localManifest = paramJarFile.getManifest();
    }
    catch (IOException localIOException)
    {
      return false;
    }
    if (localManifest == null)
      return true;
    int i = 1;
    Attributes localAttributes = localManifest.getMainAttributes();
    if (localAttributes != null)
    {
      String str1 = localAttributes.getValue(Attributes.Name.EXTENSION_LIST);
      if (str1 != null)
      {
        StringTokenizer localStringTokenizer = new StringTokenizer(str1);
        while (localStringTokenizer.hasMoreTokens())
        {
          String str2 = localStringTokenizer.nextToken();
          debug("The file " + paramJarFile.getName() + " appears to depend on " + str2);
          String str3 = str2 + "-" + Attributes.Name.EXTENSION_NAME.toString();
          if (localAttributes.getValue(str3) == null)
          {
            debug("The jar file " + paramJarFile.getName() + " appers to depend on " + str2 + " but does not define the " + str3 + " attribute in its manifest ");
          }
          else if (!(checkExtension(str2, localAttributes)))
          {
            debug("Failed installing " + str2);
            i = 0;
          }
        }
      }
      else
      {
        debug("No dependencies for " + paramJarFile.getName());
      }
    }
    return i;
  }

  protected synchronized boolean checkExtension(String paramString, Attributes paramAttributes)
    throws sun.misc.ExtensionInstallationException
  {
    debug("Checking extension " + paramString);
    if (checkExtensionAgainstInstalled(paramString, paramAttributes))
      return true;
    debug("Extension not currently installed ");
    ExtensionInfo localExtensionInfo = new ExtensionInfo(paramString, paramAttributes);
    return installExtension(localExtensionInfo, null);
  }

  boolean checkExtensionAgainstInstalled(String paramString, Attributes paramAttributes)
    throws sun.misc.ExtensionInstallationException
  {
    File[] arrayOfFile;
    File localFile = checkExtensionExists(paramString);
    if (localFile != null)
    {
      try
      {
        if (checkExtensionAgainst(paramString, paramAttributes, localFile))
          return true;
      }
      catch (FileNotFoundException localFileNotFoundException1)
      {
        debugException(localFileNotFoundException1);
      }
      catch (IOException localIOException1)
      {
        debugException(localIOException1);
      }
      return false;
    }
    try
    {
      arrayOfFile = getInstalledExtensions();
    }
    catch (IOException localIOException2)
    {
      debugException(localIOException2);
      return false;
    }
    for (int i = 0; i < arrayOfFile.length; ++i)
      try
      {
        if (checkExtensionAgainst(paramString, paramAttributes, arrayOfFile[i]))
          return true;
      }
      catch (FileNotFoundException localFileNotFoundException2)
      {
        debugException(localFileNotFoundException2);
      }
      catch (IOException localIOException3)
      {
        debugException(localIOException3);
      }
    return false;
  }

  protected boolean checkExtensionAgainst(String paramString, Attributes paramAttributes, File paramFile)
    throws IOException, FileNotFoundException, sun.misc.ExtensionInstallationException
  {
    Manifest localManifest;
    debug("Checking extension " + paramString + " against " + paramFile.getName());
    try
    {
      localManifest = (Manifest)AccessController.doPrivileged(new PrivilegedExceptionAction(this, paramFile)
      {
        public Object run()
          throws IOException, FileNotFoundException
        {
          if (!(this.val$file.exists()))
            throw new FileNotFoundException(this.val$file.getName());
          JarFile localJarFile = new JarFile(this.val$file);
          return localJarFile.getManifest();
        }
      });
    }
    catch (PrivilegedActionException localPrivilegedActionException)
    {
      if (localPrivilegedActionException.getException() instanceof FileNotFoundException)
        throw ((FileNotFoundException)localPrivilegedActionException.getException());
      throw ((IOException)localPrivilegedActionException.getException());
    }
    ExtensionInfo localExtensionInfo1 = new ExtensionInfo(paramString, paramAttributes);
    debug("Requested Extension : " + localExtensionInfo1);
    int i = 4;
    ExtensionInfo localExtensionInfo2 = null;
    if (localManifest != null)
    {
      Attributes localAttributes = localManifest.getMainAttributes();
      if (localAttributes != null)
      {
        localExtensionInfo2 = new ExtensionInfo(null, localAttributes);
        debug("Extension Installed " + localExtensionInfo2);
        i = localExtensionInfo2.isCompatibleWith(localExtensionInfo1);
        switch (i)
        {
        case 0:
          debug("Extensions are compatible");
          return true;
        case 4:
          debug("Extensions are incompatible");
          return false;
        }
        debug("Extensions require an upgrade or vendor switch");
        return installExtension(localExtensionInfo1, localExtensionInfo2);
      }
    }
    return false;
  }

  protected boolean installExtension(ExtensionInfo paramExtensionInfo1, ExtensionInfo paramExtensionInfo2)
    throws sun.misc.ExtensionInstallationException
  {
    Vector localVector;
    synchronized (providers)
    {
      localVector = (Vector)providers.clone();
    }
    ??? = localVector.elements();
    while (((Enumeration)???).hasMoreElements())
    {
      ExtensionInstallationProvider localExtensionInstallationProvider = (ExtensionInstallationProvider)((Enumeration)???).nextElement();
      if ((localExtensionInstallationProvider != null) && (localExtensionInstallationProvider.installExtension(paramExtensionInfo1, paramExtensionInfo2)))
      {
        debug(paramExtensionInfo1.name + " installation successful");
        Launcher.ExtClassLoader localExtClassLoader = (Launcher.ExtClassLoader)Launcher.getLauncher().getClassLoader().getParent();
        addNewExtensionsToClassLoader(localExtClassLoader);
        return true;
      }
    }
    debug(paramExtensionInfo1.name + " installation failed");
    return false;
  }

  private File checkExtensionExists(String paramString)
  {
    String str = paramString;
    String[] arrayOfString = { ".jar", ".zip" };
    return ((File)AccessController.doPrivileged(new PrivilegedAction(this, arrayOfString, str)
    {
      public Object run()
      {
        File[] arrayOfFile;
        try
        {
          arrayOfFile = ExtensionDependency.access$000();
          for (int i = 0; i < arrayOfFile.length; ++i)
            for (int j = 0; j < this.val$fileExt.length; ++j)
            {
              File localFile;
              if (this.val$extName.toLowerCase().endsWith(this.val$fileExt[j]))
                localFile = new File(arrayOfFile[i], this.val$extName);
              else
                localFile = new File(arrayOfFile[i], this.val$extName + this.val$fileExt[j]);
              ExtensionDependency.access$100("checkExtensionExists:fileName " + localFile.getName());
              if (localFile.exists())
                return localFile;
            }
          return null;
        }
        catch (Exception localException)
        {
          ExtensionDependency.access$200(this.this$0, localException);
        }
        return null;
      }
    }));
  }

  private static File[] getExtDirs()
  {
    File[] arrayOfFile;
    String str = (String)AccessController.doPrivileged(new GetPropertyAction("java.ext.dirs"));
    if (str != null)
    {
      StringTokenizer localStringTokenizer = new StringTokenizer(str, File.pathSeparator);
      int i = localStringTokenizer.countTokens();
      debug("getExtDirs count " + i);
      arrayOfFile = new File[i];
      for (int j = 0; j < i; ++j)
      {
        arrayOfFile[j] = new File(localStringTokenizer.nextToken());
        debug("getExtDirs dirs[" + j + "] " + arrayOfFile[j]);
      }
    }
    else
    {
      arrayOfFile = new File[0];
      debug("getExtDirs dirs " + arrayOfFile);
    }
    debug("getExtDirs dirs.length " + arrayOfFile.length);
    return arrayOfFile;
  }

  private static File[] getExtFiles(File[] paramArrayOfFile)
    throws IOException
  {
    Vector localVector = new Vector();
    for (int i = 0; i < paramArrayOfFile.length; ++i)
    {
      String[] arrayOfString = paramArrayOfFile[i].list(new JarFilter());
      if (arrayOfString != null)
      {
        debug("getExtFiles files.length " + arrayOfString.length);
        for (int j = 0; j < arrayOfString.length; ++j)
        {
          File localFile = new File(paramArrayOfFile[i], arrayOfString[j]);
          localVector.add(localFile);
          debug("getExtFiles f[" + j + "] " + localFile);
        }
      }
    }
    File[] arrayOfFile = new File[localVector.size()];
    localVector.copyInto(arrayOfFile);
    debug("getExtFiles ua.length " + arrayOfFile.length);
    return arrayOfFile;
  }

  private File[] getInstalledExtensions()
    throws IOException
  {
    return ((File[])(File[])AccessController.doPrivileged(new PrivilegedAction(this)
    {
      public Object run()
      {
        try
        {
          return ExtensionDependency.access$300(ExtensionDependency.access$000());
        }
        catch (IOException localIOException)
        {
          ExtensionDependency.access$100("Cannot get list of installed extensions");
          ExtensionDependency.access$200(this.this$0, localIOException);
        }
        return new URL[0];
      }
    }));
  }

  private Boolean addNewExtensionsToClassLoader(Launcher.ExtClassLoader paramExtClassLoader)
  {
    File[] arrayOfFile;
    try
    {
      arrayOfFile = getInstalledExtensions();
      for (int i = 0; i < arrayOfFile.length; ++i)
      {
        File localFile = arrayOfFile[i];
        URL localURL = (URL)AccessController.doPrivileged(new PrivilegedAction(this, localFile)
        {
          public Object run()
          {
            try
            {
              return ParseUtil.fileToEncodedURL(this.val$instFile);
            }
            catch (MalformedURLException localMalformedURLException)
            {
              ExtensionDependency.access$200(this.this$0, localMalformedURLException);
            }
            return null;
          }
        });
        if (localURL != null)
        {
          URL[] arrayOfURL = paramExtClassLoader.getURLs();
          int j = 0;
          for (int k = 0; k < arrayOfURL.length; ++k)
          {
            debug("URL[" + k + "] is " + arrayOfURL[k] + " looking for " + localURL);
            if (arrayOfURL[k].toString().compareToIgnoreCase(localURL.toString()) == 0)
            {
              j = 1;
              debug("Found !");
            }
          }
          if (j == 0)
          {
            debug("Not Found ! adding to the classloader " + localURL);
            paramExtClassLoader.addExtURL(localURL);
          }
        }
      }
    }
    catch (MalformedURLException localMalformedURLException)
    {
      localMalformedURLException.printStackTrace();
    }
    catch (IOException localIOException)
    {
      localIOException.printStackTrace();
    }
    return Boolean.TRUE;
  }

  private static void debug(String paramString)
  {
  }

  private void debugException(Throwable paramThrowable)
  {
  }
}