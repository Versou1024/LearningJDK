package sun.io;

import sun.nio.cs.ISO_8859_15;

public class CharToByteISO8859_15 extends CharToByteSingleByte
{
  private static final ISO_8859_15 nioCoder = new ISO_8859_15();

  public String getCharacterEncoding()
  {
    return "ISO8859_15";
  }

  public CharToByteISO8859_15()
  {
    this.mask1 = 65280;
    this.mask2 = 255;
    this.shift = 8;
    this.index1 = nioCoder.getEncoderIndex1();
    this.index2 = nioCoder.getEncoderIndex2();
  }
}