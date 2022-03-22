package sun.security.tools;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

class ChildWindowListener
  implements WindowListener
{
  private ToolDialog td;

  ChildWindowListener(ToolDialog paramToolDialog)
  {
    this.td = paramToolDialog;
  }

  public void windowOpened(WindowEvent paramWindowEvent)
  {
  }

  public void windowClosing(WindowEvent paramWindowEvent)
  {
    this.td.setVisible(false);
    this.td.dispose();
  }

  public void windowClosed(WindowEvent paramWindowEvent)
  {
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