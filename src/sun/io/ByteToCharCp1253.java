package sun.io;

import sun.nio.cs.MS1253;

public class ByteToCharCp1253 extends ByteToCharSingleByte
{
  private static final MS1253 nioCoder = new MS1253();

  public String getCharacterEncoding()
  {
    return "Cp1253";
  }

  public ByteToCharCp1253()
  {
    this.byteToCharTable = nioCoder.getDecoderSingleByteMappings();
  }
}