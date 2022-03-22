package sun.font;

import java.awt.geom.Point2D.Float;
import java.lang.ref.SoftReference;
import java.security.AccessController;
import java.util.HashMap;
import sun.security.action.LoadLibraryAction;

public final class SunLayoutEngine
  implements GlyphLayout.LayoutEngine, GlyphLayout.LayoutEngineFactory
{
  private GlyphLayout.LayoutEngineKey key;
  private static GlyphLayout.LayoutEngineFactory instance;
  private SoftReference cacheref = new SoftReference(null);

  private static native void initGVIDs();

  public static GlyphLayout.LayoutEngineFactory instance()
  {
    if (instance == null)
      instance = new SunLayoutEngine();
    return instance;
  }

  private SunLayoutEngine()
  {
  }

  public GlyphLayout.LayoutEngine getEngine(Font2D paramFont2D, int paramInt1, int paramInt2)
  {
    return getEngine(new GlyphLayout.LayoutEngineKey(paramFont2D, paramInt1, paramInt2));
  }

  public GlyphLayout.LayoutEngine getEngine(GlyphLayout.LayoutEngineKey paramLayoutEngineKey)
  {
    HashMap localHashMap = (HashMap)this.cacheref.get();
    if (localHashMap == null)
    {
      localHashMap = new HashMap();
      this.cacheref = new SoftReference(localHashMap);
    }
    Object localObject = (GlyphLayout.LayoutEngineFactory)localHashMap.get(paramLayoutEngineKey);
    if (localObject == null)
    {
      localObject = new SunLayoutEngine(paramLayoutEngineKey.copy());
      localHashMap.put(paramLayoutEngineKey, localObject);
    }
    return ((GlyphLayout.LayoutEngineFactory)localObject);
  }

  private SunLayoutEngine(GlyphLayout.LayoutEngineKey paramLayoutEngineKey)
  {
    this.key = paramLayoutEngineKey;
  }

  public void layout(FontStrikeDesc paramFontStrikeDesc, float[] paramArrayOfFloat, int paramInt1, int paramInt2, TextRecord paramTextRecord, int paramInt3, Point2D.Float paramFloat, GlyphLayout.GVData paramGVData)
  {
    Font2D localFont2D = this.key.font();
    FontStrike localFontStrike = localFont2D.getStrike(paramFontStrikeDesc);
    nativeLayout(localFont2D, localFontStrike, paramArrayOfFloat, paramInt1, paramInt2, paramTextRecord.text, paramTextRecord.start, paramTextRecord.limit, paramTextRecord.min, paramTextRecord.max, this.key.script(), this.key.lang(), paramInt3, paramFloat, paramGVData);
  }

  private static native void nativeLayout(Font2D paramFont2D, FontStrike paramFontStrike, float[] paramArrayOfFloat, int paramInt1, int paramInt2, char[] paramArrayOfChar, int paramInt3, int paramInt4, int paramInt5, int paramInt6, int paramInt7, int paramInt8, int paramInt9, Point2D.Float paramFloat, GlyphLayout.GVData paramGVData);

  static
  {
    AccessController.doPrivileged(new LoadLibraryAction("fontmanager"));
    initGVIDs();
  }
}