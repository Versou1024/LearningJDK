package sun.management;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanNotificationInfo;
import javax.management.Notification;
import javax.management.NotificationEmitter;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;

abstract class NotificationEmitterSupport
  implements NotificationEmitter
{
  private Object listenerLock = new Object();
  private List listenerList = Collections.EMPTY_LIST;

  public void addNotificationListener(NotificationListener paramNotificationListener, NotificationFilter paramNotificationFilter, Object paramObject)
  {
    if (paramNotificationListener == null)
      throw new IllegalArgumentException("Listener can't be null");
    synchronized (this.listenerLock)
    {
      ArrayList localArrayList = new ArrayList(this.listenerList.size() + 1);
      localArrayList.addAll(this.listenerList);
      localArrayList.add(new ListenerInfo(this, paramNotificationListener, paramNotificationFilter, paramObject));
      this.listenerList = localArrayList;
    }
  }

  public void removeNotificationListener(NotificationListener paramNotificationListener)
    throws ListenerNotFoundException
  {
    synchronized (this.listenerLock)
    {
      ArrayList localArrayList = new ArrayList(this.listenerList);
      for (int i = localArrayList.size() - 1; i >= 0; --i)
      {
        ListenerInfo localListenerInfo = (ListenerInfo)localArrayList.get(i);
        if (localListenerInfo.listener == paramNotificationListener)
          localArrayList.remove(i);
      }
      if (localArrayList.size() == this.listenerList.size())
        throw new ListenerNotFoundException("Listener not registered");
      this.listenerList = localArrayList;
    }
  }

  public void removeNotificationListener(NotificationListener paramNotificationListener, NotificationFilter paramNotificationFilter, Object paramObject)
    throws ListenerNotFoundException
  {
    int i = 0;
    synchronized (this.listenerLock)
    {
      ArrayList localArrayList = new ArrayList(this.listenerList);
      int j = localArrayList.size();
      int k = 0;
      while (true)
      {
        if (k >= j)
          break label113;
        ListenerInfo localListenerInfo = (ListenerInfo)localArrayList.get(k);
        if (localListenerInfo.listener == paramNotificationListener)
        {
          i = 1;
          if ((localListenerInfo.filter == paramNotificationFilter) && (localListenerInfo.handback == paramObject))
          {
            localArrayList.remove(k);
            this.listenerList = localArrayList;
            return;
          }
        }
        label113: ++k;
      }
    }
    if (i != 0)
      throw new ListenerNotFoundException("Listener not registered with this filter and handback");
    throw new ListenerNotFoundException("Listener not registered");
  }

  void sendNotification(Notification paramNotification)
  {
    List localList;
    if (paramNotification == null)
      return;
    synchronized (this.listenerLock)
    {
      localList = this.listenerList;
    }
    int i = localList.size();
    for (int j = 0; j < i; ++j)
    {
      ListenerInfo localListenerInfo = (ListenerInfo)localList.get(j);
      if ((localListenerInfo.filter == null) || (localListenerInfo.filter.isNotificationEnabled(paramNotification)))
        try
        {
          localListenerInfo.listener.handleNotification(paramNotification, localListenerInfo.handback);
        }
        catch (Exception localException)
        {
          localException.printStackTrace();
          throw new InternalError("Error in invoking listener");
        }
    }
  }

  boolean hasListeners()
  {
    synchronized (this.listenerLock)
    {
      return ((!(this.listenerList.isEmpty())) ? 1 : false);
    }
  }

  public abstract MBeanNotificationInfo[] getNotificationInfo();

  private class ListenerInfo
  {
    public NotificationListener listener;
    NotificationFilter filter;
    Object handback;

    public ListenerInfo(, NotificationListener paramNotificationListener, NotificationFilter paramNotificationFilter, Object paramObject)
    {
      this.listener = paramNotificationListener;
      this.filter = paramNotificationFilter;
      this.handback = paramObject;
    }
  }
}