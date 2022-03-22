package sun.io;

import sun.nio.cs.MS1253;

public class CharToByteCp1253 extends CharToByteSingleByte
{
  private static final MS1253 nioCoder = new MS1253();

  public String getCharacterEncoding()
  {
    return "Cp1253";
  }

  public CharToByteCp1253()
  {
    this.mask1 = 65280;
    this.mask2 = 255;
    this.shift = 8;
    this.index1 = nioCoder.getEncoderIndex1();
    this.index2 = nioCoder.getEncoderIndex2();
  }
}