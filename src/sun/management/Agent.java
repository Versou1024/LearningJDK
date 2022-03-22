package sun.management;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXServiceURL;
import sun.management.jmxremote.ConnectorBootstrap;
import sun.management.snmp.AdaptorBootstrap;
import sun.misc.VMSupport;

public class Agent
{
  private static Properties mgmtProps;
  private static ResourceBundle messageRB;
  private static final String CONFIG_FILE = "com.sun.management.config.file";
  private static final String SNMP_PORT = "com.sun.management.snmp.port";
  private static final String JMXREMOTE = "com.sun.management.jmxremote";
  private static final String JMXREMOTE_PORT = "com.sun.management.jmxremote.port";
  private static final String ENABLE_THREAD_CONTENTION_MONITORING = "com.sun.management.enableThreadContentionMonitoring";
  private static final String LOCAL_CONNECTOR_ADDRESS_PROP = "com.sun.management.jmxremote.localConnectorAddress";

  public static void premain(String paramString)
    throws Exception
  {
    agentmain(paramString);
  }

  public static void agentmain(String paramString)
    throws Exception
  {
    if ((paramString == null) || (paramString.length() == 0))
      paramString = "com.sun.management.jmxremote";
    Properties localProperties = new Properties();
    if (paramString != null)
    {
      localObject = paramString.split(",");
      for (int i = 0; i < localObject.length; ++i)
      {
        String[] arrayOfString = localObject[i].split("=");
        if ((arrayOfString.length >= 1) && (arrayOfString.length <= 2))
        {
          String str2 = arrayOfString[0];
          String str3 = (arrayOfString.length == 1) ? "" : arrayOfString[1];
          if ((str2 != null) && (str2.length() > 0))
            if (str2.startsWith("com.sun.management."))
              localProperties.setProperty(str2, str3);
            else
              error("agent.err.invalid.option", str2);
        }
      }
    }
    Object localObject = new Properties();
    String str1 = localProperties.getProperty("com.sun.management.config.file");
    readConfiguration(str1, (Properties)localObject);
    ((Properties)localObject).putAll(localProperties);
    startAgent((Properties)localObject);
  }

  private static void startAgent(Properties paramProperties)
    throws Exception
  {
    String str1 = paramProperties.getProperty("com.sun.management.snmp.port");
    String str2 = paramProperties.getProperty("com.sun.management.jmxremote");
    String str3 = paramProperties.getProperty("com.sun.management.jmxremote.port");
    String str4 = paramProperties.getProperty("com.sun.management.enableThreadContentionMonitoring");
    if (str4 != null)
      ManagementFactory.getThreadMXBean().setThreadContentionMonitoringEnabled(true);
    try
    {
      if (str1 != null)
        AdaptorBootstrap.initialize(str1, paramProperties);
      if ((str2 != null) || (str3 != null))
      {
        if (str3 != null)
          ConnectorBootstrap.initialize(str3, paramProperties);
        Properties localProperties = VMSupport.getAgentProperties();
        if (localProperties.get("com.sun.management.jmxremote.localConnectorAddress") == null)
        {
          JMXConnectorServer localJMXConnectorServer = ConnectorBootstrap.startLocalConnectorServer();
          String str5 = localJMXConnectorServer.getAddress().toString();
          localProperties.put("com.sun.management.jmxremote.localConnectorAddress", str5);
          try
          {
            ConnectorAddressLink.export(str5);
          }
          catch (Exception localException2)
          {
            warning("agent.err.exportaddress.failed", localException2.getMessage());
          }
        }
      }
    }
    catch (AgentConfigurationError localAgentConfigurationError)
    {
      error(localAgentConfigurationError.getError(), localAgentConfigurationError.getParams());
    }
    catch (Exception localException1)
    {
      error(localException1);
    }
  }

  public static Properties loadManagementProperties()
  {
    Properties localProperties = new Properties();
    String str = System.getProperty("com.sun.management.config.file");
    readConfiguration(str, localProperties);
    localProperties.putAll(System.getProperties());
    return localProperties;
  }

  public static synchronized Properties getManagementProperties()
  {
    if (mgmtProps == null)
    {
      String str1 = System.getProperty("com.sun.management.config.file");
      String str2 = System.getProperty("com.sun.management.snmp.port");
      String str3 = System.getProperty("com.sun.management.jmxremote");
      String str4 = System.getProperty("com.sun.management.jmxremote.port");
      if ((str1 == null) && (str2 == null) && (str3 == null) && (str4 == null))
        return null;
      mgmtProps = loadManagementProperties();
    }
    return mgmtProps;
  }

  private static void readConfiguration(String paramString, Properties paramProperties)
  {
    if (paramString == null)
    {
      localObject1 = System.getProperty("java.home");
      if (localObject1 == null)
        throw new Error("Can't find java.home ??");
      localObject2 = new StringBuffer((String)localObject1);
      ((StringBuffer)localObject2).append(File.separator).append("lib");
      ((StringBuffer)localObject2).append(File.separator).append("management");
      ((StringBuffer)localObject2).append(File.separator).append("management.properties");
      paramString = ((StringBuffer)localObject2).toString();
    }
    Object localObject1 = new File(paramString);
    if (!(((File)localObject1).exists()))
      error("agent.err.configfile.notfound", paramString);
    Object localObject2 = null;
    try
    {
      localObject2 = new FileInputStream((File)localObject1);
      BufferedInputStream localBufferedInputStream = new BufferedInputStream((InputStream)localObject2);
      paramProperties.load(localBufferedInputStream);
    }
    catch (FileNotFoundException localIOException2)
    {
      error("agent.err.configfile.failed", localFileNotFoundException.getMessage());
    }
    catch (IOException localIOException4)
    {
      error("agent.err.configfile.failed", localIOException3.getMessage());
    }
    catch (SecurityException localIOException5)
    {
      error("agent.err.configfile.access.denied", paramString);
    }
    finally
    {
      if (localObject2 != null)
        try
        {
          ((InputStream)localObject2).close();
        }
        catch (IOException localIOException6)
        {
          error("agent.err.configfile.closed.failed", paramString);
        }
    }
  }

  public static void startAgent()
    throws Exception
  {
    String str1 = System.getProperty("com.sun.management.agent.class");
    if (str1 == null)
    {
      localObject1 = getManagementProperties();
      if (localObject1 != null)
        startAgent((Properties)localObject1);
      return;
    }
    Object localObject1 = str1.split(":");
    if ((localObject1.length < 1) || (localObject1.length > 2))
      error("agent.err.invalid.agentclass", "\"" + str1 + "\"");
    String str2 = localObject1[0];
    Object localObject2 = (localObject1.length == 2) ? localObject1[1] : null;
    if ((str2 == null) || (str2.length() == 0))
      error("agent.err.invalid.agentclass", "\"" + str1 + "\"");
    if (str2 != null)
    {
      Object localObject3;
      try
      {
        Class localClass = ClassLoader.getSystemClassLoader().loadClass(str2);
        localObject3 = localClass.getMethod("premain", new Class[] { String.class });
        ((Method)localObject3).invoke(null, new Object[] { localObject2 });
      }
      catch (ClassNotFoundException localClassNotFoundException)
      {
        error("agent.err.agentclass.notfound", "\"" + str2 + "\"");
      }
      catch (NoSuchMethodException localNoSuchMethodException)
      {
        error("agent.err.premain.notfound", "\"" + str2 + "\"");
      }
      catch (SecurityException localSecurityException)
      {
        error("agent.err.agentclass.access.denied");
      }
      catch (Exception localException)
      {
        localObject3 = (localException.getCause() == null) ? localException.getMessage() : localException.getCause().getMessage();
        error("agent.err.agentclass.failed", (String)localObject3);
      }
    }
  }

  public static void error(String paramString)
  {
    String str = getText(paramString);
    System.err.print(getText("agent.err.error") + ": " + str);
    throw new RuntimeException(str);
  }

  public static void error(String paramString, String[] paramArrayOfString)
  {
    if ((paramArrayOfString == null) || (paramArrayOfString.length == 0))
    {
      error(paramString);
    }
    else
    {
      StringBuffer localStringBuffer = new StringBuffer(paramArrayOfString[0]);
      for (int i = 1; i < paramArrayOfString.length; ++i)
        localStringBuffer.append(" " + paramArrayOfString[i]);
      error(paramString, localStringBuffer.toString());
    }
  }

  public static void error(String paramString1, String paramString2)
  {
    String str = getText(paramString1);
    System.err.print(getText("agent.err.error") + ": " + str);
    System.err.println(": " + paramString2);
    throw new RuntimeException(str);
  }

  public static void error(Exception paramException)
  {
    paramException.printStackTrace();
    System.err.println(getText("agent.err.exception") + ": " + paramException.toString());
    throw new RuntimeException(paramException);
  }

  public static void warning(String paramString1, String paramString2)
  {
    System.err.print(getText("agent.err.warning") + ": " + getText(paramString1));
    System.err.println(": " + paramString2);
  }

  private static void initResource()
  {
    try
    {
      messageRB = ResourceBundle.getBundle("sun.management.resources.agent");
    }
    catch (MissingResourceException localMissingResourceException)
    {
      throw new Error("Fatal: Resource for management agent is missing");
    }
  }

  public static String getText(String paramString)
  {
    if (messageRB == null)
      initResource();
    try
    {
      return messageRB.getString(paramString);
    }
    catch (MissingResourceException localMissingResourceException)
    {
    }
    return "Missing management agent resource bundle: key = \"" + paramString + "\"";
  }

  public static String getText(String paramString, String[] paramArrayOfString)
  {
    if (messageRB == null)
      initResource();
    String str = messageRB.getString(paramString);
    if (str == null)
      str = "missing resource key: key = \"" + paramString + "\", " + "arguments = \"{0}\", \"{1}\", \"{2}\"";
    return MessageFormat.format(str, (Object[])paramArrayOfString);
  }
}