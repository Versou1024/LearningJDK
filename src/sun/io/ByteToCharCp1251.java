package sun.io;

import sun.nio.cs.MS1251;

public class ByteToCharCp1251 extends ByteToCharSingleByte
{
  private static final MS1251 nioCoder = new MS1251();

  public String getCharacterEncoding()
  {
    return "Cp1251";
  }

  public ByteToCharCp1251()
  {
    this.byteToCharTable = nioCoder.getDecoderSingleByteMappings();
  }
}