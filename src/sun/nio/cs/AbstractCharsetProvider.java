package sun.nio.cs;

import java.lang.ref.SoftReference;
import java.nio.charset.Charset;
import java.nio.charset.spi.CharsetProvider;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import sun.misc.ASCIICaseInsensitiveComparator;

public class AbstractCharsetProvider extends CharsetProvider
{
  private Map classMap = new TreeMap(ASCIICaseInsensitiveComparator.CASE_INSENSITIVE_ORDER);
  private Map aliasMap = new TreeMap(ASCIICaseInsensitiveComparator.CASE_INSENSITIVE_ORDER);
  private Map aliasNameMap = new TreeMap(ASCIICaseInsensitiveComparator.CASE_INSENSITIVE_ORDER);
  private Map cache = new TreeMap(ASCIICaseInsensitiveComparator.CASE_INSENSITIVE_ORDER);
  private String packagePrefix;

  protected AbstractCharsetProvider()
  {
    this.packagePrefix = "sun.nio.cs";
  }

  protected AbstractCharsetProvider(String paramString)
  {
    this.packagePrefix = paramString;
  }

  private static void put(Map paramMap, String paramString, Object paramObject)
  {
    if (!(paramMap.containsKey(paramString)))
      paramMap.put(paramString, paramObject);
  }

  private static void remove(Map paramMap, String paramString)
  {
    Object localObject = paramMap.remove(paramString);
    if ((!($assertionsDisabled)) && (localObject == null))
      throw new AssertionError();
  }

  protected void charset(String paramString1, String paramString2, String[] paramArrayOfString)
  {
    synchronized (this)
    {
      put(this.classMap, paramString1, paramString2);
      for (int i = 0; i < paramArrayOfString.length; ++i)
        put(this.aliasMap, paramArrayOfString[i], paramString1);
      put(this.aliasNameMap, paramString1, paramArrayOfString);
      this.cache.clear();
    }
  }

  protected void deleteCharset(String paramString, String[] paramArrayOfString)
  {
    synchronized (this)
    {
      remove(this.classMap, paramString);
      for (int i = 0; i < paramArrayOfString.length; ++i)
        remove(this.aliasMap, paramArrayOfString[i]);
      remove(this.aliasNameMap, paramString);
      this.cache.clear();
    }
  }

  protected void init()
  {
  }

  private String canonicalize(String paramString)
  {
    String str = (String)this.aliasMap.get(paramString);
    return ((str != null) ? str : paramString);
  }

  private Charset lookup(String paramString)
  {
    SoftReference localSoftReference = (SoftReference)this.cache.get(paramString);
    if (localSoftReference != null)
    {
      localObject = (Charset)localSoftReference.get();
      if (localObject != null)
        return localObject;
    }
    Object localObject = (String)this.classMap.get(paramString);
    if (localObject == null)
      return null;
    try
    {
      Class localClass = Class.forName(this.packagePrefix + "." + ((String)localObject), true, getClass().getClassLoader());
      Charset localCharset = (Charset)localClass.newInstance();
      this.cache.put(paramString, new SoftReference(localCharset));
      return localCharset;
    }
    catch (ClassNotFoundException localClassNotFoundException)
    {
      return null;
    }
    catch (IllegalAccessException localIllegalAccessException)
    {
      return null;
    }
    catch (InstantiationException localInstantiationException)
    {
    }
    return ((Charset)null);
  }

  public final Charset charsetForName(String paramString)
  {
    synchronized (this)
    {
      init();
      return lookup(canonicalize(paramString));
    }
  }

  public final Iterator<Charset> charsets()
  {
    ArrayList localArrayList;
    synchronized (this)
    {
      init();
      localArrayList = new ArrayList(this.classMap.keySet());
    }
    return new Iterator(this, localArrayList)
    {
      Iterator i = this.val$ks.iterator();

      public boolean hasNext()
      {
        return this.i.hasNext();
      }

      public Charset next()
      {
        String str = (String)this.i.next();
        return AbstractCharsetProvider.access$000(this.this$0, str);
      }

      public void remove()
      {
        throw new UnsupportedOperationException();
      }
    };
  }

  public final String[] aliases(String paramString)
  {
    synchronized (this)
    {
      init();
      return ((String[])(String[])this.aliasNameMap.get(paramString));
    }
  }
}