package sun.security.provider.certpath;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.Security;
import java.security.cert.CertPath;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertSelector;
import java.security.cert.CertStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateParsingException;
import java.security.cert.PKIXCertPathChecker;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.security.auth.x500.X500Principal;
import sun.security.util.Debug;
import sun.security.util.ObjectIdentifier;
import sun.security.x509.AccessDescription;
import sun.security.x509.AuthorityInfoAccessExtension;
import sun.security.x509.GeneralName;
import sun.security.x509.SerialNumber;
import sun.security.x509.URIName;
import sun.security.x509.X509CertImpl;

class OCSPChecker extends PKIXCertPathChecker
{
  public static final String OCSP_ENABLE_PROP = "ocsp.enable";
  public static final String OCSP_URL_PROP = "ocsp.responderURL";
  public static final String OCSP_CERT_SUBJECT_PROP = "ocsp.responderCertSubjectName";
  public static final String OCSP_CERT_ISSUER_PROP = "ocsp.responderCertIssuerName";
  public static final String OCSP_CERT_NUMBER_PROP = "ocsp.responderCertSerialNumber";
  private static final String HEX_DIGITS = "0123456789ABCDEFabcdef";
  private static final Debug DEBUG = Debug.getInstance("certpath");
  private static final boolean dump = 0;
  private static final int[] OCSP_NONCE_DATA = { 1, 3, 6, 1, 5, 5, 7, 48, 1, 2 };
  private static final ObjectIdentifier OCSP_NONCE_OID = ObjectIdentifier.newInternal(OCSP_NONCE_DATA);
  private int remainingCerts;
  private X509Certificate[] certs;
  private CertPath cp;
  private PKIXParameters pkixParams;
  private boolean onlyEECert;

  OCSPChecker(CertPath paramCertPath, PKIXParameters paramPKIXParameters)
    throws CertPathValidatorException
  {
    this(paramCertPath, paramPKIXParameters, false);
  }

  OCSPChecker(CertPath paramCertPath, PKIXParameters paramPKIXParameters, boolean paramBoolean)
    throws CertPathValidatorException
  {
    this.onlyEECert = false;
    this.cp = paramCertPath;
    this.pkixParams = paramPKIXParameters;
    this.onlyEECert = paramBoolean;
    List localList = this.cp.getCertificates();
    this.certs = ((X509Certificate[])(X509Certificate[])localList.toArray(new X509Certificate[localList.size()]));
    init(false);
  }

  public void init(boolean paramBoolean)
    throws CertPathValidatorException
  {
    if (!(paramBoolean))
      this.remainingCerts = (this.certs.length + 1);
    else
      throw new CertPathValidatorException("Forward checking not supported");
  }

  public boolean isForwardCheckingSupported()
  {
    return false;
  }

  public Set<String> getSupportedExtensions()
  {
    return Collections.emptySet();
  }

  public void check(Certificate paramCertificate, Collection<String> paramCollection)
    throws CertPathValidatorException
  {
    this.remainingCerts -= 1;
    try
    {
      Object localObject1 = null;
      int i = 0;
      X500Principal localX500Principal1 = null;
      X500Principal localX500Principal2 = null;
      BigInteger localBigInteger = null;
      int j = 1;
      X509CertImpl localX509CertImpl1 = null;
      X509CertImpl localX509CertImpl2 = X509CertImpl.toImpl((X509Certificate)paramCertificate);
      if ((this.onlyEECert) && (localX509CertImpl2.getBasicConstraints() != -1))
      {
        if (DEBUG != null)
          DEBUG.println("Skipping revocation check, not end entity cert");
        return;
      }
      String[] arrayOfString = getOCSPProperties();
      URL localURL = getOCSPServerURL(localX509CertImpl2, arrayOfString);
      if (arrayOfString[1] != null)
      {
        localX500Principal1 = new X500Principal(arrayOfString[1]);
      }
      else if ((arrayOfString[2] != null) && (arrayOfString[3] != null))
      {
        localX500Principal2 = new X500Principal(arrayOfString[2]);
        localObject2 = stripOutSeparators(arrayOfString[3]);
        localBigInteger = new BigInteger((String)localObject2, 16);
      }
      else if ((arrayOfString[2] != null) || (arrayOfString[3] != null))
      {
        throw new CertPathValidatorException("Must specify both ocsp.responderCertIssuerName and ocsp.responderCertSerialNumber properties");
      }
      if ((localX500Principal1 != null) || (localX500Principal2 != null))
        i = 1;
      if (this.remainingCerts < this.certs.length)
      {
        localX509CertImpl1 = X509CertImpl.toImpl(this.certs[this.remainingCerts]);
        j = 0;
        if (i == 0)
        {
          localObject1 = this.certs[this.remainingCerts];
          if (DEBUG != null)
            DEBUG.println("Responder's certificate is the same as the issuer of the certificate being validated");
        }
      }
      if ((j != 0) || (i != 0))
      {
        if ((DEBUG != null) && (i != 0))
          DEBUG.println("Searching trust anchors for responder's certificate");
        localObject2 = this.pkixParams.getTrustAnchors().iterator();
        if (!(((Iterator)localObject2).hasNext()))
          throw new CertPathValidatorException("Must specify at least one trust anchor");
        localObject3 = localX509CertImpl2.getIssuerX500Principal();
        while ((((Iterator)localObject2).hasNext()) && (((j != 0) || (i != 0))))
        {
          localObject4 = (TrustAnchor)((Iterator)localObject2).next();
          localObject5 = ((TrustAnchor)localObject4).getTrustedCert();
          localObject6 = ((X509Certificate)localObject5).getSubjectX500Principal();
          if ((j != 0) && (((X500Principal)localObject3).equals(localObject6)))
          {
            localX509CertImpl1 = X509CertImpl.toImpl((X509Certificate)localObject5);
            j = 0;
            if ((i == 0) && (localObject1 == null))
            {
              localObject1 = localObject5;
              if (DEBUG != null)
                DEBUG.println("Responder's certificate is the same as the issuer of the certificate being validated");
            }
          }
          if ((i != 0) && ((((localX500Principal1 != null) && (localX500Principal1.equals(localObject6))) || ((localX500Principal2 != null) && (localBigInteger != null) && (localX500Principal2.equals(((X509Certificate)localObject5).getIssuerX500Principal())) && (localBigInteger.equals(((X509Certificate)localObject5).getSerialNumber()))))))
          {
            localObject1 = localObject5;
            i = 0;
          }
        }
        if (localX509CertImpl1 == null)
          throw new CertPathValidatorException("No trusted certificate for " + localX509CertImpl2.getIssuerDN());
        if (i != 0)
        {
          if (DEBUG != null)
            DEBUG.println("Searching cert stores for responder's certificate");
          localObject4 = null;
          if (localX500Principal1 != null)
          {
            localObject4 = new X509CertSelector();
            ((X509CertSelector)localObject4).setSubject(localX500Principal1.getName());
          }
          else if ((localX500Principal2 != null) && (localBigInteger != null))
          {
            localObject4 = new X509CertSelector();
            ((X509CertSelector)localObject4).setIssuer(localX500Principal2.getName());
            ((X509CertSelector)localObject4).setSerialNumber(localBigInteger);
          }
          if (localObject4 != null)
          {
            localObject5 = this.pkixParams.getCertStores();
            localObject6 = ((List)localObject5).iterator();
            while (((Iterator)localObject6).hasNext())
            {
              localObject7 = (CertStore)((Iterator)localObject6).next();
              Iterator localIterator = ((CertStore)localObject7).getCertificates((CertSelector)localObject4).iterator();
              if (localIterator.hasNext())
              {
                localObject1 = (X509Certificate)localIterator.next();
                i = 0;
                break;
              }
            }
          }
        }
      }
      if (i != 0)
        throw new CertPathValidatorException("Cannot find the responder's certificate (set using the OCSP security properties).");
      Object localObject2 = new OCSPRequest(localX509CertImpl2, localX509CertImpl1);
      Object localObject3 = (HttpURLConnection)localURL.openConnection();
      if (DEBUG != null)
        DEBUG.println("connecting to OCSP service at: " + localURL);
      ((HttpURLConnection)localObject3).setDoOutput(true);
      ((HttpURLConnection)localObject3).setDoInput(true);
      ((HttpURLConnection)localObject3).setRequestMethod("POST");
      ((HttpURLConnection)localObject3).setRequestProperty("Content-type", "application/ocsp-request");
      Object localObject4 = ((OCSPRequest)localObject2).encodeBytes();
      Object localObject5 = ((OCSPRequest)localObject2).getCertId();
      ((HttpURLConnection)localObject3).setRequestProperty("Content-length", String.valueOf(localObject4.length));
      Object localObject6 = ((HttpURLConnection)localObject3).getOutputStream();
      ((OutputStream)localObject6).write(localObject4);
      ((OutputStream)localObject6).flush();
      if ((DEBUG != null) && (((HttpURLConnection)localObject3).getResponseCode() != 200))
        DEBUG.println("Received HTTP error: " + ((HttpURLConnection)localObject3).getResponseCode() + " - " + ((HttpURLConnection)localObject3).getResponseMessage());
      Object localObject7 = ((HttpURLConnection)localObject3).getInputStream();
      int k = ((HttpURLConnection)localObject3).getContentLength();
      if (k == -1)
        k = 2147483647;
      byte[] arrayOfByte = new byte[k];
      int l = 0;
      int i1 = 0;
      while ((i1 != -1) && (l < k))
      {
        i1 = ((InputStream)localObject7).read(arrayOfByte, l, arrayOfByte.length - l);
        l += i1;
      }
      ((InputStream)localObject7).close();
      ((OutputStream)localObject6).close();
      OCSPResponse localOCSPResponse = new OCSPResponse(arrayOfByte, this.pkixParams, (X509Certificate)localObject1);
      if (!(((CertId)localObject5).equals(localOCSPResponse.getCertId())))
        throw new CertPathValidatorException("Certificate in the OCSP response does not match the certificate supplied in the OCSP request.");
      SerialNumber localSerialNumber = localX509CertImpl2.getSerialNumberObject();
      int i2 = localOCSPResponse.getCertStatus(localSerialNumber);
      if (DEBUG != null)
        DEBUG.println("Status of certificate (with serial number " + localSerialNumber.getNumber() + ") is: " + OCSPResponse.certStatusToText(i2));
      if (i2 == 1)
        throw new CertificateRevokedException("Certificate has been revoked", this.cp, this.remainingCerts - 1);
      if (i2 == 2)
        throw new CertPathValidatorException("Certificate's revocation status is unknown", null, this.cp, this.remainingCerts - 1);
    }
    catch (CertificateRevokedException localCertificateRevokedException)
    {
      throw localCertificateRevokedException;
    }
    catch (CertPathValidatorException localCertPathValidatorException)
    {
      throw localCertPathValidatorException;
    }
    catch (Exception localException)
    {
      throw new CertPathValidatorException(localException);
    }
  }

  private static URL getOCSPServerURL(X509CertImpl paramX509CertImpl, String[] paramArrayOfString)
    throws CertificateParsingException, CertPathValidatorException
  {
    if (paramArrayOfString[0] != null)
      try
      {
        return new URL(paramArrayOfString[0]);
      }
      catch (MalformedURLException localMalformedURLException1)
      {
        throw new CertPathValidatorException(localMalformedURLException1);
      }
    AuthorityInfoAccessExtension localAuthorityInfoAccessExtension = paramX509CertImpl.getAuthorityInfoAccessExtension();
    if (localAuthorityInfoAccessExtension == null)
      throw new CertPathValidatorException("Must specify the location of an OCSP Responder");
    try
    {
      List localList = (List)localAuthorityInfoAccessExtension.get("descriptions");
      Iterator localIterator = localList.iterator();
      while (localIterator.hasNext())
      {
        AccessDescription localAccessDescription = (AccessDescription)localIterator.next();
        if (localAccessDescription.getAccessMethod().equals(AccessDescription.Ad_OCSP_Id))
        {
          GeneralName localGeneralName = localAccessDescription.getAccessLocation();
          if (localGeneralName.getType() == 6)
            try
            {
              URIName localURIName = (URIName)localGeneralName.getName();
              return new URL(localURIName.getName());
            }
            catch (MalformedURLException localMalformedURLException2)
            {
              throw new CertPathValidatorException(localMalformedURLException2);
            }
        }
      }
    }
    catch (IOException localIOException)
    {
    }
    throw new CertPathValidatorException("Cannot find the location of the OCSP Responder");
  }

  private static String[] getOCSPProperties()
  {
    String[] arrayOfString = new String[4];
    AccessController.doPrivileged(new PrivilegedAction(arrayOfString)
    {
      public Object run()
      {
        this.val$properties[0] = Security.getProperty("ocsp.responderURL");
        this.val$properties[1] = Security.getProperty("ocsp.responderCertSubjectName");
        this.val$properties[2] = Security.getProperty("ocsp.responderCertIssuerName");
        this.val$properties[3] = Security.getProperty("ocsp.responderCertSerialNumber");
        return null;
      }
    });
    return arrayOfString;
  }

  private static String stripOutSeparators(String paramString)
  {
    char[] arrayOfChar = paramString.toCharArray();
    StringBuilder localStringBuilder = new StringBuilder();
    for (int i = 0; i < arrayOfChar.length; ++i)
      if ("0123456789ABCDEFabcdef".indexOf(arrayOfChar[i]) != -1)
        localStringBuilder.append(arrayOfChar[i]);
    return localStringBuilder.toString();
  }
}