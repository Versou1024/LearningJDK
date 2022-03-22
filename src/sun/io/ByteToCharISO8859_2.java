package sun.io;

import sun.nio.cs.ISO_8859_2;

public class ByteToCharISO8859_2 extends ByteToCharSingleByte
{
  private static final ISO_8859_2 nioCoder = new ISO_8859_2();

  public String getCharacterEncoding()
  {
    return "ISO8859_2";
  }

  public ByteToCharISO8859_2()
  {
    this.byteToCharTable = nioCoder.getDecoderSingleByteMappings();
  }
}