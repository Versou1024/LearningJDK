package sun.java2d.pipe;

import java.awt.BasicStroke;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.PathIterator;
import sun.dc.pr.PRException;
import sun.dc.pr.Rasterizer;
import sun.java2d.SunGraphics2D;

public class DuctusShapeRenderer extends DuctusRenderer
  implements ShapeDrawPipe
{
  CompositePipe outpipe;

  public DuctusShapeRenderer(CompositePipe paramCompositePipe)
  {
    this.outpipe = paramCompositePipe;
  }

  public void draw(SunGraphics2D paramSunGraphics2D, Shape paramShape)
  {
    BasicStroke localBasicStroke;
    if (paramSunGraphics2D.stroke instanceof BasicStroke)
    {
      localBasicStroke = (BasicStroke)paramSunGraphics2D.stroke;
    }
    else
    {
      paramShape = paramSunGraphics2D.stroke.createStrokedShape(paramShape);
      localBasicStroke = null;
    }
    renderPath(paramSunGraphics2D, paramShape, localBasicStroke);
  }

  public void fill(SunGraphics2D paramSunGraphics2D, Shape paramShape)
  {
    renderPath(paramSunGraphics2D, paramShape, null);
  }

  public void renderPath(SunGraphics2D paramSunGraphics2D, Shape paramShape, BasicStroke paramBasicStroke)
  {
    PathIterator localPathIterator = paramShape.getPathIterator(paramSunGraphics2D.transform);
    boolean bool1 = (paramBasicStroke != null) && (paramSunGraphics2D.strokeHint != 2);
    boolean bool2 = paramSunGraphics2D.strokeState <= 1;
    Rasterizer localRasterizer = createShapeRasterizer(localPathIterator, paramSunGraphics2D.transform, paramBasicStroke, bool2, bool1, 0.5F);
    Object localObject1 = null;
    byte[] arrayOfByte1 = null;
    try
    {
      int[] arrayOfInt = new int[4];
      localRasterizer.getAlphaBox(arrayOfInt);
      Rectangle localRectangle = new Rectangle(arrayOfInt[0], arrayOfInt[1], arrayOfInt[2] - arrayOfInt[0], arrayOfInt[3] - arrayOfInt[1]);
      paramSunGraphics2D.getCompClip().clipBoxToBounds(arrayOfInt);
      if ((arrayOfInt[0] >= arrayOfInt[2]) || (arrayOfInt[1] >= arrayOfInt[3]))
        return;
      localRasterizer.setOutputArea(arrayOfInt[0], arrayOfInt[1], arrayOfInt[2] - arrayOfInt[0], arrayOfInt[3] - arrayOfInt[1]);
      localObject1 = this.outpipe.startSequence(paramSunGraphics2D, paramShape, localRectangle, arrayOfInt);
      int i = Rasterizer.TILE_SIZE;
      arrayOfByte1 = getAlphaTile();
      int j = arrayOfInt[1];
      while (j < arrayOfInt[3])
      {
        int k = arrayOfInt[0];
        while (k < arrayOfInt[2])
        {
          int l = Math.min(i, arrayOfInt[2] - k);
          int i1 = Math.min(i, arrayOfInt[3] - j);
          int i2 = localRasterizer.getTileState();
          if ((i2 == 0) || (!(this.outpipe.needTile(localObject1, k, j, l, i1))))
          {
            localRasterizer.nextTile();
            this.outpipe.skipTile(localObject1, k, j);
          }
          else
          {
            byte[] arrayOfByte2;
            if (i2 == 2)
            {
              arrayOfByte2 = arrayOfByte1;
              getAlpha(localRasterizer, arrayOfByte1, 1, i, 0);
            }
            else
            {
              arrayOfByte2 = null;
              localRasterizer.nextTile();
            }
            this.outpipe.renderPathTile(localObject1, arrayOfByte2, 0, i, k, j, l, i1);
          }
          k += i;
        }
        j += i;
      }
    }
    catch (PRException localPRException)
    {
      localPRException.printStackTrace();
    }
    finally
    {
      dropRasterizer(localRasterizer);
      if (localObject1 != null)
        this.outpipe.endSequence(localObject1);
      if (arrayOfByte1 != null)
        dropAlphaTile(arrayOfByte1);
    }
  }
}