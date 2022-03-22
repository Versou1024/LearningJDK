package sun.net.idn;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.Normalizer.Form;
import java.text.ParseException;
import sun.text.Normalizer;
import sun.text.normalizer.CharTrie;
import sun.text.normalizer.NormalizerImpl;
import sun.text.normalizer.Trie.DataManipulate;
import sun.text.normalizer.UCharacter;
import sun.text.normalizer.UCharacterIterator;
import sun.text.normalizer.UTF16;
import sun.text.normalizer.VersionInfo;

public final class StringPrep
{
  public static final int DEFAULT = 0;
  public static final int ALLOW_UNASSIGNED = 1;
  private static final int UNASSIGNED = 0;
  private static final int MAP = 1;
  private static final int PROHIBITED = 2;
  private static final int DELETE = 3;
  private static final int TYPE_LIMIT = 4;
  private static final int NORMALIZATION_ON = 1;
  private static final int CHECK_BIDI_ON = 2;
  private static final int TYPE_THRESHOLD = 65520;
  private static final int MAX_INDEX_VALUE = 16319;
  private static final int MAX_INDEX_TOP_LENGTH = 3;
  private static final int INDEX_TRIE_SIZE = 0;
  private static final int INDEX_MAPPING_DATA_SIZE = 1;
  private static final int NORM_CORRECTNS_LAST_UNI_VERSION = 2;
  private static final int ONE_UCHAR_MAPPING_INDEX_START = 3;
  private static final int TWO_UCHARS_MAPPING_INDEX_START = 4;
  private static final int THREE_UCHARS_MAPPING_INDEX_START = 5;
  private static final int FOUR_UCHARS_MAPPING_INDEX_START = 6;
  private static final int OPTIONS = 7;
  private static final int INDEX_TOP = 16;
  private static final int DATA_BUFFER_SIZE = 25000;
  private StringPrepTrieImpl sprepTrieImpl;
  private int[] indexes;
  private char[] mappingData;
  private byte[] formatVersion;
  private VersionInfo sprepUniVer;
  private VersionInfo normCorrVer;
  private boolean doNFKC;
  private boolean checkBiDi;

  private char getCodePointValue(int paramInt)
  {
    return StringPrepTrieImpl.access$000(this.sprepTrieImpl).getCodePointValue(paramInt);
  }

  private static VersionInfo getVersionInfo(int paramInt)
  {
    int i = paramInt & 0xFF;
    int j = paramInt >> 8 & 0xFF;
    int k = paramInt >> 16 & 0xFF;
    int l = paramInt >> 24 & 0xFF;
    return VersionInfo.getInstance(l, k, j, i);
  }

  private static VersionInfo getVersionInfo(byte[] paramArrayOfByte)
  {
    if (paramArrayOfByte.length != 4)
      return null;
    return VersionInfo.getInstance(paramArrayOfByte[0], paramArrayOfByte[1], paramArrayOfByte[2], paramArrayOfByte[3]);
  }

  public StringPrep(InputStream paramInputStream)
    throws IOException
  {
    BufferedInputStream localBufferedInputStream = new BufferedInputStream(paramInputStream, 25000);
    StringPrepDataReader localStringPrepDataReader = new StringPrepDataReader(localBufferedInputStream);
    this.indexes = localStringPrepDataReader.readIndexes(16);
    byte[] arrayOfByte = new byte[this.indexes[0]];
    this.mappingData = new char[this.indexes[1] / 2];
    localStringPrepDataReader.read(arrayOfByte, this.mappingData);
    this.sprepTrieImpl = new StringPrepTrieImpl(null);
    StringPrepTrieImpl.access$002(this.sprepTrieImpl, new CharTrie(new ByteArrayInputStream(arrayOfByte), this.sprepTrieImpl));
    this.formatVersion = localStringPrepDataReader.getDataFormatVersion();
    this.doNFKC = ((this.indexes[7] & 0x1) > 0);
    this.checkBiDi = ((this.indexes[7] & 0x2) > 0);
    this.sprepUniVer = getVersionInfo(localStringPrepDataReader.getUnicodeVersion());
    this.normCorrVer = getVersionInfo(this.indexes[2]);
    VersionInfo localVersionInfo = NormalizerImpl.getUnicodeVersion();
    if ((localVersionInfo.compareTo(this.sprepUniVer) < 0) && (localVersionInfo.compareTo(this.normCorrVer) < 0) && ((this.indexes[7] & 0x1) > 0))
      throw new IOException("Normalization Correction version not supported");
    localBufferedInputStream.close();
  }

  private static final void getValues(char paramChar, Values paramValues)
  {
    paramValues.reset();
    if (paramChar == 0)
    {
      paramValues.type = 4;
    }
    else if (paramChar >= 65520)
    {
      paramValues.type = (paramChar - 65520);
    }
    else
    {
      paramValues.type = 1;
      if ((paramChar & 0x2) > 0)
      {
        paramValues.isIndex = true;
        paramValues.value = (paramChar >> '\2');
      }
      else
      {
        paramValues.isIndex = false;
        paramValues.value = (paramChar << '\16' >> 16);
        paramValues.value >>= 2;
      }
      if (paramChar >> '\2' == 16319)
      {
        paramValues.type = 3;
        paramValues.isIndex = false;
        paramValues.value = 0;
      }
    }
  }

  private StringBuffer map(UCharacterIterator paramUCharacterIterator, int paramInt)
    throws ParseException
  {
    Values localValues = new Values(null);
    char c = ';
    int i = -1;
    StringBuffer localStringBuffer = new StringBuffer();
    int j = ((paramInt & 0x1) > 0) ? 1 : 0;
    while (true)
    {
      while (true)
      {
        while (true)
        {
          int l;
          if ((i = paramUCharacterIterator.nextCodePoint()) == -1)
            break label277;
          c = getCodePointValue(i);
          getValues(c, localValues);
          if ((localValues.type == 0) && (j == 0))
            throw new ParseException("An unassigned code point was found in the input " + paramUCharacterIterator.getText(), paramUCharacterIterator.getIndex());
          if (localValues.type != 1)
            break label255;
          if (!(localValues.isIndex))
            break;
          int k = localValues.value;
          if ((k >= this.indexes[3]) && (k < this.indexes[4]))
            l = 1;
          else if ((k >= this.indexes[4]) && (k < this.indexes[5]))
            l = 2;
          else if ((k >= this.indexes[5]) && (k < this.indexes[6]))
            l = 3;
          else
            l = this.mappingData[(k++)];
          localStringBuffer.append(this.mappingData, k, l);
        }
        i -= localValues.value;
        break;
        label255: if (localValues.type != 3)
          break;
      }
      UTF16.append(localStringBuffer, i);
    }
    label277: return localStringBuffer;
  }

  private StringBuffer normalize(StringBuffer paramStringBuffer)
  {
    return new StringBuffer(Normalizer.normalize(paramStringBuffer.toString(), Normalizer.Form.NFKC, 262432));
  }

  public StringBuffer prepare(UCharacterIterator paramUCharacterIterator, int paramInt)
    throws ParseException
  {
    StringBuffer localStringBuffer1 = map(paramUCharacterIterator, paramInt);
    StringBuffer localStringBuffer2 = localStringBuffer1;
    if (this.doNFKC)
      localStringBuffer2 = normalize(localStringBuffer1);
    UCharacterIterator localUCharacterIterator = UCharacterIterator.getInstance(localStringBuffer2);
    Values localValues = new Values(null);
    int j = 19;
    int k = 19;
    int l = -1;
    int i1 = -1;
    int i2 = 0;
    int i3 = 0;
    while (true)
    {
      do
      {
        int i;
        if ((i = localUCharacterIterator.nextCodePoint()) == -1)
          break label196;
        char c = getCodePointValue(i);
        getValues(c, localValues);
        if (localValues.type == 2)
          throw new ParseException("A prohibited code point was found in the input" + localUCharacterIterator.getText(), localValues.value);
        j = UCharacter.getDirection(i);
        if (k == 19)
          k = j;
        if (j == 0)
        {
          i3 = 1;
          i1 = localUCharacterIterator.getIndex() - 1;
        }
      }
      while ((j != 1) && (j != 13));
      i2 = 1;
      l = localUCharacterIterator.getIndex() - 1;
    }
    if (this.checkBiDi == true)
    {
      if ((i3 == 1) && (i2 == 1))
        label196: throw new ParseException("The input does not conform to the rules for BiDi code points." + localUCharacterIterator.getText(), i1);
      if ((i2 == 1) && ((((k != 1) && (k != 13)) || ((j != 1) && (j != 13)))))
        throw new ParseException("The input does not conform to the rules for BiDi code points." + localUCharacterIterator.getText(), i1);
    }
    return localStringBuffer2;
  }

  private static final class StringPrepTrieImpl
  implements Trie.DataManipulate
  {
    private CharTrie sprepTrie = null;

    public int getFoldingOffset(int paramInt)
    {
      return paramInt;
    }
  }

  private static final class Values
  {
    boolean isIndex;
    int value;
    int type;

    public void reset()
    {
      this.isIndex = false;
      this.value = 0;
      this.type = -1;
    }
  }
}