package sun.security.tools;

import java.awt.Choice;
import java.awt.TextField;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.accessibility.AccessibleContext;

class PermissionNameMenuListener
  implements ItemListener
{
  private ToolDialog td;

  PermissionNameMenuListener(ToolDialog paramToolDialog)
  {
    this.td = paramToolDialog;
  }

  public void itemStateChanged(ItemEvent paramItemEvent)
  {
    Choice localChoice = (Choice)this.td.getComponent(3);
    localChoice.getAccessibleContext().setAccessibleName(PolicyTool.splitToWords((String)paramItemEvent.getItem()));
    if (((String)paramItemEvent.getItem()).indexOf(ToolDialog.PERM_NAME) != -1)
      return;
    TextField localTextField = (TextField)this.td.getComponent(4);
    localTextField.setText((String)paramItemEvent.getItem());
  }
}