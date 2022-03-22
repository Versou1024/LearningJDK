package sun.security.jgss.spnego;

import java.io.IOException;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.Oid;
import sun.security.jgss.GSSToken;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;

abstract class SpNegoToken extends GSSToken
{
  static final int NEG_TOKEN_INIT_ID = 0;
  static final int NEG_TOKEN_TARG_ID = 1;
  private int tokenType;
  static final boolean DEBUG = SpNegoContext.DEBUG;
  public static ObjectIdentifier OID;

  protected SpNegoToken(int paramInt)
  {
    this.tokenType = paramInt;
  }

  abstract byte[] encode()
    throws GSSException;

  byte[] getEncoded()
    throws IOException, GSSException
  {
    DerOutputStream localDerOutputStream1 = new DerOutputStream();
    localDerOutputStream1.write(encode());
    switch (this.tokenType)
    {
    case 0:
      DerOutputStream localDerOutputStream2 = new DerOutputStream();
      localDerOutputStream2.write(DerValue.createTag(-128, true, 0), localDerOutputStream1);
      return localDerOutputStream2.toByteArray();
    case 1:
      DerOutputStream localDerOutputStream3 = new DerOutputStream();
      localDerOutputStream3.write(DerValue.createTag(-128, true, 1), localDerOutputStream1);
      return localDerOutputStream3.toByteArray();
    }
    return localDerOutputStream1.toByteArray();
  }

  final int getType()
  {
    return this.tokenType;
  }

  static String getTokenName(int paramInt)
  {
    switch (paramInt)
    {
    case 0:
      return "SPNEGO NegTokenInit";
    case 1:
      return "SPNEGO NegTokenTarg";
    }
    return "SPNEGO Mechanism Token";
  }

  static NegoResult getNegoResultType(int paramInt)
  {
    switch (paramInt)
    {
    case 0:
      return NegoResult.ACCEPT_COMPLETE;
    case 1:
      return NegoResult.ACCEPT_INCOMPLETE;
    case 2:
      return NegoResult.REJECT;
    }
    return NegoResult.ACCEPT_COMPLETE;
  }

  static String getNegoResultString(int paramInt)
  {
    switch (paramInt)
    {
    case 0:
      return "Accept Complete";
    case 1:
      return "Accept InComplete";
    case 2:
      return "Reject";
    }
    return "Unknown Negotiated Result: " + paramInt;
  }

  static
  {
    try
    {
      OID = new ObjectIdentifier(SpNegoMechFactory.GSS_SPNEGO_MECH_OID.toString());
    }
    catch (IOException localIOException)
    {
    }
  }

  static enum NegoResult
  {
    ACCEPT_COMPLETE, ACCEPT_INCOMPLETE, REJECT;
  }
}