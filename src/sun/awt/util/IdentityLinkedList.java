package sun.awt.util;

import java.lang.reflect.Array;
import java.util.AbstractSequentialList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

public class IdentityLinkedList<E> extends AbstractSequentialList<E>
  implements List<E>, Deque<E>
{
  private transient Entry<E> header;
  private transient int size;

  public IdentityLinkedList()
  {
    this.header = new Entry(null, null, null);
    this.size = 0;
    this.header.next = (this.header.previous = this.header);
  }

  public IdentityLinkedList(Collection<? extends E> paramCollection)
  {
    addAll(paramCollection);
  }

  public E getFirst()
  {
    if (this.size == 0)
      throw new NoSuchElementException();
    return this.header.next.element;
  }

  public E getLast()
  {
    if (this.size == 0)
      throw new NoSuchElementException();
    return this.header.previous.element;
  }

  public E removeFirst()
  {
    return remove(this.header.next);
  }

  public E removeLast()
  {
    return remove(this.header.previous);
  }

  public void addFirst(E paramE)
  {
    addBefore(paramE, this.header.next);
  }

  public void addLast(E paramE)
  {
    addBefore(paramE, this.header);
  }

  public boolean contains(Object paramObject)
  {
    return (indexOf(paramObject) != -1);
  }

  public int size()
  {
    return this.size;
  }

  public boolean add(E paramE)
  {
    addBefore(paramE, this.header);
    return true;
  }

  public boolean remove(Object paramObject)
  {
    for (Entry localEntry = this.header.next; localEntry != this.header; localEntry = localEntry.next)
      if (paramObject == localEntry.element)
      {
        remove(localEntry);
        return true;
      }
    return false;
  }

  public boolean addAll(Collection<? extends E> paramCollection)
  {
    return addAll(this.size, paramCollection);
  }

  public boolean addAll(int paramInt, Collection<? extends E> paramCollection)
  {
    if ((paramInt < 0) || (paramInt > this.size))
      throw new IndexOutOfBoundsException("Index: " + paramInt + ", Size: " + this.size);
    Object[] arrayOfObject = paramCollection.toArray();
    int i = arrayOfObject.length;
    if (i == 0)
      return false;
    this.modCount += 1;
    Entry localEntry1 = (paramInt == this.size) ? this.header : entry(paramInt);
    Object localObject = localEntry1.previous;
    for (int j = 0; j < i; ++j)
    {
      Entry localEntry2 = new Entry(arrayOfObject[j], localEntry1, (Entry)localObject);
      ((Entry)localObject).next = localEntry2;
      localObject = localEntry2;
    }
    localEntry1.previous = ((Entry)localObject);
    this.size += i;
    return true;
  }

  public void clear()
  {
    for (Object localObject = this.header.next; localObject != this.header; localObject = localEntry)
    {
      Entry localEntry = ((Entry)localObject).next;
      ((Entry)localObject).next = (((Entry)localObject).previous = null);
      ((Entry)localObject).element = null;
    }
    this.header.next = (this.header.previous = this.header);
    this.size = 0;
    this.modCount += 1;
  }

  public E get(int paramInt)
  {
    return entry(paramInt).element;
  }

  public E set(int paramInt, E paramE)
  {
    Entry localEntry = entry(paramInt);
    Object localObject = localEntry.element;
    localEntry.element = paramE;
    return localObject;
  }

  public void add(int paramInt, E paramE)
  {
    addBefore(paramE, (paramInt == this.size) ? this.header : entry(paramInt));
  }

  public E remove(int paramInt)
  {
    return remove(entry(paramInt));
  }

  private Entry<E> entry(int paramInt)
  {
    int i;
    if ((paramInt < 0) || (paramInt >= this.size))
      throw new IndexOutOfBoundsException("Index: " + paramInt + ", Size: " + this.size);
    Entry localEntry = this.header;
    if (paramInt < this.size >> 1)
      for (i = 0; i <= paramInt; ++i)
        localEntry = localEntry.next;
    else
      for (i = this.size; i > paramInt; --i)
        localEntry = localEntry.previous;
    return localEntry;
  }

  public int indexOf(Object paramObject)
  {
    int i = 0;
    for (Entry localEntry = this.header.next; localEntry != this.header; localEntry = localEntry.next)
    {
      if (paramObject == localEntry.element)
        return i;
      ++i;
    }
    return -1;
  }

  public int lastIndexOf(Object paramObject)
  {
    int i = this.size;
    for (Entry localEntry = this.header.previous; localEntry != this.header; localEntry = localEntry.previous)
    {
      --i;
      if (paramObject == localEntry.element)
        return i;
    }
    return -1;
  }

  public E peek()
  {
    if (this.size == 0)
      return null;
    return getFirst();
  }

  public E element()
  {
    return getFirst();
  }

  public E poll()
  {
    if (this.size == 0)
      return null;
    return removeFirst();
  }

  public E remove()
  {
    return removeFirst();
  }

  public boolean offer(E paramE)
  {
    return add(paramE);
  }

  public boolean offerFirst(E paramE)
  {
    addFirst(paramE);
    return true;
  }

  public boolean offerLast(E paramE)
  {
    addLast(paramE);
    return true;
  }

  public E peekFirst()
  {
    if (this.size == 0)
      return null;
    return getFirst();
  }

  public E peekLast()
  {
    if (this.size == 0)
      return null;
    return getLast();
  }

  public E pollFirst()
  {
    if (this.size == 0)
      return null;
    return removeFirst();
  }

  public E pollLast()
  {
    if (this.size == 0)
      return null;
    return removeLast();
  }

  public void push(E paramE)
  {
    addFirst(paramE);
  }

  public E pop()
  {
    return removeFirst();
  }

  public boolean removeFirstOccurrence(Object paramObject)
  {
    return remove(paramObject);
  }

  public boolean removeLastOccurrence(Object paramObject)
  {
    for (Entry localEntry = this.header.previous; localEntry != this.header; localEntry = localEntry.previous)
      if (paramObject == localEntry.element)
      {
        remove(localEntry);
        return true;
      }
    return false;
  }

  public ListIterator<E> listIterator(int paramInt)
  {
    return new ListItr(this, paramInt);
  }

  private Entry<E> addBefore(E paramE, Entry<E> paramEntry)
  {
    Entry localEntry = new Entry(paramE, paramEntry, paramEntry.previous);
    localEntry.previous.next = localEntry;
    localEntry.next.previous = localEntry;
    this.size += 1;
    this.modCount += 1;
    return localEntry;
  }

  private E remove(Entry<E> paramEntry)
  {
    if (paramEntry == this.header)
      throw new NoSuchElementException();
    Object localObject = paramEntry.element;
    paramEntry.previous.next = paramEntry.next;
    paramEntry.next.previous = paramEntry.previous;
    paramEntry.next = (paramEntry.previous = null);
    paramEntry.element = null;
    this.size -= 1;
    this.modCount += 1;
    return localObject;
  }

  public Iterator<E> descendingIterator()
  {
    return new DescendingIterator(this, null);
  }

  public Object[] toArray()
  {
    Object[] arrayOfObject = new Object[this.size];
    int i = 0;
    for (Entry localEntry = this.header.next; localEntry != this.header; localEntry = localEntry.next)
      arrayOfObject[(i++)] = localEntry.element;
    return arrayOfObject;
  }

  public <T> T[] toArray(T[] paramArrayOfT)
  {
    if (paramArrayOfT.length < this.size)
      paramArrayOfT = (Object[])(Object[])Array.newInstance(paramArrayOfT.getClass().getComponentType(), this.size);
    int i = 0;
    T[] arrayOfT = paramArrayOfT;
    for (Entry localEntry = this.header.next; localEntry != this.header; localEntry = localEntry.next)
      arrayOfT[(i++)] = localEntry.element;
    if (paramArrayOfT.length > this.size)
      paramArrayOfT[this.size] = null;
    return paramArrayOfT;
  }

  private class DescendingIterator
  implements Iterator
  {
    final IdentityLinkedList<E>.ListItr itr = new IdentityLinkedList.ListItr(this.this$0, this.this$0.size());

    public boolean hasNext()
    {
      return this.itr.hasPrevious();
    }

    public E next()
    {
      return this.itr.previous();
    }

    public void remove()
    {
      this.itr.remove();
    }
  }

  private static class Entry<E>
  {
    E element;
    Entry<E> next;
    Entry<E> previous;

    Entry(E paramE, Entry<E> paramEntry1, Entry<E> paramEntry2)
    {
      this.element = paramE;
      this.next = paramEntry1;
      this.previous = paramEntry2;
    }
  }

  private class ListItr
  implements ListIterator<E>
  {
    private IdentityLinkedList.Entry<E> lastReturned = IdentityLinkedList.access$000(this.this$0);
    private IdentityLinkedList.Entry<E> next;
    private int nextIndex;
    private int expectedModCount = IdentityLinkedList.access$100(this.this$0);

    ListItr(, int paramInt)
    {
      if ((paramInt < 0) || (paramInt > IdentityLinkedList.access$200(paramIdentityLinkedList)))
        throw new IndexOutOfBoundsException("Index: " + paramInt + ", Size: " + IdentityLinkedList.access$200(paramIdentityLinkedList));
      if (paramInt < IdentityLinkedList.access$200(paramIdentityLinkedList) >> 1)
      {
        this.next = IdentityLinkedList.access$000(paramIdentityLinkedList).next;
        this.nextIndex = 0;
        while (true)
        {
          if (this.nextIndex >= paramInt)
            return;
          this.next = this.next.next;
          this.nextIndex += 1;
        }
      }
      this.next = IdentityLinkedList.access$000(paramIdentityLinkedList);
      this.nextIndex = IdentityLinkedList.access$200(paramIdentityLinkedList);
      while (this.nextIndex > paramInt)
      {
        this.next = this.next.previous;
        this.nextIndex -= 1;
      }
    }

    public boolean hasNext()
    {
      return (this.nextIndex != IdentityLinkedList.access$200(this.this$0));
    }

    public E next()
    {
      checkForComodification();
      if (this.nextIndex == IdentityLinkedList.access$200(this.this$0))
        throw new NoSuchElementException();
      this.lastReturned = this.next;
      this.next = this.next.next;
      this.nextIndex += 1;
      return this.lastReturned.element;
    }

    public boolean hasPrevious()
    {
      return (this.nextIndex != 0);
    }

    public E previous()
    {
      if (this.nextIndex == 0)
        throw new NoSuchElementException();
      this.lastReturned = (this.next = this.next.previous);
      this.nextIndex -= 1;
      checkForComodification();
      return this.lastReturned.element;
    }

    public int nextIndex()
    {
      return this.nextIndex;
    }

    public int previousIndex()
    {
      return (this.nextIndex - 1);
    }

    public void remove()
    {
      checkForComodification();
      IdentityLinkedList.Entry localEntry = this.lastReturned.next;
      try
      {
        IdentityLinkedList.access$300(this.this$0, this.lastReturned);
      }
      catch (NoSuchElementException localNoSuchElementException)
      {
        throw new IllegalStateException();
      }
      if (this.next == this.lastReturned)
        this.next = localEntry;
      else
        this.nextIndex -= 1;
      this.lastReturned = IdentityLinkedList.access$000(this.this$0);
      this.expectedModCount += 1;
    }

    public void set()
    {
      if (this.lastReturned == IdentityLinkedList.access$000(this.this$0))
        throw new IllegalStateException();
      checkForComodification();
      this.lastReturned.element = paramE;
    }

    public void add()
    {
      checkForComodification();
      this.lastReturned = IdentityLinkedList.access$000(this.this$0);
      IdentityLinkedList.access$400(this.this$0, paramE, this.next);
      this.nextIndex += 1;
      this.expectedModCount += 1;
    }

    final void checkForComodification()
    {
      if (IdentityLinkedList.access$500(this.this$0) != this.expectedModCount)
        throw new ConcurrentModificationException();
    }
  }
}