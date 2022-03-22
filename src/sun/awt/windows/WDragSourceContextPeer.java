package sun.awt.windows;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.InvalidDnDOperationException;
import java.awt.event.InputEvent;
import java.util.Map;
import sun.awt.dnd.SunDragSourceContextPeer;

final class WDragSourceContextPeer extends SunDragSourceContextPeer
{
  private static final WDragSourceContextPeer theInstance = new WDragSourceContextPeer(null);

  private WDragSourceContextPeer(DragGestureEvent paramDragGestureEvent)
  {
    super(paramDragGestureEvent);
  }

  static WDragSourceContextPeer createDragSourceContextPeer(DragGestureEvent paramDragGestureEvent)
    throws InvalidDnDOperationException
  {
    theInstance.setTrigger(paramDragGestureEvent);
    return theInstance;
  }

  protected void startDrag(Transferable paramTransferable, long[] paramArrayOfLong, Map paramMap)
  {
    long l = 3412047153814568960L;
    l = createDragSource(getTrigger().getComponent(), paramTransferable, getTrigger().getTriggerEvent(), getTrigger().getSourceAsDragGestureRecognizer().getSourceActions(), paramArrayOfLong, paramMap);
    if (l == 3412046672778231808L)
      throw new InvalidDnDOperationException("failed to create native peer");
    setNativeContext(l);
    WDropTargetContextPeer.setCurrentJVMLocalSourceTransferable(paramTransferable);
    doDragDrop(getNativeContext(), getCursor());
  }

  native long createDragSource(Component paramComponent, Transferable paramTransferable, InputEvent paramInputEvent, int paramInt, long[] paramArrayOfLong, Map paramMap);

  native void doDragDrop(long paramLong, Cursor paramCursor);

  protected native void setNativeCursor(long paramLong, Cursor paramCursor, int paramInt);
}