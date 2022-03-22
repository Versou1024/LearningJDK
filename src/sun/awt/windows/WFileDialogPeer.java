package sun.awt.windows;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.Event;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.dnd.DropTarget;
import java.awt.image.BufferedImage;
import java.awt.peer.FileDialogPeer;
import java.io.File;
import java.io.FilenameFilter;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Iterator;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Vector;
import sun.awt.EmbeddedFrame;
import sun.java2d.pipe.Region;

public class WFileDialogPeer extends WWindowPeer
  implements FileDialogPeer
{
  private WComponentPeer parent;
  private FilenameFilter fileFilter;
  private Vector<WWindowPeer> blockedWindows = new Vector();

  private static native void setFilterString(String paramString);

  public void setFilenameFilter(FilenameFilter paramFilenameFilter)
  {
    this.fileFilter = paramFilenameFilter;
  }

  boolean checkFilenameFilter(String paramString)
  {
    FileDialog localFileDialog = (FileDialog)this.target;
    if (this.fileFilter == null)
      return true;
    File localFile = new File(paramString);
    return this.fileFilter.accept(new File(localFile.getParent()), localFile.getName());
  }

  WFileDialogPeer(FileDialog paramFileDialog)
  {
    super(paramFileDialog);
  }

  void create(WComponentPeer paramWComponentPeer)
  {
    this.parent = paramWComponentPeer;
  }

  protected void checkCreation()
  {
  }

  void initialize()
  {
  }

  private native void _dispose();

  protected void disposeImpl()
  {
    WToolkit.targetDisposedPeer(this.target, this);
    _dispose();
  }

  private native void _show();

  private native void _hide();

  public void show()
  {
    new Thread(new Runnable(this)
    {
      public void run()
      {
        WFileDialogPeer.access$000(this.this$0);
      }
    }).start();
  }

  public void hide()
  {
    _hide();
  }

  void setHWnd(long paramLong)
  {
    this.hwnd = paramLong;
    if (paramLong != 3412046672778231808L)
    {
      Iterator localIterator = this.blockedWindows.iterator();
      while (localIterator.hasNext())
      {
        WWindowPeer localWWindowPeer = (WWindowPeer)localIterator.next();
        localWWindowPeer.modalDisableByHWnd(paramLong);
        if (localWWindowPeer.target instanceof EmbeddedFrame)
          ((EmbeddedFrame)localWWindowPeer.target).notifyModalBlocked((Dialog)this.target, true);
      }
    }
  }

  void handleSelected(String paramString)
  {
    FileDialog localFileDialog = (FileDialog)this.target;
    WToolkit.executeOnEventHandlerThread(localFileDialog, new Runnable(this, paramString, localFileDialog)
    {
      public void run()
      {
        String str;
        int i = this.val$file.lastIndexOf(File.separatorChar);
        if (i == -1)
        {
          str = "." + File.separator;
          this.val$fileDialog.setFile(this.val$file);
        }
        else
        {
          str = this.val$file.substring(0, i + 1);
          this.val$fileDialog.setFile(this.val$file.substring(i + 1));
        }
        this.val$fileDialog.setDirectory(str);
        this.val$fileDialog.hide();
      }
    });
  }

  void handleCancel()
  {
    FileDialog localFileDialog = (FileDialog)this.target;
    WToolkit.executeOnEventHandlerThread(localFileDialog, new Runnable(this, localFileDialog)
    {
      public void run()
      {
        this.val$fileDialog.setFile(null);
        this.val$fileDialog.hide();
      }
    });
  }

  void blockWindow(WWindowPeer paramWWindowPeer)
  {
    if (this.hwnd != 3412046689958100992L)
      paramWWindowPeer.modalDisableByHWnd(this.hwnd);
    else
      this.blockedWindows.add(paramWWindowPeer);
  }

  void unblockWindow(WWindowPeer paramWWindowPeer)
  {
    this.blockedWindows.remove(paramWWindowPeer);
  }

  public native void toFront();

  public native void toBack();

  public void setAlwaysOnTop(boolean paramBoolean)
  {
  }

  public void setDirectory(String paramString)
  {
  }

  public void setFile(String paramString)
  {
  }

  public void setTitle(String paramString)
  {
  }

  public void setResizable(boolean paramBoolean)
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

  public void updateFocusableWindowState()
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
    String str = (String)AccessController.doPrivileged(new PrivilegedAction()
    {
      public Object run()
      {
        ResourceBundle localResourceBundle;
        try
        {
          localResourceBundle = ResourceBundle.getBundle("sun.awt.windows.awtLocalization");
          return localResourceBundle.getString("allFiles");
        }
        catch (MissingResourceException localMissingResourceException)
        {
        }
        return "All Files";
      }
    });
    setFilterString(str);
  }
}