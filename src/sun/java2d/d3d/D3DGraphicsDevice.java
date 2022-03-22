package sun.java2d.d3d;

import java.awt.Dialog;
import java.awt.DisplayMode;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.peer.WindowPeer;
import java.io.PrintStream;
import java.util.ArrayList;
import sun.awt.Win32GraphicsDevice;
import sun.awt.windows.WWindowPeer;
import sun.java2d.pipe.hw.ContextCapabilities;
import sun.java2d.windows.WindowsFlags;

public class D3DGraphicsDevice extends Win32GraphicsDevice
{
  private D3DContext context;
  private static boolean d3dAvailable;
  private ContextCapabilities d3dCaps;
  private boolean fsStatus;
  private Rectangle ownerOrigBounds = null;
  private boolean ownerWasVisible;
  private Window realFSWindow;
  private WindowListener fsWindowListener;
  private boolean fsWindowWasAlwaysOnTop;

  private static native boolean initD3D();

  public static D3DGraphicsDevice createDevice(int paramInt)
  {
    if (!(d3dAvailable))
      return null;
    ContextCapabilities localContextCapabilities = getDeviceCaps(paramInt);
    if ((localContextCapabilities.getCaps() & 0x40000) == 0)
    {
      if (WindowsFlags.isD3DVerbose())
        System.out.println("Could not enable Direct3D pipeline on screen " + paramInt);
      return null;
    }
    if (WindowsFlags.isD3DVerbose())
      System.out.println("Direct3D pipeline enabled on screen " + paramInt);
    D3DGraphicsDevice localD3DGraphicsDevice = new D3DGraphicsDevice(paramInt, localContextCapabilities);
    return localD3DGraphicsDevice;
  }

  private static native int getDeviceCapsNative(int paramInt);

  private static native String getDeviceIdNative(int paramInt);

  private static ContextCapabilities getDeviceCaps(int paramInt)
  {
    D3DContext.D3DContextCaps localD3DContextCaps = null;
    D3DRenderQueue localD3DRenderQueue = D3DRenderQueue.getInstance();
    localD3DRenderQueue.lock();
    try
    {
      1Result local1Result = new Object()
      {
        int caps;
        String id;
      };
      localD3DRenderQueue.flushAndInvokeNow(new Runnable(local1Result, paramInt)
      {
        public void run()
        {
          this.val$res.caps = D3DGraphicsDevice.access$000(this.val$screen);
          this.val$res.id = D3DGraphicsDevice.access$100(this.val$screen);
        }
      });
      localD3DContextCaps = new D3DContext.D3DContextCaps(local1Result.caps, local1Result.id);
    }
    finally
    {
      localD3DRenderQueue.unlock();
    }
    return new D3DContext.D3DContextCaps(0, null);
  }

  public final boolean isCapPresent(int paramInt)
  {
    return ((this.d3dCaps.getCaps() & paramInt) != 0);
  }

  private D3DGraphicsDevice(int paramInt, ContextCapabilities paramContextCapabilities)
  {
    super(paramInt);
    this.descString = "D3DGraphicsDevice[screen=" + paramInt;
    this.d3dCaps = paramContextCapabilities;
    this.context = new D3DContext(D3DRenderQueue.getInstance(), this);
  }

  public boolean isD3DEnabledOnDevice()
  {
    return ((isValid()) && (isCapPresent(262144)));
  }

  public static boolean isD3DAvailable()
  {
    return d3dAvailable;
  }

  private Frame getToplevelOwner(Window paramWindow)
  {
    Window localWindow = paramWindow;
    do
    {
      if (localWindow == null)
        break label23;
      localWindow = localWindow.getOwner();
    }
    while (!(localWindow instanceof Frame));
    return ((Frame)localWindow);
    label23: return null;
  }

  private static native boolean enterFullScreenExclusiveNative(int paramInt, long paramLong);

  protected void enterFullScreenExclusive(int paramInt, WindowPeer paramWindowPeer)
  {
    WWindowPeer localWWindowPeer = (WWindowPeer)this.realFSWindow.getPeer();
    D3DRenderQueue localD3DRenderQueue = D3DRenderQueue.getInstance();
    localD3DRenderQueue.lock();
    try
    {
      localD3DRenderQueue.flushAndInvokeNow(new Runnable(this, localWWindowPeer, paramInt)
      {
        public void run()
        {
          long l = this.val$wpeer.getHWnd();
          if (l == 3412041243939569664L)
          {
            D3DGraphicsDevice.access$202(this.this$0, false);
            return;
          }
          D3DGraphicsDevice.access$202(this.this$0, D3DGraphicsDevice.access$300(this.val$screen, l));
        }
      });
    }
    finally
    {
      localD3DRenderQueue.unlock();
    }
    if (!(this.fsStatus))
      super.enterFullScreenExclusive(paramInt, paramWindowPeer);
  }

  private static native boolean exitFullScreenExclusiveNative(int paramInt);

  protected void exitFullScreenExclusive(int paramInt, WindowPeer paramWindowPeer)
  {
    if (this.fsStatus)
    {
      D3DRenderQueue localD3DRenderQueue = D3DRenderQueue.getInstance();
      localD3DRenderQueue.lock();
      try
      {
        localD3DRenderQueue.flushAndInvokeNow(new Runnable(this, paramInt)
        {
          public void run()
          {
            D3DGraphicsDevice.access$400(this.val$screen);
          }
        });
      }
      finally
      {
        localD3DRenderQueue.unlock();
      }
    }
    else
    {
      super.exitFullScreenExclusive(paramInt, paramWindowPeer);
    }
  }

  protected void addFSWindowListener(Window paramWindow)
  {
    if ((!(paramWindow instanceof Frame)) && (!(paramWindow instanceof Dialog)))
      if ((this.realFSWindow = getToplevelOwner(paramWindow)) != null)
      {
        this.ownerOrigBounds = this.realFSWindow.getBounds();
        WWindowPeer localWWindowPeer = (WWindowPeer)this.realFSWindow.getPeer();
        this.ownerWasVisible = this.realFSWindow.isVisible();
        Rectangle localRectangle = paramWindow.getBounds();
        localWWindowPeer.reshape(localRectangle.x, localRectangle.y, localRectangle.width, localRectangle.height);
        localWWindowPeer.setVisible(true);
      }
    else
      this.realFSWindow = paramWindow;
    this.fsWindowWasAlwaysOnTop = this.realFSWindow.isAlwaysOnTop();
    ((WWindowPeer)this.realFSWindow.getPeer()).setAlwaysOnTop(true);
    this.fsWindowListener = new D3DFSWindowAdapter(null);
    this.realFSWindow.addWindowListener(this.fsWindowListener);
  }

  protected void removeFSWindowListener(Window paramWindow)
  {
    this.realFSWindow.removeWindowListener(this.fsWindowListener);
    this.fsWindowListener = null;
    WWindowPeer localWWindowPeer = (WWindowPeer)this.realFSWindow.getPeer();
    if (localWWindowPeer != null)
    {
      if (this.ownerOrigBounds != null)
      {
        if (this.ownerOrigBounds.width == 0)
          this.ownerOrigBounds.width = 1;
        if (this.ownerOrigBounds.height == 0)
          this.ownerOrigBounds.height = 1;
        localWWindowPeer.reshape(this.ownerOrigBounds.x, this.ownerOrigBounds.y, this.ownerOrigBounds.width, this.ownerOrigBounds.height);
        if (!(this.ownerWasVisible))
          localWWindowPeer.setVisible(false);
        this.ownerOrigBounds = null;
      }
      if (!(this.fsWindowWasAlwaysOnTop))
        localWWindowPeer.setAlwaysOnTop(false);
    }
    this.realFSWindow = null;
  }

  private static native DisplayMode getCurrentDisplayModeNative(int paramInt);

  protected DisplayMode getCurrentDisplayMode(int paramInt)
  {
    D3DRenderQueue localD3DRenderQueue = D3DRenderQueue.getInstance();
    localD3DRenderQueue.lock();
    try
    {
      2Result local2Result = new Object(this)
      {
        DisplayMode dm = null;
      };
      localD3DRenderQueue.flushAndInvokeNow(new Runnable(this, local2Result, paramInt)
      {
        public void run()
        {
          this.val$res.dm = D3DGraphicsDevice.access$600(this.val$screen);
        }
      });
      if (local2Result.dm == null)
      {
        localDisplayMode = super.getCurrentDisplayMode(paramInt);
        return localDisplayMode;
      }
      DisplayMode localDisplayMode = local2Result.dm;
      return localDisplayMode;
    }
    finally
    {
      localD3DRenderQueue.unlock();
    }
  }

  private static native void configDisplayModeNative(int paramInt1, long paramLong, int paramInt2, int paramInt3, int paramInt4, int paramInt5);

  protected void configDisplayMode(int paramInt1, WindowPeer paramWindowPeer, int paramInt2, int paramInt3, int paramInt4, int paramInt5)
  {
    if (!(this.fsStatus))
    {
      super.configDisplayMode(paramInt1, paramWindowPeer, paramInt2, paramInt3, paramInt4, paramInt5);
      return;
    }
    WWindowPeer localWWindowPeer = (WWindowPeer)this.realFSWindow.getPeer();
    if (getFullScreenWindow() != this.realFSWindow)
    {
      localObject1 = getDefaultConfiguration().getBounds();
      localWWindowPeer.reshape(((Rectangle)localObject1).x, ((Rectangle)localObject1).y, paramInt2, paramInt3);
    }
    Object localObject1 = D3DRenderQueue.getInstance();
    ((D3DRenderQueue)localObject1).lock();
    try
    {
      ((D3DRenderQueue)localObject1).flushAndInvokeNow(new Runnable(this, localWWindowPeer, paramInt1, paramInt2, paramInt3, paramInt4, paramInt5)
      {
        public void run()
        {
          long l = this.val$wpeer.getHWnd();
          if (l == 3412041243939569664L)
            return;
          D3DGraphicsDevice.access$700(this.val$screen, l, this.val$width, this.val$height, this.val$bitDepth, this.val$refreshRate);
        }
      });
    }
    finally
    {
      ((D3DRenderQueue)localObject1).unlock();
    }
  }

  private static native void enumDisplayModesNative(int paramInt, ArrayList paramArrayList);

  protected void enumDisplayModes(int paramInt, ArrayList paramArrayList)
  {
    D3DRenderQueue localD3DRenderQueue = D3DRenderQueue.getInstance();
    localD3DRenderQueue.lock();
    try
    {
      localD3DRenderQueue.flushAndInvokeNow(new Runnable(this, paramInt, paramArrayList)
      {
        public void run()
        {
          D3DGraphicsDevice.access$800(this.val$screen, this.val$modes);
        }
      });
      if (paramArrayList.size() == 0)
        paramArrayList.add(getCurrentDisplayModeNative(paramInt));
    }
    finally
    {
      localD3DRenderQueue.unlock();
    }
  }

  private static native long getAvailableAcceleratedMemoryNative(int paramInt);

  public int getAvailableAcceleratedMemory()
  {
    D3DRenderQueue localD3DRenderQueue = D3DRenderQueue.getInstance();
    localD3DRenderQueue.lock();
    try
    {
      3Result local3Result = new Object(this)
      {
        long mem = 3412040075708465152L;
      };
      localD3DRenderQueue.flushAndInvokeNow(new Runnable(this, local3Result)
      {
        public void run()
        {
          this.val$res.mem = D3DGraphicsDevice.access$900(this.this$0.getScreen());
        }
      });
      int i = (int)local3Result.mem;
      return i;
    }
    finally
    {
      localD3DRenderQueue.unlock();
    }
  }

  public GraphicsConfiguration[] getConfigurations()
  {
    if ((this.configs == null) && (isD3DEnabledOnDevice()))
    {
      this.defaultConfig = getDefaultConfiguration();
      if (this.defaultConfig != null)
      {
        this.configs = new GraphicsConfiguration[1];
        this.configs[0] = this.defaultConfig;
        return this.configs;
      }
    }
    return super.getConfigurations();
  }

  public GraphicsConfiguration getDefaultConfiguration()
  {
    if (this.defaultConfig == null)
      if (isD3DEnabledOnDevice())
        this.defaultConfig = new D3DGraphicsConfig(this);
      else
        this.defaultConfig = super.getDefaultConfiguration();
    return this.defaultConfig;
  }

  private static native boolean isD3DAvailableOnDeviceNative(int paramInt);

  public static boolean isD3DAvailableOnDevice(int paramInt)
  {
    if (!(d3dAvailable))
      return false;
    D3DRenderQueue localD3DRenderQueue = D3DRenderQueue.getInstance();
    localD3DRenderQueue.lock();
    try
    {
      4Result local4Result = new Object()
      {
        boolean avail = false;
      };
      localD3DRenderQueue.flushAndInvokeNow(new Runnable(local4Result, paramInt)
      {
        public void run()
        {
          this.val$res.avail = D3DGraphicsDevice.access$1000(this.val$screen);
        }
      });
      boolean bool = local4Result.avail;
      return bool;
    }
    finally
    {
      localD3DRenderQueue.unlock();
    }
  }

  D3DContext getContext()
  {
    return this.context;
  }

  ContextCapabilities getContextCapabilities()
  {
    return this.d3dCaps;
  }

  public void displayChanged()
  {
    super.displayChanged();
    if (d3dAvailable)
      this.d3dCaps = getDeviceCaps(getScreen());
  }

  protected void invalidate(int paramInt)
  {
    super.invalidate(paramInt);
    this.d3dCaps = new D3DContext.D3DContextCaps(0, null);
  }

  static
  {
    Toolkit.getDefaultToolkit();
    d3dAvailable = initD3D();
    if (d3dAvailable)
      pfDisabled = true;
  }

  private static class D3DFSWindowAdapter extends WindowAdapter
  {
    public void windowDeactivated(WindowEvent paramWindowEvent)
    {
      D3DRenderQueue.getInstance();
      D3DRenderQueue.restoreDevices();
    }

    public void windowActivated(WindowEvent paramWindowEvent)
    {
      D3DRenderQueue.getInstance();
      D3DRenderQueue.restoreDevices();
    }
  }
}