package sun.security.provider.certpath;

import java.security.AccessController;
import java.security.InvalidAlgorithmParameterException;
import java.security.PrivilegedAction;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.CertPath;
import java.security.cert.CertPathParameters;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertPathValidatorResult;
import java.security.cert.CertPathValidatorSpi;
import java.security.cert.PKIXCertPathChecker;
import java.security.cert.PKIXCertPathValidatorResult;
import java.security.cert.PKIXParameters;
import java.security.cert.PolicyNode;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.security.auth.x500.X500Principal;
import sun.security.util.Debug;

public class PKIXCertPathValidator extends CertPathValidatorSpi
{
  private static final Debug debug = Debug.getInstance("certpath");
  private Date testDate;
  private List<PKIXCertPathChecker> userCheckers;
  private String sigProvider;
  private BasicChecker basicChecker;

  public CertPathValidatorResult engineValidate(CertPath paramCertPath, CertPathParameters paramCertPathParameters)
    throws CertPathValidatorException, InvalidAlgorithmParameterException
  {
    TrustAnchor localTrustAnchor;
    X509Certificate localX509Certificate;
    if (debug != null)
      debug.println("PKIXCertPathValidator.engineValidate()...");
    if (!(paramCertPathParameters instanceof PKIXParameters))
      throw new InvalidAlgorithmParameterException("inappropriate parameters, must be an instance of PKIXParameters");
    if ((!(paramCertPath.getType().equals("X.509"))) && (!(paramCertPath.getType().equals("X509"))))
      throw new InvalidAlgorithmParameterException("inappropriate certification path type specified, must be X.509 or X509");
    PKIXParameters localPKIXParameters = (PKIXParameters)paramCertPathParameters;
    Set localSet = localPKIXParameters.getTrustAnchors();
    Object localObject1 = localSet.iterator();
    while (((Iterator)localObject1).hasNext())
    {
      localObject2 = (TrustAnchor)((Iterator)localObject1).next();
      if (((TrustAnchor)localObject2).getNameConstraints() != null)
        throw new InvalidAlgorithmParameterException("name constraints in trust anchor not supported");
    }
    localObject1 = new ArrayList(paramCertPath.getCertificates());
    if (debug != null)
    {
      if (((List)localObject1).isEmpty())
        debug.println("PKIXCertPathValidator.engineValidate() certList is empty");
      debug.println("PKIXCertPathValidator.engineValidate() reversing certpath...");
    }
    Collections.reverse((List)localObject1);
    populateVariables(localPKIXParameters);
    Object localObject2 = null;
    if (!(((List)localObject1).isEmpty()))
      localObject2 = (X509Certificate)((List)localObject1).get(0);
    Object localObject3 = null;
    Iterator localIterator = localSet.iterator();
    while (true)
    {
      if (!(localIterator.hasNext()))
        break label424;
      localTrustAnchor = (TrustAnchor)localIterator.next();
      localX509Certificate = localTrustAnchor.getTrustedCert();
      if (localX509Certificate == null)
        break label332;
      if (debug != null)
        debug.println("PKIXCertPathValidator.engineValidate() anchor.getTrustedCert() != null");
      if (isWorthTrying(localX509Certificate, (X509Certificate)localObject2))
        break;
    }
    PublicKey localPublicKey = localX509Certificate.getPublicKey();
    X500Principal localX500Principal = localX509Certificate.getSubjectX500Principal();
    if (debug != null)
    {
      debug.println("anchor.getTrustedCert().getSubjectX500Principal() = " + localX500Principal);
      break label360:
      if (debug != null)
        label332: debug.println("PKIXCertPathValidator.engineValidate(): anchor.getTrustedCert() == null");
      localPublicKey = localTrustAnchor.getCAPublicKey();
      localX500Principal = CertPathHelper.getCA(localTrustAnchor);
    }
    try
    {
      label360: PolicyNodeImpl localPolicyNodeImpl = new PolicyNodeImpl(null, "2.5.29.32.0", null, false, Collections.singleton("2.5.29.32.0"), false);
      PolicyNode localPolicyNode = doValidate(localPublicKey, localX500Principal, paramCertPath, (List)localObject1, localPKIXParameters, localPolicyNodeImpl);
      label424: return new PKIXCertPathValidatorResult(localTrustAnchor, localPolicyNode, this.basicChecker.getPublicKey());
    }
    catch (CertPathValidatorException localCertPathValidatorException)
    {
      while (true)
        localObject3 = localCertPathValidatorException;
      if (localObject3 != null)
        throw localObject3;
      throw new CertPathValidatorException("Path does not chain with any of the trust anchors");
    }
  }

  private boolean isWorthTrying(X509Certificate paramX509Certificate1, X509Certificate paramX509Certificate2)
    throws CertPathValidatorException
  {
    if (debug != null)
      debug.println("PKIXCertPathValidator.isWorthTrying() checking if this trusted cert is worth trying ...");
    if (paramX509Certificate2 == null)
      return true;
    X500Principal localX500Principal = paramX509Certificate1.getSubjectX500Principal();
    if (localX500Principal.equals(paramX509Certificate2.getIssuerX500Principal()))
    {
      if (debug != null)
        debug.println("YES - try this trustedCert");
      return true;
    }
    if (debug != null)
      debug.println("NO - don't try this trustedCert");
    return false;
  }

  private void populateVariables(PKIXParameters paramPKIXParameters)
    throws CertPathValidatorException
  {
    this.testDate = paramPKIXParameters.getDate();
    if (this.testDate == null)
      this.testDate = new Date(System.currentTimeMillis());
    this.userCheckers = paramPKIXParameters.getCertPathCheckers();
    this.sigProvider = paramPKIXParameters.getSigProvider();
  }

  private PolicyNode doValidate(PublicKey paramPublicKey, X500Principal paramX500Principal, CertPath paramCertPath, List<X509Certificate> paramList, PKIXParameters paramPKIXParameters, PolicyNodeImpl paramPolicyNodeImpl)
    throws CertPathValidatorException
  {
    ArrayList localArrayList = new ArrayList();
    int i = paramList.size();
    this.basicChecker = new BasicChecker(paramPublicKey, paramX500Principal, this.testDate, this.sigProvider, false);
    KeyChecker localKeyChecker = new KeyChecker(i, paramPKIXParameters.getTargetCertConstraints());
    ConstraintsChecker localConstraintsChecker = new ConstraintsChecker(i);
    PolicyChecker localPolicyChecker = new PolicyChecker(paramPKIXParameters.getInitialPolicies(), i, paramPKIXParameters.isExplicitPolicyRequired(), paramPKIXParameters.isPolicyMappingInhibited(), paramPKIXParameters.isAnyPolicyInhibited(), paramPKIXParameters.getPolicyQualifiersRejected(), paramPolicyNodeImpl);
    localArrayList.add(localKeyChecker);
    localArrayList.add(localConstraintsChecker);
    localArrayList.add(localPolicyChecker);
    localArrayList.add(this.basicChecker);
    if (paramPKIXParameters.isRevocationEnabled())
    {
      localObject1 = (String)AccessController.doPrivileged(new PrivilegedAction(this)
      {
        public Object run()
        {
          return Security.getProperty("ocsp.enable");
        }
      });
      String str = (String)AccessController.doPrivileged(new PrivilegedAction(this)
      {
        public Object run()
        {
          return Security.getProperty("com.sun.security.onlyCheckRevocationOfEECert");
        }
      });
      boolean bool = "true".equalsIgnoreCase(str);
      if ("true".equalsIgnoreCase((String)localObject1))
      {
        localObject2 = new OCSPChecker(paramCertPath, paramPKIXParameters, bool);
        localArrayList.add(localObject2);
      }
      Object localObject2 = new CrlRevocationChecker(paramPublicKey, paramPKIXParameters, paramList, bool);
      localArrayList.add(localObject2);
    }
    localArrayList.addAll(this.userCheckers);
    Object localObject1 = new PKIXMasterCertPathValidator(localArrayList);
    ((PKIXMasterCertPathValidator)localObject1).validate(paramCertPath, paramList);
    return ((PolicyNode)(PolicyNode)localPolicyChecker.getPolicyTree());
  }
}