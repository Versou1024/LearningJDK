package sun.security.rsa;

import java.security.DigestException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.MGF1ParameterSpec;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.BadPaddingException;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import javax.crypto.spec.PSource.PSpecified;
import sun.security.jca.JCAUtil;

public final class RSAPadding
{
  public static final int PAD_BLOCKTYPE_1 = 1;
  public static final int PAD_BLOCKTYPE_2 = 2;
  public static final int PAD_NONE = 3;
  public static final int PAD_OAEP_MGF1 = 4;
  private final int type;
  private final int paddedSize;
  private SecureRandom random;
  private final int maxDataSize;
  private MessageDigest md;
  private MessageDigest mgfMd;
  private byte[] lHash;
  private static final Map<String, byte[]> emptyHashes = Collections.synchronizedMap(new HashMap());

  public static RSAPadding getInstance(int paramInt1, int paramInt2)
    throws InvalidKeyException, InvalidAlgorithmParameterException
  {
    return new RSAPadding(paramInt1, paramInt2, null, null);
  }

  public static RSAPadding getInstance(int paramInt1, int paramInt2, SecureRandom paramSecureRandom)
    throws InvalidKeyException, InvalidAlgorithmParameterException
  {
    return new RSAPadding(paramInt1, paramInt2, paramSecureRandom, null);
  }

  public static RSAPadding getInstance(int paramInt1, int paramInt2, SecureRandom paramSecureRandom, OAEPParameterSpec paramOAEPParameterSpec)
    throws InvalidKeyException, InvalidAlgorithmParameterException
  {
    return new RSAPadding(paramInt1, paramInt2, paramSecureRandom, paramOAEPParameterSpec);
  }

  private RSAPadding(int paramInt1, int paramInt2, SecureRandom paramSecureRandom, OAEPParameterSpec paramOAEPParameterSpec)
    throws InvalidKeyException, InvalidAlgorithmParameterException
  {
    this.type = paramInt1;
    this.paddedSize = paramInt2;
    this.random = paramSecureRandom;
    if (paramInt2 < 64)
      throw new InvalidKeyException("Padded size must be at least 64");
    switch (paramInt1)
    {
    case 1:
    case 2:
      this.maxDataSize = (paramInt2 - 11);
      break;
    case 3:
      this.maxDataSize = paramInt2;
      break;
    case 4:
      String str1 = "SHA-1";
      String str2 = "SHA-1";
      byte[] arrayOfByte = null;
      try
      {
        if (paramOAEPParameterSpec != null)
        {
          str1 = paramOAEPParameterSpec.getDigestAlgorithm();
          String str3 = paramOAEPParameterSpec.getMGFAlgorithm();
          if (!(str3.equalsIgnoreCase("MGF1")))
            throw new InvalidAlgorithmParameterException("Unsupported MGF algo: " + str3);
          str2 = ((MGF1ParameterSpec)paramOAEPParameterSpec.getMGFParameters()).getDigestAlgorithm();
          PSource localPSource = paramOAEPParameterSpec.getPSource();
          String str4 = localPSource.getAlgorithm();
          if (!(str4.equalsIgnoreCase("PSpecified")))
            throw new InvalidAlgorithmParameterException("Unsupported pSource algo: " + str4);
          arrayOfByte = ((PSource.PSpecified)localPSource).getValue();
        }
        this.md = MessageDigest.getInstance(str1);
        this.mgfMd = MessageDigest.getInstance(str2);
      }
      catch (NoSuchAlgorithmException localNoSuchAlgorithmException)
      {
        throw new InvalidKeyException("Digest " + str1 + " not available", localNoSuchAlgorithmException);
      }
      this.lHash = getInitialHash(this.md, arrayOfByte);
      int i = this.lHash.length;
      this.maxDataSize = (paramInt2 - 2 - 2 * i);
      if (this.maxDataSize > 0)
        return;
      throw new InvalidKeyException("Key is too short for encryption using OAEPPadding with " + str1 + " and MGF1" + str2);
    default:
      throw new InvalidKeyException("Invalid padding: " + paramInt1);
    }
  }

  private static byte[] getInitialHash(MessageDigest paramMessageDigest, byte[] paramArrayOfByte)
  {
    byte[] arrayOfByte = null;
    if ((paramArrayOfByte == null) || (paramArrayOfByte.length == 0))
    {
      String str = paramMessageDigest.getAlgorithm();
      arrayOfByte = (byte[])emptyHashes.get(str);
      if (arrayOfByte == null)
      {
        arrayOfByte = paramMessageDigest.digest();
        emptyHashes.put(str, arrayOfByte);
      }
    }
    else
    {
      arrayOfByte = paramMessageDigest.digest(paramArrayOfByte);
    }
    return arrayOfByte;
  }

  public int getMaxDataSize()
  {
    return this.maxDataSize;
  }

  public byte[] pad(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
    throws BadPaddingException
  {
    return pad(RSACore.convert(paramArrayOfByte, paramInt1, paramInt2));
  }

  public byte[] pad(byte[] paramArrayOfByte)
    throws BadPaddingException
  {
    if (paramArrayOfByte.length > this.maxDataSize)
      throw new BadPaddingException("Data must be shorter than " + (this.maxDataSize + 1) + " bytes");
    switch (this.type)
    {
    case 3:
      return paramArrayOfByte;
    case 1:
    case 2:
      return padV15(paramArrayOfByte);
    case 4:
      return padOAEP(paramArrayOfByte);
    }
    throw new AssertionError();
  }

  public byte[] unpad(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
    throws BadPaddingException
  {
    return unpad(RSACore.convert(paramArrayOfByte, paramInt1, paramInt2));
  }

  public byte[] unpad(byte[] paramArrayOfByte)
    throws BadPaddingException
  {
    if (paramArrayOfByte.length != this.paddedSize)
      throw new BadPaddingException("Padded length must be " + this.paddedSize);
    switch (this.type)
    {
    case 3:
      return paramArrayOfByte;
    case 1:
    case 2:
      return unpadV15(paramArrayOfByte);
    case 4:
      return unpadOAEP(paramArrayOfByte);
    }
    throw new AssertionError();
  }

  private byte[] padV15(byte[] paramArrayOfByte)
    throws BadPaddingException
  {
    byte[] arrayOfByte1 = new byte[this.paddedSize];
    System.arraycopy(paramArrayOfByte, 0, arrayOfByte1, this.paddedSize - paramArrayOfByte.length, paramArrayOfByte.length);
    int i = this.paddedSize - 3 - paramArrayOfByte.length;
    int j = 0;
    arrayOfByte1[(j++)] = 0;
    arrayOfByte1[(j++)] = (byte)this.type;
    if (this.type == 1)
      while (true)
      {
        if (i-- <= 0)
          break label164;
        arrayOfByte1[(j++)] = -1;
      }
    if (this.random == null)
      this.random = JCAUtil.getSecureRandom();
    byte[] arrayOfByte2 = new byte[64];
    int k = -1;
    while (i-- > 0)
    {
      int l;
      do
      {
        if (k < 0)
        {
          this.random.nextBytes(arrayOfByte2);
          k = arrayOfByte2.length - 1;
        }
        l = arrayOfByte2[(k--)] & 0xFF;
      }
      while (l == 0);
      arrayOfByte1[(j++)] = (byte)l;
    }
    label164: return arrayOfByte1;
  }

  private byte[] unpadV15(byte[] paramArrayOfByte)
    throws BadPaddingException
  {
    int i = 0;
    if (paramArrayOfByte[(i++)] != 0)
      throw new BadPaddingException("Data must start with zero");
    if (paramArrayOfByte[(i++)] != this.type)
      throw new BadPaddingException("Blocktype mismatch: " + paramArrayOfByte[1]);
    while (true)
    {
      j = paramArrayOfByte[(i++)] & 0xFF;
      if (j == 0)
        break;
      if (i == paramArrayOfByte.length)
        throw new BadPaddingException("Padding string not terminated");
      if ((this.type == 1) && (j != 255))
        throw new BadPaddingException("Padding byte not 0xff: " + j);
    }
    int j = paramArrayOfByte.length - i;
    if (j > this.maxDataSize)
      throw new BadPaddingException("Padding string too short");
    byte[] arrayOfByte = new byte[j];
    System.arraycopy(paramArrayOfByte, paramArrayOfByte.length - j, arrayOfByte, 0, j);
    return arrayOfByte;
  }

  private byte[] padOAEP(byte[] paramArrayOfByte)
    throws BadPaddingException
  {
    if (this.random == null)
      this.random = JCAUtil.getSecureRandom();
    int i = this.lHash.length;
    byte[] arrayOfByte1 = new byte[i];
    this.random.nextBytes(arrayOfByte1);
    byte[] arrayOfByte2 = new byte[this.paddedSize];
    int j = 1;
    int k = i;
    System.arraycopy(arrayOfByte1, 0, arrayOfByte2, j, k);
    int l = i + 1;
    int i1 = arrayOfByte2.length - l;
    int i2 = this.paddedSize - paramArrayOfByte.length;
    System.arraycopy(this.lHash, 0, arrayOfByte2, l, i);
    arrayOfByte2[(i2 - 1)] = 1;
    System.arraycopy(paramArrayOfByte, 0, arrayOfByte2, i2, paramArrayOfByte.length);
    mgf1(arrayOfByte2, j, k, arrayOfByte2, l, i1);
    mgf1(arrayOfByte2, l, i1, arrayOfByte2, j, k);
    return arrayOfByte2;
  }

  private byte[] unpadOAEP(byte[] paramArrayOfByte)
    throws BadPaddingException
  {
    byte[] arrayOfByte1 = paramArrayOfByte;
    int i = this.lHash.length;
    if (arrayOfByte1[0] != 0)
      throw new BadPaddingException("Data must start with zero");
    int j = 1;
    int k = i;
    int l = i + 1;
    int i1 = arrayOfByte1.length - l;
    mgf1(arrayOfByte1, l, i1, arrayOfByte1, j, k);
    mgf1(arrayOfByte1, j, k, arrayOfByte1, l, i1);
    for (int i2 = 0; i2 < i; ++i2)
      if (this.lHash[i2] != arrayOfByte1[(l + i2)])
        throw new BadPaddingException("lHash mismatch");
    i2 = l + i;
    do
      if (arrayOfByte1[i2] != 0)
        break label145;
    while (++i2 < arrayOfByte1.length);
    throw new BadPaddingException("Padding string not terminated");
    if (arrayOfByte1[(i2++)] != 1)
      label145: throw new BadPaddingException("Padding string not terminated by 0x01 byte");
    int i3 = arrayOfByte1.length - i2;
    byte[] arrayOfByte2 = new byte[i3];
    System.arraycopy(arrayOfByte1, i2, arrayOfByte2, 0, i3);
    return arrayOfByte2;
  }

  private void mgf1(byte[] paramArrayOfByte1, int paramInt1, int paramInt2, byte[] paramArrayOfByte2, int paramInt3, int paramInt4)
    throws BadPaddingException
  {
    byte[] arrayOfByte1 = new byte[4];
    byte[] arrayOfByte2 = new byte[20];
    while (true)
    {
      do
      {
        if (paramInt4 <= 0)
          return;
        this.mgfMd.update(paramArrayOfByte1, paramInt1, paramInt2);
        this.mgfMd.update(arrayOfByte1);
        try
        {
          this.mgfMd.digest(arrayOfByte2, 0, arrayOfByte2.length);
        }
        catch (DigestException localDigestException)
        {
          throw new BadPaddingException(localDigestException.toString());
        }
        i = 0;
        while ((i < arrayOfByte2.length) && (paramInt4 > 0))
        {
          int tmp90_87 = (paramInt3++);
          byte[] tmp90_83 = paramArrayOfByte2;
          tmp90_83[tmp90_87] = (byte)(tmp90_83[tmp90_87] ^ arrayOfByte2[(i++)]);
          --paramInt4;
        }
      }
      while (paramInt4 <= 0);
      int i = arrayOfByte1.length - 1;
      while (true)
      {
        int tmp125_123 = i;
        byte[] tmp125_121 = arrayOfByte1;
        if (((tmp125_121[tmp125_123] = (byte)(tmp125_121[tmp125_123] + 1)) != 0) || (i <= 0))
          break;
        --i;
      }
    }
  }
}