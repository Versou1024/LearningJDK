package sun.awt.windows;

import java.awt.Adjustable;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.ScrollPane;
import java.awt.ScrollPaneAdjustable;
import java.awt.peer.ScrollPanePeer;
import sun.awt.DebugHelper;
import sun.awt.PeerEvent;

class WScrollPanePeer extends WPanelPeer
  implements ScrollPanePeer
{
  private static final DebugHelper dbg = DebugHelper.create(WScrollPanePeer.class);
  int scrollbarWidth = ???._getVScrollbarWidth();
  int scrollbarHeight = ???._getHScrollbarHeight();
  int prevx;
  int prevy;

  static native void initIDs();

  native void create(WComponentPeer paramWComponentPeer);

  native int getOffset(int paramInt);

  WScrollPanePeer(Component paramComponent)
  {
    super(paramComponent);
  }

  void initialize()
  {
    super.initialize();
    setInsets();
    Insets localInsets = getInsets();
    setScrollPosition(-localInsets.left, -localInsets.top);
  }

  public void setUnitIncrement(Adjustable paramAdjustable, int paramInt)
  {
  }

  public Insets insets()
  {
    return getInsets();
  }

  private native void setInsets();

  public synchronized native void setScrollPosition(int paramInt1, int paramInt2);

  public int getHScrollbarHeight()
  {
    return this.scrollbarHeight;
  }

  private native int _getHScrollbarHeight();

  public int getVScrollbarWidth()
  {
    return this.scrollbarWidth;
  }

  private native int _getVScrollbarWidth();

  public Point getScrollOffset()
  {
    int i = getOffset(0);
    int j = getOffset(1);
    return new Point(i, j);
  }

  public void childResized(int paramInt1, int paramInt2)
  {
    ScrollPane localScrollPane = (ScrollPane)this.target;
    Dimension localDimension = localScrollPane.getSize();
    setSpans(localDimension.width, localDimension.height, paramInt1, paramInt2);
    setInsets();
  }

  synchronized native void setSpans(int paramInt1, int paramInt2, int paramInt3, int paramInt4);

  public void setValue(Adjustable paramAdjustable, int paramInt)
  {
    Component localComponent = getScrollChild();
    if (localComponent == null)
      return;
    Point localPoint = localComponent.getLocation();
    switch (paramAdjustable.getOrientation())
    {
    case 1:
      setScrollPosition(-localPoint.x, paramInt);
      break;
    case 0:
      setScrollPosition(paramInt, -localPoint.y);
    }
  }

  private Component getScrollChild()
  {
    ScrollPane localScrollPane = (ScrollPane)this.target;
    Component localComponent = null;
    try
    {
      localComponent = localScrollPane.getComponent(0);
    }
    catch (ArrayIndexOutOfBoundsException localArrayIndexOutOfBoundsException)
    {
    }
    return localComponent;
  }

  private void postScrollEvent(int paramInt1, int paramInt2, int paramInt3, boolean paramBoolean)
  {
    Adjustor localAdjustor = new Adjustor(this, paramInt1, paramInt2, paramInt3, paramBoolean);
    WToolkit.executeOnEventHandlerThread(new ScrollEvent(this, this.target, localAdjustor));
  }

  native void setTypedValue(ScrollPaneAdjustable paramScrollPaneAdjustable, int paramInt1, int paramInt2);

  public void restack()
  {
  }

  static
  {
    initIDs();
  }

  class Adjustor
  implements Runnable
  {
    int orient;
    int type;
    int pos;
    boolean isAdjusting;

    Adjustor(, int paramInt1, int paramInt2, int paramInt3, boolean paramBoolean)
    {
      this.orient = paramInt1;
      this.type = paramInt2;
      this.pos = paramInt3;
      this.isAdjusting = paramBoolean;
    }

    public void run()
    {
      if (WScrollPanePeer.access$100(this.this$0) == null)
        return;
      ScrollPane localScrollPane = (ScrollPane)this.this$0.target;
      ScrollPaneAdjustable localScrollPaneAdjustable = null;
      if (this.orient == 1)
        localScrollPaneAdjustable = (ScrollPaneAdjustable)localScrollPane.getVAdjustable();
      else if (this.orient == 0)
        localScrollPaneAdjustable = (ScrollPaneAdjustable)localScrollPane.getHAdjustable();
      else
        WScrollPanePeer.access$000();
      if (localScrollPaneAdjustable == null)
        return;
      int i = localScrollPaneAdjustable.getValue();
      switch (this.type)
      {
      case 2:
        i -= localScrollPaneAdjustable.getUnitIncrement();
        break;
      case 1:
        i += localScrollPaneAdjustable.getUnitIncrement();
        break;
      case 3:
        i -= localScrollPaneAdjustable.getBlockIncrement();
        break;
      case 4:
        i += localScrollPaneAdjustable.getBlockIncrement();
        break;
      case 5:
        i = this.pos;
        break;
      default:
        WScrollPanePeer.access$000();
        return;
      }
      i = Math.max(localScrollPaneAdjustable.getMinimum(), i);
      i = Math.min(localScrollPaneAdjustable.getMaximum(), i);
      localScrollPaneAdjustable.setValueIsAdjusting(this.isAdjusting);
      this.this$0.setTypedValue(localScrollPaneAdjustable, i, this.type);
      for (Object localObject = WScrollPanePeer.access$100(this.this$0); (localObject != null) && (!(((Component)localObject).getPeer() instanceof WComponentPeer)); localObject = ((Component)localObject).getParent());
      WScrollPanePeer.access$000();
      WComponentPeer localWComponentPeer = (WComponentPeer)((Component)localObject).getPeer();
      localWComponentPeer.paintDamagedAreaImmediately();
    }
  }

  class ScrollEvent extends PeerEvent
  {
    ScrollEvent(, Object paramObject, Runnable paramRunnable)
    {
      super(paramObject, paramRunnable, 3412047944088551424L);
    }

    public PeerEvent coalesceEvents()
    {
      WScrollPanePeer.access$000();
      if (paramPeerEvent instanceof ScrollEvent)
        return paramPeerEvent;
      return null;
    }
  }
}