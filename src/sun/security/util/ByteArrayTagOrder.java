package sun.security.util;

import java.util.Comparator;

public class ByteArrayTagOrder
  implements Comparator
{
  public final int compare(Object paramObject1, Object paramObject2)
  {
    byte[] arrayOfByte1 = (byte[])(byte[])paramObject1;
    byte[] arrayOfByte2 = (byte[])(byte[])paramObject2;
    return ((arrayOfByte1[0] | 0x20) - (arrayOfByte2[0] | 0x20));
  }
}