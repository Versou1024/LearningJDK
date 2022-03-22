package sun.security.tools;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

class ToolWindowListener
  implements WindowListener
{
  private ToolWindow tw;

  ToolWindowListener(ToolWindow paramToolWindow)
  {
    this.tw = paramToolWindow;
  }

  public void windowOpened(WindowEvent paramWindowEvent)
  {
  }

  public void windowClosing(WindowEvent paramWindowEvent)
  {
    this.tw.setVisible(false);
    this.tw.dispose();
    System.exit(0);
  }

  public void windowClosed(WindowEvent paramWindowEvent)
  {
    System.exit(0);
  }

  public void windowIconified(WindowEvent paramWindowEvent)
  {
  }

  public void windowDeiconified(WindowEvent paramWindowEvent)
  {
  }

  public void windowActivated(WindowEvent paramWindowEvent)
  {
  }

  public void windowDeactivated(WindowEvent paramWindowEvent)
  {
  }
}