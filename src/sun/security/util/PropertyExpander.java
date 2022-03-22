package sun.security.util;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import sun.net.www.ParseUtil;

public class PropertyExpander
{
  public static String expand(String paramString)
    throws sun.security.util.PropertyExpander.ExpandException
  {
    return expand(paramString, false);
  }

  public static String expand(String paramString, boolean paramBoolean)
    throws sun.security.util.PropertyExpander.ExpandException
  {
    if (paramString == null)
      return null;
    int i = paramString.indexOf("${", 0);
    if (i == -1)
      return paramString;
    StringBuffer localStringBuffer = new StringBuffer(paramString.length());
    int j = paramString.length();
    int k = 0;
    while (i < j)
    {
      if (i > k)
      {
        localStringBuffer.append(paramString.substring(k, i));
        k = i;
      }
      int l = i + 2;
      if ((l < j) && (paramString.charAt(l) == '{'))
      {
        l = paramString.indexOf("}}", l);
        if ((l == -1) || (l + 2 == j))
        {
          localStringBuffer.append(paramString.substring(i));
          break;
        }
        localStringBuffer.append(paramString.substring(i, ++l + 1));
      }
      else
      {
        while ((l < j) && (paramString.charAt(l) != '}'))
          ++l;
        if (l == j)
        {
          localStringBuffer.append(paramString.substring(i, l));
          break;
        }
        String str1 = paramString.substring(i + 2, l);
        if (str1.equals("/"))
        {
          localStringBuffer.append(File.separatorChar);
        }
        else
        {
          String str2 = System.getProperty(str1);
          if (str2 != null)
          {
            if (paramBoolean)
              try
              {
                if ((localStringBuffer.length() > 0) || (!(new URI(str2).isAbsolute())))
                  str2 = ParseUtil.encodePath(str2);
              }
              catch (URISyntaxException localURISyntaxException)
              {
                str2 = ParseUtil.encodePath(str2);
              }
            localStringBuffer.append(str2);
          }
          else
          {
            throw new sun.security.util.PropertyExpander.ExpandException("unable to expand property " + str1);
          }
        }
      }
      k = l + 1;
      i = paramString.indexOf("${", k);
      if (i == -1)
      {
        if (k >= j)
          break;
        localStringBuffer.append(paramString.substring(k, j));
        break;
      }
    }
    return localStringBuffer.toString();
  }

  public static class ExpandException extends GeneralSecurityException
  {
    private static final long serialVersionUID = -7941948581406161702L;

    public ExpandException(String paramString)
    {
      super(paramString);
    }
  }
}