package sun.awt;

import java.lang.reflect.Array;
import java.util.EventListener;

public class EventListenerAggregate
{
  private EventListener[] listenerList;

  public EventListenerAggregate(Class paramClass)
  {
    if (paramClass == null)
      throw new NullPointerException("listener class is null");
    if (!(EventListener.class.isAssignableFrom(paramClass)))
      throw new ClassCastException("listener class " + paramClass + " is not assignable to EventListener");
    this.listenerList = ((EventListener[])(EventListener[])Array.newInstance(paramClass, 0));
  }

  private Class getListenerClass()
  {
    return this.listenerList.getClass().getComponentType();
  }

  public synchronized void add(EventListener paramEventListener)
  {
    Class localClass = getListenerClass();
    if (!(localClass.isInstance(paramEventListener)))
      throw new ClassCastException("listener " + paramEventListener + " is not " + "an instance of listener class " + localClass);
    EventListener[] arrayOfEventListener = (EventListener[])(EventListener[])Array.newInstance(localClass, this.listenerList.length + 1);
    System.arraycopy(this.listenerList, 0, arrayOfEventListener, 0, this.listenerList.length);
    arrayOfEventListener[this.listenerList.length] = paramEventListener;
    this.listenerList = arrayOfEventListener;
  }

  public synchronized boolean remove(EventListener paramEventListener)
  {
    Class localClass = getListenerClass();
    if (!(localClass.isInstance(paramEventListener)))
      throw new ClassCastException("listener " + paramEventListener + " is not " + "an instance of listener class " + localClass);
    for (int i = 0; i < this.listenerList.length; ++i)
      if (this.listenerList[i].equals(paramEventListener))
      {
        EventListener[] arrayOfEventListener = (EventListener[])(EventListener[])Array.newInstance(localClass, this.listenerList.length - 1);
        System.arraycopy(this.listenerList, 0, arrayOfEventListener, 0, i);
        System.arraycopy(this.listenerList, i + 1, arrayOfEventListener, i, this.listenerList.length - i - 1);
        this.listenerList = arrayOfEventListener;
        return true;
      }
    return false;
  }

  public synchronized EventListener[] getListenersInternal()
  {
    return this.listenerList;
  }

  public synchronized EventListener[] getListenersCopy()
  {
    return ((this.listenerList.length == 0) ? this.listenerList : (EventListener[])(EventListener[])this.listenerList.clone());
  }

  public synchronized int size()
  {
    return this.listenerList.length;
  }

  public synchronized boolean isEmpty()
  {
    return (this.listenerList.length == 0);
  }
}