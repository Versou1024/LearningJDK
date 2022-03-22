package sun.security.krb5;

import [B;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Vector;
import sun.security.krb5.internal.ccache.CCacheOutputStream;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class PrincipalName
  implements Cloneable
{
  public static final int KRB_NT_UNKNOWN = 0;
  public static final int KRB_NT_PRINCIPAL = 1;
  public static final int KRB_NT_SRV_INST = 2;
  public static final int KRB_NT_SRV_HST = 3;
  public static final int KRB_NT_SRV_XHST = 4;
  public static final int KRB_NT_UID = 5;
  public static final String TGS_DEFAULT_SRV_NAME = "krbtgt";
  public static final int TGS_DEFAULT_NT = 2;
  public static final char NAME_COMPONENT_SEPARATOR = 47;
  public static final char NAME_REALM_SEPARATOR = 64;
  public static final char REALM_COMPONENT_SEPARATOR = 46;
  public static final String NAME_COMPONENT_SEPARATOR_STR = "/";
  public static final String NAME_REALM_SEPARATOR_STR = "@";
  public static final String REALM_COMPONENT_SEPARATOR_STR = ".";
  private int nameType;
  private String[] nameStrings;
  private Realm nameRealm;
  private String salt;

  protected PrincipalName()
  {
    this.salt = null;
  }

  public PrincipalName(String[] paramArrayOfString, int paramInt)
    throws IllegalArgumentException, IOException
  {
    this.salt = null;
    if (paramArrayOfString == null)
      throw new IllegalArgumentException("Null input not allowed");
    this.nameStrings = new String[paramArrayOfString.length];
    System.arraycopy(paramArrayOfString, 0, this.nameStrings, 0, paramArrayOfString.length);
    this.nameType = paramInt;
    this.nameRealm = null;
  }

  public PrincipalName(String[] paramArrayOfString)
    throws IOException
  {
    this(paramArrayOfString, 0);
  }

  public Object clone()
  {
    PrincipalName localPrincipalName = new PrincipalName();
    localPrincipalName.nameType = this.nameType;
    if (this.nameStrings != null)
    {
      localPrincipalName.nameStrings = new String[this.nameStrings.length];
      System.arraycopy(this.nameStrings, 0, localPrincipalName.nameStrings, 0, this.nameStrings.length);
    }
    if (this.nameRealm != null)
      localPrincipalName.nameRealm = ((Realm)this.nameRealm.clone());
    return localPrincipalName;
  }

  public boolean equals(Object paramObject)
  {
    if (paramObject instanceof PrincipalName)
      return equals((PrincipalName)paramObject);
    return false;
  }

  public boolean equals(PrincipalName paramPrincipalName)
  {
    if (!(equalsWithoutRealm(paramPrincipalName)))
      return false;
    if (((this.nameRealm != null) && (paramPrincipalName.nameRealm == null)) || ((this.nameRealm == null) && (paramPrincipalName.nameRealm != null)))
      return false;
    return ((this.nameRealm == null) || (paramPrincipalName.nameRealm == null) || (this.nameRealm.equals(paramPrincipalName.nameRealm)));
  }

  boolean equalsWithoutRealm(PrincipalName paramPrincipalName)
  {
    if ((this.nameType != 0) && (paramPrincipalName.nameType != 0) && (this.nameType != paramPrincipalName.nameType))
      return false;
    if (((this.nameStrings != null) && (paramPrincipalName.nameStrings == null)) || ((this.nameStrings == null) && (paramPrincipalName.nameStrings != null)))
      return false;
    if ((this.nameStrings != null) && (paramPrincipalName.nameStrings != null))
    {
      if (this.nameStrings.length != paramPrincipalName.nameStrings.length)
        return false;
      for (int i = 0; i < this.nameStrings.length; ++i)
        if (!(this.nameStrings[i].equals(paramPrincipalName.nameStrings[i])))
          return false;
    }
    return true;
  }

  public PrincipalName(DerValue paramDerValue)
    throws Asn1Exception, IOException
  {
    this.salt = null;
    this.nameRealm = null;
    if (paramDerValue == null)
      throw new IllegalArgumentException("Null input not allowed");
    if (paramDerValue.getTag() != 48)
      throw new Asn1Exception(906);
    DerValue localDerValue1 = paramDerValue.getData().getDerValue();
    if ((localDerValue1.getTag() & 0x1F) == 0)
    {
      localObject = localDerValue1.getData().getBigInteger();
      this.nameType = ((BigInteger)localObject).intValue();
    }
    else
    {
      throw new Asn1Exception(906);
    }
    localDerValue1 = paramDerValue.getData().getDerValue();
    if ((localDerValue1.getTag() & 0x1F) == 1)
    {
      localObject = localDerValue1.getData().getDerValue();
      if (((DerValue)localObject).getTag() != 48)
        throw new Asn1Exception(906);
      Vector localVector = new Vector();
      while (((DerValue)localObject).getData().available() > 0)
      {
        DerValue localDerValue2 = ((DerValue)localObject).getData().getDerValue();
        localVector.addElement(localDerValue2.getGeneralString());
      }
      if (localVector.size() > 0)
      {
        this.nameStrings = new String[localVector.size()];
        localVector.copyInto(this.nameStrings);
      }
      else
      {
        this.nameStrings = { "" };
      }
    }
    else
    {
      throw new Asn1Exception(906);
    }
  }

  public static PrincipalName parse(DerInputStream paramDerInputStream, byte paramByte, boolean paramBoolean)
    throws Asn1Exception, IOException
  {
    if ((paramBoolean) && (((byte)paramDerInputStream.peekByte() & 0x1F) != paramByte))
      return null;
    DerValue localDerValue1 = paramDerInputStream.getDerValue();
    if (paramByte != (localDerValue1.getTag() & 0x1F))
      throw new Asn1Exception(906);
    DerValue localDerValue2 = localDerValue1.getData().getDerValue();
    return new PrincipalName(localDerValue2);
  }

  protected static String[] parseName(String paramString)
  {
    String str2;
    Vector localVector = new Vector();
    String str1 = paramString;
    int i = 0;
    int j = 0;
    while (true)
    {
      while (true)
      {
        while (true)
        {
          if (i >= str1.length())
            break label213;
          if (str1.charAt(i) != '/')
            break label115;
          if ((i <= 0) || (str1.charAt(i - 1) != '\\'))
            break;
          str1 = str1.substring(0, i - 1) + str1.substring(i, str1.length());
        }
        if (j < i)
        {
          str2 = str1.substring(j, i);
          localVector.addElement(str2);
        }
        j = i + 1;
        break label207:
        label115: if (str1.charAt(i) != '@')
          break label207;
        if ((i <= 0) || (str1.charAt(i - 1) != '\\'))
          break;
        str1 = str1.substring(0, i - 1) + str1.substring(i, str1.length());
      }
      if (j < i)
      {
        str2 = str1.substring(j, i);
        localVector.addElement(str2);
      }
      j = i + 1;
      break;
      label207: ++i;
    }
    if ((i == str1.length()) && (j < i))
    {
      label213: str2 = str1.substring(j, i);
      localVector.addElement(str2);
    }
    String[] arrayOfString = new String[localVector.size()];
    localVector.copyInto(arrayOfString);
    return arrayOfString;
  }

  public PrincipalName(String paramString, int paramInt)
    throws sun.security.krb5.RealmException
  {
    this.salt = null;
    if (paramString == null)
      throw new IllegalArgumentException("Null name not allowed");
    String[] arrayOfString = parseName(paramString);
    Realm localRealm = null;
    String str1 = Realm.parseRealmAtSeparator(paramString);
    if (str1 == null)
      try
      {
        Config localConfig = Config.getInstance();
        str1 = localConfig.getDefaultRealm();
      }
      catch (KrbException localKrbException)
      {
        RealmException localRealmException = new sun.security.krb5.RealmException(localKrbException.getMessage());
        localRealmException.initCause(localKrbException);
        throw localRealmException;
      }
    if (str1 != null)
      localRealm = new Realm(str1);
    switch (paramInt)
    {
    case 3:
      if (arrayOfString.length >= 2)
        try
        {
          String str2 = InetAddress.getByName(arrayOfString[1]).getCanonicalHostName();
          arrayOfString[1] = str2.toLowerCase();
        }
        catch (UnknownHostException localUnknownHostException)
        {
          arrayOfString[1] = arrayOfString[1].toLowerCase();
        }
      this.nameStrings = arrayOfString;
      this.nameType = paramInt;
      String str3 = mapHostToRealm(arrayOfString[1]);
      if (str3 != null)
      {
        this.nameRealm = new Realm(str3);
        return;
      }
      this.nameRealm = localRealm;
      break;
    case 0:
    case 1:
    case 2:
    case 4:
    case 5:
      this.nameStrings = arrayOfString;
      this.nameType = paramInt;
      this.nameRealm = localRealm;
      break;
    default:
      throw new IllegalArgumentException("Illegal name type");
    }
  }

  public PrincipalName(String paramString)
    throws sun.security.krb5.RealmException
  {
    this(paramString, 0);
  }

  public PrincipalName(String paramString1, String paramString2)
    throws sun.security.krb5.RealmException
  {
    this(paramString1, 0);
    this.nameRealm = new Realm(paramString2);
  }

  public String getRealmAsString()
  {
    return getRealmString();
  }

  public String getPrincipalNameAsString()
  {
    StringBuffer localStringBuffer = new StringBuffer(this.nameStrings[0]);
    for (int i = 1; i < this.nameStrings.length; ++i)
      localStringBuffer.append(this.nameStrings[i]);
    return localStringBuffer.toString();
  }

  public int hashCode()
  {
    return toString().hashCode();
  }

  public String getName()
  {
    return toString();
  }

  public int getNameType()
  {
    return this.nameType;
  }

  public String[] getNameStrings()
  {
    return this.nameStrings;
  }

  public byte[][] toByteArray()
  {
    [B[] arrayOf[B = new byte[this.nameStrings.length][];
    for (int i = 0; i < this.nameStrings.length; ++i)
    {
      arrayOf[B[i] = new byte[this.nameStrings[i].length()];
      arrayOf[B[i] = this.nameStrings[i].getBytes();
    }
    return arrayOf[B;
  }

  public String getRealmString()
  {
    if (this.nameRealm != null)
      return this.nameRealm.toString();
    return null;
  }

  public Realm getRealm()
  {
    return this.nameRealm;
  }

  public void setRealm(Realm paramRealm)
    throws sun.security.krb5.RealmException
  {
    this.nameRealm = paramRealm;
  }

  public void setRealm(String paramString)
    throws sun.security.krb5.RealmException
  {
    this.nameRealm = new Realm(paramString);
  }

  public String getSalt()
  {
    if (this.salt == null)
    {
      StringBuffer localStringBuffer = new StringBuffer();
      if (this.nameRealm != null)
        localStringBuffer.append(this.nameRealm.toString());
      for (int i = 0; i < this.nameStrings.length; ++i)
        localStringBuffer.append(this.nameStrings[i]);
      return localStringBuffer.toString();
    }
    return this.salt;
  }

  public void setSalt(String paramString)
  {
    this.salt = paramString;
  }

  public String toString()
  {
    StringBuffer localStringBuffer = new StringBuffer();
    for (int i = 0; i < this.nameStrings.length; ++i)
    {
      if (i > 0)
        localStringBuffer.append("/");
      localStringBuffer.append(this.nameStrings[i]);
    }
    if (this.nameRealm != null)
    {
      localStringBuffer.append("@");
      localStringBuffer.append(this.nameRealm.toString());
    }
    return localStringBuffer.toString();
  }

  public String getNameString()
  {
    StringBuffer localStringBuffer = new StringBuffer();
    for (int i = 0; i < this.nameStrings.length; ++i)
    {
      if (i > 0)
        localStringBuffer.append("/");
      localStringBuffer.append(this.nameStrings[i]);
    }
    return localStringBuffer.toString();
  }

  public byte[] asn1Encode()
    throws Asn1Exception, IOException
  {
    DerOutputStream localDerOutputStream1 = new DerOutputStream();
    DerOutputStream localDerOutputStream2 = new DerOutputStream();
    BigInteger localBigInteger = BigInteger.valueOf(this.nameType);
    localDerOutputStream2.putInteger(localBigInteger);
    localDerOutputStream1.write(DerValue.createTag(-128, true, 0), localDerOutputStream2);
    localDerOutputStream2 = new DerOutputStream();
    DerValue[] arrayOfDerValue = new DerValue[this.nameStrings.length];
    for (int i = 0; i < this.nameStrings.length; ++i)
      arrayOfDerValue[i] = new DerValue(27, this.nameStrings[i]);
    localDerOutputStream2.putSequence(arrayOfDerValue);
    localDerOutputStream1.write(DerValue.createTag(-128, true, 1), localDerOutputStream2);
    localDerOutputStream2 = new DerOutputStream();
    localDerOutputStream2.write(48, localDerOutputStream1);
    return localDerOutputStream2.toByteArray();
  }

  public boolean match(PrincipalName paramPrincipalName)
  {
    int i = 1;
    if ((this.nameRealm != null) && (paramPrincipalName.nameRealm != null) && (!(this.nameRealm.toString().equalsIgnoreCase(paramPrincipalName.nameRealm.toString()))))
      i = 0;
    if (this.nameStrings.length != paramPrincipalName.nameStrings.length)
      i = 0;
    else
      for (int j = 0; j < this.nameStrings.length; ++j)
        if (!(this.nameStrings[j].equalsIgnoreCase(paramPrincipalName.nameStrings[j])))
          i = 0;
    return i;
  }

  public void writePrincipal(CCacheOutputStream paramCCacheOutputStream)
    throws IOException
  {
    paramCCacheOutputStream.write32(this.nameType);
    paramCCacheOutputStream.write32(this.nameStrings.length);
    if (this.nameRealm != null)
    {
      arrayOfByte = null;
      arrayOfByte = this.nameRealm.toString().getBytes();
      paramCCacheOutputStream.write32(arrayOfByte.length);
      paramCCacheOutputStream.write(arrayOfByte, 0, arrayOfByte.length);
    }
    byte[] arrayOfByte = null;
    for (int i = 0; i < this.nameStrings.length; ++i)
    {
      arrayOfByte = this.nameStrings[i].getBytes();
      paramCCacheOutputStream.write32(arrayOfByte.length);
      paramCCacheOutputStream.write(arrayOfByte, 0, arrayOfByte.length);
    }
  }

  protected PrincipalName(String paramString1, String paramString2, String paramString3, int paramInt)
    throws KrbException
  {
    this.salt = null;
    if (paramInt != 2)
      throw new KrbException(60, "Bad name type");
    String[] arrayOfString = new String[2];
    arrayOfString[0] = paramString1;
    arrayOfString[1] = paramString2;
    this.nameStrings = arrayOfString;
    this.nameRealm = new Realm(paramString3);
    this.nameType = paramInt;
  }

  public String getInstanceComponent()
  {
    if ((this.nameStrings != null) && (this.nameStrings.length >= 2))
      return new String(this.nameStrings[1]);
    return null;
  }

  static String mapHostToRealm(String paramString)
  {
    String str1 = null;
    try
    {
      String str2 = null;
      Config localConfig = Config.getInstance();
      if ((str1 = localConfig.getDefault(paramString, "domain_realm")) != null)
        return str1;
      for (int i = 1; i < paramString.length(); ++i)
        if ((paramString.charAt(i) == '.') && (i != paramString.length() - 1))
        {
          str2 = paramString.substring(i);
          str1 = localConfig.getDefault(str2, "domain_realm");
          if (str1 != null)
            break;
          str2 = paramString.substring(i + 1);
          str1 = localConfig.getDefault(str2, "domain_realm");
          if (str1 != null)
            break;
        }
    }
    catch (KrbException localKrbException)
    {
    }
    return str1;
  }
}