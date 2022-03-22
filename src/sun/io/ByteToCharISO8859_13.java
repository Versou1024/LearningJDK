package sun.io;

import sun.nio.cs.ISO_8859_13;

public class ByteToCharISO8859_13 extends ByteToCharSingleByte
{
  private static final ISO_8859_13 nioCoder = new ISO_8859_13();

  public String getCharacterEncoding()
  {
    return "ISO8859_13";
  }

  public ByteToCharISO8859_13()
  {
    this.byteToCharTable = nioCoder.getDecoderSingleByteMappings();
  }
}