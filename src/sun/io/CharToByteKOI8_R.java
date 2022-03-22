package sun.io;

import sun.nio.cs.KOI8_R;

public class CharToByteKOI8_R extends CharToByteSingleByte
{
  private static final KOI8_R nioCoder = new KOI8_R();

  public String getCharacterEncoding()
  {
    return "KOI8_R";
  }

  public CharToByteKOI8_R()
  {
    this.mask1 = 65280;
    this.mask2 = 255;
    this.shift = 8;
    this.index1 = nioCoder.getEncoderIndex1();
    this.index2 = nioCoder.getEncoderIndex2();
  }
}