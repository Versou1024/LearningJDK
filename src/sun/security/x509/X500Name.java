package sun.security.x509;

import B;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.Principal;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.List<Lsun.security.x509.AVA;>;
import java.util.Map;
import javax.security.auth.x500.X500Principal;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;

public class X500Name
  implements GeneralNameInterface, Principal
{
  private String dn;
  private String rfc1779Dn;
  private String rfc2253Dn;
  private String canonicalDn;
  private RDN[] names;
  private X500Principal x500Principal;
  private byte[] encoded;
  private volatile List<RDN> rdnList;
  private volatile List<AVA> allAvaList;
  private static final Map<ObjectIdentifier, ObjectIdentifier> internedOIDs = new HashMap();
  private static final int[] commonName_data = { 2, 5, 4, 3 };
  private static final int[] SURNAME_DATA = { 2, 5, 4, 4 };
  private static final int[] SERIALNUMBER_DATA = { 2, 5, 4, 5 };
  private static final int[] countryName_data = { 2, 5, 4, 6 };
  private static final int[] localityName_data = { 2, 5, 4, 7 };
  private static final int[] stateName_data = { 2, 5, 4, 8 };
  private static final int[] streetAddress_data = { 2, 5, 4, 9 };
  private static final int[] orgName_data = { 2, 5, 4, 10 };
  private static final int[] orgUnitName_data = { 2, 5, 4, 11 };
  private static final int[] title_data = { 2, 5, 4, 12 };
  private static final int[] GIVENNAME_DATA = { 2, 5, 4, 42 };
  private static final int[] INITIALS_DATA = { 2, 5, 4, 43 };
  private static final int[] GENERATIONQUALIFIER_DATA = { 2, 5, 4, 44 };
  private static final int[] DNQUALIFIER_DATA = { 2, 5, 4, 46 };
  private static final int[] ipAddress_data = { 1, 3, 6, 1, 4, 1, 42, 2, 11, 2, 1 };
  private static final int[] DOMAIN_COMPONENT_DATA = { 0, 9, 2342, 19200300, 100, 1, 25 };
  private static final int[] userid_data = { 0, 9, 2342, 19200300, 100, 1, 1 };
  public static final ObjectIdentifier commonName_oid = intern(ObjectIdentifier.newInternal(commonName_data));
  public static final ObjectIdentifier countryName_oid;
  public static final ObjectIdentifier localityName_oid;
  public static final ObjectIdentifier orgName_oid;
  public static final ObjectIdentifier orgUnitName_oid;
  public static final ObjectIdentifier stateName_oid;
  public static final ObjectIdentifier streetAddress_oid;
  public static final ObjectIdentifier title_oid;
  public static final ObjectIdentifier DNQUALIFIER_OID;
  public static final ObjectIdentifier SURNAME_OID;
  public static final ObjectIdentifier GIVENNAME_OID;
  public static final ObjectIdentifier INITIALS_OID;
  public static final ObjectIdentifier GENERATIONQUALIFIER_OID;
  public static final ObjectIdentifier ipAddress_oid;
  public static final ObjectIdentifier DOMAIN_COMPONENT_OID;
  public static final ObjectIdentifier userid_oid;
  public static final ObjectIdentifier SERIALNUMBER_OID = intern(ObjectIdentifier.newInternal(SERIALNUMBER_DATA));
  private static final Constructor principalConstructor;
  private static final Field principalField;

  public X500Name(String paramString)
    throws IOException
  {
    this(paramString, Collections.EMPTY_MAP);
  }

  public X500Name(String paramString, Map<String, String> paramMap)
    throws IOException
  {
    parseDN(paramString, paramMap);
  }

  public X500Name(String paramString1, String paramString2)
    throws IOException
  {
    if (paramString1 == null)
      throw new NullPointerException("Name must not be null");
    if (paramString2.equalsIgnoreCase("RFC2253"))
      parseRFC2253DN(paramString1);
    else if (paramString2.equalsIgnoreCase("DEFAULT"))
      parseDN(paramString1, Collections.EMPTY_MAP);
    else
      throw new IOException("Unsupported format " + paramString2);
  }

  public X500Name(String paramString1, String paramString2, String paramString3, String paramString4)
    throws IOException
  {
    this.names = new RDN[4];
    this.names[3] = new RDN(1);
    this.names[3].assertion[0] = new AVA(commonName_oid, new DerValue(paramString1));
    this.names[2] = new RDN(1);
    this.names[2].assertion[0] = new AVA(orgUnitName_oid, new DerValue(paramString2));
    this.names[1] = new RDN(1);
    this.names[1].assertion[0] = new AVA(orgName_oid, new DerValue(paramString3));
    this.names[0] = new RDN(1);
    this.names[0].assertion[0] = new AVA(countryName_oid, new DerValue(paramString4));
  }

  public X500Name(String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, String paramString6)
    throws IOException
  {
    this.names = new RDN[6];
    this.names[5] = new RDN(1);
    this.names[5].assertion[0] = new AVA(commonName_oid, new DerValue(paramString1));
    this.names[4] = new RDN(1);
    this.names[4].assertion[0] = new AVA(orgUnitName_oid, new DerValue(paramString2));
    this.names[3] = new RDN(1);
    this.names[3].assertion[0] = new AVA(orgName_oid, new DerValue(paramString3));
    this.names[2] = new RDN(1);
    this.names[2].assertion[0] = new AVA(localityName_oid, new DerValue(paramString4));
    this.names[1] = new RDN(1);
    this.names[1].assertion[0] = new AVA(stateName_oid, new DerValue(paramString5));
    this.names[0] = new RDN(1);
    this.names[0].assertion[0] = new AVA(countryName_oid, new DerValue(paramString6));
  }

  public X500Name(RDN[] paramArrayOfRDN)
    throws IOException
  {
    if (paramArrayOfRDN == null)
    {
      this.names = new RDN[0];
    }
    else
    {
      this.names = ((RDN[])(RDN[])paramArrayOfRDN.clone());
      for (int i = 0; i < this.names.length; ++i)
        if (this.names[i] == null)
          throw new IOException("Cannot create an X500Name");
    }
  }

  public X500Name(DerValue paramDerValue)
    throws IOException
  {
    this(paramDerValue.toDerInputStream());
  }

  public X500Name(DerInputStream paramDerInputStream)
    throws IOException
  {
    parseDER(paramDerInputStream);
  }

  public X500Name(byte[] paramArrayOfByte)
    throws IOException
  {
    DerInputStream localDerInputStream = new DerInputStream(paramArrayOfByte);
    parseDER(localDerInputStream);
  }

  public List<RDN> rdns()
  {
    List localList = this.rdnList;
    if (localList == null)
    {
      localList = Collections.unmodifiableList(Arrays.asList(this.names));
      this.rdnList = localList;
    }
    return localList;
  }

  public int size()
  {
    return this.names.length;
  }

  public List<AVA> allAvas()
  {
    Object localObject = this.allAvaList;
    if (localObject == null)
    {
      localObject = new ArrayList();
      for (int i = 0; i < this.names.length; ++i)
        ((List)localObject).addAll(this.names[i].avas());
    }
    return ((List<AVA>)localObject);
  }

  public int avaSize()
  {
    return allAvas().size();
  }

  public boolean isEmpty()
  {
    int i = this.names.length;
    if (i == 0)
      return true;
    for (int j = 0; j < i; ++j)
      if (this.names[j].assertion.length != 0)
        return false;
    return true;
  }

  public int hashCode()
  {
    return getRFC2253CanonicalName().hashCode();
  }

  public boolean equals(Object paramObject)
  {
    if (this == paramObject)
      return true;
    if (!(paramObject instanceof X500Name))
      return false;
    X500Name localX500Name = (X500Name)paramObject;
    if ((this.canonicalDn != null) && (localX500Name.canonicalDn != null))
      return this.canonicalDn.equals(localX500Name.canonicalDn);
    int i = this.names.length;
    if (i != localX500Name.names.length)
      return false;
    for (int j = 0; j < i; ++j)
    {
      localObject = this.names[j];
      RDN localRDN = localX500Name.names[j];
      if (((RDN)localObject).assertion.length != localRDN.assertion.length)
        return false;
    }
    String str = getRFC2253CanonicalName();
    Object localObject = localX500Name.getRFC2253CanonicalName();
    return str.equals(localObject);
  }

  private String getString(DerValue paramDerValue)
    throws IOException
  {
    if (paramDerValue == null)
      return null;
    String str = paramDerValue.getAsString();
    if (str == null)
      throw new IOException("not a DER string encoding, " + paramDerValue.tag);
    return str;
  }

  public int getType()
  {
    return 4;
  }

  public String getCountry()
    throws IOException
  {
    DerValue localDerValue = findAttribute(countryName_oid);
    return getString(localDerValue);
  }

  public String getOrganization()
    throws IOException
  {
    DerValue localDerValue = findAttribute(orgName_oid);
    return getString(localDerValue);
  }

  public String getOrganizationalUnit()
    throws IOException
  {
    DerValue localDerValue = findAttribute(orgUnitName_oid);
    return getString(localDerValue);
  }

  public String getCommonName()
    throws IOException
  {
    DerValue localDerValue = findAttribute(commonName_oid);
    return getString(localDerValue);
  }

  public String getLocality()
    throws IOException
  {
    DerValue localDerValue = findAttribute(localityName_oid);
    return getString(localDerValue);
  }

  public String getState()
    throws IOException
  {
    DerValue localDerValue = findAttribute(stateName_oid);
    return getString(localDerValue);
  }

  public String getDomain()
    throws IOException
  {
    DerValue localDerValue = findAttribute(DOMAIN_COMPONENT_OID);
    return getString(localDerValue);
  }

  public String getDNQualifier()
    throws IOException
  {
    DerValue localDerValue = findAttribute(DNQUALIFIER_OID);
    return getString(localDerValue);
  }

  public String getSurname()
    throws IOException
  {
    DerValue localDerValue = findAttribute(SURNAME_OID);
    return getString(localDerValue);
  }

  public String getGivenName()
    throws IOException
  {
    DerValue localDerValue = findAttribute(GIVENNAME_OID);
    return getString(localDerValue);
  }

  public String getInitials()
    throws IOException
  {
    DerValue localDerValue = findAttribute(INITIALS_OID);
    return getString(localDerValue);
  }

  public String getGeneration()
    throws IOException
  {
    DerValue localDerValue = findAttribute(GENERATIONQUALIFIER_OID);
    return getString(localDerValue);
  }

  public String getIP()
    throws IOException
  {
    DerValue localDerValue = findAttribute(ipAddress_oid);
    return getString(localDerValue);
  }

  public String toString()
  {
    if (this.dn == null)
      generateDN();
    return this.dn;
  }

  public String getRFC1779Name()
  {
    return getRFC1779Name(Collections.EMPTY_MAP);
  }

  public String getRFC1779Name(Map<String, String> paramMap)
    throws IllegalArgumentException
  {
    if (paramMap.isEmpty())
    {
      if (this.rfc1779Dn != null)
        return this.rfc1779Dn;
      this.rfc1779Dn = generateRFC1779DN(paramMap);
      return this.rfc1779Dn;
    }
    return generateRFC1779DN(paramMap);
  }

  public String getRFC2253Name()
  {
    return getRFC2253Name(Collections.EMPTY_MAP);
  }

  public String getRFC2253Name(Map<String, String> paramMap)
  {
    if (paramMap.isEmpty())
    {
      if (this.rfc2253Dn != null)
        return this.rfc2253Dn;
      this.rfc2253Dn = generateRFC2253DN(paramMap);
      return this.rfc2253Dn;
    }
    return generateRFC2253DN(paramMap);
  }

  private String generateRFC2253DN(Map<String, String> paramMap)
  {
    if (this.names.length == 0)
      return "";
    StringBuilder localStringBuilder = new StringBuilder(48);
    for (int i = this.names.length - 1; i >= 0; --i)
    {
      if (i < this.names.length - 1)
        localStringBuilder.append(',');
      localStringBuilder.append(this.names[i].toRFC2253String(paramMap));
    }
    return localStringBuilder.toString();
  }

  public String getRFC2253CanonicalName()
  {
    if (this.canonicalDn != null)
      return this.canonicalDn;
    if (this.names.length == 0)
    {
      this.canonicalDn = "";
      return this.canonicalDn;
    }
    StringBuilder localStringBuilder = new StringBuilder(48);
    for (int i = this.names.length - 1; i >= 0; --i)
    {
      if (i < this.names.length - 1)
        localStringBuilder.append(',');
      localStringBuilder.append(this.names[i].toRFC2253String(true));
    }
    this.canonicalDn = localStringBuilder.toString();
    return this.canonicalDn;
  }

  public String getName()
  {
    return toString();
  }

  private DerValue findAttribute(ObjectIdentifier paramObjectIdentifier)
  {
    if (this.names != null)
      for (int i = 0; i < this.names.length; ++i)
      {
        DerValue localDerValue = this.names[i].findAttribute(paramObjectIdentifier);
        if (localDerValue != null)
          return localDerValue;
      }
    return null;
  }

  public DerValue findMostSpecificAttribute(ObjectIdentifier paramObjectIdentifier)
  {
    if (this.names != null)
      for (int i = this.names.length - 1; i >= 0; --i)
      {
        DerValue localDerValue = this.names[i].findAttribute(paramObjectIdentifier);
        if (localDerValue != null)
          return localDerValue;
      }
    return null;
  }

  private void parseDER(DerInputStream paramDerInputStream)
    throws IOException
  {
    DerValue[] arrayOfDerValue = null;
    byte[] arrayOfByte = paramDerInputStream.toByteArray();
    try
    {
      arrayOfDerValue = paramDerInputStream.getSequence(5);
    }
    catch (IOException localIOException)
    {
      if (arrayOfByte == null)
      {
        arrayOfDerValue = null;
      }
      else
      {
        DerValue localDerValue = new DerValue(48, arrayOfByte);
        arrayOfByte = localDerValue.toByteArray();
        arrayOfDerValue = new DerInputStream(arrayOfByte).getSequence(5);
      }
    }
    if (arrayOfDerValue == null)
    {
      this.names = new RDN[0];
    }
    else
    {
      this.names = new RDN[arrayOfDerValue.length];
      for (int i = 0; i < arrayOfDerValue.length; ++i)
        this.names[i] = new RDN(arrayOfDerValue[i]);
    }
  }

  @Deprecated
  public void emit(DerOutputStream paramDerOutputStream)
    throws IOException
  {
    encode(paramDerOutputStream);
  }

  public void encode(DerOutputStream paramDerOutputStream)
    throws IOException
  {
    DerOutputStream localDerOutputStream = new DerOutputStream();
    for (int i = 0; i < this.names.length; ++i)
      this.names[i].encode(localDerOutputStream);
    paramDerOutputStream.write(48, localDerOutputStream);
  }

  public byte[] getEncodedInternal()
    throws IOException
  {
    if (this.encoded == null)
    {
      DerOutputStream localDerOutputStream1 = new DerOutputStream();
      DerOutputStream localDerOutputStream2 = new DerOutputStream();
      for (int i = 0; i < this.names.length; ++i)
        this.names[i].encode(localDerOutputStream2);
      localDerOutputStream1.write(48, localDerOutputStream2);
      this.encoded = localDerOutputStream1.toByteArray();
    }
    return this.encoded;
  }

  public byte[] getEncoded()
    throws IOException
  {
    return ((byte[])(byte[])getEncodedInternal().clone());
  }

  private void parseDN(String paramString, Map<String, String> paramMap)
    throws IOException
  {
    if ((paramString == null) || (paramString.length() == 0))
    {
      this.names = new RDN[0];
      return;
    }
    ArrayList localArrayList = new ArrayList();
    int i = 0;
    int k = 0;
    String str2 = paramString;
    int l = 0;
    int i1 = str2.indexOf(44);
    for (int i2 = str2.indexOf(59); (i1 >= 0) || (i2 >= 0); i2 = str2.indexOf(59, l))
    {
      int j;
      if (i2 < 0)
        j = i1;
      else if (i1 < 0)
        j = i2;
      else
        j = Math.min(i1, i2);
      k += countQuotes(str2, l, j);
      if ((j >= 0) && (k != 1) && (!(escaped(j, l, str2))))
      {
        str1 = str2.substring(i, j);
        localRDN = new RDN(str1, paramMap);
        localArrayList.add(localRDN);
        i = j + 1;
        k = 0;
      }
      l = j + 1;
      i1 = str2.indexOf(44, l);
    }
    String str1 = str2.substring(i);
    RDN localRDN = new RDN(str1, paramMap);
    localArrayList.add(localRDN);
    Collections.reverse(localArrayList);
    this.names = ((RDN[])localArrayList.toArray(new RDN[localArrayList.size()]));
  }

  private void parseRFC2253DN(String paramString)
    throws IOException
  {
    if (paramString.length() == 0)
    {
      this.names = new RDN[0];
      return;
    }
    ArrayList localArrayList = new ArrayList();
    int i = 0;
    int j = 0;
    for (int k = paramString.indexOf(44); k >= 0; k = paramString.indexOf(44, j))
    {
      if ((k > 0) && (!(escaped(k, j, paramString))))
      {
        str = paramString.substring(i, k);
        localRDN = new RDN(str, "RFC2253");
        localArrayList.add(localRDN);
        i = k + 1;
      }
      j = k + 1;
    }
    String str = paramString.substring(i);
    RDN localRDN = new RDN(str, "RFC2253");
    localArrayList.add(localRDN);
    Collections.reverse(localArrayList);
    this.names = ((RDN[])localArrayList.toArray(new RDN[localArrayList.size()]));
  }

  static int countQuotes(String paramString, int paramInt1, int paramInt2)
  {
    int i = 0;
    for (int j = paramInt1; j < paramInt2; ++j)
      if (((paramString.charAt(j) == '"') && (j == paramInt1)) || ((paramString.charAt(j) == '"') && (paramString.charAt(j - 1) != '\\')))
        ++i;
    return i;
  }

  private static boolean escaped(int paramInt1, int paramInt2, String paramString)
  {
    if ((paramInt1 == 1) && (paramString.charAt(paramInt1 - 1) == '\\'))
      return true;
    if ((paramInt1 > 1) && (paramString.charAt(paramInt1 - 1) == '\\') && (paramString.charAt(paramInt1 - 2) != '\\'))
      return true;
    if ((paramInt1 > 1) && (paramString.charAt(paramInt1 - 1) == '\\') && (paramString.charAt(paramInt1 - 2) == '\\'))
    {
      int i = 0;
      --paramInt1;
      while (paramInt1 >= paramInt2)
      {
        if (paramString.charAt(paramInt1) == '\\')
          ++i;
        --paramInt1;
      }
      return (i % 2 != 0);
    }
    return false;
  }

  private void generateDN()
  {
    if (this.names.length == 1)
    {
      this.dn = this.names[0].toString();
      return;
    }
    StringBuilder localStringBuilder = new StringBuilder(48);
    if (this.names != null)
      for (int i = this.names.length - 1; i >= 0; --i)
      {
        if (i != this.names.length - 1)
          localStringBuilder.append(", ");
        localStringBuilder.append(this.names[i].toString());
      }
    this.dn = localStringBuilder.toString();
  }

  private String generateRFC1779DN(Map<String, String> paramMap)
  {
    if (this.names.length == 1)
      return this.names[0].toRFC1779String(paramMap);
    StringBuilder localStringBuilder = new StringBuilder(48);
    if (this.names != null)
      for (int i = this.names.length - 1; i >= 0; --i)
      {
        if (i != this.names.length - 1)
          localStringBuilder.append(", ");
        localStringBuilder.append(this.names[i].toRFC1779String(paramMap));
      }
    return localStringBuilder.toString();
  }

  static ObjectIdentifier intern(ObjectIdentifier paramObjectIdentifier)
  {
    ObjectIdentifier localObjectIdentifier = (ObjectIdentifier)internedOIDs.get(paramObjectIdentifier);
    if (localObjectIdentifier != null)
      return localObjectIdentifier;
    internedOIDs.put(paramObjectIdentifier, paramObjectIdentifier);
    return paramObjectIdentifier;
  }

  public int constrains(GeneralNameInterface paramGeneralNameInterface)
    throws UnsupportedOperationException
  {
    int i;
    if (paramGeneralNameInterface == null)
    {
      i = -1;
    }
    else if (paramGeneralNameInterface.getType() != 4)
    {
      i = -1;
    }
    else
    {
      X500Name localX500Name = (X500Name)paramGeneralNameInterface;
      if (localX500Name.equals(this))
        i = 0;
      else if (localX500Name.names.length == 0)
        i = 2;
      else if (this.names.length == 0)
        i = 1;
      else if (localX500Name.isWithinSubtree(this))
        i = 1;
      else if (isWithinSubtree(localX500Name))
        i = 2;
      else
        i = 3;
    }
    return i;
  }

  private boolean isWithinSubtree(X500Name paramX500Name)
  {
    if (this == paramX500Name)
      return true;
    if (paramX500Name == null)
      return false;
    if (paramX500Name.names.length == 0)
      return true;
    if (this.names.length == 0)
      return false;
    if (this.names.length < paramX500Name.names.length)
      return false;
    for (int i = 0; i < paramX500Name.names.length; ++i)
      if (!(this.names[i].equals(paramX500Name.names[i])))
        return false;
    return true;
  }

  public int subtreeDepth()
    throws UnsupportedOperationException
  {
    return this.names.length;
  }

  public X500Name commonAncestor(X500Name paramX500Name)
  {
    if (paramX500Name == null)
      return null;
    int i = paramX500Name.names.length;
    int j = this.names.length;
    if ((j == 0) || (i == 0))
      return null;
    int k = (j < i) ? j : i;
    for (int l = 0; l < k; ++l)
      if (!(this.names[l].equals(paramX500Name.names[l])))
      {
        if (l != 0)
          break;
        return null;
      }
    RDN[] arrayOfRDN = new RDN[l];
    for (int i1 = 0; i1 < l; ++i1)
      arrayOfRDN[i1] = this.names[i1];
    X500Name localX500Name = null;
    try
    {
      localX500Name = new X500Name(arrayOfRDN);
    }
    catch (IOException localIOException)
    {
      return null;
    }
    return localX500Name;
  }

  public X500Principal asX500Principal()
  {
    if (this.x500Principal == null)
      try
      {
        Object[] arrayOfObject = { this };
        this.x500Principal = ((X500Principal)principalConstructor.newInstance(arrayOfObject));
      }
      catch (Exception localException)
      {
        throw new RuntimeException("Unexpected exception", localException);
      }
    return this.x500Principal;
  }

  public static X500Name asX500Name(X500Principal paramX500Principal)
  {
    X500Name localX500Name;
    try
    {
      localX500Name = (X500Name)principalField.get(paramX500Principal);
      localX500Name.x500Principal = paramX500Principal;
      return localX500Name;
    }
    catch (Exception localException)
    {
      throw new RuntimeException("Unexpected exception", localException);
    }
  }

  static
  {
    countryName_oid = intern(ObjectIdentifier.newInternal(countryName_data));
    localityName_oid = intern(ObjectIdentifier.newInternal(localityName_data));
    orgName_oid = intern(ObjectIdentifier.newInternal(orgName_data));
    orgUnitName_oid = intern(ObjectIdentifier.newInternal(orgUnitName_data));
    stateName_oid = intern(ObjectIdentifier.newInternal(stateName_data));
    streetAddress_oid = intern(ObjectIdentifier.newInternal(streetAddress_data));
    title_oid = intern(ObjectIdentifier.newInternal(title_data));
    DNQUALIFIER_OID = intern(ObjectIdentifier.newInternal(DNQUALIFIER_DATA));
    SURNAME_OID = intern(ObjectIdentifier.newInternal(SURNAME_DATA));
    GIVENNAME_OID = intern(ObjectIdentifier.newInternal(GIVENNAME_DATA));
    INITIALS_OID = intern(ObjectIdentifier.newInternal(INITIALS_DATA));
    GENERATIONQUALIFIER_OID = intern(ObjectIdentifier.newInternal(GENERATIONQUALIFIER_DATA));
    ipAddress_oid = intern(ObjectIdentifier.newInternal(ipAddress_data));
    DOMAIN_COMPONENT_OID = intern(ObjectIdentifier.newInternal(DOMAIN_COMPONENT_DATA));
    userid_oid = intern(ObjectIdentifier.newInternal(userid_data));
    1 local1 = new PrivilegedExceptionAction()
    {
      public Object run()
        throws Exception
      {
        X500Principal localX500Principal = X500Principal.class;
        Class[] arrayOfClass = { X500Name.class };
        Constructor localConstructor = localX500Principal.getDeclaredConstructor(arrayOfClass);
        localConstructor.setAccessible(true);
        Field localField = localX500Principal.getDeclaredField("thisX500Name");
        localField.setAccessible(true);
        return { localConstructor, localField };
      }
    };
    try
    {
      Object[] arrayOfObject = (Object[])(Object[])AccessController.doPrivileged(local1);
      principalConstructor = (Constructor)arrayOfObject[0];
      principalField = (Field)arrayOfObject[1];
    }
    catch (Exception localException)
    {
      throw ((InternalError)new InternalError("Could not obtain X500Principal access").initCause(localException));
    }
  }
}