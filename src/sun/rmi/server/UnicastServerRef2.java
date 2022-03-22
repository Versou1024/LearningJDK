package sun.rmi.server;

import java.io.ObjectOutput;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.RemoteRef;
import sun.rmi.transport.LiveRef;

public class UnicastServerRef2 extends UnicastServerRef
{
  private static final long serialVersionUID = -2289703812660767614L;

  public UnicastServerRef2()
  {
  }

  public UnicastServerRef2(LiveRef paramLiveRef)
  {
    super(paramLiveRef);
  }

  public UnicastServerRef2(int paramInt, RMIClientSocketFactory paramRMIClientSocketFactory, RMIServerSocketFactory paramRMIServerSocketFactory)
  {
    super(new LiveRef(paramInt, paramRMIClientSocketFactory, paramRMIServerSocketFactory));
  }

  public String getRefClass(ObjectOutput paramObjectOutput)
  {
    return "UnicastServerRef2";
  }

  protected RemoteRef getClientRef()
  {
    return new UnicastRef2(this.ref);
  }
}