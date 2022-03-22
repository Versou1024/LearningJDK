package sun.java2d.pipe;

import java.awt.Paint;
import java.awt.PaintContext;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.lang.ref.WeakReference;
import sun.awt.image.BufImgSurfaceData;
import sun.java2d.SunGraphics2D;
import sun.java2d.SurfaceData;
import sun.java2d.loops.Blit;
import sun.java2d.loops.CompositeType;
import sun.java2d.loops.MaskBlit;

public class AlphaPaintPipe
  implements CompositePipe
{
  static WeakReference cachedLastRaster;
  static WeakReference cachedLastColorModel;
  static WeakReference cachedLastData;
  private static final int TILE_SIZE = 32;

  public Object startSequence(SunGraphics2D paramSunGraphics2D, Shape paramShape, Rectangle paramRectangle, int[] paramArrayOfInt)
  {
    PaintContext localPaintContext = paramSunGraphics2D.paint.createContext(paramSunGraphics2D.getDeviceColorModel(), paramRectangle, paramShape.getBounds2D(), paramSunGraphics2D.cloneTransform(), paramSunGraphics2D.getRenderingHints());
    return new TileContext(paramSunGraphics2D, localPaintContext);
  }

  public boolean needTile(Object paramObject, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    return true;
  }

  public void renderPathTile(Object paramObject, byte[] paramArrayOfByte, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6)
  {
    TileContext localTileContext = (TileContext)paramObject;
    PaintContext localPaintContext = localTileContext.paintCtxt;
    SunGraphics2D localSunGraphics2D = localTileContext.sunG2D;
    SurfaceData localSurfaceData1 = localTileContext.dstData;
    SurfaceData localSurfaceData2 = null;
    Object localObject1 = null;
    if ((localTileContext.lastData != null) && (localTileContext.lastRaster != null))
    {
      localSurfaceData2 = (SurfaceData)localTileContext.lastData.get();
      localObject1 = (Raster)localTileContext.lastRaster.get();
      if ((localSurfaceData2 == null) || (localObject1 == null))
      {
        localSurfaceData2 = null;
        localObject1 = null;
      }
    }
    ColorModel localColorModel = localTileContext.paintModel;
    for (int i = 0; i < paramInt6; i += 32)
    {
      int j = paramInt4 + i;
      int k = Math.min(paramInt6 - i, 32);
      for (int l = 0; l < paramInt5; l += 32)
      {
        Object localObject2;
        int i1 = paramInt3 + l;
        int i2 = Math.min(paramInt5 - l, 32);
        Raster localRaster = localPaintContext.getRaster(i1, j, i2, k);
        if ((localRaster.getMinX() != 0) || (localRaster.getMinY() != 0))
          localRaster = localRaster.createTranslatedChild(0, 0);
        if (localObject1 != localRaster)
        {
          localObject1 = localRaster;
          localTileContext.lastRaster = new WeakReference(localObject1);
          localObject2 = new BufferedImage(localColorModel, (WritableRaster)localRaster, localColorModel.isAlphaPremultiplied(), null);
          localSurfaceData2 = BufImgSurfaceData.createData((BufferedImage)localObject2);
          localTileContext.lastData = new WeakReference(localSurfaceData2);
          localTileContext.lastMask = null;
          localTileContext.lastBlit = null;
        }
        if (paramArrayOfByte == null)
        {
          if (localTileContext.lastBlit == null)
          {
            localObject2 = localSunGraphics2D.imageComp;
            if ((CompositeType.SrcOverNoEa.equals(localObject2)) && (localColorModel.getTransparency() == 1))
              localObject2 = CompositeType.SrcNoEa;
            localTileContext.lastBlit = Blit.getFromCache(localSurfaceData2.getSurfaceType(), (CompositeType)localObject2, localSurfaceData1.getSurfaceType());
          }
          localTileContext.lastBlit.Blit(localSurfaceData2, localSurfaceData1, localSunGraphics2D.composite, null, 0, 0, i1, j, i2, k);
        }
        else
        {
          if (localTileContext.lastMask == null)
          {
            localObject2 = localSunGraphics2D.imageComp;
            if ((CompositeType.SrcOverNoEa.equals(localObject2)) && (localColorModel.getTransparency() == 1))
              localObject2 = CompositeType.SrcNoEa;
            localTileContext.lastMask = MaskBlit.getFromCache(localSurfaceData2.getSurfaceType(), (CompositeType)localObject2, localSurfaceData1.getSurfaceType());
          }
          int i3 = paramInt1 + i * paramInt2 + l;
          localTileContext.lastMask.MaskBlit(localSurfaceData2, localSurfaceData1, localSunGraphics2D.composite, null, 0, 0, i1, j, i2, k, paramArrayOfByte, i3, paramInt2);
        }
      }
    }
  }

  public void skipTile(Object paramObject, int paramInt1, int paramInt2)
  {
  }

  public void endSequence(Object paramObject)
  {
    TileContext localTileContext = (TileContext)paramObject;
    if (localTileContext.paintCtxt != null)
      localTileContext.paintCtxt.dispose();
    synchronized (AlphaPaintPipe.class)
    {
      if (localTileContext.lastData != null)
      {
        cachedLastRaster = localTileContext.lastRaster;
        if ((cachedLastColorModel == null) || (cachedLastColorModel.get() != localTileContext.paintModel))
          cachedLastColorModel = new WeakReference(localTileContext.paintModel);
        cachedLastData = localTileContext.lastData;
      }
    }
  }

  static class TileContext
  {
    SunGraphics2D sunG2D;
    PaintContext paintCtxt;
    ColorModel paintModel;
    WeakReference lastRaster;
    WeakReference lastData;
    MaskBlit lastMask;
    Blit lastBlit;
    SurfaceData dstData;

    public TileContext(SunGraphics2D paramSunGraphics2D, PaintContext paramPaintContext)
    {
      this.sunG2D = paramSunGraphics2D;
      this.paintCtxt = paramPaintContext;
      this.paintModel = paramPaintContext.getColorModel();
      this.dstData = paramSunGraphics2D.getSurfaceData();
      synchronized (AlphaPaintPipe.class)
      {
        if ((AlphaPaintPipe.cachedLastColorModel != null) && (AlphaPaintPipe.cachedLastColorModel.get() == this.paintModel))
        {
          this.lastRaster = AlphaPaintPipe.cachedLastRaster;
          this.lastData = AlphaPaintPipe.cachedLastData;
        }
      }
    }
  }
}