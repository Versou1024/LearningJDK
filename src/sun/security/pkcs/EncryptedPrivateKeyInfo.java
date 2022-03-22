package sun.security.pkcs;

import B;
import java.io.IOException;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.x509.AlgorithmId;

public class EncryptedPrivateKeyInfo
{
  private AlgorithmId algid;
  private byte[] encryptedData;
  private byte[] encoded;

  public EncryptedPrivateKeyInfo(byte[] paramArrayOfByte)
    throws IOException
  {
    if (paramArrayOfByte == null)
      throw new IllegalArgumentException("encoding must not be null");
    DerValue localDerValue = new DerValue(paramArrayOfByte);
    DerValue[] arrayOfDerValue = new DerValue[2];
    arrayOfDerValue[0] = localDerValue.data.getDerValue();
    arrayOfDerValue[1] = localDerValue.data.getDerValue();
    if (localDerValue.data.available() != 0)
      throw new IOException("overrun, bytes = " + localDerValue.data.available());
    this.algid = AlgorithmId.parse(arrayOfDerValue[0]);
    if (arrayOfDerValue[0].data.available() != 0)
      throw new IOException("encryptionAlgorithm field overrun");
    this.encryptedData = arrayOfDerValue[1].getOctetString();
    if (arrayOfDerValue[1].data.available() != 0)
      throw new IOException("encryptedData field overrun");
    this.encoded = ((byte[])(byte[])paramArrayOfByte.clone());
  }

  public EncryptedPrivateKeyInfo(AlgorithmId paramAlgorithmId, byte[] paramArrayOfByte)
  {
    this.algid = paramAlgorithmId;
    this.encryptedData = ((byte[])(byte[])paramArrayOfByte.clone());
  }

  public AlgorithmId getAlgorithm()
  {
    return this.algid;
  }

  public byte[] getEncryptedData()
  {
    return ((byte[])(byte[])this.encryptedData.clone());
  }

  public byte[] getEncoded()
    throws IOException
  {
    if (this.encoded != null)
      return ((byte[])(byte[])this.encoded.clone());
    DerOutputStream localDerOutputStream1 = new DerOutputStream();
    DerOutputStream localDerOutputStream2 = new DerOutputStream();
    this.algid.encode(localDerOutputStream2);
    localDerOutputStream2.putOctetString(this.encryptedData);
    localDerOutputStream1.write(48, localDerOutputStream2);
    this.encoded = localDerOutputStream1.toByteArray();
    return ((byte[])(byte[])this.encoded.clone());
  }

  public boolean equals(Object paramObject)
  {
    if (this == paramObject)
      return true;
    if (!(paramObject instanceof EncryptedPrivateKeyInfo))
      return false;
    try
    {
      byte[] arrayOfByte1 = getEncoded();
      byte[] arrayOfByte2 = ((EncryptedPrivateKeyInfo)paramObject).getEncoded();
      if (arrayOfByte1.length != arrayOfByte2.length)
        return false;
      for (int i = 0; i < arrayOfByte1.length; ++i)
        if (arrayOfByte1[i] != arrayOfByte2[i])
          return false;
      return true;
    }
    catch (IOException localIOException)
    {
    }
    return false;
  }

  public int hashCode()
  {
    int i = 0;
    for (int j = 0; j < this.encryptedData.length; ++j)
      i += this.encryptedData[j] * j;
    return i;
  }
}