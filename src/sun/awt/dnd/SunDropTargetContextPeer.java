package sun.awt.dnd;

import java.awt.Component;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetContext;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.dnd.InvalidDnDOperationException;
import java.awt.dnd.peer.DropTargetContextPeer;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Map;
import sun.awt.AppContext;
import sun.awt.DebugHelper;
import sun.awt.SunToolkit;
import sun.awt.datatransfer.DataTransferer;
import sun.awt.datatransfer.ToolkitThreadBlockedHandler;

public abstract class SunDropTargetContextPeer
  implements DropTargetContextPeer, Transferable
{
  public static final boolean DISPATCH_SYNC = 1;
  private DropTarget currentDT;
  private DropTargetContext currentDTC;
  private long[] currentT;
  private int currentA;
  private int currentSA;
  private int currentDA;
  private int previousDA;
  private long nativeDragContext;
  private Transferable local;
  private boolean dragRejected = false;
  protected int dropStatus = 0;
  protected boolean dropComplete = false;
  protected static final Object _globalLock = new Object();
  private static final DebugHelper dbg = DebugHelper.create(SunDropTargetContextPeer.class);
  protected static Transferable currentJVMLocalSourceTransferable = null;
  protected static final int STATUS_NONE = 0;
  protected static final int STATUS_WAIT = 1;
  protected static final int STATUS_ACCEPT = 2;
  protected static final int STATUS_REJECT = -1;

  public static void setCurrentJVMLocalSourceTransferable(Transferable paramTransferable)
    throws InvalidDnDOperationException
  {
    synchronized (_globalLock)
    {
      if ((paramTransferable != null) && (currentJVMLocalSourceTransferable != null))
        throw new InvalidDnDOperationException();
      currentJVMLocalSourceTransferable = paramTransferable;
    }
  }

  private static Transferable getJVMLocalSourceTransferable()
  {
    return currentJVMLocalSourceTransferable;
  }

  public DropTarget getDropTarget()
  {
    return this.currentDT;
  }

  public synchronized void setTargetActions(int paramInt)
  {
    this.currentA = (paramInt & 0x40000003);
  }

  public int getTargetActions()
  {
    return this.currentA;
  }

  public Transferable getTransferable()
  {
    return this;
  }

  public DataFlavor[] getTransferDataFlavors()
  {
    Transferable localTransferable = this.local;
    if (localTransferable != null)
      return localTransferable.getTransferDataFlavors();
    return DataTransferer.getInstance().getFlavorsForFormatsAsArray(this.currentT, DataTransferer.adaptFlavorMap(this.currentDT.getFlavorMap()));
  }

  public boolean isDataFlavorSupported(DataFlavor paramDataFlavor)
  {
    Transferable localTransferable = this.local;
    if (localTransferable != null)
      return localTransferable.isDataFlavorSupported(paramDataFlavor);
    return DataTransferer.getInstance().getFlavorsForFormats(this.currentT, DataTransferer.adaptFlavorMap(this.currentDT.getFlavorMap())).containsKey(paramDataFlavor);
  }

  public Object getTransferData(DataFlavor paramDataFlavor)
    throws UnsupportedFlavorException, IOException, InvalidDnDOperationException
  {
    Long localLong = null;
    Transferable localTransferable = this.local;
    if (localTransferable != null)
      return localTransferable.getTransferData(paramDataFlavor);
    if ((this.dropStatus != 2) || (this.dropComplete))
      throw new InvalidDnDOperationException("No drop current");
    Map localMap = DataTransferer.getInstance().getFlavorsForFormats(this.currentT, DataTransferer.adaptFlavorMap(this.currentDT.getFlavorMap()));
    localLong = (Long)localMap.get(paramDataFlavor);
    if (localLong == null)
      throw new UnsupportedFlavorException(paramDataFlavor);
    if ((paramDataFlavor.isRepresentationClassRemote()) && (this.currentDA != 1073741824))
      throw new InvalidDnDOperationException("only ACTION_LINK is permissable for transfer of java.rmi.Remote objects");
    long l = localLong.longValue();
    Object localObject = getNativeData(l);
    if (localObject instanceof byte[])
      try
      {
        return DataTransferer.getInstance().translateBytes((byte[])(byte[])localObject, paramDataFlavor, l, this);
      }
      catch (IOException localIOException1)
      {
        throw new InvalidDnDOperationException(localIOException1.getMessage());
      }
    if (localObject instanceof InputStream)
      try
      {
        return DataTransferer.getInstance().translateStream((InputStream)localObject, paramDataFlavor, l, this);
      }
      catch (IOException localIOException2)
      {
        throw new InvalidDnDOperationException(localIOException2.getMessage());
      }
    throw new IOException("no native data was transfered");
  }

  protected abstract Object getNativeData(long paramLong)
    throws IOException;

  public boolean isTransferableJVMLocal()
  {
    return ((this.local != null) || (getJVMLocalSourceTransferable() != null));
  }

  private int handleEnterMessage(Component paramComponent, int paramInt1, int paramInt2, int paramInt3, int paramInt4, long[] paramArrayOfLong, long paramLong)
  {
    return postDropTargetEvent(paramComponent, paramInt1, paramInt2, paramInt3, paramInt4, paramArrayOfLong, paramLong, 504, true);
  }

  protected void processEnterMessage(SunDropTargetEvent paramSunDropTargetEvent)
  {
    Component localComponent = (Component)paramSunDropTargetEvent.getSource();
    DropTarget localDropTarget = localComponent.getDropTarget();
    Point localPoint = paramSunDropTargetEvent.getPoint();
    this.local = getJVMLocalSourceTransferable();
    if (this.currentDTC != null)
    {
      this.currentDTC.removeNotify();
      this.currentDTC = null;
    }
    if ((localComponent.isShowing()) && (localDropTarget != null) && (localDropTarget.isActive()))
    {
      this.currentDT = localDropTarget;
      this.currentDTC = this.currentDT.getDropTargetContext();
      this.currentDTC.addNotify(this);
      this.currentA = localDropTarget.getDefaultActions();
      try
      {
        localDropTarget.dragEnter(new DropTargetDragEvent(this.currentDTC, localPoint, this.currentDA, this.currentSA));
      }
      catch (Exception localException)
      {
        localException.printStackTrace();
        this.currentDA = 0;
      }
    }
    else
    {
      this.currentDT = null;
      this.currentDTC = null;
      this.currentDA = 0;
      this.currentSA = 0;
      this.currentA = 0;
    }
  }

  private void handleExitMessage(Component paramComponent, long paramLong)
  {
    postDropTargetEvent(paramComponent, 0, 0, 0, 0, null, paramLong, 505, true);
  }

  protected void processExitMessage(SunDropTargetEvent paramSunDropTargetEvent)
  {
    Component localComponent = (Component)paramSunDropTargetEvent.getSource();
    DropTarget localDropTarget = localComponent.getDropTarget();
    DropTargetContext localDropTargetContext = null;
    if (localDropTarget == null)
    {
      this.currentDT = null;
      this.currentT = null;
      if (this.currentDTC != null)
        this.currentDTC.removeNotify();
      this.currentDTC = null;
      return;
    }
    if (localDropTarget != this.currentDT)
    {
      if (this.currentDTC != null)
        this.currentDTC.removeNotify();
      this.currentDT = localDropTarget;
      this.currentDTC = localDropTarget.getDropTargetContext();
      this.currentDTC.addNotify(this);
    }
    localDropTargetContext = this.currentDTC;
    if (localDropTarget.isActive())
      try
      {
        localDropTarget.dragExit(new DropTargetEvent(localDropTargetContext));
      }
      catch (Exception localException)
      {
        localException.printStackTrace();
      }
      finally
      {
        this.currentA = 0;
        this.currentSA = 0;
        this.currentDA = 0;
        this.currentDT = null;
        this.currentT = null;
        this.currentDTC.removeNotify();
        this.currentDTC = null;
        this.local = null;
        this.dragRejected = false;
      }
  }

  private int handleMotionMessage(Component paramComponent, int paramInt1, int paramInt2, int paramInt3, int paramInt4, long[] paramArrayOfLong, long paramLong)
  {
    return postDropTargetEvent(paramComponent, paramInt1, paramInt2, paramInt3, paramInt4, paramArrayOfLong, paramLong, 506, true);
  }

  protected void processMotionMessage(SunDropTargetEvent paramSunDropTargetEvent, boolean paramBoolean)
  {
    Component localComponent = (Component)paramSunDropTargetEvent.getSource();
    Point localPoint = paramSunDropTargetEvent.getPoint();
    int i = paramSunDropTargetEvent.getID();
    DropTarget localDropTarget1 = localComponent.getDropTarget();
    DropTargetContext localDropTargetContext = null;
    if ((localComponent.isShowing()) && (localDropTarget1 != null) && (localDropTarget1.isActive()))
    {
      if (this.currentDT != localDropTarget1)
      {
        if (this.currentDTC != null)
          this.currentDTC.removeNotify();
        this.currentDT = localDropTarget1;
        this.currentDTC = null;
      }
      localDropTargetContext = this.currentDT.getDropTargetContext();
      if (localDropTargetContext != this.currentDTC)
      {
        if (this.currentDTC != null)
          this.currentDTC.removeNotify();
        this.currentDTC = localDropTargetContext;
        this.currentDTC.addNotify(this);
      }
      this.currentA = this.currentDT.getDefaultActions();
      try
      {
        DropTargetDragEvent localDropTargetDragEvent = new DropTargetDragEvent(localDropTargetContext, localPoint, this.currentDA, this.currentSA);
        DropTarget localDropTarget2 = localDropTarget1;
        if (paramBoolean)
          localDropTarget2.dropActionChanged(localDropTargetDragEvent);
        else
          localDropTarget2.dragOver(localDropTargetDragEvent);
        if (this.dragRejected)
          this.currentDA = 0;
      }
      catch (Exception localException)
      {
        localException.printStackTrace();
        this.currentDA = 0;
      }
    }
    else
    {
      this.currentDA = 0;
    }
  }

  private void handleDropMessage(Component paramComponent, int paramInt1, int paramInt2, int paramInt3, int paramInt4, long[] paramArrayOfLong, long paramLong)
  {
    postDropTargetEvent(paramComponent, paramInt1, paramInt2, paramInt3, paramInt4, paramArrayOfLong, paramLong, 502, false);
  }

  protected void processDropMessage(SunDropTargetEvent paramSunDropTargetEvent)
  {
    Component localComponent = (Component)paramSunDropTargetEvent.getSource();
    Point localPoint = paramSunDropTargetEvent.getPoint();
    DropTarget localDropTarget = localComponent.getDropTarget();
    this.dropStatus = 1;
    this.dropComplete = false;
    if ((localComponent.isShowing()) && (localDropTarget != null) && (localDropTarget.isActive()))
    {
      DropTargetContext localDropTargetContext = localDropTarget.getDropTargetContext();
      this.currentDT = localDropTarget;
      if (this.currentDTC != null)
        this.currentDTC.removeNotify();
      this.currentDTC = localDropTargetContext;
      this.currentDTC.addNotify(this);
      this.currentA = localDropTarget.getDefaultActions();
      synchronized (_globalLock)
      {
        if ((this.local = getJVMLocalSourceTransferable()) != null)
          setCurrentJVMLocalSourceTransferable(null);
      }
      try
      {
        localDropTarget.drop(new DropTargetDropEvent(localDropTargetContext, localPoint, this.currentDA, this.currentSA, false));
      }
      finally
      {
        if (this.dropStatus == 1)
          rejectDrop();
        else if (!(this.dropComplete))
          dropComplete(false);
      }
    }
    else
    {
      rejectDrop();
    }
  }

  protected int postDropTargetEvent(Component paramComponent, int paramInt1, int paramInt2, int paramInt3, int paramInt4, long[] paramArrayOfLong, long paramLong, int paramInt5, boolean paramBoolean)
  {
    AppContext localAppContext = SunToolkit.targetToAppContext(paramComponent);
    EventDispatcher localEventDispatcher = new EventDispatcher(this, paramInt3, paramInt4, paramArrayOfLong, paramLong, paramBoolean);
    SunDropTargetEvent localSunDropTargetEvent = new SunDropTargetEvent(paramComponent, paramInt5, paramInt1, paramInt2, localEventDispatcher);
    if (paramBoolean == true)
      DataTransferer.getInstance().getToolkitThreadBlockedHandler().lock();
    SunToolkit.postEvent(localAppContext, localSunDropTargetEvent);
    eventPosted(localSunDropTargetEvent);
    if (paramBoolean == true)
    {
      while (!(localEventDispatcher.isDone()))
        DataTransferer.getInstance().getToolkitThreadBlockedHandler().enter();
      DataTransferer.getInstance().getToolkitThreadBlockedHandler().unlock();
      return localEventDispatcher.getReturnValue();
    }
    return 0;
  }

  public synchronized void acceptDrag(int paramInt)
  {
    if (this.currentDT == null)
      throw new InvalidDnDOperationException("No Drag pending");
    this.currentDA = mapOperation(paramInt);
    if (this.currentDA != 0)
      this.dragRejected = false;
  }

  public synchronized void rejectDrag()
  {
    if (this.currentDT == null)
      throw new InvalidDnDOperationException("No Drag pending");
    this.currentDA = 0;
    this.dragRejected = true;
  }

  public synchronized void acceptDrop(int paramInt)
  {
    if (paramInt == 0)
      throw new IllegalArgumentException("invalid acceptDrop() action");
    if (this.dropStatus != 1)
      throw new InvalidDnDOperationException("invalid acceptDrop()");
    this.currentDA = (this.currentA = mapOperation(paramInt & this.currentSA));
    this.dropStatus = 2;
    this.dropComplete = false;
  }

  public synchronized void rejectDrop()
  {
    if (this.dropStatus != 1)
      throw new InvalidDnDOperationException("invalid rejectDrop()");
    this.dropStatus = -1;
    this.currentDA = 0;
    dropComplete(false);
  }

  private int mapOperation(int paramInt)
  {
    int[] arrayOfInt = { 2, 1, 1073741824 };
    int i = 0;
    for (int j = 0; j < arrayOfInt.length; ++j)
      if ((paramInt & arrayOfInt[j]) == arrayOfInt[j])
      {
        i = arrayOfInt[j];
        break;
      }
    return i;
  }

  public synchronized void dropComplete(boolean paramBoolean)
  {
    if (this.dropStatus == 0)
      throw new InvalidDnDOperationException("No Drop pending");
    if (this.currentDTC != null)
      this.currentDTC.removeNotify();
    this.currentDT = null;
    this.currentDTC = null;
    this.currentT = null;
    this.currentA = 0;
    synchronized (_globalLock)
    {
      currentJVMLocalSourceTransferable = null;
    }
    this.dropStatus = 0;
    this.dropComplete = true;
    try
    {
      doDropDone(paramBoolean, this.currentDA, this.local != null);
    }
    finally
    {
      this.currentDA = 0;
      this.nativeDragContext = 3412047531771691008L;
    }
  }

  protected abstract void doDropDone(boolean paramBoolean1, int paramInt, boolean paramBoolean2);

  protected synchronized long getNativeDragContext()
  {
    return this.nativeDragContext;
  }

  protected void eventPosted(SunDropTargetEvent paramSunDropTargetEvent)
  {
  }

  protected void eventProcessed(SunDropTargetEvent paramSunDropTargetEvent, int paramInt, boolean paramBoolean)
  {
  }

  protected static class EventDispatcher
  {
    private final SunDropTargetContextPeer peer;
    private final int dropAction;
    private final int actions;
    private final long[] formats;
    private long nativeCtxt;
    private final boolean dispatchType;
    private boolean dispatcherDone = false;
    private int returnValue = 0;
    private final HashSet eventSet = new HashSet(3);
    static final ToolkitThreadBlockedHandler handler = DataTransferer.getInstance().getToolkitThreadBlockedHandler();

    EventDispatcher(SunDropTargetContextPeer paramSunDropTargetContextPeer, int paramInt1, int paramInt2, long[] paramArrayOfLong, long paramLong, boolean paramBoolean)
    {
      this.peer = paramSunDropTargetContextPeer;
      this.nativeCtxt = paramLong;
      this.dropAction = paramInt1;
      this.actions = paramInt2;
      this.formats = paramArrayOfLong;
      this.dispatchType = paramBoolean;
    }

    void dispatchEvent(SunDropTargetEvent paramSunDropTargetEvent)
    {
      int i = paramSunDropTargetEvent.getID();
      switch (i)
      {
      case 504:
        dispatchEnterEvent(paramSunDropTargetEvent);
        break;
      case 506:
        dispatchMotionEvent(paramSunDropTargetEvent);
        break;
      case 505:
        dispatchExitEvent(paramSunDropTargetEvent);
        break;
      case 502:
        dispatchDropEvent(paramSunDropTargetEvent);
        break;
      case 503:
      default:
        throw new InvalidDnDOperationException();
      }
    }

    private void dispatchEnterEvent(SunDropTargetEvent paramSunDropTargetEvent)
    {
      synchronized (this.peer)
      {
        SunDropTargetContextPeer.access$002(this.peer, this.dropAction);
        SunDropTargetContextPeer.access$102(this.peer, this.nativeCtxt);
        SunDropTargetContextPeer.access$202(this.peer, this.formats);
        SunDropTargetContextPeer.access$302(this.peer, this.actions);
        SunDropTargetContextPeer.access$402(this.peer, this.dropAction);
        this.peer.dropStatus = 2;
        this.peer.dropComplete = false;
        try
        {
          this.peer.processEnterMessage(paramSunDropTargetEvent);
        }
        finally
        {
          this.peer.dropStatus = 0;
        }
        setReturnValue(SunDropTargetContextPeer.access$400(this.peer));
      }
    }

    private void dispatchMotionEvent(SunDropTargetEvent paramSunDropTargetEvent)
    {
      synchronized (this.peer)
      {
        boolean bool = SunDropTargetContextPeer.access$000(this.peer) != this.dropAction;
        SunDropTargetContextPeer.access$002(this.peer, this.dropAction);
        SunDropTargetContextPeer.access$102(this.peer, this.nativeCtxt);
        SunDropTargetContextPeer.access$202(this.peer, this.formats);
        SunDropTargetContextPeer.access$302(this.peer, this.actions);
        SunDropTargetContextPeer.access$402(this.peer, this.dropAction);
        this.peer.dropStatus = 2;
        this.peer.dropComplete = false;
        try
        {
          this.peer.processMotionMessage(paramSunDropTargetEvent, bool);
        }
        finally
        {
          this.peer.dropStatus = 0;
        }
        setReturnValue(SunDropTargetContextPeer.access$400(this.peer));
      }
    }

    private void dispatchExitEvent(SunDropTargetEvent paramSunDropTargetEvent)
    {
      synchronized (this.peer)
      {
        SunDropTargetContextPeer.access$102(this.peer, this.nativeCtxt);
        this.peer.processExitMessage(paramSunDropTargetEvent);
      }
    }

    private void dispatchDropEvent(SunDropTargetEvent paramSunDropTargetEvent)
    {
      synchronized (this.peer)
      {
        SunDropTargetContextPeer.access$102(this.peer, this.nativeCtxt);
        SunDropTargetContextPeer.access$202(this.peer, this.formats);
        SunDropTargetContextPeer.access$302(this.peer, this.actions);
        SunDropTargetContextPeer.access$402(this.peer, this.dropAction);
        this.peer.processDropMessage(paramSunDropTargetEvent);
      }
    }

    void setReturnValue(int paramInt)
    {
      this.returnValue = paramInt;
    }

    int getReturnValue()
    {
      return this.returnValue;
    }

    boolean isDone()
    {
      return this.eventSet.isEmpty();
    }

    void registerEvent(SunDropTargetEvent paramSunDropTargetEvent)
    {
      handler.lock();
      if (!(this.eventSet.add(paramSunDropTargetEvent)))
        SunDropTargetContextPeer.access$500();
      handler.unlock();
    }

    void unregisterEvent(SunDropTargetEvent paramSunDropTargetEvent)
    {
      handler.lock();
      try
      {
        if (!(this.eventSet.remove(paramSunDropTargetEvent)))
          return;
        if (this.eventSet.isEmpty())
        {
          if ((!(this.dispatcherDone)) && (this.dispatchType == true))
            handler.exit();
          this.dispatcherDone = true;
        }
      }
      finally
      {
        handler.unlock();
      }
      try
      {
        this.peer.eventProcessed(paramSunDropTargetEvent, this.returnValue, this.dispatcherDone);
      }
      finally
      {
        if (this.dispatcherDone)
        {
          this.nativeCtxt = 3412039869550034944L;
          SunDropTargetContextPeer.access$102(this.peer, 3412040659824017408L);
        }
      }
    }

    public void unregisterAllEvents()
    {
      Object[] arrayOfObject = null;
      handler.lock();
      try
      {
        arrayOfObject = this.eventSet.toArray();
      }
      finally
      {
        handler.unlock();
      }
      if (arrayOfObject != null)
        for (int i = 0; i < arrayOfObject.length; ++i)
          unregisterEvent((SunDropTargetEvent)arrayOfObject[i]);
    }
  }
}