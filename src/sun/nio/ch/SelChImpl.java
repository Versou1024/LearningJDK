package sun.nio.ch;

import java.io.FileDescriptor;
import java.io.IOException;

abstract interface SelChImpl
{
  public abstract FileDescriptor getFD();

  public abstract int getFDVal();

  public abstract boolean translateAndUpdateReadyOps(int paramInt, SelectionKeyImpl paramSelectionKeyImpl);

  public abstract boolean translateAndSetReadyOps(int paramInt, SelectionKeyImpl paramSelectionKeyImpl);

  public abstract void translateAndSetInterestOps(int paramInt, SelectionKeyImpl paramSelectionKeyImpl);

  public abstract int validOps();

  public abstract void kill()
    throws IOException;
}