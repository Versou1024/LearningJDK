package sun.security.tools;

import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.ResourceBundle;

class ChangeKeyStoreOKButtonListener
  implements ActionListener
{
  private PolicyTool tool;
  private ToolWindow tw;
  private ToolDialog td;

  ChangeKeyStoreOKButtonListener(PolicyTool paramPolicyTool, ToolWindow paramToolWindow, ToolDialog paramToolDialog)
  {
    this.tool = paramPolicyTool;
    this.tw = paramToolWindow;
    this.td = paramToolDialog;
  }

  public void actionPerformed(ActionEvent paramActionEvent)
  {
    String str1 = ((TextField)this.td.getComponent(1)).getText().trim();
    String str2 = ((TextField)this.td.getComponent(3)).getText().trim();
    String str3 = ((TextField)this.td.getComponent(5)).getText().trim();
    String str4 = ((TextField)this.td.getComponent(7)).getText().trim();
    try
    {
      this.tool.openKeyStore((str1.length() == 0) ? null : str1, (str2.length() == 0) ? null : str2, (str3.length() == 0) ? null : str3, (str4.length() == 0) ? null : str4);
      this.tool.modified = true;
    }
    catch (Exception localException)
    {
      MessageFormat localMessageFormat = new MessageFormat(PolicyTool.rb.getString("Unable to open KeyStore: ex.toString()"));
      Object[] arrayOfObject = { localException.toString() };
      this.tw.displayErrorDialog(this.td, localMessageFormat.format(arrayOfObject));
      return;
    }
    this.td.dispose();
  }
}