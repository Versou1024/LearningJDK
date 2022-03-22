package sun.nio.ch;

import sun.misc.Cleaner;

public abstract interface DirectBuffer
{
  public abstract long address();

  public abstract Object viewedBuffer();

  public abstract Cleaner cleaner();
}