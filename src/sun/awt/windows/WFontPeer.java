package sun.awt.windows;

import sun.awt.PlatformFont;

public class WFontPeer extends PlatformFont
{
  private String textComponentFontName;

  public WFontPeer(String paramString, int paramInt)
  {
    super(paramString, paramInt);
    if (this.fontConfig != null)
      this.textComponentFontName = ((WFontConfiguration)this.fontConfig).getTextComponentFontName(this.familyName, paramInt);
  }

  protected char getMissingGlyphCharacter()
  {
    return 10065;
  }

  private static native void initIDs();

  static
  {
    initIDs();
  }
}