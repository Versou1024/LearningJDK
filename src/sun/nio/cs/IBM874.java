package sun.nio.cs;

import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

public class IBM874 extends Charset
  implements HistoricallyNamedCharset
{
  public IBM874()
  {
    super("x-IBM874", StandardCharsets.aliases_IBM874);
  }

  public String historicalName()
  {
    return "Cp874";
  }

  public boolean contains(Charset paramCharset)
  {
    return paramCharset instanceof IBM874;
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
    private static final short[] index1 = { 0, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 428, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 520, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173, 173 };

    public Encoder(Charset paramCharset)
    {
      super(paramCharset, index1, "", 65280, 255, 8);
    }
  }
}