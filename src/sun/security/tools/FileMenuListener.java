package sun.security.tools;

import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.text.Collator;
import java.text.MessageFormat;
import java.util.ResourceBundle;

class FileMenuListener
  implements ActionListener
{
  private PolicyTool tool;
  private ToolWindow tw;

  FileMenuListener(PolicyTool paramPolicyTool, ToolWindow paramToolWindow)
  {
    this.tool = paramPolicyTool;
    this.tw = paramToolWindow;
  }

  public void actionPerformed(ActionEvent paramActionEvent)
  {
    Object localObject1;
    if (PolicyTool.collator.compare(paramActionEvent.getActionCommand(), ToolWindow.QUIT) == 0)
    {
      localObject1 = new ToolDialog(PolicyTool.rb.getString("Save Changes"), this.tool, this.tw, true);
      ((ToolDialog)localObject1).displayUserSave(1);
    }
    else if (PolicyTool.collator.compare(paramActionEvent.getActionCommand(), ToolWindow.NEW_POLICY_FILE) == 0)
    {
      localObject1 = new ToolDialog(PolicyTool.rb.getString("Save Changes"), this.tool, this.tw, true);
      ((ToolDialog)localObject1).displayUserSave(2);
    }
    else if (PolicyTool.collator.compare(paramActionEvent.getActionCommand(), ToolWindow.OPEN_POLICY_FILE) == 0)
    {
      localObject1 = new ToolDialog(PolicyTool.rb.getString("Save Changes"), this.tool, this.tw, true);
      ((ToolDialog)localObject1).displayUserSave(3);
    }
    else if (PolicyTool.collator.compare(paramActionEvent.getActionCommand(), ToolWindow.SAVE_POLICY_FILE) == 0)
    {
      Object localObject2;
      localObject1 = ((TextField)this.tw.getComponent(1)).getText();
      if ((localObject1 == null) || (((String)localObject1).length() == 0))
      {
        localObject2 = new ToolDialog(PolicyTool.rb.getString("Save As"), this.tool, this.tw, true);
        ((ToolDialog)localObject2).displaySaveAsDialog(0);
      }
      else
      {
        try
        {
          this.tool.savePolicy((String)localObject1);
          localObject2 = new MessageFormat(PolicyTool.rb.getString("Policy successfully written to filename"));
          Object[] arrayOfObject = { localObject1 };
          this.tw.displayStatusDialog(null, ((MessageFormat)localObject2).format(arrayOfObject));
        }
        catch (FileNotFoundException localFileNotFoundException)
        {
          if ((localObject1 == null) || (((String)localObject1).equals("")))
            this.tw.displayErrorDialog(null, new FileNotFoundException(PolicyTool.rb.getString("null filename")));
          else
            this.tw.displayErrorDialog(null, localFileNotFoundException);
        }
        catch (Exception localException)
        {
          this.tw.displayErrorDialog(null, localException);
        }
      }
    }
    else if (PolicyTool.collator.compare(paramActionEvent.getActionCommand(), ToolWindow.SAVE_AS_POLICY_FILE) == 0)
    {
      localObject1 = new ToolDialog(PolicyTool.rb.getString("Save As"), this.tool, this.tw, true);
      ((ToolDialog)localObject1).displaySaveAsDialog(0);
    }
    else if (PolicyTool.collator.compare(paramActionEvent.getActionCommand(), ToolWindow.VIEW_WARNINGS) == 0)
    {
      this.tw.displayWarningLog(null);
    }
  }
}