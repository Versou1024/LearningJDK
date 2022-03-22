package sun.io;

public class CharToByteUTF16 extends CharToByteUnicode
{
  public CharToByteUTF16()
  {
    super(1, true);
  }

  public String getCharacterEncoding()
  {
    return "UTF-16";
  }
}