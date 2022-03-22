package sun.security.rsa;

import java.security.AccessController;
import java.security.Provider;
import java.util.HashMap;
import java.util.Map;
import sun.security.action.PutAllAction;

public final class SunRsaSign extends Provider
{
  private static final long serialVersionUID = 866040293550393045L;

  public SunRsaSign()
  {
    super("SunRsaSign", 1.5D, "Sun RSA signature provider");
    HashMap localHashMap = new HashMap();
    localHashMap.put("KeyFactory.RSA", "sun.security.rsa.RSAKeyFactory");
    localHashMap.put("KeyPairGenerator.RSA", "sun.security.rsa.RSAKeyPairGenerator");
    localHashMap.put("Signature.MD2withRSA", "sun.security.rsa.RSASignature$MD2withRSA");
    localHashMap.put("Signature.MD5withRSA", "sun.security.rsa.RSASignature$MD5withRSA");
    localHashMap.put("Signature.SHA1withRSA", "sun.security.rsa.RSASignature$SHA1withRSA");
    localHashMap.put("Signature.SHA256withRSA", "sun.security.rsa.RSASignature$SHA256withRSA");
    localHashMap.put("Signature.SHA384withRSA", "sun.security.rsa.RSASignature$SHA384withRSA");
    localHashMap.put("Signature.SHA512withRSA", "sun.security.rsa.RSASignature$SHA512withRSA");
    String str = "java.security.interfaces.RSAPublicKey|java.security.interfaces.RSAPrivateKey";
    localHashMap.put("Signature.MD2withRSA SupportedKeyClasses", str);
    localHashMap.put("Signature.MD5withRSA SupportedKeyClasses", str);
    localHashMap.put("Signature.SHA1withRSA SupportedKeyClasses", str);
    localHashMap.put("Signature.SHA256withRSA SupportedKeyClasses", str);
    localHashMap.put("Signature.SHA384withRSA SupportedKeyClasses", str);
    localHashMap.put("Signature.SHA512withRSA SupportedKeyClasses", str);
    localHashMap.put("Alg.Alias.KeyFactory.1.2.840.113549.1.1", "RSA");
    localHashMap.put("Alg.Alias.KeyFactory.OID.1.2.840.113549.1.1", "RSA");
    localHashMap.put("Alg.Alias.KeyPairGenerator.1.2.840.113549.1.1", "RSA");
    localHashMap.put("Alg.Alias.KeyPairGenerator.OID.1.2.840.113549.1.1", "RSA");
    localHashMap.put("Alg.Alias.Signature.1.2.840.113549.1.1.2", "MD2withRSA");
    localHashMap.put("Alg.Alias.Signature.OID.1.2.840.113549.1.1.2", "MD2withRSA");
    localHashMap.put("Alg.Alias.Signature.1.2.840.113549.1.1.4", "MD5withRSA");
    localHashMap.put("Alg.Alias.Signature.OID.1.2.840.113549.1.1.4", "MD5withRSA");
    localHashMap.put("Alg.Alias.Signature.1.2.840.113549.1.1.5", "SHA1withRSA");
    localHashMap.put("Alg.Alias.Signature.OID.1.2.840.113549.1.1.5", "SHA1withRSA");
    localHashMap.put("Alg.Alias.Signature.1.3.14.3.2.29", "SHA1withRSA");
    localHashMap.put("Alg.Alias.Signature.1.2.840.113549.1.1.11", "SHA256withRSA");
    localHashMap.put("Alg.Alias.Signature.OID.1.2.840.113549.1.1.11", "SHA256withRSA");
    localHashMap.put("Alg.Alias.Signature.1.2.840.113549.1.1.12", "SHA384withRSA");
    localHashMap.put("Alg.Alias.Signature.OID.1.2.840.113549.1.1.12", "SHA384withRSA");
    localHashMap.put("Alg.Alias.Signature.1.2.840.113549.1.1.13", "SHA512withRSA");
    localHashMap.put("Alg.Alias.Signature.OID.1.2.840.113549.1.1.13", "SHA512withRSA");
    if (localHashMap != this)
      AccessController.doPrivileged(new PutAllAction(this, localHashMap));
  }
}