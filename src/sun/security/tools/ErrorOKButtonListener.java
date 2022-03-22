package sun.security.tools;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class ErrorOKButtonListener
  implements ActionListener
{
  private ToolDialog ed;

  ErrorOKButtonListener(ToolDialog paramToolDialog)
  {
    this.ed = paramToolDialog;
  }

  public void actionPerformed(ActionEvent paramActionEvent)
  {
    this.ed.setVisible(false);
    this.ed.dispose();
  }
}