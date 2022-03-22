package sun.security.krb5.internal.ccache;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.StringTokenizer;
import java.util.Vector;
import sun.security.action.GetPropertyAction;
import sun.security.action.LoadLibraryAction;
import sun.security.krb5.Asn1Exception;
import sun.security.krb5.KrbException;
import sun.security.krb5.PrincipalName;
import sun.security.krb5.Realm;
import sun.security.krb5.internal.KerberosTime;
import sun.security.krb5.internal.Krb5;
import sun.security.krb5.internal.LoginOptions;
import sun.security.krb5.internal.TicketFlags;

public class FileCredentialsCache extends CredentialsCache
  implements FileCCacheConstants
{
  public int version;
  public Tag tag;
  public PrincipalName primaryPrincipal;
  public Realm primaryRealm;
  private Vector credentialsList;
  private static String dir;
  private static boolean DEBUG = Krb5.DEBUG;
  private static final int USER_READ_WRITE = 384;
  private static boolean loadedLibrary = false;
  private static boolean loadRequired = true;

  private static native int chmod(String paramString, int paramInt);

  private static synchronized boolean needsChmod()
  {
    if (!(loadRequired))
      return false;
    if (loadedLibrary)
      return true;
    String str = (String)AccessController.doPrivileged(new GetPropertyAction("os.name"));
    loadRequired = str.startsWith("Linux");
    if (loadRequired)
    {
      AccessController.doPrivileged(new LoadLibraryAction("native_chmod"));
      loadedLibrary = true;
    }
    return loadedLibrary;
  }

  public static synchronized FileCredentialsCache acquireInstance(PrincipalName paramPrincipalName, String paramString)
  {
    FileCredentialsCache localFileCredentialsCache;
    try
    {
      localFileCredentialsCache = new FileCredentialsCache();
      if (paramString == null)
        cacheName = getDefaultCacheName();
      else
        cacheName = checkValidation(paramString);
      if ((cacheName == null) || (!(new File(cacheName).exists())))
        return null;
      if (paramPrincipalName != null)
      {
        localFileCredentialsCache.primaryPrincipal = paramPrincipalName;
        localFileCredentialsCache.primaryRealm = paramPrincipalName.getRealm();
      }
      localFileCredentialsCache.load(cacheName);
      return localFileCredentialsCache;
    }
    catch (IOException localIOException)
    {
      if (DEBUG)
        localIOException.printStackTrace();
    }
    catch (KrbException localKrbException)
    {
      if (DEBUG)
        localKrbException.printStackTrace();
    }
    return null;
  }

  public static FileCredentialsCache acquireInstance()
  {
    return acquireInstance(null, null);
  }

  static synchronized FileCredentialsCache New(PrincipalName paramPrincipalName, String paramString)
  {
    FileCredentialsCache localFileCredentialsCache;
    try
    {
      localFileCredentialsCache = new FileCredentialsCache();
      cacheName = checkValidation(paramString);
      if (cacheName == null)
        return null;
      localFileCredentialsCache.init(paramPrincipalName, cacheName);
      if (needsChmod())
      {
        int i = chmod(cacheName, 384);
        if (i != 0)
        {
          if (DEBUG)
            System.out.println("FileCredentialsCache: chmod failed");
          return null;
        }
      }
      return localFileCredentialsCache;
    }
    catch (IOException localIOException)
    {
    }
    catch (KrbException localKrbException)
    {
    }
    return null;
  }

  static synchronized FileCredentialsCache New(PrincipalName paramPrincipalName)
  {
    FileCredentialsCache localFileCredentialsCache;
    try
    {
      localFileCredentialsCache = new FileCredentialsCache();
      cacheName = getDefaultCacheName();
      localFileCredentialsCache.init(paramPrincipalName, cacheName);
      if (needsChmod())
      {
        int i = chmod(cacheName, 384);
        if (i != 0)
        {
          if (DEBUG)
            System.out.println("FileCredentialsCache: chmod failed");
          return null;
        }
      }
      return localFileCredentialsCache;
    }
    catch (IOException localIOException)
    {
      if (DEBUG)
        localIOException.printStackTrace();
    }
    catch (KrbException localKrbException)
    {
      if (DEBUG)
        localKrbException.printStackTrace();
    }
    return null;
  }

  boolean exists(String paramString)
  {
    File localFile = new File(paramString);
    return (localFile.exists());
  }

  synchronized void init(PrincipalName paramPrincipalName, String paramString)
    throws IOException, KrbException
  {
    this.primaryPrincipal = paramPrincipalName;
    this.primaryRealm = paramPrincipalName.getRealm();
    CCacheOutputStream localCCacheOutputStream = new CCacheOutputStream(new FileOutputStream(paramString));
    this.version = 1283;
    localCCacheOutputStream.writeHeader(this.primaryPrincipal, this.version);
    localCCacheOutputStream.close();
    load(paramString);
  }

  synchronized void load(String paramString)
    throws IOException, KrbException
  {
    CCacheInputStream localCCacheInputStream = new CCacheInputStream(new FileInputStream(paramString));
    this.version = localCCacheInputStream.readVersion();
    if (this.version == 1284)
    {
      this.tag = localCCacheInputStream.readTag();
    }
    else
    {
      this.tag = null;
      if ((this.version == 1281) || (this.version == 1282))
        localCCacheInputStream.setNativeByteOrder();
    }
    PrincipalName localPrincipalName = localCCacheInputStream.readPrincipal(this.version);
    if (this.primaryPrincipal != null)
    {
      if (this.primaryPrincipal.match(localPrincipalName))
        break label116;
      throw new IOException("Primary principals don't match.");
    }
    this.primaryPrincipal = localPrincipalName;
    label116: this.primaryRealm = this.primaryPrincipal.getRealm();
    this.credentialsList = new Vector();
    while (localCCacheInputStream.available() > 0)
      this.credentialsList.addElement(localCCacheInputStream.readCred(this.version));
    localCCacheInputStream.close();
  }

  public synchronized void update(Credentials paramCredentials)
  {
    if (this.credentialsList != null)
      if (this.credentialsList.isEmpty())
      {
        this.credentialsList.addElement(paramCredentials);
      }
      else
      {
        Credentials localCredentials = null;
        int i = 0;
        for (int j = 0; j < this.credentialsList.size(); ++j)
        {
          localCredentials = (Credentials)(Credentials)this.credentialsList.elementAt(j);
          if ((match(paramCredentials.sname.getNameStrings(), localCredentials.sname.getNameStrings())) && (paramCredentials.sname.getRealmString().equalsIgnoreCase(localCredentials.sname.getRealmString())))
          {
            i = 1;
            if (paramCredentials.endtime.getTime() >= localCredentials.endtime.getTime())
            {
              if (DEBUG)
                System.out.println(" >>> FileCredentialsCache Ticket matched, overwrite the old one.");
              this.credentialsList.removeElementAt(j);
              this.credentialsList.addElement(paramCredentials);
            }
          }
        }
        if (i == 0)
        {
          if (DEBUG)
            System.out.println(" >>> FileCredentialsCache Ticket not exactly matched, add new one into cache.");
          this.credentialsList.addElement(paramCredentials);
        }
      }
  }

  public synchronized PrincipalName getPrimaryPrincipal()
  {
    return this.primaryPrincipal;
  }

  public synchronized void save()
    throws IOException, Asn1Exception
  {
    CCacheOutputStream localCCacheOutputStream = new CCacheOutputStream(new FileOutputStream(cacheName));
    localCCacheOutputStream.writeHeader(this.primaryPrincipal, this.version);
    Credentials[] arrayOfCredentials = null;
    if ((arrayOfCredentials = getCredsList()) != null)
      for (int i = 0; i < arrayOfCredentials.length; ++i)
        localCCacheOutputStream.addCreds(arrayOfCredentials[i]);
    localCCacheOutputStream.close();
  }

  boolean match(String[] paramArrayOfString1, String[] paramArrayOfString2)
  {
    if (paramArrayOfString1.length != paramArrayOfString2.length)
      return false;
    for (int i = 0; i < paramArrayOfString1.length; ++i)
      if (!(paramArrayOfString1[i].equalsIgnoreCase(paramArrayOfString2[i])))
        return false;
    return true;
  }

  public synchronized Credentials[] getCredsList()
  {
    if ((this.credentialsList == null) || (this.credentialsList.isEmpty()))
      return null;
    Credentials[] arrayOfCredentials = new Credentials[this.credentialsList.size()];
    for (int i = 0; i < this.credentialsList.size(); ++i)
      arrayOfCredentials[i] = ((Credentials)(Credentials)this.credentialsList.elementAt(i));
    return arrayOfCredentials;
  }

  public Credentials getCreds(LoginOptions paramLoginOptions, PrincipalName paramPrincipalName, Realm paramRealm)
  {
    if (paramLoginOptions == null)
      return getCreds(paramPrincipalName, paramRealm);
    Credentials[] arrayOfCredentials = getCredsList();
    if (arrayOfCredentials == null)
      return null;
    for (int i = 0; i < arrayOfCredentials.length; ++i)
      if ((paramPrincipalName.match(arrayOfCredentials[i].sname)) && (paramRealm.toString().equals(arrayOfCredentials[i].srealm.toString())) && (arrayOfCredentials[i].flags.match(paramLoginOptions)))
        return arrayOfCredentials[i];
    return null;
  }

  public Credentials getCreds(PrincipalName paramPrincipalName, Realm paramRealm)
  {
    Credentials[] arrayOfCredentials = getCredsList();
    if (arrayOfCredentials == null)
      return null;
    for (int i = 0; i < arrayOfCredentials.length; ++i)
      if ((paramPrincipalName.match(arrayOfCredentials[i].sname)) && (paramRealm.toString().equals(arrayOfCredentials[i].srealm.toString())))
        return arrayOfCredentials[i];
    return null;
  }

  public Credentials getDefaultCreds()
  {
    Credentials[] arrayOfCredentials = getCredsList();
    if (arrayOfCredentials == null)
      return null;
    for (int i = arrayOfCredentials.length - 1; i >= 0; --i)
      if (arrayOfCredentials[i].sname.toString().startsWith("krbtgt"))
      {
        String[] arrayOfString = arrayOfCredentials[i].sname.getNameStrings();
        if (arrayOfString[1].equals(arrayOfCredentials[i].srealm.toString()))
          return arrayOfCredentials[i];
      }
    return null;
  }

  public static String getDefaultCacheName()
  {
    String str2;
    String str4;
    String str5;
    long l;
    String str1 = "krb5cc";
    String str3 = (String)AccessController.doPrivileged(new GetPropertyAction("os.name"));
    if (str3 != null)
    {
      str4 = null;
      str5 = null;
      l = 3412047394332737536L;
      if ((str3.startsWith("SunOS")) || (str3.startsWith("Linux")));
    }
    try
    {
      Class localClass = Class.forName("com.sun.security.auth.module.UnixSystem");
      Constructor localConstructor = localClass.getConstructor(new Class[0]);
      Object localObject = localConstructor.newInstance(new Object[0]);
      Method localMethod = localClass.getMethod("getUid", null);
      l = ((Long)localMethod.invoke(localObject, null)).longValue();
      str2 = File.separator + "tmp" + File.separator + str1 + "_" + l;
      if (DEBUG)
        System.out.println(">>>KinitOptions cache name is " + str2);
      return str2;
    }
    catch (Exception localException)
    {
      if (DEBUG)
      {
        System.out.println("Exception in obtaining uid for Unix platforms Using user's home directory");
        localException.printStackTrace();
      }
      str4 = (String)AccessController.doPrivileged(new GetPropertyAction("user.name"));
      str5 = (String)AccessController.doPrivileged(new GetPropertyAction("user.home"));
      if (str5 == null)
        str5 = (String)AccessController.doPrivileged(new GetPropertyAction("user.dir"));
      if (str4 != null)
        str2 = str5 + File.separator + str1 + "_" + str4;
      else
        str2 = str5 + File.separator + str1;
      if (DEBUG)
        System.out.println(">>>KinitOptions cache name is " + str2);
    }
    return str2;
  }

  public static String checkValidation(String paramString)
  {
    String str = null;
    if (paramString == null)
      return null;
    try
    {
      str = new File(paramString).getCanonicalPath();
      File localFile1 = new File(str);
      if (!(localFile1.exists()))
      {
        File localFile2 = new File(localFile1.getParent());
        if (!(localFile2.isDirectory()))
          str = null;
        localFile2 = null;
      }
      localFile1 = null;
    }
    catch (IOException localIOException)
    {
      str = null;
    }
    return str;
  }

  private static String exec(String paramString)
  {
    StringTokenizer localStringTokenizer = new StringTokenizer(paramString);
    Vector localVector = new Vector();
    while (localStringTokenizer.hasMoreTokens())
      localVector.addElement(localStringTokenizer.nextToken());
    String[] arrayOfString = new String[localVector.size()];
    localVector.copyInto(arrayOfString);
    try
    {
      Process localProcess = (Process)AccessController.doPrivileged(new PrivilegedAction(arrayOfString)
      {
        public Object run()
        {
          try
          {
            return Runtime.getRuntime().exec(this.val$command);
          }
          catch (IOException localIOException)
          {
            if (FileCredentialsCache.access$000())
              localIOException.printStackTrace();
          }
          return null;
        }
      });
      if (localProcess == null)
        return null;
      BufferedReader localBufferedReader = new BufferedReader(new InputStreamReader(localProcess.getInputStream(), "8859_1"));
      String str = null;
      if ((arrayOfString.length == 1) && (arrayOfString[0].equals("/usr/bin/env")))
      {
        do
          if ((str = localBufferedReader.readLine()) == null)
            break label170;
        while ((str.length() < 11) || (!(str.substring(0, 11).equalsIgnoreCase("KRB5CCNAME="))));
        str = str.substring(11);
      }
      else
      {
        str = localBufferedReader.readLine();
      }
      label170: localBufferedReader.close();
      return str;
    }
    catch (Exception localException)
    {
      if (DEBUG)
        localException.printStackTrace();
    }
    return null;
  }
}