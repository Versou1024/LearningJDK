package sun.security.provider.certpath;

import java.io.IOException;
import java.security.PublicKey;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertificateException;
import java.security.cert.PKIXCertPathChecker;
import java.security.cert.X509Certificate;
import java.security.interfaces.DSAPublicKey;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import javax.security.auth.x500.X500Principal;
import sun.security.util.Debug;
import sun.security.x509.GeneralName;
import sun.security.x509.GeneralNameInterface;
import sun.security.x509.GeneralNames;
import sun.security.x509.SubjectAlternativeNameExtension;
import sun.security.x509.X500Name;
import sun.security.x509.X509CertImpl;

class ForwardState
  implements State
{
  private static final Debug debug = Debug.getInstance("certpath");
  X500Principal issuerDN;
  X509CertImpl cert;
  HashSet<GeneralNameInterface> subjectNamesTraversed;
  int traversedCACerts;
  private boolean init = true;
  public CrlRevocationChecker crlChecker;
  ArrayList<PKIXCertPathChecker> forwardCheckers;
  boolean keyParamsNeededFlag = false;

  public boolean isInitial()
  {
    return this.init;
  }

  public boolean keyParamsNeeded()
  {
    return this.keyParamsNeededFlag;
  }

  public String toString()
  {
    StringBuffer localStringBuffer = new StringBuffer();
    try
    {
      localStringBuffer.append("State [");
      localStringBuffer.append("\n  issuerDN of last cert: " + this.issuerDN);
      localStringBuffer.append("\n  traversedCACerts: " + this.traversedCACerts);
      localStringBuffer.append("\n  init: " + String.valueOf(this.init));
      localStringBuffer.append("\n  keyParamsNeeded: " + String.valueOf(this.keyParamsNeededFlag));
      localStringBuffer.append("\n  subjectNamesTraversed: \n" + this.subjectNamesTraversed);
      localStringBuffer.append("]\n");
    }
    catch (Exception localException)
    {
      if (debug != null)
      {
        debug.println("ForwardState.toString() unexpected exception");
        localException.printStackTrace();
      }
    }
    return localStringBuffer.toString();
  }

  public void initState(List<PKIXCertPathChecker> paramList)
    throws CertPathValidatorException
  {
    this.subjectNamesTraversed = new HashSet();
    this.traversedCACerts = 0;
    this.forwardCheckers = new ArrayList();
    if (paramList != null)
    {
      Iterator localIterator = paramList.iterator();
      while (localIterator.hasNext())
      {
        PKIXCertPathChecker localPKIXCertPathChecker = (PKIXCertPathChecker)localIterator.next();
        if (localPKIXCertPathChecker.isForwardCheckingSupported())
        {
          localPKIXCertPathChecker.init(true);
          this.forwardCheckers.add(localPKIXCertPathChecker);
        }
      }
    }
    this.init = true;
  }

  public void updateState(X509Certificate paramX509Certificate)
    throws CertificateException, IOException, CertPathValidatorException
  {
    if (paramX509Certificate == null)
      return;
    X509CertImpl localX509CertImpl = X509CertImpl.toImpl(paramX509Certificate);
    PublicKey localPublicKey = localX509CertImpl.getPublicKey();
    if ((localPublicKey instanceof DSAPublicKey) && (((DSAPublicKey)localPublicKey).getParams() == null))
      this.keyParamsNeededFlag = true;
    this.cert = localX509CertImpl;
    this.issuerDN = paramX509Certificate.getIssuerX500Principal();
    if ((!(X509CertImpl.isSelfIssued(paramX509Certificate))) && (!(this.init)) && (paramX509Certificate.getBasicConstraints() != -1))
      this.traversedCACerts += 1;
    if ((this.init) || (!(X509CertImpl.isSelfIssued(paramX509Certificate))))
    {
      X500Principal localX500Principal = paramX509Certificate.getSubjectX500Principal();
      this.subjectNamesTraversed.add(X500Name.asX500Name(localX500Principal));
      try
      {
        SubjectAlternativeNameExtension localSubjectAlternativeNameExtension = localX509CertImpl.getSubjectAlternativeNameExtension();
        if (localSubjectAlternativeNameExtension != null)
        {
          GeneralNames localGeneralNames = (GeneralNames)localSubjectAlternativeNameExtension.get("subject_name");
          Iterator localIterator = localGeneralNames.iterator();
          while (localIterator.hasNext())
          {
            GeneralNameInterface localGeneralNameInterface = ((GeneralName)localIterator.next()).getName();
            this.subjectNamesTraversed.add(localGeneralNameInterface);
          }
        }
      }
      catch (Exception localException)
      {
        if (debug != null)
        {
          debug.println("ForwardState.updateState() unexpected exception");
          localException.printStackTrace();
        }
        throw new CertPathValidatorException(localException);
      }
    }
    this.init = false;
  }

  public Object clone()
  {
    ForwardState localForwardState;
    try
    {
      localForwardState = (ForwardState)super.clone();
      localForwardState.forwardCheckers = ((ArrayList)this.forwardCheckers.clone());
      ListIterator localListIterator = localForwardState.forwardCheckers.listIterator();
      while (localListIterator.hasNext())
      {
        PKIXCertPathChecker localPKIXCertPathChecker = (PKIXCertPathChecker)localListIterator.next();
        if (localPKIXCertPathChecker instanceof Cloneable)
          localListIterator.set(localPKIXCertPathChecker.clone());
      }
      localForwardState.subjectNamesTraversed = ((HashSet)this.subjectNamesTraversed.clone());
      return localForwardState;
    }
    catch (CloneNotSupportedException localCloneNotSupportedException)
    {
      throw new InternalError(localCloneNotSupportedException.toString());
    }
  }
}