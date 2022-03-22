package sun.security.krb5;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;
import sun.security.action.GetPropertyAction;
import sun.security.krb5.internal.Krb5;
import sun.security.krb5.internal.crypto.EType;
import sun.security.krb5.internal.ktab.KeyTab;

public class Config
{
  private static Config singleton = null;
  private Hashtable stanzaTable;
  private static boolean DEBUG = Krb5.DEBUG;
  private static final int BASE16_0 = 1;
  private static final int BASE16_1 = 16;
  private static final int BASE16_2 = 256;
  private static final int BASE16_3 = 4096;
  private String defaultRealm;

  private static native String getWindowsDirectory();

  public static synchronized Config getInstance()
    throws sun.security.krb5.KrbException
  {
    if (singleton == null)
      singleton = new Config();
    return singleton;
  }

  public static synchronized void refresh()
    throws sun.security.krb5.KrbException
  {
    singleton = new Config();
    KeyTab.refresh();
  }

  private Config()
    throws sun.security.krb5.KrbException
  {
    String str = (String)AccessController.doPrivileged(new GetPropertyAction("java.security.krb5.kdc"));
    this.defaultRealm = ((String)AccessController.doPrivileged(new GetPropertyAction("java.security.krb5.realm")));
    if (((str == null) && (this.defaultRealm != null)) || ((this.defaultRealm == null) && (str != null)))
      throw new sun.security.krb5.KrbException("System property java.security.krb5.kdc and java.security.krb5.realm both must be set or neither must be set.");
    if (str != null)
    {
      localObject = new Hashtable();
      ((Hashtable)localObject).put("default_realm", this.defaultRealm);
      str = str.replace(':', ' ');
      ((Hashtable)localObject).put("kdc", str);
      this.stanzaTable = new Hashtable();
      this.stanzaTable.put("libdefaults", localObject);
    }
    else
    {
      try
      {
        localObject = loadConfigFile();
        this.stanzaTable = parseStanzaTable((Vector)localObject);
      }
      catch (IOException localIOException)
      {
        KrbException localKrbException = new sun.security.krb5.KrbException("Could not load configuration file " + localIOException.getMessage());
        localKrbException.initCause(localIOException);
        throw localKrbException;
      }
    }
  }

  public int getDefaultIntValue(String paramString)
  {
    String str = null;
    int i = -2147483648;
    str = getDefault(paramString);
    if (str != null)
      try
      {
        i = parseIntValue(str);
      }
      catch (NumberFormatException localNumberFormatException)
      {
        if (DEBUG)
        {
          System.out.println("Exception in getting value of " + paramString + " " + localNumberFormatException.getMessage());
          System.out.println("Setting " + paramString + " to minimum value");
        }
        i = -2147483648;
      }
    return i;
  }

  public int getDefaultIntValue(String paramString1, String paramString2)
  {
    String str = null;
    int i = -2147483648;
    str = getDefault(paramString1, paramString2);
    if (str != null)
      try
      {
        i = parseIntValue(str);
      }
      catch (NumberFormatException localNumberFormatException)
      {
        if (DEBUG)
        {
          System.out.println("Exception in getting value of " + paramString1 + " in section " + paramString2 + " " + localNumberFormatException.getMessage());
          System.out.println("Setting " + paramString1 + " to minimum value");
        }
        i = -2147483648;
      }
    return i;
  }

  public String getDefault(String paramString)
  {
    if (this.stanzaTable == null)
      return null;
    return getDefault(paramString, this.stanzaTable);
  }

  private String getDefault(String paramString, Hashtable paramHashtable)
  {
    String str1 = null;
    if (this.stanzaTable != null)
    {
      Enumeration localEnumeration = paramHashtable.keys();
      while (localEnumeration.hasMoreElements())
      {
        String str2 = (String)localEnumeration.nextElement();
        Object localObject = paramHashtable.get(str2);
        if (localObject instanceof Hashtable)
        {
          str1 = getDefault(paramString, (Hashtable)localObject);
          if (str1 == null)
            continue;
          return str1;
        }
        if (str2.equalsIgnoreCase(paramString))
        {
          if (localObject instanceof String)
            return ((String)(String)paramHashtable.get(str2));
          if (localObject instanceof Vector)
          {
            str1 = "";
            int i = ((Vector)localObject).size();
            for (int j = 0; j < i; ++j)
              if (j == i - 1)
                str1 = str1 + ((String)(String)((Vector)localObject).elementAt(j));
              else
                str1 = str1 + ((String)(String)((Vector)localObject).elementAt(j)) + " ";
            return str1;
          }
        }
      }
    }
    return str1;
  }

  public String getDefault(String paramString1, String paramString2)
  {
    String str2 = null;
    if ((paramString1.equalsIgnoreCase("kdc")) && (!(paramString2.equalsIgnoreCase("libdefaults"))) && ((String)AccessController.doPrivileged(new GetPropertyAction("java.security.krb5.kdc")) != null))
    {
      str2 = getDefault("kdc", "libdefaults");
      return str2;
    }
    if (this.stanzaTable != null)
    {
      Enumeration localEnumeration = this.stanzaTable.keys();
      while (true)
      {
        Hashtable localHashtable1;
        do
        {
          do
          {
            if (!(localEnumeration.hasMoreElements()))
              break label321;
            String str1 = (String)localEnumeration.nextElement();
            localHashtable1 = (Hashtable)this.stanzaTable.get(str1);
            if (!(str1.equalsIgnoreCase(paramString2)))
              break label132;
          }
          while (!(localHashtable1.containsKey(paramString1)));
          label132: return ((String)(String)localHashtable1.get(paramString1));
        }
        while (!(localHashtable1.containsKey(paramString2)));
        Object localObject1 = localHashtable1.get(paramString2);
        if (localObject1 instanceof Hashtable)
        {
          Hashtable localHashtable2 = (Hashtable)localObject1;
          if (localHashtable2.containsKey(paramString1))
          {
            Object localObject2 = localHashtable2.get(paramString1);
            if (localObject2 instanceof Vector)
            {
              str2 = "";
              int i = ((Vector)localObject2).size();
              for (int j = 0; j < i; ++j)
                if (j == i - 1)
                  str2 = str2 + ((String)(String)((Vector)localObject2).elementAt(j));
                else
                  str2 = str2 + ((String)(String)((Vector)localObject2).elementAt(j)) + " ";
            }
            else
            {
              str2 = (String)localObject2;
            }
          }
        }
      }
    }
    label321: return str2;
  }

  public boolean getDefaultBooleanValue(String paramString)
  {
    String str = null;
    if (this.stanzaTable == null)
      str = null;
    else
      str = getDefault(paramString, this.stanzaTable);
    return ((str != null) && (str.equalsIgnoreCase("true")));
  }

  public boolean getDefaultBooleanValue(String paramString1, String paramString2)
  {
    String str = getDefault(paramString1, paramString2);
    return ((str != null) && (str.equalsIgnoreCase("true")));
  }

  private int parseIntValue(String paramString)
    throws NumberFormatException
  {
    String str;
    int i = 0;
    if (paramString.startsWith("+"))
    {
      str = paramString.substring(1);
      return Integer.parseInt(str);
    }
    if (paramString.startsWith("0x"))
    {
      str = paramString.substring(2);
      char[] arrayOfChar = str.toCharArray();
      if (arrayOfChar.length > 8)
        throw new NumberFormatException();
      for (int j = 0; j < arrayOfChar.length; ++j)
      {
        int k = arrayOfChar.length - j - 1;
        switch (arrayOfChar[j])
        {
        case '0':
          i += 0;
          break;
        case '1':
          i += 1 * getBase(k);
          break;
        case '2':
          i += 2 * getBase(k);
          break;
        case '3':
          i += 3 * getBase(k);
          break;
        case '4':
          i += 4 * getBase(k);
          break;
        case '5':
          i += 5 * getBase(k);
          break;
        case '6':
          i += 6 * getBase(k);
          break;
        case '7':
          i += 7 * getBase(k);
          break;
        case '8':
          i += 8 * getBase(k);
          break;
        case '9':
          i += 9 * getBase(k);
          break;
        case 'A':
        case 'a':
          i += 10 * getBase(k);
          break;
        case 'B':
        case 'b':
          i += 11 * getBase(k);
          break;
        case 'C':
        case 'c':
          i += 12 * getBase(k);
          break;
        case 'D':
        case 'd':
          i += 13 * getBase(k);
          break;
        case 'E':
        case 'e':
          i += 14 * getBase(k);
          break;
        case 'F':
        case 'f':
          i += 15 * getBase(k);
          break;
        case ':':
        case ';':
        case '<':
        case '=':
        case '>':
        case '?':
        case '@':
        case 'G':
        case 'H':
        case 'I':
        case 'J':
        case 'K':
        case 'L':
        case 'M':
        case 'N':
        case 'O':
        case 'P':
        case 'Q':
        case 'R':
        case 'S':
        case 'T':
        case 'U':
        case 'V':
        case 'W':
        case 'X':
        case 'Y':
        case 'Z':
        case '[':
        case '\\':
        case ']':
        case '^':
        case '_':
        case '`':
        default:
          throw new NumberFormatException("Invalid numerical format");
        }
      }
      if (i < 0)
        throw new NumberFormatException("Data overflow.");
    }
    else
    {
      i = Integer.parseInt(paramString);
    }
    return i;
  }

  private int getBase(int paramInt)
  {
    int i = 16;
    switch (paramInt)
    {
    case 0:
      i = 1;
      break;
    case 1:
      i = 16;
      break;
    case 2:
      i = 256;
      break;
    case 3:
      i = 4096;
      break;
    default:
      for (int j = 1; j < paramInt; ++j)
        i *= 16;
    }
    return i;
  }

  private String find(String paramString1, String paramString2)
  {
    if (this.stanzaTable != null)
    {
      String str;
      if ((str = (String)(String)((Hashtable)(Hashtable)this.stanzaTable.get(paramString1)).get(paramString2)) != null)
        return str;
    }
    return "";
  }

  private Vector loadConfigFile()
    throws IOException
  {
    String str1;
    try
    {
      str1 = getFileName();
      if (!(str1.equals("")))
      {
        BufferedReader localBufferedReader = new BufferedReader(new InputStreamReader((FileInputStream)AccessController.doPrivileged(new PrivilegedExceptionAction(this, str1)
        {
          public Object run()
            throws IOException
          {
            return new FileInputStream(this.val$fileName);
          }
        })));
        Vector localVector = new Vector();
        while (true)
        {
          String str2;
          do
            if ((str2 = localBufferedReader.readLine()) == null)
              break label83;
          while (str2.startsWith("#"));
          localVector.addElement(str2.trim());
        }
        label83: localBufferedReader.close();
        return localVector;
      }
      return null;
    }
    catch (PrivilegedActionException localPrivilegedActionException)
    {
      throw ((IOException)localPrivilegedActionException.getException());
    }
  }

  private Hashtable parseStanzaTable(Vector paramVector)
    throws sun.security.krb5.KrbException
  {
    if (paramVector == null)
      throw new sun.security.krb5.KrbException("I/O error while reading configuration file.");
    Hashtable localHashtable1 = new Hashtable();
    for (int i = 0; i < paramVector.size(); ++i)
    {
      int j;
      Hashtable localHashtable2;
      String str1 = ((String)paramVector.elementAt(i)).trim();
      if (str1.equalsIgnoreCase("[realms]"))
      {
        for (j = i + 1; j < paramVector.size() + 1; ++j)
          if ((j == paramVector.size()) || (((String)paramVector.elementAt(j)).startsWith("[")))
          {
            localHashtable2 = new Hashtable();
            localHashtable2 = parseRealmField(paramVector, i + 1, j);
            localHashtable1.put("realms", localHashtable2);
            i = j - 1;
            break;
          }
      }
      else if (str1.equalsIgnoreCase("[capaths]"))
      {
        for (j = i + 1; j < paramVector.size() + 1; ++j)
          if ((j == paramVector.size()) || (((String)paramVector.elementAt(j)).startsWith("[")))
          {
            localHashtable2 = new Hashtable();
            localHashtable2 = parseRealmField(paramVector, i + 1, j);
            localHashtable1.put("capaths", localHashtable2);
            i = j - 1;
            break;
          }
      }
      else if ((str1.startsWith("[")) && (str1.endsWith("]")))
      {
        String str2 = str1.substring(1, str1.length() - 1);
        for (int k = i + 1; k < paramVector.size() + 1; ++k)
          if ((k == paramVector.size()) || (((String)paramVector.elementAt(k)).startsWith("[")))
          {
            Hashtable localHashtable3 = parseField(paramVector, i + 1, k);
            localHashtable1.put(str2, localHashtable3);
            i = k - 1;
            break;
          }
      }
    }
    return localHashtable1;
  }

  private String getFileName()
  {
    Boolean localBoolean;
    String str1 = (String)AccessController.doPrivileged(new GetPropertyAction("java.security.krb5.conf"));
    if (str1 != null)
    {
      localBoolean = (Boolean)AccessController.doPrivileged(new FileExistsAction(str1));
      if (localBoolean.booleanValue())
        return str1;
    }
    else
    {
      str1 = ((String)AccessController.doPrivileged(new GetPropertyAction("java.home"))) + File.separator + "lib" + File.separator + "security" + File.separator + "krb5.conf";
      localBoolean = (Boolean)AccessController.doPrivileged(new FileExistsAction(str1));
      if (localBoolean.booleanValue())
        return str1;
      String str2 = (String)AccessController.doPrivileged(new GetPropertyAction("os.name"));
      if (str2.startsWith("Windows"))
      {
        try
        {
          Credentials.ensureLoaded();
        }
        catch (Exception localException)
        {
        }
        if (Credentials.alreadyLoaded)
          if ((str1 = getWindowsDirectory()) == null)
            str1 = "c:\\winnt\\krb5.ini";
          else if (str1.endsWith("\\"))
            str1 = str1 + "krb5.ini";
          else
            str1 = str1 + "\\krb5.ini";
        else
          str1 = "c:\\winnt\\krb5.ini";
      }
      else if (str2.startsWith("SunOS"))
      {
        str1 = "/etc/krb5/krb5.conf";
      }
      else if (str2.startsWith("Linux"))
      {
        str1 = "/etc/krb5.conf";
      }
    }
    if (DEBUG)
      System.out.println("Config name: " + str1);
    return str1;
  }

  private Hashtable parseField(Vector paramVector, int paramInt1, int paramInt2)
  {
    Hashtable localHashtable = new Hashtable();
    for (int i = paramInt1; i < paramInt2; ++i)
    {
      String str1 = (String)paramVector.elementAt(i);
      for (int j = 0; j < str1.length(); ++j)
        if (str1.charAt(j) == '=')
        {
          String str2 = str1.substring(0, j).trim();
          String str3 = str1.substring(j + 1).trim();
          localHashtable.put(str2, str3);
          break;
        }
    }
    return localHashtable;
  }

  private Hashtable parseRealmField(Vector paramVector, int paramInt1, int paramInt2)
  {
    Hashtable localHashtable1 = new Hashtable();
    for (int i = paramInt1; i < paramInt2; ++i)
    {
      String str1 = ((String)paramVector.elementAt(i)).trim();
      if (str1.endsWith("{"))
      {
        String str2 = "";
        for (int j = 0; j < str1.length(); ++j)
          if (str1.charAt(j) == '=')
          {
            str2 = str1.substring(0, j).trim();
            break;
          }
        for (j = i + 1; j < paramInt2; ++j)
        {
          int k = 0;
          str1 = ((String)paramVector.elementAt(j)).trim();
          for (int l = 0; l < str1.length(); ++l)
            if (str1.charAt(l) == '}')
            {
              k = 1;
              break;
            }
          if (k == 1)
          {
            Hashtable localHashtable2 = parseRealmFieldEx(paramVector, i + 1, j);
            localHashtable1.put(str2, localHashtable2);
            i = j;
            k = 0;
            break;
          }
        }
      }
    }
    return localHashtable1;
  }

  private Hashtable parseRealmFieldEx(Vector paramVector, int paramInt1, int paramInt2)
  {
    Hashtable localHashtable = new Hashtable();
    Vector localVector1 = new Vector();
    Vector localVector2 = new Vector();
    String str1 = "";
    for (int i = paramInt1; i < paramInt2; ++i)
    {
      str1 = (String)paramVector.elementAt(i);
      for (int j = 0; j < str1.length(); ++j)
        if (str1.charAt(j) == '=')
        {
          String str2 = str1.substring(0, j - 1).trim();
          if (!(exists(str2, localVector1)))
          {
            localVector1.addElement(str2);
            localVector2 = new Vector();
          }
          else
          {
            localVector2 = (Vector)(Vector)localHashtable.get(str2);
          }
          localVector2.addElement(str1.substring(j + 1).trim());
          localHashtable.put(str2, localVector2);
          break;
        }
    }
    return localHashtable;
  }

  private boolean exists(String paramString, Vector paramVector)
  {
    int i = 0;
    for (int j = 0; j < paramVector.size(); ++j)
      if (((String)(String)paramVector.elementAt(j)).equals(paramString))
        i = 1;
    return i;
  }

  public void listTable()
  {
    listTable(this.stanzaTable);
  }

  private void listTable(Hashtable paramHashtable)
  {
    Vector localVector1 = new Vector();
    Vector localVector2 = new Vector();
    if (this.stanzaTable != null)
    {
      Enumeration localEnumeration = paramHashtable.keys();
      while (localEnumeration.hasMoreElements())
      {
        String str = (String)localEnumeration.nextElement();
        Object localObject = paramHashtable.get(str);
        if (paramHashtable == this.stanzaTable)
          System.out.println("[" + str + "]");
        if (localObject instanceof Hashtable)
        {
          if (paramHashtable != this.stanzaTable)
            System.out.println("\t" + str + " = {");
          listTable((Hashtable)localObject);
          if (paramHashtable != this.stanzaTable)
            System.out.println("\t}");
        }
        else if (localObject instanceof String)
        {
          System.out.println("\t" + str + " = " + ((String)paramHashtable.get(str)));
        }
        else if (localObject instanceof Vector)
        {
          localVector1 = (Vector)localObject;
          for (int i = 0; i < localVector1.size(); ++i)
            System.out.println("\t" + str + " = " + ((String)localVector1.elementAt(i)));
        }
      }
    }
    else
    {
      System.out.println("Configuration file not found.");
    }
  }

  public int[] defaultEtype(String paramString)
  {
    int[] arrayOfInt;
    int i;
    String str1 = getDefault(paramString, "libdefaults");
    String str2 = " ";
    if (str1 == null)
    {
      if (DEBUG)
        System.out.println("Using builtin default etypes for " + paramString);
      arrayOfInt = EType.getBuiltInDefaults();
    }
    else
    {
      for (i = 0; i < str1.length(); ++i)
        if (str1.substring(i, i + 1).equals(","))
        {
          str2 = ",";
          break;
        }
      StringTokenizer localStringTokenizer = new StringTokenizer(str1, str2);
      i = localStringTokenizer.countTokens();
      ArrayList localArrayList = new ArrayList(i);
      for (int k = 0; k < i; ++k)
      {
        int j = getType(localStringTokenizer.nextToken());
        if ((j != -1) && (EType.isSupported(j)))
          localArrayList.add(new Integer(j));
      }
      if (localArrayList.size() == 0)
      {
        if (DEBUG)
          System.out.println("no supported default etypes for " + paramString);
        return null;
      }
      arrayOfInt = new int[localArrayList.size()];
      for (k = 0; k < arrayOfInt.length; ++k)
        arrayOfInt[k] = ((Integer)(Integer)localArrayList.get(k)).intValue();
    }
    if (DEBUG)
    {
      System.out.print("default etypes for " + paramString + ":");
      for (i = 0; i < arrayOfInt.length; ++i)
        System.out.print(" " + arrayOfInt[i]);
      System.out.println(".");
    }
    return arrayOfInt;
  }

  public int getType(String paramString)
  {
    int i = -1;
    if (paramString == null)
      return i;
    if ((paramString.startsWith("d")) || (paramString.startsWith("D")))
      if (paramString.equalsIgnoreCase("des-cbc-crc"))
        i = 1;
      else if (paramString.equalsIgnoreCase("des-cbc-md5"))
        i = 3;
      else if (paramString.equalsIgnoreCase("des-mac"))
        i = 4;
      else if (paramString.equalsIgnoreCase("des-mac-k"))
        i = 5;
      else if (paramString.equalsIgnoreCase("des-cbc-md4"))
        i = 2;
      else if ((paramString.equalsIgnoreCase("des3-cbc-sha1")) || (paramString.equalsIgnoreCase("des3-hmac-sha1")) || (paramString.equalsIgnoreCase("des3-cbc-sha1-kd")) || (paramString.equalsIgnoreCase("des3-cbc-hmac-sha1-kd")))
        i = 16;
    else if ((paramString.startsWith("a")) || (paramString.startsWith("A")))
      if ((paramString.equalsIgnoreCase("aes128-cts")) || (paramString.equalsIgnoreCase("aes128-cts-hmac-sha1-96")))
        i = 17;
      else if ((paramString.equalsIgnoreCase("aes256-cts")) || (paramString.equalsIgnoreCase("aes256-cts-hmac-sha1-96")))
        i = 18;
      else if ((paramString.equalsIgnoreCase("arcfour-hmac")) || (paramString.equalsIgnoreCase("arcfour-hmac-md5")))
        i = 23;
    else if (paramString.equalsIgnoreCase("rc4-hmac"))
      i = 23;
    else if (paramString.equalsIgnoreCase("CRC32"))
      i = 1;
    else if ((paramString.startsWith("r")) || (paramString.startsWith("R")))
      if (paramString.equalsIgnoreCase("rsa-md5"))
        i = 7;
      else if (paramString.equalsIgnoreCase("rsa-md5-des"))
        i = 8;
    else if (paramString.equalsIgnoreCase("hmac-sha1-des3-kd"))
      i = 12;
    else if (paramString.equalsIgnoreCase("hmac-sha1-96-aes128"))
      i = 15;
    else if (paramString.equalsIgnoreCase("hmac-sha1-96-aes256"))
      i = 16;
    else if ((paramString.equalsIgnoreCase("hmac-md5-rc4")) || (paramString.equalsIgnoreCase("hmac-md5-arcfour")) || (paramString.equalsIgnoreCase("hmac-md5-enc")))
      i = -138;
    else if (paramString.equalsIgnoreCase("NULL"))
      i = 0;
    return i;
  }

  public void resetDefaultRealm(String paramString)
  {
    this.defaultRealm = paramString;
    if (DEBUG)
      System.out.println(">>> Config reset default kdc " + this.defaultRealm);
  }

  public String getDefaultRealm()
  {
    return getDefault("default_realm", "libdefaults");
  }

  public String getKDCList(String paramString)
  {
    if (paramString == null)
      paramString = getDefaultRealm();
    String str = getDefault("kdc", paramString);
    if (str == null)
      return null;
    return str;
  }

  static class FileExistsAction
  implements PrivilegedAction
  {
    private String fileName;

    public FileExistsAction(String paramString)
    {
      this.fileName = paramString;
    }

    public Object run()
    {
      return new Boolean(new File(this.fileName).exists());
    }
  }
}