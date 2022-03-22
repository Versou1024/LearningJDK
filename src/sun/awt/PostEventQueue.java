package sun.awt;

import java.awt.AWTEvent;
import java.awt.EventQueue;

class PostEventQueue
{
  private EventQueueItem queueHead = null;
  private EventQueueItem queueTail = null;
  private final EventQueue eventQueue;

  PostEventQueue(EventQueue paramEventQueue)
  {
    this.eventQueue = paramEventQueue;
  }

  public boolean noEvents()
  {
    return (this.queueHead == null);
  }

  public void flush()
  {
    if (this.queueHead != null)
      synchronized (this)
      {
        EventQueueItem localEventQueueItem = this.queueHead;
        this.queueHead = (this.queueTail = null);
        while (localEventQueueItem != null)
        {
          this.eventQueue.postEvent(localEventQueueItem.event);
          localEventQueueItem = localEventQueueItem.next;
        }
      }
  }

  void postEvent(AWTEvent paramAWTEvent)
  {
    EventQueueItem localEventQueueItem = new EventQueueItem(paramAWTEvent);
    synchronized (this)
    {
      if (this.queueHead == null)
      {
        this.queueHead = (this.queueTail = localEventQueueItem);
      }
      else
      {
        this.queueTail.next = localEventQueueItem;
        this.queueTail = localEventQueueItem;
      }
    }
    SunToolkit.wakeupEventQueue(this.eventQueue, paramAWTEvent.getSource() == AWTAutoShutdown.getInstance());
  }
}