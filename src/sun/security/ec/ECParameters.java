package sun.security.ec;

import java.io.IOException;
import java.math.BigInteger;
import java.security.AlgorithmParameters;
import java.security.AlgorithmParametersSpi;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.ECField;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.EllipticCurve;
import java.security.spec.InvalidParameterSpecException;
import java.util.Collection;
import java.util.Iterator;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;

public final class ECParameters extends AlgorithmParametersSpi
{
  private ECParameterSpec paramSpec;

  public static ECPoint decodePoint(byte[] paramArrayOfByte, EllipticCurve paramEllipticCurve)
    throws IOException
  {
    if ((paramArrayOfByte.length == 0) || (paramArrayOfByte[0] != 4))
      throw new IOException("Only uncompressed point format supported");
    int i = paramEllipticCurve.getField().getFieldSize() + 7 >> 3;
    if (paramArrayOfByte.length != i * 2 + 1)
      throw new IOException("Point does not match field size");
    byte[] arrayOfByte1 = new byte[i];
    byte[] arrayOfByte2 = new byte[i];
    System.arraycopy(paramArrayOfByte, 1, arrayOfByte1, 0, i);
    System.arraycopy(paramArrayOfByte, i + 1, arrayOfByte2, 0, i);
    return new ECPoint(new BigInteger(1, arrayOfByte1), new BigInteger(1, arrayOfByte2));
  }

  public static byte[] encodePoint(ECPoint paramECPoint, EllipticCurve paramEllipticCurve)
  {
    int i = paramEllipticCurve.getField().getFieldSize() + 7 >> 3;
    byte[] arrayOfByte1 = trimZeroes(paramECPoint.getAffineX().toByteArray());
    byte[] arrayOfByte2 = trimZeroes(paramECPoint.getAffineY().toByteArray());
    if ((arrayOfByte1.length > i) || (arrayOfByte2.length > i))
      throw new RuntimeException("Point coordinates do not match field size");
    byte[] arrayOfByte3 = new byte[1 + (i << 1)];
    arrayOfByte3[0] = 4;
    System.arraycopy(arrayOfByte1, 0, arrayOfByte3, i - arrayOfByte1.length + 1, arrayOfByte1.length);
    System.arraycopy(arrayOfByte2, 0, arrayOfByte3, arrayOfByte3.length - arrayOfByte2.length, arrayOfByte2.length);
    return arrayOfByte3;
  }

  static byte[] trimZeroes(byte[] paramArrayOfByte)
  {
    for (int i = 0; (i < paramArrayOfByte.length - 1) && (paramArrayOfByte[i] == 0); ++i);
    if (i == 0)
      return paramArrayOfByte;
    byte[] arrayOfByte = new byte[paramArrayOfByte.length - i];
    System.arraycopy(paramArrayOfByte, i, arrayOfByte, 0, arrayOfByte.length);
    return arrayOfByte;
  }

  public static NamedCurve getNamedCurve(ECParameterSpec paramECParameterSpec)
  {
    ECParameterSpec localECParameterSpec;
    if ((paramECParameterSpec instanceof NamedCurve) || (paramECParameterSpec == null))
      return ((NamedCurve)paramECParameterSpec);
    int i = paramECParameterSpec.getCurve().getField().getFieldSize();
    Iterator localIterator = NamedCurve.knownECParameterSpecs().iterator();
    while (true)
    {
      while (true)
      {
        while (true)
        {
          while (true)
          {
            while (true)
            {
              if (!(localIterator.hasNext()))
                break label146;
              localECParameterSpec = (ECParameterSpec)localIterator.next();
              if (localECParameterSpec.getCurve().getField().getFieldSize() == i)
                break;
            }
            if (localECParameterSpec.getCurve().equals(paramECParameterSpec.getCurve()))
              break;
          }
          if (localECParameterSpec.getGenerator().equals(paramECParameterSpec.getGenerator()))
            break;
        }
        if (localECParameterSpec.getOrder().equals(paramECParameterSpec.getOrder()))
          break;
      }
      if (localECParameterSpec.getCofactor() == paramECParameterSpec.getCofactor())
        break;
    }
    return ((NamedCurve)localECParameterSpec);
    label146: return null;
  }

  public static String getCurveName(ECParameterSpec paramECParameterSpec)
  {
    NamedCurve localNamedCurve = getNamedCurve(paramECParameterSpec);
    return ((localNamedCurve == null) ? null : localNamedCurve.getObjectIdentifier().toString());
  }

  public static byte[] encodeParameters(ECParameterSpec paramECParameterSpec)
  {
    NamedCurve localNamedCurve = getNamedCurve(paramECParameterSpec);
    if (localNamedCurve == null)
      throw new RuntimeException("Not a known named curve: " + paramECParameterSpec);
    return localNamedCurve.getEncoded();
  }

  public static ECParameterSpec decodeParameters(byte[] paramArrayOfByte)
    throws IOException
  {
    DerValue localDerValue = new DerValue(paramArrayOfByte);
    if (localDerValue.tag == 6)
    {
      ObjectIdentifier localObjectIdentifier = localDerValue.getOID();
      ECParameterSpec localECParameterSpec = NamedCurve.getECParameterSpec(localObjectIdentifier);
      if (localECParameterSpec == null)
        throw new IOException("Unknown named curve: " + localObjectIdentifier);
      return localECParameterSpec;
    }
    throw new IOException("Only named ECParameters supported");
  }

  static AlgorithmParameters getAlgorithmParameters(ECParameterSpec paramECParameterSpec)
    throws InvalidKeyException
  {
    AlgorithmParameters localAlgorithmParameters;
    try
    {
      localAlgorithmParameters = AlgorithmParameters.getInstance("EC", ECKeyFactory.ecInternalProvider);
      localAlgorithmParameters.init(paramECParameterSpec);
      return localAlgorithmParameters;
    }
    catch (GeneralSecurityException localGeneralSecurityException)
    {
      throw new InvalidKeyException("EC parameters error", localGeneralSecurityException);
    }
  }

  protected void engineInit(AlgorithmParameterSpec paramAlgorithmParameterSpec)
    throws InvalidParameterSpecException
  {
    if (paramAlgorithmParameterSpec instanceof ECParameterSpec)
    {
      this.paramSpec = getNamedCurve((ECParameterSpec)paramAlgorithmParameterSpec);
      if (this.paramSpec != null)
        return;
      throw new InvalidParameterSpecException("Not a supported named curve: " + paramAlgorithmParameterSpec);
    }
    if (paramAlgorithmParameterSpec instanceof ECGenParameterSpec)
    {
      String str = ((ECGenParameterSpec)paramAlgorithmParameterSpec).getName();
      ECParameterSpec localECParameterSpec = NamedCurve.getECParameterSpec(str);
      if (localECParameterSpec == null)
        throw new InvalidParameterSpecException("Unknown curve: " + str);
      this.paramSpec = localECParameterSpec;
    }
    else
    {
      if (paramAlgorithmParameterSpec == null)
        throw new InvalidParameterSpecException("paramSpec must not be null");
      throw new InvalidParameterSpecException("Only ECParameterSpec and ECGenParameterSpec supported");
    }
  }

  protected void engineInit(byte[] paramArrayOfByte)
    throws IOException
  {
    this.paramSpec = decodeParameters(paramArrayOfByte);
  }

  protected void engineInit(byte[] paramArrayOfByte, String paramString)
    throws IOException
  {
    engineInit(paramArrayOfByte);
  }

  protected <T extends AlgorithmParameterSpec> T engineGetParameterSpec(Class<T> paramClass)
    throws InvalidParameterSpecException
  {
    if (paramClass.isAssignableFrom(ECParameterSpec.class))
      return this.paramSpec;
    if (paramClass.isAssignableFrom(ECGenParameterSpec.class))
      return new ECGenParameterSpec(getCurveName(this.paramSpec));
    throw new InvalidParameterSpecException("Only ECParameterSpec and ECGenParameterSpec supported");
  }

  protected byte[] engineGetEncoded()
    throws IOException
  {
    return encodeParameters(this.paramSpec);
  }

  protected byte[] engineGetEncoded(String paramString)
    throws IOException
  {
    return engineGetEncoded();
  }

  protected String engineToString()
  {
    return this.paramSpec.toString();
  }
}