package sun.security.krb5;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Date;
import sun.security.action.GetPropertyAction;
import sun.security.krb5.internal.CredentialsUtil;
import sun.security.krb5.internal.HostAddresses;
import sun.security.krb5.internal.KDCOptions;
import sun.security.krb5.internal.KRBError;
import sun.security.krb5.internal.KerberosTime;
import sun.security.krb5.internal.Krb5;
import sun.security.krb5.internal.Ticket;
import sun.security.krb5.internal.TicketFlags;
import sun.security.krb5.internal.ccache.CredentialsCache;
import sun.security.krb5.internal.crypto.EType;
import sun.security.krb5.internal.ktab.KeyTab;

public class Credentials
{
  Ticket ticket;
  PrincipalName client;
  PrincipalName server;
  EncryptionKey key;
  TicketFlags flags;
  KerberosTime authTime;
  KerberosTime startTime;
  KerberosTime endTime;
  KerberosTime renewTill;
  HostAddresses cAddr;
  EncryptionKey serviceKey;
  private static boolean DEBUG = Krb5.DEBUG;
  private static CredentialsCache cache;
  static boolean alreadyLoaded = false;
  private static boolean alreadyTried = false;

  private static native Credentials acquireDefaultNativeCreds();

  public Credentials(Ticket paramTicket, PrincipalName paramPrincipalName1, PrincipalName paramPrincipalName2, EncryptionKey paramEncryptionKey, TicketFlags paramTicketFlags, KerberosTime paramKerberosTime1, KerberosTime paramKerberosTime2, KerberosTime paramKerberosTime3, KerberosTime paramKerberosTime4, HostAddresses paramHostAddresses)
  {
    this.ticket = paramTicket;
    this.client = paramPrincipalName1;
    this.server = paramPrincipalName2;
    this.key = paramEncryptionKey;
    this.flags = paramTicketFlags;
    this.authTime = paramKerberosTime1;
    this.startTime = paramKerberosTime2;
    this.endTime = paramKerberosTime3;
    this.renewTill = paramKerberosTime4;
    this.cAddr = paramHostAddresses;
  }

  public Credentials(byte[] paramArrayOfByte1, String paramString1, String paramString2, byte[] paramArrayOfByte2, int paramInt, boolean[] paramArrayOfBoolean, Date paramDate1, Date paramDate2, Date paramDate3, Date paramDate4, InetAddress[] paramArrayOfInetAddress)
    throws KrbException, IOException
  {
    this(new Ticket(paramArrayOfByte1), new PrincipalName(paramString1, 1), new PrincipalName(paramString2), new EncryptionKey(paramInt, paramArrayOfByte2), new TicketFlags(paramArrayOfBoolean), new KerberosTime(paramDate1), new KerberosTime(paramDate2), new KerberosTime(paramDate3), new KerberosTime(paramDate4), null);
  }

  public final PrincipalName getClient()
  {
    return this.client;
  }

  public final PrincipalName getServer()
  {
    return this.server;
  }

  public final EncryptionKey getSessionKey()
  {
    return this.key;
  }

  public final Date getAuthTime()
  {
    if (this.authTime != null)
      return this.authTime.toDate();
    return null;
  }

  public final Date getStartTime()
  {
    if (this.startTime != null)
      return this.startTime.toDate();
    return null;
  }

  public final Date getEndTime()
  {
    if (this.endTime != null)
      return this.endTime.toDate();
    return null;
  }

  public final Date getRenewTill()
  {
    if (this.renewTill != null)
      return this.renewTill.toDate();
    return null;
  }

  public final boolean[] getFlags()
  {
    if (this.flags == null)
      return null;
    return this.flags.toBooleanArray();
  }

  public final InetAddress[] getClientAddresses()
  {
    if (this.cAddr == null)
      return null;
    return this.cAddr.getInetAddresses();
  }

  public final byte[] getEncoded()
  {
    byte[] arrayOfByte = null;
    try
    {
      arrayOfByte = this.ticket.asn1Encode();
    }
    catch (Asn1Exception localAsn1Exception)
    {
      if (DEBUG)
        System.out.println(localAsn1Exception);
    }
    catch (IOException localIOException)
    {
      if (DEBUG)
        System.out.println(localIOException);
    }
    return arrayOfByte;
  }

  public boolean isForwardable()
  {
    return this.flags.get(1);
  }

  public boolean isRenewable()
  {
    return this.flags.get(8);
  }

  public Ticket getTicket()
  {
    return this.ticket;
  }

  public TicketFlags getTicketFlags()
  {
    return this.flags;
  }

  public boolean checkDelegate()
  {
    return this.flags.get(13);
  }

  public Credentials renew()
    throws KrbException, IOException
  {
    KDCOptions localKDCOptions = new KDCOptions();
    localKDCOptions.set(30, true);
    localKDCOptions.set(8, true);
    KrbTgsReq localKrbTgsReq = new KrbTgsReq(localKDCOptions, this, this.server, null, null, null, null, this.cAddr, null, null, null);
    String str = null;
    KrbTgsRep localKrbTgsRep = null;
    try
    {
      str = localKrbTgsReq.send();
      localKrbTgsRep = localKrbTgsReq.getReply();
    }
    catch (KrbException localKrbException)
    {
      if (localKrbException.returnCode() == 52)
      {
        localKrbTgsReq.send(this.server.getRealmString(), str, true);
        localKrbTgsRep = localKrbTgsReq.getReply();
      }
      else
      {
        throw localKrbException;
      }
    }
    return localKrbTgsRep.getCreds();
  }

  public static Credentials acquireTGTFromCache(PrincipalName paramPrincipalName, String paramString)
    throws KrbException, IOException
  {
    if (paramString == null)
    {
      localObject1 = (String)AccessController.doPrivileged(new GetPropertyAction("os.name"));
      if (((String)localObject1).toUpperCase().startsWith("WINDOWS"))
      {
        localObject2 = acquireDefaultCreds();
        if (localObject2 == null)
        {
          if (DEBUG)
            System.out.println(">>> Found no TGT's in LSA");
          return null;
        }
        if (paramPrincipalName != null)
        {
          if (((Credentials)localObject2).getClient().equals(paramPrincipalName))
          {
            if (DEBUG)
              System.out.println(">>> Obtained TGT from LSA: " + localObject2);
            return localObject2;
          }
          if (DEBUG)
            System.out.println(">>> LSA contains TGT for " + ((Credentials)localObject2).getClient() + " not " + paramPrincipalName);
          return null;
        }
        if (DEBUG)
          System.out.println(">>> Obtained TGT from LSA: " + localObject2);
        return localObject2;
      }
    }
    Object localObject1 = CredentialsCache.getInstance(paramPrincipalName, paramString);
    if (localObject1 == null)
      return null;
    Object localObject2 = ((CredentialsCache)localObject1).getDefaultCreds();
    if (EType.isSupported(((sun.security.krb5.internal.ccache.Credentials)localObject2).getEType()))
      return ((sun.security.krb5.internal.ccache.Credentials)localObject2).setKrbCreds();
    if (DEBUG)
      System.out.println(">>> unsupported key type found the default TGT: " + ((sun.security.krb5.internal.ccache.Credentials)localObject2).getEType());
    return ((Credentials)(Credentials)null);
  }

  public static Credentials acquireTGT(PrincipalName paramPrincipalName, EncryptionKey[] paramArrayOfEncryptionKey, char[] paramArrayOfChar)
    throws KrbException, IOException
  {
    if (paramPrincipalName == null)
      throw new IllegalArgumentException("Cannot have null principal to do AS-Exchange");
    if (paramArrayOfEncryptionKey == null)
      throw new IllegalArgumentException("Cannot have null secretKey to do AS-Exchange");
    KrbAsRep localKrbAsRep = null;
    try
    {
      localKrbAsRep = sendASRequest(paramPrincipalName, paramArrayOfEncryptionKey, null);
    }
    catch (KrbException localKrbException)
    {
      if ((localKrbException.returnCode() != 24) && (localKrbException.returnCode() != 25))
        break label152;
      if (DEBUG)
        System.out.println("AcquireTGT: PREAUTH FAILED/REQUIRED, re-send AS-REQ");
      KRBError localKRBError = localKrbException.getError();
      byte[] arrayOfByte = localKRBError.getSalt();
      if ((arrayOfByte != null) && (arrayOfByte.length > 0))
        paramPrincipalName.setSalt(new String(arrayOfByte));
      if (paramArrayOfChar != null)
        paramArrayOfEncryptionKey = EncryptionKey.acquireSecretKeys(paramArrayOfChar, paramPrincipalName.getSalt(), true, localKRBError.getEType(), localKRBError.getParams());
      localKrbAsRep = sendASRequest(paramPrincipalName, paramArrayOfEncryptionKey, localKrbException.getError());
    }
    label152: throw localKrbException;
    return localKrbAsRep.getCreds();
  }

  private static KrbAsRep sendASRequest(PrincipalName paramPrincipalName, EncryptionKey[] paramArrayOfEncryptionKey, KRBError paramKRBError)
    throws KrbException, IOException
  {
    KrbAsReq localKrbAsReq = null;
    if (paramKRBError == null)
      localKrbAsReq = new KrbAsReq(paramPrincipalName, paramArrayOfEncryptionKey);
    else
      localKrbAsReq = new KrbAsReq(paramPrincipalName, paramArrayOfEncryptionKey, true, paramKRBError.getEType(), paramKRBError.getSalt(), paramKRBError.getParams());
    String str = null;
    KrbAsRep localKrbAsRep = null;
    try
    {
      str = localKrbAsReq.send();
      localKrbAsRep = localKrbAsReq.getReply(paramArrayOfEncryptionKey);
    }
    catch (KrbException localKrbException)
    {
      if (localKrbException.returnCode() == 52)
      {
        localKrbAsReq.send(paramPrincipalName.getRealmString(), str, true);
        localKrbAsRep = localKrbAsReq.getReply(paramArrayOfEncryptionKey);
      }
      else
      {
        throw localKrbException;
      }
    }
    return localKrbAsRep;
  }

  public static synchronized Credentials acquireDefaultCreds()
  {
    Credentials localCredentials = null;
    if (cache == null)
      cache = CredentialsCache.getInstance();
    if (cache != null)
    {
      if (DEBUG)
        System.out.println(">>> KrbCreds found the default ticket granting ticket in credential cache.");
      sun.security.krb5.internal.ccache.Credentials localCredentials1 = cache.getDefaultCreds();
      if (EType.isSupported(localCredentials1.getEType()))
        localCredentials = localCredentials1.setKrbCreds();
      else if (DEBUG)
        System.out.println(">>> unsupported key type found the default TGT: " + localCredentials1.getEType());
    }
    if (localCredentials == null)
    {
      if (!(alreadyTried))
        try
        {
          ensureLoaded();
        }
        catch (Exception localException)
        {
          if (DEBUG)
          {
            System.out.println("Can not load credentials cache");
            localException.printStackTrace();
          }
          alreadyTried = true;
        }
      if (alreadyLoaded)
      {
        if (DEBUG)
          System.out.println(">> Acquire default native Credentials");
        localCredentials = acquireDefaultNativeCreds();
      }
    }
    return localCredentials;
  }

  public static Credentials getServiceCreds(String paramString, File paramFile)
  {
    KeyTab localKeyTab;
    EncryptionKey localEncryptionKey = null;
    PrincipalName localPrincipalName = null;
    Credentials localCredentials = null;
    try
    {
      localPrincipalName = new PrincipalName(paramString);
      if (localPrincipalName.getRealm() == null)
      {
        String str = Config.getInstance().getDefaultRealm();
        if (str == null)
          return null;
        localPrincipalName.setRealm(str);
      }
    }
    catch (RealmException localRealmException)
    {
      if (DEBUG)
        localRealmException.printStackTrace();
      return null;
    }
    catch (KrbException localKrbException)
    {
      if (DEBUG)
        localKrbException.printStackTrace();
      return null;
    }
    if (paramFile == null)
      localKeyTab = KeyTab.getInstance();
    else
      localKeyTab = KeyTab.getInstance(paramFile);
    if ((localKeyTab != null) && (localKeyTab.findServiceEntry(localPrincipalName)))
    {
      localEncryptionKey = localKeyTab.readServiceKey(localPrincipalName);
      localCredentials = new Credentials(null, localPrincipalName, null, null, null, null, null, null, null, null);
      localCredentials.serviceKey = localEncryptionKey;
    }
    return localCredentials;
  }

  public static Credentials acquireServiceCreds(String paramString, Credentials paramCredentials)
    throws KrbException, IOException
  {
    return CredentialsUtil.acquireServiceCreds(paramString, paramCredentials);
  }

  private static Credentials serviceCreds(ServiceName paramServiceName, Credentials paramCredentials)
    throws KrbException, IOException
  {
    KrbTgsReq localKrbTgsReq = new KrbTgsReq(new KDCOptions(), paramCredentials, paramServiceName, null, null, null, null, null, null, null, null);
    String str = null;
    KrbTgsRep localKrbTgsRep = null;
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

  public CredentialsCache getCache()
  {
    return cache;
  }

  public EncryptionKey getServiceKey()
  {
    return this.serviceKey;
  }

  public static void printDebug(Credentials paramCredentials)
  {
    System.out.println(">>> DEBUG: ----Credentials----");
    System.out.println("\tclient: " + paramCredentials.client.toString());
    System.out.println("\tserver: " + paramCredentials.server.toString());
    System.out.println("\tticket: realm: " + paramCredentials.ticket.realm.toString());
    System.out.println("\t        sname: " + paramCredentials.ticket.sname.toString());
    if (paramCredentials.startTime != null)
      System.out.println("\tstartTime: " + paramCredentials.startTime.getTime());
    System.out.println("\tendTime: " + paramCredentials.endTime.getTime());
    System.out.println("        ----Credentials end----");
  }

  static void ensureLoaded()
  {
    AccessController.doPrivileged(new PrivilegedAction()
    {
      public Object run()
      {
        System.loadLibrary("w2k_lsa_auth");
        return null;
      }
    });
    alreadyLoaded = true;
  }

  public String toString()
  {
    StringBuffer localStringBuffer = new StringBuffer("Credentials:");
    localStringBuffer.append("\nclient=").append(this.client);
    localStringBuffer.append("\nserver=").append(this.server);
    if (this.authTime != null)
      localStringBuffer.append("\nauthTime=").append(this.authTime);
    if (this.startTime != null)
      localStringBuffer.append("\nstartTime=").append(this.startTime);
    localStringBuffer.append("\nendTime=").append(this.endTime);
    localStringBuffer.append("\nrenewTill=").append(this.renewTill);
    localStringBuffer.append("\nflags: ").append(this.flags);
    localStringBuffer.append("\nEType (int): ").append(this.key.getEType());
    return localStringBuffer.toString();
  }
}