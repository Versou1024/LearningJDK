package sun.awt.windows;

import java.awt.Component;
import java.awt.Container;
import java.awt.Event;
import java.awt.MenuComponent;
import java.awt.MenuContainer;
import java.awt.Point;
import java.awt.PopupMenu;
import java.awt.peer.PopupMenuPeer;
import java.lang.reflect.Field;

public class WPopupMenuPeer extends WMenuPeer
  implements PopupMenuPeer
{
  private static Field f_parent = WToolkit.getField(MenuComponent.class, "parent");
  private static Field f_isTrayIconPopup = WToolkit.getField(PopupMenu.class, "isTrayIconPopup");

  public WPopupMenuPeer(PopupMenu paramPopupMenu)
  {
    this.target = paramPopupMenu;
    Object localObject = null;
    boolean bool = false;
    try
    {
      bool = ((Boolean)f_isTrayIconPopup.get(paramPopupMenu)).booleanValue();
      if (bool)
        localObject = (MenuContainer)f_parent.get(paramPopupMenu);
      else
        localObject = paramPopupMenu.getParent();
    }
    catch (IllegalAccessException localIllegalAccessException)
    {
      localIllegalAccessException.printStackTrace();
      return;
    }
    if (localObject instanceof Component)
    {
      WComponentPeer localWComponentPeer = (WComponentPeer)WToolkit.targetToPeer(localObject);
      if (localWComponentPeer == null)
      {
        localObject = WToolkit.getNativeContainer((Component)localObject);
        localWComponentPeer = (WComponentPeer)WToolkit.targetToPeer(localObject);
      }
      createMenu(localWComponentPeer);
      checkMenuCreation();
    }
    else
    {
      throw new IllegalArgumentException("illegal popup menu container class");
    }
  }

  native void createMenu(WComponentPeer paramWComponentPeer);

  public void show(Event paramEvent)
  {
    Component localComponent = (Component)paramEvent.target;
    WComponentPeer localWComponentPeer = (WComponentPeer)WToolkit.targetToPeer(localComponent);
    if (localWComponentPeer == null)
    {
      Container localContainer = WToolkit.getNativeContainer(localComponent);
      paramEvent.target = localContainer;
      for (Object localObject = localComponent; localObject != localContainer; localObject = ((Component)localObject).getParent())
      {
        Point localPoint = ((Component)localObject).getLocation();
        paramEvent.x += localPoint.x;
        paramEvent.y += localPoint.y;
      }
    }
    _show(paramEvent);
  }

  void show(Component paramComponent, Point paramPoint)
  {
    WComponentPeer localWComponentPeer = (WComponentPeer)WToolkit.targetToPeer(paramComponent);
    Event localEvent = new Event(paramComponent, 3412048098707374080L, 501, paramPoint.x, paramPoint.y, 0, 0);
    if (localWComponentPeer == null)
    {
      Container localContainer = WToolkit.getNativeContainer(paramComponent);
      localEvent.target = localContainer;
    }
    localEvent.x = paramPoint.x;
    localEvent.y = paramPoint.y;
    _show(localEvent);
  }

  public native void _show(Event paramEvent);
}