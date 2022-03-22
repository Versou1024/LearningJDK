package sun.java2d.pipe;

import java.awt.Rectangle;
import java.awt.Shape;
import sun.java2d.SunGraphics2D;
import sun.java2d.loops.MaskFill;

public class AlphaColorPipe
  implements CompositePipe
{
  public Object startSequence(SunGraphics2D paramSunGraphics2D, Shape paramShape, Rectangle paramRectangle, int[] paramArrayOfInt)
  {
    return paramSunGraphics2D;
  }

  public boolean needTile(Object paramObject, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    return true;
  }

  public void renderPathTile(Object paramObject, byte[] paramArrayOfByte, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6)
  {
    SunGraphics2D localSunGraphics2D = (SunGraphics2D)paramObject;
    localSunGraphics2D.alphafill.MaskFill(localSunGraphics2D, localSunGraphics2D.getSurfaceData(), localSunGraphics2D.composite, paramInt3, paramInt4, paramInt5, paramInt6, paramArrayOfByte, paramInt1, paramInt2);
  }

  public void skipTile(Object paramObject, int paramInt1, int paramInt2)
  {
  }

  public void endSequence(Object paramObject)
  {
  }
}