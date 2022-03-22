package sun.security.krb5;

import java.io.IOException;
import java.io.PrintStream;
import java.math.BigInteger;
import sun.security.krb5.internal.KdcErrException;
import sun.security.krb5.internal.Krb5;
import sun.security.krb5.internal.KrbApErrException;
import sun.security.krb5.internal.crypto.CksumType;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class Checksum
{
  private int cksumType;
  private byte[] checksum;
  public static final int CKSUMTYPE_NULL = 0;
  public static final int CKSUMTYPE_CRC32 = 1;
  public static final int CKSUMTYPE_RSA_MD4 = 2;
  public static final int CKSUMTYPE_RSA_MD4_DES = 3;
  public static final int CKSUMTYPE_DES_MAC = 4;
  public static final int CKSUMTYPE_DES_MAC_K = 5;
  public static final int CKSUMTYPE_RSA_MD4_DES_K = 6;
  public static final int CKSUMTYPE_RSA_MD5 = 7;
  public static final int CKSUMTYPE_RSA_MD5_DES = 8;
  public static final int CKSUMTYPE_HMAC_SHA1_DES3_KD = 12;
  public static final int CKSUMTYPE_HMAC_SHA1_96_AES128 = 15;
  public static final int CKSUMTYPE_HMAC_SHA1_96_AES256 = 16;
  public static final int CKSUMTYPE_HMAC_MD5_ARCFOUR = -138;
  static int CKSUMTYPE_DEFAULT;
  static int SAFECKSUMTYPE_DEFAULT;
  private static boolean DEBUG = Krb5.DEBUG;

  public Checksum(byte[] paramArrayOfByte, int paramInt)
  {
    this.cksumType = paramInt;
    this.checksum = paramArrayOfByte;
  }

  public Checksum(int paramInt, byte[] paramArrayOfByte)
    throws KdcErrException, sun.security.krb5.KrbCryptoException
  {
    this.cksumType = paramInt;
    CksumType localCksumType = CksumType.getInstance(this.cksumType);
    if (!(localCksumType.isSafe()))
      this.checksum = localCksumType.calculateChecksum(paramArrayOfByte, paramArrayOfByte.length);
    else
      throw new KdcErrException(50);
  }

  public Checksum(int paramInt1, byte[] paramArrayOfByte, EncryptionKey paramEncryptionKey, int paramInt2)
    throws KdcErrException, KrbApErrException, sun.security.krb5.KrbCryptoException
  {
    this.cksumType = paramInt1;
    CksumType localCksumType = CksumType.getInstance(this.cksumType);
    if (!(localCksumType.isSafe()))
      throw new KrbApErrException(50);
    this.checksum = localCksumType.calculateKeyedChecksum(paramArrayOfByte, paramArrayOfByte.length, paramEncryptionKey.getBytes(), paramInt2);
  }

  public boolean verifyKeyedChecksum(byte[] paramArrayOfByte, EncryptionKey paramEncryptionKey, int paramInt)
    throws KdcErrException, KrbApErrException, sun.security.krb5.KrbCryptoException
  {
    CksumType localCksumType = CksumType.getInstance(this.cksumType);
    if (!(localCksumType.isSafe()))
      throw new KrbApErrException(50);
    return localCksumType.verifyKeyedChecksum(paramArrayOfByte, paramArrayOfByte.length, paramEncryptionKey.getBytes(), this.checksum, paramInt);
  }

  boolean isEqual(Checksum paramChecksum)
    throws KdcErrException
  {
    if (this.cksumType != paramChecksum.cksumType)
      return false;
    CksumType localCksumType = CksumType.getInstance(this.cksumType);
    return CksumType.isChecksumEqual(this.checksum, paramChecksum.checksum);
  }

  private Checksum(DerValue paramDerValue)
    throws sun.security.krb5.Asn1Exception, IOException
  {
    if (paramDerValue.getTag() != 48)
      throw new sun.security.krb5.Asn1Exception(906);
    DerValue localDerValue = paramDerValue.getData().getDerValue();
    if ((localDerValue.getTag() & 0x1F) == 0)
      this.cksumType = localDerValue.getData().getBigInteger().intValue();
    else
      throw new sun.security.krb5.Asn1Exception(906);
    localDerValue = paramDerValue.getData().getDerValue();
    if ((localDerValue.getTag() & 0x1F) == 1)
      this.checksum = localDerValue.getData().getOctetString();
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
    localDerOutputStream2.putInteger(BigInteger.valueOf(this.cksumType));
    localDerOutputStream1.write(DerValue.createTag(-128, true, 0), localDerOutputStream2);
    localDerOutputStream2 = new DerOutputStream();
    localDerOutputStream2.putOctetString(this.checksum);
    localDerOutputStream1.write(DerValue.createTag(-128, true, 1), localDerOutputStream2);
    localDerOutputStream2 = new DerOutputStream();
    localDerOutputStream2.write(48, localDerOutputStream1);
    return localDerOutputStream2.toByteArray();
  }

  public static Checksum parse(DerInputStream paramDerInputStream, byte paramByte, boolean paramBoolean)
    throws sun.security.krb5.Asn1Exception, IOException
  {
    if ((paramBoolean) && (((byte)paramDerInputStream.peekByte() & 0x1F) != paramByte))
      return null;
    DerValue localDerValue1 = paramDerInputStream.getDerValue();
    if (paramByte != (localDerValue1.getTag() & 0x1F))
      throw new sun.security.krb5.Asn1Exception(906);
    DerValue localDerValue2 = localDerValue1.getData().getDerValue();
    return new Checksum(localDerValue2);
  }

  public final byte[] getBytes()
  {
    return this.checksum;
  }

  public final int getType()
  {
    return this.cksumType;
  }

  static
  {
    String str = null;
    Config localConfig = null;
    try
    {
      localConfig = Config.getInstance();
      str = localConfig.getDefault("default_checksum", "libdefaults");
      if (str != null)
        CKSUMTYPE_DEFAULT = localConfig.getType(str);
      else
        CKSUMTYPE_DEFAULT = 7;
    }
    catch (Exception localException1)
    {
      if (DEBUG)
      {
        System.out.println("Exception in getting default checksum value from the configuration Setting default checksum to be RSA-MD5");
        localException1.printStackTrace();
      }
      CKSUMTYPE_DEFAULT = 7;
    }
    try
    {
      str = localConfig.getDefault("safe_checksum_type", "libdefaults");
      if (str != null)
        SAFECKSUMTYPE_DEFAULT = localConfig.getType(str);
      else
        SAFECKSUMTYPE_DEFAULT = 8;
    }
    catch (Exception localException2)
    {
      if (DEBUG)
      {
        System.out.println("Exception in getting safe default checksum value from the configuration Setting  safe default checksum to be RSA-MD5");
        localException2.printStackTrace();
      }
      SAFECKSUMTYPE_DEFAULT = 8;
    }
  }
}