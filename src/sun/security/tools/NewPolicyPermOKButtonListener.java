package sun.security.tools;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.Vector;
import sun.security.provider.PolicyParser.PermissionEntry;

class NewPolicyPermOKButtonListener
  implements ActionListener
{
  private PolicyTool tool;
  private ToolWindow tw;
  private ToolDialog listDialog;
  private ToolDialog infoDialog;
  private boolean edit;

  NewPolicyPermOKButtonListener(PolicyTool paramPolicyTool, ToolWindow paramToolWindow, ToolDialog paramToolDialog1, ToolDialog paramToolDialog2, boolean paramBoolean)
  {
    this.tool = paramPolicyTool;
    this.tw = paramToolWindow;
    this.listDialog = paramToolDialog1;
    this.infoDialog = paramToolDialog2;
    this.edit = paramBoolean;
  }

  public void actionPerformed(ActionEvent paramActionEvent)
  {
    PolicyParser.PermissionEntry localPermissionEntry;
    try
    {
      localPermissionEntry = this.infoDialog.getPermFromDialog();
      try
      {
        this.tool.verifyPermission(localPermissionEntry.permission, localPermissionEntry.name, localPermissionEntry.action);
      }
      catch (ClassNotFoundException localClassNotFoundException)
      {
        localObject = new MessageFormat(PolicyTool.rb.getString("Warning: Class not found: class"));
        Object[] arrayOfObject = { localPermissionEntry.permission };
        this.tool.warnings.addElement(((MessageFormat)localObject).format(arrayOfObject));
        this.tw.displayStatusDialog(this.infoDialog, ((MessageFormat)localObject).format(arrayOfObject));
      }
      TaggedList localTaggedList = (TaggedList)this.listDialog.getComponent(8);
      Object localObject = ToolDialog.PermissionEntryToUserFriendlyString(localPermissionEntry);
      if (this.edit)
      {
        int i = localTaggedList.getSelectedIndex();
        localTaggedList.replaceTaggedItem((String)localObject, localPermissionEntry, i);
      }
      else
      {
        localTaggedList.addTaggedItem((String)localObject, localPermissionEntry);
      }
      this.infoDialog.dispose();
    }
    catch (InvocationTargetException localInvocationTargetException)
    {
      this.tw.displayErrorDialog(this.infoDialog, localInvocationTargetException.getTargetException());
    }
    catch (Exception localException)
    {
      this.tw.displayErrorDialog(this.infoDialog, localException);
    }
  }
}