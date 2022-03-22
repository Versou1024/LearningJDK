package sun.misc;

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.security.AccessController;
import java.security.PrivilegedAction;

public class Cleaner extends PhantomReference
{
  private static final ReferenceQueue dummyQueue = new ReferenceQueue();
  private static Cleaner first = null;
  private Cleaner next = null;
  private Cleaner prev = null;
  private final Runnable thunk;

  private static synchronized Cleaner add(Cleaner paramCleaner)
  {
    if (first != null)
    {
      paramCleaner.next = first;
      first.prev = paramCleaner;
    }
    first = paramCleaner;
    return paramCleaner;
  }

  private static synchronized boolean remove(Cleaner paramCleaner)
  {
    if (paramCleaner.next == paramCleaner)
      return false;
    if (first == paramCleaner)
      if (paramCleaner.next != null)
        first = paramCleaner.next;
      else
        first = paramCleaner.prev;
    if (paramCleaner.next != null)
      paramCleaner.next.prev = paramCleaner.prev;
    if (paramCleaner.prev != null)
      paramCleaner.prev.next = paramCleaner.next;
    paramCleaner.next = paramCleaner;
    paramCleaner.prev = paramCleaner;
    return true;
  }

  private Cleaner(Object paramObject, Runnable paramRunnable)
  {
    super(paramObject, dummyQueue);
    this.thunk = paramRunnable;
  }

  public static Cleaner create(Object paramObject, Runnable paramRunnable)
  {
    if (paramRunnable == null)
      return null;
    return add(new Cleaner(paramObject, paramRunnable));
  }

  public void clean()
  {
    if (!(remove(this)))
      return;
    try
    {
      this.thunk.run();
    }
    catch (Throwable localThrowable)
    {
      AccessController.doPrivileged(new PrivilegedAction(this, localThrowable)
      {
        public Object run()
        {
          if (System.err != null)
            new Error("Cleaner terminated abnormally", this.val$x).printStackTrace();
          System.exit(1);
          return null;
        }
      });
    }
  }
}