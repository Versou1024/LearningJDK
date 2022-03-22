package sun.awt;

import java.awt.AWTEvent;
import java.awt.Component;

public class UngrabEvent extends AWTEvent
{
  public UngrabEvent(Component paramComponent)
  {
    super(paramComponent, 65535);
  }

  public String toString()
  {
    return "sun.awt.UngrabEvent[" + getSource() + "]";
  }
}