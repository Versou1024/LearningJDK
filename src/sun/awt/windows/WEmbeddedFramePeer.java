package sun.awt.windows;

import java.awt.Dialog;
import java.awt.Graphics;
import java.awt.Rectangle;
import sun.awt.EmbeddedFrame;
import sun.awt.Win32GraphicsEnvironment;

public class WEmbeddedFramePeer extends WFramePeer
{
  public WEmbeddedFramePeer(EmbeddedFrame paramEmbeddedFrame)
  {
    super(paramEmbeddedFrame);
  }

  native void create(WComponentPeer paramWComponentPeer);

  public void print(Graphics paramGraphics)
  {
  }

  public void updateMinimumSize()
  {
  }

  public void setModalBlocked(Dialog paramDialog, boolean paramBoolean)
  {
    super.setModalBlocked(paramDialog, paramBoolean);
    WWindowPeer localWWindowPeer = (WWindowPeer)paramDialog.getPeer();
    if ((!(paramBoolean)) || ((!(localWWindowPeer instanceof WFileDialogPeer)) && (!(localWWindowPeer instanceof WPrintDialogPeer))))
    {
      EmbeddedFrame localEmbeddedFrame = (EmbeddedFrame)this.target;
      localEmbeddedFrame.notifyModalBlocked(paramDialog, paramBoolean);
    }
  }

  public void setBoundsPrivate(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    setBounds(paramInt1, paramInt2, paramInt3, paramInt4, 16387);
  }

  public native Rectangle getBoundsPrivate();

  public native void synthesizeWmActivate(boolean paramBoolean);

  Rectangle constrainBounds(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    return new Rectangle(paramInt1, paramInt2, paramInt3, paramInt4);
  }

  public boolean isAccelCapable()
  {
    return (!(Win32GraphicsEnvironment.isDWMCompositionEnabled()));
  }
}