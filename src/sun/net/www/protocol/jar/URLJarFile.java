package sun.net.www.protocol.jar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.AccessController;
import java.security.CodeSigner;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.cert.Certificate;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import sun.net.www.ParseUtil;

public class URLJarFile extends JarFile
{
  private static URLJarFileCallBack callback = null;
  private URLJarFileCloseController closeController;
  private static int BUF_SIZE = 2048;
  private Manifest superMan;
  private Attributes superAttr;
  private Map superEntries;

  static JarFile getJarFile(URL paramURL)
    throws IOException
  {
    return getJarFile(paramURL, null);
  }

  static JarFile getJarFile(URL paramURL, URLJarFileCloseController paramURLJarFileCloseController)
    throws IOException
  {
    if (isFileURL(paramURL))
      return new URLJarFile(paramURL, paramURLJarFileCloseController);
    return retrieve(paramURL, paramURLJarFileCloseController);
  }

  public URLJarFile(File paramFile)
    throws IOException
  {
    this(paramFile, null);
  }

  public URLJarFile(File paramFile, URLJarFileCloseController paramURLJarFileCloseController)
    throws IOException
  {
    super(paramFile, true, 5);
    this.closeController = null;
    this.closeController = paramURLJarFileCloseController;
  }

  private URLJarFile(URL paramURL, URLJarFileCloseController paramURLJarFileCloseController)
    throws IOException
  {
    super(ParseUtil.decode(paramURL.getFile()));
    this.closeController = null;
    this.closeController = paramURLJarFileCloseController;
  }

  private static boolean isFileURL(URL paramURL)
  {
    if (!(paramURL.getProtocol().equalsIgnoreCase("file")))
      break label50;
    String str = paramURL.getHost();
    label50: return ((str == null) || (str.equals("")) || (str.equals("~")) || (str.equalsIgnoreCase("localhost")));
  }

  protected void finalize()
    throws IOException
  {
    close();
  }

  public ZipEntry getEntry(String paramString)
  {
    ZipEntry localZipEntry = super.getEntry(paramString);
    if (localZipEntry != null)
    {
      if (localZipEntry instanceof JarEntry)
        return new URLJarFileEntry(this, (JarEntry)localZipEntry);
      throw new InternalError(getClass() + " returned unexpected entry type " + localZipEntry.getClass());
    }
    return null;
  }

  public Manifest getManifest()
    throws IOException
  {
    if (!(isSuperMan()))
      return null;
    Manifest localManifest = new Manifest();
    Attributes localAttributes1 = localManifest.getMainAttributes();
    localAttributes1.putAll((Map)this.superAttr.clone());
    if (this.superEntries != null)
    {
      Map localMap = localManifest.getEntries();
      Iterator localIterator = this.superEntries.keySet().iterator();
      while (localIterator.hasNext())
      {
        Object localObject = localIterator.next();
        Attributes localAttributes2 = (Attributes)this.superEntries.get(localObject);
        localMap.put(localObject, localAttributes2.clone());
      }
    }
    return localManifest;
  }

  public void close()
    throws IOException
  {
    if (this.closeController != null)
      this.closeController.close(this);
    super.close();
  }

  private synchronized boolean isSuperMan()
    throws IOException
  {
    if (this.superMan == null)
      this.superMan = super.getManifest();
    if (this.superMan != null)
    {
      this.superAttr = this.superMan.getMainAttributes();
      this.superEntries = this.superMan.getEntries();
      return true;
    }
    return false;
  }

  private static JarFile retrieve(URL paramURL)
    throws IOException
  {
    return retrieve(paramURL, null);
  }

  private static JarFile retrieve(URL paramURL, URLJarFileCloseController paramURLJarFileCloseController)
    throws IOException
  {
    if (callback != null)
      return callback.retrieve(paramURL);
    JarFile localJarFile = null;
    InputStream localInputStream = paramURL.openConnection().getInputStream();
    try
    {
      localJarFile = (JarFile)AccessController.doPrivileged(new PrivilegedExceptionAction(localInputStream, paramURLJarFileCloseController)
      {
        public Object run()
          throws IOException
        {
          FileOutputStream localFileOutputStream = null;
          File localFile = null;
          try
          {
            localFile = File.createTempFile("jar_cache", null);
            localFile.deleteOnExit();
            localFileOutputStream = new FileOutputStream(localFile);
            int i = 0;
            byte[] arrayOfByte = new byte[URLJarFile.access$000()];
            while ((i = this.val$in.read(arrayOfByte)) != -1)
              localFileOutputStream.write(arrayOfByte, 0, i);
            localFileOutputStream.close();
            localFileOutputStream = null;
            URLJarFile localURLJarFile = new URLJarFile(localFile, this.val$closeController);
            return localURLJarFile;
          }
          catch (IOException localIOException)
          {
            if (localFile != null);
            throw localIOException;
          }
          finally
          {
            if (this.val$in != null)
              this.val$in.close();
            if (localFileOutputStream != null)
              localFileOutputStream.close();
          }
        }
      });
    }
    catch (PrivilegedActionException localPrivilegedActionException)
    {
      throw ((IOException)localPrivilegedActionException.getException());
    }
    return localJarFile;
  }

  public static void setCallBack(URLJarFileCallBack paramURLJarFileCallBack)
  {
    callback = paramURLJarFileCallBack;
  }

  public static abstract interface URLJarFileCloseController
  {
    public abstract void close(JarFile paramJarFile);
  }

  private class URLJarFileEntry extends JarEntry
  {
    private JarEntry je;

    URLJarFileEntry(, JarEntry paramJarEntry)
    {
      super(paramJarEntry);
      this.je = paramJarEntry;
    }

    public Attributes getAttributes()
      throws IOException
    {
      if (URLJarFile.access$100(this.this$0))
      {
        Map localMap = URLJarFile.access$200(this.this$0);
        if (localMap != null)
        {
          Attributes localAttributes = (Attributes)localMap.get(getName());
          if (localAttributes != null)
            return ((Attributes)localAttributes.clone());
        }
      }
      return null;
    }

    public Certificate[] getCertificates()
    {
      Certificate[] arrayOfCertificate = this.je.getCertificates();
      return ((arrayOfCertificate == null) ? null : (Certificate[])(Certificate[])arrayOfCertificate.clone());
    }

    public CodeSigner[] getCodeSigners()
    {
      CodeSigner[] arrayOfCodeSigner = this.je.getCodeSigners();
      return ((arrayOfCodeSigner == null) ? null : (CodeSigner[])(CodeSigner[])arrayOfCodeSigner.clone());
    }
  }
}