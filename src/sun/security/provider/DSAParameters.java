package sun.security.provider;

import java.io.IOException;
import java.math.BigInteger;
import java.security.AlgorithmParametersSpi;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.DSAParameterSpec;
import java.security.spec.InvalidParameterSpecException;
import sun.security.util.Debug;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class DSAParameters extends AlgorithmParametersSpi
{
  protected BigInteger p;
  protected BigInteger q;
  protected BigInteger g;

  protected void engineInit(AlgorithmParameterSpec paramAlgorithmParameterSpec)
    throws InvalidParameterSpecException
  {
    if (!(paramAlgorithmParameterSpec instanceof DSAParameterSpec))
      throw new InvalidParameterSpecException("Inappropriate parameter specification");
    this.p = ((DSAParameterSpec)paramAlgorithmParameterSpec).getP();
    this.q = ((DSAParameterSpec)paramAlgorithmParameterSpec).getQ();
    this.g = ((DSAParameterSpec)paramAlgorithmParameterSpec).getG();
  }

  protected void engineInit(byte[] paramArrayOfByte)
    throws IOException
  {
    DerValue localDerValue = new DerValue(paramArrayOfByte);
    if (localDerValue.tag != 48)
      throw new IOException("DSA params parsing error");
    localDerValue.data.reset();
    this.p = localDerValue.data.getBigInteger();
    this.q = localDerValue.data.getBigInteger();
    this.g = localDerValue.data.getBigInteger();
    if (localDerValue.data.available() != 0)
      throw new IOException("encoded params have " + localDerValue.data.available() + " extra bytes");
  }

  protected void engineInit(byte[] paramArrayOfByte, String paramString)
    throws IOException
  {
    engineInit(paramArrayOfByte);
  }

  protected <T extends AlgorithmParameterSpec> T engineGetParameterSpec(Class<T> paramClass)
    throws InvalidParameterSpecException
  {
    Class localClass;
    try
    {
      localClass = Class.forName("java.security.spec.DSAParameterSpec");
      if (localClass.isAssignableFrom(paramClass))
        return new DSAParameterSpec(this.p, this.q, this.g);
      throw new InvalidParameterSpecException("Inappropriate parameter Specification");
    }
    catch (ClassNotFoundException localClassNotFoundException)
    {
      throw new InvalidParameterSpecException("Unsupported parameter specification: " + localClassNotFoundException.getMessage());
    }
  }

  protected byte[] engineGetEncoded()
    throws IOException
  {
    DerOutputStream localDerOutputStream1 = new DerOutputStream();
    DerOutputStream localDerOutputStream2 = new DerOutputStream();
    localDerOutputStream2.putInteger(this.p);
    localDerOutputStream2.putInteger(this.q);
    localDerOutputStream2.putInteger(this.g);
    localDerOutputStream1.write(48, localDerOutputStream2);
    return localDerOutputStream1.toByteArray();
  }

  protected byte[] engineGetEncoded(String paramString)
    throws IOException
  {
    return engineGetEncoded();
  }

  protected String engineToString()
  {
    return "\n\tp: " + Debug.toHexString(this.p) + "\n\tq: " + Debug.toHexString(this.q) + "\n\tg: " + Debug.toHexString(this.g) + "\n";
  }
}