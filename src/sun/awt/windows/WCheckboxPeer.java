package sun.awt.windows;

import java.awt.Checkbox;
import java.awt.CheckboxGroup;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.event.ItemEvent;
import java.awt.peer.CheckboxPeer;

public class WCheckboxPeer extends WComponentPeer
  implements CheckboxPeer
{
  public native void setState(boolean paramBoolean);

  public native void setCheckboxGroup(CheckboxGroup paramCheckboxGroup);

  public native void setLabel(String paramString);

  private static native int getCheckMarkSize();

  public Dimension getMinimumSize()
  {
    String str = ((Checkbox)this.target).getLabel();
    int i = getCheckMarkSize();
    if (str == null)
      str = "";
    FontMetrics localFontMetrics = getFontMetrics(((Checkbox)this.target).getFont());
    return new Dimension(localFontMetrics.stringWidth(str) + i / 2 + i, Math.max(localFontMetrics.getHeight() + 8, i));
  }

  public boolean isFocusable()
  {
    return true;
  }

  WCheckboxPeer(Checkbox paramCheckbox)
  {
    super(paramCheckbox);
  }

  native void create(WComponentPeer paramWComponentPeer);

  void initialize()
  {
    Checkbox localCheckbox = (Checkbox)this.target;
    setState(localCheckbox.getState());
    setCheckboxGroup(localCheckbox.getCheckboxGroup());
    Color localColor = ((Component)this.target).getBackground();
    if (localColor != null)
      setBackground(localColor);
    super.initialize();
  }

  public boolean shouldClearRectBeforePaint()
  {
    return false;
  }

  void handleAction(boolean paramBoolean)
  {
    Checkbox localCheckbox = (Checkbox)this.target;
    WToolkit.executeOnEventHandlerThread(localCheckbox, new Runnable(this, localCheckbox, paramBoolean)
    {
      public void run()
      {
        CheckboxGroup localCheckboxGroup = this.val$cb.getCheckboxGroup();
        if ((localCheckboxGroup != null) && (this.val$cb == localCheckboxGroup.getSelectedCheckbox()) && (this.val$cb.getState()))
          return;
        this.val$cb.setState(this.val$state);
        this.this$0.postEvent(new ItemEvent(this.val$cb, 701, this.val$cb.getLabel(), 2));
      }
    });
  }

  public Dimension minimumSize()
  {
    return getMinimumSize();
  }
}