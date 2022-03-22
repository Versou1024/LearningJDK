package sun.security.provider;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.security.AlgorithmParameters;
import java.security.InvalidKeyException;
import java.security.interfaces.DSAParams;
import java.security.spec.DSAParameterSpec;
import java.security.spec.InvalidParameterSpecException;
import sun.security.pkcs.PKCS8Key;
import sun.security.util.Debug;
import sun.security.util.DerInputStream;
import sun.security.util.DerValue;
import sun.security.x509.AlgIdDSA;
import sun.security.x509.AlgorithmId;

public final class DSAPrivateKey extends PKCS8Key
  implements java.security.interfaces.DSAPrivateKey, Serializable
{
  private static final long serialVersionUID = -3244453684193605938L;
  private BigInteger x;

  public DSAPrivateKey()
  {
  }

  public DSAPrivateKey(BigInteger paramBigInteger1, BigInteger paramBigInteger2, BigInteger paramBigInteger3, BigInteger paramBigInteger4)
    throws InvalidKeyException
  {
    this.x = paramBigInteger1;
    this.algid = new AlgIdDSA(paramBigInteger2, paramBigInteger3, paramBigInteger4);
    try
    {
      this.key = new DerValue(2, paramBigInteger1.toByteArray()).toByteArray();
      encode();
    }
    catch (IOException localIOException)
    {
      InvalidKeyException localInvalidKeyException = new InvalidKeyException("could not DER encode x: " + localIOException.getMessage());
      localInvalidKeyException.initCause(localIOException);
      throw localInvalidKeyException;
    }
  }

  public DSAPrivateKey(byte[] paramArrayOfByte)
    throws InvalidKeyException
  {
    clearOldKey();
    decode(paramArrayOfByte);
  }

  public DSAParams getParams()
  {
    try
    {
      if (this.algid instanceof DSAParams)
        return ((DSAParams)this.algid);
      AlgorithmParameters localAlgorithmParameters = this.algid.getParameters();
      if (localAlgorithmParameters == null)
        return null;
      DSAParameterSpec localDSAParameterSpec = (DSAParameterSpec)localAlgorithmParameters.getParameterSpec(DSAParameterSpec.class);
      return localDSAParameterSpec;
    }
    catch (InvalidParameterSpecException localInvalidParameterSpecException)
    {
    }
    return null;
  }

  public BigInteger getX()
  {
    return this.x;
  }

  private void clearOldKey()
  {
    int i;
    if (this.encodedKey != null)
      for (i = 0; i < this.encodedKey.length; ++i)
        this.encodedKey[i] = 0;
    if (this.key != null)
      for (i = 0; i < this.key.length; ++i)
        this.key[i] = 0;
  }

  public String toString()
  {
    return "Sun DSA Private Key \nparameters:" + this.algid + "\nx: " + Debug.toHexString(this.x) + "\n";
  }

  protected void parseKeyBits()
    throws InvalidKeyException
  {
    DerInputStream localDerInputStream;
    try
    {
      localDerInputStream = new DerInputStream(this.key);
      this.x = localDerInputStream.getBigInteger();
    }
    catch (IOException localIOException)
    {
      InvalidKeyException localInvalidKeyException = new InvalidKeyException(localIOException.getMessage());
      localInvalidKeyException.initCause(localIOException);
      throw localInvalidKeyException;
    }
  }
}