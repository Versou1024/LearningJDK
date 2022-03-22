package sun.rmi.transport;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.ObjID;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.util.Arrays;
import sun.rmi.transport.tcp.TCPEndpoint;

public class LiveRef
  implements Cloneable
{
  private final Endpoint ep;
  private final ObjID id;
  private transient Channel ch;
  private final boolean isLocal;

  public LiveRef(ObjID paramObjID, Endpoint paramEndpoint, boolean paramBoolean)
  {
    this.ep = paramEndpoint;
    this.id = paramObjID;
    this.isLocal = paramBoolean;
  }

  public LiveRef(int paramInt)
  {
    this(new ObjID(), paramInt);
  }

  public LiveRef(int paramInt, RMIClientSocketFactory paramRMIClientSocketFactory, RMIServerSocketFactory paramRMIServerSocketFactory)
  {
    this(new ObjID(), paramInt, paramRMIClientSocketFactory, paramRMIServerSocketFactory);
  }

  public LiveRef(ObjID paramObjID, int paramInt)
  {
    this(paramObjID, TCPEndpoint.getLocalEndpoint(paramInt), true);
  }

  public LiveRef(ObjID paramObjID, int paramInt, RMIClientSocketFactory paramRMIClientSocketFactory, RMIServerSocketFactory paramRMIServerSocketFactory)
  {
    this(paramObjID, TCPEndpoint.getLocalEndpoint(paramInt, paramRMIClientSocketFactory, paramRMIServerSocketFactory), true);
  }

  public Object clone()
  {
    LiveRef localLiveRef;
    try
    {
      localLiveRef = (LiveRef)super.clone();
      return localLiveRef;
    }
    catch (CloneNotSupportedException localCloneNotSupportedException)
    {
      throw new InternalError(localCloneNotSupportedException.toString());
    }
  }

  public int getPort()
  {
    return ((TCPEndpoint)this.ep).getPort();
  }

  public RMIClientSocketFactory getClientSocketFactory()
  {
    return ((TCPEndpoint)this.ep).getClientSocketFactory();
  }

  public RMIServerSocketFactory getServerSocketFactory()
  {
    return ((TCPEndpoint)this.ep).getServerSocketFactory();
  }

  public void exportObject(Target paramTarget)
    throws RemoteException
  {
    this.ep.exportObject(paramTarget);
  }

  public Channel getChannel()
    throws RemoteException
  {
    if (this.ch == null)
      this.ch = this.ep.getChannel();
    return this.ch;
  }

  public ObjID getObjID()
  {
    return this.id;
  }

  Endpoint getEndpoint()
  {
    return this.ep;
  }

  public String toString()
  {
    String str;
    if (this.isLocal)
      str = "local";
    else
      str = "remote";
    return "[endpoint:" + this.ep + "(" + str + ")," + "objID:" + this.id + "]";
  }

  public int hashCode()
  {
    return this.id.hashCode();
  }

  public boolean equals(Object paramObject)
  {
    if ((paramObject != null) && (paramObject instanceof LiveRef))
    {
      LiveRef localLiveRef = (LiveRef)paramObject;
      return ((this.ep.equals(localLiveRef.ep)) && (this.id.equals(localLiveRef.id)) && (this.isLocal == localLiveRef.isLocal));
    }
    return false;
  }

  public boolean remoteEquals(Object paramObject)
  {
    if ((paramObject != null) && (paramObject instanceof LiveRef))
    {
      LiveRef localLiveRef = (LiveRef)paramObject;
      TCPEndpoint localTCPEndpoint1 = (TCPEndpoint)this.ep;
      TCPEndpoint localTCPEndpoint2 = (TCPEndpoint)localLiveRef.ep;
      RMIClientSocketFactory localRMIClientSocketFactory1 = localTCPEndpoint1.getClientSocketFactory();
      RMIClientSocketFactory localRMIClientSocketFactory2 = localTCPEndpoint2.getClientSocketFactory();
      if ((localTCPEndpoint1.getPort() != localTCPEndpoint2.getPort()) || (!(localTCPEndpoint1.getHost().equals(localTCPEndpoint2.getHost()))))
        return false;
      if ((((localRMIClientSocketFactory1 == null) ? 1 : 0) ^ ((localRMIClientSocketFactory2 == null) ? 1 : 0)) != 0)
        return false;
      if ((localRMIClientSocketFactory1 != null) && (((localRMIClientSocketFactory1.getClass() != localRMIClientSocketFactory2.getClass()) || (!(localRMIClientSocketFactory1.equals(localRMIClientSocketFactory2))))))
        return false;
      return this.id.equals(localLiveRef.id);
    }
    return false;
  }

  public void write(ObjectOutput paramObjectOutput, boolean paramBoolean)
    throws IOException
  {
    boolean bool = false;
    if (paramObjectOutput instanceof ConnectionOutputStream)
    {
      ConnectionOutputStream localConnectionOutputStream = (ConnectionOutputStream)paramObjectOutput;
      bool = localConnectionOutputStream.isResultStream();
      if (this.isLocal)
      {
        ObjectEndpoint localObjectEndpoint = new ObjectEndpoint(this.id, this.ep.getInboundTransport());
        Target localTarget = ObjectTable.getTarget(localObjectEndpoint);
        if (localTarget != null)
        {
          Remote localRemote = localTarget.getImpl();
          if (localRemote != null)
            localConnectionOutputStream.saveObject(localRemote);
        }
      }
      else
      {
        localConnectionOutputStream.saveObject(this);
      }
    }
    if (paramBoolean)
      ((TCPEndpoint)this.ep).write(paramObjectOutput);
    else
      ((TCPEndpoint)this.ep).writeHostPortFormat(paramObjectOutput);
    this.id.write(paramObjectOutput);
    paramObjectOutput.writeBoolean(bool);
  }

  public static LiveRef read(ObjectInput paramObjectInput, boolean paramBoolean)
    throws IOException, ClassNotFoundException
  {
    TCPEndpoint localTCPEndpoint;
    if (paramBoolean)
      localTCPEndpoint = TCPEndpoint.read(paramObjectInput);
    else
      localTCPEndpoint = TCPEndpoint.readHostPortFormat(paramObjectInput);
    ObjID localObjID = ObjID.read(paramObjectInput);
    boolean bool = paramObjectInput.readBoolean();
    LiveRef localLiveRef = new LiveRef(localObjID, localTCPEndpoint, false);
    if (paramObjectInput instanceof ConnectionInputStream)
    {
      ConnectionInputStream localConnectionInputStream = (ConnectionInputStream)paramObjectInput;
      localConnectionInputStream.saveRef(localLiveRef);
      if (bool)
        localConnectionInputStream.setAckNeeded();
    }
    else
    {
      DGCClient.registerRefs(localTCPEndpoint, Arrays.asList(new LiveRef[] { localLiveRef }));
    }
    return localLiveRef;
  }
}