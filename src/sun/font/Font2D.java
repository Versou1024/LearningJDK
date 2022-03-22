package sun.font;

import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Locale;

public abstract class Font2D
{
  public static final int FONT_CONFIG_RANK = 2;
  public static final int JRE_RANK = 2;
  public static final int TTF_RANK = 3;
  public static final int TYPE1_RANK = 4;
  public static final int NATIVE_RANK = 5;
  public static final int UNKNOWN_RANK = 6;
  public static final int DEFAULT_RANK = 4;
  private static final String[] boldNames = { "bold", "demibold", "demi-bold", "demi bold", "negreta", "demi" };
  private static final String[] italicNames = { "italic", "cursiva", "oblique", "inclined" };
  private static final String[] boldItalicNames = { "bolditalic", "bold-italic", "bold italic", "boldoblique", "bold-oblique", "bold oblique", "demibold italic", "negreta cursiva", "demi oblique" };
  private static final FontRenderContext DEFAULT_FRC = new FontRenderContext(null, false, false);
  public Font2DHandle handle;
  protected String familyName;
  protected String fullName;
  protected int style = 0;
  protected FontFamily family;
  protected int fontRank = 4;
  protected CharToGlyphMapper mapper;
  protected HashMap strikeCache = new HashMap();
  protected Reference lastFontStrike = new SoftReference(null);

  public int getStyle()
  {
    return this.style;
  }

  protected void setStyle()
  {
    String str = this.fullName.toLowerCase();
    for (int i = 0; i < boldItalicNames.length; ++i)
      if (str.indexOf(boldItalicNames[i]) != -1)
      {
        this.style = 3;
        return;
      }
    for (i = 0; i < italicNames.length; ++i)
      if (str.indexOf(italicNames[i]) != -1)
      {
        this.style = 2;
        return;
      }
    for (i = 0; i < boldNames.length; ++i)
      if (str.indexOf(boldNames[i]) != -1)
      {
        this.style = 1;
        return;
      }
  }

  int getRank()
  {
    return this.fontRank;
  }

  void setRank(int paramInt)
  {
    this.fontRank = paramInt;
  }

  abstract CharToGlyphMapper getMapper();

  protected int getValidatedGlyphCode(int paramInt)
  {
    if ((paramInt < 0) || (paramInt >= getMapper().getNumGlyphs()))
      paramInt = getMapper().getMissingGlyphCode();
    return paramInt;
  }

  abstract FontStrike createStrike(FontStrikeDesc paramFontStrikeDesc);

  public FontStrike getStrike(Font paramFont)
  {
    FontStrike localFontStrike = (FontStrike)this.lastFontStrike.get();
    if (localFontStrike != null)
      return localFontStrike;
    return getStrike(paramFont, DEFAULT_FRC);
  }

  public FontStrike getStrike(Font paramFont, AffineTransform paramAffineTransform, int paramInt1, int paramInt2)
  {
    double d = paramFont.getSize2D();
    AffineTransform localAffineTransform = (AffineTransform)paramAffineTransform.clone();
    localAffineTransform.scale(d, d);
    if (paramFont.isTransformed())
      localAffineTransform.concatenate(paramFont.getTransform());
    FontStrikeDesc localFontStrikeDesc = new FontStrikeDesc(paramAffineTransform, localAffineTransform, paramFont.getStyle(), paramInt1, paramInt2);
    return getStrike(localFontStrikeDesc, false);
  }

  public FontStrike getStrike(Font paramFont, AffineTransform paramAffineTransform1, AffineTransform paramAffineTransform2, int paramInt1, int paramInt2)
  {
    FontStrikeDesc localFontStrikeDesc = new FontStrikeDesc(paramAffineTransform1, paramAffineTransform2, paramFont.getStyle(), paramInt1, paramInt2);
    return getStrike(localFontStrikeDesc, false);
  }

  public FontStrike getStrike(Font paramFont, FontRenderContext paramFontRenderContext)
  {
    AffineTransform localAffineTransform = paramFontRenderContext.getTransform();
    double d = paramFont.getSize2D();
    localAffineTransform.scale(d, d);
    if (paramFont.isTransformed())
      localAffineTransform.concatenate(paramFont.getTransform());
    int i = FontStrikeDesc.getAAHintIntVal(this, paramFont, paramFontRenderContext);
    int j = FontStrikeDesc.getFMHintIntVal(paramFontRenderContext.getFractionalMetricsHint());
    FontStrikeDesc localFontStrikeDesc = new FontStrikeDesc(paramFontRenderContext.getTransform(), localAffineTransform, paramFont.getStyle(), i, j);
    return getStrike(localFontStrikeDesc, false);
  }

  FontStrike getStrike(FontStrikeDesc paramFontStrikeDesc)
  {
    return getStrike(paramFontStrikeDesc, true);
  }

  private FontStrike getStrike(FontStrikeDesc paramFontStrikeDesc, boolean paramBoolean)
  {
    FontStrike localFontStrike = (FontStrike)this.lastFontStrike.get();
    if ((localFontStrike != null) && (paramFontStrikeDesc.equals(localFontStrike.desc)))
      return localFontStrike;
    synchronized (this.strikeCache)
    {
      localReference = (Reference)this.strikeCache.get(paramFontStrikeDesc);
    }
    if (localReference != null)
    {
      localFontStrike = (FontStrike)localReference.get();
      if (localFontStrike != null)
      {
        this.lastFontStrike = new SoftReference(localFontStrike);
        StrikeCache.refStrike(localFontStrike);
        return localFontStrike;
      }
      ((StrikeCache.DisposableStrike)localReference).getDisposer().dispose();
    }
    if (paramBoolean)
      paramFontStrikeDesc = new FontStrikeDesc(paramFontStrikeDesc);
    localFontStrike = createStrike(paramFontStrikeDesc);
    Reference localReference = StrikeCache.getStrikeRef(localFontStrike);
    synchronized (this.strikeCache)
    {
      this.strikeCache.put(paramFontStrikeDesc, localReference);
    }
    this.lastFontStrike = new SoftReference(localFontStrike);
    StrikeCache.refStrike(localFontStrike);
    return localFontStrike;
  }

  void removeFromCache(FontStrikeDesc paramFontStrikeDesc)
  {
    synchronized (this.strikeCache)
    {
      Reference localReference = (Reference)this.strikeCache.get(paramFontStrikeDesc);
      if (localReference != null)
      {
        Object localObject1 = localReference.get();
        if (localObject1 == null)
          this.strikeCache.remove(paramFontStrikeDesc);
      }
    }
  }

  public void getFontMetrics(Font paramFont, AffineTransform paramAffineTransform, Object paramObject1, Object paramObject2, float[] paramArrayOfFloat)
  {
    int i = FontStrikeDesc.getAAHintIntVal(paramObject1, this, paramFont.getSize());
    int j = FontStrikeDesc.getFMHintIntVal(paramObject2);
    FontStrike localFontStrike = getStrike(paramFont, paramAffineTransform, i, j);
    StrikeMetrics localStrikeMetrics = localFontStrike.getFontMetrics();
    paramArrayOfFloat[0] = localStrikeMetrics.getAscent();
    paramArrayOfFloat[1] = localStrikeMetrics.getDescent();
    paramArrayOfFloat[2] = localStrikeMetrics.getLeading();
    paramArrayOfFloat[3] = localStrikeMetrics.getMaxAdvance();
    getStyleMetrics(paramFont.getSize2D(), paramArrayOfFloat, 4);
  }

  public void getStyleMetrics(float paramFloat, float[] paramArrayOfFloat, int paramInt)
  {
    paramArrayOfFloat[paramInt] = (-paramArrayOfFloat[0] / 2.5F);
    paramArrayOfFloat[(paramInt + 1)] = (paramFloat / 12.0F);
    paramArrayOfFloat[(paramInt + 2)] = (paramArrayOfFloat[(paramInt + 1)] / 1.5F);
    paramArrayOfFloat[(paramInt + 3)] = paramArrayOfFloat[(paramInt + 1)];
  }

  public void getFontMetrics(Font paramFont, FontRenderContext paramFontRenderContext, float[] paramArrayOfFloat)
  {
    StrikeMetrics localStrikeMetrics = getStrike(paramFont, paramFontRenderContext).getFontMetrics();
    paramArrayOfFloat[0] = localStrikeMetrics.getAscent();
    paramArrayOfFloat[1] = localStrikeMetrics.getDescent();
    paramArrayOfFloat[2] = localStrikeMetrics.getLeading();
    paramArrayOfFloat[3] = localStrikeMetrics.getMaxAdvance();
  }

  byte[] getTableBytes(int paramInt)
  {
    return null;
  }

  boolean supportsEncoding(String paramString)
  {
    return false;
  }

  public boolean canDoStyle(int paramInt)
  {
    return (paramInt == this.style);
  }

  public boolean useAAForPtSize(int paramInt)
  {
    return true;
  }

  public boolean hasSupplementaryChars()
  {
    return false;
  }

  public String getPostscriptName()
  {
    return this.fullName;
  }

  public String getFontName(Locale paramLocale)
  {
    return this.fullName;
  }

  public String getFamilyName(Locale paramLocale)
  {
    return this.familyName;
  }

  public int getNumGlyphs()
  {
    return getMapper().getNumGlyphs();
  }

  public int charToGlyph(int paramInt)
  {
    return getMapper().charToGlyph(paramInt);
  }

  public int getMissingGlyphCode()
  {
    return getMapper().getMissingGlyphCode();
  }

  public boolean canDisplay(char paramChar)
  {
    return getMapper().canDisplay(paramChar);
  }

  public boolean canDisplay(int paramInt)
  {
    return getMapper().canDisplay(paramInt);
  }

  public byte getBaselineFor(char paramChar)
  {
    return 0;
  }

  public float getItalicAngle(Font paramFont, AffineTransform paramAffineTransform, Object paramObject1, Object paramObject2)
  {
    int i = FontStrikeDesc.getAAHintIntVal(paramObject1, this, 12);
    int j = FontStrikeDesc.getFMHintIntVal(paramObject2);
    FontStrike localFontStrike = getStrike(paramFont, paramAffineTransform, i, j);
    StrikeMetrics localStrikeMetrics = localFontStrike.getFontMetrics();
    if ((localStrikeMetrics.ascentY == 0F) || (localStrikeMetrics.ascentX == 0F))
      return 0F;
    return (localStrikeMetrics.ascentX / -localStrikeMetrics.ascentY);
  }
}