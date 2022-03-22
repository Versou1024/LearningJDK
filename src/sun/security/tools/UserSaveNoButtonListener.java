package sun.security.tools;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class UserSaveNoButtonListener
  implements ActionListener
{
  private PolicyTool tool;
  private ToolWindow tw;
  private ToolDialog us;
  private int select;

  UserSaveNoButtonListener(ToolDialog paramToolDialog, PolicyTool paramPolicyTool, ToolWindow paramToolWindow, int paramInt)
  {
    this.us = paramToolDialog;
    this.tool = paramPolicyTool;
    this.tw = paramToolWindow;
    this.select = paramInt;
  }

  public void actionPerformed(ActionEvent paramActionEvent)
  {
    this.us.setVisible(false);
    this.us.dispose();
    this.us.userSaveContinue(this.tool, this.tw, this.us, this.select);
  }
}