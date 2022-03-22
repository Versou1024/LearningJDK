package sun.security.krb5.internal;

import java.io.IOException;
import sun.security.krb5.Asn1Exception;
import sun.security.krb5.EncryptedData;
import sun.security.krb5.PrincipalName;
import sun.security.krb5.Realm;
import sun.security.krb5.RealmException;
import sun.security.util.DerValue;

public class ASRep extends KDCRep
{
  public ASRep(PAData[] paramArrayOfPAData, Realm paramRealm, PrincipalName paramPrincipalName, Ticket paramTicket, EncryptedData paramEncryptedData)
    throws IOException
  {
    super(paramArrayOfPAData, paramRealm, paramPrincipalName, paramTicket, paramEncryptedData, 11);
  }

  public ASRep(byte[] paramArrayOfByte)
    throws Asn1Exception, RealmException, sun.security.krb5.internal.KrbApErrException, IOException
  {
    init(new DerValue(paramArrayOfByte));
  }

  public ASRep(DerValue paramDerValue)
    throws Asn1Exception, RealmException, sun.security.krb5.internal.KrbApErrException, IOException
  {
    init(paramDerValue);
  }

  private void init(DerValue paramDerValue)
    throws Asn1Exception, RealmException, sun.security.krb5.internal.KrbApErrException, IOException
  {
    init(paramDerValue, 11);
  }
}