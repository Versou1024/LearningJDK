package sun.awt.windows;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.MenuBar;
import java.awt.Rectangle;
import java.awt.peer.FramePeer;
import java.security.AccessController;
import sun.awt.im.InputMethodManager;
import sun.security.action.GetPropertyAction;

class WFramePeer extends WWindowPeer
  implements FramePeer
{
  private static final boolean keepOnMinimize = "true".equals((String)AccessController.doPrivileged(new GetPropertyAction("sun.awt.keepWorkingSetOnMinimize")));

  public native void setState(int paramInt);

  public native int getState();

  private native void setMaximizedBounds(int paramInt1, int paramInt2, int paramInt3, int paramInt4);

  private native void clearMaximizedBounds();

  public void setMaximizedBounds(Rectangle paramRectangle)
  {
    if (paramRectangle == null)
      clearMaximizedBounds();
    else
      setMaximizedBounds(paramRectangle.x, paramRectangle.y, paramRectangle.width, paramRectangle.height);
  }

  boolean isTargetUndecorated()
  {
    return ((Frame)this.target).isUndecorated();
  }

  public void reshape(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    Rectangle localRectangle = constrainBounds(paramInt1, paramInt2, paramInt3, paramInt4);
    if (((Frame)this.target).isUndecorated())
      super.reshape(localRectangle.x, localRectangle.y, localRectangle.width, localRectangle.height);
    else
      reshapeFrame(localRectangle.x, localRectangle.y, localRectangle.width, localRectangle.height);
  }

  public Dimension getMinimumSize()
  {
    Dimension localDimension = new Dimension();
    if (!(((Frame)this.target).isUndecorated()))
      localDimension.setSize(getSysMinWidth(), getSysMinHeight());
    if (((Frame)this.target).getMenuBar() != null)
      localDimension.height += getSysMenuHeight();
    return localDimension;
  }

  public void setMenuBar(MenuBar paramMenuBar)
  {
    WMenuBarPeer localWMenuBarPeer = (WMenuBarPeer)WToolkit.targetToPeer(paramMenuBar);
    setMenuBar0(localWMenuBarPeer);
    updateInsets(this.insets_);
  }

  private native void setMenuBar0(WMenuBarPeer paramWMenuBarPeer);

  WFramePeer(Frame paramFrame)
  {
    super(paramFrame);
    InputMethodManager localInputMethodManager = InputMethodManager.getInstance();
    String str = localInputMethodManager.getTriggerMenuString();
    if (str != null)
      pSetIMMOption(str);
  }

  native void createAwtFrame(WComponentPeer paramWComponentPeer);

  void create(WComponentPeer paramWComponentPeer)
  {
    createAwtFrame(paramWComponentPeer);
  }

  void initialize()
  {
    super.initialize();
    Frame localFrame = (Frame)this.target;
    if (localFrame.getTitle() != null)
      setTitle(localFrame.getTitle());
    setResizable(localFrame.isResizable());
    setState(localFrame.getExtendedState());
  }

  private static native int getSysMenuHeight();

  native void pSetIMMOption(String paramString);

  void notifyIMMOptionChange()
  {
    InputMethodManager.getInstance().notifyChangeRequest((Component)this.target);
  }

  public void setBoundsPrivate(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    setBounds(paramInt1, paramInt2, paramInt3, paramInt4, 3);
  }

  public Rectangle getBoundsPrivate()
  {
    return getBounds();
  }
}