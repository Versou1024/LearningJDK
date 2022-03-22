package sun.security.timestamp;

import java.io.IOException;
import java.math.BigInteger;
import java.security.cert.X509Extension;
import sun.security.util.DerOutputStream;
import sun.security.util.ObjectIdentifier;

public class TSRequest
{
  private static final ObjectIdentifier SHA1_OID;
  private static final ObjectIdentifier MD5_OID;
  private int version = 1;
  private ObjectIdentifier hashAlgorithmId = null;
  private byte[] hashValue;
  private String policyId = null;
  private BigInteger nonce = null;
  private boolean returnCertificate = false;
  private X509Extension[] extensions = null;

  public TSRequest(byte[] paramArrayOfByte, String paramString)
  {
    if ("MD5".equalsIgnoreCase(paramString))
    {
      this.hashAlgorithmId = MD5_OID;
      if (($assertionsDisabled) || (paramArrayOfByte.length == 16))
        break label126;
      throw new AssertionError();
    }
    if (("SHA-1".equalsIgnoreCase(paramString)) || ("SHA".equalsIgnoreCase(paramString)) || ("SHA1".equalsIgnoreCase(paramString)))
    {
      this.hashAlgorithmId = SHA1_OID;
      if ((!($assertionsDisabled)) && (paramArrayOfByte.length != 20))
        throw new AssertionError();
    }
    label126: this.hashValue = new byte[paramArrayOfByte.length];
    System.arraycopy(paramArrayOfByte, 0, this.hashValue, 0, paramArrayOfByte.length);
  }

  public void setVersion(int paramInt)
  {
    this.version = paramInt;
  }

  public void setPolicyId(String paramString)
  {
    this.policyId = paramString;
  }

  public void setNonce(BigInteger paramBigInteger)
  {
    this.nonce = paramBigInteger;
  }

  public void requestCertificate(boolean paramBoolean)
  {
    this.returnCertificate = paramBoolean;
  }

  public void setExtensions(X509Extension[] paramArrayOfX509Extension)
  {
    this.extensions = paramArrayOfX509Extension;
  }

  public byte[] encode()
    throws IOException
  {
    DerOutputStream localDerOutputStream1 = new DerOutputStream();
    localDerOutputStream1.putInteger(this.version);
    DerOutputStream localDerOutputStream2 = new DerOutputStream();
    DerOutputStream localDerOutputStream3 = new DerOutputStream();
    localDerOutputStream3.putOID(this.hashAlgorithmId);
    localDerOutputStream2.write(48, localDerOutputStream3);
    localDerOutputStream2.putOctetString(this.hashValue);
    localDerOutputStream1.write(48, localDerOutputStream2);
    if (this.policyId != null)
      localDerOutputStream1.putOID(new ObjectIdentifier(this.policyId));
    if (this.nonce != null)
      localDerOutputStream1.putInteger(this.nonce);
    if (this.returnCertificate)
      localDerOutputStream1.putBoolean(true);
    DerOutputStream localDerOutputStream4 = new DerOutputStream();
    localDerOutputStream4.write(48, localDerOutputStream1);
    return localDerOutputStream4.toByteArray();
  }

  static
  {
    ObjectIdentifier localObjectIdentifier1 = null;
    ObjectIdentifier localObjectIdentifier2 = null;
    try
    {
      localObjectIdentifier1 = new ObjectIdentifier("1.3.14.3.2.26");
      localObjectIdentifier2 = new ObjectIdentifier("1.2.840.113549.2.5");
    }
    catch (IOException localIOException)
    {
    }
    SHA1_OID = localObjectIdentifier1;
    MD5_OID = localObjectIdentifier2;
  }
}