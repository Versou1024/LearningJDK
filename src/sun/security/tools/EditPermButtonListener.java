package sun.security.tools;

import java.awt.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

class EditPermButtonListener
  implements ActionListener
{
  private PolicyTool tool;
  private ToolWindow tw;
  private ToolDialog td;
  private boolean editPolicyEntry;

  EditPermButtonListener(PolicyTool paramPolicyTool, ToolWindow paramToolWindow, ToolDialog paramToolDialog, boolean paramBoolean)
  {
    this.tool = paramPolicyTool;
    this.tw = paramToolWindow;
    this.td = paramToolDialog;
    this.editPolicyEntry = paramBoolean;
  }

  public void actionPerformed(ActionEvent paramActionEvent)
  {
    List localList = (List)this.td.getComponent(8);
    int i = localList.getSelectedIndex();
    if (i < 0)
    {
      this.tw.displayErrorDialog(this.td, new Exception(PolicyTool.rb.getString("No permission selected")));
      return;
    }
    this.td.displayPermissionDialog(this.editPolicyEntry, true);
  }
}