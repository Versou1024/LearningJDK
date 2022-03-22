package sun.io;

import sun.nio.cs.KOI8_R;

public class ByteToCharKOI8_R extends ByteToCharSingleByte
{
  private static final KOI8_R nioCoder = new KOI8_R();

  public String getCharacterEncoding()
  {
    return "KOI8_R";
  }

  public ByteToCharKOI8_R()
  {
    this.byteToCharTable = nioCoder.getDecoderSingleByteMappings();
  }
}