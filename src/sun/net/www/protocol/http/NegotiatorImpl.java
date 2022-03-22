package sun.net.www.protocol.http;

import java.security.AccessController;
import java.security.PrivilegedAction;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.Oid;
import sun.security.jgss.GSSManagerImpl;
import sun.security.jgss.GSSUtil;

public class NegotiatorImpl extends Negotiator
{
  private GSSContext context;
  private byte[] oneToken;

  private void init(String paramString1, String paramString2)
    throws GSSException
  {
    Oid localOid;
    if (paramString2.equalsIgnoreCase("Kerberos"))
    {
      localOid = GSSUtil.GSS_KRB5_MECH_OID;
    }
    else
    {
      localObject = (String)AccessController.doPrivileged(new PrivilegedAction(this)
      {
        public Object run()
        {
          return System.getProperty("http.auth.preference", "spnego");
        }
      });
      if (((String)localObject).equalsIgnoreCase("kerberos"))
        localOid = GSSUtil.GSS_KRB5_MECH_OID;
      else
        localOid = GSSUtil.GSS_SPNEGO_MECH_OID;
    }
    Object localObject = new GSSManagerImpl(5);
    String str = "HTTP/" + paramString1;
    GSSName localGSSName = ((GSSManagerImpl)localObject).createName(str, null);
    this.context = ((GSSManagerImpl)localObject).createContext(localGSSName, localOid, null, 0);
    this.context.requestCredDeleg(true);
    this.oneToken = this.context.initSecContext(new byte[0], 0, 0);
  }

  public NegotiatorImpl(String paramString1, String paramString2)
    throws Exception
  {
    init(paramString1, paramString2);
  }

  public byte[] firstToken()
  {
    return this.oneToken;
  }

  public byte[] nextToken(byte[] paramArrayOfByte)
    throws Exception
  {
    return this.context.initSecContext(paramArrayOfByte, 0, paramArrayOfByte.length);
  }
}