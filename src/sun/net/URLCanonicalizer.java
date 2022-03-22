package sun.net;

public class URLCanonicalizer
{
  public String canonicalize(String paramString)
  {
    String str = paramString;
    if (paramString.startsWith("ftp."))
    {
      str = "ftp://" + paramString;
    }
    else if (paramString.startsWith("gopher."))
    {
      str = "gopher://" + paramString;
    }
    else if (paramString.startsWith("/"))
    {
      str = "file:" + paramString;
    }
    else if (!(hasProtocolName(paramString)))
    {
      if (isSimpleHostName(paramString))
        paramString = "www." + paramString + ".com";
      str = "http://" + paramString;
    }
    return str;
  }

  public boolean hasProtocolName(String paramString)
  {
    int i = paramString.indexOf(58);
    if (i <= 0)
      return false;
    for (int j = 0; j < i; ++j)
    {
      int k = paramString.charAt(j);
      if ((((k < 65) || (k > 90))) && (((k < 97) || (k > 122))))
      {
        if (k == 45)
          break label67:
        label67: return false;
      }
    }
    return true;
  }

  protected boolean isSimpleHostName(String paramString)
  {
    for (int i = 0; i < paramString.length(); ++i)
    {
      int j = paramString.charAt(i);
      if ((((j < 65) || (j > 90))) && (((j < 97) || (j > 122))) && (((j < 48) || (j > 57))))
      {
        if (j == 45)
          break label63:
        label63: return false;
      }
    }
    return true;
  }
}