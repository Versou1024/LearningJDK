package sun.security.jgss.krb5;

import java.io.IOException;
import org.ietf.jgss.Oid;
import sun.security.jgss.GSSToken;
import sun.security.util.ObjectIdentifier;

abstract class Krb5Token extends GSSToken
{
  public static final int AP_REQ_ID = 256;
  public static final int AP_REP_ID = 512;
  public static final int ERR_ID = 768;
  public static final int MIC_ID = 257;
  public static final int WRAP_ID = 513;
  public static final int MIC_ID_v2 = 1028;
  public static final int WRAP_ID_v2 = 1284;
  public static ObjectIdentifier OID;

  public static String getTokenName(int paramInt)
  {
    String str = null;
    switch (paramInt)
    {
    case 256:
    case 512:
      str = "Context Establishment Token";
      break;
    case 257:
      str = "MIC Token";
      break;
    case 1028:
      str = "MIC Token (new format)";
      break;
    case 513:
      str = "Wrap Token";
      break;
    case 1284:
      str = "Wrap Token (new format)";
      break;
    default:
      str = "Kerberos GSS-API Mechanism Token";
    }
    return str;
  }

  static
  {
    try
    {
      OID = new ObjectIdentifier(Krb5MechFactory.GSS_KRB5_MECH_OID.toString());
    }
    catch (IOException localIOException)
    {
    }
  }
}