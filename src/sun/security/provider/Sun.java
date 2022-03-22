package sun.security.provider;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.Provider;
import java.security.Security;
import java.util.LinkedHashMap;
import java.util.Map;
import sun.security.action.PutAllAction;

public final class Sun extends Provider
{
  private static final long serialVersionUID = 6440182097568097204L;
  private static final String INFO = "SUN (DSA key/parameter generation; DSA signing; SHA-1, MD5 digests; SecureRandom; X.509 certificates; JKS keystore; PKIX CertPathValidator; PKIX CertPathBuilder; LDAP, Collection CertStores, JavaPolicy Policy; JavaLoginConfig Configuration)";
  private static final String PROP_EGD = "java.security.egd";
  private static final String PROP_RNDSOURCE = "securerandom.source";
  static final String URL_DEV_RANDOM = "file:/dev/random";
  static final String URL_DEV_URANDOM = "file:/dev/urandom";
  private static final String seedSource;

  public Sun()
  {
    super("SUN", 1.6000000000000001D, "SUN (DSA key/parameter generation; DSA signing; SHA-1, MD5 digests; SecureRandom; X.509 certificates; JKS keystore; PKIX CertPathValidator; PKIX CertPathBuilder; LDAP, Collection CertStores, JavaPolicy Policy; JavaLoginConfig Configuration)");
    LinkedHashMap localLinkedHashMap = new LinkedHashMap();
    boolean bool1 = NativePRNG.isAvailable();
    boolean bool2 = seedSource.equals("file:/dev/urandom");
    if ((bool1) && (bool2))
      localLinkedHashMap.put("SecureRandom.NativePRNG", "sun.security.provider.NativePRNG");
    localLinkedHashMap.put("SecureRandom.SHA1PRNG", "sun.security.provider.SecureRandom");
    if ((bool1) && (!(bool2)))
      localLinkedHashMap.put("SecureRandom.NativePRNG", "sun.security.provider.NativePRNG");
    localLinkedHashMap.put("Signature.SHA1withDSA", "sun.security.provider.DSA$SHA1withDSA");
    localLinkedHashMap.put("Signature.NONEwithDSA", "sun.security.provider.DSA$RawDSA");
    localLinkedHashMap.put("Alg.Alias.Signature.RawDSA", "NONEwithDSA");
    String str = "java.security.interfaces.DSAPublicKey|java.security.interfaces.DSAPrivateKey";
    localLinkedHashMap.put("Signature.SHA1withDSA SupportedKeyClasses", str);
    localLinkedHashMap.put("Signature.NONEwithDSA SupportedKeyClasses", str);
    localLinkedHashMap.put("Alg.Alias.Signature.DSA", "SHA1withDSA");
    localLinkedHashMap.put("Alg.Alias.Signature.DSS", "SHA1withDSA");
    localLinkedHashMap.put("Alg.Alias.Signature.SHA/DSA", "SHA1withDSA");
    localLinkedHashMap.put("Alg.Alias.Signature.SHA-1/DSA", "SHA1withDSA");
    localLinkedHashMap.put("Alg.Alias.Signature.SHA1/DSA", "SHA1withDSA");
    localLinkedHashMap.put("Alg.Alias.Signature.SHAwithDSA", "SHA1withDSA");
    localLinkedHashMap.put("Alg.Alias.Signature.DSAWithSHA1", "SHA1withDSA");
    localLinkedHashMap.put("Alg.Alias.Signature.OID.1.2.840.10040.4.3", "SHA1withDSA");
    localLinkedHashMap.put("Alg.Alias.Signature.1.2.840.10040.4.3", "SHA1withDSA");
    localLinkedHashMap.put("Alg.Alias.Signature.1.3.14.3.2.13", "SHA1withDSA");
    localLinkedHashMap.put("Alg.Alias.Signature.1.3.14.3.2.27", "SHA1withDSA");
    localLinkedHashMap.put("KeyPairGenerator.DSA", "sun.security.provider.DSAKeyPairGenerator");
    localLinkedHashMap.put("Alg.Alias.KeyPairGenerator.OID.1.2.840.10040.4.1", "DSA");
    localLinkedHashMap.put("Alg.Alias.KeyPairGenerator.1.2.840.10040.4.1", "DSA");
    localLinkedHashMap.put("Alg.Alias.KeyPairGenerator.1.3.14.3.2.12", "DSA");
    localLinkedHashMap.put("MessageDigest.MD2", "sun.security.provider.MD2");
    localLinkedHashMap.put("MessageDigest.MD5", "sun.security.provider.MD5");
    localLinkedHashMap.put("MessageDigest.SHA", "sun.security.provider.SHA");
    localLinkedHashMap.put("Alg.Alias.MessageDigest.SHA-1", "SHA");
    localLinkedHashMap.put("Alg.Alias.MessageDigest.SHA1", "SHA");
    localLinkedHashMap.put("MessageDigest.SHA-256", "sun.security.provider.SHA2");
    localLinkedHashMap.put("MessageDigest.SHA-384", "sun.security.provider.SHA5$SHA384");
    localLinkedHashMap.put("MessageDigest.SHA-512", "sun.security.provider.SHA5$SHA512");
    localLinkedHashMap.put("AlgorithmParameterGenerator.DSA", "sun.security.provider.DSAParameterGenerator");
    localLinkedHashMap.put("AlgorithmParameters.DSA", "sun.security.provider.DSAParameters");
    localLinkedHashMap.put("Alg.Alias.AlgorithmParameters.1.3.14.3.2.12", "DSA");
    localLinkedHashMap.put("Alg.Alias.AlgorithmParameters.1.2.840.10040.4.1", "DSA");
    localLinkedHashMap.put("KeyFactory.DSA", "sun.security.provider.DSAKeyFactory");
    localLinkedHashMap.put("Alg.Alias.KeyFactory.1.3.14.3.2.12", "DSA");
    localLinkedHashMap.put("Alg.Alias.KeyFactory.1.2.840.10040.4.1", "DSA");
    localLinkedHashMap.put("CertificateFactory.X.509", "sun.security.provider.X509Factory");
    localLinkedHashMap.put("Alg.Alias.CertificateFactory.X509", "X.509");
    localLinkedHashMap.put("KeyStore.JKS", "sun.security.provider.JavaKeyStore$JKS");
    localLinkedHashMap.put("KeyStore.CaseExactJKS", "sun.security.provider.JavaKeyStore$CaseExactJKS");
    localLinkedHashMap.put("Policy.JavaPolicy", "sun.security.provider.PolicySpiFile");
    localLinkedHashMap.put("Configuration.JavaLoginConfig", "sun.security.provider.ConfigSpiFile");
    localLinkedHashMap.put("CertPathBuilder.PKIX", "sun.security.provider.certpath.SunCertPathBuilder");
    localLinkedHashMap.put("CertPathBuilder.PKIX ValidationAlgorithm", "RFC3280");
    localLinkedHashMap.put("CertPathValidator.PKIX", "sun.security.provider.certpath.PKIXCertPathValidator");
    localLinkedHashMap.put("CertPathValidator.PKIX ValidationAlgorithm", "RFC3280");
    localLinkedHashMap.put("CertStore.LDAP", "sun.security.provider.certpath.LDAPCertStore");
    localLinkedHashMap.put("CertStore.LDAP LDAPSchema", "RFC2587");
    localLinkedHashMap.put("CertStore.Collection", "sun.security.provider.certpath.CollectionCertStore");
    localLinkedHashMap.put("CertStore.com.sun.security.IndexedCollection", "sun.security.provider.certpath.IndexedCollectionCertStore");
    localLinkedHashMap.put("Signature.SHA1withDSA KeySize", "1024");
    localLinkedHashMap.put("KeyPairGenerator.DSA KeySize", "1024");
    localLinkedHashMap.put("AlgorithmParameterGenerator.DSA KeySize", "1024");
    localLinkedHashMap.put("Signature.SHA1withDSA ImplementedIn", "Software");
    localLinkedHashMap.put("KeyPairGenerator.DSA ImplementedIn", "Software");
    localLinkedHashMap.put("MessageDigest.MD5 ImplementedIn", "Software");
    localLinkedHashMap.put("MessageDigest.SHA ImplementedIn", "Software");
    localLinkedHashMap.put("AlgorithmParameterGenerator.DSA ImplementedIn", "Software");
    localLinkedHashMap.put("AlgorithmParameters.DSA ImplementedIn", "Software");
    localLinkedHashMap.put("KeyFactory.DSA ImplementedIn", "Software");
    localLinkedHashMap.put("SecureRandom.SHA1PRNG ImplementedIn", "Software");
    localLinkedHashMap.put("CertificateFactory.X.509 ImplementedIn", "Software");
    localLinkedHashMap.put("KeyStore.JKS ImplementedIn", "Software");
    localLinkedHashMap.put("CertPathValidator.PKIX ImplementedIn", "Software");
    localLinkedHashMap.put("CertPathBuilder.PKIX ImplementedIn", "Software");
    localLinkedHashMap.put("CertStore.LDAP ImplementedIn", "Software");
    localLinkedHashMap.put("CertStore.Collection ImplementedIn", "Software");
    localLinkedHashMap.put("CertStore.com.sun.security.IndexedCollection ImplementedIn", "Software");
    if (localLinkedHashMap != this)
      AccessController.doPrivileged(new PutAllAction(this, localLinkedHashMap));
  }

  static String getSeedSource()
  {
    return seedSource;
  }

  static
  {
    Object localObject = AccessController.doPrivileged(new PrivilegedAction()
    {
      public Object run()
      {
        String str = System.getProperty("java.security.egd", "");
        if (str.length() != 0)
          return str;
        str = Security.getProperty("securerandom.source");
        if (str == null)
          return "";
        return str;
      }
    });
    seedSource = (String)localObject;
  }
}