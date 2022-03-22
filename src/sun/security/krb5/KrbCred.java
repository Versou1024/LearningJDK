package sun.security.krb5;

import java.io.IOException;
import java.io.PrintStream;
import sun.security.krb5.internal.EncKrbCredPart;
import sun.security.krb5.internal.HostAddresses;
import sun.security.krb5.internal.KDCOptions;
import sun.security.krb5.internal.KRBCred;
import sun.security.krb5.internal.KerberosTime;
import sun.security.krb5.internal.Krb5;
import sun.security.krb5.internal.KrbCredInfo;
import sun.security.krb5.internal.Ticket;
import sun.security.krb5.internal.TicketFlags;
import sun.security.util.DerValue;

public class KrbCred
{
  private static boolean DEBUG = Krb5.DEBUG;
  private byte[] obuf = null;
  private KRBCred credMessg = null;
  private Ticket ticket = null;
  private EncKrbCredPart encPart = null;
  private Credentials creds = null;
  private KerberosTime timeStamp = null;

  public KrbCred(Credentials paramCredentials1, Credentials paramCredentials2, EncryptionKey paramEncryptionKey)
    throws sun.security.krb5.KrbException, IOException
  {
    PrincipalName localPrincipalName1 = paramCredentials1.getClient();
    PrincipalName localPrincipalName2 = paramCredentials1.getServer();
    PrincipalName localPrincipalName3 = paramCredentials2.getServer();
    if (!(paramCredentials2.getClient().equals(localPrincipalName1)))
      throw new sun.security.krb5.KrbException(60, "Client principal does not match");
    KDCOptions localKDCOptions = new KDCOptions();
    localKDCOptions.set(2, true);
    localKDCOptions.set(1, true);
    HostAddresses localHostAddresses = null;
    if (localPrincipalName3.getNameType() == 3)
      localHostAddresses = new HostAddresses(localPrincipalName3);
    KrbTgsReq localKrbTgsReq = new KrbTgsReq(localKDCOptions, paramCredentials1, localPrincipalName2, null, null, null, null, localHostAddresses, null, null, null);
    KrbTgsRep localKrbTgsRep = null;
    String str = null;
    try
    {
      str = localKrbTgsReq.send();
      localKrbTgsRep = localKrbTgsReq.getReply();
    }
    catch (KrbException localKrbException)
    {
      if (localKrbException.returnCode() == 52)
      {
        localKrbTgsReq.send(localPrincipalName2.getRealmString(), str, true);
        localKrbTgsRep = localKrbTgsReq.getReply();
      }
      else
      {
        throw localKrbException;
      }
    }
    this.credMessg = createMessage(localKrbTgsRep.getCreds(), paramEncryptionKey);
    this.obuf = this.credMessg.asn1Encode();
  }

  KRBCred createMessage(Credentials paramCredentials, EncryptionKey paramEncryptionKey)
    throws sun.security.krb5.KrbException, IOException
  {
    EncryptionKey localEncryptionKey = paramCredentials.getSessionKey();
    PrincipalName localPrincipalName1 = paramCredentials.getClient();
    Realm localRealm1 = localPrincipalName1.getRealm();
    PrincipalName localPrincipalName2 = paramCredentials.getServer();
    Realm localRealm2 = localPrincipalName2.getRealm();
    KrbCredInfo localKrbCredInfo = new KrbCredInfo(localEncryptionKey, localRealm1, localPrincipalName1, paramCredentials.flags, paramCredentials.authTime, paramCredentials.startTime, paramCredentials.endTime, paramCredentials.renewTill, localRealm2, localPrincipalName2, paramCredentials.cAddr);
    this.timeStamp = new KerberosTime(true);
    KrbCredInfo[] arrayOfKrbCredInfo = { localKrbCredInfo };
    EncKrbCredPart localEncKrbCredPart = new EncKrbCredPart(arrayOfKrbCredInfo, this.timeStamp, null, null, null, null);
    EncryptedData localEncryptedData = new EncryptedData(paramEncryptionKey, localEncKrbCredPart.asn1Encode(), 14);
    Ticket[] arrayOfTicket = { paramCredentials.ticket };
    this.credMessg = new KRBCred(arrayOfTicket, localEncryptedData);
    return this.credMessg;
  }

  public KrbCred(byte[] paramArrayOfByte, EncryptionKey paramEncryptionKey)
    throws sun.security.krb5.KrbException, IOException
  {
    this.credMessg = new KRBCred(paramArrayOfByte);
    this.ticket = this.credMessg.tickets[0];
    byte[] arrayOfByte1 = this.credMessg.encPart.decrypt(paramEncryptionKey, 14);
    byte[] arrayOfByte2 = this.credMessg.encPart.reset(arrayOfByte1, true);
    DerValue localDerValue = new DerValue(arrayOfByte2);
    EncKrbCredPart localEncKrbCredPart = new EncKrbCredPart(localDerValue);
    this.timeStamp = localEncKrbCredPart.timeStamp;
    KrbCredInfo localKrbCredInfo = localEncKrbCredPart.ticketInfo[0];
    EncryptionKey localEncryptionKey = localKrbCredInfo.key;
    Realm localRealm1 = localKrbCredInfo.prealm;
    PrincipalName localPrincipalName1 = localKrbCredInfo.pname;
    localPrincipalName1.setRealm(localRealm1);
    TicketFlags localTicketFlags = localKrbCredInfo.flags;
    KerberosTime localKerberosTime1 = localKrbCredInfo.authtime;
    KerberosTime localKerberosTime2 = localKrbCredInfo.starttime;
    KerberosTime localKerberosTime3 = localKrbCredInfo.endtime;
    KerberosTime localKerberosTime4 = localKrbCredInfo.renewTill;
    Realm localRealm2 = localKrbCredInfo.srealm;
    PrincipalName localPrincipalName2 = localKrbCredInfo.sname;
    localPrincipalName2.setRealm(localRealm2);
    HostAddresses localHostAddresses = localKrbCredInfo.caddr;
    if (DEBUG)
      System.out.println(">>>Delegated Creds have pname=" + localPrincipalName1 + " sname=" + localPrincipalName2 + " authtime=" + localKerberosTime1 + " starttime=" + localKerberosTime2 + " endtime=" + localKerberosTime3 + "renewTill=" + localKerberosTime4);
    this.creds = new Credentials(this.ticket, localPrincipalName1, localPrincipalName2, localEncryptionKey, localTicketFlags, localKerberosTime1, localKerberosTime2, localKerberosTime3, localKerberosTime4, localHostAddresses);
  }

  public Credentials[] getDelegatedCreds()
  {
    Credentials[] arrayOfCredentials = { this.creds };
    return arrayOfCredentials;
  }

  public byte[] getMessage()
  {
    return this.obuf;
  }
}