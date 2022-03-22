package sun.io;

import sun.nio.cs.ISO_8859_9;

public class ByteToCharISO8859_9 extends ByteToCharSingleByte
{
  private static final ISO_8859_9 nioCoder = new ISO_8859_9();

  public String getCharacterEncoding()
  {
    return "ISO8859_9";
  }

  public ByteToCharISO8859_9()
  {
    this.byteToCharTable = nioCoder.getDecoderSingleByteMappings();
  }
}