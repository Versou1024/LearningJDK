package sun.io;

import sun.nio.cs.ISO_8859_5;

public class ByteToCharISO8859_5 extends ByteToCharSingleByte
{
  private static final ISO_8859_5 nioCoder = new ISO_8859_5();

  public String getCharacterEncoding()
  {
    return "ISO8859_5";
  }

  public ByteToCharISO8859_5()
  {
    this.byteToCharTable = nioCoder.getDecoderSingleByteMappings();
  }
}