package sun.io;

import sun.nio.cs.MS1251;

public class CharToByteCp1251 extends CharToByteSingleByte
{
  private static final MS1251 nioCoder = new MS1251();

  public String getCharacterEncoding()
  {
    return "Cp1251";
  }

  public CharToByteCp1251()
  {
    this.mask1 = 65280;
    this.mask2 = 255;
    this.shift = 8;
    this.index1 = nioCoder.getEncoderIndex1();
    this.index2 = nioCoder.getEncoderIndex2();
  }
}