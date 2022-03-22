package sun.nio.cs;

import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

public class MS1257 extends Charset
  implements HistoricallyNamedCharset
{
  public String historicalName()
  {
    return "Cp1257";
  }

  public MS1257()
  {
    super("windows-1257", StandardCharsets.aliases_MS1257);
  }

  public boolean contains(Charset paramCharset)
  {
    return ((paramCharset.name().equals("US-ASCII")) || (paramCharset instanceof MS1257));
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
    private static final short[] index1 = { 0, 256, 440, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 677, 899, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383, 383 };

    public Encoder(Charset paramCharset)
    {
      super(paramCharset, index1, "", 65280, 255, 8);
    }
  }
}