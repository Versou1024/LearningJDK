package sun.security.tools;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class UserSaveCancelButtonListener
  implements ActionListener
{
  private ToolDialog us;

  UserSaveCancelButtonListener(ToolDialog paramToolDialog)
  {
    this.us = paramToolDialog;
  }

  public void actionPerformed(ActionEvent paramActionEvent)
  {
    this.us.setVisible(false);
    this.us.dispose();
  }
}