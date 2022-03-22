package sun.font;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import sun.java2d.Disposer;
import sun.java2d.DisposerRecord;

public final class FontDesignMetrics extends FontMetrics
{
  static final long serialVersionUID = 4480069578560887773L;
  private static final float UNKNOWN_WIDTH = -1.0F;
  private static final int CURRENT_VERSION = 1;
  private static float roundingUpValue = 0.94999998807907104F;
  private Font font;
  private float ascent;
  private float descent;
  private float leading;
  private float maxAdvance;
  private double[] matrix;
  private int[] cache;
  private int serVersion;
  private boolean isAntiAliased;
  private boolean usesFractionalMetrics;
  private AffineTransform frcTx;
  private transient float[] advCache;
  private transient int height;
  private transient FontRenderContext frc;
  private transient double[] devmatrix;
  private transient FontStrike fontStrike;
  private static FontRenderContext DEFAULT_FRC = null;
  private static final HashMap<Object, KeyReference> metricsCache = new HashMap();
  private static final int MAXRECENT = 5;
  private static final FontDesignMetrics[] recentMetrics = new FontDesignMetrics[5];
  private static int recentIndex = 0;

  private static FontRenderContext getDefaultFrc()
  {
    if (DEFAULT_FRC == null)
    {
      AffineTransform localAffineTransform;
      if (GraphicsEnvironment.isHeadless())
        localAffineTransform = new AffineTransform();
      else
        localAffineTransform = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().getDefaultTransform();
      DEFAULT_FRC = new FontRenderContext(localAffineTransform, false, false);
    }
    return DEFAULT_FRC;
  }

  public static FontDesignMetrics getMetrics(Font paramFont)
  {
    return getMetrics(paramFont, getDefaultFrc());
  }

  public static FontDesignMetrics getMetrics(Font paramFont, FontRenderContext paramFontRenderContext)
  {
    KeyReference localKeyReference;
    if ((FontManager.maybeUsingAlternateCompositeFonts()) && (FontManager.getFont2D(paramFont) instanceof CompositeFont))
      return new FontDesignMetrics(paramFont, paramFontRenderContext);
    FontDesignMetrics localFontDesignMetrics = null;
    boolean bool = paramFontRenderContext.equals(getDefaultFrc());
    if (bool)
      synchronized (metricsCache)
      {
        localKeyReference = (KeyReference)metricsCache.get(paramFont);
      }
    else
      synchronized (metricsCache)
      {
        MetricsKey.key.init(paramFont, paramFontRenderContext);
        localKeyReference = (KeyReference)metricsCache.get(MetricsKey.key);
      }
    if (localKeyReference != null)
      localFontDesignMetrics = (FontDesignMetrics)localKeyReference.get();
    if (localFontDesignMetrics == null)
    {
      localFontDesignMetrics = new FontDesignMetrics(paramFont, paramFontRenderContext);
      if (bool)
      {
        synchronized (metricsCache)
        {
          metricsCache.put(paramFont, new KeyReference(paramFont, localFontDesignMetrics));
        }
      }
      else
      {
        ??? = new MetricsKey(paramFont, paramFontRenderContext);
        synchronized (metricsCache)
        {
          metricsCache.put(???, new KeyReference(???, localFontDesignMetrics));
        }
      }
    }
    for (int i = 0; i < recentMetrics.length; ++i)
      if (recentMetrics[i] == localFontDesignMetrics)
        return localFontDesignMetrics;
    synchronized (recentMetrics)
    {
      recentMetrics[(recentIndex++)] = localFontDesignMetrics;
      if (recentIndex == 5)
        recentIndex = 0;
    }
    return ((FontDesignMetrics)localFontDesignMetrics);
  }

  private FontDesignMetrics(Font paramFont)
  {
    this(paramFont, getDefaultFrc());
  }

  private FontDesignMetrics(Font paramFont, FontRenderContext paramFontRenderContext)
  {
    super(paramFont);
    this.serVersion = 0;
    this.height = -1;
    this.devmatrix = null;
    this.font = paramFont;
    this.frc = paramFontRenderContext;
    this.isAntiAliased = paramFontRenderContext.isAntiAliased();
    this.usesFractionalMetrics = paramFontRenderContext.usesFractionalMetrics();
    this.frcTx = paramFontRenderContext.getTransform();
    this.matrix = new double[4];
    initMatrixAndMetrics();
    initAdvCache();
  }

  private void initMatrixAndMetrics()
  {
    Font2D localFont2D = FontManager.getFont2D(this.font);
    this.fontStrike = localFont2D.getStrike(this.font, this.frc);
    StrikeMetrics localStrikeMetrics = this.fontStrike.getFontMetrics();
    this.ascent = localStrikeMetrics.getAscent();
    this.descent = localStrikeMetrics.getDescent();
    this.leading = localStrikeMetrics.getLeading();
    this.maxAdvance = localStrikeMetrics.getMaxAdvance();
    this.devmatrix = new double[4];
    this.frcTx.getMatrix(this.devmatrix);
  }

  private void initAdvCache()
  {
    this.advCache = new float[256];
    for (int i = 0; i < 256; ++i)
      this.advCache[i] = -1.0F;
  }

  private void readObject(ObjectInputStream paramObjectInputStream)
    throws IOException, ClassNotFoundException
  {
    paramObjectInputStream.defaultReadObject();
    if (this.serVersion != 1)
    {
      this.frc = getDefaultFrc();
      this.isAntiAliased = this.frc.isAntiAliased();
      this.usesFractionalMetrics = this.frc.usesFractionalMetrics();
      this.frcTx = this.frc.getTransform();
    }
    else
    {
      this.frc = new FontRenderContext(this.frcTx, this.isAntiAliased, this.usesFractionalMetrics);
    }
    this.height = -1;
    this.cache = null;
    initMatrixAndMetrics();
    initAdvCache();
  }

  private void writeObject(ObjectOutputStream paramObjectOutputStream)
    throws IOException
  {
    this.cache = new int[256];
    for (int i = 0; i < 256; ++i)
      this.cache[i] = -1;
    this.serVersion = 1;
    paramObjectOutputStream.defaultWriteObject();
    this.cache = null;
  }

  private float handleCharWidth(int paramInt)
  {
    return this.fontStrike.getCodePointAdvance(paramInt);
  }

  private float getLatinCharWidth(char paramChar)
  {
    float f = this.advCache[paramChar];
    if (f == -1.0F)
    {
      f = handleCharWidth(paramChar);
      this.advCache[paramChar] = f;
    }
    return f;
  }

  public FontRenderContext getFontRenderContext()
  {
    return this.frc;
  }

  public int charWidth(char paramChar)
  {
    float f;
    if (paramChar < 256)
      f = getLatinCharWidth(paramChar);
    else
      f = handleCharWidth(paramChar);
    return (int)(0.5D + f);
  }

  public int charWidth(int paramInt)
  {
    if (!(Character.isValidCodePoint(paramInt)))
      paramInt = 65535;
    float f = handleCharWidth(paramInt);
    return (int)(0.5D + f);
  }

  private boolean requiresLayout(char paramChar)
  {
    return (((paramChar >= 1424) && (paramChar < 3712)) || ((paramChar >= 8234) && (paramChar < 8239)) || ((paramChar >= 55296) && (paramChar < 57344)));
  }

  public int stringWidth(String paramString)
  {
    float f = 0F;
    if (this.font.hasLayoutAttributes())
    {
      if (paramString == null)
        throw new NullPointerException("str is null");
      if (paramString.length() == 0)
        return 0;
      f = new TextLayout(paramString, this.font, this.frc).getAdvance();
    }
    else
    {
      int i = paramString.length();
      for (int j = 0; j < i; ++j)
      {
        int k = paramString.charAt(j);
        if (k < 256)
        {
          f += getLatinCharWidth(k);
        }
        else
        {
          if (requiresLayout(k))
          {
            f = new TextLayout(paramString, this.font, this.frc).getAdvance();
            break;
          }
          f += handleCharWidth(k);
        }
      }
    }
    return (int)(0.5D + f);
  }

  public int charsWidth(char[] paramArrayOfChar, int paramInt1, int paramInt2)
  {
    float f = 0F;
    if (this.font.hasLayoutAttributes())
    {
      if (paramInt2 == 0)
        return 0;
      String str1 = new String(paramArrayOfChar, paramInt1, paramInt2);
      f = new TextLayout(str1, this.font, this.frc).getAdvance();
    }
    else
    {
      if (paramInt2 < 0)
        throw new IndexOutOfBoundsException("len=" + paramInt2);
      int i = paramInt1 + paramInt2;
      for (int j = paramInt1; j < i; ++j)
      {
        int k = paramArrayOfChar[j];
        if (k < 256)
        {
          f += getLatinCharWidth(k);
        }
        else
        {
          if (requiresLayout(k))
          {
            String str2 = new String(paramArrayOfChar, paramInt1, paramInt2);
            f = new TextLayout(str2, this.font, this.frc).getAdvance();
            break;
          }
          f += handleCharWidth(k);
        }
      }
    }
    return (int)(0.5D + f);
  }

  public int[] getWidths()
  {
    int[] arrayOfInt = new int[256];
    for (int i = 0; i < 256; i = (char)(i + 1))
    {
      float f = this.advCache[i];
      if (f == -1.0F)
        f = this.advCache[i] = handleCharWidth(i);
      arrayOfInt[i] = (int)(0.5D + f);
    }
    return arrayOfInt;
  }

  public int getMaxAdvance()
  {
    return (int)(0.99000000953674316F + this.maxAdvance);
  }

  public int getAscent()
  {
    return (int)(roundingUpValue + this.ascent);
  }

  public int getDescent()
  {
    return (int)(roundingUpValue + this.descent);
  }

  public int getLeading()
  {
    return ((int)(roundingUpValue + this.descent + this.leading) - (int)(roundingUpValue + this.descent));
  }

  public int getHeight()
  {
    if (this.height < 0)
      this.height = (getAscent() + (int)(roundingUpValue + this.descent + this.leading));
    return this.height;
  }

  private static class KeyReference extends SoftReference
  implements DisposerRecord
  {
    static ReferenceQueue queue = Disposer.getQueue();
    Object key;

    KeyReference(Object paramObject1, Object paramObject2)
    {
      super(paramObject2, queue);
      this.key = paramObject1;
      Disposer.addReference(this, this);
    }

    public void dispose()
    {
      synchronized (FontDesignMetrics.access$000())
      {
        if (FontDesignMetrics.access$000().get(this.key) == this)
          FontDesignMetrics.access$000().remove(this.key);
      }
    }
  }

  private static class MetricsKey
  {
    Font font;
    FontRenderContext frc;
    int hash;
    static final MetricsKey key = new MetricsKey();

    MetricsKey()
    {
    }

    MetricsKey(Font paramFont, FontRenderContext paramFontRenderContext)
    {
      init(paramFont, paramFontRenderContext);
    }

    void init(Font paramFont, FontRenderContext paramFontRenderContext)
    {
      this.font = paramFont;
      this.frc = paramFontRenderContext;
      this.hash = (paramFont.hashCode() + paramFontRenderContext.hashCode());
    }

    public boolean equals(Object paramObject)
    {
      if (!(paramObject instanceof MetricsKey))
        return false;
      return ((this.font.equals(((MetricsKey)paramObject).font)) && (this.frc.equals(((MetricsKey)paramObject).frc)));
    }

    public int hashCode()
    {
      return this.hash;
    }
  }
}