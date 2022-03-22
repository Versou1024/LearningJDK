package sun.net.www.protocol.jar;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.Permission;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JarURLConnection extends JarURLConnection
{
  private static final boolean debug = 0;
  private static JarFileFactory factory = new JarFileFactory();
  private URL jarFileURL = ???.getJarFileURL();
  private Permission permission;
  private URLConnection jarFileURLConnection = ???.jarFileURL.openConnection();
  private String entryName = ???.getEntryName();
  private JarEntry jarEntry;
  private JarFile jarFile;
  private String contentType;

  public JarURLConnection(URL paramURL, Handler paramHandler)
    throws MalformedURLException, IOException
  {
    super(paramURL);
  }

  public JarFile getJarFile()
    throws IOException
  {
    connect();
    return this.jarFile;
  }

  public JarEntry getJarEntry()
    throws IOException
  {
    connect();
    return this.jarEntry;
  }

  public Permission getPermission()
    throws IOException
  {
    return this.jarFileURLConnection.getPermission();
  }

  public void connect()
    throws IOException
  {
    if (!(this.connected))
    {
      this.jarFile = factory.get(getJarFileURL(), getUseCaches());
      if (getUseCaches())
        this.jarFileURLConnection = factory.getConnection(this.jarFile);
      if (this.entryName != null)
      {
        this.jarEntry = ((JarEntry)this.jarFile.getEntry(this.entryName));
        if (this.jarEntry == null)
        {
          try
          {
            if (!(getUseCaches()))
              this.jarFile.close();
          }
          catch (Exception localException)
          {
          }
          throw new FileNotFoundException("JAR entry " + this.entryName + " not found in " + this.jarFile.getName());
        }
      }
      this.connected = true;
    }
  }

  public InputStream getInputStream()
    throws IOException
  {
    connect();
    JarURLInputStream localJarURLInputStream = null;
    if (this.entryName == null)
      throw new IOException("no entry name specified");
    if (this.jarEntry == null)
      throw new FileNotFoundException("JAR entry " + this.entryName + " not found in " + this.jarFile.getName());
    localJarURLInputStream = new JarURLInputStream(this, this.jarFile.getInputStream(this.jarEntry));
    return localJarURLInputStream;
  }

  public int getContentLength()
  {
    int i = -1;
    try
    {
      connect();
      if (this.jarEntry == null)
        i = this.jarFileURLConnection.getContentLength();
      else
        i = (int)getJarEntry().getSize();
    }
    catch (IOException localIOException)
    {
    }
    return i;
  }

  public Object getContent()
    throws IOException
  {
    Object localObject = null;
    connect();
    if (this.entryName == null)
      localObject = this.jarFile;
    else
      localObject = super.getContent();
    return localObject;
  }

  public String getContentType()
  {
    if (this.contentType == null)
    {
      if (this.entryName == null)
        this.contentType = "x-java/jar";
      else
        try
        {
          connect();
          InputStream localInputStream = this.jarFile.getInputStream(this.jarEntry);
          this.contentType = guessContentTypeFromStream(new BufferedInputStream(localInputStream));
          localInputStream.close();
        }
        catch (IOException localIOException)
        {
        }
      if (this.contentType == null)
        this.contentType = guessContentTypeFromName(this.entryName);
      if (this.contentType == null)
        this.contentType = "content/unknown";
    }
    return this.contentType;
  }

  public String getHeaderField(String paramString)
  {
    return this.jarFileURLConnection.getHeaderField(paramString);
  }

  public void setRequestProperty(String paramString1, String paramString2)
  {
    this.jarFileURLConnection.setRequestProperty(paramString1, paramString2);
  }

  public String getRequestProperty(String paramString)
  {
    return this.jarFileURLConnection.getRequestProperty(paramString);
  }

  public void addRequestProperty(String paramString1, String paramString2)
  {
    this.jarFileURLConnection.addRequestProperty(paramString1, paramString2);
  }

  public Map<String, List<String>> getRequestProperties()
  {
    return this.jarFileURLConnection.getRequestProperties();
  }

  public void setAllowUserInteraction(boolean paramBoolean)
  {
    this.jarFileURLConnection.setAllowUserInteraction(paramBoolean);
  }

  public boolean getAllowUserInteraction()
  {
    return this.jarFileURLConnection.getAllowUserInteraction();
  }

  public void setUseCaches(boolean paramBoolean)
  {
    this.jarFileURLConnection.setUseCaches(paramBoolean);
  }

  public boolean getUseCaches()
  {
    return this.jarFileURLConnection.getUseCaches();
  }

  public void setIfModifiedSince(long paramLong)
  {
    this.jarFileURLConnection.setIfModifiedSince(paramLong);
  }

  public void setDefaultUseCaches(boolean paramBoolean)
  {
    this.jarFileURLConnection.setDefaultUseCaches(paramBoolean);
  }

  public boolean getDefaultUseCaches()
  {
    return this.jarFileURLConnection.getDefaultUseCaches();
  }

  class JarURLInputStream extends FilterInputStream
  {
    JarURLInputStream(, InputStream paramInputStream)
    {
      super(paramInputStream);
    }

    public void close()
      throws IOException
    {
      try
      {
        super.close();
      }
      finally
      {
        if (!(this.this$0.getUseCaches()))
          JarURLConnection.access$000(this.this$0).close();
      }
    }
  }
}