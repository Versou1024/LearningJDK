package sun.security.krb5.internal.ccache;

import sun.security.krb5.EncryptionKey;
import sun.security.krb5.PrincipalName;
import sun.security.krb5.Realm;
import sun.security.krb5.RealmException;
import sun.security.krb5.internal.AuthorizationData;
import sun.security.krb5.internal.EncKDCRepPart;
import sun.security.krb5.internal.HostAddresses;
import sun.security.krb5.internal.KDCRep;
import sun.security.krb5.internal.KerberosTime;
import sun.security.krb5.internal.Krb5;
import sun.security.krb5.internal.Ticket;
import sun.security.krb5.internal.TicketFlags;

public class Credentials
{
  PrincipalName cname;
  Realm crealm;
  PrincipalName sname;
  Realm srealm;
  EncryptionKey key;
  KerberosTime authtime;
  KerberosTime starttime;
  KerberosTime endtime;
  KerberosTime renewTill;
  HostAddresses caddr;
  AuthorizationData authorizationData;
  public boolean isEncInSKey;
  TicketFlags flags;
  Ticket ticket;
  Ticket secondTicket;
  private boolean DEBUG;

  public Credentials(PrincipalName paramPrincipalName1, PrincipalName paramPrincipalName2, EncryptionKey paramEncryptionKey, KerberosTime paramKerberosTime1, KerberosTime paramKerberosTime2, KerberosTime paramKerberosTime3, KerberosTime paramKerberosTime4, boolean paramBoolean, TicketFlags paramTicketFlags, HostAddresses paramHostAddresses, AuthorizationData paramAuthorizationData, Ticket paramTicket1, Ticket paramTicket2)
  {
    this.DEBUG = Krb5.DEBUG;
    this.cname = ((PrincipalName)paramPrincipalName1.clone());
    if (paramPrincipalName1.getRealm() != null)
      this.crealm = ((Realm)paramPrincipalName1.getRealm().clone());
    this.sname = ((PrincipalName)paramPrincipalName2.clone());
    if (paramPrincipalName2.getRealm() != null)
      this.srealm = ((Realm)paramPrincipalName2.getRealm().clone());
    this.key = ((EncryptionKey)paramEncryptionKey.clone());
    this.authtime = ((KerberosTime)paramKerberosTime1.clone());
    this.starttime = ((KerberosTime)paramKerberosTime2.clone());
    this.endtime = ((KerberosTime)paramKerberosTime3.clone());
    this.renewTill = ((KerberosTime)paramKerberosTime4.clone());
    if (paramHostAddresses != null)
      this.caddr = ((HostAddresses)paramHostAddresses.clone());
    if (paramAuthorizationData != null)
      this.authorizationData = ((AuthorizationData)paramAuthorizationData.clone());
    this.isEncInSKey = paramBoolean;
    this.flags = ((TicketFlags)paramTicketFlags.clone());
    this.ticket = ((Ticket)(Ticket)paramTicket1.clone());
    if (paramTicket2 != null)
      this.secondTicket = ((Ticket)paramTicket2.clone());
  }

  public Credentials(KDCRep paramKDCRep, Ticket paramTicket, AuthorizationData paramAuthorizationData, boolean paramBoolean)
  {
    this.DEBUG = Krb5.DEBUG;
    if (paramKDCRep.encKDCRepPart == null)
      return;
    this.crealm = ((Realm)paramKDCRep.crealm.clone());
    this.cname = ((PrincipalName)paramKDCRep.cname.clone());
    this.ticket = ((Ticket)paramKDCRep.ticket.clone());
    this.key = ((EncryptionKey)paramKDCRep.encKDCRepPart.key.clone());
    this.flags = ((TicketFlags)paramKDCRep.encKDCRepPart.flags.clone());
    this.authtime = ((KerberosTime)paramKDCRep.encKDCRepPart.authtime.clone());
    this.starttime = ((KerberosTime)paramKDCRep.encKDCRepPart.starttime.clone());
    this.endtime = ((KerberosTime)paramKDCRep.encKDCRepPart.endtime.clone());
    this.renewTill = ((KerberosTime)paramKDCRep.encKDCRepPart.renewTill.clone());
    this.srealm = ((Realm)paramKDCRep.encKDCRepPart.srealm.clone());
    this.sname = ((PrincipalName)paramKDCRep.encKDCRepPart.sname.clone());
    this.caddr = ((HostAddresses)paramKDCRep.encKDCRepPart.caddr.clone());
    this.secondTicket = ((Ticket)paramTicket.clone());
    this.authorizationData = ((AuthorizationData)paramAuthorizationData.clone());
    this.isEncInSKey = paramBoolean;
  }

  public Credentials(KDCRep paramKDCRep)
  {
    this(paramKDCRep, null);
  }

  public Credentials(KDCRep paramKDCRep, Ticket paramTicket)
  {
    this.DEBUG = Krb5.DEBUG;
    this.sname = ((PrincipalName)paramKDCRep.encKDCRepPart.sname.clone());
    this.srealm = ((Realm)paramKDCRep.encKDCRepPart.srealm.clone());
    try
    {
      this.sname.setRealm(this.srealm);
    }
    catch (RealmException localRealmException1)
    {
    }
    this.cname = ((PrincipalName)paramKDCRep.cname.clone());
    this.crealm = ((Realm)paramKDCRep.crealm.clone());
    try
    {
      this.cname.setRealm(this.crealm);
    }
    catch (RealmException localRealmException2)
    {
    }
    this.key = ((EncryptionKey)paramKDCRep.encKDCRepPart.key.clone());
    this.authtime = ((KerberosTime)paramKDCRep.encKDCRepPart.authtime.clone());
    if (paramKDCRep.encKDCRepPart.starttime != null)
      this.starttime = ((KerberosTime)paramKDCRep.encKDCRepPart.starttime.clone());
    else
      this.starttime = null;
    this.endtime = ((KerberosTime)paramKDCRep.encKDCRepPart.endtime.clone());
    if (paramKDCRep.encKDCRepPart.renewTill != null)
      this.renewTill = ((KerberosTime)paramKDCRep.encKDCRepPart.renewTill.clone());
    else
      this.renewTill = null;
    this.flags = paramKDCRep.encKDCRepPart.flags;
    if (paramKDCRep.encKDCRepPart.caddr != null)
      this.caddr = ((HostAddresses)paramKDCRep.encKDCRepPart.caddr.clone());
    else
      this.caddr = null;
    this.ticket = ((Ticket)paramKDCRep.ticket.clone());
    if (paramTicket != null)
    {
      this.secondTicket = ((Ticket)paramTicket.clone());
      this.isEncInSKey = true;
    }
    else
    {
      this.secondTicket = null;
      this.isEncInSKey = false;
    }
  }

  public boolean isValid()
  {
    int i = 1;
    if (this.endtime.getTime() < System.currentTimeMillis())
      i = 0;
    else if ((this.starttime.getTime() > System.currentTimeMillis()) || ((this.starttime == null) && (this.authtime.getTime() > System.currentTimeMillis())))
      i = 0;
    return i;
  }

  public PrincipalName getServicePrincipal()
    throws RealmException
  {
    if (this.sname.getRealm() == null)
      this.sname.setRealm(this.srealm);
    return this.sname;
  }

  public sun.security.krb5.Credentials setKrbCreds()
  {
    return new sun.security.krb5.Credentials(this.ticket, this.cname, this.sname, this.key, this.flags, this.authtime, this.starttime, this.endtime, this.renewTill, this.caddr);
  }

  public KerberosTime getAuthTime()
  {
    return this.authtime;
  }

  public KerberosTime getEndTime()
  {
    return this.endtime;
  }

  public TicketFlags getTicketFlags()
  {
    return this.flags;
  }

  public int getEType()
  {
    return this.key.getEType();
  }
}