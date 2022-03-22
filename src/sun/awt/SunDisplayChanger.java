package sun.awt;

import java.awt.IllegalComponentStateException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public class SunDisplayChanger
{
  private static final DebugHelper dbg = DebugHelper.create(SunDisplayChanger.class);
  private Map listeners = Collections.synchronizedMap(new WeakHashMap(1));

  public void add(DisplayChangedListener paramDisplayChangedListener)
  {
    this.listeners.put(paramDisplayChangedListener, null);
  }

  public void remove(DisplayChangedListener paramDisplayChangedListener)
  {
    this.listeners.remove(paramDisplayChangedListener);
  }

  public void notifyListeners()
  {
    HashMap localHashMap;
    synchronized (this.listeners)
    {
      localHashMap = new HashMap(this.listeners);
    }
    Set localSet = localHashMap.keySet();
    ??? = localSet.iterator();
    while (((Iterator)???).hasNext())
    {
      DisplayChangedListener localDisplayChangedListener = (DisplayChangedListener)((Iterator)???).next();
      try
      {
        localDisplayChangedListener.displayChanged();
      }
      catch (IllegalComponentStateException localIllegalComponentStateException)
      {
        this.listeners.remove(localDisplayChangedListener);
      }
    }
  }

  public void notifyPaletteChanged()
  {
    HashMap localHashMap;
    synchronized (this.listeners)
    {
      localHashMap = new HashMap(this.listeners);
    }
    Set localSet = localHashMap.keySet();
    ??? = localSet.iterator();
    while (((Iterator)???).hasNext())
    {
      DisplayChangedListener localDisplayChangedListener = (DisplayChangedListener)((Iterator)???).next();
      try
      {
        localDisplayChangedListener.paletteChanged();
      }
      catch (IllegalComponentStateException localIllegalComponentStateException)
      {
        this.listeners.remove(localDisplayChangedListener);
      }
    }
  }
}