package sun.security.tools;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

class PolicyListListener
  implements ActionListener
{
  private PolicyTool tool;
  private ToolWindow tw;

  PolicyListListener(PolicyTool paramPolicyTool, ToolWindow paramToolWindow)
  {
    this.tool = paramPolicyTool;
    this.tw = paramToolWindow;
  }

  public void actionPerformed(ActionEvent paramActionEvent)
  {
    ToolDialog localToolDialog = new ToolDialog(PolicyTool.rb.getString("Policy Entry"), this.tool, this.tw, true);
    localToolDialog.displayPolicyEntryDialog(true);
  }
}