package sun.security.tools;

import java.awt.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.Collator;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.Vector;
import sun.security.provider.PolicyParser.GrantEntry;

class AddEntryDoneButtonListener
  implements ActionListener
{
  private PolicyTool tool;
  private ToolWindow tw;
  private ToolDialog td;
  private boolean edit;

  AddEntryDoneButtonListener(PolicyTool paramPolicyTool, ToolWindow paramToolWindow, ToolDialog paramToolDialog, boolean paramBoolean)
  {
    this.tool = paramPolicyTool;
    this.tw = paramToolWindow;
    this.td = paramToolDialog;
    this.edit = paramBoolean;
  }

  public void actionPerformed(ActionEvent paramActionEvent)
  {
    PolicyEntry localPolicyEntry;
    try
    {
      int i;
      Object localObject2;
      localPolicyEntry = this.td.getPolicyEntryFromDialog();
      PolicyParser.GrantEntry localGrantEntry = localPolicyEntry.getGrantEntry();
      if (localGrantEntry.signedBy != null)
      {
        localObject1 = this.tool.parseSigners(localGrantEntry.signedBy);
        for (i = 0; i < localObject1.length; ++i)
        {
          localObject2 = this.tool.getPublicKeyAlias(localObject1[i]);
          if (localObject2 == null)
          {
            MessageFormat localMessageFormat = new MessageFormat(PolicyTool.rb.getString("Warning: A public key for alias 'signers[i]' does not exist.  Make sure a KeyStore is properly configured."));
            Object[] arrayOfObject = { localObject1[i] };
            this.tool.warnings.addElement(localMessageFormat.format(arrayOfObject));
            this.tw.displayStatusDialog(this.td, localMessageFormat.format(arrayOfObject));
          }
        }
      }
      Object localObject1 = (List)this.tw.getComponent(3);
      if (this.edit)
      {
        i = ((List)localObject1).getSelectedIndex();
        this.tool.addEntry(localPolicyEntry, i);
        localObject2 = localPolicyEntry.headerToString();
        if (PolicyTool.collator.compare((String)localObject2, ((List)localObject1).getItem(i)) != 0)
          this.tool.modified = true;
        ((List)localObject1).replaceItem((String)localObject2, i);
      }
      else
      {
        this.tool.addEntry(localPolicyEntry, -1);
        ((List)localObject1).add(localPolicyEntry.headerToString());
        this.tool.modified = true;
      }
      this.td.setVisible(false);
      this.td.dispose();
    }
    catch (Exception localException)
    {
      this.tw.displayErrorDialog(this.td, localException);
    }
  }
}