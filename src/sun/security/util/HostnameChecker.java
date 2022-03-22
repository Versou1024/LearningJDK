package sun.security.util;

import java.io.IOException;
import java.security.Principal;
import java.security.cert.CertificateException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import javax.security.auth.kerberos.KerberosPrincipal;
import javax.security.auth.x500.X500Principal;
import sun.net.util.IPAddressUtil;
import sun.security.krb5.PrincipalName;
import sun.security.x509.X500Name;

public class HostnameChecker
{
  public static final byte TYPE_TLS = 1;
  private static final HostnameChecker INSTANCE_TLS = new HostnameChecker(1);
  public static final byte TYPE_LDAP = 2;
  private static final HostnameChecker INSTANCE_LDAP = new HostnameChecker(2);
  private static final int ALTNAME_DNS = 2;
  private static final int ALTNAME_IP = 7;
  private final byte checkType;

  private HostnameChecker(byte paramByte)
  {
    this.checkType = paramByte;
  }

  public static HostnameChecker getInstance(byte paramByte)
  {
    if (paramByte == 1)
      return INSTANCE_TLS;
    if (paramByte == 2)
      return INSTANCE_LDAP;
    throw new IllegalArgumentException("Unknown check type: " + paramByte);
  }

  public void match(String paramString, X509Certificate paramX509Certificate)
    throws CertificateException
  {
    if (isIpAddress(paramString))
      matchIP(paramString, paramX509Certificate);
    else
      matchDNS(paramString, paramX509Certificate);
  }

  public static boolean match(String paramString, KerberosPrincipal paramKerberosPrincipal)
  {
    String str = getServerName(paramKerberosPrincipal);
    return paramString.equalsIgnoreCase(str);
  }

  public static String getServerName(KerberosPrincipal paramKerberosPrincipal)
  {
    if (paramKerberosPrincipal == null)
      return null;
    Object localObject = null;
    try
    {
      PrincipalName localPrincipalName = new PrincipalName(paramKerberosPrincipal.getName(), 3);
      String[] arrayOfString = localPrincipalName.getNameStrings();
      if (arrayOfString.length >= 2)
        localObject = arrayOfString[1];
    }
    catch (Exception localException)
    {
    }
    return localObject;
  }

  private static boolean isIpAddress(String paramString)
  {
    return ((IPAddressUtil.isIPv4LiteralAddress(paramString)) || (IPAddressUtil.isIPv6LiteralAddress(paramString)));
  }

  private static void matchIP(String paramString, X509Certificate paramX509Certificate)
    throws CertificateException
  {
    Collection localCollection = paramX509Certificate.getSubjectAlternativeNames();
    if (localCollection == null)
      throw new CertificateException("No subject alternative names present");
    Iterator localIterator = localCollection.iterator();
    while (localIterator.hasNext())
    {
      List localList = (List)localIterator.next();
      if (((Integer)localList.get(0)).intValue() == 7)
      {
        String str = (String)localList.get(1);
        if (paramString.equalsIgnoreCase(str))
          return;
      }
    }
    throw new CertificateException("No subject alternative names matching IP address " + paramString + " found");
  }

  private void matchDNS(String paramString, X509Certificate paramX509Certificate)
    throws CertificateException
  {
    Collection localCollection = paramX509Certificate.getSubjectAlternativeNames();
    if (localCollection != null)
    {
      int i = 0;
      localObject = localCollection.iterator();
      while (((Iterator)localObject).hasNext())
      {
        List localList = (List)((Iterator)localObject).next();
        if (((Integer)localList.get(0)).intValue() == 2)
        {
          i = 1;
          String str2 = (String)localList.get(1);
          if (isMatched(paramString, str2))
            return;
        }
      }
      if (i != 0)
        throw new CertificateException("No subject alternative DNS name matching " + paramString + " found.");
    }
    X500Name localX500Name = getSubjectX500Name(paramX509Certificate);
    Object localObject = localX500Name.findMostSpecificAttribute(X500Name.commonName_oid);
    if (localObject != null)
      try
      {
        if (isMatched(paramString, ((DerValue)localObject).getAsString()))
          return;
      }
      catch (IOException localIOException)
      {
      }
    String str1 = "No name matching " + paramString + " found";
    throw new CertificateException(str1);
  }

  public static X500Name getSubjectX500Name(X509Certificate paramX509Certificate)
    throws CertificateParsingException
  {
    Principal localPrincipal;
    try
    {
      localPrincipal = paramX509Certificate.getSubjectDN();
      if (localPrincipal instanceof X500Name)
        return ((X500Name)localPrincipal);
      X500Principal localX500Principal = paramX509Certificate.getSubjectX500Principal();
      return new X500Name(localX500Principal.getEncoded());
    }
    catch (IOException localIOException)
    {
      throw ((CertificateParsingException)new CertificateParsingException().initCause(localIOException));
    }
  }

  private boolean isMatched(String paramString1, String paramString2)
  {
    if (this.checkType == 1)
      return matchAllWildcards(paramString1, paramString2);
    if (this.checkType == 2)
      return matchLeftmostWildcard(paramString1, paramString2);
    return false;
  }

  private static boolean matchAllWildcards(String paramString1, String paramString2)
  {
    paramString1 = paramString1.toLowerCase();
    paramString2 = paramString2.toLowerCase();
    StringTokenizer localStringTokenizer1 = new StringTokenizer(paramString1, ".");
    StringTokenizer localStringTokenizer2 = new StringTokenizer(paramString2, ".");
    if (localStringTokenizer1.countTokens() != localStringTokenizer2.countTokens())
      return false;
    do
      if (!(localStringTokenizer1.hasMoreTokens()))
        break label68;
    while (matchWildCards(localStringTokenizer1.nextToken(), localStringTokenizer2.nextToken()));
    return false;
    label68: return true;
  }

  private static boolean matchLeftmostWildcard(String paramString1, String paramString2)
  {
    paramString1 = paramString1.toLowerCase();
    paramString2 = paramString2.toLowerCase();
    int i = paramString2.indexOf(".");
    int j = paramString1.indexOf(".");
    if (i == -1)
      i = paramString2.length();
    if (j == -1)
      j = paramString1.length();
    if (matchWildCards(paramString1.substring(0, j), paramString2.substring(0, i)))
      return paramString2.substring(i).equals(paramString1.substring(j));
    return false;
  }

  private static boolean matchWildCards(String paramString1, String paramString2)
  {
    int i = paramString2.indexOf("*");
    if (i == -1)
      return paramString1.equals(paramString2);
    int j = 1;
    String str1 = "";
    String str2 = paramString2;
    while (i != -1)
    {
      str1 = str2.substring(0, i);
      str2 = str2.substring(i + 1);
      int k = paramString1.indexOf(str1);
      if ((k == -1) || ((j != 0) && (k != 0)))
        return false;
      j = 0;
      paramString1 = paramString1.substring(k + str1.length());
      i = str2.indexOf("*");
    }
    return paramString1.endsWith(str2);
  }
}