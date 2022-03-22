package sun.font;

import java.util.HashMap;
import java.util.Locale;
import java.util.logging.Logger;

public class FontFamily
{
  private static HashMap familyNameMap = new HashMap();
  private static HashMap allLocaleNames;
  protected String familyName;
  protected Font2D plain;
  protected Font2D bold;
  protected Font2D italic;
  protected Font2D bolditalic;
  protected boolean logicalFont = false;
  protected int familyRank;

  public static FontFamily getFamily(String paramString)
  {
    synchronized (familyNameMap)
    {
      return ((FontFamily)familyNameMap.get(paramString.toLowerCase(Locale.ENGLISH)));
    }
  }

  public static String[] getAllFamilyNames()
  {
    return null;
  }

  static void remove(Font2D paramFont2D)
  {
    String str = paramFont2D.getFamilyName(Locale.ENGLISH);
    FontFamily localFontFamily = getFamily(str);
    if (localFontFamily == null)
      return;
    if (localFontFamily.plain == paramFont2D)
      localFontFamily.plain = null;
    if (localFontFamily.bold == paramFont2D)
      localFontFamily.bold = null;
    if (localFontFamily.italic == paramFont2D)
      localFontFamily.italic = null;
    if (localFontFamily.bolditalic == paramFont2D)
      localFontFamily.bolditalic = null;
    if ((localFontFamily.plain == null) && (localFontFamily.bold == null) && (localFontFamily.plain == null) && (localFontFamily.bold == null))
      synchronized (familyNameMap)
      {
        familyNameMap.remove(str);
      }
  }

  public FontFamily(String paramString, boolean paramBoolean, int paramInt)
  {
    this.logicalFont = paramBoolean;
    this.familyName = paramString;
    this.familyRank = paramInt;
    synchronized (familyNameMap)
    {
      familyNameMap.put(paramString.toLowerCase(Locale.ENGLISH), this);
    }
  }

  FontFamily(String paramString)
  {
    this.logicalFont = false;
    this.familyName = paramString;
    this.familyRank = 4;
  }

  public String getFamilyName()
  {
    return this.familyName;
  }

  public int getRank()
  {
    return this.familyRank;
  }

  public void setFont(Font2D paramFont2D, int paramInt)
  {
    if (paramFont2D.getRank() > this.familyRank)
    {
      if (FontManager.logging)
        FontManager.logger.warning("Rejecting adding " + paramFont2D + " of lower rank " + paramFont2D.getRank() + " to family " + this + " of rank " + this.familyRank);
      return;
    }
    switch (paramInt)
    {
    case 0:
      this.plain = paramFont2D;
      break;
    case 1:
      this.bold = paramFont2D;
      break;
    case 2:
      this.italic = paramFont2D;
      break;
    case 3:
      this.bolditalic = paramFont2D;
    }
  }

  public Font2D getFontWithExactStyleMatch(int paramInt)
  {
    switch (paramInt)
    {
    case 0:
      return this.plain;
    case 1:
      return this.bold;
    case 2:
      return this.italic;
    case 3:
      return this.bolditalic;
    }
    return null;
  }

  public Font2D getFont(int paramInt)
  {
    switch (paramInt)
    {
    case 0:
      return this.plain;
    case 1:
      if (this.bold != null)
        return this.bold;
      if ((this.plain != null) && (this.plain.canDoStyle(paramInt)))
        return this.plain;
      return null;
    case 2:
      if (this.italic != null)
        return this.italic;
      if ((this.plain != null) && (this.plain.canDoStyle(paramInt)))
        return this.plain;
      return null;
    case 3:
      if (this.bolditalic != null)
        return this.bolditalic;
      if ((this.italic != null) && (this.italic.canDoStyle(paramInt)))
        return this.italic;
      if ((this.bold != null) && (this.bold.canDoStyle(paramInt)))
        return this.italic;
      if ((this.plain != null) && (this.plain.canDoStyle(paramInt)))
        return this.plain;
      return null;
    }
    return null;
  }

  Font2D getClosestStyle(int paramInt)
  {
    switch (paramInt)
    {
    case 0:
      if (this.bold != null)
        return this.bold;
      if (this.italic != null)
        return this.italic;
      return this.bolditalic;
    case 1:
      if (this.plain != null)
        return this.plain;
      if (this.bolditalic != null)
        return this.bolditalic;
      return this.italic;
    case 2:
      if (this.bolditalic != null)
        return this.bolditalic;
      if (this.plain != null)
        return this.plain;
      return this.bold;
    case 3:
      if (this.italic != null)
        return this.italic;
      if (this.bold != null)
        return this.bold;
      return this.plain;
    }
    return null;
  }

  static synchronized void addLocaleNames(FontFamily paramFontFamily, String[] paramArrayOfString)
  {
    if (allLocaleNames == null)
      allLocaleNames = new HashMap();
    for (int i = 0; i < paramArrayOfString.length; ++i)
      allLocaleNames.put(paramArrayOfString[i].toLowerCase(), paramFontFamily);
  }

  public static synchronized FontFamily getLocaleFamily(String paramString)
  {
    if (allLocaleNames == null)
      return null;
    return ((FontFamily)allLocaleNames.get(paramString.toLowerCase()));
  }

  public String toString()
  {
    return "Font family: " + this.familyName + " plain=" + this.plain + " bold=" + this.bold + " italic=" + this.italic + " bolditalic=" + this.bolditalic;
  }
}