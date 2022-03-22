package sun.nio.cs;

import java.nio.charset.Charset;
import java.nio.charset.spi.CharsetProvider;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class FastCharsetProvider extends CharsetProvider
{
  private Map<String, String> classMap;
  private Map<String, String> aliasMap;
  private Map<String, Charset> cache;
  private String packagePrefix;

  protected FastCharsetProvider(String paramString, Map<String, String> paramMap1, Map<String, String> paramMap2, Map<String, Charset> paramMap)
  {
    this.packagePrefix = paramString;
    this.aliasMap = paramMap1;
    this.classMap = paramMap2;
    this.cache = paramMap;
  }

  private String canonicalize(String paramString)
  {
    String str = (String)this.aliasMap.get(paramString);
    return ((str != null) ? str : paramString);
  }

  private static String toLower(String paramString)
  {
    int i = paramString.length();
    int j = 1;
    for (int k = 0; k < i; ++k)
    {
      l = paramString.charAt(k);
      if ((l - 65 | 90 - l) >= 0)
      {
        j = 0;
        break;
      }
    }
    if (j != 0)
      return paramString;
    char[] arrayOfChar = new char[i];
    for (int l = 0; l < i; ++l)
    {
      int i1 = paramString.charAt(l);
      if ((i1 - 65 | 90 - i1) >= 0)
        arrayOfChar[l] = (char)(i1 + 32);
      else
        arrayOfChar[l] = (char)i1;
    }
    return new String(arrayOfChar);
  }

  private Charset lookup(String paramString)
  {
    String str1 = canonicalize(toLower(paramString));
    Object localObject = (Charset)this.cache.get(str1);
    if (localObject != null)
      return localObject;
    String str2 = (String)this.classMap.get(str1);
    if (str2 == null)
      return null;
    if (str2.equals("US_ASCII"))
    {
      localObject = new US_ASCII();
      this.cache.put(str1, localObject);
      return localObject;
    }
    try
    {
      Class localClass = Class.forName(this.packagePrefix + "." + str2, true, getClass().getClassLoader());
      localObject = (Charset)localClass.newInstance();
      this.cache.put(str1, localObject);
      return localObject;
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
      return lookup(canonicalize(paramString));
    }
  }

  public final Iterator<Charset> charsets()
  {
    return new Iterator(this)
    {
      Iterator<String> i = FastCharsetProvider.access$000(this.this$0).keySet().iterator();

      public boolean hasNext()
      {
        return this.i.hasNext();
      }

      public Charset next()
      {
        String str = (String)this.i.next();
        return FastCharsetProvider.access$100(this.this$0, str);
      }

      public void remove()
      {
        throw new UnsupportedOperationException();
      }
    };
  }
}