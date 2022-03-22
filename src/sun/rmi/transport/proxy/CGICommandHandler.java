package sun.rmi.transport.proxy;

abstract interface CGICommandHandler
{
  public abstract String getName();

  public abstract void execute(String paramString)
    throws sun.rmi.transport.proxy.CGIClientException, sun.rmi.transport.proxy.CGIServerException;
}