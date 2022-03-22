package sun.security.x509;

import B;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.PublicKey;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import sun.misc.HexDumpEncoder;
import sun.security.util.BitArray;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class X509Key
  implements PublicKey
{
  private static final long serialVersionUID = -5359250853002055002L;
  protected AlgorithmId algid;

  @Deprecated
  protected byte[] key = null;
  private int unusedBits = 0;
  private BitArray bitStringKey = null;
  protected byte[] encodedKey;

  public X509Key()
  {
  }

  private X509Key(AlgorithmId paramAlgorithmId, BitArray paramBitArray)
    throws InvalidKeyException
  {
    this.algid = paramAlgorithmId;
    setKey(paramBitArray);
    encode();
  }

  protected void setKey(BitArray paramBitArray)
  {
    this.bitStringKey = ((BitArray)paramBitArray.clone());
    this.key = paramBitArray.toByteArray();
    int i = paramBitArray.length() % 8;
    this.unusedBits = ((i == 0) ? 0 : 8 - i);
  }

  protected BitArray getKey()
  {
    this.bitStringKey = new BitArray(this.key.length * 8 - this.unusedBits, this.key);
    return ((BitArray)this.bitStringKey.clone());
  }

  public static PublicKey parse(DerValue paramDerValue)
    throws IOException
  {
    PublicKey localPublicKey;
    if (paramDerValue.tag != 48)
      throw new IOException("corrupt subject key");
    AlgorithmId localAlgorithmId = AlgorithmId.parse(paramDerValue.data.getDerValue());
    try
    {
      localPublicKey = buildX509Key(localAlgorithmId, paramDerValue.data.getUnalignedBitString());
    }
    catch (InvalidKeyException localInvalidKeyException)
    {
      throw new IOException("subject key, " + localInvalidKeyException.getMessage());
    }
    if (paramDerValue.data.available() != 0)
      throw new IOException("excess subject key");
    return localPublicKey;
  }

  protected void parseKeyBits()
    throws IOException, InvalidKeyException
  {
    encode();
  }

  static PublicKey buildX509Key(AlgorithmId paramAlgorithmId, BitArray paramBitArray)
    throws IOException, InvalidKeyException
  {
    DerOutputStream localDerOutputStream = new DerOutputStream();
    encode(localDerOutputStream, paramAlgorithmId, paramBitArray);
    X509EncodedKeySpec localX509EncodedKeySpec = new X509EncodedKeySpec(localDerOutputStream.toByteArray());
    try
    {
      KeyFactory localKeyFactory = KeyFactory.getInstance(paramAlgorithmId.getName());
      return localKeyFactory.generatePublic(localX509EncodedKeySpec);
    }
    catch (NoSuchAlgorithmException localNoSuchAlgorithmException)
    {
    }
    catch (InvalidKeySpecException localInvalidKeySpecException)
    {
      throw new InvalidKeyException(localInvalidKeySpecException.getMessage());
    }
    String str = "";
    try
    {
      Object localObject2;
      Provider localProvider = Security.getProvider("SUN");
      if (localProvider == null)
        throw new InstantiationException();
      str = localProvider.getProperty("PublicKey.X.509." + paramAlgorithmId.getName());
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
      if (localObject1 instanceof X509Key)
      {
        localObject2 = (X509Key)localObject1;
        ((X509Key)localObject2).algid = paramAlgorithmId;
        ((X509Key)localObject2).setKey(paramBitArray);
        ((X509Key)localObject2).parseKeyBits();
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
    X509Key localX509Key = new X509Key(paramAlgorithmId, paramBitArray);
    return ((PublicKey)localX509Key);
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
    encode(paramDerOutputStream, this.algid, getKey());
  }

  public byte[] getEncoded()
  {
    try
    {
      return ((byte[])(byte[])getEncodedInternal().clone());
    }
    catch (InvalidKeyException localInvalidKeyException)
    {
    }
    return null;
  }

  public byte[] getEncodedInternal()
    throws InvalidKeyException
  {
    byte[] arrayOfByte = this.encodedKey;
    if (arrayOfByte == null)
    {
      try
      {
        DerOutputStream localDerOutputStream = new DerOutputStream();
        encode(localDerOutputStream);
        arrayOfByte = localDerOutputStream.toByteArray();
      }
      catch (IOException localIOException)
      {
        throw new InvalidKeyException("IOException : " + localIOException.getMessage());
      }
      this.encodedKey = arrayOfByte;
    }
    return arrayOfByte;
  }

  public String getFormat()
  {
    return "X.509";
  }

  public byte[] encode()
    throws InvalidKeyException
  {
    return ((byte[])(byte[])getEncodedInternal().clone());
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
      this.algid = AlgorithmId.parse(localDerValue.data.getDerValue());
      setKey(localDerValue.data.getUnalignedBitString());
      parseKeyBits();
      if (localDerValue.data.available() != 0)
        throw new InvalidKeyException("excess key data");
    }
    catch (IOException localIOException)
    {
      throw new InvalidKeyException("IOException: " + localIOException.getMessage());
    }
  }

  public void decode(byte[] paramArrayOfByte)
    throws InvalidKeyException
  {
    decode(new ByteArrayInputStream(paramArrayOfByte));
  }

  private void writeObject(ObjectOutputStream paramObjectOutputStream)
    throws IOException
  {
    paramObjectOutputStream.write(getEncoded());
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

  public boolean equals(Object paramObject)
  {
    if (this == paramObject)
      return true;
    if (!(paramObject instanceof Key))
      return false;
    try
    {
      byte[] arrayOfByte2;
      byte[] arrayOfByte1 = getEncodedInternal();
      if (paramObject instanceof X509Key)
        arrayOfByte2 = ((X509Key)paramObject).getEncodedInternal();
      else
        arrayOfByte2 = ((Key)paramObject).getEncoded();
      return Arrays.equals(arrayOfByte1, arrayOfByte2);
    }
    catch (InvalidKeyException localInvalidKeyException)
    {
    }
    return false;
  }

  public int hashCode()
  {
    byte[] arrayOfByte;
    try
    {
      arrayOfByte = getEncodedInternal();
      int i = arrayOfByte.length;
      for (int j = 0; j < arrayOfByte.length; ++j)
        i += (arrayOfByte[j] & 0xFF) * 37;
      return i;
    }
    catch (InvalidKeyException localInvalidKeyException)
    {
    }
    return 0;
  }

  static void encode(DerOutputStream paramDerOutputStream, AlgorithmId paramAlgorithmId, BitArray paramBitArray)
    throws IOException
  {
    DerOutputStream localDerOutputStream = new DerOutputStream();
    paramAlgorithmId.encode(localDerOutputStream);
    localDerOutputStream.putUnalignedBitString(paramBitArray);
    paramDerOutputStream.write(48, localDerOutputStream);
  }
}