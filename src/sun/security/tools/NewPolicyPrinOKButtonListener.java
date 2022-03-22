package sun.security.tools;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.Vector;
import sun.security.provider.PolicyParser.PrincipalEntry;

class NewPolicyPrinOKButtonListener
  implements ActionListener
{
  private PolicyTool tool;
  private ToolWindow tw;
  private ToolDialog listDialog;
  private ToolDialog infoDialog;
  private boolean edit;

  NewPolicyPrinOKButtonListener(PolicyTool paramPolicyTool, ToolWindow paramToolWindow, ToolDialog paramToolDialog1, ToolDialog paramToolDialog2, boolean paramBoolean)
  {
    this.tool = paramPolicyTool;
    this.tw = paramToolWindow;
    this.listDialog = paramToolDialog1;
    this.infoDialog = paramToolDialog2;
    this.edit = paramBoolean;
  }

  public void actionPerformed(ActionEvent paramActionEvent)
  {
    PolicyParser.PrincipalEntry localPrincipalEntry;
    try
    {
      localPrincipalEntry = this.infoDialog.getPrinFromDialog();
      if (localPrincipalEntry != null)
      {
        try
        {
          this.tool.verifyPrincipal(localPrincipalEntry.getPrincipalClass(), localPrincipalEntry.getPrincipalName());
        }
        catch (ClassNotFoundException localClassNotFoundException)
        {
          localObject = new MessageFormat(PolicyTool.rb.getString("Warning: Class not found: class"));
          Object[] arrayOfObject = { localPrincipalEntry.getPrincipalClass() };
          this.tool.warnings.addElement(((MessageFormat)localObject).format(arrayOfObject));
          this.tw.displayStatusDialog(this.infoDialog, ((MessageFormat)localObject).format(arrayOfObject));
        }
        TaggedList localTaggedList = (TaggedList)this.listDialog.getComponent(6);
        Object localObject = ToolDialog.PrincipalEntryToUserFriendlyString(localPrincipalEntry);
        if (this.edit)
        {
          int i = localTaggedList.getSelectedIndex();
          localTaggedList.replaceTaggedItem((String)localObject, localPrincipalEntry, i);
        }
        else
        {
          localTaggedList.addTaggedItem((String)localObject, localPrincipalEntry);
        }
      }
      this.infoDialog.dispose();
    }
    catch (Exception localException)
    {
      this.tw.displayErrorDialog(this.infoDialog, localException);
    }
  }
}