package sun.org.mozilla.javascript.internal;

public class DefiningClassLoader extends ClassLoader
  implements GeneratedClassLoader
{
  private ClassLoader parentLoader;

  public DefiningClassLoader()
  {
    this.parentLoader = getClass().getClassLoader();
  }

  public DefiningClassLoader(ClassLoader paramClassLoader)
  {
    this.parentLoader = paramClassLoader;
  }

  public Class defineClass(String paramString, byte[] paramArrayOfByte)
  {
    return super.defineClass(paramString, paramArrayOfByte, 0, paramArrayOfByte.length);
  }

  public void linkClass(Class paramClass)
  {
    resolveClass(paramClass);
  }

  public Class loadClass(String paramString, boolean paramBoolean)
    throws ClassNotFoundException
  {
    Class localClass = findLoadedClass(paramString);
    if (localClass == null)
      if (this.parentLoader != null)
        localClass = this.parentLoader.loadClass(paramString);
      else
        localClass = findSystemClass(paramString);
    if (paramBoolean)
      resolveClass(localClass);
    return localClass;
  }
}