package sun.io;

import sun.nio.cs.MS1252;

public class CharToByteCp1252 extends CharToByteSingleByte
{
  private static final MS1252 nioCoder = new MS1252();

  public String getCharacterEncoding()
  {
    return "Cp1252";
  }

  public CharToByteCp1252()
  {
    this.mask1 = 65280;
    this.mask2 = 255;
    this.shift = 8;
    this.index1 = nioCoder.getEncoderIndex1();
    this.index2 = nioCoder.getEncoderIndex2();
  }
}