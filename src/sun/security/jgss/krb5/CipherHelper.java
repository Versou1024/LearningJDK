package sun.security.jgss.krb5;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.ietf.jgss.GSSException;
import sun.security.krb5.EncryptionKey;
import sun.security.krb5.internal.crypto.Aes128;
import sun.security.krb5.internal.crypto.Aes256;
import sun.security.krb5.internal.crypto.ArcFourHmac;
import sun.security.krb5.internal.crypto.Des3;

class CipherHelper
{
  private static final int KG_USAGE_SEAL = 22;
  private static final int KG_USAGE_SIGN = 23;
  private static final int KG_USAGE_SEQ = 24;
  private static final int DES_CHECKSUM_SIZE = 8;
  private static final int DES_IV_SIZE = 8;
  private static final int AES_IV_SIZE = 16;
  private static final int HMAC_CHECKSUM_SIZE = 8;
  private static final int KG_USAGE_SIGN_MS = 15;
  private static final boolean DEBUG = Krb5Util.DEBUG;
  private static final byte[] ZERO_IV = new byte[8];
  private static final byte[] ZERO_IV_AES = new byte[16];
  private int etype;
  private int sgnAlg;
  private int sealAlg;
  private byte[] keybytes;
  private int proto = 0;

  CipherHelper(EncryptionKey paramEncryptionKey)
    throws GSSException
  {
    this.etype = paramEncryptionKey.getEType();
    this.keybytes = paramEncryptionKey.getBytes();
    switch (this.etype)
    {
    case 1:
    case 3:
      this.sgnAlg = 0;
      this.sealAlg = 0;
      break;
    case 16:
      this.sgnAlg = 1024;
      this.sealAlg = 512;
      break;
    case 23:
      this.sgnAlg = 4352;
      this.sealAlg = 4096;
      break;
    case 17:
    case 18:
      this.sgnAlg = -1;
      this.sealAlg = -1;
      this.proto = 1;
      break;
    default:
      throw new GSSException(11, -1, "Unsupported encryption type: " + this.etype);
    }
  }

  int getSgnAlg()
  {
    return this.sgnAlg;
  }

  int getSealAlg()
  {
    return this.sealAlg;
  }

  int getProto()
  {
    return this.proto;
  }

  int getEType()
  {
    return this.etype;
  }

  boolean isArcFour()
  {
    int i = 0;
    if (this.etype == 23)
      i = 1;
    return i;
  }

  byte[] calculateChecksum(int paramInt1, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, byte[] paramArrayOfByte3, int paramInt2, int paramInt3, int paramInt4)
    throws GSSException
  {
    switch (paramInt1)
    {
    case 0:
      try
      {
        MessageDigest localMessageDigest = MessageDigest.getInstance("MD5");
        localMessageDigest.update(paramArrayOfByte1);
        localMessageDigest.update(paramArrayOfByte3, paramInt2, paramInt3);
        if (paramArrayOfByte2 != null)
          localMessageDigest.update(paramArrayOfByte2);
        paramArrayOfByte3 = localMessageDigest.digest();
        paramInt2 = 0;
        paramInt3 = paramArrayOfByte3.length;
        paramArrayOfByte1 = null;
        paramArrayOfByte2 = null;
      }
      catch (NoSuchAlgorithmException localNoSuchAlgorithmException)
      {
        GSSException localGSSException1 = new GSSException(11, -1, "Could not get MD5 Message Digest - " + localNoSuchAlgorithmException.getMessage());
        localGSSException1.initCause(localNoSuchAlgorithmException);
        throw localGSSException1;
      }
    case 512:
      return getDesCbcChecksum(this.keybytes, paramArrayOfByte1, paramArrayOfByte3, paramInt2, paramInt3);
    case 1024:
      byte[] arrayOfByte1;
      int i;
      int j;
      if ((paramArrayOfByte1 == null) && (paramArrayOfByte2 == null))
      {
        arrayOfByte1 = paramArrayOfByte3;
        j = paramInt3;
        i = paramInt2;
      }
      else
      {
        j = ((paramArrayOfByte1 != null) ? paramArrayOfByte1.length : 0) + paramInt3 + ((paramArrayOfByte2 != null) ? paramArrayOfByte2.length : 0);
        arrayOfByte1 = new byte[j];
        int k = 0;
        if (paramArrayOfByte1 != null)
        {
          System.arraycopy(paramArrayOfByte1, 0, arrayOfByte1, 0, paramArrayOfByte1.length);
          k = paramArrayOfByte1.length;
        }
        System.arraycopy(paramArrayOfByte3, paramInt2, arrayOfByte1, k, paramInt3);
        k += paramInt3;
        if (paramArrayOfByte2 != null)
          System.arraycopy(paramArrayOfByte2, 0, arrayOfByte1, k, paramArrayOfByte2.length);
        i = 0;
      }
      try
      {
        byte[] arrayOfByte2 = Des3.calculateChecksum(this.keybytes, 23, arrayOfByte1, i, j);
        return arrayOfByte2;
      }
      catch (GeneralSecurityException localGeneralSecurityException1)
      {
        GSSException localGSSException2 = new GSSException(11, -1, "Could not use HMAC-SHA1-DES3-KD signing algorithm - " + localGeneralSecurityException1.getMessage());
        localGSSException2.initCause(localGeneralSecurityException1);
        throw localGSSException2;
      }
    case 4352:
      byte[] arrayOfByte3;
      int l;
      int i1;
      Object localObject;
      if ((paramArrayOfByte1 == null) && (paramArrayOfByte2 == null))
      {
        arrayOfByte3 = paramArrayOfByte3;
        i1 = paramInt3;
        l = paramInt2;
      }
      else
      {
        i1 = ((paramArrayOfByte1 != null) ? paramArrayOfByte1.length : 0) + paramInt3 + ((paramArrayOfByte2 != null) ? paramArrayOfByte2.length : 0);
        arrayOfByte3 = new byte[i1];
        int i2 = 0;
        if (paramArrayOfByte1 != null)
        {
          System.arraycopy(paramArrayOfByte1, 0, arrayOfByte3, 0, paramArrayOfByte1.length);
          i2 = paramArrayOfByte1.length;
        }
        System.arraycopy(paramArrayOfByte3, paramInt2, arrayOfByte3, i2, paramInt3);
        i2 += paramInt3;
        if (paramArrayOfByte2 != null)
          System.arraycopy(paramArrayOfByte2, 0, arrayOfByte3, i2, paramArrayOfByte2.length);
        l = 0;
      }
      try
      {
        int i3 = 23;
        if (paramInt4 == 257)
          i3 = 15;
        localObject = ArcFourHmac.calculateChecksum(this.keybytes, i3, arrayOfByte3, l, i1);
        byte[] arrayOfByte4 = new byte[getChecksumLength()];
        System.arraycopy(localObject, 0, arrayOfByte4, 0, arrayOfByte4.length);
        return arrayOfByte4;
      }
      catch (GeneralSecurityException localGeneralSecurityException2)
      {
        localObject = new GSSException(11, -1, "Could not use HMAC_MD5_ARCFOUR signing algorithm - " + localGeneralSecurityException2.getMessage());
        ((GSSException)localObject).initCause(localGeneralSecurityException2);
        throw ((Throwable)localObject);
      }
    }
    throw new GSSException(11, -1, "Unsupported signing algorithm: " + this.sgnAlg);
  }

  byte[] calculateChecksum(byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, int paramInt1, int paramInt2, int paramInt3)
    throws GSSException
  {
    GSSException localGSSException;
    int i = ((paramArrayOfByte1 != null) ? paramArrayOfByte1.length : 0) + paramInt2;
    byte[] arrayOfByte1 = new byte[i];
    System.arraycopy(paramArrayOfByte2, paramInt1, arrayOfByte1, 0, paramInt2);
    if (paramArrayOfByte1 != null)
      System.arraycopy(paramArrayOfByte1, 0, arrayOfByte1, paramInt2, paramArrayOfByte1.length);
    switch (this.etype)
    {
    case 17:
      try
      {
        byte[] arrayOfByte2 = Aes128.calculateChecksum(this.keybytes, paramInt3, arrayOfByte1, 0, i);
        return arrayOfByte2;
      }
      catch (GeneralSecurityException localGeneralSecurityException1)
      {
        localGSSException = new GSSException(11, -1, "Could not use AES128 signing algorithm - " + localGeneralSecurityException1.getMessage());
        localGSSException.initCause(localGeneralSecurityException1);
        throw localGSSException;
      }
    case 18:
      try
      {
        byte[] arrayOfByte3 = Aes256.calculateChecksum(this.keybytes, paramInt3, arrayOfByte1, 0, i);
        return arrayOfByte3;
      }
      catch (GeneralSecurityException localGeneralSecurityException2)
      {
        localGSSException = new GSSException(11, -1, "Could not use AES256 signing algorithm - " + localGeneralSecurityException2.getMessage());
        localGSSException.initCause(localGeneralSecurityException2);
        throw localGSSException;
      }
    }
    throw new GSSException(11, -1, "Unsupported encryption type: " + this.etype);
  }

  byte[] encryptSeq(byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, int paramInt1, int paramInt2)
    throws GSSException
  {
    switch (this.sgnAlg)
    {
    case 0:
    case 512:
      try
      {
        Cipher localCipher = getInitializedDes(true, this.keybytes, paramArrayOfByte1);
        return localCipher.doFinal(paramArrayOfByte2, paramInt1, paramInt2);
      }
      catch (GeneralSecurityException localGeneralSecurityException)
      {
        GSSException localGSSException1 = new GSSException(11, -1, "Could not encrypt sequence number using DES - " + localGeneralSecurityException.getMessage());
        localGSSException1.initCause(localGeneralSecurityException);
        throw localGSSException1;
      }
    case 1024:
      byte[] arrayOfByte1;
      if (paramArrayOfByte1.length == 8)
      {
        arrayOfByte1 = paramArrayOfByte1;
      }
      else
      {
        arrayOfByte1 = new byte[8];
        System.arraycopy(paramArrayOfByte1, 0, arrayOfByte1, 0, 8);
      }
      try
      {
        return Des3.encryptRaw(this.keybytes, 24, arrayOfByte1, paramArrayOfByte2, paramInt1, paramInt2);
      }
      catch (Exception localException1)
      {
        GSSException localGSSException2 = new GSSException(11, -1, "Could not encrypt sequence number using DES3-KD - " + localException1.getMessage());
        localGSSException2.initCause(localException1);
        throw localGSSException2;
      }
    case 4352:
      byte[] arrayOfByte2;
      if (paramArrayOfByte1.length == 8)
      {
        arrayOfByte2 = paramArrayOfByte1;
      }
      else
      {
        arrayOfByte2 = new byte[8];
        System.arraycopy(paramArrayOfByte1, 0, arrayOfByte2, 0, 8);
      }
      try
      {
        return ArcFourHmac.encryptSeq(this.keybytes, 24, arrayOfByte2, paramArrayOfByte2, paramInt1, paramInt2);
      }
      catch (Exception localException2)
      {
        GSSException localGSSException3 = new GSSException(11, -1, "Could not encrypt sequence number using RC4-HMAC - " + localException2.getMessage());
        localGSSException3.initCause(localException2);
        throw localGSSException3;
      }
    }
    throw new GSSException(11, -1, "Unsupported signing algorithm: " + this.sgnAlg);
  }

  byte[] decryptSeq(byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, int paramInt1, int paramInt2)
    throws GSSException
  {
    switch (this.sgnAlg)
    {
    case 0:
    case 512:
      try
      {
        Cipher localCipher = getInitializedDes(false, this.keybytes, paramArrayOfByte1);
        return localCipher.doFinal(paramArrayOfByte2, paramInt1, paramInt2);
      }
      catch (GeneralSecurityException localGeneralSecurityException)
      {
        GSSException localGSSException1 = new GSSException(11, -1, "Could not decrypt sequence number using DES - " + localGeneralSecurityException.getMessage());
        localGSSException1.initCause(localGeneralSecurityException);
        throw localGSSException1;
      }
    case 1024:
      byte[] arrayOfByte1;
      if (paramArrayOfByte1.length == 8)
      {
        arrayOfByte1 = paramArrayOfByte1;
      }
      else
      {
        arrayOfByte1 = new byte[8];
        System.arraycopy(paramArrayOfByte1, 0, arrayOfByte1, 0, 8);
      }
      try
      {
        return Des3.decryptRaw(this.keybytes, 24, arrayOfByte1, paramArrayOfByte2, paramInt1, paramInt2);
      }
      catch (Exception localException1)
      {
        GSSException localGSSException2 = new GSSException(11, -1, "Could not decrypt sequence number using DES3-KD - " + localException1.getMessage());
        localGSSException2.initCause(localException1);
        throw localGSSException2;
      }
    case 4352:
      byte[] arrayOfByte2;
      if (paramArrayOfByte1.length == 8)
      {
        arrayOfByte2 = paramArrayOfByte1;
      }
      else
      {
        arrayOfByte2 = new byte[8];
        System.arraycopy(paramArrayOfByte1, 0, arrayOfByte2, 0, 8);
      }
      try
      {
        return ArcFourHmac.decryptSeq(this.keybytes, 24, arrayOfByte2, paramArrayOfByte2, paramInt1, paramInt2);
      }
      catch (Exception localException2)
      {
        GSSException localGSSException3 = new GSSException(11, -1, "Could not decrypt sequence number using RC4-HMAC - " + localException2.getMessage());
        localGSSException3.initCause(localException2);
        throw localGSSException3;
      }
    }
    throw new GSSException(11, -1, "Unsupported signing algorithm: " + this.sgnAlg);
  }

  int getChecksumLength()
    throws GSSException
  {
    switch (this.etype)
    {
    case 1:
    case 3:
      return 8;
    case 16:
      return Des3.getChecksumLength();
    case 17:
      return Aes128.getChecksumLength();
    case 18:
      return Aes256.getChecksumLength();
    case 23:
      return 8;
    }
    throw new GSSException(11, -1, "Unsupported encryption type: " + this.etype);
  }

  void decryptData(WrapToken paramWrapToken, byte[] paramArrayOfByte1, int paramInt1, int paramInt2, byte[] paramArrayOfByte2, int paramInt3)
    throws GSSException
  {
    switch (this.sealAlg)
    {
    case 0:
      desCbcDecrypt(paramWrapToken, getDesEncryptionKey(this.keybytes), paramArrayOfByte1, paramInt1, paramInt2, paramArrayOfByte2, paramInt3);
      break;
    case 512:
      des3KdDecrypt(paramWrapToken, paramArrayOfByte1, paramInt1, paramInt2, paramArrayOfByte2, paramInt3);
      break;
    case 4096:
      arcFourDecrypt(paramWrapToken, paramArrayOfByte1, paramInt1, paramInt2, paramArrayOfByte2, paramInt3);
      break;
    default:
      throw new GSSException(11, -1, "Unsupported seal algorithm: " + this.sealAlg);
    }
  }

  void decryptData(WrapToken_v2 paramWrapToken_v2, byte[] paramArrayOfByte1, int paramInt1, int paramInt2, byte[] paramArrayOfByte2, int paramInt3, int paramInt4)
    throws GSSException
  {
    switch (this.etype)
    {
    case 17:
      aes128Decrypt(paramWrapToken_v2, paramArrayOfByte1, paramInt1, paramInt2, paramArrayOfByte2, paramInt3, paramInt4);
      break;
    case 18:
      aes256Decrypt(paramWrapToken_v2, paramArrayOfByte1, paramInt1, paramInt2, paramArrayOfByte2, paramInt3, paramInt4);
      break;
    default:
      throw new GSSException(11, -1, "Unsupported etype: " + this.etype);
    }
  }

  void decryptData(WrapToken paramWrapToken, InputStream paramInputStream, int paramInt1, byte[] paramArrayOfByte, int paramInt2)
    throws GSSException, IOException
  {
    switch (this.sealAlg)
    {
    case 0:
      desCbcDecrypt(paramWrapToken, getDesEncryptionKey(this.keybytes), paramInputStream, paramInt1, paramArrayOfByte, paramInt2);
      break;
    case 512:
      byte[] arrayOfByte1 = new byte[paramInt1];
      try
      {
        Krb5Token.readFully(paramInputStream, arrayOfByte1, 0, paramInt1);
      }
      catch (IOException localIOException1)
      {
        GSSException localGSSException1 = new GSSException(10, -1, "Cannot read complete token");
        localGSSException1.initCause(localIOException1);
        throw localGSSException1;
      }
      des3KdDecrypt(paramWrapToken, arrayOfByte1, 0, paramInt1, paramArrayOfByte, paramInt2);
      break;
    case 4096:
      byte[] arrayOfByte2 = new byte[paramInt1];
      try
      {
        Krb5Token.readFully(paramInputStream, arrayOfByte2, 0, paramInt1);
      }
      catch (IOException localIOException2)
      {
        GSSException localGSSException2 = new GSSException(10, -1, "Cannot read complete token");
        localGSSException2.initCause(localIOException2);
        throw localGSSException2;
      }
      arcFourDecrypt(paramWrapToken, arrayOfByte2, 0, paramInt1, paramArrayOfByte, paramInt2);
      break;
    default:
      throw new GSSException(11, -1, "Unsupported seal algorithm: " + this.sealAlg);
    }
  }

  void decryptData(WrapToken_v2 paramWrapToken_v2, InputStream paramInputStream, int paramInt1, byte[] paramArrayOfByte, int paramInt2, int paramInt3)
    throws GSSException, IOException
  {
    byte[] arrayOfByte = new byte[paramInt1];
    try
    {
      Krb5Token.readFully(paramInputStream, arrayOfByte, 0, paramInt1);
    }
    catch (IOException localIOException)
    {
      GSSException localGSSException = new GSSException(10, -1, "Cannot read complete token");
      localGSSException.initCause(localIOException);
      throw localGSSException;
    }
    switch (this.etype)
    {
    case 17:
      aes128Decrypt(paramWrapToken_v2, arrayOfByte, 0, paramInt1, paramArrayOfByte, paramInt2, paramInt3);
      break;
    case 18:
      aes256Decrypt(paramWrapToken_v2, arrayOfByte, 0, paramInt1, paramArrayOfByte, paramInt2, paramInt3);
      break;
    default:
      throw new GSSException(11, -1, "Unsupported etype: " + this.etype);
    }
  }

  void encryptData(WrapToken paramWrapToken, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, int paramInt1, int paramInt2, byte[] paramArrayOfByte3, OutputStream paramOutputStream)
    throws GSSException, IOException
  {
    switch (this.sealAlg)
    {
    case 0:
      Cipher localCipher = getInitializedDes(true, getDesEncryptionKey(this.keybytes), ZERO_IV);
      CipherOutputStream localCipherOutputStream = new CipherOutputStream(paramOutputStream, localCipher);
      localCipherOutputStream.write(paramArrayOfByte1);
      localCipherOutputStream.write(paramArrayOfByte2, paramInt1, paramInt2);
      localCipherOutputStream.write(paramArrayOfByte3);
      break;
    case 512:
      byte[] arrayOfByte1 = des3KdEncrypt(paramArrayOfByte1, paramArrayOfByte2, paramInt1, paramInt2, paramArrayOfByte3);
      paramOutputStream.write(arrayOfByte1);
      break;
    case 4096:
      byte[] arrayOfByte2 = arcFourEncrypt(paramWrapToken, paramArrayOfByte1, paramArrayOfByte2, paramInt1, paramInt2, paramArrayOfByte3);
      paramOutputStream.write(arrayOfByte2);
      break;
    default:
      throw new GSSException(11, -1, "Unsupported seal algorithm: " + this.sealAlg);
    }
  }

  void encryptData(WrapToken_v2 paramWrapToken_v2, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, byte[] paramArrayOfByte3, int paramInt1, int paramInt2, int paramInt3, OutputStream paramOutputStream)
    throws GSSException, IOException
  {
    byte[] arrayOfByte = null;
    switch (this.etype)
    {
    case 17:
      arrayOfByte = aes128Encrypt(paramArrayOfByte1, paramArrayOfByte2, paramArrayOfByte3, paramInt1, paramInt2, paramInt3);
      break;
    case 18:
      arrayOfByte = aes256Encrypt(paramArrayOfByte1, paramArrayOfByte2, paramArrayOfByte3, paramInt1, paramInt2, paramInt3);
      break;
    default:
      throw new GSSException(11, -1, "Unsupported etype: " + this.etype);
    }
    paramOutputStream.write(arrayOfByte);
  }

  void encryptData(WrapToken paramWrapToken, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, int paramInt1, int paramInt2, byte[] paramArrayOfByte3, byte[] paramArrayOfByte4, int paramInt3)
    throws GSSException
  {
    Object localObject;
    switch (this.sealAlg)
    {
    case 0:
      int i = paramInt3;
      Cipher localCipher = getInitializedDes(true, getDesEncryptionKey(this.keybytes), ZERO_IV);
      try
      {
        i += localCipher.update(paramArrayOfByte1, 0, paramArrayOfByte1.length, paramArrayOfByte4, i);
        i += localCipher.update(paramArrayOfByte2, paramInt1, paramInt2, paramArrayOfByte4, i);
        localCipher.update(paramArrayOfByte3, 0, paramArrayOfByte3.length, paramArrayOfByte4, i);
        localCipher.doFinal();
      }
      catch (GeneralSecurityException localGeneralSecurityException)
      {
        localObject = new GSSException(11, -1, "Could not use DES Cipher - " + localGeneralSecurityException.getMessage());
        ((GSSException)localObject).initCause(localGeneralSecurityException);
        throw ((Throwable)localObject);
      }
    case 512:
      byte[] arrayOfByte = des3KdEncrypt(paramArrayOfByte1, paramArrayOfByte2, paramInt1, paramInt2, paramArrayOfByte3);
      System.arraycopy(arrayOfByte, 0, paramArrayOfByte4, paramInt3, arrayOfByte.length);
      break;
    case 4096:
      localObject = arcFourEncrypt(paramWrapToken, paramArrayOfByte1, paramArrayOfByte2, paramInt1, paramInt2, paramArrayOfByte3);
      System.arraycopy(localObject, 0, paramArrayOfByte4, paramInt3, localObject.length);
      break;
    default:
      throw new GSSException(11, -1, "Unsupported seal algorithm: " + this.sealAlg);
    }
  }

  int encryptData(WrapToken_v2 paramWrapToken_v2, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, byte[] paramArrayOfByte3, int paramInt1, int paramInt2, byte[] paramArrayOfByte4, int paramInt3, int paramInt4)
    throws GSSException
  {
    byte[] arrayOfByte = null;
    switch (this.etype)
    {
    case 17:
      arrayOfByte = aes128Encrypt(paramArrayOfByte1, paramArrayOfByte2, paramArrayOfByte3, paramInt1, paramInt2, paramInt4);
      break;
    case 18:
      arrayOfByte = aes256Encrypt(paramArrayOfByte1, paramArrayOfByte2, paramArrayOfByte3, paramInt1, paramInt2, paramInt4);
      break;
    default:
      throw new GSSException(11, -1, "Unsupported etype: " + this.etype);
    }
    System.arraycopy(arrayOfByte, 0, paramArrayOfByte4, paramInt3, arrayOfByte.length);
    return arrayOfByte.length;
  }

  private byte[] getDesCbcChecksum(byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, byte[] paramArrayOfByte3, int paramInt1, int paramInt2)
    throws GSSException
  {
    Object localObject;
    Cipher localCipher = getInitializedDes(true, paramArrayOfByte1, ZERO_IV);
    int i = localCipher.getBlockSize();
    byte[] arrayOfByte1 = new byte[i];
    int j = paramInt2 / i;
    int k = paramInt2 % i;
    if (k == 0)
      System.arraycopy(paramArrayOfByte3, paramInt1 + --j * i, arrayOfByte1, 0, i);
    else
      System.arraycopy(paramArrayOfByte3, paramInt1 + j * i, arrayOfByte1, 0, k);
    try
    {
      byte[] arrayOfByte2 = new byte[Math.max(i, (paramArrayOfByte2 == null) ? i : paramArrayOfByte2.length)];
      if (paramArrayOfByte2 != null)
        localCipher.update(paramArrayOfByte2, 0, paramArrayOfByte2.length, arrayOfByte2, 0);
      for (int l = 0; l < j; ++l)
      {
        localCipher.update(paramArrayOfByte3, paramInt1, i, arrayOfByte2, 0);
        paramInt1 += i;
      }
      localObject = new byte[i];
      localCipher.update(arrayOfByte1, 0, i, localObject, 0);
      localCipher.doFinal();
      return localObject;
    }
    catch (GeneralSecurityException localGeneralSecurityException)
    {
      localObject = new GSSException(11, -1, "Could not use DES Cipher - " + localGeneralSecurityException.getMessage());
      ((GSSException)localObject).initCause(localGeneralSecurityException);
      throw ((Throwable)localObject);
    }
  }

  private final Cipher getInitializedDes(boolean paramBoolean, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2)
    throws GSSException
  {
    IvParameterSpec localIvParameterSpec;
    Object localObject;
    try
    {
      localIvParameterSpec = new IvParameterSpec(paramArrayOfByte2);
      localObject = new SecretKeySpec(paramArrayOfByte1, "DES");
      Cipher localCipher = Cipher.getInstance("DES/CBC/NoPadding");
      localCipher.init((paramBoolean) ? 1 : 2, (Key)localObject, localIvParameterSpec);
      return localCipher;
    }
    catch (GeneralSecurityException localGeneralSecurityException)
    {
      localObject = new GSSException(11, -1, localGeneralSecurityException.getMessage());
      ((GSSException)localObject).initCause(localGeneralSecurityException);
      throw ((Throwable)localObject);
    }
  }

  private void desCbcDecrypt(WrapToken paramWrapToken, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, int paramInt1, int paramInt2, byte[] paramArrayOfByte3, int paramInt3)
    throws GSSException
  {
    int i;
    Object localObject;
    try
    {
      i = 0;
      localObject = getInitializedDes(false, paramArrayOfByte1, ZERO_IV);
      i = ((Cipher)localObject).update(paramArrayOfByte2, paramInt1, 8, paramWrapToken.confounder);
      paramInt1 += 8;
      paramInt2 -= 8;
      int j = ((Cipher)localObject).getBlockSize();
      int k = paramInt2 / j - 1;
      for (int l = 0; l < k; ++l)
      {
        i = ((Cipher)localObject).update(paramArrayOfByte2, paramInt1, j, paramArrayOfByte3, paramInt3);
        paramInt1 += j;
        paramInt3 += j;
      }
      byte[] arrayOfByte = new byte[j];
      ((Cipher)localObject).update(paramArrayOfByte2, paramInt1, j, arrayOfByte);
      ((Cipher)localObject).doFinal();
      int i1 = arrayOfByte[(j - 1)];
      if ((i1 < 1) || (i1 > 8))
        throw new GSSException(10, -1, "Invalid padding on Wrap Token");
      paramWrapToken.padding = WrapToken.pads[i1];
      j -= i1;
      System.arraycopy(arrayOfByte, 0, paramArrayOfByte3, paramInt3, j);
    }
    catch (GeneralSecurityException localGeneralSecurityException)
    {
      localObject = new GSSException(11, -1, "Could not use DES cipher - " + localGeneralSecurityException.getMessage());
      ((GSSException)localObject).initCause(localGeneralSecurityException);
      throw ((Throwable)localObject);
    }
  }

  private void desCbcDecrypt(WrapToken paramWrapToken, byte[] paramArrayOfByte1, InputStream paramInputStream, int paramInt1, byte[] paramArrayOfByte2, int paramInt2)
    throws GSSException, IOException
  {
    int i = 0;
    Cipher localCipher = getInitializedDes(false, paramArrayOfByte1, ZERO_IV);
    WrapTokenInputStream localWrapTokenInputStream = new WrapTokenInputStream(this, paramInputStream, paramInt1);
    CipherInputStream localCipherInputStream = new CipherInputStream(localWrapTokenInputStream, localCipher);
    i = localCipherInputStream.read(paramWrapToken.confounder);
    paramInt1 -= i;
    int j = localCipher.getBlockSize();
    int k = paramInt1 / j - 1;
    for (int l = 0; l < k; ++l)
    {
      i = localCipherInputStream.read(paramArrayOfByte2, paramInt2, j);
      paramInt2 += j;
    }
    byte[] arrayOfByte = new byte[j];
    i = localCipherInputStream.read(arrayOfByte);
    try
    {
      localCipher.doFinal();
    }
    catch (GeneralSecurityException localGeneralSecurityException)
    {
      GSSException localGSSException = new GSSException(11, -1, "Could not use DES cipher - " + localGeneralSecurityException.getMessage());
      localGSSException.initCause(localGeneralSecurityException);
      throw localGSSException;
    }
    int i1 = arrayOfByte[(j - 1)];
    if ((i1 < 1) || (i1 > 8))
      throw new GSSException(10, -1, "Invalid padding on Wrap Token");
    paramWrapToken.padding = WrapToken.pads[i1];
    j -= i1;
    System.arraycopy(arrayOfByte, 0, paramArrayOfByte2, paramInt2, j);
  }

  private static byte[] getDesEncryptionKey(byte[] paramArrayOfByte)
    throws GSSException
  {
    if (paramArrayOfByte.length > 8)
      throw new GSSException(11, -100, "Invalid DES Key!");
    byte[] arrayOfByte = new byte[paramArrayOfByte.length];
    for (int i = 0; i < paramArrayOfByte.length; ++i)
      arrayOfByte[i] = (byte)(paramArrayOfByte[i] ^ 0xF0);
    return arrayOfByte;
  }

  private void des3KdDecrypt(WrapToken paramWrapToken, byte[] paramArrayOfByte1, int paramInt1, int paramInt2, byte[] paramArrayOfByte2, int paramInt3)
    throws GSSException
  {
    byte[] arrayOfByte;
    try
    {
      arrayOfByte = Des3.decryptRaw(this.keybytes, 22, ZERO_IV, paramArrayOfByte1, paramInt1, paramInt2);
    }
    catch (GeneralSecurityException localGeneralSecurityException)
    {
      GSSException localGSSException = new GSSException(11, -1, "Could not use DES3-KD Cipher - " + localGeneralSecurityException.getMessage());
      localGSSException.initCause(localGeneralSecurityException);
      throw localGSSException;
    }
    int i = arrayOfByte[(arrayOfByte.length - 1)];
    if ((i < 1) || (i > 8))
      throw new GSSException(10, -1, "Invalid padding on Wrap Token");
    paramWrapToken.padding = WrapToken.pads[i];
    int j = arrayOfByte.length - 8 - i;
    System.arraycopy(arrayOfByte, 8, paramArrayOfByte2, paramInt3, j);
    System.arraycopy(arrayOfByte, 0, paramWrapToken.confounder, 0, 8);
  }

  private byte[] des3KdEncrypt(byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, int paramInt1, int paramInt2, byte[] paramArrayOfByte3)
    throws GSSException
  {
    byte[] arrayOfByte1 = new byte[paramArrayOfByte1.length + paramInt2 + paramArrayOfByte3.length];
    System.arraycopy(paramArrayOfByte1, 0, arrayOfByte1, 0, paramArrayOfByte1.length);
    System.arraycopy(paramArrayOfByte2, paramInt1, arrayOfByte1, paramArrayOfByte1.length, paramInt2);
    System.arraycopy(paramArrayOfByte3, 0, arrayOfByte1, paramArrayOfByte1.length + paramInt2, paramArrayOfByte3.length);
    try
    {
      byte[] arrayOfByte2 = Des3.encryptRaw(this.keybytes, 22, ZERO_IV, arrayOfByte1, 0, arrayOfByte1.length);
      return arrayOfByte2;
    }
    catch (Exception localException)
    {
      GSSException localGSSException = new GSSException(11, -1, "Could not use DES3-KD Cipher - " + localException.getMessage());
      localGSSException.initCause(localException);
      throw localGSSException;
    }
  }

  private void arcFourDecrypt(WrapToken paramWrapToken, byte[] paramArrayOfByte1, int paramInt1, int paramInt2, byte[] paramArrayOfByte2, int paramInt3)
    throws GSSException
  {
    byte[] arrayOfByte2;
    byte[] arrayOfByte1 = decryptSeq(paramWrapToken.getChecksum(), paramWrapToken.getEncSeqNumber(), 0, 8);
    try
    {
      arrayOfByte2 = ArcFourHmac.decryptRaw(this.keybytes, 22, ZERO_IV, paramArrayOfByte1, paramInt1, paramInt2, arrayOfByte1);
    }
    catch (GeneralSecurityException localGeneralSecurityException)
    {
      GSSException localGSSException = new GSSException(11, -1, "Could not use ArcFour Cipher - " + localGeneralSecurityException.getMessage());
      localGSSException.initCause(localGeneralSecurityException);
      throw localGSSException;
    }
    int i = arrayOfByte2[(arrayOfByte2.length - 1)];
    if (i < 1)
      throw new GSSException(10, -1, "Invalid padding on Wrap Token");
    paramWrapToken.padding = WrapToken.pads[i];
    int j = arrayOfByte2.length - 8 - i;
    System.arraycopy(arrayOfByte2, 8, paramArrayOfByte2, paramInt3, j);
    System.arraycopy(arrayOfByte2, 0, paramWrapToken.confounder, 0, 8);
  }

  private byte[] arcFourEncrypt(WrapToken paramWrapToken, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, int paramInt1, int paramInt2, byte[] paramArrayOfByte3)
    throws GSSException
  {
    byte[] arrayOfByte1 = new byte[paramArrayOfByte1.length + paramInt2 + paramArrayOfByte3.length];
    System.arraycopy(paramArrayOfByte1, 0, arrayOfByte1, 0, paramArrayOfByte1.length);
    System.arraycopy(paramArrayOfByte2, paramInt1, arrayOfByte1, paramArrayOfByte1.length, paramInt2);
    System.arraycopy(paramArrayOfByte3, 0, arrayOfByte1, paramArrayOfByte1.length + paramInt2, paramArrayOfByte3.length);
    byte[] arrayOfByte2 = new byte[4];
    WrapToken.writeBigEndian(paramWrapToken.getSequenceNumber(), arrayOfByte2);
    try
    {
      byte[] arrayOfByte3 = ArcFourHmac.encryptRaw(this.keybytes, 22, arrayOfByte2, arrayOfByte1, 0, arrayOfByte1.length);
      return arrayOfByte3;
    }
    catch (Exception localException)
    {
      GSSException localGSSException = new GSSException(11, -1, "Could not use ArcFour Cipher - " + localException.getMessage());
      localGSSException.initCause(localException);
      throw localGSSException;
    }
  }

  private byte[] aes128Encrypt(byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, byte[] paramArrayOfByte3, int paramInt1, int paramInt2, int paramInt3)
    throws GSSException
  {
    byte[] arrayOfByte1 = new byte[paramArrayOfByte1.length + paramInt2 + paramArrayOfByte2.length];
    System.arraycopy(paramArrayOfByte1, 0, arrayOfByte1, 0, paramArrayOfByte1.length);
    System.arraycopy(paramArrayOfByte3, paramInt1, arrayOfByte1, paramArrayOfByte1.length, paramInt2);
    System.arraycopy(paramArrayOfByte2, 0, arrayOfByte1, paramArrayOfByte1.length + paramInt2, paramArrayOfByte2.length);
    try
    {
      byte[] arrayOfByte2 = Aes128.encryptRaw(this.keybytes, paramInt3, ZERO_IV_AES, arrayOfByte1, 0, arrayOfByte1.length);
      return arrayOfByte2;
    }
    catch (Exception localException)
    {
      GSSException localGSSException = new GSSException(11, -1, "Could not use AES128 Cipher - " + localException.getMessage());
      localGSSException.initCause(localException);
      throw localGSSException;
    }
  }

  private void aes128Decrypt(WrapToken_v2 paramWrapToken_v2, byte[] paramArrayOfByte1, int paramInt1, int paramInt2, byte[] paramArrayOfByte2, int paramInt3, int paramInt4)
    throws GSSException
  {
    byte[] arrayOfByte = null;
    try
    {
      arrayOfByte = Aes128.decryptRaw(this.keybytes, paramInt4, ZERO_IV_AES, paramArrayOfByte1, paramInt1, paramInt2);
    }
    catch (GeneralSecurityException localGeneralSecurityException)
    {
      GSSException localGSSException = new GSSException(11, -1, "Could not use AES128 Cipher - " + localGeneralSecurityException.getMessage());
      localGSSException.initCause(localGeneralSecurityException);
      throw localGSSException;
    }
    int i = arrayOfByte.length - 16 - 16;
    System.arraycopy(arrayOfByte, 16, paramArrayOfByte2, paramInt3, i);
  }

  private byte[] aes256Encrypt(byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, byte[] paramArrayOfByte3, int paramInt1, int paramInt2, int paramInt3)
    throws GSSException
  {
    byte[] arrayOfByte1 = new byte[paramArrayOfByte1.length + paramInt2 + paramArrayOfByte2.length];
    System.arraycopy(paramArrayOfByte1, 0, arrayOfByte1, 0, paramArrayOfByte1.length);
    System.arraycopy(paramArrayOfByte3, paramInt1, arrayOfByte1, paramArrayOfByte1.length, paramInt2);
    System.arraycopy(paramArrayOfByte2, 0, arrayOfByte1, paramArrayOfByte1.length + paramInt2, paramArrayOfByte2.length);
    try
    {
      byte[] arrayOfByte2 = Aes256.encryptRaw(this.keybytes, paramInt3, ZERO_IV_AES, arrayOfByte1, 0, arrayOfByte1.length);
      return arrayOfByte2;
    }
    catch (Exception localException)
    {
      GSSException localGSSException = new GSSException(11, -1, "Could not use AES256 Cipher - " + localException.getMessage());
      localGSSException.initCause(localException);
      throw localGSSException;
    }
  }

  private void aes256Decrypt(WrapToken_v2 paramWrapToken_v2, byte[] paramArrayOfByte1, int paramInt1, int paramInt2, byte[] paramArrayOfByte2, int paramInt3, int paramInt4)
    throws GSSException
  {
    byte[] arrayOfByte;
    try
    {
      arrayOfByte = Aes256.decryptRaw(this.keybytes, paramInt4, ZERO_IV_AES, paramArrayOfByte1, paramInt1, paramInt2);
    }
    catch (GeneralSecurityException localGeneralSecurityException)
    {
      GSSException localGSSException = new GSSException(11, -1, "Could not use AES128 Cipher - " + localGeneralSecurityException.getMessage());
      localGSSException.initCause(localGeneralSecurityException);
      throw localGSSException;
    }
    int i = arrayOfByte.length - 16 - 16;
    System.arraycopy(arrayOfByte, 16, paramArrayOfByte2, paramInt3, i);
  }

  class WrapTokenInputStream extends InputStream
  {
    private InputStream is;
    private int length;
    private int remaining;
    private int temp;

    public WrapTokenInputStream(, InputStream paramInputStream, int paramInt)
    {
      this.is = paramInputStream;
      this.length = paramInt;
      this.remaining = paramInt;
    }

    public final int read()
      throws IOException
    {
      if (this.remaining == 0)
        return -1;
      this.temp = this.is.read();
      if (this.temp != -1)
        this.remaining -= this.temp;
      return this.temp;
    }

    public final int read()
      throws IOException
    {
      if (this.remaining == 0)
        return -1;
      this.temp = Math.min(this.remaining, paramArrayOfByte.length);
      this.temp = this.is.read(paramArrayOfByte, 0, this.temp);
      if (this.temp != -1)
        this.remaining -= this.temp;
      return this.temp;
    }

    public final int read(, int paramInt1, int paramInt2)
      throws IOException
    {
      if (this.remaining == 0)
        return -1;
      this.temp = Math.min(this.remaining, paramInt2);
      this.temp = this.is.read(paramArrayOfByte, paramInt1, this.temp);
      if (this.temp != -1)
        this.remaining -= this.temp;
      return this.temp;
    }

    public final long skip()
      throws IOException
    {
      if (this.remaining == 0)
        return 3412048098707374080L;
      this.temp = (int)Math.min(this.remaining, paramLong);
      this.temp = (int)this.is.skip(this.temp);
      this.remaining -= this.temp;
      return this.temp;
    }

    public final int available()
      throws IOException
    {
      return Math.min(this.remaining, this.is.available());
    }

    public final void close()
      throws IOException
    {
      this.remaining = 0;
    }
  }
}