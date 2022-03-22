package sun.io;

import sun.nio.cs.ISO_8859_7;

public class CharToByteISO8859_7 extends CharToByteSingleByte
{
  private static final ISO_8859_7 nioCoder = new ISO_8859_7();

  public String getCharacterEncoding()
  {
    return "ISO8859_7";
  }

  public CharToByteISO8859_7()
  {
    this.mask1 = 65280;
    this.mask2 = 255;
    this.shift = 8;
    this.index1 = nioCoder.getEncoderIndex1();
    this.index2 = nioCoder.getEncoderIndex2();
  }
}