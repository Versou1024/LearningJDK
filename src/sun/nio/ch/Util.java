package sun.nio.ch;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.Selector;
import java.nio.channels.spi.SelectorProvider;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import sun.misc.Cleaner;
import sun.misc.Unsafe;
import sun.misc.VM;
import sun.security.action.GetPropertyAction;
import sun.security.action.LoadLibraryAction;

class Util
{
  private static final int TEMP_BUF_POOL_SIZE = 3;
  private static ThreadLocal[] bufferPool;
  private static ThreadLocal localSelector;
  private static ThreadLocal localSelectorWrapper;
  private static Unsafe unsafe;
  private static int pageSize;
  private static Constructor directByteBufferConstructor;
  private static Constructor directByteBufferRConstructor;
  private static String bugLevel;
  private static boolean loaded;

  static ByteBuffer getTemporaryDirectBuffer(int paramInt)
  {
    ByteBuffer localByteBuffer = null;
    for (int i = 0; i < 3; ++i)
    {
      SoftReference localSoftReference = (SoftReference)(SoftReference)bufferPool[i].get();
      if (localSoftReference != null)
        if (((localByteBuffer = (ByteBuffer)localSoftReference.get()) != null) && (localByteBuffer.capacity() >= paramInt))
        {
          localByteBuffer.rewind();
          localByteBuffer.limit(paramInt);
          bufferPool[i].set(null);
          return localByteBuffer;
        }
    }
    return ByteBuffer.allocateDirect(paramInt);
  }

  static void releaseTemporaryDirectBuffer(ByteBuffer paramByteBuffer)
  {
    SoftReference localSoftReference;
    if (paramByteBuffer == null)
      return;
    for (int i = 0; i < 3; ++i)
    {
      localSoftReference = (SoftReference)(SoftReference)bufferPool[i].get();
      if ((localSoftReference == null) || (localSoftReference.get() == null))
      {
        bufferPool[i].set(new SoftReference(paramByteBuffer));
        return;
      }
    }
    for (i = 0; i < 3; ++i)
    {
      localSoftReference = (SoftReference)(SoftReference)bufferPool[i].get();
      ByteBuffer localByteBuffer = (ByteBuffer)localSoftReference.get();
      if ((localByteBuffer == null) || (paramByteBuffer.capacity() > localByteBuffer.capacity()))
      {
        bufferPool[i].set(new SoftReference(paramByteBuffer));
        return;
      }
    }
  }

  static Selector getTemporarySelector(SelectableChannel paramSelectableChannel)
    throws IOException
  {
    SoftReference localSoftReference = (SoftReference)localSelector.get();
    SelectorWrapper localSelectorWrapper1 = null;
    Object localObject = null;
    if (localSoftReference != null)
      if ((localSelectorWrapper1 = (SelectorWrapper)localSoftReference.get()) != null)
        if (((localObject = localSelectorWrapper1.get()) != null) && (((Selector)localObject).provider() == paramSelectableChannel.provider()))
          break label83;
    localObject = paramSelectableChannel.provider().openSelector();
    localSelector.set(new SoftReference(new SelectorWrapper((Selector)localObject, null)));
    label83: localSelectorWrapper.set(localSelectorWrapper1);
    return ((Selector)localObject);
  }

  static void releaseTemporarySelector(Selector paramSelector)
    throws IOException
  {
    paramSelector.selectNow();
    if ((!($assertionsDisabled)) && (!(paramSelector.keys().isEmpty())))
      throw new AssertionError("Temporary selector not empty");
    localSelectorWrapper.set(null);
  }

  static ByteBuffer[] subsequence(ByteBuffer[] paramArrayOfByteBuffer, int paramInt1, int paramInt2)
  {
    if ((paramInt1 == 0) && (paramInt2 == paramArrayOfByteBuffer.length))
      return paramArrayOfByteBuffer;
    int i = paramInt2;
    ByteBuffer[] arrayOfByteBuffer = new ByteBuffer[i];
    for (int j = 0; j < i; ++j)
      arrayOfByteBuffer[j] = paramArrayOfByteBuffer[(paramInt1 + j)];
    return arrayOfByteBuffer;
  }

  static <E> Set<E> ungrowableSet(Set<E> paramSet)
  {
    return new Set(paramSet)
    {
      public int size()
      {
        return this.val$s.size();
      }

      public boolean isEmpty()
      {
        return this.val$s.isEmpty();
      }

      public boolean contains(Object paramObject)
      {
        return this.val$s.contains(paramObject);
      }

      public Object[] toArray()
      {
        return this.val$s.toArray();
      }

      public <T> T[] toArray(T[] paramArrayOfT)
      {
        return this.val$s.toArray(paramArrayOfT);
      }

      public String toString()
      {
        return this.val$s.toString();
      }

      public Iterator<E> iterator()
      {
        return this.val$s.iterator();
      }

      public boolean equals(Object paramObject)
      {
        return this.val$s.equals(paramObject);
      }

      public int hashCode()
      {
        return this.val$s.hashCode();
      }

      public void clear()
      {
        this.val$s.clear();
      }

      public boolean remove(Object paramObject)
      {
        return this.val$s.remove(paramObject);
      }

      public boolean containsAll(Collection<?> paramCollection)
      {
        return this.val$s.containsAll(paramCollection);
      }

      public boolean removeAll(Collection<?> paramCollection)
      {
        return this.val$s.removeAll(paramCollection);
      }

      public boolean retainAll(Collection<?> paramCollection)
      {
        return this.val$s.retainAll(paramCollection);
      }

      public boolean add(E paramE)
      {
        throw new UnsupportedOperationException();
      }

      public boolean addAll(Collection<? extends E> paramCollection)
      {
        throw new UnsupportedOperationException();
      }
    };
  }

  private static byte _get(long paramLong)
  {
    return unsafe.getByte(paramLong);
  }

  private static void _put(long paramLong, byte paramByte)
  {
    unsafe.putByte(paramLong, paramByte);
  }

  static void erase(ByteBuffer paramByteBuffer)
  {
    unsafe.setMemory(((DirectBuffer)paramByteBuffer).address(), paramByteBuffer.capacity(), 0);
  }

  static Unsafe unsafe()
  {
    return unsafe;
  }

  static int pageSize()
  {
    if (pageSize == -1)
      pageSize = unsafe().pageSize();
    return pageSize;
  }

  private static void initDBBConstructor()
  {
    AccessController.doPrivileged(new PrivilegedAction()
    {
      public Object run()
      {
        Class localClass;
        try
        {
          localClass = Class.forName("java.nio.DirectByteBuffer");
          Util.access$202(localClass.getDeclaredConstructor(new Class[] { Integer.TYPE, Long.TYPE, Runnable.class }));
          Util.access$200().setAccessible(true);
        }
        catch (ClassNotFoundException localClassNotFoundException)
        {
          throw new InternalError();
        }
        catch (NoSuchMethodException localNoSuchMethodException)
        {
          throw new InternalError();
        }
        catch (IllegalArgumentException localIllegalArgumentException)
        {
          throw new InternalError();
        }
        catch (ClassCastException localClassCastException)
        {
          throw new InternalError();
        }
        return null;
      }
    });
  }

  static MappedByteBuffer newMappedByteBuffer(int paramInt, long paramLong, Runnable paramRunnable)
  {
    MappedByteBuffer localMappedByteBuffer;
    if (directByteBufferConstructor == null)
      initDBBConstructor();
    try
    {
      localMappedByteBuffer = (MappedByteBuffer)directByteBufferConstructor.newInstance(new Object[] { new Integer(paramInt), new Long(paramLong), paramRunnable });
    }
    catch (InstantiationException localInstantiationException)
    {
      throw new InternalError();
    }
    catch (IllegalAccessException localIllegalAccessException)
    {
      throw new InternalError();
    }
    catch (InvocationTargetException localInvocationTargetException)
    {
      throw new InternalError();
    }
    return localMappedByteBuffer;
  }

  private static void initDBBRConstructor()
  {
    AccessController.doPrivileged(new PrivilegedAction()
    {
      public Object run()
      {
        Class localClass;
        try
        {
          localClass = Class.forName("java.nio.DirectByteBufferR");
          Util.access$302(localClass.getDeclaredConstructor(new Class[] { Integer.TYPE, Long.TYPE, Runnable.class }));
          Util.access$300().setAccessible(true);
        }
        catch (ClassNotFoundException localClassNotFoundException)
        {
          throw new InternalError();
        }
        catch (NoSuchMethodException localNoSuchMethodException)
        {
          throw new InternalError();
        }
        catch (IllegalArgumentException localIllegalArgumentException)
        {
          throw new InternalError();
        }
        catch (ClassCastException localClassCastException)
        {
          throw new InternalError();
        }
        return null;
      }
    });
  }

  static MappedByteBuffer newMappedByteBufferR(int paramInt, long paramLong, Runnable paramRunnable)
  {
    MappedByteBuffer localMappedByteBuffer;
    if (directByteBufferRConstructor == null)
      initDBBRConstructor();
    try
    {
      localMappedByteBuffer = (MappedByteBuffer)directByteBufferRConstructor.newInstance(new Object[] { new Integer(paramInt), new Long(paramLong), paramRunnable });
    }
    catch (InstantiationException localInstantiationException)
    {
      throw new InternalError();
    }
    catch (IllegalAccessException localIllegalAccessException)
    {
      throw new InternalError();
    }
    catch (InvocationTargetException localInvocationTargetException)
    {
      throw new InternalError();
    }
    return localMappedByteBuffer;
  }

  static boolean atBugLevel(String paramString)
  {
    if (bugLevel == null)
    {
      if (!(VM.isBooted()))
        return false;
      GetPropertyAction localGetPropertyAction = new GetPropertyAction("sun.nio.ch.bugLevel");
      bugLevel = (String)AccessController.doPrivileged(localGetPropertyAction);
      if (bugLevel == null)
        bugLevel = "";
    }
    return ((bugLevel != null) && (bugLevel.equals(paramString)));
  }

  static void load()
  {
    synchronized (Util.class)
    {
      if (!(loaded))
        break label15;
      return;
      label15: loaded = true;
      AccessController.doPrivileged(new LoadLibraryAction("net"));
      AccessController.doPrivileged(new LoadLibraryAction("nio"));
      IOUtil.initIDs();
    }
  }

  static
  {
    bufferPool = new ThreadLocal[3];
    for (int i = 0; i < 3; ++i)
      bufferPool[i] = new ThreadLocal();
    localSelector = new ThreadLocal();
    localSelectorWrapper = new ThreadLocal();
    unsafe = Unsafe.getUnsafe();
    pageSize = -1;
    directByteBufferConstructor = null;
    directByteBufferRConstructor = null;
    bugLevel = null;
    loaded = false;
  }

  private static class SelectorWrapper
  {
    private Selector sel;

    private SelectorWrapper(Selector paramSelector)
    {
      this.sel = paramSelector;
      Cleaner.create(this, new Closer(paramSelector, null));
    }

    public Selector get()
    {
      return this.sel;
    }

    private static class Closer
  implements Runnable
    {
      private Selector sel;

      private Closer(Selector paramSelector)
      {
        this.sel = paramSelector;
      }

      public void run()
      {
        try
        {
          this.sel.close();
        }
        catch (Throwable localThrowable)
        {
          throw new Error(localThrowable);
        }
      }
    }
  }
}