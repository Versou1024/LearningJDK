package sun.security.krb5;

import java.io.IOException;
import java.io.PrintStream;
import java.util.EmptyStackException;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.Vector;
import sun.security.krb5.internal.Krb5;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class Realm
  implements Cloneable
{
  private String realm;
  private static boolean DEBUG = Krb5.DEBUG;

  private Realm()
  {
  }

  public Realm(String paramString)
    throws sun.security.krb5.RealmException
  {
    this.realm = parseRealm(paramString);
  }

  public Object clone()
  {
    Realm localRealm = new Realm();
    if (this.realm != null)
      localRealm.realm = new String(this.realm);
    return localRealm;
  }

  public boolean equals(Object paramObject)
  {
    if (this == paramObject)
      return true;
    if (!(paramObject instanceof Realm))
      return false;
    Realm localRealm = (Realm)paramObject;
    if ((this.realm != null) && (localRealm.realm != null))
      return this.realm.equals(localRealm.realm);
    return ((this.realm == null) && (localRealm.realm == null));
  }

  public int hashCode()
  {
    int i = 17;
    if (this.realm != null)
      i = 37 * i + this.realm.hashCode();
    return i;
  }

  public Realm(DerValue paramDerValue)
    throws sun.security.krb5.Asn1Exception, sun.security.krb5.RealmException, IOException
  {
    if (paramDerValue == null)
      throw new IllegalArgumentException("encoding can not be null");
    this.realm = paramDerValue.getGeneralString();
    if ((this.realm == null) || (this.realm.length() == 0))
      throw new sun.security.krb5.RealmException(601);
    if (!(isValidRealmString(this.realm)))
      throw new sun.security.krb5.RealmException(600);
  }

  public String toString()
  {
    return this.realm;
  }

  public static String parseRealmAtSeparator(String paramString)
    throws sun.security.krb5.RealmException
  {
    if (paramString == null)
      throw new IllegalArgumentException("null input name is not allowed");
    String str1 = new String(paramString);
    String str2 = null;
    for (int i = 0; i < str1.length(); ++i)
      if ((str1.charAt(i) == '@') && (((i == 0) || (str1.charAt(i - 1) != '\\'))))
      {
        if (i + 1 >= str1.length())
          break;
        str2 = str1.substring(i + 1, str1.length());
        break;
      }
    if (str2 != null)
    {
      if (str2.length() == 0)
        throw new sun.security.krb5.RealmException(601);
      if (!(isValidRealmString(str2)))
        throw new sun.security.krb5.RealmException(600);
    }
    return str2;
  }

  protected static String parseRealm(String paramString)
    throws sun.security.krb5.RealmException
  {
    String str = parseRealmAtSeparator(paramString);
    if (str == null)
      str = paramString;
    if ((str == null) || (str.length() == 0))
      throw new sun.security.krb5.RealmException(601);
    if (!(isValidRealmString(str)))
      throw new sun.security.krb5.RealmException(600);
    return str;
  }

  protected static boolean isValidRealmString(String paramString)
  {
    if (paramString == null)
      return false;
    if (paramString.length() == 0)
      return false;
    for (int i = 0; i < paramString.length(); ++i)
      if ((paramString.charAt(i) == '/') || (paramString.charAt(i) == ':') || (paramString.charAt(i) == 0))
        return false;
    return true;
  }

  public byte[] asn1Encode()
    throws sun.security.krb5.Asn1Exception, IOException
  {
    DerOutputStream localDerOutputStream = new DerOutputStream();
    localDerOutputStream.putGeneralString(this.realm);
    return localDerOutputStream.toByteArray();
  }

  public static Realm parse(DerInputStream paramDerInputStream, byte paramByte, boolean paramBoolean)
    throws sun.security.krb5.Asn1Exception, IOException, sun.security.krb5.RealmException
  {
    if ((paramBoolean) && (((byte)paramDerInputStream.peekByte() & 0x1F) != paramByte))
      return null;
    DerValue localDerValue1 = paramDerInputStream.getDerValue();
    if (paramByte != (localDerValue1.getTag() & 0x1F))
      throw new sun.security.krb5.Asn1Exception(906);
    DerValue localDerValue2 = localDerValue1.getData().getDerValue();
    return new Realm(localDerValue2);
  }

  private static String[] doInitialParse(String paramString1, String paramString2)
    throws sun.security.krb5.KrbException
  {
    if ((paramString1 == null) || (paramString2 == null))
      throw new sun.security.krb5.KrbException(400);
    if (DEBUG)
      System.out.println(">>> Realm doInitialParse: cRealm=[" + paramString1 + "], sRealm=[" + paramString2 + "]");
    if (paramString1.equals(paramString2))
    {
      String[] arrayOfString = null;
      arrayOfString = new String[1];
      arrayOfString[0] = new String(paramString1);
      if (DEBUG)
        System.out.println(">>> Realm doInitialParse: " + arrayOfString[0]);
      return arrayOfString;
    }
    return null;
  }

  public static String[] getRealmsList(String paramString1, String paramString2)
    throws sun.security.krb5.KrbException
  {
    String[] arrayOfString = doInitialParse(paramString1, paramString2);
    if ((arrayOfString != null) && (arrayOfString.length != 0))
      return arrayOfString;
    arrayOfString = parseCapaths(paramString1, paramString2);
    if ((arrayOfString != null) && (arrayOfString.length != 0))
      return arrayOfString;
    arrayOfString = parseHierarchy(paramString1, paramString2);
    return arrayOfString;
  }

  private static String[] parseCapaths(String paramString1, String paramString2)
    throws sun.security.krb5.KrbException
  {
    String[] arrayOfString = null;
    Config localConfig = null;
    try
    {
      localConfig = Config.getInstance();
    }
    catch (Exception localException)
    {
      if (DEBUG)
        System.out.println("Configuration information can not be obtained " + localException.getMessage());
      return null;
    }
    String str1 = localConfig.getDefault(paramString2, paramString1);
    if (str1 == null)
    {
      if (DEBUG)
        System.out.println(">>> Realm parseCapaths: no cfg entry");
      return null;
    }
    String str2 = null;
    String str3 = null;
    StringTokenizer localStringTokenizer = null;
    Stack localStack = new Stack();
    Vector localVector = new Vector(8, 8);
    localVector.add(paramString1);
    int i = 0;
    if (DEBUG)
      str2 = paramString2;
    while (true)
    {
      if (DEBUG)
      {
        ++i;
        System.out.println(">>> Realm parseCapaths: loop " + i + ": target=" + str2);
      }
      if ((str1 != null) && (!(str1.equals("."))))
      {
        if (DEBUG)
          System.out.println(">>> Realm parseCapaths: loop " + i + ": intermediaries=[" + str1 + "]");
        localStringTokenizer = new StringTokenizer(str1, " ");
        while (true)
        {
          label337: 
          do
            while (true)
            {
              do
              {
                if (!(localStringTokenizer.hasMoreTokens()))
                  break label424;
                str3 = localStringTokenizer.nextToken();
                if ((str3.equals(".")) || (localStack.contains(str3)))
                  break label337;
                localStack.push(str3);
              }
              while (!(DEBUG));
              System.out.println(">>> Realm parseCapaths: loop " + i + ": pushed realm on to stack: " + str3);
            }
          while (!(DEBUG));
          System.out.println(">>> Realm parseCapaths: loop " + i + ": ignoring realm: [" + str3 + "]");
        }
      }
      if (DEBUG)
        System.out.println(">>> Realm parseCapaths: loop " + i + ": no intermediaries");
      try
      {
        label424: str2 = (String)localStack.pop();
      }
      catch (EmptyStackException localEmptyStackException)
      {
        str2 = null;
      }
      if (str2 == null)
        break;
      localVector.add(str2);
      if (DEBUG)
        System.out.println(">>> Realm parseCapaths: loop " + i + ": added intermediary to list: " + str2);
      str1 = localConfig.getDefault(str2, paramString1);
    }
    arrayOfString = new String[localVector.size()];
    try
    {
      arrayOfString = (String[])(String[])localVector.toArray(arrayOfString);
    }
    catch (ArrayStoreException localArrayStoreException)
    {
      arrayOfString = null;
    }
    if ((DEBUG) && (arrayOfString != null))
      for (int j = 0; j < arrayOfString.length; ++j)
        System.out.println(">>> Realm parseCapaths [" + j + "]=" + arrayOfString[j]);
    return arrayOfString;
  }

  private static String[] parseHierarchy(String paramString1, String paramString2)
    throws sun.security.krb5.KrbException
  {
    String[] arrayOfString1 = null;
    String[] arrayOfString2 = null;
    String[] arrayOfString3 = null;
    StringTokenizer localStringTokenizer = new StringTokenizer(paramString1, ".");
    int i = localStringTokenizer.countTokens();
    arrayOfString2 = new String[i];
    for (i = 0; localStringTokenizer.hasMoreTokens(); ++i)
      arrayOfString2[i] = localStringTokenizer.nextToken();
    if (DEBUG)
    {
      System.out.println(">>> Realm parseHierarchy: cRealm has " + i + " components:");
      j = 0;
      while (j < i)
        System.out.println(">>> Realm parseHierarchy: cComponents[" + j + "]=" + arrayOfString2[(j++)]);
    }
    localStringTokenizer = new StringTokenizer(paramString2, ".");
    int j = localStringTokenizer.countTokens();
    arrayOfString3 = new String[j];
    for (j = 0; localStringTokenizer.hasMoreTokens(); ++j)
      arrayOfString3[j] = localStringTokenizer.nextToken();
    if (DEBUG)
    {
      System.out.println(">>> Realm parseHierarchy: sRealm has " + j + " components:");
      k = 0;
      while (k < j)
        System.out.println(">>> Realm parseHierarchy: sComponents[" + k + "]=" + arrayOfString3[(k++)]);
    }
    int k = 0;
    --j;
    --i;
    while ((j >= 0) && (i >= 0) && (arrayOfString3[j].equals(arrayOfString2[i])))
    {
      ++k;
      --j;
      --i;
    }
    int l = -1;
    int i1 = -1;
    int i2 = 0;
    if (k > 0)
    {
      i1 = j + 1;
      l = i + 1;
      i2 += i1;
      i2 += l;
    }
    else
    {
      ++i2;
    }
    if (DEBUG)
      if (k > 0)
      {
        System.out.println(">>> Realm parseHierarchy: " + k + " common component" + ((k > 1) ? "s" : " "));
        System.out.println(">>> Realm parseHierarchy: common part in cRealm (starts at index " + l + ")");
        System.out.println(">>> Realm parseHierarchy: common part in sRealm (starts at index " + i1 + ")");
        str1 = substring(paramString1, l);
        System.out.println(">>> Realm parseHierarchy: common part in cRealm=" + str1);
        str1 = substring(paramString2, i1);
        System.out.println(">>> Realm parseHierarchy: common part in sRealm=" + str1);
      }
      else
      {
        System.out.println(">>> Realm parseHierarchy: no common part");
      }
    if (DEBUG)
      System.out.println(">>> Realm parseHierarchy: total links=" + i2);
    arrayOfString1 = new String[i2];
    arrayOfString1[0] = new String(paramString1);
    if (DEBUG)
      System.out.println(">>> Realm parseHierarchy A: retList[0]=" + arrayOfString1[0]);
    String str1 = null;
    String str2 = null;
    int i3 = 1;
    for (i = 0; (i3 < i2) && (i < l); ++i)
    {
      str2 = substring(paramString1, i + 1);
      arrayOfString1[(i3++)] = new String(str2);
      if (DEBUG)
        System.out.println(">>> Realm parseHierarchy B: retList[" + (i3 - 1) + "]=" + arrayOfString1[(i3 - 1)]);
    }
    for (j = i1; (i3 < i2) && (j - 1 > 0); --j)
    {
      str2 = substring(paramString2, j - 1);
      arrayOfString1[(i3++)] = new String(str2);
      if (DEBUG)
        System.out.println(">>> Realm parseHierarchy D: retList[" + (i3 - 1) + "]=" + arrayOfString1[(i3 - 1)]);
    }
    return arrayOfString1;
  }

  private static String substring(String paramString, int paramInt)
  {
    int i = 0;
    int j = 0;
    int k = paramString.length();
    while (true)
    {
      while (true)
      {
        if ((i >= k) || (j == paramInt))
          break label43;
        if (paramString.charAt(i++) == '.')
          break;
      }
      ++j;
    }
    label43: return paramString.substring(i);
  }

  static int getRandIndex(int paramInt)
  {
    return ((int)(Math.random() * 16384.0D) % paramInt);
  }

  static void printNames(String[] paramArrayOfString)
  {
    if ((paramArrayOfString == null) || (paramArrayOfString.length == 0))
      return;
    int i = paramArrayOfString.length;
    int j = 0;
    System.out.println("List length = " + i);
    while (j < paramArrayOfString.length)
    {
      System.out.println("[" + j + "]=" + paramArrayOfString[j]);
      ++j;
    }
  }
}