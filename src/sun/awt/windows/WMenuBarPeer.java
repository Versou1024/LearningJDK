package sun.awt.windows;

import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.peer.MenuBarPeer;

class WMenuBarPeer extends WMenuPeer
  implements MenuBarPeer
{
  public native void addMenu(Menu paramMenu);

  public native void delMenu(int paramInt);

  public void addHelpMenu(Menu paramMenu)
  {
    addMenu(paramMenu);
  }

  WMenuBarPeer(MenuBar paramMenuBar)
  {
    this.target = paramMenuBar;
    WFramePeer localWFramePeer = (WFramePeer)WToolkit.targetToPeer(paramMenuBar.getParent());
    create(localWFramePeer);
    checkMenuCreation();
  }

  native void create(WFramePeer paramWFramePeer);
}