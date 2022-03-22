package sun.security.tools;

import java.awt.Choice;
import java.awt.TextField;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.accessibility.AccessibleContext;

class PermissionActionsMenuListener
  implements ItemListener
{
  private ToolDialog td;

  PermissionActionsMenuListener(ToolDialog paramToolDialog)
  {
    this.td = paramToolDialog;
  }

  public void itemStateChanged(ItemEvent paramItemEvent)
  {
    Choice localChoice = (Choice)this.td.getComponent(5);
    localChoice.getAccessibleContext().setAccessibleName((String)paramItemEvent.getItem());
    if (((String)paramItemEvent.getItem()).indexOf(ToolDialog.PERM_ACTIONS) != -1)
      return;
    TextField localTextField = (TextField)this.td.getComponent(6);
    if ((localTextField.getText() == null) || (localTextField.getText().equals("")))
      localTextField.setText((String)paramItemEvent.getItem());
    else if (localTextField.getText().indexOf((String)paramItemEvent.getItem()) == -1)
      localTextField.setText(localTextField.getText() + ", " + ((String)paramItemEvent.getItem()));
  }
}