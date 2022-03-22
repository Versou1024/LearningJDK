package sun.awt.dnd;

import java.awt.Component;
import java.awt.event.MouseEvent;

public class SunDropTargetEvent extends MouseEvent
{
  public static final int MOUSE_DROPPED = 502;
  private final SunDropTargetContextPeer.EventDispatcher dispatcher;

  public SunDropTargetEvent(Component paramComponent, int paramInt1, int paramInt2, int paramInt3, SunDropTargetContextPeer.EventDispatcher paramEventDispatcher)
  {
    super(paramComponent, paramInt1, System.currentTimeMillis(), 0, paramInt2, paramInt3, 0, 0, 0, false, 0);
    this.dispatcher = paramEventDispatcher;
    this.dispatcher.registerEvent(this);
  }

  public void dispatch()
  {
    try
    {
      this.dispatcher.dispatchEvent(this);
    }
    finally
    {
      this.dispatcher.unregisterEvent(this);
    }
  }

  public void consume()
  {
    boolean bool = isConsumed();
    super.consume();
    if ((!(bool)) && (isConsumed()))
      this.dispatcher.unregisterEvent(this);
  }

  public SunDropTargetContextPeer.EventDispatcher getDispatcher()
  {
    return this.dispatcher;
  }

  public String paramString()
  {
    String str = null;
    switch (this.id)
    {
    case 502:
      str = "MOUSE_DROPPED";
      break;
    default:
      return super.paramString();
    }
    return str + ",(" + getX() + "," + getY() + ")";
  }
}