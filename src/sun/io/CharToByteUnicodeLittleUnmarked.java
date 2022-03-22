package sun.io;

public class CharToByteUnicodeLittleUnmarked extends CharToByteUnicode
{
  public CharToByteUnicodeLittleUnmarked()
  {
    this.byteOrder = 2;
    this.usesMark = false;
  }
}