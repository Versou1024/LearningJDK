package sun.io;

import sun.nio.cs.ISO_8859_4;

public class ByteToCharISO8859_4 extends ByteToCharSingleByte
{
  private static final ISO_8859_4 nioCoder = new ISO_8859_4();

  public String getCharacterEncoding()
  {
    return "ISO8859_4";
  }

  public ByteToCharISO8859_4()
  {
    this.byteToCharTable = nioCoder.getDecoderSingleByteMappings();
  }
}