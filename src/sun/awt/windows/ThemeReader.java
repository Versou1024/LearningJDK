package sun.awt.windows;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ThemeReader
{
  private static final HashMap<String, Long> widgetToTheme = new HashMap();
  private static final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
  private static final Lock readLock = readWriteLock.readLock();
  private static final Lock writeLock = readWriteLock.writeLock();

  static void flush()
  {
    writeLock.lock();
    try
    {
      Iterator localIterator = widgetToTheme.values().iterator();
      while (localIterator.hasNext())
      {
        Long localLong = (Long)localIterator.next();
        closeTheme(localLong.longValue());
      }
      widgetToTheme.clear();
    }
    finally
    {
      writeLock.unlock();
    }
  }

  public static native boolean isThemed();

  private static Long getThemeImpl(String paramString)
  {
    Long localLong = (Long)widgetToTheme.get(paramString);
    if (localLong == null)
    {
      int i = paramString.indexOf("::");
      if (i > 0)
      {
        setWindowTheme(paramString.substring(0, i));
        localLong = Long.valueOf(openTheme(paramString.substring(i + 2)));
        setWindowTheme(null);
      }
      else
      {
        localLong = Long.valueOf(openTheme(paramString));
      }
      widgetToTheme.put(paramString, localLong);
    }
    return localLong;
  }

  private static Long getTheme(String paramString)
  {
    Long localLong = (Long)widgetToTheme.get(paramString);
    if (localLong == null)
    {
      readLock.unlock();
      writeLock.lock();
      try
      {
        localLong = getThemeImpl(paramString);
      }
      finally
      {
        readLock.lock();
        writeLock.unlock();
      }
    }
    return localLong;
  }

  public static native void paintBackground(int[] paramArrayOfInt, long paramLong, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6, int paramInt7);

  public static void paintBackground(int[] paramArrayOfInt, String paramString, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6, int paramInt7)
  {
    readLock.lock();
    try
    {
      paintBackground(paramArrayOfInt, getTheme(paramString).longValue(), paramInt1, paramInt2, paramInt3, paramInt4, paramInt5, paramInt6, paramInt7);
    }
    finally
    {
      readLock.unlock();
    }
  }

  public static native Insets getThemeMargins(long paramLong, int paramInt1, int paramInt2, int paramInt3);

  public static Insets getThemeMargins(String paramString, int paramInt1, int paramInt2, int paramInt3)
  {
    readLock.lock();
    try
    {
      Insets localInsets = getThemeMargins(getTheme(paramString).longValue(), paramInt1, paramInt2, paramInt3);
      return localInsets;
    }
    finally
    {
      readLock.unlock();
    }
  }

  private static native boolean isThemePartDefined(long paramLong, int paramInt1, int paramInt2);

  public static boolean isThemePartDefined(String paramString, int paramInt1, int paramInt2)
  {
    readLock.lock();
    try
    {
      boolean bool = isThemePartDefined(getTheme(paramString).longValue(), paramInt1, paramInt2);
      return bool;
    }
    finally
    {
      readLock.unlock();
    }
  }

  public static native Color getColor(long paramLong, int paramInt1, int paramInt2, int paramInt3);

  public static Color getColor(String paramString, int paramInt1, int paramInt2, int paramInt3)
  {
    readLock.lock();
    try
    {
      Color localColor = getColor(getTheme(paramString).longValue(), paramInt1, paramInt2, paramInt3);
      return localColor;
    }
    finally
    {
      readLock.unlock();
    }
  }

  public static native int getInt(long paramLong, int paramInt1, int paramInt2, int paramInt3);

  public static int getInt(String paramString, int paramInt1, int paramInt2, int paramInt3)
  {
    readLock.lock();
    try
    {
      int i = getInt(getTheme(paramString).longValue(), paramInt1, paramInt2, paramInt3);
      return i;
    }
    finally
    {
      readLock.unlock();
    }
  }

  public static native int getEnum(long paramLong, int paramInt1, int paramInt2, int paramInt3);

  public static int getEnum(String paramString, int paramInt1, int paramInt2, int paramInt3)
  {
    readLock.lock();
    try
    {
      int i = getEnum(getTheme(paramString).longValue(), paramInt1, paramInt2, paramInt3);
      return i;
    }
    finally
    {
      readLock.unlock();
    }
  }

  public static native boolean getBoolean(long paramLong, int paramInt1, int paramInt2, int paramInt3);

  public static boolean getBoolean(String paramString, int paramInt1, int paramInt2, int paramInt3)
  {
    readLock.lock();
    try
    {
      boolean bool = getBoolean(getTheme(paramString).longValue(), paramInt1, paramInt2, paramInt3);
      return bool;
    }
    finally
    {
      readLock.unlock();
    }
  }

  public static native boolean getSysBoolean(long paramLong, int paramInt);

  public static boolean getSysBoolean(String paramString, int paramInt)
  {
    readLock.lock();
    try
    {
      boolean bool = getSysBoolean(getTheme(paramString).longValue(), paramInt);
      return bool;
    }
    finally
    {
      readLock.unlock();
    }
  }

  public static native Point getPoint(long paramLong, int paramInt1, int paramInt2, int paramInt3);

  public static Point getPoint(String paramString, int paramInt1, int paramInt2, int paramInt3)
  {
    readLock.lock();
    try
    {
      Point localPoint = getPoint(getTheme(paramString).longValue(), paramInt1, paramInt2, paramInt3);
      return localPoint;
    }
    finally
    {
      readLock.unlock();
    }
  }

  public static native Dimension getPosition(long paramLong, int paramInt1, int paramInt2, int paramInt3);

  public static Dimension getPosition(String paramString, int paramInt1, int paramInt2, int paramInt3)
  {
    readLock.lock();
    try
    {
      Dimension localDimension = getPosition(getTheme(paramString).longValue(), paramInt1, paramInt2, paramInt3);
      return localDimension;
    }
    finally
    {
      readLock.unlock();
    }
  }

  public static native Dimension getPartSize(long paramLong, int paramInt1, int paramInt2);

  public static Dimension getPartSize(String paramString, int paramInt1, int paramInt2)
  {
    readLock.lock();
    try
    {
      Dimension localDimension = getPartSize(getTheme(paramString).longValue(), paramInt1, paramInt2);
      return localDimension;
    }
    finally
    {
      readLock.unlock();
    }
  }

  public static native long openTheme(String paramString);

  public static native void closeTheme(long paramLong);

  public static native void setWindowTheme(String paramString);

  private static native long getThemeTransitionDuration(long paramLong, int paramInt1, int paramInt2, int paramInt3, int paramInt4);

  public static long getThemeTransitionDuration(String paramString, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    readLock.lock();
    try
    {
      long l = getThemeTransitionDuration(getTheme(paramString).longValue(), paramInt1, paramInt2, paramInt3, paramInt4);
      return l;
    }
    finally
    {
      readLock.unlock();
    }
  }

  public static native boolean isGetThemeTransitionDurationDefined();

  private static native Insets getThemeBackgroundContentMargins(long paramLong, int paramInt1, int paramInt2, int paramInt3, int paramInt4);

  public static Insets getThemeBackgroundContentMargins(String paramString, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    readLock.lock();
    try
    {
      Insets localInsets = getThemeBackgroundContentMargins(getTheme(paramString).longValue(), paramInt1, paramInt2, paramInt3, paramInt4);
      return localInsets;
    }
    finally
    {
      readLock.unlock();
    }
  }
}