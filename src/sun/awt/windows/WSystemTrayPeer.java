package sun.awt.windows;

import java.awt.Dimension;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.peer.SystemTrayPeer;

public class WSystemTrayPeer extends WObjectPeer
  implements SystemTrayPeer
{
  WSystemTrayPeer(SystemTray paramSystemTray)
  {
    this.target = paramSystemTray;
  }

  public Dimension getTrayIconSize()
  {
    return new Dimension(16, 16);
  }

  public boolean isSupported()
  {
    return ((WToolkit)Toolkit.getDefaultToolkit()).isTraySupported();
  }

  protected void disposeImpl()
  {
  }
}