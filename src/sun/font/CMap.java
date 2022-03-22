package sun.font;

import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.IntBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.util.logging.Logger;

abstract class CMap
{
  static final short ShiftJISEncoding = 2;
  static final short GBKEncoding = 3;
  static final short Big5Encoding = 4;
  static final short WansungEncoding = 5;
  static final short JohabEncoding = 6;
  static final short MSUnicodeSurrogateEncoding = 10;
  static final char noSuchChar = 65533;
  static final int SHORTMASK = 65535;
  static final int INTMASK = -1;
  static final char[][] converterMaps = new char[7][];
  char[] xlat;
  public static final NullCMapClass theNullCmap = new NullCMapClass();

  static CMap initialize(TrueTypeFont paramTrueTypeFont)
  {
    CMap localCMap = null;
    int k = -1;
    int l = 0;
    int i1 = 0;
    int i2 = 0;
    int i3 = 0;
    int i4 = 0;
    int i5 = 0;
    int i6 = 0;
    int i7 = 0;
    int i8 = 0;
    ByteBuffer localByteBuffer = paramTrueTypeFont.getTableBuffer(1668112752);
    int i9 = paramTrueTypeFont.getTableSize(1668112752);
    int i10 = localByteBuffer.getShort(2);
    for (int i11 = 0; i11 < i10; ++i11)
    {
      localByteBuffer.position(i11 * 8 + 4);
      int j = localByteBuffer.getShort();
      if (j == 3)
      {
        i8 = 1;
        k = localByteBuffer.getShort();
        int i = localByteBuffer.getInt();
        switch (k)
        {
        case 0:
          l = i;
          break;
        case 1:
          i1 = i;
          break;
        case 2:
          i2 = i;
          break;
        case 3:
          i3 = i;
          break;
        case 4:
          i4 = i;
          break;
        case 5:
          i5 = i;
          break;
        case 6:
          i6 = i;
          break;
        case 10:
          i7 = i;
        case 7:
        case 8:
        case 9:
        }
      }
    }
    if (i8 != 0)
      if (i7 != 0)
        localCMap = createCMap(localByteBuffer, i7, null);
      else if (l != 0)
        localCMap = createCMap(localByteBuffer, l, null);
      else if (i1 != 0)
        localCMap = createCMap(localByteBuffer, i1, null);
      else if (i2 != 0)
        localCMap = createCMap(localByteBuffer, i2, getConverterMap(2));
      else if (i3 != 0)
        localCMap = createCMap(localByteBuffer, i3, getConverterMap(3));
      else if (i4 != 0)
        if ((FontManager.isSolaris) && (paramTrueTypeFont.platName != null) && (((paramTrueTypeFont.platName.startsWith("/usr/openwin/lib/locale/zh_CN.EUC/X11/fonts/TrueType")) || (paramTrueTypeFont.platName.startsWith("/usr/openwin/lib/locale/zh_CN/X11/fonts/TrueType")) || (paramTrueTypeFont.platName.startsWith("/usr/openwin/lib/locale/zh/X11/fonts/TrueType")))))
          localCMap = createCMap(localByteBuffer, i4, getConverterMap(3));
        else
          localCMap = createCMap(localByteBuffer, i4, getConverterMap(4));
      else if (i5 != 0)
        localCMap = createCMap(localByteBuffer, i5, getConverterMap(5));
      else if (i6 != 0)
        localCMap = createCMap(localByteBuffer, i6, getConverterMap(6));
    else
      localCMap = createCMap(localByteBuffer, localByteBuffer.getInt(8), null);
    return localCMap;
  }

  static char[] getConverter(short paramShort)
  {
    String str;
    int i = 32768;
    int j = 65535;
    switch (paramShort)
    {
    case 2:
      i = 33088;
      j = 64764;
      str = "SJIS";
      break;
    case 3:
      i = 33088;
      j = 65184;
      str = "GBK";
      break;
    case 4:
      i = 41280;
      j = 65278;
      str = "Big5";
      break;
    case 5:
      i = 41377;
      j = 65246;
      str = "EUC_KR";
      break;
    case 6:
      i = 33089;
      j = 65022;
      str = "Johab";
      break;
    default:
      return null;
    }
    try
    {
      char[] arrayOfChar1 = new char[65536];
      for (int k = 0; k < 65536; ++k)
        arrayOfChar1[k] = 65533;
      byte[] arrayOfByte = new byte[(j - i + 1) * 2];
      char[] arrayOfChar2 = new char[j - i + 1];
      int l = 0;
      if (paramShort == 2)
        for (i2 = i; i2 <= j; ++i2)
        {
          int i1 = i2 >> 8 & 0xFF;
          if ((i1 >= 161) && (i1 <= 223))
          {
            arrayOfByte[(l++)] = -1;
            arrayOfByte[(l++)] = -1;
          }
          else
          {
            arrayOfByte[(l++)] = (byte)i1;
            arrayOfByte[(l++)] = (byte)(i2 & 0xFF);
          }
        }
      else
        for (i2 = i; i2 <= j; ++i2)
        {
          arrayOfByte[(l++)] = (byte)(i2 >> 8 & 0xFF);
          arrayOfByte[(l++)] = (byte)(i2 & 0xFF);
        }
      Charset.forName(str).newDecoder().onMalformedInput(CodingErrorAction.REPLACE).onUnmappableCharacter(CodingErrorAction.REPLACE).replaceWith("").decode(ByteBuffer.wrap(arrayOfByte, 0, arrayOfByte.length), CharBuffer.wrap(arrayOfChar2, 0, arrayOfChar2.length), true);
      for (int i2 = 32; i2 <= 126; ++i2)
        arrayOfChar1[i2] = (char)i2;
      if (paramShort == 2)
        for (i2 = 161; i2 <= 223; ++i2)
          arrayOfChar1[i2] = (char)(i2 - 161 + 65377);
      System.arraycopy(arrayOfChar2, 0, arrayOfChar1, i, arrayOfChar2.length);
      char[] arrayOfChar3 = new char[65536];
      for (int i3 = 0; i3 < 65536; ++i3)
        if (arrayOfChar1[i3] != 65533)
          arrayOfChar3[arrayOfChar1[i3]] = (char)i3;
      return arrayOfChar3;
    }
    catch (Exception localException)
    {
      localException.printStackTrace();
    }
    return null;
  }

  static char[] getConverterMap(short paramShort)
  {
    if (converterMaps[paramShort] == null)
      converterMaps[paramShort] = getConverter(paramShort);
    return converterMaps[paramShort];
  }

  static CMap createCMap(ByteBuffer paramByteBuffer, int paramInt, char[] paramArrayOfChar)
  {
    long l;
    int i = paramByteBuffer.getChar(paramInt);
    if (i < 8)
      l = paramByteBuffer.getChar(paramInt + 2);
    else
      l = paramByteBuffer.getInt(paramInt + 4) & 0xFFFFFFFF;
    if ((paramInt + l > paramByteBuffer.capacity()) && (FontManager.logging))
      FontManager.logger.warning("Cmap subtable overflows buffer.");
    switch (i)
    {
    case 0:
      return new CMapFormat0(paramByteBuffer, paramInt);
    case 2:
      return new CMapFormat2(paramByteBuffer, paramInt, paramArrayOfChar);
    case 4:
      return new CMapFormat4(paramByteBuffer, paramInt, paramArrayOfChar);
    case 6:
      return new CMapFormat6(paramByteBuffer, paramInt, paramArrayOfChar);
    case 8:
      return new CMapFormat8(paramByteBuffer, paramInt, paramArrayOfChar);
    case 10:
      return new CMapFormat10(paramByteBuffer, paramInt, paramArrayOfChar);
    case 12:
      return new CMapFormat12(paramByteBuffer, paramInt, paramArrayOfChar);
    case 1:
    case 3:
    case 5:
    case 7:
    case 9:
    case 11:
    }
    throw new RuntimeException("Cmap format unimplemented: " + paramByteBuffer.getChar(paramInt));
  }

  abstract char getGlyph(int paramInt);

  final int getControlCodeGlyph(int paramInt, boolean paramBoolean)
  {
    if (paramInt < 16)
    {
      switch (paramInt)
      {
      case 9:
      case 10:
      case 13:
        return 65535;
      case 11:
      case 12:
      }
    }
    else if (paramInt >= 8204)
    {
      if ((paramInt <= 8207) || ((paramInt >= 8232) && (paramInt <= 8238)) || ((paramInt >= 8298) && (paramInt <= 8303)))
        return 65535;
      if ((paramBoolean) && (paramInt >= 65535))
        return 0;
    }
    return -1;
  }

  static class CMapFormat0 extends CMap
  {
    byte[] cmap;

    CMapFormat0(ByteBuffer paramByteBuffer, int paramInt)
    {
      int i = paramByteBuffer.getChar(paramInt + 2);
      this.cmap = new byte[i - 6];
      paramByteBuffer.position(paramInt + 6);
      paramByteBuffer.get(this.cmap);
    }

    char getGlyph(int paramInt)
    {
      if (paramInt < 256)
      {
        if (paramInt < 16)
          switch (paramInt)
          {
          case 9:
          case 10:
          case 13:
            return 65535;
          case 11:
          case 12:
          }
        return (char)(0xFF & this.cmap[paramInt]);
      }
      return ';
    }
  }

  static class CMapFormat10 extends CMap
  {
    long firstCode;
    int entryCount;
    char[] glyphIdArray;

    CMapFormat10(ByteBuffer paramByteBuffer, int paramInt, char[] paramArrayOfChar)
    {
      System.err.println("WARNING: CMapFormat10 is untested.");
      this.firstCode = (paramByteBuffer.getInt() & 0xFFFFFFFF);
      this.entryCount = (paramByteBuffer.getInt() & 0xFFFFFFFF);
      paramByteBuffer.position(paramInt + 20);
      CharBuffer localCharBuffer = paramByteBuffer.asCharBuffer();
      this.glyphIdArray = new char[this.entryCount];
      for (int i = 0; i < this.entryCount; ++i)
        this.glyphIdArray[i] = localCharBuffer.get();
    }

    char getGlyph(int paramInt)
    {
      if (this.xlat != null)
        throw new RuntimeException("xlat array for cmap fmt=10");
      int i = (int)(paramInt - this.firstCode);
      if ((i < 0) || (i >= this.entryCount))
        return ';
      return this.glyphIdArray[i];
    }
  }

  static class CMapFormat12 extends CMap
  {
    int numGroups;
    int highBit = 0;
    int power;
    int extra;
    long[] startCharCode;
    long[] endCharCode;
    int[] startGlyphID;

    CMapFormat12(ByteBuffer paramByteBuffer, int paramInt, char[] paramArrayOfChar)
    {
      if (paramArrayOfChar != null)
        throw new RuntimeException("xlat array for cmap fmt=12");
      this.numGroups = paramByteBuffer.getInt(paramInt + 12);
      this.startCharCode = new long[this.numGroups];
      this.endCharCode = new long[this.numGroups];
      this.startGlyphID = new int[this.numGroups];
      paramByteBuffer.position(paramInt + 16);
      paramByteBuffer = paramByteBuffer.slice();
      IntBuffer localIntBuffer = paramByteBuffer.asIntBuffer();
      for (int i = 0; i < this.numGroups; ++i)
      {
        this.startCharCode[i] = (localIntBuffer.get() & 0xFFFFFFFF);
        this.endCharCode[i] = (localIntBuffer.get() & 0xFFFFFFFF);
        this.startGlyphID[i] = (localIntBuffer.get() & 0xFFFFFFFF);
      }
      i = this.numGroups;
      if (i >= 65536)
      {
        i >>= 16;
        this.highBit += 16;
      }
      if (i >= 256)
      {
        i >>= 8;
        this.highBit += 8;
      }
      if (i >= 16)
      {
        i >>= 4;
        this.highBit += 4;
      }
      if (i >= 4)
      {
        i >>= 2;
        this.highBit += 2;
      }
      if (i >= 2)
      {
        i >>= 1;
        this.highBit += 1;
      }
      this.power = (1 << this.highBit);
      this.extra = (this.numGroups - this.power);
    }

    char getGlyph(int paramInt)
    {
      int i = getControlCodeGlyph(paramInt, false);
      if (i >= 0)
        return (char)i;
      int j = this.power;
      int k = 0;
      if (this.startCharCode[this.extra] <= paramInt)
        k = this.extra;
      while (true)
      {
        do
        {
          if (j <= 1)
            break label76;
          j >>= 1;
        }
        while (this.startCharCode[(k + j)] > paramInt);
        k += j;
      }
      if ((this.startCharCode[k] <= paramInt) && (this.endCharCode[k] >= paramInt))
        label76: return (char)(int)(this.startGlyphID[k] + paramInt - this.startCharCode[k]);
      return ';
    }
  }

  static class CMapFormat2 extends CMap
  {
    char[] subHeaderKey = new char[256];
    char[] firstCodeArray;
    char[] entryCountArray;
    short[] idDeltaArray;
    char[] idRangeOffSetArray;
    char[] glyphIndexArray;

    CMapFormat2(ByteBuffer paramByteBuffer, int paramInt, char[] paramArrayOfChar)
    {
      this.xlat = paramArrayOfChar;
      int i = paramByteBuffer.getChar(paramInt + 2);
      paramByteBuffer.position(paramInt + 6);
      CharBuffer localCharBuffer = paramByteBuffer.asCharBuffer();
      int j = 0;
      for (int k = 0; k < 256; ++k)
      {
        this.subHeaderKey[k] = localCharBuffer.get();
        if (this.subHeaderKey[k] > j)
          j = this.subHeaderKey[k];
      }
      k = (j >> 3) + 1;
      this.firstCodeArray = new char[k];
      this.entryCountArray = new char[k];
      this.idDeltaArray = new short[k];
      this.idRangeOffSetArray = new char[k];
      for (int l = 0; l < k; ++l)
      {
        this.firstCodeArray[l] = localCharBuffer.get();
        this.entryCountArray[l] = localCharBuffer.get();
        this.idDeltaArray[l] = (short)localCharBuffer.get();
        this.idRangeOffSetArray[l] = localCharBuffer.get();
      }
      l = (i - 518 - k * 8) / 2;
      this.glyphIndexArray = new char[l];
      for (int i1 = 0; i1 < l; ++i1)
        this.glyphIndexArray[i1] = localCharBuffer.get();
    }

    char getGlyph(int paramInt)
    {
      int i = getControlCodeGlyph(paramInt, true);
      if (i >= 0)
        return (char)i;
      if (this.xlat != null)
        paramInt = this.xlat[paramInt];
      int j = (char)(paramInt >> 8);
      int k = (char)(paramInt & 0xFF);
      int l = this.subHeaderKey[j] >> '\3';
      if (l != 0)
      {
        i1 = k;
      }
      else
      {
        i1 = j;
        if (i1 == 0)
          i1 = k;
      }
      int i2 = this.firstCodeArray[l];
      if (i1 < i2)
        return ';
      int i1 = (char)(i1 - i2);
      if (i1 < this.entryCountArray[l])
      {
        int i3 = (this.idRangeOffSetArray.length - l) * 8 - 6;
        int i4 = (this.idRangeOffSetArray[l] - i3) / 2;
        int i5 = this.glyphIndexArray[(i4 + i1)];
        if (i5 != 0)
        {
          i5 = (char)(i5 + this.idDeltaArray[l]);
          return i5;
        }
      }
      return ';
    }
  }

  static class CMapFormat4 extends CMap
  {
    int segCount;
    int entrySelector;
    int rangeShift;
    char[] endCount;
    char[] startCount;
    short[] idDelta;
    char[] idRangeOffset;
    char[] glyphIds;

    CMapFormat4(ByteBuffer paramByteBuffer, int paramInt, char[] paramArrayOfChar)
    {
      this.xlat = paramArrayOfChar;
      paramByteBuffer.position(paramInt);
      CharBuffer localCharBuffer = paramByteBuffer.asCharBuffer();
      localCharBuffer.get();
      int i = localCharBuffer.get();
      if (paramInt + i > paramByteBuffer.capacity())
        i = paramByteBuffer.capacity() - paramInt;
      localCharBuffer.get();
      this.segCount = (localCharBuffer.get() / '\2');
      int j = localCharBuffer.get();
      this.entrySelector = localCharBuffer.get();
      this.rangeShift = (localCharBuffer.get() / '\2');
      this.startCount = new char[this.segCount];
      this.endCount = new char[this.segCount];
      this.idDelta = new short[this.segCount];
      this.idRangeOffset = new char[this.segCount];
      for (int k = 0; k < this.segCount; ++k)
        this.endCount[k] = localCharBuffer.get();
      localCharBuffer.get();
      for (k = 0; k < this.segCount; ++k)
        this.startCount[k] = localCharBuffer.get();
      for (k = 0; k < this.segCount; ++k)
        this.idDelta[k] = (short)localCharBuffer.get();
      for (k = 0; k < this.segCount; ++k)
      {
        l = localCharBuffer.get();
        this.idRangeOffset[k] = (char)(l >> 1 & 0xFFFF);
      }
      k = (this.segCount * 8 + 16) / 2;
      localCharBuffer.position(k);
      int l = i / 2 - k;
      this.glyphIds = new char[l];
      for (int i1 = 0; i1 < l; ++i1)
        this.glyphIds[i1] = localCharBuffer.get();
    }

    char getGlyph(int paramInt)
    {
      int i = 0;
      int j = 0;
      int k = getControlCodeGlyph(paramInt, true);
      if (k >= 0)
        return (char)k;
      if (this.xlat != null)
        paramInt = this.xlat[paramInt];
      int l = 0;
      int i1 = this.startCount.length;
      for (i = this.startCount.length >> 1; l < i1; i = l + i1 >> 1)
        if (this.endCount[i] < paramInt)
          l = i + 1;
        else
          i1 = i;
      if ((paramInt >= this.startCount[i]) && (paramInt <= this.endCount[i]))
      {
        int i2 = this.idRangeOffset[i];
        if (i2 == 0)
        {
          j = (char)(paramInt + this.idDelta[i]);
        }
        else
        {
          int i3 = i2 - this.segCount + i + paramInt - this.startCount[i];
          j = this.glyphIds[i3];
          if (j != 0)
            j = (char)(j + this.idDelta[i]);
        }
      }
      if (j != 0);
      return j;
    }
  }

  static class CMapFormat6 extends CMap
  {
    char firstCode;
    char entryCount;
    char[] glyphIdArray;

    CMapFormat6(ByteBuffer paramByteBuffer, int paramInt, char[] paramArrayOfChar)
    {
      System.err.println("WARNING: CMapFormat8 is untested.");
      paramByteBuffer.position(paramInt + 6);
      CharBuffer localCharBuffer = paramByteBuffer.asCharBuffer();
      this.firstCode = localCharBuffer.get();
      this.entryCount = localCharBuffer.get();
      this.glyphIdArray = new char[this.entryCount];
      for (int i = 0; i < this.entryCount; ++i)
        this.glyphIdArray[i] = localCharBuffer.get();
    }

    char getGlyph(int paramInt)
    {
      int i = getControlCodeGlyph(paramInt, true);
      if (i >= 0)
        return (char)i;
      if (this.xlat != null)
        paramInt = this.xlat[paramInt];
      paramInt -= this.firstCode;
      if ((paramInt < 0) || (paramInt >= this.entryCount))
        return ';
      return this.glyphIdArray[paramInt];
    }
  }

  static class CMapFormat8 extends CMap
  {
    byte[] is32 = new byte[8192];
    int nGroups;
    int[] startCharCode;
    int[] endCharCode;
    int[] startGlyphID;

    CMapFormat8(ByteBuffer paramByteBuffer, int paramInt, char[] paramArrayOfChar)
    {
      System.err.println("WARNING: CMapFormat8 is untested.");
      paramByteBuffer.position(12);
      paramByteBuffer.get(this.is32);
      this.nGroups = paramByteBuffer.getInt();
      this.startCharCode = new int[this.nGroups];
      this.endCharCode = new int[this.nGroups];
      this.startGlyphID = new int[this.nGroups];
    }

    char getGlyph(int paramInt)
    {
      if (this.xlat != null)
        throw new RuntimeException("xlat array for cmap fmt=8");
      return ';
    }
  }

  static class NullCMapClass extends CMap
  {
    char getGlyph(int paramInt)
    {
      return ';
    }
  }
}