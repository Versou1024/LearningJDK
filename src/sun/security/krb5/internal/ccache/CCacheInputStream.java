package sun.security.krb5.internal.ccache;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Date;
import java.util.StringTokenizer;
import sun.security.krb5.Asn1Exception;
import sun.security.krb5.EncryptionKey;
import sun.security.krb5.PrincipalName;
import sun.security.krb5.Realm;
import sun.security.krb5.RealmException;
import sun.security.krb5.internal.AuthorizationData;
import sun.security.krb5.internal.AuthorizationDataEntry;
import sun.security.krb5.internal.HostAddress;
import sun.security.krb5.internal.HostAddresses;
import sun.security.krb5.internal.KerberosTime;
import sun.security.krb5.internal.Krb5;
import sun.security.krb5.internal.KrbApErrException;
import sun.security.krb5.internal.Ticket;
import sun.security.krb5.internal.TicketFlags;
import sun.security.krb5.internal.util.KrbDataInputStream;

public class CCacheInputStream extends KrbDataInputStream
  implements FileCCacheConstants
{
  private static boolean DEBUG = Krb5.DEBUG;

  public CCacheInputStream(InputStream paramInputStream)
  {
    super(paramInputStream);
  }

  public Tag readTag()
    throws IOException
  {
    char[] arrayOfChar = new char[1024];
    int j = -1;
    Integer localInteger1 = null;
    Integer localInteger2 = null;
    int i = read(2);
    if (i < 0)
      throw new IOException("stop.");
    byte[] arrayOfByte = new byte[i + 2];
    if (i > arrayOfChar.length)
      throw new IOException("Invalid tag length.");
    while (i > 0)
    {
      j = read(2);
      int k = read(2);
      switch (j)
      {
      case 1:
        localInteger1 = new Integer(read(4));
        localInteger2 = new Integer(read(4));
      }
      i -= 4 + k;
    }
    if (j == -1);
    Tag localTag = new Tag(i, j, localInteger1, localInteger2);
    return localTag;
  }

  public PrincipalName readPrincipal(int paramInt)
    throws IOException, RealmException
  {
    int i;
    PrincipalName localPrincipalName;
    String[] arrayOfString1 = null;
    if (paramInt == 1281)
      i = 0;
    else
      i = read(4);
    int j = read(4);
    String[] arrayOfString2 = new String[j + 1];
    if (paramInt == 1281)
      --j;
    for (int l = 0; l <= j; ++l)
    {
      int k = read(4);
      if (k > 1024)
        throw new IOException("Invalid name length in principal name.");
      byte[] arrayOfByte = new byte[k];
      read(arrayOfByte, 0, k);
      arrayOfString2[l] = new String(arrayOfByte);
    }
    if (isRealm(arrayOfString2[0]))
    {
      String str = arrayOfString2[0];
      arrayOfString1 = new String[j];
      System.arraycopy(arrayOfString2, 1, arrayOfString1, 0, j);
      localPrincipalName = new PrincipalName(arrayOfString1, i);
      localPrincipalName.setRealm(str);
    }
    else
    {
      localPrincipalName = new PrincipalName(arrayOfString2, i);
    }
    return localPrincipalName;
  }

  boolean isRealm(String paramString)
  {
    Realm localRealm;
    try
    {
      localRealm = new Realm(paramString);
    }
    catch (Exception localException)
    {
      return false;
    }
    StringTokenizer localStringTokenizer = new StringTokenizer(paramString, ".");
    while (localStringTokenizer.hasMoreTokens())
    {
      String str = localStringTokenizer.nextToken();
      for (int i = 0; i < str.length(); ++i)
        if (str.charAt(i) >= 141)
          return false;
    }
    return true;
  }

  EncryptionKey readKey(int paramInt)
    throws IOException
  {
    int i = read(2);
    if (paramInt == 1283)
      read(2);
    int j = read(4);
    byte[] arrayOfByte = new byte[j];
    for (int k = 0; k < j; ++k)
      arrayOfByte[k] = (byte)read();
    return new EncryptionKey(arrayOfByte, i, new Integer(paramInt));
  }

  long[] readTimes()
    throws IOException
  {
    long[] arrayOfLong = new long[4];
    arrayOfLong[0] = (read(4) * 1000L);
    arrayOfLong[1] = (read(4) * 1000L);
    arrayOfLong[2] = (read(4) * 1000L);
    arrayOfLong[3] = (read(4) * 1000L);
    return arrayOfLong;
  }

  boolean readskey()
    throws IOException
  {
    return (read() != 0);
  }

  HostAddress[] readAddr()
    throws IOException, KrbApErrException
  {
    int i = read(4);
    if (i > 0)
    {
      HostAddress[] arrayOfHostAddress = new HostAddress[i];
      for (int l = 0; l < i; ++l)
      {
        int j = read(2);
        int k = read(4);
        if ((k != 4) && (k != 16))
        {
          System.out.println("Incorrect address format.");
          return null;
        }
        byte[] arrayOfByte = new byte[k];
        for (int i1 = 0; i1 < k; ++i1)
          arrayOfByte[i1] = (byte)read(1);
        arrayOfHostAddress[l] = new HostAddress(j, arrayOfByte);
      }
      return arrayOfHostAddress;
    }
    return null;
  }

  AuthorizationDataEntry[] readAuth()
    throws IOException
  {
    int i = read(4);
    if (i > 0)
    {
      AuthorizationDataEntry[] arrayOfAuthorizationDataEntry = new AuthorizationDataEntry[i];
      byte[] arrayOfByte = null;
      for (int l = 0; l < i; ++l)
      {
        int j = read(2);
        int k = read(4);
        arrayOfByte = new byte[k];
        for (int i1 = 0; i1 < k; ++i1)
          arrayOfByte[i1] = (byte)read();
        arrayOfAuthorizationDataEntry[l] = new AuthorizationDataEntry(j, arrayOfByte);
      }
      return arrayOfAuthorizationDataEntry;
    }
    return null;
  }

  Ticket readData()
    throws IOException, RealmException, KrbApErrException, Asn1Exception
  {
    int i = read(4);
    if (i > 0)
    {
      byte[] arrayOfByte = new byte[i];
      read(arrayOfByte, 0, i);
      Ticket localTicket = new Ticket(arrayOfByte);
      return localTicket;
    }
    return null;
  }

  boolean[] readFlags()
    throws IOException
  {
    boolean[] arrayOfBoolean = new boolean[12];
    int i = read(4);
    if ((i & 0x40000000) == 1073741824)
      arrayOfBoolean[1] = true;
    if ((i & 0x20000000) == 536870912)
      arrayOfBoolean[2] = true;
    if ((i & 0x10000000) == 268435456)
      arrayOfBoolean[3] = true;
    if ((i & 0x8000000) == 134217728)
      arrayOfBoolean[4] = true;
    if ((i & 0x4000000) == 67108864)
      arrayOfBoolean[5] = true;
    if ((i & 0x2000000) == 33554432)
      arrayOfBoolean[6] = true;
    if ((i & 0x1000000) == 16777216)
      arrayOfBoolean[7] = true;
    if ((i & 0x800000) == 8388608)
      arrayOfBoolean[8] = true;
    if ((i & 0x400000) == 4194304)
      arrayOfBoolean[9] = true;
    if ((i & 0x200000) == 2097152)
      arrayOfBoolean[10] = true;
    if ((i & 0x100000) == 1048576)
      arrayOfBoolean[11] = true;
    if (DEBUG)
    {
      String str = ">>> CCacheInputStream: readFlags() ";
      if (arrayOfBoolean[1] == 1)
        str = str + " FORWARDABLE;";
      if (arrayOfBoolean[2] == 1)
        str = str + " FORWARDED;";
      if (arrayOfBoolean[3] == 1)
        str = str + " PROXIABLE;";
      if (arrayOfBoolean[4] == 1)
        str = str + " PROXY;";
      if (arrayOfBoolean[5] == 1)
        str = str + " MAY_POSTDATE;";
      if (arrayOfBoolean[6] == 1)
        str = str + " POSTDATED;";
      if (arrayOfBoolean[7] == 1)
        str = str + " INVALID;";
      if (arrayOfBoolean[8] == 1)
        str = str + " RENEWABLE;";
      if (arrayOfBoolean[9] == 1)
        str = str + " INITIAL;";
      if (arrayOfBoolean[10] == 1)
        str = str + " PRE_AUTH;";
      if (arrayOfBoolean[11] == 1)
        str = str + " HW_AUTH;";
      System.out.println(str);
    }
    return arrayOfBoolean;
  }

  Credentials readCred(int paramInt)
    throws IOException, RealmException, KrbApErrException, Asn1Exception
  {
    PrincipalName localPrincipalName1 = readPrincipal(paramInt);
    if (DEBUG)
      System.out.println(">>>DEBUG <CCacheInputStream>  client principal is " + localPrincipalName1.toString());
    PrincipalName localPrincipalName2 = readPrincipal(paramInt);
    if (DEBUG)
      System.out.println(">>>DEBUG <CCacheInputStream> server principal is " + localPrincipalName2.toString());
    EncryptionKey localEncryptionKey = readKey(paramInt);
    if (DEBUG)
      System.out.println(">>>DEBUG <CCacheInputStream> key type: " + localEncryptionKey.getEType());
    long[] arrayOfLong = readTimes();
    KerberosTime localKerberosTime1 = new KerberosTime(arrayOfLong[0]);
    KerberosTime localKerberosTime2 = new KerberosTime(arrayOfLong[1]);
    KerberosTime localKerberosTime3 = new KerberosTime(arrayOfLong[2]);
    KerberosTime localKerberosTime4 = new KerberosTime(arrayOfLong[3]);
    if (DEBUG)
    {
      System.out.println(">>>DEBUG <CCacheInputStream> auth time: " + localKerberosTime1.toDate().toString());
      System.out.println(">>>DEBUG <CCacheInputStream> start time: " + localKerberosTime2.toDate().toString());
      System.out.println(">>>DEBUG <CCacheInputStream> end time: " + localKerberosTime3.toDate().toString());
      System.out.println(">>>DEBUG <CCacheInputStream> renew_till time: " + localKerberosTime4.toDate().toString());
    }
    boolean bool = readskey();
    boolean[] arrayOfBoolean = readFlags();
    TicketFlags localTicketFlags = new TicketFlags(arrayOfBoolean);
    HostAddress[] arrayOfHostAddress = readAddr();
    HostAddresses localHostAddresses = null;
    if (arrayOfHostAddress != null)
      localHostAddresses = new HostAddresses(arrayOfHostAddress);
    AuthorizationDataEntry[] arrayOfAuthorizationDataEntry = readAuth();
    AuthorizationData localAuthorizationData = null;
    if (localAuthorizationData != null)
      localAuthorizationData = new AuthorizationData(arrayOfAuthorizationDataEntry);
    Ticket localTicket1 = readData();
    if (DEBUG)
    {
      System.out.println(">>>DEBUG <CCacheInputStream>");
      if (localTicket1 == null)
        System.out.println("///ticket is null");
    }
    Ticket localTicket2 = readData();
    Credentials localCredentials = new Credentials(localPrincipalName1, localPrincipalName2, localEncryptionKey, localKerberosTime1, localKerberosTime2, localKerberosTime3, localKerberosTime4, bool, localTicketFlags, localHostAddresses, localAuthorizationData, localTicket1, localTicket2);
    return localCredentials;
  }
}