package sun.java2d.pipe.hw;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class AccelDeviceEventNotifier
{
  private static AccelDeviceEventNotifier theInstance;
  public static final int DEVICE_RESET = 0;
  public static final int DEVICE_DISPOSED = 1;
  private final Map<AccelDeviceEventListener, Integer> listeners = Collections.synchronizedMap(new HashMap(1));

  private static synchronized AccelDeviceEventNotifier getInstance(boolean paramBoolean)
  {
    if ((theInstance == null) && (paramBoolean))
      theInstance = new AccelDeviceEventNotifier();
    return theInstance;
  }

  public static final void eventOccured(int paramInt1, int paramInt2)
  {
    AccelDeviceEventNotifier localAccelDeviceEventNotifier = getInstance(false);
    if (localAccelDeviceEventNotifier != null)
      localAccelDeviceEventNotifier.notifyListeners(paramInt2, paramInt1);
  }

  public static final void addListener(AccelDeviceEventListener paramAccelDeviceEventListener, int paramInt)
  {
    getInstance(true).add(paramAccelDeviceEventListener, paramInt);
  }

  public static final void removeListener(AccelDeviceEventListener paramAccelDeviceEventListener)
  {
    getInstance(true).remove(paramAccelDeviceEventListener);
  }

  private final void add(AccelDeviceEventListener paramAccelDeviceEventListener, int paramInt)
  {
    this.listeners.put(paramAccelDeviceEventListener, Integer.valueOf(paramInt));
  }

  private final void remove(AccelDeviceEventListener paramAccelDeviceEventListener)
  {
    this.listeners.remove(paramAccelDeviceEventListener);
  }

  private final void notifyListeners(int paramInt1, int paramInt2)
  {
    HashMap localHashMap;
    synchronized (this.listeners)
    {
      localHashMap = new HashMap(this.listeners);
    }
    Set localSet = localHashMap.keySet();
    ??? = localSet.iterator();
    while (true)
    {
      AccelDeviceEventListener localAccelDeviceEventListener;
      while (true)
      {
        if (!(((Iterator)???).hasNext()))
          return;
        localAccelDeviceEventListener = (AccelDeviceEventListener)((Iterator)???).next();
        Integer localInteger = (Integer)localHashMap.get(localAccelDeviceEventListener);
        if ((localInteger == null) || (localInteger.intValue() == paramInt2))
          break;
      }
      if (paramInt1 == 0)
        localAccelDeviceEventListener.onDeviceReset();
      else if (paramInt1 == 1)
        localAccelDeviceEventListener.onDeviceDispose();
    }
  }
}