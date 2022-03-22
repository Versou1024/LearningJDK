package sun.security.provider.certpath;

import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.util.Arrays;
import javax.security.auth.x500.X500Principal;
import sun.misc.HexDumpEncoder;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.x509.AlgorithmId;
import sun.security.x509.SerialNumber;
import sun.security.x509.X509CertImpl;

public class CertId
{
  private static final boolean debug = 0;
  private AlgorithmId hashAlgId;
  private byte[] issuerNameHash;
  private byte[] issuerKeyHash;
  private SerialNumber certSerialNumber;
  private int myhash = -1;

  public CertId(X509CertImpl paramX509CertImpl, SerialNumber paramSerialNumber)
    throws Exception
  {
    MessageDigest localMessageDigest = MessageDigest.getInstance("SHA1");
    this.hashAlgId = AlgorithmId.get("SHA1");
    localMessageDigest.update(paramX509CertImpl.getSubjectX500Principal().getEncoded());
    this.issuerNameHash = localMessageDigest.digest();
    byte[] arrayOfByte1 = paramX509CertImpl.getPublicKey().getEncoded();
    DerValue localDerValue = new DerValue(arrayOfByte1);
    DerValue[] arrayOfDerValue = new DerValue[2];
    arrayOfDerValue[0] = localDerValue.data.getDerValue();
    arrayOfDerValue[1] = localDerValue.data.getDerValue();
    byte[] arrayOfByte2 = arrayOfDerValue[1].getBitString();
    localMessageDigest.update(arrayOfByte2);
    this.issuerKeyHash = localMessageDigest.digest();
    this.certSerialNumber = paramSerialNumber;
  }

  public CertId(DerInputStream paramDerInputStream)
    throws IOException
  {
    this.hashAlgId = AlgorithmId.parse(paramDerInputStream.getDerValue());
    this.issuerNameHash = paramDerInputStream.getOctetString();
    this.issuerKeyHash = paramDerInputStream.getOctetString();
    this.certSerialNumber = new SerialNumber(paramDerInputStream);
  }

  public AlgorithmId getHashAlgorithm()
  {
    return this.hashAlgId;
  }

  public byte[] getIssuerNameHash()
  {
    return this.issuerNameHash;
  }

  public byte[] getIssuerKeyHash()
  {
    return this.issuerKeyHash;
  }

  public BigInteger getSerialNumber()
  {
    return this.certSerialNumber.getNumber();
  }

  public void encode(DerOutputStream paramDerOutputStream)
    throws IOException
  {
    DerOutputStream localDerOutputStream = new DerOutputStream();
    this.hashAlgId.encode(localDerOutputStream);
    localDerOutputStream.putOctetString(this.issuerNameHash);
    localDerOutputStream.putOctetString(this.issuerKeyHash);
    this.certSerialNumber.encode(localDerOutputStream);
    paramDerOutputStream.write(48, localDerOutputStream);
  }

  public int hashCode()
  {
    if (this.myhash == -1)
    {
      this.myhash = this.hashAlgId.hashCode();
      for (int i = 0; i < this.issuerNameHash.length; ++i)
        this.myhash += this.issuerNameHash[i] * i;
      for (i = 0; i < this.issuerKeyHash.length; ++i)
        this.myhash += this.issuerKeyHash[i] * i;
      this.myhash += this.certSerialNumber.getNumber().hashCode();
    }
    return this.myhash;
  }

  public boolean equals(Object paramObject)
  {
    if (this == paramObject)
      return true;
    if ((paramObject == null) || (!(paramObject instanceof CertId)))
      return false;
    CertId localCertId = (CertId)paramObject;
    return ((this.hashAlgId.equals(localCertId.getHashAlgorithm())) && (Arrays.equals(this.issuerNameHash, localCertId.getIssuerNameHash())) && (Arrays.equals(this.issuerKeyHash, localCertId.getIssuerKeyHash())) && (this.certSerialNumber.getNumber().equals(localCertId.getSerialNumber())));
  }

  public String toString()
  {
    StringBuilder localStringBuilder = new StringBuilder();
    localStringBuilder.append("CertId \n");
    localStringBuilder.append("Algorithm: " + this.hashAlgId.toString() + "\n");
    localStringBuilder.append("issuerNameHash \n");
    HexDumpEncoder localHexDumpEncoder = new HexDumpEncoder();
    localStringBuilder.append(localHexDumpEncoder.encode(this.issuerNameHash));
    localStringBuilder.append("\nissuerKeyHash: \n");
    localStringBuilder.append(localHexDumpEncoder.encode(this.issuerKeyHash));
    localStringBuilder.append("\n" + this.certSerialNumber.toString());
    return localStringBuilder.toString();
  }
}