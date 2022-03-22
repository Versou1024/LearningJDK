package sun.font;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.security.AccessController;
import java.security.PrivilegedAction;
import sun.java2d.Disposer;
import sun.java2d.pipe.BufferedContext;
import sun.java2d.pipe.RenderQueue;
import sun.java2d.pipe.hw.AccelGraphicsConfig;
import sun.misc.Unsafe;

public final class StrikeCache
{
  static final Unsafe unsafe = Unsafe.getUnsafe();
  static ReferenceQueue refQueue = Disposer.getQueue();
  static int MINSTRIKES = 8;
  static int recentStrikeIndex = 0;
  static FontStrike[] recentStrikes;
  static boolean cacheRefTypeWeak;
  static int nativeAddressSize;
  static int glyphInfoSize;
  static int xAdvanceOffset;
  static int yAdvanceOffset;
  static int boundsOffset;
  static int widthOffset;
  static int heightOffset;
  static int rowBytesOffset;
  static int topLeftXOffset;
  static int topLeftYOffset;
  static int pixelDataOffset;
  static long invisibleGlyphPtr;

  static native void getGlyphCacheDescription(long[] paramArrayOfLong);

  static void refStrike(FontStrike paramFontStrike)
  {
    int i = recentStrikeIndex;
    recentStrikes[i] = paramFontStrike;
    if (++i == MINSTRIKES)
      i = 0;
    recentStrikeIndex = i;
  }

  private static final void doDispose(FontStrikeDisposer paramFontStrikeDisposer)
  {
    if (paramFontStrikeDisposer.intGlyphImages != null)
    {
      freeIntMemory(paramFontStrikeDisposer.intGlyphImages, paramFontStrikeDisposer.pScalerContext);
    }
    else if (paramFontStrikeDisposer.longGlyphImages != null)
    {
      freeLongMemory(paramFontStrikeDisposer.longGlyphImages, paramFontStrikeDisposer.pScalerContext);
    }
    else
    {
      int i;
      if (paramFontStrikeDisposer.segIntGlyphImages != null)
      {
        for (i = 0; i < paramFontStrikeDisposer.segIntGlyphImages.length; ++i)
          if (paramFontStrikeDisposer.segIntGlyphImages[i] != null)
          {
            freeIntMemory(paramFontStrikeDisposer.segIntGlyphImages[i], paramFontStrikeDisposer.pScalerContext);
            paramFontStrikeDisposer.pScalerContext = 3412040195967549440L;
            paramFontStrikeDisposer.segIntGlyphImages[i] = null;
          }
        if (paramFontStrikeDisposer.pScalerContext != 3412047841009336320L)
          freeIntMemory(new int[0], paramFontStrikeDisposer.pScalerContext);
      }
      else if (paramFontStrikeDisposer.segLongGlyphImages != null)
      {
        for (i = 0; i < paramFontStrikeDisposer.segLongGlyphImages.length; ++i)
          if (paramFontStrikeDisposer.segLongGlyphImages[i] != null)
          {
            freeLongMemory(paramFontStrikeDisposer.segLongGlyphImages[i], paramFontStrikeDisposer.pScalerContext);
            paramFontStrikeDisposer.pScalerContext = 3412040316226633728L;
            paramFontStrikeDisposer.segLongGlyphImages[i] = null;
          }
        if (paramFontStrikeDisposer.pScalerContext != 3412047961268420608L)
          freeLongMemory(new long[0], paramFontStrikeDisposer.pScalerContext);
      }
    }
  }

  static void disposeStrike(FontStrikeDisposer paramFontStrikeDisposer)
  {
    RenderQueue localRenderQueue = null;
    GraphicsEnvironment localGraphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
    if (!(GraphicsEnvironment.isHeadless()))
    {
      GraphicsConfiguration localGraphicsConfiguration = localGraphicsEnvironment.getDefaultScreenDevice().getDefaultConfiguration();
      if (localGraphicsConfiguration instanceof AccelGraphicsConfig)
      {
        AccelGraphicsConfig localAccelGraphicsConfig = (AccelGraphicsConfig)localGraphicsConfiguration;
        BufferedContext localBufferedContext = localAccelGraphicsConfig.getContext();
        if (localBufferedContext != null)
          localRenderQueue = localBufferedContext.getRenderQueue();
      }
    }
    if (localRenderQueue != null)
    {
      localRenderQueue.lock();
      try
      {
        localRenderQueue.flushAndInvokeNow(new Runnable(paramFontStrikeDisposer)
        {
          public void run()
          {
            StrikeCache.access$000(this.val$disposer);
          }
        });
      }
      finally
      {
        localRenderQueue.unlock();
      }
    }
    else
    {
      doDispose(paramFontStrikeDisposer);
    }
  }

  static native void freeIntPointer(int paramInt);

  static native void freeLongPointer(long paramLong);

  private static native void freeIntMemory(int[] paramArrayOfInt, long paramLong);

  private static native void freeLongMemory(long[] paramArrayOfLong, long paramLong);

  public static Reference getStrikeRef(FontStrike paramFontStrike)
  {
    return getStrikeRef(paramFontStrike, cacheRefTypeWeak);
  }

  public static Reference getStrikeRef(FontStrike paramFontStrike, boolean paramBoolean)
  {
    if (paramFontStrike.disposer == null)
    {
      if (paramBoolean)
        return new WeakReference(paramFontStrike);
      return new SoftReference(paramFontStrike);
    }
    if (paramBoolean)
      return new WeakDisposerRef(paramFontStrike);
    return new SoftDisposerRef(paramFontStrike);
  }

  static
  {
    long[] arrayOfLong = new long[11];
    getGlyphCacheDescription(arrayOfLong);
    nativeAddressSize = (int)arrayOfLong[0];
    glyphInfoSize = (int)arrayOfLong[1];
    xAdvanceOffset = (int)arrayOfLong[2];
    yAdvanceOffset = (int)arrayOfLong[3];
    widthOffset = (int)arrayOfLong[4];
    heightOffset = (int)arrayOfLong[5];
    rowBytesOffset = (int)arrayOfLong[6];
    topLeftXOffset = (int)arrayOfLong[7];
    topLeftYOffset = (int)arrayOfLong[8];
    pixelDataOffset = (int)arrayOfLong[9];
    invisibleGlyphPtr = arrayOfLong[10];
    if (nativeAddressSize < 4)
      throw new InternalError("Unexpected address size for font data: " + nativeAddressSize);
    AccessController.doPrivileged(new PrivilegedAction()
    {
      public Object run()
      {
        String str1 = System.getProperty("sun.java2d.font.reftype", "soft");
        StrikeCache.cacheRefTypeWeak = str1.equals("weak");
        String str2 = System.getProperty("sun.java2d.font.minstrikes");
        if (str2 != null)
          try
          {
            StrikeCache.MINSTRIKES = Integer.parseInt(str2);
            if (StrikeCache.MINSTRIKES <= 0)
              StrikeCache.MINSTRIKES = 1;
          }
          catch (NumberFormatException localNumberFormatException)
          {
          }
        StrikeCache.recentStrikes = new FontStrike[StrikeCache.MINSTRIKES];
        return null;
      }
    });
  }

  static abstract interface DisposableStrike
  {
    public abstract FontStrikeDisposer getDisposer();
  }

  static class SoftDisposerRef extends SoftReference
  implements StrikeCache.DisposableStrike
  {
    private FontStrikeDisposer disposer;

    public FontStrikeDisposer getDisposer()
    {
      return this.disposer;
    }

    SoftDisposerRef(FontStrike paramFontStrike)
    {
      super(paramFontStrike, StrikeCache.refQueue);
      this.disposer = paramFontStrike.disposer;
      Disposer.addReference(this, this.disposer);
    }
  }

  static class WeakDisposerRef extends WeakReference
  implements StrikeCache.DisposableStrike
  {
    private FontStrikeDisposer disposer;

    public FontStrikeDisposer getDisposer()
    {
      return this.disposer;
    }

    WeakDisposerRef(FontStrike paramFontStrike)
    {
      super(paramFontStrike, StrikeCache.refQueue);
      this.disposer = paramFontStrike.disposer;
      Disposer.addReference(this, this.disposer);
    }
  }
}