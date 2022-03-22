package sun.awt.windows;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Window;
import java.awt.peer.CanvasPeer;
import java.lang.reflect.Method;
import sun.awt.AWTAccessor;
import sun.awt.AWTAccessor.ComponentAccessor;
import sun.awt.AWTAccessor.WindowAccessor;
import sun.awt.Graphics2Delegate;
import sun.awt.PaintEventDispatcher;
import sun.awt.SunToolkit;

class WCanvasPeer extends WComponentPeer
  implements CanvasPeer
{
  private boolean eraseBackground;
  Method resetGCMethod;

  WCanvasPeer(Component paramComponent)
  {
    super(paramComponent);
    if (AWTAccessor.getComponentAccessor().getBackgroundEraseDisabled(paramComponent))
      disableBackgroundErase();
  }

  public void displayChanged()
  {
    clearLocalGC();
    resetTargetGC();
    super.displayChanged();
  }

  public void resetTargetGC()
  {
    synchronized (this)
    {
      if (this.resetGCMethod == null)
        this.resetGCMethod = WToolkit.getMethod(Component.class, "resetGC", null);
    }
    try
    {
      this.resetGCMethod.invoke(this.target, new Object[0]);
    }
    catch (Exception localException)
    {
      localException.printStackTrace();
    }
  }

  void clearLocalGC()
  {
    this.winGraphicsConfig = null;
  }

  native void create(WComponentPeer paramWComponentPeer);

  void initialize()
  {
    this.eraseBackground = (!(SunToolkit.getSunAwtNoerasebackground()));
    boolean bool = SunToolkit.getSunAwtErasebackgroundonresize();
    if (!(PaintEventDispatcher.getPaintEventDispatcher().shouldDoNativeBackgroundErase((Component)this.target)))
      this.eraseBackground = false;
    setNativeBackgroundErase(this.eraseBackground, bool);
    super.initialize();
    Color localColor = ((Component)this.target).getBackground();
    if (localColor != null)
      setBackground(localColor);
  }

  public void paint(Graphics paramGraphics)
  {
    Dimension localDimension = ((Component)this.target).getSize();
    if ((paramGraphics instanceof Graphics2D) || (paramGraphics instanceof Graphics2Delegate))
    {
      paramGraphics.clearRect(0, 0, localDimension.width, localDimension.height);
    }
    else
    {
      paramGraphics.setColor(((Component)this.target).getBackground());
      paramGraphics.fillRect(0, 0, localDimension.width, localDimension.height);
      paramGraphics.setColor(((Component)this.target).getForeground());
    }
    super.paint(paramGraphics);
  }

  public void print(Graphics paramGraphics)
  {
    if ((!(this.target instanceof Window)) || (AWTAccessor.getWindowAccessor().isOpaque((Window)this.target)))
    {
      Dimension localDimension = ((Component)this.target).getSize();
      if ((paramGraphics instanceof Graphics2D) || (paramGraphics instanceof Graphics2Delegate))
      {
        paramGraphics.clearRect(0, 0, localDimension.width, localDimension.height);
      }
      else
      {
        paramGraphics.setColor(((Component)this.target).getBackground());
        paramGraphics.fillRect(0, 0, localDimension.width, localDimension.height);
        paramGraphics.setColor(((Component)this.target).getForeground());
      }
    }
    super.print(paramGraphics);
  }

  public boolean shouldClearRectBeforePaint()
  {
    return this.eraseBackground;
  }

  void disableBackgroundErase()
  {
    this.eraseBackground = false;
    setNativeBackgroundErase(false, false);
  }

  private native void setNativeBackgroundErase(boolean paramBoolean1, boolean paramBoolean2);
}