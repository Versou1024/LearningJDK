package sun.security.provider.certpath;

import B;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.security.AccessController;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CRL;
import java.security.cert.CRLException;
import java.security.cert.CRLSelector;
import java.security.cert.CertSelector;
import java.security.cert.CertStore;
import java.security.cert.CertStoreException;
import java.security.cert.CertStoreParameters;
import java.security.cert.CertStoreSpi;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.LDAPCertStoreParameters;
import java.security.cert.X509CRL;
import java.security.cert.X509CRLSelector;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collection<Ljava.security.cert.X509CRL;>;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.security.auth.x500.X500Principal;
import sun.misc.HexDumpEncoder;
import sun.security.action.GetPropertyAction;
import sun.security.util.Cache;
import sun.security.util.Debug;

public class LDAPCertStore extends CertStoreSpi
{
  private static final Debug debug = Debug.getInstance("certpath");
  private static final boolean DEBUG = 0;
  private static final String USER_CERT = "userCertificate;binary";
  private static final String CA_CERT = "cACertificate;binary";
  private static final String CROSS_CERT = "crossCertificatePair;binary";
  private static final String CRL = "certificateRevocationList;binary";
  private static final String ARL = "authorityRevocationList;binary";
  private static final String DELTA_CRL = "deltaRevocationList;binary";
  private static final String[] STRING0 = new String[0];
  private static final byte[][] BB0 = new byte[0][];
  private static final Attributes EMPTY_ATTRIBUTES = new BasicAttributes();
  private static final int DEFAULT_CACHE_SIZE = 750;
  private static final int DEFAULT_CACHE_LIFETIME = 30;
  private static final int LIFETIME;
  private static final String PROP_LIFETIME = "sun.security.certpath.ldap.cache.lifetime";
  private CertificateFactory cf;
  private DirContext ctx;
  private boolean prefetchCRLs = false;
  private final Cache valueCache;
  private int cacheHits = 0;
  private int cacheMisses = 0;
  private int requests = 0;
  private static final Cache certStoreCache;

  public LDAPCertStore(CertStoreParameters paramCertStoreParameters)
    throws InvalidAlgorithmParameterException
  {
    super(paramCertStoreParameters);
    if (!(paramCertStoreParameters instanceof LDAPCertStoreParameters))
      throw new InvalidAlgorithmParameterException("parameters must be LDAPCertStoreParameters");
    LDAPCertStoreParameters localLDAPCertStoreParameters = (LDAPCertStoreParameters)paramCertStoreParameters;
    createInitialDirContext(localLDAPCertStoreParameters.getServerName(), localLDAPCertStoreParameters.getPort());
    try
    {
      this.cf = CertificateFactory.getInstance("X.509");
    }
    catch (CertificateException localCertificateException)
    {
      throw new InvalidAlgorithmParameterException("unable to create CertificateFactory for X.509");
    }
    if (LIFETIME == 0)
      this.valueCache = Cache.newNullCache();
    else if (LIFETIME < 0)
      this.valueCache = Cache.newSoftMemoryCache(750);
    else
      this.valueCache = Cache.newSoftMemoryCache(750, LIFETIME);
  }

  static synchronized CertStore getInstance(LDAPCertStoreParameters paramLDAPCertStoreParameters)
    throws NoSuchAlgorithmException, InvalidAlgorithmParameterException
  {
    CertStore localCertStore = (CertStore)certStoreCache.get(paramLDAPCertStoreParameters);
    if (localCertStore == null)
    {
      localCertStore = CertStore.getInstance("LDAP", paramLDAPCertStoreParameters);
      certStoreCache.put(paramLDAPCertStoreParameters, localCertStore);
    }
    else if (debug != null)
    {
      debug.println("LDAPCertStore.getInstance: cache hit");
    }
    return localCertStore;
  }

  private void createInitialDirContext(String paramString, int paramInt)
    throws InvalidAlgorithmParameterException
  {
    String str = "ldap://" + paramString + ":" + paramInt;
    Hashtable localHashtable1 = new Hashtable();
    localHashtable1.put("java.naming.factory.initial", "com.sun.jndi.ldap.LdapCtxFactory");
    localHashtable1.put("java.naming.provider.url", str);
    try
    {
      this.ctx = new InitialDirContext(localHashtable1);
      Hashtable localHashtable2 = this.ctx.getEnvironment();
      if (localHashtable2.get("java.naming.referral") == null)
        this.ctx.addToEnvironment("java.naming.referral", "follow");
    }
    catch (NamingException localNamingException)
    {
      if (debug != null)
      {
        debug.println("LDAPCertStore.engineInit about to throw InvalidAlgorithmParameterException");
        localNamingException.printStackTrace();
      }
      InvalidAlgorithmParameterException localInvalidAlgorithmParameterException = new InvalidAlgorithmParameterException("unable to create InitialDirContext using supplied parameters");
      localInvalidAlgorithmParameterException.initCause(localNamingException);
      throw ((InvalidAlgorithmParameterException)localInvalidAlgorithmParameterException);
    }
  }

  private Collection getCertificates(LDAPRequest paramLDAPRequest, String paramString, CertSelector paramCertSelector)
    throws CertStoreException
  {
    byte[][] arrayOfByte;
    try
    {
      arrayOfByte = paramLDAPRequest.getValues(paramString);
    }
    catch (NamingException localNamingException)
    {
      throw new CertStoreException(localNamingException);
    }
    int i = arrayOfByte.length;
    if (i == 0)
      return Collections.EMPTY_LIST;
    ArrayList localArrayList = new ArrayList(i);
    for (int j = 0; j < i; ++j)
    {
      ByteArrayInputStream localByteArrayInputStream = new ByteArrayInputStream(arrayOfByte[j]);
      try
      {
        Certificate localCertificate = this.cf.generateCertificate(localByteArrayInputStream);
        if (paramCertSelector.match(localCertificate))
          localArrayList.add(localCertificate);
      }
      catch (CertificateException localCertificateException)
      {
        if (debug != null)
        {
          debug.println("LDAPCertStore.getCertificates() encountered exception while parsing cert, skipping the bad data: ");
          HexDumpEncoder localHexDumpEncoder = new HexDumpEncoder();
          debug.println("[ " + localHexDumpEncoder.encodeBuffer(arrayOfByte[j]) + " ]");
        }
      }
    }
    return localArrayList;
  }

  private Collection getCertPairs(LDAPRequest paramLDAPRequest, String paramString)
    throws CertStoreException
  {
    byte[][] arrayOfByte;
    try
    {
      arrayOfByte = paramLDAPRequest.getValues(paramString);
    }
    catch (NamingException localNamingException)
    {
      throw new CertStoreException(localNamingException);
    }
    int i = arrayOfByte.length;
    if (i == 0)
      return Collections.EMPTY_LIST;
    ArrayList localArrayList = new ArrayList(i);
    for (int j = 0; j < i; ++j)
      try
      {
        X509CertificatePair localX509CertificatePair = X509CertificatePair.generateCertificatePair(arrayOfByte[j]);
        localArrayList.add(localX509CertificatePair);
      }
      catch (CertificateException localCertificateException)
      {
        if (debug != null)
        {
          debug.println("LDAPCertStore.getCertPairs() encountered exception while parsing cert, skipping the bad data: ");
          HexDumpEncoder localHexDumpEncoder = new HexDumpEncoder();
          debug.println("[ " + localHexDumpEncoder.encodeBuffer(arrayOfByte[j]) + " ]");
        }
      }
    return localArrayList;
  }

  private Collection getMatchingCrossCerts(LDAPRequest paramLDAPRequest, CertSelector paramCertSelector1, CertSelector paramCertSelector2)
    throws CertStoreException
  {
    Collection localCollection = getCertPairs(paramLDAPRequest, "crossCertificatePair;binary");
    ArrayList localArrayList = new ArrayList();
    Iterator localIterator = localCollection.iterator();
    while (localIterator.hasNext())
    {
      X509Certificate localX509Certificate;
      X509CertificatePair localX509CertificatePair = (X509CertificatePair)localIterator.next();
      if (paramCertSelector1 != null)
      {
        localX509Certificate = localX509CertificatePair.getForward();
        if ((localX509Certificate != null) && (paramCertSelector1.match(localX509Certificate)))
          localArrayList.add(localX509Certificate);
      }
      if (paramCertSelector2 != null)
      {
        localX509Certificate = localX509CertificatePair.getReverse();
        if ((localX509Certificate != null) && (paramCertSelector2.match(localX509Certificate)))
          localArrayList.add(localX509Certificate);
      }
    }
    return localArrayList;
  }

  public synchronized Collection<X509Certificate> engineGetCertificates(CertSelector paramCertSelector)
    throws CertStoreException
  {
    LDAPRequest localLDAPRequest;
    if (debug != null)
      debug.println("LDAPCertStore.engineGetCertificates() selector: " + String.valueOf(paramCertSelector));
    if (paramCertSelector == null)
      paramCertSelector = new LDAPCertSelector();
    if (!(paramCertSelector instanceof X509CertSelector))
      throw new CertStoreException("LDAPCertStore needs an X509CertSelector to find certs");
    X509CertSelector localX509CertSelector = (X509CertSelector)paramCertSelector;
    int i = localX509CertSelector.getBasicConstraints();
    String str1 = localX509CertSelector.getSubjectAsString();
    String str2 = localX509CertSelector.getIssuerAsString();
    HashSet localHashSet = new HashSet();
    if (debug != null)
      debug.println("LDAPCertStore.engineGetCertificates() basicConstraints: " + i);
    if (str1 != null)
    {
      if (debug != null)
        debug.println("LDAPCertStore.engineGetCertificates() subject is not null");
      localLDAPRequest = new LDAPRequest(this, str1);
      if (i > -2)
      {
        localLDAPRequest.addRequestedAttribute("crossCertificatePair;binary");
        localLDAPRequest.addRequestedAttribute("cACertificate;binary");
        localLDAPRequest.addRequestedAttribute("authorityRevocationList;binary");
        if (this.prefetchCRLs)
          localLDAPRequest.addRequestedAttribute("certificateRevocationList;binary");
      }
      if (i < 0)
        localLDAPRequest.addRequestedAttribute("userCertificate;binary");
      if (i > -2)
      {
        localHashSet.addAll(getMatchingCrossCerts(localLDAPRequest, localX509CertSelector, null));
        if (debug != null)
          debug.println("LDAPCertStore.engineGetCertificates() after getMatchingCrossCerts(subject,xsel,null),certs.size(): " + localHashSet.size());
        localHashSet.addAll(getCertificates(localLDAPRequest, "cACertificate;binary", localX509CertSelector));
        if (debug != null)
          debug.println("LDAPCertStore.engineGetCertificates() after getCertificates(subject,CA_CERT,xsel),certs.size(): " + localHashSet.size());
      }
      if (i < 0)
      {
        localHashSet.addAll(getCertificates(localLDAPRequest, "userCertificate;binary", localX509CertSelector));
        if (debug != null)
          debug.println("LDAPCertStore.engineGetCertificates() after getCertificates(subject,USER_CERT, xsel),certs.size(): " + localHashSet.size());
      }
    }
    else
    {
      if (debug != null)
        debug.println("LDAPCertStore.engineGetCertificates() subject is null");
      if (i == -2)
        throw new CertStoreException("need subject to find EE certs");
      if (str2 == null)
        throw new CertStoreException("need subject or issuer to find certs");
    }
    if (debug != null)
      debug.println("LDAPCertStore.engineGetCertificates() about to getMatchingCrossCerts...");
    if ((str2 != null) && (i > -2))
    {
      localLDAPRequest = new LDAPRequest(this, str2);
      localLDAPRequest.addRequestedAttribute("crossCertificatePair;binary");
      localLDAPRequest.addRequestedAttribute("cACertificate;binary");
      localLDAPRequest.addRequestedAttribute("authorityRevocationList;binary");
      if (this.prefetchCRLs)
        localLDAPRequest.addRequestedAttribute("certificateRevocationList;binary");
      localHashSet.addAll(getMatchingCrossCerts(localLDAPRequest, null, localX509CertSelector));
      if (debug != null)
        debug.println("LDAPCertStore.engineGetCertificates() after getMatchingCrossCerts(issuer,null,xsel),certs.size(): " + localHashSet.size());
      localHashSet.addAll(getCertificates(localLDAPRequest, "cACertificate;binary", localX509CertSelector));
      if (debug != null)
        debug.println("LDAPCertStore.engineGetCertificates() after getCertificates(issuer,CA_CERT,xsel),certs.size(): " + localHashSet.size());
    }
    if (debug != null)
      debug.println("LDAPCertStore.engineGetCertificates() returning certs");
    return localHashSet;
  }

  private Collection getCRLs(LDAPRequest paramLDAPRequest, String paramString, CRLSelector paramCRLSelector)
    throws CertStoreException
  {
    byte[][] arrayOfByte;
    try
    {
      arrayOfByte = paramLDAPRequest.getValues(paramString);
    }
    catch (NamingException localNamingException)
    {
      throw new CertStoreException(localNamingException);
    }
    int i = arrayOfByte.length;
    if (i == 0)
      return Collections.EMPTY_LIST;
    ArrayList localArrayList = new ArrayList(i);
    for (int j = 0; j < i; ++j)
      try
      {
        CRL localCRL = this.cf.generateCRL(new ByteArrayInputStream(arrayOfByte[j]));
        if (paramCRLSelector.match(localCRL))
          localArrayList.add(localCRL);
      }
      catch (CRLException localCRLException)
      {
        if (debug != null)
        {
          debug.println("LDAPCertStore.getCRLs() encountered exception while parsing CRL, skipping the bad data: ");
          HexDumpEncoder localHexDumpEncoder = new HexDumpEncoder();
          debug.println("[ " + localHexDumpEncoder.encodeBuffer(arrayOfByte[j]) + " ]");
        }
      }
    return localArrayList;
  }

  public synchronized Collection<X509CRL> engineGetCRLs(CRLSelector paramCRLSelector)
    throws CertStoreException
  {
    Object localObject1;
    if (debug != null)
      debug.println("LDAPCertStore.engineGetCRLs() selector: " + paramCRLSelector);
    if (paramCRLSelector == null)
      paramCRLSelector = new X509CRLSelector();
    if (!(paramCRLSelector instanceof X509CRLSelector))
      throw new CertStoreException("need X509CRLSelector to find CRLs");
    X509CRLSelector localX509CRLSelector = (X509CRLSelector)paramCRLSelector;
    HashSet localHashSet = new HashSet();
    X509Certificate localX509Certificate = localX509CRLSelector.getCertificateChecking();
    if (localX509Certificate != null)
    {
      localObject1 = new HashSet();
      localObject2 = localX509Certificate.getIssuerX500Principal();
      ((Collection)localObject1).add(((X500Principal)localObject2).getName("RFC2253"));
    }
    else
    {
      localObject1 = localX509CRLSelector.getIssuerNames();
      if (localObject1 == null)
        throw new CertStoreException("need issuerNames or certChecking to find CRLs");
    }
    Object localObject2 = ((Collection)localObject1).iterator();
    while (((Iterator)localObject2).hasNext())
    {
      label148: String str;
      LDAPRequest localLDAPRequest;
      Object localObject3 = ((Iterator)localObject2).next();
      if (localObject3 instanceof byte[]);
      try
      {
        X500Principal localX500Principal = new X500Principal((byte[])(byte[])localObject3);
        str = localX500Principal.getName("RFC2253");
      }
      catch (IllegalArgumentException localIllegalArgumentException)
      {
        break label148:
        str = (String)localObject3;
      }
      Object localObject4 = Collections.EMPTY_LIST;
      if ((localX509Certificate == null) || (localX509Certificate.getBasicConstraints() != -1))
      {
        localLDAPRequest = new LDAPRequest(this, str);
        localLDAPRequest.addRequestedAttribute("crossCertificatePair;binary");
        localLDAPRequest.addRequestedAttribute("cACertificate;binary");
        localLDAPRequest.addRequestedAttribute("authorityRevocationList;binary");
        if (this.prefetchCRLs)
          localLDAPRequest.addRequestedAttribute("certificateRevocationList;binary");
        try
        {
          localObject4 = getCRLs(localLDAPRequest, "authorityRevocationList;binary", localX509CRLSelector);
          if (((Collection)localObject4).isEmpty())
            this.prefetchCRLs = true;
          else
            localHashSet.addAll((Collection)localObject4);
        }
        catch (CertStoreException localCertStoreException)
        {
          if (debug != null)
          {
            debug.println("LDAPCertStore.engineGetCRLs non-fatal error retrieving ARLs:" + localCertStoreException);
            localCertStoreException.printStackTrace();
          }
        }
      }
      if ((((Collection)localObject4).isEmpty()) || (localX509Certificate == null))
      {
        localLDAPRequest = new LDAPRequest(this, str);
        localLDAPRequest.addRequestedAttribute("certificateRevocationList;binary");
        localObject4 = getCRLs(localLDAPRequest, "certificateRevocationList;binary", localX509CRLSelector);
        localHashSet.addAll((Collection)localObject4);
      }
    }
    return ((Collection<X509CRL>)(Collection<X509CRL>)(Collection<X509CRL>)localHashSet);
  }

  static LDAPCertStoreParameters getParameters(URI paramURI)
  {
    String str = paramURI.getHost();
    if (str == null)
      return new SunLDAPCertStoreParameters();
    int i = paramURI.getPort();
    return new SunLDAPCertStoreParameters(str, i);
  }

  static
  {
    GetPropertyAction localGetPropertyAction = new GetPropertyAction("sun.security.certpath.ldap.cache.lifetime");
    String str = (String)AccessController.doPrivileged(localGetPropertyAction);
    if (str != null)
      LIFETIME = Integer.parseInt(str);
    else
      LIFETIME = 30;
    certStoreCache = Cache.newSoftMemoryCache(185);
  }

  static class LDAPCRLSelector extends X509CRLSelector
  {
    private Collection<X500Principal> certIssuers;

    public boolean match(CRL paramCRL)
    {
      Collection localCollection = getIssuers();
      setIssuers(this.certIssuers);
      boolean bool = super.match(paramCRL);
      setIssuers(localCollection);
      return bool;
    }

    void setCertIssuers(Collection<X500Principal> paramCollection)
    {
      this.certIssuers = paramCollection;
    }

    Collection<X500Principal> getCertIssuers()
    {
      return this.certIssuers;
    }
  }

  static class LDAPCertSelector extends X509CertSelector
  {
    private X500Principal certSubject;

    public boolean match(Certificate paramCertificate)
    {
      X500Principal localX500Principal = getSubject();
      setSubject(this.certSubject);
      boolean bool = super.match(paramCertificate);
      setSubject(localX500Principal);
      return bool;
    }

    void setCertSubject(X500Principal paramX500Principal)
    {
      this.certSubject = paramX500Principal;
    }

    X500Principal getCertSubject()
    {
      return this.certSubject;
    }
  }

  private class LDAPRequest
  {
    private final String name;
    private Map valueMap;
    private final List requestedAttributes;

    LDAPRequest(, String paramString)
    {
      this.name = paramString;
      this.requestedAttributes = new ArrayList(5);
    }

    String getName()
    {
      return this.name;
    }

    void addRequestedAttribute()
    {
      if (this.valueMap != null)
        throw new IllegalStateException("Request already sent");
      this.requestedAttributes.add(paramString);
    }

    byte[][] getValues()
      throws NamingException
    {
      String str = this.name + "|" + paramString;
      byte[][] arrayOfByte = (byte[][])(byte[][])LDAPCertStore.access$000(this.this$0).get(str);
      if (arrayOfByte != null)
      {
        LDAPCertStore.access$108(this.this$0);
        return arrayOfByte;
      }
      LDAPCertStore.access$208(this.this$0);
      Map localMap = getValueMap();
      arrayOfByte = (byte[][])(byte[][])localMap.get(paramString);
      return arrayOfByte;
    }

    private Map getValueMap()
      throws NamingException
    {
      Attributes localAttributes;
      if (this.valueMap != null)
        return this.valueMap;
      this.valueMap = new HashMap(8);
      String[] arrayOfString = (String[])(String[])this.requestedAttributes.toArray(LDAPCertStore.access$300());
      try
      {
        localAttributes = LDAPCertStore.access$400(this.this$0).getAttributes(this.name, arrayOfString);
      }
      catch (NameNotFoundException localNameNotFoundException)
      {
        localAttributes = LDAPCertStore.access$500();
      }
      Iterator localIterator = this.requestedAttributes.iterator();
      while (localIterator.hasNext())
      {
        String str = (String)localIterator.next();
        Attribute localAttribute = localAttributes.get(str);
        byte[][] arrayOfByte = getAttributeValues(localAttribute);
        cacheAttribute(str, arrayOfByte);
        this.valueMap.put(str, arrayOfByte);
      }
      return this.valueMap;
    }

    private void cacheAttribute(, byte[][] paramArrayOfByte)
    {
      String str = this.name + "|" + paramString;
      LDAPCertStore.access$000(this.this$0).put(str, paramArrayOfByte);
    }

    private byte[][] getAttributeValues()
      throws NamingException
    {
      Object localObject1;
      if (paramAttribute == null)
      {
        localObject1 = LDAPCertStore.access$600();
      }
      else
      {
        localObject1 = new byte[paramAttribute.size()][];
        int i = 0;
        NamingEnumeration localNamingEnumeration = paramAttribute.getAll();
        while (localNamingEnumeration.hasMore())
        {
          Object localObject2 = localNamingEnumeration.next();
          if ((LDAPCertStore.access$700() != null) && (localObject2 instanceof String))
            LDAPCertStore.access$700().println("LDAPCertStore.getAttrValues() enum.next is a string!: " + localObject2);
          byte[] arrayOfByte = (byte[])(byte[])localObject2;
          localObject1[(i++)] = arrayOfByte;
        }
      }
      return ((B)localObject1);
    }
  }

  private static class SunLDAPCertStoreParameters extends LDAPCertStoreParameters
  {
    private volatile int hashCode = 0;

    SunLDAPCertStoreParameters(String paramString, int paramInt)
    {
      super(paramString, paramInt);
    }

    SunLDAPCertStoreParameters(String paramString)
    {
      super(paramString);
    }

    SunLDAPCertStoreParameters()
    {
    }

    public boolean equals(Object paramObject)
    {
      if (!(paramObject instanceof LDAPCertStoreParameters))
        return false;
      LDAPCertStoreParameters localLDAPCertStoreParameters = (LDAPCertStoreParameters)paramObject;
      return ((getPort() == localLDAPCertStoreParameters.getPort()) && (getServerName().equalsIgnoreCase(localLDAPCertStoreParameters.getServerName())));
    }

    public int hashCode()
    {
      if (this.hashCode == 0)
      {
        int i = 17;
        i = 37 * i + getPort();
        i = 37 * i + getServerName().toLowerCase().hashCode();
        this.hashCode = i;
      }
      return this.hashCode;
    }
  }
}