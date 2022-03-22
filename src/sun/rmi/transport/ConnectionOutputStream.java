package sun.rmi.transport;

import java.io.IOException;
import java.rmi.server.UID;
import sun.rmi.server.MarshalOutputStream;

class ConnectionOutputStream extends MarshalOutputStream
{
  private final Connection conn;
  private final boolean resultStream;
  private final UID ackID;
  private DGCAckHandler dgcAckHandler = null;

  ConnectionOutputStream(Connection paramConnection, boolean paramBoolean)
    throws IOException
  {
    super(paramConnection.getOutputStream());
    this.conn = paramConnection;
    this.resultStream = paramBoolean;
    this.ackID = ((paramBoolean) ? new UID() : null);
  }

  void writeID()
    throws IOException
  {
    if ((!($assertionsDisabled)) && (!(this.resultStream)))
      throw new AssertionError();
    this.ackID.write(this);
  }

  boolean isResultStream()
  {
    return this.resultStream;
  }

  void saveObject(Object paramObject)
  {
    if (this.dgcAckHandler == null)
      this.dgcAckHandler = new DGCAckHandler(this.ackID);
    this.dgcAckHandler.add(paramObject);
  }

  DGCAckHandler getDGCAckHandler()
  {
    return this.dgcAckHandler;
  }

  void done()
  {
    if (this.dgcAckHandler != null)
      this.dgcAckHandler.startTimer();
  }
}