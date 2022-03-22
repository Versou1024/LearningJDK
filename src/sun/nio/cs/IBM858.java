package sun.nio.cs;

import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

public class IBM858 extends Charset
  implements HistoricallyNamedCharset
{
  public IBM858()
  {
    super("IBM00858", StandardCharsets.aliases_IBM858);
  }

  public String historicalName()
  {
    return "Cp858";
  }

  public boolean contains(Charset paramCharset)
  {
    return paramCharset instanceof IBM858;
  }

  public CharsetDecoder newDecoder()
  {
    return new Decoder(this);
  }

  public CharsetEncoder newEncoder()
  {
    return new Encoder(this);
  }

  public static String getDecoderSingleByteMappings()
  {
    return IBM850.getDecoderSingleByteMappings();
  }

  public short[] getEncoderIndex1()
  {
    return Encoder.access$000();
  }

  public String getEncoderIndex2()
  {
    return "";
  }

  private static class Decoder extends IBM850.Decoder
  {
    public Decoder(Charset paramCharset)
    {
      super(paramCharset);
    }

    public char decode(int paramInt)
    {
      return ((paramInt == -43) ? 8364 : super.decode(paramInt));
    }
  }

  private static class Encoder extends SingleByteEncoder
  {
    private static final String index2 = "";
    private static final short[] index1 = { 0, 256, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 636, 403, 403, 403, 403, 892, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403, 403 };

    public Encoder(Charset paramCharset)
    {
      super(paramCharset, index1, "", 65280, 255, 8);
    }
  }
}