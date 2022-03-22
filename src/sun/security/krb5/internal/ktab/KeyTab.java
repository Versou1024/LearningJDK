package sun.security.krb5.internal.ktab;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Vector;
import sun.security.krb5.EncryptionKey;
import sun.security.krb5.KrbException;
import sun.security.krb5.PrincipalName;
import sun.security.krb5.RealmException;
import sun.security.krb5.internal.KerberosTime;
import sun.security.krb5.internal.Krb5;
import sun.security.krb5.internal.crypto.EType;

public class KeyTab
  implements KeyTabConstants
{
  int kt_vno;
  private static KeyTab singleton = null;
  private static final boolean DEBUG = Krb5.DEBUG;
  private static String name;
  private Vector entries = new Vector();

  private KeyTab(String paramString)
    throws IOException, RealmException
  {
    init(paramString);
  }

  public static KeyTab getInstance(String paramString)
  {
    name = parse(paramString);
    if (name == null)
      return getInstance();
    return getInstance(new File(name));
  }

  public static KeyTab getInstance(File paramFile)
  {
    try
    {
      if (!(paramFile.exists()))
      {
        singleton = null;
      }
      else
      {
        String str1 = paramFile.getAbsolutePath();
        if (singleton != null)
        {
          File localFile = new File(name);
          String str2 = localFile.getAbsolutePath();
          if ((str2.equalsIgnoreCase(str1)) && (DEBUG))
            System.out.println("KeyTab instance already exists");
        }
        else
        {
          singleton = new KeyTab(str1);
        }
      }
    }
    catch (Exception localException)
    {
      singleton = null;
      if (DEBUG)
        System.out.println("Could not obtain an instance of KeyTab" + localException.getMessage());
    }
    return singleton;
  }

  public static KeyTab getInstance()
  {
    try
    {
      name = getDefaultKeyTab();
      if (name != null)
        singleton = getInstance(new File(name));
    }
    catch (Exception localException)
    {
      singleton = null;
      if (DEBUG)
        System.out.println("Could not obtain an instance of KeyTab" + localException.getMessage());
    }
    return singleton;
  }

  // ERROR //
  private static String getDefaultKeyTab()
  {
    // Byte code:
    //   0: getstatic 315	sun/security/krb5/internal/ktab/KeyTab:name	Ljava/lang/String;
    //   3: ifnull +7 -> 10
    //   6: getstatic 315	sun/security/krb5/internal/ktab/KeyTab:name	Ljava/lang/String;
    //   9: areturn
    //   10: aconst_null
    //   11: astore_0
    //   12: invokestatic 359	sun/security/krb5/Config:getInstance	()Lsun/security/krb5/Config;
    //   15: ldc 17
    //   17: ldc 20
    //   19: invokevirtual 360	sun/security/krb5/Config:getDefault	(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;
    //   22: astore_1
    //   23: aload_1
    //   24: ifnull +36 -> 60
    //   27: new 186	java/util/StringTokenizer
    //   30: dup
    //   31: aload_1
    //   32: ldc 1
    //   34: invokespecial 351	java/util/StringTokenizer:<init>	(Ljava/lang/String;Ljava/lang/String;)V
    //   37: astore_2
    //   38: aload_2
    //   39: invokevirtual 349	java/util/StringTokenizer:hasMoreTokens	()Z
    //   42: ifeq +18 -> 60
    //   45: aload_2
    //   46: invokevirtual 350	java/util/StringTokenizer:nextToken	()Ljava/lang/String;
    //   49: invokestatic 378	sun/security/krb5/internal/ktab/KeyTab:parse	(Ljava/lang/String;)Ljava/lang/String;
    //   52: astore_0
    //   53: aload_0
    //   54: ifnull -16 -> 38
    //   57: goto +3 -> 60
    //   60: goto +6 -> 66
    //   63: astore_1
    //   64: aconst_null
    //   65: astore_0
    //   66: aload_0
    //   67: ifnonnull +69 -> 136
    //   70: new 188	sun/security/action/GetPropertyAction
    //   73: dup
    //   74: ldc 22
    //   76: invokespecial 358	sun/security/action/GetPropertyAction:<init>	(Ljava/lang/String;)V
    //   79: invokestatic 343	java/security/AccessController:doPrivileged	(Ljava/security/PrivilegedAction;)Ljava/lang/Object;
    //   82: checkcast 181	java/lang/String
    //   85: astore_1
    //   86: aload_1
    //   87: ifnonnull +19 -> 106
    //   90: new 188	sun/security/action/GetPropertyAction
    //   93: dup
    //   94: ldc 21
    //   96: invokespecial 358	sun/security/action/GetPropertyAction:<init>	(Ljava/lang/String;)V
    //   99: invokestatic 343	java/security/AccessController:doPrivileged	(Ljava/security/PrivilegedAction;)Ljava/lang/Object;
    //   102: checkcast 181	java/lang/String
    //   105: astore_1
    //   106: aload_1
    //   107: ifnull +29 -> 136
    //   110: new 182	java/lang/StringBuilder
    //   113: dup
    //   114: invokespecial 337	java/lang/StringBuilder:<init>	()V
    //   117: aload_1
    //   118: invokevirtual 341	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   121: getstatic 310	java/io/File:separator	Ljava/lang/String;
    //   124: invokevirtual 341	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   127: ldc 19
    //   129: invokevirtual 341	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   132: invokevirtual 338	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   135: astore_0
    //   136: aload_0
    //   137: areturn
    //
    // Exception table:
    //   from	to	target	type
    //   12	60	63	sun/security/krb5/KrbException
  }

  private static String parse(String paramString)
  {
    String str = null;
    if (paramString == null)
      return null;
    if ((paramString.length() >= 5) && (paramString.substring(0, 5).equalsIgnoreCase("FILE:")))
      str = paramString.substring(5);
    else if ((paramString.length() >= 9) && (paramString.substring(0, 9).equalsIgnoreCase("ANY:FILE:")))
      str = paramString.substring(9);
    else if ((paramString.length() >= 7) && (paramString.substring(0, 7).equalsIgnoreCase("SRVTAB:")))
      str = paramString.substring(7);
    else
      str = paramString;
    return str;
  }

  private synchronized void init(String paramString)
    throws IOException, RealmException
  {
    if (paramString != null)
    {
      KeyTabInputStream localKeyTabInputStream = new KeyTabInputStream(new FileInputStream(paramString));
      load(localKeyTabInputStream);
      localKeyTabInputStream.close();
      name = paramString;
    }
  }

  private void load(KeyTabInputStream paramKeyTabInputStream)
    throws IOException, RealmException
  {
    this.entries.clear();
    this.kt_vno = paramKeyTabInputStream.readVersion();
    if (this.kt_vno == 1281)
      paramKeyTabInputStream.setNativeByteOrder();
    int i = 0;
    while (true)
    {
      KeyTabEntry localKeyTabEntry;
      do
      {
        if (paramKeyTabInputStream.available() <= 0)
          return;
        i = paramKeyTabInputStream.readEntryLength();
        localKeyTabEntry = paramKeyTabInputStream.readEntry(i, this.kt_vno);
        if (DEBUG)
          System.out.println(">>> KeyTab: load() entry length: " + i + "; type: " + ((localKeyTabEntry != null) ? localKeyTabEntry.keyType : 0));
      }
      while (localKeyTabEntry == null);
      this.entries.addElement(localKeyTabEntry);
    }
  }

  public EncryptionKey readServiceKey(PrincipalName paramPrincipalName)
  {
    KeyTabEntry localKeyTabEntry = null;
    if (this.entries != null)
      for (int i = this.entries.size() - 1; i >= 0; --i)
      {
        localKeyTabEntry = (KeyTabEntry)(KeyTabEntry)this.entries.elementAt(i);
        if (localKeyTabEntry.service.match(paramPrincipalName))
        {
          if (EType.isSupported(localKeyTabEntry.keyType))
            return new EncryptionKey(localKeyTabEntry.keyblock, localKeyTabEntry.keyType, new Integer(localKeyTabEntry.keyVersion));
          if (DEBUG)
            System.out.println("Found unsupported keytype (" + localKeyTabEntry.keyType + ") for " + paramPrincipalName);
        }
      }
    return null;
  }

  public EncryptionKey[] readServiceKeys(PrincipalName paramPrincipalName)
  {
    int l;
    int i = this.entries.size();
    ArrayList localArrayList = new ArrayList(i);
    if (this.entries != null)
      for (int j = i - 1; j >= 0; --j)
      {
        KeyTabEntry localKeyTabEntry = (KeyTabEntry)(KeyTabEntry)this.entries.elementAt(j);
        if (localKeyTabEntry.service.match(paramPrincipalName))
          if (EType.isSupported(localKeyTabEntry.keyType))
          {
            EncryptionKey localEncryptionKey1 = new EncryptionKey(localKeyTabEntry.keyblock, localKeyTabEntry.keyType, new Integer(localKeyTabEntry.keyVersion));
            localArrayList.add(localEncryptionKey1);
            if (DEBUG)
              System.out.println("Added key: " + localKeyTabEntry.keyType + "version: " + localKeyTabEntry.keyVersion);
          }
          else if (DEBUG)
          {
            System.out.println("Found unsupported keytype (" + localKeyTabEntry.keyType + ") for " + paramPrincipalName);
          }
      }
    i = localArrayList.size();
    if (i == 0)
      return null;
    EncryptionKey[] arrayOfEncryptionKey = new EncryptionKey[i];
    int k = 0;
    if (DEBUG)
      System.out.println("Ordering keys wrt default_tkt_enctypes list");
    int[] arrayOfInt = EType.getDefaults("default_tkt_enctypes");
    if ((arrayOfInt == null) || (arrayOfInt == EType.getBuiltInDefaults()))
    {
      for (l = 0; l < i; ++l)
        arrayOfEncryptionKey[(k++)] = ((EncryptionKey)localArrayList.get(l));
    }
    else
    {
      EncryptionKey localEncryptionKey2;
      for (l = 0; (l < arrayOfInt.length) && (k < i); ++l)
      {
        int i1 = arrayOfInt[l];
        for (int i2 = 0; (i2 < i) && (k < i); ++i2)
        {
          localEncryptionKey2 = (EncryptionKey)localArrayList.get(i2);
          if ((localEncryptionKey2 != null) && (localEncryptionKey2.getEType() == i1))
          {
            if (DEBUG)
              System.out.println(k + ": " + localEncryptionKey2);
            arrayOfEncryptionKey[(k++)] = localEncryptionKey2;
            localArrayList.set(i2, null);
          }
        }
      }
      for (l = 0; (l < i) && (k < i); ++l)
      {
        localEncryptionKey2 = (EncryptionKey)localArrayList.get(l);
        if (localEncryptionKey2 != null)
          arrayOfEncryptionKey[(k++)] = localEncryptionKey2;
      }
    }
    if (k != i)
      throw new RuntimeException("Internal Error: did not copy all keys;expecting " + i + "; got " + k);
    return arrayOfEncryptionKey;
  }

  public boolean findServiceEntry(PrincipalName paramPrincipalName)
  {
    if (this.entries != null)
      for (int i = 0; i < this.entries.size(); ++i)
      {
        KeyTabEntry localKeyTabEntry = (KeyTabEntry)(KeyTabEntry)this.entries.elementAt(i);
        if (localKeyTabEntry.service.match(paramPrincipalName))
        {
          if (EType.isSupported(localKeyTabEntry.keyType))
            return true;
          if (DEBUG)
            System.out.println("Found unsupported keytype (" + localKeyTabEntry.keyType + ") for " + paramPrincipalName);
        }
      }
    return false;
  }

  public static String tabName()
  {
    return name;
  }

  public void addEntry(PrincipalName paramPrincipalName, char[] paramArrayOfChar)
    throws KrbException
  {
    EncryptionKey[] arrayOfEncryptionKey = EncryptionKey.acquireSecretKeys(paramArrayOfChar, paramPrincipalName.getSalt());
    for (int i = 0; (arrayOfEncryptionKey != null) && (i < arrayOfEncryptionKey.length); ++i)
    {
      int j = arrayOfEncryptionKey[i].getEType();
      byte[] arrayOfByte = arrayOfEncryptionKey[i].getBytes();
      int k = retrieveEntry(paramPrincipalName, j);
      int l = 1;
      if (k != -1)
      {
        localKeyTabEntry = (KeyTabEntry)(KeyTabEntry)this.entries.elementAt(k);
        l = localKeyTabEntry.keyVersion;
        this.entries.removeElementAt(k);
        ++l;
      }
      else
      {
        l = 1;
      }
      KeyTabEntry localKeyTabEntry = new KeyTabEntry(paramPrincipalName, paramPrincipalName.getRealm(), new KerberosTime(System.currentTimeMillis()), l, j, arrayOfByte);
      if (this.entries == null)
        this.entries = new Vector();
      this.entries.addElement(localKeyTabEntry);
    }
  }

  private int retrieveEntry(PrincipalName paramPrincipalName, int paramInt)
  {
    int i = -1;
    if (this.entries != null)
      for (int j = 0; j < this.entries.size(); ++j)
      {
        KeyTabEntry localKeyTabEntry = (KeyTabEntry)(KeyTabEntry)this.entries.elementAt(j);
        if ((paramPrincipalName.match(localKeyTabEntry.getService())) && (((paramInt == -1) || (localKeyTabEntry.keyType == paramInt))))
          return j;
      }
    return i;
  }

  public KeyTabEntry[] getEntries()
  {
    if (this.entries != null)
    {
      KeyTabEntry[] arrayOfKeyTabEntry = new KeyTabEntry[this.entries.size()];
      for (int i = 0; i < arrayOfKeyTabEntry.length; ++i)
        arrayOfKeyTabEntry[i] = ((KeyTabEntry)(KeyTabEntry)this.entries.elementAt(i));
      return arrayOfKeyTabEntry;
    }
    return null;
  }

  public static synchronized KeyTab create()
    throws IOException, RealmException
  {
    String str = getDefaultKeyTab();
    return create(str);
  }

  public static synchronized KeyTab create(String paramString)
    throws IOException, RealmException
  {
    KeyTabOutputStream localKeyTabOutputStream = new KeyTabOutputStream(new FileOutputStream(paramString));
    localKeyTabOutputStream.writeVersion(1282);
    localKeyTabOutputStream.close();
    singleton = new KeyTab(paramString);
    return singleton;
  }

  public synchronized void save()
    throws IOException
  {
    KeyTabOutputStream localKeyTabOutputStream = new KeyTabOutputStream(new FileOutputStream(name));
    localKeyTabOutputStream.writeVersion(this.kt_vno);
    for (int i = 0; i < this.entries.size(); ++i)
      localKeyTabOutputStream.writeEntry((KeyTabEntry)this.entries.elementAt(i));
    localKeyTabOutputStream.close();
  }

  public void deleteEntry(PrincipalName paramPrincipalName)
  {
    int i = retrieveEntry(paramPrincipalName, -1);
    if (i != -1)
      this.entries.removeElementAt(i);
  }

  public synchronized void createVersion(File paramFile)
    throws IOException
  {
    KeyTabOutputStream localKeyTabOutputStream = new KeyTabOutputStream(new FileOutputStream(paramFile));
    localKeyTabOutputStream.write16(1282);
    localKeyTabOutputStream.close();
  }

  public static void refresh()
  {
    if (singleton != null)
    {
      if (DEBUG)
        System.out.println("Refreshing Keytab");
      singleton = null;
    }
  }
}