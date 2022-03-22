package sun.security.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import sun.net.www.ParseUtil;

public class PolicyUtil
{
  private static final String P11KEYSTORE = "PKCS11";
  private static final String NONE = "NONE";

  public static InputStream getInputStream(URL paramURL)
    throws IOException
  {
    if ("file".equals(paramURL.getProtocol()))
    {
      String str = paramURL.getFile().replace('/', File.separatorChar);
      str = ParseUtil.decode(str);
      return new FileInputStream(str);
    }
    return paramURL.openStream();
  }

  public static KeyStore getKeyStore(URL paramURL, String paramString1, String paramString2, String paramString3, String paramString4, Debug paramDebug)
    throws KeyStoreException, MalformedURLException, IOException, NoSuchProviderException, NoSuchAlgorithmException, CertificateException
  {
    if (paramString1 == null)
      throw new IllegalArgumentException("null KeyStore name");
    char[] arrayOfChar = null;
    try
    {
      KeyStore localKeyStore1;
      if (paramString2 == null)
        paramString2 = KeyStore.getDefaultType();
      if (("PKCS11".equalsIgnoreCase(paramString2)) && (!("NONE".equals(paramString1))))
        throw new IllegalArgumentException("Invalid value (" + paramString1 + ") for keystore URL.  If the keystore type is \"" + "PKCS11" + "\", the keystore url must be \"" + "NONE" + "\"");
      if (paramString3 != null)
        localKeyStore1 = KeyStore.getInstance(paramString2, paramString3);
      else
        localKeyStore1 = KeyStore.getInstance(paramString2);
      if (paramString4 != null)
      {
        try
        {
          localObject1 = new URL(paramString4);
        }
        catch (MalformedURLException localMalformedURLException1)
        {
          if (paramURL == null)
            throw localMalformedURLException1;
          localObject1 = new URL(paramURL, paramString4);
        }
        if (paramDebug != null)
          paramDebug.println("reading password" + localObject1);
        InputStream localInputStream = ((URL)localObject1).openStream();
        arrayOfChar = Password.readPassword(localInputStream);
        localInputStream.close();
      }
      if ("NONE".equals(paramString1))
      {
        localKeyStore1.load(null, arrayOfChar);
        localObject1 = localKeyStore1;
        jsr 118;
        return localObject1;
      }
      Object localObject1 = null;
      try
      {
        localObject1 = new URL(paramString1);
      }
      catch (MalformedURLException localMalformedURLException2)
      {
        if (paramURL == null)
          throw localMalformedURLException2;
        localObject1 = new URL(paramURL, paramString1);
      }
      if (paramDebug != null)
        paramDebug.println("reading keystore" + localObject1);
      BufferedInputStream localBufferedInputStream = new BufferedInputStream(getInputStream((URL)localObject1));
      localKeyStore1.load(localBufferedInputStream, arrayOfChar);
      localBufferedInputStream.close();
      KeyStore localKeyStore2 = localKeyStore1;
      return localKeyStore2;
    }
    finally
    {
      if (arrayOfChar != null)
        Arrays.fill(arrayOfChar, ' ');
    }
  }
}