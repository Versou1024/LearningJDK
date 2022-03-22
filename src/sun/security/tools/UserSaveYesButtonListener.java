package sun.security.tools;

import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.ResourceBundle;

class UserSaveYesButtonListener
  implements ActionListener
{
  private ToolDialog us;
  private PolicyTool tool;
  private ToolWindow tw;
  private int select;

  UserSaveYesButtonListener(ToolDialog paramToolDialog, PolicyTool paramPolicyTool, ToolWindow paramToolWindow, int paramInt)
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
    try
    {
      String str = ((TextField)this.tw.getComponent(1)).getText();
      if ((str == null) || (str.equals("")))
      {
        this.us.displaySaveAsDialog(this.select);
      }
      else
      {
        this.tool.savePolicy(str);
        MessageFormat localMessageFormat = new MessageFormat(PolicyTool.rb.getString("Policy successfully written to filename"));
        Object[] arrayOfObject = { str };
        this.tw.displayStatusDialog(null, localMessageFormat.format(arrayOfObject));
        this.us.userSaveContinue(this.tool, this.tw, this.us, this.select);
      }
    }
    catch (Exception localException)
    {
      this.tw.displayErrorDialog(null, localException);
    }
  }
}