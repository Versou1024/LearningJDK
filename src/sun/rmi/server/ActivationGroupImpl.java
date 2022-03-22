package sun.rmi.server;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import java.rmi.MarshalledObject;
import java.rmi.NoSuchObjectException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.activation.Activatable;
import java.rmi.activation.ActivationDesc;
import java.rmi.activation.ActivationException;
import java.rmi.activation.ActivationGroup;
import java.rmi.activation.ActivationGroupID;
import java.rmi.activation.ActivationID;
import java.rmi.activation.UnknownObjectException;
import java.rmi.server.RMIClassLoader;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.RMISocketFactory;
import java.rmi.server.UnicastRemoteObject;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import sun.rmi.registry.RegistryImpl;

public class ActivationGroupImpl extends ActivationGroup
{
  private static final long serialVersionUID = 5758693559430427303L;
  private final Hashtable<ActivationID, ActiveEntry> active = new Hashtable();
  private boolean groupInactive = false;
  private final ActivationGroupID groupID;
  private final List<ActivationID> lockedIDs = new ArrayList();

  public ActivationGroupImpl(ActivationGroupID paramActivationGroupID, MarshalledObject<?> paramMarshalledObject)
    throws RemoteException
  {
    super(paramActivationGroupID);
    this.groupID = paramActivationGroupID;
    unexportObject(this, true);
    ServerSocketFactoryImpl localServerSocketFactoryImpl = new ServerSocketFactoryImpl(null);
    UnicastRemoteObject.exportObject(this, 0, null, localServerSocketFactoryImpl);
    if (System.getSecurityManager() == null)
      try
      {
        System.setSecurityManager(new SecurityManager());
      }
      catch (Exception localException)
      {
        throw new RemoteException("unable to set security manager", localException);
      }
  }

  private void acquireLock(ActivationID paramActivationID)
  {
    while (true)
    {
      label38: ActivationID localActivationID1;
      synchronized (this.lockedIDs)
      {
        int i = this.lockedIDs.indexOf(paramActivationID);
        if (i >= 0)
          break label38;
        this.lockedIDs.add(paramActivationID);
        return;
        localActivationID1 = (ActivationID)this.lockedIDs.get(i);
      }
      synchronized (localActivationID1)
      {
        label132: synchronized (this.lockedIDs)
        {
          while (true)
          {
            int j;
            while (true)
            {
              j = this.lockedIDs.indexOf(localActivationID1);
              if (j >= 0)
                break;
              monitorexit;
            }
            ActivationID localActivationID2 = (ActivationID)this.lockedIDs.get(j);
            if (localActivationID2 == localActivationID1)
              break label132;
            monitorexit;
          }
        }
        try
        {
          localActivationID1.wait();
        }
        catch (InterruptedException localInterruptedException)
        {
        }
      }
    }
  }

  private void releaseLock(ActivationID paramActivationID)
  {
    synchronized (this.lockedIDs)
    {
      paramActivationID = (ActivationID)this.lockedIDs.remove(this.lockedIDs.indexOf(paramActivationID));
    }
    synchronized (paramActivationID)
    {
      paramActivationID.notifyAll();
    }
  }

  public MarshalledObject<? extends Remote> newInstance(ActivationID paramActivationID, ActivationDesc paramActivationDesc)
    throws ActivationException, RemoteException
  {
    RegistryImpl.checkAccess("ActivationInstantiator.newInstance");
    if (!(this.groupID.equals(paramActivationDesc.getGroupID())))
      throw new ActivationException("newInstance in wrong group");
    try
    {
      acquireLock(paramActivationID);
      synchronized (this)
      {
        if (this.groupInactive == true)
          throw new InactiveGroupException("group is inactive");
      }
      ??? = (ActiveEntry)this.active.get(paramActivationID);
      if (??? != null)
      {
        localObject3 = ((ActiveEntry)???).mobj;
        jsr 283;
        return localObject3;
      }
      Object localObject3 = paramActivationDesc.getClassName();
      Class localClass = RMIClassLoader.loadClass(paramActivationDesc.getLocation(), (String)localObject3).asSubclass(Remote.class);
      Remote localRemote = null;
      Thread localThread = Thread.currentThread();
      ClassLoader localClassLoader1 = localThread.getContextClassLoader();
      ClassLoader localClassLoader2 = localClass.getClassLoader();
      ClassLoader localClassLoader3 = (covers(localClassLoader2, localClassLoader1)) ? localClassLoader2 : localClassLoader1;
      try
      {
        localRemote = (Remote)AccessController.doPrivileged(new PrivilegedExceptionAction(this, localClass, localThread, localClassLoader3, paramActivationID, paramActivationDesc, localClassLoader1)
        {
          public Remote run()
            throws InstantiationException, NoSuchMethodException, IllegalAccessException, InvocationTargetException
          {
            Constructor localConstructor = this.val$cl.getDeclaredConstructor(new Class[] { ActivationID.class, MarshalledObject.class });
            localConstructor.setAccessible(true);
            try
            {
              this.val$t.setContextClassLoader(this.val$ccl);
              Remote localRemote = (Remote)localConstructor.newInstance(new Object[] { this.val$id, this.val$desc.getData() });
              return localRemote;
            }
            finally
            {
              this.val$t.setContextClassLoader(this.val$savedCcl);
            }
          }
        });
      }
      catch (PrivilegedActionException localPrivilegedActionException)
      {
        Exception localException2 = localPrivilegedActionException.getException();
        if (localException2 instanceof InstantiationException)
          throw ((InstantiationException)localException2);
        if (localException2 instanceof NoSuchMethodException)
          throw ((NoSuchMethodException)localException2);
        if (localException2 instanceof IllegalAccessException)
          throw ((IllegalAccessException)localException2);
        if (localException2 instanceof InvocationTargetException)
          throw ((InvocationTargetException)localException2);
        if (localException2 instanceof RuntimeException)
          throw ((RuntimeException)localException2);
        if (localException2 instanceof Error)
          throw ((Error)localException2);
      }
      ??? = new ActiveEntry(localRemote);
      this.active.put(paramActivationID, ???);
      MarshalledObject localMarshalledObject = ((ActiveEntry)???).mobj;
      return localMarshalledObject;
    }
    catch (NoSuchMethodException localNoSuchMethodException)
    {
    }
    catch (NoSuchMethodError localNoSuchMethodError)
    {
    }
    catch (InvocationTargetException localInvocationTargetException)
    {
    }
    catch (Exception localException1)
    {
    }
    finally
    {
      releaseLock(paramActivationID);
      checkInactiveGroup();
    }
  }

  public boolean inactiveObject(ActivationID paramActivationID)
    throws ActivationException, UnknownObjectException, RemoteException
  {
    try
    {
      acquireLock(paramActivationID);
      synchronized (this)
      {
        if (this.groupInactive == true)
          throw new ActivationException("group is inactive");
      }
      ??? = (ActiveEntry)this.active.get(paramActivationID);
      if (??? == null)
        throw new UnknownObjectException("object not active");
      try
      {
        if (!(Activatable.unexportObject(((ActiveEntry)???).impl, false)))
        {
          int i = 0;
          jsr 42;
          return i;
        }
      }
      catch (NoSuchObjectException localNoSuchObjectException)
      {
      }
      try
      {
        super.inactiveObject(paramActivationID);
      }
      catch (UnknownObjectException localUnknownObjectException)
      {
      }
      this.active.remove(paramActivationID);
    }
    finally
    {
      releaseLock(paramActivationID);
      checkInactiveGroup();
    }
    return true;
  }

  private void checkInactiveGroup()
  {
    int i = 0;
    synchronized (this)
    {
      if ((this.active.size() == 0) && (this.lockedIDs.size() == 0) && (!(this.groupInactive)))
      {
        this.groupInactive = true;
        i = 1;
      }
    }
    if (i != 0)
    {
      try
      {
        super.inactiveGroup();
      }
      catch (Exception localException)
      {
      }
      try
      {
        UnicastRemoteObject.unexportObject(this, true);
      }
      catch (NoSuchObjectException localNoSuchObjectException)
      {
      }
    }
  }

  public void activeObject(ActivationID paramActivationID, Remote paramRemote)
    throws ActivationException, UnknownObjectException, RemoteException
  {
    try
    {
      acquireLock(paramActivationID);
      synchronized (this)
      {
        if (this.groupInactive == true)
          throw new ActivationException("group is inactive");
      }
      if (!(this.active.contains(paramActivationID)))
      {
        ??? = new ActiveEntry(paramRemote);
        this.active.put(paramActivationID, ???);
        try
        {
          super.activeObject(paramActivationID, ((ActiveEntry)???).mobj);
        }
        catch (RemoteException localRemoteException)
        {
        }
      }
    }
    finally
    {
      releaseLock(paramActivationID);
      checkInactiveGroup();
    }
  }

  private static boolean covers(ClassLoader paramClassLoader1, ClassLoader paramClassLoader2)
  {
    if (paramClassLoader2 == null)
      return true;
    if (paramClassLoader1 == null)
      return false;
    do
    {
      if (paramClassLoader1 == paramClassLoader2)
        return true;
      paramClassLoader1 = paramClassLoader1.getParent();
    }
    while (paramClassLoader1 != null);
    return false;
  }

  private static class ActiveEntry
  {
    Remote impl;
    MarshalledObject<Remote> mobj;

    ActiveEntry(Remote paramRemote)
      throws ActivationException
    {
      this.impl = paramRemote;
      try
      {
        this.mobj = new MarshalledObject(paramRemote);
      }
      catch (IOException localIOException)
      {
        throw new ActivationException("failed to marshal remote object", localIOException);
      }
    }
  }

  private static class ServerSocketFactoryImpl
  implements RMIServerSocketFactory
  {
    public ServerSocket createServerSocket(int paramInt)
      throws IOException
    {
      RMISocketFactory localRMISocketFactory = RMISocketFactory.getSocketFactory();
      if (localRMISocketFactory == null)
        localRMISocketFactory = RMISocketFactory.getDefaultSocketFactory();
      return localRMISocketFactory.createServerSocket(paramInt);
    }
  }
}