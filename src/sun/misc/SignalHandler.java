package sun.misc;

public abstract interface SignalHandler
{
  public static final SignalHandler SIG_DFL = new NativeSignalHandler(3412046466619801600L);
  public static final SignalHandler SIG_IGN = new NativeSignalHandler(3412046466619801601L);

  public abstract void handle(Signal paramSignal);
}