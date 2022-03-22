package sun.security.provider.certpath;

import java.security.InvalidAlgorithmParameterException;
import java.security.cert.CRL;
import java.security.cert.CRLSelector;
import java.security.cert.CertSelector;
import java.security.cert.CertStoreException;
import java.security.cert.CertStoreParameters;
import java.security.cert.CertStoreSpi;
import java.security.cert.Certificate;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.X509CRL;
import java.security.cert.X509CRLSelector;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collection<+Ljava.security.cert.Certificate;>;
import java.util.Collection<Ljava.security.cert.CRL;>;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.security.auth.x500.X500Principal;

public class IndexedCollectionCertStore extends CertStoreSpi
{
  private Map certSubjects;
  private Map crlIssuers;
  private Set otherCertificates;
  private Set otherCRLs;

  public IndexedCollectionCertStore(CertStoreParameters paramCertStoreParameters)
    throws InvalidAlgorithmParameterException
  {
    super(paramCertStoreParameters);
    if (!(paramCertStoreParameters instanceof CollectionCertStoreParameters))
      throw new InvalidAlgorithmParameterException("parameters must be CollectionCertStoreParameters");
    Collection localCollection = ((CollectionCertStoreParameters)paramCertStoreParameters).getCollection();
    if (localCollection == null)
      throw new InvalidAlgorithmParameterException("Collection must not be null");
    buildIndex(localCollection);
  }

  private void buildIndex(Collection paramCollection)
  {
    this.certSubjects = new HashMap();
    this.crlIssuers = new HashMap();
    this.otherCertificates = null;
    this.otherCRLs = null;
    Iterator localIterator = paramCollection.iterator();
    while (localIterator.hasNext())
    {
      Object localObject = localIterator.next();
      if (localObject instanceof X509Certificate)
      {
        indexCertificate((X509Certificate)localObject);
      }
      else if (localObject instanceof X509CRL)
      {
        indexCRL((X509CRL)localObject);
      }
      else if (localObject instanceof Certificate)
      {
        if (this.otherCertificates == null)
          this.otherCertificates = new HashSet();
        this.otherCertificates.add(localObject);
      }
      else if (localObject instanceof CRL)
      {
        if (this.otherCRLs == null)
          this.otherCRLs = new HashSet();
        this.otherCRLs.add(localObject);
      }
    }
    if (this.otherCertificates == null)
      this.otherCertificates = Collections.EMPTY_SET;
    if (this.otherCRLs == null)
      this.otherCRLs = Collections.EMPTY_SET;
  }

  private void indexCertificate(X509Certificate paramX509Certificate)
  {
    X500Principal localX500Principal = paramX509Certificate.getSubjectX500Principal();
    Object localObject1 = this.certSubjects.put(localX500Principal, paramX509Certificate);
    if (localObject1 != null)
    {
      Object localObject2;
      if (localObject1 instanceof X509Certificate)
      {
        if (paramX509Certificate.equals(localObject1))
          return;
        localObject2 = new ArrayList(2);
        ((List)localObject2).add(paramX509Certificate);
        ((List)localObject2).add(localObject1);
        this.certSubjects.put(localX500Principal, localObject2);
      }
      else
      {
        localObject2 = (List)localObject1;
        if (!(((List)localObject2).contains(paramX509Certificate)))
          ((List)localObject2).add(paramX509Certificate);
        this.certSubjects.put(localX500Principal, localObject2);
      }
    }
  }

  private void indexCRL(X509CRL paramX509CRL)
  {
    X500Principal localX500Principal = paramX509CRL.getIssuerX500Principal();
    Object localObject1 = this.crlIssuers.put(localX500Principal, paramX509CRL);
    if (localObject1 != null)
    {
      Object localObject2;
      if (localObject1 instanceof X509CRL)
      {
        if (paramX509CRL.equals(localObject1))
          return;
        localObject2 = new ArrayList(2);
        ((List)localObject2).add(paramX509CRL);
        ((List)localObject2).add(localObject1);
        this.crlIssuers.put(localX500Principal, localObject2);
      }
      else
      {
        localObject2 = (List)localObject1;
        if (!(((List)localObject2).contains(paramX509CRL)))
          ((List)localObject2).add(paramX509CRL);
        this.crlIssuers.put(localX500Principal, localObject2);
      }
    }
  }

  public Collection<? extends Certificate> engineGetCertificates(CertSelector paramCertSelector)
    throws CertStoreException
  {
    Object localObject2;
    if (paramCertSelector == null)
    {
      localObject1 = new HashSet();
      matchX509Certs(new X509CertSelector(), (Collection)localObject1);
      ((Set)localObject1).addAll(this.otherCertificates);
      return localObject1;
    }
    if (!(paramCertSelector instanceof X509CertSelector))
    {
      localObject1 = new HashSet();
      matchX509Certs(paramCertSelector, (Collection)localObject1);
      localObject2 = this.otherCertificates.iterator();
      while (((Iterator)localObject2).hasNext())
      {
        localObject3 = (Certificate)((Iterator)localObject2).next();
        if (paramCertSelector.match((Certificate)localObject3))
          ((Set)localObject1).add(localObject3);
      }
      return localObject1;
    }
    if (this.certSubjects.isEmpty())
      return Collections.EMPTY_SET;
    Object localObject1 = (X509CertSelector)paramCertSelector;
    Object localObject3 = ((X509CertSelector)localObject1).getCertificate();
    if (localObject3 != null)
      localObject2 = ((X509Certificate)localObject3).getSubjectX500Principal();
    else
      localObject2 = CertPathHelper.getSubject((X509CertSelector)localObject1);
    if (localObject2 != null)
    {
      localObject4 = this.certSubjects.get(localObject2);
      if (localObject4 == null)
        return Collections.EMPTY_SET;
      if (localObject4 instanceof X509Certificate)
      {
        localObject5 = (X509Certificate)localObject4;
        if (((X509CertSelector)localObject1).match((Certificate)localObject5))
          return Collections.singleton(localObject5);
        return Collections.EMPTY_SET;
      }
      Object localObject5 = (List)localObject4;
      HashSet localHashSet = new HashSet(16);
      Iterator localIterator = ((List)localObject5).iterator();
      while (localIterator.hasNext())
      {
        X509Certificate localX509Certificate = (X509Certificate)localIterator.next();
        if (((X509CertSelector)localObject1).match(localX509Certificate))
          localHashSet.add(localX509Certificate);
      }
      return localHashSet;
    }
    Object localObject4 = new HashSet(16);
    matchX509Certs((CertSelector)localObject1, (Collection)localObject4);
    return ((Collection<? extends Certificate>)(Collection<? extends Certificate>)(Collection<? extends Certificate>)(Collection<? extends Certificate>)(Collection<? extends Certificate>)localObject4);
  }

  private void matchX509Certs(CertSelector paramCertSelector, Collection paramCollection)
  {
    Iterator localIterator1 = this.certSubjects.values().iterator();
    while (localIterator1.hasNext())
    {
      Object localObject2;
      Object localObject1 = localIterator1.next();
      if (localObject1 instanceof X509Certificate)
      {
        localObject2 = (X509Certificate)localObject1;
        if (paramCertSelector.match((Certificate)localObject2))
          paramCollection.add(localObject2);
      }
      else
      {
        localObject2 = (List)localObject1;
        Iterator localIterator2 = ((List)localObject2).iterator();
        while (localIterator2.hasNext())
        {
          X509Certificate localX509Certificate = (X509Certificate)localIterator2.next();
          if (paramCertSelector.match(localX509Certificate))
            paramCollection.add(localX509Certificate);
        }
      }
    }
  }

  public Collection<CRL> engineGetCRLs(CRLSelector paramCRLSelector)
    throws CertStoreException
  {
    if (paramCRLSelector == null)
    {
      localObject1 = new HashSet();
      matchX509CRLs(new X509CRLSelector(), (Collection)localObject1);
      ((Set)localObject1).addAll(this.otherCRLs);
      return localObject1;
    }
    if (!(paramCRLSelector instanceof X509CRLSelector))
    {
      localObject1 = new HashSet();
      matchX509CRLs(paramCRLSelector, (Collection)localObject1);
      localObject2 = this.otherCRLs.iterator();
      while (((Iterator)localObject2).hasNext())
      {
        localObject3 = (CRL)((Iterator)localObject2).next();
        if (paramCRLSelector.match((CRL)localObject3))
          ((Set)localObject1).add(localObject3);
      }
      return localObject1;
    }
    if (this.crlIssuers.isEmpty())
      return Collections.EMPTY_SET;
    Object localObject1 = (X509CRLSelector)paramCRLSelector;
    Object localObject2 = CertPathHelper.getIssuers((X509CRLSelector)localObject1);
    if (localObject2 != null)
    {
      localObject3 = new HashSet(16);
      Iterator localIterator1 = ((Collection)localObject2).iterator();
      while (localIterator1.hasNext())
      {
        Object localObject5;
        X500Principal localX500Principal = (X500Principal)localIterator1.next();
        Object localObject4 = this.crlIssuers.get(localX500Principal);
        if (localObject4 == null)
          continue;
        if (localObject4 instanceof X509CRL)
        {
          localObject5 = (X509CRL)localObject4;
          if (((X509CRLSelector)localObject1).match((CRL)localObject5))
            ((HashSet)localObject3).add(localObject5);
        }
        else
        {
          localObject5 = (List)localObject4;
          Iterator localIterator2 = ((List)localObject5).iterator();
          while (localIterator2.hasNext())
          {
            X509CRL localX509CRL = (X509CRL)localIterator2.next();
            if (((X509CRLSelector)localObject1).match(localX509CRL))
              ((HashSet)localObject3).add(localX509CRL);
          }
        }
      }
      return localObject3;
    }
    Object localObject3 = new HashSet(16);
    matchX509CRLs((CRLSelector)localObject1, (Collection)localObject3);
    return ((Collection<CRL>)(Collection<CRL>)(Collection<CRL>)(Collection<CRL>)localObject3);
  }

  private void matchX509CRLs(CRLSelector paramCRLSelector, Collection paramCollection)
  {
    Iterator localIterator1 = this.crlIssuers.values().iterator();
    while (localIterator1.hasNext())
    {
      Object localObject2;
      Object localObject1 = localIterator1.next();
      if (localObject1 instanceof X509CRL)
      {
        localObject2 = (X509CRL)localObject1;
        if (paramCRLSelector.match((CRL)localObject2))
          paramCollection.add(localObject2);
      }
      else
      {
        localObject2 = (List)localObject1;
        Iterator localIterator2 = ((List)localObject2).iterator();
        while (localIterator2.hasNext())
        {
          X509CRL localX509CRL = (X509CRL)localIterator2.next();
          if (paramCRLSelector.match(localX509CRL))
            paramCollection.add(localX509CRL);
        }
      }
    }
  }
}