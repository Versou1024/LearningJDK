package sun.security.krb5;

import java.io.IOException;
import java.math.BigInteger;
import sun.security.krb5.internal.KdcErrException;
import sun.security.krb5.internal.KrbApErrException;
import sun.security.krb5.internal.crypto.EType;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class EncryptedData
  implements Cloneable
{
  int eType;
  Integer kvno;
  byte[] cipher;
  byte[] plain;
  public static final int ETYPE_NULL = 0;
  public static final int ETYPE_DES_CBC_CRC = 1;
  public static final int ETYPE_DES_CBC_MD4 = 2;
  public static final int ETYPE_DES_CBC_MD5 = 3;
  public static final int ETYPE_ARCFOUR_HMAC = 23;
  public static final int ETYPE_ARCFOUR_HMAC_EXP = 24;
  public static final int ETYPE_DES3_CBC_HMAC_SHA1_KD = 16;
  public static final int ETYPE_AES128_CTS_HMAC_SHA1_96 = 17;
  public static final int ETYPE_AES256_CTS_HMAC_SHA1_96 = 18;

  private EncryptedData()
  {
  }

  public Object clone()
  {
    EncryptedData localEncryptedData = new EncryptedData();
    localEncryptedData.eType = this.eType;
    if (this.kvno != null)
      localEncryptedData.kvno = new Integer(this.kvno.intValue());
    if (this.cipher != null)
    {
      localEncryptedData.cipher = new byte[this.cipher.length];
      System.arraycopy(this.cipher, 0, localEncryptedData.cipher, 0, this.cipher.length);
    }
    return localEncryptedData;
  }

  public EncryptedData(int paramInt, Integer paramInteger, byte[] paramArrayOfByte)
  {
    this.eType = paramInt;
    this.kvno = paramInteger;
    this.cipher = paramArrayOfByte;
  }

  public EncryptedData(EncryptionKey paramEncryptionKey, byte[] paramArrayOfByte, int paramInt)
    throws KdcErrException, sun.security.krb5.KrbCryptoException
  {
    EType localEType = EType.getInstance(paramEncryptionKey.getEType());
    this.cipher = localEType.encrypt(paramArrayOfByte, paramEncryptionKey.getBytes(), paramInt);
    this.eType = paramEncryptionKey.getEType();
    this.kvno = paramEncryptionKey.getKeyVersionNumber();
  }

  public byte[] decrypt(EncryptionKey paramEncryptionKey, int paramInt)
    throws KdcErrException, KrbApErrException, sun.security.krb5.KrbCryptoException
  {
    if (this.eType != paramEncryptionKey.getEType())
      throw new sun.security.krb5.KrbCryptoException("EncryptedData is encrypted using keytype " + EType.toString(this.eType) + " but decryption key is of type " + EType.toString(paramEncryptionKey.getEType()));
    EType localEType = EType.getInstance(this.eType);
    this.plain = localEType.decrypt(this.cipher, paramEncryptionKey.getBytes(), paramInt);
    this.cipher = null;
    return localEType.decryptedData(this.plain);
  }

  private byte[] decryptedData()
    throws KdcErrException
  {
    if (this.plain != null)
    {
      EType localEType = EType.getInstance(this.eType);
      return localEType.decryptedData(this.plain);
    }
    return null;
  }

  private EncryptedData(DerValue paramDerValue)
    throws sun.security.krb5.Asn1Exception, IOException
  {
    DerValue localDerValue = null;
    if (paramDerValue.getTag() != 48)
      throw new sun.security.krb5.Asn1Exception(906);
    localDerValue = paramDerValue.getData().getDerValue();
    if ((localDerValue.getTag() & 0x1F) == 0)
      this.eType = localDerValue.getData().getBigInteger().intValue();
    else
      throw new sun.security.krb5.Asn1Exception(906);
    if ((paramDerValue.getData().peekByte() & 0x1F) == 1)
    {
      localDerValue = paramDerValue.getData().getDerValue();
      int i = localDerValue.getData().getBigInteger().intValue();
      this.kvno = new Integer(i);
    }
    else
    {
      this.kvno = null;
    }
    localDerValue = paramDerValue.getData().getDerValue();
    if ((localDerValue.getTag() & 0x1F) == 2)
      this.cipher = localDerValue.getData().getOctetString();
    else
      throw new sun.security.krb5.Asn1Exception(906);
    if (paramDerValue.getData().available() > 0)
      throw new sun.security.krb5.Asn1Exception(906);
  }

  public byte[] asn1Encode()
    throws sun.security.krb5.Asn1Exception, IOException
  {
    DerOutputStream localDerOutputStream1 = new DerOutputStream();
    DerOutputStream localDerOutputStream2 = new DerOutputStream();
    localDerOutputStream2.putInteger(BigInteger.valueOf(this.eType));
    localDerOutputStream1.write(DerValue.createTag(-128, true, 0), localDerOutputStream2);
    localDerOutputStream2 = new DerOutputStream();
    if (this.kvno != null)
    {
      localDerOutputStream2.putInteger(BigInteger.valueOf(this.kvno.intValue()));
      localDerOutputStream1.write(DerValue.createTag(-128, true, 1), localDerOutputStream2);
      localDerOutputStream2 = new DerOutputStream();
    }
    localDerOutputStream2.putOctetString(this.cipher);
    localDerOutputStream1.write(DerValue.createTag(-128, true, 2), localDerOutputStream2);
    localDerOutputStream2 = new DerOutputStream();
    localDerOutputStream2.write(48, localDerOutputStream1);
    return localDerOutputStream2.toByteArray();
  }

  public static EncryptedData parse(DerInputStream paramDerInputStream, byte paramByte, boolean paramBoolean)
    throws sun.security.krb5.Asn1Exception, IOException
  {
    if ((paramBoolean) && (((byte)paramDerInputStream.peekByte() & 0x1F) != paramByte))
      return null;
    DerValue localDerValue1 = paramDerInputStream.getDerValue();
    if (paramByte != (localDerValue1.getTag() & 0x1F))
      throw new sun.security.krb5.Asn1Exception(906);
    DerValue localDerValue2 = localDerValue1.getData().getDerValue();
    return new EncryptedData(localDerValue2);
  }

  public byte[] reset(byte[] paramArrayOfByte, boolean paramBoolean)
  {
    byte[] arrayOfByte = null;
    if (paramBoolean)
    {
      if ((paramArrayOfByte[1] & 0xFF) < 128)
      {
        arrayOfByte = new byte[paramArrayOfByte[1] + 2];
        System.arraycopy(paramArrayOfByte, 0, arrayOfByte, 0, paramArrayOfByte[1] + 2);
      }
      else if ((paramArrayOfByte[1] & 0xFF) > 128)
      {
        int i = paramArrayOfByte[1] & 0x7F;
        int j = 0;
        for (int k = 0; k < i; ++k)
          j |= (paramArrayOfByte[(k + 2)] & 0xFF) << 8 * (i - k - 1);
        arrayOfByte = new byte[j + i + 2];
        System.arraycopy(paramArrayOfByte, 0, arrayOfByte, 0, j + i + 2);
      }
    }
    else
    {
      arrayOfByte = new byte[paramArrayOfByte.length - paramArrayOfByte[(paramArrayOfByte.length - 1)]];
      System.arraycopy(paramArrayOfByte, 0, arrayOfByte, 0, paramArrayOfByte.length - paramArrayOfByte[(paramArrayOfByte.length - 1)]);
    }
    return arrayOfByte;
  }

  public int getEType()
  {
    return this.eType;
  }

  public Integer getKeyVersionNumber()
  {
    return this.kvno;
  }

  public byte[] getBytes()
  {
    return this.cipher;
  }
}