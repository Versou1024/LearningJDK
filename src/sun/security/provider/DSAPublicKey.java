package sun.security.provider;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.security.AlgorithmParameters;
import java.security.InvalidKeyException;
import java.security.interfaces.DSAParams;
import java.security.spec.DSAParameterSpec;
import java.security.spec.InvalidParameterSpecException;
import sun.security.util.Debug;
import sun.security.util.DerInputStream;
import sun.security.util.DerValue;
import sun.security.x509.AlgIdDSA;
import sun.security.x509.AlgorithmId;
import sun.security.x509.X509Key;

public class DSAPublicKey extends X509Key
  implements java.security.interfaces.DSAPublicKey, Serializable
{
  private static final long serialVersionUID = -2994193307391104133L;
  private BigInteger y;

  public DSAPublicKey()
  {
  }

  public DSAPublicKey(BigInteger paramBigInteger1, BigInteger paramBigInteger2, BigInteger paramBigInteger3, BigInteger paramBigInteger4)
    throws InvalidKeyException
  {
    this.y = paramBigInteger1;
    this.algid = new AlgIdDSA(paramBigInteger2, paramBigInteger3, paramBigInteger4);
    try
    {
      this.key = new DerValue(2, paramBigInteger1.toByteArray()).toByteArray();
      encode();
    }
    catch (IOException localIOException)
    {
      throw new InvalidKeyException("could not DER encode y: " + localIOException.getMessage());
    }
  }

  public DSAPublicKey(byte[] paramArrayOfByte)
    throws InvalidKeyException
  {
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

  public BigInteger getY()
  {
    return this.y;
  }

  public String toString()
  {
    return "Sun DSA Public Key\n    Parameters:" + this.algid + "\n  y:\n" + Debug.toHexString(this.y) + "\n";
  }

  protected void parseKeyBits()
    throws InvalidKeyException
  {
    DerInputStream localDerInputStream;
    try
    {
      localDerInputStream = new DerInputStream(this.key);
      this.y = localDerInputStream.getBigInteger();
    }
    catch (IOException localIOException)
    {
      throw new InvalidKeyException("Invalid key: y value\n" + localIOException.getMessage());
    }
  }
}