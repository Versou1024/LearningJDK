package sun.security.provider;

import java.io.IOException;
import java.security.Key;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.util.Arrays;
import sun.security.pkcs.EncryptedPrivateKeyInfo;
import sun.security.pkcs.PKCS8Key;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;
import sun.security.x509.AlgorithmId;

final class KeyProtector
{
  private static final int SALT_LEN = 20;
  private static final String DIGEST_ALG = "SHA";
  private static final int DIGEST_LEN = 20;
  private static final String KEY_PROTECTOR_OID = "1.3.6.1.4.1.42.2.17.1.1";
  private byte[] passwdBytes;
  private MessageDigest md;

  public KeyProtector(char[] paramArrayOfChar)
    throws NoSuchAlgorithmException
  {
    if (paramArrayOfChar == null)
      throw new IllegalArgumentException("password can't be null");
    this.md = MessageDigest.getInstance("SHA");
    this.passwdBytes = new byte[paramArrayOfChar.length * 2];
    int i = 0;
    int j = 0;
    while (i < paramArrayOfChar.length)
    {
      this.passwdBytes[(j++)] = (byte)(paramArrayOfChar[i] >> '\b');
      this.passwdBytes[(j++)] = (byte)paramArrayOfChar[i];
      ++i;
    }
  }

  protected void finalize()
  {
    if (this.passwdBytes != null)
    {
      Arrays.fill(this.passwdBytes, 0);
      this.passwdBytes = null;
    }
  }

  public byte[] protect(Key paramKey)
    throws KeyStoreException
  {
    int l = 0;
    if (paramKey == null)
      throw new IllegalArgumentException("plaintext key can't be null");
    if (!("PKCS#8".equalsIgnoreCase(paramKey.getFormat())))
      throw new KeyStoreException("Cannot get key bytes, not PKCS#8 encoded");
    byte[] arrayOfByte2 = paramKey.getEncoded();
    if (arrayOfByte2 == null)
      throw new KeyStoreException("Cannot get key bytes, encoding not supported");
    int j = arrayOfByte2.length / 20;
    if (arrayOfByte2.length % 20 != 0)
      ++j;
    byte[] arrayOfByte3 = new byte[20];
    SecureRandom localSecureRandom = new SecureRandom();
    localSecureRandom.nextBytes(arrayOfByte3);
    byte[] arrayOfByte4 = new byte[arrayOfByte2.length];
    int i = 0;
    int k = 0;
    byte[] arrayOfByte1 = arrayOfByte3;
    while (i < j)
    {
      this.md.update(this.passwdBytes);
      this.md.update(arrayOfByte1);
      arrayOfByte1 = this.md.digest();
      this.md.reset();
      if (i < j - 1)
        System.arraycopy(arrayOfByte1, 0, arrayOfByte4, k, arrayOfByte1.length);
      else
        System.arraycopy(arrayOfByte1, 0, arrayOfByte4, k, arrayOfByte4.length - k);
      ++i;
      k += 20;
    }
    byte[] arrayOfByte5 = new byte[arrayOfByte2.length];
    for (i = 0; i < arrayOfByte5.length; ++i)
      arrayOfByte5[i] = (byte)(arrayOfByte2[i] ^ arrayOfByte4[i]);
    byte[] arrayOfByte6 = new byte[arrayOfByte3.length + arrayOfByte5.length + 20];
    System.arraycopy(arrayOfByte3, 0, arrayOfByte6, l, arrayOfByte3.length);
    l += arrayOfByte3.length;
    System.arraycopy(arrayOfByte5, 0, arrayOfByte6, l, arrayOfByte5.length);
    l += arrayOfByte5.length;
    this.md.update(this.passwdBytes);
    Arrays.fill(this.passwdBytes, 0);
    this.passwdBytes = null;
    this.md.update(arrayOfByte2);
    arrayOfByte1 = this.md.digest();
    this.md.reset();
    System.arraycopy(arrayOfByte1, 0, arrayOfByte6, l, arrayOfByte1.length);
    try
    {
      AlgorithmId localAlgorithmId = new AlgorithmId(new ObjectIdentifier("1.3.6.1.4.1.42.2.17.1.1"));
      return new EncryptedPrivateKeyInfo(localAlgorithmId, arrayOfByte6).getEncoded();
    }
    catch (IOException localIOException)
    {
      throw new KeyStoreException(localIOException.getMessage());
    }
  }

  public Key recover(EncryptedPrivateKeyInfo paramEncryptedPrivateKeyInfo)
    throws UnrecoverableKeyException
  {
    AlgorithmId localAlgorithmId = paramEncryptedPrivateKeyInfo.getAlgorithm();
    if (!(localAlgorithmId.getOID().toString().equals("1.3.6.1.4.1.42.2.17.1.1")))
      throw new UnrecoverableKeyException("Unsupported key protection algorithm");
    byte[] arrayOfByte2 = paramEncryptedPrivateKeyInfo.getEncryptedData();
    byte[] arrayOfByte3 = new byte[20];
    System.arraycopy(arrayOfByte2, 0, arrayOfByte3, 0, 20);
    int l = arrayOfByte2.length - 20 - 20;
    int j = l / 20;
    if (l % 20 != 0)
      ++j;
    byte[] arrayOfByte4 = new byte[l];
    System.arraycopy(arrayOfByte2, 20, arrayOfByte4, 0, l);
    byte[] arrayOfByte5 = new byte[arrayOfByte4.length];
    int i = 0;
    int k = 0;
    byte[] arrayOfByte1 = arrayOfByte3;
    while (i < j)
    {
      this.md.update(this.passwdBytes);
      this.md.update(arrayOfByte1);
      arrayOfByte1 = this.md.digest();
      this.md.reset();
      if (i < j - 1)
        System.arraycopy(arrayOfByte1, 0, arrayOfByte5, k, arrayOfByte1.length);
      else
        System.arraycopy(arrayOfByte1, 0, arrayOfByte5, k, arrayOfByte5.length - k);
      ++i;
      k += 20;
    }
    byte[] arrayOfByte6 = new byte[arrayOfByte4.length];
    for (i = 0; i < arrayOfByte6.length; ++i)
      arrayOfByte6[i] = (byte)(arrayOfByte4[i] ^ arrayOfByte5[i]);
    this.md.update(this.passwdBytes);
    Arrays.fill(this.passwdBytes, 0);
    this.passwdBytes = null;
    this.md.update(arrayOfByte6);
    arrayOfByte1 = this.md.digest();
    this.md.reset();
    for (i = 0; i < arrayOfByte1.length; ++i)
      if (arrayOfByte1[i] != arrayOfByte2[(20 + l + i)])
        throw new UnrecoverableKeyException("Cannot recover key");
    try
    {
      return PKCS8Key.parseKey(new DerValue(arrayOfByte6));
    }
    catch (IOException localIOException)
    {
      throw new UnrecoverableKeyException(localIOException.getMessage());
    }
  }
}