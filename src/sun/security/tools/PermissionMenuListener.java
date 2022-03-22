package sun.security.tools;

import java.awt.Choice;
import java.awt.TextField;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.Collator;
import javax.accessibility.AccessibleContext;

class PermissionMenuListener
  implements ItemListener
{
  private ToolDialog td;

  PermissionMenuListener(ToolDialog paramToolDialog)
  {
    this.td = paramToolDialog;
  }

  public void itemStateChanged(ItemEvent paramItemEvent)
  {
    Choice localChoice1 = (Choice)this.td.getComponent(1);
    Choice localChoice2 = (Choice)this.td.getComponent(3);
    Choice localChoice3 = (Choice)this.td.getComponent(5);
    TextField localTextField1 = (TextField)this.td.getComponent(4);
    TextField localTextField2 = (TextField)this.td.getComponent(6);
    TextField localTextField3 = (TextField)this.td.getComponent(2);
    TextField localTextField4 = (TextField)this.td.getComponent(8);
    localChoice1.getAccessibleContext().setAccessibleName(PolicyTool.splitToWords((String)paramItemEvent.getItem()));
    if (PolicyTool.collator.compare((String)paramItemEvent.getItem(), ToolDialog.PERM) == 0)
    {
      if ((localTextField3.getText() != null) && (localTextField3.getText().length() > 0))
      {
        localPerm = ToolDialog.getPerm(localTextField3.getText(), true);
        if (localPerm != null)
          localChoice1.select(localPerm.CLASS);
      }
      return;
    }
    if (localTextField3.getText().indexOf((String)paramItemEvent.getItem()) == -1)
    {
      localTextField1.setText("");
      localTextField2.setText("");
      localTextField4.setText("");
    }
    Perm localPerm = ToolDialog.getPerm((String)paramItemEvent.getItem(), false);
    if (localPerm == null)
      localTextField3.setText("");
    else
      localTextField3.setText(localPerm.FULL_CLASS);
    this.td.setPermissionNames(localPerm, localChoice2, localTextField1);
    this.td.setPermissionActions(localPerm, localChoice3, localTextField2);
  }
}