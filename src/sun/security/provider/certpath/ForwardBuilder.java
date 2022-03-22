package sun.security.provider.certpath;

import java.io.IOException;
import java.net.URI;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertStore;
import java.security.cert.CertStoreException;
import java.security.cert.CertificateException;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.PKIXCertPathChecker;
import java.security.cert.TrustAnchor;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collection<Ljava.security.cert.X509Certificate;>;
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
import sun.security.x509.AccessDescription;
import sun.security.x509.AuthorityInfoAccessExtension;
import sun.security.x509.GeneralName;
import sun.security.x509.GeneralNameInterface;
import sun.security.x509.PKIXExtensions;
import sun.security.x509.URIName;
import sun.security.x509.X500Name;
import sun.security.x509.X509CertImpl;

class ForwardBuilder extends Builder
{
  private static final Debug debug = Debug.getInstance("certpath");
  private Date date;
  private final Set<X509Certificate> trustedCerts;
  private final Set<X500Principal> trustedSubjectDNs;
  private final Set<TrustAnchor> trustAnchors;
  private X509CertSelector eeSelector;
  private LDAPCertStore.LDAPCertSelector caSelector;
  private X509CertSelector caTargetSelector;
  TrustAnchor trustAnchor;
  private Comparator<X509Certificate> comparator;
  private boolean searchAllCertStores = true;

  ForwardBuilder(PKIXBuilderParameters paramPKIXBuilderParameters, X500Principal paramX500Principal, boolean paramBoolean)
  {
    super(paramPKIXBuilderParameters, paramX500Principal);
    this.date = paramPKIXBuilderParameters.getDate();
    if (this.date == null)
      this.date = new Date();
    this.trustAnchors = paramPKIXBuilderParameters.getTrustAnchors();
    this.trustedCerts = new HashSet(this.trustAnchors.size());
    this.trustedSubjectDNs = new HashSet(this.trustAnchors.size());
    Iterator localIterator = this.trustAnchors.iterator();
    while (localIterator.hasNext())
    {
      TrustAnchor localTrustAnchor = (TrustAnchor)localIterator.next();
      X509Certificate localX509Certificate = localTrustAnchor.getTrustedCert();
      if (localX509Certificate != null)
      {
        this.trustedCerts.add(localX509Certificate);
        this.trustedSubjectDNs.add(localX509Certificate.getSubjectX500Principal());
      }
      else
      {
        this.trustedSubjectDNs.add(CertPathHelper.getCA(localTrustAnchor));
      }
    }
    this.comparator = new PKIXCertComparator(this.trustedSubjectDNs);
    this.searchAllCertStores = paramBoolean;
  }

  Collection<X509Certificate> getMatchingCerts(State paramState, List<CertStore> paramList)
    throws CertStoreException, CertificateException, IOException
  {
    if (debug != null)
      debug.println("ForwardBuilder.getMatchingCerts()...");
    ForwardState localForwardState = (ForwardState)paramState;
    ArrayList localArrayList = new ArrayList();
    if (localForwardState.isInitial())
      localArrayList.addAll(getMatchingEECerts(localForwardState, paramList));
    localArrayList.addAll(getMatchingCACerts(localForwardState, paramList));
    Collections.sort(localArrayList, this.comparator);
    return localArrayList;
  }

  private Collection<X509Certificate> getMatchingEECerts(ForwardState paramForwardState, List<CertStore> paramList)
    throws CertStoreException, CertificateException, IOException
  {
    if (debug != null)
      debug.println("ForwardBuilder.getMatchingEECerts()...");
    if (this.eeSelector == null)
    {
      this.eeSelector = ((X509CertSelector)this.buildParams.getTargetCertConstraints());
      this.eeSelector.setCertificateValid(this.date);
      if (this.buildParams.isExplicitPolicyRequired())
        this.eeSelector.setPolicy(getMatchingPolicies());
      this.eeSelector.setBasicConstraints(-2);
    }
    HashSet localHashSet = new HashSet();
    addMatchingCerts(this.eeSelector, paramList, localHashSet, this.searchAllCertStores);
    return localHashSet;
  }

  private Collection<X509Certificate> getMatchingCACerts(ForwardState paramForwardState, List<CertStore> paramList)
    throws CertificateException, CertStoreException, IOException
  {
    if (debug != null)
      debug.println("ForwardBuilder.getMatchingCACerts()...");
    ArrayList localArrayList = new ArrayList();
    Object localObject1 = null;
    if (paramForwardState.isInitial())
    {
      localX509CertSelector = (X509CertSelector)this.buildParams.getTargetCertConstraints();
      if (localX509CertSelector.getBasicConstraints() == -2)
        return localArrayList;
      if (debug != null)
        debug.println("ForwardBuilder.getMatchingCACerts(): ca is target");
      if (this.caTargetSelector == null)
      {
        this.caTargetSelector = localX509CertSelector;
        this.caTargetSelector.setCertificateValid(this.date);
        if (this.buildParams.isExplicitPolicyRequired())
          this.caTargetSelector.setPolicy(getMatchingPolicies());
      }
      this.caTargetSelector.setBasicConstraints(paramForwardState.traversedCACerts);
      localObject1 = this.caTargetSelector;
    }
    else
    {
      if (this.caSelector == null)
      {
        this.caSelector = new LDAPCertStore.LDAPCertSelector();
        this.caSelector.setCertificateValid(this.date);
        if (this.buildParams.isExplicitPolicyRequired())
          this.caSelector.setPolicy(getMatchingPolicies());
      }
      CertPathHelper.setSubject(this.caSelector, paramForwardState.issuerDN);
      CertPathHelper.setPathToNames(this.caSelector, paramForwardState.subjectNamesTraversed);
      this.caSelector.setBasicConstraints(paramForwardState.traversedCACerts);
      localObject1 = this.caSelector;
    }
    X509CertSelector localX509CertSelector = new X509CertSelector();
    if (paramForwardState.isInitial())
      localX509CertSelector = (X509CertSelector)this.buildParams.getTargetCertConstraints();
    else
      CertPathHelper.setSubject(localX509CertSelector, paramForwardState.issuerDN);
    Object localObject2 = this.trustedCerts.iterator();
    while (((Iterator)localObject2).hasNext())
    {
      X509Certificate localX509Certificate = (X509Certificate)((Iterator)localObject2).next();
      if (localX509CertSelector.match(localX509Certificate))
      {
        if (debug != null)
          debug.println("ForwardBuilder.getMatchingCACerts: found matching trust anchor");
        localArrayList.add(localX509Certificate);
      }
    }
    if ((!(this.searchAllCertStores)) && (!(localArrayList.isEmpty())))
      return localArrayList;
    if ((paramForwardState.isInitial()) || (this.buildParams.getMaxPathLength() == -1) || (this.buildParams.getMaxPathLength() > paramForwardState.traversedCACerts))
      addMatchingCerts((X509CertSelector)localObject1, paramList, localArrayList, this.searchAllCertStores);
    if ((!(this.searchAllCertStores)) && (!(localArrayList.isEmpty())))
      return localArrayList;
    if (!(paramForwardState.isInitial()))
    {
      localObject2 = paramForwardState.cert.getAuthorityInfoAccessExtension();
      if (localObject2 != null)
        localArrayList.addAll(getCerts((AuthorityInfoAccessExtension)localObject2));
    }
    if (debug != null)
      debug.println("ForwardBuilder.getMatchingCACerts: found " + localArrayList.size() + " forward certs");
    return ((Collection<X509Certificate>)(Collection<X509Certificate>)localArrayList);
  }

  private Collection<X509Certificate> getCerts(AuthorityInfoAccessExtension paramAuthorityInfoAccessExtension)
  {
    if (debug != null)
      debug.println("ForwardBuilder.getCerts: checking AIA ext");
    if (!(Builder.USE_AIA))
      return Collections.emptyList();
    List localList = paramAuthorityInfoAccessExtension.getAccessDescriptions();
    if ((localList == null) || (localList.isEmpty()))
      return Collections.emptyList();
    ArrayList localArrayList = new ArrayList();
    Iterator localIterator = localList.iterator();
    while (true)
    {
      URI localURI;
      while (true)
      {
        GeneralNameInterface localGeneralNameInterface;
        while (true)
        {
          AccessDescription localAccessDescription;
          while (true)
          {
            if (!(localIterator.hasNext()))
              break label407;
            localAccessDescription = (AccessDescription)localIterator.next();
            if (localAccessDescription.getAccessMethod().equals(AccessDescription.Ad_CAISSUERS_Id))
              break;
          }
          localGeneralNameInterface = localAccessDescription.getAccessLocation().getName();
          if (localGeneralNameInterface instanceof URIName)
            break;
        }
        localURI = ((URIName)localGeneralNameInterface).getURI();
        if (localURI.getScheme().equals("ldap"))
          break;
      }
      String str = localURI.getPath();
      if (debug != null)
      {
        debug.println("ForwardBuilder.getCerts: AIA ext URIName:");
        debug.println("authority: " + localURI.getAuthority());
        debug.println("path: " + str);
      }
      if (str.charAt(0) == '/')
        str = str.substring(1);
      Collection localCollection = null;
      try
      {
        CertStore localCertStore = LDAPCertStore.getInstance(LDAPCertStore.getParameters(localURI));
        LDAPCertStore.LDAPCertSelector localLDAPCertSelector = (LDAPCertStore.LDAPCertSelector)this.caSelector.clone();
        localLDAPCertSelector.setCertSubject(localLDAPCertSelector.getSubject());
        localLDAPCertSelector.setSubject(str);
        localCollection = localCertStore.getCertificates(localLDAPCertSelector);
        if (debug != null)
          debug.println("ForwardBuilder.getCerts(AIA): found " + localCollection.size() + " certs");
      }
      catch (Exception localException)
      {
        while (true)
          if (debug != null)
          {
            debug.println("ForwardBuilder.getCerts(AIA): exception while fetching certs from CertStore: " + localException);
            localException.printStackTrace();
          }
      }
      localArrayList.addAll(localCollection);
      if ((!(this.searchAllCertStores)) && (!(localArrayList.isEmpty())))
        return localArrayList;
    }
    label407: return localArrayList;
  }

  void verifyCert(X509Certificate paramX509Certificate, State paramState, List<X509Certificate> paramList)
    throws GeneralSecurityException
  {
    Object localObject1;
    Object localObject2;
    Object localObject3;
    Object localObject4;
    if (debug != null)
      debug.println("ForwardBuilder.verifyCert(SN: " + Debug.toHexString(paramX509Certificate.getSerialNumber()) + "\n  Issuer: " + paramX509Certificate.getIssuerX500Principal() + ")" + "\n  Subject: " + paramX509Certificate.getSubjectX500Principal() + ")");
    ForwardState localForwardState = (ForwardState)paramState;
    if (paramList != null)
    {
      bool = false;
      localObject1 = paramList.iterator();
      while (((Iterator)localObject1).hasNext())
      {
        localObject2 = (X509Certificate)((Iterator)localObject1).next();
        localObject3 = X509CertImpl.toImpl((X509Certificate)localObject2);
        localObject4 = ((X509CertImpl)localObject3).getPolicyMappingsExtension();
        if (localObject4 != null)
          bool = true;
        if (debug != null)
          debug.println("policyMappingFound = " + bool);
        if ((paramX509Certificate.equals(localObject2)) && (((this.buildParams.isPolicyMappingInhibited()) || (!(bool)))))
        {
          if (debug != null)
            debug.println("loop detected!!");
          throw new CertPathValidatorException("loop detected");
        }
      }
    }
    boolean bool = isTrustedCert(paramX509Certificate);
    if (!(bool))
    {
      localObject1 = paramX509Certificate.getCriticalExtensionOIDs();
      if (localObject1 == null)
        localObject1 = Collections.emptySet();
      localObject2 = localForwardState.forwardCheckers.iterator();
      while (((Iterator)localObject2).hasNext())
      {
        localObject3 = (PKIXCertPathChecker)((Iterator)localObject2).next();
        ((PKIXCertPathChecker)localObject3).check(paramX509Certificate, (Collection)localObject1);
      }
      localObject2 = this.buildParams.getCertPathCheckers().iterator();
      while (((Iterator)localObject2).hasNext())
      {
        localObject3 = (PKIXCertPathChecker)((Iterator)localObject2).next();
        if (!(((PKIXCertPathChecker)localObject3).isForwardCheckingSupported()))
        {
          localObject4 = ((PKIXCertPathChecker)localObject3).getSupportedExtensions();
          if (localObject4 != null)
            ((Set)localObject1).removeAll((Collection)localObject4);
        }
      }
      if (!(((Set)localObject1).isEmpty()))
      {
        ((Set)localObject1).remove(PKIXExtensions.BasicConstraints_Id.toString());
        ((Set)localObject1).remove(PKIXExtensions.NameConstraints_Id.toString());
        ((Set)localObject1).remove(PKIXExtensions.CertificatePolicies_Id.toString());
        ((Set)localObject1).remove(PKIXExtensions.PolicyMappings_Id.toString());
        ((Set)localObject1).remove(PKIXExtensions.PolicyConstraints_Id.toString());
        ((Set)localObject1).remove(PKIXExtensions.InhibitAnyPolicy_Id.toString());
        ((Set)localObject1).remove(PKIXExtensions.SubjectAlternativeName_Id.toString());
        ((Set)localObject1).remove(PKIXExtensions.KeyUsage_Id.toString());
        ((Set)localObject1).remove(PKIXExtensions.ExtendedKeyUsage_Id.toString());
        if (!(((Set)localObject1).isEmpty()))
          throw new CertificateException("Unrecognized critical extension(s)");
      }
    }
    if (localForwardState.isInitial())
      return;
    if (!(bool))
    {
      if (paramX509Certificate.getBasicConstraints() == -1)
        throw new CertificateException("cert is NOT a CA cert");
      KeyChecker.verifyCAKeyUsage(paramX509Certificate);
    }
    if ((this.buildParams.isRevocationEnabled()) && (localForwardState.crlChecker.certCanSignCrl(paramX509Certificate)) && (!(localForwardState.keyParamsNeeded())))
      localForwardState.crlChecker.check(localForwardState.cert, paramX509Certificate.getPublicKey(), true);
    if (!(localForwardState.keyParamsNeeded()))
      localForwardState.cert.verify(paramX509Certificate.getPublicKey(), this.buildParams.getSigProvider());
  }

  boolean isPathCompleted(X509Certificate paramX509Certificate)
  {
    TrustAnchor localTrustAnchor;
    Iterator localIterator = this.trustAnchors.iterator();
    while (true)
    {
      do
      {
        if (!(localIterator.hasNext()))
          break label233;
        localTrustAnchor = (TrustAnchor)localIterator.next();
        if (localTrustAnchor.getTrustedCert() == null)
          break label54;
      }
      while (!(paramX509Certificate.equals(localTrustAnchor.getTrustedCert())));
      this.trustAnchor = localTrustAnchor;
      return true;
      label54: X500Principal localX500Principal = CertPathHelper.getCA(localTrustAnchor);
      if (localX500Principal.equals(paramX509Certificate.getIssuerX500Principal()))
        break;
    }
    if (this.buildParams.isRevocationEnabled())
      try
      {
        CrlRevocationChecker localCrlRevocationChecker = new CrlRevocationChecker(localTrustAnchor.getCAPublicKey(), this.buildParams);
        localCrlRevocationChecker.check(paramX509Certificate, localTrustAnchor.getCAPublicKey(), true);
      }
      catch (CertPathValidatorException localCertPathValidatorException)
      {
        while (true)
          if (debug != null)
          {
            debug.println("ForwardBuilder.isPathCompleted() cpve");
            localCertPathValidatorException.printStackTrace();
          }
      }
      catch (Exception localException1)
      {
        while (true)
          if (debug != null)
          {
            debug.println("ForwardBuilder.isPathCompleted() unexpected exception");
            localException1.printStackTrace();
          }
      }
    try
    {
      paramX509Certificate.verify(localTrustAnchor.getCAPublicKey(), this.buildParams.getSigProvider());
    }
    catch (InvalidKeyException localInvalidKeyException)
    {
      while (true)
        if (debug != null)
          debug.println("ForwardBuilder.isPathCompleted() invalid DSA key found");
    }
    catch (Exception localException2)
    {
      while (true)
        if (debug != null)
        {
          debug.println("ForwardBuilder.isPathCompleted() 2 unexpected exception");
          localException2.printStackTrace();
        }
    }
    this.trustAnchor = localTrustAnchor;
    return true;
    label233: return false;
  }

  boolean isTrustedCert(X509Certificate paramX509Certificate)
  {
    return this.trustedCerts.contains(paramX509Certificate);
  }

  void addCertToPath(X509Certificate paramX509Certificate, LinkedList<X509Certificate> paramLinkedList)
  {
    paramLinkedList.addFirst(paramX509Certificate);
  }

  void removeFinalCertFromPath(LinkedList<X509Certificate> paramLinkedList)
  {
    paramLinkedList.removeFirst();
  }

  static class PKIXCertComparator
  implements Comparator<X509Certificate>
  {
    static final String METHOD_NME = "ForwardBuilder.PKIXCertComparator.compare()";
    private final Set<X500Principal> trustedSubjectDNs;

    PKIXCertComparator(Set<X500Principal> paramSet)
    {
      this.trustedSubjectDNs = paramSet;
    }

    public int compare(X509Certificate paramX509Certificate1, X509Certificate paramX509Certificate2)
    {
      int i;
      int j;
      X500Principal localX500Principal1 = paramX509Certificate1.getIssuerX500Principal();
      X500Principal localX500Principal2 = paramX509Certificate2.getIssuerX500Principal();
      X500Name localX500Name1 = X500Name.asX500Name(localX500Principal1);
      X500Name localX500Name2 = X500Name.asX500Name(localX500Principal2);
      if (ForwardBuilder.access$000() != null)
      {
        ForwardBuilder.access$000().println("ForwardBuilder.PKIXCertComparator.compare() o1 Issuer:  " + localX500Principal1.toString());
        ForwardBuilder.access$000().println("ForwardBuilder.PKIXCertComparator.compare() o2 Issuer:  " + localX500Principal2.toString());
      }
      if (ForwardBuilder.access$000() != null)
        ForwardBuilder.access$000().println("ForwardBuilder.PKIXCertComparator.compare() MATCH TRUSTED SUBJECT TEST...");
      Object localObject = this.trustedSubjectDNs.iterator();
      while (((Iterator)localObject).hasNext())
      {
        localX500Principal3 = (X500Principal)((Iterator)localObject).next();
        boolean bool1 = localX500Principal1.equals(localX500Principal3);
        boolean bool2 = localX500Principal2.equals(localX500Principal3);
        if ((bool1) && (bool2))
        {
          if (ForwardBuilder.access$000() != null)
            ForwardBuilder.access$000().println("ForwardBuilder.PKIXCertComparator.compare() m1 && m2; RETURN 0");
          return 0;
        }
        if (bool1)
        {
          if (ForwardBuilder.access$000() != null)
            ForwardBuilder.access$000().println("ForwardBuilder.PKIXCertComparator.compare() m1; RETURN -1");
          return -1;
        }
        if (bool2)
        {
          if (ForwardBuilder.access$000() != null)
            ForwardBuilder.access$000().println("ForwardBuilder.PKIXCertComparator.compare() m2; RETURN 1");
          return 1;
        }
      }
      if (ForwardBuilder.access$000() != null)
        ForwardBuilder.access$000().println("ForwardBuilder.PKIXCertComparator.compare() NAMING DESCENDANT TEST...");
      localObject = this.trustedSubjectDNs.iterator();
      while (((Iterator)localObject).hasNext())
      {
        localX500Principal3 = (X500Principal)((Iterator)localObject).next();
        localX500Name3 = X500Name.asX500Name(localX500Principal3);
        try
        {
          i = Builder.distance(localX500Name3, localX500Name1);
        }
        catch (IOException localIOException1)
        {
          i = -1;
        }
        try
        {
          j = Builder.distance(localX500Name3, localX500Name2);
        }
        catch (IOException localIOException2)
        {
          j = -1;
        }
        if (ForwardBuilder.access$000() != null)
        {
          ForwardBuilder.access$000().println("ForwardBuilder.PKIXCertComparator.compare() distanceTto1: " + i);
          ForwardBuilder.access$000().println("ForwardBuilder.PKIXCertComparator.compare() distanceTto2: " + j);
        }
        if ((i > 0) || (j > 0))
        {
          if (ForwardBuilder.access$000() != null)
            ForwardBuilder.access$000().println("ForwardBuilder.PKIXCertComparator.compare() distanceTto1 > 0 || distanceTto2 > 0...");
          if (i == j)
          {
            if (ForwardBuilder.access$000() != null)
              ForwardBuilder.access$000().println("ForwardBuilder.PKIXCertComparator.compare() distanceTto1 == distanceTto2; RETURN 0");
            return 0;
          }
          if ((i > 0) && (j <= 0))
          {
            if (ForwardBuilder.access$000() != null)
              ForwardBuilder.access$000().println("ForwardBuilder.PKIXCertComparator.compare() distanceTto1 > 0 && distanceTto2 <= 0); RETURN -1");
            return -1;
          }
          if ((i <= 0) && (j > 0))
          {
            if (ForwardBuilder.access$000() != null)
              ForwardBuilder.access$000().println("ForwardBuilder.PKIXCertComparator.compare() distanceTto1 <= 0 && distanceTto2 > 0; RETURN 1");
            return 1;
          }
          if (i < j)
          {
            if (ForwardBuilder.access$000() != null)
              ForwardBuilder.access$000().println("ForwardBuilder.PKIXCertComparator.compare() distanceTto1 < distance Tto2; RETURN -1");
            return -1;
          }
          if (ForwardBuilder.access$000() != null)
            ForwardBuilder.access$000().println("ForwardBuilder.PKIXCertComparator.compare() distanceTto1 >= distanceTto2; RETURN 1");
          return 1;
        }
      }
      if (ForwardBuilder.access$000() != null)
        ForwardBuilder.access$000().println("ForwardBuilder.PKIXCertComparator.compare() NAMING ANCESTOR TEST...");
      localObject = this.trustedSubjectDNs.iterator();
      while (((Iterator)localObject).hasNext())
      {
        localX500Principal3 = (X500Principal)((Iterator)localObject).next();
        localX500Name3 = X500Name.asX500Name(localX500Principal3);
        i = 0;
        j = 0;
        try
        {
          i = Builder.distance(localX500Name3, localX500Name1);
        }
        catch (IOException localIOException3)
        {
          i = 2147483647;
        }
        try
        {
          j = Builder.distance(localX500Name3, localX500Name2);
        }
        catch (IOException localIOException4)
        {
          j = 2147483647;
        }
        if (ForwardBuilder.access$000() != null)
        {
          ForwardBuilder.access$000().println("ForwardBuilder.PKIXCertComparator.compare() distanceTto1: " + i);
          ForwardBuilder.access$000().println("ForwardBuilder.PKIXCertComparator.compare() distanceTto2: " + j);
        }
        if ((i < 0) || (j < 0))
        {
          if (ForwardBuilder.access$000() != null)
            ForwardBuilder.access$000().println("ForwardBuilder.PKIXCertComparator.compare() distanceTto1 < 0 || distanceTto2 < 0...");
          if (i == j)
          {
            if (ForwardBuilder.access$000() != null)
              ForwardBuilder.access$000().println("ForwardBuilder.PKIXCertComparator.compare() distance==; RETURN 0");
            return 0;
          }
          if ((i < 0) && (j >= 0))
          {
            if (ForwardBuilder.access$000() != null)
              ForwardBuilder.access$000().println("ForwardBuilder.PKIXCertComparator.compare() distanceTto1 < 0 && distanceTto2 >= 0; RETURN -1");
            return -1;
          }
          if ((i >= 0) && (j < 0))
          {
            if (ForwardBuilder.access$000() != null)
              ForwardBuilder.access$000().println("ForwardBuilder.PKIXCertComparator.compare() distanceTto1 >= 0 && distanceTto2 < 0; RETURN 1");
            return 1;
          }
          if (i > j)
          {
            if (ForwardBuilder.access$000() != null)
              ForwardBuilder.access$000().println("ForwardBuilder.PKIXCertComparator.compare() distanceTto1 > distanceTto2; RETURN -1");
            return -1;
          }
          if (ForwardBuilder.access$000() != null)
            ForwardBuilder.access$000().println("ForwardBuilder.PKIXCertComparator.compare() distanceTto1 <= distanceTto2; RETURN 1");
          return 1;
        }
      }
      if (ForwardBuilder.access$000() != null)
        ForwardBuilder.access$000().println("ForwardBuilder.PKIXCertComparator.compare() SAME NAMESPACE AS TRUSTED TEST...");
      localObject = this.trustedSubjectDNs.iterator();
      while (((Iterator)localObject).hasNext())
      {
        localX500Principal3 = (X500Principal)((Iterator)localObject).next();
        localX500Name3 = X500Name.asX500Name(localX500Principal3);
        localX500Name4 = localX500Name3.commonAncestor(localX500Name1);
        X500Name localX500Name5 = localX500Name3.commonAncestor(localX500Name2);
        if (ForwardBuilder.access$000() != null)
        {
          ForwardBuilder.access$000().println("ForwardBuilder.PKIXCertComparator.compare() tAo1: " + String.valueOf(localX500Name4));
          ForwardBuilder.access$000().println("ForwardBuilder.PKIXCertComparator.compare() tAo2: " + String.valueOf(localX500Name5));
        }
        if ((localX500Name4 != null) || (localX500Name5 != null))
        {
          if (ForwardBuilder.access$000() != null)
            ForwardBuilder.access$000().println("ForwardBuilder.PKIXCertComparator.compare() tAo1 != null || tAo2 != null...");
          if ((localX500Name4 != null) && (localX500Name5 != null))
          {
            if (ForwardBuilder.access$000() != null)
              ForwardBuilder.access$000().println("ForwardBuilder.PKIXCertComparator.compare() tAo1 != null && tAo2 != null...");
            l = 2147483647;
            int i1 = 2147483647;
            try
            {
              l = Builder.hops(localX500Name3, localX500Name1);
            }
            catch (IOException localIOException7)
            {
              if (ForwardBuilder.access$000() != null)
              {
                ForwardBuilder.access$000().println("ForwardBuilder.PKIXCertComparator.compare() exception in Builder.hops(tSubject, cIssuer1)");
                localIOException7.printStackTrace();
              }
            }
            try
            {
              i1 = Builder.hops(localX500Name3, localX500Name2);
            }
            catch (IOException localIOException8)
            {
              if (ForwardBuilder.access$000() != null)
              {
                ForwardBuilder.access$000().println("ForwardBuilder.PKIXCertComparator.compare() exception in Builder.hops(tSubject, cIssuer2)");
                localIOException8.printStackTrace();
              }
            }
            if (ForwardBuilder.access$000() != null)
            {
              ForwardBuilder.access$000().println("ForwardBuilder.PKIXCertComparator.compare() hopsTto1: " + l);
              ForwardBuilder.access$000().println("ForwardBuilder.PKIXCertComparator.compare() hopsTto2: " + i1);
            }
            if (l == i1)
            {
              if (ForwardBuilder.access$000() != null)
                ForwardBuilder.access$000().println("ForwardBuilder.PKIXCertComparator.compare() hopsTto1 == hopsTto2; continue");
            }
            else
            {
              if (l > i1)
              {
                if (ForwardBuilder.access$000() != null)
                  ForwardBuilder.access$000().println("ForwardBuilder.PKIXCertComparator.compare() hopsTto1 > hopsTto2; RETURN 1");
                return 1;
              }
              if (ForwardBuilder.access$000() != null)
                ForwardBuilder.access$000().println("ForwardBuilder.PKIXCertComparator.compare() hopsTto1 < hopsTto2; RETURN -1");
              return -1;
            }
          }
          else
          {
            if (localX500Name4 == null)
            {
              if (ForwardBuilder.access$000() != null)
                ForwardBuilder.access$000().println("ForwardBuilder.PKIXCertComparator.compare() tA01 == null; RETURN 1");
              return 1;
            }
            if (ForwardBuilder.access$000() != null)
              ForwardBuilder.access$000().println("ForwardBuilder.PKIXCertComparator.compare() tA02 == null; RETURN -1");
            return -1;
          }
        }
      }
      if (ForwardBuilder.access$000() != null)
        ForwardBuilder.access$000().println("ForwardBuilder.PKIXCertComparator.compare() CERT ISSUER/SUBJECT COMPARISON TEST...");
      localObject = paramX509Certificate1.getSubjectX500Principal();
      X500Principal localX500Principal3 = paramX509Certificate2.getSubjectX500Principal();
      X500Name localX500Name3 = X500Name.asX500Name((X500Principal)localObject);
      X500Name localX500Name4 = X500Name.asX500Name(localX500Principal3);
      if (ForwardBuilder.access$000() != null)
      {
        ForwardBuilder.access$000().println("ForwardBuilder.PKIXCertComparator.compare() o1 Subject: " + ((X500Principal)localObject).toString());
        ForwardBuilder.access$000().println("ForwardBuilder.PKIXCertComparator.compare() o2 Subject: " + localX500Principal3.toString());
      }
      int k = 0;
      int l = 0;
      try
      {
        k = Builder.distance(localX500Name3, localX500Name1);
      }
      catch (IOException localIOException5)
      {
        k = 2147483647;
      }
      try
      {
        l = Builder.distance(localX500Name4, localX500Name2);
      }
      catch (IOException localIOException6)
      {
        l = 2147483647;
      }
      if (ForwardBuilder.access$000() != null)
      {
        ForwardBuilder.access$000().println("ForwardBuilder.PKIXCertComparator.compare() distanceStoI1: " + k);
        ForwardBuilder.access$000().println("ForwardBuilder.PKIXCertComparator.compare() distanceStoI2: " + l);
      }
      if (l > k)
      {
        if (ForwardBuilder.access$000() != null)
          ForwardBuilder.access$000().println("ForwardBuilder.PKIXCertComparator.compare() distanceStoI2 > distanceStoI1; RETURN -1");
        return -1;
      }
      if (l < k)
      {
        if (ForwardBuilder.access$000() != null)
          ForwardBuilder.access$000().println("ForwardBuilder.PKIXCertComparator.compare() distanceStoI2 < distanceStoI1; RETURN 1");
        return 1;
      }
      if (ForwardBuilder.access$000() != null)
        ForwardBuilder.access$000().println("ForwardBuilder.PKIXCertComparator.compare() no tests matched; RETURN 0");
      return 0;
    }
  }
}