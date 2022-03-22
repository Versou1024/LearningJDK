package sun.java2d.pipe;

import java.awt.AlphaComposite;
import java.awt.Composite;
import sun.font.GlyphList;
import sun.java2d.SunGraphics2D;

public abstract class BufferedTextPipe extends GlyphListPipe
{
  private static final int BYTES_PER_GLYPH_IMAGE = 8;
  private static final int BYTES_PER_GLYPH_POSITION = 8;
  private static final int OFFSET_CONTRAST = 8;
  private static final int OFFSET_RGBORDER = 2;
  private static final int OFFSET_SUBPIXPOS = 1;
  private static final int OFFSET_POSITIONS = 0;
  protected final RenderQueue rq;

  private static int createPackedParams(SunGraphics2D paramSunGraphics2D, GlyphList paramGlyphList)
  {
    return (((paramGlyphList.usePositions()) ? 1 : 0) << 0 | ((paramGlyphList.isSubPixPos()) ? 1 : 0) << 1 | ((paramGlyphList.isRGBOrder()) ? 1 : 0) << 2 | (paramSunGraphics2D.lcdTextContrast & 0xFF) << 8);
  }

  protected BufferedTextPipe(RenderQueue paramRenderQueue)
  {
    this.rq = paramRenderQueue;
  }

  protected void drawGlyphList(SunGraphics2D paramSunGraphics2D, GlyphList paramGlyphList)
  {
    Object localObject1 = paramSunGraphics2D.composite;
    if (localObject1 == AlphaComposite.Src)
      localObject1 = AlphaComposite.SrcOver;
    this.rq.lock();
    try
    {
      validateContext(paramSunGraphics2D, (Composite)localObject1);
      enqueueGlyphList(paramSunGraphics2D, paramGlyphList);
    }
    finally
    {
      this.rq.unlock();
    }
  }

  private void enqueueGlyphList(SunGraphics2D paramSunGraphics2D, GlyphList paramGlyphList)
  {
    RenderBuffer localRenderBuffer = this.rq.getBuffer();
    int i = paramGlyphList.getNumGlyphs();
    int j = i * 8;
    int k = (paramGlyphList.usePositions()) ? i * 8 : 0;
    int l = 24 + j + k;
    long[] arrayOfLong = paramGlyphList.getImages();
    float f1 = paramGlyphList.getX() + 0.5F;
    float f2 = paramGlyphList.getY() + 0.5F;
    this.rq.addReference(paramGlyphList.getStrike());
    if (l <= localRenderBuffer.capacity())
    {
      if (l > localRenderBuffer.remaining())
        this.rq.flushNow();
      this.rq.ensureAlignment(20);
      localRenderBuffer.putInt(40);
      localRenderBuffer.putInt(i);
      localRenderBuffer.putInt(createPackedParams(paramSunGraphics2D, paramGlyphList));
      localRenderBuffer.putFloat(f1);
      localRenderBuffer.putFloat(f2);
      localRenderBuffer.put(arrayOfLong, 0, i);
      if (paramGlyphList.usePositions())
      {
        float[] arrayOfFloat = paramGlyphList.getPositions();
        localRenderBuffer.put(arrayOfFloat, 0, 2 * i);
      }
    }
    else
    {
      this.rq.flushAndInvokeNow(new Runnable(this, i, paramGlyphList, paramSunGraphics2D, f1, f2, arrayOfLong)
      {
        public void run()
        {
          this.this$0.drawGlyphList(this.val$totalGlyphs, this.val$gl.usePositions(), this.val$gl.isSubPixPos(), this.val$gl.isRGBOrder(), this.val$sg2d.lcdTextContrast, this.val$glyphListOrigX, this.val$glyphListOrigY, this.val$images, this.val$gl.getPositions());
        }
      });
    }
  }

  protected abstract void drawGlyphList(int paramInt1, boolean paramBoolean1, boolean paramBoolean2, boolean paramBoolean3, int paramInt2, float paramFloat1, float paramFloat2, long[] paramArrayOfLong, float[] paramArrayOfFloat);

  protected abstract void validateContext(SunGraphics2D paramSunGraphics2D, Composite paramComposite);
}