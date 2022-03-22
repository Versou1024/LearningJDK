package sun.security.jgss;

import com.sun.security.auth.callback.TextCallbackHandler;
import java.io.PrintStream;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.Security;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;
import javax.crypto.SecretKey;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.kerberos.KerberosKey;
import javax.security.auth.kerberos.KerberosPrincipal;
import javax.security.auth.kerberos.KerberosTicket;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.Oid;
import sun.net.www.protocol.http.NegotiateCallbackHandler;
import sun.security.action.GetBooleanAction;
import sun.security.action.GetPropertyAction;
import sun.security.jgss.krb5.Krb5NameElement;
import sun.security.jgss.spi.GSSCredentialSpi;
import sun.security.jgss.spi.GSSNameSpi;
import sun.security.jgss.spnego.SpNegoCredElement;
import sun.security.krb5.PrincipalName;

public class GSSUtil
{
  public static final Oid GSS_KRB5_MECH_OID;
  public static final Oid GSS_KRB5_MECH_OID2;
  public static final Oid GSS_SPNEGO_MECH_OID;
  public static final Oid NT_GSS_KRB5_PRINCIPAL;
  public static final Oid NT_HOSTBASED_SERVICE2;
  private static final String DEFAULT_HANDLER = "auth.login.defaultCallbackHandler";
  public static final int CALLER_UNKNOWN = -1;
  public static final int CALLER_INITIATE = 1;
  public static final int CALLER_ACCEPT = 2;
  public static final int CALLER_SSL_CLIENT = 3;
  public static final int CALLER_SSL_SERVER = 4;
  public static final int CALLER_HTTP_NEGOTIATE = 5;
  static final boolean DEBUG;

  static void debug(String paramString)
  {
    if (DEBUG)
    {
      if ((!($assertionsDisabled)) && (paramString == null))
        throw new AssertionError();
      System.out.println(paramString);
    }
  }

  public static Oid createOid(String paramString)
  {
    try
    {
      return new Oid(paramString);
    }
    catch (GSSException localGSSException)
    {
      debug("Ignored invalid OID: " + paramString);
    }
    return null;
  }

  public static boolean isSpNegoMech(Oid paramOid)
  {
    return GSS_SPNEGO_MECH_OID.equals(paramOid);
  }

  public static boolean isKerberosMech(Oid paramOid)
  {
    return ((GSS_KRB5_MECH_OID.equals(paramOid)) || (GSS_KRB5_MECH_OID2.equals(paramOid)));
  }

  public static String getMechStr(Oid paramOid)
  {
    if (isSpNegoMech(paramOid))
      return "SPNEGO";
    if (isKerberosMech(paramOid))
      return "Kerberos V5";
    return paramOid.toString();
  }

  public static Subject getSubject(GSSName paramGSSName, GSSCredential paramGSSCredential)
  {
    Object localObject = null;
    HashSet localHashSet1 = null;
    HashSet localHashSet2 = new HashSet();
    Set localSet = null;
    HashSet localHashSet3 = new HashSet();
    if (paramGSSName instanceof GSSNameImpl)
      try
      {
        GSSNameSpi localGSSNameSpi = ((GSSNameImpl)paramGSSName).getElement(GSS_KRB5_MECH_OID);
        String str = localGSSNameSpi.toString();
        if (localGSSNameSpi instanceof Krb5NameElement)
          str = ((Krb5NameElement)localGSSNameSpi).getKrb5PrincipalName().getName();
        KerberosPrincipal localKerberosPrincipal = new KerberosPrincipal(str);
        localHashSet3.add(localKerberosPrincipal);
      }
      catch (GSSException localGSSException)
      {
        debug("Skipped name " + paramGSSName + " due to " + localGSSException);
      }
    if (paramGSSCredential instanceof GSSCredentialImpl)
    {
      localSet = ((GSSCredentialImpl)paramGSSCredential).getElements();
      localHashSet1 = new HashSet(localSet.size());
      populateCredentials(localHashSet1, localSet);
    }
    else
    {
      localHashSet1 = new HashSet();
    }
    debug("Created Subject with the following");
    debug("principals=" + localHashSet3);
    debug("public creds=" + localHashSet2);
    debug("private creds=" + localHashSet1);
    return new Subject(false, localHashSet3, localHashSet2, localHashSet1);
  }

  private static void populateCredentials(Set paramSet1, Set paramSet2)
  {
    Iterator localIterator = paramSet2.iterator();
    while (true)
    {
      Object localObject1;
      while (true)
      {
        Object localObject2;
        while (true)
        {
          if (!(localIterator.hasNext()))
            return;
          localObject1 = localIterator.next();
          if (localObject1 instanceof SpNegoCredElement)
            localObject1 = ((SpNegoCredElement)localObject1).getInternalCred();
          if (!(localObject1 instanceof KerberosTicket))
            break;
          if (!(localObject1.getClass().getName().equals("javax.security.auth.kerberos.KerberosTicket")))
          {
            localObject2 = (KerberosTicket)localObject1;
            localObject1 = new KerberosTicket(((KerberosTicket)localObject2).getEncoded(), ((KerberosTicket)localObject2).getClient(), ((KerberosTicket)localObject2).getServer(), ((KerberosTicket)localObject2).getSessionKey().getEncoded(), ((KerberosTicket)localObject2).getSessionKeyType(), ((KerberosTicket)localObject2).getFlags(), ((KerberosTicket)localObject2).getAuthTime(), ((KerberosTicket)localObject2).getStartTime(), ((KerberosTicket)localObject2).getEndTime(), ((KerberosTicket)localObject2).getRenewTill(), ((KerberosTicket)localObject2).getClientAddresses());
          }
          paramSet1.add(localObject1);
        }
        if (!(localObject1 instanceof KerberosKey))
          break;
        if (!(localObject1.getClass().getName().equals("javax.security.auth.kerberos.KerberosKey")))
        {
          localObject2 = (KerberosKey)localObject1;
          localObject1 = new KerberosKey(((KerberosKey)localObject2).getPrincipal(), ((KerberosKey)localObject2).getEncoded(), ((KerberosKey)localObject2).getKeyType(), ((KerberosKey)localObject2).getVersionNumber());
        }
        paramSet1.add(localObject1);
      }
      debug("Skipped cred element: " + localObject1);
    }
  }

  public static Subject login(int paramInt, Oid paramOid)
    throws LoginException
  {
    Object localObject1 = null;
    if (paramInt == 5)
    {
      localObject1 = new NegotiateCallbackHandler();
    }
    else
    {
      localObject2 = Security.getProperty("auth.login.defaultCallbackHandler");
      if ((localObject2 != null) && (((String)localObject2).length() != 0))
        localObject1 = null;
      else
        localObject1 = new TextCallbackHandler();
    }
    Object localObject2 = new LoginContext("", null, (CallbackHandler)localObject1, new LoginConfigImpl(paramInt, paramOid));
    ((LoginContext)localObject2).login();
    return ((Subject)(Subject)((LoginContext)localObject2).getSubject());
  }

  public static boolean useSubjectCredsOnly()
  {
    String str = (String)AccessController.doPrivileged(new GetPropertyAction("javax.security.auth.useSubjectCredsOnly", "true"));
    return (!(str.equalsIgnoreCase("false")));
  }

  public static boolean useMSInterop()
  {
    String str = (String)AccessController.doPrivileged(new GetPropertyAction("sun.security.spnego.msinterop", "true"));
    return (!(str.equalsIgnoreCase("false")));
  }

  public static Vector searchSubject(GSSNameSpi paramGSSNameSpi, Oid paramOid, boolean paramBoolean, Class paramClass)
  {
    debug("Search Subject for " + getMechStr(paramOid) + ((paramBoolean) ? " INIT" : " ACCEPT") + " cred (" + ((paramGSSNameSpi == null) ? "<<DEF>>" : paramGSSNameSpi.toString()) + ", " + paramClass.getName() + ")");
    AccessControlContext localAccessControlContext = AccessController.getContext();
    try
    {
      Vector localVector = (Vector)AccessController.doPrivileged(new PrivilegedExceptionAction(localAccessControlContext, paramOid, paramBoolean, paramClass, paramGSSNameSpi)
      {
        public Vector run()
          throws Exception
        {
          Subject localSubject = Subject.getSubject(this.val$acc);
          Vector localVector = null;
          if (localSubject != null)
          {
            localVector = new Vector();
            Iterator localIterator = localSubject.getPrivateCredentials(GSSCredentialImpl.class).iterator();
            while (localIterator.hasNext())
            {
              GSSCredentialImpl localGSSCredentialImpl = (GSSCredentialImpl)localIterator.next();
              GSSUtil.debug("...Found cred" + localGSSCredentialImpl);
              try
              {
                GSSCredentialSpi localGSSCredentialSpi = localGSSCredentialImpl.getElement(this.val$mech, this.val$initiate);
                GSSUtil.debug("......Found element: " + localGSSCredentialSpi);
                if ((localGSSCredentialSpi.getClass().equals(this.val$credCls)) && (((this.val$name == null) || (this.val$name.equals(localGSSCredentialSpi.getName())))))
                  localVector.add(localGSSCredentialSpi);
                else
                  GSSUtil.debug("......Discard element");
              }
              catch (GSSException localGSSException)
              {
                GSSUtil.debug("...Discard cred (" + localGSSException + ")");
              }
            }
          }
          else
          {
            GSSUtil.debug("No Subject");
          }
          return localVector;
        }
      });
      return localVector;
    }
    catch (PrivilegedActionException localPrivilegedActionException)
    {
      debug("Unexpected exception when searching Subject:");
      if (DEBUG)
        localPrivilegedActionException.printStackTrace();
    }
    return null;
  }

  static
  {
    GSS_KRB5_MECH_OID = createOid("1.2.840.113554.1.2.2");
    GSS_KRB5_MECH_OID2 = createOid("1.3.5.1.5.2");
    GSS_SPNEGO_MECH_OID = createOid("1.3.6.1.5.5.2");
    NT_GSS_KRB5_PRINCIPAL = createOid("1.2.840.113554.1.2.2.1");
    NT_HOSTBASED_SERVICE2 = createOid("1.2.840.113554.1.2.1.4");
    DEBUG = ((Boolean)AccessController.doPrivileged(new GetBooleanAction("sun.security.jgss.debug"))).booleanValue();
  }
}