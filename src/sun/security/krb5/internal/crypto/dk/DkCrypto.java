package sun.security.krb5.internal.crypto.dk;

import B;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import javax.crypto.Cipher;
import sun.misc.HexDumpEncoder;
import sun.security.krb5.KrbCryptoException;
import sun.security.krb5.internal.crypto.Confounder;
import sun.security.krb5.internal.crypto.KeyUsage;

public abstract class DkCrypto
{
  protected static final boolean debug = 0;
  static final byte[] KERBEROS_CONSTANT = { 107, 101, 114, 98, 101, 114, 111, 115 };

  protected abstract int getKeySeedLength();

  protected abstract byte[] randomToKey(byte[] paramArrayOfByte);

  protected abstract Cipher getCipher(byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, int paramInt)
    throws GeneralSecurityException;

  public abstract int getChecksumLength();

  protected abstract byte[] getHmac(byte[] paramArrayOfByte1, byte[] paramArrayOfByte2)
    throws GeneralSecurityException;

  public byte[] encrypt(byte[] paramArrayOfByte1, int paramInt1, byte[] paramArrayOfByte2, byte[] paramArrayOfByte3, byte[] paramArrayOfByte4, int paramInt2, int paramInt3)
    throws GeneralSecurityException, KrbCryptoException
  {
    if (!(KeyUsage.isValid(paramInt1)))
      throw new GeneralSecurityException("Invalid key usage number: " + paramInt1);
    byte[] arrayOfByte1 = null;
    byte[] arrayOfByte2 = null;
    try
    {
      byte[] arrayOfByte3 = new byte[5];
      arrayOfByte3[0] = (byte)(paramInt1 >> 24 & 0xFF);
      arrayOfByte3[1] = (byte)(paramInt1 >> 16 & 0xFF);
      arrayOfByte3[2] = (byte)(paramInt1 >> 8 & 0xFF);
      arrayOfByte3[3] = (byte)(paramInt1 & 0xFF);
      arrayOfByte3[4] = -86;
      arrayOfByte1 = dk(paramArrayOfByte1, arrayOfByte3);
      Cipher localCipher = getCipher(arrayOfByte1, paramArrayOfByte2, 1);
      int i = localCipher.getBlockSize();
      byte[] arrayOfByte4 = Confounder.bytes(i);
      int j = roundup(arrayOfByte4.length + paramInt3, i);
      byte[] arrayOfByte5 = new byte[j];
      System.arraycopy(arrayOfByte4, 0, arrayOfByte5, 0, arrayOfByte4.length);
      System.arraycopy(paramArrayOfByte4, paramInt2, arrayOfByte5, arrayOfByte4.length, paramInt3);
      Arrays.fill(arrayOfByte5, arrayOfByte4.length + paramInt3, j, 0);
      int k = localCipher.getOutputSize(j);
      int l = k + getChecksumLength();
      byte[] arrayOfByte6 = new byte[l];
      localCipher.doFinal(arrayOfByte5, 0, j, arrayOfByte6, 0);
      if ((paramArrayOfByte3 != null) && (paramArrayOfByte3.length == i))
        System.arraycopy(arrayOfByte6, k - i, paramArrayOfByte3, 0, i);
      arrayOfByte3[4] = 85;
      arrayOfByte2 = dk(paramArrayOfByte1, arrayOfByte3);
      byte[] arrayOfByte7 = getHmac(arrayOfByte2, arrayOfByte5);
      System.arraycopy(arrayOfByte7, 0, arrayOfByte6, k, getChecksumLength());
      byte[] arrayOfByte8 = arrayOfByte6;
      return arrayOfByte8;
    }
    finally
    {
      if (arrayOfByte1 != null)
        Arrays.fill(arrayOfByte1, 0, arrayOfByte1.length, 0);
      if (arrayOfByte2 != null)
        Arrays.fill(arrayOfByte2, 0, arrayOfByte2.length, 0);
    }
  }

  public byte[] encryptRaw(byte[] paramArrayOfByte1, int paramInt1, byte[] paramArrayOfByte2, byte[] paramArrayOfByte3, int paramInt2, int paramInt3)
    throws GeneralSecurityException, KrbCryptoException
  {
    Cipher localCipher = getCipher(paramArrayOfByte1, paramArrayOfByte2, 1);
    int i = localCipher.getBlockSize();
    if (paramInt3 % i != 0)
      throw new GeneralSecurityException("length of data to be encrypted (" + paramInt3 + ") is not a multiple of the blocksize (" + i + ")");
    int j = localCipher.getOutputSize(paramInt3);
    byte[] arrayOfByte = new byte[j];
    localCipher.doFinal(paramArrayOfByte3, 0, paramInt3, arrayOfByte, 0);
    return arrayOfByte;
  }

  public byte[] decryptRaw(byte[] paramArrayOfByte1, int paramInt1, byte[] paramArrayOfByte2, byte[] paramArrayOfByte3, int paramInt2, int paramInt3)
    throws GeneralSecurityException
  {
    Cipher localCipher = getCipher(paramArrayOfByte1, paramArrayOfByte2, 2);
    int i = localCipher.getBlockSize();
    if (paramInt3 % i != 0)
      throw new GeneralSecurityException("length of data to be decrypted (" + paramInt3 + ") is not a multiple of the blocksize (" + i + ")");
    byte[] arrayOfByte = localCipher.doFinal(paramArrayOfByte3, paramInt2, paramInt3);
    return arrayOfByte;
  }

  public byte[] decrypt(byte[] paramArrayOfByte1, int paramInt1, byte[] paramArrayOfByte2, byte[] paramArrayOfByte3, int paramInt2, int paramInt3)
    throws GeneralSecurityException
  {
    if (!(KeyUsage.isValid(paramInt1)))
      throw new GeneralSecurityException("Invalid key usage number: " + paramInt1);
    byte[] arrayOfByte1 = null;
    byte[] arrayOfByte2 = null;
    try
    {
      byte[] arrayOfByte3 = new byte[5];
      arrayOfByte3[0] = (byte)(paramInt1 >> 24 & 0xFF);
      arrayOfByte3[1] = (byte)(paramInt1 >> 16 & 0xFF);
      arrayOfByte3[2] = (byte)(paramInt1 >> 8 & 0xFF);
      arrayOfByte3[3] = (byte)(paramInt1 & 0xFF);
      arrayOfByte3[4] = -86;
      arrayOfByte1 = dk(paramArrayOfByte1, arrayOfByte3);
      Cipher localCipher = getCipher(arrayOfByte1, paramArrayOfByte2, 2);
      int i = localCipher.getBlockSize();
      int j = getChecksumLength();
      int k = paramInt3 - j;
      byte[] arrayOfByte4 = localCipher.doFinal(paramArrayOfByte3, paramInt2, k);
      arrayOfByte3[4] = 85;
      arrayOfByte2 = dk(paramArrayOfByte1, arrayOfByte3);
      byte[] arrayOfByte5 = getHmac(arrayOfByte2, arrayOfByte4);
      int l = 0;
      if (arrayOfByte5.length >= j)
        for (int i1 = 0; i1 < j; ++i1)
          if (arrayOfByte5[i1] != paramArrayOfByte3[(k + i1)])
          {
            l = 1;
            break;
          }
      if (l != 0)
        throw new GeneralSecurityException("Checksum failed");
      if ((paramArrayOfByte2 != null) && (paramArrayOfByte2.length == i))
        System.arraycopy(paramArrayOfByte3, paramInt2 + k - i, paramArrayOfByte2, 0, i);
      byte[] arrayOfByte6 = new byte[arrayOfByte4.length - i];
      System.arraycopy(arrayOfByte4, i, arrayOfByte6, 0, arrayOfByte6.length);
      byte[] arrayOfByte7 = arrayOfByte6;
      return arrayOfByte7;
    }
    finally
    {
      if (arrayOfByte1 != null)
        Arrays.fill(arrayOfByte1, 0, arrayOfByte1.length, 0);
      if (arrayOfByte2 != null)
        Arrays.fill(arrayOfByte2, 0, arrayOfByte2.length, 0);
    }
  }

  int roundup(int paramInt1, int paramInt2)
  {
    return ((paramInt1 + paramInt2 - 1) / paramInt2 * paramInt2);
  }

  public byte[] calculateChecksum(byte[] paramArrayOfByte1, int paramInt1, byte[] paramArrayOfByte2, int paramInt2, int paramInt3)
    throws GeneralSecurityException
  {
    if (!(KeyUsage.isValid(paramInt1)))
      throw new GeneralSecurityException("Invalid key usage number: " + paramInt1);
    byte[] arrayOfByte1 = new byte[5];
    arrayOfByte1[0] = (byte)(paramInt1 >> 24 & 0xFF);
    arrayOfByte1[1] = (byte)(paramInt1 >> 16 & 0xFF);
    arrayOfByte1[2] = (byte)(paramInt1 >> 8 & 0xFF);
    arrayOfByte1[3] = (byte)(paramInt1 & 0xFF);
    arrayOfByte1[4] = -103;
    byte[] arrayOfByte2 = dk(paramArrayOfByte1, arrayOfByte1);
    try
    {
      byte[] arrayOfByte4;
      byte[] arrayOfByte5;
      byte[] arrayOfByte3 = getHmac(arrayOfByte2, paramArrayOfByte2);
      if (arrayOfByte3.length == getChecksumLength())
      {
        arrayOfByte4 = arrayOfByte3;
        return arrayOfByte4;
      }
      if (arrayOfByte3.length > getChecksumLength())
      {
        arrayOfByte4 = new byte[getChecksumLength()];
        System.arraycopy(arrayOfByte3, 0, arrayOfByte4, 0, arrayOfByte4.length);
        arrayOfByte5 = arrayOfByte4;
        Arrays.fill(arrayOfByte2, 0, arrayOfByte2.length, 0);
      }
      throw new GeneralSecurityException("checksum size too short: " + arrayOfByte3.length + "; expecting : " + getChecksumLength());
    }
    finally
    {
      Arrays.fill(arrayOfByte2, 0, arrayOfByte2.length, 0);
    }
  }

  byte[] dk(byte[] paramArrayOfByte1, byte[] paramArrayOfByte2)
    throws GeneralSecurityException
  {
    return randomToKey(dr(paramArrayOfByte1, paramArrayOfByte2));
  }

  private byte[] dr(byte[] paramArrayOfByte1, byte[] paramArrayOfByte2)
    throws GeneralSecurityException
  {
    Cipher localCipher = getCipher(paramArrayOfByte1, null, 1);
    int i = localCipher.getBlockSize();
    if (paramArrayOfByte2.length != i)
      paramArrayOfByte2 = nfold(paramArrayOfByte2, i * 8);
    Object localObject = paramArrayOfByte2;
    int j = getKeySeedLength() >> 3;
    byte[] arrayOfByte1 = new byte[j];
    int k = 0;
    int l = 0;
    while (l < j)
    {
      byte[] arrayOfByte2 = localCipher.doFinal(localObject);
      int i1 = (j - l <= arrayOfByte2.length) ? j - l : arrayOfByte2.length;
      System.arraycopy(arrayOfByte2, 0, arrayOfByte1, l, i1);
      l += i1;
      localObject = arrayOfByte2;
    }
    return ((B)arrayOfByte1);
  }

  static byte[] nfold(byte[] paramArrayOfByte, int paramInt)
  {
    int i = paramArrayOfByte.length;
    paramInt >>= 3;
    int j = paramInt;
    int k = i;
    while (k != 0)
    {
      int l = k;
      k = j % k;
      j = l;
    }
    int i1 = paramInt * i / j;
    byte[] arrayOfByte = new byte[paramInt];
    Arrays.fill(arrayOfByte, 0);
    int i2 = 0;
    for (int i4 = i1 - 1; i4 >= 0; --i4)
    {
      int i3 = ((i << 3) - 1 + ((i << 3) + 13) * i4 / i + (i - i4 % i << 3)) % (i << 3);
      int i5 = ((paramArrayOfByte[((i - 1 - (i3 >>> 3)) % i)] & 0xFF) << 8 | paramArrayOfByte[((i - (i3 >>> 3)) % i)] & 0xFF) >>> (i3 & 0x7) + 1 & 0xFF;
      i2 += i5;
      int i6 = arrayOfByte[(i4 % paramInt)] & 0xFF;
      i2 += i6;
      arrayOfByte[(i4 % paramInt)] = (byte)(i2 & 0xFF);
      i2 >>>= 8;
    }
    if (i2 != 0)
      for (i4 = paramInt - 1; i4 >= 0; --i4)
      {
        i2 += (arrayOfByte[i4] & 0xFF);
        arrayOfByte[i4] = (byte)(i2 & 0xFF);
        i2 >>>= 8;
      }
    return arrayOfByte;
  }

  static String bytesToString(byte[] paramArrayOfByte)
  {
    StringBuffer localStringBuffer = new StringBuffer();
    for (int i = 0; i < paramArrayOfByte.length; ++i)
      if ((paramArrayOfByte[i] & 0xFF) < 16)
        localStringBuffer.append("0" + Integer.toHexString(paramArrayOfByte[i] & 0xFF));
      else
        localStringBuffer.append(Integer.toHexString(paramArrayOfByte[i] & 0xFF));
    return localStringBuffer.toString();
  }

  private static byte[] binaryStringToBytes(String paramString)
  {
    char[] arrayOfChar = paramString.toCharArray();
    byte[] arrayOfByte = new byte[arrayOfChar.length / 2];
    for (int i = 0; i < arrayOfByte.length; ++i)
    {
      int j = Byte.parseByte(new String(arrayOfChar, i * 2, 1), 16);
      int k = Byte.parseByte(new String(arrayOfChar, i * 2 + 1, 1), 16);
      arrayOfByte[i] = (byte)(j << 4 | k);
    }
    return arrayOfByte;
  }

  static void traceOutput(String paramString, byte[] paramArrayOfByte, int paramInt1, int paramInt2)
  {
    ByteArrayOutputStream localByteArrayOutputStream;
    try
    {
      localByteArrayOutputStream = new ByteArrayOutputStream(paramInt2);
      new HexDumpEncoder().encodeBuffer(new ByteArrayInputStream(paramArrayOfByte, paramInt1, paramInt2), localByteArrayOutputStream);
      System.err.println(paramString + ":" + localByteArrayOutputStream.toString());
    }
    catch (Exception localException)
    {
    }
  }

  static byte[] charToUtf8(char[] paramArrayOfChar)
  {
    Charset localCharset = Charset.forName("UTF-8");
    CharBuffer localCharBuffer = CharBuffer.wrap(paramArrayOfChar);
    ByteBuffer localByteBuffer = localCharset.encode(localCharBuffer);
    int i = localByteBuffer.limit();
    byte[] arrayOfByte = new byte[i];
    localByteBuffer.get(arrayOfByte, 0, i);
    return arrayOfByte;
  }

  static byte[] charToUtf16(char[] paramArrayOfChar)
  {
    Charset localCharset = Charset.forName("UTF-16LE");
    CharBuffer localCharBuffer = CharBuffer.wrap(paramArrayOfChar);
    ByteBuffer localByteBuffer = localCharset.encode(localCharBuffer);
    int i = localByteBuffer.limit();
    byte[] arrayOfByte = new byte[i];
    localByteBuffer.get(arrayOfByte, 0, i);
    return arrayOfByte;
  }
}