package sun.security.x509;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.security.AccessController;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import sun.security.action.GetBooleanAction;
import sun.security.pkcs.PKCS9Attribute;
import sun.security.util.Debug;
import sun.security.util.DerEncoder;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;

public class AVA
  implements DerEncoder
{
  private static final Debug debug = Debug.getInstance("x509", "\t[AVA]");
  private static final boolean PRESERVE_OLD_DC_ENCODING = ((Boolean)AccessController.doPrivileged(new GetBooleanAction("com.sun.security.preserveOldDCEncoding"))).booleanValue();
  static final int DEFAULT = 1;
  static final int RFC1779 = 2;
  static final int RFC2253 = 3;
  final ObjectIdentifier oid;
  final DerValue value;
  private static final String specialChars = ",+=\n<>#;";
  private static final String specialChars2253 = ",+\"\\<>;";
  private static final String specialCharsAll = ",=\n+<>#;\\\" ";
  private static final String hexDigits = "0123456789ABCDEF";

  public AVA(ObjectIdentifier paramObjectIdentifier, DerValue paramDerValue)
  {
    if ((paramObjectIdentifier == null) || (paramDerValue == null))
      throw new NullPointerException();
    this.oid = paramObjectIdentifier;
    this.value = paramDerValue;
  }

  AVA(Reader paramReader)
    throws IOException
  {
    this(paramReader, 1);
  }

  AVA(Reader paramReader, Map<String, String> paramMap)
    throws IOException
  {
    this(paramReader, 1, paramMap);
  }

  AVA(Reader paramReader, int paramInt)
    throws IOException
  {
    this(paramReader, paramInt, Collections.EMPTY_MAP);
  }

  AVA(Reader paramReader, int paramInt, Map<String, String> paramMap)
    throws IOException
  {
    StringBuilder localStringBuilder = new StringBuilder();
    while (true)
    {
      i = readChar(paramReader, "Incorrect AVA format");
      if (i == 61)
        break;
      localStringBuilder.append((char)i);
    }
    this.oid = AVAKeyword.getOID(localStringBuilder.toString(), paramInt, paramMap);
    localStringBuilder.setLength(0);
    if (paramInt == 3)
    {
      i = paramReader.read();
      if (i != 32)
        break label111;
      throw new IOException("Incorrect AVA RFC2253 format - leading space must be escaped");
    }
    do
      i = paramReader.read();
    while ((i == 32) || (i == 10));
    if (i == -1)
    {
      label111: this.value = new DerValue("");
      return;
    }
    if (i == 35)
      this.value = parseHexString(paramReader, paramInt);
    else if ((i == 34) && (paramInt != 3))
      this.value = parseQuotedString(paramReader, localStringBuilder);
    else
      this.value = parseString(paramReader, i, paramInt, localStringBuilder);
  }

  public ObjectIdentifier getObjectIdentifier()
  {
    return this.oid;
  }

  public DerValue getDerValue()
  {
    return this.value;
  }

  public String getValueString()
  {
    String str;
    try
    {
      str = this.value.getAsString();
      if (str == null)
        throw new RuntimeException("AVA string is null");
      return str;
    }
    catch (IOException localIOException)
    {
      throw new RuntimeException("AVA error: " + localIOException, localIOException);
    }
  }

  private static DerValue parseHexString(Reader paramReader, int paramInt)
    throws IOException
  {
    ByteArrayOutputStream localByteArrayOutputStream = new ByteArrayOutputStream();
    int j = 0;
    int k = 0;
    while (true)
    {
      int i = paramReader.read();
      if (isTerminator(i, paramInt))
        break;
      int l = "0123456789ABCDEF".indexOf(Character.toUpperCase((char)i));
      if (l == -1)
        throw new IOException("AVA parse, invalid hex digit: " + (char)i);
      if (k % 2 == 1)
      {
        j = (byte)(j * 16 + (byte)l);
        localByteArrayOutputStream.write(j);
      }
      else
      {
        j = (byte)l;
      }
      ++k;
    }
    if (k == 0)
      throw new IOException("AVA parse, zero hex digits");
    if (k % 2 == 1)
      throw new IOException("AVA parse, odd number of hex digits");
    return new DerValue(localByteArrayOutputStream.toByteArray());
  }

  private DerValue parseQuotedString(Reader paramReader, StringBuilder paramStringBuilder)
    throws IOException
  {
    Object localObject;
    int i = readChar(paramReader, "Quoted string did not end in quote");
    ArrayList localArrayList = new ArrayList();
    boolean bool = true;
    while (true)
    {
      while (true)
      {
        if (i == 34)
          break label181;
        if (i != 92)
          break label123;
        i = readChar(paramReader, "Quoted string did not end in quote");
        localObject = null;
        if ((localObject = getEmbeddedHexPair(i, paramReader)) == null)
          break;
        bool = false;
        localArrayList.add(localObject);
        i = paramReader.read();
      }
      if ((i != 92) && (i != 34) && (",+=\n<>#;".indexOf((char)i) < 0))
        throw new IOException("Invalid escaped character in AVA: " + (char)i);
      if (localArrayList.size() > 0)
      {
        label123: localObject = getEmbeddedHexString(localArrayList);
        paramStringBuilder.append((String)localObject);
        localArrayList.clear();
      }
      bool &= DerValue.isPrintableStringChar((char)i);
      paramStringBuilder.append((char)i);
      i = readChar(paramReader, "Quoted string did not end in quote");
    }
    if (localArrayList.size() > 0)
    {
      label181: localObject = getEmbeddedHexString(localArrayList);
      paramStringBuilder.append((String)localObject);
      localArrayList.clear();
    }
    do
      i = paramReader.read();
    while ((i == 10) || (i == 32));
    if (i != -1)
      throw new IOException("AVA had characters other than whitespace after terminating quote");
    if ((this.oid.equals(PKCS9Attribute.EMAIL_ADDRESS_OID)) || ((this.oid.equals(X500Name.DOMAIN_COMPONENT_OID)) && (!(PRESERVE_OLD_DC_ENCODING))))
      return new DerValue(22, paramStringBuilder.toString().trim());
    if (bool)
      return new DerValue(paramStringBuilder.toString().trim());
    return ((DerValue)new DerValue(12, paramStringBuilder.toString().trim()));
  }

  private DerValue parseString(Reader paramReader, int paramInt1, int paramInt2, StringBuilder paramStringBuilder)
    throws IOException
  {
    ArrayList localArrayList = new ArrayList();
    boolean bool = true;
    int i = 0;
    int j = 1;
    int k = 0;
    do
    {
      i = 0;
      if (paramInt1 == 92)
      {
        i = 1;
        paramInt1 = readChar(paramReader, "Invalid trailing backslash");
        Byte localByte = null;
        if ((localByte = getEmbeddedHexPair(paramInt1, paramReader)) != null)
        {
          bool = false;
          localArrayList.add(localByte);
          paramInt1 = paramReader.read();
          j = 0;
          continue;
        }
        if (((paramInt2 == 1) && (",=\n+<>#;\\\" ".indexOf((char)paramInt1) == -1)) || ((paramInt2 == 2) && (",+=\n<>#;".indexOf((char)paramInt1) == -1) && (paramInt1 != 92) && (paramInt1 != 34)))
          throw new IOException("Invalid escaped character in AVA: '" + (char)paramInt1 + "'");
        if (paramInt2 == 3)
        {
          if (paramInt1 == 32)
          {
            if ((j != 0) || (trailingSpace(paramReader)))
              break label253;
            throw new IOException("Invalid escaped space character in AVA.  Only a leading or trailing space character can be escaped.");
          }
          if (paramInt1 == 35)
          {
            if (j != 0)
              break label253;
            throw new IOException("Invalid escaped '#' character in AVA.  Only a leading '#' can be escaped.");
          }
          label253: if (",+\"\\<>;".indexOf((char)paramInt1) == -1)
            throw new IOException("Invalid escaped character in AVA: '" + (char)paramInt1 + "'");
        }
      }
      else if ((paramInt2 == 3) && (",+\"\\<>;".indexOf((char)paramInt1) != -1))
      {
        throw new IOException("Character '" + (char)paramInt1 + "' in AVA appears without escape");
      }
      if (localArrayList.size() > 0)
      {
        for (int l = 0; l < k; ++l)
          paramStringBuilder.append(" ");
        k = 0;
        String str1 = getEmbeddedHexString(localArrayList);
        paramStringBuilder.append(str1);
        localArrayList.clear();
      }
      bool &= DerValue.isPrintableStringChar((char)paramInt1);
      if ((paramInt1 == 32) && (i == 0))
      {
        ++k;
      }
      else
      {
        for (int i1 = 0; i1 < k; ++i1)
          paramStringBuilder.append(" ");
        k = 0;
        paramStringBuilder.append((char)paramInt1);
      }
      paramInt1 = paramReader.read();
      j = 0;
    }
    while (!(isTerminator(paramInt1, paramInt2)));
    if ((paramInt2 == 3) && (k > 0))
      throw new IOException("Incorrect AVA RFC2253 format - trailing space must be escaped");
    if (localArrayList.size() > 0)
    {
      String str2 = getEmbeddedHexString(localArrayList);
      paramStringBuilder.append(str2);
      localArrayList.clear();
    }
    if ((this.oid.equals(PKCS9Attribute.EMAIL_ADDRESS_OID)) || ((this.oid.equals(X500Name.DOMAIN_COMPONENT_OID)) && (!(PRESERVE_OLD_DC_ENCODING))))
      return new DerValue(22, paramStringBuilder.toString());
    if (bool)
      return new DerValue(paramStringBuilder.toString());
    return new DerValue(12, paramStringBuilder.toString());
  }

  private static Byte getEmbeddedHexPair(int paramInt, Reader paramReader)
    throws IOException
  {
    if ("0123456789ABCDEF".indexOf(Character.toUpperCase((char)paramInt)) >= 0)
    {
      int i = readChar(paramReader, "unexpected EOF - escaped hex value must include two valid digits");
      if ("0123456789ABCDEF".indexOf(Character.toUpperCase((char)i)) >= 0)
      {
        int j = Character.digit((char)paramInt, 16);
        int k = Character.digit((char)i, 16);
        return new Byte((byte)((j << 4) + k));
      }
      throw new IOException("escaped hex value must include two valid digits");
    }
    return null;
  }

  private static String getEmbeddedHexString(List<Byte> paramList)
    throws IOException
  {
    int i = paramList.size();
    byte[] arrayOfByte = new byte[i];
    for (int j = 0; j < i; ++j)
      arrayOfByte[j] = ((Byte)paramList.get(j)).byteValue();
    return new String(arrayOfByte, "UTF8");
  }

  private static boolean isTerminator(int paramInt1, int paramInt2)
  {
    switch (paramInt1)
    {
    case -1:
    case 43:
    case 44:
      return true;
    case 59:
    case 62:
      return (paramInt2 != 3);
    }
    return false;
  }

  private static int readChar(Reader paramReader, String paramString)
    throws IOException
  {
    int i = paramReader.read();
    if (i == -1)
      throw new IOException(paramString);
    return i;
  }

  private static boolean trailingSpace(Reader paramReader)
    throws IOException
  {
    int i = 0;
    if (!(paramReader.markSupported()))
      return true;
    paramReader.mark(9999);
    while (true)
    {
      int j;
      while (true)
      {
        j = paramReader.read();
        if (j == -1)
        {
          i = 1;
          break label75:
        }
        if (j != 32)
          break;
      }
      if (j == 92)
      {
        int k = paramReader.read();
        if (k != 32)
        {
          i = 0;
          break;
        }
      }
      else
      {
        i = 0;
        break;
      }
    }
    label75: paramReader.reset();
    return i;
  }

  AVA(DerValue paramDerValue)
    throws IOException
  {
    if (paramDerValue.tag != 48)
      throw new IOException("AVA not a sequence");
    this.oid = X500Name.intern(paramDerValue.data.getOID());
    this.value = paramDerValue.data.getDerValue();
    if (paramDerValue.data.available() != 0)
      throw new IOException("AVA, extra bytes = " + paramDerValue.data.available());
  }

  AVA(DerInputStream paramDerInputStream)
    throws IOException
  {
    this(paramDerInputStream.getDerValue());
  }

  public boolean equals(Object paramObject)
  {
    if (this == paramObject)
      return true;
    if (!(paramObject instanceof AVA))
      return false;
    AVA localAVA = (AVA)paramObject;
    return toRFC2253CanonicalString().equals(localAVA.toRFC2253CanonicalString());
  }

  public int hashCode()
  {
    return toRFC2253CanonicalString().hashCode();
  }

  public void encode(DerOutputStream paramDerOutputStream)
    throws IOException
  {
    derEncode(paramDerOutputStream);
  }

  public void derEncode(OutputStream paramOutputStream)
    throws IOException
  {
    DerOutputStream localDerOutputStream1 = new DerOutputStream();
    DerOutputStream localDerOutputStream2 = new DerOutputStream();
    localDerOutputStream1.putOID(this.oid);
    this.value.encode(localDerOutputStream1);
    localDerOutputStream2.write(48, localDerOutputStream1);
    paramOutputStream.write(localDerOutputStream2.toByteArray());
  }

  private String toKeyword(int paramInt, Map<String, String> paramMap)
  {
    return AVAKeyword.getKeyword(this.oid, paramInt, paramMap);
  }

  public String toString()
  {
    return toKeywordValueString(toKeyword(1, Collections.EMPTY_MAP));
  }

  public String toRFC1779String()
  {
    return toRFC1779String(Collections.EMPTY_MAP);
  }

  public String toRFC1779String(Map<String, String> paramMap)
  {
    return toKeywordValueString(toKeyword(2, paramMap));
  }

  public String toRFC2253String()
  {
    return toRFC2253String(Collections.EMPTY_MAP);
  }

  public String toRFC2253String(Map<String, String> paramMap)
  {
    Object localObject;
    StringBuilder localStringBuilder1 = new StringBuilder(100);
    localStringBuilder1.append(toKeyword(3, paramMap));
    localStringBuilder1.append('=');
    if (((localStringBuilder1.charAt(0) >= '0') && (localStringBuilder1.charAt(0) <= '9')) || (!(isDerString(this.value, false))))
    {
      localObject = null;
      try
      {
        localObject = this.value.toByteArray();
      }
      catch (IOException localIOException1)
      {
        throw new IllegalArgumentException("DER Value conversion");
      }
      localStringBuilder1.append('#');
      for (int i = 0; i < localObject.length; ++i)
      {
        int j = localObject[i];
        localStringBuilder1.append(Character.forDigit(0xF & j >>> 4, 16));
        localStringBuilder1.append(Character.forDigit(0xF & j, 16));
      }
    }
    else
    {
      char c;
      localObject = null;
      try
      {
        localObject = new String(this.value.getDataBytes(), "UTF8");
      }
      catch (IOException localIOException2)
      {
        throw new IllegalArgumentException("DER Value conversion");
      }
      StringBuilder localStringBuilder2 = new StringBuilder();
      for (int k = 0; k < ((String)localObject).length(); ++k)
      {
        l = ((String)localObject).charAt(k);
        if ((DerValue.isPrintableStringChar(l)) || (",=+<>#;\"\\".indexOf(l) >= 0))
        {
          if (",=+<>#;\"\\".indexOf(l) >= 0)
            localStringBuilder2.append('\\');
          localStringBuilder2.append(l);
        }
        else if ((debug != null) && (Debug.isOn("ava")))
        {
          byte[] arrayOfByte = null;
          try
          {
            arrayOfByte = Character.toString(l).getBytes("UTF8");
          }
          catch (IOException localIOException3)
          {
            throw new IllegalArgumentException("DER Value conversion");
          }
          for (i2 = 0; i2 < arrayOfByte.length; ++i2)
          {
            localStringBuilder2.append('\\');
            c = Character.forDigit(0xF & arrayOfByte[i2] >>> 4, 16);
            localStringBuilder2.append(Character.toUpperCase(c));
            c = Character.forDigit(0xF & arrayOfByte[i2], 16);
            localStringBuilder2.append(Character.toUpperCase(c));
          }
        }
        else
        {
          localStringBuilder2.append(l);
        }
      }
      char[] arrayOfChar = localStringBuilder2.toString().toCharArray();
      localStringBuilder2 = new StringBuilder();
      for (int l = 0; l < arrayOfChar.length; ++l)
        if ((arrayOfChar[l] != ' ') && (arrayOfChar[l] != '\r'))
          break;
      for (int i1 = arrayOfChar.length - 1; i1 >= 0; --i1)
        if ((arrayOfChar[i1] != ' ') && (arrayOfChar[i1] != '\r'))
          break;
      for (int i2 = 0; i2 < arrayOfChar.length; ++i2)
      {
        c = arrayOfChar[i2];
        if ((i2 < l) || (i2 > i1))
          localStringBuilder2.append('\\');
        localStringBuilder2.append(c);
      }
      localStringBuilder1.append(localStringBuilder2.toString());
    }
    return ((String)localStringBuilder1.toString());
  }

  public String toRFC2253CanonicalString()
  {
    StringBuilder localStringBuilder1 = new StringBuilder(40);
    localStringBuilder1.append(toKeyword(3, Collections.EMPTY_MAP));
    localStringBuilder1.append('=');
    if (((localStringBuilder1.charAt(0) >= '0') && (localStringBuilder1.charAt(0) <= '9')) || (!(isDerString(this.value, true))))
    {
      localObject = null;
      try
      {
        localObject = this.value.toByteArray();
      }
      catch (IOException localIOException1)
      {
        throw new IllegalArgumentException("DER Value conversion");
      }
      localStringBuilder1.append('#');
      for (int i = 0; i < localObject.length; ++i)
      {
        int j = localObject[i];
        localStringBuilder1.append(Character.forDigit(0xF & j >>> 4, 16));
        localStringBuilder1.append(Character.forDigit(0xF & j, 16));
      }
    }
    else
    {
      localObject = null;
      try
      {
        localObject = new String(this.value.getDataBytes(), "UTF8");
      }
      catch (IOException localIOException2)
      {
        throw new IllegalArgumentException("DER Value conversion");
      }
      StringBuilder localStringBuilder2 = new StringBuilder();
      int k = 0;
      for (int l = 0; l < ((String)localObject).length(); ++l)
      {
        char c = ((String)localObject).charAt(l);
        if ((DerValue.isPrintableStringChar(c)) || (",+<>;\"\\".indexOf(c) >= 0) || ((l == 0) && (c == '#')))
        {
          if (((l == 0) && (c == '#')) || (",+<>;\"\\".indexOf(c) >= 0))
            localStringBuilder2.append('\\');
          if (!(Character.isWhitespace(c)))
          {
            k = 0;
            localStringBuilder2.append(c);
          }
          else if (k == 0)
          {
            k = 1;
            localStringBuilder2.append(c);
          }
        }
        else if ((debug != null) && (Debug.isOn("ava")))
        {
          k = 0;
          byte[] arrayOfByte = null;
          try
          {
            arrayOfByte = Character.toString(c).getBytes("UTF8");
          }
          catch (IOException localIOException3)
          {
            throw new IllegalArgumentException("DER Value conversion");
          }
          for (int i1 = 0; i1 < arrayOfByte.length; ++i1)
          {
            localStringBuilder2.append('\\');
            localStringBuilder2.append(Character.forDigit(0xF & arrayOfByte[i1] >>> 4, 16));
            localStringBuilder2.append(Character.forDigit(0xF & arrayOfByte[i1], 16));
          }
        }
        else
        {
          k = 0;
          localStringBuilder2.append(c);
        }
      }
      localStringBuilder1.append(localStringBuilder2.toString().trim());
    }
    Object localObject = localStringBuilder1.toString();
    localObject = ((String)localObject).toUpperCase(Locale.US).toLowerCase(Locale.US);
    return ((String)Normalizer.normalize((CharSequence)localObject, Normalizer.Form.NFKD));
  }

  private static boolean isDerString(DerValue paramDerValue, boolean paramBoolean)
  {
    if (paramBoolean)
    {
      switch (paramDerValue.tag)
      {
      case 12:
      case 19:
        return true;
      }
      return false;
    }
    switch (paramDerValue.tag)
    {
    case 12:
    case 19:
    case 20:
    case 22:
    case 27:
    case 30:
      return true;
    case 13:
    case 14:
    case 15:
    case 16:
    case 17:
    case 18:
    case 21:
    case 23:
    case 24:
    case 25:
    case 26:
    case 28:
    case 29:
    }
    return false;
  }

  boolean hasRFC2253Keyword()
  {
    return AVAKeyword.hasKeyword(this.oid, 3);
  }

  private String toKeywordValueString(String paramString)
  {
    StringBuilder localStringBuilder1 = new StringBuilder(40);
    localStringBuilder1.append(paramString);
    localStringBuilder1.append("=");
    try
    {
      String str = this.value.getAsString();
      if (str == null)
      {
        byte[] arrayOfByte1 = this.value.toByteArray();
        localStringBuilder1.append('#');
        for (int j = 0; j < arrayOfByte1.length; ++j)
        {
          localStringBuilder1.append("0123456789ABCDEF".charAt(arrayOfByte1[j] >> 4 & 0xF));
          localStringBuilder1.append("0123456789ABCDEF".charAt(arrayOfByte1[j] & 0xF));
        }
      }
      else
      {
        int i = 0;
        StringBuilder localStringBuilder2 = new StringBuilder();
        int k = 0;
        for (int l = 0; l < str.length(); ++l)
        {
          char c1 = str.charAt(l);
          if ((DerValue.isPrintableStringChar(c1)) || (",+=\n<>#;\\\"".indexOf(c1) >= 0))
          {
            if ((i == 0) && ((((l == 0) && (((c1 == ' ') || (c1 == '\n')))) || (",+=\n<>#;\\\"".indexOf(c1) >= 0))))
              i = 1;
            if ((c1 != ' ') && (c1 != '\n'))
            {
              if ((c1 == '"') || (c1 == '\\'))
                localStringBuilder2.append('\\');
              k = 0;
            }
            else
            {
              if ((i == 0) && (k != 0))
                i = 1;
              k = 1;
            }
            localStringBuilder2.append(c1);
          }
          else if ((debug != null) && (Debug.isOn("ava")))
          {
            k = 0;
            byte[] arrayOfByte2 = Character.toString(c1).getBytes("UTF8");
            for (int i1 = 0; i1 < arrayOfByte2.length; ++i1)
            {
              localStringBuilder2.append('\\');
              char c2 = Character.forDigit(0xF & arrayOfByte2[i1] >>> 4, 16);
              localStringBuilder2.append(Character.toUpperCase(c2));
              c2 = Character.forDigit(0xF & arrayOfByte2[i1], 16);
              localStringBuilder2.append(Character.toUpperCase(c2));
            }
          }
          else
          {
            k = 0;
            localStringBuilder2.append(c1);
          }
        }
        if (localStringBuilder2.length() > 0)
        {
          l = localStringBuilder2.charAt(localStringBuilder2.length() - 1);
          if ((l == 32) || (l == 10))
            i = 1;
        }
        if (i != 0)
          localStringBuilder1.append("\"" + localStringBuilder2.toString() + "\"");
        else
          localStringBuilder1.append(localStringBuilder2.toString());
      }
    }
    catch (IOException localIOException)
    {
      throw new IllegalArgumentException("DER Value conversion");
    }
    return localStringBuilder1.toString();
  }
}