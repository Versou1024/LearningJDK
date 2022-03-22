package sun.security.krb5;

import java.io.IOException;
import sun.security.krb5.internal.APRep;
import sun.security.krb5.internal.EncAPRepPart;
import sun.security.krb5.internal.KRBError;
import sun.security.krb5.internal.KdcErrException;
import sun.security.krb5.internal.KerberosTime;
import sun.security.krb5.internal.KrbApErrException;
import sun.security.krb5.internal.LocalSeqNumber;
import sun.security.krb5.internal.SeqNumber;
import sun.security.util.DerValue;

public class KrbApRep
{
  private byte[] obuf;
  private byte[] ibuf;
  private EncAPRepPart encPart;
  private APRep apRepMessg;

  public KrbApRep(KrbApReq paramKrbApReq, boolean paramBoolean1, boolean paramBoolean2)
    throws KrbException, IOException
  {
    EncryptionKey localEncryptionKey = (paramBoolean2) ? new EncryptionKey(paramKrbApReq.getCreds().getSessionKey()) : null;
    LocalSeqNumber localLocalSeqNumber = new LocalSeqNumber();
    init(paramKrbApReq, localEncryptionKey, localLocalSeqNumber);
  }

  public KrbApRep(byte[] paramArrayOfByte, Credentials paramCredentials, KrbApReq paramKrbApReq)
    throws KrbException, IOException
  {
    this(paramArrayOfByte, paramCredentials);
    authenticate(paramKrbApReq);
  }

  private void init(KrbApReq paramKrbApReq, EncryptionKey paramEncryptionKey, SeqNumber paramSeqNumber)
    throws KrbException, IOException
  {
    createMessage(paramKrbApReq.getCreds().key, paramKrbApReq.getCtime(), paramKrbApReq.cusec(), paramEncryptionKey, paramSeqNumber);
    this.obuf = this.apRepMessg.asn1Encode();
  }

  private KrbApRep(byte[] paramArrayOfByte, Credentials paramCredentials)
    throws KrbException, IOException
  {
    this(new DerValue(paramArrayOfByte), paramCredentials);
  }

  private KrbApRep(DerValue paramDerValue, Credentials paramCredentials)
    throws KrbException, IOException
  {
    APRep localAPRep = null;
    try
    {
      localAPRep = new APRep(paramDerValue);
    }
    catch (Asn1Exception localAsn1Exception)
    {
      String str2;
      localAPRep = null;
      localObject = new KRBError(paramDerValue);
      String str1 = ((KRBError)localObject).getErrorString();
      if (str1.charAt(str1.length() - 1) == 0)
        str2 = str1.substring(0, str1.length() - 1);
      else
        str2 = str1;
      KrbException localKrbException = new KrbException(((KRBError)localObject).getErrorCode(), str2);
      localKrbException.initCause(localAsn1Exception);
      throw localKrbException;
    }
    byte[] arrayOfByte = localAPRep.encPart.decrypt(paramCredentials.key, 12);
    Object localObject = localAPRep.encPart.reset(arrayOfByte, true);
    paramDerValue = new DerValue(localObject);
    this.encPart = new EncAPRepPart(paramDerValue);
  }

  private void authenticate(KrbApReq paramKrbApReq)
    throws KrbException, IOException
  {
    if ((this.encPart.ctime.getSeconds() != paramKrbApReq.getCtime().getSeconds()) || (this.encPart.cusec != paramKrbApReq.getCtime().getMicroSeconds()))
      throw new KrbApErrException(46);
  }

  public EncryptionKey getSubKey()
  {
    return this.encPart.getSubKey();
  }

  public Integer getSeqNumber()
  {
    return this.encPart.getSeqNumber();
  }

  public byte[] getMessage()
  {
    return this.obuf;
  }

  private void createMessage(EncryptionKey paramEncryptionKey1, KerberosTime paramKerberosTime, int paramInt, EncryptionKey paramEncryptionKey2, SeqNumber paramSeqNumber)
    throws sun.security.krb5.Asn1Exception, IOException, KdcErrException, sun.security.krb5.KrbCryptoException
  {
    Integer localInteger = null;
    if (paramSeqNumber != null)
      localInteger = new Integer(paramSeqNumber.current());
    this.encPart = new EncAPRepPart(paramKerberosTime, paramInt, paramEncryptionKey2, localInteger);
    byte[] arrayOfByte = this.encPart.asn1Encode();
    EncryptedData localEncryptedData = new EncryptedData(paramEncryptionKey1, arrayOfByte, 12);
    this.apRepMessg = new APRep(localEncryptedData);
  }
}