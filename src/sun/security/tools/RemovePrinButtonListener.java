package sun.security.tools;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

class RemovePrinButtonListener
  implements ActionListener
{
  private PolicyTool tool;
  private ToolWindow tw;
  private ToolDialog td;
  private boolean edit;

  RemovePrinButtonListener(PolicyTool paramPolicyTool, ToolWindow paramToolWindow, ToolDialog paramToolDialog, boolean paramBoolean)
  {
    this.tool = paramPolicyTool;
    this.tw = paramToolWindow;
    this.td = paramToolDialog;
    this.edit = paramBoolean;
  }

  public void actionPerformed(ActionEvent paramActionEvent)
  {
    TaggedList localTaggedList = (TaggedList)this.td.getComponent(6);
    int i = localTaggedList.getSelectedIndex();
    if (i < 0)
    {
      this.tw.displayErrorDialog(this.td, new Exception(PolicyTool.rb.getString("No principal selected")));
      return;
    }
    localTaggedList.removeTaggedItem(i);
  }
}