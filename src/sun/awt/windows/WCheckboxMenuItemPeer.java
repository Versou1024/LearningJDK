package sun.awt.windows;

import java.awt.CheckboxMenuItem;
import java.awt.event.ItemEvent;
import java.awt.peer.CheckboxMenuItemPeer;

class WCheckboxMenuItemPeer extends WMenuItemPeer
  implements CheckboxMenuItemPeer
{
  public native void setState(boolean paramBoolean);

  WCheckboxMenuItemPeer(CheckboxMenuItem paramCheckboxMenuItem)
  {
    super(paramCheckboxMenuItem);
    this.isCheckbox = true;
    setState(paramCheckboxMenuItem.getState());
  }

  public void handleAction(boolean paramBoolean)
  {
    CheckboxMenuItem localCheckboxMenuItem = (CheckboxMenuItem)this.target;
    WToolkit.executeOnEventHandlerThread(localCheckboxMenuItem, new Runnable(this, localCheckboxMenuItem, paramBoolean)
    {
      public void run()
      {
        this.val$target.setState(this.val$state);
        this.this$0.postEvent(new ItemEvent(this.val$target, 701, this.val$target.getLabel(), 2));
      }
    });
  }
}