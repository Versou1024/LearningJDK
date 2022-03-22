package sun.net.www.protocol.file;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilePermission;
import java.io.IOException;
import java.io.InputStream;
import java.net.FileNameMap;
import java.net.URL;
import java.security.Permission;
import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import sun.net.ProgressMonitor;
import sun.net.ProgressSource;
import sun.net.www.MessageHeader;
import sun.net.www.MeteredStream;
import sun.net.www.ParseUtil;

public class FileURLConnection extends sun.net.www.URLConnection
{
  static String CONTENT_LENGTH = "content-length";
  static String CONTENT_TYPE = "content-type";
  static String TEXT_PLAIN = "text/plain";
  static String LAST_MODIFIED = "last-modified";
  String contentType;
  InputStream is;
  File file;
  String filename;
  boolean isDirectory = false;
  boolean exists = false;
  List files;
  long length = -1L;
  long lastModified = 3412045659165949952L;
  private boolean initializedHeaders = false;
  Permission permission;

  protected FileURLConnection(URL paramURL, File paramFile)
  {
    super(paramURL);
    this.file = paramFile;
  }

  public void connect()
    throws IOException
  {
    if (!(this.connected))
    {
      try
      {
        this.filename = this.file.toString();
        this.isDirectory = this.file.isDirectory();
        if (this.isDirectory)
        {
          this.files = Arrays.asList(this.file.list());
        }
        else
        {
          this.is = new BufferedInputStream(new FileInputStream(this.filename));
          boolean bool = ProgressMonitor.getDefault().shouldMeterInput(this.url, "GET");
          if (bool)
          {
            ProgressSource localProgressSource = new ProgressSource(this.url, "GET", (int)this.file.length());
            this.is = new MeteredStream(this.is, localProgressSource, (int)this.file.length());
          }
        }
      }
      catch (IOException localIOException)
      {
        throw localIOException;
      }
      this.connected = true;
    }
  }

  private void initializeHeaders()
  {
    try
    {
      connect();
      this.exists = this.file.exists();
    }
    catch (IOException localIOException)
    {
    }
    if ((!(this.initializedHeaders)) || (!(this.exists)))
    {
      this.length = this.file.length();
      this.lastModified = this.file.lastModified();
      if (!(this.isDirectory))
      {
        FileNameMap localFileNameMap = java.net.URLConnection.getFileNameMap();
        this.contentType = localFileNameMap.getContentTypeFor(this.filename);
        if (this.contentType != null)
          this.properties.add(CONTENT_TYPE, this.contentType);
        this.properties.add(CONTENT_LENGTH, String.valueOf(this.length));
        if (this.lastModified != 3412047823829467136L)
        {
          Date localDate = new Date(this.lastModified);
          SimpleDateFormat localSimpleDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US);
          localSimpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
          this.properties.add(LAST_MODIFIED, localSimpleDateFormat.format(localDate));
        }
      }
      else
      {
        this.properties.add(CONTENT_TYPE, TEXT_PLAIN);
      }
      this.initializedHeaders = true;
    }
  }

  public String getHeaderField(String paramString)
  {
    initializeHeaders();
    return super.getHeaderField(paramString);
  }

  public String getHeaderField(int paramInt)
  {
    initializeHeaders();
    return super.getHeaderField(paramInt);
  }

  public int getContentLength()
  {
    initializeHeaders();
    return (int)this.length;
  }

  public String getHeaderFieldKey(int paramInt)
  {
    initializeHeaders();
    return super.getHeaderFieldKey(paramInt);
  }

  public MessageHeader getProperties()
  {
    initializeHeaders();
    return super.getProperties();
  }

  public long getLastModified()
  {
    initializeHeaders();
    return this.lastModified;
  }

  public synchronized InputStream getInputStream()
    throws IOException
  {
    connect();
    if (this.is == null)
      if (this.isDirectory)
      {
        FileNameMap localFileNameMap = java.net.URLConnection.getFileNameMap();
        StringBuffer localStringBuffer = new StringBuffer();
        if (this.files == null)
          throw new FileNotFoundException(this.filename);
        Collections.sort(this.files, Collator.getInstance());
        for (int i = 0; i < this.files.size(); ++i)
        {
          String str = (String)this.files.get(i);
          localStringBuffer.append(str);
          localStringBuffer.append("\n");
        }
        this.is = new ByteArrayInputStream(localStringBuffer.toString().getBytes());
      }
      else
      {
        throw new FileNotFoundException(this.filename);
      }
    return this.is;
  }

  public Permission getPermission()
    throws IOException
  {
    if (this.permission == null)
    {
      String str = ParseUtil.decode(this.url.getPath());
      if (File.separatorChar == '/')
        this.permission = new FilePermission(str, "read");
      else
        this.permission = new FilePermission(str.replace('/', File.separatorChar), "read");
    }
    return this.permission;
  }
}