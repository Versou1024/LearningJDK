package sun.java2d;

import java.io.PrintStream;
import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Hashtable;
import sun.security.action.GetPropertyAction;
import sun.security.action.LoadLibraryAction;

public class Disposer
  implements Runnable
{
  private static final ReferenceQueue queue = new ReferenceQueue();
  private static final Hashtable records = new Hashtable();
  private static Disposer disposerInstance;
  public static final int WEAK = 0;
  public static final int PHANTOM = 1;
  public static int refType = 1;

  public static void addRecord(Object paramObject, long paramLong1, long paramLong2)
  {
    disposerInstance.add(paramObject, new DefaultDisposerRecord(paramLong1, paramLong2));
  }

  public static void addRecord(Object paramObject, DisposerRecord paramDisposerRecord)
  {
    disposerInstance.add(paramObject, paramDisposerRecord);
  }

  synchronized void add(Object paramObject, DisposerRecord paramDisposerRecord)
  {
    Object localObject;
    if (paramObject instanceof DisposerTarget)
      paramObject = ((DisposerTarget)paramObject).getDisposerReferent();
    if (refType == 1)
      localObject = new PhantomReference(paramObject, queue);
    else
      localObject = new WeakReference(paramObject, queue);
    records.put(localObject, paramDisposerRecord);
  }

  public void run()
  {
    Reference localReference;
    try
    {
      localReference = queue.remove();
      ((Reference)localReference).clear();
      DisposerRecord localDisposerRecord = (DisposerRecord)records.remove(localReference);
      localDisposerRecord.dispose();
      localReference = null;
      localDisposerRecord = null;
    }
    catch (Exception localException)
    {
      System.out.println("Exception while removing reference: " + localException);
      localException.printStackTrace();
    }
  }

  private static native void initIDs();

  public static void addReference(Reference paramReference, DisposerRecord paramDisposerRecord)
  {
    records.put(paramReference, paramDisposerRecord);
  }

  public static void addObjectRecord(Object paramObject, DisposerRecord paramDisposerRecord)
  {
    records.put(new WeakReference(paramObject, queue), paramDisposerRecord);
  }

  public static ReferenceQueue getQueue()
  {
    return queue;
  }

  static
  {
    AccessController.doPrivileged(new LoadLibraryAction("awt"));
    initIDs();
    String str = (String)AccessController.doPrivileged(new GetPropertyAction("sun.java2d.reftype"));
    if (str != null)
      if (str.equals("weak"))
      {
        refType = 0;
        System.err.println("Using WEAK refs");
      }
      else
      {
        refType = 1;
        System.err.println("Using PHANTOM refs");
      }
    disposerInstance = new Disposer();
    AccessController.doPrivileged(new PrivilegedAction()
    {
      public Object run()
      {
        Object localObject1 = Thread.currentThread().getThreadGroup();
        for (Object localObject2 = localObject1; localObject2 != null; localObject2 = ((ThreadGroup)localObject1).getParent())
          localObject1 = localObject2;
        localObject2 = new Thread((ThreadGroup)localObject1, Disposer.access$000(), "Java2D Disposer");
        ((Thread)localObject2).setDaemon(true);
        ((Thread)localObject2).setPriority(10);
        ((Thread)localObject2).start();
        return null;
      }
    });
  }
}