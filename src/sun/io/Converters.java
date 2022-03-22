package sun.io;

import java.io.UnsupportedEncodingException;
import java.lang.ref.SoftReference;
import java.security.AccessController;
import java.util.Properties;
import sun.misc.VM;
import sun.security.action.GetPropertyAction;

@Deprecated
public class Converters
{
  private static Object lock = Converters.class;
  private static String converterPackageName = null;
  private static String defaultEncoding = null;
  public static final int BYTE_TO_CHAR = 0;
  public static final int CHAR_TO_BYTE = 1;
  private static final String[] converterPrefix = { "ByteToChar", "CharToByte" };
  private static final int CACHE_SIZE = 3;
  private static final Object DEFAULT_NAME = new Object();
  private static SoftReference[][] classCache = { new SoftReference[3], new SoftReference[3] };

  private static void moveToFront(Object[] paramArrayOfObject, int paramInt)
  {
    Object localObject = paramArrayOfObject[paramInt];
    for (int i = paramInt; i > 0; --i)
      paramArrayOfObject[i] = paramArrayOfObject[(i - 1)];
    paramArrayOfObject[0] = localObject;
  }

  private static Class cache(int paramInt, Object paramObject)
  {
    SoftReference[] arrayOfSoftReference = classCache[paramInt];
    for (int i = 0; i < 3; ++i)
    {
      SoftReference localSoftReference = arrayOfSoftReference[i];
      if (localSoftReference == null)
        break label75:
      Object[] arrayOfObject = (Object[])(Object[])localSoftReference.get();
      label75: if (arrayOfObject == null)
      {
        arrayOfSoftReference[i] = null;
      }
      else if (arrayOfObject[1].equals(paramObject))
      {
        moveToFront(arrayOfSoftReference, i);
        return ((Class)arrayOfObject[0]);
      }
    }
    return null;
  }

  private static Class cache(int paramInt, Object paramObject, Class paramClass)
  {
    SoftReference[] arrayOfSoftReference = classCache[paramInt];
    arrayOfSoftReference[2] = new SoftReference(new Object[] { paramClass, paramObject });
    moveToFront(arrayOfSoftReference, 2);
    return paramClass;
  }

  public static boolean isCached(int paramInt, String paramString)
  {
    synchronized (lock)
    {
      SoftReference[] arrayOfSoftReference = classCache[paramInt];
      for (int i = 0; i < 3; ++i)
      {
        SoftReference localSoftReference = arrayOfSoftReference[i];
        if (localSoftReference == null)
          break label76:
        Object[] arrayOfObject = (Object[])(Object[])localSoftReference.get();
        label76: if (arrayOfObject == null)
          arrayOfSoftReference[i] = null;
        else if (arrayOfObject[1].equals(paramString))
          return true;
      }
      return false;
    }
  }

  private static String getConverterPackageName()
  {
    String str = converterPackageName;
    if (str != null)
      return str;
    GetPropertyAction localGetPropertyAction = new GetPropertyAction("file.encoding.pkg");
    str = (String)AccessController.doPrivileged(localGetPropertyAction);
    if (str != null)
      converterPackageName = str;
    else
      str = "sun.io";
    return str;
  }

  public static String getDefaultEncodingName()
  {
    synchronized (lock)
    {
      if (defaultEncoding == null)
      {
        GetPropertyAction localGetPropertyAction = new GetPropertyAction("file.encoding");
        defaultEncoding = (String)AccessController.doPrivileged(localGetPropertyAction);
      }
    }
    return defaultEncoding;
  }

  public static void resetDefaultEncodingName()
  {
    if (VM.isBooted())
      return;
    synchronized (lock)
    {
      defaultEncoding = "ISO-8859-1";
      Properties localProperties = System.getProperties();
      localProperties.setProperty("file.encoding", defaultEncoding);
      System.setProperties(localProperties);
    }
  }

  private static Class getConverterClass(int paramInt, String paramString)
    throws UnsupportedEncodingException
  {
    String str = null;
    if (!(paramString.equals("ISO8859_1")))
      if (paramString.equals("8859_1"))
        str = "ISO8859_1";
      else if (paramString.equals("ISO8859-1"))
        str = "ISO8859_1";
      else if (paramString.equals("646"))
        str = "ASCII";
      else
        str = CharacterEncoding.aliasName(paramString);
    if (str == null)
      str = paramString;
    try
    {
      return Class.forName(getConverterPackageName() + "." + converterPrefix[paramInt] + str);
    }
    catch (ClassNotFoundException localClassNotFoundException)
    {
      throw new UnsupportedEncodingException(str);
    }
  }

  private static Object newConverter(String paramString, Class paramClass)
    throws UnsupportedEncodingException
  {
    try
    {
      return paramClass.newInstance();
    }
    catch (InstantiationException localInstantiationException)
    {
      throw new UnsupportedEncodingException(paramString);
    }
    catch (IllegalAccessException localIllegalAccessException)
    {
      throw new UnsupportedEncodingException(paramString);
    }
  }

  static Object newConverter(int paramInt, String paramString)
    throws UnsupportedEncodingException
  {
    Class localClass;
    synchronized (lock)
    {
      localClass = cache(paramInt, paramString);
      if (localClass == null)
      {
        localClass = getConverterClass(paramInt, paramString);
        if (!(localClass.getName().equals("sun.io.CharToByteUTF8")))
          cache(paramInt, paramString, localClass);
      }
    }
    return newConverter(paramString, localClass);
  }

  private static Class getDefaultConverterClass(int paramInt)
  {
    int i = 0;
    Class localClass = cache(paramInt, DEFAULT_NAME);
    if (localClass != null)
      return localClass;
    String str = getDefaultEncodingName();
    if (str != null)
      i = 1;
    else
      str = "ISO8859_1";
    try
    {
      localClass = getConverterClass(paramInt, str);
      if (i != 0)
        cache(paramInt, DEFAULT_NAME, localClass);
    }
    catch (UnsupportedEncodingException localUnsupportedEncodingException1)
    {
      try
      {
        localClass = getConverterClass(paramInt, "ISO8859_1");
      }
      catch (UnsupportedEncodingException localUnsupportedEncodingException2)
      {
        throw new InternalError("Cannot find default " + converterPrefix[paramInt] + " converter class");
      }
    }
    return localClass;
  }

  static Object newDefaultConverter(int paramInt)
  {
    Class localClass;
    synchronized (lock)
    {
      localClass = getDefaultConverterClass(paramInt);
    }
    try
    {
      return newConverter("", localClass);
    }
    catch (UnsupportedEncodingException localUnsupportedEncodingException)
    {
      throw new InternalError("Cannot instantiate default converter class " + localClass.getName());
    }
  }
}