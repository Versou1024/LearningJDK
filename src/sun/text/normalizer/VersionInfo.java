package sun.text.normalizer;

import java.util.HashMap;

public final class VersionInfo
{
  private int m_version_;
  private static final HashMap MAP_ = new HashMap();
  private static final String INVALID_VERSION_NUMBER_ = "Invalid version number: Version number may be negative or greater than 255";

  public static VersionInfo getInstance(String paramString)
  {
    int i = paramString.length();
    int[] arrayOfInt = { 0, 0, 0, 0 };
    int j = 0;
    for (int k = 0; (j < 4) && (k < i); ++k)
    {
      l = paramString.charAt(k);
      if (l == 46)
      {
        ++j;
      }
      else
      {
        l = (char)(l - 48);
        if ((l < 0) || (l > 9))
          throw new IllegalArgumentException("Invalid version number: Version number may be negative or greater than 255");
        arrayOfInt[j] *= 10;
        arrayOfInt[j] += l;
      }
    }
    if (k != i)
      throw new IllegalArgumentException("Invalid version number: String '" + paramString + "' exceeds version format");
    for (int l = 0; l < 4; ++l)
      if ((arrayOfInt[l] < 0) || (arrayOfInt[l] > 255))
        throw new IllegalArgumentException("Invalid version number: Version number may be negative or greater than 255");
    return getInstance(arrayOfInt[0], arrayOfInt[1], arrayOfInt[2], arrayOfInt[3]);
  }

  public static VersionInfo getInstance(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    if ((paramInt1 < 0) || (paramInt1 > 255) || (paramInt2 < 0) || (paramInt2 > 255) || (paramInt3 < 0) || (paramInt3 > 255) || (paramInt4 < 0) || (paramInt4 > 255))
      throw new IllegalArgumentException("Invalid version number: Version number may be negative or greater than 255");
    int i = getInt(paramInt1, paramInt2, paramInt3, paramInt4);
    Integer localInteger = new Integer(i);
    Object localObject = MAP_.get(localInteger);
    if (localObject == null)
    {
      localObject = new VersionInfo(i);
      MAP_.put(localInteger, localObject);
    }
    return ((VersionInfo)(VersionInfo)localObject);
  }

  public int compareTo(VersionInfo paramVersionInfo)
  {
    return (this.m_version_ - paramVersionInfo.m_version_);
  }

  private VersionInfo(int paramInt)
  {
    this.m_version_ = paramInt;
  }

  private static int getInt(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    return (paramInt1 << 24 | paramInt2 << 16 | paramInt3 << 8 | paramInt4);
  }
}