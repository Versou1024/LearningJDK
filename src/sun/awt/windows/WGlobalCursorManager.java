package sun.awt.windows;

import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Point;
import sun.awt.GlobalCursorManager;

public final class WGlobalCursorManager extends GlobalCursorManager
{
  private static WGlobalCursorManager manager;

  public static GlobalCursorManager getCursorManager()
  {
    if (manager == null)
      manager = new WGlobalCursorManager();
    return manager;
  }

  public static void nativeUpdateCursor(Component paramComponent)
  {
    getCursorManager().updateCursorLater(paramComponent);
  }

  protected native void setCursor(Component paramComponent, Cursor paramCursor, boolean paramBoolean);

  protected native void getCursorPos(Point paramPoint);

  protected native Component findComponentAt(Container paramContainer, int paramInt1, int paramInt2);

  protected native Component findHeavyweightUnderCursor(boolean paramBoolean);

  protected native Point getLocationOnScreen(Component paramComponent);
}