package sun.nio.cs;

import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

public class KOI8_U extends Charset
  implements HistoricallyNamedCharset
{
  public KOI8_U()
  {
    super("KOI8-U", StandardCharsets.aliases_KOI8_U);
  }

  public String historicalName()
  {
    return "KOI8_U";
  }

  public boolean contains(Charset paramCharset)
  {
    return ((paramCharset.name().equals("US-ASCII")) || (paramCharset instanceof KOI8_U));
  }

  public CharsetDecoder newDecoder()
  {
    return new Decoder(this);
  }

  public CharsetEncoder newEncoder()
  {
    return new Encoder(this);
  }

  private static class Decoder extends SingleByteDecoder
  {
    private static final String byteToCharTable = "";

    public Decoder(Charset paramCharset)
    {
      super(paramCharset, "");
    }
  }

  private static class Encoder extends SingleByteEncoder
  {
    private static final String index2 = "";
    private static final short[] index1 = { 0, 248, 248, 248, 503, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 734, 958, 248, 1214, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248, 248 };

    public Encoder(Charset paramCharset)
    {
      super(paramCharset, index1, "", 65280, 255, 8);
    }
  }
}