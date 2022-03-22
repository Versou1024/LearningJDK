package sun.corba;

import java.io.ObjectInputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.Permission;
import java.security.PrivilegedAction;
import sun.misc.Unsafe;
import sun.reflect.ReflectionFactory;
import sun.reflect.ReflectionFactory.GetReflectionFactoryAction;

public final class Bridge
{
  private static final Class[] NO_ARGS = new Class[0];
  private static final Permission getBridgePermission = new BridgePermission("getBridge");
  private static Bridge bridge = null;
  private final Method latestUserDefinedLoaderMethod = ???.getLatestUserDefinedLoaderMethod();
  private final Unsafe unsafe = ???.getUnsafe();
  private final ReflectionFactory reflectionFactory = (ReflectionFactory)AccessController.doPrivileged(new ReflectionFactory.GetReflectionFactoryAction());
  public static final long INVALID_FIELD_OFFSET = -1L;

  private Method getLatestUserDefinedLoaderMethod()
  {
    return ((Method)AccessController.doPrivileged(new PrivilegedAction(this)
    {
      public Object run()
      {
        Method localMethod = null;
        try
        {
          ObjectInputStream localObjectInputStream = ObjectInputStream.class;
          localMethod = localObjectInputStream.getDeclaredMethod("latestUserDefinedLoader", Bridge.access$000());
          localMethod.setAccessible(true);
        }
        catch (NoSuchMethodException localNoSuchMethodException)
        {
          Error localError = new Error("java.io.ObjectInputStream latestUserDefinedLoader " + localNoSuchMethodException);
          localError.initCause(localNoSuchMethodException);
          throw localError;
        }
        return localMethod;
      }
    }));
  }

  private Unsafe getUnsafe()
  {
    Field localField = (Field)AccessController.doPrivileged(new PrivilegedAction(this)
    {
      public Object run()
      {
        Field localField = null;
        try
        {
          Unsafe localUnsafe = Unsafe.class;
          localField = localUnsafe.getDeclaredField("theUnsafe");
          localField.setAccessible(true);
          return localField;
        }
        catch (NoSuchFieldException localNoSuchFieldException)
        {
          Error localError = new Error("Could not access Unsafe");
          localError.initCause(localNoSuchFieldException);
          throw localError;
        }
      }
    });
    Unsafe localUnsafe = null;
    try
    {
      localUnsafe = (Unsafe)(Unsafe)localField.get(null);
    }
    catch (Throwable localThrowable)
    {
      Error localError = new Error("Could not access Unsafe");
      localError.initCause(localThrowable);
      throw localError;
    }
    return localUnsafe;
  }

  public static final synchronized Bridge get()
  {
    SecurityManager localSecurityManager = System.getSecurityManager();
    if (localSecurityManager != null)
      localSecurityManager.checkPermission(getBridgePermission);
    if (bridge == null)
      bridge = new Bridge();
    return bridge;
  }

  public final ClassLoader getLatestUserDefinedLoader()
  {
    Error localError;
    try
    {
      return ((ClassLoader)this.latestUserDefinedLoaderMethod.invoke(null, NO_ARGS));
    }
    catch (InvocationTargetException localInvocationTargetException)
    {
      localError = new Error("sun.corba.Bridge.latestUserDefinedLoader: " + localInvocationTargetException);
      localError.initCause(localInvocationTargetException);
      throw localError;
    }
    catch (IllegalAccessException localIllegalAccessException)
    {
      localError = new Error("sun.corba.Bridge.latestUserDefinedLoader: " + localIllegalAccessException);
      localError.initCause(localIllegalAccessException);
      throw localError;
    }
  }

  public final int getInt(Object paramObject, long paramLong)
  {
    return this.unsafe.getInt(paramObject, paramLong);
  }

  public final void putInt(Object paramObject, long paramLong, int paramInt)
  {
    this.unsafe.putInt(paramObject, paramLong, paramInt);
  }

  public final Object getObject(Object paramObject, long paramLong)
  {
    return this.unsafe.getObject(paramObject, paramLong);
  }

  public final void putObject(Object paramObject1, long paramLong, Object paramObject2)
  {
    this.unsafe.putObject(paramObject1, paramLong, paramObject2);
  }

  public final boolean getBoolean(Object paramObject, long paramLong)
  {
    return this.unsafe.getBoolean(paramObject, paramLong);
  }

  public final void putBoolean(Object paramObject, long paramLong, boolean paramBoolean)
  {
    this.unsafe.putBoolean(paramObject, paramLong, paramBoolean);
  }

  public final byte getByte(Object paramObject, long paramLong)
  {
    return this.unsafe.getByte(paramObject, paramLong);
  }

  public final void putByte(Object paramObject, long paramLong, byte paramByte)
  {
    this.unsafe.putByte(paramObject, paramLong, paramByte);
  }

  public final short getShort(Object paramObject, long paramLong)
  {
    return this.unsafe.getShort(paramObject, paramLong);
  }

  public final void putShort(Object paramObject, long paramLong, short paramShort)
  {
    this.unsafe.putShort(paramObject, paramLong, paramShort);
  }

  public final char getChar(Object paramObject, long paramLong)
  {
    return this.unsafe.getChar(paramObject, paramLong);
  }

  public final void putChar(Object paramObject, long paramLong, char paramChar)
  {
    this.unsafe.putChar(paramObject, paramLong, paramChar);
  }

  public final long getLong(Object paramObject, long paramLong)
  {
    return this.unsafe.getLong(paramObject, paramLong);
  }

  public final void putLong(Object paramObject, long paramLong1, long paramLong2)
  {
    this.unsafe.putLong(paramObject, paramLong1, paramLong2);
  }

  public final float getFloat(Object paramObject, long paramLong)
  {
    return this.unsafe.getFloat(paramObject, paramLong);
  }

  public final void putFloat(Object paramObject, long paramLong, float paramFloat)
  {
    this.unsafe.putFloat(paramObject, paramLong, paramFloat);
  }

  public final double getDouble(Object paramObject, long paramLong)
  {
    return this.unsafe.getDouble(paramObject, paramLong);
  }

  public final void putDouble(Object paramObject, long paramLong, double paramDouble)
  {
    this.unsafe.putDouble(paramObject, paramLong, paramDouble);
  }

  public final long objectFieldOffset(Field paramField)
  {
    return this.unsafe.objectFieldOffset(paramField);
  }

  public final void throwException(Throwable paramThrowable)
  {
    this.unsafe.throwException(paramThrowable);
  }

  public final Constructor newConstructorForSerialization(Class paramClass, Constructor paramConstructor)
  {
    return this.reflectionFactory.newConstructorForSerialization(paramClass, paramConstructor);
  }
}