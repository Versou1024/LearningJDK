package sun.misc;

import java.util.Comparator;

public class ASCIICaseInsensitiveComparator
  implements Comparator
{
  public static final Comparator CASE_INSENSITIVE_ORDER;

  public int compare(Object paramObject1, Object paramObject2)
  {
    String str1 = (String)paramObject1;
    String str2 = (String)paramObject2;
    int i = str1.length();
    int j = str2.length();
    int k = (i < j) ? i : j;
    for (int l = 0; l < k; ++l)
    {
      int i1 = str1.charAt(l);
      int i2 = str2.charAt(l);
      if ((!($assertionsDisabled)) && (((i1 > 127) || (i2 > 127))))
        throw new AssertionError();
      if (i1 != i2)
      {
        i1 = (char)toLower(i1);
        i2 = (char)toLower(i2);
        if (i1 != i2)
          return (i1 - i2);
      }
    }
    return (i - j);
  }

  public static int lowerCaseHashCode(String paramString)
  {
    int i = 0;
    int j = paramString.length();
    for (int k = 0; k < j; ++k)
      i = 31 * i + toLower(paramString.charAt(k));
    return i;
  }

  static boolean isLower(int paramInt)
  {
    return ((paramInt - 97 | 122 - paramInt) >= 0);
  }

  static boolean isUpper(int paramInt)
  {
    return ((paramInt - 65 | 90 - paramInt) >= 0);
  }

  static int toLower(int paramInt)
  {
    return ((isUpper(paramInt)) ? paramInt + 32 : paramInt);
  }

  static int toUpper(int paramInt)
  {
    return ((isLower(paramInt)) ? paramInt - 32 : paramInt);
  }

  static
  {
    CASE_INSENSITIVE_ORDER = new ASCIICaseInsensitiveComparator();
  }
}