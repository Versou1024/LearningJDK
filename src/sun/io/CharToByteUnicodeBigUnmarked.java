package sun.io;

public class CharToByteUnicodeBigUnmarked extends CharToByteUnicode
{
  public CharToByteUnicodeBigUnmarked()
  {
    this.byteOrder = 1;
    this.usesMark = false;
  }
}