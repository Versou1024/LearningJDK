package sun.nio.cs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CharsetMapping
{
  public static final char UNMAPPABLE_DECODING = 65533;
  public static final int UNMAPPABLE_ENCODING = 65533;
  char[] b2cSB;
  char[] b2cDB1;
  char[] b2cDB2;
  int b2Min;
  int b2Max;
  int b1MinDB1;
  int b1MaxDB1;
  int b1MinDB2;
  int b1MaxDB2;
  int dbSegSize;
  char[] c2b;
  char[] c2bIndex;
  char[] b2cSupp;
  char[] c2bSupp;
  Entry[] b2cComp;
  Entry[] c2bComp;
  static Comparator<Entry> comparatorBytes = new Comparator()
  {
    public int compare(CharsetMapping.Entry paramEntry1, CharsetMapping.Entry paramEntry2)
    {
      return (paramEntry1.bs - paramEntry2.bs);
    }

    public boolean equals(Object paramObject)
    {
      return (this == paramObject);
    }
  };
  static Comparator<Entry> comparatorCP = new Comparator()
  {
    public int compare(CharsetMapping.Entry paramEntry1, CharsetMapping.Entry paramEntry2)
    {
      return (paramEntry1.cp - paramEntry2.cp);
    }

    public boolean equals(Object paramObject)
    {
      return (this == paramObject);
    }
  };
  static Comparator<Entry> comparatorComp = new Comparator()
  {
    public int compare(CharsetMapping.Entry paramEntry1, CharsetMapping.Entry paramEntry2)
    {
      int i = paramEntry1.cp - paramEntry2.cp;
      if (i == 0)
        i = paramEntry1.cp2 - paramEntry2.cp2;
      return i;
    }

    public boolean equals(Object paramObject)
    {
      return (this == paramObject);
    }
  };
  private static final int MAP_SINGLEBYTE = 1;
  private static final int MAP_DOUBLEBYTE1 = 2;
  private static final int MAP_DOUBLEBYTE2 = 3;
  private static final int MAP_SUPPLEMENT = 5;
  private static final int MAP_SUPPLEMENT_C2B = 6;
  private static final int MAP_COMPOSITE = 7;
  private static final int MAP_INDEXC2B = 8;
  int off = 0;
  byte[] bb;

  public char decodeSingle(int paramInt)
  {
    return this.b2cSB[paramInt];
  }

  public char decodeDouble(int paramInt1, int paramInt2)
  {
    if ((paramInt2 >= this.b2Min) && (paramInt2 < this.b2Max))
    {
      paramInt2 -= this.b2Min;
      if ((paramInt1 >= this.b1MinDB1) && (paramInt1 <= this.b1MaxDB1))
      {
        paramInt1 -= this.b1MinDB1;
        return this.b2cDB1[(paramInt1 * this.dbSegSize + paramInt2)];
      }
      if ((paramInt1 >= this.b1MinDB2) && (paramInt1 <= this.b1MaxDB2))
      {
        paramInt1 -= this.b1MinDB2;
        return this.b2cDB2[(paramInt1 * this.dbSegSize + paramInt2)];
      }
    }
    return 65533;
  }

  public char[] decodeSurrogate(int paramInt, char[] paramArrayOfChar)
  {
    int i = this.b2cSupp.length / 2;
    int j = Arrays.binarySearch(this.b2cSupp, 0, i, (char)paramInt);
    if (j >= 0)
    {
      Character.toChars(this.b2cSupp[(i + j)] + 131072, paramArrayOfChar, 0);
      return paramArrayOfChar;
    }
    return null;
  }

  public char[] decodeComposite(Entry paramEntry, char[] paramArrayOfChar)
  {
    int i = findBytes(this.b2cComp, paramEntry);
    if (i >= 0)
    {
      paramArrayOfChar[0] = (char)this.b2cComp[i].cp;
      paramArrayOfChar[1] = (char)this.b2cComp[i].cp2;
      return paramArrayOfChar;
    }
    return null;
  }

  public int encodeChar(char paramChar)
  {
    int i = this.c2bIndex[(paramChar >> '\b')];
    if (i == 65535)
      return 65533;
    return this.c2b[(i + (paramChar & 0xFF))];
  }

  public int encodeSurrogate(char paramChar1, char paramChar2)
  {
    int i = Character.toCodePoint(paramChar1, paramChar2);
    if ((i < 131072) || (i >= 196608))
      return 65533;
    int j = this.c2bSupp.length / 2;
    int k = Arrays.binarySearch(this.c2bSupp, 0, j, (char)i);
    if (k >= 0)
      return this.c2bSupp[(j + k)];
    return 65533;
  }

  public boolean isCompositeBase(Entry paramEntry)
  {
    if ((paramEntry.cp <= 12791) && (paramEntry.cp >= 230))
      return (findCP(this.c2bComp, paramEntry) >= 0);
    return false;
  }

  public int encodeComposite(Entry paramEntry)
  {
    int i = findComp(this.c2bComp, paramEntry);
    if (i >= 0)
      return this.c2bComp[i].bs;
    return 65533;
  }

  public static CharsetMapping get(Class paramClass, String paramString)
  {
    return ((CharsetMapping)AccessController.doPrivileged(new PrivilegedAction(paramClass, paramString)
    {
      public CharsetMapping run()
      {
        return new CharsetMapping().load(this.val$clz.getResourceAsStream(this.val$name));
      }
    }));
  }

  static int findBytes(Entry[] paramArrayOfEntry, Entry paramEntry)
  {
    return Arrays.binarySearch(paramArrayOfEntry, 0, paramArrayOfEntry.length, paramEntry, comparatorBytes);
  }

  static int findCP(Entry[] paramArrayOfEntry, Entry paramEntry)
  {
    return Arrays.binarySearch(paramArrayOfEntry, 0, paramArrayOfEntry.length, paramEntry, comparatorCP);
  }

  static int findComp(Entry[] paramArrayOfEntry, Entry paramEntry)
  {
    return Arrays.binarySearch(paramArrayOfEntry, 0, paramArrayOfEntry.length, paramEntry, comparatorComp);
  }

  private static final void writeShort(OutputStream paramOutputStream, int paramInt)
    throws IOException
  {
    paramOutputStream.write(paramInt >>> 8 & 0xFF);
    paramOutputStream.write(paramInt & 0xFF);
  }

  private static final void writeShortArray(OutputStream paramOutputStream, int paramInt1, int[] paramArrayOfInt, int paramInt2, int paramInt3)
    throws IOException
  {
    writeShort(paramOutputStream, paramInt1);
    writeShort(paramOutputStream, paramInt3);
    for (int i = paramInt2; i < paramInt3; ++i)
      writeShort(paramOutputStream, paramArrayOfInt[(paramInt2 + i)]);
  }

  public static final void writeSIZE(OutputStream paramOutputStream, int paramInt)
    throws IOException
  {
    paramOutputStream.write(paramInt >>> 24 & 0xFF);
    paramOutputStream.write(paramInt >>> 16 & 0xFF);
    paramOutputStream.write(paramInt >>> 8 & 0xFF);
    paramOutputStream.write(paramInt & 0xFF);
  }

  public static void writeINDEXC2B(OutputStream paramOutputStream, int[] paramArrayOfInt)
    throws IOException
  {
    writeShort(paramOutputStream, 8);
    writeShort(paramOutputStream, paramArrayOfInt.length);
    int i = 0;
    for (int j = 0; j < paramArrayOfInt.length; ++j)
      if (paramArrayOfInt[j] != 0)
      {
        writeShort(paramOutputStream, i);
        i += 256;
      }
      else
      {
        writeShort(paramOutputStream, -1);
      }
  }

  public static void writeSINGLEBYTE(OutputStream paramOutputStream, int[] paramArrayOfInt)
    throws IOException
  {
    writeShortArray(paramOutputStream, 1, paramArrayOfInt, 0, 256);
  }

  private static void writeDOUBLEBYTE(OutputStream paramOutputStream, int paramInt1, int[] paramArrayOfInt, int paramInt2, int paramInt3, int paramInt4, int paramInt5)
    throws IOException
  {
    writeShort(paramOutputStream, paramInt1);
    writeShort(paramOutputStream, paramInt2);
    writeShort(paramOutputStream, paramInt3);
    writeShort(paramOutputStream, paramInt4);
    writeShort(paramOutputStream, paramInt5);
    writeShort(paramOutputStream, (paramInt3 - paramInt2 + 1) * (paramInt5 - paramInt4 + 1));
    for (int i = paramInt2; i <= paramInt3; ++i)
      for (int j = paramInt4; j <= paramInt5; ++j)
        writeShort(paramOutputStream, paramArrayOfInt[(i * 256 + j)]);
  }

  public static void writeDOUBLEBYTE1(OutputStream paramOutputStream, int[] paramArrayOfInt, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
    throws IOException
  {
    writeDOUBLEBYTE(paramOutputStream, 2, paramArrayOfInt, paramInt1, paramInt2, paramInt3, paramInt4);
  }

  public static void writeDOUBLEBYTE2(OutputStream paramOutputStream, int[] paramArrayOfInt, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
    throws IOException
  {
    writeDOUBLEBYTE(paramOutputStream, 3, paramArrayOfInt, paramInt1, paramInt2, paramInt3, paramInt4);
  }

  public static void writeSUPPLEMENT(OutputStream paramOutputStream, Entry[] paramArrayOfEntry, int paramInt)
    throws IOException
  {
    writeShort(paramOutputStream, 5);
    writeShort(paramOutputStream, paramInt * 2);
    for (int i = 0; i < paramInt; ++i)
      writeShort(paramOutputStream, paramArrayOfEntry[i].bs);
    for (int j = 0; j < paramInt; ++j)
      writeShort(paramOutputStream, paramArrayOfEntry[j].cp);
    writeShort(paramOutputStream, 6);
    writeShort(paramOutputStream, paramInt * 2);
    Arrays.sort(paramArrayOfEntry, 0, paramInt, comparatorCP);
    for (int k = 0; k < paramInt; ++k)
      writeShort(paramOutputStream, paramArrayOfEntry[k].cp);
    for (int l = 0; l < paramInt; ++l)
      writeShort(paramOutputStream, paramArrayOfEntry[l].bs);
  }

  public static void writeCOMPOSITE(OutputStream paramOutputStream, Entry[] paramArrayOfEntry, int paramInt)
    throws IOException
  {
    writeShort(paramOutputStream, 7);
    writeShort(paramOutputStream, paramInt * 3);
    for (int i = 0; i < paramInt; ++i)
    {
      writeShort(paramOutputStream, (char)paramArrayOfEntry[i].bs);
      writeShort(paramOutputStream, (char)paramArrayOfEntry[i].cp);
      writeShort(paramOutputStream, (char)paramArrayOfEntry[i].cp2);
    }
  }

  private static final boolean readNBytes(InputStream paramInputStream, byte[] paramArrayOfByte, int paramInt)
    throws IOException
  {
    int i = 0;
    while (paramInt > 0)
    {
      int j = paramInputStream.read(paramArrayOfByte, i, paramInt);
      if (j == -1)
        return false;
      paramInt -= j;
      i += j;
    }
    return true;
  }

  private char[] readCharArray()
  {
    int i = (this.bb[(this.off++)] & 0xFF) << 8 | this.bb[(this.off++)] & 0xFF;
    char[] arrayOfChar = new char[i];
    for (int j = 0; j < i; ++j)
      arrayOfChar[j] = (char)((this.bb[(this.off++)] & 0xFF) << 8 | this.bb[(this.off++)] & 0xFF);
    return arrayOfChar;
  }

  void readSINGLEBYTE()
  {
    char[] arrayOfChar = readCharArray();
    for (int i = 0; i < arrayOfChar.length; ++i)
    {
      int j = arrayOfChar[i];
      if (j != 65533)
        this.c2b[(this.c2bIndex[(j >> 8)] + (j & 0xFF))] = (char)i;
    }
    this.b2cSB = arrayOfChar;
  }

  void readINDEXC2B()
  {
    char[] arrayOfChar = readCharArray();
    for (int i = arrayOfChar.length - 1; i >= 0; --i)
      if ((this.c2b == null) && (arrayOfChar[i] != '￿FF))
      {
        this.c2b = new char[arrayOfChar[i] + 256];
        Arrays.fill(this.c2b, 65533);
        break;
      }
    this.c2bIndex = arrayOfChar;
  }

  char[] readDB(int paramInt1, int paramInt2, int paramInt3)
  {
    char[] arrayOfChar = readCharArray();
    for (int i = 0; i < arrayOfChar.length; ++i)
    {
      int j = arrayOfChar[i];
      if (j != 65533)
      {
        int k = i / paramInt3;
        int l = i % paramInt3;
        int i1 = (k + paramInt1) * 256 + l + paramInt2;
        this.c2b[(this.c2bIndex[(j >> 8)] + (j & 0xFF))] = (char)i1;
      }
    }
    return arrayOfChar;
  }

  void readDOUBLEBYTE1()
  {
    this.b1MinDB1 = ((this.bb[(this.off++)] & 0xFF) << 8 | this.bb[(this.off++)] & 0xFF);
    this.b1MaxDB1 = ((this.bb[(this.off++)] & 0xFF) << 8 | this.bb[(this.off++)] & 0xFF);
    this.b2Min = ((this.bb[(this.off++)] & 0xFF) << 8 | this.bb[(this.off++)] & 0xFF);
    this.b2Max = ((this.bb[(this.off++)] & 0xFF) << 8 | this.bb[(this.off++)] & 0xFF);
    this.dbSegSize = (this.b2Max - this.b2Min + 1);
    this.b2cDB1 = readDB(this.b1MinDB1, this.b2Min, this.dbSegSize);
  }

  void readDOUBLEBYTE2()
  {
    this.b1MinDB2 = ((this.bb[(this.off++)] & 0xFF) << 8 | this.bb[(this.off++)] & 0xFF);
    this.b1MaxDB2 = ((this.bb[(this.off++)] & 0xFF) << 8 | this.bb[(this.off++)] & 0xFF);
    this.b2Min = ((this.bb[(this.off++)] & 0xFF) << 8 | this.bb[(this.off++)] & 0xFF);
    this.b2Max = ((this.bb[(this.off++)] & 0xFF) << 8 | this.bb[(this.off++)] & 0xFF);
    this.dbSegSize = (this.b2Max - this.b2Min + 1);
    this.b2cDB2 = readDB(this.b1MinDB2, this.b2Min, this.dbSegSize);
  }

  void readCOMPOSITE()
  {
    char[] arrayOfChar = readCharArray();
    int i = arrayOfChar.length / 3;
    this.b2cComp = new Entry[i];
    this.c2bComp = new Entry[i];
    int j = 0;
    int k = 0;
    while (j < i)
    {
      Entry localEntry = new Entry();
      localEntry.bs = arrayOfChar[(k++)];
      localEntry.cp = arrayOfChar[(k++)];
      localEntry.cp2 = arrayOfChar[(k++)];
      this.b2cComp[j] = localEntry;
      this.c2bComp[j] = localEntry;
      ++j;
    }
    Arrays.sort(this.c2bComp, 0, this.c2bComp.length, comparatorComp);
  }

  CharsetMapping load(InputStream paramInputStream)
  {
    int i;
    try
    {
      i = (paramInputStream.read() & 0xFF) << 24 | (paramInputStream.read() & 0xFF) << 16 | (paramInputStream.read() & 0xFF) << 8 | paramInputStream.read() & 0xFF;
      this.bb = new byte[i];
      this.off = 0;
      if (!(readNBytes(paramInputStream, this.bb, i)))
        throw new RuntimeException("Corrupted data file");
      paramInputStream.close();
      while (this.off < i)
      {
        int j = (this.bb[(this.off++)] & 0xFF) << 8 | this.bb[(this.off++)] & 0xFF;
        switch (j)
        {
        case 8:
          readINDEXC2B();
          break;
        case 1:
          readSINGLEBYTE();
          break;
        case 2:
          readDOUBLEBYTE1();
          break;
        case 3:
          readDOUBLEBYTE2();
          break;
        case 5:
          this.b2cSupp = readCharArray();
          break;
        case 6:
          this.c2bSupp = readCharArray();
          break;
        case 7:
          readCOMPOSITE();
          break;
        case 4:
        default:
          throw new RuntimeException("Corrupted data file");
        }
      }
      this.bb = null;
      return this;
    }
    catch (IOException localIOException)
    {
      localIOException.printStackTrace();
    }
    return null;
  }

  public static class Entry
  {
    public int bs;
    public int cp;
    public int cp2;

    public Entry()
    {
    }

    public Entry(int paramInt1, int paramInt2, int paramInt3)
    {
      this.bs = paramInt1;
      this.cp = paramInt2;
      this.cp2 = paramInt3;
    }
  }

  public static class Parser
  {
    static final Pattern basic = Pattern.compile("(?:0x)?(\\p{XDigit}++)\\s++(?:0x)?(\\p{XDigit}++)?\\s*+.*");
    static final int gBS = 1;
    static final int gCP = 2;
    static final int gCP2 = 3;
    BufferedReader reader;
    boolean closed;
    Matcher matcher;
    int gbs;
    int gcp;
    int gcp2;

    public Parser(InputStream paramInputStream, Pattern paramPattern, int paramInt1, int paramInt2, int paramInt3)
      throws IOException
    {
      this.reader = new BufferedReader(new InputStreamReader(paramInputStream));
      this.closed = false;
      this.matcher = paramPattern.matcher("");
      this.gbs = paramInt1;
      this.gcp = paramInt2;
      this.gcp2 = paramInt3;
    }

    public Parser(InputStream paramInputStream, Pattern paramPattern)
      throws IOException
    {
      this(paramInputStream, paramPattern, 1, 2, 3);
    }

    public Parser(InputStream paramInputStream)
      throws IOException
    {
      this(paramInputStream, basic, 1, 2, 3);
    }

    protected boolean isDirective(String paramString)
    {
      return paramString.startsWith("#");
    }

    protected CharsetMapping.Entry parse(Matcher paramMatcher, CharsetMapping.Entry paramEntry)
    {
      paramEntry.bs = Integer.parseInt(paramMatcher.group(this.gbs), 16);
      paramEntry.cp = Integer.parseInt(paramMatcher.group(this.gcp), 16);
      if ((this.gcp2 <= paramMatcher.groupCount()) && (paramMatcher.group(this.gcp2) != null))
        paramEntry.cp2 = Integer.parseInt(paramMatcher.group(this.gcp2), 16);
      else
        paramEntry.cp2 = 0;
      return paramEntry;
    }

    public CharsetMapping.Entry next()
      throws Exception
    {
      return next(new CharsetMapping.Entry());
    }

    public CharsetMapping.Entry next(CharsetMapping.Entry paramEntry)
      throws Exception
    {
      if (this.closed)
        return null;
      while (true)
      {
        String str;
        while (true)
        {
          if ((str = this.reader.readLine()) == null)
            break label64;
          if (!(isDirective(str)))
            break;
        }
        this.matcher.reset(str);
        if (this.matcher.lookingAt())
          break;
      }
      return parse(this.matcher, paramEntry);
      label64: this.reader.close();
      this.closed = true;
      return null;
    }
  }
}