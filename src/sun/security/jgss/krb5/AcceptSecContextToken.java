package sun.security.jgss.krb5;

import java.io.IOException;
import java.io.InputStream;
import org.ietf.jgss.GSSException;
import sun.security.krb5.Credentials;
import sun.security.krb5.EncryptionKey;
import sun.security.krb5.KrbApRep;
import sun.security.krb5.KrbApReq;
import sun.security.krb5.KrbException;
import sun.security.util.DerValue;

class AcceptSecContextToken extends InitialToken
{
  private KrbApRep apRep = null;

  public AcceptSecContextToken(Krb5Context paramKrb5Context, KrbApReq paramKrbApReq)
    throws KrbException, IOException
  {
    boolean bool1 = false;
    boolean bool2 = true;
    this.apRep = new KrbApRep(paramKrbApReq, bool2, bool1);
    paramKrb5Context.resetMySequenceNumber(this.apRep.getSeqNumber().intValue());
  }

  public AcceptSecContextToken(Krb5Context paramKrb5Context, Credentials paramCredentials, KrbApReq paramKrbApReq, InputStream paramInputStream)
    throws IOException, GSSException, KrbException
  {
    int i = paramInputStream.read() << 8 | paramInputStream.read();
    if (i != 512)
      throw new GSSException(10, -1, "AP_REP token id does not match!");
    byte[] arrayOfByte = new DerValue(paramInputStream).toByteArray();
    KrbApRep localKrbApRep = new KrbApRep(arrayOfByte, paramCredentials, paramKrbApReq);
    EncryptionKey localEncryptionKey = localKrbApRep.getSubKey();
    if (localEncryptionKey != null)
      paramKrb5Context.setKey(localEncryptionKey);
    Integer localInteger = localKrbApRep.getSeqNumber();
    int j = (localInteger != null) ? localInteger.intValue() : 0;
    paramKrb5Context.resetPeerSequenceNumber(j);
  }

  public final byte[] encode()
    throws IOException
  {
    byte[] arrayOfByte1 = this.apRep.getMessage();
    byte[] arrayOfByte2 = new byte[2 + arrayOfByte1.length];
    writeInt(512, arrayOfByte2, 0);
    System.arraycopy(arrayOfByte1, 0, arrayOfByte2, 2, arrayOfByte1.length);
    return arrayOfByte2;
  }
}