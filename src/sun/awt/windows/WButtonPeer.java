package sun.awt.windows;

import java.awt.Button;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.peer.ButtonPeer;

class WButtonPeer extends WComponentPeer
  implements ButtonPeer
{
  public Dimension getMinimumSize()
  {
    FontMetrics localFontMetrics = getFontMetrics(((Button)this.target).getFont());
    String str = ((Button)this.target).getLabel();
    if (str == null)
      str = "";
    return new Dimension(localFontMetrics.stringWidth(str) + 14, localFontMetrics.getHeight() + 8);
  }

  public boolean isFocusable()
  {
    return true;
  }

  public native void setLabel(String paramString);

  WButtonPeer(Button paramButton)
  {
    super(paramButton);
  }

  native void create(WComponentPeer paramWComponentPeer);

  public void handleAction(long paramLong, int paramInt)
  {
    WToolkit.executeOnEventHandlerThread(this.target, new Runnable(this, paramLong, paramInt)
    {
      public void run()
      {
        this.this$0.postEvent(new ActionEvent(this.this$0.target, 1001, ((Button)this.this$0.target).getActionCommand(), this.val$when, this.val$modifiers));
      }
    }
    , paramLong);
  }

  public boolean shouldClearRectBeforePaint()
  {
    return false;
  }

  public Dimension minimumSize()
  {
    return getMinimumSize();
  }

  private static native void initIDs();

  public boolean handleJavaKeyEvent(KeyEvent paramKeyEvent)
  {
    switch (paramKeyEvent.getID())
    {
    case 402:
      if (paramKeyEvent.getKeyCode() != 32)
        break label45;
      handleAction(paramKeyEvent.getWhen(), paramKeyEvent.getModifiers());
    }
    label45: return false;
  }

  static
  {
    initIDs();
  }
}