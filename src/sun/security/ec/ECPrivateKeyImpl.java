package sun.security.ec;

import java.io.IOException;
import java.math.BigInteger;
import java.security.AlgorithmParameters;
import java.security.InvalidKeyException;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.ECField;
import java.security.spec.ECParameterSpec;
import java.security.spec.EllipticCurve;
import java.security.spec.InvalidParameterSpecException;
import sun.security.pkcs.PKCS8Key;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.x509.AlgorithmId;

public final class ECPrivateKeyImpl extends PKCS8Key
  implements ECPrivateKey
{
  private static final long serialVersionUID = 88695385615075129L;
  private BigInteger s;
  private ECParameterSpec params;

  public ECPrivateKeyImpl(byte[] paramArrayOfByte)
    throws InvalidKeyException
  {
    decode(paramArrayOfByte);
  }

  public ECPrivateKeyImpl(BigInteger paramBigInteger, ECParameterSpec paramECParameterSpec)
    throws InvalidKeyException
  {
    this.s = paramBigInteger;
    this.params = paramECParameterSpec;
    this.algid = new AlgorithmId(AlgorithmId.EC_oid, ECParameters.getAlgorithmParameters(paramECParameterSpec));
    try
    {
      DerOutputStream localDerOutputStream = new DerOutputStream();
      localDerOutputStream.putInteger(1);
      byte[] arrayOfByte = ECParameters.trimZeroes(paramBigInteger.toByteArray());
      localDerOutputStream.putOctetString(arrayOfByte);
      DerValue localDerValue = new DerValue(48, localDerOutputStream.toByteArray());
      this.key = localDerValue.toByteArray();
    }
    catch (IOException localIOException)
    {
      throw new InvalidKeyException(localIOException);
    }
  }

  public String getAlgorithm()
  {
    return "EC";
  }

  public BigInteger getS()
  {
    return this.s;
  }

  public ECParameterSpec getParams()
  {
    return this.params;
  }

  protected void parseKeyBits()
    throws InvalidKeyException
  {
    DerInputStream localDerInputStream1;
    try
    {
      localDerInputStream1 = new DerInputStream(this.key);
      DerValue localDerValue = localDerInputStream1.getDerValue();
      if (localDerValue.tag != 48)
        throw new IOException("Not a SEQUENCE");
      DerInputStream localDerInputStream2 = localDerValue.data;
      int i = localDerInputStream2.getInteger();
      if (i != 1)
        throw new IOException("Version must be 1");
      byte[] arrayOfByte = localDerInputStream2.getOctetString();
      this.s = new BigInteger(1, arrayOfByte);
      while (localDerInputStream2.available() != 0)
      {
        localObject = localDerInputStream2.getDerValue();
        if (((DerValue)localObject).isContextSpecific(0))
          continue;
        if (((DerValue)localObject).isContextSpecific(1))
          continue;
        throw new InvalidKeyException("Unexpected value: " + localObject);
      }
      Object localObject = this.algid.getParameters();
      if (localObject == null)
        throw new InvalidKeyException("EC domain parameters must be encoded in the algorithm identifier");
      this.params = ((ECParameterSpec)((AlgorithmParameters)localObject).getParameterSpec(ECParameterSpec.class));
    }
    catch (IOException localIOException)
    {
      throw new InvalidKeyException("Invalid EC private key", localIOException);
    }
    catch (InvalidParameterSpecException localInvalidParameterSpecException)
    {
      throw new InvalidKeyException("Invalid EC private key", localInvalidParameterSpecException);
    }
  }

  public String toString()
  {
    return "Sun EC private key, " + this.params.getCurve().getField().getFieldSize() + " bits\n  private value:  " + this.s + "\n  parameters: " + this.params;
  }
}