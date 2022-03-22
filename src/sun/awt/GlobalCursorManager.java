package sun.awt;

import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.IllegalComponentStateException;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.InputEvent;
import java.awt.event.InvocationEvent;

public abstract class GlobalCursorManager
{
  private final NativeUpdater nativeUpdater = new NativeUpdater(this);
  private long lastUpdateMillis;
  private final Object lastUpdateLock = new Object();
  private final Object treeLock = new Container().getTreeLock();

  public void updateCursorImmediately()
  {
    synchronized (this.nativeUpdater)
    {
      this.nativeUpdater.pending = false;
    }
    _updateCursor(false);
  }

  public void updateCursorImmediately(InputEvent paramInputEvent)
  {
    int i;
    synchronized (this.lastUpdateLock)
    {
      i = (paramInputEvent.getWhen() >= this.lastUpdateMillis) ? 1 : 0;
    }
    if (i != 0)
      _updateCursor(true);
  }

  public void updateCursorLater(Component paramComponent)
  {
    this.nativeUpdater.postIfNotPending(paramComponent, new InvocationEvent(Toolkit.getDefaultToolkit(), this.nativeUpdater));
  }

  protected abstract void setCursor(Component paramComponent, Cursor paramCursor, boolean paramBoolean);

  protected abstract void getCursorPos(Point paramPoint);

  protected abstract Component findComponentAt(Container paramContainer, int paramInt1, int paramInt2);

  protected abstract Point getLocationOnScreen(Component paramComponent);

  protected abstract Component findHeavyweightUnderCursor(boolean paramBoolean);

  private void _updateCursor(boolean paramBoolean)
  {
    synchronized (this.lastUpdateLock)
    {
      this.lastUpdateMillis = System.currentTimeMillis();
    }
    ??? = null;
    Point localPoint = null;
    try
    {
      Object localObject3 = findHeavyweightUnderCursor(paramBoolean);
      if (localObject3 == null)
      {
        updateCursorOutOfJava();
        return;
      }
      if (localObject3 instanceof Window)
        localPoint = ((Component)localObject3).getLocation();
      else if (localObject3 instanceof Container)
        localPoint = getLocationOnScreen((Component)localObject3);
      if (localPoint != null)
      {
        ??? = new Point();
        getCursorPos((Point)???);
        Component localComponent = findComponentAt((Container)localObject3, ((Point)???).x - localPoint.x, ((Point)???).y - localPoint.y);
        if (localComponent != null)
          localObject3 = localComponent;
      }
      setCursor((Component)localObject3, ((Component)localObject3).getCursor(), paramBoolean);
    }
    catch (IllegalComponentStateException localIllegalComponentStateException)
    {
    }
  }

  protected void updateCursorOutOfJava()
  {
  }

  class NativeUpdater
  implements Runnable
  {
    boolean pending = false;

    public void run()
    {
      int i = 0;
      synchronized (this)
      {
        if (this.pending)
        {
          this.pending = false;
          i = 1;
        }
      }
      if (i != 0)
        GlobalCursorManager.access$000(this.this$0, false);
    }

    public void postIfNotPending(, InvocationEvent paramInvocationEvent)
    {
      int i = 0;
      synchronized (this)
      {
        if (!(this.pending))
          this.pending = (i = 1);
      }
      if (i != 0)
        SunToolkit.postEvent(SunToolkit.targetToAppContext(paramComponent), paramInvocationEvent);
    }
  }
}