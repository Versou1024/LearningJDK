package sun.security.tools;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class AddPrinButtonListener
  implements ActionListener
{
  private PolicyTool tool;
  private ToolWindow tw;
  private ToolDialog td;
  private boolean editPolicyEntry;

  AddPrinButtonListener(PolicyTool paramPolicyTool, ToolWindow paramToolWindow, ToolDialog paramToolDialog, boolean paramBoolean)
  {
    this.tool = paramPolicyTool;
    this.tw = paramToolWindow;
    this.td = paramToolDialog;
    this.editPolicyEntry = paramBoolean;
  }

  public void actionPerformed(ActionEvent paramActionEvent)
  {
    this.td.displayPrincipalDialog(this.editPolicyEntry, false);
  }
}