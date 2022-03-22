package sun.security.provider.certpath;

import java.security.cert.CertPath;
import java.security.cert.CertPathValidatorException;
import java.security.cert.PKIXCertPathChecker;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import sun.security.util.Debug;

class PKIXMasterCertPathValidator
{
  private static final Debug debug = Debug.getInstance("certpath");
  private List certPathCheckers;

  PKIXMasterCertPathValidator(List paramList)
  {
    this.certPathCheckers = paramList;
  }

  void validate(CertPath paramCertPath, List paramList)
    throws CertPathValidatorException
  {
    X509Certificate localX509Certificate = null;
    PKIXCertPathChecker localPKIXCertPathChecker = null;
    int i = paramList.size();
    if (debug != null)
    {
      debug.println("--------------------------------------------------------------");
      debug.println("Executing PKIX certification path validation algorithm.");
    }
    for (int j = 0; j < i; ++j)
    {
      if (debug != null)
        debug.println("Checking cert" + (j + 1) + " ...");
      localX509Certificate = (X509Certificate)paramList.get(j);
      Set localSet = localX509Certificate.getCriticalExtensionOIDs();
      if (localSet == null)
        localSet = Collections.EMPTY_SET;
      if ((debug != null) && (!(localSet.isEmpty())))
      {
        debug.println("Set of critical extensions:");
        localObject = localSet.iterator();
        while (((Iterator)localObject).hasNext())
          debug.println((String)((Iterator)localObject).next());
      }
      Object localObject = null;
      for (int k = 0; k < this.certPathCheckers.size(); ++k)
      {
        localPKIXCertPathChecker = (PKIXCertPathChecker)this.certPathCheckers.get(k);
        if (debug != null)
          debug.println("-Using checker" + (k + 1) + " ... [" + localPKIXCertPathChecker.getClass().getName() + "]");
        if (j == 0)
          localPKIXCertPathChecker.init(false);
        try
        {
          localPKIXCertPathChecker.check(localX509Certificate, localSet);
          if (isRevocationCheck(localPKIXCertPathChecker, k, this.certPathCheckers))
          {
            if (debug != null)
              debug.println("-checker" + (k + 1) + " validation succeeded");
            ++k;
            break label489:
          }
        }
        catch (CertPathValidatorException localCertPathValidatorException1)
        {
          if ((localObject != null) && (localPKIXCertPathChecker instanceof CrlRevocationChecker))
            throw ((Throwable)localObject);
          CertPathValidatorException localCertPathValidatorException2 = new CertPathValidatorException(localCertPathValidatorException1.getMessage(), localCertPathValidatorException1.getCause(), paramCertPath, i - j + 1);
          if (localCertPathValidatorException1 instanceof CertificateRevokedException)
            throw localCertPathValidatorException2;
          if (!(isRevocationCheck(localPKIXCertPathChecker, k, this.certPathCheckers)))
            throw localCertPathValidatorException2;
          localObject = localCertPathValidatorException2;
          if (debug != null)
          {
            debug.println(localCertPathValidatorException1.getMessage());
            debug.println("preparing to failover (from OCSP to CRLs)");
          }
        }
        label489: if (debug != null)
          debug.println("-checker" + (k + 1) + " validation succeeded");
      }
      if (debug != null)
        debug.println("checking for unresolvedCritExts");
      if (!(localSet.isEmpty()))
        throw new CertPathValidatorException("unrecognized critical extension(s)", null, paramCertPath, i - j + 1);
      if (debug != null)
        debug.println("\ncert" + (j + 1) + " validation succeeded.\n");
    }
    if (debug != null)
    {
      debug.println("Cert path validation succeeded. (PKIX validation algorithm)");
      debug.println("--------------------------------------------------------------");
    }
  }

  private boolean isRevocationCheck(PKIXCertPathChecker paramPKIXCertPathChecker, int paramInt, List paramList)
  {
    if ((!(paramPKIXCertPathChecker instanceof OCSPChecker)) || (paramInt + 1 >= paramList.size()))
      break label40;
    Object localObject = paramList.get(paramInt + 1);
    label40: return (localObject instanceof CrlRevocationChecker);
  }
}