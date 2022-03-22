package sun.security.tools;

import java.awt.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class ConfirmRemovePolicyEntryOKButtonListener
  implements ActionListener
{
  private PolicyTool tool;
  private ToolWindow tw;
  private ToolDialog us;

  ConfirmRemovePolicyEntryOKButtonListener(PolicyTool paramPolicyTool, ToolWindow paramToolWindow, ToolDialog paramToolDialog)
  {
    this.tool = paramPolicyTool;
    this.tw = paramToolWindow;
    this.us = paramToolDialog;
  }

  public void actionPerformed(ActionEvent paramActionEvent)
  {
    List localList = (List)this.tw.getComponent(3);
    int i = localList.getSelectedIndex();
    PolicyEntry[] arrayOfPolicyEntry = this.tool.getEntry();
    this.tool.removeEntry(arrayOfPolicyEntry[i]);
    localList = new List(40, false);
    localList.addActionListener(new PolicyListListener(this.tool, this.tw));
    arrayOfPolicyEntry = this.tool.getEntry();
    if (arrayOfPolicyEntry != null)
      for (int j = 0; j < arrayOfPolicyEntry.length; ++j)
        localList.add(arrayOfPolicyEntry[j].headerToString());
    this.tw.replacePolicyList(localList);
    this.us.setVisible(false);
    this.us.dispose();
  }
}