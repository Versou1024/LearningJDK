package sun.awt;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringBufferInputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

final class DebugSettings
{
  static final String PREFIX = "awtdebug";
  static final String PROP_FILE = "properties";
  private static final String[] DEFAULT_PROPS = { "awtdebug.assert=true", "awtdebug.trace=false", "awtdebug.on=true", "awtdebug.ctrace=false" };
  private static DebugSettings instance = null;
  private Properties props = new Properties();

  static DebugSettings getInstance()
  {
    if (instance == null)
      instance = new DebugSettings();
    return instance;
  }

  private DebugSettings()
  {
    new PrivilegedAction(this)
    {
      public Object run()
      {
        DebugSettings.access$000(this.this$0);
        return null;
      }
    }
    .run();
  }

  private synchronized void loadProperties()
  {
    AccessController.doPrivileged(new PrivilegedAction(this)
    {
      public Object run()
      {
        DebugSettings.access$100(this.this$0);
        DebugSettings.access$200(this.this$0);
        DebugSettings.access$300(this.this$0);
        return null;
      }
    });
    println(this);
  }

  public String toString()
  {
    Enumeration localEnumeration = this.props.propertyNames();
    ByteArrayOutputStream localByteArrayOutputStream = new ByteArrayOutputStream();
    PrintStream localPrintStream = new PrintStream(localByteArrayOutputStream);
    localPrintStream.println("------------------");
    localPrintStream.println("AWT Debug Settings");
    localPrintStream.println("------------------");
    while (localEnumeration.hasMoreElements())
    {
      String str1 = (String)localEnumeration.nextElement();
      String str2 = this.props.getProperty(str1, "");
      localPrintStream.println(str1 + "=" + str2);
    }
    localPrintStream.println("------------------");
    return new String(localByteArrayOutputStream.toByteArray());
  }

  private void loadDefaultProperties()
  {
    int i;
    try
    {
      for (i = 0; i < DEFAULT_PROPS.length; ++i)
      {
        StringBufferInputStream localStringBufferInputStream = new StringBufferInputStream(DEFAULT_PROPS[i]);
        this.props.load(localStringBufferInputStream);
        localStringBufferInputStream.close();
      }
    }
    catch (IOException localIOException)
    {
    }
  }

  private void loadFileProperties()
  {
    String str = System.getProperty("awtdebug.properties", "");
    if (str.equals(""))
      str = System.getProperty("user.home", "") + File.separator + "awtdebug" + "." + "properties";
    File localFile = new File(str);
    try
    {
      println("Reading debug settings from '" + localFile.getCanonicalPath() + "'...");
      FileInputStream localFileInputStream = new FileInputStream(localFile);
      this.props.load(localFileInputStream);
      localFileInputStream.close();
    }
    catch (FileNotFoundException localFileNotFoundException)
    {
      println("Did not find settings file.");
    }
    catch (IOException localIOException)
    {
      println("Problem reading settings, IOException: " + localIOException.getMessage());
    }
  }

  private void loadSystemProperties()
  {
    Properties localProperties = System.getProperties();
    Enumeration localEnumeration = localProperties.propertyNames();
    while (localEnumeration.hasMoreElements())
    {
      String str1 = (String)localEnumeration.nextElement();
      String str2 = localProperties.getProperty(str1, "");
      if (str1.startsWith("awtdebug"))
        this.props.setProperty(str1, str2);
    }
  }

  public synchronized boolean getBoolean(String paramString, boolean paramBoolean)
  {
    String str = getString(paramString, String.valueOf(paramBoolean));
    return str.equalsIgnoreCase("true");
  }

  public synchronized int getInt(String paramString, int paramInt)
  {
    String str = getString(paramString, String.valueOf(paramInt));
    return Integer.parseInt(str);
  }

  public synchronized String getString(String paramString1, String paramString2)
  {
    String str1 = "awtdebug." + paramString1;
    String str2 = this.props.getProperty(str1, paramString2);
    return str2;
  }

  public synchronized Enumeration getPropertyNames()
  {
    Vector localVector = new Vector();
    Enumeration localEnumeration = this.props.propertyNames();
    while (localEnumeration.hasMoreElements())
    {
      String str = (String)localEnumeration.nextElement();
      str = str.substring("awtdebug".length() + 1);
      localVector.addElement(str);
    }
    return localVector.elements();
  }

  private void println(Object paramObject)
  {
    DebugHelperImpl.printlnImpl(paramObject.toString());
  }
}