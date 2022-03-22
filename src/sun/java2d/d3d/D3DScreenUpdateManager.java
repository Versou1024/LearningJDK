package sun.java2d.d3d;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Window;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashMap;
import sun.awt.SunToolkit;
import sun.awt.Win32GraphicsConfig;
import sun.awt.windows.WComponentPeer;
import sun.java2d.InvalidPipeException;
import sun.java2d.ScreenUpdateManager;
import sun.java2d.SunGraphics2D;
import sun.java2d.SurfaceData;
import sun.java2d.windows.GDIWindowSurfaceData;
import sun.java2d.windows.WindowsFlags;

public class D3DScreenUpdateManager extends ScreenUpdateManager
  implements Runnable
{
  private static final int MIN_WIN_SIZE = 150;
  private volatile boolean done = false;
  private volatile Thread screenUpdater;
  private boolean needsUpdateNow;
  private java.lang.Object runLock = new java.lang.Object();
  private ArrayList<D3DSurfaceData.D3DWindowSurfaceData> d3dwSurfaces;
  private HashMap<D3DSurfaceData.D3DWindowSurfaceData, GDIWindowSurfaceData> gdiSurfaces;

  public D3DScreenUpdateManager()
  {
    AccessController.doPrivileged(new PrivilegedAction(this)
    {
      public java.lang.Object run()
      {
        java.lang.Object localObject = Thread.currentThread().getThreadGroup();
        for (ThreadGroup localThreadGroup = ((ThreadGroup)localObject).getParent(); localThreadGroup != null; localThreadGroup = ((ThreadGroup)localObject).getParent())
          localObject = localThreadGroup;
        try
        {
          Runtime.getRuntime().addShutdownHook(new Thread((ThreadGroup)localObject, new Runnable(this)
          {
            public void run()
            {
              D3DScreenUpdateManager.access$002(this.this$1.this$0, true);
              this.this$1.this$0.wakeUpUpdateThread();
            }
          }));
        }
        catch (Exception localException)
        {
          D3DScreenUpdateManager.access$002(this.this$0, true);
        }
        return null;
      }
    });
  }

  public SurfaceData createScreenSurface(Win32GraphicsConfig paramWin32GraphicsConfig, WComponentPeer paramWComponentPeer, int paramInt, boolean paramBoolean)
  {
    if ((this.done) || (!(paramWin32GraphicsConfig instanceof D3DGraphicsConfig)))
      return super.createScreenSurface(paramWin32GraphicsConfig, paramWComponentPeer, paramInt, paramBoolean);
    java.lang.Object localObject = null;
    if (canUseD3DOnScreen(paramWComponentPeer, paramWin32GraphicsConfig, paramInt))
      try
      {
        localObject = D3DSurfaceData.createData(paramWComponentPeer);
      }
      catch (InvalidPipeException localInvalidPipeException)
      {
        localObject = null;
      }
    if (localObject == null)
      localObject = GDIWindowSurfaceData.createData(paramWComponentPeer);
    if (paramBoolean)
      repaintPeerTarget(paramWComponentPeer);
    return ((SurfaceData)localObject);
  }

  public static boolean canUseD3DOnScreen(WComponentPeer paramWComponentPeer, Win32GraphicsConfig paramWin32GraphicsConfig, int paramInt)
  {
    if (!(paramWin32GraphicsConfig instanceof D3DGraphicsConfig))
      return false;
    D3DGraphicsConfig localD3DGraphicsConfig = (D3DGraphicsConfig)paramWin32GraphicsConfig;
    D3DGraphicsDevice localD3DGraphicsDevice = localD3DGraphicsConfig.getD3DDevice();
    String str = paramWComponentPeer.getClass().getName();
    Rectangle localRectangle = paramWComponentPeer.getBounds();
    Component localComponent = (Component)paramWComponentPeer.getTarget();
    Window localWindow = localD3DGraphicsDevice.getFullScreenWindow();
    return ((WindowsFlags.isD3DOnScreenEnabled()) && (localD3DGraphicsDevice.isD3DEnabledOnDevice()) && (paramWComponentPeer.isAccelCapable()) && (((localRectangle.width > 150) || (localRectangle.height > 150))) && (paramInt == 0) && (((localWindow == null) || ((localWindow == localComponent) && (!(hasHWChildren(localComponent)))))) && (((str.equals("sun.awt.windows.WCanvasPeer")) || (str.equals("sun.awt.windows.WDialogPeer")) || (str.equals("sun.awt.windows.WPanelPeer")) || (str.equals("sun.awt.windows.WWindowPeer")) || (str.equals("sun.awt.windows.WFramePeer")) || (str.equals("sun.awt.windows.WEmbeddedFramePeer")))));
  }

  public Graphics2D createGraphics(SurfaceData paramSurfaceData, WComponentPeer paramWComponentPeer, Color paramColor1, Color paramColor2, Font paramFont)
  {
    if ((!(this.done)) && (paramSurfaceData instanceof D3DSurfaceData.D3DWindowSurfaceData))
    {
      D3DSurfaceData.D3DWindowSurfaceData localD3DWindowSurfaceData = (D3DSurfaceData.D3DWindowSurfaceData)paramSurfaceData;
      if ((!(localD3DWindowSurfaceData.isSurfaceLost())) || (validate(localD3DWindowSurfaceData)))
      {
        trackScreenSurface(localD3DWindowSurfaceData);
        return new SunGraphics2D(paramSurfaceData, paramColor1, paramColor2, paramFont);
      }
      paramSurfaceData = getGdiSurface(localD3DWindowSurfaceData);
    }
    return super.createGraphics(paramSurfaceData, paramWComponentPeer, paramColor1, paramColor2, paramFont);
  }

  private void repaintPeerTarget(WComponentPeer paramWComponentPeer)
  {
    Component localComponent = (Component)paramWComponentPeer.getTarget();
    SunToolkit.executeOnEventHandlerThread(localComponent, new Runnable(this, localComponent)
    {
      public void run()
      {
        this.val$target.repaint();
      }
    });
  }

  private void trackScreenSurface(SurfaceData paramSurfaceData)
  {
    if ((!(this.done)) && (paramSurfaceData instanceof D3DSurfaceData.D3DWindowSurfaceData))
    {
      synchronized (this)
      {
        if (this.d3dwSurfaces == null)
          this.d3dwSurfaces = new ArrayList();
        D3DSurfaceData.D3DWindowSurfaceData localD3DWindowSurfaceData = (D3DSurfaceData.D3DWindowSurfaceData)paramSurfaceData;
        if (!(this.d3dwSurfaces.contains(localD3DWindowSurfaceData)))
          this.d3dwSurfaces.add(localD3DWindowSurfaceData);
      }
      startUpdateThread();
    }
  }

  public synchronized void dropScreenSurface(SurfaceData paramSurfaceData)
  {
    if ((this.d3dwSurfaces != null) && (paramSurfaceData instanceof D3DSurfaceData.D3DWindowSurfaceData))
    {
      D3DSurfaceData.D3DWindowSurfaceData localD3DWindowSurfaceData = (D3DSurfaceData.D3DWindowSurfaceData)paramSurfaceData;
      removeGdiSurface(localD3DWindowSurfaceData);
      this.d3dwSurfaces.remove(localD3DWindowSurfaceData);
    }
  }

  public SurfaceData getReplacementScreenSurface(WComponentPeer paramWComponentPeer, SurfaceData paramSurfaceData)
  {
    SurfaceData localSurfaceData = super.getReplacementScreenSurface(paramWComponentPeer, paramSurfaceData);
    trackScreenSurface(localSurfaceData);
    return localSurfaceData;
  }

  private void removeGdiSurface(D3DSurfaceData.D3DWindowSurfaceData paramD3DWindowSurfaceData)
  {
    if (this.gdiSurfaces != null)
    {
      GDIWindowSurfaceData localGDIWindowSurfaceData = (GDIWindowSurfaceData)this.gdiSurfaces.get(paramD3DWindowSurfaceData);
      if (localGDIWindowSurfaceData != null)
      {
        localGDIWindowSurfaceData.invalidate();
        this.gdiSurfaces.remove(paramD3DWindowSurfaceData);
      }
    }
  }

  private synchronized void startUpdateThread()
  {
    if (this.screenUpdater == null)
    {
      this.screenUpdater = ((Thread)AccessController.doPrivileged(new PrivilegedAction(this)
      {
        public java.lang.Object run()
        {
          java.lang.Object localObject1 = Thread.currentThread().getThreadGroup();
          for (java.lang.Object localObject2 = localObject1; localObject2 != null; localObject2 = ((ThreadGroup)localObject1).getParent())
            localObject1 = localObject2;
          localObject2 = new Thread((ThreadGroup)localObject1, this.this$0, "D3D Screen Updater");
          ((Thread)localObject2).setPriority(7);
          ((Thread)localObject2).setDaemon(true);
          return localObject2;
        }
      }));
      this.screenUpdater.start();
    }
    else
    {
      wakeUpUpdateThread();
    }
  }

  public void wakeUpUpdateThread()
  {
    synchronized (this.runLock)
    {
      this.runLock.notifyAll();
    }
  }

  // ERROR //
  public void runUpdateNow()
  {
    // Byte code:
    //   0: aload_0
    //   1: dup
    //   2: astore_1
    //   3: monitorenter
    //   4: aload_0
    //   5: getfield 259	sun/java2d/d3d/D3DScreenUpdateManager:done	Z
    //   8: ifne +27 -> 35
    //   11: aload_0
    //   12: getfield 262	sun/java2d/d3d/D3DScreenUpdateManager:screenUpdater	Ljava/lang/Thread;
    //   15: ifnull +20 -> 35
    //   18: aload_0
    //   19: getfield 263	sun/java2d/d3d/D3DScreenUpdateManager:d3dwSurfaces	Ljava/util/ArrayList;
    //   22: ifnull +13 -> 35
    //   25: aload_0
    //   26: getfield 263	sun/java2d/d3d/D3DScreenUpdateManager:d3dwSurfaces	Ljava/util/ArrayList;
    //   29: invokevirtual 276	java/util/ArrayList:size	()I
    //   32: ifne +6 -> 38
    //   35: aload_1
    //   36: monitorexit
    //   37: return
    //   38: aload_1
    //   39: monitorexit
    //   40: goto +8 -> 48
    //   43: astore_2
    //   44: aload_1
    //   45: monitorexit
    //   46: aload_2
    //   47: athrow
    //   48: aload_0
    //   49: getfield 261	sun/java2d/d3d/D3DScreenUpdateManager:runLock	Ljava/lang/Object;
    //   52: dup
    //   53: astore_1
    //   54: monitorenter
    //   55: aload_0
    //   56: iconst_1
    //   57: putfield 260	sun/java2d/d3d/D3DScreenUpdateManager:needsUpdateNow	Z
    //   60: aload_0
    //   61: getfield 261	sun/java2d/d3d/D3DScreenUpdateManager:runLock	Ljava/lang/Object;
    //   64: invokevirtual 269	java/lang/Object:notifyAll	()V
    //   67: aload_0
    //   68: getfield 260	sun/java2d/d3d/D3DScreenUpdateManager:needsUpdateNow	Z
    //   71: ifeq +17 -> 88
    //   74: aload_0
    //   75: getfield 261	sun/java2d/d3d/D3DScreenUpdateManager:runLock	Ljava/lang/Object;
    //   78: invokevirtual 270	java/lang/Object:wait	()V
    //   81: goto -14 -> 67
    //   84: astore_2
    //   85: goto -18 -> 67
    //   88: aload_1
    //   89: monitorexit
    //   90: goto +8 -> 98
    //   93: astore_3
    //   94: aload_1
    //   95: monitorexit
    //   96: aload_3
    //   97: athrow
    //   98: return
    //
    // Exception table:
    //   from	to	target	type
    //   4	37	43	finally
    //   38	40	43	finally
    //   43	46	43	finally
    //   74	81	84	java/lang/InterruptedException
    //   55	90	93	finally
    //   93	96	93	finally
  }

  public void run()
  {
    while (!(this.done))
    {
      synchronized (this.runLock)
      {
        long l = (this.d3dwSurfaces.size() > 0) ? 100L : 3412048373585281024L;
        if (!(this.needsUpdateNow))
          try
          {
            this.runLock.wait(l);
          }
          catch (InterruptedException localInterruptedException)
          {
          }
      }
      ??? = new D3DSurfaceData.D3DWindowSurfaceData[0];
      synchronized (this)
      {
        ??? = (D3DSurfaceData.D3DWindowSurfaceData[])this.d3dwSurfaces.toArray(???);
      }
      ??? = ???;
      int i = ???.length;
      for (int j = 0; j < i; ++j)
      {
        D3DSurfaceData localD3DSurfaceData = ???[j];
        if ((localD3DSurfaceData.isValid()) && (((localD3DSurfaceData.isDirty()) || (localD3DSurfaceData.isSurfaceLost()))))
          if (!(localD3DSurfaceData.isSurfaceLost()))
          {
            D3DRenderQueue localD3DRenderQueue = D3DRenderQueue.getInstance();
            localD3DRenderQueue.lock();
            try
            {
              Rectangle localRectangle = localD3DSurfaceData.getBounds();
              D3DSurfaceData.swapBuffers(localD3DSurfaceData, 0, 0, localRectangle.width, localRectangle.height);
              localD3DSurfaceData.increaseNumCopies();
            }
            finally
            {
              localD3DRenderQueue.unlock();
            }
          }
          else if (!(validate(localD3DSurfaceData)))
          {
            localD3DSurfaceData.getPeer().replaceSurfaceDataLater();
          }
      }
      synchronized (this.runLock)
      {
        this.needsUpdateNow = false;
        this.runLock.notifyAll();
      }
    }
  }

  private boolean validate(D3DSurfaceData.D3DWindowSurfaceData paramD3DWindowSurfaceData)
  {
    if (paramD3DWindowSurfaceData.isSurfaceLost())
      try
      {
        paramD3DWindowSurfaceData.restoreSurface();
        Color localColor = paramD3DWindowSurfaceData.getPeer().getBackgroundNoSync();
        SunGraphics2D localSunGraphics2D = new SunGraphics2D(paramD3DWindowSurfaceData, localColor, localColor, null);
        localSunGraphics2D.fillRect(0, 0, paramD3DWindowSurfaceData.getBounds().width, paramD3DWindowSurfaceData.getBounds().height);
        localSunGraphics2D.dispose();
        paramD3DWindowSurfaceData.increaseNumCopies();
        repaintPeerTarget(paramD3DWindowSurfaceData.getPeer());
      }
      catch (InvalidPipeException localInvalidPipeException)
      {
        return false;
      }
    return true;
  }

  private synchronized SurfaceData getGdiSurface(D3DSurfaceData.D3DWindowSurfaceData paramD3DWindowSurfaceData)
  {
    if (this.gdiSurfaces == null)
      this.gdiSurfaces = new HashMap();
    GDIWindowSurfaceData localGDIWindowSurfaceData = (GDIWindowSurfaceData)this.gdiSurfaces.get(paramD3DWindowSurfaceData);
    if (localGDIWindowSurfaceData == null)
    {
      localGDIWindowSurfaceData = GDIWindowSurfaceData.createData(paramD3DWindowSurfaceData.getPeer());
      this.gdiSurfaces.put(paramD3DWindowSurfaceData, localGDIWindowSurfaceData);
    }
    return localGDIWindowSurfaceData;
  }

  private static boolean hasHWChildren(Component paramComponent)
  {
    if (paramComponent instanceof Container)
    {
      Component[] arrayOfComponent = ((Container)paramComponent).getComponents();
      int i = arrayOfComponent.length;
      for (int j = 0; j < i; ++j)
      {
        Component localComponent = arrayOfComponent[j];
        if ((localComponent.getPeer() instanceof WComponentPeer) || (hasHWChildren(localComponent)))
          return true;
      }
    }
    return false;
  }
}