package sun.security.tools;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class StatusOKButtonListener
  implements ActionListener
{
  private ToolDialog sd;

  StatusOKButtonListener(ToolDialog paramToolDialog)
  {
    this.sd = paramToolDialog;
  }

  public void actionPerformed(ActionEvent paramActionEvent)
  {
    this.sd.setVisible(false);
    this.sd.dispose();
  }
}