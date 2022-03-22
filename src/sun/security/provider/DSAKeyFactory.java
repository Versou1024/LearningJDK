package sun.security.provider;

import java.security.AccessController;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactorySpi;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.DSAParams;
import java.security.spec.DSAPrivateKeySpec;
import java.security.spec.DSAPublicKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import sun.security.action.GetPropertyAction;

public class DSAKeyFactory extends KeyFactorySpi
{
  static final boolean SERIAL_INTEROP;
  private static final String SERIAL_PROP = "sun.security.key.serial.interop";

  protected PublicKey engineGeneratePublic(KeySpec paramKeySpec)
    throws InvalidKeySpecException
  {
    try
    {
      if (paramKeySpec instanceof DSAPublicKeySpec)
      {
        DSAPublicKeySpec localDSAPublicKeySpec = (DSAPublicKeySpec)paramKeySpec;
        if (SERIAL_INTEROP)
          return new DSAPublicKey(localDSAPublicKeySpec.getY(), localDSAPublicKeySpec.getP(), localDSAPublicKeySpec.getQ(), localDSAPublicKeySpec.getG());
        return new DSAPublicKeyImpl(localDSAPublicKeySpec.getY(), localDSAPublicKeySpec.getP(), localDSAPublicKeySpec.getQ(), localDSAPublicKeySpec.getG());
      }
      if (paramKeySpec instanceof X509EncodedKeySpec)
      {
        if (SERIAL_INTEROP)
          return new DSAPublicKey(((X509EncodedKeySpec)paramKeySpec).getEncoded());
        return new DSAPublicKeyImpl(((X509EncodedKeySpec)paramKeySpec).getEncoded());
      }
      throw new InvalidKeySpecException("Inappropriate key specification");
    }
    catch (InvalidKeyException localInvalidKeyException)
    {
      throw new InvalidKeySpecException("Inappropriate key specification: " + localInvalidKeyException.getMessage());
    }
  }

  protected PrivateKey engineGeneratePrivate(KeySpec paramKeySpec)
    throws InvalidKeySpecException
  {
    try
    {
      if (paramKeySpec instanceof DSAPrivateKeySpec)
      {
        DSAPrivateKeySpec localDSAPrivateKeySpec = (DSAPrivateKeySpec)paramKeySpec;
        return new DSAPrivateKey(localDSAPrivateKeySpec.getX(), localDSAPrivateKeySpec.getP(), localDSAPrivateKeySpec.getQ(), localDSAPrivateKeySpec.getG());
      }
      if (paramKeySpec instanceof PKCS8EncodedKeySpec)
        return new DSAPrivateKey(((PKCS8EncodedKeySpec)paramKeySpec).getEncoded());
      throw new InvalidKeySpecException("Inappropriate key specification");
    }
    catch (InvalidKeyException localInvalidKeyException)
    {
      throw new InvalidKeySpecException("Inappropriate key specification: " + localInvalidKeyException.getMessage());
    }
  }

  protected <T extends KeySpec> T engineGetKeySpec(Key paramKey, Class<T> paramClass)
    throws InvalidKeySpecException
  {
    try
    {
      DSAParams localDSAParams;
      Class localClass1;
      Class localClass2;
      Object localObject;
      if (paramKey instanceof java.security.interfaces.DSAPublicKey)
      {
        localClass1 = Class.forName("java.security.spec.DSAPublicKeySpec");
        localClass2 = Class.forName("java.security.spec.X509EncodedKeySpec");
        if (localClass1.isAssignableFrom(paramClass))
        {
          localObject = (java.security.interfaces.DSAPublicKey)paramKey;
          localDSAParams = ((java.security.interfaces.DSAPublicKey)localObject).getParams();
          return new DSAPublicKeySpec(((java.security.interfaces.DSAPublicKey)localObject).getY(), localDSAParams.getP(), localDSAParams.getQ(), localDSAParams.getG());
        }
        if (localClass2.isAssignableFrom(paramClass))
          return new X509EncodedKeySpec(paramKey.getEncoded());
        throw new InvalidKeySpecException("Inappropriate key specification");
      }
      if (paramKey instanceof java.security.interfaces.DSAPrivateKey)
      {
        localClass1 = Class.forName("java.security.spec.DSAPrivateKeySpec");
        localClass2 = Class.forName("java.security.spec.PKCS8EncodedKeySpec");
        if (localClass1.isAssignableFrom(paramClass))
        {
          localObject = (java.security.interfaces.DSAPrivateKey)paramKey;
          localDSAParams = ((java.security.interfaces.DSAPrivateKey)localObject).getParams();
          return new DSAPrivateKeySpec(((java.security.interfaces.DSAPrivateKey)localObject).getX(), localDSAParams.getP(), localDSAParams.getQ(), localDSAParams.getG());
        }
        if (localClass2.isAssignableFrom(paramClass))
          return new PKCS8EncodedKeySpec(paramKey.getEncoded());
        throw new InvalidKeySpecException("Inappropriate key specification");
      }
      throw new InvalidKeySpecException("Inappropriate key type");
    }
    catch (ClassNotFoundException localClassNotFoundException)
    {
      throw new InvalidKeySpecException("Unsupported key specification: " + localClassNotFoundException.getMessage());
    }
  }

  protected Key engineTranslateKey(Key paramKey)
    throws InvalidKeyException
  {
    try
    {
      Object localObject;
      if (paramKey instanceof java.security.interfaces.DSAPublicKey)
      {
        if (paramKey instanceof DSAPublicKey)
          return paramKey;
        localObject = (DSAPublicKeySpec)engineGetKeySpec(paramKey, DSAPublicKeySpec.class);
        return engineGeneratePublic((KeySpec)localObject);
      }
      if (paramKey instanceof java.security.interfaces.DSAPrivateKey)
      {
        if (paramKey instanceof DSAPrivateKey)
          return paramKey;
        localObject = (DSAPrivateKeySpec)engineGetKeySpec(paramKey, DSAPrivateKeySpec.class);
        return engineGeneratePrivate((KeySpec)localObject);
      }
      throw new InvalidKeyException("Wrong algorithm type");
    }
    catch (InvalidKeySpecException localInvalidKeySpecException)
    {
      throw new InvalidKeyException("Cannot translate key: " + localInvalidKeySpecException.getMessage());
    }
  }

  static
  {
    String str = (String)AccessController.doPrivileged(new GetPropertyAction("sun.security.key.serial.interop", null));
    SERIAL_INTEROP = "true".equalsIgnoreCase(str);
  }
}