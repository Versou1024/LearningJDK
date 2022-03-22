package sun.io;

import sun.nio.cs.MS1257;

public class ByteToCharCp1257 extends ByteToCharSingleByte
{
  private static final MS1257 nioCoder = new MS1257();

  public String getCharacterEncoding()
  {
    return "Cp1257";
  }

  public ByteToCharCp1257()
  {
    this.byteToCharTable = nioCoder.getDecoderSingleByteMappings();
  }
}