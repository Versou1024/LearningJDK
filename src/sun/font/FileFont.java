package sun.font;

import java.awt.FontFormatException;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D.Float;
import java.awt.geom.Rectangle2D.Float;
import java.io.File;
import java.nio.ByteBuffer;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Vector;
import sun.java2d.Disposer;

public abstract class FileFont extends PhysicalFont
{
  protected boolean useJavaRasterizer = true;
  protected int fileSize;
  protected FileFontDisposer disposer;
  protected long pScaler;
  protected boolean checkedNatives;
  protected boolean useNatives;
  protected NativeFont[] nativeFonts;
  protected char[] glyphToCharMap;

  FileFont(String paramString, Object paramObject)
    throws FontFormatException
  {
    super(paramString, paramObject);
  }

  FontStrike createStrike(FontStrikeDesc paramFontStrikeDesc)
  {
    if (!(this.checkedNatives))
      checkUseNatives();
    return new FileFontStrike(this, paramFontStrikeDesc);
  }

  protected boolean checkUseNatives()
  {
    this.checkedNatives = true;
    return this.useNatives;
  }

  protected abstract void close();

  abstract ByteBuffer readBlock(int paramInt1, int paramInt2);

  public boolean canDoStyle(int paramInt)
  {
    return true;
  }

  void setFileToRemove(File paramFile)
  {
    Disposer.addObjectRecord(this, new CreatedFontFileDisposerRecord(paramFile, null));
  }

  static native void freeScaler(long paramLong);

  static synchronized native long getNullScaler();

  synchronized native StrikeMetrics getFontMetrics(long paramLong);

  synchronized native float getGlyphAdvance(long paramLong, int paramInt);

  synchronized native void getGlyphMetrics(long paramLong, int paramInt, Point2D.Float paramFloat);

  synchronized native long getGlyphImage(long paramLong, int paramInt);

  synchronized native Rectangle2D.Float getGlyphOutlineBounds(long paramLong, int paramInt);

  synchronized native GeneralPath getGlyphOutline(long paramLong, int paramInt, float paramFloat1, float paramFloat2);

  synchronized native GeneralPath getGlyphVectorOutline(long paramLong, int[] paramArrayOfInt, int paramInt, float paramFloat1, float paramFloat2);

  protected abstract long getScaler();

  private static class CreatedFontFileDisposerRecord
  implements sun.java2d.DisposerRecord
  {
    File fontFile = null;

    private CreatedFontFileDisposerRecord(File paramFile)
    {
      this.fontFile = paramFile;
    }

    public void dispose()
    {
      AccessController.doPrivileged(new PrivilegedAction(this)
      {
        public Object run()
        {
          if (this.this$0.fontFile != null)
            try
            {
              this.this$0.fontFile.delete();
              FontManager.tmpFontFiles.remove(this.this$0.fontFile);
            }
            catch (Exception localException)
            {
            }
          return null;
        }
      });
    }
  }

  protected static class FileFontDisposer
  implements sun.java2d.DisposerRecord
  {
    long pScaler = 3412046294821109760L;
    boolean disposed = false;

    public FileFontDisposer(long paramLong)
    {
      this.pScaler = paramLong;
    }

    public synchronized void dispose()
    {
      if (!(this.disposed))
      {
        FileFont.freeScaler(this.pScaler);
        this.pScaler = 3412048098707374080L;
        this.disposed = true;
      }
    }
  }
}