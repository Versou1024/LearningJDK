package sun.misc;

final class NativeSignalHandler
  implements SignalHandler
{
  private final long handler;

  long getHandler()
  {
    return this.handler;
  }

  NativeSignalHandler(long paramLong)
  {
    this.handler = paramLong;
  }

  public void handle(Signal paramSignal)
  {
    handle0(paramSignal.getNumber(), this.handler);
  }

  private static native void handle0(int paramInt, long paramLong);
}