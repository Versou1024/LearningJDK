package sun.awt;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.security.AccessController;
import sun.nio.cs.HistoricallyNamedCharset;
import sun.security.action.GetPropertyAction;

public class FontDescriptor
  implements Cloneable
{
  String nativeName;
  public CharsetEncoder encoder;
  String charsetName;
  private int[] exclusionRanges;
  public CharsetEncoder unicodeEncoder;
  boolean useUnicode;
  static boolean isLE;

  public FontDescriptor(String paramString, CharsetEncoder paramCharsetEncoder, int[] paramArrayOfInt)
  {
    this.nativeName = paramString;
    this.encoder = paramCharsetEncoder;
    this.exclusionRanges = paramArrayOfInt;
    this.useUnicode = false;
    Charset localCharset = paramCharsetEncoder.charset();
    if (localCharset instanceof HistoricallyNamedCharset)
      this.charsetName = ((HistoricallyNamedCharset)localCharset).historicalName();
    else
      this.charsetName = localCharset.name();
  }

  public String getNativeName()
  {
    return this.nativeName;
  }

  public CharsetEncoder getFontCharsetEncoder()
  {
    return this.encoder;
  }

  public String getFontCharsetName()
  {
    return this.charsetName;
  }

  public int[] getExclusionRanges()
  {
    return this.exclusionRanges;
  }

  public boolean isExcluded(char paramChar)
  {
    int i = 0;
    while (i < this.exclusionRanges.length)
    {
      char c1 = this.exclusionRanges[(i++)];
      char c2 = this.exclusionRanges[(i++)];
      if ((paramChar >= c1) && (paramChar <= c2))
        return true;
    }
    return false;
  }

  public String toString()
  {
    return super.toString() + " [" + this.nativeName + "|" + this.encoder + "]";
  }

  private static native void initIDs();

  public boolean useUnicode()
  {
    if ((this.useUnicode) && (this.unicodeEncoder == null))
      try
      {
        this.unicodeEncoder = ((isLE) ? Charset.forName("UTF_16LE").newEncoder() : Charset.forName("UTF_16BE").newEncoder());
      }
      catch (IllegalArgumentException localIllegalArgumentException)
      {
      }
    return this.useUnicode;
  }

  static
  {
    NativeLibLoader.loadLibraries();
    initIDs();
    String str = (String)AccessController.doPrivileged(new GetPropertyAction("sun.io.unicode.encoding", "UnicodeBig"));
    isLE = !("UnicodeBig".equals(str));
  }
}