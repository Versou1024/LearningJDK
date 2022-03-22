package sun.security.tools;

import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.text.MessageFormat;
import java.util.ResourceBundle;

class OverWriteFileOKButtonListener
  implements ActionListener
{
  private PolicyTool tool;
  private ToolWindow tw;
  private ToolDialog td;
  private String filename;
  private int nextEvent;

  OverWriteFileOKButtonListener(PolicyTool paramPolicyTool, ToolWindow paramToolWindow, ToolDialog paramToolDialog, String paramString, int paramInt)
  {
    this.tool = paramPolicyTool;
    this.tw = paramToolWindow;
    this.td = paramToolDialog;
    this.filename = paramString;
    this.nextEvent = paramInt;
  }

  public void actionPerformed(ActionEvent paramActionEvent)
  {
    try
    {
      this.tool.savePolicy(this.filename);
      MessageFormat localMessageFormat = new MessageFormat(PolicyTool.rb.getString("Policy successfully written to filename"));
      Object[] arrayOfObject = { this.filename };
      this.tw.displayStatusDialog(null, localMessageFormat.format(arrayOfObject));
      TextField localTextField = (TextField)this.tw.getComponent(1);
      localTextField.setText(this.filename);
      this.tw.setVisible(true);
      this.td.setVisible(false);
      this.td.dispose();
      this.td.userSaveContinue(this.tool, this.tw, this.td, this.nextEvent);
    }
    catch (FileNotFoundException localFileNotFoundException)
    {
      if ((this.filename == null) || (this.filename.equals("")))
        this.tw.displayErrorDialog(null, new FileNotFoundException(PolicyTool.rb.getString("null filename")));
      else
        this.tw.displayErrorDialog(null, localFileNotFoundException);
      this.td.setVisible(false);
      this.td.dispose();
    }
    catch (Exception localException)
    {
      this.tw.displayErrorDialog(null, localException);
      this.td.setVisible(false);
      this.td.dispose();
    }
  }
}