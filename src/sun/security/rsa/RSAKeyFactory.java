package sun.security.rsa;

import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactorySpi;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAKey;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;

public final class RSAKeyFactory extends KeyFactorySpi
{
  private static final Class rsaPublicKeySpecClass = RSAPublicKeySpec.class;
  private static final Class rsaPrivateKeySpecClass = RSAPrivateKeySpec.class;
  private static final Class rsaPrivateCrtKeySpecClass = RSAPrivateCrtKeySpec.class;
  private static final Class x509KeySpecClass = X509EncodedKeySpec.class;
  private static final Class pkcs8KeySpecClass = PKCS8EncodedKeySpec.class;
  private static final RSAKeyFactory INSTANCE = new RSAKeyFactory();

  public static RSAKey toRSAKey(Key paramKey)
    throws InvalidKeyException
  {
    if (paramKey instanceof RSAKey)
    {
      RSAKey localRSAKey = (RSAKey)paramKey;
      checkKey(localRSAKey);
      return localRSAKey;
    }
    return ((RSAKey)INSTANCE.engineTranslateKey(paramKey));
  }

  private static void checkKey(RSAKey paramRSAKey)
    throws InvalidKeyException
  {
    label47: BigInteger localBigInteger;
    if (paramRSAKey instanceof RSAPublicKey)
    {
      if (!(paramRSAKey instanceof RSAPublicKeyImpl))
        break label47;
      return;
    }
    if (paramRSAKey instanceof RSAPrivateKey)
    {
      if ((!(paramRSAKey instanceof RSAPrivateCrtKeyImpl)) && (!(paramRSAKey instanceof RSAPrivateKeyImpl)))
        break label47;
      return;
    }
    throw new InvalidKeyException("Neither a public nor a private key");
    String str = ((Key)paramRSAKey).getAlgorithm();
    if (!(str.equals("RSA")))
      throw new InvalidKeyException("Not an RSA key: " + str);
    try
    {
      localBigInteger = paramRSAKey.getModulus();
      if (localBigInteger == null)
        throw new InvalidKeyException("Modulus is missing");
    }
    catch (RuntimeException localRuntimeException)
    {
      throw new InvalidKeyException(localRuntimeException);
    }
    checkKeyLength(localBigInteger);
  }

  static void checkKeyLength(BigInteger paramBigInteger)
    throws InvalidKeyException
  {
    if (paramBigInteger.bitLength() < 505)
      throw new InvalidKeyException("RSA keys must be at least 512 bits long");
  }

  protected Key engineTranslateKey(Key paramKey)
    throws InvalidKeyException
  {
    if (paramKey == null)
      throw new InvalidKeyException("Key must not be null");
    String str = paramKey.getAlgorithm();
    if (!(str.equals("RSA")))
      throw new InvalidKeyException("Not an RSA key: " + str);
    if (paramKey instanceof PublicKey)
      return translatePublicKey((PublicKey)paramKey);
    if (paramKey instanceof PrivateKey)
      return translatePrivateKey((PrivateKey)paramKey);
    throw new InvalidKeyException("Neither a public nor a private key");
  }

  protected PublicKey engineGeneratePublic(KeySpec paramKeySpec)
    throws InvalidKeySpecException
  {
    try
    {
      return generatePublic(paramKeySpec);
    }
    catch (InvalidKeySpecException localInvalidKeySpecException)
    {
      throw localInvalidKeySpecException;
    }
    catch (GeneralSecurityException localGeneralSecurityException)
    {
      throw new InvalidKeySpecException(localGeneralSecurityException);
    }
  }

  protected PrivateKey engineGeneratePrivate(KeySpec paramKeySpec)
    throws InvalidKeySpecException
  {
    try
    {
      return generatePrivate(paramKeySpec);
    }
    catch (InvalidKeySpecException localInvalidKeySpecException)
    {
      throw localInvalidKeySpecException;
    }
    catch (GeneralSecurityException localGeneralSecurityException)
    {
      throw new InvalidKeySpecException(localGeneralSecurityException);
    }
  }

  private PublicKey translatePublicKey(PublicKey paramPublicKey)
    throws InvalidKeyException
  {
    Object localObject;
    if (paramPublicKey instanceof RSAPublicKey)
    {
      if (paramPublicKey instanceof RSAPublicKeyImpl)
        return paramPublicKey;
      localObject = (RSAPublicKey)paramPublicKey;
      try
      {
        return new RSAPublicKeyImpl(((RSAPublicKey)localObject).getModulus(), ((RSAPublicKey)localObject).getPublicExponent());
      }
      catch (RuntimeException localRuntimeException)
      {
        throw new InvalidKeyException("Invalid key", localRuntimeException);
      }
    }
    if ("X.509".equals(paramPublicKey.getFormat()))
    {
      localObject = paramPublicKey.getEncoded();
      return new RSAPublicKeyImpl(localObject);
    }
    throw new InvalidKeyException("Public keys must be instance of RSAPublicKey or have X.509 encoding");
  }

  private PrivateKey translatePrivateKey(PrivateKey paramPrivateKey)
    throws InvalidKeyException
  {
    Object localObject;
    if (paramPrivateKey instanceof RSAPrivateCrtKey)
    {
      if (paramPrivateKey instanceof RSAPrivateCrtKeyImpl)
        return paramPrivateKey;
      localObject = (RSAPrivateCrtKey)paramPrivateKey;
      try
      {
        return new RSAPrivateCrtKeyImpl(((RSAPrivateCrtKey)localObject).getModulus(), ((RSAPrivateCrtKey)localObject).getPublicExponent(), ((RSAPrivateCrtKey)localObject).getPrivateExponent(), ((RSAPrivateCrtKey)localObject).getPrimeP(), ((RSAPrivateCrtKey)localObject).getPrimeQ(), ((RSAPrivateCrtKey)localObject).getPrimeExponentP(), ((RSAPrivateCrtKey)localObject).getPrimeExponentQ(), ((RSAPrivateCrtKey)localObject).getCrtCoefficient());
      }
      catch (RuntimeException localRuntimeException1)
      {
        throw new InvalidKeyException("Invalid key", localRuntimeException1);
      }
    }
    if (paramPrivateKey instanceof RSAPrivateKey)
    {
      if (paramPrivateKey instanceof RSAPrivateKeyImpl)
        return paramPrivateKey;
      localObject = (RSAPrivateKey)paramPrivateKey;
      try
      {
        return new RSAPrivateKeyImpl(((RSAPrivateKey)localObject).getModulus(), ((RSAPrivateKey)localObject).getPrivateExponent());
      }
      catch (RuntimeException localRuntimeException2)
      {
        throw new InvalidKeyException("Invalid key", localRuntimeException2);
      }
    }
    if ("PKCS#8".equals(paramPrivateKey.getFormat()))
    {
      localObject = paramPrivateKey.getEncoded();
      return RSAPrivateCrtKeyImpl.newKey(localObject);
    }
    throw new InvalidKeyException("Private keys must be instance of RSAPrivate(Crt)Key or have PKCS#8 encoding");
  }

  private PublicKey generatePublic(KeySpec paramKeySpec)
    throws GeneralSecurityException
  {
    Object localObject;
    if (paramKeySpec instanceof X509EncodedKeySpec)
    {
      localObject = (X509EncodedKeySpec)paramKeySpec;
      return new RSAPublicKeyImpl(((X509EncodedKeySpec)localObject).getEncoded());
    }
    if (paramKeySpec instanceof RSAPublicKeySpec)
    {
      localObject = (RSAPublicKeySpec)paramKeySpec;
      return new RSAPublicKeyImpl(((RSAPublicKeySpec)localObject).getModulus(), ((RSAPublicKeySpec)localObject).getPublicExponent());
    }
    throw new InvalidKeySpecException("Only RSAPublicKeySpec and X509EncodedKeySpec supported for RSA public keys");
  }

  private PrivateKey generatePrivate(KeySpec paramKeySpec)
    throws GeneralSecurityException
  {
    Object localObject;
    if (paramKeySpec instanceof PKCS8EncodedKeySpec)
    {
      localObject = (PKCS8EncodedKeySpec)paramKeySpec;
      return RSAPrivateCrtKeyImpl.newKey(((PKCS8EncodedKeySpec)localObject).getEncoded());
    }
    if (paramKeySpec instanceof RSAPrivateCrtKeySpec)
    {
      localObject = (RSAPrivateCrtKeySpec)paramKeySpec;
      return new RSAPrivateCrtKeyImpl(((RSAPrivateCrtKeySpec)localObject).getModulus(), ((RSAPrivateCrtKeySpec)localObject).getPublicExponent(), ((RSAPrivateCrtKeySpec)localObject).getPrivateExponent(), ((RSAPrivateCrtKeySpec)localObject).getPrimeP(), ((RSAPrivateCrtKeySpec)localObject).getPrimeQ(), ((RSAPrivateCrtKeySpec)localObject).getPrimeExponentP(), ((RSAPrivateCrtKeySpec)localObject).getPrimeExponentQ(), ((RSAPrivateCrtKeySpec)localObject).getCrtCoefficient());
    }
    if (paramKeySpec instanceof RSAPrivateKeySpec)
    {
      localObject = (RSAPrivateKeySpec)paramKeySpec;
      return new RSAPrivateKeyImpl(((RSAPrivateKeySpec)localObject).getModulus(), ((RSAPrivateKeySpec)localObject).getPrivateExponent());
    }
    throw new InvalidKeySpecException("Only RSAPrivate(Crt)KeySpec and PKCS8EncodedKeySpec supported for RSA private keys");
  }

  protected <T extends KeySpec> T engineGetKeySpec(Key paramKey, Class<T> paramClass)
    throws InvalidKeySpecException
  {
    Object localObject;
    try
    {
      paramKey = engineTranslateKey(paramKey);
    }
    catch (InvalidKeyException localInvalidKeyException)
    {
      throw new InvalidKeySpecException(localInvalidKeyException);
    }
    if (paramKey instanceof RSAPublicKey)
    {
      localObject = (RSAPublicKey)paramKey;
      if (rsaPublicKeySpecClass.isAssignableFrom(paramClass))
        return new RSAPublicKeySpec(((RSAPublicKey)localObject).getModulus(), ((RSAPublicKey)localObject).getPublicExponent());
      if (x509KeySpecClass.isAssignableFrom(paramClass))
        return new X509EncodedKeySpec(paramKey.getEncoded());
      throw new InvalidKeySpecException("KeySpec must be RSAPublicKeySpec or X509EncodedKeySpec for RSA public keys");
    }
    if (paramKey instanceof RSAPrivateKey)
    {
      if (pkcs8KeySpecClass.isAssignableFrom(paramClass))
        return new PKCS8EncodedKeySpec(paramKey.getEncoded());
      if (rsaPrivateCrtKeySpecClass.isAssignableFrom(paramClass))
      {
        if (paramKey instanceof RSAPrivateCrtKey)
        {
          localObject = (RSAPrivateCrtKey)paramKey;
          return new RSAPrivateCrtKeySpec(((RSAPrivateCrtKey)localObject).getModulus(), ((RSAPrivateCrtKey)localObject).getPublicExponent(), ((RSAPrivateCrtKey)localObject).getPrivateExponent(), ((RSAPrivateCrtKey)localObject).getPrimeP(), ((RSAPrivateCrtKey)localObject).getPrimeQ(), ((RSAPrivateCrtKey)localObject).getPrimeExponentP(), ((RSAPrivateCrtKey)localObject).getPrimeExponentQ(), ((RSAPrivateCrtKey)localObject).getCrtCoefficient());
        }
        throw new InvalidKeySpecException("RSAPrivateCrtKeySpec can only be used with CRT keys");
      }
      if (rsaPrivateKeySpecClass.isAssignableFrom(paramClass))
      {
        localObject = (RSAPrivateKey)paramKey;
        return new RSAPrivateKeySpec(((RSAPrivateKey)localObject).getModulus(), ((RSAPrivateKey)localObject).getPrivateExponent());
      }
      throw new InvalidKeySpecException("KeySpec must be RSAPrivate(Crt)KeySpec or PKCS8EncodedKeySpec for RSA private keys");
    }
    throw new InvalidKeySpecException("Neither public nor private key");
  }
}