package sun.rmi.server;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.server.RemoteCall;

public abstract interface Dispatcher
{
  public abstract void dispatch(Remote paramRemote, RemoteCall paramRemoteCall)
    throws IOException;
}