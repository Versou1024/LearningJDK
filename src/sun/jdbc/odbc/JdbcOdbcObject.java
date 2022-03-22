package sun.jdbc.odbc;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.IllegalCharsetNameException;

public class JdbcOdbcObject
{
  protected static void dumpByte(byte[] paramArrayOfByte, int paramInt)
  {
    for (int i = 0; i * 16 < paramInt; ++i)
    {
      String str1 = toHex(i * 16);
      String str2 = "";
      for (int j = 0; j < 16; ++j)
      {
        int k = i * 16 + j;
        if (k >= paramInt)
        {
          str1 = "  ";
          str2 = str2 + " ";
        }
        else
        {
          str1 = toHex(paramArrayOfByte[k]);
          str1 = hexPad(str1, 2);
          if ((paramArrayOfByte[k] < 32) || (paramArrayOfByte[k] > 128))
            str2 = str2 + ".";
          else
            str2 = str2 + new String(paramArrayOfByte, k, 1);
        }
      }
    }
  }

  public static String hexPad(String paramString, int paramInt)
  {
    if (!(paramString.startsWith("0x")))
      return paramString;
    Object localObject = paramString.substring(2);
    int i = ((String)localObject).length();
    if (i > paramInt)
    {
      localObject = ((String)localObject).substring(i - paramInt);
    }
    else if (i < paramInt)
    {
      String str1 = "0000000000000000";
      String str2 = str1.substring(0, paramInt - i) + ((String)localObject);
      localObject = str2;
    }
    localObject = ((String)localObject).toUpperCase();
    return ((String)localObject);
  }

  public static String toHex(int paramInt)
  {
    char[] arrayOfChar = new char[8];
    String str = "0123456789ABCDEF";
    for (int j = 0; j < 4; ++j)
    {
      int i = (byte)(paramInt & 0xFF);
      arrayOfChar[(6 - j * 2)] = str.charAt(i >> 4 & 0xF);
      arrayOfChar[(7 - j * 2)] = str.charAt(i & 0xF);
      paramInt >>= 8;
    }
    return "0x" + new String(arrayOfChar);
  }

  public static byte[] hexStringToByteArray(String paramString)
    throws NumberFormatException
  {
    int i = paramString.length();
    int j = (i + 1) / 2;
    byte[] arrayOfByte = new byte[j];
    for (int k = 0; k < j; ++k)
      arrayOfByte[k] = (byte)hexPairToInt(paramString.substring(k * 2, (k + 1) * 2));
    return arrayOfByte;
  }

  public static int hexPairToInt(String paramString)
    throws NumberFormatException
  {
    String str1 = "0123456789ABCDEF";
    String str2 = paramString.toUpperCase();
    int i = 0;
    int j = 0;
    int k = str2.length();
    if (k > 2)
      k = 2;
    for (int l = 0; l < k; ++l)
    {
      j = str1.indexOf(str2.substring(l, l + 1));
      if (j < 0)
        throw new NumberFormatException();
      if (l == 0)
        j *= 16;
      i += j;
    }
    return i;
  }

  public String BytesToChars(String paramString, byte[] paramArrayOfByte)
    throws UnsupportedEncodingException
  {
    String str = new String();
    try
    {
      str = Charset.forName(paramString).newDecoder().onMalformedInput(CodingErrorAction.REPLACE).onUnmappableCharacter(CodingErrorAction.REPLACE).replaceWith("?").decode(ByteBuffer.wrap(paramArrayOfByte)).toString();
    }
    catch (IllegalCharsetNameException localIllegalCharsetNameException)
    {
      throw new UnsupportedEncodingException(paramString);
    }
    catch (IllegalStateException localIllegalStateException)
    {
    }
    catch (CharacterCodingException localCharacterCodingException)
    {
    }
    char[] arrayOfChar1 = str.toCharArray();
    for (int i = 0; i < arrayOfChar1.length; ++i)
      if (arrayOfChar1[i] == 0)
        break;
    char[] arrayOfChar2 = new char[i];
    System.arraycopy(arrayOfChar1, 0, arrayOfChar2, 0, i);
    str = new String(arrayOfChar2);
    return str;
  }

  public byte[] CharsToBytes(String paramString, char[] paramArrayOfChar)
    throws UnsupportedEncodingException
  {
    char[] arrayOfChar;
    try
    {
      arrayOfChar = new char[paramArrayOfChar.length + 1];
      System.arraycopy(paramArrayOfChar, 0, arrayOfChar, 0, paramArrayOfChar.length);
      ByteBuffer localByteBuffer = Charset.forName(paramString).newEncoder().onMalformedInput(CodingErrorAction.REPLACE).onUnmappableCharacter(CodingErrorAction.REPLACE).replaceWith(new byte[] { 63 }).encode(CharBuffer.wrap(arrayOfChar));
      byte[] arrayOfByte = new byte[localByteBuffer.limit()];
      System.arraycopy(localByteBuffer.array(), 0, arrayOfByte, 0, localByteBuffer.limit());
      return arrayOfByte;
    }
    catch (IllegalCharsetNameException localIllegalCharsetNameException)
    {
      throw new UnsupportedEncodingException(paramString);
    }
    catch (IllegalStateException localIllegalStateException)
    {
      localIllegalStateException.printStackTrace();
    }
    catch (CharacterCodingException localCharacterCodingException)
    {
      localCharacterCodingException.printStackTrace();
    }
    return new byte[0];
  }
}