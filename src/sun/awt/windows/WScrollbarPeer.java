package sun.awt.windows;

import java.awt.Dimension;
import java.awt.Scrollbar;
import java.awt.event.AdjustmentEvent;
import java.awt.peer.ScrollbarPeer;

class WScrollbarPeer extends WComponentPeer
  implements ScrollbarPeer
{
  private boolean dragInProgress = false;

  static native int getScrollbarSize(int paramInt);

  public Dimension getMinimumSize()
  {
    if (((Scrollbar)this.target).getOrientation() == 1)
      return new Dimension(getScrollbarSize(1), 50);
    return new Dimension(50, getScrollbarSize(0));
  }

  public native void setValues(int paramInt1, int paramInt2, int paramInt3, int paramInt4);

  public native void setLineIncrement(int paramInt);

  public native void setPageIncrement(int paramInt);

  WScrollbarPeer(Scrollbar paramScrollbar)
  {
    super(paramScrollbar);
  }

  native void create(WComponentPeer paramWComponentPeer);

  void initialize()
  {
    Scrollbar localScrollbar = (Scrollbar)this.target;
    setValues(localScrollbar.getValue(), localScrollbar.getVisibleAmount(), localScrollbar.getMinimum(), localScrollbar.getMaximum());
    super.initialize();
  }

  private void postAdjustmentEvent(int paramInt1, int paramInt2, boolean paramBoolean)
  {
    Scrollbar localScrollbar = (Scrollbar)this.target;
    WToolkit.executeOnEventHandlerThread(localScrollbar, new Runnable(this, localScrollbar, paramBoolean, paramInt2, paramInt1)
    {
      public void run()
      {
        this.val$sb.setValueIsAdjusting(this.val$isAdjusting);
        this.val$sb.setValue(this.val$value);
        this.this$0.postEvent(new AdjustmentEvent(this.val$sb, 601, this.val$type, this.val$value, this.val$isAdjusting));
      }
    });
  }

  void lineUp(int paramInt)
  {
    postAdjustmentEvent(2, paramInt, false);
  }

  void lineDown(int paramInt)
  {
    postAdjustmentEvent(1, paramInt, false);
  }

  void pageUp(int paramInt)
  {
    postAdjustmentEvent(3, paramInt, false);
  }

  void pageDown(int paramInt)
  {
    postAdjustmentEvent(4, paramInt, false);
  }

  void warp(int paramInt)
  {
    postAdjustmentEvent(5, paramInt, false);
  }

  void drag(int paramInt)
  {
    if (!(this.dragInProgress))
      this.dragInProgress = true;
    postAdjustmentEvent(5, paramInt, true);
  }

  void dragEnd(int paramInt)
  {
    Scrollbar localScrollbar = (Scrollbar)this.target;
    if (!(this.dragInProgress))
      return;
    this.dragInProgress = false;
    WToolkit.executeOnEventHandlerThread(localScrollbar, new Runnable(this, localScrollbar, paramInt)
    {
      public void run()
      {
        this.val$sb.setValueIsAdjusting(false);
        this.this$0.postEvent(new AdjustmentEvent(this.val$sb, 601, 5, this.val$value, false));
      }
    });
  }

  public boolean shouldClearRectBeforePaint()
  {
    return false;
  }

  public Dimension minimumSize()
  {
    return getMinimumSize();
  }
}