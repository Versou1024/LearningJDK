package sun.io;

import sun.nio.cs.MS1254;

public class CharToByteCp1254 extends CharToByteSingleByte
{
  private static final MS1254 nioCoder = new MS1254();

  public String getCharacterEncoding()
  {
    return "Cp1254";
  }

  public CharToByteCp1254()
  {
    this.mask1 = 65280;
    this.mask2 = 255;
    this.shift = 8;
    this.index1 = nioCoder.getEncoderIndex1();
    this.index2 = nioCoder.getEncoderIndex2();
  }
}