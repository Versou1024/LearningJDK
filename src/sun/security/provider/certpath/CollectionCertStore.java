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
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Iterator;

public class CollectionCertStore extends CertStoreSpi
{
  private Collection coll;

  public CollectionCertStore(CertStoreParameters paramCertStoreParameters)
    throws InvalidAlgorithmParameterException
  {
    super(paramCertStoreParameters);
    if (!(paramCertStoreParameters instanceof CollectionCertStoreParameters))
      throw new InvalidAlgorithmParameterException("parameters must be CollectionCertStoreParameters");
    this.coll = ((CollectionCertStoreParameters)paramCertStoreParameters).getCollection();
  }

  public Collection<Certificate> engineGetCertificates(CertSelector paramCertSelector)
    throws CertStoreException
  {
    if (this.coll == null)
      throw new CertStoreException("Collection is null");
    int i = 0;
    if (i < 10);
    try
    {
      Object localObject;
      HashSet localHashSet = new HashSet();
      Iterator localIterator = this.coll.iterator();
      if (paramCertSelector != null)
        while (true)
        {
          if (!(localIterator.hasNext()))
            break label136;
          localObject = localIterator.next();
          if ((localObject instanceof Certificate) && (paramCertSelector.match((Certificate)localObject)))
            localHashSet.add(localObject);
        }
      while (localIterator.hasNext())
      {
        localObject = localIterator.next();
        if (localObject instanceof Certificate)
          localHashSet.add(localObject);
      }
      label136: return localHashSet;
    }
    catch (ConcurrentModificationException localConcurrentModificationException)
    {
      while (true)
        ++i;
      throw new ConcurrentModificationException("Too many ConcurrentModificationExceptions");
    }
  }

  public Collection<CRL> engineGetCRLs(CRLSelector paramCRLSelector)
    throws CertStoreException
  {
    if (this.coll == null)
      throw new CertStoreException("Collection is null");
    int i = 0;
    if (i < 10);
    try
    {
      Object localObject;
      HashSet localHashSet = new HashSet();
      Iterator localIterator = this.coll.iterator();
      if (paramCRLSelector != null)
        while (true)
        {
          if (!(localIterator.hasNext()))
            break label136;
          localObject = localIterator.next();
          if ((localObject instanceof CRL) && (paramCRLSelector.match((CRL)localObject)))
            localHashSet.add(localObject);
        }
      while (localIterator.hasNext())
      {
        localObject = localIterator.next();
        if (localObject instanceof CRL)
          localHashSet.add(localObject);
      }
      label136: return localHashSet;
    }
    catch (ConcurrentModificationException localConcurrentModificationException)
    {
      while (true)
        ++i;
      throw new ConcurrentModificationException("Too many ConcurrentModificationExceptions");
    }
  }
}