package sun.java2d.pipe;

import java.awt.Rectangle;
import java.awt.Shape;
import sun.font.GlyphList;
import sun.java2d.SunGraphics2D;

public class TextRenderer extends GlyphListPipe
{
  CompositePipe outpipe;

  public TextRenderer(CompositePipe paramCompositePipe)
  {
    this.outpipe = paramCompositePipe;
  }

  protected void drawGlyphList(SunGraphics2D paramSunGraphics2D, GlyphList paramGlyphList)
  {
    int i = paramGlyphList.getNumGlyphs();
    Region localRegion = paramSunGraphics2D.getCompClip();
    int j = localRegion.getLoX();
    int k = localRegion.getLoY();
    int l = localRegion.getHiX();
    int i1 = localRegion.getHiY();
    Object localObject1 = null;
    try
    {
      int[] arrayOfInt1 = paramGlyphList.getBounds();
      Rectangle localRectangle = new Rectangle(arrayOfInt1[0], arrayOfInt1[1], arrayOfInt1[2] - arrayOfInt1[0], arrayOfInt1[3] - arrayOfInt1[1]);
      Shape localShape = paramSunGraphics2D.untransformShape(localRectangle);
      localObject1 = this.outpipe.startSequence(paramSunGraphics2D, localShape, localRectangle, arrayOfInt1);
      for (int i2 = 0; i2 < i; ++i2)
      {
        paramGlyphList.setGlyphIndex(i2);
        int[] arrayOfInt2 = paramGlyphList.getMetrics();
        int i3 = arrayOfInt2[0];
        int i4 = arrayOfInt2[1];
        int i5 = arrayOfInt2[2];
        int i6 = i3 + i5;
        int i7 = i4 + arrayOfInt2[3];
        int i8 = 0;
        if (i3 < j)
        {
          i8 = j - i3;
          i3 = j;
        }
        if (i4 < k)
        {
          i8 += (k - i4) * i5;
          i4 = k;
        }
        if (i6 > l)
          i6 = l;
        if (i7 > i1)
          i7 = i1;
        if ((i6 > i3) && (i7 > i4) && (this.outpipe.needTile(localObject1, i3, i4, i6 - i3, i7 - i4)))
        {
          byte[] arrayOfByte = paramGlyphList.getGrayBits();
          this.outpipe.renderPathTile(localObject1, arrayOfByte, i8, i5, i3, i4, i6 - i3, i7 - i4);
        }
        else
        {
          this.outpipe.skipTile(localObject1, i3, i4);
        }
      }
    }
    finally
    {
      if (localObject1 != null)
        this.outpipe.endSequence(localObject1);
    }
  }
}