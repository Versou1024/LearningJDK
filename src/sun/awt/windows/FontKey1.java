package sun.awt.windows;

import java.awt.Font;

class FontKey1
{
  private Font font;
  private int angle;
  private float scaledSize;
  private float averageWidth;
  private int hash;

  void init(Font paramFont, int paramInt, float paramFloat1, float paramFloat2)
  {
    this.font = paramFont;
    this.angle = paramInt;
    this.scaledSize = paramFloat1;
    this.averageWidth = paramFloat2;
    this.hash = (paramFont.hashCode() + paramInt + (int)paramFloat1 + (int)this.averageWidth);
  }

  FontKey1 copy()
  {
    FontKey1 localFontKey1 = new FontKey1();
    localFontKey1.init(this.font, this.angle, this.scaledSize, this.averageWidth);
    return localFontKey1;
  }

  public boolean equals(Object paramObject)
  {
    FontKey1 localFontKey1;
    try
    {
      localFontKey1 = (FontKey1)paramObject;
      if (localFontKey1 == null)
        return false;
      return ((this.font.equals(localFontKey1.font)) && (this.angle == localFontKey1.angle) && (this.scaledSize == localFontKey1.scaledSize) && (this.averageWidth == localFontKey1.averageWidth));
    }
    catch (ClassCastException localClassCastException)
    {
    }
    return false;
  }

  public int hashCode()
  {
    return this.hash;
  }
}