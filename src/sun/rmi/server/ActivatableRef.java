package sun.rmi.server;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.MalformedURLException;
import java.rmi.ConnectException;
import java.rmi.ConnectIOException;
import java.rmi.MarshalException;
import java.rmi.NoSuchObjectException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.ServerError;
import java.rmi.ServerException;
import java.rmi.StubNotFoundException;
import java.rmi.UnknownHostException;
import java.rmi.UnmarshalException;
import java.rmi.activation.ActivateFailedException;
import java.rmi.activation.ActivationDesc;
import java.rmi.activation.ActivationException;
import java.rmi.activation.ActivationID;
import java.rmi.activation.UnknownObjectException;
import java.rmi.server.Operation;
import java.rmi.server.RMIClassLoader;
import java.rmi.server.RemoteCall;
import java.rmi.server.RemoteObject;
import java.rmi.server.RemoteObjectInvocationHandler;
import java.rmi.server.RemoteRef;
import java.rmi.server.RemoteStub;

public class ActivatableRef
  implements RemoteRef
{
  private static final long serialVersionUID = 7579060052569229166L;
  protected ActivationID id;
  protected RemoteRef ref;
  transient boolean force = false;
  private static final int MAX_RETRIES = 3;
  private static final String versionComplaint = "activation requires 1.2 stubs";

  public ActivatableRef()
  {
  }

  public ActivatableRef(ActivationID paramActivationID, RemoteRef paramRemoteRef)
  {
    this.id = paramActivationID;
    this.ref = paramRemoteRef;
  }

  public static Remote getStub(ActivationDesc paramActivationDesc, ActivationID paramActivationID)
    throws StubNotFoundException
  {
    String str = paramActivationDesc.getClassName();
    try
    {
      Class localClass = RMIClassLoader.loadClass(paramActivationDesc.getLocation(), str);
      ActivatableRef localActivatableRef = new ActivatableRef(paramActivationID, null);
      return Util.createProxy(localClass, localActivatableRef, false);
    }
    catch (IllegalArgumentException localIllegalArgumentException)
    {
      throw new StubNotFoundException("class implements an illegal remote interface", localIllegalArgumentException);
    }
    catch (ClassNotFoundException localClassNotFoundException)
    {
      throw new StubNotFoundException("unable to load class: " + str, localClassNotFoundException);
    }
    catch (MalformedURLException localMalformedURLException)
    {
      throw new StubNotFoundException("malformed URL", localMalformedURLException);
    }
  }

  public Object invoke(Remote paramRemote, Method paramMethod, Object[] paramArrayOfObject, long paramLong)
    throws Exception
  {
    Object localObject1;
    boolean bool = false;
    Object localObject2 = null;
    synchronized (this)
    {
      if (this.ref == null)
      {
        localObject1 = activate(bool);
        bool = true;
      }
      else
      {
        localObject1 = this.ref;
      }
    }
    for (int i = 3; i > 0; --i)
    {
      try
      {
        return ((RemoteRef)localObject1).invoke(paramRemote, paramMethod, paramArrayOfObject, paramLong);
      }
      catch (NoSuchObjectException localNoSuchObjectException)
      {
        localObject2 = localNoSuchObjectException;
      }
      catch (ConnectException localConnectException)
      {
        localObject2 = localConnectException;
      }
      catch (UnknownHostException localUnknownHostException)
      {
        localObject2 = localUnknownHostException;
      }
      catch (ConnectIOException localConnectIOException)
      {
        localObject2 = localConnectIOException;
      }
      catch (MarshalException localMarshalException)
      {
        throw localMarshalException;
      }
      catch (ServerError localServerError)
      {
        throw localServerError;
      }
      catch (ServerException localServerException)
      {
        throw localServerException;
      }
      catch (RemoteException localRemoteException)
      {
        synchronized (this)
        {
          if (localObject1 == this.ref)
            this.ref = null;
        }
        throw localRemoteException;
      }
      if (i > 1)
        synchronized (this)
        {
          if ((((RemoteRef)localObject1).remoteEquals(this.ref)) || (this.ref == null))
          {
            ??? = activate(bool);
            if ((((RemoteRef)???).remoteEquals((RemoteRef)localObject1)) && (localObject2 instanceof NoSuchObjectException) && (!(bool)))
              ??? = activate(true);
            localObject1 = ???;
            bool = true;
          }
          else
          {
            localObject1 = this.ref;
            bool = false;
          }
        }
    }
    throw localObject2;
  }

  private synchronized RemoteRef getRef()
    throws RemoteException
  {
    if (this.ref == null)
      this.ref = activate(false);
    return this.ref;
  }

  private RemoteRef activate(boolean paramBoolean)
    throws RemoteException
  {
    if ((!($assertionsDisabled)) && (!(Thread.holdsLock(this))))
      throw new AssertionError();
    this.ref = null;
    try
    {
      Remote localRemote = this.id.activate(paramBoolean);
      ActivatableRef localActivatableRef = null;
      if (localRemote instanceof RemoteStub)
      {
        localActivatableRef = (ActivatableRef)((RemoteStub)localRemote).getRef();
      }
      else
      {
        RemoteObjectInvocationHandler localRemoteObjectInvocationHandler = (RemoteObjectInvocationHandler)Proxy.getInvocationHandler(localRemote);
        localActivatableRef = (ActivatableRef)localRemoteObjectInvocationHandler.getRef();
      }
      this.ref = localActivatableRef.ref;
      return this.ref;
    }
    catch (ConnectException localConnectException)
    {
      throw new ConnectException("activation failed", localConnectException);
    }
    catch (RemoteException localRemoteException)
    {
      throw new ConnectIOException("activation failed", localRemoteException);
    }
    catch (UnknownObjectException localUnknownObjectException)
    {
      throw new NoSuchObjectException("object not registered");
    }
    catch (ActivationException localActivationException)
    {
      throw new ActivateFailedException("activation failed", localActivationException);
    }
  }

  public synchronized RemoteCall newCall(RemoteObject paramRemoteObject, Operation[] paramArrayOfOperation, int paramInt, long paramLong)
    throws RemoteException
  {
    throw new UnsupportedOperationException("activation requires 1.2 stubs");
  }

  public void invoke(RemoteCall paramRemoteCall)
    throws Exception
  {
    throw new UnsupportedOperationException("activation requires 1.2 stubs");
  }

  public void done(RemoteCall paramRemoteCall)
    throws RemoteException
  {
    throw new UnsupportedOperationException("activation requires 1.2 stubs");
  }

  public String getRefClass(ObjectOutput paramObjectOutput)
  {
    return "ActivatableRef";
  }

  public void writeExternal(ObjectOutput paramObjectOutput)
    throws IOException
  {
    RemoteRef localRemoteRef = this.ref;
    paramObjectOutput.writeObject(this.id);
    if (localRemoteRef == null)
    {
      paramObjectOutput.writeUTF("");
    }
    else
    {
      paramObjectOutput.writeUTF(localRemoteRef.getRefClass(paramObjectOutput));
      localRemoteRef.writeExternal(paramObjectOutput);
    }
  }

  public void readExternal(ObjectInput paramObjectInput)
    throws IOException, ClassNotFoundException
  {
    this.id = ((ActivationID)paramObjectInput.readObject());
    this.ref = null;
    String str = paramObjectInput.readUTF();
    if (str.equals(""))
      return;
    try
    {
      Class localClass = Class.forName("sun.rmi.server." + str);
      this.ref = ((RemoteRef)localClass.newInstance());
      this.ref.readExternal(paramObjectInput);
    }
    catch (InstantiationException localInstantiationException)
    {
      throw new UnmarshalException("Unable to create remote reference", localInstantiationException);
    }
    catch (IllegalAccessException localIllegalAccessException)
    {
      throw new UnmarshalException("Illegal access creating remote reference");
    }
  }

  public String remoteToString()
  {
    return Util.getUnqualifiedName(super.getClass()) + " [remoteRef: " + this.ref + "]";
  }

  public int remoteHashCode()
  {
    return this.id.hashCode();
  }

  public boolean remoteEquals(RemoteRef paramRemoteRef)
  {
    if (paramRemoteRef instanceof ActivatableRef)
      return this.id.equals(((ActivatableRef)paramRemoteRef).id);
    return false;
  }
}