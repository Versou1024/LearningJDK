package sun.security.x509;

import java.io.IOException;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class RFC822Name
  implements GeneralNameInterface
{
  private String name;

  public RFC822Name(DerValue paramDerValue)
    throws IOException
  {
    this.name = paramDerValue.getIA5String();
    parseName(this.name);
  }

  public RFC822Name(String paramString)
    throws IOException
  {
    parseName(paramString);
    this.name = paramString;
  }

  public void parseName(String paramString)
    throws IOException
  {
    if ((paramString == null) || (paramString.length() == 0))
      throw new IOException("RFC822Name may not be null or empty");
    String str = paramString.substring(paramString.indexOf(64) + 1);
    if (str.length() == 0)
      throw new IOException("RFC822Name may not end with @");
    if ((str.startsWith(".")) && (str.length() == 1))
      throw new IOException("RFC822Name domain may not be just .");
  }

  public int getType()
  {
    return 1;
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
    return "RFC822Name: " + this.name;
  }

  public boolean equals(Object paramObject)
  {
    if (this == paramObject)
      return true;
    if (!(paramObject instanceof RFC822Name))
      return false;
    RFC822Name localRFC822Name = (RFC822Name)paramObject;
    return this.name.equalsIgnoreCase(localRFC822Name.name);
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
    else if (paramGeneralNameInterface.getType() != 1)
    {
      i = -1;
    }
    else
    {
      String str1 = ((RFC822Name)paramGeneralNameInterface).getName().toLowerCase();
      String str2 = this.name.toLowerCase();
      if (str1.equals(str2))
      {
        i = 0;
      }
      else
      {
        int j;
        if (str2.endsWith(str1))
          if (str1.indexOf(64) != -1)
          {
            i = 3;
          }
          else if (str1.startsWith("."))
          {
            i = 2;
          }
          else
          {
            j = str2.lastIndexOf(str1);
            if (str2.charAt(j - 1) == '@')
              i = 2;
            else
              i = 3;
          }
        else if (str1.endsWith(str2))
          if (str2.indexOf(64) != -1)
          {
            i = 3;
          }
          else if (str2.startsWith("."))
          {
            i = 1;
          }
          else
          {
            j = str1.lastIndexOf(str2);
            if (str1.charAt(j - 1) == '@')
              i = 1;
            else
              i = 3;
          }
        else
          i = 3;
      }
    }
    return i;
  }

  public int subtreeDepth()
    throws UnsupportedOperationException
  {
    String str = this.name;
    int i = 1;
    int j = str.lastIndexOf(64);
    if (j >= 0)
    {
      ++i;
      str = str.substring(j + 1);
    }
    while (str.lastIndexOf(46) >= 0)
    {
      str = str.substring(0, str.lastIndexOf(46));
      ++i;
    }
    return i;
  }
}