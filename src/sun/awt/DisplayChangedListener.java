package sun.awt;

import java.util.EventListener;

public abstract interface DisplayChangedListener extends EventListener
{
  public abstract void displayChanged();

  public abstract void paletteChanged();
}