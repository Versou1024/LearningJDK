package sun.awt;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.peer.ComponentPeer;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.PrintStream;
import java.lang.ref.WeakReference;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import sun.awt.windows.WFontConfiguration;
import sun.awt.windows.WPrinterJob;
import sun.awt.windows.WToolkit;
import sun.font.FontManager;
import sun.java2d.SunGraphicsEnvironment;
import sun.java2d.SunGraphicsEnvironment.T1Filter;
import sun.java2d.SunGraphicsEnvironment.TTFilter;
import sun.java2d.d3d.D3DGraphicsDevice;
import sun.java2d.windows.WindowsFlags;

public class Win32GraphicsEnvironment extends SunGraphicsEnvironment
{
  private static boolean displayInitialized;
  private ArrayList<WeakReference<Win32GraphicsDevice>> oldDevices;
  static String fontsForPrinting;
  private static volatile boolean isDWMCompositionEnabled;

  public static void init()
  {
  }

  private static native void initDisplay();

  public static void initDisplayWrapper()
  {
    if (!(displayInitialized))
    {
      displayInitialized = true;
      initDisplay();
    }
  }

  protected native int getNumScreens();

  protected native int getDefaultScreen();

  public GraphicsDevice getDefaultScreenDevice()
  {
    return getScreenDevices()[getDefaultScreen()];
  }

  public native int getXResolution();

  public native int getYResolution();

  public void displayChanged()
  {
    GraphicsDevice[] arrayOfGraphicsDevice1 = new GraphicsDevice[getNumScreens()];
    GraphicsDevice[] arrayOfGraphicsDevice2 = this.screens;
    if (arrayOfGraphicsDevice2 != null)
    {
      for (i = 0; i < arrayOfGraphicsDevice2.length; ++i)
      {
        if (!(this.screens[i] instanceof Win32GraphicsDevice))
        {
          if ($assertionsDisabled)
            break label119;
          throw new AssertionError(arrayOfGraphicsDevice2[i]);
        }
        Win32GraphicsDevice localWin32GraphicsDevice1 = (Win32GraphicsDevice)arrayOfGraphicsDevice2[i];
        if (!(localWin32GraphicsDevice1.isValid()))
        {
          if (this.oldDevices == null)
            this.oldDevices = new ArrayList();
          label119: this.oldDevices.add(new WeakReference(localWin32GraphicsDevice1));
        }
        else if (i < arrayOfGraphicsDevice1.length)
        {
          arrayOfGraphicsDevice1[i] = localWin32GraphicsDevice1;
        }
      }
      arrayOfGraphicsDevice2 = null;
    }
    for (int i = 0; i < arrayOfGraphicsDevice1.length; ++i)
      if (arrayOfGraphicsDevice1[i] == null)
        arrayOfGraphicsDevice1[i] = makeScreenDevice(i);
    this.screens = arrayOfGraphicsDevice1;
    GraphicsDevice[] arrayOfGraphicsDevice3 = this.screens;
    int k = arrayOfGraphicsDevice3.length;
    for (int l = 0; l < k; ++l)
    {
      GraphicsDevice localGraphicsDevice = arrayOfGraphicsDevice3[l];
      if (localGraphicsDevice instanceof DisplayChangedListener)
        ((DisplayChangedListener)localGraphicsDevice).displayChanged();
    }
    if (this.oldDevices != null)
    {
      int j = getDefaultScreen();
      ListIterator localListIterator = this.oldDevices.listIterator();
      while (localListIterator.hasNext())
      {
        Win32GraphicsDevice localWin32GraphicsDevice2 = (Win32GraphicsDevice)((WeakReference)localListIterator.next()).get();
        if (localWin32GraphicsDevice2 != null)
        {
          localWin32GraphicsDevice2.invalidate(j);
          localWin32GraphicsDevice2.displayChanged();
        }
        else
        {
          localListIterator.remove();
        }
      }
    }
    WToolkit.resetGC();
    this.displayChanger.notifyListeners();
  }

  private static native String getEUDCFontFile();

  protected boolean useAbsoluteFontFileNames()
  {
    return false;
  }

  protected void registerFontFile(String paramString, String[] paramArrayOfString, int paramInt, boolean paramBoolean)
  {
    int i;
    if (this.registeredFontFiles.contains(paramString))
      return;
    this.registeredFontFiles.add(paramString);
    if (ttFilter.accept(null, paramString))
      i = 0;
    else if (t1Filter.accept(null, paramString))
      i = 1;
    else
      return;
    if (this.fontPath == null)
      this.fontPath = getPlatformFontPath(noType1Font);
    String str1 = jreFontDirName + File.pathSeparator + this.fontPath;
    StringTokenizer localStringTokenizer = new StringTokenizer(str1, File.pathSeparator);
    int j = 0;
    try
    {
      while ((j == 0) && (localStringTokenizer.hasMoreTokens()))
      {
        String str2 = localStringTokenizer.nextToken();
        boolean bool = str2.equals(jreFontDirName);
        File localFile = new File(str2, paramString);
        if (localFile.canRead())
        {
          j = 1;
          String str3 = localFile.getAbsolutePath();
          if (paramBoolean)
          {
            FontManager.registerDeferredFont(paramString, str3, paramArrayOfString, i, bool, paramInt);
            break;
          }
          FontManager.registerFontFile(str3, paramArrayOfString, i, bool, paramInt);
          break;
        }
      }
    }
    catch (NoSuchElementException localNoSuchElementException)
    {
      System.err.println(localNoSuchElementException);
    }
    if (j == 0)
      addToMissingFontFileList(paramString);
  }

  protected void registerJREFontsWithPlatform(String paramString)
  {
    fontsForPrinting = paramString;
  }

  public static void registerJREFontsForPrinting()
  {
    label15: String str;
    synchronized (Win32GraphicsEnvironment.class)
    {
      if (fontsForPrinting != null)
        break label15;
      return;
      str = fontsForPrinting;
      fontsForPrinting = null;
    }
    AccessController.doPrivileged(new PrivilegedAction(str)
    {
      public Object run()
      {
        File localFile1 = new File(this.val$pathName);
        String[] arrayOfString = localFile1.list(new SunGraphicsEnvironment.TTFilter());
        if (arrayOfString == null)
          return null;
        for (int i = 0; i < arrayOfString.length; ++i)
        {
          File localFile2 = new File(localFile1, arrayOfString[i]);
          Win32GraphicsEnvironment.registerFontWithPlatform(localFile2.getAbsolutePath());
        }
        return null;
      }
    });
  }

  protected static native void registerFontWithPlatform(String paramString);

  protected static native void deRegisterFontWithPlatform(String paramString);

  protected GraphicsDevice makeScreenDevice(int paramInt)
  {
    Object localObject = null;
    if (WindowsFlags.isD3DEnabled())
      localObject = D3DGraphicsDevice.createDevice(paramInt);
    if (localObject == null)
      localObject = new Win32GraphicsDevice(paramInt);
    return ((GraphicsDevice)localObject);
  }

  public PrinterJob getPrinterJob()
  {
    SecurityManager localSecurityManager = System.getSecurityManager();
    if (localSecurityManager != null)
      localSecurityManager.checkPrintJobAccess();
    return new WPrinterJob();
  }

  protected FontConfiguration createFontConfiguration()
  {
    return new WFontConfiguration(this);
  }

  public FontConfiguration createFontConfiguration(boolean paramBoolean1, boolean paramBoolean2)
  {
    return new WFontConfiguration(this, paramBoolean1, paramBoolean2);
  }

  public boolean isFlipStrategyPreferred(ComponentPeer paramComponentPeer)
  {
    if (paramComponentPeer != null)
    {
      GraphicsConfiguration localGraphicsConfiguration;
      if ((localGraphicsConfiguration = paramComponentPeer.getGraphicsConfiguration()) != null)
      {
        GraphicsDevice localGraphicsDevice = localGraphicsConfiguration.getDevice();
        if (localGraphicsDevice instanceof D3DGraphicsDevice)
          return ((D3DGraphicsDevice)localGraphicsDevice).isD3DEnabledOnDevice();
      }
    }
    return false;
  }

  public static boolean isDWMCompositionEnabled()
  {
    return isDWMCompositionEnabled;
  }

  private static void dwmCompositionChanged(boolean paramBoolean)
  {
    isDWMCompositionEnabled = paramBoolean;
  }

  public static native boolean isVistaOS();

  static
  {
    WToolkit.loadLibraries();
    WindowsFlags.initFlags();
    initDisplayWrapper();
    eudcFontFileName = getEUDCFontFile();
    fontsForPrinting = null;
  }
}