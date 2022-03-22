package sun.security.x509;

import B;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CRLException;
import java.security.cert.Certificate;
import java.security.cert.X509CRL;
import java.security.cert.X509CRLEntry;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import javax.security.auth.x500.X500Principal;
import sun.misc.HexDumpEncoder;
import sun.security.provider.X509Factory;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;

public class X509CRLImpl extends X509CRL
{
  private byte[] signedCRL;
  private byte[] signature;
  private byte[] tbsCertList;
  private AlgorithmId sigAlgId;
  private int version;
  private AlgorithmId infoSigAlgId;
  private X500Name issuer;
  private X500Principal issuerPrincipal;
  private Date thisUpdate;
  private Date nextUpdate;
  private Map<X509IssuerSerial, X509CRLEntry> revokedCerts;
  private CRLExtensions extensions;
  private static final boolean isExplicit = 1;
  private static final long YR_2050 = 2524636800000L;
  private boolean readOnly;
  private PublicKey verifiedPublicKey;
  private String verifiedProvider;

  private X509CRLImpl()
  {
    this.signedCRL = null;
    this.signature = null;
    this.tbsCertList = null;
    this.sigAlgId = null;
    this.issuer = null;
    this.issuerPrincipal = null;
    this.thisUpdate = null;
    this.nextUpdate = null;
    this.revokedCerts = new LinkedHashMap();
    this.extensions = null;
    this.readOnly = false;
  }

  public X509CRLImpl(byte[] paramArrayOfByte)
    throws CRLException
  {
    this.signedCRL = null;
    this.signature = null;
    this.tbsCertList = null;
    this.sigAlgId = null;
    this.issuer = null;
    this.issuerPrincipal = null;
    this.thisUpdate = null;
    this.nextUpdate = null;
    this.revokedCerts = new LinkedHashMap();
    this.extensions = null;
    this.readOnly = false;
    try
    {
      parse(new DerValue(paramArrayOfByte));
    }
    catch (IOException localIOException)
    {
      this.signedCRL = null;
      throw new CRLException("Parsing error: " + localIOException.getMessage());
    }
  }

  public X509CRLImpl(DerValue paramDerValue)
    throws CRLException
  {
    this.signedCRL = null;
    this.signature = null;
    this.tbsCertList = null;
    this.sigAlgId = null;
    this.issuer = null;
    this.issuerPrincipal = null;
    this.thisUpdate = null;
    this.nextUpdate = null;
    this.revokedCerts = new LinkedHashMap();
    this.extensions = null;
    this.readOnly = false;
    try
    {
      parse(paramDerValue);
    }
    catch (IOException localIOException)
    {
      this.signedCRL = null;
      throw new CRLException("Parsing error: " + localIOException.getMessage());
    }
  }

  public X509CRLImpl(InputStream paramInputStream)
    throws CRLException
  {
    this.signedCRL = null;
    this.signature = null;
    this.tbsCertList = null;
    this.sigAlgId = null;
    this.issuer = null;
    this.issuerPrincipal = null;
    this.thisUpdate = null;
    this.nextUpdate = null;
    this.revokedCerts = new LinkedHashMap();
    this.extensions = null;
    this.readOnly = false;
    try
    {
      parse(new DerValue(paramInputStream));
    }
    catch (IOException localIOException)
    {
      this.signedCRL = null;
      throw new CRLException("Parsing error: " + localIOException.getMessage());
    }
  }

  public X509CRLImpl(X500Name paramX500Name, Date paramDate1, Date paramDate2)
  {
    this.signedCRL = null;
    this.signature = null;
    this.tbsCertList = null;
    this.sigAlgId = null;
    this.issuer = null;
    this.issuerPrincipal = null;
    this.thisUpdate = null;
    this.nextUpdate = null;
    this.revokedCerts = new LinkedHashMap();
    this.extensions = null;
    this.readOnly = false;
    this.issuer = paramX500Name;
    this.thisUpdate = paramDate1;
    this.nextUpdate = paramDate2;
  }

  public X509CRLImpl(X500Name paramX500Name, Date paramDate1, Date paramDate2, X509CRLEntry[] paramArrayOfX509CRLEntry)
    throws CRLException
  {
    this.signedCRL = null;
    this.signature = null;
    this.tbsCertList = null;
    this.sigAlgId = null;
    this.issuer = null;
    this.issuerPrincipal = null;
    this.thisUpdate = null;
    this.nextUpdate = null;
    this.revokedCerts = new LinkedHashMap();
    this.extensions = null;
    this.readOnly = false;
    this.issuer = paramX500Name;
    this.thisUpdate = paramDate1;
    this.nextUpdate = paramDate2;
    if (paramArrayOfX509CRLEntry != null)
    {
      X500Principal localX500Principal1 = getIssuerX500Principal();
      X500Principal localX500Principal2 = localX500Principal1;
      for (int i = 0; i < paramArrayOfX509CRLEntry.length; ++i)
      {
        X509CRLEntryImpl localX509CRLEntryImpl = (X509CRLEntryImpl)paramArrayOfX509CRLEntry[i];
        try
        {
          localX500Principal2 = getCertIssuer(localX509CRLEntryImpl, localX500Principal2);
        }
        catch (IOException localIOException)
        {
          throw new CRLException(localIOException);
        }
        localX509CRLEntryImpl.setCertificateIssuer(localX500Principal1, localX500Principal2);
        X509IssuerSerial localX509IssuerSerial = new X509IssuerSerial(localX500Principal2, localX509CRLEntryImpl.getSerialNumber());
        this.revokedCerts.put(localX509IssuerSerial, localX509CRLEntryImpl);
        if (localX509CRLEntryImpl.hasExtensions())
          this.version = 1;
      }
    }
  }

  public X509CRLImpl(X500Name paramX500Name, Date paramDate1, Date paramDate2, X509CRLEntry[] paramArrayOfX509CRLEntry, CRLExtensions paramCRLExtensions)
    throws CRLException
  {
    this(paramX500Name, paramDate1, paramDate2, paramArrayOfX509CRLEntry);
    if (paramCRLExtensions != null)
    {
      this.extensions = paramCRLExtensions;
      this.version = 1;
    }
  }

  public byte[] getEncodedInternal()
    throws CRLException
  {
    if (this.signedCRL == null)
      throw new CRLException("Null CRL to encode");
    return this.signedCRL;
  }

  public byte[] getEncoded()
    throws CRLException
  {
    return ((byte[])(byte[])getEncodedInternal().clone());
  }

  public void encodeInfo(OutputStream paramOutputStream)
    throws CRLException
  {
    DerOutputStream localDerOutputStream1;
    try
    {
      localDerOutputStream1 = new DerOutputStream();
      DerOutputStream localDerOutputStream2 = new DerOutputStream();
      DerOutputStream localDerOutputStream3 = new DerOutputStream();
      if (this.version != 0)
        localDerOutputStream1.putInteger(this.version);
      this.infoSigAlgId.encode(localDerOutputStream1);
      if ((this.version == 0) && (this.issuer.toString() == null))
        throw new CRLException("Null Issuer DN not allowed in v1 CRL");
      this.issuer.encode(localDerOutputStream1);
      if (this.thisUpdate.getTime() < 2524636800000L)
        localDerOutputStream1.putUTCTime(this.thisUpdate);
      else
        localDerOutputStream1.putGeneralizedTime(this.thisUpdate);
      if (this.nextUpdate != null)
        if (this.nextUpdate.getTime() < 2524636800000L)
          localDerOutputStream1.putUTCTime(this.nextUpdate);
        else
          localDerOutputStream1.putGeneralizedTime(this.nextUpdate);
      if (!(this.revokedCerts.isEmpty()))
      {
        Iterator localIterator = this.revokedCerts.values().iterator();
        while (localIterator.hasNext())
          ((X509CRLEntryImpl)localIterator.next()).encode(localDerOutputStream2);
        localDerOutputStream1.write(48, localDerOutputStream2);
      }
      if (this.extensions != null)
        this.extensions.encode(localDerOutputStream1, true);
      localDerOutputStream3.write(48, localDerOutputStream1);
      this.tbsCertList = localDerOutputStream3.toByteArray();
      paramOutputStream.write(this.tbsCertList);
    }
    catch (IOException localIOException)
    {
      throw new CRLException("Encoding error: " + localIOException.getMessage());
    }
  }

  public void verify(PublicKey paramPublicKey)
    throws CRLException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException
  {
    verify(paramPublicKey, "");
  }

  public synchronized void verify(PublicKey paramPublicKey, String paramString)
    throws CRLException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException
  {
    if (paramString == null)
      paramString = "";
    if ((this.verifiedPublicKey != null) && (this.verifiedPublicKey.equals(paramPublicKey)) && (paramString.equals(this.verifiedProvider)))
      return;
    if (this.signedCRL == null)
      throw new CRLException("Uninitialized CRL");
    Signature localSignature = null;
    if (paramString.length() == 0)
      localSignature = Signature.getInstance(this.sigAlgId.getName());
    else
      localSignature = Signature.getInstance(this.sigAlgId.getName(), paramString);
    localSignature.initVerify(paramPublicKey);
    if (this.tbsCertList == null)
      throw new CRLException("Uninitialized CRL");
    localSignature.update(this.tbsCertList, 0, this.tbsCertList.length);
    if (!(localSignature.verify(this.signature)))
      throw new SignatureException("Signature does not match.");
    this.verifiedPublicKey = paramPublicKey;
    this.verifiedProvider = paramString;
  }

  public void sign(PrivateKey paramPrivateKey, String paramString)
    throws CRLException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException
  {
    sign(paramPrivateKey, paramString, null);
  }

  public void sign(PrivateKey paramPrivateKey, String paramString1, String paramString2)
    throws CRLException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException
  {
    try
    {
      if (this.readOnly)
        throw new CRLException("cannot over-write existing CRL");
      Signature localSignature = null;
      if ((paramString2 == null) || (paramString2.length() == 0))
        localSignature = Signature.getInstance(paramString1);
      else
        localSignature = Signature.getInstance(paramString1, paramString2);
      localSignature.initSign(paramPrivateKey);
      this.sigAlgId = AlgorithmId.get(localSignature.getAlgorithm());
      this.infoSigAlgId = this.sigAlgId;
      DerOutputStream localDerOutputStream1 = new DerOutputStream();
      DerOutputStream localDerOutputStream2 = new DerOutputStream();
      encodeInfo(localDerOutputStream2);
      this.sigAlgId.encode(localDerOutputStream2);
      localSignature.update(this.tbsCertList, 0, this.tbsCertList.length);
      this.signature = localSignature.sign();
      localDerOutputStream2.putBitString(this.signature);
      localDerOutputStream1.write(48, localDerOutputStream2);
      this.signedCRL = localDerOutputStream1.toByteArray();
      this.readOnly = true;
    }
    catch (IOException localIOException)
    {
      throw new CRLException("Error while encoding data: " + localIOException.getMessage());
    }
  }

  public String toString()
  {
    Object localObject1;
    Object localObject2;
    StringBuffer localStringBuffer = new StringBuffer();
    localStringBuffer.append("X.509 CRL v" + (this.version + 1) + "\n");
    if (this.sigAlgId != null)
      localStringBuffer.append("Signature Algorithm: " + this.sigAlgId.toString() + ", OID=" + this.sigAlgId.getOID().toString() + "\n");
    if (this.issuer != null)
      localStringBuffer.append("Issuer: " + this.issuer.toString() + "\n");
    if (this.thisUpdate != null)
      localStringBuffer.append("\nThis Update: " + this.thisUpdate.toString() + "\n");
    if (this.nextUpdate != null)
      localStringBuffer.append("Next Update: " + this.nextUpdate.toString() + "\n");
    if (this.revokedCerts.isEmpty())
    {
      localStringBuffer.append("\nNO certificates have been revoked\n");
    }
    else
    {
      localStringBuffer.append("\nRevoked Certificates: " + this.revokedCerts.size());
      int i = 1;
      localObject2 = this.revokedCerts.values().iterator();
      while (((Iterator)localObject2).hasNext())
      {
        localStringBuffer.append("\n[" + i + "] " + ((X509CRLEntry)((Iterator)localObject2).next()).toString());
        ++i;
      }
    }
    if (this.extensions != null)
    {
      localObject1 = this.extensions.getAllExtensions();
      localObject2 = ((Collection)localObject1).toArray();
      localStringBuffer.append("\nCRL Extensions: " + localObject2.length);
      for (int j = 0; j < localObject2.length; ++j)
      {
        localStringBuffer.append("\n[" + (j + 1) + "]: ");
        Extension localExtension = (Extension)localObject2[j];
        try
        {
          if (OIDMap.getClass(localExtension.getExtensionId()) == null)
          {
            localStringBuffer.append(localExtension.toString());
            byte[] arrayOfByte = localExtension.getExtensionValue();
            if (arrayOfByte != null)
            {
              DerOutputStream localDerOutputStream = new DerOutputStream();
              localDerOutputStream.putOctetString(arrayOfByte);
              arrayOfByte = localDerOutputStream.toByteArray();
              HexDumpEncoder localHexDumpEncoder = new HexDumpEncoder();
              localStringBuffer.append("Extension unknown: DER encoded OCTET string =\n" + localHexDumpEncoder.encodeBuffer(arrayOfByte) + "\n");
            }
          }
          else
          {
            localStringBuffer.append(localExtension.toString());
          }
        }
        catch (Exception localException)
        {
          localStringBuffer.append(", Error parsing this extension");
        }
      }
    }
    if (this.signature != null)
    {
      localObject1 = new HexDumpEncoder();
      localStringBuffer.append("\nSignature:\n" + ((HexDumpEncoder)localObject1).encodeBuffer(this.signature) + "\n");
    }
    else
    {
      localStringBuffer.append("NOT signed yet\n");
    }
    return ((String)(String)localStringBuffer.toString());
  }

  public boolean isRevoked(Certificate paramCertificate)
  {
    if ((this.revokedCerts.isEmpty()) || (!(paramCertificate instanceof X509Certificate)))
      return false;
    X509Certificate localX509Certificate = (X509Certificate)paramCertificate;
    X509IssuerSerial localX509IssuerSerial = new X509IssuerSerial(localX509Certificate);
    return this.revokedCerts.containsKey(localX509IssuerSerial);
  }

  public int getVersion()
  {
    return (this.version + 1);
  }

  public Principal getIssuerDN()
  {
    return this.issuer;
  }

  public X500Principal getIssuerX500Principal()
  {
    if (this.issuerPrincipal == null)
      this.issuerPrincipal = this.issuer.asX500Principal();
    return this.issuerPrincipal;
  }

  public Date getThisUpdate()
  {
    return new Date(this.thisUpdate.getTime());
  }

  public Date getNextUpdate()
  {
    if (this.nextUpdate == null)
      return null;
    return new Date(this.nextUpdate.getTime());
  }

  public X509CRLEntry getRevokedCertificate(BigInteger paramBigInteger)
  {
    if (this.revokedCerts.isEmpty())
      return null;
    X509IssuerSerial localX509IssuerSerial = new X509IssuerSerial(getIssuerX500Principal(), paramBigInteger);
    return ((X509CRLEntry)this.revokedCerts.get(localX509IssuerSerial));
  }

  public X509CRLEntry getRevokedCertificate(X509Certificate paramX509Certificate)
  {
    if (this.revokedCerts.isEmpty())
      return null;
    X509IssuerSerial localX509IssuerSerial = new X509IssuerSerial(paramX509Certificate);
    return ((X509CRLEntry)this.revokedCerts.get(localX509IssuerSerial));
  }

  public Set<X509CRLEntry> getRevokedCertificates()
  {
    if (this.revokedCerts.isEmpty())
      return null;
    return new HashSet(this.revokedCerts.values());
  }

  public byte[] getTBSCertList()
    throws CRLException
  {
    if (this.tbsCertList == null)
      throw new CRLException("Uninitialized CRL");
    byte[] arrayOfByte = new byte[this.tbsCertList.length];
    System.arraycopy(this.tbsCertList, 0, arrayOfByte, 0, arrayOfByte.length);
    return arrayOfByte;
  }

  public byte[] getSignature()
  {
    if (this.signature == null)
      return null;
    byte[] arrayOfByte = new byte[this.signature.length];
    System.arraycopy(this.signature, 0, arrayOfByte, 0, arrayOfByte.length);
    return arrayOfByte;
  }

  public String getSigAlgName()
  {
    if (this.sigAlgId == null)
      return null;
    return this.sigAlgId.getName();
  }

  public String getSigAlgOID()
  {
    if (this.sigAlgId == null)
      return null;
    ObjectIdentifier localObjectIdentifier = this.sigAlgId.getOID();
    return localObjectIdentifier.toString();
  }

  public byte[] getSigAlgParams()
  {
    if (this.sigAlgId == null)
      return null;
    try
    {
      return this.sigAlgId.getEncodedParams();
    }
    catch (IOException localIOException)
    {
    }
    return null;
  }

  public KeyIdentifier getAuthKeyId()
    throws IOException
  {
    AuthorityKeyIdentifierExtension localAuthorityKeyIdentifierExtension = getAuthKeyIdExtension();
    if (localAuthorityKeyIdentifierExtension != null)
    {
      KeyIdentifier localKeyIdentifier = (KeyIdentifier)localAuthorityKeyIdentifierExtension.get("key_id");
      return localKeyIdentifier;
    }
    return null;
  }

  public AuthorityKeyIdentifierExtension getAuthKeyIdExtension()
    throws IOException
  {
    Object localObject = getExtension(PKIXExtensions.AuthorityKey_Id);
    return ((AuthorityKeyIdentifierExtension)localObject);
  }

  public CRLNumberExtension getCRLNumberExtension()
    throws IOException
  {
    Object localObject = getExtension(PKIXExtensions.CRLNumber_Id);
    return ((CRLNumberExtension)localObject);
  }

  public BigInteger getCRLNumber()
    throws IOException
  {
    CRLNumberExtension localCRLNumberExtension = getCRLNumberExtension();
    if (localCRLNumberExtension != null)
    {
      BigInteger localBigInteger = (BigInteger)localCRLNumberExtension.get("value");
      return localBigInteger;
    }
    return null;
  }

  public DeltaCRLIndicatorExtension getDeltaCRLIndicatorExtension()
    throws IOException
  {
    Object localObject = getExtension(PKIXExtensions.DeltaCRLIndicator_Id);
    return ((DeltaCRLIndicatorExtension)localObject);
  }

  public BigInteger getBaseCRLNumber()
    throws IOException
  {
    DeltaCRLIndicatorExtension localDeltaCRLIndicatorExtension = getDeltaCRLIndicatorExtension();
    if (localDeltaCRLIndicatorExtension != null)
    {
      BigInteger localBigInteger = (BigInteger)localDeltaCRLIndicatorExtension.get("value");
      return localBigInteger;
    }
    return null;
  }

  public IssuerAlternativeNameExtension getIssuerAltNameExtension()
    throws IOException
  {
    Object localObject = getExtension(PKIXExtensions.IssuerAlternativeName_Id);
    return ((IssuerAlternativeNameExtension)localObject);
  }

  public IssuingDistributionPointExtension getIssuingDistributionPointExtension()
    throws IOException
  {
    Object localObject = getExtension(PKIXExtensions.IssuingDistributionPoint_Id);
    return ((IssuingDistributionPointExtension)localObject);
  }

  public boolean hasUnsupportedCriticalExtension()
  {
    if (this.extensions == null)
      return false;
    return this.extensions.hasUnsupportedCriticalExtension();
  }

  public Set<String> getCriticalExtensionOIDs()
  {
    if (this.extensions == null)
      return null;
    HashSet localHashSet = new HashSet();
    Iterator localIterator = this.extensions.getAllExtensions().iterator();
    while (localIterator.hasNext())
    {
      Extension localExtension = (Extension)localIterator.next();
      if (localExtension.isCritical())
        localHashSet.add(localExtension.getExtensionId().toString());
    }
    return localHashSet;
  }

  public Set<String> getNonCriticalExtensionOIDs()
  {
    if (this.extensions == null)
      return null;
    HashSet localHashSet = new HashSet();
    Iterator localIterator = this.extensions.getAllExtensions().iterator();
    while (localIterator.hasNext())
    {
      Extension localExtension = (Extension)localIterator.next();
      if (!(localExtension.isCritical()))
        localHashSet.add(localExtension.getExtensionId().toString());
    }
    return localHashSet;
  }

  public byte[] getExtensionValue(String paramString)
  {
    if (this.extensions == null)
      return null;
    try
    {
      String str = OIDMap.getName(new ObjectIdentifier(paramString));
      Object localObject1 = null;
      if (str == null)
      {
        ObjectIdentifier localObjectIdentifier;
        localObject2 = new ObjectIdentifier(paramString);
        localObject3 = null;
        Enumeration localEnumeration = this.extensions.getElements();
        do
        {
          if (!(localEnumeration.hasMoreElements()))
            break label94;
          localObject3 = (Extension)localEnumeration.nextElement();
          localObjectIdentifier = ((Extension)localObject3).getExtensionId();
        }
        while (!(localObjectIdentifier.equals((ObjectIdentifier)localObject2)));
        label94: localObject1 = localObject3;
      }
      else
      {
        localObject1 = this.extensions.get(str);
      }
      if (localObject1 == null)
        return null;
      Object localObject2 = ((Extension)localObject1).getExtensionValue();
      if (localObject2 == null)
        return null;
      Object localObject3 = new DerOutputStream();
      ((DerOutputStream)localObject3).putOctetString(localObject2);
      return ((DerOutputStream)localObject3).toByteArray();
    }
    catch (Exception localException)
    {
    }
    return ((B)(B)(B)null);
  }

  public Object getExtension(ObjectIdentifier paramObjectIdentifier)
  {
    if (this.extensions == null)
      return null;
    return this.extensions.get(OIDMap.getName(paramObjectIdentifier));
  }

  private void parse(DerValue paramDerValue)
    throws CRLException, IOException
  {
    if (this.readOnly)
      throw new CRLException("cannot over-write existing CRL");
    if ((paramDerValue.getData() == null) || (paramDerValue.tag != 48))
      throw new CRLException("Invalid DER-encoded CRL data");
    this.signedCRL = paramDerValue.toByteArray();
    DerValue[] arrayOfDerValue1 = new DerValue[3];
    arrayOfDerValue1[0] = paramDerValue.data.getDerValue();
    arrayOfDerValue1[1] = paramDerValue.data.getDerValue();
    arrayOfDerValue1[2] = paramDerValue.data.getDerValue();
    if (paramDerValue.data.available() != 0)
      throw new CRLException("signed overrun, bytes = " + paramDerValue.data.available());
    if (arrayOfDerValue1[0].tag != 48)
      throw new CRLException("signed CRL fields invalid");
    this.sigAlgId = AlgorithmId.parse(arrayOfDerValue1[1]);
    this.signature = arrayOfDerValue1[2].getBitString();
    if (arrayOfDerValue1[1].data.available() != 0)
      throw new CRLException("AlgorithmId field overrun");
    if (arrayOfDerValue1[2].data.available() != 0)
      throw new CRLException("Signature field overrun");
    this.tbsCertList = arrayOfDerValue1[0].toByteArray();
    DerInputStream localDerInputStream = arrayOfDerValue1[0].data;
    this.version = 0;
    int i = (byte)localDerInputStream.peekByte();
    if (i == 2)
    {
      this.version = localDerInputStream.getInteger();
      if (this.version != 1)
        throw new CRLException("Invalid version");
    }
    DerValue localDerValue = localDerInputStream.getDerValue();
    AlgorithmId localAlgorithmId = AlgorithmId.parse(localDerValue);
    if (!(localAlgorithmId.equals(this.sigAlgId)))
      throw new CRLException("Signature algorithm mismatch");
    this.infoSigAlgId = localAlgorithmId;
    this.issuer = new X500Name(localDerInputStream);
    String str = this.issuer.toString();
    if ((((str == null) || (str.length() == 0))) && (this.version == 0))
      throw new CRLException("Null Issuer DN allowed only in v2 CRL");
    i = (byte)localDerInputStream.peekByte();
    if (i == 23)
      this.thisUpdate = localDerInputStream.getUTCTime();
    else if (i == 24)
      this.thisUpdate = localDerInputStream.getGeneralizedTime();
    else
      throw new CRLException("Invalid encoding for thisUpdate (tag=" + i + ")");
    if (localDerInputStream.available() == 0)
      return;
    i = (byte)localDerInputStream.peekByte();
    if (i == 23)
      this.nextUpdate = localDerInputStream.getUTCTime();
    else if (i == 24)
      this.nextUpdate = localDerInputStream.getGeneralizedTime();
    if (localDerInputStream.available() == 0)
      return;
    i = (byte)localDerInputStream.peekByte();
    if ((i == 48) && ((i & 0xC0) != 128))
    {
      DerValue[] arrayOfDerValue2 = localDerInputStream.getSequence(4);
      X500Principal localX500Principal1 = getIssuerX500Principal();
      X500Principal localX500Principal2 = localX500Principal1;
      for (int j = 0; j < arrayOfDerValue2.length; ++j)
      {
        X509CRLEntryImpl localX509CRLEntryImpl = new X509CRLEntryImpl(arrayOfDerValue2[j]);
        localX500Principal2 = getCertIssuer(localX509CRLEntryImpl, localX500Principal2);
        localX509CRLEntryImpl.setCertificateIssuer(localX500Principal1, localX500Principal2);
        X509IssuerSerial localX509IssuerSerial = new X509IssuerSerial(localX500Principal2, localX509CRLEntryImpl.getSerialNumber());
        this.revokedCerts.put(localX509IssuerSerial, localX509CRLEntryImpl);
      }
    }
    if (localDerInputStream.available() == 0)
      return;
    localDerValue = localDerInputStream.getDerValue();
    if ((localDerValue.isConstructed()) && (localDerValue.isContextSpecific(0)))
      this.extensions = new CRLExtensions(localDerValue.data);
    this.readOnly = true;
  }

  public static X500Principal getIssuerX500Principal(X509CRL paramX509CRL)
  {
    byte[] arrayOfByte1;
    try
    {
      arrayOfByte1 = paramX509CRL.getEncoded();
      DerInputStream localDerInputStream1 = new DerInputStream(arrayOfByte1);
      DerValue localDerValue1 = localDerInputStream1.getSequence(3)[0];
      DerInputStream localDerInputStream2 = localDerValue1.data;
      int i = (byte)localDerInputStream2.peekByte();
      if (i == 2)
        localDerValue2 = localDerInputStream2.getDerValue();
      DerValue localDerValue2 = localDerInputStream2.getDerValue();
      localDerValue2 = localDerInputStream2.getDerValue();
      byte[] arrayOfByte2 = localDerValue2.toByteArray();
      return new X500Principal(arrayOfByte2);
    }
    catch (Exception localException)
    {
      throw new RuntimeException("Could not parse issuer", localException);
    }
  }

  public static byte[] getEncodedInternal(X509CRL paramX509CRL)
    throws CRLException
  {
    if (paramX509CRL instanceof X509CRLImpl)
      return ((X509CRLImpl)paramX509CRL).getEncodedInternal();
    return paramX509CRL.getEncoded();
  }

  public static X509CRLImpl toImpl(X509CRL paramX509CRL)
    throws CRLException
  {
    if (paramX509CRL instanceof X509CRLImpl)
      return ((X509CRLImpl)paramX509CRL);
    return X509Factory.intern(paramX509CRL);
  }

  private X500Principal getCertIssuer(X509CRLEntryImpl paramX509CRLEntryImpl, X500Principal paramX500Principal)
    throws IOException
  {
    CertificateIssuerExtension localCertificateIssuerExtension = paramX509CRLEntryImpl.getCertificateIssuerExtension();
    if (localCertificateIssuerExtension != null)
    {
      GeneralNames localGeneralNames = (GeneralNames)localCertificateIssuerExtension.get("issuer");
      X500Name localX500Name = (X500Name)localGeneralNames.get(0).getName();
      return localX500Name.asX500Principal();
    }
    return paramX500Principal;
  }

  private static final class X509IssuerSerial
  {
    final X500Principal issuer;
    final BigInteger serial;
    volatile int hashcode;

    X509IssuerSerial(X500Principal paramX500Principal, BigInteger paramBigInteger)
    {
      this.hashcode = 0;
      this.issuer = paramX500Principal;
      this.serial = paramBigInteger;
    }

    X509IssuerSerial(X509Certificate paramX509Certificate)
    {
      this(paramX509Certificate.getIssuerX500Principal(), paramX509Certificate.getSerialNumber());
    }

    X500Principal getIssuer()
    {
      return this.issuer;
    }

    BigInteger getSerial()
    {
      return this.serial;
    }

    public boolean equals(Object paramObject)
    {
      if (paramObject == this)
        return true;
      if (!(paramObject instanceof X509IssuerSerial))
        return false;
      X509IssuerSerial localX509IssuerSerial = (X509IssuerSerial)paramObject;
      return ((this.serial.equals(localX509IssuerSerial.getSerial())) && (this.issuer.equals(localX509IssuerSerial.getIssuer())));
    }

    public int hashCode()
    {
      if (this.hashcode == 0)
      {
        int i = 17;
        i = 37 * i + this.issuer.hashCode();
        i = 37 * i + this.serial.hashCode();
        this.hashcode = i;
      }
      return this.hashcode;
    }
  }
}