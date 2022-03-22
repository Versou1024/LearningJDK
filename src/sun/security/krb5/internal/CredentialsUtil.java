package sun.security.krb5.internal;

import java.io.IOException;
import java.io.PrintStream;
import sun.security.krb5.Config;
import sun.security.krb5.Credentials;
import sun.security.krb5.KrbException;
import sun.security.krb5.KrbTgsRep;
import sun.security.krb5.KrbTgsReq;
import sun.security.krb5.PrincipalName;
import sun.security.krb5.Realm;
import sun.security.krb5.ServiceName;

public class CredentialsUtil
{
  private static boolean DEBUG = Krb5.DEBUG;

  public static Credentials acquireServiceCreds(String paramString, Credentials paramCredentials)
    throws KrbException, IOException
  {
    ServiceName localServiceName1 = new ServiceName(paramString);
    Object localObject1 = localServiceName1.getRealmString();
    Object localObject2 = paramCredentials.getClient().getRealmString();
    String str1 = Config.getInstance().getDefaultRealm();
    if (localObject2 == null)
    {
      localObject3 = null;
      if ((localObject3 = paramCredentials.getServer()) != null)
        localObject2 = ((PrincipalName)localObject3).getRealmString();
    }
    if (localObject2 == null)
      localObject2 = str1;
    if (localObject1 == null)
    {
      localObject1 = localObject2;
      localServiceName1.setRealm((String)localObject1);
    }
    if (((String)localObject2).equals(localObject1))
    {
      if (DEBUG)
        System.out.println(">>> Credentials acquireServiceCreds: same realm");
      return serviceCreds(localServiceName1, paramCredentials);
    }
    Object localObject3 = Realm.getRealmsList((String)localObject2, (String)localObject1);
    if ((localObject3 == null) || (localObject3.length == 0))
    {
      if (DEBUG)
        System.out.println(">>> Credentials acquireServiceCreds: no realms list");
      return null;
    }
    int i = 0;
    int j = 0;
    Object localObject4 = null;
    Credentials localCredentials1 = null;
    Credentials localCredentials2 = null;
    ServiceName localServiceName2 = null;
    Object localObject5 = null;
    String str2 = null;
    String str3 = null;
    localObject4 = paramCredentials;
    i = 0;
    while (true)
    {
      do
      {
        if (i >= localObject3.length)
          break label538;
        localServiceName2 = new ServiceName("krbtgt", (String)localObject1, localObject3[i]);
        if (DEBUG)
          System.out.println(">>> Credentials acquireServiceCreds: main loop: [" + i + "] tempService=" + localServiceName2);
        try
        {
          localCredentials1 = serviceCreds(localServiceName2, (Credentials)localObject4);
        }
        catch (Exception localException1)
        {
          localCredentials1 = null;
        }
        if (localCredentials1 == null)
        {
          if (DEBUG)
            System.out.println(">>> Credentials acquireServiceCreds: no tgt; searching backwards");
          localCredentials1 = null;
          for (j = localObject3.length - 1; (localCredentials1 == null) && (j > i); --j)
          {
            localServiceName2 = new ServiceName("krbtgt", localObject3[j], localObject3[i]);
            if (DEBUG)
              System.out.println(">>> Credentials acquireServiceCreds: inner loop: [" + j + "] tempService=" + localServiceName2);
            try
            {
              localCredentials1 = serviceCreds(localServiceName2, (Credentials)localObject4);
            }
            catch (Exception localException2)
            {
              localCredentials1 = null;
            }
          }
        }
        if (localCredentials1 == null)
        {
          if (!(DEBUG))
            break label538;
          System.out.println(">>> Credentials acquireServiceCreds: no tgt; cannot get creds");
          break label538:
        }
        str2 = localCredentials1.getServer().getInstanceComponent();
        if (DEBUG)
          System.out.println(">>> Credentials acquireServiceCreds: got tgt");
        if (str2.equals(localObject1))
        {
          localCredentials2 = localCredentials1;
          str3 = str2;
          break label538:
        }
        for (j = i + 1; j < localObject3.length; ++j)
          if (str2.equals(localObject3[j]))
            break;
        if (j >= localObject3.length)
          break label538;
        i = j;
        localObject4 = localCredentials1;
      }
      while (!(DEBUG));
      System.out.println(">>> Credentials acquireServiceCreds: continuing with main loop counter reset to " + i);
    }
    label538: Credentials localCredentials3 = null;
    if (localCredentials2 != null)
    {
      if (DEBUG)
      {
        System.out.println(">>> Credentials acquireServiceCreds: got right tgt");
        System.out.println(">>> Credentials acquireServiceCreds: obtaining service creds for " + localServiceName1);
      }
      try
      {
        localCredentials3 = serviceCreds(localServiceName1, localCredentials2);
      }
      catch (Exception localException3)
      {
        if (DEBUG)
          System.out.println(localException3);
        localCredentials3 = null;
      }
    }
    if (localCredentials3 != null)
    {
      if (DEBUG)
      {
        System.out.println(">>> Credentials acquireServiceCreds: returning creds:");
        Credentials.printDebug(localCredentials3);
      }
      return localCredentials3;
    }
    throw new KrbApErrException(63, "No service creds");
  }

  private static Credentials serviceCreds(ServiceName paramServiceName, Credentials paramCredentials)
    throws KrbException, IOException
  {
    KrbTgsReq localKrbTgsReq = new KrbTgsReq(paramCredentials, paramServiceName);
    KrbTgsRep localKrbTgsRep = null;
    String str = null;
    try
    {
      str = localKrbTgsReq.send();
      localKrbTgsRep = localKrbTgsReq.getReply();
    }
    catch (KrbException localKrbException)
    {
      if (localKrbException.returnCode() == 52)
      {
        localKrbTgsReq.send(paramServiceName.getRealmString(), str, true);
        localKrbTgsRep = localKrbTgsReq.getReply();
      }
      else
      {
        throw localKrbException;
      }
    }
    return localKrbTgsRep.getCreds();
  }
}