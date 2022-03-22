package sun.applet;

import java.awt.MenuBar;
import java.net.URL;
import java.util.Hashtable;

class StdAppletViewerFactory
  implements AppletViewerFactory
{
  public AppletViewer createAppletViewer(int paramInt1, int paramInt2, URL paramURL, Hashtable paramHashtable)
  {
    return new AppletViewer(paramInt1, paramInt2, paramURL, paramHashtable, System.out, this);
  }

  public MenuBar getBaseMenuBar()
  {
    return new MenuBar();
  }

  public boolean isStandalone()
  {
    return true;
  }
}