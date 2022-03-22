package sun.rmi.transport;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.OutputStream;
import java.io.StreamCorruptedException;
import java.rmi.MarshalException;
import java.rmi.RemoteException;
import java.rmi.UnmarshalException;
import java.rmi.server.ObjID;
import java.rmi.server.RemoteCall;
import sun.rmi.runtime.Log;
import sun.rmi.server.UnicastRef;
import sun.rmi.transport.tcp.TCPEndpoint;

public class StreamRemoteCall
  implements RemoteCall
{
  private ConnectionInputStream in = null;
  private ConnectionOutputStream out = null;
  private Connection conn;
  private boolean resultStarted = false;
  private Exception serverException = null;

  public StreamRemoteCall(Connection paramConnection)
  {
    this.conn = paramConnection;
  }

  public StreamRemoteCall(Connection paramConnection, ObjID paramObjID, int paramInt, long paramLong)
    throws RemoteException
  {
    try
    {
      this.conn = paramConnection;
      Transport.transportLog.log(Log.VERBOSE, "write remote call header...");
      this.conn.getOutputStream().write(80);
      getOutputStream();
      paramObjID.write(this.out);
      this.out.writeInt(paramInt);
      this.out.writeLong(paramLong);
    }
    catch (IOException localIOException)
    {
      throw new MarshalException("Error marshaling call header", localIOException);
    }
  }

  public Connection getConnection()
  {
    return this.conn;
  }

  public ObjectOutput getOutputStream()
    throws IOException
  {
    return getOutputStream(false);
  }

  private ObjectOutput getOutputStream(boolean paramBoolean)
    throws IOException
  {
    if (this.out == null)
    {
      Transport.transportLog.log(Log.VERBOSE, "getting output stream");
      this.out = new ConnectionOutputStream(this.conn, paramBoolean);
    }
    return this.out;
  }

  public void releaseOutputStream()
    throws IOException
  {
    try
    {
      if (this.out != null)
        try
        {
          this.out.flush();
        }
        finally
        {
          this.out.done();
        }
      this.conn.releaseOutputStream();
    }
    finally
    {
      this.out = null;
    }
  }

  public ObjectInput getInputStream()
    throws IOException
  {
    if (this.in == null)
    {
      Transport.transportLog.log(Log.VERBOSE, "getting input stream");
      this.in = new ConnectionInputStream(this.conn.getInputStream());
    }
    return this.in;
  }

  public void releaseInputStream()
    throws IOException
  {
    try
    {
      if (this.in != null)
      {
        try
        {
          this.in.done();
        }
        catch (RuntimeException localRuntimeException)
        {
        }
        this.in.registerRefs();
        this.in.done(this.conn);
      }
      this.conn.releaseInputStream();
    }
    finally
    {
      this.in = null;
    }
  }

  public ObjectOutput getResultStream(boolean paramBoolean)
    throws IOException
  {
    if (this.resultStarted)
      throw new StreamCorruptedException("result already in progress");
    this.resultStarted = true;
    DataOutputStream localDataOutputStream = new DataOutputStream(this.conn.getOutputStream());
    localDataOutputStream.writeByte(81);
    getOutputStream(true);
    if (paramBoolean)
      this.out.writeByte(1);
    else
      this.out.writeByte(2);
    this.out.writeID();
    return this.out;
  }

  public void executeCall()
    throws Exception
  {
    int i;
    DGCAckHandler localDGCAckHandler = null;
    try
    {
      if (this.out != null)
        localDGCAckHandler = this.out.getDGCAckHandler();
      releaseOutputStream();
      DataInputStream localDataInputStream = new DataInputStream(this.conn.getInputStream());
      int j = localDataInputStream.readByte();
      if (j != 81)
      {
        if (Transport.transportLog.isLoggable(Log.BRIEF))
          Transport.transportLog.log(Log.BRIEF, "transport return code invalid: " + j);
        throw new UnmarshalException("Transport return code invalid");
      }
      getInputStream();
      i = this.in.readByte();
      this.in.readID();
    }
    catch (UnmarshalException localUnmarshalException)
    {
    }
    catch (IOException localIOException)
    {
    }
    finally
    {
      if (localDGCAckHandler != null)
        localDGCAckHandler.release();
    }
    switch (i)
    {
    case 1:
      break;
    case 2:
      Object localObject1;
      try
      {
        localObject1 = this.in.readObject();
      }
      catch (Exception localException)
      {
        throw new UnmarshalException("Error unmarshaling return", localException);
      }
      if (localObject1 instanceof Exception)
      {
        exceptionReceivedFromServer((Exception)localObject1);
        break label244:
      }
      throw new UnmarshalException("Return type not Exception");
    default:
      if (Transport.transportLog.isLoggable(Log.BRIEF))
        label244: Transport.transportLog.log(Log.BRIEF, "return code invalid: " + i);
      throw new UnmarshalException("Return code invalid");
    }
  }

  protected void exceptionReceivedFromServer(Exception paramException)
    throws Exception
  {
    this.serverException = paramException;
    StackTraceElement[] arrayOfStackTraceElement1 = paramException.getStackTrace();
    StackTraceElement[] arrayOfStackTraceElement2 = new Throwable().getStackTrace();
    StackTraceElement[] arrayOfStackTraceElement3 = new StackTraceElement[arrayOfStackTraceElement1.length + arrayOfStackTraceElement2.length];
    System.arraycopy(arrayOfStackTraceElement1, 0, arrayOfStackTraceElement3, 0, arrayOfStackTraceElement1.length);
    System.arraycopy(arrayOfStackTraceElement2, 0, arrayOfStackTraceElement3, arrayOfStackTraceElement1.length, arrayOfStackTraceElement2.length);
    paramException.setStackTrace(arrayOfStackTraceElement3);
    if (UnicastRef.clientCallLog.isLoggable(Log.BRIEF))
    {
      TCPEndpoint localTCPEndpoint = (TCPEndpoint)this.conn.getChannel().getEndpoint();
      UnicastRef.clientCallLog.log(Log.BRIEF, "outbound call received exception: [" + localTCPEndpoint.getHost() + ":" + localTCPEndpoint.getPort() + "] exception: ", paramException);
    }
    throw paramException;
  }

  public Exception getServerException()
  {
    return this.serverException;
  }

  public void done()
    throws IOException
  {
    releaseInputStream();
  }
}