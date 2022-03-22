package sun.security.krb5.internal.tools;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import sun.security.krb5.Config;
import sun.security.krb5.EncryptionKey;
import sun.security.krb5.KrbAsRep;
import sun.security.krb5.KrbAsReq;
import sun.security.krb5.KrbException;
import sun.security.krb5.PrincipalName;
import sun.security.krb5.RealmException;
import sun.security.krb5.internal.HostAddresses;
import sun.security.krb5.internal.KDCOptions;
import sun.security.krb5.internal.KRBError;
import sun.security.krb5.internal.Krb5;
import sun.security.krb5.internal.ccache.Credentials;
import sun.security.krb5.internal.ccache.CredentialsCache;
import sun.security.util.Password;

public class Kinit
{
  private KinitOptions options;
  private static final boolean DEBUG = Krb5.DEBUG;

  public static void main(String[] paramArrayOfString)
  {
    Kinit localKinit;
    try
    {
      localKinit = new Kinit(paramArrayOfString);
    }
    catch (Exception localException)
    {
      String str = null;
      if (localException instanceof KrbException)
        str = ((KrbException)localException).krbErrorMessage() + " " + ((KrbException)localException).returnCodeMessage();
      else
        str = localException.getMessage();
      if (str != null)
        System.err.println("Exception: " + str);
      else
        System.out.println("Exception: " + localException);
      localException.printStackTrace();
      System.exit(-1);
    }
  }

  private Kinit(String[] paramArrayOfString)
    throws IOException, RealmException, KrbException
  {
    if ((paramArrayOfString == null) || (paramArrayOfString.length == 0))
      this.options = new KinitOptions();
    else
      this.options = new KinitOptions(paramArrayOfString);
    String str1 = null;
    PrincipalName localPrincipalName1 = this.options.getPrincipal();
    if (localPrincipalName1 != null)
      str1 = localPrincipalName1.toString();
    if (DEBUG)
      System.out.println("Principal is " + localPrincipalName1);
    char[] arrayOfChar = this.options.password;
    EncryptionKey[] arrayOfEncryptionKey = null;
    boolean bool = this.options.useKeytabFile();
    if (!(bool))
    {
      if (str1 == null)
        throw new IllegalArgumentException(" Can not obtain principal name");
      if (arrayOfChar == null)
      {
        System.out.print("Password for " + str1 + ":");
        System.out.flush();
        arrayOfChar = Password.readPassword(System.in);
        if (DEBUG)
          System.out.println(">>> Kinit console input " + new String(arrayOfChar));
      }
    }
    else
    {
      if (DEBUG)
        System.out.println(">>> Kinit using keytab");
      if (str1 == null)
        throw new IllegalArgumentException("Principal name must be specified.");
      localObject1 = this.options.keytabFileName();
      if ((localObject1 != null) && (DEBUG))
        System.out.println(">>> Kinit keytab file name: " + ((String)localObject1));
      arrayOfEncryptionKey = EncryptionKey.acquireSecretKeys(localPrincipalName1, (String)localObject1);
      if ((arrayOfEncryptionKey == null) || (arrayOfEncryptionKey.length == 0))
      {
        str2 = "No supported key found in keytab";
        if (str1 != null)
          str2 = str2 + " for principal " + str1;
        throw new KrbException(str2);
      }
    }
    Object localObject1 = new KDCOptions();
    setOptions(1, this.options.forwardable, (KDCOptions)localObject1);
    setOptions(3, this.options.proxiable, (KDCOptions)localObject1);
    String str2 = this.options.getKDCRealm();
    if (str2 == null)
      str2 = Config.getInstance().getDefaultRealm();
    if (DEBUG)
      System.out.println(">>> Kinit realm name is " + str2);
    PrincipalName localPrincipalName2 = new PrincipalName("krbtgt/" + str2);
    localPrincipalName2.setRealm(str2);
    if (DEBUG)
      System.out.println(">>> Creating KrbAsReq");
    KrbAsReq localKrbAsReq = null;
    HostAddresses localHostAddresses = null;
    try
    {
      if (this.options.getAddressOption())
        localHostAddresses = HostAddresses.getLocalAddresses();
      if (bool)
        localKrbAsReq = new KrbAsReq(arrayOfEncryptionKey, (KDCOptions)localObject1, localPrincipalName1, localPrincipalName2, null, null, null, null, localHostAddresses, null);
      else
        localKrbAsReq = new KrbAsReq(arrayOfChar, (KDCOptions)localObject1, localPrincipalName1, localPrincipalName2, null, null, null, null, localHostAddresses, null);
    }
    catch (KrbException localKrbException1)
    {
      throw localKrbException1;
    }
    catch (Exception localException)
    {
      throw new KrbException(localException.toString());
    }
    KrbAsRep localKrbAsRep = null;
    try
    {
      localKrbAsRep = sendASRequest(localKrbAsReq, bool, str2, arrayOfChar, arrayOfEncryptionKey);
    }
    catch (KrbException localKrbException2)
    {
      if ((localKrbException2.returnCode() != 24) && (localKrbException2.returnCode() != 25))
        break label765;
      if (DEBUG)
        System.out.println("Kinit: PREAUTH FAILED/REQ, re-send AS-REQ");
      localObject2 = localKrbException2.getError();
      int i = ((KRBError)localObject2).getEType();
      byte[] arrayOfByte1 = ((KRBError)localObject2).getSalt();
      byte[] arrayOfByte2 = ((KRBError)localObject2).getParams();
      if (bool)
        localKrbAsReq = new KrbAsReq(arrayOfEncryptionKey, true, i, arrayOfByte1, arrayOfByte2, (KDCOptions)localObject1, localPrincipalName1, localPrincipalName2, null, null, null, null, localHostAddresses, null);
      else
        localKrbAsReq = new KrbAsReq(arrayOfChar, true, i, arrayOfByte1, arrayOfByte2, (KDCOptions)localObject1, localPrincipalName1, localPrincipalName2, null, null, null, null, localHostAddresses, null);
      localKrbAsRep = sendASRequest(localKrbAsReq, bool, str2, arrayOfChar, arrayOfEncryptionKey);
    }
    break label768:
    label765: throw localKrbException2;
    label768: Credentials localCredentials = localKrbAsRep.setCredentials();
    Object localObject2 = CredentialsCache.create(localPrincipalName1, this.options.cachename);
    if (localObject2 == null)
      throw new IOException("Unable to create the cache file " + this.options.cachename);
    ((CredentialsCache)localObject2).update(localCredentials);
    ((CredentialsCache)localObject2).save();
    if (this.options.password == null)
      System.out.println("New ticket is stored in cache file " + this.options.cachename);
    else
      Arrays.fill(this.options.password, '0');
    if (arrayOfChar != null)
      Arrays.fill(arrayOfChar, '0');
    this.options = null;
  }

  private static KrbAsRep sendASRequest(KrbAsReq paramKrbAsReq, boolean paramBoolean, String paramString, char[] paramArrayOfChar, EncryptionKey[] paramArrayOfEncryptionKey)
    throws IOException, RealmException, KrbException
  {
    if (DEBUG)
      System.out.println(">>> Kinit: sending as_req to realm " + paramString);
    String str = paramKrbAsReq.send(paramString);
    if (DEBUG)
      System.out.println(">>> reading response from kdc");
    KrbAsRep localKrbAsRep = null;
    try
    {
      if (paramBoolean)
        localKrbAsRep = paramKrbAsReq.getReply(paramArrayOfEncryptionKey);
      else
        localKrbAsRep = paramKrbAsReq.getReply(paramArrayOfChar);
    }
    catch (KrbException localKrbException)
    {
      if (localKrbException.returnCode() == 52)
      {
        paramKrbAsReq.send(paramString, str, true);
        if (paramBoolean)
          localKrbAsRep = paramKrbAsReq.getReply(paramArrayOfEncryptionKey);
        else
          localKrbAsRep = paramKrbAsReq.getReply(paramArrayOfChar);
      }
      else
      {
        throw localKrbException;
      }
    }
    return localKrbAsRep;
  }

  private static void setOptions(int paramInt1, int paramInt2, KDCOptions paramKDCOptions)
  {
    switch (paramInt2)
    {
    case 0:
      break;
    case -1:
      paramKDCOptions.set(paramInt1, false);
      break;
    case 1:
      paramKDCOptions.set(paramInt1, true);
    }
  }
}