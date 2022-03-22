package sun.security.x509;

import B;
import Z;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collection<Ljava.util.List<*>;>;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.security.auth.x500.X500Principal;
import sun.misc.BASE64Decoder;
import sun.misc.HexDumpEncoder;
import sun.security.provider.X509Factory;
import sun.security.util.DerEncoder;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;

public class X509CertImpl extends X509Certificate
  implements DerEncoder
{
  private static final long serialVersionUID = -3457612960190864406L;
  private static final String DOT = ".";
  public static final String NAME = "x509";
  public static final String INFO = "info";
  public static final String ALG_ID = "algorithm";
  public static final String SIGNATURE = "signature";
  public static final String SIGNED_CERT = "signed_cert";
  public static final String SUBJECT_DN = "x509.info.subject.dname";
  public static final String ISSUER_DN = "x509.info.issuer.dname";
  public static final String SERIAL_ID = "x509.info.serialNumber.number";
  public static final String PUBLIC_KEY = "x509.info.key.value";
  public static final String VERSION = "x509.info.version.number";
  public static final String SIG_ALG = "x509.algorithm";
  public static final String SIG = "x509.signature";
  private boolean readOnly = false;
  private byte[] signedCert = null;
  protected X509CertInfo info = null;
  protected AlgorithmId algId = null;
  protected byte[] signature = null;
  private static final String KEY_USAGE_OID = "2.5.29.15";
  private static final String EXTENDED_KEY_USAGE_OID = "2.5.29.37";
  private static final String BASIC_CONSTRAINT_OID = "2.5.29.19";
  private static final String SUBJECT_ALT_NAME_OID = "2.5.29.17";
  private static final String ISSUER_ALT_NAME_OID = "2.5.29.18";
  private static final String AUTH_INFO_ACCESS_OID = "1.3.6.1.5.5.7.1.1";
  private static final int NUM_STANDARD_KEY_USAGE = 9;
  private Collection<List<?>> subjectAlternativeNames;
  private Collection<List<?>> issuerAlternativeNames;
  private List<String> extKeyUsage;
  private Set<AccessDescription> authInfoAccess;
  private PublicKey verifiedPublicKey;
  private String verifiedProvider;
  private boolean verificationResult;

  public X509CertImpl()
  {
  }

  public X509CertImpl(byte[] paramArrayOfByte)
    throws CertificateException
  {
    try
    {
      parse(new DerValue(paramArrayOfByte));
    }
    catch (IOException localIOException)
    {
      this.signedCert = null;
      CertificateException localCertificateException = new CertificateException("Unable to initialize, " + localIOException);
      localCertificateException.initCause(localIOException);
      throw localCertificateException;
    }
  }

  public X509CertImpl(InputStream paramInputStream)
    throws CertificateException
  {
    DerValue localDerValue = null;
    BufferedInputStream localBufferedInputStream = new BufferedInputStream(paramInputStream);
    try
    {
      localBufferedInputStream.mark(2147483647);
      localDerValue = readRFC1421Cert(localBufferedInputStream);
    }
    catch (IOException localIOException1)
    {
      try
      {
        localBufferedInputStream.reset();
        localDerValue = new DerValue(localBufferedInputStream);
      }
      catch (IOException localIOException3)
      {
        CertificateException localCertificateException2 = new CertificateException("Input stream must be either DER-encoded bytes or RFC1421 hex-encoded DER-encoded bytes: " + localIOException3.getMessage());
        localCertificateException2.initCause(localIOException3);
        throw localCertificateException2;
      }
    }
    try
    {
      parse(localDerValue);
    }
    catch (IOException localIOException2)
    {
      this.signedCert = null;
      CertificateException localCertificateException1 = new CertificateException("Unable to parse DER value of certificate, " + localIOException2);
      localCertificateException1.initCause(localIOException2);
      throw localCertificateException1;
    }
  }

  private DerValue readRFC1421Cert(InputStream paramInputStream)
    throws IOException
  {
    DerValue localDerValue = null;
    String str = null;
    BufferedReader localBufferedReader = new BufferedReader(new InputStreamReader(paramInputStream, "ASCII"));
    try
    {
      str = localBufferedReader.readLine();
    }
    catch (IOException localIOException1)
    {
      throw new IOException("Unable to read InputStream: " + localIOException1.getMessage());
    }
    if (str.equals("-----BEGIN CERTIFICATE-----"))
    {
      BASE64Decoder localBASE64Decoder = new BASE64Decoder();
      ByteArrayOutputStream localByteArrayOutputStream = new ByteArrayOutputStream();
      try
      {
        while ((str = localBufferedReader.readLine()) != null)
        {
          if (str.equals("-----END CERTIFICATE-----"))
          {
            localDerValue = new DerValue(localByteArrayOutputStream.toByteArray());
            break;
          }
          localByteArrayOutputStream.write(localBASE64Decoder.decodeBuffer(str));
        }
      }
      catch (IOException localIOException2)
      {
        throw new IOException("Unable to read InputStream: " + localIOException2.getMessage());
      }
    }
    else
    {
      throw new IOException("InputStream is not RFC1421 hex-encoded DER bytes");
    }
    return localDerValue;
  }

  public X509CertImpl(X509CertInfo paramX509CertInfo)
  {
    this.info = paramX509CertInfo;
  }

  public X509CertImpl(DerValue paramDerValue)
    throws CertificateException
  {
    try
    {
      parse(paramDerValue);
    }
    catch (IOException localIOException)
    {
      this.signedCert = null;
      CertificateException localCertificateException = new CertificateException("Unable to initialize, " + localIOException);
      localCertificateException.initCause(localIOException);
      throw localCertificateException;
    }
  }

  public void encode(OutputStream paramOutputStream)
    throws CertificateEncodingException
  {
    if (this.signedCert == null)
      throw new CertificateEncodingException("Null certificate to encode");
    try
    {
      paramOutputStream.write((byte[])(byte[])this.signedCert.clone());
    }
    catch (IOException localIOException)
    {
      throw new CertificateEncodingException(localIOException.toString());
    }
  }

  public void derEncode(OutputStream paramOutputStream)
    throws IOException
  {
    if (this.signedCert == null)
      throw new IOException("Null certificate to encode");
    paramOutputStream.write((byte[])(byte[])this.signedCert.clone());
  }

  public byte[] getEncoded()
    throws CertificateEncodingException
  {
    return ((byte[])(byte[])getEncodedInternal().clone());
  }

  public byte[] getEncodedInternal()
    throws CertificateEncodingException
  {
    if (this.signedCert == null)
      throw new CertificateEncodingException("Null certificate to encode");
    return this.signedCert;
  }

  public void verify(PublicKey paramPublicKey)
    throws CertificateException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException
  {
    verify(paramPublicKey, "");
  }

  public synchronized void verify(PublicKey paramPublicKey, String paramString)
    throws CertificateException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException
  {
    if (paramString == null)
      paramString = "";
    if ((this.verifiedPublicKey != null) && (this.verifiedPublicKey.equals(paramPublicKey)) && (paramString.equals(this.verifiedProvider)))
    {
      if (this.verificationResult)
        return;
      throw new SignatureException("Signature does not match.");
    }
    if (this.signedCert == null)
      throw new CertificateEncodingException("Uninitialized certificate");
    Signature localSignature = null;
    if (paramString.length() == 0)
      localSignature = Signature.getInstance(this.algId.getName());
    else
      localSignature = Signature.getInstance(this.algId.getName(), paramString);
    localSignature.initVerify(paramPublicKey);
    byte[] arrayOfByte = this.info.getEncodedInfo();
    localSignature.update(arrayOfByte, 0, arrayOfByte.length);
    this.verificationResult = localSignature.verify(this.signature);
    this.verifiedPublicKey = paramPublicKey;
    this.verifiedProvider = paramString;
    if (!(this.verificationResult))
      throw new SignatureException("Signature does not match.");
  }

  public void sign(PrivateKey paramPrivateKey, String paramString)
    throws CertificateException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException
  {
    sign(paramPrivateKey, paramString, null);
  }

  public void sign(PrivateKey paramPrivateKey, String paramString1, String paramString2)
    throws CertificateException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException
  {
    try
    {
      if (this.readOnly)
        throw new CertificateEncodingException("cannot over-write existing certificate");
      Signature localSignature = null;
      if ((paramString2 == null) || (paramString2.length() == 0))
        localSignature = Signature.getInstance(paramString1);
      else
        localSignature = Signature.getInstance(paramString1, paramString2);
      localSignature.initSign(paramPrivateKey);
      this.algId = AlgorithmId.get(localSignature.getAlgorithm());
      DerOutputStream localDerOutputStream1 = new DerOutputStream();
      DerOutputStream localDerOutputStream2 = new DerOutputStream();
      this.info.encode(localDerOutputStream2);
      byte[] arrayOfByte = localDerOutputStream2.toByteArray();
      this.algId.encode(localDerOutputStream2);
      localSignature.update(arrayOfByte, 0, arrayOfByte.length);
      this.signature = localSignature.sign();
      localDerOutputStream2.putBitString(this.signature);
      localDerOutputStream1.write(48, localDerOutputStream2);
      this.signedCert = localDerOutputStream1.toByteArray();
      this.readOnly = true;
    }
    catch (IOException localIOException)
    {
      throw new CertificateEncodingException(localIOException.toString());
    }
  }

  public void checkValidity()
    throws CertificateExpiredException, CertificateNotYetValidException
  {
    Date localDate = new Date();
    checkValidity(localDate);
  }

  public void checkValidity(Date paramDate)
    throws CertificateExpiredException, CertificateNotYetValidException
  {
    CertificateValidity localCertificateValidity = null;
    try
    {
      localCertificateValidity = (CertificateValidity)this.info.get("validity");
    }
    catch (Exception localException)
    {
      throw new CertificateNotYetValidException("Incorrect validity period");
    }
    if (localCertificateValidity == null)
      throw new CertificateNotYetValidException("Null validity period");
    localCertificateValidity.valid(paramDate);
  }

  public Object get(String paramString)
    throws CertificateParsingException
  {
    X509AttributeName localX509AttributeName = new X509AttributeName(paramString);
    String str = localX509AttributeName.getPrefix();
    if (!(str.equalsIgnoreCase("x509")))
      throw new CertificateParsingException("Invalid root of attribute name, expected [x509], received [" + str + "]");
    localX509AttributeName = new X509AttributeName(localX509AttributeName.getSuffix());
    str = localX509AttributeName.getPrefix();
    if (str.equalsIgnoreCase("info"))
    {
      if (this.info == null)
        return null;
      if (localX509AttributeName.getSuffix() != null)
        try
        {
          return this.info.get(localX509AttributeName.getSuffix());
        }
        catch (IOException localIOException)
        {
          throw new CertificateParsingException(localIOException.toString());
        }
        catch (CertificateException localCertificateException)
        {
          throw new CertificateParsingException(localCertificateException.toString());
        }
      return this.info;
    }
    if (str.equalsIgnoreCase("algorithm"))
      return this.algId;
    if (str.equalsIgnoreCase("signature"))
    {
      if (this.signature != null)
        return this.signature.clone();
      return null;
    }
    if (str.equalsIgnoreCase("signed_cert"))
    {
      if (this.signedCert != null)
        return this.signedCert.clone();
      return null;
    }
    throw new CertificateParsingException("Attribute name not recognized or get() not allowed for the same: " + str);
  }

  public void set(String paramString, Object paramObject)
    throws CertificateException, IOException
  {
    if (this.readOnly)
      throw new CertificateException("cannot over-write existing certificate");
    X509AttributeName localX509AttributeName = new X509AttributeName(paramString);
    String str = localX509AttributeName.getPrefix();
    if (!(str.equalsIgnoreCase("x509")))
      throw new CertificateException("Invalid root of attribute name, expected [x509], received " + str);
    localX509AttributeName = new X509AttributeName(localX509AttributeName.getSuffix());
    str = localX509AttributeName.getPrefix();
    if (str.equalsIgnoreCase("info"))
      if (localX509AttributeName.getSuffix() == null)
      {
        if (!(paramObject instanceof X509CertInfo))
          throw new CertificateException("Attribute value should be of type X509CertInfo.");
        this.info = ((X509CertInfo)paramObject);
        this.signedCert = null;
      }
      else
      {
        this.info.set(localX509AttributeName.getSuffix(), paramObject);
        this.signedCert = null;
      }
    else
      throw new CertificateException("Attribute name not recognized or set() not allowed for the same: " + str);
  }

  public void delete(String paramString)
    throws CertificateException, IOException
  {
    if (this.readOnly)
      throw new CertificateException("cannot over-write existing certificate");
    X509AttributeName localX509AttributeName = new X509AttributeName(paramString);
    String str = localX509AttributeName.getPrefix();
    if (!(str.equalsIgnoreCase("x509")))
      throw new CertificateException("Invalid root of attribute name, expected [x509], received " + str);
    localX509AttributeName = new X509AttributeName(localX509AttributeName.getSuffix());
    str = localX509AttributeName.getPrefix();
    if (str.equalsIgnoreCase("info"))
      if (localX509AttributeName.getSuffix() != null)
        this.info = null;
      else
        this.info.delete(localX509AttributeName.getSuffix());
    else if (str.equalsIgnoreCase("algorithm"))
      this.algId = null;
    else if (str.equalsIgnoreCase("signature"))
      this.signature = null;
    else if (str.equalsIgnoreCase("signed_cert"))
      this.signedCert = null;
    else
      throw new CertificateException("Attribute name not recognized or delete() not allowed for the same: " + str);
  }

  public Enumeration<String> getElements()
  {
    AttributeNameEnumeration localAttributeNameEnumeration = new AttributeNameEnumeration();
    localAttributeNameEnumeration.addElement("x509.info");
    localAttributeNameEnumeration.addElement("x509.algorithm");
    localAttributeNameEnumeration.addElement("x509.signature");
    localAttributeNameEnumeration.addElement("x509.signed_cert");
    return localAttributeNameEnumeration.elements();
  }

  public String getName()
  {
    return "x509";
  }

  public String toString()
  {
    if ((this.info == null) || (this.algId == null) || (this.signature == null))
      return "";
    StringBuilder localStringBuilder = new StringBuilder();
    localStringBuilder.append("[\n");
    localStringBuilder.append(this.info.toString() + "\n");
    localStringBuilder.append("  Algorithm: [" + this.algId.toString() + "]\n");
    HexDumpEncoder localHexDumpEncoder = new HexDumpEncoder();
    localStringBuilder.append("  Signature:\n" + localHexDumpEncoder.encodeBuffer(this.signature));
    localStringBuilder.append("\n]");
    return localStringBuilder.toString();
  }

  public PublicKey getPublicKey()
  {
    if (this.info == null)
      return null;
    try
    {
      PublicKey localPublicKey = (PublicKey)this.info.get("key.value");
      return localPublicKey;
    }
    catch (Exception localException)
    {
    }
    return null;
  }

  public int getVersion()
  {
    if (this.info == null)
      return -1;
    try
    {
      int i = ((Integer)this.info.get("version.number")).intValue();
      return (i + 1);
    }
    catch (Exception localException)
    {
    }
    return -1;
  }

  public BigInteger getSerialNumber()
  {
    SerialNumber localSerialNumber = getSerialNumberObject();
    return ((localSerialNumber != null) ? localSerialNumber.getNumber() : null);
  }

  public SerialNumber getSerialNumberObject()
  {
    if (this.info == null)
      return null;
    try
    {
      SerialNumber localSerialNumber = (SerialNumber)this.info.get("serialNumber.number");
      return localSerialNumber;
    }
    catch (Exception localException)
    {
    }
    return null;
  }

  public Principal getSubjectDN()
  {
    if (this.info == null)
      return null;
    try
    {
      Principal localPrincipal = (Principal)this.info.get("subject.dname");
      return localPrincipal;
    }
    catch (Exception localException)
    {
    }
    return null;
  }

  public X500Principal getSubjectX500Principal()
  {
    if (this.info == null)
      return null;
    try
    {
      X500Principal localX500Principal = (X500Principal)this.info.get("subject.x500principal");
      return localX500Principal;
    }
    catch (Exception localException)
    {
    }
    return null;
  }

  public Principal getIssuerDN()
  {
    if (this.info == null)
      return null;
    try
    {
      Principal localPrincipal = (Principal)this.info.get("issuer.dname");
      return localPrincipal;
    }
    catch (Exception localException)
    {
    }
    return null;
  }

  public X500Principal getIssuerX500Principal()
  {
    if (this.info == null)
      return null;
    try
    {
      X500Principal localX500Principal = (X500Principal)this.info.get("issuer.x500principal");
      return localX500Principal;
    }
    catch (Exception localException)
    {
    }
    return null;
  }

  public Date getNotBefore()
  {
    if (this.info == null)
      return null;
    try
    {
      Date localDate = (Date)this.info.get("validity.notBefore");
      return localDate;
    }
    catch (Exception localException)
    {
    }
    return null;
  }

  public Date getNotAfter()
  {
    if (this.info == null)
      return null;
    try
    {
      Date localDate = (Date)this.info.get("validity.notAfter");
      return localDate;
    }
    catch (Exception localException)
    {
    }
    return null;
  }

  public byte[] getTBSCertificate()
    throws CertificateEncodingException
  {
    if (this.info != null)
      return this.info.getEncodedInfo();
    throw new CertificateEncodingException("Uninitialized certificate");
  }

  public byte[] getSignature()
  {
    if (this.signature == null)
      return null;
    byte[] arrayOfByte = new byte[this.signature.length];
    java.lang.System.arraycopy(this.signature, 0, arrayOfByte, 0, arrayOfByte.length);
    return arrayOfByte;
  }

  public String getSigAlgName()
  {
    if (this.algId == null)
      return null;
    return this.algId.getName();
  }

  public String getSigAlgOID()
  {
    if (this.algId == null)
      return null;
    ObjectIdentifier localObjectIdentifier = this.algId.getOID();
    return localObjectIdentifier.toString();
  }

  public byte[] getSigAlgParams()
  {
    if (this.algId == null)
      return null;
    try
    {
      return this.algId.getEncodedParams();
    }
    catch (IOException localIOException)
    {
    }
    return null;
  }

  public boolean[] getIssuerUniqueID()
  {
    if (this.info == null)
      return null;
    try
    {
      UniqueIdentity localUniqueIdentity = (UniqueIdentity)this.info.get("issuerID.id");
      if (localUniqueIdentity == null)
        return null;
      return localUniqueIdentity.getId();
    }
    catch (Exception localException)
    {
    }
    return null;
  }

  public boolean[] getSubjectUniqueID()
  {
    if (this.info == null)
      return null;
    try
    {
      UniqueIdentity localUniqueIdentity = (UniqueIdentity)this.info.get("subjectID.id");
      if (localUniqueIdentity == null)
        return null;
      return localUniqueIdentity.getId();
    }
    catch (Exception localException)
    {
    }
    return null;
  }

  public AuthorityKeyIdentifierExtension getAuthorityKeyIdentifierExtension()
  {
    return ((AuthorityKeyIdentifierExtension)getExtension(PKIXExtensions.AuthorityKey_Id));
  }

  public BasicConstraintsExtension getBasicConstraintsExtension()
  {
    return ((BasicConstraintsExtension)getExtension(PKIXExtensions.BasicConstraints_Id));
  }

  public CertificatePoliciesExtension getCertificatePoliciesExtension()
  {
    return ((CertificatePoliciesExtension)getExtension(PKIXExtensions.CertificatePolicies_Id));
  }

  public ExtendedKeyUsageExtension getExtendedKeyUsageExtension()
  {
    return ((ExtendedKeyUsageExtension)getExtension(PKIXExtensions.ExtendedKeyUsage_Id));
  }

  public IssuerAlternativeNameExtension getIssuerAlternativeNameExtension()
  {
    return ((IssuerAlternativeNameExtension)getExtension(PKIXExtensions.IssuerAlternativeName_Id));
  }

  public NameConstraintsExtension getNameConstraintsExtension()
  {
    return ((NameConstraintsExtension)getExtension(PKIXExtensions.NameConstraints_Id));
  }

  public PolicyConstraintsExtension getPolicyConstraintsExtension()
  {
    return ((PolicyConstraintsExtension)getExtension(PKIXExtensions.PolicyConstraints_Id));
  }

  public PolicyMappingsExtension getPolicyMappingsExtension()
  {
    return ((PolicyMappingsExtension)getExtension(PKIXExtensions.PolicyMappings_Id));
  }

  public PrivateKeyUsageExtension getPrivateKeyUsageExtension()
  {
    return ((PrivateKeyUsageExtension)getExtension(PKIXExtensions.PrivateKeyUsage_Id));
  }

  public SubjectAlternativeNameExtension getSubjectAlternativeNameExtension()
  {
    return ((SubjectAlternativeNameExtension)getExtension(PKIXExtensions.SubjectAlternativeName_Id));
  }

  public SubjectKeyIdentifierExtension getSubjectKeyIdentifierExtension()
  {
    return ((SubjectKeyIdentifierExtension)getExtension(PKIXExtensions.SubjectKey_Id));
  }

  public CRLDistributionPointsExtension getCRLDistributionPointsExtension()
  {
    return ((CRLDistributionPointsExtension)getExtension(PKIXExtensions.CRLDistributionPoints_Id));
  }

  public boolean hasUnsupportedCriticalExtension()
  {
    if (this.info == null)
      return false;
    try
    {
      CertificateExtensions localCertificateExtensions = (CertificateExtensions)this.info.get("extensions");
      if (localCertificateExtensions == null)
        return false;
      return localCertificateExtensions.hasUnsupportedCriticalExtension();
    }
    catch (Exception localException)
    {
    }
    return false;
  }

  public Set<String> getCriticalExtensionOIDs()
  {
    if (this.info == null)
      return null;
    try
    {
      CertificateExtensions localCertificateExtensions = (CertificateExtensions)this.info.get("extensions");
      if (localCertificateExtensions == null)
        return null;
      HashSet localHashSet = new HashSet();
      Iterator localIterator = localCertificateExtensions.getAllExtensions().iterator();
      while (localIterator.hasNext())
      {
        Extension localExtension = (Extension)localIterator.next();
        if (localExtension.isCritical())
          localHashSet.add(localExtension.getExtensionId().toString());
      }
      return localHashSet;
    }
    catch (Exception localException)
    {
    }
    return null;
  }

  public Set<String> getNonCriticalExtensionOIDs()
  {
    if (this.info == null)
      return null;
    try
    {
      CertificateExtensions localCertificateExtensions = (CertificateExtensions)this.info.get("extensions");
      if (localCertificateExtensions == null)
        return null;
      HashSet localHashSet = new HashSet();
      Iterator localIterator = localCertificateExtensions.getAllExtensions().iterator();
      while (localIterator.hasNext())
      {
        Extension localExtension = (Extension)localIterator.next();
        if (!(localExtension.isCritical()))
          localHashSet.add(localExtension.getExtensionId().toString());
      }
      localHashSet.addAll(localCertificateExtensions.getUnparseableExtensions().keySet());
      return localHashSet;
    }
    catch (Exception localException)
    {
    }
    return null;
  }

  public Extension getExtension(ObjectIdentifier paramObjectIdentifier)
  {
    if (this.info == null)
      return null;
    try
    {
      CertificateExtensions localCertificateExtensions;
      try
      {
        localCertificateExtensions = (CertificateExtensions)this.info.get("extensions");
      }
      catch (CertificateException localCertificateException)
      {
        return null;
      }
      if (localCertificateExtensions == null)
        return null;
      Iterator localIterator = localCertificateExtensions.getAllExtensions().iterator();
      while (localIterator.hasNext())
      {
        Extension localExtension = (Extension)localIterator.next();
        if (localExtension.getExtensionId().equals(paramObjectIdentifier))
          return localExtension;
      }
      return null;
    }
    catch (IOException localIOException)
    {
    }
    return null;
  }

  public Extension getUnparseableExtension(ObjectIdentifier paramObjectIdentifier)
  {
    if (this.info == null)
      return null;
    try
    {
      CertificateExtensions localCertificateExtensions;
      try
      {
        localCertificateExtensions = (CertificateExtensions)this.info.get("extensions");
      }
      catch (CertificateException localCertificateException)
      {
        return null;
      }
      if (localCertificateExtensions == null)
        return null;
      return ((Extension)localCertificateExtensions.getUnparseableExtensions().get(paramObjectIdentifier.toString()));
    }
    catch (IOException localIOException)
    {
    }
    return null;
  }

  public byte[] getExtensionValue(String paramString)
  {
    ObjectIdentifier localObjectIdentifier1;
    try
    {
      localObjectIdentifier1 = new ObjectIdentifier(paramString);
      String str = OIDMap.getName(localObjectIdentifier1);
      Object localObject1 = null;
      CertificateExtensions localCertificateExtensions = (CertificateExtensions)this.info.get("extensions");
      if (str == null)
      {
        if (localCertificateExtensions == null)
          return null;
        Iterator localIterator = localCertificateExtensions.getAllExtensions().iterator();
        while (localIterator.hasNext())
        {
          localObject2 = (Extension)localIterator.next();
          ObjectIdentifier localObjectIdentifier2 = ((Extension)localObject2).getExtensionId();
          if (localObjectIdentifier2.equals(localObjectIdentifier1))
          {
            localObject1 = localObject2;
            break;
          }
        }
      }
      else
      {
        try
        {
          localObject1 = (Extension)get(str);
        }
        catch (CertificateException localCertificateException)
        {
        }
      }
      if (localObject1 == null)
      {
        if (localCertificateExtensions != null)
          localObject1 = (Extension)localCertificateExtensions.getUnparseableExtensions().get(paramString);
        if (localObject1 == null)
          return null;
      }
      byte[] arrayOfByte = ((Extension)localObject1).getExtensionValue();
      if (arrayOfByte == null)
        return null;
      Object localObject2 = new DerOutputStream();
      ((DerOutputStream)localObject2).putOctetString(arrayOfByte);
      return ((DerOutputStream)localObject2).toByteArray();
    }
    catch (Exception localException)
    {
    }
    return ((B)(B)null);
  }

  public boolean[] getKeyUsage()
  {
    String str;
    try
    {
      str = OIDMap.getName(PKIXExtensions.KeyUsage_Id);
      if (str == null)
        return null;
      KeyUsageExtension localKeyUsageExtension = (KeyUsageExtension)get(str);
      if (localKeyUsageExtension == null)
        return null;
      Object localObject = localKeyUsageExtension.getBits();
      if (localObject.length < 9)
      {
        boolean[] arrayOfBoolean = new boolean[9];
        java.lang.System.arraycopy(localObject, 0, arrayOfBoolean, 0, localObject.length);
        localObject = arrayOfBoolean;
      }
      return localObject;
    }
    catch (Exception localException)
    {
    }
    return ((Z)null);
  }

  public synchronized List<String> getExtendedKeyUsage()
    throws CertificateParsingException
  {
    if ((this.readOnly) && (this.extKeyUsage != null))
      return this.extKeyUsage;
    ExtendedKeyUsageExtension localExtendedKeyUsageExtension = getExtendedKeyUsageExtension();
    if (localExtendedKeyUsageExtension == null)
      return null;
    this.extKeyUsage = Collections.unmodifiableList(localExtendedKeyUsageExtension.getExtendedKeyUsage());
    return this.extKeyUsage;
  }

  public static List<String> getExtendedKeyUsage(X509Certificate paramX509Certificate)
    throws CertificateParsingException
  {
    byte[] arrayOfByte1;
    Object localObject;
    try
    {
      arrayOfByte1 = paramX509Certificate.getExtensionValue("2.5.29.37");
      if (arrayOfByte1 == null)
        return null;
      localObject = new DerValue(arrayOfByte1);
      byte[] arrayOfByte2 = ((DerValue)localObject).getOctetString();
      ExtendedKeyUsageExtension localExtendedKeyUsageExtension = new ExtendedKeyUsageExtension(Boolean.FALSE, arrayOfByte2);
      return Collections.unmodifiableList(localExtendedKeyUsageExtension.getExtendedKeyUsage());
    }
    catch (IOException localIOException)
    {
      localObject = new CertificateParsingException();
      ((CertificateParsingException)localObject).initCause(localIOException);
      throw ((java.lang.Throwable)localObject);
    }
  }

  public int getBasicConstraints()
  {
    String str;
    try
    {
      str = OIDMap.getName(PKIXExtensions.BasicConstraints_Id);
      if (str == null)
        return -1;
      BasicConstraintsExtension localBasicConstraintsExtension = (BasicConstraintsExtension)get(str);
      if (localBasicConstraintsExtension == null)
        return -1;
      if (((Boolean)localBasicConstraintsExtension.get("is_ca")).booleanValue() == true)
        return ((Integer)localBasicConstraintsExtension.get("path_len")).intValue();
      return -1;
    }
    catch (Exception localException)
    {
    }
    return -1;
  }

  private static Collection<List<?>> makeAltNames(GeneralNames paramGeneralNames)
  {
    if (paramGeneralNames.isEmpty())
      return Collections.emptySet();
    HashSet localHashSet = new HashSet();
    Iterator localIterator = paramGeneralNames.names().iterator();
    while (localIterator.hasNext())
    {
      GeneralName localGeneralName = (GeneralName)localIterator.next();
      GeneralNameInterface localGeneralNameInterface = localGeneralName.getName();
      ArrayList localArrayList = new ArrayList(2);
      localArrayList.add(Integer.valueOf(localGeneralNameInterface.getType()));
      switch (localGeneralNameInterface.getType())
      {
      case 1:
        localArrayList.add(((RFC822Name)localGeneralNameInterface).getName());
        break;
      case 2:
        localArrayList.add(((DNSName)localGeneralNameInterface).getName());
        break;
      case 4:
        localArrayList.add(((X500Name)localGeneralNameInterface).getRFC2253Name());
        break;
      case 6:
        localArrayList.add(((URIName)localGeneralNameInterface).getName());
        break;
      case 7:
        try
        {
          localArrayList.add(((IPAddressName)localGeneralNameInterface).getName());
        }
        catch (IOException localIOException1)
        {
          throw new RuntimeException("IPAddress cannot be parsed", localIOException1);
        }
      case 8:
        localArrayList.add(((OIDName)localGeneralNameInterface).getOID().toString());
        break;
      case 3:
      case 5:
      default:
        DerOutputStream localDerOutputStream = new DerOutputStream();
        try
        {
          localGeneralNameInterface.encode(localDerOutputStream);
        }
        catch (IOException localIOException2)
        {
          throw new RuntimeException("name cannot be encoded", localIOException2);
        }
        localArrayList.add(localDerOutputStream.toByteArray());
      }
      localHashSet.add(Collections.unmodifiableList(localArrayList));
    }
    return Collections.unmodifiableCollection(localHashSet);
  }

  private static Collection<List<?>> cloneAltNames(Collection<List<?>> paramCollection)
  {
    Object localObject2;
    int i = 0;
    Object localObject1 = paramCollection.iterator();
    while (((Iterator)localObject1).hasNext())
    {
      localObject2 = (List)((Iterator)localObject1).next();
      if (((List)localObject2).get(1) instanceof byte[])
        i = 1;
    }
    if (i != 0)
    {
      localObject1 = new HashSet();
      localObject2 = paramCollection.iterator();
      while (((Iterator)localObject2).hasNext())
      {
        List localList = (List)((Iterator)localObject2).next();
        Object localObject3 = localList.get(1);
        if (localObject3 instanceof byte[])
        {
          ArrayList localArrayList = new ArrayList(localList);
          localArrayList.set(1, ((byte[])(byte[])localObject3).clone());
          ((Set)localObject1).add(Collections.unmodifiableList(localArrayList));
        }
        else
        {
          ((Set)localObject1).add(localList);
        }
      }
      return Collections.unmodifiableCollection((Collection)localObject1);
    }
    return ((Collection<List<?>>)(Collection<List<?>>)paramCollection);
  }

  public synchronized Collection<List<?>> getSubjectAlternativeNames()
    throws CertificateParsingException
  {
    GeneralNames localGeneralNames;
    if ((this.readOnly) && (this.subjectAlternativeNames != null))
      return cloneAltNames(this.subjectAlternativeNames);
    SubjectAlternativeNameExtension localSubjectAlternativeNameExtension = getSubjectAlternativeNameExtension();
    if (localSubjectAlternativeNameExtension == null)
      return null;
    try
    {
      localGeneralNames = (GeneralNames)localSubjectAlternativeNameExtension.get("subject_name");
    }
    catch (IOException localIOException)
    {
      return Collections.emptySet();
    }
    this.subjectAlternativeNames = makeAltNames(localGeneralNames);
    return this.subjectAlternativeNames;
  }

  public static Collection<List<?>> getSubjectAlternativeNames(X509Certificate paramX509Certificate)
    throws CertificateParsingException
  {
    byte[] arrayOfByte1;
    Object localObject;
    try
    {
      GeneralNames localGeneralNames;
      arrayOfByte1 = paramX509Certificate.getExtensionValue("2.5.29.17");
      if (arrayOfByte1 == null)
        return null;
      localObject = new DerValue(arrayOfByte1);
      byte[] arrayOfByte2 = ((DerValue)localObject).getOctetString();
      SubjectAlternativeNameExtension localSubjectAlternativeNameExtension = new SubjectAlternativeNameExtension(Boolean.FALSE, arrayOfByte2);
      try
      {
        localGeneralNames = (GeneralNames)localSubjectAlternativeNameExtension.get("subject_name");
      }
      catch (IOException localIOException2)
      {
        return Collections.emptySet();
      }
      return makeAltNames(localGeneralNames);
    }
    catch (IOException localIOException1)
    {
      localObject = new CertificateParsingException();
      ((CertificateParsingException)localObject).initCause(localIOException1);
      throw ((java.lang.Throwable)localObject);
    }
  }

  public synchronized Collection<List<?>> getIssuerAlternativeNames()
    throws CertificateParsingException
  {
    GeneralNames localGeneralNames;
    if ((this.readOnly) && (this.issuerAlternativeNames != null))
      return cloneAltNames(this.issuerAlternativeNames);
    IssuerAlternativeNameExtension localIssuerAlternativeNameExtension = getIssuerAlternativeNameExtension();
    if (localIssuerAlternativeNameExtension == null)
      return null;
    try
    {
      localGeneralNames = (GeneralNames)localIssuerAlternativeNameExtension.get("issuer_name");
    }
    catch (IOException localIOException)
    {
      return Collections.emptySet();
    }
    this.issuerAlternativeNames = makeAltNames(localGeneralNames);
    return this.issuerAlternativeNames;
  }

  public static Collection<List<?>> getIssuerAlternativeNames(X509Certificate paramX509Certificate)
    throws CertificateParsingException
  {
    byte[] arrayOfByte1;
    Object localObject;
    try
    {
      GeneralNames localGeneralNames;
      arrayOfByte1 = paramX509Certificate.getExtensionValue("2.5.29.18");
      if (arrayOfByte1 == null)
        return null;
      localObject = new DerValue(arrayOfByte1);
      byte[] arrayOfByte2 = ((DerValue)localObject).getOctetString();
      IssuerAlternativeNameExtension localIssuerAlternativeNameExtension = new IssuerAlternativeNameExtension(Boolean.FALSE, arrayOfByte2);
      try
      {
        localGeneralNames = (GeneralNames)localIssuerAlternativeNameExtension.get("issuer_name");
      }
      catch (IOException localIOException2)
      {
        return Collections.emptySet();
      }
      return makeAltNames(localGeneralNames);
    }
    catch (IOException localIOException1)
    {
      localObject = new CertificateParsingException();
      ((CertificateParsingException)localObject).initCause(localIOException1);
      throw ((java.lang.Throwable)localObject);
    }
  }

  public AuthorityInfoAccessExtension getAuthorityInfoAccessExtension()
  {
    return ((AuthorityInfoAccessExtension)getExtension(PKIXExtensions.AuthInfoAccess_Id));
  }

  private void parse(DerValue paramDerValue)
    throws CertificateException, IOException
  {
    if (this.readOnly)
      throw new CertificateParsingException("cannot over-write existing certificate");
    if ((paramDerValue.data == null) || (paramDerValue.tag != 48))
      throw new CertificateParsingException("invalid DER-encoded certificate data");
    this.signedCert = paramDerValue.toByteArray();
    DerValue[] arrayOfDerValue = new DerValue[3];
    arrayOfDerValue[0] = paramDerValue.data.getDerValue();
    arrayOfDerValue[1] = paramDerValue.data.getDerValue();
    arrayOfDerValue[2] = paramDerValue.data.getDerValue();
    if (paramDerValue.data.available() != 0)
      throw new CertificateParsingException("signed overrun, bytes = " + paramDerValue.data.available());
    if (arrayOfDerValue[0].tag != 48)
      throw new CertificateParsingException("signed fields invalid");
    this.algId = AlgorithmId.parse(arrayOfDerValue[1]);
    this.signature = arrayOfDerValue[2].getBitString();
    if (arrayOfDerValue[1].data.available() != 0)
      throw new CertificateParsingException("algid field overrun");
    if (arrayOfDerValue[2].data.available() != 0)
      throw new CertificateParsingException("signed fields overrun");
    this.info = new X509CertInfo(arrayOfDerValue[0]);
    AlgorithmId localAlgorithmId = (AlgorithmId)this.info.get("algorithmID.algorithm");
    if (!(this.algId.equals(localAlgorithmId)))
      throw new CertificateException("Signature algorithm mismatch");
    this.readOnly = true;
  }

  private static X500Principal getX500Principal(X509Certificate paramX509Certificate, boolean paramBoolean)
    throws Exception
  {
    byte[] arrayOfByte1 = paramX509Certificate.getEncoded();
    DerInputStream localDerInputStream1 = new DerInputStream(arrayOfByte1);
    DerValue localDerValue1 = localDerInputStream1.getSequence(3)[0];
    DerInputStream localDerInputStream2 = localDerValue1.data;
    DerValue localDerValue2 = localDerInputStream2.getDerValue();
    if (localDerValue2.isContextSpecific(0))
      localDerValue2 = localDerInputStream2.getDerValue();
    localDerValue2 = localDerInputStream2.getDerValue();
    localDerValue2 = localDerInputStream2.getDerValue();
    if (!(paramBoolean))
    {
      localDerValue2 = localDerInputStream2.getDerValue();
      localDerValue2 = localDerInputStream2.getDerValue();
    }
    byte[] arrayOfByte2 = localDerValue2.toByteArray();
    return new X500Principal(arrayOfByte2);
  }

  public static X500Principal getSubjectX500Principal(X509Certificate paramX509Certificate)
  {
    try
    {
      return getX500Principal(paramX509Certificate, false);
    }
    catch (Exception localException)
    {
      throw new RuntimeException("Could not parse subject", localException);
    }
  }

  public static X500Principal getIssuerX500Principal(X509Certificate paramX509Certificate)
  {
    try
    {
      return getX500Principal(paramX509Certificate, true);
    }
    catch (Exception localException)
    {
      throw new RuntimeException("Could not parse issuer", localException);
    }
  }

  public static byte[] getEncodedInternal(Certificate paramCertificate)
    throws CertificateEncodingException
  {
    if (paramCertificate instanceof X509CertImpl)
      return ((X509CertImpl)paramCertificate).getEncodedInternal();
    return paramCertificate.getEncoded();
  }

  public static X509CertImpl toImpl(X509Certificate paramX509Certificate)
    throws CertificateException
  {
    if (paramX509Certificate instanceof X509CertImpl)
      return ((X509CertImpl)paramX509Certificate);
    return X509Factory.intern(paramX509Certificate);
  }

  public static boolean isSelfIssued(X509Certificate paramX509Certificate)
  {
    X500Principal localX500Principal1 = paramX509Certificate.getSubjectX500Principal();
    X500Principal localX500Principal2 = paramX509Certificate.getIssuerX500Principal();
    return localX500Principal1.equals(localX500Principal2);
  }

  public static boolean isSelfSigned(X509Certificate paramX509Certificate, String paramString)
  {
    if (isSelfIssued(paramX509Certificate))
      try
      {
        if (paramString == null)
          paramX509Certificate.verify(paramX509Certificate.getPublicKey());
        else
          paramX509Certificate.verify(paramX509Certificate.getPublicKey(), paramString);
        return true;
      }
      catch (Exception localException)
      {
      }
    return false;
  }
}