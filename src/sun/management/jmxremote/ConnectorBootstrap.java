package sun.management.jmxremote;

import com.sun.jmx.remote.internal.RMIExporter;
import com.sun.jmx.remote.security.JMXPluggableAuthenticator;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.NoSuchObjectException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.RemoteObject;
import java.rmi.server.UnicastRemoteObject;
import java.security.KeyStore;
import java.security.Principal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import javax.management.MBeanServer;
import javax.management.remote.JMXAuthenticator;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.rmi.ssl.SslRMIServerSocketFactory;
import javax.security.auth.Subject;
import sun.management.Agent;
import sun.management.AgentConfigurationError;
import sun.management.ConnectorAddressLink;
import sun.management.FileSystem;
import sun.management.snmp.util.MibLogger;
import sun.rmi.server.UnicastRef;
import sun.rmi.server.UnicastServerRef;
import sun.rmi.server.UnicastServerRef2;
import sun.rmi.transport.LiveRef;

public final class ConnectorBootstrap
{
  private static final MibLogger log = new MibLogger(ConnectorBootstrap.class);

  public static synchronized JMXConnectorServer initialize()
  {
    Properties localProperties = Agent.loadManagementProperties();
    if (localProperties == null)
      return null;
    String str = localProperties.getProperty("com.sun.management.jmxremote.port");
    return initialize(str, localProperties);
  }

  public static synchronized JMXConnectorServer initialize(String paramString, Properties paramProperties)
  {
    int i;
    try
    {
      i = Integer.parseInt(paramString);
    }
    catch (NumberFormatException localNumberFormatException)
    {
      throw new AgentConfigurationError("agent.err.invalid.jmxremote.port", localNumberFormatException, new String[] { paramString });
    }
    if (i < 0)
      throw new AgentConfigurationError("agent.err.invalid.jmxremote.port", new String[] { paramString });
    String str1 = paramProperties.getProperty("com.sun.management.jmxremote.authenticate", "true");
    boolean bool1 = Boolean.valueOf(str1).booleanValue();
    String str2 = paramProperties.getProperty("com.sun.management.jmxremote.ssl", "true");
    boolean bool2 = Boolean.valueOf(str2).booleanValue();
    String str3 = paramProperties.getProperty("com.sun.management.jmxremote.registry.ssl", "false");
    boolean bool3 = Boolean.valueOf(str3).booleanValue();
    String str4 = paramProperties.getProperty("com.sun.management.jmxremote.ssl.enabled.cipher.suites");
    String[] arrayOfString1 = null;
    if (str4 != null)
    {
      localObject1 = new StringTokenizer(str4, ",");
      int j = ((StringTokenizer)localObject1).countTokens();
      arrayOfString1 = new String[j];
      for (int k = 0; k < j; ++k)
        arrayOfString1[k] = ((StringTokenizer)localObject1).nextToken();
    }
    Object localObject1 = paramProperties.getProperty("com.sun.management.jmxremote.ssl.enabled.protocols");
    String[] arrayOfString2 = null;
    if (localObject1 != null)
    {
      localObject2 = new StringTokenizer((String)localObject1, ",");
      int l = ((StringTokenizer)localObject2).countTokens();
      arrayOfString2 = new String[l];
      for (int i1 = 0; i1 < l; ++i1)
        arrayOfString2[i1] = ((StringTokenizer)localObject2).nextToken();
    }
    Object localObject2 = paramProperties.getProperty("com.sun.management.jmxremote.ssl.need.client.auth", "false");
    boolean bool4 = Boolean.valueOf((String)localObject2).booleanValue();
    String str5 = paramProperties.getProperty("com.sun.management.jmxremote.ssl.config.file");
    String str6 = null;
    String str7 = null;
    String str8 = null;
    if (bool1)
    {
      str6 = paramProperties.getProperty("com.sun.management.jmxremote.login.config");
      if (str6 == null)
      {
        str7 = paramProperties.getProperty("com.sun.management.jmxremote.password.file", getDefaultFileName("jmxremote.password"));
        checkPasswordFile(str7);
      }
      str8 = paramProperties.getProperty("com.sun.management.jmxremote.access.file", getDefaultFileName("jmxremote.access"));
      checkAccessFile(str8);
    }
    if (log.isDebugOn())
      log.debug("initialize", Agent.getText("jmxremote.ConnectorBootstrap.initialize") + "\n\t" + "com.sun.management.jmxremote.port" + "=" + i + "\n\t" + "com.sun.management.jmxremote.ssl" + "=" + bool2 + "\n\t" + "com.sun.management.jmxremote.registry.ssl" + "=" + bool3 + "\n\t" + "com.sun.management.jmxremote.ssl.config.file" + "=" + str5 + "\n\t" + "com.sun.management.jmxremote.ssl.enabled.cipher.suites" + "=" + str4 + "\n\t" + "com.sun.management.jmxremote.ssl.enabled.protocols" + "=" + ((String)localObject1) + "\n\t" + "com.sun.management.jmxremote.ssl.need.client.auth" + "=" + bool4 + "\n\t" + "com.sun.management.jmxremote.authenticate" + "=" + bool1 + ((bool1) ? "\n\tcom.sun.management.jmxremote.login.config=" + str6 : (str6 == null) ? "\n\tcom.sun.management.jmxremote.password.file=" + str7 : new StringBuilder().append("\n\t").append(Agent.getText("jmxremote.ConnectorBootstrap.initialize.noAuthentication")).toString()) + ((bool1) ? "\n\tcom.sun.management.jmxremote.access.file=" + str8 : "") + "");
    MBeanServer localMBeanServer = ManagementFactory.getPlatformMBeanServer();
    JMXConnectorServer localJMXConnectorServer = null;
    JMXServiceURL localJMXServiceURL = null;
    try
    {
      JMXConnectorServerData localJMXConnectorServerData = exportMBeanServer(localMBeanServer, i, bool2, bool3, str5, arrayOfString1, arrayOfString2, bool4, bool1, str6, str7, str8);
      localJMXConnectorServer = localJMXConnectorServerData.jmxConnectorServer;
      localJMXServiceURL = localJMXConnectorServerData.jmxRemoteURL;
      log.config("initialize", Agent.getText("jmxremote.ConnectorBootstrap.initialize.ready", new String[] { localJMXServiceURL.toString() }));
    }
    catch (Exception localException1)
    {
      throw new AgentConfigurationError("agent.err.exception", localException1, new String[] { localException1.toString() });
    }
    try
    {
      HashMap localHashMap = new HashMap();
      localHashMap.put("remoteAddress", localJMXServiceURL.toString());
      localHashMap.put("authenticate", str1);
      localHashMap.put("ssl", str2);
      localHashMap.put("sslRegistry", str3);
      localHashMap.put("sslNeedClientAuth", localObject2);
      ConnectorAddressLink.exportRemote(localHashMap);
    }
    catch (Exception localException2)
    {
      log.debug("initialize", localException2);
    }
    return ((JMXConnectorServer)(JMXConnectorServer)localJMXConnectorServer);
  }

  public static JMXConnectorServer startLocalConnectorServer()
  {
    System.setProperty("java.rmi.server.randomIDs", "true");
    HashMap localHashMap = new HashMap();
    localHashMap.put("com.sun.jmx.remote.rmi.exporter", new PermanentExporter(null));
    String str1 = "localhost";
    InetAddress localInetAddress = null;
    try
    {
      localInetAddress = InetAddress.getByName(str1);
      str1 = localInetAddress.getHostAddress();
    }
    catch (UnknownHostException localUnknownHostException)
    {
    }
    if ((localInetAddress == null) || (!(localInetAddress.isLoopbackAddress())))
      str1 = "127.0.0.1";
    MBeanServer localMBeanServer = ManagementFactory.getPlatformMBeanServer();
    try
    {
      JMXServiceURL localJMXServiceURL = new JMXServiceURL("rmi", str1, 0);
      Properties localProperties = Agent.getManagementProperties();
      if (localProperties == null)
        localProperties = new Properties();
      String str2 = localProperties.getProperty("com.sun.management.jmxremote.local.only", "true");
      boolean bool = Boolean.valueOf(str2).booleanValue();
      if (bool)
        localHashMap.put("jmx.remote.rmi.server.socket.factory", new LocalRMIServerSocketFactory());
      JMXConnectorServer localJMXConnectorServer = JMXConnectorServerFactory.newJMXConnectorServer(localJMXServiceURL, localHashMap, localMBeanServer);
      localJMXConnectorServer.start();
      return localJMXConnectorServer;
    }
    catch (Exception localException)
    {
      throw new AgentConfigurationError("agent.err.exception", localException, new String[] { localException.toString() });
    }
  }

  private static void checkPasswordFile(String paramString)
  {
    if ((paramString == null) || (paramString.length() == 0))
      throw new AgentConfigurationError("agent.err.password.file.notset");
    File localFile = new File(paramString);
    if (!(localFile.exists()))
      throw new AgentConfigurationError("agent.err.password.file.notfound", new String[] { paramString });
    if (!(localFile.canRead()))
      throw new AgentConfigurationError("agent.err.password.file.not.readable", new String[] { paramString });
    FileSystem localFileSystem = FileSystem.open();
    try
    {
      if ((localFileSystem.supportsFileSecurity(localFile)) && (!(localFileSystem.isAccessUserOnly(localFile))))
      {
        String str = Agent.getText("jmxremote.ConnectorBootstrap.initialize.password.readonly", new String[] { paramString });
        log.config("initialize", str);
        throw new AgentConfigurationError("agent.err.password.file.access.notrestricted", new String[] { paramString });
      }
    }
    catch (IOException localIOException)
    {
      throw new AgentConfigurationError("agent.err.password.file.read.failed", localIOException, new String[] { paramString });
    }
  }

  private static void checkAccessFile(String paramString)
  {
    if ((paramString == null) || (paramString.length() == 0))
      throw new AgentConfigurationError("agent.err.access.file.notset");
    File localFile = new File(paramString);
    if (!(localFile.exists()))
      throw new AgentConfigurationError("agent.err.access.file.notfound", new String[] { paramString });
    if (!(localFile.canRead()))
      throw new AgentConfigurationError("agent.err.access.file.not.readable", new String[] { paramString });
  }

  private static void checkRestrictedFile(String paramString)
  {
    if ((paramString == null) || (paramString.length() == 0))
      throw new AgentConfigurationError("agent.err.file.not.set");
    File localFile = new File(paramString);
    if (!(localFile.exists()))
      throw new AgentConfigurationError("agent.err.file.not.found", new String[] { paramString });
    if (!(localFile.canRead()))
      throw new AgentConfigurationError("agent.err.file.not.readable", new String[] { paramString });
    FileSystem localFileSystem = FileSystem.open();
    try
    {
      if ((localFileSystem.supportsFileSecurity(localFile)) && (!(localFileSystem.isAccessUserOnly(localFile))))
      {
        String str = Agent.getText("jmxremote.ConnectorBootstrap.initialize.file.readonly", new String[] { paramString });
        log.config("initialize", str);
        throw new AgentConfigurationError("agent.err.file.access.not.restricted", new String[] { paramString });
      }
    }
    catch (IOException localIOException)
    {
      throw new AgentConfigurationError("agent.err.file.read.failed", localIOException, new String[] { paramString });
    }
  }

  private static String getDefaultFileName(String paramString)
  {
    String str = File.separator;
    return System.getProperty("java.home") + str + "lib" + str + "management" + str + paramString;
  }

  private static SslRMIServerSocketFactory createSslRMIServerSocketFactory(String paramString, String[] paramArrayOfString1, String[] paramArrayOfString2, boolean paramBoolean)
  {
    if (paramString == null)
      return new SslRMIServerSocketFactory(paramArrayOfString1, paramArrayOfString2, paramBoolean);
    checkRestrictedFile(paramString);
    try
    {
      Properties localProperties = new Properties();
      FileInputStream localFileInputStream = new FileInputStream(paramString);
      try
      {
        localObject1 = new BufferedInputStream(localFileInputStream);
        localProperties.load((InputStream)localObject1);
      }
      finally
      {
        localFileInputStream.close();
      }
      Object localObject1 = localProperties.getProperty("javax.net.ssl.keyStore");
      String str1 = localProperties.getProperty("javax.net.ssl.keyStorePassword", "");
      String str2 = localProperties.getProperty("javax.net.ssl.trustStore");
      String str3 = localProperties.getProperty("javax.net.ssl.trustStorePassword", "");
      char[] arrayOfChar1 = null;
      if (str1.length() != 0)
        arrayOfChar1 = str1.toCharArray();
      char[] arrayOfChar2 = null;
      if (str3.length() != 0)
        arrayOfChar2 = str3.toCharArray();
      KeyStore localKeyStore1 = null;
      if (localObject1 != null)
      {
        localKeyStore1 = KeyStore.getInstance(KeyStore.getDefaultType());
        localObject3 = new FileInputStream((String)localObject1);
        try
        {
          localKeyStore1.load((InputStream)localObject3, arrayOfChar1);
        }
        finally
        {
          ((FileInputStream)localObject3).close();
        }
      }
      Object localObject3 = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
      ((KeyManagerFactory)localObject3).init(localKeyStore1, arrayOfChar1);
      KeyStore localKeyStore2 = null;
      if (str2 != null)
      {
        localKeyStore2 = KeyStore.getInstance(KeyStore.getDefaultType());
        localObject5 = new FileInputStream(str2);
        try
        {
          localKeyStore2.load((InputStream)localObject5, arrayOfChar2);
        }
        finally
        {
          ((FileInputStream)localObject5).close();
        }
      }
      Object localObject5 = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
      ((TrustManagerFactory)localObject5).init(localKeyStore2);
      SSLContext localSSLContext = SSLContext.getInstance("SSL");
      localSSLContext.init(((KeyManagerFactory)localObject3).getKeyManagers(), ((TrustManagerFactory)localObject5).getTrustManagers(), null);
      return new SSLContextRMIServerSocketFactory(localSSLContext, paramArrayOfString1, paramArrayOfString2, paramBoolean);
    }
    catch (Exception localException)
    {
      throw new AgentConfigurationError("agent.err.exception", localException, new String[] { localException.toString() });
    }
  }

  private static JMXConnectorServerData exportMBeanServer(MBeanServer paramMBeanServer, int paramInt, boolean paramBoolean1, boolean paramBoolean2, String paramString1, String[] paramArrayOfString1, String[] paramArrayOfString2, boolean paramBoolean3, boolean paramBoolean4, String paramString2, String paramString3, String paramString4)
    throws IOException, MalformedURLException
  {
    SingleEntryRegistry localSingleEntryRegistry;
    System.setProperty("java.rmi.server.randomIDs", "true");
    JMXServiceURL localJMXServiceURL1 = new JMXServiceURL("rmi", null, 0);
    HashMap localHashMap = new HashMap();
    PermanentExporter localPermanentExporter = new PermanentExporter(null);
    localHashMap.put("com.sun.jmx.remote.rmi.exporter", localPermanentExporter);
    if (paramBoolean4)
    {
      if (paramString2 != null)
        localHashMap.put("jmx.remote.x.login.config", paramString2);
      if (paramString3 != null)
        localHashMap.put("jmx.remote.x.password.file", paramString3);
      localHashMap.put("jmx.remote.x.access.file", paramString4);
      if ((localHashMap.get("jmx.remote.x.password.file") != null) || (localHashMap.get("jmx.remote.x.login.config") != null))
        localHashMap.put("jmx.remote.authenticator", new AccessFileCheckerAuthenticator(localHashMap));
    }
    SslRMIClientSocketFactory localSslRMIClientSocketFactory = null;
    SslRMIServerSocketFactory localSslRMIServerSocketFactory = null;
    if ((paramBoolean1) || (paramBoolean2))
    {
      localSslRMIClientSocketFactory = new SslRMIClientSocketFactory();
      localSslRMIServerSocketFactory = createSslRMIServerSocketFactory(paramString1, paramArrayOfString1, paramArrayOfString2, paramBoolean3);
    }
    if (paramBoolean1)
    {
      localHashMap.put("jmx.remote.rmi.client.socket.factory", localSslRMIClientSocketFactory);
      localHashMap.put("jmx.remote.rmi.server.socket.factory", localSslRMIServerSocketFactory);
    }
    JMXConnectorServer localJMXConnectorServer = null;
    try
    {
      localJMXConnectorServer = JMXConnectorServerFactory.newJMXConnectorServer(localJMXServiceURL1, localHashMap, paramMBeanServer);
      localJMXConnectorServer.start();
    }
    catch (IOException localIOException)
    {
      if (localJMXConnectorServer == null)
        throw new AgentConfigurationError("agent.err.connector.server.io.error", localIOException, new String[] { localJMXServiceURL1.toString() });
      throw new AgentConfigurationError("agent.err.connector.server.io.error", localIOException, new String[] { localJMXConnectorServer.getAddress().toString() });
    }
    if (paramBoolean2)
      localSingleEntryRegistry = new SingleEntryRegistry(paramInt, localSslRMIClientSocketFactory, localSslRMIServerSocketFactory, "jmxrmi", localPermanentExporter.firstExported);
    else
      localSingleEntryRegistry = new SingleEntryRegistry(paramInt, "jmxrmi", localPermanentExporter.firstExported);
    JMXServiceURL localJMXServiceURL2 = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + localJMXServiceURL1.getHost() + ":" + ((UnicastRef)((RemoteObject)localSingleEntryRegistry).getRef()).getLiveRef().getPort() + "/jmxrmi");
    return new JMXConnectorServerData(localJMXConnectorServer, localJMXServiceURL2);
  }

  private static class AccessFileCheckerAuthenticator
  implements JMXAuthenticator
  {
    private final Map<String, Object> environment;
    private final Properties properties;
    private final String accessFile;

    public AccessFileCheckerAuthenticator(Map<String, Object> paramMap)
      throws IOException
    {
      this.environment = paramMap;
      this.accessFile = ((String)paramMap.get("jmx.remote.x.access.file"));
      this.properties = propertiesFromFile(this.accessFile);
    }

    public Subject authenticate(Object paramObject)
    {
      JMXPluggableAuthenticator localJMXPluggableAuthenticator = new JMXPluggableAuthenticator(this.environment);
      Subject localSubject = localJMXPluggableAuthenticator.authenticate(paramObject);
      checkAccessFileEntries(localSubject);
      return localSubject;
    }

    private void checkAccessFileEntries(Subject paramSubject)
    {
      if (paramSubject == null)
        throw new SecurityException("Access denied! No matching entries found in the access file [" + this.accessFile + "] as the " + "authenticated Subject is null");
      Set localSet = paramSubject.getPrincipals();
      Object localObject1 = localSet.iterator();
      while (((Iterator)localObject1).hasNext())
      {
        localObject2 = (Principal)((Iterator)localObject1).next();
        if (this.properties.containsKey(((Principal)localObject2).getName()))
          return;
      }
      localObject1 = new HashSet();
      Object localObject2 = localSet.iterator();
      while (((Iterator)localObject2).hasNext())
      {
        Principal localPrincipal = (Principal)((Iterator)localObject2).next();
        ((Set)localObject1).add(localPrincipal.getName());
      }
      throw new SecurityException("Access denied! No entries found in the access file [" + this.accessFile + "] for any of the authenticated identities " + localObject1);
    }

    private static Properties propertiesFromFile(String paramString)
      throws IOException
    {
      Properties localProperties = new Properties();
      if (paramString == null)
        return localProperties;
      FileInputStream localFileInputStream = new FileInputStream(paramString);
      localProperties.load(localFileInputStream);
      localFileInputStream.close();
      return localProperties;
    }
  }

  public static abstract interface DefaultValues
  {
    public static final String PORT = "0";
    public static final String CONFIG_FILE_NAME = "management.properties";
    public static final String USE_LOCAL_ONLY = "true";
    public static final String USE_SSL = "true";
    public static final String USE_REGISTRY_SSL = "false";
    public static final String USE_AUTHENTICATION = "true";
    public static final String PASSWORD_FILE_NAME = "jmxremote.password";
    public static final String ACCESS_FILE_NAME = "jmxremote.access";
    public static final String SSL_NEED_CLIENT_AUTH = "false";
  }

  private static class JMXConnectorServerData
  {
    JMXConnectorServer jmxConnectorServer;
    JMXServiceURL jmxRemoteURL;

    public JMXConnectorServerData(JMXConnectorServer paramJMXConnectorServer, JMXServiceURL paramJMXServiceURL)
    {
      this.jmxConnectorServer = paramJMXConnectorServer;
      this.jmxRemoteURL = paramJMXServiceURL;
    }
  }

  private static class PermanentExporter
  implements RMIExporter
  {
    Remote firstExported;

    public Remote exportObject(Remote paramRemote, int paramInt, RMIClientSocketFactory paramRMIClientSocketFactory, RMIServerSocketFactory paramRMIServerSocketFactory)
      throws RemoteException
    {
      synchronized (this)
      {
        if (this.firstExported == null)
          this.firstExported = paramRemote;
      }
      if ((paramRMIClientSocketFactory == null) && (paramRMIServerSocketFactory == null))
        ??? = new UnicastServerRef(paramInt);
      else
        ??? = new UnicastServerRef2(paramInt, paramRMIClientSocketFactory, paramRMIServerSocketFactory);
      return ((Remote)((UnicastServerRef)???).exportObject(paramRemote, null, true));
    }

    public boolean unexportObject(Remote paramRemote, boolean paramBoolean)
      throws NoSuchObjectException
    {
      return UnicastRemoteObject.unexportObject(paramRemote, paramBoolean);
    }
  }

  public static abstract interface PropertyNames
  {
    public static final String PORT = "com.sun.management.jmxremote.port";
    public static final String CONFIG_FILE_NAME = "com.sun.management.config.file";
    public static final String USE_LOCAL_ONLY = "com.sun.management.jmxremote.local.only";
    public static final String USE_SSL = "com.sun.management.jmxremote.ssl";
    public static final String USE_REGISTRY_SSL = "com.sun.management.jmxremote.registry.ssl";
    public static final String USE_AUTHENTICATION = "com.sun.management.jmxremote.authenticate";
    public static final String PASSWORD_FILE_NAME = "com.sun.management.jmxremote.password.file";
    public static final String ACCESS_FILE_NAME = "com.sun.management.jmxremote.access.file";
    public static final String LOGIN_CONFIG_NAME = "com.sun.management.jmxremote.login.config";
    public static final String SSL_ENABLED_CIPHER_SUITES = "com.sun.management.jmxremote.ssl.enabled.cipher.suites";
    public static final String SSL_ENABLED_PROTOCOLS = "com.sun.management.jmxremote.ssl.enabled.protocols";
    public static final String SSL_NEED_CLIENT_AUTH = "com.sun.management.jmxremote.ssl.need.client.auth";
    public static final String SSL_CONFIG_FILE_NAME = "com.sun.management.jmxremote.ssl.config.file";
  }
}