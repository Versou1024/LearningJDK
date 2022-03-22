package sun.security.provider.certpath;

import java.io.IOException;
import java.net.URI;
import java.security.GeneralSecurityException;
import java.security.cert.CertStore;
import java.security.cert.CertStoreException;
import java.security.cert.CertificateException;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.security.auth.x500.X500Principal;
import sun.security.util.Debug;
import sun.security.util.ObjectIdentifier;
import sun.security.x509.AccessDescription;
import sun.security.x509.GeneralName;
import sun.security.x509.GeneralNameInterface;
import sun.security.x509.GeneralNames;
import sun.security.x509.GeneralSubtree;
import sun.security.x509.GeneralSubtrees;
import sun.security.x509.NameConstraintsExtension;
import sun.security.x509.SubjectAlternativeNameExtension;
import sun.security.x509.URIName;
import sun.security.x509.X500Name;
import sun.security.x509.X509CertImpl;

public abstract class Builder
{
  private static final Debug debug = Debug.getInstance("certpath");
  final PKIXBuilderParameters buildParams;
  final X500Principal targetSubjectDN;
  static final boolean USE_AIA = DistributionPointFetcher.getBooleanProperty("com.sun.security.enableAIAcaIssuers", false);

  Builder(PKIXBuilderParameters paramPKIXBuilderParameters, X500Principal paramX500Principal)
  {
    this.buildParams = paramPKIXBuilderParameters;
    this.targetSubjectDN = paramX500Principal;
  }

  abstract Collection<X509Certificate> getMatchingCerts(State paramState, List<CertStore> paramList)
    throws CertStoreException, CertificateException, IOException;

  abstract void verifyCert(X509Certificate paramX509Certificate, State paramState, List<X509Certificate> paramList)
    throws GeneralSecurityException;

  abstract boolean isPathCompleted(X509Certificate paramX509Certificate);

  abstract void addCertToPath(X509Certificate paramX509Certificate, LinkedList<X509Certificate> paramLinkedList);

  abstract void removeFinalCertFromPath(LinkedList<X509Certificate> paramLinkedList);

  static int distance(GeneralNameInterface paramGeneralNameInterface1, GeneralNameInterface paramGeneralNameInterface2)
    throws IOException
  {
    switch (paramGeneralNameInterface1.constrains(paramGeneralNameInterface2))
    {
    case -1:
      throw new IOException("Names are different types");
    case 3:
      throw new IOException("Names are same type but in different subtrees");
    case 0:
      return 0;
    case 2:
      break;
    case 1:
      break;
    default:
      throw new IOException("Unknown name relationship");
    }
    return (paramGeneralNameInterface2.subtreeDepth() - paramGeneralNameInterface1.subtreeDepth());
  }

  static int hops(GeneralNameInterface paramGeneralNameInterface1, GeneralNameInterface paramGeneralNameInterface2)
    throws IOException
  {
    int i = paramGeneralNameInterface1.constrains(paramGeneralNameInterface2);
    switch (i)
    {
    case -1:
      throw new IOException("Names are different types");
    case 3:
      break;
    case 0:
      return 0;
    case 2:
      return (paramGeneralNameInterface2.subtreeDepth() - paramGeneralNameInterface1.subtreeDepth());
    case 1:
      return (paramGeneralNameInterface2.subtreeDepth() - paramGeneralNameInterface1.subtreeDepth());
    default:
      throw new IOException("Unknown name relationship");
    }
    if (paramGeneralNameInterface1.getType() != 4)
      throw new IOException("hopDistance not implemented for this name type");
    X500Name localX500Name1 = (X500Name)paramGeneralNameInterface1;
    X500Name localX500Name2 = (X500Name)paramGeneralNameInterface2;
    X500Name localX500Name3 = localX500Name1.commonAncestor(localX500Name2);
    if (localX500Name3 == null)
      throw new IOException("Names are in different namespaces");
    int j = localX500Name3.subtreeDepth();
    int k = localX500Name1.subtreeDepth();
    int l = localX500Name2.subtreeDepth();
    return (k + l - 2 * j);
  }

  static int targetDistance(NameConstraintsExtension paramNameConstraintsExtension, X509Certificate paramX509Certificate, GeneralNameInterface paramGeneralNameInterface)
    throws IOException
  {
    X509CertImpl localX509CertImpl;
    if ((paramNameConstraintsExtension != null) && (!(paramNameConstraintsExtension.verify(paramX509Certificate))))
      throw new IOException("certificate does not satisfy existing name constraints");
    try
    {
      localX509CertImpl = X509CertImpl.toImpl(paramX509Certificate);
    }
    catch (CertificateException localCertificateException)
    {
      throw ((IOException)new IOException("Invalid certificate").initCause(localCertificateException));
    }
    X500Name localX500Name = X500Name.asX500Name(localX509CertImpl.getSubjectX500Principal());
    if (localX500Name.equals(paramGeneralNameInterface))
      return 0;
    SubjectAlternativeNameExtension localSubjectAlternativeNameExtension = localX509CertImpl.getSubjectAlternativeNameExtension();
    if (localSubjectAlternativeNameExtension != null)
    {
      localObject = (GeneralNames)localSubjectAlternativeNameExtension.get("subject_name");
      if (localObject != null)
      {
        int i = 0;
        int j = ((GeneralNames)localObject).size();
        while (i < j)
        {
          GeneralNameInterface localGeneralNameInterface1 = ((GeneralNames)localObject).get(i).getName();
          if (localGeneralNameInterface1.equals(paramGeneralNameInterface))
            return 0;
          ++i;
        }
      }
    }
    Object localObject = localX509CertImpl.getNameConstraintsExtension();
    if (localObject == null)
      return -1;
    if (paramNameConstraintsExtension != null)
      paramNameConstraintsExtension.merge((NameConstraintsExtension)localObject);
    else
      paramNameConstraintsExtension = (NameConstraintsExtension)((NameConstraintsExtension)localObject).clone();
    if (debug != null)
      debug.println("Builder.targetDistance() merged constraints: " + String.valueOf(paramNameConstraintsExtension));
    GeneralSubtrees localGeneralSubtrees1 = (GeneralSubtrees)paramNameConstraintsExtension.get("permitted_subtrees");
    GeneralSubtrees localGeneralSubtrees2 = (GeneralSubtrees)paramNameConstraintsExtension.get("excluded_subtrees");
    if (localGeneralSubtrees1 != null)
      localGeneralSubtrees1.reduce(localGeneralSubtrees2);
    if (debug != null)
      debug.println("Builder.targetDistance() reduced constraints: " + localGeneralSubtrees1);
    if (!(paramNameConstraintsExtension.verify(paramGeneralNameInterface)))
      throw new IOException("New certificate not allowed to sign certificate for target");
    if (localGeneralSubtrees1 == null)
      return -1;
    int k = 0;
    int l = localGeneralSubtrees1.size();
    while (k < l)
    {
      GeneralNameInterface localGeneralNameInterface2 = localGeneralSubtrees1.get(k).getName().getName();
      try
      {
        int i1 = distance(localGeneralNameInterface2, paramGeneralNameInterface);
        if (i1 >= 0)
          return (i1 + 1);
      }
      catch (IOException localIOException)
      {
      }
      ++k;
    }
    return -1;
  }

  Set<String> getMatchingPolicies()
  {
    Set localSet = this.buildParams.getInitialPolicies();
    if ((!(localSet.isEmpty())) && (!(localSet.contains("2.5.29.32.0"))) && (this.buildParams.isPolicyMappingInhibited()))
    {
      localSet.add("2.5.29.32.0");
      return localSet;
    }
    return new HashSet();
  }

  void addMatchingCerts(X509CertSelector paramX509CertSelector, Collection<CertStore> paramCollection, Collection<X509Certificate> paramCollection1, boolean paramBoolean)
  {
    X509Certificate localX509Certificate1 = paramX509CertSelector.getCertificate();
    if (localX509Certificate1 != null)
    {
      if ((paramX509CertSelector.match(localX509Certificate1)) && (!(X509CertImpl.isSelfSigned(localX509Certificate1, this.buildParams.getSigProvider()))))
        paramCollection1.add(localX509Certificate1);
      if (debug != null)
        debug.println("Builder.addMatchingCerts: adding target cert");
      return;
    }
    Iterator localIterator1 = paramCollection.iterator();
    while (localIterator1.hasNext())
    {
      CertStore localCertStore = (CertStore)localIterator1.next();
      try
      {
        Collection localCollection = localCertStore.getCertificates(paramX509CertSelector);
        Iterator localIterator2 = localCollection.iterator();
        while (localIterator2.hasNext())
        {
          X509Certificate localX509Certificate2 = (X509Certificate)localIterator2.next();
          if (!(X509CertImpl.isSelfSigned(localX509Certificate2, this.buildParams.getSigProvider())))
            paramCollection1.add(localX509Certificate2);
        }
        if ((!(paramBoolean)) && (!(paramCollection1.isEmpty())))
          return;
      }
      catch (CertStoreException localCertStoreException)
      {
        if (debug != null)
        {
          debug.println("Builder.addMatchingCerts, non-fatal exception retrieving certs: " + localCertStoreException);
          localCertStoreException.printStackTrace();
        }
      }
    }
  }

  static CertStore createCertStore(AccessDescription paramAccessDescription)
  {
    if (!(paramAccessDescription.getAccessMethod().equals(AccessDescription.Ad_CAISSUERS_Id)))
      return null;
    GeneralNameInterface localGeneralNameInterface = paramAccessDescription.getAccessLocation().getName();
    if (!(localGeneralNameInterface instanceof URIName))
      return null;
    URI localURI = ((URIName)localGeneralNameInterface).getURI();
    if (debug != null)
      debug.println("AIA URI:" + localURI);
    if (!(localURI.getScheme().equals("ldap")))
      return null;
    try
    {
      return LDAPCertStore.getInstance(LDAPCertStore.getParameters(localURI));
    }
    catch (Exception localException)
    {
      if (debug != null)
        debug.println("Builder.createCertStore(AccessDescription): non-fatal exception creating CertStore: " + localException);
    }
    return null;
  }

  static boolean isLocalCertStore(CertStore paramCertStore)
  {
    return ((paramCertStore.getType().equals("Collection")) || (paramCertStore.getCertStoreParameters() instanceof CollectionCertStoreParameters));
  }
}