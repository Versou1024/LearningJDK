package sun.awt.windows;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.Event;
import java.awt.Font;
import java.awt.dnd.DropTarget;
import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.Vector;
import sun.awt.EmbeddedFrame;
import sun.java2d.pipe.Region;

public class WPrintDialogPeer extends WWindowPeer
{
  private WComponentPeer parent;
  private Vector<WWindowPeer> blockedWindows = new Vector();

  WPrintDialogPeer(WPrintDialog paramWPrintDialog)
  {
    super(paramWPrintDialog);
  }

  void create(WComponentPeer paramWComponentPeer)
  {
    this.parent = paramWComponentPeer;
  }

  protected void checkCreation()
  {
  }

  protected void disposeImpl()
  {
    WToolkit.targetDisposedPeer(this.target, this);
  }

  private native boolean _show();

  public void show()
  {
    new Thread(new Runnable(this)
    {
      public void run()
      {
        ((WPrintDialog)this.this$0.target).setRetVal(WPrintDialogPeer.access$000(this.this$0));
        ((WPrintDialog)this.this$0.target).hide();
      }
    }).start();
  }

  synchronized void setHWnd(long paramLong)
  {
    this.hwnd = paramLong;
    if (paramLong != 3412046810217185280L)
    {
      Iterator localIterator = this.blockedWindows.iterator();
      while (localIterator.hasNext())
      {
        WWindowPeer localWWindowPeer = (WWindowPeer)localIterator.next();
        localWWindowPeer.modalDisableByHWnd(paramLong);
        if (localWWindowPeer.target instanceof EmbeddedFrame)
          ((EmbeddedFrame)localWWindowPeer.target).notifyModalBlocked((Dialog)this.target, true);
      }
      this.blockedWindows.clear();
    }
  }

  synchronized void blockWindow(WWindowPeer paramWWindowPeer)
  {
    if (this.hwnd != 3412046827397054464L)
      paramWWindowPeer.modalDisableByHWnd(this.hwnd);
    else
      this.blockedWindows.add(paramWWindowPeer);
  }

  synchronized void unblockWindow(WWindowPeer paramWWindowPeer)
  {
    this.blockedWindows.remove(paramWWindowPeer);
  }

  public native void toFront();

  public native void toBack();

  void initialize()
  {
  }

  public void setAlwaysOnTop(boolean paramBoolean)
  {
  }

  public void setResizable(boolean paramBoolean)
  {
  }

  public void hide()
  {
  }

  public void enable()
  {
  }

  public void disable()
  {
  }

  public void reshape(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
  }

  public boolean handleEvent(Event paramEvent)
  {
    return false;
  }

  public void setForeground(Color paramColor)
  {
  }

  public void setBackground(Color paramColor)
  {
  }

  public void setFont(Font paramFont)
  {
  }

  public void updateMinimumSize()
  {
  }

  public void updateIconImages()
  {
  }

  public boolean requestFocus(boolean paramBoolean1, boolean paramBoolean2)
  {
    return false;
  }

  public void updateFocusableWindowState()
  {
  }

  void start()
  {
  }

  public void beginValidate()
  {
  }

  public void endValidate()
  {
  }

  void invalidate(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
  }

  public void addDropTarget(DropTarget paramDropTarget)
  {
  }

  public void removeDropTarget(DropTarget paramDropTarget)
  {
  }

  private static native void initIDs();

  public void restack()
  {
  }

  public boolean isRestackSupported()
  {
    return false;
  }

  public void applyShape(Region paramRegion)
  {
  }

  public void setOpacity(float paramFloat)
  {
  }

  public void setOpaque(boolean paramBoolean)
  {
  }

  public void updateWindow(BufferedImage paramBufferedImage)
  {
  }

  static
  {
    initIDs();
  }
}