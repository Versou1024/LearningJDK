package sun.org.mozilla.javascript.internal;

import B;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Method;
import java.util.Hashtable;

public class Kit
{
  private static Method Throwable_initCause = null;

  public static Class classOrNull(String paramString)
  {
    try
    {
      return Class.forName(paramString);
    }
    catch (ClassNotFoundException localClassNotFoundException)
    {
    }
    catch (SecurityException localSecurityException)
    {
    }
    catch (LinkageError localLinkageError)
    {
    }
    catch (IllegalArgumentException localIllegalArgumentException)
    {
    }
    return null;
  }

  public static Class classOrNull(ClassLoader paramClassLoader, String paramString)
  {
    try
    {
      return paramClassLoader.loadClass(paramString);
    }
    catch (ClassNotFoundException localClassNotFoundException)
    {
    }
    catch (SecurityException localSecurityException)
    {
    }
    catch (LinkageError localLinkageError)
    {
    }
    catch (IllegalArgumentException localIllegalArgumentException)
    {
    }
    return null;
  }

  static Object newInstanceOrNull(Class paramClass)
  {
    try
    {
      return paramClass.newInstance();
    }
    catch (SecurityException localSecurityException)
    {
    }
    catch (LinkageError localLinkageError)
    {
    }
    catch (InstantiationException localInstantiationException)
    {
    }
    catch (IllegalAccessException localIllegalAccessException)
    {
    }
    return null;
  }

  static boolean testIfCanLoadRhinoClasses(ClassLoader paramClassLoader)
  {
    Class localClass1 = ScriptRuntime.ContextFactoryClass;
    Class localClass2 = classOrNull(paramClassLoader, localClass1.getName());
    return (localClass2 == localClass1);
  }

  public static RuntimeException initCause(RuntimeException paramRuntimeException, Throwable paramThrowable)
  {
    if (Throwable_initCause != null)
    {
      Object[] arrayOfObject = { paramThrowable };
      try
      {
        Throwable_initCause.invoke(paramRuntimeException, arrayOfObject);
      }
      catch (Exception localException)
      {
      }
    }
    return paramRuntimeException;
  }

  public static String[] semicolonSplit(String paramString)
  {
    String[] arrayOfString = null;
    while (true)
    {
      int i = 0;
      int j = 0;
      while (true)
      {
        int k = paramString.indexOf(59, j);
        if (k < 0)
          break;
        if (arrayOfString != null)
          arrayOfString[i] = paramString.substring(j, k);
        ++i;
        j = k + 1;
      }
      if (arrayOfString != null)
        break;
      if (j != paramString.length())
        throw new IllegalArgumentException();
      arrayOfString = new String[i];
    }
    return arrayOfString;
  }

  public static int xDigitToInt(int paramInt1, int paramInt2)
  {
    if (paramInt1 <= 57)
      if (0 <= (paramInt1 -= 48));
    else if (paramInt1 <= 70)
      if (65 <= paramInt1)
        paramInt1 -= 55;
    else if ((paramInt1 <= 102) && (97 <= paramInt1))
      paramInt1 -= 87;
    else
      return -1;
    return (paramInt2 << 4 | paramInt1);
  }

  public static Object addListener(Object paramObject1, Object paramObject2)
  {
    if (paramObject2 == null)
      throw new IllegalArgumentException();
    if (paramObject2 instanceof Object[])
      throw new IllegalArgumentException();
    if (paramObject1 == null)
    {
      paramObject1 = paramObject2;
    }
    else if (!(paramObject1 instanceof Object[]))
    {
      paramObject1 = { paramObject1, paramObject2 };
    }
    else
    {
      Object[] arrayOfObject1 = (Object[])(Object[])paramObject1;
      int i = arrayOfObject1.length;
      if (i < 2)
        throw new IllegalArgumentException();
      Object[] arrayOfObject2 = new Object[i + 1];
      System.arraycopy(arrayOfObject1, 0, arrayOfObject2, 0, i);
      arrayOfObject2[i] = paramObject2;
      paramObject1 = arrayOfObject2;
    }
    return paramObject1;
  }

  public static Object removeListener(Object paramObject1, Object paramObject2)
  {
    if (paramObject2 == null)
      throw new IllegalArgumentException();
    if (paramObject2 instanceof Object[])
      throw new IllegalArgumentException();
    if (paramObject1 == paramObject2)
    {
      paramObject1 = null;
    }
    else if (paramObject1 instanceof Object[])
    {
      Object[] arrayOfObject1 = (Object[])(Object[])paramObject1;
      int i = arrayOfObject1.length;
      if (i < 2)
        throw new IllegalArgumentException();
      if (i == 2)
      {
        if (arrayOfObject1[1] == paramObject2)
          paramObject1 = arrayOfObject1[0];
        else if (arrayOfObject1[0] == paramObject2)
          paramObject1 = arrayOfObject1[1];
      }
      else
      {
        int j = i;
        do
          if (arrayOfObject1[(--j)] == paramObject2)
          {
            Object[] arrayOfObject2 = new Object[i - 1];
            System.arraycopy(arrayOfObject1, 0, arrayOfObject2, 0, j);
            System.arraycopy(arrayOfObject1, j + 1, arrayOfObject2, j, i - j + 1);
            paramObject1 = arrayOfObject2;
            break;
          }
        while (j != 0);
      }
    }
    return paramObject1;
  }

  public static Object getListener(Object paramObject, int paramInt)
  {
    if (paramInt == 0)
    {
      if (paramObject == null)
        return null;
      if (!(paramObject instanceof Object[]))
        return paramObject;
      arrayOfObject = (Object[])(Object[])paramObject;
      if (arrayOfObject.length < 2)
        throw new IllegalArgumentException();
      return arrayOfObject[0];
    }
    if (paramInt == 1)
    {
      if (!(paramObject instanceof Object[]))
      {
        if (paramObject == null)
          throw new IllegalArgumentException();
        return null;
      }
      arrayOfObject = (Object[])(Object[])paramObject;
      return arrayOfObject[1];
    }
    Object[] arrayOfObject = (Object[])(Object[])paramObject;
    int i = arrayOfObject.length;
    if (i < 2)
      throw new IllegalArgumentException();
    if (paramInt == i)
      return null;
    return arrayOfObject[paramInt];
  }

  static Object initHash(Hashtable paramHashtable, Object paramObject1, Object paramObject2)
  {
    synchronized (paramHashtable)
    {
      Object localObject1 = paramHashtable.get(paramObject1);
      if (localObject1 == null)
        paramHashtable.put(paramObject1, paramObject2);
      else
        paramObject2 = localObject1;
    }
    return paramObject2;
  }

  public static Object makeHashKeyFromPair(Object paramObject1, Object paramObject2)
  {
    if (paramObject1 == null)
      throw new IllegalArgumentException();
    if (paramObject2 == null)
      throw new IllegalArgumentException();
    return new ComplexKey(paramObject1, paramObject2);
  }

  public static String readReader(Reader paramReader)
    throws IOException
  {
    Object localObject = new char[512];
    int i = 0;
    while (true)
    {
      int j = paramReader.read(localObject, i, localObject.length - i);
      if (j < 0)
        break;
      i += j;
      if (i == localObject.length)
      {
        char[] arrayOfChar = new char[localObject.length * 2];
        System.arraycopy(localObject, 0, arrayOfChar, 0, i);
        localObject = arrayOfChar;
      }
    }
    return ((String)new String(localObject, 0, i));
  }

  public static byte[] readStream(InputStream paramInputStream, int paramInt)
    throws IOException
  {
    if (paramInt <= 0)
      throw new IllegalArgumentException("Bad initialBufferCapacity: " + paramInt);
    Object localObject = new byte[paramInt];
    int i = 0;
    while (true)
    {
      int j = paramInputStream.read(localObject, i, localObject.length - i);
      if (j < 0)
        break;
      i += j;
      if (i == localObject.length)
      {
        byte[] arrayOfByte2 = new byte[localObject.length * 2];
        System.arraycopy(localObject, 0, arrayOfByte2, 0, i);
        localObject = arrayOfByte2;
      }
    }
    if (i != localObject.length)
    {
      byte[] arrayOfByte1 = new byte[i];
      System.arraycopy(localObject, 0, arrayOfByte1, 0, i);
      localObject = arrayOfByte1;
    }
    return ((B)localObject);
  }

  public static RuntimeException codeBug()
    throws RuntimeException
  {
    IllegalStateException localIllegalStateException = new IllegalStateException("FAILED ASSERTION");
    localIllegalStateException.printStackTrace(System.err);
    throw localIllegalStateException;
  }

  static
  {
    try
    {
      Class localClass = classOrNull("java.lang.Throwable");
      Class[] arrayOfClass = { localClass };
      Throwable_initCause = localClass.getMethod("initCause", arrayOfClass);
    }
    catch (Exception localException)
    {
    }
  }

  private static final class ComplexKey
  {
    private Object key1;
    private Object key2;
    private int hash;

    ComplexKey(Object paramObject1, Object paramObject2)
    {
      this.key1 = paramObject1;
      this.key2 = paramObject2;
    }

    public boolean equals(Object paramObject)
    {
      if (!(paramObject instanceof ComplexKey))
        return false;
      ComplexKey localComplexKey = (ComplexKey)paramObject;
      return ((this.key1.equals(localComplexKey.key1)) && (this.key2.equals(localComplexKey.key2)));
    }

    public int hashCode()
    {
      if (this.hash == 0)
        this.hash = (this.key1.hashCode() ^ this.key2.hashCode());
      return this.hash;
    }
  }
}