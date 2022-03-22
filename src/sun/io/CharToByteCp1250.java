package sun.io;

import sun.nio.cs.MS1250;

public class CharToByteCp1250 extends CharToByteSingleByte
{
  private static final MS1250 nioCoder = new MS1250();

  public String getCharacterEncoding()
  {
    return "Cp1250";
  }

  public CharToByteCp1250()
  {
    this.mask1 = 65280;
    this.mask2 = 255;
    this.shift = 8;
    this.index1 = nioCoder.getEncoderIndex1();
    this.index2 = nioCoder.getEncoderIndex2();
  }
}