package sun.awt.windows;

import java.awt.AWTEvent;
import java.awt.Font;
import java.awt.MenuItem;
import java.awt.MenuShortcut;
import java.awt.event.ActionEvent;
import java.awt.peer.MenuItemPeer;
import java.io.PrintStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

class WMenuItemPeer extends WObjectPeer
  implements MenuItemPeer
{
  String shortcutLabel;
  protected WMenuPeer parent;
  boolean isCheckbox = false;
  private static Font defaultMenuFont;

  private synchronized native void _dispose();

  protected void disposeImpl()
  {
    WToolkit.targetDisposedPeer(this.target, this);
    _dispose();
  }

  public void setEnabled(boolean paramBoolean)
  {
    enable(paramBoolean);
  }

  public void enable()
  {
    enable(true);
  }

  public void disable()
  {
    enable(false);
  }

  public void readShortcutLabel()
  {
    for (WMenuPeer localWMenuPeer = this.parent; (localWMenuPeer != null) && (!(localWMenuPeer instanceof WMenuBarPeer)); localWMenuPeer = localWMenuPeer.parent);
    if (localWMenuPeer instanceof WMenuBarPeer)
    {
      MenuShortcut localMenuShortcut = ((MenuItem)this.target).getShortcut();
      this.shortcutLabel = ((localMenuShortcut != null) ? localMenuShortcut.toString() : null);
    }
    else
    {
      this.shortcutLabel = null;
    }
  }

  public void setLabel(String paramString)
  {
    readShortcutLabel();
    _setLabel(paramString);
  }

  public native void _setLabel(String paramString);

  protected WMenuItemPeer()
  {
  }

  WMenuItemPeer(MenuItem paramMenuItem)
  {
    this.target = paramMenuItem;
    this.parent = ((WMenuPeer)WToolkit.targetToPeer(paramMenuItem.getParent()));
    create(this.parent);
    checkMenuCreation();
    readShortcutLabel();
  }

  protected void checkMenuCreation()
  {
    if (this.pData == 3412046810217185280L)
    {
      if (this.createError != null)
        throw this.createError;
      throw new InternalError("couldn't create menu peer");
    }
  }

  void postEvent(AWTEvent paramAWTEvent)
  {
    WToolkit.postEvent(WToolkit.targetToAppContext(this.target), paramAWTEvent);
  }

  native void create(WMenuPeer paramWMenuPeer);

  native void enable(boolean paramBoolean);

  void handleAction(long paramLong, int paramInt)
  {
    WToolkit.executeOnEventHandlerThread(this.target, new Runnable(this, paramLong, paramInt)
    {
      public void run()
      {
        this.this$0.postEvent(new ActionEvent(this.this$0.target, 1001, ((MenuItem)this.this$0.target).getActionCommand(), this.val$when, this.val$modifiers));
      }
    });
  }

  static Font getDefaultFont()
  {
    return defaultMenuFont;
  }

  private static native void initIDs();

  public void setFont(Font paramFont)
  {
  }

  static
  {
    initIDs();
    defaultMenuFont = (Font)AccessController.doPrivileged(new PrivilegedAction()
    {
      public Object run()
      {
        ResourceBundle localResourceBundle;
        try
        {
          localResourceBundle = ResourceBundle.getBundle("sun.awt.windows.awtLocalization");
          return Font.decode(localResourceBundle.getString("menuFont"));
        }
        catch (MissingResourceException localMissingResourceException)
        {
          System.out.println(localMissingResourceException.getMessage());
          System.out.println("Using default MenuItem font\n");
        }
        return new Font("SanSerif", 0, 11);
      }
    });
  }
}