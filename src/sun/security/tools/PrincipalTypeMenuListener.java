package sun.security.tools;

import java.awt.Choice;
import java.awt.TextField;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.accessibility.AccessibleContext;

class PrincipalTypeMenuListener
  implements ItemListener
{
  private ToolDialog td;

  PrincipalTypeMenuListener(ToolDialog paramToolDialog)
  {
    this.td = paramToolDialog;
  }

  public void itemStateChanged(ItemEvent paramItemEvent)
  {
    Choice localChoice = (Choice)this.td.getComponent(1);
    TextField localTextField1 = (TextField)this.td.getComponent(2);
    TextField localTextField2 = (TextField)this.td.getComponent(4);
    localChoice.getAccessibleContext().setAccessibleName(PolicyTool.splitToWords((String)paramItemEvent.getItem()));
    if (((String)paramItemEvent.getItem()).equals(ToolDialog.PRIN_TYPE))
    {
      if ((localTextField1.getText() != null) && (localTextField1.getText().length() > 0))
      {
        localPrin = ToolDialog.getPrin(localTextField1.getText(), true);
        localChoice.select(localPrin.CLASS);
      }
      return;
    }
    if (localTextField1.getText().indexOf((String)paramItemEvent.getItem()) == -1)
      localTextField2.setText("");
    Prin localPrin = ToolDialog.getPrin((String)paramItemEvent.getItem(), false);
    if (localPrin != null)
      localTextField1.setText(localPrin.FULL_CLASS);
  }
}