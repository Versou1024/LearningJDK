package sun.security.krb5;

import sun.security.krb5.internal.EncKDCRepPart;
import sun.security.krb5.internal.HostAddresses;
import sun.security.krb5.internal.KDCOptions;
import sun.security.krb5.internal.KDCRep;
import sun.security.krb5.internal.KDCReq;
import sun.security.krb5.internal.KDCReqBody;
import sun.security.krb5.internal.KerberosTime;
import sun.security.krb5.internal.KrbApErrException;
import sun.security.krb5.internal.TicketFlags;

abstract class KrbKdcRep
{
  static void check(KDCReq paramKDCReq, KDCRep paramKDCRep)
    throws KrbApErrException
  {
    if (!(paramKDCReq.reqBody.cname.equalsWithoutRealm(paramKDCRep.cname)))
    {
      paramKDCRep.encKDCRepPart.key.destroy();
      throw new KrbApErrException(41);
    }
    if (!(paramKDCReq.reqBody.sname.equalsWithoutRealm(paramKDCRep.encKDCRepPart.sname)))
    {
      paramKDCRep.encKDCRepPart.key.destroy();
      throw new KrbApErrException(41);
    }
    if (!(paramKDCReq.reqBody.crealm.equals(paramKDCRep.encKDCRepPart.srealm)))
    {
      paramKDCRep.encKDCRepPart.key.destroy();
      throw new KrbApErrException(41);
    }
    if ((paramKDCReq.reqBody.addresses != null) && (paramKDCRep.encKDCRepPart.caddr != null) && (!(paramKDCReq.reqBody.addresses.equals(paramKDCRep.encKDCRepPart.caddr))))
    {
      paramKDCRep.encKDCRepPart.key.destroy();
      throw new KrbApErrException(41);
    }
    for (int i = 1; i < 6; ++i)
      if (paramKDCReq.reqBody.kdcOptions.get(i) != paramKDCRep.encKDCRepPart.flags.get(i))
        throw new KrbApErrException(41);
    if (paramKDCReq.reqBody.kdcOptions.get(8) != paramKDCRep.encKDCRepPart.flags.get(8))
      throw new KrbApErrException(41);
    if ((((paramKDCReq.reqBody.from == null) || (paramKDCReq.reqBody.from.isZero()))) && (paramKDCRep.encKDCRepPart.starttime != null) && (!(paramKDCRep.encKDCRepPart.starttime.inClockSkew())))
    {
      paramKDCRep.encKDCRepPart.key.destroy();
      throw new KrbApErrException(37);
    }
    if ((paramKDCReq.reqBody.from != null) && (!(paramKDCReq.reqBody.from.isZero())) && (paramKDCRep.encKDCRepPart.starttime != null) && (!(paramKDCReq.reqBody.from.equals(paramKDCRep.encKDCRepPart.starttime))))
    {
      paramKDCRep.encKDCRepPart.key.destroy();
      throw new KrbApErrException(41);
    }
    if ((!(paramKDCReq.reqBody.till.isZero())) && (paramKDCRep.encKDCRepPart.endtime.greaterThan(paramKDCReq.reqBody.till)))
    {
      paramKDCRep.encKDCRepPart.key.destroy();
      throw new KrbApErrException(41);
    }
    if ((paramKDCReq.reqBody.kdcOptions.get(8)) && (paramKDCReq.reqBody.rtime != null) && (!(paramKDCReq.reqBody.rtime.isZero())) && (((paramKDCRep.encKDCRepPart.renewTill == null) || (paramKDCRep.encKDCRepPart.renewTill.greaterThan(paramKDCReq.reqBody.rtime)))))
    {
      paramKDCRep.encKDCRepPart.key.destroy();
      throw new KrbApErrException(41);
    }
    if ((paramKDCReq.reqBody.kdcOptions.get(27)) && (paramKDCRep.encKDCRepPart.flags.get(8)) && (!(paramKDCReq.reqBody.till.isZero())) && (((paramKDCRep.encKDCRepPart.renewTill == null) || (paramKDCRep.encKDCRepPart.renewTill.greaterThan(paramKDCReq.reqBody.till)))))
    {
      paramKDCRep.encKDCRepPart.key.destroy();
      throw new KrbApErrException(41);
    }
  }
}