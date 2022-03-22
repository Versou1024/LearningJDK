package sun.security.provider.certpath;

import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.CertPath;
import java.security.cert.CertPathBuilder;
import java.security.cert.CertPathBuilderException;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertStore;
import java.security.cert.CertStoreParameters;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.PKIXCertPathBuilderResult;
import java.security.cert.PKIXCertPathChecker;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509CRL;
import java.security.cert.X509CRLEntry;
import java.security.cert.X509CRLSelector;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.security.interfaces.DSAPublicKey;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collection<Ljava.security.cert.X509CRL;>;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.security.auth.x500.X500Principal;
import sun.security.util.Debug;
import sun.security.util.ObjectIdentifier;
import sun.security.x509.AccessDescription;
import sun.security.x509.AuthorityInfoAccessExtension;
import sun.security.x509.CRLDistributionPointsExtension;
import sun.security.x509.DistributionPoint;
import sun.security.x509.GeneralName;
import sun.security.x509.GeneralNameInterface;
import sun.security.x509.GeneralNames;
import sun.security.x509.KeyUsageExtension;
import sun.security.x509.PKIXExtensions;
import sun.security.x509.X500Name;
import sun.security.x509.X509CRLEntryImpl;
import sun.security.x509.X509CertImpl;

class CrlRevocationChecker extends PKIXCertPathChecker
{
  private static final Debug debug = Debug.getInstance("certpath");
  private final PublicKey mInitPubKey;
  private final List<CertStore> mStores;
  private final String mSigProvider;
  private final Date mCurrentTime;
  private PublicKey mPrevPubKey;
  private boolean mCRLSignFlag;
  private HashSet<X509CRL> mPossibleCRLs;
  private HashSet<X509CRL> mApprovedCRLs;
  private final PKIXParameters mParams;
  private final Collection<X509Certificate> mExtraCerts;
  private static final boolean[] mCrlSignUsage = { false, false, false, false, false, false, true };
  private static final boolean[] ALL_REASONS = { true, true, true, true, true, true, true, true, true };
  private boolean mOnlyEECert;

  CrlRevocationChecker(PublicKey paramPublicKey, PKIXParameters paramPKIXParameters)
    throws CertPathValidatorException
  {
    this(paramPublicKey, paramPKIXParameters, null);
  }

  CrlRevocationChecker(PublicKey paramPublicKey, PKIXParameters paramPKIXParameters, Collection<X509Certificate> paramCollection)
    throws CertPathValidatorException
  {
    this(paramPublicKey, paramPKIXParameters, paramCollection, false);
  }

  CrlRevocationChecker(PublicKey paramPublicKey, PKIXParameters paramPKIXParameters, Collection<X509Certificate> paramCollection, boolean paramBoolean)
    throws CertPathValidatorException
  {
    this.mOnlyEECert = false;
    this.mInitPubKey = paramPublicKey;
    this.mParams = paramPKIXParameters;
    this.mStores = paramPKIXParameters.getCertStores();
    this.mSigProvider = paramPKIXParameters.getSigProvider();
    this.mExtraCerts = paramCollection;
    Date localDate = paramPKIXParameters.getDate();
    this.mCurrentTime = new Date();
    this.mOnlyEECert = paramBoolean;
    init(false);
  }

  public void init(boolean paramBoolean)
    throws CertPathValidatorException
  {
    if (!(paramBoolean))
    {
      this.mPrevPubKey = this.mInitPubKey;
      this.mCRLSignFlag = true;
    }
    else
    {
      throw new CertPathValidatorException("forward checking not supported");
    }
  }

  public boolean isForwardCheckingSupported()
  {
    return false;
  }

  public Set<String> getSupportedExtensions()
  {
    return null;
  }

  public void check(Certificate paramCertificate, Collection<String> paramCollection)
    throws CertPathValidatorException
  {
    X509Certificate localX509Certificate = (X509Certificate)paramCertificate;
    verifyRevocationStatus(localX509Certificate, this.mPrevPubKey, this.mCRLSignFlag, true);
    PublicKey localPublicKey = localX509Certificate.getPublicKey();
    if ((localPublicKey instanceof DSAPublicKey) && (((DSAPublicKey)localPublicKey).getParams() == null))
      localPublicKey = BasicChecker.makeInheritedParamsKey(localPublicKey, this.mPrevPubKey);
    this.mPrevPubKey = localPublicKey;
    this.mCRLSignFlag = certCanSignCrl(localX509Certificate);
  }

  public boolean check(X509Certificate paramX509Certificate, PublicKey paramPublicKey, boolean paramBoolean)
    throws CertPathValidatorException
  {
    verifyRevocationStatus(paramX509Certificate, paramPublicKey, paramBoolean, true);
    return certCanSignCrl(paramX509Certificate);
  }

  public boolean certCanSignCrl(X509Certificate paramX509Certificate)
  {
    boolean[] arrayOfBoolean;
    try
    {
      arrayOfBoolean = paramX509Certificate.getKeyUsage();
      if (arrayOfBoolean != null)
      {
        KeyUsageExtension localKeyUsageExtension = new KeyUsageExtension(arrayOfBoolean);
        Boolean localBoolean = (Boolean)localKeyUsageExtension.get("crl_sign");
        return localBoolean.booleanValue();
      }
      return true;
    }
    catch (Exception localException)
    {
      if (debug != null)
        debug.println("CrlRevocationChecker.certCanSignCRL() unexpected exception");
    }
    return false;
  }

  private void verifyRevocationStatus(X509Certificate paramX509Certificate, PublicKey paramPublicKey, boolean paramBoolean1, boolean paramBoolean2)
    throws CertPathValidatorException
  {
    verifyRevocationStatus(paramX509Certificate, paramPublicKey, paramBoolean1, paramBoolean2, null);
  }

  private void verifyRevocationStatus(X509Certificate paramX509Certificate, PublicKey paramPublicKey, boolean paramBoolean1, boolean paramBoolean2, Set<X509Certificate> paramSet)
    throws CertPathValidatorException
  {
    Object localObject2;
    String str = "revocation status";
    if (debug != null)
      debug.println("CrlRevocationChecker.verifyRevocationStatus() ---checking " + str + "...");
    if ((this.mOnlyEECert) && (paramX509Certificate.getBasicConstraints() != -1))
    {
      if (debug != null)
        debug.println("Skipping revocation check, not end entity cert");
      return;
    }
    if ((paramSet != null) && (paramSet.contains(paramX509Certificate)))
      throw new CertPathValidatorException("circular dependency - cert can't vouch for CRL");
    if (!(paramBoolean1))
    {
      if ((paramBoolean2) && (verifyWithSeparateSigningKey(paramX509Certificate, paramPublicKey, paramBoolean1, paramSet)))
        return;
      throw new CertPathValidatorException("cert can't vouch for CRL");
    }
    this.mPossibleCRLs = new HashSet();
    this.mApprovedCRLs = new HashSet();
    boolean[] arrayOfBoolean = new boolean[9];
    try
    {
      X509CRLSelector localX509CRLSelector = new X509CRLSelector();
      localX509CRLSelector.setCertificateChecking(paramX509Certificate);
      localX509CRLSelector.setDateAndTime(this.mCurrentTime);
      localObject1 = this.mStores.iterator();
      while (((Iterator)localObject1).hasNext())
      {
        localObject2 = (CertStore)((Iterator)localObject1).next();
        this.mPossibleCRLs.addAll(((CertStore)localObject2).getCRLs(localX509CRLSelector));
      }
      localObject1 = DistributionPointFetcher.getInstance();
      this.mApprovedCRLs.addAll(((DistributionPointFetcher)localObject1).getCRLs(localX509CRLSelector, paramPublicKey, this.mSigProvider, this.mStores, arrayOfBoolean));
    }
    catch (Exception localException1)
    {
      if (debug != null)
        debug.println("CrlRevocationChecker.verifyRevocationStatus() unexpected exception: " + localException1.getMessage());
      throw new CertPathValidatorException(localException1);
    }
    if ((this.mPossibleCRLs.isEmpty()) && (this.mApprovedCRLs.isEmpty()))
    {
      if ((paramBoolean2) && (verifyWithSeparateSigningKey(paramX509Certificate, paramPublicKey, paramBoolean1, paramSet)))
        return;
      throw new CertPathValidatorException(str + " check failed: no CRL found");
    }
    if (debug != null)
      debug.println("CrlRevocationChecker.verifyRevocationStatus() crls.size() = " + this.mPossibleCRLs.size());
    this.mApprovedCRLs.addAll(verifyPossibleCRLs(this.mPossibleCRLs, paramX509Certificate, paramPublicKey, arrayOfBoolean));
    if (debug != null)
      debug.println("CrlRevocationChecker.verifyRevocationStatus() approved crls.size() = " + this.mApprovedCRLs.size());
    if ((this.mApprovedCRLs.isEmpty()) || (!(Arrays.equals(arrayOfBoolean, ALL_REASONS))))
    {
      if ((paramBoolean2) && (verifyWithSeparateSigningKey(paramX509Certificate, paramPublicKey, paramBoolean1, paramSet)))
        return;
      throw new CertPathValidatorException("Could not determine revocation status");
    }
    if (debug != null)
    {
      BigInteger localBigInteger = paramX509Certificate.getSerialNumber();
      debug.println("starting the final sweep...");
      debug.println("CrlRevocationChecker.verifyRevocationStatus cert SN: " + localBigInteger.toString());
    }
    int i = 0;
    Object localObject1 = this.mApprovedCRLs.iterator();
    while (((Iterator)localObject1).hasNext())
    {
      localObject2 = (X509CRL)((Iterator)localObject1).next();
      X509CRLEntry localX509CRLEntry = ((X509CRL)localObject2).getRevokedCertificate(paramX509Certificate);
      if (localX509CRLEntry != null)
      {
        if (debug != null)
          debug.println("CrlRevocationChecker.verifyRevocationStatus CRL entry: " + localX509CRLEntry.toString());
        int j = 0;
        try
        {
          X509CRLEntryImpl localX509CRLEntryImpl = X509CRLEntryImpl.toImpl(localX509CRLEntry);
          Integer localInteger = localX509CRLEntryImpl.getReasonCode();
          j = (localInteger == null) ? 0 : localInteger.intValue();
        }
        catch (Exception localException2)
        {
          throw new CertPathValidatorException(localException2);
        }
        i = (j == 6) ? 1 : 0;
        if ((i == 0) && (j != 8))
          throw new CertPathValidatorException("Certificate has been revoked, reason: " + reasonToString(j));
        Set localSet = localX509CRLEntry.getCriticalExtensionOIDs();
        if ((localSet != null) && (!(localSet.isEmpty())))
        {
          localSet.remove(PKIXExtensions.ReasonCode_Id.toString());
          if (!(localSet.isEmpty()))
            throw new CertPathValidatorException("Unrecognized critical extension(s) in revoked CRL entry: " + localSet);
        }
      }
    }
    if (i != 0)
      throw new CertPathValidatorException("Certificate is on hold");
  }

  private boolean verifyWithSeparateSigningKey(X509Certificate paramX509Certificate, PublicKey paramPublicKey, boolean paramBoolean, Set<X509Certificate> paramSet)
  {
    HashSet localHashSet;
    PublicKey localPublicKey;
    String str = "revocation status";
    if (debug != null)
      debug.println("CrlRevocationChecker.verifyWithSeparateSigningKey() ---checking " + str + "...");
    if ((paramSet != null) && (paramSet.contains(paramX509Certificate)))
      return false;
    if (!(paramBoolean))
      paramPublicKey = null;
    try
    {
      localHashSet = new HashSet();
      if (paramPublicKey != null)
        localHashSet.add(paramPublicKey);
      localPublicKey = buildToNewKey(paramX509Certificate, localHashSet, paramSet);
    }
    catch (Exception localException)
    {
      try
      {
        verifyRevocationStatus(paramX509Certificate, localPublicKey, true, false);
        return true;
      }
      catch (CertPathValidatorException localCertPathValidatorException)
      {
        while (true)
          localHashSet.add(localPublicKey);
        localException = localException;
        if (debug != null)
          debug.println("CrlRevocationChecker.verifyWithSeparateSigningKey() got exception " + localException);
      }
    }
    return false;
  }

  private PublicKey buildToNewKey(X509Certificate paramX509Certificate, Set<PublicKey> paramSet, Set<X509Certificate> paramSet1)
    throws CertPathBuilderException
  {
    if (debug != null)
      debug.println("CrlRevocationChecker.buildToNewKey() starting work");
    try
    {
      PKIXBuilderParameters localPKIXBuilderParameters;
      Object localObject2;
      Object localObject6;
      RejectKeySelector localRejectKeySelector = new RejectKeySelector(paramSet);
      localRejectKeySelector.setSubject(paramX509Certificate.getIssuerX500Principal().getName());
      localRejectKeySelector.setKeyUsage(mCrlSignUsage);
      if (this.mParams instanceof PKIXBuilderParameters)
      {
        localPKIXBuilderParameters = (PKIXBuilderParameters)this.mParams.clone();
        localPKIXBuilderParameters.setTargetCertConstraints(localRejectKeySelector);
        localPKIXBuilderParameters.setPolicyQualifiersRejected(true);
      }
      else
      {
        localPKIXBuilderParameters = new PKIXBuilderParameters(this.mParams.getTrustAnchors(), localRejectKeySelector);
        localPKIXBuilderParameters.setInitialPolicies(this.mParams.getInitialPolicies());
        localPKIXBuilderParameters.setCertStores(this.mParams.getCertStores());
        localPKIXBuilderParameters.setExplicitPolicyRequired(this.mParams.isExplicitPolicyRequired());
        localPKIXBuilderParameters.setPolicyMappingInhibited(this.mParams.isPolicyMappingInhibited());
        localPKIXBuilderParameters.setAnyPolicyInhibited(this.mParams.isAnyPolicyInhibited());
        localPKIXBuilderParameters.setDate(this.mParams.getDate());
        localPKIXBuilderParameters.setCertPathCheckers(this.mParams.getCertPathCheckers());
        localPKIXBuilderParameters.setSigProvider(this.mParams.getSigProvider());
      }
      if (this.mInitPubKey != null)
      {
        localObject1 = localPKIXBuilderParameters.getTrustAnchors();
        localObject2 = new HashSet();
        localObject4 = ((Set)localObject1).iterator();
        while (((Iterator)localObject4).hasNext())
        {
          localObject5 = (TrustAnchor)((Iterator)localObject4).next();
          localObject6 = ((TrustAnchor)localObject5).getCAPublicKey();
          if (localObject6 != null)
          {
            if (localObject6.equals(this.mInitPubKey))
              ((Set)localObject2).add(localObject5);
          }
          else
          {
            localObject7 = ((TrustAnchor)localObject5).getTrustedCert();
            localObject6 = ((X509Certificate)localObject7).getPublicKey();
            if (localObject6.equals(this.mInitPubKey))
              ((Set)localObject2).add(localObject5);
          }
        }
        localPKIXBuilderParameters.setTrustAnchors((Set)localObject2);
      }
      localPKIXBuilderParameters.setRevocationEnabled(false);
      if (this.mExtraCerts != null)
      {
        localObject1 = new CollectionCertStoreParameters(this.mExtraCerts);
        localObject2 = CertStore.getInstance("Collection", (CertStoreParameters)localObject1);
        localPKIXBuilderParameters.addCertStore((CertStore)localObject2);
      }
      if (Builder.USE_AIA == true)
      {
        localObject1 = null;
        try
        {
          localObject1 = X509CertImpl.toImpl(paramX509Certificate);
        }
        catch (CertificateException localCertificateException)
        {
          if (debug != null)
            debug.println("CrlRevocationChecker.buildToNewKey: error decoding cert: " + localCertificateException);
        }
        localObject3 = null;
        if (localObject1 != null)
          localObject3 = ((X509CertImpl)localObject1).getAuthorityInfoAccessExtension();
        if (localObject3 != null)
        {
          localObject4 = ((AuthorityInfoAccessExtension)localObject3).getAccessDescriptions();
          if (localObject4 != null)
          {
            localObject5 = ((List)localObject4).iterator();
            while (((Iterator)localObject5).hasNext())
            {
              localObject6 = (AccessDescription)((Iterator)localObject5).next();
              localObject7 = Builder.createCertStore((AccessDescription)localObject6);
              if (localObject7 != null)
              {
                if (debug != null)
                  debug.println("adding AIAext CertStore");
                localPKIXBuilderParameters.addCertStore((CertStore)localObject7);
              }
            }
          }
        }
      }
      Object localObject1 = CertPathBuilder.getInstance("PKIX");
      if (debug != null)
        debug.println("CrlRevocationChecker.buildToNewKey() about to try build ...");
      Object localObject3 = (PKIXCertPathBuilderResult)((CertPathBuilder)localObject1).build(localPKIXBuilderParameters);
      if (debug != null)
        debug.println("CrlRevocationChecker.buildToNewKey() about to check revocation ...");
      if (paramSet1 == null)
        paramSet1 = new HashSet();
      else
        paramSet1 = new HashSet(paramSet1);
      paramSet1.add(paramX509Certificate);
      Object localObject4 = ((PKIXCertPathBuilderResult)localObject3).getTrustAnchor();
      Object localObject5 = ((TrustAnchor)localObject4).getCAPublicKey();
      if (localObject5 == null)
      {
        localObject6 = ((TrustAnchor)localObject4).getTrustedCert();
        localObject5 = ((X509Certificate)localObject6).getPublicKey();
      }
      boolean bool = true;
      Object localObject7 = ((PKIXCertPathBuilderResult)localObject3).getCertPath();
      List localList = ((CertPath)localObject7).getCertificates();
      for (int i = localList.size() - 1; i >= 0; --i)
      {
        X509Certificate localX509Certificate = (X509Certificate)localList.get(i);
        if (debug != null)
          debug.println("CrlRevocationChecker.buildToNewKey() index " + i + " checking " + localX509Certificate);
        verifyRevocationStatus(localX509Certificate, (PublicKey)localObject5, bool, true, paramSet1);
        bool = certCanSignCrl(paramX509Certificate);
        localObject5 = localX509Certificate.getPublicKey();
      }
      if (debug != null)
        debug.println("CrlRevocationChecker.buildToNewKey() got key " + ((PKIXCertPathBuilderResult)localObject3).getPublicKey());
      return ((PKIXCertPathBuilderResult)localObject3).getPublicKey();
    }
    catch (InvalidAlgorithmParameterException localInvalidAlgorithmParameterException)
    {
      throw new CertPathBuilderException(localInvalidAlgorithmParameterException);
    }
    catch (IOException localIOException)
    {
      throw new CertPathBuilderException(localIOException);
    }
    catch (NoSuchAlgorithmException localNoSuchAlgorithmException)
    {
      throw new CertPathBuilderException(localNoSuchAlgorithmException);
    }
    catch (CertPathValidatorException localCertPathValidatorException)
    {
      throw new CertPathBuilderException(localCertPathValidatorException);
    }
  }

  private static String reasonToString(int paramInt)
  {
    switch (paramInt)
    {
    case 0:
      return "unspecified";
    case 1:
      return "key compromise";
    case 2:
      return "CA compromise";
    case 3:
      return "affiliation changed";
    case 4:
      return "superseded";
    case 5:
      return "cessation of operation";
    case 6:
      return "certificate hold";
    case 8:
      return "remove from CRL";
    case 7:
    }
    return "unrecognized reason code";
  }

  private Collection<X509CRL> verifyPossibleCRLs(Set<X509CRL> paramSet, X509Certificate paramX509Certificate, PublicKey paramPublicKey, boolean[] paramArrayOfBoolean)
    throws CertPathValidatorException
  {
    X509CertImpl localX509CertImpl;
    try
    {
      localX509CertImpl = X509CertImpl.toImpl(paramX509Certificate);
      if (debug != null)
        debug.println("CRLRevocationChecker.verifyPossibleCRLs: Checking CRLDPs for " + localX509CertImpl.getSubjectX500Principal());
      CRLDistributionPointsExtension localCRLDistributionPointsExtension = localX509CertImpl.getCRLDistributionPointsExtension();
      List localList = null;
      if (localCRLDistributionPointsExtension == null)
      {
        localObject1 = (X500Name)localX509CertImpl.getIssuerDN();
        localObject2 = new DistributionPoint(new GeneralNames().add(new GeneralName((GeneralNameInterface)localObject1)), null, null);
        localList = Collections.singletonList(localObject2);
      }
      else
      {
        localList = (List)localCRLDistributionPointsExtension.get("points");
      }
      Object localObject1 = new HashSet();
      Object localObject2 = DistributionPointFetcher.getInstance();
      Iterator localIterator1 = localList.iterator();
      while ((localIterator1.hasNext()) && (!(Arrays.equals(paramArrayOfBoolean, ALL_REASONS))))
      {
        DistributionPoint localDistributionPoint = (DistributionPoint)localIterator1.next();
        Iterator localIterator2 = paramSet.iterator();
        while (localIterator2.hasNext())
        {
          X509CRL localX509CRL = (X509CRL)localIterator2.next();
          if (((DistributionPointFetcher)localObject2).verifyCRL(localX509CertImpl, localDistributionPoint, localX509CRL, paramArrayOfBoolean, paramPublicKey, this.mSigProvider))
            ((Set)localObject1).add(localX509CRL);
        }
      }
      return localObject1;
    }
    catch (Exception localException)
    {
      if (debug != null)
        debug.println("Exception while verifying CRL: " + localException.getMessage());
    }
    return ((Collection<X509CRL>)(Collection<X509CRL>)Collections.emptySet());
  }

  static class RejectKeySelector extends X509CertSelector
  {
    private final Set<PublicKey> badKeySet;

    RejectKeySelector(Collection<PublicKey> paramCollection)
    {
      this.badKeySet = new HashSet(paramCollection);
    }

    public boolean match(Certificate paramCertificate)
    {
      if (!(super.match(paramCertificate)))
        return false;
      if (this.badKeySet.contains(paramCertificate.getPublicKey()))
      {
        if (CrlRevocationChecker.access$000() != null)
          CrlRevocationChecker.access$000().println("RejectCertSelector.match: bad key");
        return false;
      }
      if (CrlRevocationChecker.access$000() != null)
        CrlRevocationChecker.access$000().println("RejectCertSelector.match: returning true");
      return true;
    }

    public String toString()
    {
      StringBuilder localStringBuilder = new StringBuilder();
      localStringBuilder.append("RejectCertSelector: [\n");
      localStringBuilder.append(super.toString());
      localStringBuilder.append(this.badKeySet);
      localStringBuilder.append("]");
      return localStringBuilder.toString();
    }
  }
}