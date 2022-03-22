package sun.net.www;

import java.io.File;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.StringTokenizer;

public class MimeEntry
  implements Cloneable
{
  private String typeName;
  private String tempFileNameTemplate;
  private int action;
  private String command;
  private String description;
  private String imageFileName;
  private String[] fileExtensions;
  boolean starred;
  public static final int UNKNOWN = 0;
  public static final int LOAD_INTO_BROWSER = 1;
  public static final int SAVE_TO_FILE = 2;
  public static final int LAUNCH_APPLICATION = 3;
  static final String[] actionKeywords = { "unknown", "browser", "save", "application" };

  public MimeEntry(String paramString)
  {
    this(paramString, 0, null, null, null);
  }

  MimeEntry(String paramString1, String paramString2, String paramString3)
  {
    this.typeName = paramString1.toLowerCase();
    this.action = 0;
    this.command = null;
    this.imageFileName = paramString2;
    setExtensions(paramString3);
    this.starred = isStarred(this.typeName);
  }

  MimeEntry(String paramString1, int paramInt, String paramString2, String paramString3)
  {
    this.typeName = paramString1.toLowerCase();
    this.action = paramInt;
    this.command = paramString2;
    this.imageFileName = null;
    this.fileExtensions = null;
    this.tempFileNameTemplate = paramString3;
  }

  MimeEntry(String paramString1, int paramInt, String paramString2, String paramString3, String[] paramArrayOfString)
  {
    this.typeName = paramString1.toLowerCase();
    this.action = paramInt;
    this.command = paramString2;
    this.imageFileName = paramString3;
    this.fileExtensions = paramArrayOfString;
    this.starred = isStarred(paramString1);
  }

  public synchronized String getType()
  {
    return this.typeName;
  }

  public synchronized void setType(String paramString)
  {
    this.typeName = paramString.toLowerCase();
  }

  public synchronized int getAction()
  {
    return this.action;
  }

  public synchronized void setAction(int paramInt, String paramString)
  {
    this.action = paramInt;
    this.command = paramString;
  }

  public synchronized void setAction(int paramInt)
  {
    this.action = paramInt;
  }

  public synchronized String getLaunchString()
  {
    return this.command;
  }

  public synchronized void setCommand(String paramString)
  {
    this.command = paramString;
  }

  public synchronized String getDescription()
  {
    return ((this.description != null) ? this.description : this.typeName);
  }

  public synchronized void setDescription(String paramString)
  {
    this.description = paramString;
  }

  public String getImageFileName()
  {
    return this.imageFileName;
  }

  public synchronized void setImageFileName(String paramString)
  {
    File localFile = new File(paramString);
    if (localFile.getParent() == null)
      this.imageFileName = System.getProperty("java.net.ftp.imagepath." + paramString);
    else
      this.imageFileName = paramString;
    if (paramString.lastIndexOf(46) < 0)
      this.imageFileName += ".gif";
  }

  public String getTempFileTemplate()
  {
    return this.tempFileNameTemplate;
  }

  public synchronized String[] getExtensions()
  {
    return this.fileExtensions;
  }

  public synchronized String getExtensionsAsList()
  {
    String str = "";
    if (this.fileExtensions != null)
      for (int i = 0; i < this.fileExtensions.length; ++i)
      {
        str = str + this.fileExtensions[i];
        if (i < this.fileExtensions.length - 1)
          str = str + ",";
      }
    return str;
  }

  public synchronized void setExtensions(String paramString)
  {
    StringTokenizer localStringTokenizer = new StringTokenizer(paramString, ",");
    int i = localStringTokenizer.countTokens();
    String[] arrayOfString = new String[i];
    for (int j = 0; j < i; ++j)
    {
      String str = (String)localStringTokenizer.nextElement();
      arrayOfString[j] = str.trim();
    }
    this.fileExtensions = arrayOfString;
  }

  private boolean isStarred(String paramString)
  {
    return ((paramString != null) && (paramString.length() > 0) && (paramString.endsWith("/*")));
  }

  public Object launch(URLConnection paramURLConnection, InputStream paramInputStream, MimeTable paramMimeTable)
    throws sun.net.www.ApplicationLaunchException
  {
    switch (this.action)
    {
    case 2:
      try
      {
        return paramInputStream;
      }
      catch (Exception localException1)
      {
        return "Load to file failed:\n" + localException1;
      }
    case 1:
      try
      {
        return paramURLConnection.getContent();
      }
      catch (Exception localException2)
      {
        return null;
      }
    case 3:
      String str = this.command;
      int i = str.indexOf(32);
      if (i > 0)
        str = str.substring(0, i);
      return new MimeLauncher(this, paramURLConnection, paramInputStream, paramMimeTable.getTempFileTemplate(), str);
    case 0:
      return null;
    }
    return null;
  }

  public boolean matches(String paramString)
  {
    if (this.starred)
      return paramString.startsWith(this.typeName);
    return paramString.equals(this.typeName);
  }

  public Object clone()
  {
    MimeEntry localMimeEntry = new MimeEntry(this.typeName);
    localMimeEntry.action = this.action;
    localMimeEntry.command = this.command;
    localMimeEntry.description = this.description;
    localMimeEntry.imageFileName = this.imageFileName;
    localMimeEntry.tempFileNameTemplate = this.tempFileNameTemplate;
    localMimeEntry.fileExtensions = this.fileExtensions;
    return localMimeEntry;
  }

  public synchronized String toProperty()
  {
    StringBuffer localStringBuffer = new StringBuffer();
    String str1 = "; ";
    int i = 0;
    int j = getAction();
    if (j != 0)
    {
      localStringBuffer.append("action=" + actionKeywords[j]);
      i = 1;
    }
    String str2 = getLaunchString();
    if ((str2 != null) && (str2.length() > 0))
    {
      if (i != 0)
        localStringBuffer.append(str1);
      localStringBuffer.append("application=" + str2);
      i = 1;
    }
    if (getImageFileName() != null)
    {
      if (i != 0)
        localStringBuffer.append(str1);
      localStringBuffer.append("icon=" + getImageFileName());
      i = 1;
    }
    String str3 = getExtensionsAsList();
    if (str3.length() > 0)
    {
      if (i != 0)
        localStringBuffer.append(str1);
      localStringBuffer.append("file_extensions=" + str3);
      i = 1;
    }
    String str4 = getDescription();
    if ((str4 != null) && (!(str4.equals(getType()))))
    {
      if (i != 0)
        localStringBuffer.append(str1);
      localStringBuffer.append("description=" + str4);
    }
    return localStringBuffer.toString();
  }

  public String toString()
  {
    return "MimeEntry[contentType=" + this.typeName + ", image=" + this.imageFileName + ", action=" + this.action + ", command=" + this.command + ", extensions=" + getExtensionsAsList() + "]";
  }
}