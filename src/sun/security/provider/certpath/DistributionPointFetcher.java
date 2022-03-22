package sun.security.provider.certpath;

import Z;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.security.AccessController;
import java.security.PublicKey;
import java.security.cert.CRL;
import java.security.cert.CRLException;
import java.security.cert.CRLSelector;
import java.security.cert.CertStore;
import java.security.cert.CertStoreException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;
import java.security.cert.X509CRLSelector;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collection<Ljava.security.cert.X509CRL;>;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.security.auth.x500.X500Principal;
import sun.security.action.GetPropertyAction;
import sun.security.util.Cache;
import sun.security.util.Debug;
import sun.security.util.ObjectIdentifier;
import sun.security.x509.CRLDistributionPointsExtension;
import sun.security.x509.DistributionPoint;
import sun.security.x509.DistributionPointName;
import sun.security.x509.GeneralName;
import sun.security.x509.GeneralNameInterface;
import sun.security.x509.GeneralNames;
import sun.security.x509.IssuingDistributionPointExtension;
import sun.security.x509.PKIXExtensions;
import sun.security.x509.RDN;
import sun.security.x509.ReasonFlags;
import sun.security.x509.URIName;
import sun.security.x509.X500Name;
import sun.security.x509.X509CRLImpl;
import sun.security.x509.X509CertImpl;

class DistributionPointFetcher
{
  private static final Debug debug = Debug.getInstance("certpath");
  private static final boolean[] ALL_REASONS = { true, true, true, true, true, true, true, true, true };
  private static final boolean USE_CRLDP = getBooleanProperty("com.sun.security.enableCRLDP", false);
  private static final DistributionPointFetcher INSTANCE = new DistributionPointFetcher();
  private static final int CHECK_INTERVAL = 30000;
  private static final int CACHE_SIZE = 185;
  private final CertificateFactory factory;
  private final Cache cache;

  public static boolean getBooleanProperty(String paramString, boolean paramBoolean)
  {
    String str = (String)AccessController.doPrivileged(new GetPropertyAction(paramString));
    if (str == null)
      return paramBoolean;
    if (str.equalsIgnoreCase("false"))
      return false;
    if (str.equalsIgnoreCase("true"))
      return true;
    throw new RuntimeException("Value of " + paramString + " must either be 'true' or 'false'");
  }

  private DistributionPointFetcher()
  {
    try
    {
      this.factory = CertificateFactory.getInstance("X.509");
    }
    catch (CertificateException localCertificateException)
    {
      throw new RuntimeException();
    }
    this.cache = Cache.newSoftMemoryCache(185);
  }

  static DistributionPointFetcher getInstance()
  {
    return INSTANCE;
  }

  Collection<X509CRL> getCRLs(CRLSelector paramCRLSelector, PublicKey paramPublicKey, String paramString, List<CertStore> paramList, boolean[] paramArrayOfBoolean)
    throws CertStoreException
  {
    if (!(USE_CRLDP))
      return Collections.emptySet();
    if (!(paramCRLSelector instanceof X509CRLSelector))
      return Collections.emptySet();
    X509CRLSelector localX509CRLSelector = (X509CRLSelector)paramCRLSelector;
    X509Certificate localX509Certificate = localX509CRLSelector.getCertificateChecking();
    if (localX509Certificate == null)
      return Collections.emptySet();
    try
    {
      X509CertImpl localX509CertImpl = X509CertImpl.toImpl(localX509Certificate);
      if (debug != null)
        debug.println("DistributionPointFetcher.getCRLs: Checking CRLDPs for " + localX509CertImpl.getSubjectX500Principal());
      CRLDistributionPointsExtension localCRLDistributionPointsExtension = localX509CertImpl.getCRLDistributionPointsExtension();
      if (localCRLDistributionPointsExtension == null)
      {
        if (debug != null)
          debug.println("No CRLDP ext");
        return Collections.emptySet();
      }
      List localList = (List)localCRLDistributionPointsExtension.get("points");
      HashSet localHashSet = new HashSet();
      Iterator localIterator = localList.iterator();
      while ((localIterator.hasNext()) && (!(Arrays.equals(paramArrayOfBoolean, ALL_REASONS))))
      {
        DistributionPoint localDistributionPoint = (DistributionPoint)localIterator.next();
        Collection localCollection = getCRLs(localX509CRLSelector, localX509CertImpl, localDistributionPoint, paramArrayOfBoolean, paramPublicKey, paramString, paramList);
        localHashSet.addAll(localCollection);
      }
      if (debug != null)
        debug.println("Returning " + localHashSet.size() + " CRLs");
      return localHashSet;
    }
    catch (CertificateException localCertificateException)
    {
      return Collections.emptySet();
    }
    catch (IOException localIOException)
    {
    }
    return Collections.emptySet();
  }

  private Collection<X509CRL> getCRLs(X509CRLSelector paramX509CRLSelector, X509CertImpl paramX509CertImpl, DistributionPoint paramDistributionPoint, boolean[] paramArrayOfBoolean, PublicKey paramPublicKey, String paramString, List<CertStore> paramList)
  {
    Object localObject1;
    GeneralNames localGeneralNames = paramDistributionPoint.getFullName();
    if (localGeneralNames == null)
      return Collections.emptySet();
    ArrayList localArrayList1 = new ArrayList();
    ArrayList localArrayList2 = new ArrayList(2);
    Iterator localIterator = localGeneralNames.iterator();
    while (localIterator.hasNext())
    {
      Object localObject2;
      localObject1 = (GeneralName)localIterator.next();
      if (((GeneralName)localObject1).getType() == 4)
      {
        localObject2 = (X500Name)((GeneralName)localObject1).getName();
        localArrayList1.addAll(getCRLs((X500Name)localObject2, paramX509CertImpl.getIssuerX500Principal(), paramList));
      }
      else if (((GeneralName)localObject1).getType() == 6)
      {
        localObject2 = (URIName)((GeneralName)localObject1).getName();
        X509CRL localX509CRL = getCRL((URIName)localObject2);
        if (localX509CRL != null)
          localArrayList1.add(localX509CRL);
      }
    }
    localIterator = localArrayList1.iterator();
    while (localIterator.hasNext())
    {
      localObject1 = (X509CRL)localIterator.next();
      try
      {
        paramX509CRLSelector.setIssuerNames(null);
        if ((paramX509CRLSelector.match((CRL)localObject1)) && (verifyCRL(paramX509CertImpl, paramDistributionPoint, (X509CRL)localObject1, paramArrayOfBoolean, paramPublicKey, paramString)))
          localArrayList2.add(localObject1);
      }
      catch (Exception localException)
      {
        if (debug != null)
        {
          debug.println("Exception verifying CRL: " + localException.getMessage());
          localException.printStackTrace();
        }
      }
    }
    return ((Collection<X509CRL>)(Collection<X509CRL>)localArrayList2);
  }

  private X509CRL getCRL(URIName paramURIName)
  {
    URI localURI = paramURIName.getURI();
    if (debug != null)
      debug.println("Trying to fetch CRL from DP " + localURI);
    if (localURI.getScheme().toLowerCase().equals("ldap"))
    {
      localObject = localURI.getPath();
      if (debug != null)
      {
        debug.println("authority:" + localURI.getAuthority());
        debug.println("path:" + ((String)localObject));
      }
      if (((String)localObject).charAt(0) == '/')
        localObject = ((String)localObject).substring(1);
      try
      {
        LDAPCertStore.LDAPCRLSelector localLDAPCRLSelector = new LDAPCertStore.LDAPCRLSelector();
        localLDAPCRLSelector.addIssuerName((String)localObject);
        CertStore localCertStore = LDAPCertStore.getInstance(LDAPCertStore.getParameters(localURI));
        Collection localCollection = localCertStore.getCRLs(localLDAPCRLSelector);
        if (localCollection.isEmpty())
          return null;
        return ((X509CRL)localCollection.iterator().next());
      }
      catch (Exception localException)
      {
        if (debug != null)
        {
          debug.println("Exception getting CRL from CertStore: " + localException);
          localException.printStackTrace();
        }
        return null;
      }
    }
    Object localObject = (CacheEntry)this.cache.get(localURI);
    if (localObject == null)
    {
      localObject = new CacheEntry();
      this.cache.put(localURI, localObject);
    }
    return ((X509CRL)((CacheEntry)localObject).getCRL(this.factory, localURI));
  }

  private Collection<X509CRL> getCRLs(X500Name paramX500Name, X500Principal paramX500Principal, List<CertStore> paramList)
  {
    if (debug != null)
      debug.println("Trying to fetch CRL from DP " + paramX500Name);
    X509CRLSelector localX509CRLSelector = new X509CRLSelector();
    localX509CRLSelector.addIssuer(paramX500Name.asX500Principal());
    localX509CRLSelector.addIssuer(paramX500Principal);
    ArrayList localArrayList = new ArrayList();
    Iterator localIterator = paramList.iterator();
    while (localIterator.hasNext())
    {
      CertStore localCertStore = (CertStore)localIterator.next();
      try
      {
        localArrayList.addAll(localCertStore.getCRLs(localX509CRLSelector));
      }
      catch (CertStoreException localCertStoreException)
      {
        if (debug != null)
        {
          debug.println("Non-fatal exception while retrieving CRLs: " + localCertStoreException);
          localCertStoreException.printStackTrace();
        }
      }
    }
    return localArrayList;
  }

  boolean verifyCRL(X509CertImpl paramX509CertImpl, DistributionPoint paramDistributionPoint, X509CRL paramX509CRL, boolean[] paramArrayOfBoolean, PublicKey paramPublicKey, String paramString)
    throws CRLException, IOException
  {
    Object localObject3;
    Object localObject4;
    Object localObject6;
    X509CRLImpl localX509CRLImpl = X509CRLImpl.toImpl(paramX509CRL);
    IssuingDistributionPointExtension localIssuingDistributionPointExtension = localX509CRLImpl.getIssuingDistributionPointExtension();
    X500Name localX500Name1 = (X500Name)paramX509CertImpl.getIssuerDN();
    X500Name localX500Name2 = (X500Name)localX509CRLImpl.getIssuerDN();
    GeneralNames localGeneralNames = paramDistributionPoint.getCRLIssuer();
    if (localGeneralNames != null)
    {
      if ((localIssuingDistributionPointExtension == null) || (((Boolean)localIssuingDistributionPointExtension.get("indirect_crl")).equals(Boolean.FALSE)))
        return false;
      int i = 0;
      localObject2 = localGeneralNames.iterator();
      while ((i == 0) && (((Iterator)localObject2).hasNext()))
      {
        localObject3 = ((GeneralName)((Iterator)localObject2).next()).getName();
        if (localX500Name2.equals(localObject3) == true)
          i = 1;
      }
      if (i == 0)
        return false;
    }
    else if (!(localX500Name2.equals(localX500Name1)))
    {
      if (debug != null)
        debug.println("crl issuer does not equal cert issuer");
      return false;
    }
    if (localIssuingDistributionPointExtension != null)
    {
      localObject1 = (DistributionPointName)localIssuingDistributionPointExtension.get("point");
      if (localObject1 != null)
      {
        Object localObject5;
        Object localObject7;
        localObject2 = ((DistributionPointName)localObject1).getFullName();
        if (localObject2 == null)
        {
          localObject3 = ((DistributionPointName)localObject1).getRelativeName();
          if (localObject3 == null)
          {
            if (debug != null)
              debug.println("IDP must be relative or full DN");
            return false;
          }
          if (debug != null)
            debug.println("IDP relativeName:" + localObject3);
          localObject2 = getFullNames(localX500Name2, (RDN)localObject3);
        }
        if ((paramDistributionPoint.getFullName() != null) || (paramDistributionPoint.getRelativeName() != null))
        {
          localObject3 = paramDistributionPoint.getFullName();
          if (localObject3 == null)
          {
            RDN localRDN = paramDistributionPoint.getRelativeName();
            if (localRDN == null)
            {
              if (debug != null)
                debug.println("DP must be relative or full DN");
              return false;
            }
            if (debug != null)
              debug.println("DP relativeName:" + localRDN);
            localObject3 = getFullNames(localX500Name1, localRDN);
          }
          boolean bool2 = false;
          localObject5 = ((GeneralNames)localObject2).iterator();
          while ((!(bool2)) && (((Iterator)localObject5).hasNext()))
          {
            localObject6 = ((GeneralName)((Iterator)localObject5).next()).getName();
            if (debug != null)
              debug.println("idpName: " + localObject6);
            localObject7 = ((GeneralNames)localObject3).iterator();
            while ((!(bool2)) && (((Iterator)localObject7).hasNext()))
            {
              GeneralNameInterface localGeneralNameInterface = ((GeneralName)((Iterator)localObject7).next()).getName();
              if (debug != null)
                debug.println("pointName: " + localGeneralNameInterface);
              bool2 = localObject6.equals(localGeneralNameInterface);
            }
          }
          if (!(bool2))
          {
            if (debug != null)
              debug.println("IDP name does not match DP name");
            return false;
          }
        }
        else
        {
          boolean bool1 = false;
          localObject4 = localGeneralNames.iterator();
          while ((!(bool1)) && (((Iterator)localObject4).hasNext()))
          {
            localObject5 = ((GeneralName)((Iterator)localObject4).next()).getName();
            localObject6 = ((GeneralNames)localObject2).iterator();
            while ((!(bool1)) && (((Iterator)localObject6).hasNext()))
            {
              localObject7 = ((GeneralName)((Iterator)localObject6).next()).getName();
              bool1 = localObject5.equals(localObject7);
            }
          }
          if (!(bool1))
            return false;
        }
      }
      localObject2 = (Boolean)localIssuingDistributionPointExtension.get("only_user_certs");
      if ((((Boolean)localObject2).equals(Boolean.TRUE)) && (paramX509CertImpl.getBasicConstraints() != -1))
      {
        if (debug != null)
          debug.println("cert must be a EE cert");
        return false;
      }
      localObject2 = (Boolean)localIssuingDistributionPointExtension.get("only_ca_certs");
      if ((((Boolean)localObject2).equals(Boolean.TRUE)) && (paramX509CertImpl.getBasicConstraints() == -1))
      {
        if (debug != null)
          debug.println("cert must be a CA cert");
        return false;
      }
      localObject2 = (Boolean)localIssuingDistributionPointExtension.get("only_attribute_certs");
      if (((Boolean)localObject2).equals(Boolean.TRUE))
      {
        if (debug != null)
          debug.println("cert must not be an AA cert");
        return false;
      }
    }
    Object localObject1 = new boolean[9];
    Object localObject2 = null;
    if (localIssuingDistributionPointExtension != null)
      localObject2 = (ReasonFlags)localIssuingDistributionPointExtension.get("reasons");
    boolean[] arrayOfBoolean = paramDistributionPoint.getReasonFlags();
    if (localObject2 != null)
      if (arrayOfBoolean != null)
      {
        localObject4 = ((ReasonFlags)localObject2).getFlags();
        for (k = 0; k < localObject4.length; ++k)
          if ((localObject4[k] != 0) && (arrayOfBoolean[k] != 0))
            localObject1[k] = 1;
      }
      else
      {
        localObject1 = (boolean[])(boolean[])((ReasonFlags)localObject2).getFlags().clone();
      }
    else if ((localIssuingDistributionPointExtension == null) || (localObject2 == null))
      if (arrayOfBoolean != null)
      {
        localObject1 = (boolean[])(boolean[])arrayOfBoolean.clone();
      }
      else
      {
        localObject1 = new boolean[9];
        Arrays.fill(localObject1, true);
      }
    int j = 0;
    for (int k = 0; (k < localObject1.length) && (j == 0); ++k)
      if ((paramArrayOfBoolean[k] == 0) && (localObject1[k] != 0))
        j = 1;
    if (j == 0)
      return false;
    try
    {
      paramX509CRL.verify(paramPublicKey, paramString);
    }
    catch (Exception localException)
    {
      if (debug != null)
        debug.println("CRL signature failed to verify");
      return false;
    }
    Set localSet = paramX509CRL.getCriticalExtensionOIDs();
    if (localSet != null)
    {
      localSet.remove(PKIXExtensions.IssuingDistributionPoint_Id.toString());
      if (!(localSet.isEmpty()))
      {
        if (debug != null)
        {
          debug.println("Unrecognized critical extension(s) in CRL: " + localSet);
          localObject6 = localSet.iterator();
          while (((Iterator)localObject6).hasNext())
            debug.println((String)((Iterator)localObject6).next());
        }
        return false;
      }
    }
    for (int l = 0; l < localObject1.length; ++l)
      if ((paramArrayOfBoolean[l] == 0) && (localObject1[l] != 0))
        paramArrayOfBoolean[l] = true;
    return true;
  }

  private GeneralNames getFullNames(X500Name paramX500Name, RDN paramRDN)
    throws IOException
  {
    ArrayList localArrayList = new ArrayList(paramX500Name.rdns());
    localArrayList.add(paramRDN);
    X500Name localX500Name = new X500Name((RDN[])(RDN[])localArrayList.toArray(new RDN[0]));
    GeneralNames localGeneralNames = new GeneralNames();
    localGeneralNames.add(new GeneralName(localX500Name));
    return localGeneralNames;
  }

  private static class CacheEntry
  {
    private X509CRL crl;
    private long lastChecked;
    private long lastModified;

    synchronized X509CRL getCRL(CertificateFactory paramCertificateFactory, URI paramURI)
    {
      long l1 = System.currentTimeMillis();
      if (l1 - this.lastChecked < 30000L)
      {
        if (DistributionPointFetcher.access$000() != null)
          DistributionPointFetcher.access$000().println("Returning CRL from cache");
        return this.crl;
      }
      this.lastChecked = l1;
      InputStream localInputStream = null;
      try
      {
        URL localURL = paramURI.toURL();
        URLConnection localURLConnection = localURL.openConnection();
        if (this.lastModified != 3412047737930121216L)
          localURLConnection.setIfModifiedSince(this.lastModified);
        localInputStream = localURLConnection.getInputStream();
        long l2 = this.lastModified;
        this.lastModified = localURLConnection.getLastModified();
        if (l2 != 3412047737930121216L)
        {
          if (l2 == this.lastModified)
          {
            if (DistributionPointFetcher.access$000() != null)
              DistributionPointFetcher.access$000().println("Not modified, using cached copy");
            localObject1 = this.crl;
            return localObject1;
          }
          if (localURLConnection instanceof HttpURLConnection)
          {
            localObject1 = (HttpURLConnection)localURLConnection;
            if (((HttpURLConnection)localObject1).getResponseCode() == 304)
            {
              if (DistributionPointFetcher.access$000() != null)
                DistributionPointFetcher.access$000().println("Not modified, using cached copy");
              X509CRL localX509CRL = this.crl;
              return localX509CRL;
            }
          }
        }
        if (DistributionPointFetcher.access$000() != null)
          DistributionPointFetcher.access$000().println("Downloading new CRL...");
        this.crl = ((X509CRL)paramCertificateFactory.generateCRL(localInputStream));
        Object localObject1 = this.crl;
        return localObject1;
      }
      catch (IOException localIOException2)
      {
        if (DistributionPointFetcher.access$000() != null)
        {
          DistributionPointFetcher.access$000().println("Exception fetching CRLDP:");
          localIOException1.printStackTrace();
        }
      }
      catch (CRLException localIOException3)
      {
        if (DistributionPointFetcher.access$000() != null)
        {
          DistributionPointFetcher.access$000().println("Exception fetching CRLDP:");
          localCRLException.printStackTrace();
        }
      }
      finally
      {
        if (localInputStream != null)
          try
          {
            localInputStream.close();
          }
          catch (IOException localIOException7)
          {
          }
      }
      this.lastModified = 3412047463052214272L;
      this.crl = null;
      return ((X509CRL)null);
    }
  }
}