package sun.net.www;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetEncoder;
import java.util.BitSet;
import sun.nio.cs.ThreadLocalCoders;

public class ParseUtil
{
  static BitSet encodedInPath;
  private static final char[] hexDigits;
  private static final long L_DIGIT;
  private static final long H_DIGIT = 0L;
  private static final long L_HEX;
  private static final long H_HEX;
  private static final long L_UPALPHA = 0L;
  private static final long H_UPALPHA;
  private static final long L_LOWALPHA = 0L;
  private static final long H_LOWALPHA;
  private static final long L_ALPHA = 0L;
  private static final long H_ALPHA;
  private static final long L_ALPHANUM;
  private static final long H_ALPHANUM;
  private static final long L_MARK;
  private static final long H_MARK;
  private static final long L_UNRESERVED;
  private static final long H_UNRESERVED;
  private static final long L_RESERVED;
  private static final long H_RESERVED;
  private static final long L_ESCAPED = 1L;
  private static final long H_ESCAPED = 0L;
  private static final long L_DASH;
  private static final long H_DASH;
  private static final long L_URIC;
  private static final long H_URIC;
  private static final long L_PCHAR;
  private static final long H_PCHAR;
  private static final long L_PATH;
  private static final long H_PATH;
  private static final long L_USERINFO;
  private static final long H_USERINFO;
  private static final long L_REG_NAME;
  private static final long H_REG_NAME;
  private static final long L_SERVER;
  private static final long H_SERVER;

  public static String encodePath(String paramString)
  {
    return encodePath(paramString, true);
  }

  public static String encodePath(String paramString, boolean paramBoolean)
  {
    Object localObject = new char[paramString.length() * 2 + 16];
    int i = 0;
    char[] arrayOfChar1 = paramString.toCharArray();
    int j = paramString.length();
    for (int k = 0; k < j; ++k)
    {
      int l = arrayOfChar1[k];
      if (((!(paramBoolean)) && (l == 47)) || ((paramBoolean) && (l == File.separatorChar)))
      {
        localObject[(i++)] = 47;
      }
      else if (l <= 127)
      {
        if (((l >= 97) && (l <= 122)) || ((l >= 65) && (l <= 90)) || ((l >= 48) && (l <= 57)))
          localObject[(i++)] = l;
        else if (encodedInPath.get(l))
          i = escape(localObject, l, i);
        else
          localObject[(i++)] = l;
      }
      else if (l > 2047)
      {
        i = escape(localObject, (char)(0xE0 | l >> 12 & 0xF), i);
        i = escape(localObject, (char)(0x80 | l >> 6 & 0x3F), i);
        i = escape(localObject, (char)(0x80 | l >> 0 & 0x3F), i);
      }
      else
      {
        i = escape(localObject, (char)(0xC0 | l >> 6 & 0x1F), i);
        i = escape(localObject, (char)(0x80 | l >> 0 & 0x3F), i);
      }
      if (i + 9 > localObject.length)
      {
        int i1 = localObject.length * 2 + 16;
        if (i1 < 0)
          i1 = 2147483647;
        char[] arrayOfChar2 = new char[i1];
        System.arraycopy(localObject, 0, arrayOfChar2, 0, i);
        localObject = arrayOfChar2;
      }
    }
    return ((String)new String(localObject, 0, i));
  }

  private static int escape(char[] paramArrayOfChar, char paramChar, int paramInt)
  {
    paramArrayOfChar[(paramInt++)] = '%';
    paramArrayOfChar[(paramInt++)] = Character.forDigit(paramChar >> '\4' & 0xF, 16);
    paramArrayOfChar[(paramInt++)] = Character.forDigit(paramChar & 0xF, 16);
    return paramInt;
  }

  private static char unescape(String paramString, int paramInt)
  {
    return (char)Integer.parseInt(paramString.substring(paramInt + 1, paramInt + 3), 16);
  }

  public static String decode(String paramString)
  {
    StringBuilder localStringBuilder = new StringBuilder();
    int i = 0;
    while (i < paramString.length())
    {
      char c = paramString.charAt(i);
      if (c != '%')
        ++i;
      else
        try
        {
          c = unescape(paramString, i);
          i += 3;
          if ((c & 0x80) != 0)
          {
            int j;
            switch (c >> '\4')
            {
            case 12:
            case 13:
              j = unescape(paramString, i);
              i += 3;
              c = (char)((c & 0x1F) << '\6' | j & 0x3F);
              break;
            case 14:
              j = unescape(paramString, i);
              int k = unescape(paramString, i += 3);
              i += 3;
              c = (char)((c & 0xF) << '\f' | (j & 0x3F) << 6 | k & 0x3F);
              break;
            default:
              throw new IllegalArgumentException();
            }
          }
        }
        catch (NumberFormatException localNumberFormatException)
        {
          throw new IllegalArgumentException();
        }
      localStringBuilder.append(c);
    }
    return localStringBuilder.toString();
  }

  public String canonizeString(String paramString)
  {
    int i = 0;
    int j = paramString.length();
    while (true)
    {
      while (true)
      {
        if ((i = paramString.indexOf("/../")) < 0)
          break label76;
        if ((j = paramString.lastIndexOf(47, i - 1)) < 0)
          break;
        paramString = paramString.substring(0, j) + paramString.substring(i + 3);
      }
      paramString = paramString.substring(i + 3);
    }
    while ((i = paramString.indexOf("/./")) >= 0)
      label76: paramString = paramString.substring(0, i) + paramString.substring(i + 2);
    while (true)
    {
      while (true)
      {
        if (!(paramString.endsWith("/..")))
          break label172;
        i = paramString.indexOf("/..");
        if ((j = paramString.lastIndexOf(47, i - 1)) < 0)
          break;
        paramString = paramString.substring(0, j + 1);
      }
      paramString = paramString.substring(0, i);
    }
    if (paramString.endsWith("/."))
      label172: paramString = paramString.substring(0, paramString.length() - 1);
    return paramString;
  }

  public static URL fileToEncodedURL(File paramFile)
    throws MalformedURLException
  {
    String str = paramFile.getAbsolutePath();
    str = encodePath(str);
    if (!(str.startsWith("/")))
      str = "/" + str;
    if ((!(str.endsWith("/"))) && (paramFile.isDirectory()))
      str = str + "/";
    return new URL("file", "", str);
  }

  public static URI toURI(URL paramURL)
  {
    URI localURI;
    String str1 = paramURL.getProtocol();
    String str2 = paramURL.getAuthority();
    String str3 = paramURL.getPath();
    String str4 = paramURL.getQuery();
    String str5 = paramURL.getRef();
    if ((str3 != null) && (!(str3.startsWith("/"))))
      str3 = "/" + str3;
    if (str2.endsWith(":-1"))
      str2 = str2.substring(0, str2.length() - 3);
    try
    {
      localURI = createURI(str1, str2, str3, str4, str5);
    }
    catch (URISyntaxException localURISyntaxException)
    {
      localURI = null;
    }
    return localURI;
  }

  private static URI createURI(String paramString1, String paramString2, String paramString3, String paramString4, String paramString5)
    throws URISyntaxException
  {
    String str = toString(paramString1, null, paramString2, null, null, -1, paramString3, paramString4, paramString5);
    checkPath(str, paramString1, paramString3);
    return new URI(str);
  }

  private static String toString(String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, int paramInt, String paramString6, String paramString7, String paramString8)
  {
    StringBuffer localStringBuffer = new StringBuffer();
    if (paramString1 != null)
    {
      localStringBuffer.append(paramString1);
      localStringBuffer.append(':');
    }
    appendSchemeSpecificPart(localStringBuffer, paramString2, paramString3, paramString4, paramString5, paramInt, paramString6, paramString7);
    appendFragment(localStringBuffer, paramString8);
    return localStringBuffer.toString();
  }

  private static void appendSchemeSpecificPart(StringBuffer paramStringBuffer, String paramString1, String paramString2, String paramString3, String paramString4, int paramInt, String paramString5, String paramString6)
  {
    if (paramString1 != null)
    {
      if (paramString1.startsWith("//["))
      {
        int i = paramString1.indexOf("]");
        if ((i != -1) && (paramString1.indexOf(":") != -1))
        {
          String str1;
          String str2;
          if (i == paramString1.length())
          {
            str2 = paramString1;
            str1 = "";
          }
          else
          {
            str2 = paramString1.substring(0, i + 1);
            str1 = paramString1.substring(i + 1);
          }
          paramStringBuffer.append(str2);
          paramStringBuffer.append(quote(str1, L_URIC, H_URIC));
        }
      }
      else
      {
        paramStringBuffer.append(quote(paramString1, L_URIC, H_URIC));
      }
    }
    else
    {
      appendAuthority(paramStringBuffer, paramString2, paramString3, paramString4, paramInt);
      if (paramString5 != null)
        paramStringBuffer.append(quote(paramString5, L_PATH, H_PATH));
      if (paramString6 != null)
      {
        paramStringBuffer.append('?');
        paramStringBuffer.append(quote(paramString6, L_URIC, H_URIC));
      }
    }
  }

  private static void appendAuthority(StringBuffer paramStringBuffer, String paramString1, String paramString2, String paramString3, int paramInt)
  {
    int i;
    if (paramString3 != null)
    {
      paramStringBuffer.append("//");
      if (paramString2 != null)
      {
        paramStringBuffer.append(quote(paramString2, L_USERINFO, H_USERINFO));
        paramStringBuffer.append('@');
      }
      i = ((paramString3.indexOf(58) >= 0) && (!(paramString3.startsWith("["))) && (!(paramString3.endsWith("]")))) ? 1 : 0;
      if (i != 0)
        paramStringBuffer.append('[');
      paramStringBuffer.append(paramString3);
      if (i != 0)
        paramStringBuffer.append(']');
      if (paramInt != -1)
      {
        paramStringBuffer.append(':');
        paramStringBuffer.append(paramInt);
      }
    }
    else if (paramString1 != null)
    {
      paramStringBuffer.append("//");
      if (paramString1.startsWith("["))
      {
        i = paramString1.indexOf("]");
        if ((i != -1) && (paramString1.indexOf(":") != -1))
        {
          String str1;
          String str2;
          if (i == paramString1.length())
          {
            str2 = paramString1;
            str1 = "";
          }
          else
          {
            str2 = paramString1.substring(0, i + 1);
            str1 = paramString1.substring(i + 1);
          }
          paramStringBuffer.append(str2);
          paramStringBuffer.append(quote(str1, L_REG_NAME | L_SERVER, H_REG_NAME | H_SERVER));
        }
      }
      else
      {
        paramStringBuffer.append(quote(paramString1, L_REG_NAME | L_SERVER, H_REG_NAME | H_SERVER));
      }
    }
  }

  private static void appendFragment(StringBuffer paramStringBuffer, String paramString)
  {
    if (paramString != null)
    {
      paramStringBuffer.append('#');
      paramStringBuffer.append(quote(paramString, L_URIC, H_URIC));
    }
  }

  private static String quote(String paramString, long paramLong1, long paramLong2)
  {
    int i = paramString.length();
    StringBuffer localStringBuffer = null;
    int j = ((paramLong1 & 3412039697751343105L) != 3412047772289859584L) ? 1 : 0;
    for (int k = 0; k < paramString.length(); ++k)
    {
      char c = paramString.charAt(k);
      if (c < 128)
      {
        if ((!(match(c, paramLong1, paramLong2))) && (!(isEscaped(paramString, k))))
        {
          if (localStringBuffer == null)
          {
            localStringBuffer = new StringBuffer();
            localStringBuffer.append(paramString.substring(0, k));
          }
          appendEscape(localStringBuffer, (byte)c);
        }
        else if (localStringBuffer != null)
        {
          localStringBuffer.append(c);
        }
      }
      else if ((j != 0) && (((Character.isSpaceChar(c)) || (Character.isISOControl(c)))))
      {
        if (localStringBuffer == null)
        {
          localStringBuffer = new StringBuffer();
          localStringBuffer.append(paramString.substring(0, k));
        }
        appendEncoded(localStringBuffer, c);
      }
      else if (localStringBuffer != null)
      {
        localStringBuffer.append(c);
      }
    }
    return ((localStringBuffer == null) ? paramString : localStringBuffer.toString());
  }

  private static boolean isEscaped(String paramString, int paramInt)
  {
    if ((paramString == null) || (paramString.length() < paramInt + 2))
      return false;
    return ((paramString.charAt(paramInt) == '%') && (match(paramString.charAt(paramInt + 1), L_HEX, H_HEX)) && (match(paramString.charAt(paramInt + 2), L_HEX, H_HEX)));
  }

  private static void appendEncoded(StringBuffer paramStringBuffer, char paramChar)
  {
    ByteBuffer localByteBuffer = null;
    try
    {
      localByteBuffer = ThreadLocalCoders.encoderFor("UTF-8").encode(CharBuffer.wrap("" + paramChar));
    }
    catch (CharacterCodingException localCharacterCodingException)
    {
      if (!($assertionsDisabled))
        throw new AssertionError();
    }
    while (localByteBuffer.hasRemaining())
    {
      int i = localByteBuffer.get() & 0xFF;
      if (i >= 128)
        appendEscape(paramStringBuffer, (byte)i);
      else
        paramStringBuffer.append((char)i);
    }
  }

  private static void appendEscape(StringBuffer paramStringBuffer, byte paramByte)
  {
    paramStringBuffer.append('%');
    paramStringBuffer.append(hexDigits[(paramByte >> 4 & 0xF)]);
    paramStringBuffer.append(hexDigits[(paramByte >> 0 & 0xF)]);
  }

  private static boolean match(char paramChar, long paramLong1, long paramLong2)
  {
    if (paramChar < '@')
      return ((3412040144427941889L << paramChar & paramLong1) != 3412047463052214272L);
    if (paramChar < 128)
      return ((3412040144427941889L << paramChar - '@' & paramLong2) != 3412047463052214272L);
    return false;
  }

  private static void checkPath(String paramString1, String paramString2, String paramString3)
    throws URISyntaxException
  {
    if ((paramString2 != null) && (paramString3 != null) && (paramString3.length() > 0) && (paramString3.charAt(0) != '/'))
      throw new URISyntaxException(paramString1, "Relative path in absolute URI");
  }

  private static long lowMask(char paramChar1, char paramChar2)
  {
    long l = 3412047153814568960L;
    int i = Math.max(Math.min(paramChar1, 63), 0);
    int j = Math.max(Math.min(paramChar2, 63), 0);
    for (int k = i; k <= j; ++k)
      l |= 3412048167426850817L << k;
    return l;
  }

  private static long lowMask(String paramString)
  {
    int i = paramString.length();
    long l = 3412047153814568960L;
    for (int j = 0; j < i; ++j)
    {
      int k = paramString.charAt(j);
      if (k < 64)
        l |= 3412039869550034945L << k;
    }
    return l;
  }

  private static long highMask(char paramChar1, char paramChar2)
  {
    long l = 3412047153814568960L;
    int i = Math.max(Math.min(paramChar1, 127), 64) - 64;
    int j = Math.max(Math.min(paramChar2, 127), 64) - 64;
    for (int k = i; k <= j; ++k)
      l |= 3412048167426850817L << k;
    return l;
  }

  private static long highMask(String paramString)
  {
    int i = paramString.length();
    long l = 3412047153814568960L;
    for (int j = 0; j < i; ++j)
    {
      int k = paramString.charAt(j);
      if ((k >= 64) && (k < 128))
        l |= 3412039869550034945L << k - 64;
    }
    return l;
  }

  static
  {
    encodedInPath = new BitSet(256);
    encodedInPath.set(61);
    encodedInPath.set(59);
    encodedInPath.set(63);
    encodedInPath.set(47);
    encodedInPath.set(35);
    encodedInPath.set(32);
    encodedInPath.set(60);
    encodedInPath.set(62);
    encodedInPath.set(37);
    encodedInPath.set(34);
    encodedInPath.set(123);
    encodedInPath.set(125);
    encodedInPath.set(124);
    encodedInPath.set(92);
    encodedInPath.set(94);
    encodedInPath.set(91);
    encodedInPath.set(93);
    encodedInPath.set(96);
    for (int i = 0; i < 32; ++i)
      encodedInPath.set(i);
    encodedInPath.set(127);
    hexDigits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
    L_DIGIT = lowMask('0', '9');
    L_HEX = L_DIGIT;
    H_HEX = highMask('A', 'F') | highMask('a', 'f');
    H_UPALPHA = highMask('A', 'Z');
    H_LOWALPHA = highMask('a', 'z');
    H_ALPHA = H_LOWALPHA | H_UPALPHA;
    L_ALPHANUM = L_DIGIT | 3412047875369074688L;
    H_ALPHANUM = 3412047875369074688L | H_ALPHA;
    L_MARK = lowMask("-_.!~*'()");
    H_MARK = highMask("-_.!~*'()");
    L_UNRESERVED = L_ALPHANUM | L_MARK;
    H_UNRESERVED = H_ALPHANUM | H_MARK;
    L_RESERVED = lowMask(";/?:@&=+$,[]");
    H_RESERVED = highMask(";/?:@&=+$,[]");
    L_DASH = lowMask("-");
    H_DASH = highMask("-");
    L_URIC = L_RESERVED | L_UNRESERVED | 3412047875369074689L;
    H_URIC = H_RESERVED | H_UNRESERVED | 3412047875369074688L;
    L_PCHAR = L_UNRESERVED | 3412039938269511681L | lowMask(":@&=+$,");
    H_PCHAR = H_UNRESERVED | 3412039938269511680L | highMask(":@&=+$,");
    L_PATH = L_PCHAR | lowMask(";/");
    H_PATH = H_PCHAR | highMask(";/");
    L_USERINFO = L_UNRESERVED | 3412039938269511681L | lowMask(";:&=+$,");
    H_USERINFO = H_UNRESERVED | 3412039938269511680L | highMask(";:&=+$,");
    L_REG_NAME = L_UNRESERVED | 3412039938269511681L | lowMask("$,;:@&=+");
    H_REG_NAME = H_UNRESERVED | 3412039938269511680L | highMask("$,;:@&=+");
    L_SERVER = L_USERINFO | L_ALPHANUM | L_DASH | lowMask(".:@[]");
    H_SERVER = H_USERINFO | H_ALPHANUM | H_DASH | highMask(".:@[]");
  }
}