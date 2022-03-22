package sun.awt.windows;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import sun.awt.AWTCharset;
import sun.awt.AWTCharset.Encoder;

public class WDefaultFontCharset extends AWTCharset
{
  private String fontName;

  public WDefaultFontCharset(String paramString)
  {
    super("WDefaultFontCharset", Charset.forName("windows-1252"));
    this.fontName = paramString;
  }

  public CharsetEncoder newEncoder()
  {
    return new Encoder(this, null);
  }

  public synchronized native boolean canConvert(char paramChar);

  private static native void initIDs();

  static
  {
    initIDs();
  }

  private class Encoder extends AWTCharset.Encoder
  {
    private Encoder()
    {
      super(paramWDefaultFontCharset);
    }

    public boolean canEncode()
    {
      return this.this$0.canConvert(paramChar);
    }
  }
}