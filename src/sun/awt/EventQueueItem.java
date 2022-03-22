package sun.awt;

import java.awt.AWTEvent;

class EventQueueItem
{
  AWTEvent event;
  EventQueueItem next;

  EventQueueItem(AWTEvent paramAWTEvent)
  {
    this.event = paramAWTEvent;
  }
}