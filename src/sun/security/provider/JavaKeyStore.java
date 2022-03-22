package sun.security.provider;

import B;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.DigestInputStream;
import java.security.DigestOutputStream;
import java.security.Key;
import java.security.KeyStoreException;
import java.security.KeyStoreSpi;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import sun.security.pkcs.EncryptedPrivateKeyInfo;

abstract class JavaKeyStore extends KeyStoreSpi
{
  private static final int MAGIC = -17957139;
  private static final int VERSION_1 = 1;
  private static final int VERSION_2 = 2;
  private final Hashtable entries = new Hashtable();

  abstract String convertAlias(String paramString);

  public Key engineGetKey(String paramString, char[] paramArrayOfChar)
    throws NoSuchAlgorithmException, UnrecoverableKeyException
  {
    EncryptedPrivateKeyInfo localEncryptedPrivateKeyInfo;
    Object localObject = this.entries.get(convertAlias(paramString));
    if ((localObject == null) || (!(localObject instanceof KeyEntry)))
      return null;
    if (paramArrayOfChar == null)
      throw new UnrecoverableKeyException("Password must not be null");
    KeyProtector localKeyProtector = new KeyProtector(paramArrayOfChar);
    byte[] arrayOfByte = ((KeyEntry)localObject).protectedPrivKey;
    try
    {
      localEncryptedPrivateKeyInfo = new EncryptedPrivateKeyInfo(arrayOfByte);
    }
    catch (IOException localIOException)
    {
      throw new UnrecoverableKeyException("Private key not stored as PKCS #8 EncryptedPrivateKeyInfo");
    }
    return localKeyProtector.recover(localEncryptedPrivateKeyInfo);
  }

  public Certificate[] engineGetCertificateChain(String paramString)
  {
    Object localObject = this.entries.get(convertAlias(paramString));
    if ((localObject != null) && (localObject instanceof KeyEntry))
    {
      if (((KeyEntry)localObject).chain == null)
        return null;
      return ((Certificate[])(Certificate[])((KeyEntry)localObject).chain.clone());
    }
    return null;
  }

  public Certificate engineGetCertificate(String paramString)
  {
    Object localObject = this.entries.get(convertAlias(paramString));
    if (localObject != null)
    {
      if (localObject instanceof TrustedCertEntry)
        return ((TrustedCertEntry)localObject).cert;
      if (((KeyEntry)localObject).chain == null)
        return null;
      return ((KeyEntry)localObject).chain[0];
    }
    return null;
  }

  public Date engineGetCreationDate(String paramString)
  {
    Object localObject = this.entries.get(convertAlias(paramString));
    if (localObject != null)
    {
      if (localObject instanceof TrustedCertEntry)
        return new Date(((TrustedCertEntry)localObject).date.getTime());
      return new Date(((KeyEntry)localObject).date.getTime());
    }
    return null;
  }

  public void engineSetKeyEntry(String paramString, Key paramKey, char[] paramArrayOfChar, Certificate[] paramArrayOfCertificate)
    throws KeyStoreException
  {
    KeyProtector localKeyProtector = null;
    if (!(paramKey instanceof PrivateKey))
      throw new KeyStoreException("Cannot store non-PrivateKeys");
    try
    {
      synchronized (this.entries)
      {
        KeyEntry localKeyEntry = new KeyEntry(null);
        localKeyEntry.date = new Date();
        localKeyProtector = new KeyProtector(paramArrayOfChar);
        localKeyEntry.protectedPrivKey = localKeyProtector.protect(paramKey);
        if ((paramArrayOfCertificate != null) && (paramArrayOfCertificate.length != 0))
          localKeyEntry.chain = ((Certificate[])(Certificate[])paramArrayOfCertificate.clone());
        else
          localKeyEntry.chain = null;
        this.entries.put(convertAlias(paramString), localKeyEntry);
      }
    }
    catch (NoSuchAlgorithmException localNoSuchAlgorithmException)
    {
    }
    finally
    {
      localKeyProtector = null;
    }
  }

  public void engineSetKeyEntry(String paramString, byte[] paramArrayOfByte, Certificate[] paramArrayOfCertificate)
    throws KeyStoreException
  {
    synchronized (this.entries)
    {
      try
      {
        new EncryptedPrivateKeyInfo(paramArrayOfByte);
      }
      catch (IOException localIOException)
      {
        throw new KeyStoreException("key is not encoded as EncryptedPrivateKeyInfo");
      }
      KeyEntry localKeyEntry = new KeyEntry(null);
      localKeyEntry.date = new Date();
      localKeyEntry.protectedPrivKey = ((byte[])(byte[])paramArrayOfByte.clone());
      if ((paramArrayOfCertificate != null) && (paramArrayOfCertificate.length != 0))
        localKeyEntry.chain = ((Certificate[])(Certificate[])paramArrayOfCertificate.clone());
      else
        localKeyEntry.chain = null;
      this.entries.put(convertAlias(paramString), localKeyEntry);
    }
  }

  public void engineSetCertificateEntry(String paramString, Certificate paramCertificate)
    throws KeyStoreException
  {
    synchronized (this.entries)
    {
      Object localObject1 = this.entries.get(convertAlias(paramString));
      if ((localObject1 != null) && (localObject1 instanceof KeyEntry))
        throw new KeyStoreException("Cannot overwrite own certificate");
      TrustedCertEntry localTrustedCertEntry = new TrustedCertEntry(null);
      localTrustedCertEntry.cert = paramCertificate;
      localTrustedCertEntry.date = new Date();
      this.entries.put(convertAlias(paramString), localTrustedCertEntry);
    }
  }

  public void engineDeleteEntry(String paramString)
    throws KeyStoreException
  {
    synchronized (this.entries)
    {
      this.entries.remove(convertAlias(paramString));
    }
  }

  public Enumeration engineAliases()
  {
    return this.entries.keys();
  }

  public boolean engineContainsAlias(String paramString)
  {
    return this.entries.containsKey(convertAlias(paramString));
  }

  public int engineSize()
  {
    return this.entries.size();
  }

  public boolean engineIsKeyEntry(String paramString)
  {
    Object localObject = this.entries.get(convertAlias(paramString));
    return ((localObject != null) && (localObject instanceof KeyEntry));
  }

  public boolean engineIsCertificateEntry(String paramString)
  {
    Object localObject = this.entries.get(convertAlias(paramString));
    return ((localObject != null) && (localObject instanceof TrustedCertEntry));
  }

  public String engineGetCertificateAlias(Certificate paramCertificate)
  {
    Enumeration localEnumeration = this.entries.keys();
    while (true)
    {
      String str;
      Object localObject;
      do
      {
        if (!(localEnumeration.hasMoreElements()))
          break label95;
        str = (String)localEnumeration.nextElement();
        localObject = this.entries.get(str);
        if (localObject instanceof TrustedCertEntry)
        {
          localCertificate = ((TrustedCertEntry)localObject).cert;
          break label81:
        }
      }
      while (((KeyEntry)localObject).chain == null);
      Certificate localCertificate = ((KeyEntry)localObject).chain[0];
      if (localCertificate.equals(paramCertificate))
        label81: return str;
    }
    label95: return null;
  }

  public void engineStore(OutputStream paramOutputStream, char[] paramArrayOfChar)
    throws IOException, NoSuchAlgorithmException, CertificateException
  {
    synchronized (this.entries)
    {
      if (paramArrayOfChar == null)
        throw new IllegalArgumentException("password can't be null");
      MessageDigest localMessageDigest = getPreKeyedHash(paramArrayOfChar);
      DataOutputStream localDataOutputStream = new DataOutputStream(new DigestOutputStream(paramOutputStream, localMessageDigest));
      localDataOutputStream.writeInt(-17957139);
      localDataOutputStream.writeInt(2);
      localDataOutputStream.writeInt(this.entries.size());
      Object localObject1 = this.entries.keys();
      while (((Enumeration)localObject1).hasMoreElements())
      {
        byte[] arrayOfByte;
        String str = (String)((Enumeration)localObject1).nextElement();
        Object localObject2 = this.entries.get(str);
        if (localObject2 instanceof KeyEntry)
        {
          int i;
          localDataOutputStream.writeInt(1);
          localDataOutputStream.writeUTF(str);
          localDataOutputStream.writeLong(((KeyEntry)localObject2).date.getTime());
          localDataOutputStream.writeInt(((KeyEntry)localObject2).protectedPrivKey.length);
          localDataOutputStream.write(((KeyEntry)localObject2).protectedPrivKey);
          if (((KeyEntry)localObject2).chain == null)
            i = 0;
          else
            i = ((KeyEntry)localObject2).chain.length;
          localDataOutputStream.writeInt(i);
          for (int j = 0; j < i; ++j)
          {
            arrayOfByte = ((KeyEntry)localObject2).chain[j].getEncoded();
            localDataOutputStream.writeUTF(((KeyEntry)localObject2).chain[j].getType());
            localDataOutputStream.writeInt(arrayOfByte.length);
            localDataOutputStream.write(arrayOfByte);
          }
        }
        else
        {
          localDataOutputStream.writeInt(2);
          localDataOutputStream.writeUTF(str);
          localDataOutputStream.writeLong(((TrustedCertEntry)localObject2).date.getTime());
          arrayOfByte = ((TrustedCertEntry)localObject2).cert.getEncoded();
          localDataOutputStream.writeUTF(((TrustedCertEntry)localObject2).cert.getType());
          localDataOutputStream.writeInt(arrayOfByte.length);
          localDataOutputStream.write(arrayOfByte);
        }
      }
      localObject1 = localMessageDigest.digest();
      localDataOutputStream.write(localObject1);
      localDataOutputStream.flush();
    }
  }

  public void engineLoad(InputStream paramInputStream, char[] paramArrayOfChar)
    throws IOException, NoSuchAlgorithmException, CertificateException
  {
    synchronized (this.entries)
    {
      label29: label62: label72: label115: Object localObject1;
      MessageDigest localMessageDigest = null;
      CertificateFactory localCertificateFactory = null;
      Hashtable localHashtable2 = null;
      ByteArrayInputStream localByteArrayInputStream = null;
      byte[] arrayOfByte1 = null;
      if (paramInputStream != null)
        break label29;
      return;
      if (paramArrayOfChar == null)
        break label62;
      localMessageDigest = getPreKeyedHash(paramArrayOfChar);
      DataInputStream localDataInputStream = new DataInputStream(new DigestInputStream(paramInputStream, localMessageDigest));
      break label72:
      localDataInputStream = new DataInputStream(paramInputStream);
      int i = localDataInputStream.readInt();
      int j = localDataInputStream.readInt();
      if ((i == -17957139) && (((j == 1) || (j == 2))))
        break label115;
      throw new IOException("Invalid keystore format");
      if (j != 1)
        break label131;
      localCertificateFactory = CertificateFactory.getInstance("X509");
      break label141:
      label131: localHashtable2 = new Hashtable(3);
      label141: this.entries.clear();
      int k = localDataInputStream.readInt();
      for (int l = 0; l < k; ++l)
      {
        String str1;
        int i1 = localDataInputStream.readInt();
        if (i1 == 1)
        {
          localObject1 = new KeyEntry(null);
          str1 = localDataInputStream.readUTF();
          ((KeyEntry)localObject1).date = new Date(localDataInputStream.readLong());
          try
          {
            ((KeyEntry)localObject1).protectedPrivKey = new byte[localDataInputStream.readInt()];
          }
          catch (OutOfMemoryError localOutOfMemoryError1)
          {
            throw new IOException("Keysize too big");
          }
          localDataInputStream.readFully(((KeyEntry)localObject1).protectedPrivKey);
          int i3 = localDataInputStream.readInt();
          try
          {
            if (i3 > 0)
              ((KeyEntry)localObject1).chain = new Certificate[i3];
          }
          catch (OutOfMemoryError localOutOfMemoryError3)
          {
            throw new IOException("Too many certificates in chain");
          }
          for (int i4 = 0; i4 < i3; ++i4)
          {
            if (j == 2)
            {
              String str3 = localDataInputStream.readUTF();
              if (localHashtable2.containsKey(str3))
              {
                localCertificateFactory = (CertificateFactory)localHashtable2.get(str3);
              }
              else
              {
                localCertificateFactory = CertificateFactory.getInstance(str3);
                localHashtable2.put(str3, localCertificateFactory);
              }
            }
            try
            {
              arrayOfByte1 = new byte[localDataInputStream.readInt()];
            }
            catch (OutOfMemoryError localOutOfMemoryError4)
            {
              throw new IOException("Certificate too big");
            }
            localDataInputStream.readFully(arrayOfByte1);
            localByteArrayInputStream = new ByteArrayInputStream(arrayOfByte1);
            localObject1.chain[i4] = localCertificateFactory.generateCertificate(localByteArrayInputStream);
            localByteArrayInputStream.close();
          }
          this.entries.put(str1, localObject1);
        }
        else if (i1 == 2)
        {
          localObject1 = new TrustedCertEntry(null);
          str1 = localDataInputStream.readUTF();
          ((TrustedCertEntry)localObject1).date = new Date(localDataInputStream.readLong());
          if (j == 2)
          {
            String str2 = localDataInputStream.readUTF();
            if (localHashtable2.containsKey(str2))
            {
              localCertificateFactory = (CertificateFactory)localHashtable2.get(str2);
            }
            else
            {
              localCertificateFactory = CertificateFactory.getInstance(str2);
              localHashtable2.put(str2, localCertificateFactory);
            }
          }
          try
          {
            arrayOfByte1 = new byte[localDataInputStream.readInt()];
          }
          catch (OutOfMemoryError localOutOfMemoryError2)
          {
            throw new IOException("Certificate too big");
          }
          localDataInputStream.readFully(arrayOfByte1);
          localByteArrayInputStream = new ByteArrayInputStream(arrayOfByte1);
          ((TrustedCertEntry)localObject1).cert = localCertificateFactory.generateCertificate(localByteArrayInputStream);
          localByteArrayInputStream.close();
          this.entries.put(str1, localObject1);
        }
        else
        {
          throw new IOException("Unrecognized keystore entry");
        }
      }
      if (paramArrayOfChar == null)
        break label703;
      byte[] arrayOfByte2 = localMessageDigest.digest();
      byte[] arrayOfByte3 = new byte[arrayOfByte2.length];
      localDataInputStream.readFully(arrayOfByte3);
      int i2 = 0;
      while (true)
      {
        if (i2 >= arrayOfByte2.length)
          break label703;
        if (arrayOfByte2[i2] != arrayOfByte3[i2])
        {
          localObject1 = new UnrecoverableKeyException("Password verification failed");
          throw ((IOException)new IOException("Keystore was tampered with, or password was incorrect").initCause((Throwable)localObject1));
        }
        label703: ++i2;
      }
    }
  }

  private MessageDigest getPreKeyedHash(char[] paramArrayOfChar)
    throws NoSuchAlgorithmException, UnsupportedEncodingException
  {
    MessageDigest localMessageDigest = MessageDigest.getInstance("SHA");
    byte[] arrayOfByte = new byte[paramArrayOfChar.length * 2];
    int i = 0;
    int j = 0;
    while (i < paramArrayOfChar.length)
    {
      arrayOfByte[(j++)] = (byte)(paramArrayOfChar[i] >> '\b');
      arrayOfByte[(j++)] = (byte)paramArrayOfChar[i];
      ++i;
    }
    localMessageDigest.update(arrayOfByte);
    for (i = 0; i < arrayOfByte.length; ++i)
      arrayOfByte[i] = 0;
    localMessageDigest.update("Mighty Aphrodite".getBytes("UTF8"));
    return localMessageDigest;
  }

  public static final class CaseExactJKS extends JavaKeyStore
  {
    String convertAlias(String paramString)
    {
      return paramString;
    }
  }

  public static final class JKS extends JavaKeyStore
  {
    String convertAlias(String paramString)
    {
      return paramString.toLowerCase();
    }
  }

  private static class KeyEntry
  {
    Date date;
    byte[] protectedPrivKey;
    Certificate[] chain;
  }

  private static class TrustedCertEntry
  {
    Date date;
    Certificate cert;
  }
}