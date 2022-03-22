package sun.security.provider.certpath;

import java.io.IOException;
import java.security.PublicKey;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertificateException;
import java.security.cert.PKIXCertPathChecker;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.security.interfaces.DSAPublicKey;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import javax.security.auth.x500.X500Principal;
import sun.security.util.Debug;
import sun.security.x509.NameConstraintsExtension;
import sun.security.x509.SubjectKeyIdentifierExtension;
import sun.security.x509.X509CertImpl;

class ReverseState
  implements State
{
  private static final Debug debug = Debug.getInstance("certpath");
  X500Principal subjectDN;
  PublicKey pubKey;
  SubjectKeyIdentifierExtension subjKeyId;
  NameConstraintsExtension nc;
  int explicitPolicy;
  int policyMapping;
  int inhibitAnyPolicy;
  int certIndex;
  PolicyNodeImpl rootNode;
  int remainingCACerts;
  ArrayList<PKIXCertPathChecker> userCheckers;
  private boolean init = true;
  public CrlRevocationChecker crlChecker;
  TrustAnchor trustAnchor;
  public boolean crlSign = true;

  public boolean isInitial()
  {
    return this.init;
  }

  public String toString()
  {
    StringBuffer localStringBuffer = new StringBuffer();
    try
    {
      localStringBuffer.append("State [");
      localStringBuffer.append("\n  subjectDN of last cert: " + this.subjectDN);
      localStringBuffer.append("\n  subjectKeyIdentifier: " + String.valueOf(this.subjKeyId));
      localStringBuffer.append("\n  nameConstraints: " + String.valueOf(this.nc));
      localStringBuffer.append("\n  certIndex: " + this.certIndex);
      localStringBuffer.append("\n  explicitPolicy: " + this.explicitPolicy);
      localStringBuffer.append("\n  policyMapping:  " + this.policyMapping);
      localStringBuffer.append("\n  inhibitAnyPolicy:  " + this.inhibitAnyPolicy);
      localStringBuffer.append("\n  rootNode: " + this.rootNode);
      localStringBuffer.append("\n  remainingCACerts: " + this.remainingCACerts);
      localStringBuffer.append("\n  crlSign: " + this.crlSign);
      localStringBuffer.append("\n  init: " + this.init);
      localStringBuffer.append("\n]\n");
    }
    catch (Exception localException)
    {
      if (debug != null)
      {
        debug.println("ReverseState.toString() unexpected exception");
        localException.printStackTrace();
      }
    }
    return localStringBuffer.toString();
  }

  public void initState(int paramInt, boolean paramBoolean1, boolean paramBoolean2, boolean paramBoolean3, List<PKIXCertPathChecker> paramList)
    throws CertPathValidatorException
  {
    this.remainingCACerts = ((paramInt == -1) ? 2147483647 : paramInt);
    if (paramBoolean1)
      this.explicitPolicy = 0;
    else
      this.explicitPolicy = ((paramInt == -1) ? paramInt : paramInt + 2);
    if (paramBoolean2)
      this.policyMapping = 0;
    else
      this.policyMapping = ((paramInt == -1) ? paramInt : paramInt + 2);
    if (paramBoolean3)
      this.inhibitAnyPolicy = 0;
    else
      this.inhibitAnyPolicy = ((paramInt == -1) ? paramInt : paramInt + 2);
    this.certIndex = 1;
    HashSet localHashSet = new HashSet(1);
    localHashSet.add("2.5.29.32.0");
    this.rootNode = new PolicyNodeImpl(null, "2.5.29.32.0", null, false, localHashSet, false);
    if (paramList != null)
    {
      this.userCheckers = new ArrayList(paramList);
      Iterator localIterator = paramList.iterator();
      while (localIterator.hasNext())
      {
        PKIXCertPathChecker localPKIXCertPathChecker = (PKIXCertPathChecker)localIterator.next();
        localPKIXCertPathChecker.init(false);
      }
    }
    else
    {
      this.userCheckers = new ArrayList();
    }
    this.crlSign = true;
    this.init = true;
  }

  public void updateState(TrustAnchor paramTrustAnchor)
    throws CertificateException, IOException, CertPathValidatorException
  {
    this.trustAnchor = paramTrustAnchor;
    X509Certificate localX509Certificate = paramTrustAnchor.getTrustedCert();
    if (localX509Certificate != null)
    {
      updateState(localX509Certificate);
    }
    else
    {
      X500Principal localX500Principal = CertPathHelper.getCA(paramTrustAnchor);
      updateState(paramTrustAnchor.getCAPublicKey(), localX500Principal);
    }
    this.init = false;
  }

  private void updateState(PublicKey paramPublicKey, X500Principal paramX500Principal)
  {
    this.subjectDN = paramX500Principal;
    this.pubKey = paramPublicKey;
  }

  public void updateState(X509Certificate paramX509Certificate)
    throws CertificateException, IOException, CertPathValidatorException
  {
    if (paramX509Certificate == null)
      return;
    this.subjectDN = paramX509Certificate.getSubjectX500Principal();
    X509CertImpl localX509CertImpl = X509CertImpl.toImpl(paramX509Certificate);
    PublicKey localPublicKey = paramX509Certificate.getPublicKey();
    if ((localPublicKey instanceof DSAPublicKey) && (((DSAPublicKey)localPublicKey).getParams() == null))
      localPublicKey = BasicChecker.makeInheritedParamsKey(localPublicKey, this.pubKey);
    this.pubKey = localPublicKey;
    if (this.init)
    {
      this.init = false;
      return;
    }
    this.subjKeyId = localX509CertImpl.getSubjectKeyIdentifierExtension();
    this.crlSign = this.crlChecker.certCanSignCrl(paramX509Certificate);
    if (this.nc != null)
    {
      this.nc.merge(localX509CertImpl.getNameConstraintsExtension());
    }
    else
    {
      this.nc = localX509CertImpl.getNameConstraintsExtension();
      if (this.nc != null)
        this.nc = ((NameConstraintsExtension)this.nc.clone());
    }
    this.explicitPolicy = PolicyChecker.mergeExplicitPolicy(this.explicitPolicy, localX509CertImpl, false);
    this.policyMapping = PolicyChecker.mergePolicyMapping(this.policyMapping, localX509CertImpl);
    this.inhibitAnyPolicy = PolicyChecker.mergeInhibitAnyPolicy(this.inhibitAnyPolicy, localX509CertImpl);
    this.certIndex += 1;
    this.remainingCACerts = ConstraintsChecker.mergeBasicConstraints(paramX509Certificate, this.remainingCACerts);
    this.init = false;
  }

  public boolean keyParamsNeeded()
  {
    return false;
  }

  public Object clone()
  {
    ReverseState localReverseState;
    try
    {
      localReverseState = (ReverseState)super.clone();
      localReverseState.userCheckers = ((ArrayList)this.userCheckers.clone());
      ListIterator localListIterator = localReverseState.userCheckers.listIterator();
      while (localListIterator.hasNext())
      {
        PKIXCertPathChecker localPKIXCertPathChecker = (PKIXCertPathChecker)localListIterator.next();
        if (localPKIXCertPathChecker instanceof Cloneable)
          localListIterator.set(localPKIXCertPathChecker.clone());
      }
      if (this.nc != null)
        localReverseState.nc = ((NameConstraintsExtension)this.nc.clone());
      if (this.rootNode != null)
        localReverseState.rootNode = this.rootNode.copyTree();
      return localReverseState;
    }
    catch (CloneNotSupportedException localCloneNotSupportedException)
    {
      throw new InternalError(localCloneNotSupportedException.toString());
    }
  }
}