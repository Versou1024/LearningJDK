package sun.security.x509;

import java.io.IOException;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class DNSName
  implements GeneralNameInterface
{
  private String name;
  private static final String alpha = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
  private static final String digitsAndHyphen = "0123456789-";
  private static final String alphaDigitsAndHyphen = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-";

  public DNSName(DerValue paramDerValue)
    throws IOException
  {
    this.name = paramDerValue.getIA5String();
  }

  public DNSName(String paramString)
    throws IOException
  {
    if ((paramString == null) || (paramString.length() == 0))
      throw new IOException("DNS name must not be null");
    if (paramString.indexOf(32) != -1)
      throw new IOException("DNS names or NameConstraints with blank components are not permitted");
    if ((paramString.charAt(0) == '.') || (paramString.charAt(paramString.length() - 1) == '.'))
      throw new IOException("DNS names or NameConstraints may not begin or end with a .");
    for (int j = 0; j < paramString.length(); j = i + 1)
    {
      int i = paramString.indexOf(46, j);
      if (i < 0)
        i = paramString.length();
      if (i - j < 1)
        throw new IOException("DNSName SubjectAltNames with empty components are not permitted");
      if ("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".indexOf(paramString.charAt(j)) < 0)
        throw new IOException("DNSName components must begin with a letter");
      for (int k = j + 1; k < i; ++k)
      {
        int l = paramString.charAt(k);
        if ("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-".indexOf(l) < 0)
          throw new IOException("DNSName components must consist of letters, digits, and hyphens");
      }
    }
    this.name = paramString;
  }

  public int getType()
  {
    return 2;
  }

  public String getName()
  {
    return this.name;
  }

  public void encode(DerOutputStream paramDerOutputStream)
    throws IOException
  {
    paramDerOutputStream.putIA5String(this.name);
  }

  public String toString()
  {
    return "DNSName: " + this.name;
  }

  public boolean equals(Object paramObject)
  {
    if (this == paramObject)
      return true;
    if (!(paramObject instanceof DNSName))
      return false;
    DNSName localDNSName = (DNSName)paramObject;
    return this.name.equalsIgnoreCase(localDNSName.name);
  }

  public int hashCode()
  {
    return this.name.toUpperCase().hashCode();
  }

  public int constrains(GeneralNameInterface paramGeneralNameInterface)
    throws UnsupportedOperationException
  {
    int i;
    if (paramGeneralNameInterface == null)
    {
      i = -1;
    }
    else if (paramGeneralNameInterface.getType() != 2)
    {
      i = -1;
    }
    else
    {
      String str1 = ((DNSName)paramGeneralNameInterface).getName().toLowerCase();
      String str2 = this.name.toLowerCase();
      if (str1.equals(str2))
      {
        i = 0;
      }
      else
      {
        int j;
        if (str2.endsWith(str1))
        {
          j = str2.lastIndexOf(str1);
          if (str2.charAt(j - 1) == '.')
            i = 2;
          else
            i = 3;
        }
        else if (str1.endsWith(str2))
        {
          j = str1.lastIndexOf(str2);
          if (str1.charAt(j - 1) == '.')
            i = 1;
          else
            i = 3;
        }
        else
        {
          i = 3;
        }
      }
    }
    return i;
  }

  public int subtreeDepth()
    throws UnsupportedOperationException
  {
    String str = this.name;
    for (int i = 1; str.lastIndexOf(46) >= 0; ++i)
      str = str.substring(0, str.lastIndexOf(46));
    return i;
  }
}