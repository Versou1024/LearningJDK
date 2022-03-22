package sun.awt;

import java.awt.event.WindowEvent;

public abstract interface WindowClosingListener
{
  public abstract RuntimeException windowClosingNotify(WindowEvent paramWindowEvent);

  public abstract RuntimeException windowClosingDelivered(WindowEvent paramWindowEvent);
}