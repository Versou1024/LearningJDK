package sun.net.www;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.FileNameMap;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.StringTokenizer;

public class MimeTable
  implements FileNameMap
{
  private Hashtable entries = new Hashtable();
  private Hashtable extensionMap = new Hashtable();
  private static String tempFileTemplate;
  private static final String filePreamble = "sun.net.www MIME content-types table";
  private static final String fileMagic = "#sun.net.www MIME content-types table";
  private static MimeTable defaultInstance;
  protected static String[] mailcapLocations;

  MimeTable()
  {
    load();
  }

  public static MimeTable getDefaultTable()
  {
    if (defaultInstance == null)
      AccessController.doPrivileged(new PrivilegedAction()
      {
        public Object run()
        {
          MimeTable.access$102(new MimeTable());
          URLConnection.setFileNameMap(MimeTable.access$100());
          return null;
        }
      });
    return defaultInstance;
  }

  public static FileNameMap loadTable()
  {
    MimeTable localMimeTable = getDefaultTable();
    return localMimeTable;
  }

  public synchronized int getSize()
  {
    return this.entries.size();
  }

  public synchronized String getContentTypeFor(String paramString)
  {
    MimeEntry localMimeEntry = findByFileName(paramString);
    if (localMimeEntry != null)
      return localMimeEntry.getType();
    return null;
  }

  public synchronized void add(MimeEntry paramMimeEntry)
  {
    this.entries.put(paramMimeEntry.getType(), paramMimeEntry);
    String[] arrayOfString = paramMimeEntry.getExtensions();
    if (arrayOfString == null)
      return;
    for (int i = 0; i < arrayOfString.length; ++i)
      this.extensionMap.put(arrayOfString[i], paramMimeEntry);
  }

  public synchronized MimeEntry remove(String paramString)
  {
    MimeEntry localMimeEntry = (MimeEntry)this.entries.get(paramString);
    return remove(localMimeEntry);
  }

  public synchronized MimeEntry remove(MimeEntry paramMimeEntry)
  {
    String[] arrayOfString = paramMimeEntry.getExtensions();
    if (arrayOfString != null)
      for (int i = 0; i < arrayOfString.length; ++i)
        this.extensionMap.remove(arrayOfString[i]);
    return ((MimeEntry)this.entries.remove(paramMimeEntry.getType()));
  }

  public synchronized MimeEntry find(String paramString)
  {
    MimeEntry localMimeEntry1 = (MimeEntry)this.entries.get(paramString);
    if (localMimeEntry1 == null)
    {
      Enumeration localEnumeration = this.entries.elements();
      while (localEnumeration.hasMoreElements())
      {
        MimeEntry localMimeEntry2 = (MimeEntry)localEnumeration.nextElement();
        if (localMimeEntry2.matches(paramString))
          return localMimeEntry2;
      }
    }
    return localMimeEntry1;
  }

  public MimeEntry findByFileName(String paramString)
  {
    String str = "";
    int i = paramString.lastIndexOf(35);
    if (i > 0)
      paramString = paramString.substring(0, i - 1);
    i = paramString.lastIndexOf(46);
    i = Math.max(i, paramString.lastIndexOf(47));
    i = Math.max(i, paramString.lastIndexOf(63));
    if ((i != -1) && (paramString.charAt(i) == '.'))
      str = paramString.substring(i).toLowerCase();
    return findByExt(str);
  }

  public synchronized MimeEntry findByExt(String paramString)
  {
    return ((MimeEntry)this.extensionMap.get(paramString));
  }

  public synchronized MimeEntry findByDescription(String paramString)
  {
    Enumeration localEnumeration = elements();
    while (localEnumeration.hasMoreElements())
    {
      MimeEntry localMimeEntry = (MimeEntry)localEnumeration.nextElement();
      if (paramString.equals(localMimeEntry.getDescription()))
        return localMimeEntry;
    }
    return find(paramString);
  }

  String getTempFileTemplate()
  {
    return tempFileTemplate;
  }

  public synchronized Enumeration elements()
  {
    return this.entries.elements();
  }

  public synchronized void load()
  {
    Properties localProperties = new Properties();
    File localFile = null;
    try
    {
      String str = System.getProperty("content.types.user.table");
      if (str != null)
      {
        localFile = new File(str);
        if (!(localFile.exists()))
          localFile = new File(System.getProperty("java.home") + File.separator + "lib" + File.separator + "content-types.properties");
      }
      else
      {
        localFile = new File(System.getProperty("java.home") + File.separator + "lib" + File.separator + "content-types.properties");
      }
      BufferedInputStream localBufferedInputStream = new BufferedInputStream(new FileInputStream(localFile));
      localProperties.load(localBufferedInputStream);
      localBufferedInputStream.close();
    }
    catch (IOException localIOException)
    {
      System.err.println("Warning: default mime table not found: " + localFile.getPath());
      return;
    }
    parse(localProperties);
  }

  void parse(Properties paramProperties)
  {
    String str1 = (String)paramProperties.get("temp.file.template");
    if (str1 != null)
    {
      paramProperties.remove("temp.file.template");
      tempFileTemplate = str1;
    }
    Enumeration localEnumeration = paramProperties.propertyNames();
    while (localEnumeration.hasMoreElements())
    {
      String str2 = (String)localEnumeration.nextElement();
      String str3 = paramProperties.getProperty(str2);
      parse(str2, str3);
    }
  }

  void parse(String paramString1, String paramString2)
  {
    MimeEntry localMimeEntry = new MimeEntry(paramString1);
    StringTokenizer localStringTokenizer = new StringTokenizer(paramString2, ";");
    while (localStringTokenizer.hasMoreTokens())
    {
      String str = localStringTokenizer.nextToken();
      parse(str, localMimeEntry);
    }
    add(localMimeEntry);
  }

  void parse(String paramString, MimeEntry paramMimeEntry)
  {
    String str1 = null;
    String str2 = null;
    int i = 0;
    StringTokenizer localStringTokenizer = new StringTokenizer(paramString, "=");
    while (true)
    {
      while (true)
      {
        if (!(localStringTokenizer.hasMoreTokens()))
          break label61;
        if (i == 0)
          break;
        str2 = localStringTokenizer.nextToken().trim();
      }
      str1 = localStringTokenizer.nextToken().trim();
      i = 1;
    }
    label61: fill(paramMimeEntry, str1, str2);
  }

  void fill(MimeEntry paramMimeEntry, String paramString1, String paramString2)
  {
    if ("description".equalsIgnoreCase(paramString1))
      paramMimeEntry.setDescription(paramString2);
    else if ("action".equalsIgnoreCase(paramString1))
      paramMimeEntry.setAction(getActionCode(paramString2));
    else if ("application".equalsIgnoreCase(paramString1))
      paramMimeEntry.setCommand(paramString2);
    else if ("icon".equalsIgnoreCase(paramString1))
      paramMimeEntry.setImageFileName(paramString2);
    else if ("file_extensions".equalsIgnoreCase(paramString1))
      paramMimeEntry.setExtensions(paramString2);
  }

  String[] getExtensions(String paramString)
  {
    StringTokenizer localStringTokenizer = new StringTokenizer(paramString, ",");
    int i = localStringTokenizer.countTokens();
    String[] arrayOfString = new String[i];
    for (int j = 0; j < i; ++j)
      arrayOfString[j] = localStringTokenizer.nextToken();
    return arrayOfString;
  }

  int getActionCode(String paramString)
  {
    for (int i = 0; i < MimeEntry.actionKeywords.length; ++i)
      if (paramString.equalsIgnoreCase(MimeEntry.actionKeywords[i]))
        return i;
    return 0;
  }

  public synchronized boolean save(String paramString)
  {
    if (paramString == null)
      paramString = System.getProperty("user.home" + File.separator + "lib" + File.separator + "content-types.properties");
    return saveAsProperties(new File(paramString));
  }

  public Properties getAsProperties()
  {
    Properties localProperties = new Properties();
    Enumeration localEnumeration = elements();
    while (localEnumeration.hasMoreElements())
    {
      MimeEntry localMimeEntry = (MimeEntry)localEnumeration.nextElement();
      localProperties.put(localMimeEntry.getType(), localMimeEntry.toProperty());
    }
    return localProperties;
  }

  protected boolean saveAsProperties(File paramFile)
  {
    FileOutputStream localFileOutputStream = null;
    try
    {
      localFileOutputStream = new FileOutputStream(paramFile);
      Properties localProperties = getAsProperties();
      localProperties.put("temp.file.template", tempFileTemplate);
      String str2 = System.getProperty("user.name");
      if (str2 != null)
      {
        String str1 = "; customized for " + str2;
        localProperties.save(localFileOutputStream, "sun.net.www MIME content-types table" + str1);
      }
      else
      {
        localProperties.save(localFileOutputStream, "sun.net.www MIME content-types table");
      }
    }
    catch (IOException localIOException1)
    {
      localIOException1.printStackTrace();
      int i = 0;
      return i;
    }
    finally
    {
      if (localFileOutputStream != null)
        try
        {
          localFileOutputStream.close();
        }
        catch (IOException localIOException2)
        {
        }
    }
    return true;
  }

  static
  {
    AccessController.doPrivileged(new PrivilegedAction()
    {
      public Object run()
      {
        MimeTable.access$002(System.getProperty("content.types.temp.file.template", "/tmp/%s"));
        MimeTable.mailcapLocations = { System.getProperty("user.mailcap"), System.getProperty("user.home") + "/.mailcap", "/etc/mailcap", "/usr/etc/mailcap", "/usr/local/etc/mailcap", System.getProperty("hotjava.home", "/usr/local/hotjava") + "/lib/mailcap" };
        return null;
      }
    });
    defaultInstance = null;
  }
}