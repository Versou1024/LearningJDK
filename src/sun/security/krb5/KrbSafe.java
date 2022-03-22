package sun.security.krb5;

import java.io.IOException;
import sun.security.krb5.internal.HostAddress;
import sun.security.krb5.internal.KRBSafe;
import sun.security.krb5.internal.KRBSafeBody;
import sun.security.krb5.internal.KdcErrException;
import sun.security.krb5.internal.KerberosTime;
import sun.security.krb5.internal.KrbApErrException;
import sun.security.krb5.internal.SeqNumber;

class KrbSafe extends KrbAppMessage
{
  private byte[] obuf;
  private byte[] userData;

  public KrbSafe(byte[] paramArrayOfByte, Credentials paramCredentials, EncryptionKey paramEncryptionKey, KerberosTime paramKerberosTime, SeqNumber paramSeqNumber, HostAddress paramHostAddress1, HostAddress paramHostAddress2)
    throws sun.security.krb5.KrbException, IOException
  {
    EncryptionKey localEncryptionKey = null;
    if (paramEncryptionKey != null)
      localEncryptionKey = paramEncryptionKey;
    else
      localEncryptionKey = paramCredentials.key;
    this.obuf = mk_safe(paramArrayOfByte, localEncryptionKey, paramKerberosTime, paramSeqNumber, paramHostAddress1, paramHostAddress2);
  }

  public KrbSafe(byte[] paramArrayOfByte, Credentials paramCredentials, EncryptionKey paramEncryptionKey, SeqNumber paramSeqNumber, HostAddress paramHostAddress1, HostAddress paramHostAddress2, boolean paramBoolean1, boolean paramBoolean2)
    throws sun.security.krb5.KrbException, IOException
  {
    KRBSafe localKRBSafe = new KRBSafe(paramArrayOfByte);
    EncryptionKey localEncryptionKey = null;
    if (paramEncryptionKey != null)
      localEncryptionKey = paramEncryptionKey;
    else
      localEncryptionKey = paramCredentials.key;
    this.userData = rd_safe(localKRBSafe, localEncryptionKey, paramSeqNumber, paramHostAddress1, paramHostAddress2, paramBoolean1, paramBoolean2, paramCredentials.client, paramCredentials.client.getRealm());
  }

  public byte[] getMessage()
  {
    return this.obuf;
  }

  public byte[] getData()
  {
    return this.userData;
  }

  private byte[] mk_safe(byte[] paramArrayOfByte, EncryptionKey paramEncryptionKey, KerberosTime paramKerberosTime, SeqNumber paramSeqNumber, HostAddress paramHostAddress1, HostAddress paramHostAddress2)
    throws sun.security.krb5.Asn1Exception, IOException, KdcErrException, KrbApErrException, sun.security.krb5.KrbCryptoException
  {
    Integer localInteger1 = null;
    Integer localInteger2 = null;
    if (paramKerberosTime != null)
      localInteger1 = new Integer(paramKerberosTime.getMicroSeconds());
    if (paramSeqNumber != null)
    {
      localInteger2 = new Integer(paramSeqNumber.current());
      paramSeqNumber.step();
    }
    KRBSafeBody localKRBSafeBody = new KRBSafeBody(paramArrayOfByte, paramKerberosTime, localInteger1, localInteger2, paramHostAddress1, paramHostAddress2);
    byte[] arrayOfByte = localKRBSafeBody.asn1Encode();
    Checksum localChecksum = new Checksum(Checksum.SAFECKSUMTYPE_DEFAULT, arrayOfByte, paramEncryptionKey, 15);
    KRBSafe localKRBSafe = new KRBSafe(localKRBSafeBody, localChecksum);
    arrayOfByte = localKRBSafe.asn1Encode();
    return localKRBSafe.asn1Encode();
  }

  private byte[] rd_safe(KRBSafe paramKRBSafe, EncryptionKey paramEncryptionKey, SeqNumber paramSeqNumber, HostAddress paramHostAddress1, HostAddress paramHostAddress2, boolean paramBoolean1, boolean paramBoolean2, PrincipalName paramPrincipalName, Realm paramRealm)
    throws sun.security.krb5.Asn1Exception, KdcErrException, KrbApErrException, IOException, sun.security.krb5.KrbCryptoException
  {
    byte[] arrayOfByte = paramKRBSafe.safeBody.asn1Encode();
    if (!(paramKRBSafe.cksum.verifyKeyedChecksum(arrayOfByte, paramEncryptionKey, 15)))
      throw new KrbApErrException(41);
    check(paramKRBSafe.safeBody.timestamp, paramKRBSafe.safeBody.usec, paramKRBSafe.safeBody.seqNumber, paramKRBSafe.safeBody.sAddress, paramKRBSafe.safeBody.rAddress, paramSeqNumber, paramHostAddress1, paramHostAddress2, paramBoolean1, paramBoolean2, paramPrincipalName, paramRealm);
    return paramKRBSafe.safeBody.userData;
  }
}