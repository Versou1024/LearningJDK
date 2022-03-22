package sun.applet;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.io.StreamCorruptedException;
import java.lang.reflect.Array;

class AppletObjectInputStream extends ObjectInputStream
{
  private AppletClassLoader loader;

  public AppletObjectInputStream(InputStream paramInputStream, AppletClassLoader paramAppletClassLoader)
    throws IOException, StreamCorruptedException
  {
    super(paramInputStream);
    if (paramAppletClassLoader == null)
      throw new AppletIllegalArgumentException("appletillegalargumentexception.objectinputstream");
    this.loader = paramAppletClassLoader;
  }

  private Class primitiveType(char paramChar)
  {
    switch (paramChar)
    {
    case 'B':
      return Byte.TYPE;
    case 'C':
      return Character.TYPE;
    case 'D':
      return Double.TYPE;
    case 'F':
      return Float.TYPE;
    case 'I':
      return Integer.TYPE;
    case 'J':
      return Long.TYPE;
    case 'S':
      return Short.TYPE;
    case 'Z':
      return Boolean.TYPE;
    case 'E':
    case 'G':
    case 'H':
    case 'K':
    case 'L':
    case 'M':
    case 'N':
    case 'O':
    case 'P':
    case 'Q':
    case 'R':
    case 'T':
    case 'U':
    case 'V':
    case 'W':
    case 'X':
    case 'Y':
    }
    return null;
  }

  protected Class resolveClass(ObjectStreamClass paramObjectStreamClass)
    throws IOException, ClassNotFoundException
  {
    String str = paramObjectStreamClass.getName();
    if (str.startsWith("["))
    {
      Class localClass;
      for (int i = 1; str.charAt(i) == '['; ++i);
      if (str.charAt(i) == 'L')
      {
        localClass = this.loader.loadClass(str.substring(i + 1, str.length() - 1));
      }
      else
      {
        if (str.length() != i + 1)
          throw new ClassNotFoundException(str);
        localClass = primitiveType(str.charAt(i));
      }
      int[] arrayOfInt = new int[i];
      for (int j = 0; j < i; ++j)
        arrayOfInt[j] = 0;
      return Array.newInstance(localClass, arrayOfInt).getClass();
    }
    return this.loader.loadClass(str);
  }
}