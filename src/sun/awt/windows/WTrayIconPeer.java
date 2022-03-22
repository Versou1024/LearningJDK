package sun.awt.windows;

import java.awt.AWTEvent;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.PopupMenu;
import java.awt.TrayIcon;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.ImageObserver;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.awt.peer.TrayIconPeer;
import sun.awt.image.IntegerComponentRaster;

public class WTrayIconPeer extends WObjectPeer
  implements TrayIconPeer
{
  static final int TRAY_ICON_WIDTH = 16;
  static final int TRAY_ICON_HEIGHT = 16;
  static final int TRAY_ICON_MASK_SIZE = 32;
  IconObserver observer = new IconObserver(this);
  boolean firstUpdate = true;
  Frame popupParent = new Frame();
  PopupMenu popup;

  protected void disposeImpl()
  {
    if (this.popupParent != null)
      this.popupParent.dispose();
    this.popupParent.dispose();
    _dispose();
    WToolkit.targetDisposedPeer(this.target, this);
  }

  WTrayIconPeer(TrayIcon paramTrayIcon)
  {
    this.target = paramTrayIcon;
    this.popupParent.addNotify();
    create();
    updateImage();
  }

  public void updateImage()
  {
    Image localImage = ((TrayIcon)this.target).getImage();
    if (localImage != null)
      updateNativeImage(localImage);
  }

  public native void setToolTip(String paramString);

  public synchronized void showPopupMenu(int paramInt1, int paramInt2)
  {
    if (isDisposed())
      return;
    EventQueue.invokeLater(new Runnable(this, paramInt1, paramInt2)
    {
      public void run()
      {
        PopupMenu localPopupMenu = ((TrayIcon)this.this$0.target).getPopupMenu();
        if (this.this$0.popup != localPopupMenu)
        {
          if (this.this$0.popup != null)
            this.this$0.popupParent.remove(this.this$0.popup);
          if (localPopupMenu != null)
            this.this$0.popupParent.add(localPopupMenu);
          this.this$0.popup = localPopupMenu;
        }
        if (this.this$0.popup != null)
          ((WPopupMenuPeer)this.this$0.popup.getPeer()).show(this.this$0.popupParent, new Point(this.val$x, this.val$y));
      }
    });
  }

  public void displayMessage(String paramString1, String paramString2, String paramString3)
  {
    if (paramString1 == null)
      paramString1 = "";
    if (paramString2 == null)
      paramString2 = "";
    _displayMessage(paramString1, paramString2, paramString3);
  }

  synchronized void updateNativeImage(Image paramImage)
  {
    if (isDisposed())
      return;
    boolean bool = ((TrayIcon)this.target).isImageAutoSize();
    BufferedImage localBufferedImage = new BufferedImage(16, 16, 2);
    Graphics2D localGraphics2D = localBufferedImage.createGraphics();
    if (localGraphics2D != null)
      try
      {
        localGraphics2D.setPaintMode();
        localGraphics2D.drawImage(paramImage, 0, 0, (bool) ? 16 : paramImage.getWidth(this.observer), (bool) ? 16 : paramImage.getHeight(this.observer), this.observer);
        createNativeImage(localBufferedImage);
        updateNativeIcon(!(this.firstUpdate));
        if (this.firstUpdate)
          this.firstUpdate = false;
      }
      finally
      {
        localGraphics2D.dispose();
      }
  }

  void createNativeImage(BufferedImage paramBufferedImage)
  {
    WritableRaster localWritableRaster = paramBufferedImage.getRaster();
    byte[] arrayOfByte = new byte[32];
    int[] arrayOfInt = ((DataBufferInt)localWritableRaster.getDataBuffer()).getData();
    int i = arrayOfInt.length;
    int j = localWritableRaster.getWidth();
    for (int k = 0; k < i; ++k)
    {
      int l = k / 8;
      int i1 = 1 << 7 - k % 8;
      if (((arrayOfInt[k] & 0xFF000000) == 0) && (l < arrayOfByte.length))
      {
        int tmp83_81 = l;
        byte[] tmp83_80 = arrayOfByte;
        tmp83_80[tmp83_81] = (byte)(tmp83_80[tmp83_81] | i1);
      }
    }
    if (localWritableRaster instanceof IntegerComponentRaster)
      j = ((IntegerComponentRaster)localWritableRaster).getScanlineStride();
    setNativeIcon(((DataBufferInt)paramBufferedImage.getRaster().getDataBuffer()).getData(), arrayOfByte, j, localWritableRaster.getWidth(), localWritableRaster.getHeight());
  }

  void postEvent(AWTEvent paramAWTEvent)
  {
    WToolkit.postEvent(WToolkit.targetToAppContext(this.target), paramAWTEvent);
  }

  native void create();

  synchronized native void _dispose();

  native void updateNativeIcon(boolean paramBoolean);

  native void setNativeIcon(int[] paramArrayOfInt, byte[] paramArrayOfByte, int paramInt1, int paramInt2, int paramInt3);

  native void _displayMessage(String paramString1, String paramString2, String paramString3);

  class IconObserver
  implements ImageObserver
  {
    public boolean imageUpdate(, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5)
    {
      if ((paramImage != ((TrayIcon)this.this$0.target).getImage()) || (this.this$0.isDisposed()))
        return false;
      if ((paramInt1 & 0x33) != 0)
        this.this$0.updateNativeImage(paramImage);
      return ((paramInt1 & 0x20) == 0);
    }
  }
}