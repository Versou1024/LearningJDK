package sun.security.pkcs;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.SignatureException;
import java.security.cert.CRLException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.Vector;
import sun.security.util.Debug;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;
import sun.security.x509.AlgorithmId;
import sun.security.x509.X500Name;
import sun.security.x509.X509CRLImpl;
import sun.security.x509.X509CertImpl;
import sun.security.x509.X509CertInfo;

public class PKCS7
{
  private ObjectIdentifier contentType;
  private BigInteger version = null;
  private AlgorithmId[] digestAlgorithmIds = null;
  private ContentInfo contentInfo = null;
  private X509Certificate[] certificates = null;
  private X509CRL[] crls = null;
  private SignerInfo[] signerInfos = null;
  private boolean oldStyle = false;
  private Principal[] certIssuerNames;

  public PKCS7(InputStream paramInputStream)
    throws sun.security.pkcs.ParsingException, IOException
  {
    DataInputStream localDataInputStream = new DataInputStream(paramInputStream);
    byte[] arrayOfByte = new byte[localDataInputStream.available()];
    localDataInputStream.readFully(arrayOfByte);
    parse(new DerInputStream(arrayOfByte));
  }

  public PKCS7(DerInputStream paramDerInputStream)
    throws sun.security.pkcs.ParsingException
  {
    parse(paramDerInputStream);
  }

  public PKCS7(byte[] paramArrayOfByte)
    throws sun.security.pkcs.ParsingException
  {
    try
    {
      DerInputStream localDerInputStream = new DerInputStream(paramArrayOfByte);
      parse(localDerInputStream);
    }
    catch (IOException localIOException)
    {
      ParsingException localParsingException = new sun.security.pkcs.ParsingException("Unable to parse the encoded bytes");
      localParsingException.initCause(localIOException);
      throw localParsingException;
    }
  }

  private void parse(DerInputStream paramDerInputStream)
    throws sun.security.pkcs.ParsingException
  {
    try
    {
      paramDerInputStream.mark(paramDerInputStream.available());
      parse(paramDerInputStream, false);
    }
    catch (IOException localIOException1)
    {
      try
      {
        paramDerInputStream.reset();
        parse(paramDerInputStream, true);
        this.oldStyle = true;
      }
      catch (IOException localIOException2)
      {
        ParsingException localParsingException = new sun.security.pkcs.ParsingException(localIOException2.getMessage());
        localParsingException.initCause(localIOException2);
        throw localParsingException;
      }
    }
  }

  private void parse(DerInputStream paramDerInputStream, boolean paramBoolean)
    throws IOException
  {
    this.contentInfo = new ContentInfo(paramDerInputStream, paramBoolean);
    this.contentType = this.contentInfo.contentType;
    DerValue localDerValue = this.contentInfo.getContent();
    if (this.contentType.equals(ContentInfo.SIGNED_DATA_OID))
      parseSignedData(localDerValue);
    else if (this.contentType.equals(ContentInfo.OLD_SIGNED_DATA_OID))
      parseOldSignedData(localDerValue);
    else if (this.contentType.equals(ContentInfo.NETSCAPE_CERT_SEQUENCE_OID))
      parseNetscapeCertChain(localDerValue);
    else
      throw new sun.security.pkcs.ParsingException("content type " + this.contentType + " not supported.");
  }

  public PKCS7(AlgorithmId[] paramArrayOfAlgorithmId, ContentInfo paramContentInfo, X509Certificate[] paramArrayOfX509Certificate, SignerInfo[] paramArrayOfSignerInfo)
  {
    this.version = BigInteger.ONE;
    this.digestAlgorithmIds = paramArrayOfAlgorithmId;
    this.contentInfo = paramContentInfo;
    this.certificates = paramArrayOfX509Certificate;
    this.signerInfos = paramArrayOfSignerInfo;
  }

  private void parseNetscapeCertChain(DerValue paramDerValue)
    throws sun.security.pkcs.ParsingException, IOException
  {
    DerInputStream localDerInputStream = new DerInputStream(paramDerValue.toByteArray());
    DerValue[] arrayOfDerValue = localDerInputStream.getSequence(2);
    this.certificates = new X509Certificate[arrayOfDerValue.length];
    CertificateFactory localCertificateFactory = null;
    try
    {
      localCertificateFactory = CertificateFactory.getInstance("X.509");
    }
    catch (CertificateException localCertificateException1)
    {
    }
    for (int i = 0; i < arrayOfDerValue.length; ++i)
    {
      ParsingException localParsingException;
      ByteArrayInputStream localByteArrayInputStream = null;
      try
      {
        if (localCertificateFactory == null)
        {
          this.certificates[i] = new X509CertImpl(arrayOfDerValue[i]);
        }
        else
        {
          byte[] arrayOfByte = arrayOfDerValue[i].toByteArray();
          localByteArrayInputStream = new ByteArrayInputStream(arrayOfByte);
          this.certificates[i] = ((X509Certificate)localCertificateFactory.generateCertificate(localByteArrayInputStream));
          localByteArrayInputStream.close();
          localByteArrayInputStream = null;
        }
      }
      catch (CertificateException localCertificateException2)
      {
        localParsingException = new sun.security.pkcs.ParsingException(localCertificateException2.getMessage());
        throw localParsingException;
      }
      catch (IOException localIOException)
      {
        localParsingException = new sun.security.pkcs.ParsingException(localIOException.getMessage());
        throw localParsingException;
      }
      finally
      {
        if (localByteArrayInputStream != null)
          localByteArrayInputStream.close();
      }
    }
  }

  private void parseSignedData(DerValue paramDerValue)
    throws sun.security.pkcs.ParsingException, IOException
  {
    Object localObject1;
    Object localObject2;
    ParsingException localParsingException;
    DerInputStream localDerInputStream = paramDerValue.toDerInputStream();
    this.version = localDerInputStream.getBigInteger();
    DerValue[] arrayOfDerValue1 = localDerInputStream.getSet(1);
    int i = arrayOfDerValue1.length;
    this.digestAlgorithmIds = new AlgorithmId[i];
    try
    {
      for (int j = 0; j < i; ++j)
      {
        localObject1 = arrayOfDerValue1[j];
        this.digestAlgorithmIds[j] = AlgorithmId.parse((DerValue)localObject1);
      }
    }
    catch (IOException localIOException1)
    {
      localObject1 = new sun.security.pkcs.ParsingException("Error parsing digest AlgorithmId IDs: " + localIOException1.getMessage());
      ((sun.security.pkcs.ParsingException)localObject1).initCause(localIOException1);
      throw ((Throwable)localObject1);
    }
    this.contentInfo = new ContentInfo(localDerInputStream);
    CertificateFactory localCertificateFactory = null;
    try
    {
      localCertificateFactory = CertificateFactory.getInstance("X.509");
    }
    catch (CertificateException localCertificateException1)
    {
    }
    if ((byte)localDerInputStream.peekByte() == -96)
    {
      arrayOfDerValue2 = localDerInputStream.getSet(2, true);
      i = arrayOfDerValue2.length;
      this.certificates = new X509Certificate[i];
      for (k = 0; k < i; ++k)
      {
        localObject2 = null;
        try
        {
          if (localCertificateFactory == null)
          {
            this.certificates[k] = new X509CertImpl(arrayOfDerValue2[k]);
          }
          else
          {
            byte[] arrayOfByte1 = arrayOfDerValue2[k].toByteArray();
            localObject2 = new ByteArrayInputStream(arrayOfByte1);
            this.certificates[k] = ((X509Certificate)localCertificateFactory.generateCertificate((InputStream)localObject2));
            ((ByteArrayInputStream)localObject2).close();
            localObject2 = null;
          }
        }
        catch (CertificateException localCertificateException2)
        {
          localParsingException = new sun.security.pkcs.ParsingException(localCertificateException2.getMessage());
          throw localParsingException;
        }
        catch (IOException localIOException2)
        {
          localParsingException = new sun.security.pkcs.ParsingException(localIOException2.getMessage());
          throw localParsingException;
        }
        finally
        {
          if (localObject2 != null)
            ((ByteArrayInputStream)localObject2).close();
        }
      }
    }
    if ((byte)localDerInputStream.peekByte() == -95)
    {
      arrayOfDerValue2 = localDerInputStream.getSet(1, true);
      i = arrayOfDerValue2.length;
      this.crls = new X509CRL[i];
      for (k = 0; k < i; ++k)
      {
        localObject2 = null;
        try
        {
          if (localCertificateFactory == null)
          {
            this.crls[k] = new X509CRLImpl(arrayOfDerValue2[k]);
          }
          else
          {
            byte[] arrayOfByte2 = arrayOfDerValue2[k].toByteArray();
            localObject2 = new ByteArrayInputStream(arrayOfByte2);
            this.crls[k] = ((X509CRL)localCertificateFactory.generateCRL((InputStream)localObject2));
            ((ByteArrayInputStream)localObject2).close();
            localObject2 = null;
          }
        }
        catch (CRLException localCRLException)
        {
          localParsingException = new sun.security.pkcs.ParsingException(localCRLException.getMessage());
          throw localParsingException;
        }
        finally
        {
          if (localObject2 != null)
            ((ByteArrayInputStream)localObject2).close();
        }
      }
    }
    DerValue[] arrayOfDerValue2 = localDerInputStream.getSet(1);
    i = arrayOfDerValue2.length;
    this.signerInfos = new SignerInfo[i];
    for (int k = 0; k < i; ++k)
    {
      localObject2 = arrayOfDerValue2[k].toDerInputStream();
      this.signerInfos[k] = new SignerInfo((DerInputStream)localObject2);
    }
  }

  private void parseOldSignedData(DerValue paramDerValue)
    throws sun.security.pkcs.ParsingException, IOException
  {
    DerInputStream localDerInputStream1 = paramDerValue.toDerInputStream();
    this.version = localDerInputStream1.getBigInteger();
    DerValue[] arrayOfDerValue1 = localDerInputStream1.getSet(1);
    int i = arrayOfDerValue1.length;
    this.digestAlgorithmIds = new AlgorithmId[i];
    try
    {
      for (int j = 0; j < i; ++j)
      {
        DerValue localDerValue = arrayOfDerValue1[j];
        this.digestAlgorithmIds[j] = AlgorithmId.parse(localDerValue);
      }
    }
    catch (IOException localIOException1)
    {
      throw new sun.security.pkcs.ParsingException("Error parsing digest AlgorithmId IDs");
    }
    this.contentInfo = new ContentInfo(localDerInputStream1, true);
    CertificateFactory localCertificateFactory = null;
    try
    {
      localCertificateFactory = CertificateFactory.getInstance("X.509");
    }
    catch (CertificateException localCertificateException1)
    {
    }
    DerValue[] arrayOfDerValue2 = localDerInputStream1.getSet(2);
    i = arrayOfDerValue2.length;
    this.certificates = new X509Certificate[i];
    for (int k = 0; k < i; ++k)
    {
      ParsingException localParsingException;
      ByteArrayInputStream localByteArrayInputStream = null;
      try
      {
        if (localCertificateFactory == null)
        {
          this.certificates[k] = new X509CertImpl(arrayOfDerValue2[k]);
        }
        else
        {
          byte[] arrayOfByte = arrayOfDerValue2[k].toByteArray();
          localByteArrayInputStream = new ByteArrayInputStream(arrayOfByte);
          this.certificates[k] = ((X509Certificate)localCertificateFactory.generateCertificate(localByteArrayInputStream));
          localByteArrayInputStream.close();
          localByteArrayInputStream = null;
        }
      }
      catch (CertificateException localCertificateException2)
      {
        localParsingException = new sun.security.pkcs.ParsingException(localCertificateException2.getMessage());
        throw localParsingException;
      }
      catch (IOException localIOException2)
      {
        localParsingException = new sun.security.pkcs.ParsingException(localIOException2.getMessage());
        throw localParsingException;
      }
      finally
      {
        if (localByteArrayInputStream != null)
          localByteArrayInputStream.close();
      }
    }
    localDerInputStream1.getSet(0);
    DerValue[] arrayOfDerValue3 = localDerInputStream1.getSet(1);
    i = arrayOfDerValue3.length;
    this.signerInfos = new SignerInfo[i];
    for (int l = 0; l < i; ++l)
    {
      DerInputStream localDerInputStream2 = arrayOfDerValue3[l].toDerInputStream();
      this.signerInfos[l] = new SignerInfo(localDerInputStream2, true);
    }
  }

  public void encodeSignedData(OutputStream paramOutputStream)
    throws IOException
  {
    DerOutputStream localDerOutputStream = new DerOutputStream();
    encodeSignedData(localDerOutputStream);
    paramOutputStream.write(localDerOutputStream.toByteArray());
  }

  public void encodeSignedData(DerOutputStream paramDerOutputStream)
    throws IOException
  {
    DerOutputStream localDerOutputStream = new DerOutputStream();
    localDerOutputStream.putInteger(this.version);
    localDerOutputStream.putOrderedSetOf(49, this.digestAlgorithmIds);
    this.contentInfo.encode(localDerOutputStream);
    if ((this.certificates != null) && (this.certificates.length != 0))
    {
      localObject = new X509CertImpl[this.certificates.length];
      for (int i = 0; i < this.certificates.length; ++i)
        if (this.certificates[i] instanceof X509CertImpl)
          localObject[i] = ((X509CertImpl)this.certificates[i]);
        else
          try
          {
            byte[] arrayOfByte = this.certificates[i].getEncoded();
            localObject[i] = new X509CertImpl(arrayOfByte);
          }
          catch (CertificateException localCertificateException)
          {
            IOException localIOException = new IOException(localCertificateException.getMessage());
            localIOException.initCause(localCertificateException);
            throw localIOException;
          }
      localDerOutputStream.putOrderedSetOf(-96, localObject);
    }
    localDerOutputStream.putOrderedSetOf(49, this.signerInfos);
    Object localObject = new DerValue(48, localDerOutputStream.toByteArray());
    ContentInfo localContentInfo = new ContentInfo(ContentInfo.SIGNED_DATA_OID, (DerValue)localObject);
    localContentInfo.encode(paramDerOutputStream);
  }

  public SignerInfo verify(SignerInfo paramSignerInfo, byte[] paramArrayOfByte)
    throws NoSuchAlgorithmException, SignatureException
  {
    return paramSignerInfo.verify(this, paramArrayOfByte);
  }

  public SignerInfo[] verify(byte[] paramArrayOfByte)
    throws NoSuchAlgorithmException, SignatureException
  {
    Vector localVector = new Vector();
    for (int i = 0; i < this.signerInfos.length; ++i)
    {
      SignerInfo localSignerInfo = verify(this.signerInfos[i], paramArrayOfByte);
      if (localSignerInfo != null)
        localVector.addElement(localSignerInfo);
    }
    if (localVector.size() != 0)
    {
      SignerInfo[] arrayOfSignerInfo = new SignerInfo[localVector.size()];
      localVector.copyInto(arrayOfSignerInfo);
      return arrayOfSignerInfo;
    }
    return null;
  }

  public SignerInfo[] verify()
    throws NoSuchAlgorithmException, SignatureException
  {
    return verify(null);
  }

  public BigInteger getVersion()
  {
    return this.version;
  }

  public AlgorithmId[] getDigestAlgorithmIds()
  {
    return this.digestAlgorithmIds;
  }

  public ContentInfo getContentInfo()
  {
    return this.contentInfo;
  }

  public X509Certificate[] getCertificates()
  {
    if (this.certificates != null)
      return ((X509Certificate[])(X509Certificate[])this.certificates.clone());
    return null;
  }

  public X509CRL[] getCRLs()
  {
    if (this.crls != null)
      return ((X509CRL[])(X509CRL[])this.crls.clone());
    return null;
  }

  public SignerInfo[] getSignerInfos()
  {
    return this.signerInfos;
  }

  public X509Certificate getCertificate(BigInteger paramBigInteger, X500Name paramX500Name)
  {
    if (this.certificates != null)
    {
      if (this.certIssuerNames == null)
        populateCertIssuerNames();
      for (int i = 0; i < this.certificates.length; ++i)
      {
        X509Certificate localX509Certificate = this.certificates[i];
        BigInteger localBigInteger = localX509Certificate.getSerialNumber();
        if ((paramBigInteger.equals(localBigInteger)) && (paramX500Name.equals(this.certIssuerNames[i])))
          return localX509Certificate;
      }
    }
    return null;
  }

  private void populateCertIssuerNames()
  {
    if (this.certificates == null)
      return;
    this.certIssuerNames = new Principal[this.certificates.length];
    for (int i = 0; i < this.certificates.length; ++i)
    {
      X509Certificate localX509Certificate = this.certificates[i];
      Principal localPrincipal = localX509Certificate.getIssuerDN();
      if (!(localPrincipal instanceof X500Name))
        try
        {
          X509CertInfo localX509CertInfo = new X509CertInfo(localX509Certificate.getTBSCertificate());
          localPrincipal = (Principal)localX509CertInfo.get("issuer.dname");
        }
        catch (Exception localException)
        {
        }
      this.certIssuerNames[i] = localPrincipal;
    }
  }

  public String toString()
  {
    int i;
    String str = "";
    str = str + this.contentInfo + "\n";
    if (this.version != null)
      str = str + "PKCS7 :: version: " + Debug.toHexString(this.version) + "\n";
    if (this.digestAlgorithmIds != null)
    {
      str = str + "PKCS7 :: digest AlgorithmIds: \n";
      for (i = 0; i < this.digestAlgorithmIds.length; ++i)
        str = str + "\t" + this.digestAlgorithmIds[i] + "\n";
    }
    if (this.certificates != null)
    {
      str = str + "PKCS7 :: certificates: \n";
      for (i = 0; i < this.certificates.length; ++i)
        str = str + "\t" + i + ".   " + this.certificates[i] + "\n";
    }
    if (this.crls != null)
    {
      str = str + "PKCS7 :: crls: \n";
      for (i = 0; i < this.crls.length; ++i)
        str = str + "\t" + i + ".   " + this.crls[i] + "\n";
    }
    if (this.signerInfos != null)
    {
      str = str + "PKCS7 :: signer infos: \n";
      for (i = 0; i < this.signerInfos.length; ++i)
        str = str + "\t" + i + ".  " + this.signerInfos[i] + "\n";
    }
    return str;
  }

  public boolean isOldStyle()
  {
    return this.oldStyle;
  }
}