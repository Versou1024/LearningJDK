package sun.misc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class VMSupport
{
  private static Properties agentProps = null;

  public static synchronized Properties getAgentProperties()
  {
    if (agentProps == null)
    {
      agentProps = new Properties();
      initAgentProperties(agentProps);
    }
    return agentProps;
  }

  private static native Properties initAgentProperties(Properties paramProperties);

  private static byte[] serializePropertiesToByteArray(Properties paramProperties)
    throws IOException
  {
    ByteArrayOutputStream localByteArrayOutputStream = new ByteArrayOutputStream(4096);
    Properties localProperties = new Properties();
    Set localSet = paramProperties.stringPropertyNames();
    Iterator localIterator = localSet.iterator();
    while (localIterator.hasNext())
    {
      String str1 = (String)localIterator.next();
      String str2 = paramProperties.getProperty(str1);
      localProperties.put(str1, str2);
    }
    localProperties.store(localByteArrayOutputStream, null);
    return localByteArrayOutputStream.toByteArray();
  }

  public static byte[] serializePropertiesToByteArray()
    throws IOException
  {
    return serializePropertiesToByteArray(System.getProperties());
  }

  public static byte[] serializeAgentPropertiesToByteArray()
    throws IOException
  {
    return serializePropertiesToByteArray(getAgentProperties());
  }

  public static boolean isClassPathAttributePresent(String paramString)
  {
    Manifest localManifest;
    try
    {
      localManifest = new JarFile(paramString).getManifest();
      return ((localManifest != null) && (localManifest.getMainAttributes().getValue(Attributes.Name.CLASS_PATH) != null));
    }
    catch (IOException localIOException)
    {
      throw new RuntimeException(localIOException.getMessage());
    }
  }
}