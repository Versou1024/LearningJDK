package sun.awt.event;

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.PaintEvent;

public class IgnorePaintEvent extends PaintEvent
{
  public IgnorePaintEvent(Component paramComponent, int paramInt, Rectangle paramRectangle)
  {
    super(paramComponent, paramInt, paramRectangle);
  }
}