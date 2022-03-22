package sun.security.provider.certpath;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.PublicKey;
import java.security.cert.CertPathBuilderException;
import java.security.cert.CertPathBuilderResult;
import java.security.cert.CertPathBuilderSpi;
import java.security.cert.CertPathParameters;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertStore;
import java.security.cert.CertStoreException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.PKIXCertPathChecker;
import java.security.cert.PolicyNode;
import java.security.cert.TrustAnchor;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.security.interfaces.DSAPublicKey;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.security.auth.x500.X500Principal;
import sun.security.util.Debug;
import sun.security.util.ObjectIdentifier;
import sun.security.x509.PKIXExtensions;

public final class SunCertPathBuilder extends CertPathBuilderSpi
{
  private static final Debug debug = Debug.getInstance("certpath");
  private PKIXBuilderParameters buildParams;
  private CertificateFactory cf;
  private boolean pathCompleted = false;
  private X500Principal targetSubjectDN;
  private PolicyNode policyTreeResult;
  private TrustAnchor trustAnchor;
  private PublicKey finalPublicKey;
  private X509CertSelector targetSel;
  private List<CertStore> orderedCertStores;

  public SunCertPathBuilder()
    throws CertPathBuilderException
  {
    try
    {
      this.cf = CertificateFactory.getInstance("X.509");
    }
    catch (CertificateException localCertificateException)
    {
      throw new CertPathBuilderException(localCertificateException);
    }
  }

  public CertPathBuilderResult engineBuild(CertPathParameters paramCertPathParameters)
    throws CertPathBuilderException, InvalidAlgorithmParameterException
  {
    if (debug != null)
      debug.println("SunCertPathBuilder.engineBuild(" + paramCertPathParameters + ")");
    if (!(paramCertPathParameters instanceof PKIXBuilderParameters))
      throw new InvalidAlgorithmParameterException("inappropriate parameter type, must be an instance of PKIXBuilderParameters");
    boolean bool = true;
    if (paramCertPathParameters instanceof SunCertPathBuilderParameters)
      bool = ((SunCertPathBuilderParameters)paramCertPathParameters).getBuildForward();
    this.buildParams = ((PKIXBuilderParameters)paramCertPathParameters);
    Object localObject1 = this.buildParams.getTrustAnchors().iterator();
    while (((Iterator)localObject1).hasNext())
    {
      localObject2 = (TrustAnchor)((Iterator)localObject1).next();
      if (((TrustAnchor)localObject2).getNameConstraints() != null)
        throw new InvalidAlgorithmParameterException("name constraints in trust anchor not supported");
    }
    localObject1 = this.buildParams.getTargetCertConstraints();
    if (!(localObject1 instanceof X509CertSelector))
      throw new InvalidAlgorithmParameterException("the targetCertConstraints parameter must be an X509CertSelector");
    this.targetSel = ((X509CertSelector)localObject1);
    this.targetSubjectDN = CertPathHelper.getSubject(this.targetSel);
    if (this.targetSubjectDN == null)
    {
      localObject2 = this.targetSel.getCertificate();
      if (localObject2 != null)
        this.targetSubjectDN = ((X509Certificate)localObject2).getSubjectX500Principal();
    }
    this.orderedCertStores = new ArrayList(this.buildParams.getCertStores());
    Collections.sort(this.orderedCertStores, new CertStoreComparator(null));
    if (this.targetSubjectDN == null)
      this.targetSubjectDN = getTargetSubjectDN(this.orderedCertStores, this.targetSel);
    if (this.targetSubjectDN == null)
      throw new InvalidAlgorithmParameterException("Could not determine unique target subject");
    Object localObject2 = new ArrayList();
    CertPathBuilderResult localCertPathBuilderResult = buildCertPath(bool, false, (List)localObject2);
    if (localCertPathBuilderResult == null)
    {
      if (debug != null)
        debug.println("SunCertPathBuilder.engineBuild: 2nd pass");
      ((List)localObject2).clear();
      localCertPathBuilderResult = buildCertPath(bool, true, (List)localObject2);
      if (localCertPathBuilderResult == null)
        throw new SunCertPathBuilderException("unable to find valid certification path to requested target", new AdjacencyList((List)localObject2));
    }
    return ((CertPathBuilderResult)(CertPathBuilderResult)localCertPathBuilderResult);
  }

  private CertPathBuilderResult buildCertPath(boolean paramBoolean1, boolean paramBoolean2, List<List<Vertex>> paramList)
    throws CertPathBuilderException
  {
    this.pathCompleted = false;
    this.trustAnchor = null;
    this.finalPublicKey = null;
    this.policyTreeResult = null;
    LinkedList localLinkedList = new LinkedList();
    try
    {
      if (paramBoolean1)
        buildForward(paramList, localLinkedList, paramBoolean2);
      else
        buildReverse(paramList, localLinkedList);
    }
    catch (Exception localException1)
    {
      if (debug != null)
      {
        debug.println("SunCertPathBuilder.engineBuild() exception in build");
        localException1.printStackTrace();
      }
      throw new SunCertPathBuilderException("unable to find valid certification path to requested target", localException1, new AdjacencyList(paramList));
    }
    try
    {
      if (this.pathCompleted)
      {
        if (debug != null)
          debug.println("SunCertPathBuilder.engineBuild() pathCompleted");
        Collections.reverse(localLinkedList);
        return new SunCertPathBuilderResult(this.cf.generateCertPath(localLinkedList), this.trustAnchor, this.policyTreeResult, this.finalPublicKey, new AdjacencyList(paramList));
      }
    }
    catch (Exception localException2)
    {
      if (debug != null)
      {
        debug.println("SunCertPathBuilder.engineBuild() exception in wrap-up");
        localException2.printStackTrace();
      }
      throw new SunCertPathBuilderException("unable to find valid certification path to requested target", localException2, new AdjacencyList(paramList));
    }
    return null;
  }

  private void buildReverse(List<List<Vertex>> paramList, LinkedList<X509Certificate> paramLinkedList)
    throws Exception
  {
    if (debug != null)
    {
      debug.println("SunCertPathBuilder.buildReverse()...");
      debug.println("SunCertPathBuilder.buildReverse() InitialPolicies: " + this.buildParams.getInitialPolicies());
    }
    ReverseState localReverseState = new ReverseState();
    paramList.clear();
    paramList.add(new LinkedList());
    Iterator localIterator = this.buildParams.getTrustAnchors().iterator();
    if (localIterator.hasNext())
    {
      TrustAnchor localTrustAnchor = (TrustAnchor)localIterator.next();
      if (anchorIsTarget(localTrustAnchor, this.targetSel))
      {
        this.trustAnchor = localTrustAnchor;
        this.pathCompleted = true;
        this.finalPublicKey = localTrustAnchor.getTrustedCert().getPublicKey();
      }
      else
      {
        localReverseState.initState(this.buildParams.getMaxPathLength(), this.buildParams.isExplicitPolicyRequired(), this.buildParams.isPolicyMappingInhibited(), this.buildParams.isAnyPolicyInhibited(), this.buildParams.getCertPathCheckers());
        localReverseState.updateState(localTrustAnchor);
        localReverseState.crlChecker = new CrlRevocationChecker(null, this.buildParams);
        try
        {
          depthFirstSearchReverse(null, localReverseState, new ReverseBuilder(this.buildParams, this.targetSubjectDN), paramList, paramLinkedList);
        }
        catch (Exception localException)
        {
          while (localIterator.hasNext());
          throw localException;
        }
      }
    }
    if (debug != null)
    {
      debug.println("SunCertPathBuilder.buildReverse() returned from depthFirstSearchReverse()");
      debug.println("SunCertPathBuilder.buildReverse() certPathList.size: " + paramLinkedList.size());
    }
  }

  private void buildForward(List<List<Vertex>> paramList, LinkedList<X509Certificate> paramLinkedList, boolean paramBoolean)
    throws GeneralSecurityException, IOException
  {
    if (debug != null)
      debug.println("SunCertPathBuilder.buildForward()...");
    ForwardState localForwardState = new ForwardState();
    localForwardState.initState(this.buildParams.getCertPathCheckers());
    paramList.clear();
    paramList.add(new LinkedList());
    localForwardState.crlChecker = new CrlRevocationChecker(null, this.buildParams);
    depthFirstSearchForward(this.targetSubjectDN, localForwardState, new ForwardBuilder(this.buildParams, this.targetSubjectDN, paramBoolean), paramList, paramLinkedList);
  }

  void depthFirstSearchForward(X500Principal paramX500Principal, ForwardState paramForwardState, ForwardBuilder paramForwardBuilder, List<List<Vertex>> paramList, LinkedList<X509Certificate> paramLinkedList)
    throws GeneralSecurityException, IOException
  {
    if (debug != null)
      debug.println("SunCertPathBuilder.depthFirstSearchForward(" + paramX500Principal + ", " + paramForwardState.toString() + ")");
    List localList = addVertices(paramForwardBuilder.getMatchingCerts(paramForwardState, this.orderedCertStores), paramList);
    if (debug != null)
      debug.println("SunCertPathBuilder.depthFirstSearchForward(): certs.size=" + localList.size());
    Iterator localIterator1 = localList.iterator();
    while (localIterator1.hasNext())
    {
      Vertex localVertex = (Vertex)localIterator1.next();
      ForwardState localForwardState = (ForwardState)paramForwardState.clone();
      X509Certificate localX509Certificate1 = (X509Certificate)localVertex.getCertificate();
      try
      {
        paramForwardBuilder.verifyCert(localX509Certificate1, localForwardState, paramLinkedList);
      }
      catch (GeneralSecurityException localGeneralSecurityException)
      {
        while (true)
        {
          if (debug != null)
          {
            debug.println("SunCertPathBuilder.depthFirstSearchForward(): validation failed: " + localGeneralSecurityException);
            localGeneralSecurityException.printStackTrace();
          }
          localVertex.setThrowable(localGeneralSecurityException);
        }
      }
      if (paramForwardBuilder.isPathCompleted(localX509Certificate1))
      {
        BasicChecker localBasicChecker = null;
        if (debug != null)
          debug.println("SunCertPathBuilder.depthFirstSearchForward(): commencing final verification");
        ArrayList localArrayList1 = new ArrayList(paramLinkedList);
        if (paramForwardBuilder.trustAnchor.getTrustedCert() == null)
          localArrayList1.add(0, localX509Certificate1);
        HashSet localHashSet = new HashSet(1);
        localHashSet.add("2.5.29.32.0");
        PolicyNodeImpl localPolicyNodeImpl = new PolicyNodeImpl(null, "2.5.29.32.0", null, false, localHashSet, false);
        PolicyChecker localPolicyChecker = new PolicyChecker(this.buildParams.getInitialPolicies(), localArrayList1.size(), this.buildParams.isExplicitPolicyRequired(), this.buildParams.isPolicyMappingInhibited(), this.buildParams.isAnyPolicyInhibited(), this.buildParams.getPolicyQualifiersRejected(), localPolicyNodeImpl);
        ArrayList localArrayList2 = new ArrayList(this.buildParams.getCertPathCheckers());
        int i = 0;
        localArrayList2.add(i, localPolicyChecker);
        ++i;
        if (localForwardState.keyParamsNeeded())
        {
          PublicKey localPublicKey = localX509Certificate1.getPublicKey();
          if (paramForwardBuilder.trustAnchor.getTrustedCert() == null)
          {
            localPublicKey = paramForwardBuilder.trustAnchor.getCAPublicKey();
            if (debug != null)
              debug.println("SunCertPathBuilder.depthFirstSearchForward using buildParams public key: " + localPublicKey.toString());
          }
          localBasicChecker = new BasicChecker(localPublicKey, localX509Certificate1.getSubjectX500Principal(), this.buildParams.getDate(), this.buildParams.getSigProvider(), true);
          localArrayList2.add(i, localBasicChecker);
          ++i;
          if (this.buildParams.isRevocationEnabled())
          {
            localArrayList2.add(i, new CrlRevocationChecker(localPublicKey, this.buildParams));
            ++i;
          }
        }
        for (int j = 0; j < localArrayList1.size(); ++j)
        {
          PKIXCertPathChecker localPKIXCertPathChecker;
          X509Certificate localX509Certificate2 = (X509Certificate)localArrayList1.get(j);
          if (debug != null)
            debug.println("current subject = " + localX509Certificate2.getSubjectX500Principal());
          Set localSet1 = localX509Certificate2.getCriticalExtensionOIDs();
          if (localSet1 == null)
            localSet1 = Collections.emptySet();
          for (int k = 0; k < localArrayList2.size(); ++k)
          {
            localPKIXCertPathChecker = (PKIXCertPathChecker)localArrayList2.get(k);
            if ((k < i) || (!(localPKIXCertPathChecker.isForwardCheckingSupported())))
            {
              if (j == 0)
                localPKIXCertPathChecker.init(false);
              try
              {
                localPKIXCertPathChecker.check(localX509Certificate2, localSet1);
              }
              catch (CertPathValidatorException localCertPathValidatorException)
              {
                while (true)
                {
                  if (debug != null)
                    debug.println("SunCertPathBuilder.depthFirstSearchForward(): final verification failed: " + localCertPathValidatorException);
                  localVertex.setThrowable(localCertPathValidatorException);
                }
              }
            }
          }
          Iterator localIterator2 = this.buildParams.getCertPathCheckers().iterator();
          while (localIterator2.hasNext())
          {
            localPKIXCertPathChecker = (PKIXCertPathChecker)localIterator2.next();
            if (localPKIXCertPathChecker.isForwardCheckingSupported())
            {
              Set localSet2 = localPKIXCertPathChecker.getSupportedExtensions();
              if (localSet2 != null)
                localSet1.removeAll(localSet2);
            }
          }
          if (!(localSet1.isEmpty()))
          {
            localSet1.remove(PKIXExtensions.BasicConstraints_Id.toString());
            localSet1.remove(PKIXExtensions.NameConstraints_Id.toString());
            localSet1.remove(PKIXExtensions.CertificatePolicies_Id.toString());
            localSet1.remove(PKIXExtensions.PolicyMappings_Id.toString());
            localSet1.remove(PKIXExtensions.PolicyConstraints_Id.toString());
            localSet1.remove(PKIXExtensions.InhibitAnyPolicy_Id.toString());
            localSet1.remove(PKIXExtensions.SubjectAlternativeName_Id.toString());
            localSet1.remove(PKIXExtensions.KeyUsage_Id.toString());
            localSet1.remove(PKIXExtensions.ExtendedKeyUsage_Id.toString());
            if (!(localSet1.isEmpty()))
              throw new CertPathValidatorException("unrecognized critical extension(s)");
          }
        }
        if (debug != null)
          debug.println("SunCertPathBuilder.depthFirstSearchForward(): final verification succeeded - path completed!");
        this.pathCompleted = true;
        if (paramForwardBuilder.trustAnchor.getTrustedCert() == null)
          paramForwardBuilder.addCertToPath(localX509Certificate1, paramLinkedList);
        this.trustAnchor = paramForwardBuilder.trustAnchor;
        if (localBasicChecker != null)
        {
          this.finalPublicKey = localBasicChecker.getPublicKey();
        }
        else
        {
          Object localObject;
          if (paramLinkedList.size() == 0)
            localObject = paramForwardBuilder.trustAnchor.getTrustedCert();
          else
            localObject = (Certificate)paramLinkedList.get(paramLinkedList.size() - 1);
          this.finalPublicKey = ((Certificate)localObject).getPublicKey();
        }
        this.policyTreeResult = localPolicyChecker.getPolicyTree();
        return;
      }
      paramForwardBuilder.addCertToPath(localX509Certificate1, paramLinkedList);
      localForwardState.updateState(localX509Certificate1);
      paramList.add(new LinkedList());
      localVertex.setIndex(paramList.size() - 1);
      depthFirstSearchForward(localX509Certificate1.getIssuerX500Principal(), localForwardState, paramForwardBuilder, paramList, paramLinkedList);
      if (this.pathCompleted)
        return;
      if (debug != null)
        debug.println("SunCertPathBuilder.depthFirstSearchForward(): backtracking");
      paramForwardBuilder.removeFinalCertFromPath(paramLinkedList);
    }
  }

  void depthFirstSearchReverse(X500Principal paramX500Principal, ReverseState paramReverseState, ReverseBuilder paramReverseBuilder, List<List<Vertex>> paramList, LinkedList<X509Certificate> paramLinkedList)
    throws GeneralSecurityException, IOException
  {
    if (debug != null)
      debug.println("SunCertPathBuilder.depthFirstSearchReverse(" + paramX500Principal + ", " + paramReverseState.toString() + ")");
    List localList = addVertices(paramReverseBuilder.getMatchingCerts(paramReverseState, this.orderedCertStores), paramList);
    if (debug != null)
      debug.println("SunCertPathBuilder.depthFirstSearchReverse(): certs.size=" + localList.size());
    Iterator localIterator = localList.iterator();
    while (localIterator.hasNext())
    {
      Vertex localVertex = (Vertex)localIterator.next();
      ReverseState localReverseState = (ReverseState)paramReverseState.clone();
      X509Certificate localX509Certificate = (X509Certificate)localVertex.getCertificate();
      try
      {
        paramReverseBuilder.verifyCert(localX509Certificate, localReverseState, paramLinkedList);
      }
      catch (GeneralSecurityException localGeneralSecurityException)
      {
        while (true)
        {
          if (debug != null)
            debug.println("SunCertPathBuilder.depthFirstSearchReverse(): validation failed: " + localGeneralSecurityException);
          localVertex.setThrowable(localGeneralSecurityException);
        }
      }
      if (!(paramReverseState.isInitial()))
        paramReverseBuilder.addCertToPath(localX509Certificate, paramLinkedList);
      this.trustAnchor = paramReverseState.trustAnchor;
      if (paramReverseBuilder.isPathCompleted(localX509Certificate))
      {
        if (debug != null)
          debug.println("SunCertPathBuilder.depthFirstSearchReverse(): path completed!");
        this.pathCompleted = true;
        PolicyNodeImpl localPolicyNodeImpl = localReverseState.rootNode;
        if (localPolicyNodeImpl == null)
        {
          this.policyTreeResult = null;
        }
        else
        {
          this.policyTreeResult = localPolicyNodeImpl.copyTree();
          ((PolicyNodeImpl)this.policyTreeResult).setImmutable();
        }
        this.finalPublicKey = localX509Certificate.getPublicKey();
        if ((this.finalPublicKey instanceof DSAPublicKey) && (((DSAPublicKey)this.finalPublicKey).getParams() == null))
          this.finalPublicKey = BasicChecker.makeInheritedParamsKey(this.finalPublicKey, paramReverseState.pubKey);
        return;
      }
      localReverseState.updateState(localX509Certificate);
      paramList.add(new LinkedList());
      localVertex.setIndex(paramList.size() - 1);
      depthFirstSearchReverse(localX509Certificate.getSubjectX500Principal(), localReverseState, paramReverseBuilder, paramList, paramLinkedList);
      if (this.pathCompleted)
        return;
      if (debug != null)
        debug.println("SunCertPathBuilder.depthFirstSearchReverse(): backtracking");
      if (!(paramReverseState.isInitial()))
        paramReverseBuilder.removeFinalCertFromPath(paramLinkedList);
    }
    if (debug != null)
      debug.println("SunCertPathBuilder.depthFirstSearchReverse() all certs in this adjacency list checked");
  }

  private List<Vertex> addVertices(Collection<X509Certificate> paramCollection, List<List<Vertex>> paramList)
  {
    List localList = (List)paramList.get(paramList.size() - 1);
    Iterator localIterator = paramCollection.iterator();
    while (localIterator.hasNext())
    {
      X509Certificate localX509Certificate = (X509Certificate)localIterator.next();
      Vertex localVertex = new Vertex(localX509Certificate);
      localList.add(localVertex);
    }
    return localList;
  }

  private boolean anchorIsTarget(TrustAnchor paramTrustAnchor, X509CertSelector paramX509CertSelector)
  {
    X509Certificate localX509Certificate = paramTrustAnchor.getTrustedCert();
    if (localX509Certificate != null)
      return paramX509CertSelector.match(localX509Certificate);
    return false;
  }

  private X500Principal getTargetSubjectDN(List<CertStore> paramList, X509CertSelector paramX509CertSelector)
  {
    Iterator localIterator = paramList.iterator();
    while (localIterator.hasNext())
    {
      CertStore localCertStore = (CertStore)localIterator.next();
      try
      {
        Collection localCollection = localCertStore.getCertificates(paramX509CertSelector);
        if (!(localCollection.isEmpty()))
        {
          X509Certificate localX509Certificate = (X509Certificate)localCollection.iterator().next();
          return localX509Certificate.getSubjectX500Principal();
        }
      }
      catch (CertStoreException localCertStoreException)
      {
        if (debug != null)
        {
          debug.println("SunCertPathBuilder.getTargetSubjectDN: non-fatal exception retrieving certs: " + localCertStoreException);
          localCertStoreException.printStackTrace();
        }
      }
    }
    return null;
  }

  private static class CertStoreComparator
  implements Comparator<CertStore>
  {
    public int compare(CertStore paramCertStore1, CertStore paramCertStore2)
    {
      if (Builder.isLocalCertStore(paramCertStore1))
        return -1;
      return 1;
    }
  }
}