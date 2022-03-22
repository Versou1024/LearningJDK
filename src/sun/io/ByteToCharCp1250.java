package sun.io;

import sun.nio.cs.MS1250;

public class ByteToCharCp1250 extends ByteToCharSingleByte
{
  private static final MS1250 nioCoder = new MS1250();

  public String getCharacterEncoding()
  {
    return "Cp1250";
  }

  public ByteToCharCp1250()
  {
    this.byteToCharTable = nioCoder.getDecoderSingleByteMappings();
  }
}