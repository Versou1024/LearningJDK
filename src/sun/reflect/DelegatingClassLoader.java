package sun.reflect;

class DelegatingClassLoader extends ClassLoader
{
  DelegatingClassLoader(ClassLoader paramClassLoader)
  {
    super(paramClassLoader);
  }
}