package sun.security.x509;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;
import sun.security.util.ObjectIdentifier;

public class OIDMap
{
  private static final String ROOT = "x509.info.extensions";
  private static final String AUTH_KEY_IDENTIFIER = "x509.info.extensions.AuthorityKeyIdentifier";
  private static final String SUB_KEY_IDENTIFIER = "x509.info.extensions.SubjectKeyIdentifier";
  private static final String KEY_USAGE = "x509.info.extensions.KeyUsage";
  private static final String PRIVATE_KEY_USAGE = "x509.info.extensions.PrivateKeyUsage";
  private static final String POLICY_MAPPINGS = "x509.info.extensions.PolicyMappings";
  private static final String SUB_ALT_NAME = "x509.info.extensions.SubjectAlternativeName";
  private static final String ISSUER_ALT_NAME = "x509.info.extensions.IssuerAlternativeName";
  private static final String BASIC_CONSTRAINTS = "x509.info.extensions.BasicConstraints";
  private static final String NAME_CONSTRAINTS = "x509.info.extensions.NameConstraints";
  private static final String POLICY_CONSTRAINTS = "x509.info.extensions.PolicyConstraints";
  private static final String CRL_NUMBER = "x509.info.extensions.CRLNumber";
  private static final String CRL_REASON = "x509.info.extensions.CRLReasonCode";
  private static final String NETSCAPE_CERT = "x509.info.extensions.NetscapeCertType";
  private static final String CERT_POLICIES = "x509.info.extensions.CertificatePolicies";
  private static final String EXT_KEY_USAGE = "x509.info.extensions.ExtendedKeyUsage";
  private static final String INHIBIT_ANY_POLICY = "x509.info.extensions.InhibitAnyPolicy";
  private static final String CRL_DIST_POINTS = "x509.info.extensions.CRLDistributionPoints";
  private static final String CERT_ISSUER = "x509.info.extensions.CertificateIssuer";
  private static final String AUTH_INFO_ACCESS = "x509.info.extensions.AuthorityInfoAccess";
  private static final String ISSUING_DIST_POINT = "x509.info.extensions.IssuingDistributionPoint";
  private static final String DELTA_CRL_INDICATOR = "x509.info.extensions.DeltaCRLIndicator";
  private static final String FRESHEST_CRL = "x509.info.extensions.FreshestCRL";
  private static final int[] NetscapeCertType_data = { 2, 16, 840, 1, 113730, 1, 1 };
  private static final Map<ObjectIdentifier, OIDInfo> oidMap = new HashMap();
  private static final Map<String, OIDInfo> nameMap = new HashMap();

  private static void addInternal(String paramString1, ObjectIdentifier paramObjectIdentifier, String paramString2)
  {
    OIDInfo localOIDInfo = new OIDInfo(paramString1, paramObjectIdentifier, paramString2);
    oidMap.put(paramObjectIdentifier, localOIDInfo);
    nameMap.put(paramString1, localOIDInfo);
  }

  public static void addAttribute(String paramString1, String paramString2, Class paramClass)
    throws CertificateException
  {
    ObjectIdentifier localObjectIdentifier;
    try
    {
      localObjectIdentifier = new ObjectIdentifier(paramString2);
    }
    catch (IOException localIOException)
    {
      throw new CertificateException("Invalid Object identifier: " + paramString2);
    }
    OIDInfo localOIDInfo = new OIDInfo(paramString1, localObjectIdentifier, paramClass);
    if (oidMap.put(localObjectIdentifier, localOIDInfo) != null)
      throw new CertificateException("Object identifier already exists: " + paramString2);
    if (nameMap.put(paramString1, localOIDInfo) != null)
      throw new CertificateException("Name already exists: " + paramString1);
  }

  public static String getName(ObjectIdentifier paramObjectIdentifier)
  {
    OIDInfo localOIDInfo = (OIDInfo)oidMap.get(paramObjectIdentifier);
    return ((localOIDInfo == null) ? null : localOIDInfo.name);
  }

  public static ObjectIdentifier getOID(String paramString)
  {
    OIDInfo localOIDInfo = (OIDInfo)nameMap.get(paramString);
    return ((localOIDInfo == null) ? null : localOIDInfo.oid);
  }

  public static Class getClass(String paramString)
    throws CertificateException
  {
    OIDInfo localOIDInfo = (OIDInfo)nameMap.get(paramString);
    return ((localOIDInfo == null) ? null : localOIDInfo.getClazz());
  }

  public static Class getClass(ObjectIdentifier paramObjectIdentifier)
    throws CertificateException
  {
    OIDInfo localOIDInfo = (OIDInfo)oidMap.get(paramObjectIdentifier);
    return ((localOIDInfo == null) ? null : localOIDInfo.getClazz());
  }

  static
  {
    addInternal("x509.info.extensions.SubjectKeyIdentifier", PKIXExtensions.SubjectKey_Id, "sun.security.x509.SubjectKeyIdentifierExtension");
    addInternal("x509.info.extensions.KeyUsage", PKIXExtensions.KeyUsage_Id, "sun.security.x509.KeyUsageExtension");
    addInternal("x509.info.extensions.PrivateKeyUsage", PKIXExtensions.PrivateKeyUsage_Id, "sun.security.x509.PrivateKeyUsageExtension");
    addInternal("x509.info.extensions.SubjectAlternativeName", PKIXExtensions.SubjectAlternativeName_Id, "sun.security.x509.SubjectAlternativeNameExtension");
    addInternal("x509.info.extensions.IssuerAlternativeName", PKIXExtensions.IssuerAlternativeName_Id, "sun.security.x509.IssuerAlternativeNameExtension");
    addInternal("x509.info.extensions.BasicConstraints", PKIXExtensions.BasicConstraints_Id, "sun.security.x509.BasicConstraintsExtension");
    addInternal("x509.info.extensions.CRLNumber", PKIXExtensions.CRLNumber_Id, "sun.security.x509.CRLNumberExtension");
    addInternal("x509.info.extensions.CRLReasonCode", PKIXExtensions.ReasonCode_Id, "sun.security.x509.CRLReasonCodeExtension");
    addInternal("x509.info.extensions.NameConstraints", PKIXExtensions.NameConstraints_Id, "sun.security.x509.NameConstraintsExtension");
    addInternal("x509.info.extensions.PolicyMappings", PKIXExtensions.PolicyMappings_Id, "sun.security.x509.PolicyMappingsExtension");
    addInternal("x509.info.extensions.AuthorityKeyIdentifier", PKIXExtensions.AuthorityKey_Id, "sun.security.x509.AuthorityKeyIdentifierExtension");
    addInternal("x509.info.extensions.PolicyConstraints", PKIXExtensions.PolicyConstraints_Id, "sun.security.x509.PolicyConstraintsExtension");
    addInternal("x509.info.extensions.NetscapeCertType", ObjectIdentifier.newInternal(new int[] { 2, 16, 840, 1, 113730, 1, 1 }), "sun.security.x509.NetscapeCertTypeExtension");
    addInternal("x509.info.extensions.CertificatePolicies", PKIXExtensions.CertificatePolicies_Id, "sun.security.x509.CertificatePoliciesExtension");
    addInternal("x509.info.extensions.ExtendedKeyUsage", PKIXExtensions.ExtendedKeyUsage_Id, "sun.security.x509.ExtendedKeyUsageExtension");
    addInternal("x509.info.extensions.InhibitAnyPolicy", PKIXExtensions.InhibitAnyPolicy_Id, "sun.security.x509.InhibitAnyPolicyExtension");
    addInternal("x509.info.extensions.CRLDistributionPoints", PKIXExtensions.CRLDistributionPoints_Id, "sun.security.x509.CRLDistributionPointsExtension");
    addInternal("x509.info.extensions.CertificateIssuer", PKIXExtensions.CertificateIssuer_Id, "sun.security.x509.CertificateIssuerExtension");
    addInternal("x509.info.extensions.AuthorityInfoAccess", PKIXExtensions.AuthInfoAccess_Id, "sun.security.x509.AuthorityInfoAccessExtension");
    addInternal("x509.info.extensions.IssuingDistributionPoint", PKIXExtensions.IssuingDistributionPoint_Id, "sun.security.x509.IssuingDistributionPointExtension");
    addInternal("x509.info.extensions.DeltaCRLIndicator", PKIXExtensions.DeltaCRLIndicator_Id, "sun.security.x509.DeltaCRLIndicatorExtension");
    addInternal("x509.info.extensions.FreshestCRL", PKIXExtensions.FreshestCRL_Id, "sun.security.x509.FreshestCRLExtension");
  }

  private static class OIDInfo
  {
    final ObjectIdentifier oid;
    final String name;
    final String className;
    private volatile Class clazz;

    OIDInfo(String paramString1, ObjectIdentifier paramObjectIdentifier, String paramString2)
    {
      this.name = paramString1;
      this.oid = paramObjectIdentifier;
      this.className = paramString2;
    }

    OIDInfo(String paramString, ObjectIdentifier paramObjectIdentifier, Class paramClass)
    {
      this.name = paramString;
      this.oid = paramObjectIdentifier;
      this.className = paramClass.getName();
      this.clazz = paramClass;
    }

    Class getClazz()
      throws CertificateException
    {
      Class localClass;
      try
      {
        localClass = this.clazz;
        if (localClass == null)
        {
          localClass = Class.forName(this.className);
          this.clazz = localClass;
        }
        return localClass;
      }
      catch (ClassNotFoundException localClassNotFoundException)
      {
        throw ((CertificateException)new CertificateException("Could not load class: " + localClassNotFoundException).initCause(localClassNotFoundException));
      }
    }
  }
}