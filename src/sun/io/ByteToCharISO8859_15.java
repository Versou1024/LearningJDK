package sun.io;

import sun.nio.cs.ISO_8859_15;

public class ByteToCharISO8859_15 extends ByteToCharSingleByte
{
  private static final ISO_8859_15 nioCoder = new ISO_8859_15();

  public String getCharacterEncoding()
  {
    return "ISO8859_15";
  }

  public ByteToCharISO8859_15()
  {
    this.byteToCharTable = nioCoder.getDecoderSingleByteMappings();
  }
}