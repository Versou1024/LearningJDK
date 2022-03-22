package sun.java2d.windows;

import java.io.PrintStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import sun.awt.windows.WToolkit;
import sun.java2d.opengl.WGLGraphicsConfig;

public class WindowsFlags
{
  private static boolean gdiBlitEnabled;
  private static boolean d3dEnabled;
  private static boolean d3dVerbose;
  private static boolean d3dSet;
  private static boolean d3dOnScreenEnabled;
  private static boolean oglEnabled;
  private static boolean oglVerbose;
  private static boolean offscreenSharingEnabled;
  private static boolean accelReset;
  private static boolean checkRegistry;
  private static boolean disableRegistry;
  private static boolean magPresent;
  private static boolean setHighDPIAware;
  private static String javaVersion;

  private static native boolean initNativeFlags();

  public static void initFlags()
  {
  }

  private static boolean getBooleanProp(String paramString, boolean paramBoolean)
  {
    String str = System.getProperty(paramString);
    boolean bool = paramBoolean;
    if (str != null)
      if ((str.equals("true")) || (str.equals("t")) || (str.equals("True")) || (str.equals("T")) || (str.equals("")))
        bool = true;
      else if ((str.equals("false")) || (str.equals("f")) || (str.equals("False")) || (str.equals("F")))
        bool = false;
    return bool;
  }

  private static boolean isBooleanPropTrueVerbose(String paramString)
  {
    String str = System.getProperty(paramString);
    return ((str != null) && (((str.equals("True")) || (str.equals("T")))));
  }

  private static int getIntProp(String paramString, int paramInt)
  {
    String str = System.getProperty(paramString);
    int i = paramInt;
    if (str != null)
      try
      {
        i = Integer.parseInt(str);
      }
      catch (NumberFormatException localNumberFormatException)
      {
      }
    return i;
  }

  private static boolean getPropertySet(String paramString)
  {
    String str = System.getProperty(paramString);
    return (str != null);
  }

  private static void initJavaFlags()
  {
    AccessController.doPrivileged(new PrivilegedAction()
    {
      public Object run()
      {
        WindowsFlags.access$002(WindowsFlags.access$100("javax.accessibility.screen_magnifier_present", false));
        boolean bool1 = !(WindowsFlags.access$100("sun.java2d.noddraw", WindowsFlags.access$000()));
        boolean bool2 = WindowsFlags.access$100("sun.java2d.ddoffscreen", bool1);
        WindowsFlags.access$202(WindowsFlags.access$100("sun.java2d.d3d", (bool1) && (bool2)));
        WindowsFlags.access$302(WindowsFlags.access$100("sun.java2d.d3d.onscreen", WindowsFlags.access$200()));
        WindowsFlags.access$402(WindowsFlags.access$100("sun.java2d.opengl", false));
        if (WindowsFlags.access$400())
        {
          WindowsFlags.access$502(WindowsFlags.access$600("sun.java2d.opengl"));
          if (WGLGraphicsConfig.isWGLAvailable())
          {
            WindowsFlags.access$202(false);
          }
          else
          {
            if (WindowsFlags.access$500())
              System.out.println("Could not enable OpenGL pipeline (WGL not available)");
            WindowsFlags.access$402(false);
          }
        }
        WindowsFlags.access$702(WindowsFlags.access$100("sun.java2d.gdiBlit", true));
        WindowsFlags.access$802(WindowsFlags.access$900("sun.java2d.d3d"));
        if (WindowsFlags.access$800())
          WindowsFlags.access$1002(WindowsFlags.access$600("sun.java2d.d3d"));
        WindowsFlags.access$1102(WindowsFlags.access$100("sun.java2d.offscreenSharing", false));
        WindowsFlags.access$1202(WindowsFlags.access$100("sun.java2d.accelReset", false));
        WindowsFlags.access$1302(WindowsFlags.access$100("sun.java2d.checkRegistry", false));
        WindowsFlags.access$1402(WindowsFlags.access$100("sun.java2d.disableRegistry", false));
        WindowsFlags.access$1502(System.getProperty("java.version"));
        if (WindowsFlags.access$1500() == null)
        {
          WindowsFlags.access$1502("default");
        }
        else
        {
          int i = WindowsFlags.access$1500().indexOf(45);
          if (i >= 0)
            WindowsFlags.access$1502(WindowsFlags.access$1500().substring(0, i));
        }
        String str1 = System.getProperty("sun.java2d.dpiaware");
        if (str1 != null)
        {
          WindowsFlags.access$1602(str1.equalsIgnoreCase("true"));
        }
        else
        {
          String str2 = System.getProperty("sun.java.launcher", "unknown");
          WindowsFlags.access$1602(str2.equalsIgnoreCase("SUN_STANDARD"));
        }
        return null;
      }
    });
  }

  public static boolean isD3DEnabled()
  {
    return d3dEnabled;
  }

  public static boolean isD3DSet()
  {
    return d3dSet;
  }

  public static boolean isD3DOnScreenEnabled()
  {
    return d3dOnScreenEnabled;
  }

  public static boolean isD3DVerbose()
  {
    return d3dVerbose;
  }

  public static boolean isGdiBlitEnabled()
  {
    return gdiBlitEnabled;
  }

  public static boolean isTranslucentAccelerationEnabled()
  {
    return d3dEnabled;
  }

  public static boolean isOffscreenSharingEnabled()
  {
    return offscreenSharingEnabled;
  }

  public static boolean isMagPresent()
  {
    return magPresent;
  }

  public static boolean isOGLEnabled()
  {
    return oglEnabled;
  }

  public static boolean isOGLVerbose()
  {
    return oglVerbose;
  }

  static
  {
    WToolkit.loadLibraries();
    initJavaFlags();
    initNativeFlags();
  }
}