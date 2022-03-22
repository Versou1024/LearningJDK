package sun.awt.windows;

import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuContainer;
import java.awt.MenuItem;
import java.awt.peer.MenuPeer;

class WMenuPeer extends WMenuItemPeer
  implements MenuPeer
{
  public native void addSeparator();

  public void addItem(MenuItem paramMenuItem)
  {
    WMenuItemPeer localWMenuItemPeer = (WMenuItemPeer)WToolkit.targetToPeer(paramMenuItem);
  }

  public native void delItem(int paramInt);

  WMenuPeer()
  {
  }

  WMenuPeer(Menu paramMenu)
  {
    this.target = paramMenu;
    MenuContainer localMenuContainer = paramMenu.getParent();
    if (localMenuContainer instanceof MenuBar)
    {
      WMenuBarPeer localWMenuBarPeer = (WMenuBarPeer)WToolkit.targetToPeer(localMenuContainer);
      this.parent = localWMenuBarPeer;
      createMenu(localWMenuBarPeer);
    }
    else if (localMenuContainer instanceof Menu)
    {
      this.parent = ((WMenuPeer)WToolkit.targetToPeer(localMenuContainer));
      createSubMenu(this.parent);
    }
    else
    {
      throw new IllegalArgumentException("unknown menu container class");
    }
    checkMenuCreation();
  }

  native void createMenu(WMenuBarPeer paramWMenuBarPeer);

  native void createSubMenu(WMenuPeer paramWMenuPeer);
}