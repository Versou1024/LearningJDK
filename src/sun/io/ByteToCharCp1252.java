package sun.io;

import sun.nio.cs.MS1252;

public class ByteToCharCp1252 extends ByteToCharSingleByte
{
  private static final MS1252 nioCoder = new MS1252();

  public String getCharacterEncoding()
  {
    return "Cp1252";
  }

  public ByteToCharCp1252()
  {
    this.byteToCharTable = nioCoder.getDecoderSingleByteMappings();
  }
}