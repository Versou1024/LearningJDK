package sun.security.krb5.internal;

import java.io.IOException;
import sun.security.krb5.Asn1Exception;
import sun.security.krb5.KrbException;
import sun.security.util.DerValue;

public class ASReq extends KDCReq
{
  public ASReq(PAData[] paramArrayOfPAData, KDCReqBody paramKDCReqBody)
    throws IOException
  {
    super(paramArrayOfPAData, paramKDCReqBody, 10);
  }

  public ASReq(byte[] paramArrayOfByte)
    throws Asn1Exception, KrbException, IOException
  {
    init(new DerValue(paramArrayOfByte));
  }

  public ASReq(DerValue paramDerValue)
    throws Asn1Exception, KrbException, IOException
  {
    init(paramDerValue);
  }

  private void init(DerValue paramDerValue)
    throws Asn1Exception, IOException, KrbException
  {
    super.init(paramDerValue, 10);
  }
}