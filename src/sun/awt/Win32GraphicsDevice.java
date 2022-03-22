package sun.awt;

import java.awt.AWTPermission;
import java.awt.DisplayMode;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.ColorModel;
import java.awt.peer.WindowPeer;
import java.io.PrintStream;
import java.security.AccessController;
import java.util.ArrayList;
import java.util.Vector;
import sun.awt.windows.WWindowPeer;
import sun.java2d.opengl.WGLGraphicsConfig;
import sun.java2d.windows.WindowsFlags;
import sun.security.action.GetPropertyAction;

public class Win32GraphicsDevice extends GraphicsDevice
  implements DisplayChangedListener
{
  int screen;
  ColorModel dynamicColorModel;
  ColorModel colorModel;
  protected GraphicsConfiguration[] configs;
  protected GraphicsConfiguration defaultConfig;
  private final String idString;
  protected String descString;
  private boolean valid;
  private SunDisplayChanger topLevels = new SunDisplayChanger();
  protected static boolean pfDisabled;
  private static AWTPermission fullScreenExclusivePermission;
  private DisplayMode defaultDisplayMode;
  private WindowListener fsWindowListener;

  private static native void initIDs();

  native void initDevice(int paramInt);

  public Win32GraphicsDevice(int paramInt)
  {
    this.screen = paramInt;
    this.idString = "\\Display" + this.screen;
    this.descString = "Win32GraphicsDevice[screen=" + this.screen;
    this.valid = true;
    initDevice(paramInt);
  }

  public int getType()
  {
    return 0;
  }

  public int getScreen()
  {
    return this.screen;
  }

  public boolean isValid()
  {
    return this.valid;
  }

  protected void invalidate(int paramInt)
  {
    this.valid = false;
    this.screen = paramInt;
  }

  public String getIDstring()
  {
    return this.idString;
  }

  public GraphicsConfiguration[] getConfigurations()
  {
    if (this.configs == null)
    {
      if ((WindowsFlags.isOGLEnabled()) && (isDefaultDevice()))
      {
        this.defaultConfig = getDefaultConfiguration();
        if (this.defaultConfig != null)
        {
          this.configs = new GraphicsConfiguration[1];
          this.configs[0] = this.defaultConfig;
          return this.configs;
        }
      }
      int i = getMaxConfigs(this.screen);
      int j = getDefaultPixID(this.screen);
      Vector localVector = new Vector(i);
      if (j == 0)
      {
        this.defaultConfig = Win32GraphicsConfig.getConfig(this, j);
        localVector.addElement(this.defaultConfig);
      }
      else
      {
        for (int k = 1; k <= i; ++k)
          if (isPixFmtSupported(k, this.screen))
            if (k == j)
            {
              this.defaultConfig = Win32GraphicsConfig.getConfig(this, k);
              localVector.addElement(this.defaultConfig);
            }
            else
            {
              localVector.addElement(Win32GraphicsConfig.getConfig(this, k));
            }
      }
      this.configs = new GraphicsConfiguration[localVector.size()];
      localVector.copyInto(this.configs);
    }
    return this.configs;
  }

  protected int getMaxConfigs(int paramInt)
  {
    if (pfDisabled)
      return 1;
    return getMaxConfigsImpl(paramInt);
  }

  private native int getMaxConfigsImpl(int paramInt);

  protected native boolean isPixFmtSupported(int paramInt1, int paramInt2);

  protected int getDefaultPixID(int paramInt)
  {
    if (pfDisabled)
      return 0;
    return getDefaultPixIDImpl(paramInt);
  }

  private native int getDefaultPixIDImpl(int paramInt);

  public GraphicsConfiguration getDefaultConfiguration()
  {
    if (this.defaultConfig == null)
    {
      if ((WindowsFlags.isOGLEnabled()) && (isDefaultDevice()))
      {
        int i = WGLGraphicsConfig.getDefaultPixFmt(this.screen);
        this.defaultConfig = WGLGraphicsConfig.getConfig(this, i);
        if (WindowsFlags.isOGLVerbose())
        {
          if (this.defaultConfig != null)
            System.out.print("OpenGL pipeline enabled");
          else
            System.out.print("Could not enable OpenGL pipeline");
          System.out.println(" for default config on screen " + this.screen);
        }
      }
      if (this.defaultConfig == null)
        this.defaultConfig = Win32GraphicsConfig.getConfig(this, 0);
    }
    return this.defaultConfig;
  }

  public String toString()
  {
    return this.descString + ", removed]";
  }

  private boolean isDefaultDevice()
  {
    return (this == GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice());
  }

  private static boolean isFSExclusiveModeAllowed()
  {
    SecurityManager localSecurityManager = System.getSecurityManager();
    if (localSecurityManager != null)
    {
      if (fullScreenExclusivePermission == null)
        fullScreenExclusivePermission = new AWTPermission("fullScreenExclusive");
      try
      {
        localSecurityManager.checkPermission(fullScreenExclusivePermission);
      }
      catch (SecurityException localSecurityException)
      {
        return false;
      }
    }
    return true;
  }

  public boolean isFullScreenSupported()
  {
    return isFSExclusiveModeAllowed();
  }

  public synchronized void setFullScreenWindow(Window paramWindow)
  {
    WWindowPeer localWWindowPeer1;
    Window localWindow = getFullScreenWindow();
    if (paramWindow == localWindow)
      return;
    if (!(isFullScreenSupported()))
    {
      super.setFullScreenWindow(paramWindow);
      return;
    }
    if (localWindow != null)
    {
      if (this.defaultDisplayMode != null)
      {
        setDisplayMode(this.defaultDisplayMode);
        this.defaultDisplayMode = null;
      }
      localWWindowPeer1 = (WWindowPeer)localWindow.getPeer();
      if (localWWindowPeer1 != null)
        synchronized (localWWindowPeer1)
        {
          exitFullScreenExclusive(this.screen, localWWindowPeer1);
        }
      removeFSWindowListener(localWindow);
    }
    super.setFullScreenWindow(paramWindow);
    if (paramWindow != null)
    {
      this.defaultDisplayMode = getDisplayMode();
      addFSWindowListener(paramWindow);
      localWWindowPeer1 = (WWindowPeer)paramWindow.getPeer();
      synchronized (localWWindowPeer1)
      {
        enterFullScreenExclusive(this.screen, localWWindowPeer1);
      }
      localWWindowPeer1.updateGC();
      localWWindowPeer1.resetTargetGC();
    }
  }

  protected native void enterFullScreenExclusive(int paramInt, WindowPeer paramWindowPeer);

  protected native void exitFullScreenExclusive(int paramInt, WindowPeer paramWindowPeer);

  public boolean isDisplayChangeSupported()
  {
    return ((isFullScreenSupported()) && (getFullScreenWindow() != null));
  }

  public synchronized void setDisplayMode(DisplayMode paramDisplayMode)
  {
    if (!(isDisplayChangeSupported()))
    {
      super.setDisplayMode(paramDisplayMode);
      return;
    }
    if (paramDisplayMode != null)
      if ((paramDisplayMode = getMatchingDisplayMode(paramDisplayMode)) != null)
        break label37;
    throw new IllegalArgumentException("Invalid display mode");
    if (getDisplayMode().equals(paramDisplayMode))
      label37: return;
    Window localWindow = getFullScreenWindow();
    if (localWindow != null)
    {
      WWindowPeer localWWindowPeer = (WWindowPeer)localWindow.getPeer();
      configDisplayMode(this.screen, localWWindowPeer, paramDisplayMode.getWidth(), paramDisplayMode.getHeight(), paramDisplayMode.getBitDepth(), paramDisplayMode.getRefreshRate());
      Rectangle localRectangle = getDefaultConfiguration().getBounds();
      localWindow.setBounds(localRectangle.x, localRectangle.y, paramDisplayMode.getWidth(), paramDisplayMode.getHeight());
    }
    else
    {
      throw new IllegalStateException("Must be in fullscreen mode in order to set display mode");
    }
  }

  protected native DisplayMode getCurrentDisplayMode(int paramInt);

  protected native void configDisplayMode(int paramInt1, WindowPeer paramWindowPeer, int paramInt2, int paramInt3, int paramInt4, int paramInt5);

  protected native void enumDisplayModes(int paramInt, ArrayList paramArrayList);

  public synchronized DisplayMode getDisplayMode()
  {
    DisplayMode localDisplayMode = getCurrentDisplayMode(this.screen);
    return localDisplayMode;
  }

  public synchronized DisplayMode[] getDisplayModes()
  {
    ArrayList localArrayList = new ArrayList();
    enumDisplayModes(this.screen, localArrayList);
    int i = localArrayList.size();
    DisplayMode[] arrayOfDisplayMode = new DisplayMode[i];
    for (int j = 0; j < i; ++j)
      arrayOfDisplayMode[j] = ((DisplayMode)localArrayList.get(j));
    return arrayOfDisplayMode;
  }

  protected synchronized DisplayMode getMatchingDisplayMode(DisplayMode paramDisplayMode)
  {
    if (!(isDisplayChangeSupported()))
      return null;
    DisplayMode[] arrayOfDisplayMode1 = getDisplayModes();
    DisplayMode[] arrayOfDisplayMode2 = arrayOfDisplayMode1;
    int i = arrayOfDisplayMode2.length;
    for (int j = 0; j < i; ++j)
    {
      DisplayMode localDisplayMode = arrayOfDisplayMode2[j];
      if ((paramDisplayMode.equals(localDisplayMode)) || ((paramDisplayMode.getRefreshRate() == 0) && (paramDisplayMode.getWidth() == localDisplayMode.getWidth()) && (paramDisplayMode.getHeight() == localDisplayMode.getHeight()) && (paramDisplayMode.getBitDepth() == localDisplayMode.getBitDepth())))
        return localDisplayMode;
    }
    return null;
  }

  public void displayChanged()
  {
    this.dynamicColorModel = null;
    this.defaultConfig = null;
    this.configs = null;
    this.topLevels.notifyListeners();
  }

  public void paletteChanged()
  {
  }

  public void addDisplayChangedListener(DisplayChangedListener paramDisplayChangedListener)
  {
    this.topLevels.add(paramDisplayChangedListener);
  }

  public void removeDisplayChangedListener(DisplayChangedListener paramDisplayChangedListener)
  {
    this.topLevels.remove(paramDisplayChangedListener);
  }

  private native ColorModel makeColorModel(int paramInt, boolean paramBoolean);

  public ColorModel getDynamicColorModel()
  {
    if (this.dynamicColorModel == null)
      this.dynamicColorModel = makeColorModel(this.screen, true);
    return this.dynamicColorModel;
  }

  public ColorModel getColorModel()
  {
    if (this.colorModel == null)
      this.colorModel = makeColorModel(this.screen, false);
    return this.colorModel;
  }

  protected void addFSWindowListener(Window paramWindow)
  {
    this.fsWindowListener = new Win32FSWindowAdapter(this);
    paramWindow.addWindowListener(this.fsWindowListener);
  }

  protected void removeFSWindowListener(Window paramWindow)
  {
    paramWindow.removeWindowListener(this.fsWindowListener);
    this.fsWindowListener = null;
  }

  static
  {
    String str = (String)AccessController.doPrivileged(new GetPropertyAction("sun.awt.nopixfmt"));
    pfDisabled = str != null;
    initIDs();
  }

  private static class Win32FSWindowAdapter extends WindowAdapter
  {
    private Win32GraphicsDevice device;
    private DisplayMode dm;

    Win32FSWindowAdapter(Win32GraphicsDevice paramWin32GraphicsDevice)
    {
      this.device = paramWin32GraphicsDevice;
    }

    private void setFSWindowsState(Window paramWindow, int paramInt)
    {
      GraphicsDevice localGraphicsDevice;
      GraphicsDevice[] arrayOfGraphicsDevice1 = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
      if (paramWindow != null)
      {
        arrayOfGraphicsDevice2 = arrayOfGraphicsDevice1;
        i = arrayOfGraphicsDevice2.length;
        for (j = 0; j < i; ++j)
        {
          localGraphicsDevice = arrayOfGraphicsDevice2[j];
          if (paramWindow == localGraphicsDevice.getFullScreenWindow())
            return;
        }
      }
      GraphicsDevice[] arrayOfGraphicsDevice2 = arrayOfGraphicsDevice1;
      int i = arrayOfGraphicsDevice2.length;
      for (int j = 0; j < i; ++j)
      {
        localGraphicsDevice = arrayOfGraphicsDevice2[j];
        Window localWindow = localGraphicsDevice.getFullScreenWindow();
        if (localWindow instanceof Frame)
          ((Frame)localWindow).setExtendedState(paramInt);
      }
    }

    public void windowDeactivated(WindowEvent paramWindowEvent)
    {
      setFSWindowsState(paramWindowEvent.getOppositeWindow(), 1);
    }

    public void windowActivated(WindowEvent paramWindowEvent)
    {
      setFSWindowsState(paramWindowEvent.getOppositeWindow(), 0);
    }

    public void windowIconified(WindowEvent paramWindowEvent)
    {
      DisplayMode localDisplayMode = Win32GraphicsDevice.access$000(this.device);
      if (localDisplayMode != null)
      {
        this.dm = this.device.getDisplayMode();
        this.device.setDisplayMode(localDisplayMode);
      }
    }

    public void windowDeiconified(WindowEvent paramWindowEvent)
    {
      if (this.dm != null)
      {
        this.device.setDisplayMode(this.dm);
        this.dm = null;
      }
    }
  }
}