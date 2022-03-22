package sun.io;

import sun.nio.cs.MS1254;

public class ByteToCharCp1254 extends ByteToCharSingleByte
{
  private static final MS1254 nioCoder = new MS1254();

  public String getCharacterEncoding()
  {
    return "Cp1254";
  }

  public ByteToCharCp1254()
  {
    this.byteToCharTable = nioCoder.getDecoderSingleByteMappings();
  }
}