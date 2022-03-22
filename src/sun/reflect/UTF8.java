package sun.reflect;

class UTF8
{
  static byte[] encode(String paramString)
  {
    int i = paramString.length();
    byte[] arrayOfByte = new byte[utf8Length(paramString)];
    int j = 0;
    try
    {
      for (int k = 0; k < i; ++k)
      {
        int l = paramString.charAt(k) & 0xFFFF;
        if ((l >= 1) && (l <= 127))
        {
          arrayOfByte[(j++)] = (byte)l;
        }
        else if ((l == 0) || ((l >= 128) && (l <= 2047)))
        {
          arrayOfByte[(j++)] = (byte)(192 + (l >> 6));
          arrayOfByte[(j++)] = (byte)(128 + (l & 0x3F));
        }
        else
        {
          arrayOfByte[(j++)] = (byte)(224 + (l >> 12));
          arrayOfByte[(j++)] = (byte)(128 + (l >> 6 & 0x3F));
          arrayOfByte[(j++)] = (byte)(128 + (l & 0x3F));
        }
      }
    }
    catch (ArrayIndexOutOfBoundsException localArrayIndexOutOfBoundsException)
    {
      throw new InternalError("Bug in sun.reflect bootstrap UTF-8 encoder");
    }
    return arrayOfByte;
  }

  private static int utf8Length(String paramString)
  {
    int i = paramString.length();
    int j = 0;
    for (int k = 0; k < i; ++k)
    {
      int l = paramString.charAt(k) & 0xFFFF;
      if ((l >= 1) && (l <= 127))
        ++j;
      else if ((l == 0) || ((l >= 128) && (l <= 2047)))
        j += 2;
      else
        j += 3;
    }
    return j;
  }
}