package sun.java2d.loops;

import java.awt.Composite;
import java.awt.CompositeContext;
import java.awt.RenderingHints;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.PrintStream;
import java.lang.ref.WeakReference;
import sun.java2d.SurfaceData;
import sun.java2d.pipe.Region;
import sun.java2d.pipe.SpanIterator;

public class Blit extends GraphicsPrimitive
{
  public static final String methodSignature = "Blit(...)".toString();
  public static final int primTypeID = makePrimTypeID();
  private static RenderCache blitcache = new RenderCache(20);

  public static Blit locate(SurfaceType paramSurfaceType1, CompositeType paramCompositeType, SurfaceType paramSurfaceType2)
  {
    return ((Blit)GraphicsPrimitiveMgr.locate(primTypeID, paramSurfaceType1, paramCompositeType, paramSurfaceType2));
  }

  public static Blit getFromCache(SurfaceType paramSurfaceType1, CompositeType paramCompositeType, SurfaceType paramSurfaceType2)
  {
    Object localObject = blitcache.get(paramSurfaceType1, paramCompositeType, paramSurfaceType2);
    if (localObject != null)
      return ((Blit)localObject);
    Blit localBlit = locate(paramSurfaceType1, paramCompositeType, paramSurfaceType2);
    if (localBlit == null)
    {
      System.out.println("blit loop not found for:");
      System.out.println("src:  " + paramSurfaceType1);
      System.out.println("comp: " + paramCompositeType);
      System.out.println("dst:  " + paramSurfaceType2);
    }
    else
    {
      blitcache.put(paramSurfaceType1, paramCompositeType, paramSurfaceType2, localBlit);
    }
    return localBlit;
  }

  protected Blit(SurfaceType paramSurfaceType1, CompositeType paramCompositeType, SurfaceType paramSurfaceType2)
  {
    super(methodSignature, primTypeID, paramSurfaceType1, paramCompositeType, paramSurfaceType2);
  }

  public Blit(long paramLong, SurfaceType paramSurfaceType1, CompositeType paramCompositeType, SurfaceType paramSurfaceType2)
  {
    super(paramLong, methodSignature, primTypeID, paramSurfaceType1, paramCompositeType, paramSurfaceType2);
  }

  public native void Blit(SurfaceData paramSurfaceData1, SurfaceData paramSurfaceData2, Composite paramComposite, Region paramRegion, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6);

  public GraphicsPrimitive makePrimitive(SurfaceType paramSurfaceType1, CompositeType paramCompositeType, SurfaceType paramSurfaceType2)
  {
    if (paramCompositeType.isDerivedFrom(CompositeType.Xor))
    {
      GeneralXorBlit localGeneralXorBlit = new GeneralXorBlit(paramSurfaceType1, paramCompositeType, paramSurfaceType2);
      setupGeneralBinaryOp(localGeneralXorBlit);
      return localGeneralXorBlit;
    }
    if (paramCompositeType.isDerivedFrom(CompositeType.AnyAlpha))
      return new GeneralMaskBlit(paramSurfaceType1, paramCompositeType, paramSurfaceType2);
    return AnyBlit.instance;
  }

  public GraphicsPrimitive traceWrap()
  {
    return new TraceBlit(this);
  }

  static
  {
    GraphicsPrimitiveMgr.registerGeneral(new Blit(null, null, null));
  }

  private static class AnyBlit extends Blit
  {
    public static AnyBlit instance = new AnyBlit();

    public AnyBlit()
    {
      super(SurfaceType.Any, CompositeType.Any, SurfaceType.Any);
    }

    public void Blit(SurfaceData paramSurfaceData1, SurfaceData paramSurfaceData2, Composite paramComposite, Region paramRegion, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6)
    {
      ColorModel localColorModel1 = paramSurfaceData1.getColorModel();
      ColorModel localColorModel2 = paramSurfaceData2.getColorModel();
      CompositeContext localCompositeContext = paramComposite.createContext(localColorModel1, localColorModel2, new RenderingHints(null));
      Raster localRaster = paramSurfaceData1.getRaster(paramInt1, paramInt2, paramInt5, paramInt6);
      WritableRaster localWritableRaster = (WritableRaster)paramSurfaceData2.getRaster(paramInt3, paramInt4, paramInt5, paramInt6);
      if (paramRegion == null)
        paramRegion = Region.getInstanceXYWH(paramInt3, paramInt4, paramInt5, paramInt6);
      int[] arrayOfInt = { paramInt3, paramInt4, paramInt3 + paramInt5, paramInt4 + paramInt6 };
      SpanIterator localSpanIterator = paramRegion.getSpanIterator(arrayOfInt);
      paramInt1 -= paramInt3;
      paramInt2 -= paramInt4;
      while (localSpanIterator.nextSpan(arrayOfInt))
      {
        int i = arrayOfInt[2] - arrayOfInt[0];
        int j = arrayOfInt[3] - arrayOfInt[1];
        localRaster = localRaster.createChild(paramInt1 + arrayOfInt[0], paramInt2 + arrayOfInt[1], i, j, 0, 0, null);
        localWritableRaster = localWritableRaster.createWritableChild(arrayOfInt[0], arrayOfInt[1], i, j, 0, 0, null);
        localCompositeContext.compose(localRaster, localWritableRaster, localWritableRaster);
      }
      localCompositeContext.dispose();
    }
  }

  private static class GeneralMaskBlit extends Blit
  {
    MaskBlit performop;

    public GeneralMaskBlit(SurfaceType paramSurfaceType1, CompositeType paramCompositeType, SurfaceType paramSurfaceType2)
    {
      super(paramSurfaceType1, paramCompositeType, paramSurfaceType2);
      this.performop = MaskBlit.locate(paramSurfaceType1, paramCompositeType, paramSurfaceType2);
    }

    public void Blit(SurfaceData paramSurfaceData1, SurfaceData paramSurfaceData2, Composite paramComposite, Region paramRegion, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6)
    {
      this.performop.MaskBlit(paramSurfaceData1, paramSurfaceData2, paramComposite, paramRegion, paramInt1, paramInt2, paramInt3, paramInt4, paramInt5, paramInt6, null, 0, 0);
    }
  }

  private static class GeneralXorBlit extends Blit
  implements GraphicsPrimitive.GeneralBinaryOp
  {
    Blit convertsrc;
    Blit convertdst;
    Blit performop;
    Blit convertresult;
    WeakReference srcTmp;
    WeakReference dstTmp;

    public GeneralXorBlit(SurfaceType paramSurfaceType1, CompositeType paramCompositeType, SurfaceType paramSurfaceType2)
    {
      super(paramSurfaceType1, paramCompositeType, paramSurfaceType2);
    }

    public void setPrimitives(Blit paramBlit1, Blit paramBlit2, GraphicsPrimitive paramGraphicsPrimitive, Blit paramBlit3)
    {
      this.convertsrc = paramBlit1;
      this.convertdst = paramBlit2;
      this.performop = ((Blit)paramGraphicsPrimitive);
      this.convertresult = paramBlit3;
    }

    public synchronized void Blit(SurfaceData paramSurfaceData1, SurfaceData paramSurfaceData2, Composite paramComposite, Region paramRegion, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6)
    {
      SurfaceData localSurfaceData1;
      SurfaceData localSurfaceData2;
      Region localRegion;
      int i;
      int j;
      int k;
      int l;
      SurfaceData localSurfaceData3;
      if (this.convertsrc == null)
      {
        localSurfaceData1 = paramSurfaceData1;
        i = paramInt1;
        j = paramInt2;
      }
      else
      {
        localSurfaceData3 = null;
        if (this.srcTmp != null)
          localSurfaceData3 = (SurfaceData)this.srcTmp.get();
        localSurfaceData1 = convertFrom(this.convertsrc, paramSurfaceData1, paramInt1, paramInt2, paramInt5, paramInt6, localSurfaceData3);
        i = 0;
        j = 0;
        if (localSurfaceData1 != localSurfaceData3)
          this.srcTmp = new WeakReference(localSurfaceData1);
      }
      if (this.convertdst == null)
      {
        localSurfaceData2 = paramSurfaceData2;
        k = paramInt3;
        l = paramInt4;
        localRegion = paramRegion;
      }
      else
      {
        localSurfaceData3 = null;
        if (this.dstTmp != null)
          localSurfaceData3 = (SurfaceData)this.dstTmp.get();
        localSurfaceData2 = convertFrom(this.convertdst, paramSurfaceData2, paramInt3, paramInt4, paramInt5, paramInt6, localSurfaceData3);
        k = 0;
        l = 0;
        localRegion = null;
        if (localSurfaceData2 != localSurfaceData3)
          this.dstTmp = new WeakReference(localSurfaceData2);
      }
      this.performop.Blit(localSurfaceData1, localSurfaceData2, paramComposite, localRegion, i, j, k, l, paramInt5, paramInt6);
      if (this.convertresult != null)
        convertTo(this.convertresult, localSurfaceData2, paramSurfaceData2, paramRegion, paramInt3, paramInt4, paramInt5, paramInt6);
    }
  }

  private static class TraceBlit extends Blit
  {
    Blit target;

    public TraceBlit(Blit paramBlit)
    {
      super(paramBlit.getSourceType(), paramBlit.getCompositeType(), paramBlit.getDestType());
      this.target = paramBlit;
    }

    public GraphicsPrimitive traceWrap()
    {
      return this;
    }

    public void Blit(SurfaceData paramSurfaceData1, SurfaceData paramSurfaceData2, Composite paramComposite, Region paramRegion, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6)
    {
      tracePrimitive(this.target);
      this.target.Blit(paramSurfaceData1, paramSurfaceData2, paramComposite, paramRegion, paramInt1, paramInt2, paramInt3, paramInt4, paramInt5, paramInt6);
    }
  }
}