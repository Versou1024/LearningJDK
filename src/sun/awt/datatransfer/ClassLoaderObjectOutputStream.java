package sun.awt.datatransfer;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class ClassLoaderObjectOutputStream extends ObjectOutputStream
{
  private final Map<Set<String>, ClassLoader> map = new HashMap();

  public ClassLoaderObjectOutputStream(OutputStream paramOutputStream)
    throws IOException
  {
    super(paramOutputStream);
  }

  protected void annotateClass(Class<?> paramClass)
    throws IOException
  {
    ClassLoader localClassLoader = (ClassLoader)AccessController.doPrivileged(new PrivilegedAction(this, paramClass)
    {
      public Object run()
      {
        return this.val$cl.getClassLoader();
      }
    });
    HashSet localHashSet = new HashSet(1);
    localHashSet.add(paramClass.getName());
    this.map.put(localHashSet, localClassLoader);
  }

  protected void annotateProxyClass(Class<?> paramClass)
    throws IOException
  {
    ClassLoader localClassLoader = (ClassLoader)AccessController.doPrivileged(new PrivilegedAction(this, paramClass)
    {
      public Object run()
      {
        return this.val$cl.getClassLoader();
      }
    });
    Class[] arrayOfClass = paramClass.getInterfaces();
    HashSet localHashSet = new HashSet(arrayOfClass.length);
    for (int i = 0; i < arrayOfClass.length; ++i)
      localHashSet.add(arrayOfClass[i].getName());
    this.map.put(localHashSet, localClassLoader);
  }

  public Map<Set<String>, ClassLoader> getClassLoaderMap()
  {
    return new HashMap(this.map);
  }
}