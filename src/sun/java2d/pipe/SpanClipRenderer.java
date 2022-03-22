package sun.java2d.pipe;

import java.awt.Rectangle;
import java.awt.Shape;
import sun.java2d.SunGraphics2D;

public class SpanClipRenderer
  implements CompositePipe
{
  CompositePipe outpipe;
  static Class RegionClass = Region.class;
  static Class RegionIteratorClass = RegionIterator.class;

  static native void initIDs(Class paramClass1, Class paramClass2);

  public SpanClipRenderer(CompositePipe paramCompositePipe)
  {
    this.outpipe = paramCompositePipe;
  }

  public Object startSequence(SunGraphics2D paramSunGraphics2D, Shape paramShape, Rectangle paramRectangle, int[] paramArrayOfInt)
  {
    RegionIterator localRegionIterator = paramSunGraphics2D.clipRegion.getIterator();
    return new SCRcontext(this, localRegionIterator, this.outpipe.startSequence(paramSunGraphics2D, paramShape, paramRectangle, paramArrayOfInt));
  }

  public boolean needTile(Object paramObject, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    SCRcontext localSCRcontext = (SCRcontext)paramObject;
    return this.outpipe.needTile(localSCRcontext.outcontext, paramInt1, paramInt2, paramInt3, paramInt4);
  }

  public void renderPathTile(Object paramObject, byte[] paramArrayOfByte, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6, ShapeSpanIterator paramShapeSpanIterator)
  {
    renderPathTile(paramObject, paramArrayOfByte, paramInt1, paramInt2, paramInt3, paramInt4, paramInt5, paramInt6);
  }

  public void renderPathTile(Object paramObject, byte[] paramArrayOfByte, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6)
  {
    SCRcontext localSCRcontext = (SCRcontext)paramObject;
    RegionIterator localRegionIterator = localSCRcontext.iterator.createCopy();
    int[] arrayOfInt = localSCRcontext.band;
    arrayOfInt[0] = paramInt3;
    arrayOfInt[1] = paramInt4;
    arrayOfInt[2] = (paramInt3 + paramInt5);
    arrayOfInt[3] = (paramInt4 + paramInt6);
    if (paramArrayOfByte == null)
    {
      int i = paramInt5 * paramInt6;
      paramArrayOfByte = localSCRcontext.tile;
      if ((paramArrayOfByte != null) && (paramArrayOfByte.length < i))
        paramArrayOfByte = null;
      if (paramArrayOfByte == null)
      {
        paramArrayOfByte = new byte[i];
        localSCRcontext.tile = paramArrayOfByte;
      }
      paramInt1 = 0;
      paramInt2 = paramInt5;
      fillTile(localRegionIterator, paramArrayOfByte, paramInt1, paramInt2, arrayOfInt);
    }
    else
    {
      eraseTile(localRegionIterator, paramArrayOfByte, paramInt1, paramInt2, arrayOfInt);
    }
    if ((arrayOfInt[2] > arrayOfInt[0]) && (arrayOfInt[3] > arrayOfInt[1]))
    {
      paramInt1 += (arrayOfInt[1] - paramInt4) * paramInt2 + arrayOfInt[0] - paramInt3;
      this.outpipe.renderPathTile(localSCRcontext.outcontext, paramArrayOfByte, paramInt1, paramInt2, arrayOfInt[0], arrayOfInt[1], arrayOfInt[2] - arrayOfInt[0], arrayOfInt[3] - arrayOfInt[1]);
    }
  }

  public native void fillTile(RegionIterator paramRegionIterator, byte[] paramArrayOfByte, int paramInt1, int paramInt2, int[] paramArrayOfInt);

  public native void eraseTile(RegionIterator paramRegionIterator, byte[] paramArrayOfByte, int paramInt1, int paramInt2, int[] paramArrayOfInt);

  public void skipTile(Object paramObject, int paramInt1, int paramInt2)
  {
    SCRcontext localSCRcontext = (SCRcontext)paramObject;
    this.outpipe.skipTile(localSCRcontext.outcontext, paramInt1, paramInt2);
  }

  public void endSequence(Object paramObject)
  {
    SCRcontext localSCRcontext = (SCRcontext)paramObject;
    this.outpipe.endSequence(localSCRcontext.outcontext);
  }

  static
  {
    initIDs(RegionClass, RegionIteratorClass);
  }

  class SCRcontext
  {
    RegionIterator iterator;
    Object outcontext;
    int[] band;
    byte[] tile;

    public SCRcontext(, RegionIterator paramRegionIterator, Object paramObject)
    {
      this.iterator = paramRegionIterator;
      this.outcontext = paramObject;
      this.band = new int[4];
    }
  }
}