package sun.security.util;

import java.util.Comparator;

public class ByteArrayLexOrder
  implements Comparator
{
  public final int compare(Object paramObject1, Object paramObject2)
  {
    byte[] arrayOfByte1 = (byte[])(byte[])paramObject1;
    byte[] arrayOfByte2 = (byte[])(byte[])paramObject2;
    for (int j = 0; (j < arrayOfByte1.length) && (j < arrayOfByte2.length); ++j)
    {
      int i = (arrayOfByte1[j] & 0xFF) - (arrayOfByte2[j] & 0xFF);
      if (i != 0)
        return i;
    }
    return (arrayOfByte1.length - arrayOfByte2.length);
  }
}