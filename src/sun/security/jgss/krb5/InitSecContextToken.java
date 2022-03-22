package sun.security.jgss.krb5;

import java.io.IOException;
import java.io.InputStream;
import org.ietf.jgss.GSSException;
import sun.security.krb5.Checksum;
import sun.security.krb5.Credentials;
import sun.security.krb5.EncryptionKey;
import sun.security.krb5.KrbApReq;
import sun.security.krb5.KrbException;
import sun.security.util.DerValue;

class InitSecContextToken extends InitialToken
{
  private KrbApReq apReq = null;

  InitSecContextToken(Krb5Context paramKrb5Context, Credentials paramCredentials1, Credentials paramCredentials2)
    throws KrbException, IOException, GSSException
  {
    boolean bool1 = paramKrb5Context.getMutualAuthState();
    boolean bool2 = true;
    boolean bool3 = true;
    InitialToken.OverloadedChecksum localOverloadedChecksum = new InitialToken.OverloadedChecksum(this, paramKrb5Context, paramCredentials1, paramCredentials2);
    Checksum localChecksum = localOverloadedChecksum.getChecksum();
    this.apReq = new KrbApReq(paramCredentials2, bool1, bool2, bool3, localChecksum);
    paramKrb5Context.resetMySequenceNumber(this.apReq.getSeqNumber().intValue());
    EncryptionKey localEncryptionKey = this.apReq.getSubKey();
    if (localEncryptionKey != null)
      paramKrb5Context.setKey(localEncryptionKey);
    else
      paramKrb5Context.setKey(paramCredentials2.getSessionKey());
    if (!(bool1))
      paramKrb5Context.resetPeerSequenceNumber(0);
  }

  InitSecContextToken(Krb5Context paramKrb5Context, EncryptionKey[] paramArrayOfEncryptionKey, InputStream paramInputStream)
    throws IOException, GSSException, KrbException
  {
    int i = paramInputStream.read() << 8 | paramInputStream.read();
    if (i != 256)
      throw new GSSException(10, -1, "AP_REQ token id does not match!");
    byte[] arrayOfByte = new DerValue(paramInputStream).toByteArray();
    this.apReq = new KrbApReq(arrayOfByte, paramArrayOfEncryptionKey);
    EncryptionKey localEncryptionKey1 = this.apReq.getCreds().getSessionKey();
    EncryptionKey localEncryptionKey2 = this.apReq.getSubKey();
    if (localEncryptionKey2 != null)
      paramKrb5Context.setKey(localEncryptionKey2);
    else
      paramKrb5Context.setKey(localEncryptionKey1);
    InitialToken.OverloadedChecksum localOverloadedChecksum = new InitialToken.OverloadedChecksum(this, paramKrb5Context, this.apReq.getChecksum(), localEncryptionKey1);
    localOverloadedChecksum.setContextFlags(paramKrb5Context);
    Credentials localCredentials = localOverloadedChecksum.getDelegatedCreds();
    if (localCredentials != null)
    {
      localObject = Krb5InitCredential.getInstance((Krb5NameElement)paramKrb5Context.getSrcName(), localCredentials);
      paramKrb5Context.setDelegCred((Krb5CredElement)localObject);
    }
    Object localObject = this.apReq.getSeqNumber();
    int j = (localObject != null) ? ((Integer)localObject).intValue() : 0;
    paramKrb5Context.resetPeerSequenceNumber(j);
    if (!(paramKrb5Context.getMutualAuthState()))
      paramKrb5Context.resetMySequenceNumber(j);
  }

  public final KrbApReq getKrbApReq()
  {
    return this.apReq;
  }

  public final byte[] encode()
    throws IOException
  {
    byte[] arrayOfByte1 = this.apReq.getMessage();
    byte[] arrayOfByte2 = new byte[2 + arrayOfByte1.length];
    writeInt(256, arrayOfByte2, 0);
    System.arraycopy(arrayOfByte1, 0, arrayOfByte2, 2, arrayOfByte1.length);
    return arrayOfByte2;
  }
}