package sun.awt;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Shape;
import java.awt.Window;
import java.awt.image.BufferedImage;
import sun.misc.Unsafe;

public final class AWTAccessor
{
  private static final Unsafe unsafe = Unsafe.getUnsafe();
  private static WindowAccessor windowAccessor;
  private static ComponentAccessor componentAccessor;
  private static AWTEventAccessor awtEventAccessor;

  public static void setWindowAccessor(WindowAccessor paramWindowAccessor)
  {
    windowAccessor = paramWindowAccessor;
  }

  public static WindowAccessor getWindowAccessor()
  {
    if (windowAccessor == null)
      unsafe.ensureClassInitialized(Window.class);
    return windowAccessor;
  }

  public static void setComponentAccessor(ComponentAccessor paramComponentAccessor)
  {
    componentAccessor = paramComponentAccessor;
  }

  public static ComponentAccessor getComponentAccessor()
  {
    if (componentAccessor == null)
      unsafe.ensureClassInitialized(Component.class);
    return componentAccessor;
  }

  public static void setAWTEventAccessor(AWTEventAccessor paramAWTEventAccessor)
  {
    awtEventAccessor = paramAWTEventAccessor;
  }

  public static AWTEventAccessor getAWTEventAccessor()
  {
    return awtEventAccessor;
  }

  public static abstract interface AWTEventAccessor
  {
    public abstract void setSystemGenerated(AWTEvent paramAWTEvent);

    public abstract boolean isSystemGenerated(AWTEvent paramAWTEvent);
  }

  public static abstract interface ComponentAccessor
  {
    public abstract void setBackgroundEraseDisabled(Component paramComponent, boolean paramBoolean);

    public abstract boolean getBackgroundEraseDisabled(Component paramComponent);
  }

  public static abstract interface WindowAccessor
  {
    public abstract float getOpacity(Window paramWindow);

    public abstract void setOpacity(Window paramWindow, float paramFloat);

    public abstract Shape getShape(Window paramWindow);

    public abstract void setShape(Window paramWindow, Shape paramShape);

    public abstract boolean isOpaque(Window paramWindow);

    public abstract void setOpaque(Window paramWindow, boolean paramBoolean);

    public abstract void updateWindow(Window paramWindow, BufferedImage paramBufferedImage);
  }
}