package sun.applet;

import java.util.EventListener;

public abstract interface AppletListener extends EventListener
{
  public abstract void appletStateChanged(AppletEvent paramAppletEvent);
}