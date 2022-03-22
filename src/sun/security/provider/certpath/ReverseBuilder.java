package sun.security.provider.certpath;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertStore;
import java.security.cert.CertStoreException;
import java.security.cert.CertificateException;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.PKIXCertPathChecker;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.security.auth.x500.X500Principal;
import sun.security.util.Debug;
import sun.security.util.ObjectIdentifier;
import sun.security.x509.NameConstraintsExtension;
import sun.security.x509.PKIXExtensions;
import sun.security.x509.X500Name;
import sun.security.x509.X509CertImpl;

class ReverseBuilder extends Builder
{
  private Debug debug = Debug.getInstance("certpath");
  private Date date;
  private X509CertSelector targetCertSelector;
  Set<String> initPolicies;

  ReverseBuilder(PKIXBuilderParameters paramPKIXBuilderParameters, X500Principal paramX500Principal)
  {
    super(paramPKIXBuilderParameters, paramX500Principal);
    this.date = paramPKIXBuilderParameters.getDate();
    if (this.date == null)
      this.date = new Date();
    this.targetCertSelector = ((X509CertSelector)paramPKIXBuilderParameters.getTargetCertConstraints());
    Set localSet = paramPKIXBuilderParameters.getInitialPolicies();
    this.initPolicies = new HashSet();
    if (localSet.isEmpty())
    {
      this.initPolicies.add("2.5.29.32.0");
    }
    else
    {
      Iterator localIterator = localSet.iterator();
      while (localIterator.hasNext())
      {
        String str = (String)localIterator.next();
        this.initPolicies.add(str);
      }
    }
  }

  Collection<X509Certificate> getMatchingCerts(State paramState, List<CertStore> paramList)
    throws CertStoreException, CertificateException, IOException
  {
    ReverseState localReverseState = (ReverseState)paramState;
    if (this.debug != null)
      this.debug.println("In ReverseBuilder.getMatchingCerts.");
    Collection localCollection = getMatchingEECerts(localReverseState, paramList);
    localCollection.addAll(getMatchingCACerts(localReverseState, paramList));
    return localCollection;
  }

  private Collection<X509Certificate> getMatchingEECerts(ReverseState paramReverseState, List<CertStore> paramList)
    throws CertStoreException, CertificateException, IOException
  {
    X509CertSelector localX509CertSelector = (X509CertSelector)this.buildParams.getTargetCertConstraints();
    CertPathHelper.setIssuer(localX509CertSelector, paramReverseState.subjectDN);
    localX509CertSelector.setCertificateValid(this.date);
    if (paramReverseState.explicitPolicy == 0)
      localX509CertSelector.setPolicy(getMatchingPolicies());
    localX509CertSelector.setBasicConstraints(-2);
    HashSet localHashSet = new HashSet();
    addMatchingCerts(localX509CertSelector, paramList, localHashSet, true);
    if (this.debug != null)
      this.debug.println("ReverseBuilder.getMatchingEECerts got " + localHashSet.size() + " certs.");
    return localHashSet;
  }

  private Collection<X509Certificate> getMatchingCACerts(ReverseState paramReverseState, List<CertStore> paramList)
    throws CertificateException, CertStoreException, IOException
  {
    X509CertSelector localX509CertSelector = new X509CertSelector();
    CertPathHelper.setIssuer(localX509CertSelector, paramReverseState.subjectDN);
    localX509CertSelector.setCertificateValid(this.date);
    localX509CertSelector.addPathToName(4, this.targetCertSelector.getSubjectAsBytes());
    if (paramReverseState.explicitPolicy == 0)
      localX509CertSelector.setPolicy(getMatchingPolicies());
    localX509CertSelector.setBasicConstraints(0);
    ArrayList localArrayList = new ArrayList();
    addMatchingCerts(localX509CertSelector, paramList, localArrayList, true);
    Collections.sort(localArrayList, new PKIXCertComparator(this));
    if (this.debug != null)
      this.debug.println("ReverseBuilder.getMatchingCACerts got " + localArrayList.size() + " certs.");
    return localArrayList;
  }

  void verifyCert(X509Certificate paramX509Certificate, State paramState, List<X509Certificate> paramList)
    throws GeneralSecurityException
  {
    Object localObject4;
    if (this.debug != null)
      this.debug.println("ReverseBuilder.verifyCert(SN: " + Debug.toHexString(paramX509Certificate.getSerialNumber()) + "\n  Subject: " + paramX509Certificate.getSubjectX500Principal() + ")");
    ReverseState localReverseState = (ReverseState)paramState;
    if (localReverseState.isInitial())
      return;
    if ((paramList != null) && (!(paramList.isEmpty())))
    {
      ArrayList localArrayList = new ArrayList();
      Iterator localIterator = paramList.iterator();
      while (localIterator.hasNext())
      {
        localObject1 = (X509Certificate)localIterator.next();
        localArrayList.add(0, localObject1);
      }
      bool2 = false;
      Object localObject1 = localArrayList.iterator();
      while (((Iterator)localObject1).hasNext())
      {
        localObject2 = (X509Certificate)((Iterator)localObject1).next();
        localObject3 = X509CertImpl.toImpl((X509Certificate)localObject2);
        localObject4 = ((X509CertImpl)localObject3).getPolicyMappingsExtension();
        if (localObject4 != null)
          bool2 = true;
        if (this.debug != null)
          this.debug.println("policyMappingFound = " + bool2);
        if ((paramX509Certificate.equals(localObject2)) && (((this.buildParams.isPolicyMappingInhibited()) || (!(bool2)))))
        {
          if (this.debug != null)
            this.debug.println("loop detected!!");
          throw new CertPathValidatorException("loop detected");
        }
      }
    }
    boolean bool1 = paramX509Certificate.getSubjectX500Principal().equals(this.targetSubjectDN);
    boolean bool2 = paramX509Certificate.getBasicConstraints() != -1;
    if (!(bool1))
    {
      if (!(bool2))
        throw new CertPathValidatorException("cert is NOT a CA cert");
      if ((localReverseState.remainingCACerts <= 0) && (!(X509CertImpl.isSelfIssued(paramX509Certificate))))
        throw new CertPathValidatorException("pathLenConstraint violated, path too long");
      KeyChecker.verifyCAKeyUsage(paramX509Certificate);
    }
    else if (!(this.targetCertSelector.match(paramX509Certificate)))
    {
      throw new CertPathValidatorException("target certificate constraints check failed");
    }
    if (this.buildParams.isRevocationEnabled())
      localReverseState.crlChecker.check(paramX509Certificate, localReverseState.pubKey, localReverseState.crlSign);
    if ((((bool1) || (!(X509CertImpl.isSelfIssued(paramX509Certificate))))) && (localReverseState.nc != null))
      try
      {
        if (!(localReverseState.nc.verify(paramX509Certificate)))
          throw new CertPathValidatorException("name constraints check failed");
      }
      catch (IOException localIOException)
      {
        throw new CertPathValidatorException(localIOException);
      }
    X509CertImpl localX509CertImpl = X509CertImpl.toImpl(paramX509Certificate);
    localReverseState.rootNode = PolicyChecker.processPolicies(localReverseState.certIndex, this.initPolicies, localReverseState.explicitPolicy, localReverseState.policyMapping, localReverseState.inhibitAnyPolicy, this.buildParams.getPolicyQualifiersRejected(), localReverseState.rootNode, localX509CertImpl, bool1);
    Object localObject2 = paramX509Certificate.getCriticalExtensionOIDs();
    if (localObject2 == null)
      localObject2 = Collections.emptySet();
    Object localObject3 = localReverseState.userCheckers.iterator();
    while (((Iterator)localObject3).hasNext())
    {
      localObject4 = (PKIXCertPathChecker)((Iterator)localObject3).next();
      ((PKIXCertPathChecker)localObject4).check(paramX509Certificate, (Collection)localObject2);
    }
    if (!(((Set)localObject2).isEmpty()))
    {
      ((Set)localObject2).remove(PKIXExtensions.BasicConstraints_Id.toString());
      ((Set)localObject2).remove(PKIXExtensions.NameConstraints_Id.toString());
      ((Set)localObject2).remove(PKIXExtensions.CertificatePolicies_Id.toString());
      ((Set)localObject2).remove(PKIXExtensions.PolicyMappings_Id.toString());
      ((Set)localObject2).remove(PKIXExtensions.PolicyConstraints_Id.toString());
      ((Set)localObject2).remove(PKIXExtensions.InhibitAnyPolicy_Id.toString());
      ((Set)localObject2).remove(PKIXExtensions.SubjectAlternativeName_Id.toString());
      ((Set)localObject2).remove(PKIXExtensions.KeyUsage_Id.toString());
      ((Set)localObject2).remove(PKIXExtensions.ExtendedKeyUsage_Id.toString());
      if (!(((Set)localObject2).isEmpty()))
        throw new CertificateException("Unrecognized critical extension(s)");
    }
    if (this.buildParams.getSigProvider() != null)
      paramX509Certificate.verify(localReverseState.pubKey, this.buildParams.getSigProvider());
    else
      paramX509Certificate.verify(localReverseState.pubKey);
  }

  boolean isPathCompleted(X509Certificate paramX509Certificate)
  {
    return paramX509Certificate.getSubjectX500Principal().equals(this.targetSubjectDN);
  }

  void addCertToPath(X509Certificate paramX509Certificate, LinkedList<X509Certificate> paramLinkedList)
  {
    paramLinkedList.addLast(paramX509Certificate);
  }

  void removeFinalCertFromPath(LinkedList<X509Certificate> paramLinkedList)
  {
    paramLinkedList.removeLast();
  }

  class PKIXCertComparator
  implements Comparator<X509Certificate>
  {
    private Debug debug = Debug.getInstance("certpath");

    public int compare(, X509Certificate paramX509Certificate2)
    {
      int i;
      int j;
      if (paramX509Certificate1.getSubjectX500Principal().equals(this.this$0.targetSubjectDN))
        return -1;
      if (paramX509Certificate2.getSubjectX500Principal().equals(this.this$0.targetSubjectDN))
        return 1;
      try
      {
        X500Name localX500Name = X500Name.asX500Name(this.this$0.targetSubjectDN);
        i = Builder.targetDistance(null, paramX509Certificate1, localX500Name);
        j = Builder.targetDistance(null, paramX509Certificate2, localX500Name);
      }
      catch (IOException localIOException)
      {
        if (this.debug != null)
        {
          this.debug.println("IOException in call to Builder.targetDistance");
          localIOException.printStackTrace();
        }
        throw new ClassCastException("Invalid target subject distinguished name");
      }
      if (i == j)
        return 0;
      if (i == -1)
        return 1;
      if (i < j)
        return -1;
      return 1;
    }
  }
}