package sun.security.validator;

import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.cert.CertPath;
import java.security.cert.CertPathBuilder;
import java.security.cert.CertPathValidator;
import java.security.cert.CertStore;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.PKIXCertPathBuilderResult;
import java.security.cert.PKIXCertPathValidatorResult;
import java.security.cert.TrustAnchor;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.security.auth.x500.X500Principal;

public final class PKIXValidator extends Validator
{
  private static final boolean TRY_VALIDATOR = 1;
  private final Set trustedCerts;
  private final PKIXBuilderParameters parameterTemplate;
  private int certPathLength = -1;
  private Map trustedSubjects;
  private CertificateFactory factory;
  private boolean plugin = false;

  PKIXValidator(String paramString, Collection paramCollection)
  {
    super("PKIX", paramString);
    if (paramCollection instanceof Set)
      this.trustedCerts = ((Set)paramCollection);
    else
      this.trustedCerts = new HashSet(paramCollection);
    HashSet localHashSet = new HashSet();
    Iterator localIterator = paramCollection.iterator();
    while (localIterator.hasNext())
    {
      X509Certificate localX509Certificate = (X509Certificate)localIterator.next();
      localHashSet.add(new TrustAnchor(localX509Certificate, null));
    }
    try
    {
      this.parameterTemplate = new PKIXBuilderParameters(localHashSet, null);
    }
    catch (InvalidAlgorithmParameterException localInvalidAlgorithmParameterException)
    {
      throw new RuntimeException("Unexpected error: " + localInvalidAlgorithmParameterException.toString(), localInvalidAlgorithmParameterException);
    }
    setDefaultParameters(paramString);
    initCommon();
  }

  PKIXValidator(String paramString, PKIXBuilderParameters paramPKIXBuilderParameters)
  {
    super("PKIX", paramString);
    this.trustedCerts = new HashSet();
    Iterator localIterator = paramPKIXBuilderParameters.getTrustAnchors().iterator();
    while (localIterator.hasNext())
    {
      TrustAnchor localTrustAnchor = (TrustAnchor)localIterator.next();
      X509Certificate localX509Certificate = localTrustAnchor.getTrustedCert();
      if (localX509Certificate != null)
        this.trustedCerts.add(localX509Certificate);
    }
    this.parameterTemplate = paramPKIXBuilderParameters;
    initCommon();
  }

  private void initCommon()
  {
    this.trustedSubjects = new HashMap();
    Iterator localIterator = this.trustedCerts.iterator();
    while (localIterator.hasNext())
    {
      X509Certificate localX509Certificate = (X509Certificate)localIterator.next();
      this.trustedSubjects.put(localX509Certificate.getSubjectX500Principal(), localX509Certificate);
    }
    try
    {
      this.factory = CertificateFactory.getInstance("X.509");
    }
    catch (CertificateException localCertificateException)
    {
      throw new RuntimeException("Internal error", localCertificateException);
    }
    this.plugin = this.variant.equals("plugin code signing");
  }

  public Collection getTrustedCertificates()
  {
    return this.trustedCerts;
  }

  public int getCertPathLength()
  {
    return this.certPathLength;
  }

  private void setDefaultParameters(String paramString)
  {
    this.parameterTemplate.setRevocationEnabled(false);
  }

  public PKIXBuilderParameters getParameters()
  {
    return this.parameterTemplate;
  }

  X509Certificate[] engineValidate(X509Certificate[] paramArrayOfX509Certificate, Collection paramCollection, Object paramObject)
    throws CertificateException
  {
    if ((paramArrayOfX509Certificate == null) || (paramArrayOfX509Certificate.length == 0))
      throw new CertificateException("null or zero-length certificate chain");
    for (int i = 0; i < paramArrayOfX509Certificate.length; ++i)
      if (this.trustedCerts.contains(paramArrayOfX509Certificate[i]))
      {
        if (i == 0)
          return { paramArrayOfX509Certificate[0] };
        localObject = new X509Certificate[i];
        System.arraycopy(paramArrayOfX509Certificate, 0, localObject, 0, i);
        return doValidate(localObject);
      }
    X509Certificate localX509Certificate = paramArrayOfX509Certificate[(paramArrayOfX509Certificate.length - 1)];
    Object localObject = localX509Certificate.getIssuerX500Principal();
    X500Principal localX500Principal = localX509Certificate.getSubjectX500Principal();
    if ((this.trustedSubjects.containsKey(localObject)) && (!(((X500Principal)localObject).equals(localX500Principal))) && (isSignatureValid((X509Certificate)this.trustedSubjects.get(localObject), localX509Certificate)))
      return doValidate(paramArrayOfX509Certificate);
    if (this.plugin)
    {
      if (paramArrayOfX509Certificate.length > 1)
      {
        X509Certificate[] arrayOfX509Certificate = new X509Certificate[paramArrayOfX509Certificate.length - 1];
        System.arraycopy(paramArrayOfX509Certificate, 0, arrayOfX509Certificate, 0, arrayOfX509Certificate.length);
        PKIXBuilderParameters localPKIXBuilderParameters = (PKIXBuilderParameters)this.parameterTemplate.clone();
        try
        {
          localPKIXBuilderParameters.setTrustAnchors(Collections.singleton(new TrustAnchor(paramArrayOfX509Certificate[(paramArrayOfX509Certificate.length - 1)], null)));
        }
        catch (InvalidAlgorithmParameterException localInvalidAlgorithmParameterException)
        {
          throw new CertificateException(localInvalidAlgorithmParameterException);
        }
        doValidate(arrayOfX509Certificate, localPKIXBuilderParameters);
      }
      throw new ValidatorException(ValidatorException.T_NO_TRUST_ANCHOR);
    }
    return ((X509Certificate)doBuild(paramArrayOfX509Certificate, paramCollection));
  }

  private boolean isSignatureValid(X509Certificate paramX509Certificate1, X509Certificate paramX509Certificate2)
  {
    if (this.plugin)
    {
      try
      {
        paramX509Certificate2.verify(paramX509Certificate1.getPublicKey());
      }
      catch (Exception localException)
      {
        return false;
      }
      return true;
    }
    return true;
  }

  private static X509Certificate[] toArray(CertPath paramCertPath, TrustAnchor paramTrustAnchor)
    throws CertificateException
  {
    List localList = paramCertPath.getCertificates();
    X509Certificate[] arrayOfX509Certificate = new X509Certificate[localList.size() + 1];
    localList.toArray(arrayOfX509Certificate);
    X509Certificate localX509Certificate = paramTrustAnchor.getTrustedCert();
    if (localX509Certificate == null)
      throw new ValidatorException("TrustAnchor must be specified as certificate");
    arrayOfX509Certificate[(arrayOfX509Certificate.length - 1)] = localX509Certificate;
    return arrayOfX509Certificate;
  }

  private void setDate(PKIXBuilderParameters paramPKIXBuilderParameters)
  {
    Date localDate = this.validationDate;
    if (localDate != null)
      paramPKIXBuilderParameters.setDate(localDate);
  }

  private X509Certificate[] doValidate(X509Certificate[] paramArrayOfX509Certificate)
    throws CertificateException
  {
    PKIXBuilderParameters localPKIXBuilderParameters = (PKIXBuilderParameters)this.parameterTemplate.clone();
    return doValidate(paramArrayOfX509Certificate, localPKIXBuilderParameters);
  }

  private X509Certificate[] doValidate(X509Certificate[] paramArrayOfX509Certificate, PKIXBuilderParameters paramPKIXBuilderParameters)
    throws CertificateException
  {
    try
    {
      setDate(paramPKIXBuilderParameters);
      CertPathValidator localCertPathValidator = CertPathValidator.getInstance("PKIX");
      CertPath localCertPath = this.factory.generateCertPath(Arrays.asList(paramArrayOfX509Certificate));
      this.certPathLength = paramArrayOfX509Certificate.length;
      PKIXCertPathValidatorResult localPKIXCertPathValidatorResult = (PKIXCertPathValidatorResult)localCertPathValidator.validate(localCertPath, paramPKIXBuilderParameters);
      return toArray(localCertPath, localPKIXCertPathValidatorResult.getTrustAnchor());
    }
    catch (GeneralSecurityException localGeneralSecurityException)
    {
      throw new ValidatorException("PKIX path validation failed: " + localGeneralSecurityException.toString(), localGeneralSecurityException);
    }
  }

  private X509Certificate[] doBuild(X509Certificate[] paramArrayOfX509Certificate, Collection paramCollection)
    throws CertificateException
  {
    PKIXBuilderParameters localPKIXBuilderParameters;
    try
    {
      localPKIXBuilderParameters = (PKIXBuilderParameters)this.parameterTemplate.clone();
      setDate(localPKIXBuilderParameters);
      X509CertSelector localX509CertSelector = new X509CertSelector();
      localX509CertSelector.setCertificate(paramArrayOfX509Certificate[0]);
      localPKIXBuilderParameters.setTargetCertConstraints(localX509CertSelector);
      ArrayList localArrayList = new ArrayList();
      localArrayList.addAll(Arrays.asList(paramArrayOfX509Certificate));
      if (paramCollection != null)
        localArrayList.addAll(paramCollection);
      CertStore localCertStore = CertStore.getInstance("Collection", new CollectionCertStoreParameters(localArrayList));
      localPKIXBuilderParameters.addCertStore(localCertStore);
      CertPathBuilder localCertPathBuilder = CertPathBuilder.getInstance("PKIX");
      PKIXCertPathBuilderResult localPKIXCertPathBuilderResult = (PKIXCertPathBuilderResult)localCertPathBuilder.build(localPKIXBuilderParameters);
      return toArray(localPKIXCertPathBuilderResult.getCertPath(), localPKIXCertPathBuilderResult.getTrustAnchor());
    }
    catch (GeneralSecurityException localGeneralSecurityException)
    {
      throw new ValidatorException("PKIX path building failed: " + localGeneralSecurityException.toString(), localGeneralSecurityException);
    }
  }
}