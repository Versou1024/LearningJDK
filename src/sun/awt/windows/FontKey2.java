package sun.awt.windows;

class FontKey2
{
  private String name;
  private int style;
  private int size;
  private int angle;
  private float averageWidth;
  private int hash;

  void init(String paramString, int paramInt1, int paramInt2, int paramInt3, float paramFloat)
  {
    this.name = paramString;
    this.style = paramInt1;
    this.size = paramInt2;
    this.angle = paramInt3;
    this.averageWidth = paramFloat;
    this.hash = (paramString.hashCode() + paramInt1 + paramInt2 + paramInt3 + (int)this.averageWidth);
  }

  FontKey2 copy()
  {
    FontKey2 localFontKey2 = new FontKey2();
    localFontKey2.init(this.name, this.style, this.size, this.angle, this.averageWidth);
    return localFontKey2;
  }

  public boolean equals(Object paramObject)
  {
    FontKey2 localFontKey2;
    try
    {
      localFontKey2 = (FontKey2)paramObject;
      if (localFontKey2 == null)
        return false;
      return ((this.name.equals(localFontKey2.name)) && (this.style == localFontKey2.style) && (this.size == localFontKey2.size) && (this.angle == localFontKey2.angle) && (this.averageWidth == localFontKey2.averageWidth));
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