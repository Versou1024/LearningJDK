package sun.nio.cs;

import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

public class ISO_8859_7 extends Charset
  implements HistoricallyNamedCharset
{
  public ISO_8859_7()
  {
    super("ISO-8859-7", StandardCharsets.aliases_ISO_8859_7);
  }

  public String historicalName()
  {
    return "ISO8859_7";
  }

  public boolean contains(Charset paramCharset)
  {
    return ((paramCharset.name().equals("US-ASCII")) || (paramCharset instanceof ISO_8859_7));
  }

  public CharsetDecoder newDecoder()
  {
    return new Decoder(this);
  }

  public CharsetEncoder newEncoder()
  {
    return new Encoder(this);
  }

  public String getDecoderSingleByteMappings()
  {
    return "";
  }

  public short[] getEncoderIndex1()
  {
    return Encoder.access$000();
  }

  public String getEncoderIndex2()
  {
    return "";
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
    private static final short[] index1 = { 0, 190, 190, 324, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 559, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190, 190 };

    public Encoder(Charset paramCharset)
    {
      super(paramCharset, index1, "", 65280, 255, 8);
    }
  }
}