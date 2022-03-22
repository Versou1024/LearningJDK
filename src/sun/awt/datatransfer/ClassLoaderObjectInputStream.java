package sun.awt.datatransfer;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class ClassLoaderObjectInputStream extends ObjectInputStream
{
  private final Map<Set<String>, ClassLoader> map;

  public ClassLoaderObjectInputStream(InputStream paramInputStream, Map<Set<String>, ClassLoader> paramMap)
    throws IOException
  {
    super(paramInputStream);
    if (paramMap == null)
      throw new NullPointerException("Null map");
    this.map = paramMap;
  }

  protected Class<?> resolveClass(ObjectStreamClass paramObjectStreamClass)
    throws IOException, ClassNotFoundException
  {
    String str = paramObjectStreamClass.getName();
    HashSet localHashSet = new HashSet(1);
    localHashSet.add(str);
    ClassLoader localClassLoader = (ClassLoader)this.map.get(localHashSet);
    return Class.forName(str, false, localClassLoader);
  }

  protected Class<?> resolveProxyClass(String[] paramArrayOfString)
    throws IOException, ClassNotFoundException
  {
    HashSet localHashSet = new HashSet(paramArrayOfString.length);
    for (int i = 0; i < paramArrayOfString.length; ++i)
      localHashSet.add(paramArrayOfString[i]);
    ClassLoader localClassLoader1 = (ClassLoader)this.map.get(localHashSet);
    ClassLoader localClassLoader2 = null;
    int j = 0;
    Class[] arrayOfClass = new Class[paramArrayOfString.length];
    for (int k = 0; k < paramArrayOfString.length; ++k)
    {
      Class localClass = Class.forName(paramArrayOfString[k], false, localClassLoader1);
      if ((localClass.getModifiers() & 0x1) == 0)
      {
        if (j != 0)
        {
          if (localClassLoader2 == localClass.getClassLoader())
            break label127;
          throw new IllegalAccessError("conflicting non-public interface class loaders");
        }
        localClassLoader2 = localClass.getClassLoader();
        j = 1;
      }
      label127: arrayOfClass[k] = localClass;
    }
    try
    {
      return Proxy.getProxyClass((j != 0) ? localClassLoader2 : localClassLoader1, arrayOfClass);
    }
    catch (IllegalArgumentException localIllegalArgumentException)
    {
      throw new ClassNotFoundException(null, localIllegalArgumentException);
    }
  }
}