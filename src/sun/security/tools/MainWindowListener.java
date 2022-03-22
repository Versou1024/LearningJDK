package sun.security.tools;

import java.awt.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.Collator;
import java.util.ResourceBundle;

class MainWindowListener
  implements ActionListener
{
  private PolicyTool tool;
  private ToolWindow tw;

  MainWindowListener(PolicyTool paramPolicyTool, ToolWindow paramToolWindow)
  {
    this.tool = paramPolicyTool;
    this.tw = paramToolWindow;
  }

  public void actionPerformed(ActionEvent paramActionEvent)
  {
    Object localObject;
    if (PolicyTool.collator.compare(paramActionEvent.getActionCommand(), ToolWindow.ADD_POLICY_ENTRY) == 0)
    {
      localObject = new ToolDialog(PolicyTool.rb.getString("Policy Entry"), this.tool, this.tw, true);
      ((ToolDialog)localObject).displayPolicyEntryDialog(false);
    }
    else
    {
      int i;
      ToolDialog localToolDialog;
      if (PolicyTool.collator.compare(paramActionEvent.getActionCommand(), ToolWindow.REMOVE_POLICY_ENTRY) == 0)
      {
        localObject = (List)this.tw.getComponent(3);
        i = ((List)localObject).getSelectedIndex();
        if (i < 0)
        {
          this.tw.displayErrorDialog(null, new Exception(PolicyTool.rb.getString("No Policy Entry selected")));
          return;
        }
        localToolDialog = new ToolDialog(PolicyTool.rb.getString("Remove Policy Entry"), this.tool, this.tw, true);
        localToolDialog.displayConfirmRemovePolicyEntry();
      }
      else if (PolicyTool.collator.compare(paramActionEvent.getActionCommand(), ToolWindow.EDIT_POLICY_ENTRY) == 0)
      {
        localObject = (List)this.tw.getComponent(3);
        i = ((List)localObject).getSelectedIndex();
        if (i < 0)
        {
          this.tw.displayErrorDialog(null, new Exception(PolicyTool.rb.getString("No Policy Entry selected")));
          return;
        }
        localToolDialog = new ToolDialog(PolicyTool.rb.getString("Policy Entry"), this.tool, this.tw, true);
        localToolDialog.displayPolicyEntryDialog(true);
      }
      else if (PolicyTool.collator.compare(paramActionEvent.getActionCommand(), ToolWindow.EDIT_KEYSTORE) == 0)
      {
        localObject = new ToolDialog(PolicyTool.rb.getString("KeyStore"), this.tool, this.tw, true);
        ((ToolDialog)localObject).keyStoreDialog(0);
      }
    }
  }
}