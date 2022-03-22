package sun.java2d.loops;

import java.awt.Rectangle;
import sun.java2d.SurfaceData;
import sun.java2d.pipe.Region;

public final class CustomComponent
{
  public static void register()
  {
    CustomComponent localCustomComponent = CustomComponent.class;
    GraphicsPrimitive[] arrayOfGraphicsPrimitive = { new GraphicsPrimitiveProxy(localCustomComponent, "OpaqueCopyAnyToArgb", Blit.methodSignature, Blit.primTypeID, SurfaceType.Any, CompositeType.SrcNoEa, SurfaceType.IntArgb), new GraphicsPrimitiveProxy(localCustomComponent, "OpaqueCopyArgbToAny", Blit.methodSignature, Blit.primTypeID, SurfaceType.IntArgb, CompositeType.SrcNoEa, SurfaceType.Any), new GraphicsPrimitiveProxy(localCustomComponent, "XorCopyArgbToAny", Blit.methodSignature, Blit.primTypeID, SurfaceType.IntArgb, CompositeType.Xor, SurfaceType.Any) };
    GraphicsPrimitiveMgr.register(arrayOfGraphicsPrimitive);
  }

  public static Region getRegionOfInterest(SurfaceData paramSurfaceData1, SurfaceData paramSurfaceData2, Region paramRegion, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6)
  {
    Region localRegion = Region.getInstanceXYWH(paramInt3, paramInt4, paramInt5, paramInt6);
    localRegion = localRegion.getIntersection(paramSurfaceData2.getBounds());
    Rectangle localRectangle = paramSurfaceData1.getBounds();
    localRectangle.translate(paramInt3 - paramInt1, paramInt4 - paramInt2);
    localRegion = localRegion.getIntersection(localRectangle);
    if (paramRegion != null)
      localRegion = localRegion.getIntersection(paramRegion);
    return localRegion;
  }
}