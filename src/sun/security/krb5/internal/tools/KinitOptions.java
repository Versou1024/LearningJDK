package sun.security.krb5.internal.tools;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import sun.security.krb5.Config;
import sun.security.krb5.KrbException;
import sun.security.krb5.PrincipalName;
import sun.security.krb5.RealmException;
import sun.security.krb5.internal.KerberosTime;
import sun.security.krb5.internal.Krb5;
import sun.security.krb5.internal.ccache.CCacheInputStream;
import sun.security.krb5.internal.ccache.FileCredentialsCache;

class KinitOptions
{
  public boolean validate = false;
  public short forwardable = -1;
  public short proxiable = -1;
  public boolean renew = false;
  public KerberosTime lifetime;
  public KerberosTime renewable_lifetime;
  public String target_service;
  public String keytab_file;
  public String cachename;
  private PrincipalName principal;
  public String realm;
  char[] password = null;
  public boolean keytab;
  private boolean DEBUG = Krb5.DEBUG;
  private boolean includeAddresses = true;
  private boolean useKeytab = false;
  private String ktabName;

  public KinitOptions()
    throws RuntimeException, RealmException
  {
    this.cachename = FileCredentialsCache.getDefaultCacheName();
    if (this.cachename == null)
      throw new RuntimeException("default cache name error");
    this.principal = getDefaultPrincipal();
  }

  public void setKDCRealm(String paramString)
    throws RealmException
  {
    this.realm = paramString;
  }

  public String getKDCRealm()
  {
    if ((this.realm == null) && (this.principal != null))
      return this.principal.getRealmString();
    return null;
  }

  public KinitOptions(String[] paramArrayOfString)
    throws KrbException, RuntimeException, IOException
  {
    String str1 = null;
    for (int i = 0; i < paramArrayOfString.length; ++i)
      if (paramArrayOfString[i].equals("-f"))
      {
        this.forwardable = 1;
      }
      else if (paramArrayOfString[i].equals("-p"))
      {
        this.proxiable = 1;
      }
      else if (paramArrayOfString[i].equals("-c"))
      {
        if (paramArrayOfString[(i + 1)].startsWith("-"))
          throw new IllegalArgumentException("input format  not correct:  -c  option must be followed by the cache name");
        this.cachename = paramArrayOfString[(++i)];
        if ((this.cachename.length() >= 5) && (this.cachename.substring(0, 5).equalsIgnoreCase("FILE:")))
          this.cachename = this.cachename.substring(5);
      }
      else if (paramArrayOfString[i].equals("-A"))
      {
        this.includeAddresses = false;
      }
      else if (paramArrayOfString[i].equals("-k"))
      {
        this.useKeytab = true;
      }
      else if (paramArrayOfString[i].equals("-t"))
      {
        if (this.ktabName != null)
          throw new IllegalArgumentException("-t option/keytab file name repeated");
        if (i + 1 < paramArrayOfString.length)
          this.ktabName = paramArrayOfString[(++i)];
        else
          throw new IllegalArgumentException("-t option requires keytab file name");
        this.useKeytab = true;
      }
      else if (paramArrayOfString[i].equalsIgnoreCase("-help"))
      {
        printHelp();
        System.exit(0);
      }
      else if (str1 == null)
      {
        str1 = paramArrayOfString[i];
        try
        {
          this.principal = new PrincipalName(str1);
        }
        catch (Exception localException)
        {
          throw new IllegalArgumentException("invalid Principal name: " + str1 + localException.getMessage());
        }
        if (this.principal.getRealm() == null)
        {
          String str2 = Config.getInstance().getDefault("default_realm", "libdefaults");
          if (str2 != null)
            this.principal.setRealm(str2);
          else
            throw new IllegalArgumentException("invalid Realm name");
        }
      }
      else if (this.password == null)
      {
        this.password = paramArrayOfString[i].toCharArray();
      }
      else
      {
        throw new IllegalArgumentException("too many parameters");
      }
    if (this.cachename == null)
    {
      this.cachename = FileCredentialsCache.getDefaultCacheName();
      if (this.cachename == null)
        throw new RuntimeException("default cache name error");
    }
    if (this.principal == null)
      this.principal = getDefaultPrincipal();
  }

  PrincipalName getDefaultPrincipal()
  {
    String str1 = null;
    try
    {
      str1 = Config.getInstance().getDefaultRealm();
    }
    catch (KrbException localKrbException)
    {
      System.out.println("Can not get default realm " + localKrbException.getMessage());
      localKrbException.printStackTrace();
      return null;
    }
    try
    {
      int i;
      CCacheInputStream localCCacheInputStream = new CCacheInputStream(new FileInputStream(this.cachename));
      if ((i = localCCacheInputStream.readVersion()) == 1284)
        localCCacheInputStream.readTag();
      else if ((i == 1281) || (i == 1282))
        localCCacheInputStream.setNativeByteOrder();
      PrincipalName localPrincipalName2 = localCCacheInputStream.readPrincipal(i);
      localCCacheInputStream.close();
      String str3 = localPrincipalName2.getRealmString();
      if (str3 == null)
        localPrincipalName2.setRealm(str1);
      if (this.DEBUG)
        System.out.println(">>>KinitOptions principal name from the cache is :" + localPrincipalName2);
      return localPrincipalName2;
    }
    catch (IOException localIOException)
    {
      if (this.DEBUG)
        localIOException.printStackTrace();
    }
    catch (RealmException localRealmException1)
    {
      if (this.DEBUG)
        localRealmException1.printStackTrace();
    }
    String str2 = System.getProperty("user.name");
    if (this.DEBUG)
      System.out.println(">>>KinitOptions default username is :" + str2);
    if (str1 != null)
      try
      {
        PrincipalName localPrincipalName1 = new PrincipalName(str2);
        if (localPrincipalName1.getRealm() == null)
          localPrincipalName1.setRealm(str1);
        return localPrincipalName1;
      }
      catch (RealmException localRealmException2)
      {
        if (this.DEBUG)
        {
          System.out.println("Exception in getting principal name " + localRealmException2.getMessage());
          localRealmException2.printStackTrace();
        }
      }
    return null;
  }

  void printHelp()
  {
    System.out.println("Usage: kinit [-A] [-f] [-p] [-c cachename] [[-k [-t keytab_file_name]] [principal] [password]");
    System.out.println("\tavailable options to Kerberos 5 ticket request:");
    System.out.println("\t    -A   do not include addresses");
    System.out.println("\t    -f   forwardable");
    System.out.println("\t    -p   proxiable");
    System.out.println("\t    -c   cache name (i.e., FILE:\\d:\\myProfiles\\mykrb5cache)");
    System.out.println("\t    -k   use keytab");
    System.out.println("\t    -t   keytab file name");
    System.out.println("\t    principal   the principal name (i.e., qweadf@ATHENA.MIT.EDU qweadf)");
    System.out.println("\t    password   the principal's Kerberos password");
  }

  public boolean getAddressOption()
  {
    return this.includeAddresses;
  }

  public boolean useKeytabFile()
  {
    return this.useKeytab;
  }

  public String keytabFileName()
  {
    return this.ktabName;
  }

  public PrincipalName getPrincipal()
  {
    return this.principal;
  }
}