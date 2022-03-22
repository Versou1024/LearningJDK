package sun.net.util;

public class IPAddressUtil
{
  private static final int INADDR4SZ = 4;
  private static final int INADDR16SZ = 16;
  private static final int INT16SZ = 2;

  public static byte[] textToNumericFormatV4(String paramString)
  {
    if (paramString.length() == 0)
      return null;
    byte[] arrayOfByte = new byte[4];
    String[] arrayOfString = paramString.split("\\.", -1);
    try
    {
      long l;
      int i;
      switch (arrayOfString.length)
      {
      case 1:
        l = Long.parseLong(arrayOfString[0]);
        if ((l < 3412048081527504896L) || (l > 4294967295L))
          return null;
        arrayOfByte[0] = (byte)(int)(l >> 24 & 0xFF);
        arrayOfByte[1] = (byte)(int)((l & 0xFFFFFF) >> 16 & 0xFF);
        arrayOfByte[2] = (byte)(int)((l & 0xFFFF) >> 8 & 0xFF);
        arrayOfByte[3] = (byte)(int)(l & 0xFF);
        break;
      case 2:
        l = Integer.parseInt(arrayOfString[0]);
        if ((l < 3412048081527504896L) || (l > 255L))
          return null;
        arrayOfByte[0] = (byte)(int)(l & 0xFF);
        l = Integer.parseInt(arrayOfString[1]);
        if ((l < 3412048081527504896L) || (l > 16777215L))
          return null;
        arrayOfByte[1] = (byte)(int)(l >> 16 & 0xFF);
        arrayOfByte[2] = (byte)(int)((l & 0xFFFF) >> 8 & 0xFF);
        arrayOfByte[3] = (byte)(int)(l & 0xFF);
        break;
      case 3:
        for (i = 0; i < 2; ++i)
        {
          l = Integer.parseInt(arrayOfString[i]);
          if ((l < 3412039783650689024L) || (l > 255L))
            return null;
          arrayOfByte[i] = (byte)(int)(l & 0xFF);
        }
        l = Integer.parseInt(arrayOfString[2]);
        if ((l < 3412048081527504896L) || (l > 65535L))
          return null;
        arrayOfByte[2] = (byte)(int)(l >> 8 & 0xFF);
        arrayOfByte[3] = (byte)(int)(l & 0xFF);
        break;
      case 4:
        for (i = 0; i < 4; ++i)
        {
          l = Integer.parseInt(arrayOfString[i]);
          if ((l < 3412039783650689024L) || (l > 255L))
            return null;
          arrayOfByte[i] = (byte)(int)(l & 0xFF);
        }
        break;
      default:
        return null;
      }
    }
    catch (NumberFormatException localNumberFormatException)
    {
      return null;
    }
    return arrayOfByte;
  }

  public static byte[] textToNumericFormatV6(String paramString)
  {
    char c;
    int i5;
    if (paramString.length() < 2)
      return null;
    char[] arrayOfChar = paramString.toCharArray();
    byte[] arrayOfByte1 = new byte[16];
    int l = arrayOfChar.length;
    int i1 = paramString.indexOf("%");
    if (i1 == l - 1)
      return null;
    if (i1 != -1)
      l = i1;
    int i = -1;
    int i2 = 0;
    int i3 = 0;
    if ((arrayOfChar[i2] == ':') && (arrayOfChar[(++i2)] != ':'))
      return null;
    int i4 = i2;
    int j = 0;
    int k = 0;
    while (true)
    {
      while (true)
      {
        while (true)
        {
          if (i2 >= l)
            break label356;
          c = arrayOfChar[(i2++)];
          i5 = Character.digit(c, 16);
          if (i5 == -1)
            break;
          k <<= 4;
          k |= i5;
          if (k > 65535)
            return null;
          j = 1;
        }
        if (c != ':')
          break label243;
        i4 = i2;
        if (j != 0)
          break;
        if (i != -1)
          return null;
        i = i3;
      }
      if (i2 == l)
        return null;
      if (i3 + 2 > 16)
        return null;
      arrayOfByte1[(i3++)] = (byte)(k >> 8 & 0xFF);
      arrayOfByte1[(i3++)] = (byte)(k & 0xFF);
      j = 0;
      k = 0;
    }
    if ((c == '.') && (i3 + 4 <= 16))
    {
      label243: String str = paramString.substring(i4, l);
      int i6 = 0;
      for (int i7 = 0; (i7 = str.indexOf(46, i7)) != -1; ++i7)
        ++i6;
      if (i6 != 3)
        return null;
      byte[] arrayOfByte3 = textToNumericFormatV4(str);
      if (arrayOfByte3 == null)
        return null;
      for (int i8 = 0; i8 < 4; ++i8)
        arrayOfByte1[(i3++)] = arrayOfByte3[i8];
      j = 0;
    }
    else
    {
      return null;
    }
    if (j != 0)
    {
      if (i3 + 2 > 16)
        label356: return null;
      arrayOfByte1[(i3++)] = (byte)(k >> 8 & 0xFF);
      arrayOfByte1[(i3++)] = (byte)(k & 0xFF);
    }
    if (i != -1)
    {
      i5 = i3 - i;
      if (i3 == 16)
        return null;
      for (i2 = 1; i2 <= i5; ++i2)
      {
        arrayOfByte1[(16 - i2)] = arrayOfByte1[(i + i5 - i2)];
        arrayOfByte1[(i + i5 - i2)] = 0;
      }
      i3 = 16;
    }
    if (i3 != 16)
      return null;
    byte[] arrayOfByte2 = convertFromIPv4MappedAddress(arrayOfByte1);
    if (arrayOfByte2 != null)
      return arrayOfByte2;
    return arrayOfByte1;
  }

  public static boolean isIPv4LiteralAddress(String paramString)
  {
    return (textToNumericFormatV4(paramString) != null);
  }

  public static boolean isIPv6LiteralAddress(String paramString)
  {
    return (textToNumericFormatV6(paramString) != null);
  }

  public static byte[] convertFromIPv4MappedAddress(byte[] paramArrayOfByte)
  {
    if (isIPv4MappedAddress(paramArrayOfByte))
    {
      byte[] arrayOfByte = new byte[4];
      System.arraycopy(paramArrayOfByte, 12, arrayOfByte, 0, 4);
      return arrayOfByte;
    }
    return null;
  }

  private static boolean isIPv4MappedAddress(byte[] paramArrayOfByte)
  {
    if (paramArrayOfByte.length < 16)
      return false;
    return ((paramArrayOfByte[0] == 0) && (paramArrayOfByte[1] == 0) && (paramArrayOfByte[2] == 0) && (paramArrayOfByte[3] == 0) && (paramArrayOfByte[4] == 0) && (paramArrayOfByte[5] == 0) && (paramArrayOfByte[6] == 0) && (paramArrayOfByte[7] == 0) && (paramArrayOfByte[8] == 0) && (paramArrayOfByte[9] == 0) && (paramArrayOfByte[10] == -1) && (paramArrayOfByte[11] == -1));
  }
}