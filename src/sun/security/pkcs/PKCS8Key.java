package sun.security.pkcs;

import B;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyRep;
import java.security.KeyRep.Type;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import sun.misc.HexDumpEncoder;
import sun.security.util.Debug;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.x509.AlgorithmId;

public class PKCS8Key
  implements PrivateKey
{
  private static final long serialVersionUID = -3836890099307167124L;
  protected AlgorithmId algid;
  protected byte[] key;
  protected byte[] encodedKey;
  public static final BigInteger version = BigInteger.ZERO;

  public PKCS8Key()
  {
  }

  private PKCS8Key(AlgorithmId paramAlgorithmId, byte[] paramArrayOfByte)
    throws InvalidKeyException
  {
    this.algid = paramAlgorithmId;
    this.key = paramArrayOfByte;
    encode();
  }

  public static PKCS8Key parse(DerValue paramDerValue)
    throws IOException
  {
    PrivateKey localPrivateKey = parseKey(paramDerValue);
    if (localPrivateKey instanceof PKCS8Key)
      return ((PKCS8Key)localPrivateKey);
    throw new IOException("Provider did not return PKCS8Key");
  }

  public static PrivateKey parseKey(DerValue paramDerValue)
    throws IOException
  {
    PrivateKey localPrivateKey;
    if (paramDerValue.tag != 48)
      throw new IOException("corrupt private key");
    BigInteger localBigInteger = paramDerValue.data.getBigInteger();
    if (!(version.equals(localBigInteger)))
      throw new IOException("version mismatch: (supported: " + Debug.toHexString(version) + ", parsed: " + Debug.toHexString(localBigInteger));
    AlgorithmId localAlgorithmId = AlgorithmId.parse(paramDerValue.data.getDerValue());
    try
    {
      localPrivateKey = buildPKCS8Key(localAlgorithmId, paramDerValue.data.getOctetString());
    }
    catch (InvalidKeyException localInvalidKeyException)
    {
      throw new IOException("corrupt private key");
    }
    if (paramDerValue.data.available() != 0)
      throw new IOException("excess private key");
    return localPrivateKey;
  }

  protected void parseKeyBits()
    throws IOException, InvalidKeyException
  {
    encode();
  }

  static PrivateKey buildPKCS8Key(AlgorithmId paramAlgorithmId, byte[] paramArrayOfByte)
    throws IOException, InvalidKeyException
  {
    DerOutputStream localDerOutputStream = new DerOutputStream();
    encode(localDerOutputStream, paramAlgorithmId, paramArrayOfByte);
    PKCS8EncodedKeySpec localPKCS8EncodedKeySpec = new PKCS8EncodedKeySpec(localDerOutputStream.toByteArray());
    try
    {
      KeyFactory localKeyFactory = KeyFactory.getInstance(paramAlgorithmId.getName());
      return localKeyFactory.generatePrivate(localPKCS8EncodedKeySpec);
    }
    catch (NoSuchAlgorithmException localNoSuchAlgorithmException)
    {
    }
    catch (InvalidKeySpecException localInvalidKeySpecException)
    {
    }
    String str = "";
    try
    {
      Object localObject2;
      Provider localProvider = Security.getProvider("SUN");
      if (localProvider == null)
        throw new InstantiationException();
      str = localProvider.getProperty("PrivateKey.PKCS#8." + paramAlgorithmId.getName());
      if (str == null)
        throw new InstantiationException();
      Class localClass = null;
      try
      {
        localClass = Class.forName(str);
      }
      catch (ClassNotFoundException localClassNotFoundException2)
      {
        localObject2 = ClassLoader.getSystemClassLoader();
        if (localObject2 != null)
          localClass = ((ClassLoader)localObject2).loadClass(str);
      }
      Object localObject1 = null;
      if (localClass != null)
        localObject1 = localClass.newInstance();
      if (localObject1 instanceof PKCS8Key)
      {
        localObject2 = (PKCS8Key)localObject1;
        ((PKCS8Key)localObject2).algid = paramAlgorithmId;
        ((PKCS8Key)localObject2).key = paramArrayOfByte;
        ((PKCS8Key)localObject2).parseKeyBits();
        return localObject2;
      }
    }
    catch (ClassNotFoundException localClassNotFoundException1)
    {
    }
    catch (InstantiationException localInstantiationException)
    {
    }
    catch (IllegalAccessException localIllegalAccessException)
    {
      throw new IOException(str + " [internal error]");
    }
    PKCS8Key localPKCS8Key = new PKCS8Key();
    localPKCS8Key.algid = paramAlgorithmId;
    localPKCS8Key.key = paramArrayOfByte;
    return ((PrivateKey)localPKCS8Key);
  }

  public String getAlgorithm()
  {
    return this.algid.getName();
  }

  public AlgorithmId getAlgorithmId()
  {
    return this.algid;
  }

  public final void encode(DerOutputStream paramDerOutputStream)
    throws IOException
  {
    encode(paramDerOutputStream, this.algid, this.key);
  }

  public synchronized byte[] getEncoded()
  {
    byte[] arrayOfByte = null;
    try
    {
      arrayOfByte = encode();
    }
    catch (InvalidKeyException localInvalidKeyException)
    {
    }
    return arrayOfByte;
  }

  public String getFormat()
  {
    return "PKCS#8";
  }

  public byte[] encode()
    throws InvalidKeyException
  {
    if (this.encodedKey == null)
      try
      {
        DerOutputStream localDerOutputStream = new DerOutputStream();
        encode(localDerOutputStream);
        this.encodedKey = localDerOutputStream.toByteArray();
      }
      catch (IOException localIOException)
      {
        throw new InvalidKeyException("IOException : " + localIOException.getMessage());
      }
    return ((byte[])(byte[])this.encodedKey.clone());
  }

  public String toString()
  {
    HexDumpEncoder localHexDumpEncoder = new HexDumpEncoder();
    return "algorithm = " + this.algid.toString() + ", unparsed keybits = \n" + localHexDumpEncoder.encodeBuffer(this.key);
  }

  public void decode(InputStream paramInputStream)
    throws InvalidKeyException
  {
    DerValue localDerValue;
    try
    {
      localDerValue = new DerValue(paramInputStream);
      if (localDerValue.tag != 48)
        throw new InvalidKeyException("invalid key format");
      BigInteger localBigInteger = localDerValue.data.getBigInteger();
      if (!(localBigInteger.equals(version)))
        throw new IOException("version mismatch: (supported: " + Debug.toHexString(version) + ", parsed: " + Debug.toHexString(localBigInteger));
      this.algid = AlgorithmId.parse(localDerValue.data.getDerValue());
      this.key = localDerValue.data.getOctetString();
      parseKeyBits();
      if (localDerValue.data.available() != 0);
    }
    catch (IOException localIOException)
    {
      throw new InvalidKeyException("IOException : " + localIOException.getMessage());
    }
  }

  public void decode(byte[] paramArrayOfByte)
    throws InvalidKeyException
  {
    decode(new ByteArrayInputStream(paramArrayOfByte));
  }

  protected Object writeReplace()
    throws ObjectStreamException
  {
    return new KeyRep(KeyRep.Type.PRIVATE, getAlgorithm(), getFormat(), getEncoded());
  }

  private void readObject(ObjectInputStream paramObjectInputStream)
    throws IOException
  {
    try
    {
      decode(paramObjectInputStream);
    }
    catch (InvalidKeyException localInvalidKeyException)
    {
      localInvalidKeyException.printStackTrace();
      throw new IOException("deserialized key is invalid: " + localInvalidKeyException.getMessage());
    }
  }

  static void encode(DerOutputStream paramDerOutputStream, AlgorithmId paramAlgorithmId, byte[] paramArrayOfByte)
    throws IOException
  {
    DerOutputStream localDerOutputStream = new DerOutputStream();
    localDerOutputStream.putInteger(version);
    paramAlgorithmId.encode(localDerOutputStream);
    localDerOutputStream.putOctetString(paramArrayOfByte);
    paramDerOutputStream.write(48, localDerOutputStream);
  }

  public boolean equals(Object paramObject)
  {
    if (this == paramObject)
      return true;
    if (paramObject instanceof Key)
    {
      byte[] arrayOfByte1;
      if (this.encodedKey != null)
        arrayOfByte1 = this.encodedKey;
      else
        arrayOfByte1 = getEncoded();
      byte[] arrayOfByte2 = ((Key)paramObject).getEncoded();
      if (arrayOfByte1.length != arrayOfByte2.length)
        return false;
      for (int i = 0; i < arrayOfByte1.length; ++i)
        if (arrayOfByte1[i] != arrayOfByte2[i])
          return false;
      return true;
    }
    return false;
  }

  public int hashCode()
  {
    int i = 0;
    byte[] arrayOfByte = getEncoded();
    for (int j = 1; j < arrayOfByte.length; ++j)
      i += arrayOfByte[j] * j;
    return i;
  }
}