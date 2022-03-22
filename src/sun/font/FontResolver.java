package sun.font;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.Map;
import sun.text.CodePointIterator;

public final class FontResolver
{
  private Font[] allFonts;
  private Font[] supplementaryFonts;
  private int[] supplementaryIndices;
  private static final int DEFAULT_SIZE = 12;
  private Font defaultFont = new Font("Dialog", 0, 12);
  private static final int SHIFT = 9;
  private static final int BLOCKSIZE = 128;
  private static final int MASK = 127;
  private int[][] blocks = new int[512][];
  private static FontResolver INSTANCE;

  private Font[] getAllFonts()
  {
    if (this.allFonts == null)
    {
      this.allFonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
      for (int i = 0; i < this.allFonts.length; ++i)
        this.allFonts[i] = this.allFonts[i].deriveFont(12.0F);
    }
    return this.allFonts;
  }

  private int getIndexFor(char paramChar)
  {
    if (this.defaultFont.canDisplay(paramChar))
      return 1;
    for (int i = 0; i < getAllFonts().length; ++i)
      if (this.allFonts[i].canDisplay(paramChar))
        return (i + 2);
    return 1;
  }

  private Font[] getAllSCFonts()
  {
    if (this.supplementaryFonts == null)
    {
      ArrayList localArrayList1 = new ArrayList();
      ArrayList localArrayList2 = new ArrayList();
      for (int i = 0; i < getAllFonts().length; ++i)
      {
        Font localFont = this.allFonts[i];
        Font2D localFont2D = FontManager.getFont2D(localFont);
        if (localFont2D.hasSupplementaryChars())
        {
          localArrayList1.add(localFont);
          localArrayList2.add(new Integer(i));
        }
      }
      i = localArrayList1.size();
      this.supplementaryIndices = new int[i];
      for (int j = 0; j < i; ++j)
        this.supplementaryIndices[j] = ((Integer)localArrayList2.get(j)).intValue();
      this.supplementaryFonts = ((Font[])localArrayList1.toArray(new Font[i]));
    }
    return this.supplementaryFonts;
  }

  private int getIndexFor(int paramInt)
  {
    if (this.defaultFont.canDisplay(paramInt))
      return 1;
    for (int i = 0; i < getAllSCFonts().length; ++i)
      if (this.supplementaryFonts[i].canDisplay(paramInt))
        return (this.supplementaryIndices[i] + 2);
    return 1;
  }

  public int getFontIndex(char paramChar)
  {
    int i = paramChar >> '\t';
    int[] arrayOfInt = this.blocks[i];
    if (arrayOfInt == null)
    {
      arrayOfInt = new int[128];
      this.blocks[i] = arrayOfInt;
    }
    int j = paramChar & 0x7F;
    if (arrayOfInt[j] == 0)
      arrayOfInt[j] = getIndexFor(paramChar);
    return arrayOfInt[j];
  }

  public int getFontIndex(int paramInt)
  {
    if (paramInt < 65536)
      return getFontIndex((char)paramInt);
    return getIndexFor(paramInt);
  }

  public int nextFontRunIndex(CodePointIterator paramCodePointIterator)
  {
    int i = paramCodePointIterator.next();
    int j = 1;
    if (i != -1)
    {
      j = getFontIndex(i);
      do
        if ((i = paramCodePointIterator.next()) == -1)
          break label45;
      while (getFontIndex(i) == j);
      paramCodePointIterator.prev();
    }
    label45: return j;
  }

  public Font getFont(int paramInt, Map paramMap)
  {
    Font localFont = this.defaultFont;
    if (paramInt >= 2)
      localFont = this.allFonts[(paramInt - 2)];
    return localFont.deriveFont(paramMap);
  }

  public static FontResolver getInstance()
  {
    if (INSTANCE == null)
      INSTANCE = new FontResolver();
    return INSTANCE;
  }
}