package sun.awt.windows;

import java.awt.AWTKeyStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import sun.awt.EmbeddedFrame;
import sun.awt.image.ByteInterleavedRaster;

public class WEmbeddedFrame extends EmbeddedFrame
{
  private long handle;
  private int bandWidth;
  private int bandHeight;
  private int imgWid;
  private int imgHgt;
  private static final int MAX_BAND_SIZE = 30720;
  private static Field peerField;

  public WEmbeddedFrame()
  {
    this(3412047308433391616L);
  }

  @Deprecated
  public WEmbeddedFrame(int paramInt)
  {
    this(paramInt);
  }

  public WEmbeddedFrame(long paramLong)
  {
    this.bandWidth = 0;
    this.bandHeight = 0;
    this.imgWid = 0;
    this.imgHgt = 0;
    this.handle = paramLong;
    if (paramLong != 3412046810217185280L)
    {
      addNotify();
      show();
    }
  }

  public void addNotify()
  {
    if (getPeer() == null)
    {
      WToolkit localWToolkit = (WToolkit)Toolkit.getDefaultToolkit();
      setPeer(localWToolkit.createEmbeddedFrame(this));
    }
    super.addNotify();
  }

  public long getEmbedderHandle()
  {
    return this.handle;
  }

  void print(int paramInt)
  {
    BufferedImage localBufferedImage = null;
    int i = 1;
    int j = 1;
    if (isPrinterDC(paramInt))
    {
      i = 4;
      j = 4;
    }
    int k = getHeight();
    if (localBufferedImage == null)
    {
      this.bandWidth = getWidth();
      if (this.bandWidth % 4 != 0)
        this.bandWidth += 4 - this.bandWidth % 4;
      if (this.bandWidth <= 0)
        return;
      this.bandHeight = Math.min(30720 / this.bandWidth, k);
      this.imgWid = (this.bandWidth * i);
      this.imgHgt = (this.bandHeight * j);
      localBufferedImage = new BufferedImage(this.imgWid, this.imgHgt, 5);
    }
    Graphics localGraphics = localBufferedImage.getGraphics();
    localGraphics.setColor(Color.white);
    Graphics2D localGraphics2D = (Graphics2D)localBufferedImage.getGraphics();
    localGraphics2D.translate(0, this.imgHgt);
    localGraphics2D.scale(i, -j);
    ByteInterleavedRaster localByteInterleavedRaster = (ByteInterleavedRaster)localBufferedImage.getRaster();
    byte[] arrayOfByte = localByteInterleavedRaster.getDataStorage();
    int l = 0;
    while (l < k)
    {
      localGraphics.fillRect(0, 0, this.bandWidth, this.bandHeight);
      printComponents(localGraphics2D);
      int i1 = 0;
      int i2 = this.bandHeight;
      int i3 = this.imgHgt;
      if (l + this.bandHeight > k)
      {
        i2 = k - l;
        i3 = i2 * j;
        i1 = this.imgWid * (this.imgHgt - i3) * 3;
      }
      printBand(paramInt, arrayOfByte, i1, 0, 0, this.imgWid, i3, 0, l, this.bandWidth, i2);
      localGraphics2D.translate(0, -this.bandHeight);
      l += this.bandHeight;
    }
  }

  protected native boolean isPrinterDC(long paramLong);

  protected native void printBand(long paramLong, byte[] paramArrayOfByte, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6, int paramInt7, int paramInt8, int paramInt9);

  private static native void initIDs();

  public void activateEmbeddingTopLevel()
  {
  }

  public void synthesizeWindowActivation(boolean paramBoolean)
  {
    if ((!(paramBoolean)) || (EventQueue.isDispatchThread()))
      ((WEmbeddedFramePeer)getPeer()).synthesizeWmActivate(paramBoolean);
    else
      EventQueue.invokeLater(new Runnable(this)
      {
        public void run()
        {
          ((WEmbeddedFramePeer)this.this$0.getPeer()).synthesizeWmActivate(true);
        }
      });
  }

  public void registerAccelerator(AWTKeyStroke paramAWTKeyStroke)
  {
  }

  public void unregisterAccelerator(AWTKeyStroke paramAWTKeyStroke)
  {
  }

  public void notifyModalBlocked(Dialog paramDialog, boolean paramBoolean)
  {
    try
    {
      notifyModalBlockedImpl((WEmbeddedFramePeer)(WEmbeddedFramePeer)peerField.get(this), (WWindowPeer)(WWindowPeer)peerField.get(paramDialog), paramBoolean);
    }
    catch (Exception localException)
    {
      localException.printStackTrace(System.err);
    }
  }

  native void notifyModalBlockedImpl(WEmbeddedFramePeer paramWEmbeddedFramePeer, WWindowPeer paramWWindowPeer, boolean paramBoolean);

  static
  {
    initIDs();
    peerField = WToolkit.getField(Component.class, "peer");
  }
}