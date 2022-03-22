package sun.rmi.transport;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.rmi.server.UID;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import sun.rmi.runtime.Log;
import sun.rmi.server.MarshalInputStream;

class ConnectionInputStream extends MarshalInputStream
{
  private boolean dgcAckNeeded = false;
  private Map incomingRefTable = new HashMap(5);
  private UID ackID;

  ConnectionInputStream(InputStream paramInputStream)
    throws IOException
  {
    super(paramInputStream);
  }

  void readID()
    throws IOException
  {
    this.ackID = UID.read(this);
  }

  void saveRef(LiveRef paramLiveRef)
  {
    Endpoint localEndpoint = paramLiveRef.getEndpoint();
    Object localObject = (List)this.incomingRefTable.get(localEndpoint);
    if (localObject == null)
    {
      localObject = new ArrayList();
      this.incomingRefTable.put(localEndpoint, localObject);
    }
    ((List)localObject).add(paramLiveRef);
  }

  void registerRefs()
    throws IOException
  {
    if (!(this.incomingRefTable.isEmpty()))
    {
      Set localSet = this.incomingRefTable.entrySet();
      Iterator localIterator = localSet.iterator();
      while (localIterator.hasNext())
      {
        Map.Entry localEntry = (Map.Entry)localIterator.next();
        Endpoint localEndpoint = (Endpoint)localEntry.getKey();
        List localList = (List)localEntry.getValue();
        DGCClient.registerRefs(localEndpoint, localList);
      }
    }
  }

  void setAckNeeded()
  {
    this.dgcAckNeeded = true;
  }

  void done(Connection paramConnection)
  {
    if (this.dgcAckNeeded)
    {
      Connection localConnection = null;
      Channel localChannel = null;
      boolean bool = true;
      DGCImpl.dgcLog.log(Log.VERBOSE, "send ack");
      try
      {
        localChannel = paramConnection.getChannel();
        localConnection = localChannel.newConnection();
        DataOutputStream localDataOutputStream = new DataOutputStream(localConnection.getOutputStream());
        localDataOutputStream.writeByte(84);
        if (this.ackID == null)
          this.ackID = new UID();
        this.ackID.write(localDataOutputStream);
        localConnection.releaseOutputStream();
        localConnection.getInputStream().available();
        localConnection.releaseInputStream();
      }
      catch (RemoteException localRemoteException1)
      {
        bool = false;
      }
      catch (IOException localIOException)
      {
        bool = false;
      }
      try
      {
        if (localConnection != null)
          localChannel.free(localConnection, bool);
      }
      catch (RemoteException localRemoteException2)
      {
      }
    }
  }
}