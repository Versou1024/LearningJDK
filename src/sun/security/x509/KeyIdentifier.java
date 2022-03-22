package sun.security.x509;

import B;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.Arrays;
import sun.misc.HexDumpEncoder;
import sun.security.util.BitArray;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class KeyIdentifier
{
  private byte[] octetString;

  public KeyIdentifier(byte[] paramArrayOfByte)
  {
    this.octetString = ((byte[])(byte[])paramArrayOfByte.clone());
  }

  public KeyIdentifier(DerValue paramDerValue)
    throws IOException
  {
    this.octetString = paramDerValue.getOctetString();
  }

  public KeyIdentifier(PublicKey paramPublicKey)
    throws IOException
  {
    DerValue localDerValue = new DerValue(paramPublicKey.getEncoded());
    if (localDerValue.tag != 48)
      throw new IOException("PublicKey value is not a valid X.509 public key");
    AlgorithmId localAlgorithmId = AlgorithmId.parse(localDerValue.data.getDerValue());
    byte[] arrayOfByte = localDerValue.data.getUnalignedBitString().toByteArray();
    MessageDigest localMessageDigest = null;
    try
    {
      localMessageDigest = MessageDigest.getInstance("SHA1");
    }
    catch (NoSuchAlgorithmException localNoSuchAlgorithmException)
    {
      throw new IOException("SHA1 not supported");
    }
    localMessageDigest.update(arrayOfByte);
    this.octetString = localMessageDigest.digest();
  }

  public byte[] getIdentifier()
  {
    return ((byte[])(byte[])this.octetString.clone());
  }

  public String toString()
  {
    String str = "KeyIdentifier [\n";
    HexDumpEncoder localHexDumpEncoder = new HexDumpEncoder();
    str = str + localHexDumpEncoder.encodeBuffer(this.octetString);
    str = str + "]\n";
    return str;
  }

  void encode(DerOutputStream paramDerOutputStream)
    throws IOException
  {
    paramDerOutputStream.putOctetString(this.octetString);
  }

  public int hashCode()
  {
    int i = 0;
    for (int j = 0; j < this.octetString.length; ++j)
      i += this.octetString[j] * j;
    return i;
  }

  public boolean equals(Object paramObject)
  {
    if (this == paramObject)
      return true;
    if (!(paramObject instanceof KeyIdentifier))
      return false;
    return Arrays.equals(this.octetString, ((KeyIdentifier)paramObject).getIdentifier());
  }
}