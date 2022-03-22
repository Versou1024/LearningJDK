package sun.awt.windows;

import java.awt.Component;
import java.awt.Image;

public class WBufferStrategy
{
  private static native void initIDs(Class paramClass);

  public static native Image getDrawBuffer(Component paramComponent);

  static
  {
    initIDs(Component.class);
  }
}