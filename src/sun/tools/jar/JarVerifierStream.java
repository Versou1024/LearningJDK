package sun.tools.jar;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.AccessController;
import java.security.KeyManagementException;
import java.security.Principal;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import sun.security.provider.SystemIdentity;
import sun.security.x509.X509Cert;

public class JarVerifierStream extends ZipInputStream
{
  private JarEntry current;
  private Hashtable verified = new Hashtable();
  private JarInputStream jis;
  private Manifest man = null;
  private ArrayList certCache = null;

  public JarVerifierStream(InputStream paramInputStream)
    throws IOException
  {
    super(paramInputStream);
    this.jis = new JarInputStream(paramInputStream);
  }

  public void close()
    throws IOException
  {
    this.jis.close();
  }

  public void closeEntry()
    throws IOException
  {
    this.jis.closeEntry();
  }

  public synchronized ZipEntry getNextEntry()
    throws IOException
  {
    this.current = ((JarEntry)this.jis.getNextEntry());
    return this.current;
  }

  public int read()
    throws IOException
  {
    int i = this.jis.read();
    if (i == -1)
      addIds();
    return i;
  }

  public int read(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
    throws IOException
  {
    int i = this.jis.read(paramArrayOfByte, paramInt1, paramInt2);
    if (i == -1)
      addIds();
    return i;
  }

  private void addIds()
  {
    if (this.current != null)
    {
      java.security.cert.Certificate[] arrayOfCertificate = this.current.getCertificates();
      if (arrayOfCertificate != null)
      {
        Vector localVector = getIds(arrayOfCertificate);
        if (localVector != null)
          this.verified.put(this.current.getName(), localVector);
      }
    }
  }

  public Hashtable getVerifiedSignatures()
  {
    if (this.verified.isEmpty())
      return null;
    return this.verified;
  }

  public Enumeration getBlocks()
  {
    if (this.verified.isEmpty())
      return null;
    return new Enumeration(this)
    {
      public boolean hasMoreElements()
      {
        return false;
      }

      public Object nextElement()
      {
        return null;
      }
    };
  }

  public Hashtable getNameToHash()
  {
    return null;
  }

  public Manifest getManifest()
  {
    if (this.man == null)
      try
      {
        java.util.jar.Manifest localManifest = this.jis.getManifest();
        if (localManifest == null)
          return null;
        ByteArrayOutputStream localByteArrayOutputStream = new ByteArrayOutputStream();
        localManifest.write(localByteArrayOutputStream);
        byte[] arrayOfByte = localByteArrayOutputStream.toByteArray();
        this.man = new Manifest(arrayOfByte);
      }
      catch (IOException localIOException)
      {
      }
    return this.man;
  }

  protected Vector getIds(java.security.cert.Certificate[] paramArrayOfCertificate)
  {
    if (paramArrayOfCertificate == null)
      return null;
    if (this.certCache == null)
      this.certCache = new ArrayList();
    for (int i = 0; i < this.certCache.size(); ++i)
    {
      localCertCache = (CertCache)this.certCache.get(i);
      if (localCertCache.equals(paramArrayOfCertificate))
        return localCertCache.ids;
    }
    CertCache localCertCache = new CertCache();
    localCertCache.certs = paramArrayOfCertificate;
    if (paramArrayOfCertificate.length > 0)
      for (i = 0; i < paramArrayOfCertificate.length; ++i)
        try
        {
          X509Certificate localX509Certificate = (X509Certificate)paramArrayOfCertificate[i];
          Principal localPrincipal = localX509Certificate.getSubjectDN();
          SystemIdentity localSystemIdentity = new SystemIdentity(localPrincipal.getName(), null);
          byte[] arrayOfByte = localX509Certificate.getEncoded();
          X509Cert localX509Cert = new X509Cert(arrayOfByte);
          try
          {
            AccessController.doPrivileged(new PrivilegedExceptionAction(this, localSystemIdentity, localX509Cert)
            {
              public Object run()
                throws KeyManagementException
              {
                this.val$id.addCertificate(this.val$oldC);
                return null;
              }
            });
          }
          catch (PrivilegedActionException localPrivilegedActionException)
          {
            throw ((KeyManagementException)localPrivilegedActionException.getException());
          }
          if (localCertCache.ids == null)
            localCertCache.ids = new Vector();
          localCertCache.ids.addElement(localSystemIdentity);
        }
        catch (KeyManagementException localKeyManagementException)
        {
        }
        catch (IOException localIOException)
        {
        }
        catch (CertificateEncodingException localCertificateEncodingException)
        {
        }
    this.certCache.add(localCertCache);
    return localCertCache.ids;
  }

  static class CertCache
  {
    java.security.cert.Certificate[] certs;
    Vector ids;

    boolean equals(java.security.cert.Certificate[] paramArrayOfCertificate)
    {
      int i;
      int k;
      if (this.certs == null)
        return (paramArrayOfCertificate == null);
      if (paramArrayOfCertificate == null)
        return false;
      for (int j = 0; j < paramArrayOfCertificate.length; ++j)
      {
        i = 0;
        for (k = 0; k < this.certs.length; ++k)
          if (paramArrayOfCertificate[j].equals(this.certs[k]))
          {
            i = 1;
            break;
          }
        if (i == 0)
          return false;
      }
      for (j = 0; j < this.certs.length; ++j)
      {
        i = 0;
        for (k = 0; k < paramArrayOfCertificate.length; ++k)
          if (this.certs[j].equals(paramArrayOfCertificate[k]))
          {
            i = 1;
            break;
          }
        if (i == 0)
          return false;
      }
      return true;
    }
  }
}