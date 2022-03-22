package sun.awt;

import java.awt.Point;
import java.awt.Window;
import java.awt.peer.MouseInfoPeer;

public class DefaultMouseInfoPeer
  implements MouseInfoPeer
{
  public native int fillPointWithCoords(Point paramPoint);

  public native boolean isWindowUnderMouse(Window paramWindow);
}