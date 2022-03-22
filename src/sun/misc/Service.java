package sun.misc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;

public final class Service
{
  private static final String prefix = "META-INF/services/";

  private static void fail(Class paramClass, String paramString, Throwable paramThrowable)
    throws sun.misc.ServiceConfigurationError
  {
    ServiceConfigurationError localServiceConfigurationError = new sun.misc.ServiceConfigurationError(paramClass.getName() + ": " + paramString);
    localServiceConfigurationError.initCause(paramThrowable);
    throw localServiceConfigurationError;
  }

  private static void fail(Class paramClass, String paramString)
    throws sun.misc.ServiceConfigurationError
  {
    throw new sun.misc.ServiceConfigurationError(paramClass.getName() + ": " + paramString);
  }

  private static void fail(Class paramClass, URL paramURL, int paramInt, String paramString)
    throws sun.misc.ServiceConfigurationError
  {
    fail(paramClass, paramURL + ":" + paramInt + ": " + paramString);
  }

  private static int parseLine(Class paramClass, URL paramURL, BufferedReader paramBufferedReader, int paramInt, List paramList, Set paramSet)
    throws IOException, sun.misc.ServiceConfigurationError
  {
    String str = paramBufferedReader.readLine();
    if (str == null)
      return -1;
    int i = str.indexOf(35);
    if (i >= 0)
      str = str.substring(0, i);
    str = str.trim();
    int j = str.length();
    if (j != 0)
    {
      if ((str.indexOf(32) >= 0) || (str.indexOf(9) >= 0))
        fail(paramClass, paramURL, paramInt, "Illegal configuration-file syntax");
      int k = str.codePointAt(0);
      if (!(Character.isJavaIdentifierStart(k)))
        fail(paramClass, paramURL, paramInt, "Illegal provider-class name: " + str);
      int l = Character.charCount(k);
      while (l < j)
      {
        k = str.codePointAt(l);
        if ((!(Character.isJavaIdentifierPart(k))) && (k != 46))
          fail(paramClass, paramURL, paramInt, "Illegal provider-class name: " + str);
        l += Character.charCount(k);
      }
      if (!(paramSet.contains(str)))
      {
        paramList.add(str);
        paramSet.add(str);
      }
    }
    return (paramInt + 1);
  }

  private static Iterator parse(Class paramClass, URL paramURL, Set paramSet)
    throws sun.misc.ServiceConfigurationError
  {
    InputStream localInputStream = null;
    BufferedReader localBufferedReader = null;
    ArrayList localArrayList = new ArrayList();
    try
    {
      localInputStream = paramURL.openStream();
      localBufferedReader = new BufferedReader(new InputStreamReader(localInputStream, "utf-8"));
      int i = 1;
      while ((i = parseLine(paramClass, paramURL, localBufferedReader, i, localArrayList, paramSet)) >= 0);
    }
    catch (IOException localIOException1)
    {
      fail(paramClass, ": " + localIOException1);
    }
    finally
    {
      try
      {
        if (localBufferedReader != null)
          localBufferedReader.close();
        if (localInputStream != null)
          localInputStream.close();
      }
      catch (IOException localIOException2)
      {
        fail(paramClass, ": " + localIOException2);
      }
    }
    return localArrayList.iterator();
  }

  public static Iterator providers(Class paramClass, ClassLoader paramClassLoader)
    throws sun.misc.ServiceConfigurationError
  {
    return new LazyIterator(paramClass, paramClassLoader, null);
  }

  public static Iterator providers(Class paramClass)
    throws sun.misc.ServiceConfigurationError
  {
    ClassLoader localClassLoader = Thread.currentThread().getContextClassLoader();
    return providers(paramClass, localClassLoader);
  }

  public static Iterator installedProviders(Class paramClass)
    throws sun.misc.ServiceConfigurationError
  {
    ClassLoader localClassLoader1 = ClassLoader.getSystemClassLoader();
    ClassLoader localClassLoader2 = null;
    while (localClassLoader1 != null)
    {
      localClassLoader2 = localClassLoader1;
      localClassLoader1 = localClassLoader1.getParent();
    }
    return providers(paramClass, localClassLoader2);
  }

  private static class LazyIterator
  implements Iterator
  {
    Class service;
    ClassLoader loader;
    Enumeration configs = null;
    Iterator pending = null;
    Set returned = new TreeSet();
    String nextName = null;

    private LazyIterator(Class paramClass, ClassLoader paramClassLoader)
    {
      this.service = paramClass;
      this.loader = paramClassLoader;
    }

    public boolean hasNext()
      throws sun.misc.ServiceConfigurationError
    {
      if (this.nextName != null)
        return true;
      if (this.configs == null)
        try
        {
          String str = "META-INF/services/" + this.service.getName();
          if (this.loader == null)
            this.configs = ClassLoader.getSystemResources(str);
          else
            this.configs = this.loader.getResources(str);
        }
        catch (IOException localIOException)
        {
          Service.access$000(this.service, ": " + localIOException);
        }
      while ((this.pending == null) || (!(this.pending.hasNext())))
      {
        if (!(this.configs.hasMoreElements()))
          return false;
        this.pending = Service.access$100(this.service, (URL)this.configs.nextElement(), this.returned);
      }
      this.nextName = ((String)this.pending.next());
      return true;
    }

    public Object next()
      throws sun.misc.ServiceConfigurationError
    {
      if (!(hasNext()))
        throw new NoSuchElementException();
      String str = this.nextName;
      this.nextName = null;
      try
      {
        return Class.forName(str, true, this.loader).newInstance();
      }
      catch (ClassNotFoundException localClassNotFoundException)
      {
        Service.access$000(this.service, "Provider " + str + " not found");
      }
      catch (Exception localException)
      {
        Service.access$200(this.service, "Provider " + str + " could not be instantiated: " + localException, localException);
      }
      return null;
    }

    public void remove()
    {
      throw new UnsupportedOperationException();
    }
  }
}