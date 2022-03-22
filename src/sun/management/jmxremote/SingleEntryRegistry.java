package sun.management.jmxremote;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import sun.rmi.registry.RegistryImpl;

public class SingleEntryRegistry extends RegistryImpl
{
  private final String name;
  private final Remote object;

  SingleEntryRegistry(int paramInt, String paramString, Remote paramRemote)
    throws RemoteException
  {
    super(paramInt);
    this.name = paramString;
    this.object = paramRemote;
  }

  SingleEntryRegistry(int paramInt, RMIClientSocketFactory paramRMIClientSocketFactory, RMIServerSocketFactory paramRMIServerSocketFactory, String paramString, Remote paramRemote)
    throws RemoteException
  {
    super(paramInt, paramRMIClientSocketFactory, paramRMIServerSocketFactory);
    this.name = paramString;
    this.object = paramRemote;
  }

  public String[] list()
  {
    return { this.name };
  }

  public Remote lookup(String paramString)
    throws NotBoundException
  {
    if (paramString.equals(this.name))
      return this.object;
    throw new NotBoundException("Not bound: \"" + paramString + "\" (only " + "bound name is \"" + this.name + "\")");
  }

  public void bind(String paramString, Remote paramRemote)
    throws AccessException
  {
    throw new AccessException("Cannot modify this registry");
  }

  public void rebind(String paramString, Remote paramRemote)
    throws AccessException
  {
    throw new AccessException("Cannot modify this registry");
  }

  public void unbind(String paramString)
    throws AccessException
  {
    throw new AccessException("Cannot modify this registry");
  }
}