package sun.security.krb5;

import java.io.IOException;
import sun.security.krb5.internal.EncKDCRepPart;
import sun.security.krb5.internal.EncTGSRepPart;
import sun.security.krb5.internal.KDCReqBody;
import sun.security.krb5.internal.KRBError;
import sun.security.krb5.internal.Krb5;
import sun.security.krb5.internal.TGSRep;
import sun.security.krb5.internal.TGSReq;
import sun.security.krb5.internal.Ticket;
import sun.security.util.DerValue;

public class KrbTgsRep extends KrbKdcRep
{
  private TGSRep rep;
  private Credentials creds;
  private Ticket secondTicket;
  private static final boolean DEBUG = Krb5.DEBUG;

  KrbTgsRep(byte[] paramArrayOfByte, KrbTgsReq paramKrbTgsReq)
    throws KrbException, IOException
  {
    DerValue localDerValue = new DerValue(paramArrayOfByte);
    TGSReq localTGSReq = paramKrbTgsReq.getMessage();
    TGSRep localTGSRep = null;
    try
    {
      localTGSRep = new TGSRep(localDerValue);
    }
    catch (Asn1Exception localAsn1Exception)
    {
      KrbException localKrbException;
      localTGSRep = null;
      localObject1 = new KRBError(localDerValue);
      localObject2 = ((KRBError)localObject1).getErrorString();
      Object localObject3 = null;
      if ((localObject2 != null) && (((String)localObject2).length() > 0))
        if (((String)localObject2).charAt(((String)localObject2).length() - 1) == 0)
          localObject3 = ((String)localObject2).substring(0, ((String)localObject2).length() - 1);
        else
          localObject3 = localObject2;
      if (localObject3 == null)
        localKrbException = new KrbException(((KRBError)localObject1).getErrorCode());
      else
        localKrbException = new KrbException(((KRBError)localObject1).getErrorCode(), (String)localObject3);
      localKrbException.initCause(localAsn1Exception);
      throw localKrbException;
    }
    byte[] arrayOfByte = localTGSRep.encPart.decrypt(paramKrbTgsReq.tgsReqKey, (paramKrbTgsReq.usedSubkey()) ? 9 : 8);
    Object localObject1 = localTGSRep.encPart.reset(arrayOfByte, true);
    localDerValue = new DerValue(localObject1);
    Object localObject2 = new EncTGSRepPart(localDerValue);
    localTGSRep.ticket.sname.setRealm(localTGSRep.ticket.realm);
    localTGSRep.encKDCRepPart = ((EncKDCRepPart)localObject2);
    check(localTGSReq, localTGSRep);
    this.creds = new Credentials(localTGSRep.ticket, localTGSReq.reqBody.cname, localTGSRep.ticket.sname, ((EncTGSRepPart)localObject2).key, ((EncTGSRepPart)localObject2).flags, ((EncTGSRepPart)localObject2).authtime, ((EncTGSRepPart)localObject2).starttime, ((EncTGSRepPart)localObject2).endtime, ((EncTGSRepPart)localObject2).renewTill, ((EncTGSRepPart)localObject2).caddr);
    this.rep = localTGSRep;
    this.creds = this.creds;
    this.secondTicket = paramKrbTgsReq.getSecondTicket();
  }

  public Credentials getCreds()
  {
    return this.creds;
  }

  sun.security.krb5.internal.ccache.Credentials setCredentials()
  {
    return new sun.security.krb5.internal.ccache.Credentials(this.rep, this.secondTicket);
  }
}