package sun.rmi.server;

import com.sun.rmi.rmid.ExecOptionPermission;
import com.sun.rmi.rmid.ExecPermission;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.Channel;
import java.nio.channels.ServerSocketChannel;
import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.ConnectException;
import java.rmi.ConnectIOException;
import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.activation.ActivationDesc;
import java.rmi.activation.ActivationException;
import java.rmi.activation.ActivationGroup;
import java.rmi.activation.ActivationGroupDesc.CommandEnvironment;
import java.rmi.activation.ActivationGroupID;
import java.rmi.activation.ActivationID;
import java.rmi.activation.ActivationInstantiator;
import java.rmi.activation.ActivationMonitor;
import java.rmi.activation.ActivationSystem;
import java.rmi.activation.Activator;
import java.rmi.activation.UnknownGroupException;
import java.rmi.activation.UnknownObjectException;
import java.rmi.registry.Registry;
import java.rmi.server.ObjID;
import java.rmi.server.RMIClassLoader;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.RemoteObject;
import java.rmi.server.RemoteServer;
import java.rmi.server.UnicastRemoteObject;
import java.security.AccessControlException;
import java.security.AccessController;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.Policy;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;
import java.security.cert.Certificate;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import sun.rmi.log.LogHandler;
import sun.rmi.log.ReliableLog;
import sun.rmi.registry.RegistryImpl;
import sun.rmi.transport.LiveRef;
import sun.security.action.GetBooleanAction;
import sun.security.action.GetIntegerAction;
import sun.security.action.GetPropertyAction;
import sun.security.provider.PolicyFile;

public class Activation
  implements Serializable
{
  private static final long serialVersionUID = 2921265612698155191L;
  private static final byte MAJOR_VERSION = 1;
  private static final byte MINOR_VERSION = 0;
  private static Object execPolicy;
  private static Method execPolicyMethod;
  private static boolean debugExec;
  private Map<ActivationID, ActivationGroupID> idTable = new HashMap();
  private Map<ActivationGroupID, GroupEntry> groupTable = new HashMap();
  private byte majorVersion = 1;
  private byte minorVersion = 0;
  private transient int groupSemaphore;
  private transient int groupCounter;
  private transient ReliableLog log;
  private transient int numUpdates;
  private transient String[] command;
  private static final long groupTimeout = getInt("sun.rmi.activation.groupTimeout", 60000);
  private static final int snapshotInterval = getInt("sun.rmi.activation.snapshotInterval", 200);
  private static final long execTimeout = getInt("sun.rmi.activation.execTimeout", 30000);
  private static final Object initLock = new Object();
  private static boolean initDone = false;
  private transient Activator activator;
  private transient Activator activatorStub;
  private transient ActivationSystem system;
  private transient ActivationSystem systemStub;
  private transient ActivationMonitor monitor;
  private transient Registry registry;
  private volatile transient boolean shuttingDown = false;
  private volatile transient Object startupLock;
  private transient Thread shutdownHook;
  private static ResourceBundle resources = null;

  private static int getInt(String paramString, int paramInt)
  {
    return ((Integer)AccessController.doPrivileged(new GetIntegerAction(paramString, paramInt))).intValue();
  }

  private static void startActivation(int paramInt, RMIServerSocketFactory paramRMIServerSocketFactory, String paramString, String[] paramArrayOfString)
    throws Exception
  {
    ReliableLog localReliableLog = new ReliableLog(paramString, new ActLogHandler());
    Activation localActivation = (Activation)localReliableLog.recover();
    localActivation.init(paramInt, paramRMIServerSocketFactory, localReliableLog, paramArrayOfString);
  }

  private void init(int paramInt, RMIServerSocketFactory paramRMIServerSocketFactory, ReliableLog paramReliableLog, String[] paramArrayOfString)
    throws Exception
  {
    this.log = paramReliableLog;
    this.numUpdates = 0;
    this.shutdownHook = new ShutdownHook(this);
    this.groupSemaphore = getInt("sun.rmi.activation.groupThrottle", 3);
    this.groupCounter = 0;
    Runtime.getRuntime().addShutdownHook(this.shutdownHook);
    ActivationGroupID[] arrayOfActivationGroupID = (ActivationGroupID[])this.groupTable.keySet().toArray(new ActivationGroupID[this.groupTable.size()]);
    synchronized (this.startupLock = new Object())
    {
      this.activator = new ActivatorImpl(this, paramInt, paramRMIServerSocketFactory);
      this.activatorStub = ((Activator)RemoteObject.toStub(this.activator));
      this.system = new ActivationSystemImpl(this, paramInt, paramRMIServerSocketFactory);
      this.systemStub = ((ActivationSystem)RemoteObject.toStub(this.system));
      this.monitor = new ActivationMonitorImpl(this, paramInt, paramRMIServerSocketFactory);
      initCommand(paramArrayOfString);
      this.registry = new SystemRegistryImpl(paramInt, null, paramRMIServerSocketFactory, this.systemStub);
      if (paramRMIServerSocketFactory != null)
        synchronized (initLock)
        {
          initDone = true;
          initLock.notifyAll();
        }
    }
    this.startupLock = null;
    int i = arrayOfActivationGroupID.length;
    if (--i >= 0)
      try
      {
        getGroupEntry(arrayOfActivationGroupID[i]).restartServices();
      }
      catch (UnknownGroupException localUnknownGroupException)
      {
        System.err.println(getTextResource("rmid.restart.group.warning"));
        localUnknownGroupException.printStackTrace();
      }
  }

  private void checkShutdown()
    throws ActivationException
  {
    Object localObject1 = this.startupLock;
    if (localObject1 != null)
      synchronized (localObject1)
      {
      }
    if (this.shuttingDown == true)
      throw new ActivationException("activation system shutting down");
  }

  private static void unexport(Remote paramRemote)
  {
    try
    {
      if (UnicastRemoteObject.unexportObject(paramRemote, false) == true)
        return;
      Thread.sleep(100L);
    }
    catch (Exception localException)
    {
    }
  }

  private ActivationGroupID getGroupID(ActivationID paramActivationID)
    throws UnknownObjectException
  {
    synchronized (this.idTable)
    {
      ActivationGroupID localActivationGroupID = (ActivationGroupID)this.idTable.get(paramActivationID);
      if (localActivationGroupID == null)
        break label29;
      label29: return localActivationGroupID;
    }
    throw new UnknownObjectException("unknown object: " + paramActivationID);
  }

  private GroupEntry getGroupEntry(ActivationGroupID paramActivationGroupID)
    throws UnknownGroupException
  {
    if (paramActivationGroupID.getClass() == ActivationGroupID.class)
      synchronized (this.groupTable)
      {
        GroupEntry localGroupEntry = (GroupEntry)this.groupTable.get(paramActivationGroupID);
        if ((localGroupEntry == null) || (localGroupEntry.removed))
          break label46;
        label46: return localGroupEntry;
      }
    throw new UnknownGroupException("group unknown");
  }

  private GroupEntry getGroupEntry(ActivationID paramActivationID)
    throws UnknownObjectException
  {
    ActivationGroupID localActivationGroupID = getGroupID(paramActivationID);
    synchronized (this.groupTable)
    {
      GroupEntry localGroupEntry = (GroupEntry)this.groupTable.get(localActivationGroupID);
      if (localGroupEntry == null)
        break label38;
      label38: return localGroupEntry;
    }
    throw new UnknownObjectException("object's group removed");
  }

  private String[] activationArgs(java.rmi.activation.ActivationGroupDesc paramActivationGroupDesc)
  {
    ActivationGroupDesc.CommandEnvironment localCommandEnvironment = paramActivationGroupDesc.getCommandEnvironment();
    ArrayList localArrayList = new ArrayList();
    localArrayList.add(((localCommandEnvironment != null) && (localCommandEnvironment.getCommandPath() != null)) ? localCommandEnvironment.getCommandPath() : this.command[0]);
    if ((localCommandEnvironment != null) && (localCommandEnvironment.getCommandOptions() != null))
      localArrayList.addAll(Arrays.asList(localCommandEnvironment.getCommandOptions()));
    Properties localProperties = paramActivationGroupDesc.getPropertyOverrides();
    if (localProperties != null)
    {
      Enumeration localEnumeration = localProperties.propertyNames();
      while (localEnumeration.hasMoreElements())
      {
        String str = (String)localEnumeration.nextElement();
        localArrayList.add("-D" + str + "=" + localProperties.getProperty(str));
      }
    }
    for (int i = 1; i < this.command.length; ++i)
      localArrayList.add(this.command[i]);
    String[] arrayOfString = new String[localArrayList.size()];
    System.arraycopy(localArrayList.toArray(), 0, arrayOfString, 0, arrayOfString.length);
    return arrayOfString;
  }

  private void checkArgs(java.rmi.activation.ActivationGroupDesc paramActivationGroupDesc, String[] paramArrayOfString)
    throws SecurityException, ActivationException
  {
    if (execPolicyMethod != null)
    {
      if (paramArrayOfString == null)
        paramArrayOfString = activationArgs(paramActivationGroupDesc);
      try
      {
        execPolicyMethod.invoke(execPolicy, new Object[] { paramActivationGroupDesc, paramArrayOfString });
      }
      catch (InvocationTargetException localInvocationTargetException)
      {
        Throwable localThrowable = localInvocationTargetException.getTargetException();
        if (localThrowable instanceof SecurityException)
          throw ((SecurityException)localThrowable);
        throw new ActivationException(execPolicyMethod.getName() + ": unexpected exception", localInvocationTargetException);
      }
      catch (Exception localException)
      {
        throw new ActivationException(execPolicyMethod.getName() + ": unexpected exception", localException);
      }
    }
  }

  private void addLogRecord(LogRecord paramLogRecord)
    throws ActivationException
  {
    synchronized (this.log)
    {
      checkShutdown();
      try
      {
        this.log.update(paramLogRecord, true);
      }
      catch (Exception localException1)
      {
        this.numUpdates = snapshotInterval;
        System.err.println(getTextResource("rmid.log.update.warning"));
        localException1.printStackTrace();
      }
      if (++this.numUpdates >= snapshotInterval)
        break label66;
      return;
      try
      {
        label66: this.log.snapshot(this);
        this.numUpdates = 0;
      }
      catch (Exception localException2)
      {
        System.err.println(getTextResource("rmid.log.snapshot.warning"));
        localException2.printStackTrace();
        try
        {
          this.system.shutdown();
        }
        catch (RemoteException localRemoteException)
        {
        }
        throw new ActivationException("log snapshot failed", localException2);
      }
    }
  }

  private void initCommand(String[] paramArrayOfString)
  {
    this.command = new String[paramArrayOfString.length + 2];
    AccessController.doPrivileged(new PrivilegedAction(this)
    {
      public Void run()
      {
        try
        {
          Activation.access$2900(this.this$0)[0] = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
        }
        catch (Exception localException)
        {
          System.err.println(Activation.access$1700("rmid.unfound.java.home.property"));
          Activation.access$2900(this.this$0)[0] = "java";
        }
        return null;
      }
    });
    System.arraycopy(paramArrayOfString, 0, this.command, 1, paramArrayOfString.length);
    this.command[(this.command.length - 1)] = "sun.rmi.server.ActivationGroupInit";
  }

  private static void bomb(String paramString)
  {
    System.err.println("rmid: " + paramString);
    System.err.println(MessageFormat.format(getTextResource("rmid.usage"), new Object[] { "rmid" }));
    System.exit(1);
  }

  public static void main(String[] paramArrayOfString)
  {
    int i = 0;
    if (System.getSecurityManager() == null)
      System.setSecurityManager(new SecurityManager());
    try
    {
      Exception localException1 = 1098;
      ActivationServerSocketFactory localActivationServerSocketFactory = null;
      Channel localChannel = (Channel)AccessController.doPrivileged(new PrivilegedExceptionAction()
      {
        public Channel run()
          throws IOException
        {
          return System.inheritedChannel();
        }
      });
      if ((localChannel != null) && (localChannel instanceof ServerSocketChannel))
      {
        AccessController.doPrivileged(new PrivilegedExceptionAction()
        {
          public Void run()
            throws IOException
          {
            File localFile = File.createTempFile("rmid-err", null, null);
            PrintStream localPrintStream = new PrintStream(new FileOutputStream(localFile));
            System.setErr(localPrintStream);
            return null;
          }
        });
        localObject = ((ServerSocketChannel)localChannel).socket();
        localException1 = ((ServerSocket)localObject).getLocalPort();
        localActivationServerSocketFactory = new ActivationServerSocketFactory((ServerSocket)localObject);
        System.err.println(new Date());
        System.err.println(getTextResource("rmid.inherited.channel.info") + ": " + localChannel);
      }
      Object localObject = null;
      ArrayList localArrayList = new ArrayList();
      for (int j = 0; j < paramArrayOfString.length; ++j)
        if (paramArrayOfString[j].equals("-port"))
        {
          if (localActivationServerSocketFactory != null)
            bomb(getTextResource("rmid.syntax.port.badarg"));
          if (j + 1 < paramArrayOfString.length)
            try
            {
              localException1 = Integer.parseInt(paramArrayOfString[(++j)]);
            }
            catch (NumberFormatException localNumberFormatException)
            {
              bomb(getTextResource("rmid.syntax.port.badnumber"));
            }
          else
            bomb(getTextResource("rmid.syntax.port.missing"));
        }
        else if (paramArrayOfString[j].equals("-log"))
        {
          if (j + 1 < paramArrayOfString.length)
            localObject = paramArrayOfString[(++j)];
          else
            bomb(getTextResource("rmid.syntax.log.missing"));
        }
        else if (paramArrayOfString[j].equals("-stop"))
        {
          i = 1;
        }
        else if (paramArrayOfString[j].startsWith("-C"))
        {
          localArrayList.add(paramArrayOfString[j].substring(2));
        }
        else
        {
          bomb(MessageFormat.format(getTextResource("rmid.syntax.illegal.option"), new Object[] { paramArrayOfString[j] }));
        }
      if (localObject == null)
        if (localActivationServerSocketFactory != null)
          bomb(getTextResource("rmid.syntax.log.required"));
        else
          localObject = "log";
      debugExec = ((Boolean)AccessController.doPrivileged(new GetBooleanAction("sun.rmi.server.activation.debugExec"))).booleanValue();
      String str = (String)AccessController.doPrivileged(new GetPropertyAction("sun.rmi.activation.execPolicy", null));
      if (str == null)
      {
        if (i == 0)
          DefaultExecPolicy.checkConfiguration();
        str = "default";
      }
      if (!(str.equals("none")))
      {
        if ((str.equals("")) || (str.equals("default")))
          str = DefaultExecPolicy.class.getName();
        try
        {
          Class localClass = RMIClassLoader.loadClass(str);
          execPolicy = localClass.newInstance();
          execPolicyMethod = localClass.getMethod("checkExecCommand", new Class[] { java.rmi.activation.ActivationGroupDesc.class, [Ljava.lang.String.class });
        }
        catch (Exception localException3)
        {
          if (debugExec)
          {
            System.err.println(getTextResource("rmid.exec.policy.exception"));
            localException3.printStackTrace();
          }
          bomb(getTextResource("rmid.exec.policy.invalid"));
        }
      }
      if (i == 1)
      {
        localException3 = localException1;
        AccessController.doPrivileged(new PrivilegedAction(localException3)
        {
          public Void run()
          {
            System.setProperty("java.rmi.activation.port", Integer.toString(this.val$finalPort));
            return null;
          }
        });
        ActivationSystem localActivationSystem = ActivationGroup.getSystem();
        localActivationSystem.shutdown();
        System.exit(0);
      }
      label606: startActivation(localException1, localActivationServerSocketFactory, (String)localObject, (String[])localArrayList.toArray(new String[localArrayList.size()]));
    }
    catch (Exception localException2)
    {
      try
      {
        Thread.sleep(9223372036854775807L);
      }
      catch (InterruptedException localInterruptedException)
      {
        break label606:
        localException2 = localException2;
        System.err.println(MessageFormat.format(getTextResource("rmid.unexpected.exception"), new Object[] { localException2 }));
        localException2.printStackTrace();
        System.exit(1);
      }
    }
  }

  private static String getTextResource(String paramString)
  {
    if (resources == null)
    {
      try
      {
        resources = ResourceBundle.getBundle("sun.rmi.rmid.resources.rmid");
      }
      catch (MissingResourceException localMissingResourceException1)
      {
      }
      if (resources == null)
        return "[missing resource file: " + paramString + "]";
    }
    String str = null;
    try
    {
      str = resources.getString(paramString);
    }
    catch (MissingResourceException localMissingResourceException2)
    {
    }
    if (str == null)
      return "[missing resource: " + paramString + "]";
    return str;
  }

  private synchronized String Pstartgroup()
    throws ActivationException
  {
    checkShutdown();
    if (this.groupSemaphore > 0)
    {
      this.groupSemaphore -= 1;
      return "Group-" + (this.groupCounter++);
    }
    try
    {
      super.wait();
    }
    catch (InterruptedException localInterruptedException)
    {
    }
  }

  private synchronized void Vstartgroup()
  {
    this.groupSemaphore += 1;
    super.notifyAll();
  }

  private static class ActLogHandler extends LogHandler
  {
    public Object initialSnapshot()
    {
      return new Activation(null);
    }

    public Object applyUpdate(Object paramObject1, Object paramObject2)
      throws Exception
    {
      return ((Activation.LogRecord)paramObject1).apply(paramObject2);
    }
  }

  class ActivationMonitorImpl extends UnicastRemoteObject
  implements ActivationMonitor
  {
    private static final long serialVersionUID = -6214940464757948867L;

    ActivationMonitorImpl(, int paramInt, RMIServerSocketFactory paramRMIServerSocketFactory)
      throws RemoteException
    {
      super(paramInt, null, paramRMIServerSocketFactory);
    }

    public void inactiveObject()
      throws UnknownObjectException, RemoteException
    {
      try
      {
        Activation.access$000(this.this$0);
      }
      catch (ActivationException localActivationException)
      {
        return;
      }
      RegistryImpl.checkAccess("Activator.inactiveObject");
      Activation.access$100(this.this$0, paramActivationID).inactiveObject(paramActivationID);
    }

    public void activeObject(, java.rmi.MarshalledObject<? extends Remote> paramMarshalledObject)
      throws UnknownObjectException, RemoteException
    {
      try
      {
        Activation.access$000(this.this$0);
      }
      catch (ActivationException localActivationException)
      {
        return;
      }
      RegistryImpl.checkAccess("ActivationSystem.activeObject");
      Activation.access$100(this.this$0, paramActivationID).activeObject(paramActivationID, paramMarshalledObject);
    }

    public void inactiveGroup(, long paramLong)
      throws UnknownGroupException, RemoteException
    {
      try
      {
        Activation.access$000(this.this$0);
      }
      catch (ActivationException localActivationException)
      {
        return;
      }
      RegistryImpl.checkAccess("ActivationMonitor.inactiveGroup");
      Activation.access$200(this.this$0, paramActivationGroupID).inactiveGroup(paramLong, false);
    }
  }

  private static class ActivationServerSocketFactory
  implements RMIServerSocketFactory
  {
    private final ServerSocket serverSocket;

    ActivationServerSocketFactory(ServerSocket paramServerSocket)
    {
      this.serverSocket = paramServerSocket;
    }

    public ServerSocket createServerSocket(int paramInt)
      throws IOException
    {
      return new Activation.DelayedAcceptServerSocket(this.serverSocket);
    }
  }

  class ActivationSystemImpl extends RemoteServer
  implements ActivationSystem
  {
    private static final long serialVersionUID = 9100152600327688967L;

    ActivationSystemImpl(, int paramInt, RMIServerSocketFactory paramRMIServerSocketFactory)
      throws RemoteException
    {
      LiveRef localLiveRef = new LiveRef(new ObjID(4), paramInt, null, paramRMIServerSocketFactory);
      UnicastServerRef localUnicastServerRef = new UnicastServerRef(localLiveRef);
      this.ref = localUnicastServerRef;
      localUnicastServerRef.exportObject(this, null);
    }

    public ActivationID registerObject()
      throws ActivationException, UnknownGroupException, RemoteException
    {
      Activation.access$000(this.this$0);
      RegistryImpl.checkAccess("ActivationSystem.registerObject");
      ActivationGroupID localActivationGroupID = paramActivationDesc.getGroupID();
      ActivationID localActivationID = new ActivationID(Activation.access$300(this.this$0));
      Activation.access$200(this.this$0, localActivationGroupID).registerObject(localActivationID, paramActivationDesc, true);
      return localActivationID;
    }

    public void unregisterObject()
      throws ActivationException, UnknownObjectException, RemoteException
    {
      Activation.access$000(this.this$0);
      RegistryImpl.checkAccess("ActivationSystem.unregisterObject");
      Activation.access$100(this.this$0, paramActivationID).unregisterObject(paramActivationID, true);
    }

    public ActivationGroupID registerGroup()
      throws ActivationException, RemoteException
    {
      Activation.access$000(this.this$0);
      RegistryImpl.checkAccess("ActivationSystem.registerGroup");
      Activation.access$400(this.this$0, paramActivationGroupDesc, null);
      ActivationGroupID localActivationGroupID = new ActivationGroupID(Activation.access$500(this.this$0));
      Activation.GroupEntry localGroupEntry = new Activation.GroupEntry(this.this$0, localActivationGroupID, paramActivationGroupDesc);
      synchronized (Activation.access$600(this.this$0))
      {
        Activation.access$600(this.this$0).put(localActivationGroupID, localGroupEntry);
      }
      Activation.access$700(this.this$0, new Activation.LogRegisterGroup(localActivationGroupID, paramActivationGroupDesc));
      return localActivationGroupID;
    }

    public ActivationMonitor activeGroup(, ActivationInstantiator paramActivationInstantiator, long paramLong)
      throws ActivationException, UnknownGroupException, RemoteException
    {
      Activation.access$000(this.this$0);
      RegistryImpl.checkAccess("ActivationSystem.activeGroup");
      Activation.access$200(this.this$0, paramActivationGroupID).activeGroup(paramActivationInstantiator, paramLong);
      return Activation.access$800(this.this$0);
    }

    public void unregisterGroup()
      throws ActivationException, UnknownGroupException, RemoteException
    {
      Activation.access$000(this.this$0);
      RegistryImpl.checkAccess("ActivationSystem.unregisterGroup");
      synchronized (Activation.access$600(this.this$0))
      {
        Activation.GroupEntry localGroupEntry = Activation.access$200(this.this$0, paramActivationGroupID);
        Activation.access$600(this.this$0).remove(paramActivationGroupID);
        localGroupEntry.unregisterGroup(true);
      }
    }

    public ActivationDesc setActivationDesc(, ActivationDesc paramActivationDesc)
      throws ActivationException, UnknownObjectException, RemoteException
    {
      Activation.access$000(this.this$0);
      RegistryImpl.checkAccess("ActivationSystem.setActivationDesc");
      if (!(Activation.access$900(this.this$0, paramActivationID).equals(paramActivationDesc.getGroupID())))
        throw new ActivationException("ActivationDesc contains wrong group");
      return Activation.access$100(this.this$0, paramActivationID).setActivationDesc(paramActivationID, paramActivationDesc, true);
    }

    public java.rmi.activation.ActivationGroupDesc setActivationGroupDesc(, java.rmi.activation.ActivationGroupDesc paramActivationGroupDesc)
      throws ActivationException, UnknownGroupException, RemoteException
    {
      Activation.access$000(this.this$0);
      RegistryImpl.checkAccess("ActivationSystem.setActivationGroupDesc");
      Activation.access$400(this.this$0, paramActivationGroupDesc, null);
      return Activation.access$200(this.this$0, paramActivationGroupID).setActivationGroupDesc(paramActivationGroupID, paramActivationGroupDesc, true);
    }

    public ActivationDesc getActivationDesc()
      throws ActivationException, UnknownObjectException, RemoteException
    {
      Activation.access$000(this.this$0);
      RegistryImpl.checkAccess("ActivationSystem.getActivationDesc");
      return Activation.access$100(this.this$0, paramActivationID).getActivationDesc(paramActivationID);
    }

    public java.rmi.activation.ActivationGroupDesc getActivationGroupDesc()
      throws ActivationException, UnknownGroupException, RemoteException
    {
      Activation.access$000(this.this$0);
      RegistryImpl.checkAccess("ActivationSystem.getActivationGroupDesc");
      return Activation.access$200(this.this$0, paramActivationGroupID).desc;
    }

    public void shutdown()
      throws AccessException
    {
      RegistryImpl.checkAccess("ActivationSystem.shutdown");
      Object localObject1 = Activation.access$1000(this.this$0);
      if (localObject1 != null)
        synchronized (localObject1)
        {
        }
      synchronized (this.this$0)
      {
        if (!(Activation.access$1100(this.this$0)))
        {
          Activation.access$1102(this.this$0, true);
          new Activation.Shutdown(this.this$0).start();
        }
      }
    }
  }

  class ActivatorImpl extends RemoteServer
  implements Activator
  {
    private static final long serialVersionUID = -3654244726254566136L;

    ActivatorImpl(, int paramInt, RMIServerSocketFactory paramRMIServerSocketFactory)
      throws RemoteException
    {
      LiveRef localLiveRef = new LiveRef(new ObjID(1), paramInt, null, paramRMIServerSocketFactory);
      UnicastServerRef localUnicastServerRef = new UnicastServerRef(localLiveRef);
      this.ref = localUnicastServerRef;
      localUnicastServerRef.exportObject(this, null, false);
    }

    public java.rmi.MarshalledObject<? extends Remote> activate(, boolean paramBoolean)
      throws ActivationException, UnknownObjectException, RemoteException
    {
      Activation.access$000(this.this$0);
      return Activation.access$100(this.this$0, paramActivationID).activate(paramActivationID, paramBoolean);
    }
  }

  public static class DefaultExecPolicy
  {
    public void checkExecCommand(java.rmi.activation.ActivationGroupDesc paramActivationGroupDesc, String[] paramArrayOfString)
      throws SecurityException
    {
      String str1;
      Object localObject3;
      PermissionCollection localPermissionCollection = getExecPermissions();
      Properties localProperties = paramActivationGroupDesc.getPropertyOverrides();
      if (localProperties != null)
      {
        localObject1 = localProperties.propertyNames();
        while (((Enumeration)localObject1).hasMoreElements())
        {
          localObject2 = (String)((Enumeration)localObject1).nextElement();
          str1 = localProperties.getProperty((String)localObject2);
          localObject3 = "-D" + ((String)localObject2) + "=" + str1;
          try
          {
            checkPermission(localPermissionCollection, new ExecOptionPermission((String)localObject3));
          }
          catch (AccessControlException localAccessControlException)
          {
            if (str1.equals(""))
              checkPermission(localPermissionCollection, new ExecOptionPermission("-D" + ((String)localObject2)));
            else
              throw localAccessControlException;
          }
        }
      }
      Object localObject1 = paramActivationGroupDesc.getClassName();
      if (((localObject1 != null) && (!(((String)localObject1).equals(ActivationGroupImpl.class.getName())))) || (paramActivationGroupDesc.getLocation() != null) || (paramActivationGroupDesc.getData() != null))
        throw new AccessControlException("access denied (custom group implementation not allowed)");
      Object localObject2 = paramActivationGroupDesc.getCommandEnvironment();
      if (localObject2 != null)
      {
        str1 = ((ActivationGroupDesc.CommandEnvironment)localObject2).getCommandPath();
        if (str1 != null)
          checkPermission(localPermissionCollection, new ExecPermission(str1));
        localObject3 = ((ActivationGroupDesc.CommandEnvironment)localObject2).getCommandOptions();
        if (localObject3 != null)
        {
          Object localObject4 = localObject3;
          int i = localObject4.length;
          for (int j = 0; j < i; ++j)
          {
            String str2 = localObject4[j];
            checkPermission(localPermissionCollection, new ExecOptionPermission(str2));
          }
        }
      }
    }

    static void checkConfiguration()
    {
      Policy localPolicy = (Policy)AccessController.doPrivileged(new PrivilegedAction()
      {
        public Policy run()
        {
          return Policy.getPolicy();
        }
      });
      if (!(localPolicy instanceof PolicyFile))
        return;
      PermissionCollection localPermissionCollection = getExecPermissions();
      Enumeration localEnumeration = localPermissionCollection.elements();
      while (localEnumeration.hasMoreElements())
      {
        Permission localPermission = (Permission)localEnumeration.nextElement();
        if ((localPermission instanceof AllPermission) || (localPermission instanceof ExecPermission) || (localPermission instanceof ExecOptionPermission))
          return;
      }
      System.err.println(Activation.access$1700("rmid.exec.perms.inadequate"));
    }

    private static PermissionCollection getExecPermissions()
    {
      PermissionCollection localPermissionCollection = (PermissionCollection)AccessController.doPrivileged(new PrivilegedAction()
      {
        public PermissionCollection run()
        {
          CodeSource localCodeSource = new CodeSource(null, (Certificate[])null);
          Policy localPolicy = Policy.getPolicy();
          if (localPolicy != null)
            return localPolicy.getPermissions(localCodeSource);
          return new Permissions();
        }
      });
      return localPermissionCollection;
    }

    private static void checkPermission(PermissionCollection paramPermissionCollection, Permission paramPermission)
      throws AccessControlException
    {
      if (!(paramPermissionCollection.implies(paramPermission)))
        throw new AccessControlException("access denied " + paramPermission.toString());
    }
  }

  private static class DelayedAcceptServerSocket extends ServerSocket
  {
    private final ServerSocket serverSocket;

    DelayedAcceptServerSocket(ServerSocket paramServerSocket)
      throws IOException
    {
      this.serverSocket = paramServerSocket;
    }

    public void bind(SocketAddress paramSocketAddress)
      throws IOException
    {
      this.serverSocket.bind(paramSocketAddress);
    }

    public void bind(SocketAddress paramSocketAddress, int paramInt)
      throws IOException
    {
      this.serverSocket.bind(paramSocketAddress, paramInt);
    }

    public InetAddress getInetAddress()
    {
      return this.serverSocket.getInetAddress();
    }

    public int getLocalPort()
    {
      return this.serverSocket.getLocalPort();
    }

    public SocketAddress getLocalSocketAddress()
    {
      return this.serverSocket.getLocalSocketAddress();
    }

    public Socket accept()
      throws IOException
    {
      synchronized (Activation.access$3000())
      {
        try
        {
          while (!(Activation.access$3100()))
            Activation.access$3000().wait();
        }
        catch (InterruptedException localInterruptedException)
        {
          throw new AssertionError(localInterruptedException);
        }
      }
      return this.serverSocket.accept();
    }

    public void close()
      throws IOException
    {
      this.serverSocket.close();
    }

    public ServerSocketChannel getChannel()
    {
      return this.serverSocket.getChannel();
    }

    public boolean isBound()
    {
      return this.serverSocket.isBound();
    }

    public boolean isClosed()
    {
      return this.serverSocket.isClosed();
    }

    public void setSoTimeout(int paramInt)
      throws SocketException
    {
      this.serverSocket.setSoTimeout(paramInt);
    }

    public int getSoTimeout()
      throws IOException
    {
      return this.serverSocket.getSoTimeout();
    }

    public void setReuseAddress(boolean paramBoolean)
      throws SocketException
    {
      this.serverSocket.setReuseAddress(paramBoolean);
    }

    public boolean getReuseAddress()
      throws SocketException
    {
      return this.serverSocket.getReuseAddress();
    }

    public String toString()
    {
      return this.serverSocket.toString();
    }

    public void setReceiveBufferSize(int paramInt)
      throws SocketException
    {
      this.serverSocket.setReceiveBufferSize(paramInt);
    }

    public int getReceiveBufferSize()
      throws SocketException
    {
      return this.serverSocket.getReceiveBufferSize();
    }
  }

  private class GroupEntry
  implements Serializable
  {
    private static final long serialVersionUID = 7222464070032993304L;
    private static final int MAX_TRIES = 2;
    private static final int NORMAL = 0;
    private static final int CREATING = 1;
    private static final int TERMINATE = 2;
    private static final int TERMINATING = 3;
    java.rmi.activation.ActivationGroupDesc desc = null;
    ActivationGroupID groupID = null;
    long incarnation = 3412046294821109760L;
    Map<ActivationID, Activation.ObjectEntry> objects = new HashMap();
    Set<ActivationID> restartSet = new HashSet();
    transient ActivationInstantiator group = null;
    transient int status = 0;
    transient long waitTime = 3412046294821109760L;
    transient String groupName = null;
    transient Process child = null;
    transient boolean removed = false;
    transient Watchdog watchdog = null;

    GroupEntry(, ActivationGroupID paramActivationGroupID, java.rmi.activation.ActivationGroupDesc paramActivationGroupDesc)
    {
      this.groupID = paramActivationGroupID;
      this.desc = paramActivationGroupDesc;
    }

    void restartServices()
    {
      Iterator localIterator = null;
      synchronized (this)
      {
        if (!(this.restartSet.isEmpty()))
          break label21;
        return;
        label21: localIterator = new HashSet(this.restartSet).iterator();
      }
      while (localIterator.hasNext())
      {
        ??? = (ActivationID)localIterator.next();
        try
        {
          activate((ActivationID)???, true);
        }
        catch (Exception localException)
        {
          if (Activation.access$1100(this.this$0))
            return;
          System.err.println(Activation.access$1700("rmid.restart.service.warning"));
          localException.printStackTrace();
        }
      }
    }

    synchronized void activeGroup(, long paramLong)
      throws ActivationException, UnknownGroupException
    {
      if (this.incarnation != paramLong)
        throw new ActivationException("invalid incarnation");
      if (this.group != null)
      {
        if (this.group.equals(paramActivationInstantiator))
          return;
        throw new ActivationException("group already active");
      }
      if ((this.child != null) && (this.status != 1))
        throw new ActivationException("group not being created");
      this.group = paramActivationInstantiator;
      this.status = 0;
      super.notifyAll();
    }

    private void checkRemoved()
      throws UnknownGroupException
    {
      if (this.removed)
        throw new UnknownGroupException("group removed");
    }

    private Activation.ObjectEntry getObjectEntry()
      throws UnknownObjectException
    {
      if (this.removed)
        throw new UnknownObjectException("object's group removed");
      Activation.ObjectEntry localObjectEntry = (Activation.ObjectEntry)this.objects.get(paramActivationID);
      if (localObjectEntry == null)
        throw new UnknownObjectException("object unknown");
      return localObjectEntry;
    }

    synchronized void registerObject(, ActivationDesc paramActivationDesc, boolean paramBoolean)
      throws UnknownGroupException, ActivationException
    {
      checkRemoved();
      this.objects.put(paramActivationID, new Activation.ObjectEntry(paramActivationDesc));
      if (paramActivationDesc.getRestartMode() == true)
        this.restartSet.add(paramActivationID);
      synchronized (Activation.access$1800(this.this$0))
      {
        Activation.access$1800(this.this$0).put(paramActivationID, this.groupID);
      }
      if (paramBoolean)
        Activation.access$700(this.this$0, new Activation.LogRegisterObject(paramActivationID, paramActivationDesc));
    }

    synchronized void unregisterObject(, boolean paramBoolean)
      throws UnknownGroupException, ActivationException
    {
      Activation.ObjectEntry localObjectEntry = getObjectEntry(paramActivationID);
      localObjectEntry.removed = true;
      this.objects.remove(paramActivationID);
      if (localObjectEntry.desc.getRestartMode() == true)
        this.restartSet.remove(paramActivationID);
      synchronized (Activation.access$1800(this.this$0))
      {
        Activation.access$1800(this.this$0).remove(paramActivationID);
      }
      if (paramBoolean)
        Activation.access$700(this.this$0, new Activation.LogUnregisterObject(paramActivationID));
    }

    synchronized void unregisterGroup()
      throws UnknownGroupException, ActivationException
    {
      checkRemoved();
      this.removed = true;
      Iterator localIterator = this.objects.entrySet().iterator();
      while (localIterator.hasNext())
      {
        Map.Entry localEntry = (Map.Entry)localIterator.next();
        ActivationID localActivationID = (ActivationID)localEntry.getKey();
        synchronized (Activation.access$1800(this.this$0))
        {
          Activation.access$1800(this.this$0).remove(localActivationID);
        }
        ??? = (Activation.ObjectEntry)localEntry.getValue();
        ((Activation.ObjectEntry)???).removed = true;
      }
      this.objects.clear();
      this.restartSet.clear();
      reset();
      childGone();
      if (paramBoolean)
        Activation.access$700(this.this$0, new Activation.LogUnregisterGroup(this.groupID));
    }

    synchronized ActivationDesc setActivationDesc(, ActivationDesc paramActivationDesc, boolean paramBoolean)
      throws UnknownObjectException, UnknownGroupException, ActivationException
    {
      Activation.ObjectEntry localObjectEntry = getObjectEntry(paramActivationID);
      ActivationDesc localActivationDesc = localObjectEntry.desc;
      localObjectEntry.desc = paramActivationDesc;
      if (paramActivationDesc.getRestartMode() == true)
        this.restartSet.add(paramActivationID);
      else
        this.restartSet.remove(paramActivationID);
      if (paramBoolean)
        Activation.access$700(this.this$0, new Activation.LogUpdateDesc(paramActivationID, paramActivationDesc));
      return localActivationDesc;
    }

    synchronized ActivationDesc getActivationDesc()
      throws UnknownObjectException, UnknownGroupException
    {
      return getObjectEntry(paramActivationID).desc;
    }

    synchronized java.rmi.activation.ActivationGroupDesc setActivationGroupDesc(, java.rmi.activation.ActivationGroupDesc paramActivationGroupDesc, boolean paramBoolean)
      throws UnknownGroupException, ActivationException
    {
      checkRemoved();
      java.rmi.activation.ActivationGroupDesc localActivationGroupDesc = this.desc;
      this.desc = paramActivationGroupDesc;
      if (paramBoolean)
        Activation.access$700(this.this$0, new Activation.LogUpdateGroupDesc(paramActivationGroupID, paramActivationGroupDesc));
      return localActivationGroupDesc;
    }

    synchronized void inactiveGroup(, boolean paramBoolean)
      throws UnknownGroupException
    {
      checkRemoved();
      if (this.incarnation != paramLong)
        throw new UnknownGroupException("invalid incarnation");
      reset();
      if (paramBoolean)
      {
        terminate();
      }
      else if ((this.child != null) && (this.status == 0))
      {
        this.status = 2;
        this.watchdog.noRestart();
      }
    }

    synchronized void activeObject(, java.rmi.MarshalledObject<? extends Remote> paramMarshalledObject)
      throws UnknownObjectException
    {
      getObjectEntry(paramActivationID).stub = paramMarshalledObject;
    }

    synchronized void inactiveObject()
      throws UnknownObjectException
    {
      getObjectEntry(paramActivationID).reset();
    }

    private synchronized void reset()
    {
      this.group = null;
      Iterator localIterator = this.objects.values().iterator();
      while (localIterator.hasNext())
      {
        Activation.ObjectEntry localObjectEntry = (Activation.ObjectEntry)localIterator.next();
        localObjectEntry.reset();
      }
    }

    private void childGone()
    {
      if (this.child != null)
      {
        this.child = null;
        this.watchdog.dispose();
        this.watchdog = null;
        this.status = 0;
        super.notifyAll();
      }
    }

    private void terminate()
    {
      if ((this.child != null) && (this.status != 3))
      {
        this.child.destroy();
        this.status = 3;
        this.waitTime = (System.currentTimeMillis() + Activation.access$1900());
        super.notifyAll();
      }
    }

    private void await()
    {
      while (true)
        switch (this.status)
        {
        case 0:
          return;
        case 2:
          terminate();
        case 3:
          try
          {
            this.child.exitValue();
          }
          catch (IllegalThreadStateException localIllegalThreadStateException)
          {
            while (true)
            {
              long l = System.currentTimeMillis();
              if (this.waitTime <= l)
                break;
              try
              {
                super.wait(this.waitTime - l);
              }
              catch (InterruptedException localInterruptedException2)
              {
              }
            }
          }
          childGone();
          return;
        case 1:
          try
          {
            super.wait();
          }
          catch (InterruptedException localInterruptedException1)
          {
          }
        }
    }

    void shutdownFast()
    {
      Process localProcess = this.child;
      if (localProcess != null)
        localProcess.destroy();
    }

    synchronized void shutdown()
    {
      reset();
      terminate();
      await();
    }

    java.rmi.MarshalledObject<? extends Remote> activate(, boolean paramBoolean)
      throws ActivationException
    {
      Object localObject1 = null;
      for (int i = 2; i > 0; --i)
      {
        label43: ActivationInstantiator localActivationInstantiator;
        long l;
        Activation.ObjectEntry localObjectEntry;
        synchronized (this)
        {
          localObjectEntry = getObjectEntry(paramActivationID);
          if ((paramBoolean) || (localObjectEntry.stub == null))
            break label43;
          return localObjectEntry.stub;
          localActivationInstantiator = getInstantiator(this.groupID);
          l = this.incarnation;
        }
        int j = 0;
        boolean bool = false;
        try
        {
          return localObjectEntry.activate(paramActivationID, paramBoolean, localActivationInstantiator);
        }
        catch (NoSuchObjectException localNoSuchObjectException)
        {
          j = 1;
          localObject1 = localNoSuchObjectException;
        }
        catch (ConnectException localConnectException)
        {
          j = 1;
          bool = true;
          localObject1 = localConnectException;
        }
        catch (ConnectIOException localConnectIOException)
        {
          j = 1;
          bool = true;
          localObject1 = localConnectIOException;
        }
        catch (InactiveGroupException localInactiveGroupException)
        {
          j = 1;
          localObject1 = localInactiveGroupException;
        }
        catch (RemoteException localRemoteException)
        {
          if (localObject1 == null)
            localObject1 = localRemoteException;
        }
        if (j != 0)
          try
          {
            System.err.println(MessageFormat.format(Activation.access$1700("rmid.group.inactive"), new Object[] { localObject1.toString() }));
            localObject1.printStackTrace();
            Activation.access$200(this.this$0, this.groupID).inactiveGroup(l, bool);
          }
          catch (UnknownGroupException localUnknownGroupException)
          {
          }
      }
      throw new ActivationException("object activation failed after 2 tries", localObject1);
    }

    private ActivationInstantiator getInstantiator()
      throws ActivationException
    {
      if ((!($assertionsDisabled)) && (!(Thread.holdsLock(this))))
        throw new AssertionError();
      await();
      if (this.group != null)
        return this.group;
      checkRemoved();
      int i = 0;
      try
      {
        Object localObject1;
        this.groupName = Activation.access$2000(this.this$0);
        i = 1;
        String[] arrayOfString = Activation.access$2100(this.this$0, this.desc);
        Activation.access$400(this.this$0, this.desc, arrayOfString);
        if (Activation.access$2200())
        {
          localObject1 = new StringBuffer(arrayOfString[0]);
          for (int j = 1; j < arrayOfString.length; ++j)
          {
            ((StringBuffer)localObject1).append(' ');
            ((StringBuffer)localObject1).append(arrayOfString[j]);
          }
          System.err.println(MessageFormat.format(Activation.access$1700("rmid.exec.command"), new Object[] { ((StringBuffer)localObject1).toString() }));
        }
        try
        {
          this.child = Runtime.getRuntime().exec(arrayOfString);
          this.status = 1;
          this.incarnation += 3412040024168857601L;
          this.watchdog = new Watchdog(this);
          this.watchdog.start();
          Activation.access$700(this.this$0, new Activation.LogGroupIncarnation(paramActivationGroupID, this.incarnation));
          PipeWriter.plugTogetherPair(this.child.getInputStream(), System.out, this.child.getErrorStream(), System.err);
          localObject1 = new MarshalOutputStream(this.child.getOutputStream());
          ((MarshalOutputStream)localObject1).writeObject(paramActivationGroupID);
          ((MarshalOutputStream)localObject1).writeObject(this.desc);
          ((MarshalOutputStream)localObject1).writeLong(this.incarnation);
          ((MarshalOutputStream)localObject1).flush();
          ((MarshalOutputStream)localObject1).close();
        }
        catch (IOException localIOException)
        {
          terminate();
          throw new ActivationException("unable to create activation group", localIOException);
        }
        try
        {
          long l1 = System.currentTimeMillis();
          long l2 = l1 + Activation.access$2300();
          do
          {
            super.wait(l2 - l1);
            if (this.group != null)
            {
              ActivationInstantiator localActivationInstantiator = this.group;
              if (i != 0)
                Activation.access$2400(this.this$0);
              return localActivationInstantiator;
            }
            l1 = System.currentTimeMillis();
            if (this.status != 1)
              break;
          }
          while (l1 < l2);
        }
        catch (InterruptedException localInterruptedException)
        {
        }
        terminate();
        if (this.removed);
        throw new ActivationException("timeout creating child process");
      }
      finally
      {
        if (i != 0)
          Activation.access$2400(this.this$0);
      }
    }

    private class Watchdog extends Thread
    {
      private final Process groupProcess = this.this$1.child;
      private final long groupIncarnation = this.this$1.incarnation;
      private boolean canInterrupt = true;
      private boolean shouldQuit = false;
      private boolean shouldRestart = true;

      Watchdog()
      {
        super("WatchDog-" + paramGroupEntry.groupName + "-" + paramGroupEntry.incarnation);
        setDaemon(true);
      }

      public void run()
      {
        if (this.shouldQuit)
          return;
        try
        {
          this.groupProcess.waitFor();
        }
        catch (InterruptedException localInterruptedException)
        {
          return;
        }
        int i = 0;
        synchronized (this.this$1)
        {
          if (!(this.shouldQuit))
            break label40;
          return;
          label40: this.canInterrupt = false;
          interrupted();
          if (this.groupIncarnation != this.this$1.incarnation)
            break label104;
          i = ((this.shouldRestart) && (!(Activation.access$1100(this.this$1.this$0)))) ? 1 : 0;
          Activation.GroupEntry.access$2500(this.this$1);
          label104: Activation.GroupEntry.access$2600(this.this$1);
        }
        if (i != 0)
          this.this$1.restartServices();
      }

      void dispose()
      {
        this.shouldQuit = true;
        if (this.canInterrupt)
          interrupt();
      }

      void noRestart()
      {
        this.shouldRestart = false;
      }
    }
  }

  private static class LogGroupIncarnation extends Activation.LogRecord
  {
    private static final long serialVersionUID = 4146872747377631897L;
    private ActivationGroupID id;
    private long inc;

    LogGroupIncarnation(ActivationGroupID paramActivationGroupID, long paramLong)
    {
      super(null);
      this.id = paramActivationGroupID;
      this.inc = paramLong;
    }

    Object apply(Object paramObject)
    {
      Activation.GroupEntry localGroupEntry;
      try
      {
        localGroupEntry = Activation.access$200((Activation)paramObject, this.id);
        localGroupEntry.incarnation = this.inc;
      }
      catch (Exception localException)
      {
        System.err.println(MessageFormat.format(Activation.access$1700("rmid.log.recover.warning"), new Object[] { "LogGroupIncarnation" }));
        localException.printStackTrace();
      }
      return paramObject;
    }
  }

  private static abstract class LogRecord
  implements Serializable
  {
    private static final long serialVersionUID = 8395140512322687529L;

    abstract Object apply(Object paramObject)
      throws Exception;
  }

  private static class LogRegisterGroup extends Activation.LogRecord
  {
    private static final long serialVersionUID = -1966827458515403625L;
    private ActivationGroupID id;
    private java.rmi.activation.ActivationGroupDesc desc;

    LogRegisterGroup(ActivationGroupID paramActivationGroupID, java.rmi.activation.ActivationGroupDesc paramActivationGroupDesc)
    {
      super(null);
      this.id = paramActivationGroupID;
      this.desc = paramActivationGroupDesc;
    }

    Object apply(Object paramObject)
    {
      Activation tmp19_16 = ((Activation)paramObject);
      tmp19_16.getClass();
      Activation.access$600((Activation)paramObject).put(this.id, new Activation.GroupEntry(tmp19_16, this.id, this.desc));
      return paramObject;
    }
  }

  private static class LogRegisterObject extends Activation.LogRecord
  {
    private static final long serialVersionUID = -6280336276146085143L;
    private ActivationID id;
    private ActivationDesc desc;

    LogRegisterObject(ActivationID paramActivationID, ActivationDesc paramActivationDesc)
    {
      super(null);
      this.id = paramActivationID;
      this.desc = paramActivationDesc;
    }

    Object apply(Object paramObject)
    {
      try
      {
        Activation.access$200((Activation)paramObject, this.desc.getGroupID()).registerObject(this.id, this.desc, false);
      }
      catch (Exception localException)
      {
        System.err.println(MessageFormat.format(Activation.access$1700("rmid.log.recover.warning"), new Object[] { "LogRegisterObject" }));
        localException.printStackTrace();
      }
      return paramObject;
    }
  }

  private static class LogUnregisterGroup extends Activation.LogRecord
  {
    private static final long serialVersionUID = -3356306586522147344L;
    private ActivationGroupID id;

    LogUnregisterGroup(ActivationGroupID paramActivationGroupID)
    {
      super(null);
      this.id = paramActivationGroupID;
    }

    Object apply(Object paramObject)
    {
      Activation.GroupEntry localGroupEntry = (Activation.GroupEntry)Activation.access$600((Activation)paramObject).remove(this.id);
      try
      {
        localGroupEntry.unregisterGroup(false);
      }
      catch (Exception localException)
      {
        System.err.println(MessageFormat.format(Activation.access$1700("rmid.log.recover.warning"), new Object[] { "LogUnregisterGroup" }));
        localException.printStackTrace();
      }
      return paramObject;
    }
  }

  private static class LogUnregisterObject extends Activation.LogRecord
  {
    private static final long serialVersionUID = 6269824097396935501L;
    private ActivationID id;

    LogUnregisterObject(ActivationID paramActivationID)
    {
      super(null);
      this.id = paramActivationID;
    }

    Object apply(Object paramObject)
    {
      try
      {
        Activation.access$100((Activation)paramObject, this.id).unregisterObject(this.id, false);
      }
      catch (Exception localException)
      {
        System.err.println(MessageFormat.format(Activation.access$1700("rmid.log.recover.warning"), new Object[] { "LogUnregisterObject" }));
        localException.printStackTrace();
      }
      return paramObject;
    }
  }

  private static class LogUpdateDesc extends Activation.LogRecord
  {
    private static final long serialVersionUID = 545511539051179885L;
    private ActivationID id;
    private ActivationDesc desc;

    LogUpdateDesc(ActivationID paramActivationID, ActivationDesc paramActivationDesc)
    {
      super(null);
      this.id = paramActivationID;
      this.desc = paramActivationDesc;
    }

    Object apply(Object paramObject)
    {
      try
      {
        Activation.access$100((Activation)paramObject, this.id).setActivationDesc(this.id, this.desc, false);
      }
      catch (Exception localException)
      {
        System.err.println(MessageFormat.format(Activation.access$1700("rmid.log.recover.warning"), new Object[] { "LogUpdateDesc" }));
        localException.printStackTrace();
      }
      return paramObject;
    }
  }

  private static class LogUpdateGroupDesc extends Activation.LogRecord
  {
    private static final long serialVersionUID = -1271300989218424337L;
    private ActivationGroupID id;
    private java.rmi.activation.ActivationGroupDesc desc;

    LogUpdateGroupDesc(ActivationGroupID paramActivationGroupID, java.rmi.activation.ActivationGroupDesc paramActivationGroupDesc)
    {
      super(null);
      this.id = paramActivationGroupID;
      this.desc = paramActivationGroupDesc;
    }

    Object apply(Object paramObject)
    {
      try
      {
        Activation.access$200((Activation)paramObject, this.id).setActivationGroupDesc(this.id, this.desc, false);
      }
      catch (Exception localException)
      {
        System.err.println(MessageFormat.format(Activation.access$1700("rmid.log.recover.warning"), new Object[] { "LogUpdateGroupDesc" }));
        localException.printStackTrace();
      }
      return paramObject;
    }
  }

  private static class ObjectEntry
  implements Serializable
  {
    private static final long serialVersionUID = -5500114225321357856L;
    ActivationDesc desc;
    volatile transient java.rmi.MarshalledObject<? extends Remote> stub = null;
    volatile transient boolean removed = false;

    ObjectEntry(ActivationDesc paramActivationDesc)
    {
      this.desc = paramActivationDesc;
    }

    synchronized java.rmi.MarshalledObject<? extends Remote> activate(ActivationID paramActivationID, boolean paramBoolean, ActivationInstantiator paramActivationInstantiator)
      throws RemoteException, ActivationException
    {
      java.rmi.MarshalledObject localMarshalledObject = this.stub;
      if (this.removed)
        throw new UnknownObjectException("object removed");
      if ((!(paramBoolean)) && (localMarshalledObject != null))
        return localMarshalledObject;
      localMarshalledObject = paramActivationInstantiator.newInstance(paramActivationID, this.desc);
      this.stub = localMarshalledObject;
      return localMarshalledObject;
    }

    void reset()
    {
      this.stub = null;
    }
  }

  private class Shutdown extends Thread
  {
    Shutdown()
    {
      super("rmid Shutdown");
    }

    public void run()
    {
      try
      {
        Activation.GroupEntry[] arrayOfGroupEntry;
        Activation.access$1300(Activation.access$1200(this.this$0));
        Activation.access$1300(Activation.access$1400(this.this$0));
        synchronized (Activation.access$600(this.this$0))
        {
          arrayOfGroupEntry = (Activation.GroupEntry[])Activation.access$600(this.this$0).values().toArray(new Activation.GroupEntry[Activation.access$600(this.this$0).size()]);
        }
        ??? = arrayOfGroupEntry;
        int i = ???.length;
        for (int j = 0; j < i; ++j)
        {
          Object localObject3 = ???[j];
          localObject3.shutdown();
        }
        Runtime.getRuntime().removeShutdownHook(Activation.access$1500(this.this$0));
        Activation.access$1300(Activation.access$800(this.this$0));
        try
        {
          synchronized (Activation.access$1600(this.this$0))
          {
            Activation.access$1600(this.this$0).close();
          }
        }
        catch (IOException localIOException)
        {
        }
      }
      finally
      {
        System.err.println(Activation.access$1700("rmid.daemon.shutdown"));
        System.exit(0);
      }
    }
  }

  private class ShutdownHook extends Thread
  {
    ShutdownHook()
    {
      super("rmid ShutdownHook");
    }

    public void run()
    {
      synchronized (this.this$0)
      {
        Activation.access$1102(this.this$0, true);
      }
      synchronized (Activation.access$600(this.this$0))
      {
        Iterator localIterator = Activation.access$600(this.this$0).values().iterator();
        while (localIterator.hasNext())
        {
          Activation.GroupEntry localGroupEntry = (Activation.GroupEntry)localIterator.next();
          localGroupEntry.shutdownFast();
        }
      }
    }
  }

  private static class SystemRegistryImpl extends RegistryImpl
  {
    private static final String NAME = ActivationSystem.class.getName();
    private final ActivationSystem systemStub;

    SystemRegistryImpl(int paramInt, RMIClientSocketFactory paramRMIClientSocketFactory, RMIServerSocketFactory paramRMIServerSocketFactory, ActivationSystem paramActivationSystem)
      throws RemoteException
    {
      super(paramInt, paramRMIClientSocketFactory, paramRMIServerSocketFactory);
      this.systemStub = paramActivationSystem;
    }

    public Remote lookup(String paramString)
      throws RemoteException, NotBoundException
    {
      if (paramString.equals(NAME))
        return this.systemStub;
      return super.lookup(paramString);
    }

    public String[] list()
      throws RemoteException
    {
      String[] arrayOfString1 = super.list();
      int i = arrayOfString1.length;
      String[] arrayOfString2 = new String[i + 1];
      if (i > 0)
        System.arraycopy(arrayOfString1, 0, arrayOfString2, 0, i);
      arrayOfString2[i] = NAME;
      return arrayOfString2;
    }

    public void bind(String paramString, Remote paramRemote)
      throws RemoteException, AlreadyBoundException, AccessException
    {
      if (paramString.equals(NAME))
        throw new AccessException("binding ActivationSystem is disallowed");
      super.bind(paramString, paramRemote);
    }

    public void unbind(String paramString)
      throws RemoteException, NotBoundException, AccessException
    {
      if (paramString.equals(NAME))
        throw new AccessException("unbinding ActivationSystem is disallowed");
      super.unbind(paramString);
    }

    public void rebind(String paramString, Remote paramRemote)
      throws RemoteException, AccessException
    {
      if (paramString.equals(NAME))
        throw new AccessException("binding ActivationSystem is disallowed");
      super.rebind(paramString, paramRemote);
    }
  }
}