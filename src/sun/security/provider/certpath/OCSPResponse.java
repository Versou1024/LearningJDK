package sun.security.provider.certpath;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CertPathValidatorException;
import java.security.cert.PKIXParameters;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.List;
import sun.security.util.Debug;
import sun.security.util.DerInputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;
import sun.security.x509.AlgorithmId;
import sun.security.x509.CertificateIssuerName;
import sun.security.x509.Extension;
import sun.security.x509.SerialNumber;
import sun.security.x509.X509CertImpl;

class OCSPResponse
{
  public static final int CERT_STATUS_GOOD = 0;
  public static final int CERT_STATUS_REVOKED = 1;
  public static final int CERT_STATUS_UNKNOWN = 2;
  private static final Debug DEBUG = Debug.getInstance("certpath");
  private static final boolean dump = 0;
  private static final ObjectIdentifier OCSP_BASIC_RESPONSE_OID;
  private static final ObjectIdentifier OCSP_NONCE_EXTENSION_OID;
  private static final int OCSP_RESPONSE_OK = 0;
  private static final int NAME_TAG = 1;
  private static final int KEY_TAG = 2;
  private static final String KP_OCSP_SIGNING_OID = "1.3.6.1.5.5.7.3.9";
  private SingleResponse singleResponse;
  private static final long MAX_CLOCK_SKEW = 600000L;

  OCSPResponse(byte[] paramArrayOfByte, PKIXParameters paramPKIXParameters, X509Certificate paramX509Certificate)
    throws IOException, CertPathValidatorException
  {
    try
    {
      label507: DerValue localDerValue5;
      Object localObject3;
      CertificateIssuerName localCertificateIssuerName = null;
      DerValue localDerValue1 = new DerValue(paramArrayOfByte);
      if (localDerValue1.tag != 48)
        throw new IOException("Bad encoding in OCSP response: expected ASN.1 SEQUENCE tag.");
      DerInputStream localDerInputStream1 = localDerValue1.getData();
      int i = localDerInputStream1.getEnumerated();
      if (DEBUG != null)
        DEBUG.println("OCSP response: " + responseToText(i));
      if (i != 0)
        throw new CertPathValidatorException("OCSP Response Failure: " + responseToText(i));
      localDerValue1 = localDerInputStream1.getDerValue();
      if (!(localDerValue1.isContextSpecific(0)))
        throw new IOException("Bad encoding in responseBytes element of OCSP response: expected ASN.1 context specific tag 0.");
      DerValue localDerValue2 = localDerValue1.data.getDerValue();
      if (localDerValue2.tag != 48)
        throw new IOException("Bad encoding in responseBytes element of OCSP response: expected ASN.1 SEQUENCE tag.");
      localDerInputStream1 = localDerValue2.data;
      ObjectIdentifier localObjectIdentifier = localDerInputStream1.getOID();
      if (localObjectIdentifier.equals(OCSP_BASIC_RESPONSE_OID))
      {
        if (DEBUG != null)
          DEBUG.println("OCSP response type: basic");
      }
      else
      {
        if (DEBUG != null)
          DEBUG.println("OCSP response type: " + localObjectIdentifier);
        throw new IOException("Unsupported OCSP response type: " + localObjectIdentifier);
      }
      DerInputStream localDerInputStream2 = new DerInputStream(localDerInputStream1.getOctetString());
      DerValue[] arrayOfDerValue1 = localDerInputStream2.getSequence(2);
      DerValue localDerValue3 = arrayOfDerValue1[0];
      byte[] arrayOfByte2 = arrayOfDerValue1[0].toByteArray();
      if (localDerValue3.tag != 48)
        throw new IOException("Bad encoding in tbsResponseData  element of OCSP response: expected ASN.1 SEQUENCE tag.");
      DerInputStream localDerInputStream3 = localDerValue3.data;
      DerValue localDerValue4 = localDerInputStream3.getDerValue();
      if ((localDerValue4.isContextSpecific(0)) && (localDerValue4.isConstructed()) && (localDerValue4.isContextSpecific()))
      {
        localDerValue4 = localDerValue4.data.getDerValue();
        int j = localDerValue4.getInteger();
        if (localDerValue4.data.available() != 0)
          throw new IOException("Bad encoding in version  element of OCSP response: bad format");
        localDerValue4 = localDerInputStream3.getDerValue();
      }
      int k = (short)(byte)(localDerValue4.tag & 0x1F);
      if (k == 1)
      {
        localCertificateIssuerName = new CertificateIssuerName(localDerValue4.getData());
        if (DEBUG != null)
          DEBUG.println("OCSP Responder name: " + localCertificateIssuerName);
      }
      else
      {
        if (k == 2)
          break label507:
        throw new IOException("Bad encoding in responderID element of OCSP response: expected ASN.1 context specific tag 0 or 1");
      }
      localDerValue4 = localDerInputStream3.getDerValue();
      Date localDate = localDerValue4.getGeneralizedTime();
      DerValue[] arrayOfDerValue2 = localDerInputStream3.getSequence(1);
      this.singleResponse = new SingleResponse(this, arrayOfDerValue2[0], null);
      if (localDerInputStream3.available() > 0)
      {
        localDerValue4 = localDerInputStream3.getDerValue();
        if (localDerValue4.isContextSpecific(1))
        {
          localObject1 = localDerValue4.data.getSequence(3);
          localObject2 = new Extension[localObject1.length];
          for (int l = 0; l < localObject1.length; ++l)
          {
            localObject2[l] = new Extension(localObject1[l]);
            if (DEBUG != null)
              DEBUG.println("OCSP extension: " + localObject2[l]);
            if (localObject2[l].getExtensionId().equals(OCSP_NONCE_EXTENSION_OID))
              byte[] arrayOfByte1 = localObject2[l].getExtensionValue();
            else if (localObject2[l].isCritical())
              throw new IOException("Unsupported OCSP criticial extension: " + localObject2[l].getExtensionId());
          }
        }
      }
      AlgorithmId localAlgorithmId = AlgorithmId.parse(arrayOfDerValue1[1]);
      Object localObject1 = arrayOfDerValue1[2].getBitString();
      Object localObject2 = null;
      if (arrayOfDerValue1.length > 3)
      {
        localDerValue5 = arrayOfDerValue1[3];
        if (!(localDerValue5.isContextSpecific(0)))
          throw new IOException("Bad encoding in certs element of OCSP response: expected ASN.1 context specific tag 0.");
        localObject3 = localDerValue5.getData().getSequence(3);
        localObject2 = new X509CertImpl[localObject3.length];
        for (int i1 = 0; i1 < localObject3.length; ++i1)
          localObject2[i1] = new X509CertImpl(localObject3[i1].toByteArray());
      }
      if ((localObject2 != null) && (localObject2[0] != null))
      {
        localDerValue5 = localObject2[0];
        if (localDerValue5.equals(paramX509Certificate))
          break label954:
        if (localDerValue5.getIssuerDN().equals(paramX509Certificate.getSubjectDN()))
        {
          localObject3 = localDerValue5.getExtendedKeyUsage();
          if ((localObject3 == null) || (!(((List)localObject3).contains("1.3.6.1.5.5.7.3.9"))))
          {
            if (DEBUG != null)
              DEBUG.println("Responder's certificate is not valid for signing OCSP responses.");
            throw new CertPathValidatorException("Responder's certificate not valid for signing OCSP responses");
          }
          try
          {
            localDerValue5.verify(paramX509Certificate.getPublicKey());
            paramX509Certificate = localDerValue5;
          }
          catch (GeneralSecurityException localGeneralSecurityException)
          {
            paramX509Certificate = null;
          }
        }
      }
      if (paramX509Certificate != null)
      {
        label954: if (verifyResponse(arrayOfByte2, paramX509Certificate, localAlgorithmId, localObject1, paramPKIXParameters))
          break label1021;
        if (DEBUG != null)
          DEBUG.println("Error verifying OCSP Responder's signature");
        throw new CertPathValidatorException("Error verifying OCSP Responder's signature");
      }
      if (DEBUG != null)
        DEBUG.println("Unable to verify OCSP Responder's signature");
      label1021: throw new CertPathValidatorException("Unable to verify OCSP Responder's signature");
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

  private boolean verifyResponse(byte[] paramArrayOfByte1, X509Certificate paramX509Certificate, AlgorithmId paramAlgorithmId, byte[] paramArrayOfByte2, PKIXParameters paramPKIXParameters)
    throws SignatureException
  {
    Signature localSignature;
    try
    {
      localSignature = Signature.getInstance(paramAlgorithmId.getName());
      localSignature.initVerify(paramX509Certificate);
      localSignature.update(paramArrayOfByte1);
      if (localSignature.verify(paramArrayOfByte2))
      {
        if (DEBUG != null)
          DEBUG.println("Verified signature of OCSP Responder");
        return true;
      }
      if (DEBUG != null)
        DEBUG.println("Error verifying signature of OCSP Responder");
      return false;
    }
    catch (InvalidKeyException localInvalidKeyException)
    {
      throw new SignatureException(localInvalidKeyException);
    }
    catch (NoSuchAlgorithmException localNoSuchAlgorithmException)
    {
      throw new SignatureException(localNoSuchAlgorithmException);
    }
  }

  int getCertStatus(SerialNumber paramSerialNumber)
  {
    return SingleResponse.access$100(this.singleResponse);
  }

  CertId getCertId()
  {
    return SingleResponse.access$200(this.singleResponse);
  }

  private static String responseToText(int paramInt)
  {
    switch (paramInt)
    {
    case 0:
      return "Successful";
    case 1:
      return "Malformed request";
    case 2:
      return "Internal error";
    case 3:
      return "Try again later";
    case 4:
      return "Unused status code";
    case 5:
      return "Request must be signed";
    case 6:
      return "Request is unauthorized";
    }
    return "Unknown status code: " + paramInt;
  }

  static String certStatusToText(int paramInt)
  {
    switch (paramInt)
    {
    case 0:
      return "Good";
    case 1:
      return "Revoked";
    case 2:
      return "Unknown";
    }
    return "Unknown certificate status code: " + paramInt;
  }

  static
  {
    ObjectIdentifier localObjectIdentifier1 = null;
    ObjectIdentifier localObjectIdentifier2 = null;
    try
    {
      localObjectIdentifier1 = new ObjectIdentifier("1.3.6.1.5.5.7.48.1.1");
      localObjectIdentifier2 = new ObjectIdentifier("1.3.6.1.5.5.7.48.1.2");
    }
    catch (Exception localException)
    {
    }
    OCSP_BASIC_RESPONSE_OID = localObjectIdentifier1;
    OCSP_NONCE_EXTENSION_OID = localObjectIdentifier2;
  }

  private class SingleResponse
  {
    private CertId certId;
    private int certStatus;
    private Date thisUpdate;
    private Date nextUpdate;

    private SingleResponse(, DerValue paramDerValue)
      throws IOException
    {
      if (paramDerValue.tag != 48)
        throw new IOException("Bad ASN.1 encoding in SingleResponse");
      DerInputStream localDerInputStream = paramDerValue.data;
      this.certId = new CertId(localDerInputStream.getDerValue().data);
      DerValue localDerValue = localDerInputStream.getDerValue();
      int i = (short)(byte)(localDerValue.tag & 0x1F);
      if (i == 0)
      {
        this.certStatus = 0;
      }
      else if (i == 1)
      {
        this.certStatus = 1;
        if (OCSPResponse.access$300() != null)
        {
          Date localDate1 = localDerValue.data.getGeneralizedTime();
          OCSPResponse.access$300().println("Revocation time: " + localDate1);
        }
      }
      else if (i == 2)
      {
        this.certStatus = 2;
      }
      else
      {
        throw new IOException("Invalid certificate status");
      }
      this.thisUpdate = localDerInputStream.getGeneralizedTime();
      if (localDerInputStream.available() == 0)
        break label241:
      localDerValue = localDerInputStream.getDerValue();
      i = (short)(byte)(localDerValue.tag & 0x1F);
      if (i == 0)
      {
        this.nextUpdate = localDerValue.data.getGeneralizedTime();
        if (localDerInputStream.available() == 0)
          return;
        localDerValue = localDerInputStream.getDerValue();
        i = (short)(byte)(localDerValue.tag & 0x1F);
      }
      label241: long l = System.currentTimeMillis();
      Date localDate2 = new Date(l + 600000L);
      Date localDate3 = new Date(l - 600000L);
      if (OCSPResponse.access$300() != null)
      {
        String str = "";
        if (this.nextUpdate != null)
          str = " until " + this.nextUpdate;
        OCSPResponse.access$300().println("Response's validity interval is from " + this.thisUpdate + str);
      }
      if (((this.thisUpdate != null) && (localDate2.before(this.thisUpdate))) || ((this.nextUpdate != null) && (localDate3.after(this.nextUpdate))))
      {
        if (OCSPResponse.access$300() != null)
          OCSPResponse.access$300().println("Response is unreliable: its validity interval is out-of-date");
        throw new IOException("Response is unreliable: its validity interval is out-of-date");
      }
    }

    private int getStatus()
    {
      return this.certStatus;
    }

    private CertId getCertId()
    {
      return this.certId;
    }

    public String toString()
    {
      StringBuilder localStringBuilder = new StringBuilder();
      localStringBuilder.append("SingleResponse:  \n");
      localStringBuilder.append(this.certId);
      localStringBuilder.append("\nCertStatus: " + OCSPResponse.certStatusToText(this.this$0.getCertStatus(null)) + "\n");
      localStringBuilder.append("thisUpdate is " + this.thisUpdate + "\n");
      if (this.nextUpdate != null)
        localStringBuilder.append("nextUpdate is " + this.nextUpdate + "\n");
      return localStringBuilder.toString();
    }
  }
}