package sun.security.krb5;

import B;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import sun.misc.HexDumpEncoder;
import sun.security.krb5.internal.Krb5;
import sun.security.krb5.internal.ccache.CCacheOutputStream;
import sun.security.krb5.internal.crypto.Aes128;
import sun.security.krb5.internal.crypto.Aes256;
import sun.security.krb5.internal.crypto.ArcFourHmac;
import sun.security.krb5.internal.crypto.Des;
import sun.security.krb5.internal.crypto.Des3;
import sun.security.krb5.internal.crypto.EType;
import sun.security.krb5.internal.ktab.KeyTab;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class EncryptionKey
  implements Cloneable
{
  public static final EncryptionKey NULL_KEY = new EncryptionKey(new byte[0], 0, null);
  private int keyType;
  private byte[] keyValue;
  private Integer kvno;
  private static final boolean DEBUG = Krb5.DEBUG;

  public synchronized int getEType()
  {
    return this.keyType;
  }

  public final Integer getKeyVersionNumber()
  {
    return this.kvno;
  }

  public final byte[] getBytes()
  {
    return this.keyValue;
  }

  public synchronized Object clone()
  {
    return new EncryptionKey(this.keyValue, this.keyType, this.kvno);
  }

  public static EncryptionKey[] acquireSecretKeys(PrincipalName paramPrincipalName, String paramString)
    throws sun.security.krb5.KrbException, IOException
  {
    if (paramPrincipalName == null)
      throw new IllegalArgumentException("Cannot have null pricipal name to look in keytab.");
    KeyTab localKeyTab = KeyTab.getInstance(paramString);
    if (localKeyTab == null)
      return null;
    return localKeyTab.readServiceKeys(paramPrincipalName);
  }

  public static EncryptionKey[] acquireSecretKeys(char[] paramArrayOfChar, String paramString)
    throws sun.security.krb5.KrbException
  {
    return acquireSecretKeys(paramArrayOfChar, paramString, false, 0, null);
  }

  public static EncryptionKey[] acquireSecretKeys(char[] paramArrayOfChar, String paramString, boolean paramBoolean, int paramInt, byte[] paramArrayOfByte)
    throws sun.security.krb5.KrbException
  {
    int[] arrayOfInt = EType.getDefaults("default_tkt_enctypes");
    if (arrayOfInt == null)
      arrayOfInt = EType.getBuiltInDefaults();
    if ((paramBoolean) && (paramInt != 0))
    {
      if (DEBUG)
        System.out.println("Pre-Authentication: Set preferred etype = " + paramInt);
      if (EType.isSupported(paramInt))
      {
        arrayOfInt = new int[1];
        arrayOfInt[0] = paramInt;
      }
    }
    EncryptionKey[] arrayOfEncryptionKey = new EncryptionKey[arrayOfInt.length];
    for (int i = 0; i < arrayOfInt.length; ++i)
      if (EType.isSupported(arrayOfInt[i]))
        arrayOfEncryptionKey[i] = new EncryptionKey(stringToKey(paramArrayOfChar, paramString, paramArrayOfByte, arrayOfInt[i]), arrayOfInt[i], null);
      else if (DEBUG)
        System.out.println("Encryption Type " + EType.toString(arrayOfInt[i]) + " is not supported/enabled");
    return arrayOfEncryptionKey;
  }

  public EncryptionKey(byte[] paramArrayOfByte, int paramInt, Integer paramInteger)
  {
    if (paramArrayOfByte != null)
    {
      this.keyValue = new byte[paramArrayOfByte.length];
      System.arraycopy(paramArrayOfByte, 0, this.keyValue, 0, paramArrayOfByte.length);
    }
    else
    {
      throw new IllegalArgumentException("EncryptionKey: Key bytes cannot be null!");
    }
    this.keyType = paramInt;
    this.kvno = paramInteger;
  }

  public EncryptionKey(int paramInt, byte[] paramArrayOfByte)
  {
    this(paramArrayOfByte, paramInt, null);
  }

  private static byte[] stringToKey(char[] paramArrayOfChar, String paramString, byte[] paramArrayOfByte, int paramInt)
    throws sun.security.krb5.KrbCryptoException
  {
    char[] arrayOfChar1 = paramString.toCharArray();
    char[] arrayOfChar2 = new char[paramArrayOfChar.length + arrayOfChar1.length];
    System.arraycopy(paramArrayOfChar, 0, arrayOfChar2, 0, paramArrayOfChar.length);
    System.arraycopy(arrayOfChar1, 0, arrayOfChar2, paramArrayOfChar.length, arrayOfChar1.length);
    Arrays.fill(arrayOfChar1, '0');
    try
    {
      byte[] arrayOfByte;
      switch (paramInt)
      {
      case 1:
      case 3:
        arrayOfByte = Des.string_to_key_bytes(arrayOfChar2);
        return arrayOfByte;
      case 16:
        arrayOfByte = Des3.stringToKey(arrayOfChar2);
        return arrayOfByte;
      case 23:
        arrayOfByte = ArcFourHmac.stringToKey(paramArrayOfChar);
        return arrayOfByte;
      case 17:
        arrayOfByte = Aes128.stringToKey(paramArrayOfChar, paramString, paramArrayOfByte);
        return arrayOfByte;
      case 18:
        arrayOfByte = Aes256.stringToKey(paramArrayOfChar, paramString, paramArrayOfByte);
        Arrays.fill(arrayOfChar2, '0');
      }
      throw new IllegalArgumentException("encryption type " + EType.toString(paramInt) + " not supported");
    }
    catch (GeneralSecurityException localGeneralSecurityException)
    {
      KrbCryptoException localKrbCryptoException = new sun.security.krb5.KrbCryptoException(localGeneralSecurityException.getMessage());
      throw localKrbCryptoException;
    }
    finally
    {
      Arrays.fill(arrayOfChar2, '0');
    }
  }

  public EncryptionKey(char[] paramArrayOfChar, String paramString1, String paramString2)
    throws sun.security.krb5.KrbCryptoException
  {
    if ((paramString2 == null) || (paramString2.equalsIgnoreCase("DES")))
    {
      this.keyType = 3;
    }
    else if (paramString2.equalsIgnoreCase("DESede"))
    {
      this.keyType = 16;
    }
    else if (paramString2.equalsIgnoreCase("AES128"))
    {
      this.keyType = 17;
    }
    else if (paramString2.equalsIgnoreCase("ArcFourHmac"))
    {
      this.keyType = 23;
    }
    else
    {
      if (paramString2.equalsIgnoreCase("AES256"))
      {
        this.keyType = 18;
        if (EType.isSupported(this.keyType))
          break label168;
        throw new IllegalArgumentException("Algorithm " + paramString2 + " not enabled");
      }
      throw new IllegalArgumentException("Algorithm " + paramString2 + " not supported");
    }
    label168: this.keyValue = stringToKey(paramArrayOfChar, paramString1, null, this.keyType);
    this.kvno = null;
  }

  EncryptionKey(EncryptionKey paramEncryptionKey)
    throws sun.security.krb5.KrbCryptoException
  {
    this.keyValue = ((byte[])(byte[])paramEncryptionKey.keyValue.clone());
    this.keyType = paramEncryptionKey.keyType;
  }

  public EncryptionKey(DerValue paramDerValue)
    throws Asn1Exception, IOException
  {
    if (paramDerValue.getTag() != 48)
      throw new Asn1Exception(906);
    DerValue localDerValue = paramDerValue.getData().getDerValue();
    if ((localDerValue.getTag() & 0x1F) == 0)
      this.keyType = localDerValue.getData().getBigInteger().intValue();
    else
      throw new Asn1Exception(906);
    localDerValue = paramDerValue.getData().getDerValue();
    if ((localDerValue.getTag() & 0x1F) == 1)
      this.keyValue = localDerValue.getData().getOctetString();
    else
      throw new Asn1Exception(906);
    if (localDerValue.getData().available() > 0)
      throw new Asn1Exception(906);
  }

  public synchronized byte[] asn1Encode()
    throws Asn1Exception, IOException
  {
    DerOutputStream localDerOutputStream1 = new DerOutputStream();
    DerOutputStream localDerOutputStream2 = new DerOutputStream();
    localDerOutputStream2.putInteger(this.keyType);
    localDerOutputStream1.write(DerValue.createTag(-128, true, 0), localDerOutputStream2);
    localDerOutputStream2 = new DerOutputStream();
    localDerOutputStream2.putOctetString(this.keyValue);
    localDerOutputStream1.write(DerValue.createTag(-128, true, 1), localDerOutputStream2);
    localDerOutputStream2 = new DerOutputStream();
    localDerOutputStream2.write(48, localDerOutputStream1);
    return localDerOutputStream2.toByteArray();
  }

  public synchronized void destroy()
  {
    if (this.keyValue != null)
      for (int i = 0; i < this.keyValue.length; ++i)
        this.keyValue[i] = 0;
  }

  public static EncryptionKey parse(DerInputStream paramDerInputStream, byte paramByte, boolean paramBoolean)
    throws Asn1Exception, IOException
  {
    if ((paramBoolean) && (((byte)paramDerInputStream.peekByte() & 0x1F) != paramByte))
      return null;
    DerValue localDerValue1 = paramDerInputStream.getDerValue();
    if (paramByte != (localDerValue1.getTag() & 0x1F))
      throw new Asn1Exception(906);
    DerValue localDerValue2 = localDerValue1.getData().getDerValue();
    return new EncryptionKey(localDerValue2);
  }

  public synchronized void writeKey(CCacheOutputStream paramCCacheOutputStream)
    throws IOException
  {
    paramCCacheOutputStream.write16(this.keyType);
    paramCCacheOutputStream.write16(this.keyType);
    paramCCacheOutputStream.write32(this.keyValue.length);
    for (int i = 0; i < this.keyValue.length; ++i)
      paramCCacheOutputStream.write8(this.keyValue[i]);
  }

  public String toString()
  {
    return new String("EncryptionKey: keyType=" + this.keyType + " kvno=" + this.kvno + " keyValue (hex dump)=" + new StringBuilder().append('\n').append(Krb5.hexDumper.encode(this.keyValue)).append('\n').toString());
  }

  public static EncryptionKey findKey(int paramInt, EncryptionKey[] paramArrayOfEncryptionKey)
    throws sun.security.krb5.KrbException
  {
    int i;
    if (!(EType.isSupported(paramInt)))
      throw new sun.security.krb5.KrbException("Encryption type " + EType.toString(paramInt) + " is not supported/enabled");
    for (int j = 0; j < paramArrayOfEncryptionKey.length; ++j)
    {
      i = paramArrayOfEncryptionKey[j].getEType();
      if ((EType.isSupported(i)) && (paramInt == i))
        return paramArrayOfEncryptionKey[j];
    }
    if ((paramInt == 1) || (paramInt == 3))
      for (j = 0; j < paramArrayOfEncryptionKey.length; ++j)
      {
        i = paramArrayOfEncryptionKey[j].getEType();
        if ((i == 1) || (i == 3))
          return new EncryptionKey(paramInt, paramArrayOfEncryptionKey[j].getBytes());
      }
    return null;
  }
}