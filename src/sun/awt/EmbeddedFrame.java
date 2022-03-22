package sun.awt;

import java.applet.Applet;
import java.awt.AWTKeyStroke;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FocusTraversalPolicy;
import java.awt.Frame;
import java.awt.Image;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.MenuBar;
import java.awt.MenuComponent;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.awt.peer.ComponentPeer;
import java.awt.peer.FramePeer;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.List;
import java.util.Set;

public abstract class EmbeddedFrame extends Frame
  implements KeyEventDispatcher, PropertyChangeListener
{
  private boolean isCursorAllowed;
  private static Field fieldPeer;
  private static Field currentCycleRoot;
  private boolean supportsXEmbed;
  private KeyboardFocusManager appletKFM;
  private static final long serialVersionUID = 2967042741780317130L;
  protected static final boolean FORWARD = 1;
  protected static final boolean BACKWARD = 0;

  public boolean supportsXEmbed()
  {
    return ((this.supportsXEmbed) && (SunToolkit.needsXEmbed()));
  }

  protected EmbeddedFrame(boolean paramBoolean)
  {
    this(3412047170994438144L, paramBoolean);
  }

  protected EmbeddedFrame()
  {
    this(3412047170994438144L);
  }

  @Deprecated
  protected EmbeddedFrame(int paramInt)
  {
    this(paramInt);
  }

  protected EmbeddedFrame(long paramLong)
  {
    this(paramLong, false);
  }

  protected EmbeddedFrame(long paramLong, boolean paramBoolean)
  {
    this.isCursorAllowed = true;
    this.supportsXEmbed = false;
    this.supportsXEmbed = paramBoolean;
    registerListeners();
  }

  public Container getParent()
  {
    return null;
  }

  public void propertyChange(PropertyChangeEvent paramPropertyChangeEvent)
  {
    if (!(paramPropertyChangeEvent.getPropertyName().equals("managingFocus")))
      return;
    if (paramPropertyChangeEvent.getNewValue() == Boolean.TRUE)
      return;
    removeTraversingOutListeners((KeyboardFocusManager)paramPropertyChangeEvent.getSource());
    this.appletKFM = KeyboardFocusManager.getCurrentKeyboardFocusManager();
    if (isVisible())
      addTraversingOutListeners(this.appletKFM);
  }

  private void addTraversingOutListeners(KeyboardFocusManager paramKeyboardFocusManager)
  {
    paramKeyboardFocusManager.addKeyEventDispatcher(this);
    paramKeyboardFocusManager.addPropertyChangeListener("managingFocus", this);
  }

  private void removeTraversingOutListeners(KeyboardFocusManager paramKeyboardFocusManager)
  {
    paramKeyboardFocusManager.removeKeyEventDispatcher(this);
    paramKeyboardFocusManager.removePropertyChangeListener("managingFocus", this);
  }

  public void registerListeners()
  {
    if (this.appletKFM != null)
      removeTraversingOutListeners(this.appletKFM);
    this.appletKFM = KeyboardFocusManager.getCurrentKeyboardFocusManager();
    if (isVisible())
      addTraversingOutListeners(this.appletKFM);
  }

  public void show()
  {
    if (this.appletKFM != null)
      addTraversingOutListeners(this.appletKFM);
    super.show();
  }

  public void hide()
  {
    if (this.appletKFM != null)
      removeTraversingOutListeners(this.appletKFM);
    super.hide();
  }

  public boolean dispatchKeyEvent(KeyEvent paramKeyEvent)
  {
    if (currentCycleRoot == null)
      currentCycleRoot = (Field)AccessController.doPrivileged(new PrivilegedAction(this)
      {
        public Object run()
        {
          Field localField;
          try
          {
            localField = KeyboardFocusManager.class.getDeclaredField("currentFocusCycleRoot");
            if (localField != null)
              localField.setAccessible(true);
            return localField;
          }
          catch (NoSuchFieldException localNoSuchFieldException)
          {
            if (!($assertionsDisabled))
              throw new AssertionError();
          }
          catch (SecurityException localSecurityException)
          {
            if (!($assertionsDisabled))
              throw new AssertionError();
          }
          return null;
        }
      });
    Container localContainer = null;
    if (currentCycleRoot != null)
      try
      {
        localContainer = (Container)currentCycleRoot.get(null);
      }
      catch (IllegalAccessException localIllegalAccessException)
      {
        if (!($assertionsDisabled))
          throw new AssertionError();
      }
    if (this != localContainer)
      return false;
    if (paramKeyEvent.getID() == 400)
      return false;
    if ((!(getFocusTraversalKeysEnabled())) || (paramKeyEvent.isConsumed()))
      return false;
    AWTKeyStroke localAWTKeyStroke = AWTKeyStroke.getAWTKeyStrokeForEvent(paramKeyEvent);
    Component localComponent1 = paramKeyEvent.getComponent();
    Component localComponent2 = getFocusTraversalPolicy().getLastComponent(this);
    Set localSet = getFocusTraversalKeys(0);
    if ((localSet.contains(localAWTKeyStroke)) && (((localComponent1 == localComponent2) || (localComponent2 == null))) && (traverseOut(true)))
    {
      paramKeyEvent.consume();
      return true;
    }
    Component localComponent3 = getFocusTraversalPolicy().getFirstComponent(this);
    localSet = getFocusTraversalKeys(1);
    if ((localSet.contains(localAWTKeyStroke)) && (((localComponent1 == localComponent3) || (localComponent3 == null))) && (traverseOut(false)))
    {
      paramKeyEvent.consume();
      return true;
    }
    return false;
  }

  public boolean traverseIn(boolean paramBoolean)
  {
    Component localComponent = null;
    if (paramBoolean == true)
      localComponent = getFocusTraversalPolicy().getFirstComponent(this);
    else
      localComponent = getFocusTraversalPolicy().getLastComponent(this);
    if (localComponent != null)
    {
      SunToolkit.setMostRecentFocusOwner(this, localComponent);
      synthesizeWindowActivation(true);
    }
    return (null != localComponent);
  }

  protected boolean traverseOut(boolean paramBoolean)
  {
    return false;
  }

  public void setTitle(String paramString)
  {
  }

  public void setIconImage(Image paramImage)
  {
  }

  public void setIconImages(List<? extends Image> paramList)
  {
  }

  public void setMenuBar(MenuBar paramMenuBar)
  {
  }

  public void setResizable(boolean paramBoolean)
  {
  }

  public void remove(MenuComponent paramMenuComponent)
  {
  }

  public boolean isResizable()
  {
    return true;
  }

  public void addNotify()
  {
    synchronized (getTreeLock())
    {
      if (getPeer() == null)
        setPeer(new NullEmbeddedFramePeer(null));
      super.addNotify();
    }
  }

  public void setCursorAllowed(boolean paramBoolean)
  {
    this.isCursorAllowed = paramBoolean;
    getPeer().updateCursorImmediately();
  }

  public boolean isCursorAllowed()
  {
    return this.isCursorAllowed;
  }

  public Cursor getCursor()
  {
    return ((this.isCursorAllowed) ? super.getCursor() : Cursor.getPredefinedCursor(0));
  }

  protected void setPeer(ComponentPeer paramComponentPeer)
  {
    if (fieldPeer == null)
      fieldPeer = (Field)AccessController.doPrivileged(new PrivilegedAction(this)
      {
        public Object run()
        {
          Field localField;
          try
          {
            localField = Component.class.getDeclaredField("peer");
            if (localField != null)
              localField.setAccessible(true);
            return localField;
          }
          catch (NoSuchFieldException localNoSuchFieldException)
          {
            if (!($assertionsDisabled))
              throw new AssertionError();
          }
          catch (SecurityException localSecurityException)
          {
            if (!($assertionsDisabled))
              throw new AssertionError();
          }
          return null;
        }
      });
    try
    {
      if (fieldPeer != null)
        fieldPeer.set(this, paramComponentPeer);
    }
    catch (IllegalAccessException localIllegalAccessException)
    {
      if (!($assertionsDisabled))
        throw new AssertionError();
    }
  }

  public void synthesizeWindowActivation(boolean paramBoolean)
  {
  }

  protected void setLocationPrivate(int paramInt1, int paramInt2)
  {
    Dimension localDimension = getSize();
    setBoundsPrivate(paramInt1, paramInt2, localDimension.width, localDimension.height);
  }

  protected Point getLocationPrivate()
  {
    Rectangle localRectangle = getBoundsPrivate();
    return new Point(localRectangle.x, localRectangle.y);
  }

  protected void setBoundsPrivate(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    FramePeer localFramePeer = (FramePeer)getPeer();
    if (localFramePeer != null)
      localFramePeer.setBoundsPrivate(paramInt1, paramInt2, paramInt3, paramInt4);
  }

  protected Rectangle getBoundsPrivate()
  {
    FramePeer localFramePeer = (FramePeer)getPeer();
    if (localFramePeer != null)
      return localFramePeer.getBoundsPrivate();
    return getBounds();
  }

  public void toFront()
  {
  }

  public void toBack()
  {
  }

  public abstract void registerAccelerator(AWTKeyStroke paramAWTKeyStroke);

  public abstract void unregisterAccelerator(AWTKeyStroke paramAWTKeyStroke);

  public static Applet getAppletIfAncestorOf(Component paramComponent)
  {
    Container localContainer = paramComponent.getParent();
    Applet localApplet = null;
    while ((localContainer != null) && (!(localContainer instanceof EmbeddedFrame)))
    {
      if (localContainer instanceof Applet)
        localApplet = (Applet)localContainer;
      localContainer = localContainer.getParent();
    }
    return ((localContainer == null) ? null : localApplet);
  }

  public void notifyModalBlocked(Dialog paramDialog, boolean paramBoolean)
  {
  }

  private static class NullEmbeddedFramePeer extends NullComponentPeer
  implements FramePeer
  {
    public void setTitle(String paramString)
    {
    }

    public void setIconImage(Image paramImage)
    {
    }

    public void updateIconImages()
    {
    }

    public void setMenuBar(MenuBar paramMenuBar)
    {
    }

    public void setResizable(boolean paramBoolean)
    {
    }

    public void setState(int paramInt)
    {
    }

    public int getState()
    {
      return 0;
    }

    public void setMaximizedBounds(Rectangle paramRectangle)
    {
    }

    public void toFront()
    {
    }

    public void toBack()
    {
    }

    public void updateFocusableWindowState()
    {
    }

    public void updateAlwaysOnTop()
    {
    }

    public void setAlwaysOnTop(boolean paramBoolean)
    {
    }

    public Component getGlobalHeavyweightFocusOwner()
    {
      return null;
    }

    public void setBoundsPrivate(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
    {
      setBounds(paramInt1, paramInt2, paramInt3, paramInt4, 3);
    }

    public Rectangle getBoundsPrivate()
    {
      return getBounds();
    }

    public void setModalBlocked(Dialog paramDialog, boolean paramBoolean)
    {
    }

    public void restack()
    {
      throw new UnsupportedOperationException();
    }

    public boolean isRestackSupported()
    {
      return false;
    }

    public boolean requestWindowFocus()
    {
      return false;
    }

    public void updateMinimumSize()
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
  }
}