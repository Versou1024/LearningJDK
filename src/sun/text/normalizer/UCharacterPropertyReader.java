package sun.text.normalizer;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

final class UCharacterPropertyReader
  implements ICUBinary.Authenticate
{
  private static final int INDEX_SIZE_ = 16;
  private DataInputStream m_dataInputStream_;
  private int m_propertyOffset_;
  private int m_exceptionOffset_;
  private int m_caseOffset_;
  private int m_additionalOffset_;
  private int m_additionalVectorsOffset_;
  private int m_additionalColumnsCount_;
  private int m_reservedOffset_;
  private byte[] m_unicodeVersion_ = ICUBinary.readHeader(paramInputStream, DATA_FORMAT_ID_, this);
  private static final byte[] DATA_FORMAT_ID_ = { 85, 80, 114, 111 };
  private static final byte[] DATA_FORMAT_VERSION_ = { 3, 1, 5, 2 };

  public boolean isDataVersionAcceptable(byte[] paramArrayOfByte)
  {
    return ((paramArrayOfByte[0] == DATA_FORMAT_VERSION_[0]) && (paramArrayOfByte[2] == DATA_FORMAT_VERSION_[2]) && (paramArrayOfByte[3] == DATA_FORMAT_VERSION_[3]));
  }

  protected UCharacterPropertyReader(InputStream paramInputStream)
    throws IOException
  {
    this.m_dataInputStream_ = new DataInputStream(paramInputStream);
  }

  protected void read(UCharacterProperty paramUCharacterProperty)
    throws IOException
  {
    int i = 16;
    this.m_propertyOffset_ = this.m_dataInputStream_.readInt();
    --i;
    this.m_exceptionOffset_ = this.m_dataInputStream_.readInt();
    --i;
    this.m_caseOffset_ = this.m_dataInputStream_.readInt();
    --i;
    this.m_additionalOffset_ = this.m_dataInputStream_.readInt();
    --i;
    this.m_additionalVectorsOffset_ = this.m_dataInputStream_.readInt();
    --i;
    this.m_additionalColumnsCount_ = this.m_dataInputStream_.readInt();
    --i;
    this.m_reservedOffset_ = this.m_dataInputStream_.readInt();
    --i;
    this.m_dataInputStream_.skipBytes(12);
    i -= 3;
    paramUCharacterProperty.m_maxBlockScriptValue_ = this.m_dataInputStream_.readInt();
    --i;
    paramUCharacterProperty.m_maxJTGValue_ = this.m_dataInputStream_.readInt();
    this.m_dataInputStream_.skipBytes(--i << 2);
    paramUCharacterProperty.m_trie_ = new CharTrie(this.m_dataInputStream_, paramUCharacterProperty);
    int j = this.m_exceptionOffset_ - this.m_propertyOffset_;
    paramUCharacterProperty.m_property_ = new int[j];
    for (int k = 0; k < j; ++k)
      paramUCharacterProperty.m_property_[k] = this.m_dataInputStream_.readInt();
    j = this.m_caseOffset_ - this.m_exceptionOffset_;
    paramUCharacterProperty.m_exception_ = new int[j];
    for (k = 0; k < j; ++k)
      paramUCharacterProperty.m_exception_[k] = this.m_dataInputStream_.readInt();
    j = this.m_additionalOffset_ - this.m_caseOffset_ << 1;
    paramUCharacterProperty.m_case_ = new char[j];
    for (k = 0; k < j; ++k)
      paramUCharacterProperty.m_case_[k] = this.m_dataInputStream_.readChar();
    paramUCharacterProperty.m_additionalTrie_ = new CharTrie(this.m_dataInputStream_, paramUCharacterProperty);
    j = this.m_reservedOffset_ - this.m_additionalVectorsOffset_;
    paramUCharacterProperty.m_additionalVectors_ = new int[j];
    for (k = 0; k < j; ++k)
      paramUCharacterProperty.m_additionalVectors_[k] = this.m_dataInputStream_.readInt();
    this.m_dataInputStream_.close();
    paramUCharacterProperty.m_additionalColumnsCount_ = this.m_additionalColumnsCount_;
    paramUCharacterProperty.m_unicodeVersion_ = VersionInfo.getInstance(this.m_unicodeVersion_[0], this.m_unicodeVersion_[1], this.m_unicodeVersion_[2], this.m_unicodeVersion_[3]);
  }
}