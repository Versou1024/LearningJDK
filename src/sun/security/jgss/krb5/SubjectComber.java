package sun.security.jgss.krb5;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.security.auth.DestroyFailedException;
import javax.security.auth.Subject;
import javax.security.auth.kerberos.KerberosKey;
import javax.security.auth.kerberos.KerberosPrincipal;
import javax.security.auth.kerberos.KerberosTicket;

class SubjectComber
{
  private static final boolean DEBUG = Krb5Util.DEBUG;

  static Object find(Subject paramSubject, String paramString1, String paramString2, Class paramClass)
  {
    return findAux(paramSubject, paramString1, paramString2, paramClass, true);
  }

  static Object findMany(Subject paramSubject, String paramString1, String paramString2, Class paramClass)
  {
    return findAux(paramSubject, paramString1, paramString2, paramClass, false);
  }

  private static Object findAux(Subject paramSubject, String paramString1, String paramString2, Class paramClass, boolean paramBoolean)
  {
    Object localObject1;
    if (paramSubject == null)
      return null;
    ArrayList localArrayList = new ArrayList();
    if (paramClass == KerberosKey.class)
    {
      localObject1 = paramSubject.getPrivateCredentials(KerberosKey.class).iterator();
      while (((Iterator)localObject1).hasNext())
      {
        ??? = (KerberosKey)((Iterator)localObject1).next();
        if ((paramString1 == null) || (paramString1.equals(((KerberosKey)???).getPrincipal().getName())))
        {
          if (DEBUG)
            System.out.println("Found key for " + ((KerberosKey)???).getPrincipal() + "(" + ((KerberosKey)???).getKeyType() + ")");
          if (paramBoolean)
            return ???;
          if (paramString1 == null)
            paramString1 = ((KerberosKey)???).getPrincipal().getName();
          label486: localArrayList.add(???);
        }
      }
    }
    else if (paramClass == KerberosTicket.class)
    {
      localObject1 = paramSubject.getPrivateCredentials();
      synchronized (localObject1)
      {
        Iterator localIterator = ((Set)localObject1).iterator();
        while (true)
        {
          if (!(localIterator.hasNext()))
            break label486;
          Object localObject3 = localIterator.next();
          if (localObject3 instanceof KerberosTicket)
          {
            KerberosTicket localKerberosTicket = (KerberosTicket)localObject3;
            if (DEBUG)
              System.out.println("Found ticket for " + localKerberosTicket.getClient() + " to go to " + localKerberosTicket.getServer() + " expiring on " + localKerberosTicket.getEndTime());
            if (!(localKerberosTicket.isCurrent()))
            {
              if (!(paramSubject.isReadOnly()))
              {
                localIterator.remove();
                try
                {
                  localKerberosTicket.destroy();
                  if (DEBUG)
                    System.out.println("Removed and destroyed the expired Ticket \n" + localKerberosTicket);
                }
                catch (DestroyFailedException localDestroyFailedException)
                {
                  if (DEBUG)
                    System.out.println("Expired ticket not detroyed successfully. " + localDestroyFailedException);
                }
              }
            }
            else if ((((paramString1 == null) || (localKerberosTicket.getServer().getName().equals(paramString1)))) && (((paramString2 == null) || (paramString2.equals(localKerberosTicket.getClient().getName())))))
            {
              if (paramBoolean)
                return localKerberosTicket;
              if (paramString2 == null)
                paramString2 = localKerberosTicket.getClient().getName();
              if (paramString1 == null)
                paramString1 = localKerberosTicket.getServer().getName();
              localArrayList.add(localKerberosTicket);
            }
          }
        }
      }
    }
    return localArrayList;
  }
}