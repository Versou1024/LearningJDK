package sun.security.x509;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import sun.security.pkcs.PKCS9Attribute;
import sun.security.util.ObjectIdentifier;

class AVAKeyword
{
  private static final Map<ObjectIdentifier, AVAKeyword> oidMap = new HashMap();
  private static final Map<String, AVAKeyword> keywordMap = new HashMap();
  private String keyword;
  private ObjectIdentifier oid;
  private boolean rfc1779Compliant;
  private boolean rfc2253Compliant;

  private AVAKeyword(String paramString, ObjectIdentifier paramObjectIdentifier, boolean paramBoolean1, boolean paramBoolean2)
  {
    this.keyword = paramString;
    this.oid = paramObjectIdentifier;
    this.rfc1779Compliant = paramBoolean1;
    this.rfc2253Compliant = paramBoolean2;
    oidMap.put(paramObjectIdentifier, this);
    keywordMap.put(paramString, this);
  }

  private boolean isCompliant(int paramInt)
  {
    switch (paramInt)
    {
    case 2:
      return this.rfc1779Compliant;
    case 3:
      return this.rfc2253Compliant;
    case 1:
      return true;
    }
    throw new IllegalArgumentException("Invalid standard " + paramInt);
  }

  static ObjectIdentifier getOID(String paramString, int paramInt)
    throws IOException
  {
    return getOID(paramString, paramInt, Collections.EMPTY_MAP);
  }

  static ObjectIdentifier getOID(String paramString, int paramInt, Map<String, String> paramMap)
    throws IOException
  {
    paramString = paramString.toUpperCase();
    if (paramInt == 3)
    {
      if ((!(paramString.startsWith(" "))) && (!(paramString.endsWith(" "))))
        break label65;
      throw new IOException("Invalid leading or trailing space in keyword \"" + paramString + "\"");
    }
    paramString = paramString.trim();
    label65: String str = (String)paramMap.get(paramString);
    if (str == null)
    {
      AVAKeyword localAVAKeyword = (AVAKeyword)keywordMap.get(paramString);
      if ((localAVAKeyword != null) && (localAVAKeyword.isCompliant(paramInt)))
        return localAVAKeyword.oid;
    }
    else
    {
      return new ObjectIdentifier(str);
    }
    if (paramInt == 2)
    {
      if (!(paramString.startsWith("OID.")))
        throw new IOException("Invalid RFC1779 keyword: " + paramString);
      paramString = paramString.substring(4);
    }
    else if ((paramInt == 1) && (paramString.startsWith("OID.")))
    {
      paramString = paramString.substring(4);
    }
    int i = 0;
    if (paramString.length() != 0)
    {
      int j = paramString.charAt(0);
      if ((j >= 48) && (j <= 57))
        i = 1;
    }
    if (i == 0)
      throw new IOException("Invalid keyword \"" + paramString + "\"");
    return new ObjectIdentifier(paramString);
  }

  static String getKeyword(ObjectIdentifier paramObjectIdentifier, int paramInt)
  {
    return getKeyword(paramObjectIdentifier, paramInt, Collections.EMPTY_MAP);
  }

  static String getKeyword(ObjectIdentifier paramObjectIdentifier, int paramInt, Map<String, String> paramMap)
  {
    String str1 = paramObjectIdentifier.toString();
    String str2 = (String)paramMap.get(str1);
    if (str2 == null)
    {
      AVAKeyword localAVAKeyword = (AVAKeyword)oidMap.get(paramObjectIdentifier);
      if ((localAVAKeyword != null) && (localAVAKeyword.isCompliant(paramInt)))
        return localAVAKeyword.keyword;
    }
    else
    {
      if (str2.length() == 0)
        throw new IllegalArgumentException("keyword cannot be empty");
      str2 = str2.trim();
      int i = str2.charAt(0);
      if ((i < 65) || (i > 122) || ((i > 90) && (i < 97)))
        throw new IllegalArgumentException("keyword does not start with letter");
      for (int j = 1; j < str2.length(); ++j)
      {
        i = str2.charAt(j);
        if ((((i < 65) || (i > 122) || ((i > 90) && (i < 97)))) && (((i < 48) || (i > 57))) && (i != 95))
          throw new IllegalArgumentException("keyword character is not a letter, digit, or underscore");
      }
      return str2;
    }
    if (paramInt == 3)
      return str1;
    return "OID." + str1;
  }

  static boolean hasKeyword(ObjectIdentifier paramObjectIdentifier, int paramInt)
  {
    AVAKeyword localAVAKeyword = (AVAKeyword)oidMap.get(paramObjectIdentifier);
    if (localAVAKeyword == null)
      return false;
    return localAVAKeyword.isCompliant(paramInt);
  }

  static
  {
    new AVAKeyword("CN", X500Name.commonName_oid, true, true);
    new AVAKeyword("C", X500Name.countryName_oid, true, true);
    new AVAKeyword("L", X500Name.localityName_oid, true, true);
    new AVAKeyword("S", X500Name.stateName_oid, false, false);
    new AVAKeyword("ST", X500Name.stateName_oid, true, true);
    new AVAKeyword("O", X500Name.orgName_oid, true, true);
    new AVAKeyword("OU", X500Name.orgUnitName_oid, true, true);
    new AVAKeyword("T", X500Name.title_oid, false, false);
    new AVAKeyword("IP", X500Name.ipAddress_oid, false, false);
    new AVAKeyword("STREET", X500Name.streetAddress_oid, true, true);
    new AVAKeyword("DC", X500Name.DOMAIN_COMPONENT_OID, false, true);
    new AVAKeyword("DNQUALIFIER", X500Name.DNQUALIFIER_OID, false, false);
    new AVAKeyword("DNQ", X500Name.DNQUALIFIER_OID, false, false);
    new AVAKeyword("SURNAME", X500Name.SURNAME_OID, false, false);
    new AVAKeyword("GIVENNAME", X500Name.GIVENNAME_OID, false, false);
    new AVAKeyword("INITIALS", X500Name.INITIALS_OID, false, false);
    new AVAKeyword("GENERATION", X500Name.GENERATIONQUALIFIER_OID, false, false);
    new AVAKeyword("EMAIL", PKCS9Attribute.EMAIL_ADDRESS_OID, false, false);
    new AVAKeyword("EMAILADDRESS", PKCS9Attribute.EMAIL_ADDRESS_OID, false, false);
    new AVAKeyword("UID", X500Name.userid_oid, false, true);
    new AVAKeyword("SERIALNUMBER", X500Name.SERIALNUMBER_OID, false, false);
  }
}