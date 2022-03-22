package sun.misc;

import java.util.Enumeration;
import java.util.NoSuchElementException;

final class LIFOQueueEnumerator
  implements Enumeration
{
  Queue queue;
  QueueElement cursor;

  LIFOQueueEnumerator(Queue paramQueue)
  {
    this.queue = paramQueue;
    this.cursor = paramQueue.head;
  }

  public boolean hasMoreElements()
  {
    return (this.cursor != null);
  }

  public Object nextElement()
  {
    synchronized (this.queue)
    {
      if (this.cursor == null)
        break label37;
      QueueElement localQueueElement = this.cursor;
      this.cursor = this.cursor.next;
      label37: return localQueueElement.obj;
    }
    throw new NoSuchElementException("LIFOQueueEnumerator");
  }
}