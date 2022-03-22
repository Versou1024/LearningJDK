package sun.java2d.pipe;

import java.awt.font.GlyphVector;
import sun.java2d.SunGraphics2D;

public abstract interface TextPipe
{
  public abstract void drawString(SunGraphics2D paramSunGraphics2D, String paramString, double paramDouble1, double paramDouble2);

  public abstract void drawGlyphVector(SunGraphics2D paramSunGraphics2D, GlyphVector paramGlyphVector, float paramFloat1, float paramFloat2);

  public abstract void drawChars(SunGraphics2D paramSunGraphics2D, char[] paramArrayOfChar, int paramInt1, int paramInt2, int paramInt3, int paramInt4);
}