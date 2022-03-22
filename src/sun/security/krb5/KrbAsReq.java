package sun.security.krb5;

import java.io.IOException;
import java.io.PrintStream;
import java.net.UnknownHostException;
import sun.security.krb5.internal.ASReq;
import sun.security.krb5.internal.HostAddresses;
import sun.security.krb5.internal.KDCOptions;
import sun.security.krb5.internal.KDCReqBody;
import sun.security.krb5.internal.KerberosTime;
import sun.security.krb5.internal.Krb5;
import sun.security.krb5.internal.KrbApErrException;
import sun.security.krb5.internal.PAData;
import sun.security.krb5.internal.PAEncTSEnc;
import sun.security.krb5.internal.Ticket;
import sun.security.krb5.internal.crypto.EType;
import sun.security.krb5.internal.crypto.Nonce;

public class KrbAsReq extends KrbKdcReq
{
  private PrincipalName princName;
  private ASReq asReqMessg;
  private boolean DEBUG;
  private static KDCOptions defaultKDCOptions = new KDCOptions();
  private boolean PA_ENC_TIMESTAMP_REQUIRED;
  private boolean pa_exists;
  private int pa_etype;
  private byte[] pa_salt;
  private byte[] pa_s2kparams;

  KrbAsReq(PrincipalName paramPrincipalName, EncryptionKey[] paramArrayOfEncryptionKey)
    throws sun.security.krb5.KrbException, IOException
  {
    this(paramArrayOfEncryptionKey, false, 0, null, null, defaultKDCOptions, paramPrincipalName, null, null, null, null, null, null, null);
  }

  KrbAsReq(PrincipalName paramPrincipalName, EncryptionKey[] paramArrayOfEncryptionKey, boolean paramBoolean, int paramInt, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2)
    throws sun.security.krb5.KrbException, IOException
  {
    this(paramArrayOfEncryptionKey, paramBoolean, paramInt, paramArrayOfByte1, paramArrayOfByte2, defaultKDCOptions, paramPrincipalName, null, null, null, null, null, null, null);
  }

  private static int[] getETypesFromKeys(EncryptionKey[] paramArrayOfEncryptionKey)
  {
    int[] arrayOfInt = new int[paramArrayOfEncryptionKey.length];
    for (int i = 0; i < paramArrayOfEncryptionKey.length; ++i)
      arrayOfInt[i] = paramArrayOfEncryptionKey[i].getEType();
    return arrayOfInt;
  }

  public void updatePA(int paramInt, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, PrincipalName paramPrincipalName)
  {
    this.pa_exists = true;
    this.pa_etype = paramInt;
    this.pa_salt = paramArrayOfByte1;
    this.pa_s2kparams = paramArrayOfByte2;
    if ((paramArrayOfByte1 != null) && (paramArrayOfByte1.length > 0))
    {
      String str = new String(paramArrayOfByte1);
      paramPrincipalName.setSalt(str);
      if (this.DEBUG)
        System.out.println("Updated salt from pre-auth = " + paramPrincipalName.getSalt());
    }
    this.PA_ENC_TIMESTAMP_REQUIRED = true;
  }

  public KrbAsReq(char[] paramArrayOfChar, KDCOptions paramKDCOptions, PrincipalName paramPrincipalName1, PrincipalName paramPrincipalName2, KerberosTime paramKerberosTime1, KerberosTime paramKerberosTime2, KerberosTime paramKerberosTime3, int[] paramArrayOfInt, HostAddresses paramHostAddresses, Ticket[] paramArrayOfTicket)
    throws sun.security.krb5.KrbException, IOException
  {
    this(paramArrayOfChar, false, 0, null, null, paramKDCOptions, paramPrincipalName1, paramPrincipalName2, paramKerberosTime1, paramKerberosTime2, paramKerberosTime3, paramArrayOfInt, paramHostAddresses, paramArrayOfTicket);
  }

  public KrbAsReq(char[] paramArrayOfChar, boolean paramBoolean, int paramInt, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, KDCOptions paramKDCOptions, PrincipalName paramPrincipalName1, PrincipalName paramPrincipalName2, KerberosTime paramKerberosTime1, KerberosTime paramKerberosTime2, KerberosTime paramKerberosTime3, int[] paramArrayOfInt, HostAddresses paramHostAddresses, Ticket[] paramArrayOfTicket)
    throws sun.security.krb5.KrbException, IOException
  {
    this.DEBUG = Krb5.DEBUG;
    this.PA_ENC_TIMESTAMP_REQUIRED = false;
    this.pa_exists = false;
    this.pa_etype = 0;
    this.pa_salt = null;
    this.pa_s2kparams = null;
    EncryptionKey[] arrayOfEncryptionKey = null;
    if (paramBoolean)
      updatePA(paramInt, paramArrayOfByte1, paramArrayOfByte2, paramPrincipalName1);
    if (paramArrayOfChar != null)
      arrayOfEncryptionKey = EncryptionKey.acquireSecretKeys(paramArrayOfChar, paramPrincipalName1.getSalt(), paramBoolean, this.pa_etype, this.pa_s2kparams);
    if (this.DEBUG)
      System.out.println(">>>KrbAsReq salt is " + paramPrincipalName1.getSalt());
    try
    {
      init(arrayOfEncryptionKey, paramKDCOptions, paramPrincipalName1, paramPrincipalName2, paramKerberosTime1, paramKerberosTime2, paramKerberosTime3, paramArrayOfInt, paramHostAddresses, paramArrayOfTicket);
    }
    finally
    {
      int i;
      if (arrayOfEncryptionKey != null)
        for (int j = 0; j < arrayOfEncryptionKey.length; ++j)
          arrayOfEncryptionKey[j].destroy();
    }
  }

  public KrbAsReq(EncryptionKey[] paramArrayOfEncryptionKey, KDCOptions paramKDCOptions, PrincipalName paramPrincipalName1, PrincipalName paramPrincipalName2, KerberosTime paramKerberosTime1, KerberosTime paramKerberosTime2, KerberosTime paramKerberosTime3, int[] paramArrayOfInt, HostAddresses paramHostAddresses, Ticket[] paramArrayOfTicket)
    throws sun.security.krb5.KrbException, IOException
  {
    this(paramArrayOfEncryptionKey, false, 0, null, null, paramKDCOptions, paramPrincipalName1, paramPrincipalName2, paramKerberosTime1, paramKerberosTime2, paramKerberosTime3, paramArrayOfInt, paramHostAddresses, paramArrayOfTicket);
  }

  public KrbAsReq(EncryptionKey[] paramArrayOfEncryptionKey, boolean paramBoolean, int paramInt, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, KDCOptions paramKDCOptions, PrincipalName paramPrincipalName1, PrincipalName paramPrincipalName2, KerberosTime paramKerberosTime1, KerberosTime paramKerberosTime2, KerberosTime paramKerberosTime3, int[] paramArrayOfInt, HostAddresses paramHostAddresses, Ticket[] paramArrayOfTicket)
    throws sun.security.krb5.KrbException, IOException
  {
    this.DEBUG = Krb5.DEBUG;
    this.PA_ENC_TIMESTAMP_REQUIRED = false;
    this.pa_exists = false;
    this.pa_etype = 0;
    this.pa_salt = null;
    this.pa_s2kparams = null;
    if (paramBoolean)
    {
      updatePA(paramInt, paramArrayOfByte1, paramArrayOfByte2, paramPrincipalName1);
      if (this.DEBUG)
        System.out.println(">>>KrbAsReq salt is " + paramPrincipalName1.getSalt());
    }
    init(paramArrayOfEncryptionKey, paramKDCOptions, paramPrincipalName1, paramPrincipalName2, paramKerberosTime1, paramKerberosTime2, paramKerberosTime3, paramArrayOfInt, paramHostAddresses, paramArrayOfTicket);
  }

  private void init(EncryptionKey[] paramArrayOfEncryptionKey, KDCOptions paramKDCOptions, PrincipalName paramPrincipalName1, PrincipalName paramPrincipalName2, KerberosTime paramKerberosTime1, KerberosTime paramKerberosTime2, KerberosTime paramKerberosTime3, int[] paramArrayOfInt, HostAddresses paramHostAddresses, Ticket[] paramArrayOfTicket)
    throws sun.security.krb5.KrbException, IOException
  {
    if ((paramKDCOptions.get(2)) || (paramKDCOptions.get(4)) || (paramKDCOptions.get(28)) || (paramKDCOptions.get(30)) || (paramKDCOptions.get(31)))
      throw new sun.security.krb5.KrbException(101);
    if (paramKDCOptions.get(6))
      break label73:
    if (paramKerberosTime1 != null)
      paramKerberosTime1 = null;
    if (paramKDCOptions.get(8))
      label73: break label93:
    if (paramKerberosTime3 != null)
      paramKerberosTime3 = null;
    label93: this.princName = paramPrincipalName1;
    EncryptionKey localEncryptionKey = null;
    int[] arrayOfInt = null;
    if ((this.pa_exists) && (this.pa_etype != 0))
    {
      if (this.DEBUG)
        System.out.println("Pre-Authenticaton: find key for etype = " + this.pa_etype);
      localEncryptionKey = EncryptionKey.findKey(this.pa_etype, paramArrayOfEncryptionKey);
      arrayOfInt = new int[1];
      arrayOfInt[0] = this.pa_etype;
    }
    else
    {
      arrayOfInt = EType.getDefaults("default_tkt_enctypes", paramArrayOfEncryptionKey);
      localEncryptionKey = EncryptionKey.findKey(arrayOfInt[0], paramArrayOfEncryptionKey);
    }
    PAData[] arrayOfPAData = null;
    if (this.PA_ENC_TIMESTAMP_REQUIRED)
    {
      if (this.DEBUG)
        System.out.println("AS-REQ: Add PA_ENC_TIMESTAMP now");
      PAEncTSEnc localPAEncTSEnc = new PAEncTSEnc();
      byte[] arrayOfByte = localPAEncTSEnc.asn1Encode();
      if (localEncryptionKey != null)
      {
        EncryptedData localEncryptedData = new EncryptedData(localEncryptionKey, arrayOfByte, 1);
        arrayOfPAData = new PAData[1];
        arrayOfPAData[0] = new PAData(2, localEncryptedData.asn1Encode());
      }
    }
    if (this.DEBUG)
      System.out.println(">>> KrbAsReq calling createMessage");
    if (paramArrayOfInt == null)
      paramArrayOfInt = arrayOfInt;
    this.asReqMessg = createMessage(arrayOfPAData, paramKDCOptions, paramPrincipalName1, paramPrincipalName1.getRealm(), paramPrincipalName2, paramKerberosTime1, paramKerberosTime2, paramKerberosTime3, paramArrayOfInt, paramHostAddresses, paramArrayOfTicket);
    this.obuf = this.asReqMessg.asn1Encode();
  }

  public KrbAsRep getReply(char[] paramArrayOfChar)
    throws sun.security.krb5.KrbException, IOException
  {
    if (paramArrayOfChar == null)
      throw new sun.security.krb5.KrbException(400);
    KrbAsRep localKrbAsRep = null;
    EncryptionKey[] arrayOfEncryptionKey = null;
    try
    {
      arrayOfEncryptionKey = EncryptionKey.acquireSecretKeys(paramArrayOfChar, this.princName.getSalt(), this.pa_exists, this.pa_etype, this.pa_s2kparams);
      localKrbAsRep = getReply(arrayOfEncryptionKey);
    }
    finally
    {
      int i;
      if (arrayOfEncryptionKey != null)
        for (int j = 0; j < arrayOfEncryptionKey.length; ++j)
          arrayOfEncryptionKey[j].destroy();
    }
    return localKrbAsRep;
  }

  public String send()
    throws IOException, sun.security.krb5.KrbException
  {
    String str = null;
    if (this.princName != null)
      str = this.princName.getRealmString();
    return send(str);
  }

  public KrbAsRep getReply(EncryptionKey[] paramArrayOfEncryptionKey)
    throws sun.security.krb5.KrbException, IOException
  {
    return new KrbAsRep(this.ibuf, paramArrayOfEncryptionKey, this);
  }

  private ASReq createMessage(PAData[] paramArrayOfPAData, KDCOptions paramKDCOptions, PrincipalName paramPrincipalName1, Realm paramRealm, PrincipalName paramPrincipalName2, KerberosTime paramKerberosTime1, KerberosTime paramKerberosTime2, KerberosTime paramKerberosTime3, int[] paramArrayOfInt, HostAddresses paramHostAddresses, Ticket[] paramArrayOfTicket)
    throws Asn1Exception, KrbApErrException, sun.security.krb5.RealmException, UnknownHostException, IOException
  {
    if (this.DEBUG)
      System.out.println(">>> KrbAsReq in createMessage");
    PrincipalName localPrincipalName = null;
    if (paramPrincipalName2 == null)
    {
      if (paramRealm == null)
        throw new sun.security.krb5.RealmException(601, "default realm not specified ");
      localPrincipalName = new PrincipalName("krbtgt/" + paramRealm.toString(), 2);
    }
    else
    {
      localPrincipalName = paramPrincipalName2;
    }
    KerberosTime localKerberosTime = null;
    if (paramKerberosTime2 == null)
      localKerberosTime = new KerberosTime();
    else
      localKerberosTime = paramKerberosTime2;
    KDCReqBody localKDCReqBody = new KDCReqBody(paramKDCOptions, paramPrincipalName1, paramRealm, localPrincipalName, paramKerberosTime1, localKerberosTime, paramKerberosTime3, Nonce.value(), paramArrayOfInt, paramHostAddresses, null, paramArrayOfTicket);
    return new ASReq(paramArrayOfPAData, localKDCReqBody);
  }

  ASReq getMessage()
  {
    return this.asReqMessg;
  }
}