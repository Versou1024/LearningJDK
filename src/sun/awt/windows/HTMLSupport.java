package sun.awt.windows;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

final class HTMLSupport
{
  public static final String ENCODING = "UTF-8";
  public static final String VERSION = "Version:";
  public static final String START_HTML = "StartHTML:";
  public static final String END_HTML = "EndHTML:";
  public static final String START_FRAGMENT = "StartFragment:";
  public static final String END_FRAGMENT = "EndFragment:";
  public static final String START_FRAGMENT_CMT = "<!--StartFragment-->";
  public static final String END_FRAGMENT_CMT = "<!--EndFragment-->";
  public static final String EOLN = "\r\n";
  private static final String VERSION_NUM = "0.9";
  private static final String HTML_START_END = "-1";
  private static final int PADDED_WIDTH = 10;
  private static final int HEADER_LEN = "Version:".length() + "0.9".length() + "\r\n".length() + "StartHTML:".length() + "-1".length() + "\r\n".length() + "EndHTML:".length() + "-1".length() + "\r\n".length() + "StartFragment:".length() + 10 + "\r\n".length() + "EndFragment:".length() + 10 + "\r\n".length() + "<!--StartFragment-->".length() + "\r\n".length();
  private static final String HEADER_LEN_STR = toPaddedString(HEADER_LEN, 10);
  private static final String TRAILER = "";

  private static String toPaddedString(int paramInt1, int paramInt2)
  {
    String str = "" + paramInt1;
    int i = str.length();
    if ((paramInt1 >= 0) && (i < paramInt2))
    {
      char[] arrayOfChar = new char[paramInt2 - i];
      Arrays.fill(arrayOfChar, '0');
      StringBuffer localStringBuffer = new StringBuffer();
      localStringBuffer.append(arrayOfChar);
      localStringBuffer.append(str);
      str = localStringBuffer.toString();
    }
    return str;
  }

  public static byte[] convertToHTMLFormat(byte[] paramArrayOfByte)
  {
    StringBuffer localStringBuffer = new StringBuffer(HEADER_LEN);
    localStringBuffer.append("Version:");
    localStringBuffer.append("0.9");
    localStringBuffer.append("\r\n");
    localStringBuffer.append("StartHTML:");
    localStringBuffer.append("-1");
    localStringBuffer.append("\r\n");
    localStringBuffer.append("EndHTML:");
    localStringBuffer.append("-1");
    localStringBuffer.append("\r\n");
    localStringBuffer.append("StartFragment:");
    localStringBuffer.append(HEADER_LEN_STR);
    localStringBuffer.append("\r\n");
    localStringBuffer.append("EndFragment:");
    localStringBuffer.append(toPaddedString(HEADER_LEN + paramArrayOfByte.length - 1, 10));
    localStringBuffer.append("\r\n");
    localStringBuffer.append("<!--StartFragment-->");
    localStringBuffer.append("\r\n");
    byte[] arrayOfByte1 = null;
    byte[] arrayOfByte2 = null;
    try
    {
      arrayOfByte1 = new String(localStringBuffer).getBytes("UTF-8");
      arrayOfByte2 = "".getBytes("UTF-8");
    }
    catch (UnsupportedEncodingException localUnsupportedEncodingException)
    {
    }
    byte[] arrayOfByte3 = new byte[arrayOfByte1.length + paramArrayOfByte.length - 1 + arrayOfByte2.length];
    System.arraycopy(arrayOfByte1, 0, arrayOfByte3, 0, arrayOfByte1.length);
    System.arraycopy(paramArrayOfByte, 0, arrayOfByte3, arrayOfByte1.length, paramArrayOfByte.length - 1);
    System.arraycopy(arrayOfByte2, 0, arrayOfByte3, arrayOfByte1.length + paramArrayOfByte.length - 1, arrayOfByte2.length);
    return arrayOfByte3;
  }
}