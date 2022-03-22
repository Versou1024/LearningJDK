package sun.security.validator;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.security.auth.x500.X500Principal;
import sun.security.util.BitArray;
import sun.security.util.DerInputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;
import sun.security.x509.NetscapeCertTypeExtension;
import sun.security.x509.X509CertImpl;

public final class SimpleValidator extends Validator
{
  static final String OID_BASIC_CONSTRAINTS = "2.5.29.19";
  static final String OID_NETSCAPE_CERT_TYPE = "2.16.840.1.113730.1.1";
  static final String OID_KEY_USAGE = "2.5.29.15";
  static final String OID_EXTENDED_KEY_USAGE = "2.5.29.37";
  static final String OID_EKU_ANY_USAGE = "2.5.29.37.0";
  static final ObjectIdentifier OBJID_NETSCAPE_CERT_TYPE = NetscapeCertTypeExtension.NetscapeCertType_Id;
  private static final String NSCT_SSL_CA = "ssl_ca";
  private static final String NSCT_CODE_SIGNING_CA = "object_signing_ca";
  private Map trustedX500Principals;
  private Collection trustedCerts;

  SimpleValidator(String paramString, Collection paramCollection)
  {
    super("Simple", paramString);
    this.trustedCerts = paramCollection;
    this.trustedX500Principals = new HashMap();
    Iterator localIterator = paramCollection.iterator();
    while (localIterator.hasNext())
    {
      X509Certificate localX509Certificate = (X509Certificate)localIterator.next();
      X500Principal localX500Principal = localX509Certificate.getSubjectX500Principal();
      Object localObject = (Collection)this.trustedX500Principals.get(localX500Principal);
      if (localObject == null)
      {
        localObject = new ArrayList(2);
        this.trustedX500Principals.put(localX500Principal, localObject);
      }
      ((Collection)localObject).add(localX509Certificate);
    }
  }

  public Collection getTrustedCertificates()
  {
    return this.trustedCerts;
  }

  X509Certificate[] engineValidate(X509Certificate[] paramArrayOfX509Certificate, Collection paramCollection, Object paramObject)
    throws CertificateException
  {
    if ((paramArrayOfX509Certificate == null) || (paramArrayOfX509Certificate.length == 0))
      throw new CertificateException("null or zero-length certificate chain");
    paramArrayOfX509Certificate = buildTrustedChain(paramArrayOfX509Certificate);
    Date localDate = this.validationDate;
    if (localDate == null)
      localDate = new Date();
    for (int i = paramArrayOfX509Certificate.length - 2; i >= 0; --i)
    {
      X509Certificate localX509Certificate1 = paramArrayOfX509Certificate[(i + 1)];
      X509Certificate localX509Certificate2 = paramArrayOfX509Certificate[i];
      if ((!(this.variant.equals("code signing"))) && (!(this.variant.equals("jce signing"))))
        localX509Certificate2.checkValidity(localDate);
      if (!(localX509Certificate2.getIssuerX500Principal().equals(localX509Certificate1.getSubjectX500Principal())))
        throw new ValidatorException(ValidatorException.T_NAME_CHAINING, localX509Certificate2);
      try
      {
        localX509Certificate2.verify(localX509Certificate1.getPublicKey());
      }
      catch (GeneralSecurityException localGeneralSecurityException)
      {
        throw new ValidatorException(ValidatorException.T_SIGNATURE_ERROR, localX509Certificate2, localGeneralSecurityException);
      }
      if (i != 0)
        checkExtensions(localX509Certificate2, i);
    }
    return paramArrayOfX509Certificate;
  }

  private void checkExtensions(X509Certificate paramX509Certificate, int paramInt)
    throws CertificateException
  {
    Set localSet = paramX509Certificate.getCriticalExtensionOIDs();
    if (localSet == null)
      localSet = Collections.EMPTY_SET;
    checkBasicConstraints(paramX509Certificate, localSet, paramInt);
    checkKeyUsage(paramX509Certificate, localSet);
    checkNetscapeCertType(paramX509Certificate, localSet);
    if (!(localSet.isEmpty()))
      throw new ValidatorException("Certificate contains unknown critical extensions: " + localSet, ValidatorException.T_CA_EXTENSIONS, paramX509Certificate);
  }

  private void checkNetscapeCertType(X509Certificate paramX509Certificate, Set paramSet)
    throws CertificateException
  {
    if (this.variant.equals("generic"))
      return;
    if ((this.variant.equals("tls client")) || (this.variant.equals("tls server")))
    {
      if (!(getNetscapeCertTypeBit(paramX509Certificate, "ssl_ca")))
        throw new ValidatorException("Invalid Netscape CertType extension for SSL CA certificate", ValidatorException.T_CA_EXTENSIONS, paramX509Certificate);
      paramSet.remove("2.16.840.1.113730.1.1");
    }
    else if ((this.variant.equals("code signing")) || (this.variant.equals("jce signing")))
    {
      if (!(getNetscapeCertTypeBit(paramX509Certificate, "object_signing_ca")))
        throw new ValidatorException("Invalid Netscape CertType extension for code signing CA certificate", ValidatorException.T_CA_EXTENSIONS, paramX509Certificate);
      paramSet.remove("2.16.840.1.113730.1.1");
    }
    else
    {
      throw new CertificateException("Unknown variant " + this.variant);
    }
  }

  static boolean getNetscapeCertTypeBit(X509Certificate paramX509Certificate, String paramString)
  {
    try
    {
      NetscapeCertTypeExtension localNetscapeCertTypeExtension;
      Object localObject2;
      if (paramX509Certificate instanceof X509CertImpl)
      {
        localObject1 = (X509CertImpl)paramX509Certificate;
        localObject2 = OBJID_NETSCAPE_CERT_TYPE;
        localNetscapeCertTypeExtension = (NetscapeCertTypeExtension)((X509CertImpl)localObject1).getExtension((ObjectIdentifier)localObject2);
        if (localNetscapeCertTypeExtension == null)
          return true;
      }
      else
      {
        localObject1 = paramX509Certificate.getExtensionValue("2.16.840.1.113730.1.1");
        if (localObject1 == null)
          return true;
        localObject2 = new DerInputStream(localObject1);
        byte[] arrayOfByte = ((DerInputStream)localObject2).getOctetString();
        arrayOfByte = new DerValue(arrayOfByte).getUnalignedBitString().toByteArray();
        localNetscapeCertTypeExtension = new NetscapeCertTypeExtension(arrayOfByte);
      }
      Object localObject1 = (Boolean)localNetscapeCertTypeExtension.get(paramString);
      return ((Boolean)localObject1).booleanValue();
    }
    catch (IOException localIOException)
    {
    }
    return false;
  }

  private void checkBasicConstraints(X509Certificate paramX509Certificate, Set paramSet, int paramInt)
    throws CertificateException
  {
    paramSet.remove("2.5.29.19");
    int i = paramX509Certificate.getBasicConstraints();
    if (i < 0)
      throw new ValidatorException("End user tried to act as a CA", ValidatorException.T_CA_EXTENSIONS, paramX509Certificate);
    if (paramInt - 1 > i)
      throw new ValidatorException("Violated path length constraints", ValidatorException.T_CA_EXTENSIONS, paramX509Certificate);
  }

  private void checkKeyUsage(X509Certificate paramX509Certificate, Set paramSet)
    throws CertificateException
  {
    paramSet.remove("2.5.29.15");
    paramSet.remove("2.5.29.37");
    boolean[] arrayOfBoolean = paramX509Certificate.getKeyUsage();
    if ((arrayOfBoolean != null) && (((arrayOfBoolean.length < 6) || (arrayOfBoolean[5] == 0))))
      throw new ValidatorException("Wrong key usage: expected keyCertSign", ValidatorException.T_CA_EXTENSIONS, paramX509Certificate);
  }

  private X509Certificate[] buildTrustedChain(X509Certificate[] paramArrayOfX509Certificate)
    throws CertificateException
  {
    ArrayList localArrayList = new ArrayList(paramArrayOfX509Certificate.length);
    for (int i = 0; i < paramArrayOfX509Certificate.length; ++i)
    {
      localObject1 = paramArrayOfX509Certificate[i];
      localObject2 = getTrustedCertificate((X509Certificate)localObject1);
      if (localObject2 != null)
      {
        localArrayList.add(localObject2);
        return ((X509Certificate[])(X509Certificate[])localArrayList.toArray(CHAIN0));
      }
      localArrayList.add(localObject1);
    }
    X509Certificate localX509Certificate1 = paramArrayOfX509Certificate[(paramArrayOfX509Certificate.length - 1)];
    Object localObject1 = localX509Certificate1.getSubjectX500Principal();
    Object localObject2 = localX509Certificate1.getIssuerX500Principal();
    if (!(((X500Principal)localObject1).equals(localObject2)))
    {
      List localList = (List)this.trustedX500Principals.get(localObject2);
      if (localList != null)
      {
        X509Certificate localX509Certificate2 = (X509Certificate)localList.iterator().next();
        localArrayList.add(localX509Certificate2);
        return ((X509Certificate[])(X509Certificate[])localArrayList.toArray(CHAIN0));
      }
    }
    throw new ValidatorException(ValidatorException.T_NO_TRUST_ANCHOR);
  }

  private X509Certificate getTrustedCertificate(X509Certificate paramX509Certificate)
  {
    X509Certificate localX509Certificate;
    X500Principal localX500Principal1 = paramX509Certificate.getSubjectX500Principal();
    List localList = (List)this.trustedX500Principals.get(localX500Principal1);
    if (localList == null)
      return null;
    X500Principal localX500Principal2 = paramX509Certificate.getIssuerX500Principal();
    PublicKey localPublicKey = paramX509Certificate.getPublicKey();
    Iterator localIterator = localList.iterator();
    while (true)
    {
      while (true)
      {
        if (!(localIterator.hasNext()))
          break label113;
        localX509Certificate = (X509Certificate)localIterator.next();
        if (localX509Certificate.equals(paramX509Certificate))
          return paramX509Certificate;
        if (localX509Certificate.getIssuerX500Principal().equals(localX500Principal2))
          break;
      }
      if (localX509Certificate.getPublicKey().equals(localPublicKey))
        break;
    }
    return localX509Certificate;
    label113: return null;
  }
}