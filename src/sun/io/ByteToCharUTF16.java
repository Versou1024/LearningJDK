package sun.io;

public class ByteToCharUTF16 extends ByteToCharUnicode
{
  public ByteToCharUTF16()
  {
    super(0, true);
  }

  public String getCharacterEncoding()
  {
    return "UTF-16";
  }
}