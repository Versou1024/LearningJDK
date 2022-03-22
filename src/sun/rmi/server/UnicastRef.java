package sun.rmi.server;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Method;
import java.rmi.MarshalException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.UnmarshalException;
import java.rmi.server.ObjID;
import java.rmi.server.Operation;
import java.rmi.server.RemoteCall;
import java.rmi.server.RemoteObject;
import java.rmi.server.RemoteRef;
import java.security.AccessController;
import sun.rmi.runtime.Log;
import sun.rmi.transport.Channel;
import sun.rmi.transport.Connection;
import sun.rmi.transport.LiveRef;
import sun.rmi.transport.StreamRemoteCall;
import sun.security.action.GetBooleanAction;

public class UnicastRef
  implements RemoteRef
{
  public static final Log clientRefLog = Log.getLog("sun.rmi.client.ref", "transport", Util.logLevel);
  public static final Log clientCallLog = Log.getLog("sun.rmi.client.call", "RMI", ((Boolean)AccessController.doPrivileged(new GetBooleanAction("sun.rmi.client.logCalls"))).booleanValue());
  protected LiveRef ref;

  public UnicastRef()
  {
  }

  public UnicastRef(LiveRef paramLiveRef)
  {
    this.ref = paramLiveRef;
  }

  public LiveRef getLiveRef()
  {
    return this.ref;
  }

  public Object invoke(Remote paramRemote, Method paramMethod, Object[] paramArrayOfObject, long paramLong)
    throws Exception
  {
    if (clientRefLog.isLoggable(Log.VERBOSE))
      clientRefLog.log(Log.VERBOSE, "method: " + paramMethod);
    if (clientCallLog.isLoggable(Log.VERBOSE))
      logClientCall(paramRemote, paramMethod);
    Connection localConnection = this.ref.getChannel().newConnection();
    StreamRemoteCall localStreamRemoteCall = null;
    boolean bool = true;
    int i = 0;
    try
    {
      Object localObject1;
      if (clientRefLog.isLoggable(Log.VERBOSE));
      try
      {
        Class localClass = paramMethod.getReturnType();
        if (localClass == Void.TYPE)
        {
          localObject1 = null;
          try
          {
            localStreamRemoteCall.done();
          }
          catch (IOException localIOException3)
          {
            bool = false;
          }
          if (i == 0)
            if (clientRefLog.isLoggable(Log.BRIEF))
              clientRefLog.log(Log.BRIEF, "free connection (reuse = " + bool + ")");
          return localObject1;
        }
        localObject1 = localStreamRemoteCall.getInputStream();
        Object localObject2 = unmarshalValue(localClass, (ObjectInput)localObject1);
        i = 1;
        clientRefLog.log(Log.BRIEF, "free connection (reuse = true)");
        this.ref.getChannel().free(localConnection, true);
        Object localObject3 = localObject2;
        try
        {
          localStreamRemoteCall.done();
        }
        catch (IOException localIOException4)
        {
          bool = false;
        }
        if (i == 0)
          if (clientRefLog.isLoggable(Log.BRIEF))
            clientRefLog.log(Log.BRIEF, "free connection (reuse = " + bool + ")");
        return localObject3;
      }
      catch (IOException localIOException2)
      {
        throw new UnmarshalException("error unmarshalling return", localIOException2);
      }
      catch (ClassNotFoundException localClassNotFoundException)
      {
        throw new UnmarshalException("error unmarshalling return", localClassNotFoundException);
      }
      finally
      {
        try
        {
          localStreamRemoteCall.done();
        }
        catch (IOException localIOException5)
        {
          bool = false;
        }
      }
    }
    catch (RuntimeException localRuntimeException)
    {
    }
    catch (RemoteException localRemoteException)
    {
    }
    catch (Error localError)
    {
    }
    finally
    {
      if (i == 0)
      {
        if (clientRefLog.isLoggable(Log.BRIEF))
          clientRefLog.log(Log.BRIEF, "free connection (reuse = " + bool + ")");
        this.ref.getChannel().free(localConnection, bool);
      }
    }
  }

  protected void marshalCustomCallData(ObjectOutput paramObjectOutput)
    throws IOException
  {
  }

  protected static void marshalValue(Class<?> paramClass, Object paramObject, ObjectOutput paramObjectOutput)
    throws IOException
  {
    if (paramClass.isPrimitive())
    {
      if (paramClass == Integer.TYPE)
      {
        paramObjectOutput.writeInt(((Integer)paramObject).intValue());
        return;
      }
      if (paramClass == Boolean.TYPE)
      {
        paramObjectOutput.writeBoolean(((Boolean)paramObject).booleanValue());
        return;
      }
      if (paramClass == Byte.TYPE)
      {
        paramObjectOutput.writeByte(((Byte)paramObject).byteValue());
        return;
      }
      if (paramClass == Character.TYPE)
      {
        paramObjectOutput.writeChar(((Character)paramObject).charValue());
        return;
      }
      if (paramClass == Short.TYPE)
      {
        paramObjectOutput.writeShort(((Short)paramObject).shortValue());
        return;
      }
      if (paramClass == Long.TYPE)
      {
        paramObjectOutput.writeLong(((Long)paramObject).longValue());
        return;
      }
      if (paramClass == Float.TYPE)
      {
        paramObjectOutput.writeFloat(((Float)paramObject).floatValue());
        return;
      }
      if (paramClass == Double.TYPE)
      {
        paramObjectOutput.writeDouble(((Double)paramObject).doubleValue());
        return;
      }
      throw new Error("Unrecognized primitive type: " + paramClass);
    }
    paramObjectOutput.writeObject(paramObject);
  }

  protected static Object unmarshalValue(Class<?> paramClass, ObjectInput paramObjectInput)
    throws IOException, ClassNotFoundException
  {
    if (paramClass.isPrimitive())
    {
      if (paramClass == Integer.TYPE)
        return Integer.valueOf(paramObjectInput.readInt());
      if (paramClass == Boolean.TYPE)
        return Boolean.valueOf(paramObjectInput.readBoolean());
      if (paramClass == Byte.TYPE)
        return Byte.valueOf(paramObjectInput.readByte());
      if (paramClass == Character.TYPE)
        return Character.valueOf(paramObjectInput.readChar());
      if (paramClass == Short.TYPE)
        return Short.valueOf(paramObjectInput.readShort());
      if (paramClass == Long.TYPE)
        return Long.valueOf(paramObjectInput.readLong());
      if (paramClass == Float.TYPE)
        return Float.valueOf(paramObjectInput.readFloat());
      if (paramClass == Double.TYPE)
        return Double.valueOf(paramObjectInput.readDouble());
      throw new Error("Unrecognized primitive type: " + paramClass);
    }
    return paramObjectInput.readObject();
  }

  public RemoteCall newCall(RemoteObject paramRemoteObject, Operation[] paramArrayOfOperation, int paramInt, long paramLong)
    throws RemoteException
  {
    clientRefLog.log(Log.BRIEF, "get connection");
    Connection localConnection = this.ref.getChannel().newConnection();
    try
    {
      clientRefLog.log(Log.VERBOSE, "create call context");
      if (clientCallLog.isLoggable(Log.VERBOSE))
        logClientCall(paramRemoteObject, paramArrayOfOperation[paramInt]);
      StreamRemoteCall localStreamRemoteCall = new StreamRemoteCall(localConnection, this.ref.getObjID(), paramInt, paramLong);
      try
      {
        marshalCustomCallData(localStreamRemoteCall.getOutputStream());
      }
      catch (IOException localIOException)
      {
        throw new MarshalException("error marshaling custom call data");
      }
      return localStreamRemoteCall;
    }
    catch (RemoteException localRemoteException)
    {
      this.ref.getChannel().free(localConnection, false);
      throw localRemoteException;
    }
  }

  public void invoke(RemoteCall paramRemoteCall)
    throws Exception
  {
    try
    {
      clientRefLog.log(Log.VERBOSE, "execute call");
      paramRemoteCall.executeCall();
    }
    catch (RemoteException localRemoteException)
    {
      clientRefLog.log(Log.BRIEF, "exception: ", localRemoteException);
      free(paramRemoteCall, false);
      throw localRemoteException;
    }
    catch (Error localError)
    {
      clientRefLog.log(Log.BRIEF, "error: ", localError);
      free(paramRemoteCall, false);
      throw localError;
    }
    catch (RuntimeException localRuntimeException)
    {
      clientRefLog.log(Log.BRIEF, "exception: ", localRuntimeException);
      free(paramRemoteCall, false);
      throw localRuntimeException;
    }
    catch (Exception localException)
    {
      clientRefLog.log(Log.BRIEF, "exception: ", localException);
      free(paramRemoteCall, true);
      throw localException;
    }
  }

  private void free(RemoteCall paramRemoteCall, boolean paramBoolean)
    throws RemoteException
  {
    Connection localConnection = ((StreamRemoteCall)paramRemoteCall).getConnection();
    this.ref.getChannel().free(localConnection, paramBoolean);
  }

  public void done(RemoteCall paramRemoteCall)
    throws RemoteException
  {
    clientRefLog.log(Log.BRIEF, "free connection (reuse = true)");
    free(paramRemoteCall, true);
    try
    {
      paramRemoteCall.done();
    }
    catch (IOException localIOException)
    {
    }
  }

  void logClientCall(Object paramObject1, Object paramObject2)
  {
    clientCallLog.log(Log.VERBOSE, "outbound call: " + this.ref + " : " + paramObject1.getClass().getName() + this.ref.getObjID().toString() + ": " + paramObject2);
  }

  public String getRefClass(ObjectOutput paramObjectOutput)
  {
    return "UnicastRef";
  }

  public void writeExternal(ObjectOutput paramObjectOutput)
    throws IOException
  {
    this.ref.write(paramObjectOutput, false);
  }

  public void readExternal(ObjectInput paramObjectInput)
    throws IOException, ClassNotFoundException
  {
    this.ref = LiveRef.read(paramObjectInput, false);
  }

  public String remoteToString()
  {
    return Util.getUnqualifiedName(super.getClass()) + " [liveRef: " + this.ref + "]";
  }

  public int remoteHashCode()
  {
    return this.ref.hashCode();
  }

  public boolean remoteEquals(RemoteRef paramRemoteRef)
  {
    if (paramRemoteRef instanceof UnicastRef)
      return this.ref.remoteEquals(((UnicastRef)paramRemoteRef).ref);
    return false;
  }
}