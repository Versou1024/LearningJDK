package sun.management.snmp;

import com.sun.jmx.snmp.IPAcl.SnmpAcl;
import com.sun.jmx.snmp.InetAddressAcl;
import com.sun.jmx.snmp.daemon.CommunicationException;
import com.sun.jmx.snmp.daemon.SnmpAdaptorServer;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import sun.management.Agent;
import sun.management.AgentConfigurationError;
import sun.management.FileSystem;
import sun.management.snmp.jvminstr.JVM_MANAGEMENT_MIB_IMPL;
import sun.management.snmp.jvminstr.NotificationTargetImpl;
import sun.management.snmp.util.JvmContextFactory;
import sun.management.snmp.util.MibLogger;

public final class AdaptorBootstrap
{
  private static final MibLogger log = new MibLogger(AdaptorBootstrap.class);
  private SnmpAdaptorServer adaptor;
  private JVM_MANAGEMENT_MIB_IMPL jvmmib;

  private AdaptorBootstrap(SnmpAdaptorServer paramSnmpAdaptorServer, JVM_MANAGEMENT_MIB_IMPL paramJVM_MANAGEMENT_MIB_IMPL)
  {
    this.jvmmib = paramJVM_MANAGEMENT_MIB_IMPL;
    this.adaptor = paramSnmpAdaptorServer;
  }

  private static String getDefaultFileName(String paramString)
  {
    String str = File.separator;
    return System.getProperty("java.home") + str + "lib" + str + "management" + str + paramString;
  }

  private static List getTargetList(InetAddressAcl paramInetAddressAcl, int paramInt)
  {
    ArrayList localArrayList = new ArrayList();
    if (paramInetAddressAcl != null)
    {
      if (log.isDebugOn())
        log.debug("getTargetList", Agent.getText("jmxremote.AdaptorBootstrap.getTargetList.processing"));
      Enumeration localEnumeration1 = paramInetAddressAcl.getTrapDestinations();
      while (localEnumeration1.hasMoreElements())
      {
        InetAddress localInetAddress = (InetAddress)localEnumeration1.nextElement();
        Enumeration localEnumeration2 = paramInetAddressAcl.getTrapCommunities(localInetAddress);
        while (localEnumeration2.hasMoreElements())
        {
          String str = (String)localEnumeration2.nextElement();
          NotificationTargetImpl localNotificationTargetImpl = new NotificationTargetImpl(localInetAddress, paramInt, str);
          if (log.isDebugOn())
            log.debug("getTargetList", Agent.getText("jmxremote.AdaptorBootstrap.getTargetList.adding", new String[] { localNotificationTargetImpl.toString() }));
          localArrayList.add(localNotificationTargetImpl);
        }
      }
    }
    return localArrayList;
  }

  public static synchronized AdaptorBootstrap initialize()
  {
    Properties localProperties = Agent.loadManagementProperties();
    if (localProperties == null)
      return null;
    String str = localProperties.getProperty("com.sun.management.snmp.port");
    return initialize(str, localProperties);
  }

  public static synchronized AdaptorBootstrap initialize(String paramString, Properties paramProperties)
  {
    int i;
    int j;
    if (paramString.length() == 0)
      paramString = "161";
    try
    {
      i = Integer.parseInt(paramString);
    }
    catch (NumberFormatException localNumberFormatException1)
    {
      throw new AgentConfigurationError("agent.err.invalid.snmp.port", localNumberFormatException1, new String[] { paramString });
    }
    if (i < 0)
      throw new AgentConfigurationError("agent.err.invalid.snmp.port", new String[] { paramString });
    String str1 = paramProperties.getProperty("com.sun.management.snmp.trap", "162");
    try
    {
      j = Integer.parseInt(str1);
    }
    catch (NumberFormatException localNumberFormatException2)
    {
      throw new AgentConfigurationError("agent.err.invalid.snmp.trap.port", localNumberFormatException2, new String[] { str1 });
    }
    if (j < 0)
      throw new AgentConfigurationError("agent.err.invalid.snmp.trap.port", new String[] { str1 });
    String str2 = paramProperties.getProperty("com.sun.management.snmp.interface", "localhost");
    String str3 = getDefaultFileName("snmp.acl");
    String str4 = paramProperties.getProperty("com.sun.management.snmp.acl.file", str3);
    String str5 = paramProperties.getProperty("com.sun.management.snmp.acl", "true");
    boolean bool = Boolean.valueOf(str5).booleanValue();
    if (bool)
      checkAclFile(str4);
    AdaptorBootstrap localAdaptorBootstrap = null;
    try
    {
      localAdaptorBootstrap = getAdaptorBootstrap(i, j, str2, bool, str4);
    }
    catch (Exception localException)
    {
      throw new AgentConfigurationError("agent.err.exception", localException, new String[] { localException.getMessage() });
    }
    return localAdaptorBootstrap;
  }

  private static AdaptorBootstrap getAdaptorBootstrap(int paramInt1, int paramInt2, String paramString1, boolean paramBoolean, String paramString2)
  {
    InetAddress localInetAddress;
    InetAddressAcl localInetAddressAcl;
    try
    {
      localInetAddress = InetAddress.getByName(paramString1);
    }
    catch (UnknownHostException localUnknownHostException1)
    {
      throw new AgentConfigurationError("agent.err.unknown.snmp.interface", localUnknownHostException1, new String[] { paramString1 });
    }
    if (log.isDebugOn())
      log.debug("initialize", Agent.getText("jmxremote.AdaptorBootstrap.getTargetList.starting\n\tcom.sun.management.snmp.port=" + paramInt1 + "\n\t" + "com.sun.management.snmp.trap" + "=" + paramInt2 + "\n\t" + "com.sun.management.snmp.interface" + "=" + localInetAddress + ((paramBoolean) ? "\n\tcom.sun.management.snmp.acl.file=" + paramString2 : "\n\tNo ACL") + ""));
    try
    {
      localInetAddressAcl = (paramBoolean) ? new SnmpAcl(System.getProperty("user.name"), paramString2) : null;
    }
    catch (UnknownHostException localUnknownHostException2)
    {
      throw new AgentConfigurationError("agent.err.unknown.snmp.interface", localUnknownHostException2, new String[] { localUnknownHostException2.getMessage() });
    }
    SnmpAdaptorServer localSnmpAdaptorServer = new SnmpAdaptorServer(localInetAddressAcl, paramInt1, localInetAddress);
    localSnmpAdaptorServer.setUserDataFactory(new JvmContextFactory());
    localSnmpAdaptorServer.setTrapPort(paramInt2);
    JVM_MANAGEMENT_MIB_IMPL localJVM_MANAGEMENT_MIB_IMPL = new JVM_MANAGEMENT_MIB_IMPL();
    try
    {
      localJVM_MANAGEMENT_MIB_IMPL.init();
    }
    catch (IllegalAccessException localIllegalAccessException)
    {
      throw new AgentConfigurationError("agent.err.snmp.mib.init.failed", localIllegalAccessException, new String[] { localIllegalAccessException.getMessage() });
    }
    localJVM_MANAGEMENT_MIB_IMPL.addTargets(getTargetList(localInetAddressAcl, paramInt2));
    try
    {
      localSnmpAdaptorServer.start(9223372036854775807L);
    }
    catch (Exception localException)
    {
      Object localObject = localException;
      if (localException instanceof CommunicationException)
      {
        Throwable localThrowable = ((Throwable)localObject).getCause();
        if (localThrowable != null)
          localObject = localThrowable;
      }
      throw new AgentConfigurationError("agent.err.snmp.adaptor.start.failed", (Throwable)localObject, new String[] { localInetAddress + ":" + paramInt1, "(" + ((Throwable)localObject).getMessage() + ")" });
    }
    if (!(localSnmpAdaptorServer.isActive()))
      throw new AgentConfigurationError("agent.err.snmp.adaptor.start.failed", new String[] { localInetAddress + ":" + paramInt1 });
    try
    {
      localSnmpAdaptorServer.addMib(localJVM_MANAGEMENT_MIB_IMPL);
      localJVM_MANAGEMENT_MIB_IMPL.setSnmpAdaptor(localSnmpAdaptorServer);
    }
    catch (RuntimeException localRuntimeException)
    {
      new AdaptorBootstrap(localSnmpAdaptorServer, localJVM_MANAGEMENT_MIB_IMPL).terminate();
      throw localRuntimeException;
    }
    log.debug("initialize", Agent.getText("jmxremote.AdaptorBootstrap.getTargetList.initialize1"));
    log.config("initialize", Agent.getText("jmxremote.AdaptorBootstrap.getTargetList.initialize2", new String[] { localInetAddress.toString(), Integer.toString(localSnmpAdaptorServer.getPort()) }));
    return ((AdaptorBootstrap)new AdaptorBootstrap(localSnmpAdaptorServer, localJVM_MANAGEMENT_MIB_IMPL));
  }

  private static void checkAclFile(String paramString)
  {
    if ((paramString == null) || (paramString.length() == 0))
      throw new AgentConfigurationError("agent.err.acl.file.notset");
    File localFile = new File(paramString);
    if (!(localFile.exists()))
      throw new AgentConfigurationError("agent.err.acl.file.notfound", new String[] { paramString });
    if (!(localFile.canRead()))
      throw new AgentConfigurationError("agent.err.acl.file.not.readable", new String[] { paramString });
    FileSystem localFileSystem = FileSystem.open();
    try
    {
      if ((localFileSystem.supportsFileSecurity(localFile)) && (!(localFileSystem.isAccessUserOnly(localFile))))
        throw new AgentConfigurationError("agent.err.acl.file.access.notrestricted", new String[] { paramString });
    }
    catch (IOException localIOException)
    {
      throw new AgentConfigurationError("agent.err.acl.file.read.failed", new String[] { paramString });
    }
  }

  public synchronized int getPort()
  {
    if (this.adaptor != null)
      return this.adaptor.getPort();
    return 0;
  }

  public synchronized void terminate()
  {
    if (this.adaptor == null)
      return;
    try
    {
      this.jvmmib.terminate();
    }
    catch (Exception localException)
    {
      log.debug("jmxremote.AdaptorBootstrap.getTargetList.terminate", localException.toString());
    }
    finally
    {
      this.jvmmib = null;
    }
    try
    {
      this.adaptor.stop();
    }
    finally
    {
      this.adaptor = null;
    }
  }

  public static abstract interface DefaultValues
  {
    public static final String PORT = "161";
    public static final String CONFIG_FILE_NAME = "management.properties";
    public static final String TRAP_PORT = "162";
    public static final String USE_ACL = "true";
    public static final String ACL_FILE_NAME = "snmp.acl";
    public static final String BIND_ADDRESS = "localhost";
  }

  public static abstract interface PropertyNames
  {
    public static final String PORT = "com.sun.management.snmp.port";
    public static final String CONFIG_FILE_NAME = "com.sun.management.config.file";
    public static final String TRAP_PORT = "com.sun.management.snmp.trap";
    public static final String USE_ACL = "com.sun.management.snmp.acl";
    public static final String ACL_FILE_NAME = "com.sun.management.snmp.acl.file";
    public static final String BIND_ADDRESS = "com.sun.management.snmp.interface";
  }
}