package sun.font;

import java.security.AccessController;
import sun.security.action.GetPropertyAction;

public final class CompositeFont extends Font2D
{
  private boolean[] deferredInitialisation;
  String[] componentFileNames;
  String[] componentNames;
  private PhysicalFont[] components;
  int numSlots;
  int numMetricsSlots;
  int[] exclusionRanges;
  int[] maxIndices;
  int numGlyphs = 0;
  int localeSlot = -1;
  boolean isStdComposite = true;

  public CompositeFont(String paramString, String[] paramArrayOfString1, String[] paramArrayOfString2, int paramInt, int[] paramArrayOfInt1, int[] paramArrayOfInt2, boolean paramBoolean)
  {
    this.handle = new Font2DHandle(this);
    this.fullName = paramString;
    this.componentFileNames = paramArrayOfString1;
    this.componentNames = paramArrayOfString2;
    if (paramArrayOfString2 == null)
      this.numSlots = this.componentFileNames.length;
    else
      this.numSlots = this.componentNames.length;
    this.numMetricsSlots = paramInt;
    this.exclusionRanges = paramArrayOfInt1;
    this.maxIndices = paramArrayOfInt2;
    if (FontManager.eudcFont != null)
    {
      this.numSlots += 1;
      if (this.componentNames != null)
      {
        this.componentNames = new String[this.numSlots];
        System.arraycopy(paramArrayOfString2, 0, this.componentNames, 0, this.numSlots - 1);
        this.componentNames[(this.numSlots - 1)] = FontManager.eudcFont.getFontName(null);
      }
      if (this.componentFileNames != null)
      {
        this.componentFileNames = new String[this.numSlots];
        System.arraycopy(paramArrayOfString1, 0, this.componentFileNames, 0, this.numSlots - 1);
      }
      this.components = new PhysicalFont[this.numSlots];
      this.components[(this.numSlots - 1)] = FontManager.eudcFont;
      this.deferredInitialisation = new boolean[this.numSlots];
      if (paramBoolean)
        for (i = 0; i < this.numSlots - 1; ++i)
          this.deferredInitialisation[i] = true;
    }
    else
    {
      this.components = new PhysicalFont[this.numSlots];
      this.deferredInitialisation = new boolean[this.numSlots];
      if (paramBoolean)
        for (i = 0; i < this.numSlots; ++i)
          this.deferredInitialisation[i] = true;
    }
    this.fontRank = 2;
    int i = this.fullName.indexOf(46);
    if (i > 0)
    {
      this.familyName = this.fullName.substring(0, i);
      if (i + 1 < this.fullName.length())
      {
        String str = this.fullName.substring(i + 1);
        if ("plain".equals(str))
          this.style = 0;
        else if ("bold".equals(str))
          this.style = 1;
        else if ("italic".equals(str))
          this.style = 2;
        else if ("bolditalic".equals(str))
          this.style = 3;
      }
    }
    else
    {
      this.familyName = this.fullName;
    }
  }

  CompositeFont(PhysicalFont paramPhysicalFont, CompositeFont paramCompositeFont)
  {
    this.isStdComposite = false;
    this.handle = new Font2DHandle(this);
    this.fullName = paramPhysicalFont.fullName;
    this.familyName = paramPhysicalFont.familyName;
    this.style = paramPhysicalFont.style;
    this.numMetricsSlots = 1;
    this.numSlots = (paramCompositeFont.numSlots + 1);
    synchronized (FontManager.class)
    {
      this.components = new PhysicalFont[this.numSlots];
      this.components[0] = paramPhysicalFont;
      System.arraycopy(paramCompositeFont.components, 0, this.components, 1, paramCompositeFont.numSlots);
      if (paramCompositeFont.componentNames != null)
      {
        this.componentNames = new String[this.numSlots];
        this.componentNames[0] = paramPhysicalFont.fullName;
        System.arraycopy(paramCompositeFont.componentNames, 0, this.componentNames, 1, paramCompositeFont.numSlots);
      }
      if (paramCompositeFont.componentFileNames != null)
      {
        this.componentFileNames = new String[this.numSlots];
        this.componentFileNames[0] = null;
        System.arraycopy(paramCompositeFont.componentFileNames, 0, this.componentFileNames, 1, paramCompositeFont.numSlots);
      }
      this.deferredInitialisation = new boolean[this.numSlots];
      this.deferredInitialisation[0] = false;
      System.arraycopy(paramCompositeFont.deferredInitialisation, 0, this.deferredInitialisation, 1, paramCompositeFont.numSlots);
    }
  }

  private void doDeferredInitialisation(int paramInt)
  {
    if (this.deferredInitialisation[paramInt] == 0)
      return;
    synchronized (FontManager.class)
    {
      if (this.componentNames == null)
        this.componentNames = new String[this.numSlots];
      if (this.components[paramInt] == null)
      {
        if ((this.componentFileNames != null) && (this.componentFileNames[paramInt] != null))
          this.components[paramInt] = FontManager.initialiseDeferredFont(this.componentFileNames[paramInt]);
        if (this.components[paramInt] == null)
          this.components[paramInt] = FontManager.getDefaultPhysicalFont();
        String str = this.components[paramInt].getFontName(null);
        if (this.componentNames[paramInt] == null)
          this.componentNames[paramInt] = str;
        else if (!(this.componentNames[paramInt].equalsIgnoreCase(str)))
          this.components[paramInt] = ((PhysicalFont)FontManager.findFont2D(this.componentNames[paramInt], this.style, 1));
      }
      this.deferredInitialisation[paramInt] = false;
    }
  }

  void replaceComponentFont(PhysicalFont paramPhysicalFont1, PhysicalFont paramPhysicalFont2)
  {
    if (this.components == null)
      return;
    for (int i = 0; i < this.numSlots; ++i)
      if (this.components[i] == paramPhysicalFont1)
      {
        this.components[i] = paramPhysicalFont2;
        if (this.componentNames != null)
          this.componentNames[i] = paramPhysicalFont2.getFontName(null);
      }
  }

  public boolean isExcludedChar(int paramInt1, int paramInt2)
  {
    if ((this.exclusionRanges == null) || (this.maxIndices == null) || (paramInt1 >= this.numMetricsSlots))
      return false;
    int i = 0;
    int j = this.maxIndices[paramInt1];
    if (paramInt1 > 0)
      i = this.maxIndices[(paramInt1 - 1)];
    for (int k = i; j > k; k += 2)
      if ((paramInt2 >= this.exclusionRanges[k]) && (paramInt2 <= this.exclusionRanges[(k + 1)]))
        return true;
    return false;
  }

  public void getStyleMetrics(float paramFloat, float[] paramArrayOfFloat, int paramInt)
  {
    PhysicalFont localPhysicalFont = getSlotFont(0);
    if (localPhysicalFont == null)
      super.getStyleMetrics(paramFloat, paramArrayOfFloat, paramInt);
    else
      localPhysicalFont.getStyleMetrics(paramFloat, paramArrayOfFloat, paramInt);
  }

  public int getNumSlots()
  {
    return this.numSlots;
  }

  public PhysicalFont getSlotFont(int paramInt)
  {
    if (this.deferredInitialisation[paramInt] != 0)
      doDeferredInitialisation(paramInt);
    try
    {
      PhysicalFont localPhysicalFont = this.components[paramInt];
      if (localPhysicalFont == null)
        try
        {
          localPhysicalFont = (PhysicalFont)FontManager.findFont2D(this.componentNames[paramInt], this.style, 1);
          this.components[paramInt] = localPhysicalFont;
        }
        catch (ClassCastException localClassCastException)
        {
          localPhysicalFont = FontManager.getDefaultPhysicalFont();
        }
      return localPhysicalFont;
    }
    catch (Exception localException)
    {
    }
    return FontManager.getDefaultPhysicalFont();
  }

  FontStrike createStrike(FontStrikeDesc paramFontStrikeDesc)
  {
    return new CompositeStrike(this, paramFontStrikeDesc);
  }

  public boolean isStdComposite()
  {
    return this.isStdComposite;
  }

  protected int getValidatedGlyphCode(int paramInt)
  {
    int i = paramInt >>> 24;
    if (i >= this.numSlots)
      return getMapper().getMissingGlyphCode();
    int j = paramInt & 0xFFFFFF;
    PhysicalFont localPhysicalFont = getSlotFont(i);
    if (localPhysicalFont.getValidatedGlyphCode(j) == localPhysicalFont.getMissingGlyphCode())
      return getMapper().getMissingGlyphCode();
    return paramInt;
  }

  public CharToGlyphMapper getMapper()
  {
    if (this.mapper == null)
      this.mapper = new CompositeGlyphMapper(this);
    return this.mapper;
  }

  public boolean hasSupplementaryChars()
  {
    for (int i = 0; i < this.numSlots; ++i)
      if (getSlotFont(i).hasSupplementaryChars())
        return true;
    return false;
  }

  public int getNumGlyphs()
  {
    if (this.numGlyphs == 0)
      this.numGlyphs = getMapper().getNumGlyphs();
    return this.numGlyphs;
  }

  public int getMissingGlyphCode()
  {
    return getMapper().getMissingGlyphCode();
  }

  public boolean canDisplay(char paramChar)
  {
    return getMapper().canDisplay(paramChar);
  }

  public boolean useAAForPtSize(int paramInt)
  {
    if (this.localeSlot == -1)
    {
      int i = this.numMetricsSlots;
      if ((i == 1) && (!(isStdComposite())))
        i = this.numSlots;
      for (int j = 0; j < i; ++j)
        if (getSlotFont(j).supportsEncoding(null))
        {
          this.localeSlot = j;
          break;
        }
      if (this.localeSlot == -1)
        this.localeSlot = 0;
    }
    return getSlotFont(this.localeSlot).useAAForPtSize(paramInt);
  }

  public String toString()
  {
    String str1 = (String)AccessController.doPrivileged(new GetPropertyAction("line.separator"));
    String str2 = "";
    for (int i = 0; i < this.numSlots; ++i)
      str2 = str2 + "    Slot[" + i + "]=" + getSlotFont(i) + str1;
    return "** Composite Font: Family=" + this.familyName + " Name=" + this.fullName + " style=" + this.style + str1 + str2;
  }
}