package sun.misc;

public class Regexp
{
  public boolean ignoreCase;
  public String exp;
  public String prefix;
  public String suffix;
  public boolean exact;
  public int prefixLen;
  public int suffixLen;
  public int totalLen;
  public String[] mids;

  public Regexp(String paramString)
  {
    this.exp = paramString;
    int i = paramString.indexOf(42);
    int j = paramString.lastIndexOf(42);
    if (i < 0)
    {
      this.totalLen = paramString.length();
      this.exact = true;
    }
    else
    {
      this.prefixLen = i;
      if (i == 0)
        this.prefix = null;
      else
        this.prefix = paramString.substring(0, i);
      this.suffixLen = (paramString.length() - j - 1);
      if (this.suffixLen == 0)
        this.suffix = null;
      else
        this.suffix = paramString.substring(j + 1);
      int k = 0;
      for (int l = i; (l < j) && (l >= 0); l = paramString.indexOf(42, l + 1))
        ++k;
      this.totalLen = (this.prefixLen + this.suffixLen);
      if (k > 0)
      {
        this.mids = new String[k];
        l = i;
        for (int i1 = 0; i1 < k; ++i1)
        {
          int i2 = paramString.indexOf(42, ++l);
          if (l < i2)
          {
            this.mids[i1] = paramString.substring(l, i2);
            this.totalLen += this.mids[i1].length();
          }
          l = i2;
        }
      }
    }
  }

  final boolean matches(String paramString)
  {
    return matches(paramString, 0, paramString.length());
  }

  boolean matches(String paramString, int paramInt1, int paramInt2)
  {
    if (this.exact)
      return ((paramInt2 == this.totalLen) && (this.exp.regionMatches(this.ignoreCase, 0, paramString, paramInt1, paramInt2)));
    if (paramInt2 < this.totalLen)
      return false;
    if (((this.prefixLen > 0) && (!(this.prefix.regionMatches(this.ignoreCase, 0, paramString, paramInt1, this.prefixLen)))) || ((this.suffixLen > 0) && (!(this.suffix.regionMatches(this.ignoreCase, 0, paramString, paramInt1 + paramInt2 - this.suffixLen, this.suffixLen)))))
      return false;
    if (this.mids == null)
      return true;
    int i = this.mids.length;
    int j = paramInt1 + this.prefixLen;
    int k = paramInt1 + paramInt2 - this.suffixLen;
    for (int l = 0; l < i; ++l)
    {
      String str = this.mids[l];
      int i1 = str.length();
      while ((j + i1 <= k) && (!(str.regionMatches(this.ignoreCase, 0, paramString, j, i1))))
        ++j;
      if (j + i1 > k)
        return false;
      j += i1;
    }
    return true;
  }
}