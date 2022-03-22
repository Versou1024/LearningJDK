package sun.io;

import sun.nio.cs.ISO_8859_7;

public class ByteToCharISO8859_7 extends ByteToCharSingleByte
{
  private static final ISO_8859_7 nioCoder = new ISO_8859_7();

  public String getCharacterEncoding()
  {
    return "ISO8859_7";
  }

  public ByteToCharISO8859_7()
  {
    this.byteToCharTable = nioCoder.getDecoderSingleByteMappings();
  }
}