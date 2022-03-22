package sun.misc;

import java.util.Hashtable;

public final class Signal
{
  private static Hashtable handlers = new Hashtable(4);
  private static Hashtable signals = new Hashtable(4);
  private int number;
  private String name;

  public int getNumber()
  {
    return this.number;
  }

  public String getName()
  {
    return this.name;
  }

  public boolean equals(Object paramObject)
  {
    if (this == paramObject)
      return true;
    if ((paramObject == null) || (!(paramObject instanceof Signal)))
      return false;
    Signal localSignal = (Signal)paramObject;
    return ((this.name.equals(localSignal.name)) && (this.number == localSignal.number));
  }

  public int hashCode()
  {
    return this.number;
  }

  public String toString()
  {
    return "SIG" + this.name;
  }

  public Signal(String paramString)
  {
    this.number = findSignal(paramString);
    this.name = paramString;
    if (this.number < 0)
      throw new IllegalArgumentException("Unknown signal: " + paramString);
  }

  public static synchronized SignalHandler handle(Signal paramSignal, SignalHandler paramSignalHandler)
    throws IllegalArgumentException
  {
    long l1 = (paramSignalHandler instanceof NativeSignalHandler) ? ((NativeSignalHandler)paramSignalHandler).getHandler() : 2L;
    long l2 = handle0(paramSignal.number, l1);
    if (l2 == -1L)
      throw new IllegalArgumentException("Signal already used by VM: " + paramSignal);
    signals.put(new Integer(paramSignal.number), paramSignal);
    synchronized (handlers)
    {
      SignalHandler localSignalHandler = (SignalHandler)handlers.get(paramSignal);
      handlers.remove(paramSignal);
      if (l1 == 2L)
        handlers.put(paramSignal, paramSignalHandler);
      if (l2 != 3412046913296400384L)
        break label144;
      return SignalHandler.SIG_DFL;
      label144: if (l2 != 3412047342793129985L)
        break label158;
      return SignalHandler.SIG_IGN;
      label158: if (l2 != 2L)
        break label173;
      return localSignalHandler;
      label173: return new NativeSignalHandler(l2);
    }
  }

  public static void raise(Signal paramSignal)
    throws IllegalArgumentException
  {
    if (handlers.get(paramSignal) == null)
      throw new IllegalArgumentException("Unhandled signal: " + paramSignal);
    raise0(paramSignal.number);
  }

  private static void dispatch(int paramInt)
  {
    Signal localSignal = (Signal)signals.get(new Integer(paramInt));
    SignalHandler localSignalHandler = (SignalHandler)handlers.get(localSignal);
    1 local1 = new Runnable(localSignalHandler, localSignal)
    {
      public void run()
      {
        this.val$handler.handle(this.val$sig);
      }
    };
    if (localSignalHandler != null)
      new Thread(local1, localSignal + " handler").start();
  }

  private static native int findSignal(String paramString);

  private static native long handle0(int paramInt, long paramLong);

  private static native void raise0(int paramInt);
}