package sun.io;

import sun.nio.cs.ISO_8859_4;

public class CharToByteISO8859_4 extends CharToByteSingleByte
{
  private static final ISO_8859_4 nioCoder = new ISO_8859_4();

  public String getCharacterEncoding()
  {
    return "ISO8859_4";
  }

  public CharToByteISO8859_4()
  {
    this.mask1 = 65280;
    this.mask2 = 255;
    this.shift = 8;
    this.index1 = nioCoder.getEncoderIndex1();
    this.index2 = nioCoder.getEncoderIndex2();
  }
}