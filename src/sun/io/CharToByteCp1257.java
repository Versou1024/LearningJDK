package sun.io;

import sun.nio.cs.MS1257;

public class CharToByteCp1257 extends CharToByteSingleByte
{
  private static final MS1257 nioCoder = new MS1257();

  public String getCharacterEncoding()
  {
    return "Cp1257";
  }

  public CharToByteCp1257()
  {
    this.mask1 = 65280;
    this.mask2 = 255;
    this.shift = 8;
    this.index1 = nioCoder.getEncoderIndex1();
    this.index2 = nioCoder.getEncoderIndex2();
  }
}