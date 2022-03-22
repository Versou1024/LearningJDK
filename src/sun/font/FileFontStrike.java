package sun.font;

import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D.Float;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Float;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import sun.misc.Unsafe;

public class FileFontStrike extends PhysicalStrike
{
  static final int INVISIBLE_GLYPHS = 65534;
  private FileFont fileFont;
  private static final int UNINITIALISED = 0;
  private static final int INTARRAY = 1;
  private static final int LONGARRAY = 2;
  private static final int SEGINTARRAY = 3;
  private static final int SEGLONGARRAY = 4;
  private int glyphCacheFormat = 0;
  private static final int SEGSHIFT = 8;
  private static final int SEGSIZE = 256;
  private boolean segmentedCache;
  private int[][] segIntGlyphImages;
  private long[][] segLongGlyphImages;
  private float[] horizontalAdvances;
  private float[][] segHorizontalAdvances;
  ConcurrentHashMap boundsMap;
  SoftReference glyphMetricsMapRef;
  AffineTransform invertDevTx;
  boolean useNatives;
  NativeStrike[] nativeStrikes;
  private int intPtSize;
  private static boolean isXPorLater = false;
  private static final int SLOTZEROMAX = 16777215;

  private static native boolean initNative();

  FileFontStrike(FileFont paramFileFont, FontStrikeDesc paramFontStrikeDesc)
  {
    super(paramFileFont, paramFontStrikeDesc);
    this.fileFont = paramFileFont;
    if (paramFontStrikeDesc.style != paramFileFont.style)
    {
      if (((paramFontStrikeDesc.style & 0x2) == 2) && ((paramFileFont.style & 0x2) == 0))
      {
        this.algoStyle = true;
        this.italic = 0.69999998807907104F;
      }
      if (((paramFontStrikeDesc.style & 0x1) == 1) && ((paramFileFont.style & 0x1) == 0))
      {
        this.algoStyle = true;
        this.boldness = 1.3300000429153442F;
      }
    }
    double[] arrayOfDouble = new double[4];
    AffineTransform localAffineTransform = paramFontStrikeDesc.glyphTx;
    localAffineTransform.getMatrix(arrayOfDouble);
    if ((!(paramFontStrikeDesc.devTx.isIdentity())) && (paramFontStrikeDesc.devTx.getType() != 1))
      try
      {
        this.invertDevTx = paramFontStrikeDesc.devTx.createInverse();
      }
      catch (NoninvertibleTransformException localNoninvertibleTransformException)
      {
      }
    if ((Double.isNaN(arrayOfDouble[0])) || (Double.isNaN(arrayOfDouble[1])) || (Double.isNaN(arrayOfDouble[2])) || (Double.isNaN(arrayOfDouble[3])))
      this.pScalerContext = getNullScalerContext(FileFont.getNullScaler());
    else
      this.pScalerContext = createScalerContext(paramFileFont.getScaler(), arrayOfDouble, paramFileFont instanceof TrueTypeFont, paramFontStrikeDesc.aaHint, paramFontStrikeDesc.fmHint, this.algoStyle, this.boldness, this.italic);
    this.mapper = paramFileFont.getMapper();
    int i = this.mapper.getNumGlyphs();
    float f = (float)arrayOfDouble[3];
    int j = this.intPtSize = (int)f;
    int k = ((localAffineTransform.getType() & 0x7C) == 0) ? 1 : 0;
    this.segmentedCache = ((i > 2048) || ((i > 512) && (((k == 0) || (f != j) || (j < 6) || (j > 36)))));
    if (this.pScalerContext == 3412046810217185280L)
    {
      this.disposer = new FontStrikeDisposer(paramFileFont, paramFontStrikeDesc);
      initGlyphCache();
      this.pScalerContext = getNullScalerContext(FileFont.getNullScaler());
      FontManager.deRegisterBadFont(paramFileFont);
      return;
    }
    if ((FontManager.isWindows) && (isXPorLater) && (!(FontManager.useT2K)) && (!(GraphicsEnvironment.isHeadless())) && (!(paramFileFont.useJavaRasterizer)) && (((paramFontStrikeDesc.aaHint == 4) || (paramFontStrikeDesc.aaHint == 5))) && (arrayOfDouble[1] == 0D) && (arrayOfDouble[2] == 0D) && (arrayOfDouble[0] == arrayOfDouble[3]) && (arrayOfDouble[0] >= 3.0D) && (arrayOfDouble[0] <= 100.0D) && (!(((TrueTypeFont)paramFileFont).useEmbeddedBitmapsForSize(this.intPtSize))))
    {
      this.useNatives = true;
    }
    else if ((paramFileFont.checkUseNatives()) && (paramFontStrikeDesc.aaHint == 0) && (!(this.algoStyle)) && (arrayOfDouble[1] == 0D) && (arrayOfDouble[2] == 0D) && (arrayOfDouble[0] >= 6.0D) && (arrayOfDouble[0] <= 36.0D) && (arrayOfDouble[0] == arrayOfDouble[3]))
    {
      this.useNatives = true;
      int l = paramFileFont.nativeFonts.length;
      this.nativeStrikes = new NativeStrike[l];
      for (int i1 = 0; i1 < l; ++i1)
        this.nativeStrikes[i1] = new NativeStrike(paramFileFont.nativeFonts[i1], paramFontStrikeDesc, false);
    }
    if ((FontManager.logging) && (FontManager.isWindows))
      FontManager.logger.info("Strike for " + paramFileFont + " at size = " + this.intPtSize + " use natives = " + this.useNatives + " useJavaRasteriser = " + paramFileFont.useJavaRasterizer + " AAHint = " + paramFontStrikeDesc.aaHint + " Has Embedded bitmaps = " + ((TrueTypeFont)paramFileFont).useEmbeddedBitmapsForSize(this.intPtSize));
    this.disposer = new FontStrikeDisposer(paramFileFont, paramFontStrikeDesc, this.pScalerContext);
    double d = 48.0D;
    this.getImageWithAdvance = ((Math.abs(localAffineTransform.getScaleX()) <= d) && (Math.abs(localAffineTransform.getScaleY()) <= d) && (Math.abs(localAffineTransform.getShearX()) <= d) && (Math.abs(localAffineTransform.getShearY()) <= d));
    if (!(this.getImageWithAdvance))
    {
      int i2;
      if (!(this.segmentedCache))
      {
        this.horizontalAdvances = new float[i];
        for (i2 = 0; i2 < i; ++i2)
          this.horizontalAdvances[i2] = 3.4028235e+38F;
      }
      else
      {
        i2 = (i + 256 - 1) / 256;
        this.segHorizontalAdvances = new float[i2][];
      }
    }
  }

  static synchronized native long getNullScalerContext(long paramLong);

  private native long createScalerContext(long paramLong, double[] paramArrayOfDouble, boolean paramBoolean1, int paramInt1, int paramInt2, boolean paramBoolean2, float paramFloat1, float paramFloat2);

  public int getNumGlyphs()
  {
    return this.fileFont.getNumGlyphs();
  }

  long getGlyphImageFromNative(int paramInt)
  {
    if (FontManager.isWindows)
      return getGlyphImageFromWindows(paramInt);
    return getGlyphImageFromX11(paramInt);
  }

  private native long _getGlyphImageFromWindows(String paramString, int paramInt1, int paramInt2, int paramInt3, boolean paramBoolean);

  long getGlyphImageFromWindows(int paramInt)
  {
    String str = this.fileFont.getFamilyName(null);
    int i = this.desc.style & 0x1 | this.desc.style & 0x2 | this.fileFont.getStyle();
    int j = this.intPtSize;
    long l = _getGlyphImageFromWindows(str, i, j, paramInt, this.desc.fmHint == 2);
    if (l != 3412046810217185280L)
    {
      float f = getGlyphAdvance(paramInt, false);
      StrikeCache.unsafe.putFloat(l + StrikeCache.xAdvanceOffset, f);
      return l;
    }
    return this.fileFont.getGlyphImage(this.pScalerContext, paramInt);
  }

  long getGlyphImageFromX11(int paramInt)
  {
    char c = this.fileFont.glyphToCharMap[paramInt];
    for (int i = 0; i < this.nativeStrikes.length; ++i)
    {
      CharToGlyphMapper localCharToGlyphMapper = this.fileFont.nativeFonts[i].getMapper();
      int j = localCharToGlyphMapper.charToGlyph(c) & 0xFFFF;
      if (j != localCharToGlyphMapper.getMissingGlyphCode())
      {
        long l = this.nativeStrikes[i].getGlyphImagePtrNoCache(j);
        if (l != 3412047669210644480L)
          return l;
      }
    }
    return this.fileFont.getGlyphImage(this.pScalerContext, paramInt);
  }

  long getGlyphImagePtr(int paramInt)
  {
    if (paramInt >= 65534)
      return StrikeCache.invisibleGlyphPtr;
    long l = 3412047291253522432L;
    if ((l = getCachedGlyphPtr(paramInt)) != 3412046810217185280L)
      return l;
    if (this.useNatives)
    {
      l = getGlyphImageFromNative(paramInt);
      if ((l == 3412047463052214272L) && (FontManager.logging))
        FontManager.logger.info("Strike for " + this.fileFont + " at size = " + this.intPtSize + " couldn't get native glyph for code = " + paramInt);
    }
    if (l == 3412046810217185280L)
      l = this.fileFont.getGlyphImage(this.pScalerContext, paramInt);
    return setCachedGlyphPtr(paramInt, l);
  }

  void getGlyphImagePtrs(int[] paramArrayOfInt, long[] paramArrayOfLong, int paramInt)
  {
    for (int i = 0; i < paramInt; ++i)
    {
      int j = paramArrayOfInt[i];
      if (j >= 65534)
      {
        paramArrayOfLong[i] = StrikeCache.invisibleGlyphPtr;
      }
      else
      {
        if ((paramArrayOfLong[i] = getCachedGlyphPtr(j)) != 3412047823829467136L)
          break label103:
        long l = 3412048304865804288L;
        if (this.useNatives)
          l = getGlyphImageFromNative(j);
        if (l == 3412047823829467136L)
          l = this.fileFont.getGlyphImage(this.pScalerContext, j);
        label103: paramArrayOfLong[i] = setCachedGlyphPtr(j, l);
      }
    }
  }

  int getSlot0GlyphImagePtrs(int[] paramArrayOfInt, long[] paramArrayOfLong, int paramInt)
  {
    int i = 0;
    for (int j = 0; j < paramInt; ++j)
    {
      int k = paramArrayOfInt[j];
      if (k >= 16777215)
        return i;
      ++i;
      if (k >= 65534)
      {
        paramArrayOfLong[j] = StrikeCache.invisibleGlyphPtr;
      }
      else
      {
        if ((paramArrayOfLong[j] = getCachedGlyphPtr(k)) != 3412047823829467136L)
          break label119:
        long l = 3412048304865804288L;
        if (this.useNatives)
          l = getGlyphImageFromNative(k);
        if (l == 3412047823829467136L)
          l = this.fileFont.getGlyphImage(this.pScalerContext, k);
        label119: paramArrayOfLong[j] = setCachedGlyphPtr(k, l);
      }
    }
    return i;
  }

  long getCachedGlyphPtr(int paramInt)
  {
    int i;
    int j;
    switch (this.glyphCacheFormat)
    {
    case 1:
      return (this.intGlyphImages[paramInt] & 0xFFFFFFFF);
    case 3:
      i = paramInt >> 8;
      if (this.segIntGlyphImages[i] != null)
      {
        j = paramInt % 256;
        return (this.segIntGlyphImages[i][j] & 0xFFFFFFFF);
      }
      return 3412047652030775296L;
    case 2:
      return this.longGlyphImages[paramInt];
    case 4:
      i = paramInt >> 8;
      if (this.segLongGlyphImages[i] != null)
      {
        j = paramInt % 256;
        return this.segLongGlyphImages[i][j];
      }
      return 3412047652030775296L;
    }
    return 3412046964836007936L;
  }

  private synchronized long setCachedGlyphPtr(int paramInt, long paramLong)
  {
    int i;
    int j;
    switch (this.glyphCacheFormat)
    {
    case 1:
      if (this.intGlyphImages[paramInt] == 0)
      {
        this.intGlyphImages[paramInt] = (int)paramLong;
        return paramLong;
      }
      StrikeCache.freeIntPointer((int)paramLong);
      return (this.intGlyphImages[paramInt] & 0xFFFFFFFF);
    case 3:
      i = paramInt >> 8;
      j = paramInt % 256;
      if (this.segIntGlyphImages[i] == null)
        this.segIntGlyphImages[i] = new int[256];
      if (this.segIntGlyphImages[i][j] == 0)
      {
        this.segIntGlyphImages[i][j] = (int)paramLong;
        return paramLong;
      }
      StrikeCache.freeIntPointer((int)paramLong);
      return (this.segIntGlyphImages[i][j] & 0xFFFFFFFF);
    case 2:
      if (this.longGlyphImages[paramInt] == 3412047497411952640L)
      {
        this.longGlyphImages[paramInt] = paramLong;
        return paramLong;
      }
      StrikeCache.freeLongPointer(paramLong);
      return this.longGlyphImages[paramInt];
    case 4:
      i = paramInt >> 8;
      j = paramInt % 256;
      if (this.segLongGlyphImages[i] == null)
        this.segLongGlyphImages[i] = new long[256];
      if (this.segLongGlyphImages[i][j] == 3412047497411952640L)
      {
        this.segLongGlyphImages[i][j] = paramLong;
        return paramLong;
      }
      StrikeCache.freeLongPointer(paramLong);
      return this.segLongGlyphImages[i][j];
    }
    initGlyphCache();
    return setCachedGlyphPtr(paramInt, paramLong);
  }

  private void initGlyphCache()
  {
    int i = this.mapper.getNumGlyphs();
    if (this.segmentedCache)
    {
      int j = (i + 256 - 1) / 256;
      if (FontManager.longAddresses)
      {
        this.glyphCacheFormat = 4;
        this.segLongGlyphImages = new long[j][];
        this.disposer.segLongGlyphImages = this.segLongGlyphImages;
      }
      else
      {
        this.glyphCacheFormat = 3;
        this.segIntGlyphImages = new int[j][];
        this.disposer.segIntGlyphImages = this.segIntGlyphImages;
      }
    }
    else if (FontManager.longAddresses)
    {
      this.glyphCacheFormat = 2;
      this.longGlyphImages = new long[i];
      this.disposer.longGlyphImages = this.longGlyphImages;
    }
    else
    {
      this.glyphCacheFormat = 1;
      this.intGlyphImages = new int[i];
      this.disposer.intGlyphImages = this.intGlyphImages;
    }
  }

  float getGlyphAdvance(int paramInt)
  {
    return getGlyphAdvance(paramInt, true);
  }

  private float getGlyphAdvance(int paramInt, boolean paramBoolean)
  {
    float f;
    if (paramInt >= 65534)
      return 0F;
    if (this.horizontalAdvances != null)
    {
      f = this.horizontalAdvances[paramInt];
      if (f == 3.4028235e+38F)
        break label83;
      return f;
    }
    if ((this.segmentedCache) && (this.segHorizontalAdvances != null))
    {
      int i = paramInt >> 8;
      float[] arrayOfFloat = this.segHorizontalAdvances[i];
      if (arrayOfFloat != null)
      {
        f = arrayOfFloat[(paramInt % 256)];
        if (f != 3.4028235e+38F)
          return f;
      }
    }
    if ((this.invertDevTx != null) || (!(paramBoolean)))
    {
      label83: f = getGlyphMetrics(paramInt, paramBoolean).x;
    }
    else
    {
      long l;
      if (this.getImageWithAdvance)
        l = getGlyphImagePtr(paramInt);
      else
        l = getCachedGlyphPtr(paramInt);
      if (l != 3412047480232083456L)
        f = StrikeCache.unsafe.getFloat(l + StrikeCache.xAdvanceOffset);
      else
        f = this.fileFont.getGlyphAdvance(this.pScalerContext, paramInt);
    }
    if (this.horizontalAdvances != null)
    {
      this.horizontalAdvances[paramInt] = f;
    }
    else if ((this.segmentedCache) && (this.segHorizontalAdvances != null))
    {
      int j = paramInt >> 8;
      int k = paramInt % 256;
      if (this.segHorizontalAdvances[j] == null)
      {
        this.segHorizontalAdvances[j] = new float[256];
        for (int i1 = 0; i1 < 256; ++i1)
          this.segHorizontalAdvances[j][i1] = 3.4028235e+38F;
      }
      this.segHorizontalAdvances[j][k] = f;
    }
    return f;
  }

  float getCodePointAdvance(int paramInt)
  {
    return getGlyphAdvance(this.mapper.charToGlyph(paramInt));
  }

  void getGlyphImageBounds(int paramInt, Point2D.Float paramFloat, Rectangle paramRectangle)
  {
    long l = getGlyphImagePtr(paramInt);
    float f1 = StrikeCache.unsafe.getFloat(l + StrikeCache.topLeftXOffset);
    float f2 = StrikeCache.unsafe.getFloat(l + StrikeCache.topLeftYOffset);
    paramRectangle.x = (int)Math.floor(paramFloat.x + f1);
    paramRectangle.y = (int)Math.floor(paramFloat.y + f2);
    paramRectangle.width = (StrikeCache.unsafe.getShort(l + StrikeCache.widthOffset) & 0xFFFF);
    paramRectangle.height = (StrikeCache.unsafe.getShort(l + StrikeCache.heightOffset) & 0xFFFF);
    if ((((this.desc.aaHint == 4) || (this.desc.aaHint == 5))) && (f1 <= -2.0F))
    {
      int i = getGlyphImageMinX(l, paramRectangle.x);
      if (i > paramRectangle.x)
      {
        paramRectangle.x += 1;
        paramRectangle.width -= 1;
      }
    }
  }

  private int getGlyphImageMinX(long paramLong, int paramInt)
  {
    long l;
    int i = StrikeCache.unsafe.getChar(paramLong + StrikeCache.widthOffset);
    int j = StrikeCache.unsafe.getChar(paramLong + StrikeCache.heightOffset);
    int k = StrikeCache.unsafe.getChar(paramLong + StrikeCache.rowBytesOffset);
    if (k == i)
      return paramInt;
    if (StrikeCache.nativeAddressSize == 4)
      l = 0xFFFFFFFF & StrikeCache.unsafe.getInt(paramLong + StrikeCache.pixelDataOffset);
    else
      l = StrikeCache.unsafe.getLong(paramLong + StrikeCache.pixelDataOffset);
    if (l == 3412046810217185280L)
      return paramInt;
    for (int i1 = 0; i1 < j; ++i1)
      for (int i2 = 0; i2 < 3; ++i2)
        if (StrikeCache.unsafe.getByte(l + i1 * k + i2) != 0)
          return paramInt;
    return (paramInt + 1);
  }

  StrikeMetrics getFontMetrics()
  {
    if (this.strikeMetrics == null)
    {
      this.strikeMetrics = this.fileFont.getFontMetrics(this.pScalerContext);
      if (this.invertDevTx != null)
        this.strikeMetrics.convertToUserSpace(this.invertDevTx);
    }
    return this.strikeMetrics;
  }

  Point2D.Float getGlyphMetrics(int paramInt)
  {
    return getGlyphMetrics(paramInt, true);
  }

  private Point2D.Float getGlyphMetrics(int paramInt, boolean paramBoolean)
  {
    long l;
    Point2D.Float localFloat1 = new Point2D.Float();
    if (paramInt >= 65534)
      return localFloat1;
    if ((this.getImageWithAdvance) && (paramBoolean))
      l = getGlyphImagePtr(paramInt);
    else
      l = getCachedGlyphPtr(paramInt);
    if (l != 3412046827397054464L)
    {
      localFloat1 = new Point2D.Float();
      localFloat1.x = StrikeCache.unsafe.getFloat(l + StrikeCache.xAdvanceOffset);
      localFloat1.y = StrikeCache.unsafe.getFloat(l + StrikeCache.yAdvanceOffset);
      label202: if ((this.invertDevTx != null) && (paramBoolean))
        this.invertDevTx.deltaTransform(localFloat1, localFloat1);
    }
    else
    {
      Integer localInteger = new Integer(paramInt);
      Point2D.Float localFloat2 = null;
      HashMap localHashMap = null;
      if (this.glyphMetricsMapRef != null)
        localHashMap = (HashMap)this.glyphMetricsMapRef.get();
      if (localHashMap != null)
        synchronized (this)
        {
          localFloat2 = (Point2D.Float)localHashMap.get(localInteger);
          if (localFloat2 == null)
            break label202;
          localFloat1.x = localFloat2.x;
          localFloat1.y = localFloat2.y;
          return localFloat1;
        }
      if (localFloat2 == null)
      {
        this.fileFont.getGlyphMetrics(this.pScalerContext, paramInt, localFloat1);
        if ((this.invertDevTx != null) && (paramBoolean))
          this.invertDevTx.deltaTransform(localFloat1, localFloat1);
        localFloat2 = new Point2D.Float(localFloat1.x, localFloat1.y);
        synchronized (this)
        {
          if (localHashMap == null)
          {
            localHashMap = new HashMap();
            this.glyphMetricsMapRef = new SoftReference(localHashMap);
          }
          localHashMap.put(localInteger, localFloat2);
        }
      }
    }
    return localFloat1;
  }

  Point2D.Float getCharMetrics(char paramChar)
  {
    return getGlyphMetrics(this.mapper.charToGlyph(paramChar));
  }

  Rectangle2D.Float getGlyphOutlineBounds(int paramInt)
  {
    if (this.boundsMap == null)
      this.boundsMap = new ConcurrentHashMap();
    Integer localInteger = new Integer(paramInt);
    Rectangle2D.Float localFloat = (Rectangle2D.Float)this.boundsMap.get(localInteger);
    if (localFloat == null)
    {
      localFloat = this.fileFont.getGlyphOutlineBounds(this.pScalerContext, paramInt);
      this.boundsMap.put(localInteger, localFloat);
    }
    return localFloat;
  }

  public Rectangle2D getOutlineBounds(int paramInt)
  {
    return this.fileFont.getGlyphOutlineBounds(this.pScalerContext, paramInt);
  }

  GeneralPath getGlyphOutline(int paramInt, float paramFloat1, float paramFloat2)
  {
    return this.fileFont.getGlyphOutline(this.pScalerContext, paramInt, paramFloat1, paramFloat2);
  }

  GeneralPath getGlyphVectorOutline(int[] paramArrayOfInt, float paramFloat1, float paramFloat2)
  {
    return this.fileFont.getGlyphVectorOutline(this.pScalerContext, paramArrayOfInt, paramArrayOfInt.length, paramFloat1, paramFloat2);
  }

  protected void adjustPoint(Point2D.Float paramFloat)
  {
    if (this.invertDevTx != null)
      this.invertDevTx.deltaTransform(paramFloat, paramFloat);
  }

  static
  {
    if ((FontManager.isWindows) && (!(FontManager.useT2K)) && (!(GraphicsEnvironment.isHeadless())))
      isXPorLater = initNative();
  }
}