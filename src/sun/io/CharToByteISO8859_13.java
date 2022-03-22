package sun.io;

import sun.nio.cs.ISO_8859_13;

public class CharToByteISO8859_13 extends CharToByteSingleByte
{
  private static final ISO_8859_13 nioCoder = new ISO_8859_13();

  public String getCharacterEncoding()
  {
    return "ISO8859_13";
  }

  public CharToByteISO8859_13()
  {
    this.mask1 = 65280;
    this.mask2 = 255;
    this.shift = 8;
    this.index1 = nioCoder.getEncoderIndex1();
    this.index2 = nioCoder.getEncoderIndex2();
  }
}