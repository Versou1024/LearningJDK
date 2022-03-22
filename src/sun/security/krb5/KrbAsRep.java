package sun.security.krb5;

import java.io.IOException;
import java.io.PrintStream;
import sun.security.krb5.internal.ASRep;
import sun.security.krb5.internal.ASReq;
import sun.security.krb5.internal.EncASRepPart;
import sun.security.krb5.internal.EncKDCRepPart;
import sun.security.krb5.internal.KDCReqBody;
import sun.security.krb5.internal.KRBError;
import sun.security.krb5.internal.Krb5;
import sun.security.krb5.internal.Ticket;
import sun.security.krb5.internal.crypto.EType;
import sun.security.util.DerValue;

public class KrbAsRep extends KrbKdcRep
{
  private ASRep rep;
  private Credentials creds;
  private boolean DEBUG = Krb5.DEBUG;

  KrbAsRep(byte[] paramArrayOfByte, EncryptionKey[] paramArrayOfEncryptionKey, KrbAsReq paramKrbAsReq)
    throws sun.security.krb5.KrbException, Asn1Exception, IOException
  {
    if (paramArrayOfEncryptionKey == null)
      throw new sun.security.krb5.KrbException(400);
    DerValue localDerValue = new DerValue(paramArrayOfByte);
    ASReq localASReq = paramKrbAsReq.getMessage();
    ASRep localASRep = null;
    try
    {
      localASRep = new ASRep(localDerValue);
    }
    catch (Asn1Exception localAsn1Exception)
    {
      localASRep = null;
      localObject1 = new KRBError(localDerValue);
      localObject2 = ((KRBError)localObject1).getErrorString();
      localObject3 = null;
      if ((localObject2 != null) && (((String)localObject2).length() > 0))
        if (((String)localObject2).charAt(((String)localObject2).length() - 1) == 0)
          localObject3 = ((String)localObject2).substring(0, ((String)localObject2).length() - 1);
        else
          localObject3 = localObject2;
      if (localObject3 == null)
      {
        localObject4 = new sun.security.krb5.KrbException((KRBError)localObject1);
      }
      else
      {
        if (this.DEBUG)
          System.out.println("KRBError received: " + ((String)localObject3));
        localObject4 = new sun.security.krb5.KrbException((KRBError)localObject1, (String)localObject3);
      }
      ((sun.security.krb5.KrbException)localObject4).initCause(localAsn1Exception);
      throw ((Throwable)localObject4);
    }
    int i = localASRep.encPart.getEType();
    Object localObject1 = EncryptionKey.findKey(i, paramArrayOfEncryptionKey);
    if (localObject1 == null)
      throw new sun.security.krb5.KrbException(400, "Cannot find key of appropriate type to decrypt AS REP - " + EType.toString(i));
    Object localObject2 = localASRep.encPart.decrypt((EncryptionKey)localObject1, 3);
    Object localObject3 = localASRep.encPart.reset(localObject2, true);
    localDerValue = new DerValue(localObject3);
    Object localObject4 = new EncASRepPart(localDerValue);
    localASRep.ticket.sname.setRealm(localASRep.ticket.realm);
    localASRep.encKDCRepPart = ((EncKDCRepPart)localObject4);
    check(localASReq, localASRep);
    this.creds = new Credentials(localASRep.ticket, localASReq.reqBody.cname, localASRep.ticket.sname, ((EncASRepPart)localObject4).key, ((EncASRepPart)localObject4).flags, ((EncASRepPart)localObject4).authtime, ((EncASRepPart)localObject4).starttime, ((EncASRepPart)localObject4).endtime, ((EncASRepPart)localObject4).renewTill, ((EncASRepPart)localObject4).caddr);
    if (this.DEBUG)
      System.out.println(">>> KrbAsRep cons in KrbAsReq.getReply " + localASReq.reqBody.cname.getNameString());
    this.rep = localASRep;
    this.creds = this.creds;
  }

  public Credentials getCreds()
  {
    return this.creds;
  }

  public sun.security.krb5.internal.ccache.Credentials setCredentials()
  {
    return new sun.security.krb5.internal.ccache.Credentials(this.rep);
  }
}