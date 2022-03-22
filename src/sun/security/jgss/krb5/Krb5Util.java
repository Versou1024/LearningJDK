package sun.security.jgss.krb5;

import java.io.IOException;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.util.List;
import java.util.Set;
import javax.crypto.SecretKey;
import javax.security.auth.Subject;
import javax.security.auth.kerberos.KerberosKey;
import javax.security.auth.kerberos.KerberosPrincipal;
import javax.security.auth.kerberos.KerberosTicket;
import javax.security.auth.login.LoginException;
import sun.security.action.GetBooleanAction;
import sun.security.jgss.GSSUtil;
import sun.security.krb5.Credentials;
import sun.security.krb5.EncryptionKey;
import sun.security.krb5.KrbException;
import sun.security.krb5.PrincipalName;

public class Krb5Util
{
  static final boolean DEBUG = ((Boolean)AccessController.doPrivileged(new GetBooleanAction("sun.security.krb5.debug"))).booleanValue();

  public static KerberosTicket getTicketFromSubjectAndTgs(int paramInt, String paramString1, String paramString2, String paramString3, AccessControlContext paramAccessControlContext)
    throws LoginException, KrbException, IOException
  {
    int i;
    Subject localSubject1 = Subject.getSubject(paramAccessControlContext);
    KerberosTicket localKerberosTicket1 = (KerberosTicket)SubjectComber.find(localSubject1, paramString2, paramString1, KerberosTicket.class);
    if (localKerberosTicket1 != null)
      return localKerberosTicket1;
    Subject localSubject2 = null;
    if (!(GSSUtil.useSubjectCredsOnly()))
      try
      {
        localSubject2 = GSSUtil.login(paramInt, GSSUtil.GSS_KRB5_MECH_OID);
        localKerberosTicket1 = (KerberosTicket)SubjectComber.find(localSubject2, paramString2, paramString1, KerberosTicket.class);
        if (localKerberosTicket1 != null)
          return localKerberosTicket1;
      }
      catch (LoginException localLoginException)
      {
      }
    KerberosTicket localKerberosTicket2 = (KerberosTicket)SubjectComber.find(localSubject1, paramString3, paramString1, KerberosTicket.class);
    if ((localKerberosTicket2 == null) && (localSubject2 != null))
    {
      localKerberosTicket2 = (KerberosTicket)SubjectComber.find(localSubject2, paramString3, paramString1, KerberosTicket.class);
      i = 0;
    }
    else
    {
      i = 1;
    }
    if (localKerberosTicket2 != null)
    {
      Credentials localCredentials1 = ticketToCreds(localKerberosTicket2);
      Credentials localCredentials2 = Credentials.acquireServiceCreds(paramString2, localCredentials1);
      if (localCredentials2 != null)
      {
        localKerberosTicket1 = credsToTicket(localCredentials2);
        if ((i != 0) && (localSubject1 != null) && (!(localSubject1.isReadOnly())))
          localSubject1.getPrivateCredentials().add(localKerberosTicket1);
      }
    }
    return localKerberosTicket1;
  }

  static KerberosTicket getTicket(int paramInt, String paramString1, String paramString2, AccessControlContext paramAccessControlContext)
    throws LoginException
  {
    Subject localSubject1 = Subject.getSubject(paramAccessControlContext);
    KerberosTicket localKerberosTicket = (KerberosTicket)SubjectComber.find(localSubject1, paramString2, paramString1, KerberosTicket.class);
    if ((localKerberosTicket == null) && (!(GSSUtil.useSubjectCredsOnly())))
    {
      Subject localSubject2 = GSSUtil.login(paramInt, GSSUtil.GSS_KRB5_MECH_OID);
      localKerberosTicket = (KerberosTicket)SubjectComber.find(localSubject2, paramString2, paramString1, KerberosTicket.class);
    }
    return localKerberosTicket;
  }

  public static Subject getSubject(int paramInt, AccessControlContext paramAccessControlContext)
    throws LoginException
  {
    Subject localSubject = Subject.getSubject(paramAccessControlContext);
    if ((localSubject == null) && (!(GSSUtil.useSubjectCredsOnly())))
      localSubject = GSSUtil.login(paramInt, GSSUtil.GSS_KRB5_MECH_OID);
    return localSubject;
  }

  public static KerberosKey[] getKeys(int paramInt, String paramString, AccessControlContext paramAccessControlContext)
    throws LoginException
  {
    Subject localSubject1 = Subject.getSubject(paramAccessControlContext);
    List localList = (List)SubjectComber.findMany(localSubject1, paramString, null, KerberosKey.class);
    if ((localList == null) && (!(GSSUtil.useSubjectCredsOnly())))
    {
      Subject localSubject2 = GSSUtil.login(paramInt, GSSUtil.GSS_KRB5_MECH_OID);
      localList = (List)SubjectComber.findMany(localSubject2, paramString, null, KerberosKey.class);
    }
    if (localList != null)
    {
      int i;
      if ((i = localList.size()) > 0)
      {
        KerberosKey[] arrayOfKerberosKey = new KerberosKey[i];
        localList.toArray(arrayOfKerberosKey);
        return arrayOfKerberosKey;
      }
    }
    return null;
  }

  public static KerberosTicket credsToTicket(Credentials paramCredentials)
  {
    EncryptionKey localEncryptionKey = paramCredentials.getSessionKey();
    return new KerberosTicket(paramCredentials.getEncoded(), new KerberosPrincipal(paramCredentials.getClient().getName()), new KerberosPrincipal(paramCredentials.getServer().getName()), localEncryptionKey.getBytes(), localEncryptionKey.getEType(), paramCredentials.getFlags(), paramCredentials.getAuthTime(), paramCredentials.getStartTime(), paramCredentials.getEndTime(), paramCredentials.getRenewTill(), paramCredentials.getClientAddresses());
  }

  public static Credentials ticketToCreds(KerberosTicket paramKerberosTicket)
    throws KrbException, IOException
  {
    return new Credentials(paramKerberosTicket.getEncoded(), paramKerberosTicket.getClient().getName(), paramKerberosTicket.getServer().getName(), paramKerberosTicket.getSessionKey().getEncoded(), paramKerberosTicket.getSessionKeyType(), paramKerberosTicket.getFlags(), paramKerberosTicket.getAuthTime(), paramKerberosTicket.getStartTime(), paramKerberosTicket.getEndTime(), paramKerberosTicket.getRenewTill(), paramKerberosTicket.getClientAddresses());
  }
}