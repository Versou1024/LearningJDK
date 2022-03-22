package sun.nio.ch;

class PollArrayWrapper
{
  private AllocatedNativeObject pollArray;
  long pollArrayAddress;
  private static final short FD_OFFSET = 0;
  private static final short EVENT_OFFSET = 4;
  static short SIZE_POLLFD = 8;
  static final short POLLIN = 1;
  static final short POLLOUT = 4;
  static final short POLLERR = 8;
  static final short POLLHUP = 16;
  static final short POLLNVAL = 32;
  static final short POLLREMOVE = 2048;
  static final short POLLCONN = 2;
  private int size;

  PollArrayWrapper(int paramInt)
  {
    int i = paramInt * SIZE_POLLFD;
    this.pollArray = new AllocatedNativeObject(i, true);
    this.pollArrayAddress = this.pollArray.address();
    this.size = paramInt;
  }

  void addEntry(int paramInt, SelectionKeyImpl paramSelectionKeyImpl)
  {
    putDescriptor(paramInt, paramSelectionKeyImpl.channel.getFDVal());
  }

  void replaceEntry(PollArrayWrapper paramPollArrayWrapper1, int paramInt1, PollArrayWrapper paramPollArrayWrapper2, int paramInt2)
  {
    paramPollArrayWrapper2.putDescriptor(paramInt2, paramPollArrayWrapper1.getDescriptor(paramInt1));
    paramPollArrayWrapper2.putEventOps(paramInt2, paramPollArrayWrapper1.getEventOps(paramInt1));
  }

  void grow(int paramInt)
  {
    PollArrayWrapper localPollArrayWrapper = new PollArrayWrapper(paramInt);
    for (int i = 0; i < this.size; ++i)
      replaceEntry(this, i, localPollArrayWrapper, i);
    this.pollArray.free();
    this.pollArray = localPollArrayWrapper.pollArray;
    this.size = localPollArrayWrapper.size;
    this.pollArrayAddress = this.pollArray.address();
  }

  void free()
  {
    this.pollArray.free();
  }

  void putDescriptor(int paramInt1, int paramInt2)
  {
    this.pollArray.putInt(SIZE_POLLFD * paramInt1 + 0, paramInt2);
  }

  void putEventOps(int paramInt1, int paramInt2)
  {
    this.pollArray.putShort(SIZE_POLLFD * paramInt1 + 4, (short)paramInt2);
  }

  int getEventOps(int paramInt)
  {
    return this.pollArray.getShort(SIZE_POLLFD * paramInt + 4);
  }

  int getDescriptor(int paramInt)
  {
    return this.pollArray.getInt(SIZE_POLLFD * paramInt + 0);
  }

  void addWakeupSocket(int paramInt1, int paramInt2)
  {
    putDescriptor(paramInt2, paramInt1);
    putEventOps(paramInt2, 1);
  }
}