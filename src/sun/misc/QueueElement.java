package sun.misc;

class QueueElement
{
  QueueElement next = null;
  QueueElement prev = null;
  Object obj = null;

  QueueElement(Object paramObject)
  {
    this.obj = paramObject;
  }

  public String toString()
  {
    return "QueueElement[obj=" + this.obj + ((this.prev == null) ? " null" : " prev") + ((this.next == null) ? " null" : " next") + "]";
  }
}