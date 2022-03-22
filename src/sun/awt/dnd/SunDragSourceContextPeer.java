package sun.awt.dnd;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.Point;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceContext;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.InvalidDnDOperationException;
import java.awt.dnd.peer.DragSourceContextPeer;
import java.awt.event.MouseEvent;
import java.util.Map;
import java.util.SortedMap;
import sun.awt.SunToolkit;
import sun.awt.datatransfer.DataTransferer;

public abstract class SunDragSourceContextPeer
  implements DragSourceContextPeer
{
  private DragGestureEvent trigger;
  private Component component;
  private Cursor cursor;
  private long nativeCtxt;
  private DragSourceContext dragSourceContext;
  private int sourceActions;
  private static boolean dragDropInProgress = false;
  private static boolean discardingMouseEvents = false;
  protected static final int DISPATCH_ENTER = 1;
  protected static final int DISPATCH_MOTION = 2;
  protected static final int DISPATCH_CHANGED = 3;
  protected static final int DISPATCH_EXIT = 4;
  protected static final int DISPATCH_FINISH = 5;
  protected static final int DISPATCH_MOUSE_MOVED = 6;

  public SunDragSourceContextPeer(DragGestureEvent paramDragGestureEvent)
  {
    this.trigger = paramDragGestureEvent;
    if (this.trigger != null)
      this.component = this.trigger.getComponent();
    else
      this.component = null;
  }

  public void startDrag(DragSourceContext paramDragSourceContext, Cursor paramCursor, Image paramImage, Point paramPoint)
    throws InvalidDnDOperationException
  {
    if (getTrigger().getTriggerEvent() == null)
      throw new InvalidDnDOperationException("DragGestureEvent has a null trigger");
    this.dragSourceContext = paramDragSourceContext;
    this.cursor = paramCursor;
    this.sourceActions = getDragSourceContext().getSourceActions();
    Transferable localTransferable = getDragSourceContext().getTransferable();
    SortedMap localSortedMap = DataTransferer.getInstance().getFormatsForTransferable(localTransferable, DataTransferer.adaptFlavorMap(getTrigger().getDragSource().getFlavorMap()));
    DataTransferer.getInstance();
    long[] arrayOfLong = DataTransferer.keysToLongArray(localSortedMap);
    startDrag(localTransferable, arrayOfLong, localSortedMap);
    discardingMouseEvents = true;
    EventQueue.invokeLater(new Runnable(this)
    {
      public void run()
      {
        SunDragSourceContextPeer.access$002(false);
      }
    });
  }

  protected abstract void startDrag(Transferable paramTransferable, long[] paramArrayOfLong, Map paramMap);

  public void setCursor(Cursor paramCursor)
    throws InvalidDnDOperationException
  {
    synchronized (this)
    {
      if ((this.cursor == null) || (!(this.cursor.equals(paramCursor))))
      {
        this.cursor = paramCursor;
        setNativeCursor(getNativeContext(), paramCursor, (paramCursor != null) ? paramCursor.getType() : 0);
      }
    }
  }

  public Cursor getCursor()
  {
    return this.cursor;
  }

  protected abstract void setNativeCursor(long paramLong, Cursor paramCursor, int paramInt);

  protected synchronized void setTrigger(DragGestureEvent paramDragGestureEvent)
  {
    this.trigger = paramDragGestureEvent;
    if (this.trigger != null)
      this.component = this.trigger.getComponent();
    else
      this.component = null;
  }

  protected DragGestureEvent getTrigger()
  {
    return this.trigger;
  }

  protected Component getComponent()
  {
    return this.component;
  }

  protected synchronized void setNativeContext(long paramLong)
  {
    this.nativeCtxt = paramLong;
  }

  protected synchronized long getNativeContext()
  {
    return this.nativeCtxt;
  }

  protected DragSourceContext getDragSourceContext()
  {
    return this.dragSourceContext;
  }

  public void transferablesFlavorsChanged()
  {
  }

  protected final void postDragSourceDragEvent(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5)
  {
    int i = convertModifiersToDropAction(paramInt2, this.sourceActions);
    DragSourceDragEvent localDragSourceDragEvent = new DragSourceDragEvent(getDragSourceContext(), i, paramInt1 & this.sourceActions, paramInt2, paramInt3, paramInt4);
    EventDispatcher localEventDispatcher = new EventDispatcher(this, paramInt5, localDragSourceDragEvent);
    SunToolkit.executeOnEventHandlerThread(getComponent(), localEventDispatcher);
  }

  private void dragEnter(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    postDragSourceDragEvent(paramInt1, paramInt2, paramInt3, paramInt4, 1);
  }

  private void dragMotion(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    postDragSourceDragEvent(paramInt1, paramInt2, paramInt3, paramInt4, 2);
  }

  private void operationChanged(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    postDragSourceDragEvent(paramInt1, paramInt2, paramInt3, paramInt4, 3);
  }

  protected final void dragExit(int paramInt1, int paramInt2)
  {
    DragSourceEvent localDragSourceEvent = new DragSourceEvent(getDragSourceContext(), paramInt1, paramInt2);
    EventDispatcher localEventDispatcher = new EventDispatcher(this, 4, localDragSourceEvent);
    SunToolkit.executeOnEventHandlerThread(getComponent(), localEventDispatcher);
  }

  private void dragMouseMoved(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    postDragSourceDragEvent(paramInt1, paramInt2, paramInt3, paramInt4, 6);
  }

  protected final void dragDropFinished(boolean paramBoolean, int paramInt1, int paramInt2, int paramInt3)
  {
    DragSourceDropEvent localDragSourceDropEvent = new DragSourceDropEvent(getDragSourceContext(), paramInt1 & this.sourceActions, paramBoolean, paramInt2, paramInt3);
    EventDispatcher localEventDispatcher = new EventDispatcher(this, 5, localDragSourceDropEvent);
    SunToolkit.executeOnEventHandlerThread(getComponent(), localEventDispatcher);
    setNativeContext(3412047308433391616L);
  }

  public static void setDragDropInProgress(boolean paramBoolean)
    throws InvalidDnDOperationException
  {
    if (dragDropInProgress == paramBoolean)
      throw new InvalidDnDOperationException(getExceptionMessage(paramBoolean));
    synchronized (SunDragSourceContextPeer.class)
    {
      if (dragDropInProgress == paramBoolean)
        throw new InvalidDnDOperationException(getExceptionMessage(paramBoolean));
      dragDropInProgress = paramBoolean;
    }
  }

  public static boolean checkEvent(AWTEvent paramAWTEvent)
  {
    if ((!(discardingMouseEvents)) || (!(paramAWTEvent instanceof MouseEvent)))
      break label27;
    MouseEvent localMouseEvent = (MouseEvent)paramAWTEvent;
    label27: return (localMouseEvent instanceof SunDropTargetEvent);
  }

  public static void checkDragDropInProgress()
    throws InvalidDnDOperationException
  {
    if (dragDropInProgress)
      throw new InvalidDnDOperationException(getExceptionMessage(true));
  }

  private static String getExceptionMessage(boolean paramBoolean)
  {
    return ((paramBoolean) ? "Drag and drop in progress" : "No drag in progress");
  }

  public static int convertModifiersToDropAction(int paramInt1, int paramInt2)
  {
    int i = 0;
    switch (paramInt1 & 0xC0)
    {
    case 192:
      i = 1073741824;
      break;
    case 128:
      i = 1;
      break;
    case 64:
      i = 2;
      break;
    default:
      if ((paramInt2 & 0x2) != 0)
      {
        i = 2;
        break label88:
      }
      if ((paramInt2 & 0x1) != 0)
      {
        i = 1;
        break label88:
      }
      if ((paramInt2 & 0x40000000) == 0)
        break label88;
      i = 1073741824;
    }
    label88: return (i & paramInt2);
  }

  private void cleanup()
  {
    this.trigger = null;
    this.component = null;
    this.cursor = null;
    this.dragSourceContext = null;
    SunDropTargetContextPeer.setCurrentJVMLocalSourceTransferable(null);
    setDragDropInProgress(false);
  }

  private class EventDispatcher
  implements Runnable
  {
    private final int dispatchType;
    private final DragSourceEvent event;

    EventDispatcher(, int paramInt, DragSourceEvent paramDragSourceEvent)
    {
      switch (paramInt)
      {
      case 1:
      case 2:
      case 3:
      case 6:
        if (!(paramDragSourceEvent instanceof DragSourceDragEvent))
          throw new IllegalArgumentException("Event: " + paramDragSourceEvent);
      case 4:
        break;
      case 5:
        if (!(paramDragSourceEvent instanceof DragSourceDropEvent))
          throw new IllegalArgumentException("Event: " + paramDragSourceEvent);
      default:
        throw new IllegalArgumentException("Dispatch type: " + paramInt);
      }
      this.dispatchType = paramInt;
      this.event = paramDragSourceEvent;
    }

    public void run()
    {
      DragSourceContext localDragSourceContext = this.this$0.getDragSourceContext();
      switch (this.dispatchType)
      {
      case 1:
        localDragSourceContext.dragEnter((DragSourceDragEvent)this.event);
        break;
      case 2:
        localDragSourceContext.dragOver((DragSourceDragEvent)this.event);
        break;
      case 3:
        localDragSourceContext.dropActionChanged((DragSourceDragEvent)this.event);
        break;
      case 4:
        localDragSourceContext.dragExit(this.event);
        break;
      case 6:
        localDragSourceContext.dragMouseMoved((DragSourceDragEvent)this.event);
        break;
      case 5:
        try
        {
          localDragSourceContext.dragDropEnd((DragSourceDropEvent)this.event);
        }
        finally
        {
          SunDragSourceContextPeer.access$100(this.this$0);
        }
        break;
      default:
        throw new IllegalStateException("Dispatch type: " + this.dispatchType);
      }
    }
  }
}