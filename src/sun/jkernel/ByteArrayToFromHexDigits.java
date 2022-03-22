package sun.jkernel;

public final class ByteArrayToFromHexDigits
{
  private static final char[] chars = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
  private static final boolean debug = 0;

  public static String bytesToHexString(byte[] paramArrayOfByte)
  {
    if ((paramArrayOfByte == null) || (paramArrayOfByte.length == 0))
      throw new IllegalArgumentException("argument null or zero length");
    StringBuffer localStringBuffer = new StringBuffer(paramArrayOfByte.length * 2);
    for (int i = 0; i < paramArrayOfByte.length; ++i)
    {
      localStringBuffer.insert(i * 2, chars[(paramArrayOfByte[i] >> 4 & 0xF)]);
      localStringBuffer.insert(i * 2 + 1, chars[(paramArrayOfByte[i] & 0xF)]);
    }
    return localStringBuffer.toString();
  }

  private static byte hexCharToByte(char paramChar)
    throws IllegalArgumentException
  {
    if ((paramChar < '0') || ((paramChar < 'A') && (paramChar > 'F') && (paramChar < 'a') && (paramChar > 'f')))
      throw new IllegalArgumentException("not a hex digit");
    if (paramChar > '9')
    {
      if (paramChar > 'F')
        return (byte)(paramChar - 'a' + 10 & 0xF);
      return (byte)(paramChar - 'A' + 10 & 0xF);
    }
    return (byte)(paramChar - '0' & 0xF);
  }

  public static byte[] hexStringToBytes(String paramString)
    throws IllegalArgumentException
  {
    if (paramString == null)
      throw new IllegalArgumentException("parameter cannot be null");
    if (paramString.length() == 0)
      throw new IllegalArgumentException("parameter cannot be zero length");
    if ((paramString.length() & 0x1) != 0)
      throw new IllegalArgumentException("odd length string");
    byte[] arrayOfByte = new byte[paramString.length() / 2];
    for (int i = 0; i < paramString.length(); i += 2)
      arrayOfByte[(i / 2)] = (byte)((hexCharToByte(paramString.charAt(i)) << 4) + hexCharToByte(paramString.charAt(i + 1)));
    return arrayOfByte;
  }
}