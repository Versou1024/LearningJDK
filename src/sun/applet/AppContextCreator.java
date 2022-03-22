package sun.applet;

import sun.awt.AppContext;
import sun.awt.SunToolkit;

class AppContextCreator extends Thread
{
  Object syncObject = new Object();
  AppContext appContext = null;

  AppContextCreator(ThreadGroup paramThreadGroup)
  {
    super(paramThreadGroup, "AppContextCreator");
  }

  public void run()
  {
    synchronized (this.syncObject)
    {
      this.appContext = SunToolkit.createNewAppContext();
      this.syncObject.notifyAll();
    }
  }
}