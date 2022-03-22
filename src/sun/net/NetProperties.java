package sun.net;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Properties;

public class NetProperties
{
  private static Properties props = new Properties();

  private static void loadDefaultProperties()
  {
    String str = System.getProperty("java.home");
    if (str == null)
      throw new Error("Can't find java.home ??");
    try
    {
      File localFile = new File(str, "lib");
      localFile = new File(localFile, "net.properties");
      str = localFile.getCanonicalPath();
      FileInputStream localFileInputStream = new FileInputStream(str);
      BufferedInputStream localBufferedInputStream = new BufferedInputStream(localFileInputStream);
      props.load(localBufferedInputStream);
      localBufferedInputStream.close();
    }
    catch (Exception localException)
    {
    }
  }

  public static String get(String paramString)
  {
    String str = props.getProperty(paramString);
    try
    {
      return System.getProperty(paramString, str);
    }
    catch (IllegalArgumentException localIllegalArgumentException)
    {
    }
    catch (NullPointerException localNullPointerException)
    {
    }
    return null;
  }

  public static Integer getInteger(String paramString, int paramInt)
  {
    String str = null;
    try
    {
      str = System.getProperty(paramString, props.getProperty(paramString));
    }
    catch (IllegalArgumentException localIllegalArgumentException)
    {
    }
    catch (NullPointerException localNullPointerException)
    {
    }
    if (str != null)
      try
      {
        return Integer.decode(str);
      }
      catch (NumberFormatException localNumberFormatException)
      {
      }
    return new Integer(paramInt);
  }

  public static Boolean getBoolean(String paramString)
  {
    String str = null;
    try
    {
      str = System.getProperty(paramString, props.getProperty(paramString));
    }
    catch (IllegalArgumentException localIllegalArgumentException)
    {
    }
    catch (NullPointerException localNullPointerException)
    {
    }
    if (str != null)
      try
      {
        return Boolean.valueOf(str);
      }
      catch (NumberFormatException localNumberFormatException)
      {
      }
    return null;
  }

  static
  {
    AccessController.doPrivileged(new PrivilegedAction()
    {
      public Object run()
      {
        NetProperties.access$000();
        return null;
      }
    });
  }
}