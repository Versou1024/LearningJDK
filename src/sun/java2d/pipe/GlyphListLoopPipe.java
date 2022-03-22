package sun.java2d.pipe;

import sun.font.GlyphList;
import sun.java2d.SunGraphics2D;
import sun.java2d.loops.DrawGlyphList;
import sun.java2d.loops.DrawGlyphListAA;
import sun.java2d.loops.DrawGlyphListLCD;
import sun.java2d.loops.RenderLoops;

public abstract class GlyphListLoopPipe extends GlyphListPipe
{
  protected void drawGlyphList(SunGraphics2D paramSunGraphics2D, GlyphList paramGlyphList, int paramInt)
  {
    switch (paramInt)
    {
    case 1:
      paramSunGraphics2D.loops.drawGlyphListLoop.DrawGlyphList(paramSunGraphics2D, paramSunGraphics2D.surfaceData, paramGlyphList);
      return;
    case 2:
      paramSunGraphics2D.loops.drawGlyphListAALoop.DrawGlyphListAA(paramSunGraphics2D, paramSunGraphics2D.surfaceData, paramGlyphList);
      return;
    case 4:
    case 6:
      paramSunGraphics2D.loops.drawGlyphListLCDLoop.DrawGlyphListLCD(paramSunGraphics2D, paramSunGraphics2D.surfaceData, paramGlyphList);
      return;
    case 3:
    case 5:
    }
  }
}