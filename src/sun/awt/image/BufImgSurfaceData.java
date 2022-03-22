package sun.awt.image;

import java.awt.GraphicsConfiguration;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import sun.java2d.SunGraphics2D;
import sun.java2d.SurfaceData;
import sun.java2d.loops.CompositeType;
import sun.java2d.loops.RenderLoops;
import sun.java2d.loops.SurfaceType;

public class BufImgSurfaceData extends SurfaceData
{
  BufferedImage bufImg;
  private BufferedImageGraphicsConfig graphicsConfig;
  RenderLoops solidloops;
  private static final int DCM_RGBX_RED_MASK = -16777216;
  private static final int DCM_RGBX_GREEN_MASK = 16711680;
  private static final int DCM_RGBX_BLUE_MASK = 65280;
  private static final int DCM_555X_RED_MASK = 63488;
  private static final int DCM_555X_GREEN_MASK = 1984;
  private static final int DCM_555X_BLUE_MASK = 62;
  private static final int DCM_4444_RED_MASK = 3840;
  private static final int DCM_4444_GREEN_MASK = 240;
  private static final int DCM_4444_BLUE_MASK = 15;
  private static final int DCM_4444_ALPHA_MASK = 61440;
  private static final int DCM_ARGBBM_ALPHA_MASK = 16777216;
  private static final int DCM_ARGBBM_RED_MASK = 16711680;
  private static final int DCM_ARGBBM_GREEN_MASK = 65280;
  private static final int DCM_ARGBBM_BLUE_MASK = 255;
  private static final int CACHE_SIZE = 5;
  private static RenderLoops[] loopcache;
  private static SurfaceType[] typecache;

  private static native void initIDs();

  public static SurfaceData createData(BufferedImage paramBufferedImage)
  {
    Object localObject1;
    Object localObject2;
    if (paramBufferedImage == null)
      throw new NullPointerException("BufferedImage cannot be null");
    ColorModel localColorModel = paramBufferedImage.getColorModel();
    int i = paramBufferedImage.getType();
    switch (i)
    {
    case 4:
      localObject1 = createDataIC(paramBufferedImage, SurfaceType.IntBgr);
      break;
    case 1:
      localObject1 = createDataIC(paramBufferedImage, SurfaceType.IntRgb);
      break;
    case 2:
      localObject1 = createDataIC(paramBufferedImage, SurfaceType.IntArgb);
      break;
    case 3:
      localObject1 = createDataIC(paramBufferedImage, SurfaceType.IntArgbPre);
      break;
    case 5:
      localObject1 = createDataBC(paramBufferedImage, SurfaceType.ThreeByteBgr, 2);
      break;
    case 6:
      localObject1 = createDataBC(paramBufferedImage, SurfaceType.FourByteAbgr, 3);
      break;
    case 7:
      localObject1 = createDataBC(paramBufferedImage, SurfaceType.FourByteAbgrPre, 3);
      break;
    case 8:
      localObject1 = createDataSC(paramBufferedImage, SurfaceType.Ushort565Rgb, null);
      break;
    case 9:
      localObject1 = createDataSC(paramBufferedImage, SurfaceType.Ushort555Rgb, null);
      break;
    case 13:
      switch (localColorModel.getTransparency())
      {
      case 1:
        if (isOpaqueGray((IndexColorModel)localColorModel))
          localObject2 = SurfaceType.Index8Gray;
        else
          localObject2 = SurfaceType.ByteIndexedOpaque;
        break;
      case 2:
        localObject2 = SurfaceType.ByteIndexedBm;
        break;
      case 3:
        localObject2 = SurfaceType.ByteIndexed;
        break;
      default:
        throw new InternalError("Unrecognized transparency");
      }
      localObject1 = createDataBC(paramBufferedImage, (SurfaceType)localObject2, 0);
      break;
    case 10:
      localObject1 = createDataBC(paramBufferedImage, SurfaceType.ByteGray, 0);
      break;
    case 11:
      localObject1 = createDataSC(paramBufferedImage, SurfaceType.UshortGray, null);
      break;
    case 12:
      SampleModel localSampleModel = paramBufferedImage.getRaster().getSampleModel();
      switch (localSampleModel.getSampleSize(0))
      {
      case 1:
        localObject2 = SurfaceType.ByteBinary1Bit;
        break;
      case 2:
        localObject2 = SurfaceType.ByteBinary2Bit;
        break;
      case 4:
        localObject2 = SurfaceType.ByteBinary4Bit;
        break;
      case 3:
      default:
        throw new InternalError("Unrecognized pixel size");
      }
      localObject1 = createDataBP(paramBufferedImage, (SurfaceType)localObject2);
      break;
    case 0:
    default:
      SurfaceType localSurfaceType;
      Object localObject3;
      int l;
      int i1;
      int i2;
      localObject2 = paramBufferedImage.getRaster();
      int j = ((Raster)localObject2).getNumBands();
      if ((localObject2 instanceof IntegerComponentRaster) && (((Raster)localObject2).getNumDataElements() == 1) && (((IntegerComponentRaster)localObject2).getPixelStride() == 1))
      {
        localSurfaceType = SurfaceType.AnyInt;
        if (localColorModel instanceof DirectColorModel)
        {
          localObject3 = (DirectColorModel)localColorModel;
          int k = ((DirectColorModel)localObject3).getAlphaMask();
          l = ((DirectColorModel)localObject3).getRedMask();
          i1 = ((DirectColorModel)localObject3).getGreenMask();
          i2 = ((DirectColorModel)localObject3).getBlueMask();
          if ((j == 3) && (k == 0) && (l == -16777216) && (i1 == 16711680) && (i2 == 65280))
            localSurfaceType = SurfaceType.IntRgbx;
          else if ((j == 4) && (k == 16777216) && (l == 16711680) && (i1 == 65280) && (i2 == 255))
            localSurfaceType = SurfaceType.IntArgbBm;
          else
            localSurfaceType = SurfaceType.AnyDcm;
        }
        localObject1 = createDataIC(paramBufferedImage, localSurfaceType);
      }
      else if ((localObject2 instanceof ShortComponentRaster) && (((Raster)localObject2).getNumDataElements() == 1) && (((ShortComponentRaster)localObject2).getPixelStride() == 1))
      {
        localSurfaceType = SurfaceType.AnyShort;
        localObject3 = null;
        if (localColorModel instanceof DirectColorModel)
        {
          DirectColorModel localDirectColorModel = (DirectColorModel)localColorModel;
          l = localDirectColorModel.getAlphaMask();
          i1 = localDirectColorModel.getRedMask();
          i2 = localDirectColorModel.getGreenMask();
          int i3 = localDirectColorModel.getBlueMask();
          if ((j == 3) && (l == 0) && (i1 == 63488) && (i2 == 1984) && (i3 == 62))
            localSurfaceType = SurfaceType.Ushort555Rgbx;
          else if ((j == 4) && (l == 61440) && (i1 == 3840) && (i2 == 240) && (i3 == 15))
            localSurfaceType = SurfaceType.Ushort4444Argb;
        }
        else if (localColorModel instanceof IndexColorModel)
        {
          localObject3 = (IndexColorModel)localColorModel;
          if (((IndexColorModel)localObject3).getPixelSize() == 12)
            if (isOpaqueGray((IndexColorModel)localObject3))
              localSurfaceType = SurfaceType.Index12Gray;
            else
              localSurfaceType = SurfaceType.UshortIndexed;
          else
            localObject3 = null;
        }
        localObject1 = createDataSC(paramBufferedImage, localSurfaceType, (IndexColorModel)localObject3);
      }
      else
      {
        localObject1 = new BufImgSurfaceData(paramBufferedImage, SurfaceType.Custom);
      }
    }
    ((BufImgSurfaceData)localObject1).initSolidLoops();
    return ((SurfaceData)(SurfaceData)(SurfaceData)localObject1);
  }

  public static SurfaceData createData(Raster paramRaster, ColorModel paramColorModel)
  {
    throw new InternalError("SurfaceData not implemented for Raster/CM");
  }

  public static SurfaceData createDataIC(BufferedImage paramBufferedImage, SurfaceType paramSurfaceType)
  {
    BufImgSurfaceData localBufImgSurfaceData = new BufImgSurfaceData(paramBufferedImage, paramSurfaceType);
    IntegerComponentRaster localIntegerComponentRaster = (IntegerComponentRaster)paramBufferedImage.getRaster();
    localBufImgSurfaceData.initRaster(localIntegerComponentRaster.getDataStorage(), localIntegerComponentRaster.getDataOffset(0) * 4, 0, localIntegerComponentRaster.getWidth(), localIntegerComponentRaster.getHeight(), localIntegerComponentRaster.getPixelStride() * 4, localIntegerComponentRaster.getScanlineStride() * 4, null);
    return localBufImgSurfaceData;
  }

  public static SurfaceData createDataSC(BufferedImage paramBufferedImage, SurfaceType paramSurfaceType, IndexColorModel paramIndexColorModel)
  {
    BufImgSurfaceData localBufImgSurfaceData = new BufImgSurfaceData(paramBufferedImage, paramSurfaceType);
    ShortComponentRaster localShortComponentRaster = (ShortComponentRaster)paramBufferedImage.getRaster();
    localBufImgSurfaceData.initRaster(localShortComponentRaster.getDataStorage(), localShortComponentRaster.getDataOffset(0) * 2, 0, localShortComponentRaster.getWidth(), localShortComponentRaster.getHeight(), localShortComponentRaster.getPixelStride() * 2, localShortComponentRaster.getScanlineStride() * 2, paramIndexColorModel);
    return localBufImgSurfaceData;
  }

  public static SurfaceData createDataBC(BufferedImage paramBufferedImage, SurfaceType paramSurfaceType, int paramInt)
  {
    BufImgSurfaceData localBufImgSurfaceData = new BufImgSurfaceData(paramBufferedImage, paramSurfaceType);
    ByteComponentRaster localByteComponentRaster = (ByteComponentRaster)paramBufferedImage.getRaster();
    ColorModel localColorModel = paramBufferedImage.getColorModel();
    IndexColorModel localIndexColorModel = (localColorModel instanceof IndexColorModel) ? (IndexColorModel)localColorModel : null;
    localBufImgSurfaceData.initRaster(localByteComponentRaster.getDataStorage(), localByteComponentRaster.getDataOffset(paramInt), 0, localByteComponentRaster.getWidth(), localByteComponentRaster.getHeight(), localByteComponentRaster.getPixelStride(), localByteComponentRaster.getScanlineStride(), localIndexColorModel);
    return localBufImgSurfaceData;
  }

  public static SurfaceData createDataBP(BufferedImage paramBufferedImage, SurfaceType paramSurfaceType)
  {
    BufImgSurfaceData localBufImgSurfaceData = new BufImgSurfaceData(paramBufferedImage, paramSurfaceType);
    BytePackedRaster localBytePackedRaster = (BytePackedRaster)paramBufferedImage.getRaster();
    ColorModel localColorModel = paramBufferedImage.getColorModel();
    IndexColorModel localIndexColorModel = (localColorModel instanceof IndexColorModel) ? (IndexColorModel)localColorModel : null;
    localBufImgSurfaceData.initRaster(localBytePackedRaster.getDataStorage(), localBytePackedRaster.getDataBitOffset() / 8, localBytePackedRaster.getDataBitOffset() & 0x7, localBytePackedRaster.getWidth(), localBytePackedRaster.getHeight(), 0, localBytePackedRaster.getScanlineStride(), localIndexColorModel);
    return localBufImgSurfaceData;
  }

  public RenderLoops getRenderLoops(SunGraphics2D paramSunGraphics2D)
  {
    if ((paramSunGraphics2D.paintState <= 1) && (paramSunGraphics2D.compositeState <= 0))
      return this.solidloops;
    return super.getRenderLoops(paramSunGraphics2D);
  }

  public Raster getRaster(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    return this.bufImg.getRaster();
  }

  protected native void initRaster(Object paramObject, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6, IndexColorModel paramIndexColorModel);

  public BufImgSurfaceData(BufferedImage paramBufferedImage, SurfaceType paramSurfaceType)
  {
    super(paramSurfaceType, paramBufferedImage.getColorModel());
    this.bufImg = paramBufferedImage;
  }

  public void initSolidLoops()
  {
    this.solidloops = getSolidLoops(getSurfaceType());
  }

  public static synchronized RenderLoops getSolidLoops(SurfaceType paramSurfaceType)
  {
    for (int i = 4; i >= 0; --i)
    {
      SurfaceType localSurfaceType = typecache[i];
      if (localSurfaceType == paramSurfaceType)
        return loopcache[i];
      if (localSurfaceType == null)
        break;
    }
    RenderLoops localRenderLoops = makeRenderLoops(SurfaceType.OpaqueColor, CompositeType.SrcNoEa, paramSurfaceType);
    System.arraycopy(loopcache, 1, loopcache, 0, 4);
    System.arraycopy(typecache, 1, typecache, 0, 4);
    loopcache[4] = localRenderLoops;
    typecache[4] = paramSurfaceType;
    return localRenderLoops;
  }

  public SurfaceData getReplacement()
  {
    return restoreContents(this.bufImg);
  }

  public synchronized GraphicsConfiguration getDeviceConfiguration()
  {
    if (this.graphicsConfig == null)
      this.graphicsConfig = BufferedImageGraphicsConfig.getConfig(this.bufImg);
    return this.graphicsConfig;
  }

  public Rectangle getBounds()
  {
    return new Rectangle(this.bufImg.getWidth(), this.bufImg.getHeight());
  }

  protected void checkCustomComposite()
  {
  }

  public static native void freeNativeICMData(IndexColorModel paramIndexColorModel);

  public Object getDestination()
  {
    return this.bufImg;
  }

  static
  {
    initIDs();
    loopcache = new RenderLoops[5];
    typecache = new SurfaceType[5];
  }
}