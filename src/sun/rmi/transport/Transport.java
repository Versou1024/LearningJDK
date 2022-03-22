package sun.rmi.transport;

import java.io.IOException;
import java.io.ObjectOutput;
import java.rmi.MarshalException;
import java.rmi.NoSuchObjectException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.LogStream;
import java.rmi.server.ObjID;
import java.rmi.server.RemoteCall;
import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import sun.rmi.runtime.Log;
import sun.rmi.server.Dispatcher;
import sun.rmi.server.UnicastServerRef;
import sun.security.action.GetPropertyAction;

public abstract class Transport
{
  static final int logLevel = LogStream.parseLevel(getLogLevel());
  static final Log transportLog = Log.getLog("sun.rmi.transport.misc", "transport", logLevel);
  private static final ThreadLocal currentTransport = new ThreadLocal();
  private static final ObjID dgcID = new ObjID(2);

  private static String getLogLevel()
  {
    return ((String)AccessController.doPrivileged(new GetPropertyAction("sun.rmi.transport.logLevel")));
  }

  public abstract Channel getChannel(Endpoint paramEndpoint);

  public abstract void free(Endpoint paramEndpoint);

  public void exportObject(Target paramTarget)
    throws RemoteException
  {
    paramTarget.setExportedTransport(this);
    ObjectTable.putTarget(paramTarget);
  }

  protected void targetUnexported()
  {
  }

  static Transport currentTransport()
  {
    return ((Transport)currentTransport.get());
  }

  protected abstract void checkAcceptPermission(AccessControlContext paramAccessControlContext);

  public boolean serviceCall(RemoteCall paramRemoteCall)
  {
    Object localObject1;
    try
    {
      Remote localRemote;
      try
      {
        localObject1 = ObjID.read(paramRemoteCall.getInputStream());
      }
      catch (IOException localIOException2)
      {
        throw new MarshalException("unable to read objID", localIOException2);
      }
      Transport localTransport = (((ObjID)localObject1).equals(dgcID)) ? null : this;
      Target localTarget = ObjectTable.getTarget(new ObjectEndpoint((ObjID)localObject1, localTransport));
      if (localTarget != null)
        if ((localRemote = localTarget.getImpl()) != null)
          break label84;
      throw new NoSuchObjectException("no such object in table");
      label84: Dispatcher localDispatcher = localTarget.getDispatcher();
      localTarget.incrementCallCount();
      try
      {
        transportLog.log(Log.VERBOSE, "call dispatcher");
        AccessControlContext localAccessControlContext = localTarget.getAccessControlContext();
        ClassLoader localClassLoader1 = localTarget.getContextClassLoader();
        Thread localThread = Thread.currentThread();
        ClassLoader localClassLoader2 = localThread.getContextClassLoader();
        try
        {
          localThread.setContextClassLoader(localClassLoader1);
          currentTransport.set(this);
          try
          {
            AccessController.doPrivileged(new PrivilegedExceptionAction(this, localAccessControlContext, localDispatcher, localRemote, paramRemoteCall)
            {
              public Object run()
                throws IOException
              {
                this.this$0.checkAcceptPermission(this.val$acc);
                this.val$disp.dispatch(this.val$impl, this.val$call);
                return null;
              }
            }
            , localAccessControlContext);
          }
          catch (PrivilegedActionException localPrivilegedActionException)
          {
            throw ((IOException)localPrivilegedActionException.getException());
          }
        }
        finally
        {
          localThread.setContextClassLoader(localClassLoader2);
          currentTransport.set(null);
        }
      }
      catch (IOException localIOException3)
      {
        transportLog.log(Log.BRIEF, "exception thrown by dispatcher: ", localIOException3);
        int i = 0;
        return i;
      }
      finally
      {
        localTarget.decrementCallCount();
      }
    }
    catch (RemoteException localRemoteException)
    {
      if (UnicastServerRef.callLog.isLoggable(Log.BRIEF))
      {
        localObject1 = "";
        try
        {
          localObject1 = "[" + RemoteServer.getClientHost() + "] ";
        }
        catch (ServerNotActiveException localServerNotActiveException)
        {
        }
        String str = ((String)localObject1) + "exception: ";
        UnicastServerRef.callLog.log(Log.BRIEF, str, localRemoteException);
      }
      try
      {
        localObject1 = paramRemoteCall.getResultStream(false);
        UnicastServerRef.clearStackTraces(localRemoteException);
        ((ObjectOutput)localObject1).writeObject(localRemoteException);
        paramRemoteCall.releaseOutputStream();
      }
      catch (IOException localIOException1)
      {
        transportLog.log(Log.BRIEF, "exception thrown marshalling exception: ", localIOException1);
        return false;
      }
    }
    return true;
  }
}