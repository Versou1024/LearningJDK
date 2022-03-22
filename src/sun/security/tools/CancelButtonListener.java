package sun.security.tools;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class CancelButtonListener
  implements ActionListener
{
  private ToolDialog td;

  CancelButtonListener(ToolDialog paramToolDialog)
  {
    this.td = paramToolDialog;
  }

  public void actionPerformed(ActionEvent paramActionEvent)
  {
    this.td.setVisible(false);
    this.td.dispose();
  }
}