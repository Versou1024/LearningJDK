package sun.text.normalizer;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public abstract class Trie
{
  protected static final int LEAD_INDEX_OFFSET_ = 320;
  protected static final int INDEX_STAGE_1_SHIFT_ = 5;
  protected static final int INDEX_STAGE_2_SHIFT_ = 2;
  protected static final int INDEX_STAGE_3_MASK_ = 31;
  protected static final int SURROGATE_MASK_ = 1023;
  protected char[] m_index_;
  protected DataManipulate m_dataManipulate_;
  protected int m_dataOffset_;
  protected int m_dataLength_;
  private static final int HEADER_SIGNATURE_INDEX_ = 0;
  private static final int HEADER_OPTIONS_INDEX_ = 2;
  private static final int HEADER_INDEX_LENGTH_INDEX_ = 4;
  private static final int HEADER_DATA_LENGTH_INDEX_ = 6;
  private static final int HEADER_LENGTH_ = 8;
  private static final int HEADER_OPTIONS_LATIN1_IS_LINEAR_MASK_ = 512;
  private static final int HEADER_SIGNATURE_ = 1416784229;
  private static final int HEADER_OPTIONS_SHIFT_MASK_ = 15;
  private static final int HEADER_OPTIONS_INDEX_SHIFT_ = 4;
  private static final int HEADER_OPTIONS_DATA_IS_32_BIT_ = 256;
  private boolean m_isLatin1Linear_;
  private int m_options_;

  protected Trie(InputStream paramInputStream, DataManipulate paramDataManipulate)
    throws IOException
  {
    DataInputStream localDataInputStream = new DataInputStream(paramInputStream);
    int i = localDataInputStream.readInt();
    this.m_options_ = localDataInputStream.readInt();
    if (!(checkHeader(i)))
      throw new IllegalArgumentException("ICU data file error: Trie header authentication failed, please check if you have the most updated ICU data file");
    this.m_dataManipulate_ = paramDataManipulate;
    this.m_isLatin1Linear_ = ((this.m_options_ & 0x200) != 0);
    this.m_dataOffset_ = localDataInputStream.readInt();
    this.m_dataLength_ = localDataInputStream.readInt();
    unserialize(paramInputStream);
  }

  protected Trie(char[] paramArrayOfChar, int paramInt, DataManipulate paramDataManipulate)
  {
    this.m_options_ = paramInt;
    this.m_dataManipulate_ = paramDataManipulate;
    this.m_isLatin1Linear_ = ((this.m_options_ & 0x200) != 0);
    this.m_index_ = paramArrayOfChar;
    this.m_dataOffset_ = this.m_index_.length;
  }

  protected abstract int getSurrogateOffset(char paramChar1, char paramChar2);

  protected abstract int getValue(int paramInt);

  protected abstract int getInitialValue();

  protected final int getRawOffset(int paramInt, char paramChar)
  {
    return ((this.m_index_[(paramInt + (paramChar >> '\5'))] << '\2') + (paramChar & 0x1F));
  }

  protected final int getBMPOffset(char paramChar)
  {
    return (((paramChar >= 55296) && (paramChar <= 56319)) ? getRawOffset(320, paramChar) : getRawOffset(0, paramChar));
  }

  protected final int getLeadOffset(char paramChar)
  {
    return getRawOffset(0, paramChar);
  }

  protected final int getCodePointOffset(int paramInt)
  {
    if ((paramInt >= 0) && (paramInt < 65536))
      return getBMPOffset((char)paramInt);
    if ((paramInt >= 0) && (paramInt <= 1114111))
      return getSurrogateOffset(UTF16.getLeadSurrogate(paramInt), (char)(paramInt & 0x3FF));
    return -1;
  }

  protected void unserialize(InputStream paramInputStream)
    throws IOException
  {
    this.m_index_ = new char[this.m_dataOffset_];
    DataInputStream localDataInputStream = new DataInputStream(paramInputStream);
    for (int i = 0; i < this.m_dataOffset_; ++i)
      this.m_index_[i] = localDataInputStream.readChar();
  }

  protected final boolean isIntTrie()
  {
    return ((this.m_options_ & 0x100) != 0);
  }

  protected final boolean isCharTrie()
  {
    return ((this.m_options_ & 0x100) == 0);
  }

  private final boolean checkHeader(int paramInt)
  {
    if (paramInt != 1416784229)
      return false;
    return (((this.m_options_ & 0xF) == 5) && ((this.m_options_ >> 4 & 0xF) == 2));
  }

  public static abstract interface DataManipulate
  {
    public abstract int getFoldingOffset(int paramInt);
  }
}