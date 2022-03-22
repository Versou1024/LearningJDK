package sun.nio.ch;

import java.io.IOException;
import java.net.Socket;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.Pipe;
import java.nio.channels.Pipe.SinkChannel;
import java.nio.channels.Pipe.SourceChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

final class WindowsSelectorImpl extends SelectorImpl
{
  private final int INIT_CAP = 8;
  private static final int MAX_SELECTABLE_FDS = 1024;
  private SelectionKeyImpl[] channelArray = new SelectionKeyImpl[8];
  private PollArrayWrapper pollWrapper = new PollArrayWrapper(8);
  private int totalChannels = 1;
  private int threadsCount = 0;
  private final List threads = new ArrayList();
  private final Pipe wakeupPipe = Pipe.open();
  private final int wakeupSourceFd = ((SelChImpl)this.wakeupPipe.source()).getFDVal();
  private final int wakeupSinkFd;
  private final FdMap fdMap = new FdMap(null);
  private final SubSelector subSelector = new SubSelector(this, null);
  private long timeout;
  private final Object interruptLock = new Object();
  private volatile boolean interruptTriggered = false;
  private final StartLock startLock = new StartLock(this, null);
  private final FinishLock finishLock = new FinishLock(this, null);
  private long updateCount = 3412045659165949952L;

  WindowsSelectorImpl(SelectorProvider paramSelectorProvider)
    throws IOException
  {
    super(paramSelectorProvider);
    SinkChannelImpl localSinkChannelImpl = (SinkChannelImpl)this.wakeupPipe.sink();
    localSinkChannelImpl.sc.socket().setTcpNoDelay(true);
    this.wakeupSinkFd = localSinkChannelImpl.getFDVal();
    this.pollWrapper.addWakeupSocket(this.wakeupSourceFd, 0);
  }

  protected int doSelect(long paramLong)
    throws IOException
  {
    if (this.channelArray == null)
      throw new ClosedSelectorException();
    this.timeout = paramLong;
    processDeregisterQueue();
    if (this.interruptTriggered)
    {
      resetWakeupSocket();
      return 0;
    }
    adjustThreadsCount();
    FinishLock.access$200(this.finishLock);
    StartLock.access$300(this.startLock);
    try
    {
      begin();
      try
      {
        SubSelector.access$400(this.subSelector);
      }
      catch (IOException localIOException)
      {
        FinishLock.access$500(this.finishLock, localIOException);
      }
      if (this.threads.size() > 0)
        FinishLock.access$600(this.finishLock);
    }
    finally
    {
      end();
    }
    FinishLock.access$700(this.finishLock);
    processDeregisterQueue();
    int i = updateSelectedKeys();
    resetWakeupSocket();
    return i;
  }

  private void adjustThreadsCount()
  {
    int i;
    if (this.threadsCount > this.threads.size())
      for (i = this.threads.size(); i < this.threadsCount; ++i)
      {
        SelectThread localSelectThread = new SelectThread(this, i, null);
        this.threads.add(localSelectThread);
        localSelectThread.setDaemon(true);
        localSelectThread.start();
      }
    else if (this.threadsCount < this.threads.size())
      for (i = this.threads.size() - 1; i >= this.threadsCount; --i)
        this.threads.remove(i);
  }

  private void setWakeupSocket()
  {
    setWakeupSocket0(this.wakeupSinkFd);
  }

  private native void setWakeupSocket0(int paramInt);

  private void resetWakeupSocket()
  {
    synchronized (this.interruptLock)
    {
      if (this.interruptTriggered)
        break label17;
      return;
      label17: resetWakeupSocket0(this.wakeupSourceFd);
      this.interruptTriggered = false;
    }
  }

  private native void resetWakeupSocket0(int paramInt);

  private int updateSelectedKeys()
  {
    this.updateCount += 3412047188174307329L;
    int i = 0;
    i += SubSelector.access$2900(this.subSelector, this.updateCount);
    Iterator localIterator = this.threads.iterator();
    while (localIterator.hasNext())
      i += SubSelector.access$2900(((SelectThread)localIterator.next()).subSelector, this.updateCount);
    return i;
  }

  protected void implClose()
    throws IOException
  {
    if ((this.channelArray != null) && (this.pollWrapper != null))
    {
      this.wakeupPipe.sink().close();
      this.wakeupPipe.source().close();
      for (int i = 1; i < this.totalChannels; ++i)
        if (i % 1024 != 0)
        {
          deregister(this.channelArray[i]);
          SelectableChannel localSelectableChannel = this.channelArray[i].channel();
          if ((!(localSelectableChannel.isOpen())) && (!(localSelectableChannel.isRegistered())))
            ((SelChImpl)localSelectableChannel).kill();
        }
      this.pollWrapper.free();
      this.pollWrapper = null;
      this.selectedKeys = null;
      this.channelArray = null;
      this.threads.clear();
      StartLock.access$300(this.startLock);
    }
  }

  protected void implRegister(SelectionKeyImpl paramSelectionKeyImpl)
  {
    growIfNeeded();
    this.channelArray[this.totalChannels] = paramSelectionKeyImpl;
    paramSelectionKeyImpl.setIndex(this.totalChannels);
    FdMap.access$3000(this.fdMap, paramSelectionKeyImpl);
    this.keys.add(paramSelectionKeyImpl);
    this.pollWrapper.addEntry(this.totalChannels, paramSelectionKeyImpl);
    this.totalChannels += 1;
  }

  private void growIfNeeded()
  {
    if (this.channelArray.length == this.totalChannels)
    {
      int i = this.totalChannels * 2;
      SelectionKeyImpl[] arrayOfSelectionKeyImpl = new SelectionKeyImpl[i];
      System.arraycopy(this.channelArray, 1, arrayOfSelectionKeyImpl, 1, this.totalChannels - 1);
      this.channelArray = arrayOfSelectionKeyImpl;
      this.pollWrapper.grow(i);
    }
    if (this.totalChannels % 1024 == 0)
    {
      this.pollWrapper.addWakeupSocket(this.wakeupSourceFd, this.totalChannels);
      this.totalChannels += 1;
      this.threadsCount += 1;
    }
  }

  protected void implDereg(SelectionKeyImpl paramSelectionKeyImpl)
    throws IOException
  {
    int i = paramSelectionKeyImpl.getIndex();
    if ((!($assertionsDisabled)) && (i < 0))
      throw new AssertionError();
    if (i != this.totalChannels - 1)
    {
      localObject = this.channelArray[(this.totalChannels - 1)];
      this.channelArray[i] = localObject;
      ((SelectionKeyImpl)localObject).setIndex(i);
      this.pollWrapper.replaceEntry(this.pollWrapper, this.totalChannels - 1, this.pollWrapper, i);
    }
    this.channelArray[(this.totalChannels - 1)] = null;
    this.totalChannels -= 1;
    paramSelectionKeyImpl.setIndex(-1);
    if ((this.totalChannels != 1) && (this.totalChannels % 1024 == 1))
    {
      this.totalChannels -= 1;
      this.threadsCount -= 1;
    }
    FdMap.access$3100(this.fdMap, paramSelectionKeyImpl);
    this.keys.remove(paramSelectionKeyImpl);
    this.selectedKeys.remove(paramSelectionKeyImpl);
    deregister(paramSelectionKeyImpl);
    Object localObject = paramSelectionKeyImpl.channel();
    if ((!(((SelectableChannel)localObject).isOpen())) && (!(((SelectableChannel)localObject).isRegistered())))
      ((SelChImpl)localObject).kill();
  }

  void putEventOps(SelectionKeyImpl paramSelectionKeyImpl, int paramInt)
  {
    this.pollWrapper.putEventOps(paramSelectionKeyImpl.getIndex(), paramInt);
  }

  public Selector wakeup()
  {
    synchronized (this.interruptLock)
    {
      if (!(this.interruptTriggered))
      {
        setWakeupSocket();
        this.interruptTriggered = true;
      }
    }
    return this;
  }

  static
  {
    Util.load();
  }

  private static final class FdMap extends HashMap<Integer, WindowsSelectorImpl.MapEntry>
  {
    private WindowsSelectorImpl.MapEntry get(int paramInt)
    {
      return ((WindowsSelectorImpl.MapEntry)get(new Integer(paramInt)));
    }

    private WindowsSelectorImpl.MapEntry put(SelectionKeyImpl paramSelectionKeyImpl)
    {
      return ((WindowsSelectorImpl.MapEntry)put(new Integer(paramSelectionKeyImpl.channel.getFDVal()), new WindowsSelectorImpl.MapEntry(paramSelectionKeyImpl)));
    }

    private WindowsSelectorImpl.MapEntry remove(SelectionKeyImpl paramSelectionKeyImpl)
    {
      Integer localInteger = new Integer(paramSelectionKeyImpl.channel.getFDVal());
      WindowsSelectorImpl.MapEntry localMapEntry = (WindowsSelectorImpl.MapEntry)get(localInteger);
      if ((localMapEntry != null) && (localMapEntry.ski.channel == paramSelectionKeyImpl.channel))
        return ((WindowsSelectorImpl.MapEntry)remove(localInteger));
      return null;
    }
  }

  private final class FinishLock
  {
    private int threadsToFinish;
    IOException exception = null;

    private void reset()
    {
      this.threadsToFinish = WindowsSelectorImpl.access$1200(this.this$0).size();
    }

    private synchronized void threadFinished()
    {
      if (this.threadsToFinish == WindowsSelectorImpl.access$1200(this.this$0).size())
        this.this$0.wakeup();
      this.threadsToFinish -= 1;
      if (this.threadsToFinish == 0)
        super.notify();
    }

    private synchronized void waitForHelperThreads()
    {
      if (this.threadsToFinish == WindowsSelectorImpl.access$1200(this.this$0).size())
        this.this$0.wakeup();
      if (this.threadsToFinish != 0)
        try
        {
          WindowsSelectorImpl.access$1400(this.this$0).wait();
        }
        catch (InterruptedException localInterruptedException)
        {
          Thread.currentThread().interrupt();
        }
    }

    private synchronized void setException()
    {
      this.exception = paramIOException;
    }

    private void checkForException()
      throws IOException
    {
      if (this.exception == null)
        return;
      StringBuffer localStringBuffer = new StringBuffer("An exception occured during the execution of select(): \n");
      localStringBuffer.append(this.exception);
      localStringBuffer.append('\n');
      this.exception = null;
      throw new IOException(localStringBuffer.toString());
    }
  }

  private static final class MapEntry
  {
    SelectionKeyImpl ski;
    long updateCount = 3412046294821109760L;
    long clearedCount = 3412046294821109760L;

    MapEntry(SelectionKeyImpl paramSelectionKeyImpl)
    {
      this.ski = paramSelectionKeyImpl;
    }
  }

  private final class SelectThread extends Thread
  {
    private int index;
    WindowsSelectorImpl.SubSelector subSelector;
    private long lastRun = 3412046294821109760L;

    private SelectThread(, int paramInt)
    {
      this.index = paramInt;
      this.subSelector = new WindowsSelectorImpl.SubSelector(paramWindowsSelectorImpl, paramInt, null);
      this.lastRun = WindowsSelectorImpl.StartLock.access$2400(WindowsSelectorImpl.access$1000(paramWindowsSelectorImpl));
    }

    public void run()
    {
      while (true)
      {
        if (WindowsSelectorImpl.StartLock.access$2500(WindowsSelectorImpl.access$1000(this.this$0), this))
          return;
        try
        {
          WindowsSelectorImpl.SubSelector.access$2600(this.subSelector, this.index);
        }
        catch (IOException localIOException)
        {
          WindowsSelectorImpl.FinishLock.access$500(WindowsSelectorImpl.access$1400(this.this$0), localIOException);
        }
        WindowsSelectorImpl.FinishLock.access$2700(WindowsSelectorImpl.access$1400(this.this$0));
      }
    }
  }

  private final class StartLock
  {
    private long runsCounter;

    private synchronized void startThreads()
    {
      this.runsCounter += 3412047823829467137L;
      super.notifyAll();
    }

    private synchronized boolean waitForStart()
    {
      if (this.runsCounter == WindowsSelectorImpl.SelectThread.access$900(paramSelectThread));
      try
      {
        WindowsSelectorImpl.access$1000(this.this$0).wait();
      }
      catch (InterruptedException localInterruptedException)
      {
        while (true)
          Thread.currentThread().interrupt();
        if (WindowsSelectorImpl.SelectThread.access$1100(paramSelectThread) >= WindowsSelectorImpl.access$1200(this.this$0).size())
          return true;
        WindowsSelectorImpl.SelectThread.access$902(paramSelectThread, this.runsCounter);
      }
      return false;
    }
  }

  private final class SubSelector
  {
    private final int pollArrayIndex;
    private final int[] readFds = new int[1025];
    private final int[] writeFds = new int[1025];
    private final int[] exceptFds = new int[1025];

    private SubSelector()
    {
      this.pollArrayIndex = 0;
    }

    private SubSelector(, int paramInt)
    {
      this.pollArrayIndex = ((paramInt + 1) * 1024);
    }

    private int poll()
      throws IOException
    {
      return poll0(WindowsSelectorImpl.access$1500(this.this$0).pollArrayAddress, Math.min(WindowsSelectorImpl.access$1600(this.this$0), 1024), this.readFds, this.writeFds, this.exceptFds, WindowsSelectorImpl.access$1700(this.this$0));
    }

    private int poll()
      throws IOException
    {
      return poll0(WindowsSelectorImpl.access$1500(this.this$0).pollArrayAddress + this.pollArrayIndex * PollArrayWrapper.SIZE_POLLFD, Math.min(1024, WindowsSelectorImpl.access$1600(this.this$0) - (paramInt + 1) * 1024), this.readFds, this.writeFds, this.exceptFds, WindowsSelectorImpl.access$1700(this.this$0));
    }

    private native int poll0(, int paramInt, int[] paramArrayOfInt1, int[] paramArrayOfInt2, int[] paramArrayOfInt3, long paramLong2);

    private int processSelectedKeys()
    {
      int i = 0;
      i += processFDSet(paramLong, this.readFds, 1);
      i += processFDSet(paramLong, this.writeFds, 6);
      i += processFDSet(paramLong, this.exceptFds, 7);
      return i;
    }

    private int processFDSet(, int[] paramArrayOfInt, int paramInt)
    {
      int i = 0;
      for (int j = 1; j <= paramArrayOfInt[0]; ++j)
      {
        int k = paramArrayOfInt[j];
        if (k == WindowsSelectorImpl.access$1800(this.this$0))
        {
          synchronized (WindowsSelectorImpl.access$1900(this.this$0))
          {
            WindowsSelectorImpl.access$2002(this.this$0, true);
          }
        }
        else
        {
          ??? = WindowsSelectorImpl.FdMap.access$2200(WindowsSelectorImpl.access$2100(this.this$0), k);
          if (??? == null)
            break label334:
          SelectionKeyImpl localSelectionKeyImpl = ((WindowsSelectorImpl.MapEntry)???).ski;
          if (this.this$0.selectedKeys.contains(localSelectionKeyImpl))
          {
            if (((WindowsSelectorImpl.MapEntry)???).clearedCount != paramLong)
            {
              if ((localSelectionKeyImpl.channel.translateAndSetReadyOps(paramInt, localSelectionKeyImpl)) && (((WindowsSelectorImpl.MapEntry)???).updateCount != paramLong))
              {
                ((WindowsSelectorImpl.MapEntry)???).updateCount = paramLong;
                ++i;
              }
            }
            else if ((localSelectionKeyImpl.channel.translateAndUpdateReadyOps(paramInt, localSelectionKeyImpl)) && (((WindowsSelectorImpl.MapEntry)???).updateCount != paramLong))
            {
              ((WindowsSelectorImpl.MapEntry)???).updateCount = paramLong;
              ++i;
            }
            label334: ((WindowsSelectorImpl.MapEntry)???).clearedCount = paramLong;
          }
          else
          {
            if (((WindowsSelectorImpl.MapEntry)???).clearedCount != paramLong)
            {
              localSelectionKeyImpl.channel.translateAndSetReadyOps(paramInt, localSelectionKeyImpl);
              if ((localSelectionKeyImpl.nioReadyOps() & localSelectionKeyImpl.nioInterestOps()) != 0)
              {
                this.this$0.selectedKeys.add(localSelectionKeyImpl);
                ((WindowsSelectorImpl.MapEntry)???).updateCount = paramLong;
                ++i;
              }
            }
            else
            {
              localSelectionKeyImpl.channel.translateAndUpdateReadyOps(paramInt, localSelectionKeyImpl);
              if ((localSelectionKeyImpl.nioReadyOps() & localSelectionKeyImpl.nioInterestOps()) != 0)
              {
                this.this$0.selectedKeys.add(localSelectionKeyImpl);
                ((WindowsSelectorImpl.MapEntry)???).updateCount = paramLong;
                ++i;
              }
            }
            ((WindowsSelectorImpl.MapEntry)???).clearedCount = paramLong;
          }
        }
      }
      return i;
    }
  }
}