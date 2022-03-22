package sun.security.timestamp;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Date;
import sun.security.util.DerInputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;
import sun.security.x509.AlgorithmId;

public class TimestampToken
{
  private int version;
  private ObjectIdentifier policy;
  private BigInteger serialNumber;
  private AlgorithmId hashAlgorithm;
  private byte[] hashedMessage;
  private Date genTime;

  public TimestampToken(byte[] paramArrayOfByte)
    throws IOException
  {
    if (paramArrayOfByte == null)
      throw new IOException("No timestamp token info");
    parse(paramArrayOfByte);
  }

  public Date getDate()
  {
    return this.genTime;
  }

  private void parse(byte[] paramArrayOfByte)
    throws IOException
  {
    DerValue localDerValue1 = new DerValue(paramArrayOfByte);
    if (localDerValue1.tag != 48)
      throw new IOException("Bad encoding for timestamp token info");
    this.version = localDerValue1.data.getInteger();
    this.policy = localDerValue1.data.getOID();
    DerValue localDerValue2 = localDerValue1.data.getDerValue();
    this.hashAlgorithm = AlgorithmId.parse(localDerValue2.data.getDerValue());
    this.hashedMessage = localDerValue2.data.getOctetString();
    this.serialNumber = localDerValue1.data.getBigInteger();
    this.genTime = localDerValue1.data.getGeneralizedTime();
    if (localDerValue1.data.available() > 0);
  }
}