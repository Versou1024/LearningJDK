package sun.font;

import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public final class GlyphLayout
{
  private GVData _gvdata = new GVData();
  private static volatile GlyphLayout cache;
  private LayoutEngineFactory _lef;
  private TextRecord _textRecord = new TextRecord();
  private ScriptRun _scriptRuns = new ScriptRun();
  private FontRunIterator _fontRuns = new FontRunIterator();
  private int _ercount;
  private ArrayList _erecords = new ArrayList(10);
  private java.awt.geom.Point2D.Float _pt = new java.awt.geom.Point2D.Float();
  private FontStrikeDesc _sd = new FontStrikeDesc();
  private float[] _mat = new float[4];
  private int _typo_flags;
  private int _offset;

  public static GlyphLayout get(LayoutEngineFactory paramLayoutEngineFactory)
  {
    if (paramLayoutEngineFactory == null)
      paramLayoutEngineFactory = SunLayoutEngine.instance();
    GlyphLayout localGlyphLayout1 = null;
    synchronized (GlyphLayout.class)
    {
      if (cache != null)
      {
        localGlyphLayout1 = cache;
        cache = null;
      }
    }
    if (localGlyphLayout1 == null)
      localGlyphLayout1 = new GlyphLayout();
    localGlyphLayout1._lef = paramLayoutEngineFactory;
    return localGlyphLayout1;
  }

  public static void done(GlyphLayout paramGlyphLayout)
  {
    paramGlyphLayout._lef = null;
    cache = paramGlyphLayout;
  }

  public StandardGlyphVector layout(Font paramFont, FontRenderContext paramFontRenderContext, char[] paramArrayOfChar, int paramInt1, int paramInt2, int paramInt3, StandardGlyphVector paramStandardGlyphVector)
  {
    if ((paramArrayOfChar == null) || (paramInt1 < 0) || (paramInt2 < 0) || (paramInt2 > paramArrayOfChar.length - paramInt1))
      throw new IllegalArgumentException();
    init(paramInt2);
    if (paramFont.hasLayoutAttributes())
    {
      localObject1 = ((AttributeMap)paramFont.getAttributes()).getValues();
      if (((AttributeValues)localObject1).getKerning() != 0)
        this._typo_flags |= 1;
      if (((AttributeValues)localObject1).getLigatures() != 0)
        this._typo_flags |= 2;
    }
    this._offset = paramInt1;
    Object localObject1 = SDCache.get(paramFont, paramFontRenderContext);
    this._mat[0] = (float)((SDCache)localObject1).gtx.getScaleX();
    this._mat[1] = (float)((SDCache)localObject1).gtx.getShearY();
    this._mat[2] = (float)((SDCache)localObject1).gtx.getShearX();
    this._mat[3] = (float)((SDCache)localObject1).gtx.getScaleY();
    this._pt.setLocation(((SDCache)localObject1).delta);
    int i = paramInt1 + paramInt2;
    int j = 0;
    int k = paramArrayOfChar.length;
    if (paramInt3 != 0)
    {
      if ((paramInt3 & 0x1) != 0)
        this._typo_flags |= -2147483648;
      if ((paramInt3 & 0x2) != 0)
        j = paramInt1;
      if ((paramInt3 & 0x4) != 0)
        k = i;
    }
    int l = -1;
    Font2D localFont2D = FontManager.getFont2D(paramFont);
    this._textRecord.init(paramArrayOfChar, paramInt1, i, j, k);
    int i1 = paramInt1;
    if (localFont2D instanceof CompositeFont)
    {
      this._scriptRuns.init(paramArrayOfChar, paramInt1, paramInt2);
      this._fontRuns.init((CompositeFont)localFont2D, paramArrayOfChar, paramInt1, i);
      while (true)
      {
        if (!(this._scriptRuns.next()))
          break label478;
        i2 = this._scriptRuns.getScriptLimit();
        i3 = this._scriptRuns.getScriptCode();
        while (this._fontRuns.next(i3, i2))
        {
          PhysicalFont localPhysicalFont = this._fontRuns.getFont();
          if (localPhysicalFont instanceof NativeFont)
            localPhysicalFont = ((NativeFont)localPhysicalFont).getDelegateFont();
          int i5 = this._fontRuns.getGlyphMask();
          int i6 = this._fontRuns.getPos();
          nextEngineRecord(i1, i6, i3, l, localPhysicalFont, i5);
          i1 = i6;
        }
      }
    }
    this._scriptRuns.init(paramArrayOfChar, paramInt1, paramInt2);
    while (this._scriptRuns.next())
    {
      i2 = this._scriptRuns.getScriptLimit();
      i3 = this._scriptRuns.getScriptCode();
      nextEngineRecord(i1, i2, i3, l, localFont2D, 0);
      i1 = i2;
    }
    label478: int i2 = 0;
    int i3 = this._ercount;
    int i4 = 1;
    if (this._typo_flags < 0)
    {
      i2 = i3 - 1;
      i3 = -1;
      i4 = -1;
    }
    this._sd = ((SDCache)localObject1).sd;
    while (i2 != i3)
    {
      localObject2 = (EngineRecord)this._erecords.get(i2);
      try
      {
        ((EngineRecord)localObject2).layout();
      }
      catch (IndexOutOfBoundsException localIndexOutOfBoundsException)
      {
        while (true)
          this._gvdata.grow();
      }
      i2 += i4;
    }
    Object localObject2 = this._gvdata.createGlyphVector(paramFont, paramFontRenderContext, paramStandardGlyphVector);
    return ((StandardGlyphVector)(StandardGlyphVector)localObject2);
  }

  private void init(int paramInt)
  {
    this._typo_flags = 0;
    this._ercount = 0;
    this._gvdata.init(paramInt);
  }

  private void nextEngineRecord(int paramInt1, int paramInt2, int paramInt3, int paramInt4, Font2D paramFont2D, int paramInt5)
  {
    EngineRecord localEngineRecord = null;
    if (this._ercount == this._erecords.size())
    {
      localEngineRecord = new EngineRecord(this);
      this._erecords.add(localEngineRecord);
    }
    else
    {
      localEngineRecord = (EngineRecord)this._erecords.get(this._ercount);
    }
    localEngineRecord.init(paramInt1, paramInt2, paramFont2D, paramInt3, paramInt4, paramInt5);
    this._ercount += 1;
  }

  private final class EngineRecord
  {
    private int start;
    private int limit;
    private int gmask;
    private int eflags;
    private GlyphLayout.LayoutEngineKey key = new GlyphLayout.LayoutEngineKey();
    private GlyphLayout.LayoutEngine engine;

    void init(, int paramInt2, Font2D paramFont2D, int paramInt3, int paramInt4, int paramInt5)
    {
      this.start = paramInt1;
      this.limit = paramInt2;
      this.gmask = paramInt5;
      this.key.init(paramFont2D, paramInt3, paramInt4);
      this.eflags = 0;
      for (int i = paramInt1; i < paramInt2; ++i)
      {
        int j = GlyphLayout.access$000(this.this$0).text[i];
        if ((Character.isHighSurrogate((char)j)) && (i < paramInt2 - 1) && (Character.isLowSurrogate(GlyphLayout.access$000(this.this$0).text[(i + 1)])))
          j = Character.toCodePoint((char)j, GlyphLayout.access$000(this.this$0).text[(++i)]);
        int k = Character.getType(j);
        if ((k == 6) || (k == 7) || (k == 8))
        {
          this.eflags = 4;
          break;
        }
      }
      this.engine = GlyphLayout.access$100(this.this$0).getEngine(this.key);
    }

    void layout()
    {
      GlyphLayout.access$000(this.this$0).start = this.start;
      GlyphLayout.access$000(this.this$0).limit = this.limit;
      this.engine.layout(GlyphLayout.access$200(this.this$0), GlyphLayout.access$300(this.this$0), this.gmask, this.start - GlyphLayout.access$400(this.this$0), GlyphLayout.access$000(this.this$0), GlyphLayout.access$500(this.this$0) | this.eflags, GlyphLayout.access$600(this.this$0), GlyphLayout.access$700(this.this$0));
    }
  }

  public static final class GVData
  {
    public int _count;
    public int _flags;
    public int[] _glyphs;
    public float[] _positions;
    public int[] _indices;
    private static final int UNINITIALIZED_FLAGS = -1;

    public void init(int paramInt)
    {
      this._count = 0;
      this._flags = -1;
      if ((this._glyphs == null) || (this._glyphs.length < paramInt))
      {
        if (paramInt < 20)
          paramInt = 20;
        this._glyphs = new int[paramInt];
        this._positions = new float[paramInt * 2 + 2];
        this._indices = new int[paramInt];
      }
    }

    public void grow()
    {
      grow(this._glyphs.length / 4);
    }

    public void grow(int paramInt)
    {
      int i = this._glyphs.length + paramInt;
      int[] arrayOfInt1 = new int[i];
      System.arraycopy(this._glyphs, 0, arrayOfInt1, 0, this._count);
      this._glyphs = arrayOfInt1;
      float[] arrayOfFloat = new float[i * 2 + 2];
      System.arraycopy(this._positions, 0, arrayOfFloat, 0, this._count * 2 + 2);
      this._positions = arrayOfFloat;
      int[] arrayOfInt2 = new int[i];
      System.arraycopy(this._indices, 0, arrayOfInt2, 0, this._count);
      this._indices = arrayOfInt2;
    }

    public void adjustPositions(AffineTransform paramAffineTransform)
    {
      paramAffineTransform.transform(this._positions, 0, this._positions, 0, this._count);
    }

    public StandardGlyphVector createGlyphVector(Font paramFont, FontRenderContext paramFontRenderContext, StandardGlyphVector paramStandardGlyphVector)
    {
      if (this._flags == -1)
      {
        this._flags = 0;
        if (this._count > 1)
        {
          int i = 1;
          int j = 1;
          int k = this._count;
          for (int l = 0; (l < this._count) && (((i != 0) || (j != 0))); ++l)
          {
            int i1 = this._indices[l];
            i = ((i != 0) && (i1 == l)) ? 1 : 0;
            j = ((j != 0) && (i1 == --k)) ? 1 : 0;
          }
          if (j != 0)
            this._flags |= 4;
          if ((j == 0) && (i == 0))
            this._flags |= 8;
        }
        this._flags |= 2;
      }
      int[] arrayOfInt1 = new int[this._count];
      System.arraycopy(this._glyphs, 0, arrayOfInt1, 0, this._count);
      float[] arrayOfFloat = null;
      if ((this._flags & 0x2) != 0)
      {
        arrayOfFloat = new float[this._count * 2 + 2];
        System.arraycopy(this._positions, 0, arrayOfFloat, 0, arrayOfFloat.length);
      }
      int[] arrayOfInt2 = null;
      if ((this._flags & 0x8) != 0)
      {
        arrayOfInt2 = new int[this._count];
        System.arraycopy(this._indices, 0, arrayOfInt2, 0, this._count);
      }
      if (paramStandardGlyphVector == null)
        paramStandardGlyphVector = new StandardGlyphVector(paramFont, paramFontRenderContext, arrayOfInt1, arrayOfFloat, arrayOfInt2, this._flags);
      else
        paramStandardGlyphVector.initGlyphVector(paramFont, paramFontRenderContext, arrayOfInt1, arrayOfFloat, arrayOfInt2, this._flags);
      return paramStandardGlyphVector;
    }
  }

  public static abstract interface LayoutEngine
  {
    public abstract void layout(FontStrikeDesc paramFontStrikeDesc, float[] paramArrayOfFloat, int paramInt1, int paramInt2, TextRecord paramTextRecord, int paramInt3, java.awt.geom.Point2D.Float paramFloat, GlyphLayout.GVData paramGVData);
  }

  public static abstract interface LayoutEngineFactory
  {
    public abstract GlyphLayout.LayoutEngine getEngine(Font2D paramFont2D, int paramInt1, int paramInt2);

    public abstract GlyphLayout.LayoutEngine getEngine(GlyphLayout.LayoutEngineKey paramLayoutEngineKey);
  }

  public static final class LayoutEngineKey
  {
    private Font2D font;
    private int script;
    private int lang;

    LayoutEngineKey()
    {
    }

    LayoutEngineKey(Font2D paramFont2D, int paramInt1, int paramInt2)
    {
      init(paramFont2D, paramInt1, paramInt2);
    }

    void init(Font2D paramFont2D, int paramInt1, int paramInt2)
    {
      this.font = paramFont2D;
      this.script = paramInt1;
      this.lang = paramInt2;
    }

    LayoutEngineKey copy()
    {
      return new LayoutEngineKey(this.font, this.script, this.lang);
    }

    Font2D font()
    {
      return this.font;
    }

    int script()
    {
      return this.script;
    }

    int lang()
    {
      return this.lang;
    }

    public boolean equals(Object paramObject)
    {
      if (this == paramObject)
        return true;
      if (paramObject == null)
        return false;
      try
      {
        LayoutEngineKey localLayoutEngineKey = (LayoutEngineKey)paramObject;
        return ((this.script == localLayoutEngineKey.script) && (this.lang == localLayoutEngineKey.lang) && (this.font.equals(localLayoutEngineKey.font)));
      }
      catch (ClassCastException localClassCastException)
      {
      }
      return false;
    }

    public int hashCode()
    {
      return (this.script ^ this.lang ^ this.font.hashCode());
    }
  }

  private static final class SDCache
  {
    public Font key_font;
    public FontRenderContext key_frc;
    public AffineTransform dtx;
    public AffineTransform invdtx;
    public AffineTransform gtx;
    public java.awt.geom.Point2D.Float delta;
    public FontStrikeDesc sd;
    private static final java.awt.geom.Point2D.Float ZERO_DELTA = new java.awt.geom.Point2D.Float();
    private static SoftReference<ConcurrentHashMap<SDKey, SDCache>> cacheRef;

    private SDCache(Font paramFont, FontRenderContext paramFontRenderContext)
    {
      this.key_font = paramFont;
      this.key_frc = paramFontRenderContext;
      this.dtx = paramFontRenderContext.getTransform();
      this.dtx.setTransform(this.dtx.getScaleX(), this.dtx.getShearY(), this.dtx.getShearX(), this.dtx.getScaleY(), 0D, 0D);
      if (!(this.dtx.isIdentity()))
        try
        {
          this.invdtx = this.dtx.createInverse();
        }
        catch (NoninvertibleTransformException localNoninvertibleTransformException)
        {
          throw new InternalError();
        }
      float f = paramFont.getSize2D();
      if (paramFont.isTransformed())
      {
        this.gtx = paramFont.getTransform();
        this.gtx.scale(f, f);
        this.delta = new java.awt.geom.Point2D.Float((float)this.gtx.getTranslateX(), (float)this.gtx.getTranslateY());
        this.gtx.setTransform(this.gtx.getScaleX(), this.gtx.getShearY(), this.gtx.getShearX(), this.gtx.getScaleY(), 0D, 0D);
        this.gtx.preConcatenate(this.dtx);
      }
      else
      {
        this.delta = ZERO_DELTA;
        this.gtx = new AffineTransform(this.dtx);
        this.gtx.scale(f, f);
      }
      int i = FontStrikeDesc.getAAHintIntVal(paramFontRenderContext.getAntiAliasingHint(), FontManager.getFont2D(paramFont), (int)Math.abs(f));
      int j = FontStrikeDesc.getFMHintIntVal(paramFontRenderContext.getFractionalMetricsHint());
      this.sd = new FontStrikeDesc(this.dtx, this.gtx, paramFont.getStyle(), i, j);
    }

    public static SDCache get(Font paramFont, FontRenderContext paramFontRenderContext)
    {
      if (paramFontRenderContext.isTransformed())
      {
        localObject = paramFontRenderContext.getTransform();
        if ((((AffineTransform)localObject).getTranslateX() != 0D) || (((AffineTransform)localObject).getTranslateY() != 0D))
        {
          localObject = new AffineTransform(((AffineTransform)localObject).getScaleX(), ((AffineTransform)localObject).getShearY(), ((AffineTransform)localObject).getShearX(), ((AffineTransform)localObject).getScaleY(), 0D, 0D);
          paramFontRenderContext = new FontRenderContext((AffineTransform)localObject, paramFontRenderContext.getAntiAliasingHint(), paramFontRenderContext.getFractionalMetricsHint());
        }
      }
      Object localObject = new SDKey(paramFont, paramFontRenderContext);
      ConcurrentHashMap localConcurrentHashMap = null;
      SDCache localSDCache = null;
      if (cacheRef != null)
      {
        localConcurrentHashMap = (ConcurrentHashMap)cacheRef.get();
        if (localConcurrentHashMap != null)
          localSDCache = (SDCache)localConcurrentHashMap.get(localObject);
      }
      if (localSDCache == null)
      {
        localSDCache = new SDCache(paramFont, paramFontRenderContext);
        if (localConcurrentHashMap == null)
        {
          localConcurrentHashMap = new ConcurrentHashMap(10);
          cacheRef = new SoftReference(localConcurrentHashMap);
        }
        localConcurrentHashMap.put(localObject, localSDCache);
      }
      return ((SDCache)localSDCache);
    }

    private static final class SDKey
    {
      private final Font font;
      private final FontRenderContext frc;
      private final int hash;

      SDKey(Font paramFont, FontRenderContext paramFontRenderContext)
      {
        this.font = paramFont;
        this.frc = paramFontRenderContext;
        this.hash = (paramFont.hashCode() ^ paramFontRenderContext.hashCode());
      }

      public int hashCode()
      {
        return this.hash;
      }

      public boolean equals(Object paramObject)
      {
        SDKey localSDKey;
        try
        {
          localSDKey = (SDKey)paramObject;
          return ((this.hash == localSDKey.hash) && (this.font.equals(localSDKey.font)) && (this.frc.equals(localSDKey.frc)));
        }
        catch (ClassCastException localClassCastException)
        {
        }
        return false;
      }
    }
  }
}