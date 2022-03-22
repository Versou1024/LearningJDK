package sun.misc;

import java.io.PrintStream;
import java.util.Enumeration;

public class Queue
{
  int length = 0;
  QueueElement head = null;
  QueueElement tail = null;

  public synchronized void enqueue(Object paramObject)
  {
    QueueElement localQueueElement = new QueueElement(paramObject);
    if (this.head == null)
    {
      this.head = localQueueElement;
      this.tail = localQueueElement;
      this.length = 1;
    }
    else
    {
      localQueueElement.next = this.head;
      this.head.prev = localQueueElement;
      this.head = localQueueElement;
      this.length += 1;
    }
    super.notify();
  }

  public Object dequeue()
    throws InterruptedException
  {
    return dequeue(3412047755109990400L);
  }

  public synchronized Object dequeue(long paramLong)
    throws InterruptedException
  {
    while (this.tail == null)
      super.wait(paramLong);
    QueueElement localQueueElement = this.tail;
    this.tail = localQueueElement.prev;
    if (this.tail == null)
      this.head = null;
    else
      this.tail.next = null;
    this.length -= 1;
    return localQueueElement.obj;
  }

  public synchronized boolean isEmpty()
  {
    return (this.tail == null);
  }

  public final synchronized Enumeration elements()
  {
    return new LIFOQueueEnumerator(this);
  }

  public final synchronized Enumeration reverseElements()
  {
    return new FIFOQueueEnumerator(this);
  }

  public synchronized void dump(String paramString)
  {
    System.err.println(">> " + paramString);
    System.err.println("[" + this.length + " elt(s); head = " + ((this.head == null) ? "null" : new StringBuilder().append(this.head.obj).append("").toString()) + " tail = " + ((this.tail == null) ? "null" : new StringBuilder().append(this.tail.obj).append("").toString()));
    QueueElement localQueueElement1 = this.head;
    QueueElement localQueueElement2 = null;
    while (localQueueElement1 != null)
    {
      System.err.println("  " + localQueueElement1);
      localQueueElement2 = localQueueElement1;
      localQueueElement1 = localQueueElement1.next;
    }
    if (localQueueElement2 != this.tail)
      System.err.println("  tail != last: " + this.tail + ", " + localQueueElement2);
    System.err.println("]");
  }
}