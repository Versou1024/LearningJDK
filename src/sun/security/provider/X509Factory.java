package sun.security.provider;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.cert.CRL;
import java.security.cert.CRLException;
import java.security.cert.CertPath;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactorySpi;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import sun.misc.BASE64Decoder;
import sun.security.pkcs.PKCS7;
import sun.security.provider.certpath.X509CertPath;
import sun.security.provider.certpath.X509CertificatePair;
import sun.security.util.Cache;
import sun.security.util.Cache.EqualByteArray;
import sun.security.util.DerValue;
import sun.security.x509.X509CRLImpl;
import sun.security.x509.X509CertImpl;

public class X509Factory extends CertificateFactorySpi
{
  public static final String BEGIN_CERT = "-----BEGIN CERTIFICATE-----";
  public static final String END_CERT = "-----END CERTIFICATE-----";
  private static final int defaultExpectedLineLength = 80;
  private static final char[] endBoundary = "-----END".toCharArray();
  private static final int ENC_MAX_LENGTH = 4194304;
  private static final Cache certCache = Cache.newSoftMemoryCache(750);
  private static final Cache crlCache = Cache.newSoftMemoryCache(750);

  public Certificate engineGenerateCertificate(InputStream paramInputStream)
    throws CertificateException
  {
    if (paramInputStream == null)
    {
      certCache.clear();
      X509CertificatePair.clearCache();
      throw new CertificateException("Missing input stream");
    }
    try
    {
      X509CertImpl localX509CertImpl;
      if (!(paramInputStream.markSupported()))
      {
        arrayOfByte1 = getTotalBytes(new BufferedInputStream(paramInputStream));
        paramInputStream = new ByteArrayInputStream(arrayOfByte1);
      }
      byte[] arrayOfByte1 = readSequence(paramInputStream);
      if (arrayOfByte1 != null)
      {
        localX509CertImpl = (X509CertImpl)getFromCache(certCache, arrayOfByte1);
        if (localX509CertImpl != null)
          return localX509CertImpl;
        localX509CertImpl = new X509CertImpl(arrayOfByte1);
        addToCache(certCache, localX509CertImpl.getEncodedInternal(), localX509CertImpl);
        return localX509CertImpl;
      }
      if (isBase64(paramInputStream))
      {
        byte[] arrayOfByte2 = base64_to_binary(paramInputStream);
        localX509CertImpl = new X509CertImpl(arrayOfByte2);
      }
      else
      {
        localX509CertImpl = new X509CertImpl(new DerValue(paramInputStream));
      }
      return intern(localX509CertImpl);
    }
    catch (IOException localIOException)
    {
      throw ((CertificateException)new CertificateException("Could not parse certificate: " + localIOException.toString()).initCause(localIOException));
    }
  }

  private static byte[] readSequence(InputStream paramInputStream)
    throws IOException
  {
    int j;
    int k;
    paramInputStream.mark(4194304);
    byte[] arrayOfByte1 = new byte[4];
    int i = readFully(paramInputStream, arrayOfByte1, 0, arrayOfByte1.length);
    if ((i != arrayOfByte1.length) || (arrayOfByte1[0] != 48))
    {
      paramInputStream.reset();
      return null;
    }
    i = arrayOfByte1[1] & 0xFF;
    if (i < 128)
    {
      k = i;
      j = k + 2;
    }
    else if (i == 129)
    {
      k = arrayOfByte1[2] & 0xFF;
      j = k + 3;
    }
    else if (i == 130)
    {
      k = (arrayOfByte1[2] & 0xFF) << 8 | arrayOfByte1[3] & 0xFF;
      j = k + 4;
    }
    else
    {
      paramInputStream.reset();
      return null;
    }
    if (j > 4194304)
    {
      paramInputStream.reset();
      return null;
    }
    byte[] arrayOfByte2 = new byte[j];
    if (j < arrayOfByte1.length)
    {
      paramInputStream.reset();
      i = readFully(paramInputStream, arrayOfByte2, 0, j);
      if (i == j)
        break label216;
      paramInputStream.reset();
      return null;
    }
    System.arraycopy(arrayOfByte1, 0, arrayOfByte2, 0, arrayOfByte1.length);
    int l = j - arrayOfByte1.length;
    i = readFully(paramInputStream, arrayOfByte2, arrayOfByte1.length, l);
    if (i != l)
    {
      paramInputStream.reset();
      return null;
    }
    label216: return arrayOfByte2;
  }

  private static int readFully(InputStream paramInputStream, byte[] paramArrayOfByte, int paramInt1, int paramInt2)
    throws IOException
  {
    int i = 0;
    while (paramInt2 > 0)
    {
      int j = paramInputStream.read(paramArrayOfByte, paramInt1, paramInt2);
      if (j <= 0)
        break;
      i += j;
      paramInt2 -= j;
      paramInt1 += j;
    }
    return i;
  }

  public static synchronized X509CertImpl intern(X509Certificate paramX509Certificate)
    throws CertificateException
  {
    byte[] arrayOfByte;
    if (paramX509Certificate == null)
      return null;
    boolean bool = paramX509Certificate instanceof X509CertImpl;
    if (bool)
      arrayOfByte = ((X509CertImpl)paramX509Certificate).getEncodedInternal();
    else
      arrayOfByte = paramX509Certificate.getEncoded();
    X509CertImpl localX509CertImpl = (X509CertImpl)getFromCache(certCache, arrayOfByte);
    if (localX509CertImpl != null)
      return localX509CertImpl;
    if (bool)
    {
      localX509CertImpl = (X509CertImpl)paramX509Certificate;
    }
    else
    {
      localX509CertImpl = new X509CertImpl(arrayOfByte);
      arrayOfByte = localX509CertImpl.getEncodedInternal();
    }
    addToCache(certCache, arrayOfByte, localX509CertImpl);
    return localX509CertImpl;
  }

  public static synchronized X509CRLImpl intern(X509CRL paramX509CRL)
    throws CRLException
  {
    byte[] arrayOfByte;
    if (paramX509CRL == null)
      return null;
    boolean bool = paramX509CRL instanceof X509CRLImpl;
    if (bool)
      arrayOfByte = ((X509CRLImpl)paramX509CRL).getEncodedInternal();
    else
      arrayOfByte = paramX509CRL.getEncoded();
    X509CRLImpl localX509CRLImpl = (X509CRLImpl)getFromCache(crlCache, arrayOfByte);
    if (localX509CRLImpl != null)
      return localX509CRLImpl;
    if (bool)
    {
      localX509CRLImpl = (X509CRLImpl)paramX509CRL;
    }
    else
    {
      localX509CRLImpl = new X509CRLImpl(arrayOfByte);
      arrayOfByte = localX509CRLImpl.getEncodedInternal();
    }
    addToCache(crlCache, arrayOfByte, localX509CRLImpl);
    return localX509CRLImpl;
  }

  private static synchronized Object getFromCache(Cache paramCache, byte[] paramArrayOfByte)
  {
    Cache.EqualByteArray localEqualByteArray = new Cache.EqualByteArray(paramArrayOfByte);
    Object localObject = paramCache.get(localEqualByteArray);
    return localObject;
  }

  private static synchronized void addToCache(Cache paramCache, byte[] paramArrayOfByte, Object paramObject)
  {
    if (paramArrayOfByte.length > 4194304)
      return;
    Cache.EqualByteArray localEqualByteArray = new Cache.EqualByteArray(paramArrayOfByte);
    paramCache.put(localEqualByteArray, paramObject);
  }

  public CertPath engineGenerateCertPath(InputStream paramInputStream)
    throws CertificateException
  {
    if (paramInputStream == null)
      throw new CertificateException("Missing input stream");
    try
    {
      byte[] arrayOfByte;
      if (!(paramInputStream.markSupported()))
      {
        arrayOfByte = getTotalBytes(new BufferedInputStream(paramInputStream));
        paramInputStream = new ByteArrayInputStream(arrayOfByte);
      }
      if (isBase64(paramInputStream))
      {
        arrayOfByte = base64_to_binary(paramInputStream);
        return new X509CertPath(new ByteArrayInputStream(arrayOfByte));
      }
      return new X509CertPath(paramInputStream);
    }
    catch (IOException localIOException)
    {
      throw new CertificateException(localIOException.getMessage());
    }
  }

  public CertPath engineGenerateCertPath(InputStream paramInputStream, String paramString)
    throws CertificateException
  {
    if (paramInputStream == null)
      throw new CertificateException("Missing input stream");
    try
    {
      byte[] arrayOfByte;
      if (!(paramInputStream.markSupported()))
      {
        arrayOfByte = getTotalBytes(new BufferedInputStream(paramInputStream));
        paramInputStream = new ByteArrayInputStream(arrayOfByte);
      }
      if (isBase64(paramInputStream))
      {
        arrayOfByte = base64_to_binary(paramInputStream);
        return new X509CertPath(new ByteArrayInputStream(arrayOfByte), paramString);
      }
      return new X509CertPath(paramInputStream, paramString);
    }
    catch (IOException localIOException)
    {
      throw new CertificateException(localIOException.getMessage());
    }
  }

  public CertPath engineGenerateCertPath(List<? extends Certificate> paramList)
    throws CertificateException
  {
    return new X509CertPath(paramList);
  }

  public Iterator<String> engineGetCertPathEncodings()
  {
    return X509CertPath.getEncodingsStatic();
  }

  public Collection<? extends Certificate> engineGenerateCertificates(InputStream paramInputStream)
    throws CertificateException
  {
    if (paramInputStream == null)
      throw new CertificateException("Missing input stream");
    try
    {
      if (!(paramInputStream.markSupported()))
        paramInputStream = new ByteArrayInputStream(getTotalBytes(new BufferedInputStream(paramInputStream)));
      return parseX509orPKCS7Cert(paramInputStream);
    }
    catch (IOException localIOException)
    {
      throw new CertificateException(localIOException);
    }
  }

  public CRL engineGenerateCRL(InputStream paramInputStream)
    throws CRLException
  {
    if (paramInputStream == null)
    {
      crlCache.clear();
      throw new CRLException("Missing input stream");
    }
    try
    {
      X509CRLImpl localX509CRLImpl;
      if (!(paramInputStream.markSupported()))
      {
        arrayOfByte1 = getTotalBytes(new BufferedInputStream(paramInputStream));
        paramInputStream = new ByteArrayInputStream(arrayOfByte1);
      }
      byte[] arrayOfByte1 = readSequence(paramInputStream);
      if (arrayOfByte1 != null)
      {
        localX509CRLImpl = (X509CRLImpl)getFromCache(crlCache, arrayOfByte1);
        if (localX509CRLImpl != null)
          return localX509CRLImpl;
        localX509CRLImpl = new X509CRLImpl(arrayOfByte1);
        addToCache(crlCache, localX509CRLImpl.getEncodedInternal(), localX509CRLImpl);
        return localX509CRLImpl;
      }
      if (isBase64(paramInputStream))
      {
        byte[] arrayOfByte2 = base64_to_binary(paramInputStream);
        localX509CRLImpl = new X509CRLImpl(arrayOfByte2);
      }
      else
      {
        localX509CRLImpl = new X509CRLImpl(new DerValue(paramInputStream));
      }
      return intern(localX509CRLImpl);
    }
    catch (IOException localIOException)
    {
      throw new CRLException(localIOException.getMessage());
    }
  }

  public Collection<? extends CRL> engineGenerateCRLs(InputStream paramInputStream)
    throws CRLException
  {
    if (paramInputStream == null)
      throw new CRLException("Missing input stream");
    try
    {
      if (!(paramInputStream.markSupported()))
        paramInputStream = new ByteArrayInputStream(getTotalBytes(new BufferedInputStream(paramInputStream)));
      return parseX509orPKCS7CRL(paramInputStream);
    }
    catch (IOException localIOException)
    {
      throw new CRLException(localIOException.getMessage());
    }
  }

  private Collection parseX509orPKCS7Cert(InputStream paramInputStream)
    throws CertificateException, IOException
  {
    ArrayList localArrayList = new ArrayList();
    for (int i = 1; paramInputStream.available() != 0; i = 0)
    {
      Object localObject = paramInputStream;
      if (isBase64((InputStream)localObject))
        localObject = new ByteArrayInputStream(base64_to_binary((InputStream)localObject));
      if (i != 0)
        ((InputStream)localObject).mark(((InputStream)localObject).available());
      try
      {
        localArrayList.add(intern(new X509CertImpl(new DerValue((InputStream)localObject))));
      }
      catch (CertificateException localCertificateException)
      {
        Throwable localThrowable = localCertificateException.getCause();
        if ((i != 0) && (localThrowable != null) && (localThrowable instanceof IOException))
        {
          ((InputStream)localObject).reset();
          PKCS7 localPKCS7 = new PKCS7((InputStream)localObject);
          X509Certificate[] arrayOfX509Certificate = localPKCS7.getCertificates();
          if (arrayOfX509Certificate != null)
            return Arrays.asList(arrayOfX509Certificate);
          return new ArrayList(0);
        }
        throw localCertificateException;
      }
    }
    return ((Collection)localArrayList);
  }

  private Collection parseX509orPKCS7CRL(InputStream paramInputStream)
    throws CRLException, IOException
  {
    ArrayList localArrayList = new ArrayList();
    for (int i = 1; paramInputStream.available() != 0; i = 0)
    {
      Object localObject = paramInputStream;
      if (isBase64(paramInputStream))
        localObject = new ByteArrayInputStream(base64_to_binary((InputStream)localObject));
      if (i != 0)
        ((InputStream)localObject).mark(((InputStream)localObject).available());
      try
      {
        localArrayList.add(new X509CRLImpl((InputStream)localObject));
      }
      catch (CRLException localCRLException)
      {
        if (i != 0)
        {
          ((InputStream)localObject).reset();
          PKCS7 localPKCS7 = new PKCS7((InputStream)localObject);
          X509CRL[] arrayOfX509CRL = localPKCS7.getCRLs();
          if (arrayOfX509CRL != null)
            return Arrays.asList(arrayOfX509CRL);
          return new ArrayList(0);
        }
      }
    }
    return ((Collection)localArrayList);
  }

  private byte[] base64_to_binary(InputStream paramInputStream)
    throws IOException
  {
    String str;
    long l = 3412047291253522432L;
    paramInputStream.mark(paramInputStream.available());
    BufferedInputStream localBufferedInputStream = new BufferedInputStream(paramInputStream);
    BufferedReader localBufferedReader = new BufferedReader(new InputStreamReader(localBufferedInputStream, "ASCII"));
    if (((str = readLine(localBufferedReader)) == null) || (!(str.startsWith("-----BEGIN"))))
      throw new IOException("Unsupported encoding");
    l += str.length();
    StringBuffer localStringBuffer = new StringBuffer();
    while (((str = readLine(localBufferedReader)) != null) && (!(str.startsWith("-----END"))))
      localStringBuffer.append(str);
    if (str == null)
      throw new IOException("Unsupported encoding");
    l += str.length();
    l += localStringBuffer.length();
    paramInputStream.reset();
    paramInputStream.skip(l);
    BASE64Decoder localBASE64Decoder = new BASE64Decoder();
    return localBASE64Decoder.decodeBuffer(localStringBuffer.toString());
  }

  private byte[] getTotalBytes(InputStream paramInputStream)
    throws IOException
  {
    byte[] arrayOfByte = new byte[8192];
    ByteArrayOutputStream localByteArrayOutputStream = new ByteArrayOutputStream(2048);
    localByteArrayOutputStream.reset();
    while ((i = paramInputStream.read(arrayOfByte, 0, arrayOfByte.length)) != -1)
    {
      int i;
      localByteArrayOutputStream.write(arrayOfByte, 0, i);
    }
    return localByteArrayOutputStream.toByteArray();
  }

  private boolean isBase64(InputStream paramInputStream)
    throws IOException
  {
    if (paramInputStream.available() >= 10)
    {
      paramInputStream.mark(10);
      int i = paramInputStream.read();
      int j = paramInputStream.read();
      int k = paramInputStream.read();
      int l = paramInputStream.read();
      int i1 = paramInputStream.read();
      int i2 = paramInputStream.read();
      int i3 = paramInputStream.read();
      int i4 = paramInputStream.read();
      int i5 = paramInputStream.read();
      int i6 = paramInputStream.read();
      paramInputStream.reset();
      return ((i == 45) && (j == 45) && (k == 45) && (l == 45) && (i1 == 45) && (i2 == 66) && (i3 == 69) && (i4 == 71) && (i5 == 73) && (i6 == 78));
    }
    return false;
  }

  private String readLine(BufferedReader paramBufferedReader)
    throws IOException
  {
    int i;
    int j = 0;
    int k = 1;
    int l = 0;
    StringBuffer localStringBuffer = new StringBuffer(80);
    do
    {
      i = paramBufferedReader.read();
      if ((k != 0) && (j < endBoundary.length))
        k = ((char)i != endBoundary[(j++)]) ? 0 : 1;
      if (l == 0)
        l = ((k != 0) && (j == endBoundary.length)) ? 1 : 0;
      localStringBuffer.append((char)i);
    }
    while ((i != -1) && (i != 10) && (i != 13));
    if ((l == 0) && (i == -1))
      return null;
    if (i == 13)
    {
      paramBufferedReader.mark(1);
      int i1 = paramBufferedReader.read();
      if (i1 == 10)
        localStringBuffer.append((char)i);
      else
        paramBufferedReader.reset();
    }
    return localStringBuffer.toString();
  }
}